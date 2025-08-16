package com.report_design_lib.core

import com.lowagie.text.Element

enum Alignment {
    LEFT(Element.ALIGN_LEFT),
    CENTER(Element.ALIGN_CENTER),
    RIGHT(Element.ALIGN_RIGHT)

    final int pdfAlignment

    Alignment(int pdfAlignment) {
        this.pdfAlignment = pdfAlignment
    }
}