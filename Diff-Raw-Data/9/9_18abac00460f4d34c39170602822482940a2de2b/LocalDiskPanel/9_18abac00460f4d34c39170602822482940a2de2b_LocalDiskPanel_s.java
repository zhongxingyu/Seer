 /*
  * Autopsy Forensic Browser
  *
  * Copyright 2012 Basis Technology Corp.
  * Contact: carrier <at> sleuthkit <dot> org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.sleuthkit.autopsy.casemodule;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.ComboBoxModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.ListCellRenderer;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListDataListener;
 import org.sleuthkit.autopsy.coreutils.PlatformUtil;
 
 /**
  * ImageTypePanel for adding a local disk or partition such as \\.\PhysicalDrive0 or  \\.\C:.
  */
 public class LocalDiskPanel extends ImageTypePanel {
     private static LocalDiskPanel instance;
     private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
     private List<LocalDisk> disks;
     private LocalDiskModel model;
     private int separatorIndex;
     private boolean diskSelected = true;
 
     /**
      * Creates new form LocalDiskPanel
      */
     public LocalDiskPanel() {
         initComponents();
         updateDisks();
     }
     
     /**
      * Get the default instance of this panel.
      */
     public static LocalDiskPanel getDefault() {
         if (instance == null) {
             instance = new LocalDiskPanel();
         }
         instance.updateDisks();
         return instance;
     }
     
     /**
      * Update the JComboBox with the list of disks.
      */
     private void updateDisks() {
         errorLabel.setText("");
         diskComboBox.setEnabled(true);
         disks = new ArrayList<LocalDisk>();
         List<LocalDisk> physical = PlatformUtil.getPhysicalDrives();
         List<LocalDisk> partitions = PlatformUtil.getPartitions();
         separatorIndex = physical.size() - 1;
         if(physical.isEmpty()) {
             errorLabel.setText("Some disks were not detected. On some systems it requires admin privileges (or \"Run as administrator\").");
         }
         
         disks.addAll(physical);
         disks.addAll(partitions);
         model = new LocalDiskModel();
         diskComboBox.setModel(model);
         diskComboBox.setRenderer(model);
         
         if(physical.isEmpty() && partitions.isEmpty()) {
             if(PlatformUtil.isWindowsOS()) {
                errorLabel.setText("Local drives were not detected.");
             } else {
                errorLabel.setText("Local drives were not detected. Auto-detection not supported on this OS.");
             }
             diskComboBox.setEnabled(false);
             diskSelected = false;
         } else {
             diskComboBox.setSelectedIndex(0);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         diskLabel = new javax.swing.JLabel();
         diskComboBox = new javax.swing.JComboBox();
         errorLabel = new javax.swing.JLabel();
 
         setMinimumSize(new java.awt.Dimension(0, 65));
         setPreferredSize(new java.awt.Dimension(485, 65));
 
         org.openide.awt.Mnemonics.setLocalizedText(diskLabel, org.openide.util.NbBundle.getMessage(LocalDiskPanel.class, "LocalDiskPanel.diskLabel.text")); // NOI18N
 
         errorLabel.setForeground(new java.awt.Color(255, 0, 0));
         org.openide.awt.Mnemonics.setLocalizedText(errorLabel, org.openide.util.NbBundle.getMessage(LocalDiskPanel.class, "LocalDiskPanel.errorLabel.text")); // NOI18N
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(diskLabel)
                     .addComponent(diskComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(errorLabel))
                 .addGap(0, 140, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(diskLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(diskComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(errorLabel))
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox diskComboBox;
     private javax.swing.JLabel diskLabel;
     private javax.swing.JLabel errorLabel;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Return the currently selected disk path.
      * @return String selected disk path
      */
     @Override
     public String getImagePath() {
         LocalDisk selected = (LocalDisk) diskComboBox.getSelectedItem();
         return selected.getPath();
     }
 
     /**
      * Set the selected disk.
      */
     @Override
     public void setImagePath(String s) {
         for(int i=0; i<disks.size(); i++) {
             if(disks.get(i).getPath().equals(s)) {
                 diskComboBox.setSelectedIndex(i);
             }
         }
     }
 
     /**
      * Should we enable the wizard's next button?
      * Always return true because we control the possible selections.
      * @return true
      */
     @Override
     public boolean enableNext() {
         return diskSelected;
     }
     
     /**
      * @return the representation of this panel as a String.
      */
     @Override
     public String toString() {
         return "Local Disk";
     }
     
    /**
      * Set the focus of this panel to the desired component.
      */
     @Override
     public void setFocus() {
         diskComboBox.requestFocusInWindow();
         updateDisks(); // called every time the panel is switched to
                        // so why not update the list while we're here?
     }
     
     @Override
     public void addPropertyChangeListener(PropertyChangeListener pcl) {
         pcs = new PropertyChangeSupport(this);
         pcs.addPropertyChangeListener(pcl);
     }
 
     @Override
     public void removePropertyChangeListener(PropertyChangeListener pcl) {
         pcs.removePropertyChangeListener(pcl);
     }
     
     private class LocalDiskModel implements ComboBoxModel, ListCellRenderer {
         Object selected;
 
         @Override
         public void setSelectedItem(Object anItem) {
             selected = anItem;
             if(selected != null) {
                 diskSelected = true;
             } else {
                 diskSelected = false;
             }
             pcs.firePropertyChange(AddImageVisualPanel1.EVENT.UPDATE_UI.toString(), false, true);
         }
 
         @Override
         public Object getSelectedItem() {
             return selected;
         }
 
         @Override
         public int getSize() {
             return disks.size();
         }
 
         @Override
         public Object getElementAt(int index) {
             return disks.get(index);
         }
 
         @Override
         public void addListDataListener(ListDataListener l) {
         }
 
         @Override
         public void removeListDataListener(ListDataListener l) {
         }
 
         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
             JPanel panel = new JPanel(new BorderLayout());
             JLabel label = new JLabel();
             if(index == separatorIndex) {
                 panel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);
             }
             if (isSelected) {
                 label.setBackground(list.getSelectionBackground());
                 label.setForeground(list.getSelectionForeground());
                 panel.setBackground(list.getSelectionBackground());
                 panel.setForeground(list.getSelectionForeground());
             } else {
                 label.setBackground(list.getBackground());
                 label.setForeground(list.getForeground());
                 panel.setBackground(list.getBackground());
                 panel.setForeground(list.getForeground());
             }
             label.setText(value != null ? value.toString() : "");
             label.setOpaque(true);
             label.setBorder(new EmptyBorder(2, 2, 2, 2));
             
             panel.add(label, BorderLayout.CENTER);
             return panel;
         }
     }
 }
