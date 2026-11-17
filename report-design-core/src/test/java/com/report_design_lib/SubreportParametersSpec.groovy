package com.report_design_lib

import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.elements.SubreportElement
import spock.lang.Specification

class SubreportParametersSpec extends Specification {

    def "subreport передаёт параметры"() {
        when:
        def report = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report("Sub") { }
                        param "orderId", 42
                        param "region", "EU"
                    }
                }
            }
        }

        then:
        def sub = report.bands.detail.elements[0] as SubreportElement
        sub.parameters == [orderId: 42, region: "EU"]
    }

    def "params(Map) добавляет несколько параметров сразу"() {
        when:
        def report = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report("Sub") { }
                        params orderId: 7, region: "US", currency: "USD"
                    }
                }
            }
        }

        then:
        def sub = report.bands.detail.elements[0] as SubreportElement
        sub.parameters == [orderId: 7, region: "US", currency: "USD"]
    }

    def "param переопределяет ранее заданное значение"() {
        when:
        def report = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report("Sub") { }
                        param "region", "EU"
                        param "region", "US"
                    }
                }
            }
        }

        then:
        def sub = report.bands.detail.elements[0] as SubreportElement
        sub.parameters["region"] == "US"
    }

    def "inheritData по умолчанию false"() {
        when:
        def report = ReportDsl.report("Main") {
            bands { detail { subreport { report("Sub") { } } } }
        }

        then:
        def sub = report.bands.detail.elements[0] as SubreportElement
        !sub.inheritData
    }

    def "inheritData(true) устанавливает флаг"() {
        when:
        def report = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report("Sub") { }
                        inheritData(true)
                    }
                }
            }
        }

        then:
        (report.bands.detail.elements[0] as SubreportElement).inheritData
    }
}
