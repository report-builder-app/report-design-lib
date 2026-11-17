package com.report_design_lib.core

class ReportDsl {
    static Report report(String name, @DelegatesTo(ReportBuilder) Closure cl) {
        def builder = new ReportBuilder(name)
        cl.delegate = builder
        cl()
        builder.build()
    }
}
