package com.report_design_lib.core

import com.report_design_lib.core.data_source.CustomConfig

class CustomConfigBuilder {
    CustomConfig cfg
    void option(String k, Object v)  { cfg.options[k] = v }
    void options(Map<String, Object> m) { cfg.options.putAll(m) }
}
