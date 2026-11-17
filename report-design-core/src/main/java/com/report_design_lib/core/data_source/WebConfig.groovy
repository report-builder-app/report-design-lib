package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceConfig

class WebConfig implements DataSourceConfig {
    String url
    String method = 'GET'
    Map<String, String> headers = [:]
    Map<String, String> query = [:]     // ?a=1&b=2
    String body                          // для POST
    String jsonPath                      // '$.data[*]'

    String getProviderName() { 'web' }
}
