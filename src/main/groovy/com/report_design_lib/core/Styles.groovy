package com.report_design_lib.core

class Styles {
    Map<String, Style> styleMap = [:]

    def style(String name, @DelegatesTo(Style) Closure cl) {
        def style = new Style()
        cl.delegate = style
        cl()
        styleMap[name] = style
    }
}
