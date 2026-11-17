package com.report_design_lib.renderer.html

import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import com.report_design_lib.core.render.support.ReportFlattener

class HtmlRenderer implements ReportRenderer {
    String getFormat() { 'html' }
    String getContentType() { 'text/html; charset=UTF-8' }
    String getDefaultExtension() { 'html' }

    @Override
    void render(Report report, Map<String, Object> runtimeParams, OutputStream out) {
        def w = new OutputStreamWriter(out, 'UTF-8')
        w.write("""<!DOCTYPE html>
<html><head><meta charset="UTF-8"/><title>${escape(report.title ?: report.name)}</title>
<style>
table { border-collapse: collapse; }
td { padding: 2px 6px; border-bottom: 1px solid #eee; }
tr.TITLE td   { font-weight: bold; font-size: 1.4em; }
tr.COLUMN_HEADER td { font-weight: bold; background: #eee; }
</style></head><body>
""")

        w.write("<table>")
        ReportFlattener.flatten(report, runtimeParams).each { row ->
            w.write("<tr class=\"${row.band}\">")
            row.cells.each { cell ->
                if (cell.isLine) {
                    w.write("<td colspan=\"100\"><hr/></td>")
                } else {
                    w.write("<td>${escape(cell.value?.toString() ?: '')}</td>")
                }
            }
            w.write("</tr>")
        }
        w.write("</table></body></html>")
        w.flush()
    }

    private static String escape(String s) {
        s?.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;') ?: ''
    }
}
