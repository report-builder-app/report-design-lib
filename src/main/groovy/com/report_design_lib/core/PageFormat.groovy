package com.report_design_lib.core

import com.lowagie.text.PageSize

enum PageFormat {
    A4(PageSize.A4.width, PageSize.A4.height),
    A3(PageSize.A3.width, PageSize.A3.height),
    LETTER(PageSize.LETTER.width, PageSize.LETTER.height)

    final float width
    final float height

    PageFormat(float width, float height) {
        this.width = width
        this.height = height
    }
}