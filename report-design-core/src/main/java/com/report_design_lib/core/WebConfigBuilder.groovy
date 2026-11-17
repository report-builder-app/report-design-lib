package com.report_design_lib.core

import com.report_design_lib.core.data_source.WebConfig

class WebConfigBuilder {
    WebConfig cfg
    void url(String v)        { cfg.url = v }
    void method(String v)     { cfg.method = v }
    void header(String k, String v) { cfg.headers[k] = v }
    void headers(Map m)       { cfg.headers.putAll(m as Map<String, String>) }
    void queryParam(String k, String v) { cfg.query[k] = v }
    void body(String v)       { cfg.body = v }
    void jsonPath(String v)   { cfg.jsonPath = v }
}
