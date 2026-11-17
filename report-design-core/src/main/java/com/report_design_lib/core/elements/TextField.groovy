package com.report_design_lib.core.elements

class TextField  extends ReportElement {
    String expression

    @Override
    float calculateHeight() {
        height ?: 12f
    }
}