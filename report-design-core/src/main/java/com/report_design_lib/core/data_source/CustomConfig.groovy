package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceConfig

class CustomConfig implements DataSourceConfig {
    String providerName
    Map<String, Object> options = [:]

    CustomConfig() { }

    CustomConfig(String providerName) {
        this.providerName = providerName
    }

    CustomConfig(String providerName, Map<String, Object> options) {
        this.providerName = providerName
        this.options = options ?: [:]
    }
}
