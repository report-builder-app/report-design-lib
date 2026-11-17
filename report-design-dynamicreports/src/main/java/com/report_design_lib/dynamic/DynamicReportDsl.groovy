package com.report_design_lib.dynamic

class DynamicReportDsl {
    static FluentReportBuilder report(String name) {
        FluentReportBuilder.report(name)
    }

    static ColumnBuilder column(String title, String field, Class type) {
        ColumnBuilder.column(title, field, type)
    }

    static StyleBuilder style() {
        StyleBuilder.style()
    }
}
