# CLAUDE.md — Project Context for Claude Code

This file is read automatically by Claude Code at the start of every session.
Do not delete or rename it.

---

## Project Overview

**Repository:** https://github.com/geki-research/knime-extensions
**Owner:** geki-research
**Purpose:** KNIME 5.x community extension nodes for advanced data ingestion
and transformation.

---

## Development Environment

| Item | Detail |
|---|---|
| OS | Debian 12 |
| Java | OpenJDK 17 (required — KNIME 5.x is strict about this) |
| Build system | Maven 3.9+ with Eclipse Tycho 4.0.6 |
| IDE | Eclipse for RCP and RAP Developers 2024-03 |
| Eclipse workspace | `~/knime-dev/workspace` |
| KNIME target platform | `~/knime-dev/knime-sdk-setup` → `KNIME-AP.target` (1897 plugins) |

---

## Repository Structure

```
knime-extensions/
  CLAUDE.md                                     ← this file
  README.md                                     ← project documentation
  pom.xml                                       ← parent POM (Tycho config)
  org.geki.knime.excelformreader/               ← Excel Reader (non-tabular) plugin
  org.geki.knime.excelformreader.tests/         ← test project
  org.geki.knime.excelformreader.feature/       ← feature project
  org.geki.knime.excelformreader.update/        ← update site
```

---

## Test Project

```
org.geki.knime.excelformreader.tests/
  testdata/
    forms/          ← .xlsx test files
    definitions/    ← CSV form definition tables
  src/org/geki/knime/excelformreader/tests/
                    ← JUnit test classes (to be added)
```

Test fixture: Legacy_IT_System_Assessment_Test.xlsx
  - 2 data sheets: Test_01, Test_02 (identical layout)
  - 1 excluded sheet: Config
  - Data types covered: string, int, date
  - Data types not covered (unit tests only): double, boolean

Form definition: it_assessment_form_definition.csv
  - 40 field mappings
  - Covers all cell address patterns used in the real form

---

## Build

```bash
# Full build
cd ~/knime-dev/knime-extensions
mvn clean verify

# Build output (update site) is at:
org.geki.knime.excelformreader.update/target/repository/
```

BUILD SUCCESS is the only acceptable outcome before committing.
Always run the build and confirm success before committing any code changes.

---

## Git Conventions

**Branching strategy:** GitHub Flow
- `main` — always releasable, always passing build
- `develop` — integration branch
- `feature/<name>` — one branch per node or feature

**Commit message format:**
```
<type>: <short description>

Types: feat | fix | refactor | chore | docs | test
Examples:
  feat: implement CellValueConverter for all supported data types
  fix: handle merged cells in ExcelFormExtractor
  chore: update Tycho version to 4.0.7
```

**Before every commit:**
1. `mvn clean verify` must produce BUILD SUCCESS
2. `git status` must show only intentional changes
3. Push to remote immediately after committing

---

## Plugin Project: Excel Reader (non-tabular)

**Plugin ID:** `org.geki.knime.excelformreader`
**Node name:** Excel Reader (non-tabular)
**Category:** `/community-extensions/io`
**KNIME API version:** 5.3.x

### What this node does
Reads non-tabular, form-structured Excel worksheets (.xlsx) and extracts
field values into a standard KNIME data table. The form structure
(label → cell address mapping) is provided via an input table, making
the node generic and reusable across any form layout.

### Node ports
| Port | Direction | Type | Description |
|---|---|---|---|
| 0 | Input | BufferedDataTable | Form definition table |
| 0 | Output | BufferedDataTable | Extracted data |

### Form definition table schema
| Column | Required | Type | Description |
|---|---|---|---|
| `Name` | ✅ | String | Output column name |
| `Cell Range` | ✅ | String | Cell address (`C4`) or range (`B10:D15`) |
| `Content Type` | ❌ | String | `data` / `label` — defaults to `data` |
| `Data Type` | ❌ | String | `string`/`int`/`double`/`date`/`boolean` — defaults to `string` |

### Reading modes
| Mode | Description |
|---|---|
| `SINGLE_FILE` + single sheet | One file, one named sheet |
| `SINGLE_FILE` + all sheets | One file, all sheets except excluded ones |
| `FOLDER` | All .xlsx files in a folder, all sheets per file |
| `FOLDER` + recursive | Same, including all subfolders |

Each (file, sheet) pair = one form instance = one output row (wide mode).

### Output formats
- **Wide:** one row per (file, sheet), one column per field
- **Long:** one row per (file, sheet, field) — columns: `field_name`, `value`
- Configurable via dialog toggle

### Dialog settings
| Panel | Setting | Type | Default |
|---|---|---|---|
| File/Folder | Reading mode | Enum | SINGLE_FILE |
| File/Folder | Path | String | — |
| File/Folder | Include subfolders | Boolean | false |
| Sheet | Default sheet name | String | first sheet |
| Sheet | Excluded sheet names | String (comma-separated) | `Config` |
| Output | Output format | Enum | Wide |
| Output | Add provenance columns | Boolean | true |
| Output | Range delimiter | String | `, ` |
| Output | Include label fields in port 0 | Boolean | false |
| Output | Output label fields in port 1 | Boolean | true |
| Error handling | On missing cell | Enum | WARN |
| Error handling | On unparseable value | Enum | WARN |

