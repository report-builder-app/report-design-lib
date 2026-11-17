package com.report_design_lib.renderer.html

import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.data_source.DataSourceRegistry
import spock.lang.Specification

class HtmlRendererSpec extends Specification {
    def "рендерит HTML с заголовком и таблицей"() {
        given:
        def report = ReportDsl.report("R") {
            title "Hello"
            bands {
                columnHeader { label(text: "ID", width: 50f) }
                detail       { textField(expression: "\$F{id}", width: 50f) }
            }
            dataSource { custom('static') { option 'rows', [[id: 1], [id: 2]] } }
        }
        DataSourceRegistry.register(staticProvider())
        def out = new ByteArrayOutputStream()

        when:
        new HtmlRenderer().render(report, [:], out)

        then:
        def html = out.toString('UTF-8')
        html.contains('<title>Hello</title>')
        html.contains('COLUMN_HEADER')
        html.contains('<td>1</td>')
        html.contains('<td>2</td>')
    }

    static StaticProvider staticProvider() {
        new StaticProvider()
    }
}
