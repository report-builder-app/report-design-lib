package com.report_design_lib.core.elements

import com.report_design_lib.core.Report

class SubreportElement extends ReportElement {
    /** Готовый вложенный отчёт (или ленивый поставщик, см. ниже). */
    Report report

    /** Поставщик вложенного отчёта — для ленивой сборки. */
    Closure<Report> reportSupplier

    /** Параметры, передаваемые во вложенный отчёт. */
    Map<String, Object> parameters = [:]

    /** Если true — вложенный отчёт получает ту же строку данных, что и detail. */
    boolean inheritData = false

    @Override
    float calculateHeight() {
        height ?: 50f
    }

    Report resolveReport() {
        def r = report ?: reportSupplier?.call()
        if (r == null) {
            throw new IllegalStateException(
                    "Subreport has no report: neither report nor reportSupplier is set")
        }
        r
    }
}
