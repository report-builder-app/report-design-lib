package com.report_design_lib.core

import com.lowagie.text.Document
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfWriter

/**
 * Abstract base class for all report elements.
 * Provides common properties and behavior for all report elements.
 */
abstract class ReportElement {
    /** Horizontal position relative to band (in points) */
    Float x

    /** Vertical position relative to band (in points) */
    Float y

    /** Width of the element (in points) */
    Float width

    /** Height of the element (in points) */
    Float height

    /** Style name reference from the report's styles */
    String style

    /**
     * Renders the element to the PDF document
     * @param document The PDF document being generated
     * @param writer The PDF writer
     * @param data Current data row (for detail bands) or null
     * @param params Report parameters
     * @param styles Map of defined styles
     */
    abstract void render(Document document, PdfWriter writer,
                         Map data, Map params, Map<String, Style> styles)

    /**
     * Calculates the element's height based on content
     * @return height in points
     */
    abstract float calculateHeight()

    /**
     * Applies common style properties to a PDF element
     */
    protected void applyCommonStyle(Element element, Style style) {
        if (!style) return

        // Apply font if element supports it
        if (element instanceof Phrase || element instanceof Paragraph) {
            Font font = createFont(style.font)
            if (element instanceof Paragraph) {
                element.font = font
            } else {
                element.font = font
            }
        }

        // Apply background if element supports it
        if (element instanceof PdfPCell) {
            if (style.background) {
                element.backgroundColor = style.background.pdfColor
            }
        }
    }

//    protected Font createFont(FontStyle fontStyle) {
//        if (!fontStyle) return FontFactory.getFont(FontFactory.HELVETICA)
//
//        int style = Font.NORMAL
//        if (fontStyle.isBold) style |= Font.BOLD
//        if (fontStyle.isItalic) style |= Font.ITALIC
//
//        return FontFactory.getFont(
//                FontFactory.HELVETICA,
//                fontStyle.size ?: 12,
//                style,
//                fontStyle.color?.pdfColor ?: BaseColor.BLACK
//        )
//    }
}
