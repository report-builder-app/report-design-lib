package com.report_design_lib.core.elements.bands

import com.report_design_lib.core.elements.BandType

class Bands {
    TitleBand titleBand
    ColumnHeaderBand columnHeader
    DetailBand detail
    SummaryBand summary

    Bands titleBand(@DelegatesTo(TitleBand) Closure cl) {
        titleBand = new TitleBand()
        cl.delegate = titleBand
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        this
    }

    Bands columnHeader(@DelegatesTo(ColumnHeaderBand) Closure cl) {
        columnHeader = new ColumnHeaderBand()
        cl.delegate = columnHeader
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        this
    }

    Bands detail(@DelegatesTo(DetailBand) Closure cl) {
        detail = new DetailBand()
        cl.delegate = detail
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        this
    }

    Bands summary(@DelegatesTo(SummaryBand) Closure cl) {
        summary = new SummaryBand()
        cl.delegate = summary
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        this
    }

    // ---- удобные аксессоры ----

    /** Непустые бэнды в семантическом порядке. */
    List<Band> getList() {
        [titleBand, columnHeader, detail, summary].findAll { it != null }
    }

    int size() { getList().size() }
    boolean isEmpty() { getList().isEmpty() }

    boolean hasTitle()        { titleBand != null }
    boolean hasColumnHeader() { columnHeader != null }
    boolean hasDetail()       { detail != null }
    boolean hasSummary()      { summary != null }

    Band get(BandType type) {
        switch (type) {
            case BandType.TITLE:         return titleBand
            case BandType.COLUMN_HEADER: return columnHeader
            case BandType.DETAIL:        return detail
            case BandType.SUMMARY:       return summary
            default: throw new IllegalArgumentException("Unknown band: $type")
        }
    }
}
