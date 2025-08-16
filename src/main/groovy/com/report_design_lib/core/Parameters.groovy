package com.report_design_lib.core

class Parameters {
    List<Parameter> params = []

    def param(String name, Class type, def defaultValue = null) {
        params << new Parameter(name: name, type: type, defaultValue: defaultValue)
    }
}
