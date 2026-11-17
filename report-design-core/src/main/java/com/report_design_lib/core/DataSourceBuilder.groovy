package com.report_design_lib.core

import com.report_design_lib.core.data_source.CustomConfig
import com.report_design_lib.core.data_source.SqlConfig
import com.report_design_lib.core.data_source.WebConfig
import com.report_design_lib.interfaces.DataSourceConfig

class DataSourceBuilder {
    private DataSourceConfig result

    void sql(@DelegatesTo(SqlConfigBuilder) Closure cl) {
        def c = new SqlConfig()
        def b = new SqlConfigBuilder(cfg: c)
        cl.delegate = b
        cl()
        result = c
    }

    void web(@DelegatesTo(WebConfigBuilder) Closure cl) {
        def c = new WebConfig()
        def b = new WebConfigBuilder(cfg: c)
        cl.delegate = b
        cl()
        result = c
    }

    void custom(String providerName, @DelegatesTo(CustomConfigBuilder) Closure cl = null) {
        def c = new CustomConfig(providerName: providerName)
        if (cl) {
            def b = new CustomConfigBuilder(cfg: c)
            cl.delegate = b
            cl()
        }
        result = c
    }

    DataSourceConfig build() {
        if (!result) throw new IllegalStateException("dataSource { } must call sql/web/custom")
        result
    }
}

