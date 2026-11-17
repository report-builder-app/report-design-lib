package com.report_design_lib.core.render.support

import com.report_design_lib.core.Report
import com.report_design_lib.core.data_source.DataSourceRegistry
import com.report_design_lib.core.elements.*

/**
 * Разворачивает бэнды отчёта в последовательность (row, element) с готовыми значениями.
 * Никаких PDF/Excel/HTML типов — только нейтральные значения.
 */
class ReportFlattener {

    static class Cell {
        String style
        Float width
        Float height
        Object value
        boolean isLine
    }

    static class Row {
        BandType band
        List<Cell> cells = []
    }

    static List<Row> flatten(Report report, Map<String, Object> runtimeParams) {
        def params = resolveParams(report, runtimeParams)
        def result = []

        def title = report.bands?.titleBand
        if (title) result << toRow(BandType.TITLE, title, null, params)

        def header = report.bands?.columnHeader
        if (header) result << toRow(BandType.COLUMN_HEADER, header, null, params)

        def detail = report.bands?.detail
        if (detail) {
            def ds = report.dataSource
                    ? DataSourceRegistry.create(report.dataSource, params)
                    : new com.report_design_lib.core.data_source.CollectionReportDataSource([])
            while (ds.next()) {
                result << toRow(BandType.DETAIL, detail, ds.row as Map, params)
            }
        }

        def summary = report.bands?.summary
        if (summary) result << toRow(BandType.SUMMARY, summary, null, params)

        result
    }

    private static Row toRow(BandType type, Band band, Map data, Map params) {
        def row = new Row(band: type)
        band.elements.each { el ->
            row.cells << toCell(el, data, params)
        }
        row
    }

    private static Cell toCell(ReportElement el, Map data, Map params) {
        switch (el) {
            case Label:
                def l = el as Label
                return new Cell(style: l.style, width: l.width, height: l.calculateHeight(), value: l.text)
            case TextField:
                def f = el as TextField
                return new Cell(style: f.style, width: f.width, height: f.calculateHeight(),
                        value: resolveExpression(f.expression, data, params))
            case Line:
                def ln = el as Line
                return new Cell(style: ln.style, width: ln.width, height: ln.calculateHeight(),
                        value: null, isLine: true)
            default:
                throw new IllegalArgumentException("Unsupported element: ${el.class}")
        }
    }

    private static Map<String, Object> resolveParams(Report report, Map<String, Object> runtime) {
        def base = report.parameters?.list?.collectEntries {
            [(it.name): runtime[it.name] ?: it.defaultValue]
        } ?: [:]
        base + (runtime ?: [:])
    }

    private static Object resolveExpression(String expr, Map data, Map params) {
        if (!expr) return null
        def m = (expr =~ /^\$(F|P)\{(\w+)\}$/)
        if (m.matches()) {
            return m.group(1) == 'F' ? data?.get(m.group(2)) : params?.get(m.group(2))
        }
        expr
    }
}
