 package edu.mit.wi.haploview;
 
 
 import edu.mit.wi.pedfile.PedFileException;
 import edu.mit.wi.pedfile.CheckData;
 import edu.mit.wi.haploview.association.*;
 import edu.mit.wi.haploview.tagger.TaggerConfigPanel;
 import edu.mit.wi.haploview.tagger.TaggerResultsPanel;
 import edu.mit.wi.tagger.Tagger;
 
 import javax.help.*;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import java.net.URL;
 
 import com.sun.jimi.core.Jimi;
 import com.sun.jimi.core.JimiException;
 
 public class HaploView extends JFrame implements ActionListener, Constants{
 
     boolean DEBUG = false;
 
     JMenuItem readMarkerItem, analysisItem, blocksItem, gbrowseItem, spacingItem, gbEditItem;
     String exportItems[] = {
         EXPORT_TEXT, EXPORT_PNG, EXPORT_OPTIONS
     };
     JMenuItem exportMenuItems[];
     JMenu keyMenu, displayMenu, analysisMenu;
     JMenuItem clearBlocksItem;
 
     String viewItems[] = {
         VIEW_DPRIME, VIEW_HAPLOTYPES, VIEW_CHECK_PANEL, VIEW_TAGGER, VIEW_ASSOC
     };
     JRadioButtonMenuItem viewMenuItems[];
     String zoomItems[] = {
         "Zoomed", "Medium", "Unzoomed"
     };
     JRadioButtonMenuItem zoomMenuItems[];
     String colorItems[] = {
         "Standard (D' / LOD)", "R-squared", "D' / LOD (alt)", "Confidence bounds", "4 Gamete"
     };
     JRadioButtonMenuItem colorMenuItems[];
     JRadioButtonMenuItem blockMenuItems[];
     String blockItems[] = {"Confidence intervals (Gabriel et al)",
                            "Four Gamete Rule",
                            "Solid spine of LD",
                            "Custom"};
 
     HaploData theData;
 
     private int currentBlockDef = BLOX_GABRIEL;
     private javax.swing.Timer timer;
 
     static HaploView window;
     JFileChooser fc;
     DPrimeDisplay dPrimeDisplay;
     private JScrollPane hapScroller;
     private HaplotypeDisplay hapDisplay;
     JTabbedPane tabs;
     CheckDataController cdc;
     PermutationTestPanel permutationPanel;
     CheckDataPanel checkPanel;
     CustomAssocPanel custAssocPanel;
     TDTPanel tdtPanel;    
     HaploAssocPanel hapAssocPanel;
     private TaggerConfigPanel taggerConfigPanel;
     HaploviewTab ldTab, hapsTab, checkTab, taggerTab, associationTab;
 
     public HaploView(){
         try{
             fc = new JFileChooser(System.getProperty("user.dir"));
         }catch(NullPointerException n){
             try{
                 UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                 fc = new JFileChooser(System.getProperty("user.dir"));
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             }catch(Exception e){
             }
         }
 
 
         //menu setup
         JMenuBar menuBar = new JMenuBar();
         setJMenuBar(menuBar);
         JMenuItem menuItem;
 
         //file menu
         JMenu fileMenu = new JMenu("File");
         menuBar.add(fileMenu);
 
         menuItem = new JMenuItem(READ_GENOTYPES);
         setAccelerator(menuItem, 'O', false);
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         /*
         viewGenotypesItem = new JMenuItem(VIEW_GENOTYPES);
         viewGenotypesItem.addActionListener(this);
         //viewGenotypesItem.setEnabled(false);
         fileMenu.add(viewGenotypesItem);
         */
 
         readMarkerItem = new JMenuItem(READ_MARKERS);
         setAccelerator(readMarkerItem, 'I', false);
         readMarkerItem.addActionListener(this);
         readMarkerItem.setEnabled(false);
         fileMenu.add(readMarkerItem);
 
         analysisItem = new JMenuItem(READ_ANALYSIS_TRACK);
         setAccelerator(analysisItem, 'A', false);
         analysisItem.addActionListener(this);
         analysisItem.setEnabled(false);
         fileMenu.add(analysisItem);
 
         blocksItem = new JMenuItem(READ_BLOCKS_FILE);
         setAccelerator(blocksItem, 'B', false);
         blocksItem.addActionListener(this);
         blocksItem.setEnabled(false);
         fileMenu.add(blocksItem);
 
         gbrowseItem = new JMenuItem(DOWNLOAD_GBROWSE);
         gbrowseItem.addActionListener(this);
         gbrowseItem.setEnabled(false);
         fileMenu.add(gbrowseItem);
 
         /*
         viewMarkerItem = new JMenuItem(VIEW_MARKERS);
         viewMarkerItem.addActionListener(this);
         //viewMarkerItem.setEnabled(false);
         fileMenu.add(viewMarkerItem);
         */
 
         fileMenu.addSeparator();
 
         exportMenuItems = new JMenuItem[exportItems.length];
         for (int i = 0; i < exportItems.length; i++) {
             exportMenuItems[i] = new JMenuItem(exportItems[i]);
             exportMenuItems[i].addActionListener(this);
             exportMenuItems[i].setEnabled(false);
             fileMenu.add(exportMenuItems[i]);
         }
 
         //update menu item
         fileMenu.addSeparator();
         menuItem = new JMenuItem("Check for update");
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
 
         fileMenu.addSeparator();
         menuItem = new JMenuItem("Quit");
         setAccelerator(menuItem, 'Q', false);
         menuItem.addActionListener(this);
         fileMenu.add(menuItem);
 
         fileMenu.setMnemonic(KeyEvent.VK_F);
 
         /// display menu
         displayMenu = new JMenu("Display");
         displayMenu.setMnemonic(KeyEvent.VK_D);
         menuBar.add(displayMenu);
 
         ButtonGroup group = new ButtonGroup();
         viewMenuItems = new JRadioButtonMenuItem[viewItems.length];
         for (int i = 0; i < viewItems.length; i++) {
             viewMenuItems[i] = new JRadioButtonMenuItem(viewItems[i], i == 0);
             viewMenuItems[i].addActionListener(this);
 
             KeyStroke ks = KeyStroke.getKeyStroke('1' + i, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
             viewMenuItems[i].setAccelerator(ks);
 
             displayMenu.add(viewMenuItems[i]);
             viewMenuItems[i].setEnabled(false);
             group.add(viewMenuItems[i]);
         }
         displayMenu.addSeparator();
         //a submenu
         ButtonGroup zg = new ButtonGroup();
         JMenu zoomMenu = new JMenu("LD zoom");
         zoomMenu.setMnemonic(KeyEvent.VK_Z);
         zoomMenuItems = new JRadioButtonMenuItem[zoomItems.length];
         for (int i = 0; i < zoomItems.length; i++){
             zoomMenuItems[i] = new JRadioButtonMenuItem(zoomItems[i], i==0);
             zoomMenuItems[i].addActionListener(this);
             zoomMenuItems[i].setActionCommand("zoom" + i);
             zoomMenu.add(zoomMenuItems[i]);
             zg.add(zoomMenuItems[i]);
         }
         displayMenu.add(zoomMenu);
         //another submenu
         ButtonGroup cg = new ButtonGroup();
         JMenu colorMenu = new JMenu("LD color scheme");
         colorMenu.setMnemonic(KeyEvent.VK_C);
         colorMenuItems = new JRadioButtonMenuItem[colorItems.length];
         for (int i = 0; i< colorItems.length; i++){
             colorMenuItems[i] = new JRadioButtonMenuItem(colorItems[i],i==0);
             colorMenuItems[i].addActionListener(this);
             colorMenuItems[i].setActionCommand("color" + i);
             colorMenu.add(colorMenuItems[i]);
             cg.add(colorMenuItems[i]);
         }
         colorMenuItems[Options.getLDColorScheme()].setSelected(true);
         displayMenu.add(colorMenu);
 
         spacingItem = new JMenuItem("LD Display Spacing");
         spacingItem.setMnemonic(KeyEvent.VK_S);
         spacingItem.addActionListener(this);
         spacingItem.setEnabled(false);
         displayMenu.add(spacingItem);
 
         //gbrowse options editor
         gbEditItem = new JMenuItem(GBROWSE_OPTS);
         gbEditItem.setMnemonic(KeyEvent.VK_H);
         gbEditItem.addActionListener(this);
         gbEditItem.setEnabled(false);
         displayMenu.add(gbEditItem);
 
         displayMenu.setEnabled(false);
 
         //analysis menu
         analysisMenu = new JMenu("Analysis");
         analysisMenu.setMnemonic(KeyEvent.VK_A);
         menuBar.add(analysisMenu);
         //a submenu
         ButtonGroup bg = new ButtonGroup();
         JMenu blockMenu = new JMenu("Define Blocks");
         blockMenu.setMnemonic(KeyEvent.VK_B);
         blockMenuItems = new JRadioButtonMenuItem[blockItems.length];
         for (int i = 0; i < blockItems.length; i++){
             blockMenuItems[i] = new JRadioButtonMenuItem(blockItems[i], i==0);
             blockMenuItems[i].addActionListener(this);
             blockMenuItems[i].setActionCommand("block" + i);
             blockMenuItems[i].setEnabled(false);
             blockMenu.add(blockMenuItems[i]);
             bg.add(blockMenuItems[i]);
         }
         analysisMenu.add(blockMenu);
         clearBlocksItem = new JMenuItem(CLEAR_BLOCKS);
         setAccelerator(clearBlocksItem, 'C', false);
         clearBlocksItem.addActionListener(this);
         clearBlocksItem.setEnabled(false);
         analysisMenu.add(clearBlocksItem);
         JMenuItem customizeBlocksItem = new JMenuItem(CUST_BLOCKS);
         customizeBlocksItem.addActionListener(this);
         analysisMenu.add(customizeBlocksItem);
         analysisMenu.setEnabled(false);
 
         JMenu helpMenu = new JMenu("Help");
         helpMenu.setMnemonic(KeyEvent.VK_H);
         menuBar.add(helpMenu);
 
         JMenuItem helpContentsItem;
         helpContentsItem = new JMenuItem("Help Contents");
         HelpSet hs;
         HelpBroker hb;
         String helpHS = "jhelpset.hs";
         try {
             URL hsURL = HelpSet.findHelpSet(HaploView.class.getClassLoader(), helpHS);
             hs = new HelpSet(null, hsURL);
         } catch (Exception ee) {
             System.out.println( "HelpSet " + ee.getMessage());
             System.out.println("HelpSet "+ helpHS +" not found");
             return;
         }
         hb = hs.createHelpBroker();
         helpContentsItem.addActionListener(new CSH.DisplayHelpFromSource(hb));
         helpContentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
         helpMenu.add(helpContentsItem);
 
         menuItem = new JMenuItem("About Haploview");
         menuItem.addActionListener(this);
         helpMenu.add(menuItem);
 
 
         //color key
         keyMenu = new JMenu("Key");
         menuBar.add(Box.createHorizontalGlue());
         menuBar.add(keyMenu);
 
 
 
         /*
         Configuration.readConfigFile();
         if(Configuration.isCheckForUpdate()) {
             Object[] options = {"Yes",
                                 "Not now",
                                 "Never ask again"};
             int n = JOptionPane.showOptionDialog(this,
                     "Would you like to check if a new version "
                     + "of haploview is available?",
                     "Check for update",
                     JOptionPane.YES_NO_CANCEL_OPTION,
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     options,
                     options[1]);
 
             if(n == JOptionPane.YES_OPTION) {
                 UpdateChecker uc = new UpdateChecker();
                 if(uc.checkForUpdate()) {
                     JOptionPane.showMessageDialog(this,
                             "A new version of Haploview is available!\n Visit http://www.broad.mit.edu/mpg/haploview/ to download the new version\n (current version: " + Constants.VERSION
                             + "  newest version: " + uc.getNewVersion() + ")" ,
                             "Update Available",
                             JOptionPane.INFORMATION_MESSAGE);
                 }
             }
             else if(n == JOptionPane.CANCEL_OPTION) {
                 Configuration.setCheckForUpdate(false);
                 Configuration.writeConfigFile();
             }
         }           */
 
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e){
                 quit();
             }
         });
     }
 
 
     // function workaround for overdesigned, underthought swing api -fry
     void setAccelerator(JMenuItem menuItem, char what, boolean shift) {
         menuItem.setAccelerator(KeyStroke.getKeyStroke(what, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | (shift ? ActionEvent.SHIFT_MASK : 0)));
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals(READ_GENOTYPES)){
             ReadDataDialog readDialog = new ReadDataDialog("Open new data", this);
             readDialog.pack();
             readDialog.setVisible(true);
         } else if (command.equals(READ_MARKERS)){
             //JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 readMarkers(fc.getSelectedFile(),null);
             }
         }else if (command.equals(READ_ANALYSIS_TRACK)){
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION){
                 readAnalysisFile(fc.getSelectedFile());
             }
         }else if (command.equals(DOWNLOAD_GBROWSE)){
             GBrowseDialog gbd = new GBrowseDialog(this, "Connect to HapMap Info Server");
             gbd.pack();
             gbd.setVisible(true);
         }else if (command.equals(GBROWSE_OPTS)){
             GBrowseOptionDialog gbod = new GBrowseOptionDialog(this, "HapMap Info Track Options");
             gbod.pack();
             gbod.setVisible(true);
         }else if (command.equals(READ_BLOCKS_FILE)){
             fc.setSelectedFile(new File(""));
             if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
                 readBlocksFile(fc.getSelectedFile());
             }
         }else if (command.equals(CUST_BLOCKS)){
             TweakBlockDefsDialog tweakDialog = new TweakBlockDefsDialog("Customize Blocks", this);
             tweakDialog.pack();
             tweakDialog.setVisible(true);
         }else if (command.equals(CLEAR_BLOCKS)){
             changeBlocks(BLOX_NONE);
 
             //blockdef clauses
         }else if (command.startsWith("block")){
             int method = Integer.valueOf(command.substring(5)).intValue();
 
             changeBlocks(method);
 
             //zooming clauses
         }else if (command.startsWith("zoom")){
             dPrimeDisplay.zoom(Integer.valueOf(command.substring(4)).intValue());
 
             //coloring clauses
         }else if (command.startsWith("color")){
             Options.setLDColorScheme(Integer.valueOf(command.substring(5)).intValue());
             dPrimeDisplay.colorDPrime();
             changeKey();
             //exporting clauses
         }else if (command.equals(EXPORT_PNG)){
             export((HaploviewTab)tabs.getSelectedComponent(), PNG_MODE, 0, Chromosome.getUnfilteredSize());
         }else if (command.equals(EXPORT_TEXT)){
             export((HaploviewTab)tabs.getSelectedComponent(), TXT_MODE, 0, Chromosome.getUnfilteredSize());
         }else if (command.equals(EXPORT_OPTIONS)){
             ExportDialog exDialog = new ExportDialog(this);
             exDialog.pack();
             exDialog.setVisible(true);
         }else if (command.equals("Select All")){
             checkPanel.selectAll();
         }else if (command.equals("Rescore Markers")){
             String cut = cdc.hwcut.getText();
             if (cut.equals("")){
                 cut = "0";
             }
             CheckData.hwCut = Double.parseDouble(cut);
 
             cut = cdc.genocut.getText();
             if (cut.equals("")){
                 cut="0";
             }
             CheckData.failedGenoCut = Integer.parseInt(cut);
 
             cut = cdc.mendcut.getText();
             if (cut.equals("")){
                 cut="0";
             }
             CheckData.numMendErrCut = Integer.parseInt(cut);
 
             cut = cdc.mafcut.getText();
             if (cut.equals("")){
                 cut="0";
             }
             CheckData.mafCut = Double.parseDouble(cut);
 
             checkPanel.redoRatings();
             JTable jt = checkPanel.getTable();
             jt.repaint();
         }else if (command.equals("LD Display Spacing")){
             ProportionalSpacingDialog spaceDialog = new ProportionalSpacingDialog(this, "Adjust LD Spacing");
             spaceDialog.pack();
             spaceDialog.setVisible(true);
         }else if (command.equals("About Haploview")){
             JOptionPane.showMessageDialog(this,
                     ABOUT_STRING,
                     "About Haploview",
                     JOptionPane.INFORMATION_MESSAGE);
         } else if(command.equals("Check for update")) {
             final SwingWorker worker = new SwingWorker(){
                 UpdateChecker uc;
                 String unableToConnect;
                 public Object construct() {
                     uc = new UpdateChecker();
                     try {
                         uc.checkForUpdate();
                     } catch(IOException ioe) {
                         unableToConnect = ioe.getMessage();
                     }
                     return null;
                 }
                 public void finished() {
                     window.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                     if(uc != null) {
                         if(unableToConnect != null) {
                             JOptionPane.showMessageDialog(window,
                                     "An error occured while checking for update.\n " + unableToConnect ,
                                     "Update Check",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                         else if(uc.isNewVersionAvailable()) {
                             UpdateDisplayDialog udp = new UpdateDisplayDialog(window,"Update Check",uc);
                             udp.pack();
                             udp.setVisible(true);
                         } else {
                             JOptionPane.showMessageDialog(window,
                                     "Your version of Haploview is up to date.",
                                     "Update Check",
                                     JOptionPane.INFORMATION_MESSAGE);
                         }
                     }
 
                 }
             };
             setCursor(new Cursor(Cursor.WAIT_CURSOR));
             worker.start();
         }else if (command.equals("Quit")){
             quit();
         } else {
             for (int i = 0; i < viewItems.length; i++) {
                 if (command.equals(viewItems[i])) tabs.setSelectedIndex(i);
             }
         }
     }
 
     private void changeKey() {
         int scheme = Options.getLDColorScheme();
         keyMenu.removeAll();
         if (scheme == WMF_SCHEME){
             JMenuItem keyItem = new JMenuItem("High D' / High LOD");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.black);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             for (int i = 1; i < 5; i++){
                 double gr = i * (255.0 / 6.0);
                 keyItem = new JMenuItem("");
                 keyItem.setPreferredSize(size);
                 keyItem.setBackground(new Color((int)gr, (int)gr, (int)gr));
                 keyMenu.add(keyItem);
             }
             keyItem = new JMenuItem("Low D' / Low LOD");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
 
             keyItem = new JMenuItem("High D' / High LOD");
             keyItem.setBackground(Color.black);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             for (int i = 1; i < 5; i++){
                 double r = i * (255.0 / 6.0);
                 double gb = i * (200.0 / 6.0);
                 keyItem = new JMenuItem("");
                 keyItem.setPreferredSize(size);
                 keyItem.setBackground(new Color((int)r, (int)gb, (int)gb));
                 keyMenu.add(keyItem);
             }
             keyItem = new JMenuItem("High D' / Low LOD");
             keyItem.setBackground(new Color(255, 200, 200));
             keyMenu.add(keyItem);
 	} else if (scheme == RSQ_SCHEME){
             JMenuItem keyItem = new JMenuItem("High R-squared");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.black);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
 	    keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.darkGray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
 	    keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.gray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("");
 	    keyItem.setPreferredSize(size);
             keyItem.setBackground(Color.lightGray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Low R-squared");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
 	} else if (scheme == STD_SCHEME){
             JMenuItem keyItem = new JMenuItem("High D'");
             Dimension size = keyItem.getPreferredSize();
             keyItem.setBackground(Color.red);
             keyMenu.add(keyItem);
             for (int i = 1; i < 4; i++){
                 double blgr = (255-32)*2*(0.5*i/3);
                 keyItem = new JMenuItem("");
                 keyItem.setPreferredSize(size);
                 keyItem.setBackground(new Color(255,(int)blgr, (int)blgr));
                 keyMenu.add(keyItem);
             }
             keyItem = new JMenuItem("Low D'");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("High D' / Low LOD");
             keyItem.setBackground(new Color(192, 192, 240));
             keyMenu.add(keyItem);
         } else if (scheme == GAB_SCHEME){
             JMenuItem keyItem = new JMenuItem("Strong Linkage");
             keyItem.setBackground(Color.darkGray);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Uninformative");
             keyItem.setBackground(Color.lightGray);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("Recombination");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
         } else if (scheme == GAM_SCHEME){
             JMenuItem keyItem = new JMenuItem("< 4 Gametes");
             keyItem.setBackground(Color.darkGray);
             keyItem.setForeground(Color.white);
             keyMenu.add(keyItem);
             keyItem = new JMenuItem("4 Gametes");
             keyItem.setBackground(Color.white);
             keyMenu.add(keyItem);
         }
     }
 
     void quit(){
         //any handling that might need to take place here
         Configuration.writeConfigFile();
         System.exit(0);
     }
 
     void readAnalysisFile(File inFile){
         try{
             theData.readAnalysisTrack(inFile);
         }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (IOException ioe){
             JOptionPane.showMessageDialog(this,
                     ioe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         dPrimeDisplay.computePreferredSize();
         if (dPrimeDisplay != null && tabs.getSelectedComponent().equals(ldTab)){
             dPrimeDisplay.repaint();
         }
     }
 
     void readGenotypes(String[] inputOptions, int type){
         //input is a 2 element array with
         //inputOptions[0] = ped file
         //inputOptions[1] = info file (null if none)
         //inputOptions[2] = custom association test list file (null if none)
         //type is either 3 or 4 for ped and hapmap files respectively
         final File inFile = new File(inputOptions[0]);
         final AssociationTestSet customAssocSet;
 
         try {
             if (inputOptions[2] != null && inputOptions[1] == null){
                 throw new HaploViewException("A marker information file is required if a tests file is specified.");
             }
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             if (inFile.length() < 1){
                 throw new HaploViewException("Genotype file is empty or nonexistent: " + inFile.getName());
             }
 
             if (type == HAPS_FILE){
                 //these are not available for non ped files
                 viewMenuItems[VIEW_CHECK_NUM].setEnabled(false);
                 viewMenuItems[VIEW_ASSOC_NUM].setEnabled(false);
                 Options.setAssocTest(ASSOC_NONE);
             }
             theData = new HaploData();
 
             if (type == HAPS_FILE){
                 theData.prepareHapsInput(new File(inputOptions[0]));
             }else{
                 theData.linkageToChrom(inFile, type);
             }
 
             if(type != HAPS_FILE && theData.getPedFile().isBogusParents()) {
                 JOptionPane.showMessageDialog(this,
                         "One or more individuals in the file reference non-existent parents.\nThese references have been ignored.",
                         "File Error",
                         JOptionPane.ERROR_MESSAGE);
             }
 
 
             //deal with marker information
             theData.infoKnown = false;
             File markerFile;
             if (inputOptions[1] == null){
                 markerFile = null;
             }else{
                 markerFile = new File(inputOptions[1]);
             }
 
             //turn on/off gbrowse menu
             if (Options.isGBrowseShown()){
                 gbEditItem.setEnabled(true);
             }else{
                 gbEditItem.setEnabled(false);
             }
 
             checkPanel = null;
             if (type == HAPS_FILE){
                 readMarkers(markerFile, null);
 
                 //initialize realIndex
                 Chromosome.doFilter(Chromosome.getUnfilteredSize());
                 customAssocSet = null;
             }else{
                 readMarkers(markerFile, theData.getPedFile().getHMInfo());
                 //we read the file in first, so we can whitelist all the markers in the custom test set
                 HashSet whiteListedCustomMarkers = new HashSet();
                 if (inputOptions[2] != null){
                     customAssocSet = new AssociationTestSet(inputOptions[2]);
                     whiteListedCustomMarkers = customAssocSet.getWhitelist();
                 }else{
                     customAssocSet = null;
                 }
                 theData.setWhiteList(whiteListedCustomMarkers);
 
                 checkPanel = new CheckDataPanel(this);
                 checkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
                 //set up the indexing to take into account skipped markers.
                 Chromosome.doFilter(checkPanel.getMarkerResults());
             }
 
 
             //let's start the math
             final SwingWorker worker = new SwingWorker(){
                 public Object construct(){
                     for (int i = 0; i < viewMenuItems.length; i++){
                         viewMenuItems[i].setEnabled(false);
                     }
                     dPrimeDisplay=null;
 
                     changeKey();
                     theData.generateDPrimeTable();
                     theData.guessBlocks(BLOX_GABRIEL);
                     //theData.guessBlocks(BLOX_NONE);  //for debugging, doesn't call blocks at first
 
                     blockMenuItems[0].setSelected(true);
                     zoomMenuItems[0].setSelected(true);
                     theData.blocksChanged = false;
                     Container contents = getContentPane();
                     contents.removeAll();
 
                     tabs = new JTabbedPane();
                     tabs.addChangeListener(new TabChangeListener());
 
                     //first, draw the D' picture
                     dPrimeDisplay = new DPrimeDisplay(window);
                     JScrollPane dPrimeScroller = new JScrollPane(dPrimeDisplay);
                     dPrimeScroller.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
                     dPrimeScroller.getVerticalScrollBar().setUnitIncrement(60);
                     dPrimeScroller.getHorizontalScrollBar().setUnitIncrement(60);
                     ldTab = new HaploviewTab(dPrimeScroller);
                     tabs.addTab(VIEW_DPRIME, ldTab);
                     viewMenuItems[VIEW_D_NUM].setEnabled(true);
 
                    HaploviewTab currentTab = ldTab;

                     //compute and show haps on next tab
                     try {
                         hapDisplay = new HaplotypeDisplay(theData);
                     } catch(HaploViewException e) {
                         JOptionPane.showMessageDialog(window,
                                 e.getMessage(),
                                 "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                     HaplotypeDisplayController hdc =
                             new HaplotypeDisplayController(hapDisplay);
                     hapScroller = new JScrollPane(hapDisplay);
                     hapScroller.getVerticalScrollBar().setUnitIncrement(60);
                     hapScroller.getHorizontalScrollBar().setUnitIncrement(60);
                     hapsTab = new HaploviewTab(hapScroller);
                     hapsTab.add(hdc);
                     tabs.addTab(VIEW_HAPLOTYPES, hapsTab);
                     viewMenuItems[VIEW_HAP_NUM].setEnabled(true);
                     displayMenu.setEnabled(true);
                     analysisMenu.setEnabled(true);
 
                     //check data panel
                     if (checkPanel != null){
                         checkTab = new HaploviewTab(checkPanel);
                         cdc = new CheckDataController(window);
                         checkTab.add(cdc);
 
                         tabs.addTab(VIEW_CHECK_PANEL, checkTab);
                         viewMenuItems[VIEW_CHECK_NUM].setEnabled(true);
                         currentTab=checkTab;
                     }
 
                     //only show tagger if we have a .info file
                     if (theData.infoKnown){
                         //tagger display
                         taggerConfigPanel = new TaggerConfigPanel(theData);
 
                         JPanel metaTagPanel = new JPanel();
 
                         metaTagPanel.setLayout(new BoxLayout(metaTagPanel,BoxLayout.Y_AXIS));
                         metaTagPanel.add(taggerConfigPanel);
 
                         JTabbedPane tagTabs = new JTabbedPane();
                         tagTabs.add("Configuration",metaTagPanel);
 
                         JPanel resMetaPanel = new JPanel();
 
                         resMetaPanel.setLayout(new BoxLayout(resMetaPanel,BoxLayout.Y_AXIS));
 
                         TaggerResultsPanel tagResultsPanel = new TaggerResultsPanel();
                         taggerConfigPanel.addActionListener(tagResultsPanel);
 
                         resMetaPanel.add(tagResultsPanel);
                         tagTabs.addTab("Results",resMetaPanel);
                         taggerTab = new HaploviewTab(tagTabs);
                         tabs.addTab(VIEW_TAGGER,taggerTab);
                         viewMenuItems[VIEW_TAGGER_NUM].setEnabled(true);
                     }
 
                     //Association panel
                     if(Options.getAssocTest() != ASSOC_NONE) {
                         JTabbedPane metaAssoc = new JTabbedPane();
                         try{
                             tdtPanel = new TDTPanel(new AssociationTestSet(theData.getPedFile(), null, Chromosome.getAllMarkers()));
                         } catch(PedFileException e) {
                             JOptionPane.showMessageDialog(window,
                                     e.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
 
 
                         metaAssoc.add("Single Marker", tdtPanel);
 
                         hapAssocPanel = new HaploAssocPanel(new AssociationTestSet(theData.getHaplotypes(), null));
                         metaAssoc.add("Haplotypes", hapAssocPanel);
 
                         //custom association tests
                         custAssocPanel = null;
                         if(customAssocSet != null) {
                             try {
                                 customAssocSet.runFileTests(theData, tdtPanel.getTestSet().getMarkerAssociationResults());
                                 custAssocPanel = new CustomAssocPanel(customAssocSet);
                                 metaAssoc.addTab("Custom",custAssocPanel);
                                 metaAssoc.setSelectedComponent(custAssocPanel);
                             } catch (HaploViewException e) {
                                 JOptionPane.showMessageDialog(window,
                                     e.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                             }
                         }
 
                         AssociationTestSet permSet;
                         boolean cust = false;
                         if (custAssocPanel != null){
                             permSet = custAssocPanel.getTestSet();
                             cust = true;
                         }else{
                             permSet = new AssociationTestSet();
                             permSet.cat(tdtPanel.getTestSet());
                             permSet.cat(hapAssocPanel.getTestSet());
                         }
 
                         permutationPanel = new PermutationTestPanel(new PermutationTestSet(0,theData.getSavedEMs(),
                                 theData.getPedFile(),permSet), cust);
                         metaAssoc.add(permutationPanel,"Permutation Tests");
 
                         associationTab = new HaploviewTab(metaAssoc);
                         tabs.addTab(VIEW_ASSOC, associationTab);
                         viewMenuItems[VIEW_ASSOC_NUM].setEnabled(true);
 
                     }
 
 
 
                     tabs.setSelectedComponent(currentTab);
                     contents.add(tabs);
 
                     repaint();
                     setVisible(true);
 
                     theData.finished = true;
                     setTitle(TITLE_STRING + " -- " + inFile.getName());
                     return null;
                 }
             };
 
             timer = new javax.swing.Timer(50, new ActionListener(){
                 public void actionPerformed(ActionEvent evt){
                     if (theData.finished){
                         timer.stop();
                         for (int i = 0; i < blockMenuItems.length; i++){
                             blockMenuItems[i].setEnabled(true);
                         }
                         clearBlocksItem.setEnabled(true);
                         readMarkerItem.setEnabled(true);
                         blocksItem.setEnabled(true);
                         exportMenuItems[2].setEnabled(true);
 
                         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     }
                 }
             });
 
             worker.start();
             timer.start();
         }catch(IOException ioexec) {
             JOptionPane.showMessageDialog(this,
                     ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }catch(PedFileException pfe){
             JOptionPane.showMessageDialog(this,
                     pfe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
             setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
     }
 
     void readBlocksFile(File file) {
        try{
            Vector cust = theData.readBlocks(file);
            theData.guessBlocks(BLOX_CUSTOM, cust);
            changeBlocks(BLOX_CUSTOM);
        }catch (HaploViewException hve){
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (IOException ioe){
             JOptionPane.showMessageDialog(this,
                     ioe.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     void readMarkers(File inputFile, String[][] hminfo){
         try {
             theData.prepareMarkerInput(inputFile, hminfo);
             if (theData.infoKnown){
                 analysisItem.setEnabled(true);
                 gbrowseItem.setEnabled(true);
                 spacingItem.setEnabled(true);
             }else{
                 analysisItem.setEnabled(false);
                 gbrowseItem.setEnabled(false);
                 spacingItem.setEnabled(false);
             }
             if (checkPanel != null){
                 //this is triggered when loading markers after already loading genotypes
                 //it is dumb and sucks, but at least it works. bah.
                 checkPanel = new CheckDataPanel(this);
                 checkTab.removeAll();
 
                 JPanel metaCheckPanel = new JPanel();
                 metaCheckPanel.setLayout(new BoxLayout(metaCheckPanel, BoxLayout.Y_AXIS));
                 metaCheckPanel.add(checkPanel);
                 cdc = new CheckDataController(window);
                 metaCheckPanel.add(cdc);
 
                 checkTab.add(metaCheckPanel);
                 repaint();
             }
             if (tdtPanel != null){
                 tdtPanel.refreshNames();
             }
 
             if (dPrimeDisplay != null){
                 dPrimeDisplay.computePreferredSize();
             }
         }catch (HaploViewException e){
             JOptionPane.showMessageDialog(this,
                     e.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }catch (IOException ioexec){
             JOptionPane.showMessageDialog(this,
                     ioexec.getMessage(),
                     "File Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 
     public int getCurrentBlockDef() {
         return currentBlockDef;
     }
 
     public void changeBlocks(int method){
         if (method == BLOX_NONE || method == BLOX_CUSTOM){
             blockMenuItems[BLOX_CUSTOM].setSelected(true);
         }
         if (method != BLOX_CUSTOM){
             theData.guessBlocks(method);
         }
 
         dPrimeDisplay.repaint();
         currentBlockDef = method;
 
         try{
             if (tabs.getSelectedComponent().equals(hapsTab)){
                 hapDisplay.getHaps();
             }
         }catch(HaploViewException hve) {
             JOptionPane.showMessageDialog(this,
                     hve.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         hapScroller.setViewportView(hapDisplay);
     }
 
     public void clearDisplays() {
         if (tabs != null){
             tabs.removeAll();
             dPrimeDisplay = null;
             hapDisplay = null;
             tdtPanel = null;
             checkPanel = null;
         }
     }
 
     class TabChangeListener implements ChangeListener{
         public void stateChanged(ChangeEvent e) {
             if (tabs.getSelectedIndex() != -1){
                 window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
                 HaploviewTab tab = (HaploviewTab)tabs.getSelectedComponent();
                 if (tab.equals(ldTab) || tab.equals(hapsTab)){
                     exportMenuItems[0].setEnabled(true);
                     exportMenuItems[1].setEnabled(true);
                 }else if (tab.equals(associationTab) || tab.equals(checkTab) || tab.equals(taggerTab)){
                     exportMenuItems[0].setEnabled(true);
                     exportMenuItems[1].setEnabled(false);
                 }else{
                     exportMenuItems[0].setEnabled(false);
                     exportMenuItems[1].setEnabled(false);
                 }
 
                 //if we've adjusted the haps display thresh we need to change the haps ass panel
                 if (tab.equals(associationTab)){
                     JTabbedPane metaAssoc = (JTabbedPane)associationTab.getComponent(0);
                     //this is the haps ass tab inside the assoc super-tab
                     HaploAssocPanel htp = (HaploAssocPanel) metaAssoc.getComponent(1);
                     if (htp.initialHaplotypeDisplayThreshold != Options.getHaplotypeDisplayThreshold()){
                         htp.makeTable(new AssociationTestSet(theData.getHaplotypes(), null));
                         permutationPanel.setBlocksChanged();
                         if (custAssocPanel == null){
                             //change tests if we don't have a custom set
                             AssociationTestSet permSet = new AssociationTestSet();
                             permSet.cat(tdtPanel.getTestSet());
                             permSet.cat(hapAssocPanel.getTestSet());
                             permutationPanel.setTestSet(new PermutationTestSet(0,theData.getSavedEMs(),theData.getPedFile(),permSet));
                         }
                     }
                 }
 
                 if (tab.equals(ldTab)){
                     keyMenu.setEnabled(true);
                 }else{
                     keyMenu.setEnabled(false);
                 }
 
                 viewMenuItems[tabs.getSelectedIndex()].setSelected(true);
 
                 if (checkPanel != null && checkPanel.changed){
                     //first store up the current blocks
                     Vector currentBlocks = new Vector();
                     for (int blocks = 0; blocks < theData.blocks.size(); blocks++){
                         int thisBlock[] = (int[]) theData.blocks.elementAt(blocks);
                         int thisBlockReal[] = new int[thisBlock.length];
                         for (int marker = 0; marker < thisBlock.length; marker++){
                             thisBlockReal[marker] = Chromosome.realIndex[thisBlock[marker]];
                         }
                         currentBlocks.add(thisBlockReal);
                     }
 
                     Chromosome.doFilter(checkPanel.getMarkerResults());
 
                     //after editing the filtered marker list, needs to be prodded into
                     //resizing correctly
                     dPrimeDisplay.computePreferredSize();
                     dPrimeDisplay.colorDPrime();
 
                     hapDisplay.theData = theData;
 
                     if (currentBlockDef != BLOX_CUSTOM){
                         changeBlocks(currentBlockDef);
                     }else{
                         //adjust the blocks
                         Vector theBlocks = new Vector();
                         for (int x = 0; x < currentBlocks.size(); x++){
                             Vector goodies = new Vector();
                             int currentBlock[] = (int[])currentBlocks.elementAt(x);
                             for (int marker = 0; marker < currentBlock.length; marker++){
                                 for (int y = 0; y < Chromosome.realIndex.length; y++){
                                     //we only keep markers from the input that are "good" from checkdata
                                     //we also realign the input file to the current "good" subset since input is
                                     //indexed of all possible markers in the dataset
                                     if (Chromosome.realIndex[y] == currentBlock[marker]){
                                         goodies.add(new Integer(y));
                                     }
                                 }
                             }
                             int thisBlock[] = new int[goodies.size()];
                             for (int marker = 0; marker < thisBlock.length; marker++){
                                 thisBlock[marker] = ((Integer)goodies.elementAt(marker)).intValue();
                             }
                             if (thisBlock.length > 1){
                                 theBlocks.add(thisBlock);
                             }
                         }
                         theData.guessBlocks(BLOX_CUSTOM, theBlocks);
                     }
 
                     if (tdtPanel != null){
                         tdtPanel.refreshTable();
                     }
 
                     if (taggerConfigPanel != null){
                         taggerConfigPanel.refreshTable();
                     }
 
                     if(permutationPanel != null) {
                         permutationPanel.setBlocksChanged();
                         if (custAssocPanel == null){
                             //change tests if we don't have a custom set
                             AssociationTestSet permSet = new AssociationTestSet();
                             permSet.cat(tdtPanel.getTestSet());
                             permSet.cat(hapAssocPanel.getTestSet());
                             permutationPanel.setTestSet(new PermutationTestSet(0,theData.getSavedEMs(),theData.getPedFile(),permSet));
                         }
                     }
 
                     checkPanel.changed=false;
                 }
 
                 if (hapDisplay != null && theData.blocksChanged){
                     try{
                         hapDisplay.getHaps();
                         if(Options.getAssocTest() != ASSOC_NONE) {
                             //this is the haps ass tab inside the assoc super-tab
                             hapAssocPanel.makeTable(new AssociationTestSet(theData.getHaplotypes(), null));
 
                             permutationPanel.setBlocksChanged();
                             if (custAssocPanel == null){
                                 //change tests if we don't have a custom set
                                 AssociationTestSet permSet = new AssociationTestSet();
                                 permSet.cat(tdtPanel.getTestSet());
                                 permSet.cat(hapAssocPanel.getTestSet());
                                 permutationPanel.setTestSet(new PermutationTestSet(0,theData.getSavedEMs(),theData.getPedFile(),permSet));
                             }
                         }
                     }catch(HaploViewException hv){
                         JOptionPane.showMessageDialog(window,
                                 hv.getMessage(),
                                 "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                     hapScroller.setViewportView(hapDisplay);
 
                     theData.blocksChanged = false;
                 }
                 setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             }
         }
     }
 
     void export(HaploviewTab tab, int format, int start, int stop){
         fc.setSelectedFile(new File(""));
         if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION){
             File outfile = fc.getSelectedFile();
             if (format == PNG_MODE || format == COMPRESSED_PNG_MODE){
                 BufferedImage image = null;
                 if (tab.equals(ldTab)){
                     try {
                         if (format == PNG_MODE){
                             image = dPrimeDisplay.export(start, stop, false);
                         }else{
                             image = dPrimeDisplay.export(start, stop, true);
                         }
                     } catch(HaploViewException hve) {
                         JOptionPane.showMessageDialog(this,
                                 hve.getMessage(),
                                 "Export Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }else if (tab.equals(hapsTab)){
                     image = hapDisplay.export();
                 }else{
                     image = new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
                 }
 
                 if (image != null){
                     try{
                         String filename = outfile.getPath();
                         if (! (filename.endsWith(".png") || filename.endsWith(".PNG"))){
                             filename += ".png";
                         }
                         Jimi.putImage("image/png", image, filename);
                     }catch(JimiException je){
                         JOptionPane.showMessageDialog(this,
                                 je.getMessage(),
                                 "Error",
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             } else if (format == TXT_MODE){
                 try{
                     if (tab.equals(ldTab)){
                         theData.saveDprimeToText(outfile, TABLE_TYPE, start, stop);
                     }else if (tab.equals(hapsTab)){
                         theData.saveHapsToText(hapDisplay.filteredHaplos,hapDisplay.multidprimeArray, outfile);
                     }else if (tab.equals(checkTab)){
                         checkPanel.printTable(outfile);
                     }else if (tab.equals(associationTab)){
                         Component selectedTab = ((JTabbedPane)associationTab.getComponent(0)).getSelectedComponent();
 
                         if(selectedTab == tdtPanel){
                             tdtPanel.getTestSet().saveSNPsToText(outfile);
                         }else if (selectedTab == hapAssocPanel){
                             hapAssocPanel.getTestSet().saveHapsToText(outfile);
                         }else if (selectedTab == permutationPanel){
                             permutationPanel.export(outfile);
                         }else if (selectedTab == custAssocPanel){
                             custAssocPanel.getTestSet().saveResultsToText(outfile);
                         }
                     }else if (tab.equals(taggerTab)){
                         taggerConfigPanel.export(outfile);
                     }
                 }catch(IOException ioe){
                     JOptionPane.showMessageDialog(this,
                             ioe.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }catch(HaploViewException he){
                     JOptionPane.showMessageDialog(this,
                             he.getMessage(),
                             "Error",
                             JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
     }
 
     public static void main(String[] args) {
         //set defaults
         Options.setTaggerRsqCutoff(Tagger.DEFAULT_RSQ_CUTOFF);
         Options.setTaggerLODCutoff(Tagger.DEFAULT_LOD_CUTOFF);
         Options.setMissingThreshold(0.5);
         Options.setSpacingThreshold(0.0);
         Options.setAssocTest(ASSOC_NONE);
         Options.setHaplotypeDisplayThreshold(1);
         Options.setMaxDistance(500);
         Options.setLDColorScheme(STD_SCHEME);
         Options.setShowGBrowse(false);
         Options.setgBrowseOpts(GB_DEFAULT_OPTS);
         Options.setgBrowseTypes(GB_DEFAULT_TYPES);
 
         //this parses the command line arguments. if nogui mode is specified,
         //then haploText will execute whatever the user specified
         HaploText argParser = new HaploText(args);
 
         //if nogui is specified, then HaploText has already executed everything, and let Main() return
         //otherwise, we want to actually load and run the gui
         if(!argParser.isNogui()) {
             try {
                 UIManager.put("EditorPane.selectionBackground",Color.lightGray);
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             } catch (Exception e) { }
             //System.setProperty("swing.disableFileChooserSpeedFix", "true");
 
             window  =  new HaploView();
 
             //setup view object
             window.setTitle(TITLE_STRING);
             window.setSize(800,600);
 
             final SwingWorker worker = new SwingWorker(){
                 UpdateChecker uc;
                 public Object construct() {
                     uc = new UpdateChecker();
                     try {
                         uc.checkForUpdate();
                     } catch(IOException ioe) {
                         //this means we couldnt connect but we want it to die quietly
                     }
                     return null;
                 }
                 public void finished() {
                     if(uc != null) {
                         if(uc.isNewVersionAvailable()) {
                             //theres an update available so lets pop some crap up
                             final JLayeredPane jlp = window.getLayeredPane();
 
                             final JPanel udp = new JPanel();
                             udp.setLayout(new BoxLayout(udp, BoxLayout.Y_AXIS));
                             double version = uc.getNewVersion();
                             Font detailsFont = new Font("Default", Font.PLAIN, 14);
                             JLabel announceLabel = new JLabel("A newer version of Haploview (" +version+") is available.");
                             announceLabel.setFont(detailsFont);
                             JLabel detailsLabel = new JLabel("See \"Check for update\" in the file menu for details.");
                             detailsLabel.setFont(detailsFont);
                             udp.add(announceLabel);
                             udp.add(detailsLabel);
 
                             udp.setBorder(BorderFactory.createRaisedBevelBorder());
                             int width = udp.getPreferredSize().width;
                             int height = udp.getPreferredSize().height;
                             int borderwidth = udp.getBorder().getBorderInsets(udp).right;
                             int borderheight = udp.getBorder().getBorderInsets(udp).bottom;
                             udp.setBounds(jlp.getWidth()-width-borderwidth, jlp.getHeight()-height-borderheight,
                                    udp.getPreferredSize().width, udp.getPreferredSize().height);
                             udp.setOpaque(true);
 
                             jlp.add(udp, JLayeredPane.POPUP_LAYER);
 
                             java.util.Timer updateTimer = new java.util.Timer();
                             //show this update message for 6.5 seconds
                             updateTimer.schedule(new TimerTask() {
                                 public void run() {
                                     jlp.remove(udp);
                                     jlp.repaint();
                                 }
                             },6000);
 
                         }
                     }
                 }
             };
 
 
 
             //center the window on the screen
             Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
             window.setLocation((screen.width - window.getWidth()) / 2,
                     (screen.height - window.getHeight()) / 2);
 
             window.setVisible(true);
             worker.start();
 
 
             //parse command line stuff for input files or prompt data dialog
             String[] inputArray = new String[2];
             if (argParser.getHapsFileName() != null){
                 inputArray[0] = argParser.getHapsFileName();
                 inputArray[1] = argParser.getInfoFileName();
                 window.readGenotypes(inputArray, HAPS_FILE);
             }else if (argParser.getPedFileName() != null){
                 inputArray[0] = argParser.getPedFileName();
                 inputArray[1] = argParser.getInfoFileName();
                 window.readGenotypes(inputArray, PED_FILE);
             }else if (argParser.getHapmapFileName() != null){
                 inputArray[0] = argParser.getHapmapFileName();
                 inputArray[1] = null;
                 window.readGenotypes(inputArray, HMP_FILE);
             }else{
                 ReadDataDialog readDialog = new ReadDataDialog("Welcome to HaploView", window);
                 readDialog.pack();
                 readDialog.setVisible(true);
             }
         }
     }
 }
 
 
 
 