### Output Ports

**Port 0 — Main output:**
- Always contains data fields (`Content Type = "data"`)
- Optionally contains label fields (`Content Type = "label"`) — controlled by "Include label fields in port 0" toggle (default: false)
- Wide or long format per dialog setting
- Definition table order preserved throughout

**Port 1 — Label fields output:**
- Fixed wide format regardless of Port 0 format setting
- One row per label field per (file, sheet) pair
- Fixed columns (in order): `Source File` (if provenance enabled), `Sheet Name` (if provenance enabled), `Name`, `Cell Range`, `Cell Content`
- Controlled by "Output label fields in port 1" toggle (default: true)
- Always produced as an empty table when toggle is disabled

---

## Package Structure & Class Responsibilities

```
org.geki.knime.excelformreader/
  ExcelFormReaderNodeFactory    — Node registration, XML description
  ExcelFormReaderNodeModel      — configure(), execute() — orchestrator only,
                                  no business logic here
  ExcelFormReaderNodeDialog     — Swing dialog panels
  ExcelFormReaderSettings       — All SettingsModel fields, save/load

  domain/
    FieldMapping                — One row from the definition table
                                  (fieldName, valueCell, dataType, sheetName)
    FormDefinition              — List<FieldMapping>, static factory
                                  fromDataTable(BufferedDataTable)
    CellAddress                 — Parses "C4" / "B10:D15" into typed fields,
                                  validates format
    ReadingMode                 — Enum: SINGLE_FILE, FOLDER

  excel/
    WorkbookIterator            — Lazy Iterator<Entry> over (Path, Sheet) pairs
                                  Respects excluded sheets, recursive flag
                                  Opens/closes workbooks one at a time
    ExcelFormExtractor          — POI-based: resolves FormDefinition against
                                  a Sheet → Map<String, DataCell>
                                  Evaluates formulas transparently
    CellValueConverter          — POI Cell → KNIME DataCell per data_type
                                  Handles string/int/double/date/boolean

  output/
    OutputSpecFactory           — Creates DataTableSpec at configure() time
                                  createWideSpec() / createLongSpec()
    WideOutputBuilder           — One DataRow per (file, sheet)
    LongOutputBuilder           — N DataRows per (file, sheet, field)
```

---

## Key Implementation Rules

1. **No business logic in NodeModel or NodeDialog** — they orchestrate and
   delegate only. All logic lives in domain/, excel/, output/.

2. **WorkbookIterator must be lazy** — open one workbook at a time, close
   it before opening the next. Never load all workbooks into memory.

3. **OutputSpecFactory runs at configure() time** — this gives KNIME
   downstream spec knowledge before execution. The definition table is
   available at configure() via the input port spec.

4. **Formula evaluation is transparent** — CellValueConverter always uses
   a FormulaEvaluator. Never return formula strings.

5. **Cell ranges (B10:D15)** — read left-to-right, top-to-bottom,
   concatenated with the configured range delimiter.

6. **Missing/unresolvable cells** — never throw unchecked exceptions.
   Honour the error handling settings (FAIL vs WARN+missing value).

7. **Apache POI is provided by KNIME** — do NOT add POI as a Maven
   dependency. It is declared in MANIFEST.MF as `Require-Bundle`.

8. **All SettingsModel types** — use only KNIME SettingsModel* classes
   (SettingsModelString, SettingsModelBoolean, etc.) in Settings class.
   Never use raw strings/booleans for persistent settings.

9. **Provenance columns** — `source_file` (StringCell) and `sheet_name`
   (StringCell) are always the first two columns when enabled.

10. **Sheet exclusion** — comparison is case-insensitive and trimmed.

---

## Apache POI Notes

POI is bundled inside KNIME — use these classes:
```java
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellRangeAddress;
```

Open workbooks with try-with-resources:
```java
try (Workbook wb = WorkbookFactory.create(file.toFile(), null, true)) {
    // true = read-only mode, much more memory efficient
}
```

---

## KNIME API Notes

```java
// Correct NodeModel constructor for 1 input, 1 output:
super(new PortType[]{BufferedDataTable.TYPE},
      new PortType[]{BufferedDataTable.TYPE});

// DataCell types to use:
StringCell, IntCell, DoubleCell, BooleanCell, DateAndTimeCell, MissingCell

// Always use DataType.getMissingCell() for missing values — never null.

// BufferedDataContainer pattern in execute():
BufferedDataContainer container = exec.createDataContainer(spec);
container.addRowToTable(row);
container.close();
return new BufferedDataTable[]{container.getTable()};
```

---

## What Is Not Yet Implemented

All Java classes currently exist as stubs with TODO comments.
Implementation order (start from the bottom up):

1. `ExcelFormReaderSettings`
2. `CellAddress`
3. `FieldMapping` + `FormDefinition`
4. `ReadingMode`
5. `CellValueConverter`
6. `ExcelFormExtractor`
7. `WorkbookIterator`
8. `OutputSpecFactory`
9. `WideOutputBuilder` + `LongOutputBuilder`
10. `ExcelFormReaderNodeModel`
11. `ExcelFormReaderNodeDialog`
12. `ExcelFormReaderNodeFactory`
