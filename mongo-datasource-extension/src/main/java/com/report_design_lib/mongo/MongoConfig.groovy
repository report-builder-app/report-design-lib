package com.report_design_lib.mongo

import com.report_design_lib.interfaces.DataSourceConfig

class MongoConfig implements DataSourceConfig {
    String uri
    String database
    String collection
    String filterJson = '{}'
    String getProviderName() { 'mongodb' }
}
