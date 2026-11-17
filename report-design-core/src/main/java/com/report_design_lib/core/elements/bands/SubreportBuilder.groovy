package com.report_design_lib.core.elements.bands

import com.report_design_lib.core.Report
import com.report_design_lib.core.ReportBuilder
import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.elements.SubreportElement

class SubreportBuilder {
    private final SubreportElement element = new SubreportElement()

    SubreportBuilder report(String name, @DelegatesTo(ReportBuilder) Closure cl) {
        element.report = ReportDsl.report(name, cl)
        this
    }

    SubreportBuilder report(Report report) {
        element.report = report
        this
    }

    SubreportBuilder lazy(Closure<Report> supplier) {
        element.reportSupplier = supplier
        this
    }

    SubreportBuilder param(String name, Object value) {
        element.parameters[name] = value
        this
    }

    SubreportBuilder params(Map<String, Object> m) {
        element.parameters.putAll(m)
        this
    }

    SubreportBuilder inheritData(boolean v = true) {
        element.inheritData = v
        this
    }

    SubreportBuilder width(Float w)   { element.width = w; this }
    SubreportBuilder height(Float h)  { element.height = h; this }
    SubreportBuilder style(String s)  { element.style = s; this }

    SubreportElement build() { element }
}
