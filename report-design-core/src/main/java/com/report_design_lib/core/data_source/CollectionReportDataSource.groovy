package com.report_design_lib.core.data_source

import com.report_design_lib.interfaces.ReportDataSource

class CollectionReportDataSource implements ReportDataSource {
    private final Iterator<ReportRow> iterator
    private ReportRow current

    CollectionReportDataSource(Collection<Map<String, Object>> rows) {
        this.iterator = (rows ?: []).collect { new ReportRow(it as Map<String, Object>) }.iterator()
    }

    boolean next() {
        if (iterator.hasNext()) { current = iterator.next(); return true }
        current = null
        false
    }

    ReportRow getRow() { current }
}
