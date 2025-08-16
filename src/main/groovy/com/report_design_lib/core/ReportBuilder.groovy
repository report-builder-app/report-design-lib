package com.report_design_lib.core

class ReportBuilder {
    String name
    private Report report

    ReportBuilder(String name) {
        this.name = name
        this.report = new Report(name)
    }

    def title(String title) { report.title = title }
    def pageFormat(PageFormat format) { report.pageFormat = format }
    def orientation(PageOrientation orientation) { report.orientation = orientation }

    def parameters(@DelegatesTo(Parameters) Closure cl) {
        def parameters = new Parameters()
        cl.delegate = parameters
        cl()
        report.parameters = parameters
    }

    def fields(@DelegatesTo(Fields) Closure cl) {
        def fields = new Fields()
        cl.delegate = fields
        cl()
        report.fields = fields
    }

    def queryString(String query) { report.queryString = query }

    def styles(@DelegatesTo(Styles) Closure cl) {
        def styles = new Styles()
        cl.delegate = styles
        cl()
        report.styles = styles
    }

    def bands(@DelegatesTo(Bands) Closure cl) {
        def bands = new Bands()
        cl.delegate = bands
        cl()
        report.bands = bands
    }

    Report build() { report }
}

class Report {
    String name
    String title
    PageFormat pageFormat = PageFormat.A4
    PageOrientation orientation = PageOrientation.PORTRAIT
    Parameters parameters
    Fields fields
    String queryString
    Styles styles
    Bands bands

    Report(String name) {
        this.name = name
    }
}

