package org.geki.knime.excelformreader;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.ErrorHandling;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.InputMode;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.OutputFormat;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetFilterMode;
import org.geki.knime.excelformreader.ExcelFormReaderSettings.SheetSelection;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.lang.reflect.Field;

public class ExcelFormReaderNodeDialog extends NodeDialogPane {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(ExcelFormReaderNodeDialog.class);

    private final ExcelFormReaderSettings m_settings = new ExcelFormReaderSettings();

    // ── General tab ───────────────────────────────────────────────────────────

    private final JRadioButton m_singleFileRadio = new JRadioButton("Single File");
    private final JRadioButton m_folderRadio     = new JRadioButton("Folder");

    private final JRadioButton m_wideRadio             = new JRadioButton("Wide");
    private final JRadioButton m_longRadio             = new JRadioButton("Long");
    private final JCheckBox    m_includeSourceFilename = new JCheckBox("Include source filename");
    private final JCheckBox    m_includeSheetName      = new JCheckBox("Include sheet name");
    private final JCheckBox    m_includeLabelFields       = new JCheckBox("Include label fields in port 0");
    private final JCheckBox    m_outputLabelPort          = new JCheckBox("Output label fields in port 1");
    private final JCheckBox    m_includeFormatCondition   = new JCheckBox("Include format condition operator columns");
    private final JCheckBox    m_includeValidationType    = new JCheckBox("Include validation type columns");

    private final JRadioButton m_missingCellWarn = new JRadioButton("Warn and insert missing value");
    private final JRadioButton m_missingCellFail = new JRadioButton("Fail");
    private final JRadioButton m_badValueWarn    = new JRadioButton("Warn and insert missing value");
    private final JRadioButton m_badValueFail    = new JRadioButton("Fail");

    // ── File tab ──────────────────────────────────────────────────────────────

    private final JComboBox<String> m_fileReadFrom = new JComboBox<>();
    private final JTextField        m_filePath     = new JTextField(30);
    private final JButton           m_fileBrowse   = new JButton("Browse...");

    private final JRadioButton m_fileSingleSheetRadio = new JRadioButton("Process single sheet");
    private final JRadioButton m_fileManySheetRadio   = new JRadioButton("Process many sheets");
    private JPanel             m_fileSingleSheetPanel;
    private JPanel             m_fileManySheetPanel;

    private final JRadioButton      m_fileFirstRadio      = new JRadioButton("First");
    private final JRadioButton      m_fileByNameRadio     = new JRadioButton("By name");
    private final JRadioButton      m_fileByPositionRadio = new JRadioButton("By position");
    private final JLabel            m_fileFirstSheetName  = new JLabel("(no file selected)");
    private final JComboBox<String> m_fileSheetNameCombo  = new JComboBox<>();
    private final JSpinner          m_fileSheetPosition   =
        new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JCheckBox         m_fileSingleHiddenSheets = new JCheckBox("Include hidden worksheets");
    private final JSeparator        m_fileSingleSeparator    = new JSeparator(SwingConstants.HORIZONTAL);

    private final JCheckBox    m_fileManyHiddenSheets     = new JCheckBox("Include hidden worksheets");
    private final JRadioButton m_fileSheetFilterAll       = new JRadioButton("All");
    private final JRadioButton m_fileSheetFilterBlacklist = new JRadioButton("Blacklist");
    private final JRadioButton m_fileSheetFilterWhitelist = new JRadioButton("Whitelist");
    private final JTextField   m_fileSheetFilterNames     = new JTextField(30);
    private JPanel             m_fileSheetFilterNamesPanel;

    // ── Folder tab ────────────────────────────────────────────────────────────

    private final JComboBox<String> m_folderReadFrom       = new JComboBox<>();
    private final JTextField        m_folderPath           = new JTextField(30);
    private final JButton           m_folderBrowse         = new JButton("Browse...");
    private final JCheckBox         m_recursive            = new JCheckBox("Include subfolders");
    private final JCheckBox         m_includeHiddenFolders = new JCheckBox("Include hidden folders");

    private final JRadioButton m_filterByExtension = new JRadioButton("Filter by file extension");
    private final JTextField   m_fileExtensions    = new JTextField(20);
    private JPanel             m_extensionsPanel;
    private final JCheckBox    m_includeHiddenFiles = new JCheckBox("Include hidden files");

    private final JRadioButton m_folderSingleSheetRadio    = new JRadioButton("Process single sheet");
    private final JRadioButton m_folderManySheetRadio      = new JRadioButton("Process many sheets");
    private JPanel             m_folderSingleSheetPanel;
    private JPanel             m_folderManySheetPanel;

    private final JRadioButton m_folderFirstRadio          = new JRadioButton("First");
    private final JRadioButton m_folderByNameRadio         = new JRadioButton("By name");
    private final JRadioButton m_folderByPositionRadio     = new JRadioButton("By position");
    private final JTextField   m_folderSheetNameField      = new JTextField();
    private final JSpinner     m_folderSheetPosition       =
        new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
    private final JCheckBox    m_folderSingleHiddenSheets  = new JCheckBox("Include hidden worksheets");
    private final JSeparator   m_folderSingleSeparator     = new JSeparator(SwingConstants.HORIZONTAL);

