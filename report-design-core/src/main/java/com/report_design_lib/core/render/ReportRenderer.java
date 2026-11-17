package com.report_design_lib.core.render;

import com.report_design_lib.core.Report;

import java.io.OutputStream;
import java.util.Map;

public interface ReportRenderer {
    /** Код формата: 'pdf', 'xls', 'html', 'json', ... */
    String getFormat();

    /** MIME-тип для HTTP-ответа. */
    String getContentType();

    /** Расширение файла по умолчанию. */
    String getDefaultExtension();

    /** Отрендерить отчёт в поток. */
    void render(Report report, Map<String, Object> runtimeParams, OutputStream out);
}
