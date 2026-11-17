package com.report_design_lib

import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.data_source.*
import com.report_design_lib.core.render.support.ReportFlattener
import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource
import spock.lang.Specification

class SubreportCycleSpec extends Specification {

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

    def "циклическая ссылка A → B → A бросает IllegalStateException"() {
        given:
        def aHolder = [value: null]
        def bHolder = [value: null]

        def a = ReportDsl.report("A") {
            bands { detail { subreport { lazy { bHolder.value } } } }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }
        def b = ReportDsl.report("B") {
            bands { detail { subreport { lazy { aHolder.value } } } }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }
        aHolder.value = a
        bHolder.value = b

        when:
        ReportFlattener.flatten(a, [:])

        then:
        def e = thrown(IllegalStateException)
        e.message.contains("Cyclic subreport")
        e.message.contains("A")
        e.message.contains("B")
    }

    def "self-reference A → A бросает IllegalStateException"() {
        given:
        def holder = [value: null]
        def a = ReportDsl.report("Self") {
            bands { detail { subreport { lazy { holder.value } } } }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }
        holder.value = a

        when:
        ReportFlattener.flatten(a, [:])

        then:
        def e = thrown(IllegalStateException)
        e.message.contains("Cyclic subreport")
        e.message.contains("Self")
    }

    def "стек очищается после успешного flatten"() {
        given:
        def sub = ReportDsl.report("Sub") {
            bands { titleBand { label(text: "x", width: 50f) } }
        }
        def main = ReportDsl.report("Main") {
            bands { detail { subreport { report(sub) } } }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }

        when: "дважды flatten на одном отчёте"
        def first  = ReportFlattener.flatten(main, [:])
        def second = ReportFlattener.flatten(main, [:])

        then: "нет ложного срабатывания защиты"
        first.size() == 1
        second.size() == 1
    }

    def "разные ветки с одинаковыми именами подотчётов не мешают друг другу"() {
        given: "два независимых sub с именем 'S' в одном detail"
        def subS1 = ReportDsl.report("S") {
            bands { titleBand { label(text: "one", width: 50f) } }
        }
        def subS2 = ReportDsl.report("S") {
            bands { titleBand { label(text: "two", width: 50f) } }
        }
        def main = ReportDsl.report("Main") {
            bands {
                detail {
                    subreport { report(subS1) }
                    subreport { report(subS2) }
                }
            }
            dataSource { custom('static') { option 'rows', [[:]] } }
        }

        when:
        def rows = ReportFlattener.flatten(main, [:])

        then: "стек не считает повторное имя циклической ссылкой"
        rows[0].cells[0].subRows[0].cells[0].value == 'one'
        rows[0].cells[1].subRows[0].cells[0].value == 'two'
    }
}
