package com.report_design_lib

import com.report_design_lib.core.data_source.SqlConfig
import com.report_design_lib.core.data_source.SqlDataSourceProvider
import spock.lang.Specification
import java.sql.DriverManager

class SqlDataSourceProviderSpec extends Specification {

    static {
        // H2 в памяти с тестовыми данными
        def c = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE", "sa", "")
        c.createStatement().execute("CREATE TABLE users (id INT, name VARCHAR(50))")
        c.createStatement().execute("INSERT INTO users VALUES (1, 'Alice'), (2, 'Bob')")
        c.close()
    }

    def "извлекает строки из H2"() {
        given:
        def cfg = new SqlConfig(
                jdbcUrl: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                user: "sa", password: "",
                queryString: "SELECT id, name FROM users ORDER BY id"
        )
        def provider = new SqlDataSourceProvider()

        when:
        def ds = provider.create(cfg, [:])
        def rows = []
        while (ds.next()) rows << ds.row

        then:
        rows.size() == 2
        rows[0].id == 1
        rows[0].name == 'Alice'
        rows[1].id == 2
        rows[1].name == 'Bob'
    }

    def "подставляет \$P{} параметры в SQL"() {
        given:
        def cfg = new SqlConfig(
                jdbcUrl: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                user: "sa", password: "",
                queryString: "SELECT id, name FROM users WHERE id = \$P{userId}"
        )
        def provider = new SqlDataSourceProvider()

        when:
        def ds = provider.create(cfg, [userId: 2])
        def rows = []
        while (ds.next()) rows << ds.row

        then:
        rows.size() == 1
        rows[0].name == 'Bob'
    }

    def "экранирует строковые параметры от SQL-инъекций"() {
        given:
        def cfg = new SqlConfig(
                jdbcUrl: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                user: "sa", password: "",
                queryString: "SELECT id, name FROM users WHERE name = \$P{name}"
        )
        def provider = new SqlDataSourceProvider()

        when:
        def ds = provider.create(cfg, [name: "O'Reilly"])
        def rows = []
        while (ds.next()) rows << ds.row

        then:
        noExceptionThrown()
        rows.isEmpty()
    }
}
