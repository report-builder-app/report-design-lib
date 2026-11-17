package com.report_design_lib

import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.elements.SubreportElement
import spock.lang.Specification

class SubreportDslSpec extends Specification {

    def "subreport добавляется в бэнд"() {
        when:
        def report = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report("Sub") {
                            fields { field("x", String) }
                            bands { detail { textField(expression: "\$F{x}", width: 100f) } }
                        }
                        width 300f
                    }
                }
            }
        }

        then:
        report.bands.detail.elements.size() == 1
        def sub = report.bands.detail.elements[0] as SubreportElement
        sub.resolveReport().name == "Sub"
        sub.width == 300f
    }

    def "subreport принимает готовый Report"() {
        given:
        def sub = ReportDsl.report("Sub") {
            bands { detail { textField(expression: "\$F{x}", width: 100f) } }
        }

        when:
        def main = ReportDsl.report("Main") {
            bands { detail { subreport { report(sub) } } }
        }

        then:
        def el = main.bands.detail.elements[0] as SubreportElement
        el.resolveReport() == sub
        el.reportSupplier == null
    }

    def "subreport поддерживает lazy-поставщик"() {
        given:
        def called = false

        when:
        def main = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        lazy {
                            called = true
                            ReportDsl.report("LazySub") { }
                        }
                    }
                }
            }
        }

        then: "лямбда не вызвана на этапе построения"
        !called

        when:
        def el = main.bands.detail.elements[0] as SubreportElement
        def resolved = el.resolveReport()

        then: "вызвана при resolveReport"
        called
        resolved.name == "LazySub"
    }

    def "subreport сохраняет height и style"() {
        when:
        def report = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report("Sub") { }
                        height 40f
                        style "highlight"
                    }
                }
            }
        }

        then:
        def el = report.bands.detail.elements[0] as SubreportElement
        el.height == 40f
        el.style == "highlight"
    }

    def "subreport без report и без lazy бросает исключение при resolveReport"() {
        given:
        def report = ReportDsl.report("Main") {
            bands { detail { subreport { } } }
        }
        def el = report.bands.detail.elements[0] as SubreportElement

        when:
        el.resolveReport()

        then:
        thrown(IllegalStateException)
    }
}

