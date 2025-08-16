package com.report_design_lib.core

class ColumnHeaderBand {
    List<Label> labels = []

    def label(Map args) {
        labels << new Label(text: args.text, width: args.width)
    }
}
