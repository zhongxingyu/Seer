 package org.bitbucket.theimplementer.mipsdisassembler.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.table.TableModel;
 
 public class SearchInstructionActionListener implements ActionListener {
 
     private final JTable table;
 
     public SearchInstructionActionListener(JTable table) {
         this.table = table;
     }
 
     @Override
     public void actionPerformed(ActionEvent actionEvent) {
        final String searchString = JOptionPane.showInputDialog(null);
         final int selectedRow = table.getSelectionModel().getMinSelectionIndex();
         final TableModel tableModel = table.getModel();
         for (int count = selectedRow + 1; count != tableModel.getRowCount(); ++count) {
             final String rowValue = tableModel.getValueAt(count, 2).toString();
             if (rowValue.contains(searchString)) {
                 table.getSelectionModel().setSelectionInterval(0, count);
                 return;
             }
         }
         JOptionPane.showMessageDialog(table, "Cannot find specified value");
     }
 }
