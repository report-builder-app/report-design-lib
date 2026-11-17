package com.report_design_lib.dynamic

import com.report_design_lib.core.RendererRegistry
import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import spock.lang.Specification

class FluentReportBuilderSpec extends Specification {
    static class FakePdfRenderer implements ReportRenderer {
        String getFormat() { 'pdf' }
        String getContentType() { 'application/pdf' }
        String getDefaultExtension() { 'pdf' }
        void render(Report r, Map p, OutputStream out) { out << 'FAKE-PDF'.bytes }
    }

    def "строит отчёт с колонками и заголовком"() {
        when:
        def report = FluentReportBuilder.report("R")
                .columns(
                        ColumnBuilder.column("Name", "name", String),
                        ColumnBuilder.column("Age", "age", Integer)
                )
                .title("People")
                .build()

        then:
        report.name == "R"
        report.title == "People"
        report.fields.size() == 2
        report.fields[0].name == "name"
        report.bands.detail.elements.size() == 2
        report.bands.columnHeader.elements.size() == 2
    }

    def setup() {
        RendererRegistry.clear()
        RendererRegistry.register(new FakePdfRenderer())
    }

    def cleanup() {
        RendererRegistry.clear()
    }

    def "toPdf делегирует в RendererRegistry"() {
        given:
        def out = new ByteArrayOutputStream()

        when:
        FluentReportBuilder.report("R")
                .columns(ColumnBuilder.column("X", "x", String))
                .toPdf(out)

        then:
        out.toString() == 'FAKE-PDF'
    }
}
