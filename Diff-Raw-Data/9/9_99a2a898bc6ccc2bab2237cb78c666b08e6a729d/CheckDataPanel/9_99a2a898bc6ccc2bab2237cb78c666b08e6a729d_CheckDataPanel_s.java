 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.Vector;
 
 import edu.mit.wi.pedfile.MarkerResult;
 import edu.mit.wi.pedfile.PedFile;
 import edu.mit.wi.pedfile.CheckData;
 
 public class CheckDataPanel extends JPanel
         implements TableModelListener, ActionListener {
     private JTable table;
     private CheckDataTableModel tableModel;
     private HaploData theData;
 
     boolean changed;
     static int STATUS_COL = 8;
     private HaploView hv;
 
     public CheckDataPanel(HaploView hv){
         this(hv.theData);
         this.hv = hv;
 
         setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
 
         JPanel missingPanel = new JPanel();
         JLabel countsLabel;
         countsLabel = new JLabel("Using " + theData.numSingletons + " singletons and "
                 + theData.numTrios + " trios from "
                 + theData.numPeds + " families.");
         if (theData.numTrios + theData.numSingletons == 0){
             countsLabel.setForeground(Color.red);
         }
         countsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         missingPanel.add(countsLabel);
         JButton missingButton = new JButton("Show Excluded Individuals");
         JButton individualButton = new JButton("Individual Summary");
         individualButton.setEnabled(true);
         JButton mendelButton = new JButton("Mendel Errors");
         mendelButton.setEnabled(true);
         if (hv.theData.getPedFile().getAxedPeople().size() == 0){
             missingButton.setEnabled(false);
         }
 
         missingButton.addActionListener(this);
         missingPanel.add(missingButton);
         individualButton.addActionListener(this);
         missingPanel.add(individualButton);
         mendelButton.addActionListener(this);
         missingPanel.add(mendelButton);
         missingPanel.setBorder(BorderFactory.createLineBorder(Color.black));
         JPanel extraPanel = new JPanel();
         extraPanel.add(missingPanel);
 
        CheckDataTableSorter sorter = new CheckDataTableSorter(tableModel);
         table = new JTable(sorter);
         sorter.setTableHeader(table.getTableHeader());
 
         final CheckDataCellRenderer renderer = new CheckDataCellRenderer();
         try{
             table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.String"),renderer);
         }catch (Exception e){
         }
 
         table.getColumnModel().getColumn(0).setPreferredWidth(30);
         table.getColumnModel().getColumn(0).setMinWidth(30);
         if (theData.infoKnown){
             table.getColumnModel().getColumn(1).setMinWidth(100);
             table.getColumnModel().getColumn(2).setMinWidth(60);
         }
         JScrollPane tableScroller = new JScrollPane(table);
         //changed 600 to 800 and tableScroller.getPreferredSize().height to Integer.MAX_VALUE.
         tableScroller.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
 
         add(extraPanel);
         add(tableScroller);
 
         if (theData.dupsToBeFlagged){
             JOptionPane.showMessageDialog(hv,
                     "Two or more SNPs have identical position. They have been flagged in yellow\n"+
                     "and the less completely genotyped duplicate has been deselected.",
                     "Duplicate SNPs",
                     JOptionPane.INFORMATION_MESSAGE);
         }
 
         if (theData.dupNames){
             JOptionPane.showMessageDialog(hv,
                     "Two or more SNPs have identical names. They have been renamed with\n"+
                     ".X extensions where X is an integer unique to each duplicate.",
                     "Duplicate SNPs",
                     JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     public CheckDataPanel(HaploData hd){
         STATUS_COL = 9;
 
         theData = hd;
         PedFile pf = theData.getPedFile();
 
         Vector tableColumnNames = pf.getColumnNames();
         if (theData.infoKnown){
             STATUS_COL += 2;
         }
 
         tableModel = new CheckDataTableModel(tableColumnNames, pf.getTableData(), pf.getMarkerRatings(), pf.getDups());
         tableModel.addTableModelListener(this);
     }
 
     public JTable getTable(){
         return table;
     }
 
     public void tableChanged(TableModelEvent e) {
         if (e.getColumn() == STATUS_COL){
             changed = true;
         }
     }
 
     public void selectAll(){
         for (int i = 0; i < table.getRowCount(); i++){
             table.setValueAt(new Boolean(true), i, STATUS_COL);
         }
         changed = true;
     }
 
     public void deSelectAll(){
         for (int i = 0; i < table.getRowCount(); i++){
             table.setValueAt(new Boolean(false), i, STATUS_COL);
         }
         changed = true;
     }
 
     public void redoRatings(){
         try{
             Vector result = new CheckData(theData.getPedFile()).check();
 
             for (int i = 0; i < table.getRowCount(); i++){
                 MarkerResult cur = (MarkerResult)result.get(i);
 
                 //use this marker as long as it has a good (i.e. positive) rating and is not an "unused" dup (==2)
                 int curRating = cur.getRating();
                 int dupStatus = Chromosome.getUnfilteredMarker(i).getDupStatus();
                 if ((curRating > 0 && dupStatus != 2) ||
                     theData.getPedFile().isWhiteListed(Chromosome.getUnfilteredMarker(i))){
                     table.setValueAt(new Boolean(true),i,STATUS_COL);
                 }else{
                     table.setValueAt(new Boolean(false),i,STATUS_COL);
                 }
                 tableModel.setRating(i,curRating);
             }
             changed = true;
         }catch (Exception e){
             e.printStackTrace();
         }
     }
 
     public boolean[] getMarkerResults(){
         boolean[] markerResults = new boolean[table.getRowCount()];
         for (int i = 0; i < table.getRowCount(); i++){
             markerResults[i] = ((Boolean)table.getValueAt(i,CheckDataPanel.STATUS_COL)).booleanValue();
         }
 
         return markerResults;
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals("Show Excluded Individuals")) {
             //show details of individuals removed due to excessive missing data
             FilteredIndividualsDialog fid = new FilteredIndividualsDialog(hv,"Filtered Individuals");
             fid.pack();
             fid.setVisible(true);
         }
         if (command.equals("Individual Summary")) {
             IndividualDialog fd = new IndividualDialog(hv,"Individual Summary");
             fd.pack();
             fd.setVisible(true);
         }
         if (command.equals("Mendel Errors")) {
             MendelDialog md = new MendelDialog(hv,"Mendel Errors");
             md.pack();
             md.setVisible(true);
         }
     }
 
     class CheckDataTableModel extends AbstractTableModel {
         Vector columnNames; Vector data; int[] ratings; int[] dups;
 
         public CheckDataTableModel(Vector c, Vector d, int[] r, int[] dups){
             columnNames=c;
             data=d;
             ratings = r;
             this.dups = dups;
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
 
         public int getRating(int row){
             return ratings[row];
         }
 
         public int getDupStatus(int row){
             return dups[row];
         }
 
         public void setRating(int row, int value) {
             if(row < ratings.length) {
                 ratings[row] = value;
             }
         }
 
         public String getColumnName(int n){
             return (String)columnNames.elementAt(n);
         }
 
         public boolean isCellEditable(int row, int col){
             if (getColumnName(col).equals("Rating")){
                 return true;
             }else{
                 return false;
             }
         }
 
         public void setValueAt(Object value, int row, int col){
             ((Vector)data.elementAt(row)).set(col, value);
             fireTableCellUpdated(row, col);
         }
     }
 
     class CheckDataCellRenderer extends DefaultTableCellRenderer {
         public Component getTableCellRendererComponent
                 (JTable table, Object value, boolean isSelected,
                  boolean hasFocus, int row, int column)
         {
             Component cell = super.getTableCellRendererComponent
                     (table, value, isSelected, hasFocus, row, column);
             int myRating = ((CheckDataTableSorter)table.getModel()).getRating(row);
             int myDupStatus = ((CheckDataTableSorter)table.getModel()).getDupStatus(row);
             String thisColumnName = table.getColumnName(column);
             cell.setForeground(Color.black);
             cell.setBackground(Color.white);
 
             if (myDupStatus > 0){
                 //I'm a dup so color the background in bright, ugly yellow
                 cell.setBackground(Color.yellow);
             }
 
             //bitmasking to decode the status bits
             if (myRating < 0){
                 myRating *= -1;
                 if ((myRating & 1) != 0){
                     if(thisColumnName.equals("ObsHET")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 2) != 0){
                     if (thisColumnName.equals("%Geno")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 4) != 0){
                     if (thisColumnName.equals("HWpval")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 8) != 0){
                     if (thisColumnName.equals("MendErr")){
                         cell.setForeground(Color.red);
                     }
                 }
                 if ((myRating & 16) != 0){
                     if (thisColumnName.equals("MAF")){
                         cell.setForeground(Color.red);
                     }
                 }
             }
             return cell;
         }
     }
 }
