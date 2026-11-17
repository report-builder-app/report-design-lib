package com.report_design_lib

import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.elements.Label
import com.report_design_lib.core.elements.Line
import com.report_design_lib.core.elements.TextField
import spock.lang.Specification
import com.report_design_lib.core.elements.PageFormat
import com.report_design_lib.core.elements.PageOrientation

class ReportDslSpec extends Specification {

    def "создаёт отчёт с name из аргумента"() {
        when:
        def report = ReportDsl.report("MyReport") { }

        then:
        report.name == "MyReport"
        report.title == null
        report.parameters == null
        report.fields == null
        report.styles == null
        report.bands == null
        report.dataSource == null
    }

    def "устанавливает title, pageFormat и orientation"() {
        when:
        def report = ReportDsl.report("R") {
            title "Customer List"
            pageFormat PageFormat.A4
            orientation PageOrientation.LANDSCAPE
        }

        then:
        report.title == "Customer List"
        report.pageFormat == PageFormat.A4
        report.orientation == PageOrientation.LANDSCAPE
    }

    def "значения по умолчанию для pageFormat/orientation"() {
        when:
        def report = ReportDsl.report("R") { }

        then:
        report.pageFormat == PageFormat.A4
        report.orientation == PageOrientation.PORTRAIT
    }

    def "bands содержат единый список элементов"() {
        when:
        def report = ReportDsl.report("R") {
            bands {
                titleBand    { label(text: "Customers", width: 500) }
                columnHeader { label(text: "ID", width: 50); label(text: "Name", width: 200) }
                detail {
                    label(text: "Name: ", width: 50)
                    textField(expression: "\$F{name}", width: 200)
                }
                summary {
                    line(y: 0)
                    textField(expression: "\$V{REPORT_COUNT}", width: 100)
                }
            }
        }

        then:
        report.bands.size() == 4
        report.bands.titleBand.elements.size() == 1
        report.bands.columnHeader.elements.size() == 2
        report.bands.detail.elements.size() == 2
        report.bands.summary.elements.size() == 2

        and: "типы элементов корректны"
        report.bands.titleBand.elements[0] instanceof Label
        report.bands.detail.elements[0] instanceof Label
        report.bands.detail.elements[1] instanceof TextField
        report.bands.summary.elements[0] instanceof Line
        report.bands.summary.elements[1] instanceof TextField
    }

    def "все бэнды используют единый API добавления элементов"() {
        when:
        def report = ReportDsl.report("R") {
            bands {
                titleBand    { label(text: "T", width: 100); textField(expression: "\$P{x}", width: 100) }
                columnHeader { label(text: "C", width: 100) }
                detail       { textField(expression: "\$F{id}", width: 50); label(text: "x", width: 50) }
                summary      { line(y: 0) }
            }
        }

        then: "порядок и состав элементов сохраняются"
        report.bands.titleBand.elements*.class == [Label, TextField]
        report.bands.detail.elements*.class == [TextField, Label]
        report.bands.summary.elements*.class == [Line]
    }
}
