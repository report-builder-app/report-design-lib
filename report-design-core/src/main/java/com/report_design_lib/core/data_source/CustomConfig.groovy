package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceConfig

class CustomConfig implements DataSourceConfig {
    String providerName
    Map<String, Object> options = [:]
}
