package com.report_design_lib

import com.report_design_lib.core.RendererRegistry
import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import spock.lang.Specification

class RendererRegistrySpec extends Specification {
    def cleanup() { RendererRegistry.clear() }

    static class FakeRenderer implements ReportRenderer {
        String format = 'fake'
        String getContentType() { 'text/plain' }
        String getDefaultExtension() { 'txt' }
        void render(Report r, Map p, OutputStream out) { out << 'ok'.bytes }
    }

    def "регистрирует и находит рендерер"() {
        given:
        RendererRegistry.register(new FakeRenderer())

        expect:
        RendererRegistry.available() == ['fake']
        RendererRegistry.get('fake').contentType == 'text/plain'
    }

    def "бросает исключение для неизвестного формата"() {
        when:
        RendererRegistry.get('nope')

        then:
        def e = thrown(IllegalStateException)
        e.message.contains("nope")
    }
}
