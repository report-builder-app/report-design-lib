package com.report_design_lib.renderer.json

import groovy.json.JsonOutput
import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import com.report_design_lib.core.render.support.ReportFlattener

class JsonRenderer implements ReportRenderer {
    String getFormat() { 'json' }
    String getContentType() { 'application/json; charset=UTF-8' }
    String getDefaultExtension() { 'json' }

    @Override
    void render(Report report, Map<String, Object> runtimeParams, OutputStream out) {
        def rows = ReportFlattener.flatten(report, runtimeParams).collect { row ->
            [
                    band: row.band.name(),
                    cells: row.cells.findAll { !it.isLine }.collect { cell ->
                        [value: cell.value, style: cell.style, width: cell.width, height: cell.height]
                    }
            ]
        }
        def payload = [
                name: report.name,
                title: report.title,
                pageFormat: report.pageFormat?.name(),
                orientation: report.orientation?.name(),
                rows: rows
        ]
        out.withWriter('UTF-8') { it << JsonOutput.prettyPrint(JsonOutput.toJson(payload)) }
    }
}
