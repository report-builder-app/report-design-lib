package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource

import java.sql.Connection
import java.sql.DriverManager

class SqlDataSourceProvider implements DataSourceProvider<SqlConfig> {
    String getName() { 'sql' }
    Class<SqlConfig> getConfigType() { SqlConfig }

    ReportDataSource create(SqlConfig cfg, Map<String, Object> params) {
        String query = interpolate(cfg.queryString ?: params['_queryString'] as String, params)

        Connection conn = DriverManager.getConnection(cfg.jdbcUrl, cfg.user, cfg.password)
        try {
            List<Map<String, Object>> rows = []
            conn.createStatement().withCloseable { stmt ->
                stmt.executeQuery(query).withCloseable { rs ->
                    def meta = rs.metaData
                    def cols = (1..meta.columnCount).collect { meta.getColumnLabel(it) }
                    while (rs.next()) {
                        rows << cols.collectEntries { c ->
                            [(c): rs.getObject(c)]
                        }
                    }
                }
            }
            new CollectionReportDataSource(rows)
        } finally {
            conn.close()
        }
    }

    private String interpolate(String q, Map params) {
        params.findAll { !it.key.toString().startsWith('_') }.each { k, v ->
            def safe = (v instanceof Number || v instanceof Boolean)
                    ? v : "'${v?.toString()?.replace("'", "''")}'"
            q = q.replaceAll("\\\$P\\{$k\\}", safe.toString())
        }
        q
    }
}
