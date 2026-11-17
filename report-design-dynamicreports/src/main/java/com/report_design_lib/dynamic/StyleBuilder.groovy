package com.report_design_lib.dynamic

import com.report_design_lib.core.elements.*
import java.awt.Color

class StyleBuilder {
    Font font = new Font()
    Alignment alignment
    Color background

    static StyleBuilder style() {
        new StyleBuilder()
    }

    StyleBuilder bold() {
        font.isBold = true
        this
    }

    StyleBuilder fontSize(int size) {
        font.size = size
        this
    }

    StyleBuilder setHorizontalAlignment(Alignment a) {
        this.alignment = a
        this
    }

    StyleBuilder setBackgroundColor(Color c) {
        this.background = c
        this
    }
}

