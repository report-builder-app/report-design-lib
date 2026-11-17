package com.report_design.mongo

import com.report_design_lib.interfaces.DataSourceConfig

class MongoConfig implements DataSourceConfig {
    String uri
    String database
    String collection
    String filterJson = '{}'
    String getProviderName() { 'mongodb' }
}
