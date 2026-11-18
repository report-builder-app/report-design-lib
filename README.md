# report-design-lib

**Декларативный DSL для отчётов на Groovy: опишите один раз — рендерите в PDF, XLSX, HTML и JSON.**

[![CI](https://github.com/report-builder-app/report-design-lib/actions/workflows/publish.yml/badge.svg)](https://github.com/report-builder-app/report-design-lib/actions/workflows/publish.yml)
[![GitHub release](https://img.shields.io/github/v/release/report-builder-app/report-design-lib?sort=semver&label=release)](https://github.com/report-builder-app/report-design-lib/releases)
[![Maven Central](https://img.shields.io/badge/packages-GitHub-blue?logo=github)](https://github.com/orgs/report-builder-app/packages?repo_name=report-design-lib)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Groovy](https://img.shields.io/badge/Groovy-4.0.31-4298b8?logo=apachegroovy)](https://groovy-lang.org/)
[![JDK](https://img.shields.io/badge/JDK-17%2B-orange)](https://adoptium.net/)

## Зачем это нужно

Классические генераторы отчётов (JasperReports, BIRT, Pentaho) требуют визуального дизайнера, XML-шаблонов и тесной привязки к конкретному движку. В результате:

- **шаблоны** живут отдельно от кода и не проходят code review;
- **формат вывода** (PDF, Excel, HTML) определяется библиотекой, а не задачей;
- **источник данных** (БД, REST, Mongo) зашит в движок и плохо расширяется;
- **тестирование** отчёта требует запуска всего рендерера.

`report-design-lib` решает эти проблемы:

| Проблема | Решение |
|---|---|
| Шаблоны в XML | **Groovy DSL** — тот же язык, что и бизнес-логика |
| Один формат | **Сменные рендереры** (PDF, XLSX, HTML, JSON) — один отчёт, любой вывод |
| Привязка к СУБД | **SPI источников данных** — SQL, REST, Mongo, свой провайдер |
| Сложно тестировать | **Модель без рендерера** — тесты без OpenPDF и POI |
| Монолитная библиотека | **Модули** — подключаете только то, что нужно |

---

## Идея

**Один раз опишите отчёт в декларативном Groovy-DSL — и рендерите его в любой поддерживаемый формат, не меняя модель.**

```groovy
def report = ReportDsl.report("CustomerReport") {
    title "Customers"

    parameters {
        param("StartDate", Date)
        param("EndDate", Date)
    }

    fields {
        field("id", Long)
        field("name", String)
        field("email", String)
    }

    queryString """
        SELECT id, name, email
        FROM customers
        WHERE created_at BETWEEN \$P{StartDate} AND \$P{EndDate}
    """

    bands {
        titleBand    { label(text: "Customers", style: "title", width: 500f) }
        columnHeader {
            label(text: "ID",    width: 50f)
            label(text: "Name",  width: 200f)
            label(text: "Email", width: 250f)
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
        sql {
            jdbcUrl "jdbc:postgresql://localhost:5432/mydb"
            user "report_user"
            password "secret"
        }
    }
}

// Один отчёт — четыре формата:
Reports.renderToFile(report, 'pdf',  [StartDate: from, EndDate: to])
Reports.renderToFile(report, 'xls',  [StartDate: from, EndDate: to])
Reports.renderToFile(report, 'html', [StartDate: from, EndDate: to])
Reports.renderToFile(report, 'json', [StartDate: from, EndDate: to])
```

Ядро ничего не знает ни про конкретный рендер, ни про СУБД. Всё подключается как отдельные Maven-модули через SPI:
- Рендерер — реализация ReportRenderer в модуле report-design-renderer-*.
- Источник данных — реализация DataSourceProvider<C> в модуле-расширении.
- Новый рендер (docx, markdown, CSV) — новый модуль, core не меняется.

### 🧩 Сменные рендереры

Один и тот же `Report` рендерится в:

| Формат | Модуль | Зависимость | Изображения |
|---|---|---|---|
| **PDF** | `report-design-renderer-pdf` | OpenPDF | ✅ Поддерживаются (встраивание) |
| **XLSX** | `report-design-renderer-xls` | Apache POI | ✅ Поддерживаются (встраивание) |
| **HTML** | `report-design-renderer-html` | — | ✅ Поддерживаются (`<img src="...">` / base64) |
| **JSON** | `report-design-renderer-json` | groovy-json | ✅ Поддерживаются (как URL или base64) |

Рендереры находятся автоматически через `ServiceLoader` или регистрируются явно. Достаточно подключить нужный модуль — и `RendererRegistry.get('pdf')` уже работает.

### 🔌 Подключаемые источники данных

- **SQL** — встроенный `sql { jdbcUrl ... }`, `queryString` с `$P{...}`.
- **Web/REST** — встроенный `web { url ...; jsonPath '$.data[*]' }`.
- **MongoDB** — отдельный модуль `mongo-datasource-extension`.
- **Свой источник** — реализуйте `DataSourceProvider<CustomConfig>` и зарегистрируйте в `DataSourceRegistry`.

```groovy
// SQL
dataSource { sql { jdbcUrl "jdbc:..." } }

// REST → JSON
dataSource { web { url "https://api/orders"; jsonPath '$.data[*]' } }

// MongoDB (отдельный модуль)
dataSource {
    custom('mongodb') {
        option 'uri', 'mongodb://localhost:27017'
        option 'database', 'shop'
        option 'collection', 'orders'
    }
}
```

### 🪆 Подотчёты

Вложенные отчёты с передачей параметров, наследованием данных текущей строки и ленивыми поставщиками:

```groovy
detail {
    textField(expression: "\$F{orderId}", width: 100f)
    subreport {
        report("OrderItems") {
            queryString "SELECT * FROM order_items WHERE order_id = \$P{orderId}"
            dataSource { sql { jdbcUrl "..." } }
            bands { detail { textField(expression: "\$F{product}", width: 200f) } }
        }
        inheritData(true)     // orderId из текущей строки
    }
}
```
Циклические ссылки (A → B → A) обнаруживаются при разворачивании и бросают `IllegalStateException` вместо `StackOverflowError`.

### 🧪 Модель без рендерера

Тесты ядра **не требуют** OpenPDF, Apache POI или PDF-стека:

```groovy
def report = ReportDsl.report("R") {
    bands { detail { textField(expression: "\$F{x}", width: 100f) } }
}

def ds = new CollectionReportDataSource([[x: 'a'], [x: 'b']])
def rows = ReportFlattener.flatten(report, [:], ds)

assert rows*.cells*.value.flatten() == ['a', 'b']
```

Это ускоряет тесты и позволяет разрабатывать модель независимо от рендереров.

### 🧪 Fluent-фасад в стиле DynamicReports
Для тех, кто предпочитает цепочки — модуль report-design-dynamicreports:

```groovy
report("CustomerReport")
.columns(
column("Item", "item", String),
column("Quantity", "quantity", Integer)
)
.title("Getting started")
.setDataSource(new CustomConfig("static", [rows: data]))
.toPdf(out)
```

Строит тот же Report, что и блочный DSL. Рендеринг — через тот же RendererRegistry.

### 🧪 Конвертер JRXML → DSL
Модуль report-design-jasper-to-dsl мигрирует существующие шаблоны JasperReports:

```bash
mvn -pl report-design-jasper-to-dsl exec:java \
-Dexec.args="report.jrxml report.groovy"
```

## Почему не JasperReports / DynamicReports

| Аспект | JasperReports | report-design-lib |
|---|---|---|
| Формат шаблона | JRXML (XML) | Groovy DSL — проходит code review |
| Источник данных | JRDataSource, JNDI | SPI — SQL, REST, Mongo, свой |
| Формат вывода | PDF, XLS, HTML | PDF, XLSX, HTML, JSON + свой |
| Тестирование | требует рендерера | модель без рендерера |
| Зависимости ядра | JasperReports + OpenPDF | только Groovy |
| Расширение | subreports, скриптлеты | SPI + модули |
| Подотчёты | есть | есть, с защитой от циклов |

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
| `report-design-jasper-to-dsl` | Конвертер JRXML → Groovy DSL (CLI + библиотека) | Groovy 4, groovy-xml |
  | `report-design-dynamicreport` | Fluent-фасад в стиле DynamicReports над core | report-design-core |
 | `report-design-testkit` *(опционально)* | Тестовые провайдеры и утилиты | Groovy, Spock |

Зависимости между модулями:
```
                    ┌──────────────────────────┐
                    │    report-design-core    │
                    │  модель, DSL, SPI        │
                    └────────────┬─────────────┘
                                 │
     ┌──────────────┬────────────┼────────────┬──────────────┐
     │              │            │            │              │
     ▼              ▼            ▼            ▼              ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────────────────┐
│renderer- │ │renderer- │ │renderer- │ │renderer- │ │dynamicreports      │
│pdf       │ │xls       │ │html      │ │json      │ │(fluent-фасад)      │
│(OpenPDF) │ │(POI)     │ │(—)       │ │(—)       │ │                    │
└──────────┘ └──────────┘ └──────────┘ └──────────┘ └────────────────────┘

┌────────────────────────────┐         ┌──────────────────────────┐
│report-design-jasper-to-dsl │         │report-design-testkit     │
│(не зависит от core;        │         │(test scope)              │
│ нужен только groovy-xml)   │         └────────────┬─────────────┘
└────────────────────────────┘                      │
                                                    ▼ (используется в тестах)
                                          core / renderer-* / dynamicreports
```

### Принципы

- `core` не зависит ни от OpenPDF, ни от POI, ни от какого-либо рендерера. Это позволяет тестировать модель и DSL без тяжёлых PDF/Excel-библиотек.
- Каждый рендерер — отдельный модуль. Приложение подключает только те, что ему нужны. `renderer-html` и `renderer-json `не тянут внешних зависимостей; renderer-pdf тянет OpenPDF, renderer-xls — Apache POI.
- `renderer-*` зависит от core, но не наоборот. core предоставляет интерфейс `ReportRenderer` и `RendererRegistry`; реализации находятся в модулях.
- `report-design-jasper-to-dsl` не зависит от core — конвертер генерирует текст Groovy-DSL и не строит объекты модели. Это позволяет запускать его как автономную утилиту.
- `report-design-dynamicreports` зависит только от core — предоставляет fluent-фасад над тем же Report. Рендеринг — через тот же RendererRegistry.
- `report-design-testkit` — опциональный модуль только для test scope. Содержит `StaticProvider`, `InMemoryProvider`, `FakeRenderer` и другие утилиты, общие для тестов всех модулей.

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
    <version>0.1.2</version>
</dependency>

        <!-- нужные рендереры -->
<dependency>
    <groupId>com.report_design_libcom.report_design_lib</groupId>
    <artifactId>report-design-renderer-pdf</artifactId>
    <version>0.1.2</version>
</dependency>
<dependency>
    <groupId>com.report_design_libcom.report_design_lib</groupId>
    <artifactId>report-design-renderer-html</artifactId>
    <version>0.1.2</version>
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

## Публикация и релизы

### GitHub Packages

[![packages](https://img.shields.io/badge/packages-GitHub-blue?logo=github)](https://github.com/orgs/report-builder-app/packages?repo_name=report-design-lib)
[![release](https://img.shields.io/github/v/release/report-builder-app/report-design-lib?sort=semver&label=latest%20release)](https://github.com/report-builder-app/report-design-lib/releases)

Все модули публикуются в GitHub Packages организации `report-builder-app`:

- **Страница пакетов:** https://github.com/orgs/report-builder-app/packages?repo_name=report-design-lib
- **Maven-репозиторий:** `https://maven.pkg.github.com/report-builder-app/report-design-lib`

Публикация выполняется автоматически при пуше тега `v*` (см. `.github/workflows/publish.yml`).

### Доступные модули и артефакты

| Модуль  | Версия | Артефакт |
|---|---|---|
| `report-design-core`  | ![core](https://img.shields.io/badge/core-1.0.0-blue) | `com.report_design:report-design-core` |
| `report-design-renderer-pdf` | ![pdf](https://img.shields.io/badge/pdf-1.0.0-red) | `com.report_design:report-design-renderer-pdf` |
| `report-design-renderer-xls` | ![xls](https://img.shields.io/badge/pdf-1.0.0-red) | `com.report_design:report-design-renderer-xls` |
| `report-design-renderer-html` | ![html](https://img.shields.io/badge/pdf-1.0.0-red) | `com.report_design:report-design-renderer-html` |
| `report-design-renderer-json` | ![json](https://img.shields.io/badge/pdf-1.0.0-red) | `com.report_design:report-design-renderer-json` |
| `report-design-jasper-to-dsl`  | ![converter](https://img.shields.io/badge/xls-1.0.0-green) | `com.report_design:report-design-jasper-to-dsl` |
| `report-design-dynamicreports`  | ![dynamicreports](https://img.shields.io/badge/html-1.0.0-yellow) | `com.report_design:report-design-dynamicreports` |

Список версий — на странице пакетов: https://github.com/orgs/report-builder-app/packages?repo_name=report-design-lib

### Подключение пакетов

Добавьте репозиторий и зависимость в `pom.xml` приложения:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/report-builder-app/report-design-lib</url>
        <snapshots><enabled>true</enabled></snapshots>
        <releases><enabled>true</enabled></releases>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.report_design</groupId>
        <artifactId>report-design-core</artifactId>
        <version>0.1.2</version>
    </dependency>
    <dependency>
        <groupId>com.report_design</groupId>
        <artifactId>report-design-renderer-pdf</artifactId>
        <version>0.1.2</version>
    </dependency>
</dependencies>
```

Для разрешения зависимостей нужен Personal Access Token (PAT) с правом read:packages. Добавьте сервер в `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
        </server>
    </servers>
</settings>
```
id в settings.xml (github) должен совпадать с id репозитория в pom.xml

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
