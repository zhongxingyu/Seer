 package de.aidger.view.tabs;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Color;
 import java.awt.Component;
import java.util.List;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.BudgetExportAction;
 import de.aidger.controller.actions.BudgetGenerateAction;
 import de.aidger.model.budgets.BudgetFilter;
 import de.aidger.model.budgets.BudgetFilter.Comparison;
 import de.aidger.model.models.Course;
 import de.aidger.utils.reports.BalanceHelper;
 import de.aidger.view.UI;
 import de.aidger.view.reports.BalanceFilterPanel;
 import de.unistuttgart.iste.se.adohive.exceptions.AdoHiveException;
 import de.unistuttgart.iste.se.adohive.model.ICourse;
 
 /**
  * This class displays a table of all the course budgets.
  * 
  * @author aidGer Team
  */
 @SuppressWarnings("serial")
 public class BudgetViewerTab extends ReportTab {
 
     /**
      * The budget filter of this budget check.
      */
     private BudgetFilter budgetFilter = null;
 
     /**
      * The filter panels of this budget check.
      */
     private ArrayList<BalanceFilterPanel> filterPanels = null;
 
     /**
      * The table model of the content table.
      */
     private final DefaultTableModel contentTableModel = new DefaultTableModel(
         null, new String[] { _("Course"), _("Semester"), _("Lecturer"),
                 _("Booked budgets"), _("Remaining budgets"),
                 _("Available budgets") }) {
 
         @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
             return false;
         }
     };
 
     /**
      * Initializes a new BudgetViewerTab with the available filters.
      */
     public BudgetViewerTab() {
         initComponents();
         budgetFilter = new BudgetFilter();
         filterPanels = new ArrayList<BalanceFilterPanel>();
         budgetFilterText.setVisible(false);
         try {
             generateButton.setAction(ActionRegistry.getInstance().get(
                 BudgetGenerateAction.class.getName()));
             exportButton.setAction(ActionRegistry.getInstance().get(
                 BudgetExportAction.class.getName()));
         } catch (ActionNotFoundException ex) {
             ex.printStackTrace();
             UI.displayError(ex.getMessage());
         }
         String[] filterNames = { _("Lecturer"), _("Semester"),
                 _("Remaining budgets"), _("Booked budgets"),
                 _("Available budgets") };
         for (int i = 0; i < filterNames.length; i++) {
             filterNameComboBox.addItem(filterNames[i]);
         }
     }
 
     /**
      * Returns the budget filter of this budget report.
      * 
      * @return The budget filter
      */
     public BudgetFilter getBudgetFilter() {
         return budgetFilter;
     }
 
     /**
      * Removes all rows of the table.
      */
     public void clearTable() {
         while (contentTableModel.getRowCount() > 0) {
             contentTableModel.removeRow(0);
         }
     }
 
     /**
      * Adds a row, with a budget course, to the table of budget courses.
      * 
      * @param objectArray
      *            The budget course to add.
      */
     public void addRow(Object[] objectArray) {
         contentTableModel.addRow(objectArray);
         contentTable.setVisible(false);
         contentTable.setVisible(true);
     }
 
     /**
      * Removes the given panel from the filter panel.
      * 
      * @param panel
      *            The panel to remove
      */
     @Override
     public void removeFilterPanel(JPanel panel) {
         filterContentPanel.remove(panel);
         filterContentPanel.setVisible(false);
         filterContentPanel.setVisible(true);
     }
 
     /**
      * Removes the specified filter from the filters list.
      * 
      * @param type
      *            The type of filter.
      * @param value
      *            The value of the filter.
      */
     @Override
     public void removeFilter(String name, String value) {
         if (name.equals(_("Lecturer"))) {
             /*
              * The filter is a filter for lecturers. Check if it is contained in
              * the filter list and then remove it.
              */
             if (budgetFilter.getLecturers().contains(value)) {
                 budgetFilter.removeLecturer(value);
             }
         } else if (name.equals(_("Semester"))) {
             /*
              * The filter is a filter for semesters. Check if it is contained in
              * teh filter list and the remove it.
              */
             if (budgetFilter.getSemesters().contains(value)) {
                 budgetFilter.removeSemester(value);
             }
         } else {
             /*
              * The filter is a filter for a specific budget. Remove the filter
              * panel of that filter and set its comparison method to none.
              */
             for (BalanceFilterPanel filterPanel : filterPanels) {
                 if (filterPanel.getName().equals(name)) {
                     filterPanels.remove(filterPanel);
                     break;
                 }
             }
            if (name.equals(_("Available budgets"))) {
                 budgetFilter.setAvailableComparison(Comparison.NONE);
             } else if (name.equals(_("Booked budgets"))) {
                 budgetFilter.setBookedComparison(Comparison.NONE);
            } else if (name.equals(_("Total budgets"))) {
                 budgetFilter.setTotalComparison(Comparison.NONE);
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
         exportButton = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JToolBar.Separator();
         jPanel1 = new javax.swing.JPanel();
         filtersPanel = new javax.swing.JPanel();
         filterCreationPanel = new javax.swing.JPanel();
         filtersLabel = new javax.swing.JLabel();
         filterNameComboBox = new javax.swing.JComboBox();
         filterComboBox = new javax.swing.JComboBox();
         budgetFilterText = new javax.swing.JTextField();
         addFilterButton = new javax.swing.JButton();
         filterContentPanel = new javax.swing.JPanel();
         contentPanel = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         contentTable = new javax.swing.JTable() {
             @Override
             public Component prepareRenderer(TableCellRenderer renderer,
                     int row, int column) {
                 Component returnComp = super.prepareRenderer(renderer, row,
                     column);
                 Color alternateColor = new Color(252, 200, 150);
                 Color alternateModColor = new Color(239, 187, 137);
                 Color grey = new Color(242, 242, 242);
                 Color white = new Color(255, 255, 255);
                 if (row % 2 == 0) {
                     if (Double.parseDouble(this.getValueAt(row, 4).toString()) == 0) {
                         returnComp.setBackground(alternateModColor);
                     } else {
                         returnComp.setBackground(grey);
                     }
                 } else {
                     if (Double.parseDouble(this.getValueAt(row, 4).toString()) == 0) {
                         returnComp.setBackground(alternateColor);
                     } else {
                         returnComp.setBackground(white);
                     }
                 }
                 returnComp.setForeground(Color.black);
                 return returnComp;
             }
         };
 
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
 
         exportButton.setText(_("Export"));
         exportButton.setFocusable(false);
         exportButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jToolBar1.add(exportButton);
         jToolBar1.add(jSeparator2);
 
         add(jToolBar1, java.awt.BorderLayout.PAGE_START);
 
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         filtersPanel.setLayout(new java.awt.BorderLayout());
 
         filterCreationPanel.setLayout(new java.awt.FlowLayout(
             java.awt.FlowLayout.LEFT));
 
         filtersLabel.setText(_("Filters") + ":");
         filterCreationPanel.add(filtersLabel);
 
         filterNameComboBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 filterNameComboBoxItemStateChanged(evt);
             }
         });
         filterCreationPanel.add(filterNameComboBox);
 
         filterCreationPanel.add(filterComboBox);
 
         budgetFilterText.setMinimumSize(new java.awt.Dimension(40, 25));
         budgetFilterText.setPreferredSize(new java.awt.Dimension(40, 25));
         filterCreationPanel.add(budgetFilterText);
 
         addFilterButton.setIcon(new ImageIcon(getClass().getResource(
             "/de/aidger/view/icons/plus-small.png")));
         addFilterButton.setFocusable(false);
         addFilterButton
             .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         addFilterButton
             .setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         addFilterButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addFilterButtonActionPerformed(evt);
             }
         });
         filterCreationPanel.add(addFilterButton);
 
         filtersPanel.add(filterCreationPanel, java.awt.BorderLayout.PAGE_START);
 
         filterContentPanel.setLayout(new java.awt.GridLayout(0, 1));
         filtersPanel.add(filterContentPanel, java.awt.BorderLayout.CENTER);
 
         jPanel1.add(filtersPanel, java.awt.BorderLayout.PAGE_START);
 
         contentPanel.setLayout(new java.awt.GridLayout(0, 1));
 
         contentTable.setModel(contentTableModel);
         jScrollPane1.setViewportView(contentTable);
 
         contentPanel.add(jScrollPane1);
 
         jPanel1.add(contentPanel, java.awt.BorderLayout.CENTER);
 
         add(jPanel1, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void filterNameComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_filterNameComboBoxItemStateChanged
         switch (filterNameComboBox.getSelectedIndex()) {
         /*
          * Clear the filter combo box and add all entries of the selected filter
          * type.
          */
         case 0:
             filterComboBox.removeAllItems();
             budgetFilterText.setVisible(false);
             budgetFilterText.setText("");
             List<ICourse> courses = null;
             try {
                 courses = (new Course()).getAll();
             } catch (AdoHiveException e) {
                 UI.displayError(e.toString());
             }
             ArrayList<String> courseLecturers = new ArrayList<String>();
             for (ICourse course : courses) {
                 if (!courseLecturers.contains(course.getLecturer())) {
                     courseLecturers.add(course.getLecturer());
                     filterComboBox.addItem(course.getLecturer());
                 }
             }
             break;
         /*
          * Clear the filter combo box and add all entries of the selected filter
          * type.
          */
         case 1:
             filterComboBox.removeAllItems();
             budgetFilterText.setVisible(false);
             budgetFilterText.setText("");
             try {
                 courses = (new Course()).getAll();
                 ArrayList<String> courseSemesters = new BalanceHelper()
                     .getSemesters();
                 for (String semester : courseSemesters) {
                     filterComboBox.addItem(semester);
                 }
             } catch (AdoHiveException e) {
                 UI.displayError(e.toString());
             }
             break;
         /*
          * Clear the filter combo box and add all entries of the selected filter
          * type. Cases 2-4 are the same, as they compare numbers.
          */
         case 2:
         case 3:
         case 4:
             filterComboBox.removeAllItems();
             filterComboBox.addItem(Comparison.LESS);
             filterComboBox.addItem(Comparison.LESSEQUAL);
             filterComboBox.addItem(Comparison.EQUAL);
             filterComboBox.addItem(Comparison.GREATEREQUAL);
             filterComboBox.addItem(Comparison.GREATER);
             budgetFilterText.setText("");
             budgetFilterText.setVisible(true);
             break;
         }
     }//GEN-LAST:event_filterNameComboBoxItemStateChanged
 
     private void addFilterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFilterButtonActionPerformed
         switch (filterNameComboBox.getSelectedIndex()) {
         /*
          * Add the filter to the filter list if it doesn't exist yet and create
          * a new panel that displays the filter.
          */
         case 0:
             if (!budgetFilter.getLecturers().contains(
                 filterComboBox.getSelectedItem().toString())
                     && filterComboBox.getItemCount() > 0) {
                 budgetFilter.addLecturer((String) filterComboBox
                     .getSelectedItem());
                 filterContentPanel.add(new BalanceFilterPanel(
                     filterNameComboBox.getItemAt(0).toString(),
                     (String) filterComboBox.getSelectedItem(), this));
             }
             break;
         /*
          * Add the filter to the filter list if it doesn't exist yet and create
          * a new panel that displays the filter.
          */
         case 1:
             if (!budgetFilter.getSemesters().contains(
                 filterComboBox.getSelectedItem().toString())
                     && filterComboBox.getItemCount() > 0) {
                 budgetFilter.addSemester((String) filterComboBox
                     .getSelectedItem());
                 filterContentPanel.add(new BalanceFilterPanel(
                     filterNameComboBox.getItemAt(1).toString(),
                     (String) filterComboBox.getSelectedItem(), this));
             }
             break;
         /*
          * Change the value and comparison method of the budget filter to the
          * ones given. If there already is a panel containing a filter of this
          * type, update it. Otherwise create one.
          */
         case 2:
             if (!budgetFilterText.getText().equals("")) {
                 budgetFilter.setAvailableBudget(Double
                     .parseDouble(budgetFilterText.getText()));
                 budgetFilter.setAvailableComparison((Comparison) filterComboBox
                     .getSelectedItem());
                 int i = 0;
                 boolean filterPanelFound = false;
                 for (BalanceFilterPanel filterPanel : filterPanels) {
                     if (filterPanel.getName().equals(
                         filterNameComboBox.getSelectedItem())) {
                         filterPanelFound = true;
                         break;
                     }
                     i++;
                 }
                 if (i == filterPanels.size() || !filterPanelFound) {
                     BalanceFilterPanel filterPanel = new BalanceFilterPanel(
                         filterNameComboBox.getItemAt(2).toString(),
                         filterComboBox.getSelectedItem().toString(), this);
                     filterPanel.setBudgetLabel(budgetFilterText.getText());
                     filterPanels.add(filterPanel);
                     filterContentPanel.add(filterPanel);
                 } else {
                     BalanceFilterPanel filterPanel = filterPanels.get(i);
                     filterPanel.setFilterLabel(filterComboBox.getSelectedItem()
                         .toString());
                     filterPanel.setBudgetLabel(budgetFilterText.getText());
                 }
             }
             break;
         /*
          * Change the value and comparison method of the budget filter to the
          * ones given. If there already is a panel containing a filter of this
          * type, update it. Otherwise create one.
          */
         case 3:
             if (!budgetFilterText.getText().equals("")) {
                 budgetFilter.setBookedBudget(Double
                     .parseDouble(budgetFilterText.getText()));
                 budgetFilter.setBookedComparison((Comparison) filterComboBox
                     .getSelectedItem());
                 int i = 0;
                 boolean filterPanelFound = false;
                 for (BalanceFilterPanel filterPanel : filterPanels) {
                     if (filterPanel.getName().equals(
                         filterNameComboBox.getSelectedItem())) {
                         filterPanelFound = true;
                         break;
                     }
                     i++;
                 }
                 if (i == filterPanels.size() || !filterPanelFound) {
                     BalanceFilterPanel filterPanel = new BalanceFilterPanel(
                         filterNameComboBox.getItemAt(3).toString(),
                         filterComboBox.getSelectedItem().toString(), this);
                     filterPanel.setBudgetLabel(budgetFilterText.getText());
                     filterPanels.add(filterPanel);
                     filterContentPanel.add(filterPanel);
                 } else {
                     BalanceFilterPanel filterPanel = filterPanels.get(i);
                     filterPanel.setFilterLabel(filterComboBox.getSelectedItem()
                         .toString());
                     filterPanel.setBudgetLabel(budgetFilterText.getText());
                 }
             }
             break;
         /*
          * Change the value and comparison method of the budget filter to the
          * ones given. If there already is a panel containing a filter of this
          * type, update it. Otherwise create one.
          */
         case 4:
             if (!budgetFilterText.getText().equals("")) {
                 budgetFilter.setTotalBudget(Double.parseDouble(budgetFilterText
                     .getText()));
                 budgetFilter.setTotalComparison((Comparison) filterComboBox
                     .getSelectedItem());
                 int i = 0;
                 boolean filterPanelFound = false;
                 for (BalanceFilterPanel filterPanel : filterPanels) {
                     if (filterPanel.getName().equals(
                         filterNameComboBox.getSelectedItem())) {
                         filterPanelFound = true;
                         break;
                     }
                     i++;
                 }
                 if (i == filterPanels.size() || !filterPanelFound) {
                     BalanceFilterPanel filterPanel = new BalanceFilterPanel(
                         filterNameComboBox.getItemAt(4).toString(),
                         filterComboBox.getSelectedItem().toString(), this);
                     filterPanel.setBudgetLabel(budgetFilterText.getText());
                     filterPanels.add(filterPanel);
                     filterContentPanel.add(filterPanel);
                 } else {
                     BalanceFilterPanel filterPanel = filterPanels.get(i);
                     filterPanel.setFilterLabel(filterComboBox.getSelectedItem()
                         .toString());
                     filterPanel.setBudgetLabel(budgetFilterText.getText());
                 }
             }
             break;
         }
         filterContentPanel.setVisible(false);
         filterContentPanel.setVisible(true);
     }//GEN-LAST:event_addFilterButtonActionPerformed
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.tabs.Tab#getTabName()
      */
     @Override
     public String getTabName() {
         return _("Course controlling");
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addFilterButton;
     private javax.swing.JTextField budgetFilterText;
     private javax.swing.JPanel contentPanel;
     private javax.swing.JTable contentTable;
     private javax.swing.JButton exportButton;
     private javax.swing.JComboBox filterComboBox;
     private javax.swing.JPanel filterContentPanel;
     private javax.swing.JPanel filterCreationPanel;
     private javax.swing.JComboBox filterNameComboBox;
     private javax.swing.JLabel filtersLabel;
     private javax.swing.JPanel filtersPanel;
     private javax.swing.JButton generateButton;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JToolBar.Separator jSeparator1;
     private javax.swing.JToolBar.Separator jSeparator2;
     private javax.swing.JToolBar.Separator jSeparator3;
     private javax.swing.JToolBar jToolBar1;
     // End of variables declaration//GEN-END:variables
 
 }
