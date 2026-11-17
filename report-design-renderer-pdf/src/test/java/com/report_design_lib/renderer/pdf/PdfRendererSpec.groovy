package com.report_design_lib.renderer.pdf

import com.report_design_lib.core.ReportDsl
import spock.lang.Specification

class PdfRendererSpec extends Specification {
    def "рендерит непустой PDF"() {
        given:
        def report = ReportDsl.report("R") {
            bands { titleBand { label(text: "Hello", width: 100f) } }
        }
        def out = new ByteArrayOutputStream()

        when:
        new PdfRenderer().render(report, [:], out)

        then:
        out.size() > 0
        new String(out.toByteArray(), 0, 4) == "%PDF"
    }
}
