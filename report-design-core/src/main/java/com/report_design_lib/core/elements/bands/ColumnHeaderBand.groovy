package com.report_design_lib.core.elements.bands

import com.report_design_lib.core.elements.BandType

class ColumnHeaderBand extends Band {
    @Override BandType getType() { BandType.COLUMN_HEADER }
}
