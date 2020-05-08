 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.util.ArrayList;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.ControllingExportAllAction;
 import de.aidger.controller.actions.ControllingExportDifferencesAction;
 import de.aidger.controller.actions.ControllingGenerateAction;
 import de.aidger.model.Runtime;
 import de.aidger.model.controlling.ControllingAssistant;
 import de.aidger.model.controlling.ControllingCreator;
 import de.aidger.utils.controlling.ControllingHelper;
 import de.aidger.view.UI;
 
 /**
  * This class is used to display the controlling report.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class ControllingViewerTab extends ReportTab {
 
     /**
      * The year, month and funds to be used with the controlling report.
      */
     private int year, month, funds;
 
     /**
      * The table model of the content table.
      */
     private final DefaultTableModel controllingTableModel = new DefaultTableModel(
         null, new String[] { _("Assistant"), _("Planned costs (pre-tax)"),
                 _("Actual costs (pre-tax)"), _("Remark") }) {
 
         boolean[] editable = { false, false, true, true };
 
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
             return editable[columnIndex];
         }
     };
 
     /**
      * Initializes a new controlling viewer tab.
      */
     public ControllingViewerTab() {
         initComponents();
         // Fill up the combo boxes for the first time.
         createYearItems();
         /*
          * This table listener checks the entered value in the 3rd column with
          * the value in the 2nd column. If it's acceptable, it leaves the
          * comment column clear. If the comparison reveals, that the values
          * differ by more than the given tolerance, it writes it to the comment
          * column.
          */
         controllingTableModel.addTableModelListener(new TableModelListener() {
 
             @Override
             public void tableChanged(TableModelEvent e) {
                 if (e.getType() == TableModelEvent.UPDATE) {
                     int row = e.getFirstRow();
                     int column = e.getColumn();
                     double epsilon;
                     try {
                         epsilon = Double.parseDouble(Runtime.getInstance()
                             .getOption("tolerance", "0.0"));
                     } catch (NumberFormatException ex) {
                         epsilon = 0.0;
                     }
                     DefaultTableModel model = (DefaultTableModel) e.getSource();
                     if (column == 2) {
                         try {
                             if (Double.parseDouble(model
                                 .getValueAt(row, column).toString())
                                     - epsilon > Double.parseDouble(model
                                 .getValueAt(row, column - 1).toString())
                                     || Double.parseDouble(model.getValueAt(row,
                                         column).toString())
                                             + epsilon < Double
                                         .parseDouble(model.getValueAt(row,
                                             column - 1).toString())) {
                                 model.setValueAt(_("Costs don't match!"), row,
                                     column + 1);
                             } else {
                                 model.setValueAt("", row, column + 1);
                             }
                         } catch (NumberFormatException ex) {
                            model.setValueAt("<< " + _("Wrong format!"), row,
                                column + 1);
                         }
                     }
                 }
             }
 
         });
         /*
          * Assign the actions to the buttons.
          */
         try {
             generateButton.setAction(ActionRegistry.getInstance().get(
                 ControllingGenerateAction.class.getName()));
             exportAllButton.setAction(ActionRegistry.getInstance().get(
                 ControllingExportAllAction.class.getName()));
             exportDifferencesButton.setAction(ActionRegistry.getInstance().get(
                 ControllingExportDifferencesAction.class.getName()));
         } catch (ActionNotFoundException e) {
             UI.displayError(e.getMessage());
         }
         /*
          * The export buttons should not be visible before a report has been
          * generated.
          */
         exportAllButton.setVisible(false);
         exportDifferencesButton.setVisible(false);
         jSeparator2.setVisible(false);
         jSeparator4.setVisible(false);
     }
 
     /**
      * Sets the export buttons and their separators visible.
      */
     public void visualizeButtons() {
         exportAllButton.setVisible(false);
         exportAllButton.setVisible(true);
         exportDifferencesButton.setVisible(false);
         exportDifferencesButton.setVisible(true);
         jSeparator2.setVisible(false);
         jSeparator2.setVisible(true);
         jSeparator4.setVisible(false);
         jSeparator4.setVisible(true);
     }
 
     /**
      * Returns all of the rows of the table that meet the filter criteria.
      * 
      * @param filter
      *            The filter to use
      * @return The rows of the table.
      */
     public ArrayList<String[]> getRows(String filter) {
         ArrayList<String[]> returnVector = new ArrayList<String[]>();
         if (filter == null) {
             for (int i = 0; i < controllingTableModel.getRowCount(); i++) {
                 String[] returnString = new String[controllingTableModel
                     .getColumnCount()];
                 for (int j = 0; j < controllingTableModel.getColumnCount(); j++) {
                     returnString[j] = controllingTableModel.getValueAt(i, j)
                         .toString();
                 }
                 returnVector.add(returnString);
             }
         } else {
             for (int i = 0; i < controllingTableModel.getRowCount(); i++) {
                 String[] returnString = new String[controllingTableModel
                     .getColumnCount()];
                 if (filter.equals(controllingTableModel.getValueAt(i,
                     controllingTableModel.getColumnCount() - 1).toString())) {
                     for (int j = 0; j < controllingTableModel.getColumnCount(); j++) {
                         returnString[j] = controllingTableModel
                             .getValueAt(i, j).toString();
                     }
                     returnVector.add(returnString);
                 }
             }
         }
         return returnVector;
     }
 
     /**
      * Creates the table containing the assistants that match the criteria.
      */
     public void createTable() {
         while (controllingTableModel.getRowCount() > 0) {
             controllingTableModel.removeRow(0);
         }
         for (ControllingAssistant assistant : new ControllingCreator(year,
             month, funds).getAssistants()) {
             addRow(assistant.getObjectArray());
         }
     }
 
     /**
      * Adds a row containing the given assistant to the table.
      * 
      * @param assistant
      *            The assistant to add.
      */
     private void addRow(Object[] assistant) {
         Object[] rowArray = new Object[controllingTableModel.getColumnCount()];
         for (int i = 0; i < rowArray.length; i++) {
             if (i < assistant.length) {
                 rowArray[i] = assistant[i];
             } else {
                 rowArray[i] = "";
             }
         }
         controllingTableModel.addRow(rowArray);
     }
 
     /**
      * Creates the year items for the year combo box.
      */
     private void createYearItems() {
         int[] years = new ControllingHelper().getEmploymentYears();
         Object[] yearObjects = new Object[years.length];
         for (int i = 0; i < yearObjects.length; i++) {
             yearObjects[i] = years[i];
         }
         yearComboBox.setModel(new DefaultComboBoxModel(yearObjects));
         if (yearComboBox.getItemCount() > 0) {
             year = (Integer) yearComboBox.getSelectedItem();
             createMonthItems(year);
         }
     }
 
     /**
      * Creates the month items for the month combo box.
      * 
      * @param year
      *            The year of which to get the months.
      */
     private void createMonthItems(int year) {
         if (yearComboBox.getSelectedIndex() >= 0) {
             monthComboBox.removeAllItems();
             int[] months = new ControllingHelper().getYearMonths(year);
             Object[] monthObjects = new Object[months.length];
             for (int i = 0; i < monthObjects.length; i++) {
                 monthObjects[i] = months[i];
             }
             monthComboBox.setModel(new DefaultComboBoxModel(monthObjects));
             if (monthComboBox.getItemCount() > 0) {
                 month = (Integer) monthComboBox.getSelectedItem();
                 createFundsItems(year, month);
             }
         }
     }
 
     /**
      * Creates the funds items for the funds combo box.
      * 
      * @param year
      *            The year of which to get the funds.
      * @param month
      *            The month of which to get the funds.
      */
     private void createFundsItems(int year, int month) {
         if (monthComboBox.getSelectedIndex() >= 0) {
             fundsComboBox.removeAllItems();
             int[] funds = new ControllingHelper().getFunds(year, month);
             Object[] fundsObjects = new Object[funds.length];
             for (int i = 0; i < fundsObjects.length; i++) {
                 fundsObjects[i] = funds[i];
             }
             fundsComboBox.setModel(new DefaultComboBoxModel(fundsObjects));
             if (fundsComboBox.getItemCount() > 0) {
                 this.funds = (Integer) fundsComboBox.getSelectedItem();
             }
         }
 
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jToolBar1 = new javax.swing.JToolBar();
         jSeparator3 = new javax.swing.JToolBar.Separator();
         generateButton = new javax.swing.JButton();
         jSeparator1 = new javax.swing.JToolBar.Separator();
         exportAllButton = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         exportDifferencesButton = new javax.swing.JButton();
         jSeparator4 = new javax.swing.JToolBar.Separator();
         contentPanel = new javax.swing.JPanel();
         filtersPanel = new javax.swing.JPanel();
         yearLabel = new javax.swing.JLabel();
         yearComboBox = new javax.swing.JComboBox();
         monthLabel = new javax.swing.JLabel();
         monthComboBox = new javax.swing.JComboBox();
         fundsLabel = new javax.swing.JLabel();
         fundsComboBox = new javax.swing.JComboBox();
         tablePanel = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
 
         setLayout(new java.awt.BorderLayout());
 
         jToolBar1.setFloatable(false);
         jToolBar1.setRollover(true);
         jToolBar1.add(jSeparator3);
 
         generateButton.setText(_("Generate"));
         generateButton.setFocusable(false);
         generateButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         generateButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(generateButton);
         jToolBar1.add(jSeparator1);
 
         exportAllButton.setText(_("Export"));
         exportAllButton.setFocusable(false);
         exportAllButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         exportAllButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         exportAllButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 exportAllButtonActionPerformed(evt);
             }
         });
         jToolBar1.add(exportAllButton);
         jToolBar1.add(jSeparator2);
 
         exportDifferencesButton.setText(_("Export differences"));
         exportDifferencesButton.setFocusable(false);
         exportDifferencesButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         exportDifferencesButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(exportDifferencesButton);
         jToolBar1.add(jSeparator4);
 
         add(jToolBar1, java.awt.BorderLayout.PAGE_START);
 
         contentPanel.setLayout(new java.awt.BorderLayout());
 
         filtersPanel
             .setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
         yearLabel.setText(_("Year") + ":");
         filtersPanel.add(yearLabel);
 
         yearComboBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 yearComboBoxItemStateChanged(evt);
             }
         });
         filtersPanel.add(yearComboBox);
 
         monthLabel.setText(_("Month") + ":");
         filtersPanel.add(monthLabel);
 
         monthComboBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 monthComboBoxItemStateChanged(evt);
             }
         });
         filtersPanel.add(monthComboBox);
 
         fundsLabel.setText(_("Funds") + ":");
         filtersPanel.add(fundsLabel);
 
         fundsComboBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 fundsComboBoxItemStateChanged(evt);
             }
         });
         filtersPanel.add(fundsComboBox);
 
         contentPanel.add(filtersPanel, java.awt.BorderLayout.PAGE_START);
 
         tablePanel.setLayout(new java.awt.GridLayout(1, 0));
 
         jTable1.setModel(controllingTableModel);
         jScrollPane1.setViewportView(jTable1);
 
         tablePanel.add(jScrollPane1);
 
         contentPanel.add(tablePanel, java.awt.BorderLayout.CENTER);
 
         add(contentPanel, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void yearComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_yearComboBoxItemStateChanged
         if (yearComboBox.getSelectedIndex() >= 0) {
             year = (Integer) yearComboBox.getSelectedItem();
             createMonthItems(year);
         }
     }//GEN-LAST:event_yearComboBoxItemStateChanged
 
     private void monthComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_monthComboBoxItemStateChanged
         if (monthComboBox.getSelectedIndex() >= 0) {
             month = (Integer) monthComboBox.getSelectedItem();
             createFundsItems(year, month);
         }
     }//GEN-LAST:event_monthComboBoxItemStateChanged
 
     private void fundsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fundsComboBoxItemStateChanged
         if (fundsComboBox.getSelectedIndex() >= 0) {
             this.funds = (Integer) fundsComboBox.getSelectedItem();
         }
     }//GEN-LAST:event_fundsComboBoxItemStateChanged
 
     private void exportAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAllButtonActionPerformed
     }//GEN-LAST:event_exportAllButtonActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPanel contentPanel;
     private javax.swing.JButton exportAllButton;
     private javax.swing.JButton exportDifferencesButton;
     private javax.swing.JPanel filtersPanel;
     private javax.swing.JComboBox fundsComboBox;
     private javax.swing.JLabel fundsLabel;
     private javax.swing.JButton generateButton;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JToolBar.Separator jSeparator3;
     private javax.swing.JToolBar.Separator jSeparator4;
     private javax.swing.JTable jTable1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JComboBox monthComboBox;
     private javax.swing.JLabel monthLabel;
     private javax.swing.JPanel tablePanel;
     private javax.swing.JComboBox yearComboBox;
     private javax.swing.JLabel yearLabel;
 
     // End of variables declaration//GEN-END:variables
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         return _("Assistant controlling");
     }
 
 }
