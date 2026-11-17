package com.report_design_lib.core.elements.bands

import com.report_design_lib.core.elements.BandType

class SummaryBand extends Band {
    @Override BandType getType() { BandType.SUMMARY }
}
