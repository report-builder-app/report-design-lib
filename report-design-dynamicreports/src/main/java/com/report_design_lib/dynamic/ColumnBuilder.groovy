package com.report_design_lib.dynamic

class ColumnBuilder {
    String title
    String fieldName
    Class type
    Float width

    static ColumnBuilder column(String title, String fieldName, Class type) {
        new ColumnBuilder(title: title, fieldName: fieldName, type: type)
    }

    ColumnBuilder setWidth(Float w) {
        this.width = w
        this
    }
}
