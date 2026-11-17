package com.report_design_lib.core.elements

class Parameters {
    private final List<Parameter> params = []

    def param(String name, Class type, Object defaultValue = null) {
        params << new Parameter(name: name, type: type, defaultValue: defaultValue)
        this
    }

    List<Parameter> getList()    { Collections.unmodifiableList(params) }
    int size()                   { params.size() }
    boolean isEmpty()            { params.isEmpty() }
    Parameter get(int i)         { params[i] }

    Parameter getAt(int i)       { params[i] }

    /** уже есть, но проверим, что работает parameters["Title"] */
    Parameter getAt(String name) { params.find { it.name == name } }

    /** запись parameters[i] = ... и parameters["name"] = ... — если нужно */
    void putAt(int i, Parameter p) { params[i] = p }
    void putAt(String name, Parameter p) {
        def idx = params.findIndexOf { it.name == name }
        if (idx >= 0) params[idx] = p else params << p
    }
}
