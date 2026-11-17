package com.report_design_lib.core.elements

class Styles {
    private final Map<String, Style> styles = [:]

    Styles style(String name, @DelegatesTo(Style) Closure cl) {
        def s = new Style(name: name)
        cl.delegate = s
        cl.resolveStrategy = Closure.DELEGATE_FIRST
        cl()
        styles[name] = s
        this
    }

    Map<String, Style> getMap() { Collections.unmodifiableMap(styles) }
    int size()                  { styles.size() }
    boolean isEmpty()           { styles.isEmpty() }
    Style get(String name)      { styles[name] }
    boolean contains(String n)  { styles.containsKey(n) }
}
