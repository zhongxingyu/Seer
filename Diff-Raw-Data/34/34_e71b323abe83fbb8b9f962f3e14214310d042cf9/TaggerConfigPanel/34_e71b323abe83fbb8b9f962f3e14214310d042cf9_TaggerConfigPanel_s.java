 package edu.mit.wi.haploview.tagger;
 
 import edu.mit.wi.haploview.*;
 import edu.mit.wi.tagger.Tagger;
 import edu.mit.wi.tagger.TaggerException;
 
 import javax.swing.*;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Dimension;
 import java.awt.Color;
 import java.util.Vector;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 import java.io.*;
 
 public class TaggerConfigPanel extends HaploviewTab
        implements TableModelListener, ActionListener {
     static final long serialVersionUID = 1616877753933097437L;
     private JTable table;
     private TableSorter sorter;
     private TaggerController tagControl;
 
     private final static int NUM_COL = 0;
     private final static int NAME_COL = 1;
     private final static int POSITION_COL = 2;
     private final static int DESIGN_COL = 3;
     private final static int INCLUDE_COL = 4;
     private final static int EXCLUDE_COL = 5;
     private final static int CAPTURE_COL = 6;
 
     private JButton runTaggerButton;
     public static JFileChooser fc;
     private Timer timer;
     private HaploData theData;
     private Hashtable snpsByName, designScores;
     private NumberTextField rsqField, minDesignField, lodField, maxNumTagsField, minDistField;
     private ButtonGroup aggressiveGroup;
     private JPanel bottomButtonPanel = new JPanel();
     private JPanel taggerProgressPanel = new JPanel();
     JProgressBar taggerProgress = new JProgressBar();
     private JLabel taggerProgressLabel = new JLabel("Tagging...");
     private boolean plinkExists = false;
 
     public TaggerConfigPanel(HaploData hd, boolean plink)  {
         theData = hd;
         plinkExists = plink;
         refreshTable();
         try{
             fc = new JFileChooser(System.getProperty("user.dir"));
         }catch(NullPointerException n){
             try{
                 UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                 fc = new JFileChooser(System.getProperty("user.dir"));
                 UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             }catch(Exception e){
                 JOptionPane.showMessageDialog(this,
                         e.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
    public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == INCLUDE_COL){
            //if they check force include for some row, then we uncheck force exclude for that row
            if(((Boolean)table.getValueAt(e.getFirstRow(),e.getColumn())).booleanValue()) {
                table.setValueAt(new Boolean(false),e.getFirstRow(),EXCLUDE_COL);
            }
        }
        else if(e.getColumn() == EXCLUDE_COL) {
            //if they check force exclude for some row, then we uncheck force include for that row
            if(((Boolean)table.getValueAt(e.getFirstRow(),e.getColumn())).booleanValue()) {
                table.setValueAt(new Boolean(false),e.getFirstRow(),INCLUDE_COL);
            }
        }
    }
 
     public void refreshTable(){
         this.removeAll();
 
         snpsByName = new Hashtable();
 
         this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
         Vector columnNames = new Vector();
         Vector tableData = new Vector();
 
         columnNames.add("#");
         columnNames.add("Name");
         columnNames.add("Position");
         columnNames.add("Design Score");
         columnNames.add("Force Include");
         columnNames.add("Force Exclude");
         columnNames.add("Capture this Allele?");
 
         for (int i = 0; i < Chromosome.getSize(); i++){
             SNP tempSNP = Chromosome.getMarker(i);
             snpsByName.put(tempSNP.getDisplayName(), tempSNP);
             Vector tempData = new Vector();
 
             tempData.add(Integer.toString(Chromosome.realIndex[i]+1));
             tempData.add(tempSNP.getDisplayName());
             tempData.add(String.valueOf(tempSNP.getPosition()));
             tempData.add("0");
             tempData.add(new Boolean(false));
             tempData.add(new Boolean(false));
             tempData.add(new Boolean(true));
 
             tableData.add(tempData);
         }
         TagConfigTableModel tableModel = new TagConfigTableModel(columnNames,tableData);
        tableModel.addTableModelListener(this);
         sorter = new TableSorter(tableModel);
         table = new JTable(sorter);
         sorter.setTableHeader(table.getTableHeader());
         table.getColumnModel().getColumn(NUM_COL).setPreferredWidth(30);
         table.getColumnModel().getColumn(POSITION_COL).setPreferredWidth(60);
         table.getColumnModel().getColumn(DESIGN_COL).setPreferredWidth(60);
         table.getColumnModel().getColumn(INCLUDE_COL).setPreferredWidth(65);
         table.getColumnModel().getColumn(EXCLUDE_COL).setPreferredWidth(65);
         table.getColumnModel().getColumn(CAPTURE_COL).setPreferredWidth(90);
         table.getTableHeader().setReorderingAllowed(false);
 
 
         JScrollPane scrollPane = new JScrollPane(table);
         scrollPane.setPreferredSize(new Dimension(600, 700));
         scrollPane.setMaximumSize(scrollPane.getPreferredSize());
         //scrollPane.getViewport().setBackground(Color.WHITE);
         add(scrollPane);
 
         runTaggerButton = new JButton("<html><b>Run Tagger</b>");
         runTaggerButton.addActionListener(this);
         runTaggerButton.setActionCommand("Run Tagger");
         JButton includeResultsButton = new JButton("Force in PLINK SNPs");
         includeResultsButton.addActionListener(this);
         JButton resetTableButton = new JButton("Reset Table");
         resetTableButton.addActionListener(this);
         JButton forceIncludeButton = new JButton("Load Includes");
         forceIncludeButton.addActionListener(this);
         JButton includeAllButton = new JButton("Include All");
         includeAllButton.addActionListener(this);
         JButton forceExcludeButton = new JButton("Load Excludes");
         forceExcludeButton.addActionListener(this);
         JButton excludeAllButton = new JButton("Exclude All");
         excludeAllButton.addActionListener(this);
         JButton excludeATCGButton = new JButton("Exclude A/T & C/G SNPs");
         excludeATCGButton.addActionListener(this);
         JButton uncaptureAllButton = new JButton("Uncapture All");
         uncaptureAllButton.addActionListener(this);
         JButton designScoresButton = new JButton("Design Scores");
         designScoresButton.addActionListener(this);
         JButton allelesCapturedButton = new JButton("Alleles to Capture");
         allelesCapturedButton.addActionListener(this);
         JButton resetThresholdsButton = new JButton("Reset Thresholds");
         resetThresholdsButton.addActionListener(this);
 
         JPanel buttonPanel = new JPanel();
         //buttonPanel.setMaximumSize(new Dimension(600,100));
         buttonPanel.add(includeAllButton);
         buttonPanel.add(excludeAllButton);
         buttonPanel.add(uncaptureAllButton);
         buttonPanel.add(excludeATCGButton);
         buttonPanel.add(resetTableButton);
         add(buttonPanel);
 
         JPanel optsRightPanel = new JPanel();
         optsRightPanel.setLayout(new BoxLayout(optsRightPanel, BoxLayout.Y_AXIS));
 
         JPanel rsqPanel = new JPanel();
         JLabel rsqLabel = new JLabel("r\u00b2 threshold");
         rsqPanel.add(rsqLabel);
         rsqField = new NumberTextField(String.valueOf(Options.getTaggerRsqCutoff()),5,true,false);
         rsqPanel.add(rsqField);
         JLabel mdsLabel = new JLabel("Min design score");
         rsqPanel.add(mdsLabel);
         minDesignField = new NumberTextField("",5,true,false);
         rsqPanel.add(minDesignField);
         optsRightPanel.add(rsqPanel);
 
         JPanel lodPanel = new JPanel();
         JLabel lodLabel = new JLabel("LOD threshold for multi-marker tests");
         lodPanel.add(lodLabel);
         lodField = new NumberTextField(String.valueOf(Options.getTaggerLODCutoff()),5,true,false);
         lodPanel.add(lodField);
         optsRightPanel.add(lodPanel);
 
         JPanel maxNumPanel = new JPanel();
         maxNumPanel.add(new JLabel("Max tags"));
         maxNumTagsField = new NumberTextField("",5,false,false);
         maxNumPanel.add(maxNumTagsField);
         maxNumPanel.add(new JLabel("Min distance between tags"));
         minDistField = new NumberTextField("0",5,false,false);
         maxNumPanel.add(minDistField);
         maxNumPanel.add(new JLabel("bp"));
 
         optsRightPanel.add(maxNumPanel);
 
         JPanel optsLeftPanel = new JPanel();
         optsLeftPanel.setLayout(new BoxLayout(optsLeftPanel, BoxLayout.Y_AXIS));
         JRadioButton pairwiseButton = new JRadioButton("pairwise tagging only");
         pairwiseButton.setActionCommand(String.valueOf(Tagger.PAIRWISE_ONLY));
         optsLeftPanel.add(pairwiseButton);
         JRadioButton dupleButton = new JRadioButton("aggressive tagging: use 2-marker haplotypes");
         dupleButton.setActionCommand(String.valueOf(Tagger.AGGRESSIVE_DUPLE));
         optsLeftPanel.add(dupleButton);
         JRadioButton tripleButton = new JRadioButton("aggressive tagging: use 2- and 3-marker haplotypes");
         tripleButton.setActionCommand(String.valueOf(Tagger.AGGRESSIVE_TRIPLE));
         optsLeftPanel.add(tripleButton);
         aggressiveGroup = new ButtonGroup();
         aggressiveGroup.add(pairwiseButton);
         aggressiveGroup.add(dupleButton);
         aggressiveGroup.add(tripleButton);
         pairwiseButton.setSelected(true);
 
         JPanel optsPanel = new JPanel();
         //optsPanel.setMaximumSize(new Dimension(800,600));
         //preferredViewPortsize
         optsPanel.add(optsLeftPanel);
         optsPanel.add(optsRightPanel);
         add(optsPanel);
 
         bottomButtonPanel = new JPanel();
         bottomButtonPanel.setPreferredSize(new Dimension(350,100));
         //bottomButtonPanel.
         //bottomButtonPanel.setBorder(BorderFactory.createLineBorder(Color.black));
         bottomButtonPanel.add(runTaggerButton);
         if (plinkExists){
             bottomButtonPanel.add(includeResultsButton);
         }
         bottomButtonPanel.add(forceIncludeButton);
         bottomButtonPanel.add(forceExcludeButton);
         bottomButtonPanel.add(allelesCapturedButton);
         bottomButtonPanel.add(designScoresButton);
         bottomButtonPanel.add(resetThresholdsButton);
 
         add(bottomButtonPanel);
     }
 
     public void addActionListener(ActionListener al){
         listenerList.add(ActionListener.class, al);
     }
 
     public void fireTaggerEvent(ActionEvent ae){
         Object listeners[] = listenerList.getListenerList();
         for (int i = 0; i <= listeners.length-1; i+=2){
             if (listeners[i] == ActionListener.class){
                 ((ActionListener)listeners[i+1]).actionPerformed(ae);
             }
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals("Run Tagger")) {
             for (int j = 0; j < table.getColumnCount(); j++){
                 sorter.setSortingStatus(j,TableSorter.NOT_SORTED);
             }
 
             try{
                 if(!rsqField.getText().equals("")){
                     double rsqCut = Double.parseDouble(rsqField.getText());
                     if (rsqCut > 1){
                         Options.setTaggerRsqCutoff(1.0);
                         rsqField.setText("1.0");
                     }else if (rsqCut < 0){
                         Options.setTaggerRsqCutoff(0.0);
                         rsqField.setText("0.0");
                     }else{
                         Options.setTaggerRsqCutoff(rsqCut);
                     }
                 }
 
                 if (!(minDesignField.getText().equals(""))){
                     Options.setTaggerMinDesignScore(Double.parseDouble(minDesignField.getText()));
                 }else{
                     Options.setTaggerMinDesignScore(Tagger.DEFAULT_MIN_DESIGNSCORE);
                 }
 
                 if (!lodField.getText().equals("")){
                     double lodCut = Double.parseDouble(lodField.getText());
                     if (lodCut < 0){
                         Options.setTaggerLODCutoff(0.0);
                         lodField.setText("0.0");
                     }else{
                         Options.setTaggerLODCutoff(lodCut);
                     }
                 }
 
                 int minDist;
                 if (minDistField.getText().equals("")){
                     minDist = 0;
                 }else{
                     minDist = Integer.parseInt(minDistField.getText());
                 }
                 if (minDist < 0){
                     Options.setTaggerMinDistance(0);
                     minDistField.setText("");
                 }else{
                     Options.setTaggerMinDistance(minDist);
                 }
 
                 int maxNumTags;
                 if (maxNumTagsField.getText().equals("")){
                     maxNumTags = 0;
                 }else{
                     maxNumTags = Integer.parseInt(maxNumTagsField.getText());
                 }
 
                 //build include/exclude lists
                 Vector include = new Vector();
                 Vector exclude = new Vector();
                 Vector capture = new Vector();
                 Vector allsnps = new Vector();
                 for(int i= 0;i <table.getRowCount(); i++) {
                     if(((Boolean)table.getValueAt(i,INCLUDE_COL)).booleanValue()) {
                         include.add((String)table.getValueAt(i,NAME_COL));
                     }else if(((Boolean)table.getValueAt(i,EXCLUDE_COL)).booleanValue()) {
                         exclude.add((String)table.getValueAt(i,NAME_COL));
                     }
                     if (((Boolean)table.getValueAt(i,CAPTURE_COL)).booleanValue()){
                         capture.add((String)table.getValueAt(i,NAME_COL));
                     }
                     allsnps.add(snpsByName.get(table.getValueAt(i,NAME_COL)));
                 }
 
                 tagControl = new TaggerController(theData,allsnps,include,exclude,capture,designScores,
                         Integer.valueOf(aggressiveGroup.getSelection().getActionCommand()).intValue(),maxNumTags,true);
 
                 runTaggerButton.setEnabled(false);
 
                 taggerProgress.setIndeterminate(true);
                 taggerProgress.setForeground(new Color(40,40,255));
                 taggerProgress.setMaximumSize(new Dimension(250,20));
                 taggerProgressPanel.setLayout(new BoxLayout(taggerProgressPanel,BoxLayout.Y_AXIS));
                 taggerProgressPanel.add(taggerProgressLabel);
                 taggerProgressLabel.setAlignmentX(CENTER_ALIGNMENT);
                 taggerProgressPanel.add(new JLabel("         "));
                 taggerProgressPanel.add(taggerProgress);
                 remove(bottomButtonPanel);
                 add(taggerProgressPanel);
                 revalidate();
 
                 tagControl.runTagger();
 
                 final TaggerConfigPanel tcp = this;
                 timer = new Timer(100, new ActionListener(){
                     public void actionPerformed(ActionEvent e) {
                         if(tagControl.isTaggingCompleted()) {
                             remove(taggerProgressPanel);
                             add(bottomButtonPanel);
                             runTaggerButton.setEnabled(true);
                             //the parent of this is the jtabbedPane in the tagger tab of HV
                             ((JTabbedPane)(tcp.getParent())).setSelectedIndex(1);
                             fireTaggerEvent(new ActionEvent(tcp,ActionEvent.ACTION_PERFORMED,"taggingdone"));
                             timer.stop();
                         }
                     }
                 });
 
                 timer.start();
             }catch (TaggerException t){
                 JOptionPane.showMessageDialog(this,
                         t.getMessage(),
                         "Tagger",
                         JOptionPane.ERROR_MESSAGE);
             }
 
         }else if (command.equals("Reset Table")){
             for (int j = 0; j < table.getColumnCount(); j++){
                 sorter.setSortingStatus(j,TableSorter.NOT_SORTED);
             }
 
             for (int i = 0; i < table.getRowCount(); i++){
                 table.setValueAt(new Boolean(false), i, EXCLUDE_COL);
                 table.setValueAt(new Boolean(false), i, INCLUDE_COL);
                 table.setValueAt(new Boolean(true), i, CAPTURE_COL);
             }
             rsqField.setText(String.valueOf(Tagger.DEFAULT_RSQ_CUTOFF));
         }else if (command.equals("Load Includes")){
             Hashtable forceIncludes = new Hashtable(1,1);
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION){
                 try{
                     BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));
                     String line;
                     while((line = br.readLine()) != null) {
                         if(line.length() > 0 && line.charAt(0) != '#'){
                             line = line.trim();
                             forceIncludes.put(line,"I");
                         }
                     }
                 }catch(IOException ioe){
                     //throw new IOException("An error occured while reading the force includes file.");
                 }
             }
 
             for (int i = 0; i < table.getRowCount(); i++){
                 if (forceIncludes.containsKey(table.getValueAt(i,NAME_COL))){
                     table.setValueAt(new Boolean(true),i,INCLUDE_COL);
                 }
             }
         }else if (command.equals("Include All")){
             for (int i = 0; i < table.getRowCount(); i++){
                 table.setValueAt(new Boolean(true),i,INCLUDE_COL);
             }
         }else if (command.equals("Load Excludes")){
             Hashtable forceExcludes = new Hashtable(1,1);
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION){
                 try{
                     BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));
                     String line;
                     while((line = br.readLine()) != null) {
                         if(line.length() > 0 && line.charAt(0) != '#'){
                             line = line.trim();
                             forceExcludes.put(line,"E");
                         }
                     }
                 }catch(IOException ioe){
                     //throw new IOException("An error occured while reading the force excludes file.");
                 }
             }
 
             for (int j = 0; j < table.getRowCount(); j++){
                 if (forceExcludes.containsKey(table.getValueAt(j,NAME_COL))){
                     table.setValueAt(new Boolean(true),j,EXCLUDE_COL);
                 }
             }
         }else if (command.equals("Exclude All")){
             for (int i = 0; i < table.getRowCount(); i++){
                 table.setValueAt(new Boolean(true),i,EXCLUDE_COL);
             }
         }else if (command.equals("Uncapture All")){
             for (int i = 0; i < table.getRowCount(); i++){
                 table.setValueAt(new Boolean(false),i,CAPTURE_COL);
             }
         }else if (command.equals("Exclude A/T & C/G SNPs")){
             for (int i = 0; i < table.getRowCount(); i++){
                 if (((SNP)snpsByName.get(table.getValueAt(i,NAME_COL))).getStrandIssue()){
                     table.setValueAt(new Boolean(true),i,EXCLUDE_COL);
                 }
             }
         }else if (command.equals("Design Scores")){
             designScores = new Hashtable(1,1);
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION){
                 try{
                     BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));
                     String line;
                     while((line = br.readLine()) != null) {
                         if(line.length() > 0 && line.charAt(0) != '#'){
                             StringTokenizer st = new StringTokenizer(line);
                             String marker = st.nextToken();
                             Double score = new Double(st.nextToken());
                             designScores.put(marker,score);
                         }
                     }
                 }catch(IOException ioe){
                     JOptionPane.showMessageDialog(this,
                             "Error reading the design scores file",
                             "File Error",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }catch(NumberFormatException nfe){
                     JOptionPane.showMessageDialog(this,
                             "Invalid file formatting",
                             "File Error",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 for (int i = 0; i < table.getRowCount();i++){
                     if (designScores.containsKey(table.getValueAt(i,NAME_COL))){
                         table.setValueAt(designScores.get(table.getValueAt(i,NAME_COL)),i,DESIGN_COL);
                     }
                 }
             }
         }else if (command.equals("Alleles to Capture")){
             Hashtable allelesCaptured = new Hashtable(1,1);
             fc.setSelectedFile(new File(""));
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION){
                 try{
                     BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));
                     String line;
                     while((line = br.readLine()) != null) {
                         if(line.length() > 0 && line.charAt(0) != '#'){
                             line = line.trim();
                             allelesCaptured.put(line,"C");
                         }
                     }
                 }catch(IOException ioe){
                     JOptionPane.showMessageDialog(this,
                             "Error reading the alleles file",
                             "File Error",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
 
                 for (int j = 0; j < table.getRowCount(); j++){
                     if (allelesCaptured.containsKey(table.getValueAt(j,NAME_COL))){
                         table.setValueAt(new Boolean(true),j,CAPTURE_COL);
                     }else{
                         table.setValueAt(new Boolean(false),j,CAPTURE_COL);
                     }
                 }
             }
         }else if (command.equals("Reset Thresholds")){
             rsqField.setText(Double.toString(Tagger.DEFAULT_RSQ_CUTOFF));
             lodField.setText(Double.toString(Tagger.DEFAULT_LOD_CUTOFF));
             minDistField.setText(Short.toString(Tagger.DEFAULT_MIN_DISTANCE));
             maxNumTagsField.setText("");
             minDesignField.setText("");
         }else if (command.equals("Force in PLINK SNPs")){
             Hashtable forceIncludes = new Hashtable(1,1);
             for (int i = 0; i < Chromosome.getSize(); i++){
                 if (Chromosome.getMarker(i).getExtra() != null){
                     forceIncludes.put(Chromosome.getMarker(i).getDisplayName(),"");
                 }
             }
 
             for (int i = 0; i < table.getRowCount(); i++){
                 if (forceIncludes.containsKey(table.getValueAt(i,NAME_COL))){
                     table.setValueAt(new Boolean(true),i,INCLUDE_COL);
                 }
             }
         }
     }
 
     public TaggerController getTaggerController() {
         return tagControl;
     }
 
     public void export(File outfile) throws IOException, HaploViewException{
         if (tagControl != null){
             tagControl.saveResultsToFile(outfile);
         }else{
             throw new HaploViewException("Tagger has not yet generated any results");
         }
     }
 
     class TagConfigTableModel extends AbstractTableModel {
         static final long serialVersionUID = 5952046961517951909L;
         Vector columnNames; Vector data;
 
         public TagConfigTableModel(Vector c, Vector d){
             columnNames=c;
             data=d;
         }
 
         public int getColumnCount(){
             return columnNames.size();
         }
 
         public int getRowCount(){
             return data.size();
         }
 
         public Object getValueAt(int row, int column){
             return ((Vector)data.elementAt(row)).elementAt(column);
         }
 
         public Class getColumnClass(int c){
             return getValueAt(0, c).getClass();
         }
 
         public String getColumnName(int n){
             return (String)columnNames.elementAt(n);
         }
 
         public boolean isCellEditable(int row, int col){
             if (col == CAPTURE_COL || col == INCLUDE_COL || col == EXCLUDE_COL){
                 return true;   
             }
             return false;
         }
 
         public void setValueAt(Object value, int row, int col){
             ((Vector)data.elementAt(row)).set(col, value);
             fireTableCellUpdated(row, col);
         }
     }
 }
