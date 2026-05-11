# CLAUDE.md — Project Context for Claude Code

This file is read automatically by Claude Code at the start of every session.
Do not delete or rename it.

---

## Project Overview

**Repository:** https://github.com/geki-research/knime-extensions
**Owner:** geki-research
**Vendor:** DataCraft Labs
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
  - 4 label fields: reference_date, assessor, system_type, system_name
  - 36 data fields
  - Covers all cell address patterns used in the real form
  - Data types covered: string, int, date
  - Data types not covered (unit tests only): double, boolean

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

## Branch Strategy for KNIME Versions

| Branch | p2 Repository | Active profile |
|---|---|---|
| `main` | nightly (default) | `knime-nightly` |
| `releases/5.5` | `https://update.knime.com/analytics-platform/5.5` | `knime-5.5` |
| `releases/5.8` | `https://update.knime.com/analytics-platform/5.8` | `knime-5.8` |
| `releases/5.12` | `https://update.knime.com/analytics-platform/5.12` | `knime-5.12` |

Maven profiles in `pom.xml` control the active p2 repository. Each release
branch sets its profile as `activeByDefault`; `main` defaults to nightly.

```bash
mvn clean verify              # uses nightly profile (main branch default)
mvn clean verify -P knime-5.5 # explicitly use 5.5 update site
```

KNIME Jenkins activates the correct profile per branch automatically via
`-P knime-X.Y` in its build command.

**New release branch checklist:**
1. `git checkout -b releases/X.Y` from `main`
2. Set `<activeByDefault>true</activeByDefault>` on the `knime-X.Y` profile
   in `pom.xml` (remove it from `knime-nightly`)
3. Commit, push branch
4. Notify KNIME team to add a build job for the new branch

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
**Category:** `/datacraft-labs/io`
**KNIME API version:** 5.5.x (minimum)

### What this node does
Reads non-tabular, form-structured Excel worksheets (.xlsx) and extracts
field values into a standard KNIME data table. The form structure
(label → cell address mapping) is provided via an input table, making
the node generic and reusable across any form layout.

### Node ports
| Port | Direction | Type | Description |
|---|---|---|---|
| 0 | Input | BufferedDataTable | Form definition table |
| 0 | Output | BufferedDataTable | Extracted data (wide or long) |
| 1 | Output | BufferedDataTable | Label fields (always produced, may be empty) |

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
| General / Input | Input mode | Radio | Single File |
| General / Output | Output format | Radio | Wide |
| General / Output | Include source filename | Boolean | true |
| General / Output | Include sheet name | Boolean | true |
| General / Output | Include label fields in port 0 | Boolean | false |
| General / Output | Output label fields in port 1 | Boolean | true |
| General / Output | Include format condition operator columns | Boolean | false |
| General / Output | Include validation type columns | Boolean | false |
| General / Error Handling | On missing cell | Radio | Warn |
| General / Error Handling | On unparseable value | Radio | Warn |
| File / Input Location | Read from | Dropdown | Local File System |
| File / Input Location | File path | String | — |
| File / Select Sheet(s) | Process single/many sheets | Radio | Single |
| File / Select Sheet(s) | Sheet selection (single) | Radio | First |
| File / Select Sheet(s) | Include hidden worksheets (single) | Boolean | false |
| File / Select Sheet(s) | Sheet filter mode (many) | Radio | All |
| File / Select Sheet(s) | Include hidden worksheets (many) | Boolean | false |
| Folder / Input Location | Folder path | String | — |
| Folder / Input Location | Include subfolders | Boolean | false |
| Folder / Input Location | Include hidden folders | Boolean | false |
| Folder / File Filter | Filter by file extension | Radio | Selected |
| Folder / File Filter | File extensions | String | xlsx |
| Folder / File Filter | Include hidden files | Boolean | false |
| Folder / Select Sheet(s) | Process single/many sheets | Radio | Single |
| Folder / Select Sheet(s) | Sheet selection (single) | Radio | First |
| Folder / Select Sheet(s) | Include hidden worksheets (single) | Boolean | false |
| Folder / Select Sheet(s) | Sheet filter mode (many) | Radio | All |
| Folder / Select Sheet(s) | Include hidden worksheets (many) | Boolean | false |

### Output Ports

