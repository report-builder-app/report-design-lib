package com.report_design_lib.core

import com.report_design_lib.core.elements.Bands
import com.report_design_lib.core.elements.Fields
import com.report_design_lib.core.elements.PageFormat
import com.report_design_lib.core.elements.PageOrientation
import com.report_design_lib.core.elements.Parameters
import com.report_design_lib.core.elements.Styles
import com.report_design_lib.interfaces.DataSourceConfig

class Report {
    String name
    String title
    PageFormat pageFormat = PageFormat.A4
    PageOrientation orientation = PageOrientation.PORTRAIT
    Parameters parameters
    Fields fields
    String queryString
    Styles styles
    Bands bands

    DataSourceConfig dataSource

    Report(String name) {
        this.name = name
    }
}
