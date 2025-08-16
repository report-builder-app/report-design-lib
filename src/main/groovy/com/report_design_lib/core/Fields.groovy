package com.report_design_lib.core

class Fields {
    List<Field> fieldList = []

    def field(String name, Class type) {
        fieldList << new Field(name: name, type: type)
    }
}
