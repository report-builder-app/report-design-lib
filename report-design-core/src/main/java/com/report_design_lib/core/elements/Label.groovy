package com.report_design_lib.core.elements

class Label extends ReportElement {
    String text

    @Override
    float calculateHeight() {
        height ?: 12f
    }
}
