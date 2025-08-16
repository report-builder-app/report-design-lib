package com.report_design_lib.core

class TitleBand {
    List<Label> labels = []

    def label(Map args) {
        labels << new Label(text: args.text, style: args.style)
    }
}
