package com.report_design_lib.dynamic

import com.report_design_lib.core.RendererRegistry
import com.report_design_lib.core.Report
import com.report_design_lib.core.elements.*
import com.report_design_lib.core.elements.bands.Bands
import com.report_design_lib.core.elements.bands.ColumnHeaderBand
import com.report_design_lib.core.elements.bands.DetailBand
import com.report_design_lib.core.elements.bands.SummaryBand
import com.report_design_lib.core.elements.bands.TitleBand
import com.report_design_lib.interfaces.DataSourceConfig

class FluentReportBuilder {
    private final Report report

    private FluentReportBuilder(String name) {
        this.report = new Report(name)
    }

    static FluentReportBuilder report(String name) {
        new FluentReportBuilder(name)
    }

    FluentReportBuilder columns(ColumnBuilder... cols) {
        def fields = report.fields ?: new Fields()
        def bands = report.bands ?: new Bands()
        def detail = bands.detail ?: new DetailBand()
        def header = bands.columnHeader ?: new ColumnHeaderBand()

        cols.each { col ->
            fields.field(col.fieldName, col.type)
            header.label(text: col.title, width: col.width ?: 100f)
            detail.textField(expression: "\$F{${col.fieldName}}", width: col.width ?: 100f)
        }

        bands.detail = detail
        bands.columnHeader = header
        report.fields = fields
        report.bands = bands
        this
    }

    FluentReportBuilder title(String text) {
        report.title = text
        def bands = report.bands ?: new Bands()
        def tb = bands.titleBand ?: new TitleBand()
        tb.label(text: text, width: 500f)
        bands.titleBand = tb
        report.bands = bands
        this
    }

    FluentReportBuilder pageFooter(String expression) {
        def bands = report.bands ?: new Bands()
        def summary = bands.summary ?: new SummaryBand()
        summary.textField(expression: expression, width: 200f)
        bands.summary = summary
        report.bands = bands
        this
    }

    FluentReportBuilder setColumnTitleStyle(StyleBuilder style) {
        report.styles = report.styles ?: new Styles()
        report.styles.style("columnTitle", { s ->
            s.font = new Font(
                    size: style.font.size,
                    isBold: style.font.isBold,
                    isItalic: style.font.isItalic
            )
            s.horizontalAlignment = style.alignment
            s.background = style.background
        })
        this
    }

    FluentReportBuilder setDataSource(DataSourceConfig cfg) {
        report.dataSource = cfg
        this
    }

    void toPdf(OutputStream out, Map params = [:]) { RendererRegistry.get('pdf').render(report, params, out) }
    void toXls(OutputStream out, Map params = [:]) { RendererRegistry.get('xls').render(report, params, out) }
    void toHtml(OutputStream out, Map params = [:]) { RendererRegistry.get('html').render(report, params, out) }
    void toJson(OutputStream out, Map params = [:]) { RendererRegistry.get('json').render(report, params, out) }

    Report build() { report }
}
