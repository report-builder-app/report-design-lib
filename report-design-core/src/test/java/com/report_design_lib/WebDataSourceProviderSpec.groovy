package com.report_design_lib

import com.report_design_lib.core.data_source.WebConfig
import com.report_design_lib.core.data_source.WebDataSourceProvider
import com.sun.net.httpserver.HttpServer
import spock.lang.Specification

class WebDataSourceProviderSpec extends Specification {

    HttpServer server

    def setup() {
        server = HttpServer.create(new InetSocketAddress(0), 0)
        server.createContext("/orders") { exchange ->
            def body = '{"data":[{"id":1,"amount":100},{"id":2,"amount":200}]}'
            exchange.sendResponseHeaders(200, body.length())
            exchange.responseBody.withCloseable { it.write(body.bytes) }
        }
        server.start()
    }

    def cleanup() {
        server.stop(0)
    }

    def "получает данные по REST и распаковывает jsonPath"() {
        given:
        def port = server.address.port
        def cfg = new WebConfig(
                url: "http://localhost:${port}/orders",
                method: "GET",
                jsonPath: '$.data[*]'
        )
        def provider = new WebDataSourceProvider()

        when:
        def ds = provider.create(cfg, [:])
        def rows = []
        while (ds.next()) rows << ds.row

        then:
        rows.size() == 2
        rows[0].id == 1
        rows[0].amount == 100
        rows[1].id == 2
        rows[1].amount == 200
    }

    def "подставляет параметры в URL"() {
        given:
        server.createContext("/orders/from/2024-01-01") { exchange ->
            def body = '{"data":[{"id":9}]}'
            exchange.sendResponseHeaders(200, body.length())
            exchange.responseBody.withCloseable { it.write(body.bytes) }
        }
        def port = server.address.port
        def cfg = new WebConfig(
                url: "http://localhost:${port}/orders/from/\${from}",
                jsonPath: '$.data[*]'
        )
        def provider = new WebDataSourceProvider()

        when:
        def ds = provider.create(cfg, [from: "2024-01-01"])
        def rows = []
        while (ds.next()) rows << ds.row

        then:
        rows.size() == 1
        rows[0].id == 9
    }
}
