package com.report_design_lib.core

import com.report_design_lib.core.data_source.SqlConfig

class SqlConfigBuilder {
    SqlConfig cfg
    void jdbcUrl(String v)    { cfg.jdbcUrl = v }
    void user(String v)       { cfg.user = v }
    void password(String v)   { cfg.password = v }
    void driver(String v)     { cfg.driver = v }
    void query(String v)      { cfg.queryString = v }
    void queryParam(String k, Object v) { cfg.queryParams[k] = v }
}
