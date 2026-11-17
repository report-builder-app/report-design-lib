package com.report_design_lib.core.elements

class Fields {
    private final List<Field> fields = []

    Fields field(String name, Class type) {
        fields << new Field(name: name, type: type)
        this
    }

    List<Field> getList()  { Collections.unmodifiableList(fields) }
    int size()             { fields.size() }
    boolean isEmpty()      { fields.isEmpty() }
    Field get(int i)       { fields[i] }
    Field get(String name) { fields.find { it.name == name } }
}