**Port 0 — Main output (wide mode columns in order):**
1. `source_file` (String, optional)
2. `sheet_name` (String, optional)
3. Per field in definition order:
   - `<Name>` — value typed per Data Type
   - `<Name> (Format Condition Operator)` — String, optional (toggle)
   - `<Name> (Validation Type)` — String, optional (toggle)

Label fields included when "Include label fields in port 0" is enabled.

**Port 0 — Main output (long mode columns in order):**
1. `source_file` (String, optional)
2. `sheet_name` (String, optional)
3. `field_name` (String)
4. `value` (String)
5. `Format Condition Operator` (String, optional)
6. `Validation Type` (String, optional)

**Port 1 — Label fields output:**
- Fixed wide format regardless of Port 0 format setting
- One row per label field per (file, sheet) pair
- Columns (in order): `Source File` (optional), `Sheet Name` (optional), `Name`, `Cell Range`, `Cell Content`, `Format Condition Operator` (optional), `Validation Type` (optional)
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
                                  (name, cellRange, contentType, dataType)
                                  isData() / isLabel() convenience methods
    FormDefinition              — List<FieldMapping>, static factory
                                  fromDataTable(BufferedDataTable)
                                  getDataFields() / getLabelFields()
    CellAddress                 — Parses "C4" / "B10:D15" into typed fields,
                                  validates format; toString() produces
                                  canonical address string
    ReadingMode                 — Enum: SINGLE_FILE, FOLDER

  excel/
    WorkbookIterator            — Lazy Iterator<Entry> over (Path, Sheet) pairs
                                  Respects sheet filter, hidden sheet flag,
                                  recursive folder walk
                                  Opens/closes workbooks one at a time
    ExcelFormExtractor          — POI-based: resolves FormDefinition against
                                  a Sheet → Map<String, CellExtractionResult>
                                  CellExtractionResult holds value +
                                  formatConditionOperator + validationType
                                  Evaluates formulas transparently
    CellValueConverter          — POI Cell → KNIME DataCell per data_type
                                  Handles string/int/double/date/boolean
    CellMetadataReader          — Stateless; reads conditional formatting
                                  operators and data validation types per cell
                                  Resolves LIST validation options including
                                  inline lists, same-sheet ranges, cross-sheet
                                  ranges, and named ranges
                                  Always requires Workbook parameter

  output/
    OutputSpecFactory           — Creates DataTableSpec at configure() time
                                  createWideSpec() / createLongSpec() /
                                  createLabelSpec()
    WideOutputBuilder           — One DataRow per (file, sheet)
    LongOutputBuilder           — N DataRows per (file, sheet, field)
    LabelOutputBuilder          — One DataRow per label field per (file, sheet)
                                  Produces Port 1 output
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

11. **Content Type filtering** — use `FormDefinition.getDataFields()` and
    `FormDefinition.getLabelFields()` for filtered iteration. Never filter
    inline in builders.

12. **Cell metadata** — `CellMetadataReader` is stateless. Always pass
    `Workbook` to enable cross-sheet list resolution. Range delimiter is
    hardcoded as `", "` in `ExcelFormExtractor`.

13. **Port 1** — always produced (may be empty table). Empty is simpler and
    faster than an optional port for large volumes.

14. **LIST validation resolution order** — (1) inline list, (2) named range,
    (3) same-sheet range, (4) cross-sheet range,
    (5) fall back to raw formula string.

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
// Correct NodeModel constructor for 1 input, 2 outputs:
super(new PortType[]{BufferedDataTable.TYPE},
      new PortType[]{BufferedDataTable.TYPE,
                     BufferedDataTable.TYPE});

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

Unit tests — planned for all layers:
- `CellAddress.parse()` edge cases
- `CellValueConverter` per data type
- `FormDefinition.fromDataTable()` column validation
- `WorkbookIterator` sheet filtering and file discovery
- `CellMetadataReader` format condition and validation type reading

Known limitations:
- `configure()` returns partial spec in WIDE mode (see TODO comment above
  `configure()` in NodeModel — accepted, cosmetic only)
- Format condition operator reads `CELL_VALUE_IS` rules for operator name;
  other rule types return the condition type name instead

## Node Icon

Node icon extracted from the KNIME native Excel Reader node for visual
consistency in the node repository.
Located at: `icons/excelformreader.png`
