package com.report_design_lib.core

import com.report_design_lib.core.render.ReportRenderer

class RendererRegistry {
    private static final Map<String, ReportRenderer> renderers = new LinkedHashMap<>()
    private static boolean spiLoaded = false

    static synchronized void register(ReportRenderer r) {
        renderers[r.format.toLowerCase()] = r
    }

    static synchronized ReportRenderer get(String format) {
        ensureSpiLoaded()
        def r = renderers[format?.toLowerCase()]
        if (!r) throw new IllegalStateException(
                "No renderer for format: '$format'. Available: ${available()}")
        r
    }

    static synchronized List<String> available() {
        ensureSpiLoaded()
        renderers.keySet().toList()
    }

    static synchronized void clear() {       // для тестов
        renderers.clear()
        spiLoaded = false
    }

    private static void ensureSpiLoaded() {
        if (spiLoaded) return
        try {
            ServiceLoader.load(ReportRenderer).each { register(it) }
        } catch (Throwable ignored) { /* SPI отсутствует — ок */ }
        spiLoaded = true
    }
}
