package com.report_design_lib.core

class Bands {
    TitleBand titleBand
    ColumnHeaderBand columnHeader
    DetailBand detail
    SummaryBand summary

    def titleBand(@DelegatesTo(TitleBand) Closure cl) {
        titleBand = new TitleBand()
        cl.delegate = titleBand
        cl()
    }

    def columnHeader(@DelegatesTo(ColumnHeaderBand) Closure cl) {
        columnHeader = new ColumnHeaderBand()
        cl.delegate = columnHeader
        cl()
    }

    def detail(@DelegatesTo(DetailBand) Closure cl) {
        detail = new DetailBand()
        cl.delegate = detail
        cl()
    }

    def summary(@DelegatesTo(SummaryBand) Closure cl) {
        summary = new SummaryBand()
        cl.delegate = summary
        cl()
    }
}
