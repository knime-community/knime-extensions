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
  (field name to cell address mapping) — supports 60+
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

Port 0 Input: Form definition table
Port 0 Output: Extracted data (wide or long format)
Port 1 Output: Label fields (for downstream validation)

#### Form definition table schema

Columns:
- Name (required, String): output column name
- Cell Range (required, String): cell address such as C4
  or range such as B10:D15
- Content Type (optional, String): "data" (default) or
  "label"
- Data Type (optional, String): string, int, double,
  date, or boolean

#### Dialog overview

General tab:
- Input group box: Single File or Folder mode
- Output group box: Wide/Long format, provenance columns,
  label field toggles
- Error Handling group box: Warn or Fail per error type

File tab:
- Input Location group box: file path and Read from
- Select Sheet(s) group box: single sheet (First, By
  name, By position) or many sheets (All, Blacklist,
  Whitelist)

Folder tab:
- Input Location group box: folder path, subfolders,
  hidden folders
- File Filter group box: extension filter, hidden files
- Select Sheet(s) group box: single sheet or many sheets
  per file

---

## Development Setup

### Prerequisites

- Debian 12 or compatible Linux
- JDK 17 (openjdk-17-jdk) for building
- JDK 21 via SDKMAN for Eclipse launch configuration only
- Maven 3.9+
- Eclipse for RCP and RAP Developers 2024-03 or later
- KNIME SDK Setup from https://github.com/knime/knime-sdk-setup
  using KNIME-AP.target

### Build

Run from the repository root:

  mvn clean verify

Build output (update site) is produced at:

  org.geki.knime.excelformreader.update/target/repository/

### Eclipse setup

1. Import all projects via File, Import, Maven,
   Existing Maven Projects
2. Activate the KNIME target platform by opening
   KNIME-AP.target in org.knime.sdk.setup and clicking
   Set as Active Target Platform
3. Create an Eclipse Application launch configuration
   pointing to org.knime.product.KNIME_PRODUCT with
   JRE set to Java 21 and program argument -clean

### Repository structure

  knime-extensions/
    pom.xml                                  parent POM
    org.geki.knime.excelformreader/          plugin
    org.geki.knime.excelformreader.feature/  feature
    org.geki.knime.excelformreader.update/   update site
    org.geki.knime.excelformreader.tests/    test project

### Branching strategy

- main: always releasable
- develop: integration branch
- feature/name: one branch per feature

---

## License

Apache License 2.0 — see LICENSE file.
