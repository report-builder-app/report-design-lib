package com.report_design_lib.core

class DetailBand {
    List<TextField> textFields = []

    def textField(Map args) {
        textFields << new TextField(expression: args.expression, width: args.width)
    }
}
