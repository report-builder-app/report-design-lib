package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.DataSourceConfig
import com.report_design_lib.interfaces.DataSourceProvider
import com.report_design_lib.interfaces.ReportDataSource

class DataSourceRegistry {
    private static final Map<String, DataSourceProvider<?>> providers = [:]

    static <C extends DataSourceConfig> void register(DataSourceProvider<C> p) {
        providers[p.name] = p
    }

    /** Получить провайдер по имени. */
    static DataSourceProvider<?> get(String providerName) {
        def p = providers[providerName]
        if (!p) {
            throw new IllegalStateException("No provider for '${providerName}'. Available: ${available()}")
        }
        p
    }

    static <C extends DataSourceConfig> DataSourceProvider<C> getTyped(String providerName, Class<C> type) {
        def p = providers[providerName]
        if (!p) throw new IllegalStateException("No provider for '${providerName}'")
        if (!type.isAssignableFrom(p.configType))
            throw new IllegalArgumentException("Provider '$providerName' expects ${p.configType.simpleName}, got ${type.simpleName}")
        (DataSourceProvider<C>) p
    }

    static ReportDataSource create(DataSourceConfig cfg, Map<String, Object> params) {
        def p = providers[cfg.providerName]
        if (!p) throw new IllegalStateException("No provider for '${cfg.providerName}'")
        if (!p.configType.isInstance(cfg))
            throw new IllegalArgumentException(
                    "Provider '${p.name}' expects ${p.configType.simpleName}, got ${cfg.class.simpleName}")
        p.create(cfg, params)
    }

    static List<String> available() { providers.keySet().toList() }

    /**
     * Очистить реестр. Нужно для тестов и переинициализации.
     */
    static void clear() { providers.clear() }
}
