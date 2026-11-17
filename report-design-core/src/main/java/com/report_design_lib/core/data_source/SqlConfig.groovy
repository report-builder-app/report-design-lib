package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceConfig

class SqlConfig implements DataSourceConfig {
    String jdbcUrl
    String user
    String password
    String driver
    String queryString // может быть задан и в корне отчёта
    Map<String, Object> queryParams = [:]

    String getProviderName() { 'sql' }
}
