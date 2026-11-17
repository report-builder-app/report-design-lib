# report-design-lib

DSL для описания отчётов на Groovy с подключаемыми источниками данных и сменными рендерерами (PDF, XLSX, HTML, JSON).

Идея: один раз описать отчёт в декларативном Groovy-DSL — и рендерить его в любой поддерживаемый формат, не меняя модель. Ядро ничего не знает про конкретный рендер или СУБД: всё подключается как отдельные Maven-модули.

---

## Содержание

- [Модули](#модули)
- [Требования](#требования)
- [Быстрый старт](#быстрый-старт)
- [DSL: описание отчёта](#dsl-описание-отчёта)
- [Источники данных](#источники-данных)
- [Рендеринг](#рендеринг)
- [Расширение](#расширение)
- [Сборка и тесты](#сборка-и-тесты)
- [Лицензия](#лицензия)

---

## Модули

| Модуль | Назначение | Ключевые зависимости |
|---|---|---|
| `report-design-core` | Модель, DSL, SPI источников данных и рендереров | Groovy 4, groovy-json |
| `report-design-renderer-pdf` | Рендеринг в PDF | OpenPDF |
| `report-design-renderer-xls` | Рендеринг в XLSX | Apache POI |
| `report-design-renderer-html` | Рендеринг в HTML | — |
| `report-design-renderer-json` | Рендеринг в JSON | groovy-json |
| `report-design-testkit` *(опционально)* | Тестовые провайдеры и утилиты | Groovy, Spock |

Зависимости между модулями:
core ← renderer-pdf
← renderer-xls
← renderer-html
← renderer-json
← testkit

`core` не зависит ни от OpenPDF, ни от POI, ни от какого-либо рендерера.

---

## Требования

- JDK 17+ (проверено на JDK 21)
- Maven 3.9+
- Groovy 4.0.31 (подтягивается транзитивно)

---

## Быстрый старт

### 1. Подключение

В `pom.xml` приложения:

```xml
<dependency>
    <groupId>com.report_design_libcom.report_design_lib</groupId>
    <artifactId>report-design-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

        <!-- нужные рендереры -->
<dependency>
<groupId>com.report_design_libcom.report_design_lib</groupId>
<artifactId>report-design-renderer-pdf</artifactId>
<version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
<groupId>com.report_design_libcom.report_design_lib</groupId>
<artifactId>report-design-renderer-html</artifactId>
<version>1.0-SNAPSHOT</version>
</dependency>
```

Рендереры регистрируются автоматически через ServiceLoader при первом обращении к RendererRegistry.

```groovy
import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.render.Reports

def report = ReportDsl.report("HelloReport") {
    title "Hello, world"

    bands {
        titleBand  { label(text: "Customers", width: 400f) }
        detail     { textField(expression: "\$F{name}", width: 200f) }
    }

    // источник данных — см. раздел «Источники данных»
    dataSource {
        custom('static') {
            option 'rows', [[name: "Alice"], [name: "Bob"]]
        }
    }
}

Reports.renderToFile(report, 'pdf', [:])
Reports.renderToFile(report, 'html', [:])
Reports.renderToFile(report, 'json', [:])
```

## DSL: описание отчёта

Точка входа — ReportDsl.report(name) { ... }. Внутри доступны секции:

```groovy
ReportDsl.report("CustomerReport") {
    title "Customer List"
    pageFormat PageFormat.A4
    orientation PageOrientation.LANDSCAPE

    parameters {
        param("StartDate", Date)
        param("EndDate", Date)
        param("Title", String, "Customers")   // со значением по умолчанию
    }

    fields {
        field("id", Long)
        field("name", String)
        field("email", String)
    }

    queryString """
        SELECT id, first_name AS name, email
        FROM customers
        WHERE created_at BETWEEN \$P{StartDate} AND \$P{EndDate}
    """

    styles {
        style("titleStyle") {
            font { size = 18; isBold = true }
            horizontalAlignment = Alignment.CENTER
        }
        style("header") {
            font { isBold = true }
        }
    }

    bands {
        titleBand    { label(text: "\$P{Title}", style: "titleStyle", width: 500f) }
        columnHeader {
            label(text: "ID",    width: 50f,  style: "header")
            label(text: "Name",  width: 200f, style: "header")
            label(text: "Email", width: 250f, style: "header")
        }
        detail {
            textField(expression: "\$F{id}",    width: 50f)
            textField(expression: "\$F{name}",  width: 200f)
            textField(expression: "\$F{email}", width: 250f)
        }
        summary {
            line(y: 0f, width: 500f)
            textField(expression: "\$V{REPORT_COUNT}", width: 100f)
        }
    }

    dataSource {
        // см. ниже
    }
}
```

## Секции

| Секция | Методы | Назначение |
|---|---|---|
| `title(...)` | — | Заголовок отчёта |
| `pageFormat(...)` | `PageFormat.A4/A5/LETTER` | Формат страницы |
| `orientation(...)` | `PageOrientation.PORTRAIT/LANDSCAPE` | Ориентация |
| `parameters { }` | `param(name, type[, default])` | Параметры |
| `fields { }` | `field(name, type)` | Поля из datasource |
| `queryString(...)` | — | SQL-запрос для SQL-источника |
| `styles { }` | `style(name) { font { ... }; ... }` | Стили |
| `bands { }` | `titleBand`, `columnHeader`, `detail`, `summary` | Полосы отчёта |
| `dataSource { }` | `sql`, `web`, `custom` | Источник данных |

### Подотчёты

В любой бэнд можно вложить другой отчёт:

```groovy
bands {
    detail {
        textField(expression: "\$F{orderId}", width: 100f)
        subreport {
            report("OrderItems") {
                queryString "SELECT * FROM order_items WHERE order_id = \$P{orderId}"
                dataSource { sql { jdbcUrl "..." } }
                fields { field("product", String) }
                bands { detail { textField(expression: "\$F{product}", width: 200f) } }
            }
            inheritData(true)   // параметры текущей строки родителя
            width 400f
        }
    }
}
```

Подотчёт разворачивается в плоский список строк на этапе `ReportFlattener`, поэтому его поддерживают все рендереры без изменений в модели. Циклические ссылки (A → B → A) обнаруживаются при разворачивании и бросают `IllegalStateException`

Подотчёты добавляются **в core** и не требуют изменений в рендерерах: `SubreportElement` — обычный `ReportElement`, а `ReportFlattener` разворачивает его в вложенные `subRows`. DSL получает метод `subreport { ... }`, через который можно вложить другой DSL-отчёт (`report("Name") { ... }`), готовый `Report` или ленивый поставщик (`lazy { ... }`). Параметры подотчёта — явные (`param("k", v)`) плюс опциональный проброс данных родителя (`inheritData(true)`). Защита от рекурсии реализуется на этапе flatten, поэтому цикл A → B → A диагностируется понятным исключением, а не `StackOverflowError`.

### Элементы внутри band'ов
Любой band поддерживает единый API:

```groovy
titleBand {
    label(text: "...", style: "...", width: 100f, height: 20f)
}
detail {
    textField(expression: "\$F{field}", width: 100f)
}
summary {
    line(y: 0f, width: 500f)
}
```

Общие поля всех элементов: x, y, width, height, style.

## Выражения
- `$F{field}` — значение поля из текущей строки источника данных.
- `$P{param}` — значение параметра отчёта.
- `$V{...}` — системные переменные (например, REPORT_COUNT).

Всё остальное возвращается как литерал (для HTML/JSON-рендереров).

## Источники данных

Все источники реализуют SPI `DataSourceProvider<C extends DataSourceConfig>`.

Регистрация — явная через `DataSourceRegistry.register(...)` или через `ServiceLoader`.

#### SQL (queryString)
```groovy
dataSource {
    sql {
        jdbcUrl "jdbc:postgresql://localhost:5432/mydb"
        user "report_user"
        password "secret"
        // query берётся из report.queryString, если не задан здесь
    }
}
```

Провайдер `SqlDataSourceProvider` выполняет запрос, подставляет `$P{...}` (с экранированием строк), возвращает `ReportDataSource`.

#### Web (REST + JSON)

```groovy
dataSource {
    web {
        url "https://api.example.com/orders"
        method "GET"
        header "Authorization", "Bearer \${API_TOKEN}"
        header "Accept", "application/json"
        queryParam "from", "\${StartDate}"
        queryParam "to",   "\${EndDate}"
        jsonPath '$.data[*]'      // путь к массиву данных
    }
}
```

#### Custom (MongoDB, Kafka, произвольный источник)
В core нет встроенного Mongo-провайдера — пользователь пишет свой и регистрирует его.

```groovy
dataSource {
    custom('mongodb') {
        option 'uri',       'mongodb://localhost:27017'
        option 'database',  'shop'
        option 'collection','orders'
        option 'filterJson','{ "createdAt": { "$gte": "\${From}" } }'
    }
}
```

Реализация провайдера — в отдельном модуле приложения:
```groovy
class MongoDataSourceProvider implements DataSourceProvider<CustomConfig> {
    String getName() { 'mongodb' }
    Class<CustomConfig> getConfigType() { CustomConfig }

    ReportDataSource create(CustomConfig cfg, Map<String, Object> params) {
        // ... подключение к Mongo, выполнение запроса
        new CollectionReportDataSource(rows)
    }
}

DataSourceRegistry.register(new MongoDataSourceProvider())
```

## Рендеринг

### Программно

```groovy
import com.report_design_lib.core.render.RendererRegistry
import com.report_design_lib.core.render.Reports

// 1. Список доступных форматов
assert RendererRegistry.available().containsAll(['pdf', 'xls', 'html', 'json'])

// 2. В байты
byte[] pdf = Reports.renderToBytes(report, 'pdf', [StartDate: from, EndDate: to])

// 3. В файл
Reports.renderToFile(report, 'html', [StartDate: from, EndDate: to])

// 4. Напрямую через рендерер
def renderer = RendererRegistry.get('json')
response.setContentType(renderer.contentType)
renderer.render(report, params, response.outputStream)
```

### Форматы

| Формат | Content-Type | Расширение | Зависимость |
|---|---|---|---|
| `pdf` | `application/pdf` | `.pdf` | OpenPDF |
| `xls` | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` | `.xlsx` | Apache POI |
| `html` | `text/html; charset=UTF-8` | `.html` | — |
| `json` | `application/json; charset=UTF-8` | `.json` | groovy-json |


## Расширение

### Свой рендерер
1. Создать Maven-модуль с зависимостью на report-design-core.
2. Реализовать ReportRenderer:
```groovy
package com.example.renderer.markdown

import com.report_design_lib.core.Report
import com.report_design_lib.core.render.ReportRenderer
import com.report_design_lib.core.render.support.ReportFlattener

class MarkdownRenderer implements ReportRenderer {
    String getFormat()            { 'md' }
    String getContentType()       { 'text/markdown; charset=UTF-8' }
    String getDefaultExtension()  { 'md' }

    void render(Report report, Map<String, Object> params, OutputStream out) {
        def w = new OutputStreamWriter(out, 'UTF-8')
        w.write("# ${report.title ?: report.name}\n\n")
        ReportFlattener.flatten(report, params).each { row ->
            w.write(row.cells.collect { it.value ?: '' }.join(' | '))
            w.write('\n')
        }
        w.flush()
    }
}
```
3. Добавить `src/main/resources/META-INF/services/com.report_design_lib.core.render.ReportRenderer`:
```text
com.example.renderer.markdown.MarkdownRenderer
```
4. Подключить модуль в приложении — он появится в `RendererRegistry.available()`.

### Свой источник данных
Аналогично: реализовать `DataSourceProvider<CustomConfig>` и зарегистрировать в `DataSourceRegistry`.

Ядро при этом не меняется — расширение только через модули и SPI.

## Сборка и тесты

```shell
# Полная сборка и тесты
mvn -U clean install

# Только core
mvn -pl report-design-core test

# Конкретный модуль рендерера
mvn -pl report-design-renderer-pdf test
```

Проект использует Spock 2.4 (Groovy 4) поверх JUnit Platform 1.9. Тесты core не требуют ни OpenPDF, ни POI — это следствие архитектурного разделения модели и рендеринга.

### Структура тестов

```text
report-design-core/src/test/groovy/
├── ReportDslSpec                 — базовое построение отчёта
├── ReportSectionsSpec            — parameters/fields/styles/bands
├── ReportDataSourceDslSpec       — секция dataSource
├── DataSourceRegistrySpec        — реестр провайдеров
├── SqlDataSourceProviderSpec     — SQL-провайдер на H2
├── WebDataSourceProviderSpec     — Web-провайдер на HttpServer
└── CustomProviderSpec            — кастомный провайдер (эмуляция Mongo)
```

## Лицензия

Apache License 2.0 — см. файл LICENSE

JasperReports® and JRXML are trademarks or registered trademarks of Cloud Software Group, Inc.
or its subsidiaries (formerly TIBCO Software Inc. / Jaspersoft Corporation).
This project is an independent converter tool and is not affiliated with, endorsed by,
or sponsored by Cloud Software Group, Inc.

## Контрибьютинг

1. Форкните репозиторий.
2. Создайте ветку feature/... или fix/....
3. Добавьте тесты (Spock) на новое поведение.
4. Убедитесь, что mvn -U clean test проходит во всех модулях.
5. Отправьте pull request.

Новые источники данных и рендереры приветствуются как отдельные модули — core расширять не нужно.
