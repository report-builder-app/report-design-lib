package com.report_design.mongo

import com.report_design_lib.core.data_source.CollectionReportDataSource
import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource

class MongoDataSourceProvider implements DataSourceProvider<MongoConfig> {
    String getName() { 'mongodb' }
    Class<MongoConfig> getConfigType() { MongoConfig }

    ReportDataSource create(MongoConfig cfg, Map<String, Object> params) {
        def filter = cfg.filterJson
        params.findAll { !it.key.toString().startsWith('_') }.each { k, v ->
            filter = filter.replace("\${$k}", v.toString())
        }

        def client = com.mongodb.client.MongoClients.create(cfg.uri)
        try {
            def coll = client.getDatabase(cfg.database).getCollection(cfg.collection)
            def doc = org.bson.Document.parse(filter)
            def rows = coll.find(doc).into([]).collect { it as Map<String, Object> }
            new CollectionReportDataSource(rows)
        } finally {
            client.close()
        }
    }
}
