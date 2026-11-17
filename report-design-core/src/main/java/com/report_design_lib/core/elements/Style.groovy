package com.report_design_lib.core.elements

class Style {
    String name  // полезно для отладки
    Font font = new Font()
    Alignment horizontalAlignment
    Color background

    Style font(@DelegatesTo(Font) Closure cl) {
        cl.delegate = font
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        this
    }
}
