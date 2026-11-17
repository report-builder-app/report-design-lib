package com.report_design_lib.renderer.html

import com.report_design_lib.core.data_source.CustomConfig
import com.report_design_lib.core.data_source.CollectionReportDataSource
import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource

class StaticProvider implements DataSourceProvider<CustomConfig> {

    String getName() { 'static' }
    Class<CustomConfig> getConfigType() { CustomConfig }

    @Override
    ReportDataSource create(CustomConfig cfg, Map<String, Object> params) {
        def rows = (cfg.options['rows'] ?: []) as List<Map<String, Object>>
        new CollectionReportDataSource(rows)
    }
}
