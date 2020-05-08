 /*
  * Copyright 2000-2003 Oracle, Inc. This software was developed in conjunction with the National Cancer Institute, and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
  *
  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
  *
  * "This product includes software developed by Oracle, Inc. and the National Cancer Institute."
  *
  * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself, wherever such third-party acknowledgments normally appear.
  *
  * 3. The names "The National Cancer Institute", "NCI" and "Oracle" must not be used to endorse or promote products derived from this software.
  *
  * 4. This license does not authorize the incorporation of this software into any proprietary programs. This license does not authorize the recipient to use any trademarks owned by either NCI or Oracle, Inc.
  *
  * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, ORACLE, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
  */
 package gov.nih.nci.ncicb.cadsr.loader.ui;
 
 import gov.nih.nci.ncicb.cadsr.domain.Concept;
 import gov.nih.nci.ncicb.cadsr.loader.ext.EvsModule;
 import gov.nih.nci.ncicb.cadsr.loader.ext.EvsResult;
 import gov.nih.nci.ncicb.cadsr.loader.util.UserPreferences;
 import gov.nih.nci.ncicb.cadsr.loader.util.StringUtil;
 import gov.nih.nci.ncicb.cadsr.loader.ui.util.UIUtil;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.table.*;
 import javax.swing.border.*;
 
 
 /**
  * The EVS Search dialog
  *
  * @author <a href="mailto:chris.ludet@oracle.com">Christophe Ludet</a>
  */
 public class EvsDialog extends JDialog implements ActionListener, KeyListener
 {
   private EvsDialog _this = this;
 
   private JLabel searchLabel = new JLabel("Search:");
   private JTextField searchField = new JTextField(10);
   private JLabel whereToSearchLabel = new JLabel("Search By");
   private JLabel numberOfResultsLabel = new JLabel("Results Per Page");
   private JComboBox searchSourceCombo;
   private JComboBox numberOfResultsCombo;
   
   private JCheckBox includeRetiredCB = new JCheckBox("Include Retired?");
 
   private JButton searchButton = new JButton("Search");
 
   private AbstractTableModel tableModel = null;
   private JTable resultTable = null;
 
   static final String SYNONYMS = "Synonyms";
   static final String CONCEPT_CODE = "Concept Code";
 
   private JButton previousButton = new JButton("Previous"),
     nextButton = new JButton("Next"), 
     closeButton = new JButton("Close");
   
   private JLabel indexLabel = new JLabel("");
 
   private static String SEARCH = "SEARCH",
     PREVIOUS = "PREVIOUS",
     NEXT = "NEXT",
     CLOSE = "CLOSE";
 
   private java.util.List<EvsResult> resultSet = new ArrayList();
 
   private String[] columnNames = {
     "Code", "Concept Name", "Preferred Name", "Synonyms", "Definition", "Source"
   };
 
   private int colWidth[] = {30, 30, 30, 30, 300, 15};
 
   private UserPreferences prefs = UserPreferences.getInstance();
 
   private int pageSize = prefs.getEvsResultsPerPage();
 
   private int pageIndex = 0;
 
   private Concept choiceConcept = null;
 
   public EvsDialog()
   {
     super((Frame)null, true);
     this.setTitle("Search Thesaurus");
 
     this.getContentPane().setLayout(new BorderLayout());
 
     String values[] = {SYNONYMS, CONCEPT_CODE};
     searchSourceCombo = new JComboBox(values);
     JPanel searchPanel = new JPanel(new GridBagLayout());
 
     tableModel = new AbstractTableModel() {
         public String getColumnName(int col) {
           return columnNames[col].toString();
         }
         public int getRowCount() { 
           return (int)Math.min(resultSet.size(), pageSize); 
         }
         public int getColumnCount() { return columnNames.length; }
         public Object getValueAt(int row, int col) {
           row = row + pageSize * pageIndex;
 
           if(row >= resultSet.size())
             return "";
 
           EvsResult res = resultSet.get(row);
 
           String s = "";
           switch (col) {
           case 0:
             s = res.getConcept().getPreferredName();
             break;
           case 1:
             s = res.getConceptName();
             break;
           case 2:
             s = res.getConcept().getLongName();
             break;
           case 3: 
             s += "<html><body>";
             for(int i = 0; i<res.getSynonyms().length; i++)
               s += res.getSynonyms()[i] + "<br>";
             s += "</body></html>"; 
             break;
           case 4:
             s += "<html><body>";
             s += res.getConcept().getPreferredDefinition();
             s += "</body></html>";
             break;
           case 5:
             s = res.getConcept().getDefinitionSource();
             break;
           default:
             break;
           }
            
           return s;
         }
         public boolean isCellEditable(int row, int col)
         { return false; }
 
       };
 
     resultTable = new JTable(tableModel) {
         public String getToolTipText(java.awt.event.MouseEvent e) {
           String tip = null;
           java.awt.Point p = e.getPoint();
           int rowIndex = rowAtPoint(p);
           int colIndex = columnAtPoint(p);
           
           return (String)getModel().getValueAt(rowIndex, colIndex);
         }
       };
 
     resultTable.setRowHeight(60);
     
     DefaultTableCellRenderer  tcrColumn  =  new DefaultTableCellRenderer();
     tcrColumn.setVerticalAlignment(JTextField.TOP);
     resultTable.getColumnModel().getColumn(3).setCellRenderer(tcrColumn);
     resultTable.getColumnModel().getColumn(4).setCellRenderer(tcrColumn);
 
     int c = 0;
     for(int width : colWidth) {
       TableColumn col = resultTable.getColumnModel().getColumn(c++);
       col.setPreferredWidth(width);
     }
 
     resultTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent evt) {
           if(evt.getClickCount() == 2) {
             int row = resultTable.getSelectedRow();
             if(row > -1) {
               choiceConcept = resultSet.get(pageIndex * pageSize + row).getConcept();
               _this.dispose();
             }
           }
         }
       });
 
     JScrollPane scrollPane = new JScrollPane(resultTable);
 
     Integer[] number = {new Integer(5),
                         new Integer(10),
                         new Integer(25),
                         new Integer(50),
                         new Integer(100)};
     
     numberOfResultsCombo = new JComboBox(number);
     numberOfResultsCombo.setSelectedItem(prefs.getEvsResultsPerPage());
     
     UIUtil.insertInBag(searchPanel,searchLabel,0, 0);
     UIUtil.insertInBag(searchPanel,searchField, 1, 0);
     UIUtil.insertInBag(searchPanel,whereToSearchLabel, 2,0);
     UIUtil.insertInBag(searchPanel,searchSourceCombo, 3, 0);
     UIUtil.insertInBag(searchPanel,includeRetiredCB, 4, 0);
     UIUtil.insertInBag(searchPanel,searchButton, 5, 0);
 
     searchField.addKeyListener(this);
 
     searchButton.addActionListener(this);
     searchButton.addKeyListener(this);
     searchButton.setActionCommand(SEARCH);
 
     JPanel browsePanel = new JPanel();
     browsePanel.add(previousButton);
     browsePanel.add(indexLabel);
     browsePanel.add(nextButton);
     browsePanel.add(closeButton);
     browsePanel.add(numberOfResultsLabel);
     browsePanel.add(numberOfResultsCombo);
     
     numberOfResultsCombo.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent event) {
         JComboBox cb = (JComboBox)event.getSource();
         Integer selection = (Integer)cb.getSelectedItem();
         pageSize = selection.intValue();
         updateTable();
         updateIndexLabel();
         prefs.setEvsResultsPerPage(selection.intValue());
       }
     });
 
     previousButton.setActionCommand(PREVIOUS);
     nextButton.setActionCommand(NEXT);
     closeButton.setActionCommand(CLOSE);
     previousButton.setEnabled(false);
     nextButton.setEnabled(false);
     previousButton.addActionListener(this);
     nextButton.addActionListener(this);
     closeButton.addActionListener(this);
 
     this.getContentPane().add(searchPanel, BorderLayout.NORTH);
     this.getContentPane().add(scrollPane, BorderLayout.CENTER);
     this.getContentPane().add(browsePanel, BorderLayout.SOUTH);
 
     this.setSize(600,425);
   }
 
 
   public void startSearch(String searchString, String searchBy) {
     searchField.setText(searchString);
     searchSourceCombo.setSelectedItem(searchBy);
     searchButton.doClick();
   }
 
   public Concept getConcept() {
     try {
       return choiceConcept;
     } finally {
       choiceConcept = null;
     } 
   }
   
   public void actionPerformed(ActionEvent event) 
   {
     JButton button = (JButton)event.getSource();
     if(button.getActionCommand().equals(SEARCH)) {
       String selection = (String) searchSourceCombo.getSelectedItem();
       String text = searchField.getText();
 
       EvsModule module = new EvsModule();
 
       resultSet = new ArrayList();
 
       _this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
       if(!StringUtil.isEmpty(text)) {
         if(selection.equals(CONCEPT_CODE)) {
          resultSet.add(module.findByConceptCode(text, includeRetiredCB.isSelected()));
         }
         if(selection.equals(SYNONYMS)) {
           resultSet.addAll(module.findBySynonym(text, includeRetiredCB.isSelected()));
         }
       }
 
       _this.setCursor(Cursor.getDefaultCursor());
       
       pageIndex = 0;
       updateTable();
 
     } else if(button.getActionCommand().equals(PREVIOUS)) {
       pageIndex--;
       updateTable();
     } else if(button.getActionCommand().equals(NEXT)) {
       pageIndex++;
       updateTable();
     } else if(button.getActionCommand().equals(CLOSE)) {
       this.dispose();
     }
     updateIndexLabel();
   }
 
   private void updateIndexLabel() {
     if(resultSet.size() == 0) {
       indexLabel.setText("");
     } else {
       StringBuilder sb = new StringBuilder();
       int start = pageSize * pageIndex;
       int end = (int)Math.min(resultSet.size(), start + pageSize); 
       sb.append(start);
       sb.append("-");
       sb.append(end);
       sb.append(" of " + resultSet.size());
       indexLabel.setText(sb.toString());
     }
     
   }
 
   private void updateTable() {
     tableModel.fireTableDataChanged();
 
     previousButton.setEnabled(pageIndex > 0);
 
     nextButton.setEnabled(resultSet.size() > (pageIndex * pageSize + pageSize));
 
   }
 
   public void keyPressed(KeyEvent evt) {
     if(evt.getKeyCode() == KeyEvent.VK_ENTER)
       searchButton.doClick();
   }
 
   public void keyTyped(KeyEvent evt) {
     if(evt.getKeyCode() == KeyEvent.VK_ENTER)
       searchButton.doClick();
   }
 
   public void keyReleased(KeyEvent evt) {
   }
 
   
   public static void main(String[] args) {
     EvsDialog dialog = new EvsDialog();
     dialog.setVisible(true);
   }
 
 
 }
 
