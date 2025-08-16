package com.report_design_lib.pdf

import com.lowagie.text.*
import com.lowagie.text.pdf.*
import com.report_design_lib.core.*

class PdfReportGenerator {
    static byte[] generate(Report report) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        // Create document with report's page settings
        Document document = new Document(
                report.pageFormat.width,
                report.pageFormat.height
        )

        if (report.orientation == PageOrientation.LANDSCAPE) {
            document.setPageSize(PageSize.A4.rotate())
        }

        PdfWriter.getInstance(document, outputStream)
        document.open()

        // Apply styles and generate content
        applyStyles(document, report)
        generateBands(document, report)

        document.close()
        return outputStream.toByteArray()
    }

    private static void applyStyles(Document document, Report report) {
        // Would create PDF styles based on the report.styles definitions
    }

    private static void generateBands(Document document, Report report) {
        // Process each band type in order
        generateTitleBand(document, report.bands.titleBand, report)
        generateColumnHeader(document, report.bands.columnHeader, report)
        generateDetailBand(document, report.bands.detail, report)
        generateSummaryBand(document, report.bands.summary, report)
    }

    private static void generateTitleBand(Document document, TitleBand band, Report report) {
        band.labels.each { label ->
            Paragraph p = new Paragraph(label.text)
            if (label.style) {
                applyStyleToParagraph(p, report.styles.styleMap[label.style])
            }
            document.add(p)
        }
    }

    private static void generateColumnHeader(Document document, ColumnHeaderBand band, Report report) {
        PdfPTable table = createTableFromBand(band)
        table.setHeaderRows(1)

        // Apply column header style
        if (report.styles.styleMap["columnHeader"]) {
            Style style = report.styles.styleMap["columnHeader"]
            table.defaultCell.apply {
                if (style.font?.isBold) setBorderWidth(1)
                if (style.background) backgroundColor = style.background.pdfColor
            }
        }

        document.add(table)
    }

    // Modified detail band generation
    private static void generateDetailBand(Document document, DetailBand band,
                                           Report report, java.util.List<Map> data) {
        PdfPTable table = new PdfPTable(band.textFields.size())

        data.each { row ->
            band.textFields.each { field ->
                String expression = field.expression
                // Simple expression evaluation - in real implementation use a proper parser
                String value = expression.replaceAll(/\$F\{(\w+)\}/) {
                    fullMatch, fieldName -> row[fieldName]?.toString() ?: ""
                }

                PdfPCell cell = new PdfPCell(new Phrase(value))
                if (field.width) cell.fixedWidth = field.width
                table.addCell(cell)
            }
        }

        document.add(table)
    }

    private static void generateSummaryBand(Document document, SummaryBand band, Report report) {
        band.elements.each { element ->
            if (element instanceof Line) {
                document.add(new Chunk(LineSeparator()))
            } else if (element instanceof TextField) {
                document.add(new Paragraph(element.expression))
            }
        }
    }

    private static PdfPTable createTableFromBand(ColumnHeaderBand band) {
        PdfPTable table = new PdfPTable(band.labels.size())

        band.labels.each { label ->
            PdfPCell cell = new PdfPCell(new Phrase(label.text))
            if (label.width) cell.fixedWidth = label.width
            table.addCell(cell)
        }

        return table
    }

    private static void applyStyleToParagraph(Paragraph p, Style style) {
        if (!style) return

        Font font = FontFactory.getFont(FontFactory.HELVETICA)
        if (style.font) {
            if (style.font.size) font.size = style.font.size
            if (style.font.isBold) font.style |= Font.BOLD
            if (style.font.isItalic) font.style |= Font.ITALIC
        }
        p.font = font

        if (style.horizontalAlignment) {
            p.alignment = style.horizontalAlignment.pdfAlignment
        }
    }
}
