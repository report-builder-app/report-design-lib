package com.report_design_lib.interfaces;

import com.report_design_lib.core.data_source.ReportRow;

public interface ReportDataSource {
    /** Перейти к следующей строке. true — есть данные. */
    boolean next();
    /** Текущая строка. Вызывается после next() == true. */
    ReportRow getRow();
}
