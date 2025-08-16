package com.report_design_lib.core

class SummaryBand {
    List<ReportElement> elements = []

    def line(Map args) {
        elements << new Line(y: args.y ?: 0)
    }

    def textField(Map args) {
        elements << new TextField(expression: args.expression)
    }
}
