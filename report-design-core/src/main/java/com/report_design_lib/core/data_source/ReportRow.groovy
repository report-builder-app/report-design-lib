package com.report_design_lib.core.data_source

class ReportRow implements Map<String, Object> {
    private final Map<String, Object> data
    ReportRow(Map<String, Object> data) { this.data = data ?: [:] }

    // Делегируем Map-интерфейс
    @Override Object get(Object key) { data.get(key) }
    @Override int size() { data.size() }
    @Override boolean isEmpty() { data.isEmpty() }
    @Override boolean containsKey(Object key) { data.containsKey(key) }
    @Override boolean containsValue(Object value) { data.containsValue(value) }
    @Override Object put(String key, Object value) { data.put(key, value) }
    @Override Object remove(Object key) { data.remove(key) }
    @Override void putAll(Map<? extends String, ?> m) { data.putAll(m) }
    @Override void clear() { data.clear() }
    @Override Set<String> keySet() { data.keySet() }
    @Override Collection<Object> values() { data.values() }
    @Override Set<Map.Entry<String, Object>> entrySet() { data.entrySet() }
}
