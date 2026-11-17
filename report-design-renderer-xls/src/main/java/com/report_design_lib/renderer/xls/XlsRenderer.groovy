package com.report_design_lib.renderer.xls

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import com.report_design_lib.core.render.support.ReportFlattener

class XlsRenderer implements ReportRenderer {
    String getFormat() { 'xls' }
    String getContentType() { 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }
    String getDefaultExtension() { 'xlsx' }

    @Override
    void render(Report report, Map<String, Object> runtimeParams, OutputStream out) {
        def wb = new XSSFWorkbook()
        try {
            def sheet = wb.createSheet(report.name ?: 'Report')
            int r = 0
            ReportFlattener.flatten(report, runtimeParams).each { row ->
                def excelRow = sheet.createRow(r++)
                int c = 0
                row.cells.each { cell ->
                    if (cell.isLine) { c++; return }
                    def excelCell = excelRow.createCell(c++)
                    excelCell.setCellValue(cell.value?.toString() ?: "")
                }
            }
            wb.write(out)
        } finally {
            wb.close()
        }
    }
}
