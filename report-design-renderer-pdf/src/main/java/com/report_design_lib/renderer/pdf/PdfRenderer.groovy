package com.report_design_lib.renderer.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfWriter
import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import com.report_design_lib.core.render.support.ReportFlattener

class PdfRenderer implements ReportRenderer {
    String getFormat() { 'pdf' }
    String getContentType() { 'application/pdf' }
    String getDefaultExtension() { 'pdf' }

    @Override
    void render(Report report, Map<String, Object> runtimeParams, OutputStream out) {
        def document = new Document()
        def writer = PdfWriter.getInstance(document, out)
        document.open()
        try {
            def styles = report.styles?.map ?: [:]
            ReportFlattener.flatten(report, runtimeParams).each { row ->
                def line = new Paragraph()
                row.cells.each { cell ->
                    if (cell.isLine) {
                        line.add(new Chunk("\n"))
                        return
                    }
                    def chunk = new Chunk(cell.value?.toString() ?: "")
                    chunk.font = createFont(styles[cell.style]?.font)
                    line.add(chunk)
                    line.add(Chunk.NEWLINE)
                }
                document.add(line)
            }
        } finally {
            document.close()
        }
    }

    private static com.lowagie.text.Font createFont(com.report_design_lib.core.elements.Font f) {
        if (!f) return FontFactory.getFont(FontFactory.HELVETICA)
        int s = com.lowagie.text.Font.NORMAL
        if (f.isBold) s |= com.lowagie.text.Font.BOLD
        FontFactory.getFont(FontFactory.HELVETICA, f.size ?: 12, s)
    }
}
