 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath"
  * exception as provided by Sun in the License file that accompanied
  * this code.
  */
 
 package org.jdesktop.wonderland.modules.sample.client;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.JPanel;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import org.jdesktop.wonderland.client.cell.properties.CellPropertiesEditor;
 import org.jdesktop.wonderland.client.cell.properties.spi.CellComponentPropertiesSPI;
 import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;
 import org.jdesktop.wonderland.common.cell.state.CellServerState;
 import org.jdesktop.wonderland.modules.sample.common.SampleCellComponentServerState;
 
 /**
  *
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class SampleComponentProperties extends javax.swing.JPanel implements CellComponentPropertiesSPI {
 
     private CellPropertiesEditor editor = null;
     private String originalInfo = null;
 
     /** Creates new form SampleComponentProperties */
     public SampleComponentProperties() {
         // Initialize the GUI
         initComponents();
 
         // Listen for changes to the info text field
         infoTextField.getDocument().addDocumentListener(new InfoTextFieldListener());
         
     }
 
     /**
      * @inheritDoc()
      */
     public Class getServerCellComponentClass() {
         return SampleCellComponentServerState.class;
     }
 
     /**
      * @inheritDoc()
      */
     public String getDisplayName() {
         return "Sample Component";
     }
 
     /**
      * @inheritDoc()
      */
     public JPanel getPropertiesJPanel(CellPropertiesEditor editor) {
         this.editor = editor;
         return this;
     }
 
     /**
      * @inheritDoc()
      */
     public <T extends CellServerState> void updateGUI(T cellServerState) {
        CellComponentServerState states[] = cellServerState.getCellComponentSetups();
         for (CellComponentServerState state : states) {
             if (state instanceof SampleCellComponentServerState) {
                 originalInfo = ((SampleCellComponentServerState)state).getInfo();
                 infoTextField.setText(originalInfo);
                 return;
             }
         }
     }
 
     /**
      * @inheritDoc()
      */
     public <T extends CellServerState> void getCellServerState(T cellServerState) {
         // Figure out whether there already exists a server state for the
         // component -- this is ugly, need to clean up.
         List<CellComponentServerState> list = new LinkedList(
                Arrays.asList(cellServerState.getCellComponentSetups()));
         SampleCellComponentServerState state = null;
         Iterator<CellComponentServerState> it = list.iterator();
         while (it.hasNext() == true) {
             CellComponentServerState tmp = it.next();
             if (tmp instanceof SampleCellComponentServerState) {
                 state = (SampleCellComponentServerState)tmp;
                 break;
             }
         }
 
         // If it does not exist, create it
         if (state == null) {
             state = new SampleCellComponentServerState();
             list.add(state);
         }
         state.setInfo(infoTextField.getText());
        cellServerState.setCellComponentSetups(
                 list.toArray(new CellComponentServerState[] {}));
     }
 
     /**
      * Inner class to listen for changes to the text field and fire off dirty
      * or clean indications to the cell properties editor.
      */
     class InfoTextFieldListener implements DocumentListener {
         public void insertUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void removeUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         public void changedUpdate(DocumentEvent e) {
             checkDirty();
         }
 
         private void checkDirty() {
             String name = infoTextField.getText();
             if (editor != null && name.equals(originalInfo) == false) {
                 editor.setPanelDirty(SampleComponentProperties.class, true);
             }
             else if (editor != null) {
                 editor.setPanelDirty(SampleComponentProperties.class, false);
             }
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         infoTextField = new javax.swing.JTextField();
 
         jLabel1.setText("Info:");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(infoTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(infoTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(252, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextField infoTextField;
     private javax.swing.JLabel jLabel1;
     // End of variables declaration//GEN-END:variables
 }
