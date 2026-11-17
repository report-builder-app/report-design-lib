package com.report_design_lib

import com.report_design_lib.core.data_source.CustomConfig
import com.report_design_lib.core.data_source.SqlConfig
import com.report_design_lib.core.data_source.WebConfig
import spock.lang.Specification
import com.report_design_lib.core.ReportDsl

class ReportDataSourceDslSpec extends Specification {

    def "секция dataSource { sql { } } создаёт SqlConfig"() {
        when:
        def report = ReportDsl.report("R") {
            dataSource {
                sql {
                    jdbcUrl "jdbc:h2:mem:test"
                    user "sa"
                    password ""
                    driver "org.h2.Driver"
                }
            }
        }

        then:
        report.dataSource instanceof SqlConfig
        with(report.dataSource as SqlConfig) {
            jdbcUrl == "jdbc:h2:mem:test"
            user == "sa"
            password == ""
            driver == "org.h2.Driver"
            providerName == "sql"
        }
    }

    def "секция dataSource { web { } } создаёт WebConfig"() {
        when:
        def report = ReportDsl.report("R") {
            dataSource {
                web {
                    url "https://api.example.com/orders"
                    method "GET"
                    header "Accept", "application/json"
                    header "Authorization", "Bearer token"
                    queryParam "from", "\${StartDate}"
                    jsonPath '$.data[*]'
                }
            }
        }

        then:
        report.dataSource instanceof WebConfig
        with(report.dataSource as WebConfig) {
            url == "https://api.example.com/orders"
            method == "GET"
            headers == ["Accept": "application/json", "Authorization": "Bearer token"]
            query == ["from": "\${StartDate}"]
            jsonPath == '$.data[*]'
            providerName == "web"
        }
    }

    def "секция dataSource { custom('mongodb') { } } создаёт CustomConfig"() {
        when:
        def report = ReportDsl.report("R") {
            dataSource {
                custom('mongodb') {
                    option 'uri', 'mongodb://localhost:27017'
                    option 'database', 'shop'
                    option 'collection', 'orders'
                }
            }
        }

        then:
        report.dataSource instanceof CustomConfig
        with(report.dataSource as CustomConfig) {
            providerName == "mongodb"
            options == ["uri": "mongodb://localhost:27017", "database": "shop", "collection": "orders"]
        }
    }

    def "секция dataSource { custom('x') } без блока допустима"() {
        when:
        def report = ReportDsl.report("R") {
            dataSource { custom('lazy') }
        }

        then:
        report.dataSource instanceof CustomConfig
        (report.dataSource as CustomConfig).providerName == 'lazy'
        (report.dataSource as CustomConfig).options == [:]
    }

    def "секция dataSource без выбора провайдера бросает исключение"() {
        when:
        ReportDsl.report("R") {
            dataSource { }
        }

        then:
        thrown(IllegalStateException)
    }
}
