package com.report_design_lib.core

class Reports {
    static void renderTo(Report report, String format, Map<String, Object> params, OutputStream out) {
        RendererRegistry.get(format).render(report, params, out)
    }

    static byte[] renderToBytes(Report report, String format, Map<String, Object> params = [:]) {
        def out = new ByteArrayOutputStream()
        renderTo(report, format, params, out)
        out.toByteArray()
    }

    static void renderToFile(Report report, String format, Map<String, Object> params,
                             File file = new File("${report.name}.${RendererRegistry.get(format).defaultExtension}")) {
        file.withOutputStream { renderTo(report, format, params, it) }
    }
}
