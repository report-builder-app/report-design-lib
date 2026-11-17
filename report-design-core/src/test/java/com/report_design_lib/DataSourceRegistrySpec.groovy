package com.report_design_lib

import com.report_design_lib.core.data_source.CollectionReportDataSource
import com.report_design_lib.core.data_source.DataSourceRegistry
import com.report_design_lib.core.data_source.CustomConfig
import com.report_design_lib.core.data_source.SqlConfig
import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource
import spock.lang.Specification

class DataSourceRegistrySpec extends Specification {

    def cleanup() {
        // очищаем реестр между тестами (если нет метода clear — добавляем рефлексией)
        def f = DataSourceRegistry.getDeclaredField("providers")
        f.accessible = true
        ((Map) f.get(null)).clear()
    }

    static class TestProvider implements DataSourceProvider<CustomConfig> {
        String getName() { 'test' }
        Class<CustomConfig> getConfigType() { CustomConfig }

        ReportDataSource create(CustomConfig cfg, Map params) {
            new CollectionReportDataSource([[id: 1], [id: 2]])
        }
    }

    static class WrongProvider implements DataSourceProvider<SqlConfig> {
        String getName() { 'wrong' }
        Class<SqlConfig> getConfigType() { SqlConfig }
        ReportDataSource create(SqlConfig cfg, Map params) { null }
    }

    def "регистрирует и создаёт источник по имени"() {
        given:
        DataSourceRegistry.register(new TestProvider())
        def cfg = new CustomConfig(providerName: 'test')

        when:
        def ds = DataSourceRegistry.create(cfg, [:])

        then:
        ds != null
        ds.next()
        ds.row.id == 1
    }

    def "бросает исключение для неизвестного провайдера"() {
        when:
        DataSourceRegistry.create(new CustomConfig(providerName: 'nope'), [:])

        then:
        def e = thrown(IllegalStateException)
        e.message.contains("nope")
    }

    def "бросает исключение при несоответствии типа конфига"() {
        given:
        DataSourceRegistry.register(new WrongProvider())

        when:
        DataSourceRegistry.create(new CustomConfig(providerName: 'wrong'), [:])

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("expects SqlConfig")
    }

    def "available() возвращает имена зарегистрированных провайдеров"() {
        given:
        DataSourceRegistry.register(new TestProvider())

        expect:
        DataSourceRegistry.available() == ['test']
    }
}

