package com.report_design_lib.core.render.support

import com.report_design_lib.core.Report
import com.report_design_lib.core.data_source.CollectionReportDataSource
import com.report_design_lib.core.data_source.DataSourceRegistry
import com.report_design_lib.core.elements.*
import com.report_design_lib.core.elements.bands.Band
import com.report_design_lib.interfaces.ReportDataSource

/**
 * Разворачивает бэнды отчёта в последовательность (row, element) с готовыми значениями.
 * Никаких PDF/Excel/HTML типов — только нейтральные значения.
 */
class ReportFlattener {
    // ---------- модель результата ----------

    static class Cell {
        String style
        Float width
        Float height
        Object value
        boolean isLine
        List<Row> subRows           // непустой для SubreportElement
    }

    static class Row {
        BandType band
        List<Cell> cells = []
    }

    // ---------- защита от циклов ----------

    private static final ThreadLocal<Set<String>> STACK =
            ThreadLocal.withInitial { new LinkedHashSet<String>() }

    // ---------- публичный вход ----------

    static List<Row> flatten(Report report,
                             Map<String, Object> params,
                             ReportDataSource explicitDs = null) {
        def stack = STACK.get()
        if (!stack.add(report.name)) {
            throw new IllegalStateException(
                    "Cyclic subreport reference: ${stack.join(' → ')} → ${report.name}")
        }
        try {
            return doFlatten(report, params ?: [:], explicitDs)
        } finally {
            stack.remove(report.name)
        }
    }

    // ---------- основной обход ----------

    private static List<Row> doFlatten(Report report,
                                       Map<String, Object> runtimeParams,
                                       ReportDataSource explicitDs) {
        def params = mergeParams(report, runtimeParams)
        def bands  = report.bands
        if (bands == null) return []

        def rows = []

        // title
        if (bands.titleBand) {
            rows << toRow(BandType.TITLE, bands.titleBand, null, params)
        }

        // columnHeader
        if (bands.columnHeader) {
            rows << toRow(BandType.COLUMN_HEADER, bands.columnHeader, null, params)
        }

        // detail — по одной строке данных на каждый элемент detail
        if (bands.detail) {
            def ds = explicitDs
                    ?: (report.dataSource
                    ? DataSourceRegistry.create(report.dataSource, params)
                    : new CollectionReportDataSource([]))

            int index = 0
            while (ds.next()) {
                def data = ds.row as Map
                data = new LinkedHashMap(data ?: [:])
                data['REPORT_COUNT'] = index + 1     // для $V{REPORT_COUNT}
                rows << toRow(BandType.DETAIL, bands.detail, data, params)
                index++
            }
        }

        // summary
        if (bands.summary) {
            rows << toRow(BandType.SUMMARY, bands.summary, null, params)
        }

        return rows
    }

    // ---------- строка из band ----------

    private static Row toRow(BandType type, Band band, Map data, Map params) {
        def row = new Row(band: type)
        band.elements.each { el ->
            def cell = toCell(el, data, params)
            if (cell != null) row.cells << cell
        }
        row
    }

    // ---------- ячейка из элемента ----------

    private static Cell toCell(ReportElement el, Map data, Map params) {
        switch (el) {
            case Label:
                def l = el as Label
                return new Cell(
                        style:  l.style,
                        width:  l.width,
                        height: l.calculateHeight(),
                        value:  resolveExpression(l.text, data, params)   // ← резолвить
                )
            case TextField:
                def f = el as TextField
                return new Cell(
                        style:  f.style,
                        width:  f.width,
                        height: f.calculateHeight(),
                        value:  resolveExpression(f.expression, data, params)
                )
            case Line:
                def ln = el as Line
                return new Cell(
                        style:  ln.style,
                        width:  ln.width,
                        height: ln.calculateHeight(),
                        isLine: true
                )
            case SubreportElement:
                def sr = el as SubreportElement
                return new Cell(
                        style:  sr.style,
                        width:  sr.width,
                        height: sr.height,
                        subRows: expandSubreport(sr, data, params)
                )

            default:
                throw new IllegalArgumentException(
                        "Unsupported element: ${el.class.name}")
        }
    }

    // ---------- подотчёт ----------

    private static List<Row> expandSubreport(SubreportElement sr, Map data, Map params) {
        def sub = sr.resolveReport()
        def subParams = new LinkedHashMap(params)
        subParams.putAll(sr.parameters ?: [:])
        if (sr.inheritData && data) {
            // Пробрасываем поля текущей строки как параметры подотчёта
            data.each { k, v ->
                if (!k.toString().startsWith('_')) subParams[k] = v
            }
        }

        // subreport может иметь свой datasource, либо explicitDs = null →
        // doFlatten внутри flatten() сам создаст его через DataSourceRegistry
        return flatten(sub, subParams, null)
    }

    // ---------- параметры ----------

    private static Map<String, Object> mergeParams(Report report, Map<String, Object> runtime) {
        def base = [:]
        report.parameters?.list?.each { p ->
            def provided = runtime?.containsKey(p.name) ? runtime[p.name] : null
            base[p.name] = provided != null ? provided : p.defaultValue
        }
        if (runtime) base.putAll(runtime)
        base
    }

    // ---------- выражения ----------
    private static final java.util.regex.Pattern EXPR_PATTERN =
            java.util.regex.Pattern.compile('^\\$(F|P|V)\\{([A-Za-z_][A-Za-z0-9_]*)\\}$')

    private static Object resolveExpression(String expr, Map data, Map params) {
        if (expr == null) return null
        def s = expr.toString().trim()
        if (!s) return s

        def m = EXPR_PATTERN.matcher(s)
        if (m.matches()) {
            def kind = m.group(1)
            def name = m.group(2)
            switch (kind) {
                case 'F': return data?.get(name)
                case 'P': return params?.get(name)
                case 'V': return data?.get(name) ?: data?.get("V_$name")
            }
        }

        if (s.contains('$')) {
            return s.replaceAll(/\$(?:F|P|V)\{([A-Za-z_][A-Za-z0-9_]*)\}/) { full, name ->
                def v = data?.get(name) ?: params?.get(name)
                v?.toString() ?: ''
            }
        }

        return s
    }
}
