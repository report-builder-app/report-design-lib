package com.report_design_lib.core.elements


import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfWriter

/**
 * Abstract base class for all report elements.
 * Provides common properties and behavior for all report elements.
 */
abstract class ReportElement {
    Float x
    Float y
    Float width
    Float height
    String style

    /**
     * Высота элемента в точках, если содержимое не задано явно.
     * Чистая функция — не зависит от рендерера.
     */
    abstract float calculateHeight()
}
