package com.report_design_lib

import com.report_design_lib.core.ReportDsl
import spock.lang.Specification

class ReportSectionsSpec extends Specification {

    def "секция parameters добавляет параметры"() {
        when:
        def report = ReportDsl.report("R") {
            parameters {
                param("StartDate", Date)
                param("EndDate", Date)
                param("Title", String, "Default")
            }
        }

        then:
        report.parameters != null
        report.parameters.size() == 3

        and: "доступ по индексу"
        report.parameters[0].name == "StartDate"
        report.parameters[0].type == Date
        report.parameters[1].name == "EndDate"
        report.parameters[2].defaultValue == "Default"

        and: "доступ по имени"
        report.parameters["Title"].defaultValue == "Default"
        report.parameters["StartDate"].type == Date
        report.parameters["Unknown"] == null
    }

    def "секция fields добавляет поля"() {
        when:
        def report = ReportDsl.report("R") {
            fields {
                field("id", Long)
                field("name", String)
            }
        }

        then:
        report.fields.list.size() == 2
        report.fields.list*.name == ["id", "name"]
        report.fields.list*.type == [Long, String]
    }

    def "секция styles добавляет стили"() {
        when:
        def report = ReportDsl.report("R") {
            styles {
                style("titleStyle") {
                    font {
                        size = 18
                        isBold = true
                    }
                }
            }
        }

        then:
        report.styles.map.containsKey("titleStyle")
        report.styles.map["titleStyle"].font.size == 18
        report.styles.map["titleStyle"].font.isBold
    }

    def "queryString сохраняется"() {
        when:
        def report = ReportDsl.report("R") {
            queryString "SELECT * FROM users"
        }

        then:
        report.queryString == "SELECT * FROM users"
    }

    def "повторный вызов секций заменяет значение"() {
        when:
        def report = ReportDsl.report("R") {
            parameters { param("A", String) }
            parameters { param("B", String) }
        }

        then:
        report.parameters.list.size() == 1
        report.parameters.list[0].name == "B"
    }
}
