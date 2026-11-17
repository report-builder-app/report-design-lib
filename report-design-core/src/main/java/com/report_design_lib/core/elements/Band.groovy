package com.report_design_lib.core.elements

abstract class Band {
    abstract BandType getType()

    final List<ReportElement> elements = []

    Band add(ReportElement el) {
        elements << el
        this
    }

    // Удобные методы-хелперы (доступны всем бэндам)
    Band label(Map args) {
        add(new Label(
                text: args.text as String,
                style: args.style as String,
                width: args.width as Integer,
                height: args.height as Integer
        ))
    }

    Band textField(Map args) {
        add(new TextField(
                expression: args.expression as String,
                style: args.style as String,
                width: args.width as Integer,
                height: args.height as Integer
        ))
    }

    Band line(Map args) {
        add(new Line(y: (args.y ?: 0) as Integer))
    }

    int size() { elements.size() }
    boolean isEmpty() { elements.isEmpty() }
    List<ReportElement> getList() { Collections.unmodifiableList(elements) }
}
