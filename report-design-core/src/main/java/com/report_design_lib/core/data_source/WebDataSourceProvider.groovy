package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource

import groovy.json.JsonSlurper

class WebDataSourceProvider implements DataSourceProvider<WebConfig> {
    String getName() { 'web' }
    Class<WebConfig> getConfigType() { WebConfig }

    ReportDataSource create(WebConfig cfg, Map<String, Object> params) {
        def url = buildUrl(cfg, params)

        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = cfg.method
        cfg.headers.each { k, v -> conn.setRequestProperty(k, v) }

        if (cfg.body && cfg.method in ['POST','PUT','PATCH']) {
            conn.doOutput = true
            conn.outputStream.withWriter('UTF-8') { it << cfg.body }
        }

        def json = new JsonSlurper().parseText(conn.inputStream.text)
        List<Map<String, Object>> rows = extractRows(json, cfg.jsonPath)

        new CollectionReportDataSource(rows)
    }

    private String buildUrl(WebConfig cfg, Map params) {
        def url = cfg.url
        params.findAll { !it.key.toString().startsWith('_') }.each { k, v ->
            url = url.replace("\${$k}", v.toString())
        }
        if (cfg.query) {
            def qs = cfg.query.collect { k, v -> "$k=${URLEncoder.encode(v, 'UTF-8')}" }.join('&')
            url += (url.contains('?') ? '&' : '?') + qs
        }
        url
    }

    private List<Map<String, Object>> extractRows(Object json, String path) {
        def result = json
        if (path && path != '$') {
            def clean = path.replace('$.', '').replace('[*]', '')
            clean.split('\\.').each { key -> result = result?."$key" }
        }
        result instanceof List ? result.collect { it as Map } : (result ? [result as Map] : [])
    }
}
