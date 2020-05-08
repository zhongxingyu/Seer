 package edu.mit.wi.haploview;
 
 import edu.mit.wi.plink.PlinkTableModel;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.border.TitledBorder;
 import java.util.Vector;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 public class PlinkResultsPanel extends JPanel implements ActionListener, Constants {
     private JTable table;
     private PlinkTableModel plinkTableModel;
     private TableSorter sorter;
 
     String[] pvalues = {"","<html>10<sup>-2</sup>","<html>10<sup>-3</sup>","<html>10<sup>-4</sup>",
             "<html>10<sup>-5</sup>"};
     String[] chromNames = {"","1","2","3","4","5","6","7","8","9","10",
             "11","12","13","14","15","16","17","18","19","20","21","22","X"};
     String[] signs = {"",">=","<=","="};
     private JComboBox chromChooser, genericChooser, signChooser;
     private NumberTextField chromStart, chromEnd, topField;
     private JTextField valueField;
 
     private int startPos, endPos;
     private int numResults;
     private String chromChoice, columnChoice, signChoice, value;
     private HaploView hv;
 
 
     public PlinkResultsPanel(HaploView h, Vector results, Vector colNames, Vector filters){
         hv = h;
 
         setLayout(new GridBagLayout());
 
         plinkTableModel = new PlinkTableModel(colNames,results);
         sorter = new TableSorter(plinkTableModel);
 
 
         table = new JTable(sorter);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.getTableHeader().setReorderingAllowed(false);
         //table.getModel().addTableModelListener(this);
 
         sorter.setTableHeader(table.getTableHeader());
         table.getColumnModel().getColumn(2).setPreferredWidth(100);
         table.getColumnModel().getColumn(3).setPreferredWidth(100);
         table.getColumnModel().getColumn(8).setPreferredWidth(100);
 
         final PlinkCellRenderer renderer = new PlinkCellRenderer();
         try{
             table.setDefaultRenderer(Class.forName("java.lang.Double"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.Integer"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.Long"), renderer);
             table.setDefaultRenderer(Class.forName("java.lang.String"),renderer);
         }catch (Exception e){
         }
 
         JScrollPane tableScroller = new JScrollPane(table);
         //tableScroller.setMinimumSize(new Dimension(800,600));
         //tableScroller.setMaximumSize(new Dimension(800,(int)tableScroller.getPreferredSize().getHeight()));
 
 
         JPanel chromFilterPanel = new JPanel();
         chromFilterPanel.setMinimumSize(new Dimension(700,40));
         chromFilterPanel.add(new JLabel("Chromosome:"));
         chromChooser = new JComboBox(chromNames);
         chromFilterPanel.add(chromChooser);
         chromFilterPanel.add(new JLabel("Start kb:"));
         chromStart = new NumberTextField("",6,false);
         chromFilterPanel.add(chromStart);
         chromFilterPanel.add(new JLabel("End kb:"));
         chromEnd = new NumberTextField("",6,false);
         chromFilterPanel.add(chromEnd);
         chromFilterPanel.add(new JLabel("Other:"));
         genericChooser = new JComboBox(plinkTableModel.getUnknownColumns());
         genericChooser.setSelectedIndex(-1);
         chromFilterPanel.add(genericChooser);
         signChooser = new JComboBox(signs);
         signChooser.setSelectedIndex(-1);
         chromFilterPanel.add(signChooser);
         valueField = new JTextField(8);
         chromFilterPanel.add(valueField);
         JButton doFilter = new JButton("Filter");
         doFilter.addActionListener(this);
         chromFilterPanel.add(doFilter);
 
         JPanel topResultsFilterPanel = new JPanel();
         JLabel topLabel = new JLabel("View top");
         topResultsFilterPanel.add(topLabel);
         topField = new NumberTextField("100",6,false);
         topResultsFilterPanel.add(topField);
         JLabel resultsLabel = new JLabel("results");
         topResultsFilterPanel.add(resultsLabel);
         JButton doTopFilter = new JButton("Go");
         doTopFilter.setActionCommand("top filter");
         doTopFilter.addActionListener(this);
         topResultsFilterPanel.add(doTopFilter);
         if (!plinkTableModel.pColExists()){
             topLabel.setEnabled(false);
             topField.setEnabled(false);
             resultsLabel.setEnabled(false);
             doTopFilter.setEnabled(false);
         }
 
         JButton resetFilters = new JButton("Reset Filters");
         resetFilters.addActionListener(this);
         JButton moreResults = new JButton("Load Additional Results");
         moreResults.addActionListener(this);
 
         JPanel filterPanel = new JPanel(new GridBagLayout());
         GridBagConstraints a = new GridBagConstraints();
         filterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Filters"));
         ((TitledBorder)(filterPanel.getBorder())).setTitleColor(Color.black);
 
         //a.gridx = 1;
         a.gridwidth = 5;
         a.anchor = GridBagConstraints.CENTER;
         a.weightx = 1;
         filterPanel.add(chromFilterPanel,a);
         a.gridy = 1;
         filterPanel.add(topResultsFilterPanel,a);
         a.gridy = 2;
         //a.gridx = 0;
         a.gridwidth = 1;
         a.anchor = GridBagConstraints.SOUTHWEST;
         filterPanel.add(moreResults,a);
         a.gridx = 2;
         a.anchor = GridBagConstraints.SOUTHEAST;
         filterPanel.add(resetFilters,a);
 
 
         JPanel goPanel = new JPanel();
         JButton goButton = new JButton("<html><b>Go to Selected Region</b>");
         goButton.addActionListener(this);
         goButton.setActionCommand("Go to Selected Region");
         goPanel.add(goButton);
 
         GridBagConstraints c = new GridBagConstraints();
         c.fill = 1;
         c.weightx = 1;
         c.weighty = 1;
         c.insets = new Insets(10,50,10,50);
         add(tableScroller, c);
         c.weighty = 0;
         c.gridy = 1;
         add(filterPanel, c);
         c.gridy = 2;
         c.weighty = 0;
         add(goPanel,c);
 
         if (filters != null){
             if (((String)filters.get(0)).equals("")){
                 chromChooser.setSelectedIndex(0);
             }else{
                 chromChooser.setSelectedIndex(new Integer((String)filters.get(0)).intValue());
             }
             
 
             if (((String)filters.get(1)).equals("") || ((String)filters.get(2)).equals("")){
                 chromStart.setText("");
                 chromEnd.setText("");
             }else if (new Integer((String)filters.get(1)).intValue() > 0 &&
                     new Integer((String)filters.get(2)).intValue() > 0){
                 chromStart.setText((String)filters.get(1));
                 chromEnd.setText((String)filters.get(2));
             }
 
             if (!((String)filters.get(3)).equals("0")){
                 genericChooser.setSelectedIndex(new Integer((String)filters.get(3)).intValue());
                 signChooser.setSelectedIndex(new Integer((String)filters.get(4)).intValue());
                 valueField.setText((String)filters.get(5));
             }
 
 
             doFilters();
         }
 
     }
 
     public void doTopFilter(){
         clearSorting();
         plinkTableModel.filterTop(numResults);
         repaint();
     }
 
     public void doFilters(){
         chromChoice = (String)chromChooser.getSelectedItem();
 
         if (chromStart.getText().equals("")){
             startPos = -1;
         }else{
             startPos = Integer.parseInt(chromStart.getText());
         }
 
         if (chromEnd.getText().equals("")){
             endPos = -1;
         }else{
             endPos = Integer.parseInt(chromEnd.getText());
         }
         if (startPos > endPos){
             JOptionPane.showMessageDialog(this.getParent(),
                     "End position must be greater then start position.",
                     "Invalid value",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         if (genericChooser.getSelectedIndex() > 0){
             columnChoice = (String)genericChooser.getSelectedItem();
 
             if (signChooser.getSelectedIndex() > 0){
                 signChoice = (String)signChooser.getSelectedItem();
 
                 if (!(valueField.getText().equals(""))){
                     value = valueField.getText();
                 }
             }
         }
 
         clearSorting();
         plinkTableModel.filterAll(chromChoice,startPos,endPos,columnChoice,signChoice,value);
         repaint();
     }
 
     public void clearFilters(){
         clearSorting();
         plinkTableModel.resetFilters();
         chromChooser.setSelectedIndex(0);
         chromChoice = null;
         chromStart.setText("");
         startPos = -1;
         chromEnd.setText("");
         endPos = -1;
         topField.setText("100");
         numResults = 0;
         genericChooser.setSelectedIndex(0);
         columnChoice = null;
         signChooser.setSelectedIndex(0);
         signChoice = null;
         valueField.setText("");
         value = null;
         repaint();
     }
 
     public void clearSorting(){
         for (int i = 0; i < table.getColumnCount(); i++){
             sorter.setSortingStatus(i,TableSorter.NOT_SORTED);
         }
     }
 
     public void gotoRegion(){
         if (table.getSelectedRow() == -1){
             JOptionPane.showMessageDialog(this,
                     "Please select a region.",
                     "Invalid value",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
         String gotoChrom = (String)table.getValueAt(table.getSelectedRow(),1);
         String gotoMarker = (String)table.getValueAt(table.getSelectedRow(),2);
         long markerPosition = ((Long)(table.getValueAt(table.getSelectedRow(),3))).longValue();
         Vector filters = new Vector();
 
         if (chromChoice != null && !chromChoice.equals("")){
             if(chromChoice.equals("X")){
                 filters.add("23");
             }else{
                 filters.add(new Integer(chromChooser.getSelectedIndex()).toString());
             }
 
             filters.add(new Integer(startPos).toString());
             filters.add(new Integer(endPos).toString());
 
         }else{
             filters.add("");
             filters.add("0");
             filters.add("0");
         }
 
         filters.add(new Integer(genericChooser.getSelectedIndex()).toString());
         filters.add(new Integer(signChooser.getSelectedIndex()).toString());
         filters.add(valueField.getText());
 
         hv.setPlinkFilters(filters);
         RegionDialog rd = new RegionDialog(hv,gotoChrom,gotoMarker,markerPosition,"Go to Region");
         rd.pack();
         rd.setVisible(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals("Filter")){
             doFilters();
         }else if (command.equals("top filter")){
             numResults = Integer.parseInt(topField.getText());
             doTopFilter();
         }else if (command.equals("Reset Filters")){
             clearFilters();
         }else if (command.equals("Load Additional Results")){
             HaploView.fc.setSelectedFile(new File(""));
             int returned = HaploView.fc.showOpenDialog(this);
             if (returned != JFileChooser.APPROVE_OPTION) return;
             File file = HaploView.fc.getSelectedFile();
             String fullName = file.getParent()+File.separator+file.getName();
            String[] inputs = {null,null,fullName};
             hv.readWGA(inputs);
         }else if (command.equals("Go to Selected Region")){
             gotoRegion();
         }
     }
 
     class PlinkCellRenderer extends DefaultTableCellRenderer {
         public Component getTableCellRendererComponent
                 (JTable table, Object value, boolean isSelected,
                  boolean hasFocus, int row, int column)
         {
             Component cell = super.getTableCellRendererComponent
                     (table, value, isSelected, hasFocus, row, column);
             String thisMarker = (String)table.getValueAt(row,2);
             cell.setForeground(Color.black);
             cell.setBackground(Color.white);
 
             if (isSelected){
                 cell.setBackground(table.getSelectionBackground());
             }else{
                 cell.setBackground(table.getBackground());
             }
 
             if (thisMarker.equals(hv.getChosenMarker())){
                 cell.setBackground(Color.cyan);
             }
 
 
             return cell;
         }
     }
 
 }
