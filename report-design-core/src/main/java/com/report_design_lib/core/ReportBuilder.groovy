package com.report_design_lib.core

import com.report_design_lib.core.elements.bands.Bands
import com.report_design_lib.core.elements.Fields
import com.report_design_lib.core.elements.PageFormat
import com.report_design_lib.core.elements.PageOrientation
import com.report_design_lib.core.elements.Parameters
import com.report_design_lib.core.elements.Styles

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
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        report.parameters = parameters
    }

    def fields(@DelegatesTo(Fields) Closure cl) {
        def fields = new Fields()
        cl.delegate = fields
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        report.fields = fields
    }

    def queryString(String query) { report.queryString = query }

    def styles(@DelegatesTo(Styles) Closure cl) {
        def styles = new Styles()
        cl.delegate = styles
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        report.styles = styles
    }

    def bands(@DelegatesTo(Bands) Closure cl) {
        def bands = new Bands()
        cl.delegate = bands
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        report.bands = bands
    }

    def dataSource(@DelegatesTo(DataSourceBuilder) Closure cl) {
        def builder = new DataSourceBuilder()
        cl.delegate = builder
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        report.dataSource = builder.build()
    }

    Report build() { report }
}