    private final JCheckBox    m_folderManyHiddenSheets     = new JCheckBox("Include hidden worksheets");
    private final JRadioButton m_folderSheetFilterAll       = new JRadioButton("All");
    private final JRadioButton m_folderSheetFilterBlacklist = new JRadioButton("Blacklist");
    private final JRadioButton m_folderSheetFilterWhitelist = new JRadioButton("Whitelist");
    private final JTextField   m_folderSheetFilterNames     = new JTextField(30);
    private JPanel             m_folderSheetFilterNamesPanel;

    // ─────────────────────────────────────────────────────────────────────────

    public ExcelFormReaderNodeDialog() {
        addTab("General", buildGeneralPanel());
        addTab("File",    buildFilePanel());
        addTab("Folder",  buildFolderPanel());
    }

    // ── General tab ───────────────────────────────────────────────────────────

    private JPanel buildGeneralPanel() {
        // ── Input group box ───────────────────────────────────────────────────
        ButtonGroup inputGroup = new ButtonGroup();
        inputGroup.add(m_singleFileRadio);
        inputGroup.add(m_folderRadio);
        m_singleFileRadio.setSelected(true);
        m_singleFileRadio.addItemListener(e -> updateTabStates());

        JPanel inputBox = createGroupBox("Input");
        inputBox.add(m_singleFileRadio, gbc(0, 0, 1, false));
        inputBox.add(new JPanel(),      gbc(1, 0, 1, true));  // stretch filler
        inputBox.add(m_folderRadio,     gbc(0, 1, 1, false));

        // ── Output group box ──────────────────────────────────────────────────
        ButtonGroup outputGroup = new ButtonGroup();
        outputGroup.add(m_wideRadio);
        outputGroup.add(m_longRadio);
        m_wideRadio.setSelected(true);

        m_outputLabelPort.setSelected(true);

        JPanel outputBox = createGroupBox("Output");
        outputBox.add(new JLabel("Output format:"), gbc(0, 0, 1, false));
        outputBox.add(m_wideRadio,                  gbc(1, 0, 1, false));
        outputBox.add(m_longRadio,                  gbc(2, 0, 1, false));
        outputBox.add(new JPanel(),                 gbc(3, 0, 1, true));  // stretch filler
        outputBox.add(m_includeSourceFilename,      gbc(0, 1, 4, false));
        outputBox.add(m_includeSheetName,           gbc(0, 2, 4, false));
        outputBox.add(m_includeLabelFields,         gbc(0, 3, 4, false));
        outputBox.add(m_outputLabelPort,            gbc(0, 4, 4, false));
        outputBox.add(m_includeFormatCondition,     gbc(0, 5, 4, false));
        outputBox.add(m_includeValidationType,      gbc(0, 6, 4, false));

        // ── Error Handling group box ──────────────────────────────────────────
        ButtonGroup missingGroup = new ButtonGroup();
        missingGroup.add(m_missingCellWarn);
        missingGroup.add(m_missingCellFail);
        m_missingCellWarn.setSelected(true);

        ButtonGroup badValueGroup = new ButtonGroup();
        badValueGroup.add(m_badValueWarn);
        badValueGroup.add(m_badValueFail);
        m_badValueWarn.setSelected(true);

        JPanel errorBox = createGroupBox("Error Handling");
        errorBox.add(new JLabel("On missing cell:"),      gbc(0, 0, 1, false));
        errorBox.add(m_missingCellWarn,                   gbc(1, 0, 1, false));
        errorBox.add(m_missingCellFail,                   gbc(2, 0, 1, false));
        errorBox.add(new JPanel(),                        gbc(3, 0, 1, true));  // stretch filler
        errorBox.add(new JLabel("On unparseable value:"), gbc(0, 1, 1, false));
        errorBox.add(m_badValueWarn,                      gbc(1, 1, 1, false));
        errorBox.add(m_badValueFail,                      gbc(2, 1, 1, false));

        // ── Assemble ──────────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints row = new GridBagConstraints();
        row.gridx    = 0;
        row.fill     = GridBagConstraints.HORIZONTAL;
        row.weightx  = 1.0;
        row.insets   = new Insets(4, 4, 4, 4);

        row.gridy = 0; content.add(inputBox,  row);
        row.gridy = 1; content.add(outputBox, row);
        row.gridy = 2; content.add(errorBox,  row);

        row.gridy   = 3;
        row.weighty = 1.0;
        row.fill    = GridBagConstraints.BOTH;
        content.add(new JPanel(), row);

        JPanel root = new JPanel(new BorderLayout());
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    // ── File tab ──────────────────────────────────────────────────────────────

    private JPanel buildFilePanel() {
        // ── Input Location group box ──────────────────────────────────────────
        m_fileReadFrom.addItem("Local File System");

        m_fileBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Excel files (*.xlsx)", "xlsx"));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                m_filePath.setText(chooser.getSelectedFile().getAbsolutePath());
                refreshSheetNames();
            }
        });

        JPanel locationBox = createGroupBox("Input Location");
        locationBox.add(new JLabel("Read from:"), gbc(0, 0, 1, false));
        locationBox.add(m_fileReadFrom,            gbc(1, 0, 1, false));
        locationBox.add(new JPanel(),              gbc(2, 0, 1, true));   // stretch filler
        locationBox.add(new JLabel("File:"),       gbc(0, 1, 1, false));
        locationBox.add(m_filePath,                gbc(1, 1, 1, true));   // stretches
        locationBox.add(m_fileBrowse,              gbc(2, 1, 1, false));

        // ── Select Sheet group — top-level mode toggle ────────────────────────
        ButtonGroup sheetsModeGroup = new ButtonGroup();
        sheetsModeGroup.add(m_fileSingleSheetRadio);
        sheetsModeGroup.add(m_fileManySheetRadio);
        m_fileSingleSheetRadio.setSelected(true);
        m_fileSingleSheetRadio.addItemListener(e -> updateSheetModeControls());

        // ── Section A: single sheet panel (GridBagLayout) ─────────────────────
        ButtonGroup sheetSelGroup = new ButtonGroup();
        sheetSelGroup.add(m_fileFirstRadio);
        sheetSelGroup.add(m_fileByNameRadio);
        sheetSelGroup.add(m_fileByPositionRadio);
        m_fileFirstRadio.setSelected(true);

        m_fileSheetNameCombo.addItem("(select a file first)");
        m_fileSheetNameCombo.setEnabled(false);

        m_fileFirstRadio.addItemListener(e -> updateSheetControls());
        m_fileByNameRadio.addItemListener(e -> updateSheetControls());
        m_fileByPositionRadio.addItemListener(e -> updateSheetControls());
        m_fileSingleHiddenSheets.addItemListener(e -> refreshSheetNames());

        m_fileSingleSheetPanel = new JPanel(new GridBagLayout());

        // Row 0: First radio + first-sheet-name label
        m_fileSingleSheetPanel.add(m_fileFirstRadio,     gbc(0, 0, 1, false));
        m_fileSingleSheetPanel.add(m_fileFirstSheetName, gbc(1, 0, 3, true));

        // Row 1: By name radio + combo
        m_fileSingleSheetPanel.add(m_fileByNameRadio,   gbc(0, 1, 1, false));
        m_fileSingleSheetPanel.add(m_fileSheetNameCombo, gbc(1, 1, 3, true));

        // Row 2: By position radio + spinner + hint label + filler
        m_fileSingleSheetPanel.add(m_fileByPositionRadio,              gbc(0, 2, 1, false));
        m_fileSingleSheetPanel.add(m_fileSheetPosition,                gbc(1, 2, 1, false));
        m_fileSingleSheetPanel.add(new JLabel("Position starts with 0."), gbc(2, 2, 1, false));
        m_fileSingleSheetPanel.add(new JPanel(),                       gbc(3, 2, 1, true));

        // ── Section B: many sheets panel (GridBagLayout) ──────────────────────
        ButtonGroup filterModeGroup = new ButtonGroup();
        filterModeGroup.add(m_fileSheetFilterAll);
        filterModeGroup.add(m_fileSheetFilterBlacklist);
        filterModeGroup.add(m_fileSheetFilterWhitelist);
        m_fileSheetFilterAll.setSelected(true);

        m_fileSheetFilterAll.addItemListener(e -> updateSheetFilterControls());
        m_fileSheetFilterBlacklist.addItemListener(e -> updateSheetFilterControls());
        m_fileSheetFilterWhitelist.addItemListener(e -> updateSheetFilterControls());

        // filter-names row wrapper (toggled with setVisible)
        m_fileSheetFilterNamesPanel = new JPanel(new GridBagLayout());
        m_fileSheetFilterNamesPanel.add(new JLabel("Sheet names:"),  gbc(0, 0, 1, false));
        m_fileSheetFilterNamesPanel.add(m_fileSheetFilterNames,       gbc(1, 0, 1, true));
        m_fileSheetFilterNamesPanel.setVisible(false);

        m_fileManySheetPanel = new JPanel(new GridBagLayout());
        m_fileManySheetPanel.setVisible(false);

        // Row 0: hidden sheets checkbox
        m_fileManySheetPanel.add(m_fileManyHiddenSheets, gbc(0, 0, 5, false));

        // Row 1: separator
        JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
        GridBagConstraints sep2Gbc = gbc(0, 1, 5, true);
        sep2Gbc.insets = new Insets(6, 4, 6, 4);
        m_fileManySheetPanel.add(sep2, sep2Gbc);

        // Row 2: sheet filter mode row
        m_fileManySheetPanel.add(new JLabel("Sheet filter:"),       gbc(0, 2, 1, false));
        m_fileManySheetPanel.add(m_fileSheetFilterAll,               gbc(1, 2, 1, false));
        m_fileManySheetPanel.add(m_fileSheetFilterBlacklist,         gbc(2, 2, 1, false));
        m_fileManySheetPanel.add(m_fileSheetFilterWhitelist,         gbc(3, 2, 1, false));
        m_fileManySheetPanel.add(new JPanel(),                       gbc(4, 2, 1, true));

        // Row 3: filter names panel (shown when Blacklist/Whitelist)
        m_fileManySheetPanel.add(m_fileSheetFilterNamesPanel, gbc(0, 3, 5, true));

        // ── Assemble Select Sheet group box ───────────────────────────────────
        JPanel sheetBox = createGroupBox("Select Sheet(s) to be processed");
        sheetBox.add(m_fileSingleSheetRadio,   gbc(0, 0, 1, false));
        sheetBox.add(new JPanel(),             gbc(1, 0, 1, true));   // stretch filler
        sheetBox.add(m_fileManySheetRadio,     gbc(0, 1, 1, false));
        sheetBox.add(m_fileSingleHiddenSheets, gbc(0, 2, 2, false));  // visible only in single mode
        GridBagConstraints sepSingleGbc = gbc(0, 3, 2, true);
        sepSingleGbc.insets = new Insets(6, 4, 6, 4);
        sheetBox.add(m_fileSingleSeparator,    sepSingleGbc);          // visible only in single mode
        sheetBox.add(m_fileSingleSheetPanel,   gbc(0, 4, 2, true));   // section A
        sheetBox.add(m_fileManySheetPanel,     gbc(0, 4, 2, true));   // section B (same row, only one visible)

        updateSheetControls();

        // ── Assemble tab ──────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints row = new GridBagConstraints();
        row.gridx   = 0;
        row.fill    = GridBagConstraints.HORIZONTAL;
        row.weightx = 1.0;
        row.insets  = new Insets(4, 4, 4, 4);

        row.gridy = 0; content.add(locationBox, row);
        row.gridy = 1; content.add(sheetBox,    row);

        row.gridy   = 2;
        row.weighty = 1.0;
        row.fill    = GridBagConstraints.BOTH;
        content.add(new JPanel(), row);

        JPanel root = new JPanel(new BorderLayout());
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private void updateSheetModeControls() {
        boolean single = m_fileSingleSheetRadio.isSelected();
        m_fileSingleHiddenSheets.setVisible(single);
        m_fileSingleSeparator.setVisible(single);
        m_fileSingleSheetPanel.setVisible(single);
        m_fileManySheetPanel.setVisible(!single);
    }

    private void updateSheetControls() {
        boolean first    = m_fileFirstRadio.isSelected();
        boolean byName   = m_fileByNameRadio.isSelected();
        boolean byPos    = m_fileByPositionRadio.isSelected();
        m_fileFirstSheetName.setEnabled(first);
        m_fileSheetNameCombo.setEnabled(byName);
        m_fileSheetPosition.setEnabled(byPos);
    }

    private void updateSheetFilterControls() {
        boolean showNames = m_fileSheetFilterBlacklist.isSelected()
            || m_fileSheetFilterWhitelist.isSelected();
        m_fileSheetFilterNamesPanel.setVisible(showNames);
    }

    private void refreshSheetNames() {
        String path = m_filePath.getText().trim();
        m_fileSheetNameCombo.removeAllItems();
        m_fileFirstSheetName.setText("(no file selected)");

        if (path.isEmpty()) {
            updateSheetControls();
            return;
        }

        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            updateSheetControls();
            return;
        }

        boolean includeHidden = m_fileSingleHiddenSheets.isSelected();

        try (Workbook wb = WorkbookFactory.create(file, null, true)) {
            String firstName = null;
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                if (!includeHidden && wb.isSheetHidden(i)) {
                    continue;
                }
                String name = wb.getSheetName(i);
                m_fileSheetNameCombo.addItem(name);
                if (firstName == null) firstName = name;
            }
            if (firstName != null) {
                m_fileFirstSheetName.setText(firstName);
            } else {
                m_fileFirstSheetName.setText("(no visible sheets)");
            }
        } catch (Exception e) {
            m_fileFirstSheetName.setText("(error reading file)");
            LOGGER.warn("Could not read sheet names from: " + path, e);
        }

        updateSheetControls();
    }

    // ── Folder tab ────────────────────────────────────────────────────────────

    private JPanel buildFolderPanel() {
        // ── Input Location group box ──────────────────────────────────────────
        m_folderReadFrom.addItem("Local File System");

        m_folderBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                m_folderPath.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        m_includeHiddenFolders.setEnabled(false);
        m_recursive.addItemListener(e -> {
            boolean recursive = m_recursive.isSelected();
            m_includeHiddenFolders.setEnabled(recursive);
            if (!recursive) {
                m_includeHiddenFolders.setSelected(false);
            }
        });

        JPanel locationBox = createGroupBox("Input Location");
        locationBox.add(new JLabel("Read from:"), gbc(0, 0, 1, false));
        locationBox.add(m_folderReadFrom,          gbc(1, 0, 1, false));
        locationBox.add(new JPanel(),              gbc(2, 0, 1, true));   // stretch filler
        locationBox.add(new JLabel("Folder:"),     gbc(0, 1, 1, false));
        locationBox.add(m_folderPath,              gbc(1, 1, 1, true));   // stretches
        locationBox.add(m_folderBrowse,            gbc(2, 1, 1, false));
        locationBox.add(m_recursive,               gbc(0, 2, 3, false));
        locationBox.add(m_includeHiddenFolders,    gbc(0, 3, 3, false));

        // ── File Filter group box ─────────────────────────────────────────────
        ButtonGroup fileFilterModeGroup = new ButtonGroup();
        fileFilterModeGroup.add(m_filterByExtension);
        m_filterByExtension.setSelected(true);
        m_fileExtensions.setText("xlsx");

        // extensions row wrapper (toggled with setVisible)
        m_extensionsPanel = new JPanel(new GridBagLayout());
        m_extensionsPanel.add(new JLabel("File extensions (comma-separated):"),
            gbc(0, 0, 1, false));
        m_extensionsPanel.add(m_fileExtensions, gbc(1, 0, 1, true));

        m_filterByExtension.addItemListener(
            e -> m_extensionsPanel.setVisible(m_filterByExtension.isSelected()));

        JSeparator fileFilterSep = new JSeparator(SwingConstants.HORIZONTAL);
        GridBagConstraints fileFilterSepGbc = gbc(0, 1, 3, true);
        fileFilterSepGbc.insets = new Insets(6, 4, 6, 4);

        JPanel fileFilterBox = createGroupBox("File Filter");
        fileFilterBox.add(m_filterByExtension, gbc(0, 0, 3, false));
        fileFilterBox.add(fileFilterSep,        fileFilterSepGbc);
        fileFilterBox.add(m_extensionsPanel,   gbc(0, 2, 3, true));
        fileFilterBox.add(m_includeHiddenFiles, gbc(0, 3, 3, false));

        // ── Select Sheet(s) group — top-level mode toggle ────────────────────
        ButtonGroup folderSheetsGroup = new ButtonGroup();
        folderSheetsGroup.add(m_folderSingleSheetRadio);
        folderSheetsGroup.add(m_folderManySheetRadio);
        m_folderSingleSheetRadio.setSelected(true);
        m_folderSingleSheetRadio.addItemListener(e -> updateFolderSheetModeControls());

        // ── Section A: single sheet panel (GridBagLayout) ─────────────────────
        ButtonGroup folderSheetSelGroup = new ButtonGroup();
        folderSheetSelGroup.add(m_folderFirstRadio);
        folderSheetSelGroup.add(m_folderByNameRadio);
        folderSheetSelGroup.add(m_folderByPositionRadio);
        m_folderFirstRadio.setSelected(true);

        m_folderFirstRadio.addItemListener(e -> updateFolderSheetControls());
        m_folderByNameRadio.addItemListener(e -> updateFolderSheetControls());
        m_folderByPositionRadio.addItemListener(e -> updateFolderSheetControls());

        m_folderSingleSheetPanel = new JPanel(new GridBagLayout());

        // Row 0: First radio (full width)
        m_folderSingleSheetPanel.add(m_folderFirstRadio, gbc(0, 0, 4, false));

        // Row 1: By name radio + text field
        m_folderSingleSheetPanel.add(m_folderByNameRadio,    gbc(0, 1, 1, false));
        m_folderSingleSheetPanel.add(m_folderSheetNameField, gbc(1, 1, 3, true));

        // Row 2: By position radio + spinner + hint label + filler
        m_folderSingleSheetPanel.add(m_folderByPositionRadio,               gbc(0, 2, 1, false));
        m_folderSingleSheetPanel.add(m_folderSheetPosition,                 gbc(1, 2, 1, false));
        m_folderSingleSheetPanel.add(new JLabel("Position starts with 0."), gbc(2, 2, 1, false));
        m_folderSingleSheetPanel.add(new JPanel(),                          gbc(3, 2, 1, true));

        // ── Section B: many sheets panel (GridBagLayout) ──────────────────────
        ButtonGroup folderFilterModeGroup = new ButtonGroup();
        folderFilterModeGroup.add(m_folderSheetFilterAll);
        folderFilterModeGroup.add(m_folderSheetFilterBlacklist);
        folderFilterModeGroup.add(m_folderSheetFilterWhitelist);
        m_folderSheetFilterAll.setSelected(true);

        m_folderSheetFilterAll.addItemListener(e -> updateFolderSheetFilterControls());
        m_folderSheetFilterBlacklist.addItemListener(e -> updateFolderSheetFilterControls());
        m_folderSheetFilterWhitelist.addItemListener(e -> updateFolderSheetFilterControls());

        // filter-names row wrapper (toggled with setVisible)
        m_folderSheetFilterNamesPanel = new JPanel(new GridBagLayout());
        m_folderSheetFilterNamesPanel.add(new JLabel("Sheet names:"),  gbc(0, 0, 1, false));
        m_folderSheetFilterNamesPanel.add(m_folderSheetFilterNames,     gbc(1, 0, 1, true));
        m_folderSheetFilterNamesPanel.setVisible(false);

        m_folderManySheetPanel = new JPanel(new GridBagLayout());
        m_folderManySheetPanel.setVisible(false);

        // Row 0: hidden sheets checkbox
        m_folderManySheetPanel.add(m_folderManyHiddenSheets, gbc(0, 0, 5, false));

        // Row 1: separator
        JSeparator folderManySep = new JSeparator(SwingConstants.HORIZONTAL);
        GridBagConstraints folderManySepGbc = gbc(0, 1, 5, true);
        folderManySepGbc.insets = new Insets(6, 4, 6, 4);
        m_folderManySheetPanel.add(folderManySep, folderManySepGbc);

        // Row 2: sheet filter mode row
        m_folderManySheetPanel.add(new JLabel("Sheet filter:"),        gbc(0, 2, 1, false));
        m_folderManySheetPanel.add(m_folderSheetFilterAll,              gbc(1, 2, 1, false));
        m_folderManySheetPanel.add(m_folderSheetFilterBlacklist,        gbc(2, 2, 1, false));
        m_folderManySheetPanel.add(m_folderSheetFilterWhitelist,        gbc(3, 2, 1, false));
        m_folderManySheetPanel.add(new JPanel(),                        gbc(4, 2, 1, true));

        // Row 3: filter names panel (shown when Blacklist/Whitelist)
        m_folderManySheetPanel.add(m_folderSheetFilterNamesPanel, gbc(0, 3, 5, true));

        // ── Assemble Select Sheet(s) group box ────────────────────────────────
        JPanel sheetBox = createGroupBox("Select Sheet(s) to be processed");
        sheetBox.add(m_folderSingleSheetRadio,   gbc(0, 0, 1, false));
        sheetBox.add(new JPanel(),               gbc(1, 0, 1, true));   // stretch filler
        sheetBox.add(m_folderManySheetRadio,     gbc(0, 1, 1, false));
        sheetBox.add(m_folderSingleHiddenSheets, gbc(0, 2, 2, false));  // visible only in single mode
        GridBagConstraints folderSepSingleGbc = gbc(0, 3, 2, true);
        folderSepSingleGbc.insets = new Insets(6, 4, 6, 4);
        sheetBox.add(m_folderSingleSeparator,    folderSepSingleGbc);    // visible only in single mode
        sheetBox.add(m_folderSingleSheetPanel,   gbc(0, 4, 2, true));   // section A
        sheetBox.add(m_folderManySheetPanel,     gbc(0, 4, 2, true));   // section B (same row, only one visible)

        updateFolderSheetControls();

        // ── Assemble tab ──────────────────────────────────────────────────────
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints row = new GridBagConstraints();
        row.gridx   = 0;
        row.fill    = GridBagConstraints.HORIZONTAL;
        row.weightx = 1.0;
        row.insets  = new Insets(4, 4, 4, 4);

        row.gridy = 0; content.add(locationBox,  row);
        row.gridy = 1; content.add(fileFilterBox, row);
        row.gridy = 2; content.add(sheetBox,      row);

        row.gridy   = 3;
        row.weighty = 1.0;
        row.fill    = GridBagConstraints.BOTH;
        content.add(new JPanel(), row);

        JPanel root = new JPanel(new BorderLayout());
        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private void updateFolderSheetModeControls() {
        boolean single = m_folderSingleSheetRadio.isSelected();
        m_folderSingleHiddenSheets.setVisible(single);
        m_folderSingleSeparator.setVisible(single);
        m_folderSingleSheetPanel.setVisible(single);
        m_folderManySheetPanel.setVisible(!single);
    }

    private void updateFolderSheetControls() {
        m_folderSheetNameField.setEnabled(m_folderByNameRadio.isSelected());
        m_folderSheetPosition.setEnabled(m_folderByPositionRadio.isSelected());
    }

    private void updateFolderSheetFilterControls() {
        boolean showNames = m_folderSheetFilterBlacklist.isSelected()
            || m_folderSheetFilterWhitelist.isSelected();
        m_folderSheetFilterNamesPanel.setVisible(showNames);
    }

    // ── Tabbed pane helpers ───────────────────────────────────────────────────

    private void updateTabStates() {
        boolean singleFile = m_singleFileRadio.isSelected();
        JTabbedPane pane = getTabbedPane();
        if (pane != null) {
            pane.setEnabledAt(1, singleFile);
            pane.setEnabledAt(2, !singleFile);
        }
    }

    // NodeDialogPane.m_pane is private with no public accessor
    private JTabbedPane getTabbedPane() {
        try {
            Field f = NodeDialogPane.class.getDeclaredField("m_pane");
            f.setAccessible(true);
            return (JTabbedPane) f.get(this);
        } catch (Exception e) {
            return null;
        }
    }

    private JPanel createGroupBox(final String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return panel;
    }

    private GridBagConstraints gbc(final int x, final int y,
                                    final int width, final boolean stretch) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx     = x;
        c.gridy     = y;
        c.gridwidth = width;
        c.fill      = stretch ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.weightx   = stretch ? 1.0 : 0.0;
        c.anchor    = GridBagConstraints.WEST;
        c.insets    = new Insets(2, 4, 2, 4);
        return c;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        // General tab
        m_settings.getInputModeModel().setStringValue(
            m_singleFileRadio.isSelected() ? "SINGLE_FILE" : "FOLDER");
        m_settings.getOutputFormatModel().setStringValue(
            m_wideRadio.isSelected() ? "WIDE" : "LONG");
        m_settings.getIncludeSourceFilenameModel().setBooleanValue(
            m_includeSourceFilename.isSelected());
        m_settings.getIncludeSheetNameModel().setBooleanValue(
            m_includeSheetName.isSelected());
        m_settings.getIncludeLabelFieldsModel().setBooleanValue(
            m_includeLabelFields.isSelected());
        m_settings.getOutputLabelPortModel().setBooleanValue(
            m_outputLabelPort.isSelected());
        m_settings.getIncludeFormatConditionModel().setBooleanValue(
            m_includeFormatCondition.isSelected());
        m_settings.getIncludeValidationTypeModel().setBooleanValue(
            m_includeValidationType.isSelected());
        m_settings.getOnMissingCellModel().setStringValue(
            m_missingCellWarn.isSelected() ? "WARN" : "FAIL");
        m_settings.getOnBadValueModel().setStringValue(
            m_badValueWarn.isSelected() ? "WARN" : "FAIL");

        // File tab
        m_settings.getFilePathModel().setStringValue(m_filePath.getText().trim());

        String sheetSel = m_fileFirstRadio.isSelected() ? "FIRST"
            : m_fileByNameRadio.isSelected() ? "BY_NAME"
            : "BY_POSITION";
        m_settings.getFileSheetSelectionModel().setStringValue(sheetSel);

        m_settings.getFileSheetNameModel().setStringValue(
            m_fileSheetNameCombo.getSelectedItem() != null
            ? (String) m_fileSheetNameCombo.getSelectedItem() : "");
        m_settings.getFileSheetPositionModel().setIntValue(
            (Integer) m_fileSheetPosition.getValue());
        m_settings.getFileManySheeetsModel().setBooleanValue(
            m_fileManySheetRadio.isSelected());
        m_settings.getFileSingleHiddenSheetsModel().setBooleanValue(
            m_fileSingleHiddenSheets.isSelected());
        m_settings.getFileHiddenSheetsModel().setBooleanValue(
            m_fileManyHiddenSheets.isSelected());

        String filterMode = m_fileSheetFilterAll.isSelected() ? "ALL"
            : m_fileSheetFilterBlacklist.isSelected() ? "BLACKLIST"
            : "WHITELIST";
        m_settings.getFileSheetFilterModeModel().setStringValue(filterMode);
        m_settings.getFileSheetFilterNamesModel().setStringValue(
            m_fileSheetFilterNames.getText().trim());

        // Folder tab
        m_settings.getFolderPathModel().setStringValue(m_folderPath.getText().trim());
        m_settings.getRecursiveModel().setBooleanValue(m_recursive.isSelected());
        m_settings.getIncludeHiddenFoldersModel().setBooleanValue(m_includeHiddenFolders.isSelected());
        m_settings.getFilterByExtensionModel().setBooleanValue(m_filterByExtension.isSelected());
        m_settings.getFileExtensionsModel().setStringValue(m_fileExtensions.getText().trim());
        m_settings.getIncludeHiddenFilesModel().setBooleanValue(m_includeHiddenFiles.isSelected());

        m_settings.getFolderManySheetsModel().setBooleanValue(
            m_folderManySheetRadio.isSelected());

        String folderSheetSel = m_folderFirstRadio.isSelected() ? "FIRST"
            : m_folderByNameRadio.isSelected() ? "BY_NAME"
            : "BY_POSITION";
        m_settings.getFolderSheetSelectionModel().setStringValue(folderSheetSel);

        m_settings.getFolderSheetNameModel().setStringValue(
            m_folderSheetNameField.getText().trim());

        m_settings.getFolderSheetPositionModel().setIntValue(
            (Integer) m_folderSheetPosition.getValue());

        m_settings.getFolderSingleHiddenSheetsModel().setBooleanValue(
            m_folderSingleHiddenSheets.isSelected());

        m_settings.getFolderHiddenSheetsModel().setBooleanValue(
            m_folderManyHiddenSheets.isSelected());

        String folderFilterMode = m_folderSheetFilterAll.isSelected() ? "ALL"
            : m_folderSheetFilterBlacklist.isSelected() ? "BLACKLIST"
            : "WHITELIST";
        m_settings.getFolderSheetFilterModeModel().setStringValue(folderFilterMode);
        m_settings.getFolderSheetFilterNamesModel().setStringValue(
            m_folderSheetFilterNames.getText().trim());

        m_settings.saveSettings(settings);
    }

    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
        try {
            m_settings.loadSettings(settings);
        } catch (InvalidSettingsException e) {
            // use defaults on first open
        }

        // General tab
        boolean singleFile = m_settings.getInputMode() == InputMode.SINGLE_FILE;
        m_singleFileRadio.setSelected(singleFile);
        m_folderRadio.setSelected(!singleFile);

        m_wideRadio.setSelected(m_settings.getOutputFormat() == OutputFormat.WIDE);
        m_longRadio.setSelected(m_settings.getOutputFormat() == OutputFormat.LONG);

        m_includeSourceFilename.setSelected(m_settings.isIncludeSourceFilename());
        m_includeSheetName.setSelected(m_settings.isIncludeSheetName());
        m_includeLabelFields.setSelected(m_settings.isIncludeLabelFields());
        m_outputLabelPort.setSelected(m_settings.isOutputLabelPort());
        m_includeFormatCondition.setSelected(m_settings.isIncludeFormatCondition());
        m_includeValidationType.setSelected(m_settings.isIncludeValidationType());

        m_missingCellWarn.setSelected(m_settings.getOnMissingCell() == ErrorHandling.WARN);
        m_missingCellFail.setSelected(m_settings.getOnMissingCell() == ErrorHandling.FAIL);

        m_badValueWarn.setSelected(m_settings.getOnBadValue() == ErrorHandling.WARN);
        m_badValueFail.setSelected(m_settings.getOnBadValue() == ErrorHandling.FAIL);

        updateTabStates();

        // File tab
        m_filePath.setText(m_settings.getFilePath());
        refreshSheetNames(); // calls updateSheetControls() internally

        SheetSelection sel = m_settings.getFileSheetSelection();
        m_fileFirstRadio.setSelected(sel == SheetSelection.FIRST);
        m_fileByNameRadio.setSelected(sel == SheetSelection.BY_NAME);
        m_fileByPositionRadio.setSelected(sel == SheetSelection.BY_POSITION);

        String savedName = m_settings.getFileSheetName();
        if (!savedName.isEmpty()) {
            m_fileSheetNameCombo.setSelectedItem(savedName);
        }

        m_fileSheetPosition.setValue(m_settings.getFileSheetPosition());

        boolean manySheets = m_settings.getFileManySheets();
        m_fileSingleSheetRadio.setSelected(!manySheets);
        m_fileManySheetRadio.setSelected(manySheets);
        m_fileSingleHiddenSheets.setSelected(m_settings.isFileSingleIncludeHiddenSheets());
        m_fileManyHiddenSheets.setSelected(m_settings.isFileIncludeHiddenSheets());

        SheetFilterMode fm = m_settings.getFileSheetFilterMode();
        m_fileSheetFilterAll.setSelected(fm == SheetFilterMode.ALL);
        m_fileSheetFilterBlacklist.setSelected(fm == SheetFilterMode.BLACKLIST);
        m_fileSheetFilterWhitelist.setSelected(fm == SheetFilterMode.WHITELIST);

        m_fileSheetFilterNames.setText(String.join(", ", m_settings.getFileSheetFilterNames()));

        updateSheetControls();
        updateSheetFilterControls();
        updateSheetModeControls();

        // Folder tab
        m_folderPath.setText(m_settings.getFolderPath());
        m_recursive.setSelected(m_settings.isRecursive());
        m_includeHiddenFolders.setEnabled(m_settings.isRecursive());
        m_includeHiddenFolders.setSelected(m_settings.isIncludeHiddenFolders());
        m_filterByExtension.setSelected(m_settings.isFilterByExtension());
        m_fileExtensions.setText(String.join(", ", m_settings.getFileExtensions()));
        m_extensionsPanel.setVisible(m_settings.isFilterByExtension());
        m_includeHiddenFiles.setSelected(m_settings.isIncludeHiddenFiles());
        boolean folderMany = m_settings.getFolderManySheets();
        m_folderSingleSheetRadio.setSelected(!folderMany);
        m_folderManySheetRadio.setSelected(folderMany);

        SheetSelection fsel = m_settings.getFolderSheetSelection();
        m_folderFirstRadio.setSelected(fsel == SheetSelection.FIRST);
        m_folderByNameRadio.setSelected(fsel == SheetSelection.BY_NAME);
        m_folderByPositionRadio.setSelected(fsel == SheetSelection.BY_POSITION);

        m_folderSheetNameField.setText(m_settings.getFolderSheetName());

        m_folderSheetPosition.setValue(m_settings.getFolderSheetPosition());

        m_folderSingleHiddenSheets.setSelected(
            m_settings.isFolderSingleIncludeHiddenSheets());

        m_folderManyHiddenSheets.setSelected(m_settings.isFolderIncludeHiddenSheets());

        SheetFilterMode ffm = m_settings.getFolderSheetFilterMode();
        m_folderSheetFilterAll.setSelected(ffm == SheetFilterMode.ALL);
        m_folderSheetFilterBlacklist.setSelected(ffm == SheetFilterMode.BLACKLIST);
        m_folderSheetFilterWhitelist.setSelected(ffm == SheetFilterMode.WHITELIST);

        m_folderSheetFilterNames.setText(String.join(", ", m_settings.getFolderSheetFilterNames()));

        updateFolderSheetModeControls();
        updateFolderSheetControls();
        updateFolderSheetFilterControls();
    }
}
