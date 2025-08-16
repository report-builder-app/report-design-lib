package com.report_design_lib.core

class Style {
    Font font = new Font()
    Alignment horizontalAlignment
    Color background

    def font(@DelegatesTo(Font) Closure cl) {
        cl.delegate = font
        cl()
    }
}
