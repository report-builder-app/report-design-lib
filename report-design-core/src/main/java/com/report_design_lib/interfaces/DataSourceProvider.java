package com.report_design_lib.interfaces;

import java.util.Map;

public interface DataSourceProvider<C extends DataSourceConfig> {
    String getName();
    Class<C> getConfigType();
    ReportDataSource create(C config, Map<String, Object> params);
}
