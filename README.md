# knime-extensions

Community KNIME extensions developed by
[geki-research](https://github.com/geki-research).

## Extensions

### Excel Reader (non-tabular)

A KNIME node that extracts data from non-tabular,
form-structured Excel worksheets (.xlsx) into a standard
KNIME data table.

Unlike the native KNIME Excel Reader — which expects
tabular data — this node is designed for Excel files that
are formatted as human-readable forms, where labels and
input fields are placed at specific cell addresses rather
than in rows and columns.

#### Key features

- Form structure defined via a configurable input table
  (field name → cell address mapping) — supports 60+
  fields without cumbersome dialog configuration
- Reads single files, all sheets in a workbook, folders,
  and recursive folder structures
- Each worksheet is treated as one form instance
- Output format configurable: wide (one row per form) or
  long (one row per field)
- Content Type support: distinguish data fields from label
  fields — label fields can be included in port 0 and/or
  output separately to port 1 for downstream validation
- Cell ranges supported (e.g. B10:D15) — read as
  delimited strings
- Formula evaluation via Apache POI — computed values,
  not formula strings
- Configurable sheet selection: first sheet, by name,
  or by position
- Blacklist/Whitelist sheet filtering
- Configurable file extension filtering in folder mode
- Optional provenance columns: Source File and Sheet Name
- Configurable error handling: warn or fail on missing
  cells and unparseable values
- Compatible with KNIME Analytics Platform 5.5+

#### Node ports

| Port | Direction | Type | Description |
|---|---|---|---|
| 0 | Input | Table | Form definition table |
| 0 | Output | Table | Extracted data (wide or long) |
| 1 | Output | Table | Label fields (for validation) |

#### Form definition table schema

| Column | Required | Type | Description |
|---|---|---|---|
| `Name` | ✅ | String | Output column name |
| `Cell Range` | ✅ | String | Cell address (`C4`) or range (`B10:D15`) |
| `Content Type` | ❌ | String | `data` (default) or `label` |
| `Data Type` | ❌ | String | `string` / `int` / `double` / `date` / `boolean` |

#### Dialog overview

| Tab | Group box | Key settings |
|---|---|---|
| General | Input | Single File / Folder mode |
| General | Output | Wide/Long, provenance columns, label field toggles |
| General | Error Handling | Warn or Fail per error type |
| File | Input Location | File path, Read from |
| File | Select Sheet(s) | Single sheet (First/By name/By position) or many sheets (All/Blacklist/Whitelist) |
| Folder | Input Location | Folder path, subfolders, hidden folders |
| Folder | File Filter | Extension filter, hidden files |
| Folder | Select Sheet(s) | Single sheet or many sheets per file |

---

## Development Setup

### Prerequisites

- Debian 12 (or compatible Linux)
- JDK 17 (`openjdk-17-jdk`)
- JDK 21 (`sdkman`) — required for Eclipse launch
  configuration only
- Maven 3.9+
- Eclipse for RCP and RAP Developers 2024-03+
- KNIME SDK Setup:
  https://github.com/knime/knime-sdk-setup
  (use `KNIME-AP.target`)

### Build

```bash
cd ~/knime-dev/knime-extensions
mvn clean verify
```

Build output (update site):

```
org.geki.knime.excelformreader.update/target/repository/
```

### Eclipse Setup

1. Import all projects from this repository into Eclipse
2. Activate the KNIME target platform via `knime-sdk-setup`
   (`KNIME-AP.target`, 1897 plugins)
3. Run a KNIME launch configuration to test nodes interactively

## License

Apache License 2.0 — see [LICENSE](LICENSE)
