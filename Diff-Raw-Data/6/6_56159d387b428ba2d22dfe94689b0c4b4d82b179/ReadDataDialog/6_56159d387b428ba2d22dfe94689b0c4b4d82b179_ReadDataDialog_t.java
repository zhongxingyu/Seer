 package edu.mit.wi.haploview;
 
 import edu.mit.wi.haploview.genecruiser.GeneCruiser;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import java.awt.event.*;
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 
 public class ReadDataDialog extends JDialog
         implements ActionListener, DocumentListener, Constants {
     static final long serialVersionUID = -1864177851666320697L;
 
     static final String MARKER_DATA_EXT = ".info";
     static final String MAP_FILE_EXT = ".map";
     static final String BIM_FILE_EXT = ".bim";
     static final String BROWSE_GENO = "browse for geno files";
     static final String BROWSE_HAPS = "browse for haps files";
     static final String BROWSE_HMP = "browse for hapmap files";
     static final String BROWSE_PHASE = "browse for PHASE files";
     static final String BROWSE_SAMPLE = "browse for sample files";
     static final String BROWSE_LEGEND = "browse for legend files";
     static final String BROWSE_INFO = "browse for info files";
     static final String BROWSE_ASSOC = "browse for association test files";
     static final String BROWSE_WGA = "browse for PLINK files";
     static final String BROWSE_MAP = "browse for PLINK map files";
 
     private int fileType;
     private JTextField pedFileField, pedInfoField, hapsFileField, hapsInfoField, hmpFileField,
             phaseFileField, phaseSampleField, phaseLegendField, geneCruiseField, plinkFileField, plinkMapField, testFileField, geneCruiseField3;
     private JCheckBox doAssociation, doGB, phaseDoGB, downloadDoGB, xChrom, gZip, embeddedMap, plinkChrom, selectColumns, nonSNP, downloadDoGB3;
     private JRadioButton trioButton, ccButton, standardTDT, parenTDT;
     private JButton browseAssocButton, browsePlinkMapButton, browsePhaseButton, browseSampleButton, browseLegendButton, geneCruiseButton;
     private NumberTextField maxComparisonDistField, missingCutoffField, chromStartField, chromStartField3, chromEndField, chromEndField3, rangeField, rangeField3;
     private JLabel testFileLabel, mapLabel, downloadLabel;
     private JComboBox chromChooser = new JComboBox(CHROM_NAMES);
     private JComboBox loadChromChooser = new JComboBox(CHROM_NAMES);
     private JComboBox plinkChromChooser = new JComboBox(CHROM_NAMES);
     private JComboBox panelChooser = new JComboBox(PANEL_NAMES);
     private JComboBox phaseChooser = new JComboBox(RELEASE_NAMES);
     private JComboBox hapVersionChooser = new JComboBox(HAPMAP_VERSIONS);
     //TODO: Uncomment all the fastPHASE stuff once it's ready
     //private JComboBox phaseFormatChooser = new JComboBox(PHASE_FORMATS);
     private JComboBox geneCruiseChooser = new JComboBox(GENE_DATABASES);
     private JComboBox geneCruiseChooser3 = new JComboBox(GENE_DATABASES);
     private String chromChoice, panelChoice, phaseChoice, embed, selectCols;
     private JPanel phaseTab, downloadTab, downloadTab3, /*phaseFormatPanel,*/
             downloadChooserPanel, downloadChooserPanel3,
             downloadPositionPanel, downloadPositionPanel3, downloadBrowsePanel, metaAnalyzePanel, downloadBrowsePanel3, geneCruiserPanel, geneCruiserPanel3, phaseGzipPanel, phaseChromPanel;
 
     JTabbedPane dataFormatPane = new JTabbedPane(JTabbedPane.LEFT);
     static int currTab = 0;
     private GridBagConstraints c;
 
     public ReadDataDialog(String title, HaploView h) {
         super(h, title);
 
         try {
             //OSX has problem displaying left sided tabs
             if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
 
                 dataFormatPane.setTabPlacement(JTabbedPane.TOP);
 
             }
         } catch (RuntimeException rte) {
             //catches all of the following:
             //-SecurityException
             //-NullPointerException
             //-IllegalArgumentException
         }
 
         //Ped Tab Objects
         pedFileField = new JTextField(20);
         JButton browsePedFileButton = new JButton("Browse");
         browsePedFileButton.setActionCommand(BROWSE_GENO);
         browsePedFileButton.addActionListener(this);
         pedInfoField = new JTextField(20);
         pedInfoField.getDocument().addDocumentListener(this);
         JButton browsePedInfoButton = new JButton("Browse");
         browsePedInfoButton.setActionCommand(BROWSE_INFO);
         browsePedInfoButton.addActionListener(this);
         JPanel assocPanel = new JPanel();
         doAssociation = new JCheckBox("Do association test");
         doAssociation.setSelected(false);
         doAssociation.setEnabled(false);
         doAssociation.setActionCommand("association");
         doAssociation.addActionListener(this);
         xChrom = new JCheckBox("X Chromosome");
         xChrom.setSelected(false);
         xChrom.setActionCommand("xChrom");
         xChrom.addActionListener(this);
         assocPanel.add(xChrom);
         assocPanel.add(doAssociation);
         JPanel tdtOptsPanel = new JPanel();
         trioButton = new JRadioButton("Family trio data", true);
         trioButton.setEnabled(false);
         trioButton.setActionCommand("tdt");
         trioButton.addActionListener(this);
         ccButton = new JRadioButton("Case/Control data");
         ccButton.setEnabled(false);
         ccButton.setActionCommand("ccButton");
         ccButton.addActionListener(this);
         ButtonGroup group = new ButtonGroup();
         group.add(trioButton);
         group.add(ccButton);
         tdtOptsPanel.add(trioButton);
         tdtOptsPanel.add(ccButton);
         JPanel tdtTypePanel = new JPanel();
         standardTDT = new JRadioButton("Standard TDT", true);
         standardTDT.setEnabled(false);
         parenTDT = new JRadioButton("ParenTDT", true);
         parenTDT.setEnabled(false);
         ButtonGroup tdtGroup = new ButtonGroup();
         tdtGroup.add(standardTDT);
         tdtGroup.add(parenTDT);
         tdtTypePanel.add(standardTDT);
         tdtTypePanel.add(parenTDT);
         testFileField = new JTextField(20);
         testFileField.setEnabled(false);
         testFileField.setBackground(this.getBackground());
         browseAssocButton = new JButton("Browse");
         browseAssocButton.setActionCommand(BROWSE_ASSOC);
         browseAssocButton.addActionListener(this);
         browseAssocButton.setEnabled(false);
         testFileLabel = new JLabel("Test list file (optional):");
         testFileLabel.setEnabled(false);
 
         //Layout the Ped Tab
         JPanel pedTab = new JPanel(new GridBagLayout());
         pedTab.setPreferredSize(new Dimension(375, 200));
         c = new GridBagConstraints();
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         pedTab.add(new JLabel("Data File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         pedTab.add(pedFileField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         pedTab.add(browsePedFileButton, c);
         c.gridy = 1;
         c.gridx = 0;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         pedTab.add(new JLabel("Locus Information File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(0, 0, 0, 0);
         pedTab.add(pedInfoField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         pedTab.add(browsePedInfoButton, c);
         c.gridy = 2;
         c.gridx = 0;
         c.gridwidth = 3;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(0, 0, 0, 0);
         pedTab.add(assocPanel, c);
         c.gridy = 3;
         pedTab.add(tdtOptsPanel, c);
         c.gridy = 4;
         pedTab.add(tdtTypePanel, c);
         c.gridy = 5;
         c.gridwidth = 1;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(0, 0, 0, 5);
         pedTab.add(testFileLabel, c);
         c.gridx = 1;
         c.insets = new Insets(0, 0, 0, 0);
         pedTab.add(testFileField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         pedTab.add(browseAssocButton, c);
 
         //Haps Tab Objects
         hapsFileField = new JTextField(20);
         JButton browseHapsFileButton = new JButton("Browse");
         browseHapsFileButton.setActionCommand(BROWSE_HAPS);
         browseHapsFileButton.addActionListener(this);
         hapsInfoField = new JTextField(20);
         JButton browseHapsInfoButton = new JButton("Browse");
         browseHapsInfoButton.setActionCommand(BROWSE_INFO);
         browseHapsInfoButton.addActionListener(this);
 
         //Layout the Haps Tab
         JPanel hapsTab = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         hapsTab.add(new JLabel("Data File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         hapsTab.add(hapsFileField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         hapsTab.add(browseHapsFileButton, c);
         c.gridy = 1;
         c.gridx = 0;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         hapsTab.add(new JLabel("Locus Information File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(0, 0, 0, 0);
         hapsTab.add(hapsInfoField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         hapsTab.add(browseHapsInfoButton, c);
 
         //HMP Tab Objects
         hmpFileField = new JTextField(20);
         JButton browseHmpButton = new JButton("Browse");
         browseHmpButton.setActionCommand(BROWSE_HMP);
         browseHmpButton.addActionListener(this);
         doGB = new JCheckBox("Download and show HapMap info track (requires internet connection)");
         doGB.setSelected(false);
 
         //Layout the HMP Tab
         JPanel hmpTab = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         c.weightx = 1;
         hmpTab.add(new JLabel("Data File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         c.weightx = 0;
         hmpTab.add(hmpFileField, c);
         c.gridx = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(0, 10, 0, 0);
         c.weightx = 1;
         hmpTab.add(browseHmpButton, c);
         c.gridy = 1;
         c.gridx = 0;
         c.gridwidth = 3;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(0, 0, 0, 0);
         c.weightx = 0;
         hmpTab.add(doGB, c);
 
         //Phased Tab Objects
         /* phaseFormatPanel = new JPanel();
         phaseFormatPanel.add(new JLabel("Format:"));
         phaseFormatChooser.addActionListener(this);
         phaseFormatPanel.add(phaseFormatChooser);*/
         phaseFileField = new JTextField(20);
         browsePhaseButton = new JButton("Browse");
         browsePhaseButton.setActionCommand(BROWSE_PHASE);
         browsePhaseButton.addActionListener(this);
         phaseSampleField = new JTextField(20);
         browseSampleButton = new JButton("Browse");
         browseSampleButton.setActionCommand(BROWSE_SAMPLE);
         browseSampleButton.addActionListener(this);
         phaseLegendField = new JTextField(20);
         browseLegendButton = new JButton("Browse");
         browseLegendButton.setActionCommand(BROWSE_LEGEND);
         browseLegendButton.addActionListener(this);
         phaseGzipPanel = new JPanel();
         gZip = new JCheckBox("Files are GZIP compressed", false);
         gZip.setEnabled(true);
         phaseGzipPanel.add(gZip);
         phaseChromPanel = new JPanel();
         phaseDoGB = new JCheckBox("Download and show HapMap info track");
         phaseDoGB.setSelected(false);
         phaseChromPanel.add(phaseDoGB);
         phaseChromPanel.add(new JLabel("Chromosome:"));
         loadChromChooser.setSelectedIndex(-1);
         phaseChromPanel.add(loadChromChooser);
 
         //Layout the Phased Tab
         phaseTab = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.insets = new Insets(5, 5, 5, 5);
         c.weightx = 0;
         c.gridwidth = 1;
         c.anchor = GridBagConstraints.EAST;
         c.gridy = 1;
         phaseTab.add(new JLabel("Data File:"), c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 1;
         c.insets = new Insets(0, 0, 0, 0);
         phaseTab.add(phaseFileField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         c.anchor = GridBagConstraints.WEST;
         phaseTab.add(browsePhaseButton, c);
         c.gridx = 0;
         c.gridy = 2;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         phaseTab.add(new JLabel("Sample File:"), c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 1;
         phaseTab.add(phaseSampleField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         c.anchor = GridBagConstraints.WEST;
         phaseTab.add(browseSampleButton, c);
         c.gridx = 0;
         c.gridy = 3;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         phaseTab.add(new JLabel("Legend File:"), c);
         c.anchor = GridBagConstraints.CENTER;
         c.gridx = 1;
         phaseTab.add(phaseLegendField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         c.anchor = GridBagConstraints.WEST;
         phaseTab.add(browseLegendButton, c);
         c.gridx = 0;
         c.gridy = 4;
         c.anchor = GridBagConstraints.CENTER;
         c.gridwidth = 3;
         c.insets = new Insets(0, 0, 0, 0);
         phaseTab.add(phaseGzipPanel, c);
         c.gridy = 5;
         phaseTab.add(phaseChromPanel, c);
 
         //Download Tab Objects
         downloadChooserPanel = new JPanel();
         downloadChooserPanel.add(new JLabel("Version:"));
         downloadChooserPanel.add(hapVersionChooser);
         hapVersionChooser.setSelectedIndex(1);
         hapVersionChooser.setActionCommand("HapmapVersion");
         hapVersionChooser.addActionListener(this);
         downloadChooserPanel.add(new JLabel("Release:"));
         downloadChooserPanel.add(phaseChooser);
         phaseChooser.setActionCommand("Release");
         phaseChooser.addActionListener(this);
 //        downloadChooserPanel.add(new JLabel("Build:"));
 //        chromChooser.setSelectedIndex(-1);
 //        downloadChooserPanel.add(new JComboBox(BUILD_NAMES));
         downloadChooserPanel.add(new JLabel("Chr:"));
         chromChooser.setSelectedIndex(-1);
         downloadChooserPanel.add(chromChooser);
         downloadChooserPanel.add(new JLabel("Analysis Panel:"));
         downloadChooserPanel.add(panelChooser);
         downloadPositionPanel = new JPanel();
         chromStartField = new NumberTextField("", 6, false, false);
         chromStartField.setEnabled(true);
         chromEndField = new NumberTextField("", 6, false, false);
         chromEndField.setEnabled(true);
         downloadPositionPanel.add(new JLabel("Start kb:"));
         downloadPositionPanel.add(chromStartField);
         downloadPositionPanel.add(new JLabel("End kb:"));
         downloadPositionPanel.add(chromEndField);
         downloadBrowsePanel = new JPanel();
         downloadDoGB = new JCheckBox("Show HapMap info track");
         downloadDoGB.setSelected(true);
         downloadBrowsePanel.add(downloadDoGB);
 
         //GENECRUISER PANEL
         geneCruiserPanel = new JPanel();
         geneCruiserPanel.add(geneCruiseChooser);
         geneCruiserPanel.add(new JLabel("ID:"));
         geneCruiseField = new JTextField(10);
         geneCruiserPanel.add(geneCruiseField);
         geneCruiserPanel.add(new JLabel("+/-"));
         rangeField = new NumberTextField("100", 6, false, false);
         geneCruiserPanel.add(rangeField);
         geneCruiserPanel.add(new JLabel("kb"));
         geneCruiseButton = new JButton("Go");
         geneCruiseButton.setActionCommand("GeneCruise");
         geneCruiseButton.addActionListener(this);
         geneCruiserPanel.add(geneCruiseButton);
         geneCruiserPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray),
                 "GeneCruiser"));
         ((TitledBorder) (geneCruiserPanel.getBorder())).setTitleColor(Color.black);
         downloadLabel = new JLabel("*Phased HapMap downloads require an active internet connection");
 
         //Layout the Download Tab
         downloadTab = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.insets = new Insets(0, 0, 5, 0);
         c.anchor = GridBagConstraints.CENTER;
         downloadTab.add(downloadChooserPanel, c);
         c.gridy = 1;
         downloadTab.add(downloadPositionPanel, c);
         c.gridy = 2;
         downloadTab.add(downloadBrowsePanel, c);
         c.gridy = 3;
         c.weightx = 1;
         downloadTab.add(geneCruiserPanel, c);
         c.gridy = 4;
         downloadTab.add(downloadLabel, c);
 
         //Plink Tab Objects
         plinkFileField = new JTextField(20);
         JButton browsePlinkFileButton = new JButton("Browse");
         browsePlinkFileButton.setActionCommand(BROWSE_WGA);
         browsePlinkFileButton.addActionListener(this);
         mapLabel = new JLabel("Map File:");
         plinkMapField = new JTextField(20);
         browsePlinkMapButton = new JButton("Browse");
         browsePlinkMapButton.setActionCommand(BROWSE_MAP);
         browsePlinkMapButton.addActionListener(this);
         JPanel inputPanel = new JPanel();
         embeddedMap = new JCheckBox("Integrated Map Info");
         embeddedMap.addActionListener(this);
         embeddedMap.setSelected(false);
         inputPanel.add(embeddedMap);
         nonSNP = new JCheckBox("Non-SNP");
         nonSNP.addActionListener(this);
         nonSNP.setSelected(false);
         inputPanel.add(nonSNP);
         JPanel plinkChromPanel = new JPanel();
         plinkChrom = new JCheckBox("Only load results from Chromosome");
         plinkChrom.addActionListener(this);
         plinkChromPanel.add(plinkChrom);
         plinkChromChooser.setSelectedIndex(-1);
         plinkChromPanel.add(plinkChromChooser);
         plinkChromChooser.setEnabled(false);
         selectColumns = new JCheckBox("Select Columns");
         plinkChromPanel.add(selectColumns);
 
         //Layout the Plink Tab
         JPanel plinkTab = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         plinkTab.add(new JLabel("Results File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         plinkTab.add(plinkFileField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         plinkTab.add(browsePlinkFileButton, c);
         c.gridy = 1;
         c.gridx = 0;
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         plinkTab.add(mapLabel, c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(0, 0, 0, 0);
         plinkTab.add(plinkMapField, c);
         c.gridx = 2;
         c.insets = new Insets(0, 10, 0, 0);
         plinkTab.add(browsePlinkMapButton, c);
         c.gridy = 2;
         c.gridx = 0;
         c.gridwidth = 3;
         c.insets = new Insets(0, 0, 0, 0);
         plinkTab.add(inputPanel, c);
         c.gridy = 3;
         plinkTab.add(plinkChromPanel, c);
 
         //Meta Analysis Tab
         JTextField MetaFileField = new JTextField(20);
         JButton browseMetaButton = new JButton("Browse");
         browseMetaButton.setActionCommand(BROWSE_HMP);
         browseMetaButton.addActionListener(this);
 
         //Layout the HMP Tab
         JPanel metaTab = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.anchor = GridBagConstraints.EAST;
         c.insets = new Insets(5, 5, 5, 5);
         c.weightx = 1;
         metaTab.add(new JLabel("Data File:"), c);
         c.gridx = 1;
         c.anchor = GridBagConstraints.CENTER;
         c.weightx = 0;
         metaTab.add(MetaFileField, c);
         c.gridx = 2;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(0, 10, 0, 0);
         c.weightx = 1;
         metaTab.add(browseMetaButton, c);
         c.gridy = 1;
         c.gridx = 0;
         c.gridwidth = 3;
 
         //Add the tabs to the tabbed pane
         dataFormatPane.setFont(new Font("Default", Font.BOLD, 12));
         dataFormatPane.addTab("Linkage Format", pedTab);
         dataFormatPane.addTab("Haps Format", hapsTab);
         dataFormatPane.addTab("HapMap Format", hmpTab);
         //dataFormatPane.addTab("Phased Formats",phaseTab);
         dataFormatPane.addTab("HapMap PHASE", phaseTab);
         dataFormatPane.addTab("HapMap Download", downloadTab);
         dataFormatPane.addTab("PLINK Format", plinkTab);
         dataFormatPane.addTab("Meta Analysis", metaTab);
         //Set the selected Tab so you go back to the tab you were previously in
         dataFormatPane.setSelectedIndex(currTab);
 
         //Bottom pane with the OK button
         JButton okButton = new JButton("OK");
         okButton.addActionListener(this);
         this.getRootPane().setDefaultButton(okButton);
         JButton cancelButton = new JButton("Cancel");
         cancelButton.addActionListener(this);
         JButton proxyButton = new JButton("Proxy Settings");
         proxyButton.addActionListener(this);
         JPanel choicePanel = new JPanel();
         choicePanel.add(okButton);
         choicePanel.add(cancelButton);
         JPanel proxyPanel = new JPanel();
         proxyPanel.add(proxyButton);
         JPanel contents = new JPanel(new GridBagLayout());
         c = new GridBagConstraints();
         c.gridwidth = 3;
         contents.add(dataFormatPane, c);
         JPanel compDistPanel = new JPanel();
         compDistPanel.add(new JLabel("Ignore pairwise comparisons of markers >"));
         maxComparisonDistField = new NumberTextField(String.valueOf(Options.getMaxDistance() / 1000), 6, false, false);
         compDistPanel.add(maxComparisonDistField);
         compDistPanel.add(new JLabel("kb apart."));
         c.gridy = 1;
         contents.add(compDistPanel, c);
         JPanel missingCutoffPanel = new JPanel();
         missingCutoffField = new NumberTextField(String.valueOf(Options.getMissingThreshold() * 100), 3, false, false);
         missingCutoffPanel.add(new JLabel("Exclude individuals with >"));
         missingCutoffPanel.add(missingCutoffField);
         missingCutoffPanel.add(new JLabel("% missing genotypes."));
         c.gridy = 2;
         contents.add(missingCutoffPanel, c);
         c.gridy = 3;
         contents.add(choicePanel, c);
         c.gridx = 2;
         c.anchor = GridBagConstraints.EAST;
         contents.add(proxyPanel, c);
 
 //      SET GUI BASED ON PARAMETERS
         if (h.getPhasedSelection() != null) {
             if (((String) h.getPhasedSelection().get(0)).startsWith("16")) {
                 phaseChooser.setSelectedIndex(0);
             } else if (((String) h.getPhasedSelection().get(0)).startsWith("21")) {
                 phaseChooser.setSelectedIndex(1);
             } else {
                 phaseChooser.setSelectedIndex(2);
             }
 
             if (((String) h.getPhasedSelection().get(1)).equals("X")) {
                 chromChooser.setSelectedIndex(22);
             } else if (((String) h.getPhasedSelection().get(1)).equals("Y")) {
                 chromChooser.setSelectedIndex(23);
             } else {
                 chromChooser.setSelectedIndex(Integer.parseInt((String) h.getPhasedSelection().get(1)) - 1);
             }
 
             if (((String) h.getPhasedSelection().get(2)).equals("YRI")) {
                 panelChooser.setSelectedIndex(1);
             } else if (((String) h.getPhasedSelection().get(2)).equals("CHB+JPT")) {
                 panelChooser.setSelectedIndex(2);
             }
 
             chromStartField.setText((String) h.getPhasedSelection().get(3));
             chromEndField.setText((String) h.getPhasedSelection().get(4));
         }
 
         checkHapVersionChooser();
         checkPhaseChooser();
 
         this.setContentPane(contents);
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
         this.setResizable(false);
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         currTab = dataFormatPane.getSelectedIndex();
 
         if (command.equals("Cancel"))
             this.dispose();
 
         if (command.equals("Proxy Settings")) {
             ProxyDialog pd = new ProxyDialog(this, "Proxy Settings");
             pd.pack();
             pd.setVisible(true);
         }
 
         if (command.equals("OK")) {
 
             //workaround for dumb Swing can't requestFocus until shown bug
             //this one seems to throw a harmless exception in certain versions of the linux JRE
             try {
                 SwingUtilities.invokeLater(new Runnable() {
                     public void run() {
                         pedFileField.requestFocus();
                     }
                 });
             } catch (RuntimeException re) {
             }
 
             if (currTab == 0) {
                 fileType = PED_FILE;
 
                 if (doAssociation.isSelected() && fileType == PED_FILE) {
                     if (trioButton.isSelected()) {
                         Options.setAssocTest(ASSOC_TRIO);
                         if (standardTDT.isSelected()) {
                             Options.setTdtType(TDT_STD);
                         } else if (parenTDT.isSelected()) {
                             Options.setTdtType(TDT_PAREN);
                         }
                     } else {
                         Options.setAssocTest(ASSOC_CC);
                     }
                 } else {
                     Options.setAssocTest(ASSOC_NONE);
                 }
 
                 if (xChrom.isSelected() && fileType == PED_FILE) {
                     Chromosome.setDataChrom("chrx");
                 } else {
                     Chromosome.setDataChrom("none");
                 }
             } else if (currTab == 1) {
                 fileType = HAPS_FILE;
             } else if (currTab == 2) {
                 fileType = HMP_FILE;
             } else if (currTab == 3) {
                 /* if (phaseFormatChooser.getSelectedIndex() == 0){
                 fileType = PHASEHMP_FILE;
                 }else if (phaseFormatChooser.getSelectedIndex() == 1){
                     fileType = FASTPHASE_FILE;
                 }*/
                 fileType = PHASEHMP_FILE;
             } else if (currTab == 4) {
                 fileType = HMPDL_FILE;
 
             } else if (currTab == 5) {
                 fileType = PLINK_FILE;
             } else if (currTab == 6) {
                 fileType = META_FILE;
             }
 
             HaploView caller = (HaploView) this.getParent();
             if (missingCutoffField.getText().equals("")) {
                 Options.setMissingThreshold(1);
             } else {
                 double missingThreshold = (double) (Integer.parseInt(missingCutoffField.getText())) / 100;
                 if (missingThreshold > 1) {
                     JOptionPane.showMessageDialog(caller,
                             "Missing cutoff must be between 0 and 100",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 Options.setMissingThreshold(missingThreshold);
             }
 
             if (doGB.isSelected() && fileType == HMP_FILE) {
                 Options.setShowGBrowse(true);
             } else {
                 Options.setShowGBrowse(false);
             }
             Options.setgBrowseLeft(0);
             Options.setgBrowseRight(0);
 
             if (maxComparisonDistField.getText().equals("")) {
                 Options.setMaxDistance(0);
             } else {
                 Options.setMaxDistance(Integer.parseInt(maxComparisonDistField.getText()));
             }
             if (fileType == PHASEHMP_FILE /*|| fileType == FASTPHASE_FILE*/) {
                 if (gZip.isSelected()) {
                     Options.setGzip(true);
                 } else {
                     Options.setGzip(false);
                 }
                 if (phaseDoGB.isSelected()) {
                     Options.setShowGBrowse(true);
                     if (loadChromChooser.getSelectedIndex() == -1) {
                         JOptionPane.showMessageDialog(caller,
                                 "HapMap Info Track download requires a chromosome.",
                                 "Invalid value",
                                 JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                 } else {
                     Options.setShowGBrowse(false);
                 }
                 if (loadChromChooser.getSelectedIndex() == -1) {
                     chromChoice = "";
                 } else {
                     chromChoice = (String) loadChromChooser.getSelectedItem();
                 }
             }
             if (fileType == HMPDL_FILE) {
                 if (downloadDoGB.isSelected()) {
                     Options.setShowGBrowse(true);
                 } else {
                     Options.setShowGBrowse(false);
                 }
                 if (chromChooser.getSelectedIndex() == -1) {
                     JOptionPane.showMessageDialog(caller,
                             "Please select a chromosome.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (chromStartField.getText().equals("")) {
                     JOptionPane.showMessageDialog(caller,
                             "Please enter a starting value.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (chromEndField.getText().equals("")) {
                     JOptionPane.showMessageDialog(caller,
                             "Please enter an ending value.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 if (Integer.parseInt(chromStartField.getText()) >= Integer.parseInt(chromEndField.getText())) {
                     JOptionPane.showMessageDialog(caller,
                             "End position must be larger then start position.",
                             "Invalid value",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 chromChoice = (String) chromChooser.getSelectedItem();
                 panelChoice = (String) panelChooser.getSelectedItem();
                 phaseChoice = (String) phaseChooser.getSelectedItem();
             }
             if (fileType == PLINK_FILE) {
                 if (embeddedMap.isSelected()) {
                     embed = "E";
                 }
                 Options.setSNPBased(!nonSNP.isSelected());
                 if (plinkChrom.isSelected()) {
                     if (plinkChromChooser.getSelectedIndex() == -1) {
                         JOptionPane.showMessageDialog(caller,
                                 "Please select a chromosome to load.",
                                 "Invalid value",
                                 JOptionPane.ERROR_MESSAGE);
                         return;
                     }
                     chromChoice = new Integer((plinkChromChooser.getSelectedIndex() + 1)).toString();
                 } else {
                     chromChoice = null;
                 }
                 if (selectColumns.isSelected()) {
                     selectCols = "Y";
                 }
             }
             String[] returnStrings;
 
             if (fileType == HAPS_FILE) {
                 returnStrings = new String[]{hapsFileField.getText(), hapsInfoField.getText(), null};
                 if (returnStrings[1].equals("")) returnStrings[1] = null;
             } else if (fileType == HMP_FILE) {
                 returnStrings = new String[]{hmpFileField.getText(), null, null};
             } else if (fileType == PHASEHMP_FILE) {
                 returnStrings = new String[]{phaseFileField.getText(), phaseSampleField.getText(), phaseLegendField.getText(), chromChoice};
             }/*else if (fileType == FASTPHASE_FILE){
                 returnStrings = new String[]{phaseFileField.getText(), phaseSampleField.getText(), null, chromChoice};
             }*/
             else if (fileType == HMPDL_FILE) {
 
                 //TODO Give the user an option to choose the hapmap file.
 //                String hmpInfoFile = hapmapInfoFile;
 //
 //                File hmFile = new File(hmpInfoFile);
 //                if (!hmFile.exists()){
 //
 //                    JOptionPane.showMessageDialog(caller,
 //                            "Could not find Hapmap-info.txt",
 //                            "Invalid value",
 //                            JOptionPane.ERROR_MESSAGE);
 //
 //                    HaploView.fc.setSelectedFile(new File(""));
 //                    int returned = HaploView.fc.showOpenDialog(this);
 //                    if (returned != JFileChooser.APPROVE_OPTION) return;
 //                    System.out.println(HaploView.fc.getSelectedFile().getAbsolutePath());
 //
 //                    hmpInfoFile = HaploView.fc.getSelectedFile().getAbsolutePath();
 //
 //                }
 
                 returnStrings = new String[]{"Chr" + chromChoice + ":" + panelChoice + ":" + chromStartField.getText() + ".." +
                         chromEndField.getText(), panelChoice, chromStartField.getText(), chromEndField.getText(), chromChoice, phaseChoice, "txt"};
 
             } else if (fileType == PLINK_FILE) {
                 returnStrings = new String[]{plinkFileField.getText(), plinkMapField.getText(), null, embed, null, chromChoice, selectCols};
             } else {
                 returnStrings = new String[]{pedFileField.getText(), pedInfoField.getText(), testFileField.getText()};
                 if (returnStrings[1].equals("")) returnStrings[1] = null;
                 if (returnStrings[2].equals("") || !doAssociation.isSelected()) returnStrings[2] = null;
             }
             //if a dataset was previously loaded during this session, discard the display panes for it.
             caller.clearDisplays();
             this.dispose();
             if (fileType != PLINK_FILE) {
                 caller.readGenotypes(returnStrings, fileType);
             } else {
                 caller.readWGA(returnStrings);
             }
         }
 
         //TAB SPECIFIC TASKS
         if (currTab == 0) {                 // PED_FILE TAB
             if (command.equals(BROWSE_GENO)) {
                 browse(GENO_FILE);
             } else if (command.equals(BROWSE_INFO)) {
                 browse(INFO_FILE);
             } else if (command.equals(BROWSE_ASSOC)) {
                 browse(ASSOC_FILE);
             } else if (command.equals("xChrom")) {
                 if (xChrom.isSelected()) {
                     parenTDT.setEnabled(false);
                     standardTDT.setSelected(true);
                 } else if (standardTDT.isEnabled()) {
                     parenTDT.setEnabled(true);
                 }
             } else if (command.equals("association")) {
                 switchAssoc(doAssociation.isSelected());
             } else if (command.equals("tdt")) {
                 standardTDT.setEnabled(true);
                 if (!xChrom.isSelected()) {
                     parenTDT.setEnabled(true);
                 }
             } else if (command.equals("ccButton")) {
                 standardTDT.setEnabled(false);
                 parenTDT.setEnabled(false);
             }
         } else if (currTab == 1) {          // HAPS_FILE TAB;
             if (command.equals(BROWSE_HAPS)) {
                 browse(HAPS_FILE);
             } else if (command.equals(BROWSE_INFO)) {
                 browse(INFO_FILE);
             }
         } else if (currTab == 2) {                              // HMP_FILE TAB
             if (command.equals(BROWSE_HMP)) {
                 browse(HMP_FILE);
             }
         } else if (currTab == 3) {                              // PHASE TAB
             if (command.equals(BROWSE_PHASE)) {
                 browse(PHASEHMP_FILE);
             } else if (command.equals(BROWSE_LEGEND)) {
                 browse(LEGENDHMP_FILE);
             } else if (command.equals(BROWSE_SAMPLE)) {
                 browse(SAMPLEHMP_FILE);
             }
         } else if (currTab == 4) {                              //HMPDL TAB
 
             if (command.equals("Release")) {
 
                 checkPhaseChooser();
 
             } else if (command.equals("HapmapVersion")) {
 
                 checkHapVersionChooser();
                 checkPhaseChooser();
 
             } else if (command.equals("GeneCruise")) {
                 setCursor(new Cursor(Cursor.WAIT_CURSOR));
                 if (rangeField.getText().length() < 1) {
                     rangeField.setText("100");
                 }
                 try {
 
                     GeneCruiser gncr = new GeneCruiser(geneCruiseChooser.getSelectedIndex(), geneCruiseField.getText());
                    chromStartField.setText(String.valueOf((((int)gncr.getStart() / 1000)) -
                            Integer.parseInt(rangeField.getText())));
                    chromEndField.setText(String.valueOf((((int)gncr.getEnd() / 1000)) +
                            Integer.parseInt(rangeField.getText())));
                     chromChooser.setSelectedIndex(gncr.getChromosome() - 1);
 
                 } catch (HaploViewException hve) {
                     JOptionPane.showMessageDialog(this.getParent(),
                             hve.getMessage(),
                             "GeneCruiser Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
                 setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
 
         } else if (currTab == 5) {                              //PLINK TAB
             if (command.equals(BROWSE_WGA)) {
                 browse(PLINK_FILE);
             } else if (command.equals(BROWSE_MAP)) {
                 browse(MAP_FILE);
             } else if (command.equals("Integrated Map Info"))
 
             {
                 if (embeddedMap.isSelected()) {
                     embeddedMap.setSelected(true);
                     mapLabel.setEnabled(false);
                     plinkMapField.setEnabled(false);
                     browsePlinkMapButton.setEnabled(false);
                 } else {
                     embeddedMap.setSelected(false);
                     mapLabel.setEnabled(true);
                     plinkMapField.setEnabled(true);
                     browsePlinkMapButton.setEnabled(true);
                 }
             } else if (command.equals("Non-SNP"))
 
             {
                 if (nonSNP.isSelected()) {
                     nonSNP.setSelected(true);
                     mapLabel.setEnabled(false);
                     plinkMapField.setEnabled(false);
                     plinkMapField.setText("");
                     browsePlinkMapButton.setEnabled(false);
                     plinkChrom.setSelected(false);
                     plinkChrom.setEnabled(false);
                     plinkChromChooser.setSelectedIndex(-1);
                     plinkChromChooser.setEnabled(false);
                     embeddedMap.setSelected(false);
                     embeddedMap.setEnabled(false);
                 } else {
                     nonSNP.setSelected(false);
                     mapLabel.setEnabled(true);
                     plinkMapField.setEnabled(true);
                     browsePlinkMapButton.setEnabled(true);
                     plinkChrom.setEnabled(true);
                     embeddedMap.setEnabled(true);
                 }
             } else if (command.equals("Only load results from Chromosome"))
 
             {
                 if (plinkChrom.isSelected()) {
                     plinkChrom.setSelected(true);
                     plinkChromChooser.setEnabled(true);
                 } else {
                     plinkChrom.setSelected(false);
                     plinkChromChooser.setEnabled(false);
                 }
             }
         }
     }
 
     void browse(int browseType) {
         String name;
         String markerInfoName = "";
         String mapFileName = "";
         HaploView.fc.setSelectedFile(new File(""));
         int returned = HaploView.fc.showOpenDialog(this);
         if (returned != JFileChooser.APPROVE_OPTION) return;
         File file = HaploView.fc.getSelectedFile();
 
         if (browseType == GENO_FILE) {
             name = file.getName();
             pedFileField.setText(file.getParent() + File.separator + name);
 
             if (pedInfoField.getText().equals("")) {
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name, ".");
                 String baseName = st.nextToken();
                 int numPieces = st.countTokens() - 1;
                 for (int i = 0; i < numPieces; i++) {
                     baseName = baseName.concat(".").concat(st.nextToken());
                 }
 
                 //check for info file for original file sample.ped
                 //either sample.ped.info or sample.info
                 File maybeMarkers1 = new File(file.getParent(), name + MARKER_DATA_EXT);
                 File maybeMarkers2 = new File(file.getParent(), baseName + MARKER_DATA_EXT);
                 if (maybeMarkers1.exists()) {
                     markerInfoName = maybeMarkers1.getName();
                 } else if (maybeMarkers2.exists()) {
                     markerInfoName = maybeMarkers2.getName();
                 } else {
                     return;
                 }
                 pedInfoField.setText(file.getParent() + File.separator + markerInfoName);
             }
         } else if (browseType == HAPS_FILE) {
             name = file.getName();
             hapsFileField.setText(file.getParent() + File.separator + name);
 
             if (hapsInfoField.getText().equals("")) {
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name, ".");
                 String baseName = st.nextToken();
                 int numPieces = st.countTokens() - 1;
                 for (int i = 0; i < numPieces; i++) {
                     baseName = baseName.concat(".").concat(st.nextToken());
                 }
 
                 //check for info file for original file sample.haps
                 //either sample.haps.info or sample.info
                 File maybeMarkers1 = new File(file.getParent(), name + MARKER_DATA_EXT);
                 File maybeMarkers2 = new File(file.getParent(), baseName + MARKER_DATA_EXT);
                 if (maybeMarkers1.exists()) {
                     markerInfoName = maybeMarkers1.getName();
                 } else if (maybeMarkers2.exists()) {
                     markerInfoName = maybeMarkers2.getName();
                 } else {
                     return;
                 }
                 hapsInfoField.setText(file.getParent() + File.separator + markerInfoName);
             }
         } else if (browseType == HMP_FILE) {
             name = file.getName();
             hmpFileField.setText(file.getParent() + File.separator + name);
         } else if (browseType == PHASEHMP_FILE) {
             name = file.getName();
             phaseFileField.setText(file.getParent() + File.separator + name);
         } else if (browseType == SAMPLEHMP_FILE) {
             name = file.getName();
             phaseSampleField.setText(file.getParent() + File.separator + name);
         } else if (browseType == LEGENDHMP_FILE) {
             name = file.getName();
             phaseLegendField.setText(file.getParent() + File.separator + name);
         } else if (browseType == INFO_FILE) {
             markerInfoName = file.getName();
             if (dataFormatPane.getSelectedIndex() == 1) {
                 hapsInfoField.setText(file.getParent() + File.separator + markerInfoName);
             } else {
                 pedInfoField.setText(file.getParent() + File.separator + markerInfoName);
             }
         } else if (browseType == ASSOC_FILE) {
             testFileField.setText(file.getParent() + File.separator + file.getName());
         } else if (browseType == PLINK_FILE) {
             name = file.getName();
             plinkFileField.setText(file.getParent() + File.separator + name);
 
             if (plinkMapField.getText().equals("") && !nonSNP.isSelected()) {
                 //baseName should be everything but the final ".XXX" extension
                 StringTokenizer st = new StringTokenizer(name, ".");
                 String baseName = st.nextToken();
                 int numPieces = st.countTokens() - 1;
                 for (int i = 0; i < numPieces; i++) {
                     baseName = baseName.concat(".").concat(st.nextToken());
                 }
 
                 //check for map file for original file
                 //either .map or .bim extensions
                 File maybeMap1 = new File(file.getParent(), name + MAP_FILE_EXT);
                 File maybeMap2 = new File(file.getParent(), baseName + MAP_FILE_EXT);
                 File maybeMap3 = new File(file.getParent(), name + BIM_FILE_EXT);
                 File maybeMap4 = new File(file.getParent(), baseName + BIM_FILE_EXT);
                 if (maybeMap1.exists()) {
                     mapFileName = maybeMap1.getName();
                 } else if (maybeMap2.exists()) {
                     mapFileName = maybeMap2.getName();
                 } else if (maybeMap3.exists()) {
                     mapFileName = maybeMap3.getName();
                 } else if (maybeMap4.exists()) {
                     mapFileName = maybeMap4.getName();
                 } else {
                     return;
                 }
                 plinkMapField.setText(file.getParent() + File.separator + mapFileName);
             }
         } else if (browseType == MAP_FILE) {
             name = file.getName();
             plinkMapField.setText(file.getParent() + File.separator + name);
         }
     }
 
     public void insertUpdate
             (DocumentEvent
                     e) {
         checkInfo(e);
     }
 
     public void removeUpdate
             (DocumentEvent
                     e) {
         checkInfo(e);
     }
 
     public void changedUpdate
             (DocumentEvent
                     e) {
         //not fired by plain text components
     }
 
     private void switchAssoc
             (
                     boolean b) {
         if (b) {
             doAssociation.setEnabled(true);
             trioButton.setEnabled(true);
             ccButton.setEnabled(true);
             browseAssocButton.setEnabled(true);
             testFileField.setEnabled(true);
             testFileField.setBackground(Color.white);
             testFileLabel.setEnabled(true);
             if (trioButton.isSelected()) {
                 standardTDT.setEnabled(true);
                 if (!xChrom.isSelected()) {
                     parenTDT.setEnabled(true);
                 }
             }
         } else {
             doAssociation.setSelected(false);
             trioButton.setEnabled(false);
             ccButton.setEnabled(false);
             browseAssocButton.setEnabled(false);
             testFileField.setEnabled(false);
             testFileField.setBackground(this.getBackground());
             testFileLabel.setEnabled(false);
             standardTDT.setEnabled(false);
             parenTDT.setEnabled(false);
         }
     }
 
     private void checkHapVersionChooser() {
 
         String currentSelection = (String)hapVersionChooser.getSelectedItem();
         if (currentSelection.equals("2")) {
             phaseChooser.setModel(new JComboBox(RELEASE_NAMES).getModel());
             panelChooser.setModel(new JComboBox(PANEL_NAMES).getModel());
             phaseChooser.setSelectedIndex(1);
 
         } else {
             phaseChooser.setModel(new JComboBox(RELEASE_NAMES_HM3).getModel());
             panelChooser.setModel(new JComboBox(PANEL_NAMES_HM3).getModel());
         }
 
     }
 
     private void checkPhaseChooser() {
 
         String currentSelection = (String)phaseChooser.getSelectedItem();
         if ((currentSelection.equals("22")) || (currentSelection.equals("23"))) {
 
             geneCruiserPanel.setEnabled(true);
             geneCruiseChooser.setEnabled(true);
             geneCruiseField.setEnabled(true);
             rangeField.setEnabled(true);
             geneCruiseButton.setEnabled(true);
 
         } else {
 
             geneCruiserPanel.setEnabled(false);
             geneCruiseChooser.setEnabled(false);
             geneCruiseField.setEnabled(false);
             rangeField.setEnabled(false);
             geneCruiseButton.setEnabled(false);
 
         }
     }
 
     private void checkInfo
             (DocumentEvent
                     e) {
         //the text in the info field has changed. if it is empty, disable assoc testing
         if (pedInfoField.getText().equals("")) {
             switchAssoc(false);
             doAssociation.setEnabled(false);
         } else {
             doAssociation.setEnabled(true);
         }
     }
 }
