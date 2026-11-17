package com.report_design_lib

import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.data_source.*
import com.report_design_lib.core.elements.BandType
import com.report_design_lib.core.render.support.ReportFlattener
import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource
import spock.lang.Specification

class SubreportFlattenSpec extends Specification {

    static class StaticProvider implements DataSourceProvider<CustomConfig> {
        String getName() { 'static' }
        Class<CustomConfig> getConfigType() { CustomConfig }

        ReportDataSource create(CustomConfig cfg, Map params) {
            new CollectionReportDataSource((cfg.options['rows'] ?: []) as List<Map<String, Object>>)
        }
    }

    def setup() {
        DataSourceRegistry.clear()
        DataSourceRegistry.register(new StaticProvider())
    }

    def cleanup() {
        DataSourceRegistry.clear()
    }

    def "ReportFlattener разворачивает subreport в subRows"() {
        given:
        def sub = ReportDsl.report("Sub") {
            bands { detail { textField(expression: "\$F{x}", width: 100f) } }
            dataSource { custom('static') { option 'rows', [[x: 'a'], [x: 'b']] } }
        }
        def main = ReportDsl.report("Main") {
            bands { detail { subreport { report(sub) } } }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }

        when:
        def rows = ReportFlattener.flatten(main, [:])

        then: "1 detail-строка родителя"
        rows.size() == 1
        rows[0].band == BandType.DETAIL

        and: "cell[0] содержит подотчёт с 2 строками"
        def cell = rows[0].cells[0]
        cell.subRows != null
        cell.subRows.size() == 2
        cell.subRows*.cells*.value.flatten() == ['a', 'b']
    }

    def "параметры подотчёта переопределяют родительские"() {
        given:
        def sub = ReportDsl.report("Sub") {
            bands { titleBand { label(text: "\$P{region}", width: 100f) } }
        }
        def main = ReportDsl.report("Main") {
            parameters { param("region", String, "DEFAULT") }
            bands {
                detail {
                    subreport {
                        report(sub)
                        param "region", "EU"
                    }
                }
            }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }

        when:
        def rows = ReportFlattener.flatten(main, [:])

        then:
        rows[0].cells[0].subRows[0].cells[0].value == 'EU'
    }

    def "inheritData пробрасывает поля текущей строки"() {
        given:
        def sub = ReportDsl.report("Sub") {
            bands { titleBand { label(text: "\$P{orderId}", width: 100f) } }
        }
        def main = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        report(sub)
                        inheritData(true)
                    }
                }
            }
            dataSource { custom('static') { option 'rows', [[orderId: 42]] } }
        }

        when:
        def rows = ReportFlattener.flatten(main, [:])

        then:
        rows[0].cells[0].subRows[0].cells[0].value == 42
    }

    def "lazy-поставщик резолвится только при flatten"() {
        given:
        def called = false
        def main = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport {
                        lazy {
                            called = true
                            ReportDsl.report("Sub") {
                                bands { titleBand { label(text: "Lazy", width: 100f) } }
                            }
                        }
                    }
                }
            }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }

        expect:
        !called

        when:
        def rows = ReportFlattener.flatten(main, [:])

        then:
        called
        rows[0].cells[0].subRows[0].cells[0].value == 'Lazy'
    }

    def "несколько subreport в одном бэнде разворачиваются независимо"() {
        given:
        def subA = ReportDsl.report("A") {
            bands { detail { textField(expression: "\$F{x}", width: 50f) } }
            dataSource { custom('static') { option 'rows', [[x: 'a1'], [x: 'a2']] } }
        }
        def subB = ReportDsl.report("B") {
            bands { detail { textField(expression: "\$F{y}", width: 50f) } }
            dataSource { custom('static') { option 'rows', [[y: 'b1']] } }
        }
        def main = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport { report(subA) }
                    subreport { report(subB) }
                }
            }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }

        when:
        def rows = ReportFlattener.flatten(main, [:])

        then:
        rows[0].cells.size() == 2
        rows[0].cells[0].subRows.size() == 2
        rows[0].cells[1].subRows.size() == 1
        rows[0].cells[0].subRows*.cells*.value.flatten() == ['a1', 'a2']
        rows[0].cells[1].subRows*.cells*.value.flatten() == ['b1']
    }
}
