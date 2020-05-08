 /*
  * Copyright (C) 2013 Zhao Yi
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package zhyi.zse.swing.cas;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Collections;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import zhyi.zse.collection.CollectionUtils;
 import zhyi.zse.i18n.FallbackLocaleControl;
 import zhyi.zse.swing.GroupLayoutUtils;
 import zhyi.zse.swing.MultiValueSelector;
 import zhyi.zse.swing.SwingUtils;
 import zhyi.zse.swing.event.SelectionChangeEvent;
 import zhyi.zse.swing.event.SelectionChangeListener;
 
 /**
  * The context action support for table header.
  * <p>
  * The following actions are provided by this class:
  * <dl>
  * <dt><b>Size Column to Fit</b>
  * <dd>Sizes the width of the selected column to fit contents.
  * <dt><b>Size All Columns to Fit</b>
  * <dd>Sizes all column widths to fit contents.
  * <dt><b>Select Columns</b>
  * <dd>Selects columns to be displayed or hidden.
  * </dl>
  *
  * @author Zhao Yi
  */
 public class TableHeaderContextActionSupport extends ContextActionSupport<JTableHeader> {
     private static final String BUNDLE = "zhyi.zse.swing.cas.TableHeaderContextActionSupport";
     private static final MouseListener SELECTED_COLUMN_HANDLER = new SelectedColumnHandler();
 
     private Map<TableColumn, HiddenColumnInfo> hiddenColumnMap;
 
     /**
      * Constructs a new instance.
      *
      * @param tableHeader The table header to be supported.
      */
     public TableHeaderContextActionSupport(JTableHeader tableHeader) {
         super(tableHeader);
         hiddenColumnMap = new IdentityHashMap<>();
 
         install(new FitColumnAction(), 0);
         install(new FitAllColumnsAction(), 0);
         install(new SelectColumnsAction(), 1);
 
         tableHeader.putClientProperty(Key.SELECTED_COLUMN, -1);
         tableHeader.addMouseListener(SELECTED_COLUMN_HANDLER);
     }
 
     @Override
     protected void cleanUp() {
         hiddenColumnMap.clear();
         component.putClientProperty(Key.SELECTED_COLUMN, null);
         component.removeMouseListener(SELECTED_COLUMN_HANDLER);
     }
 
     @SuppressWarnings("serial")
     private class FitColumnAction extends AbstractContextAction {
         private FitColumnAction() {
             super(BUNDLE, "fitColumn");
         }
 
         @Override
         public boolean isEnabled() {
             if (component.isEnabled()
                     && component.getResizingAllowed()
                     && component.getTable() != null) {
                 int column = (int) component.getClientProperty(Key.SELECTED_COLUMN);
                 if (column != -1) {
                     return component.getColumnModel().getColumn(column).getResizable();
                 }
             }
             return false;
         }
 
         @Override
         void doAction() {
             SwingUtils.fitColumnWidth(component.getTable(),
                     (int) component.getClientProperty(Key.SELECTED_COLUMN));
         }
     }
 
     @SuppressWarnings("serial")
     private class FitAllColumnsAction extends AbstractContextAction {
         private FitAllColumnsAction() {
             super(BUNDLE, "fitAllColumns");
         }
 
         @Override
         public boolean isEnabled() {
             return component.isEnabled() && component.getTable() != null
                     && component.getTable().isEnabled()
                     && component.getColumnModel().getColumnCount() > 0;
         }
 
         @Override
         void doAction() {
             SwingUtils.fitAllColumnWidth(component.getTable());
         }
     }
 
     @SuppressWarnings("serial")
     private class SelectColumnsAction extends AbstractContextAction {
         private MultiValueSelector<JCheckBox, TableColumn> columnSelector;
 
         private SelectColumnsAction() {
             super(BUNDLE, "selectColumns");
             columnSelector = new MultiValueSelector<>();
             columnSelector.addSelectionChangeListener(
                     new SelectionChangeListener<JCheckBox, TableColumn>() {
                 @Override
                 public void selectionChanged(SelectionChangeEvent<JCheckBox, TableColumn> e) {
                     updateSelectionState();
                 }
             });
         }
 
         @Override
         String getName() {
             return super.getName() + "...";
         }
 
         @Override
         public boolean isEnabled() {
             return component.isEnabled() && component.getTable() != null
                     && component.getTable().isEnabled()
                     && component.getColumnModel().getColumnCount() > 0;
         }
 
         @Override
         void doAction() {
             List<TableColumn> tableColumns = Collections.list(
                     component.getColumnModel().getColumns());
 
             // Remove columns that no longer exist in the column model.
             hiddenColumnMap.keySet().retainAll(tableColumns);
             columnSelector.removeAll();
             for (TableColumn tableColumn : tableColumns) {
                 columnSelector.add(
                         new JCheckBox(tableColumn.getHeaderValue().toString(),
                                 !hiddenColumnMap.containsKey(tableColumn)),
                         tableColumn);
             }
             updateSelectionState();
 
             JPanel panel = new JPanel();
             GroupLayout gl = GroupLayoutUtils.createGroupLayout(panel, false, true);
             Set<JCheckBox> checkBoxSet = columnSelector.getButtons();
             int count = checkBoxSet.size();
             JCheckBox[] checkBoxes = checkBoxSet.toArray(new JCheckBox[count]);
 
             int columns = 4;
             int rows = count / columns;
             if (rows * columns < count) {
                 rows++;
             }
 
             SequentialGroup hg = gl.createSequentialGroup();
             gl.setHorizontalGroup(hg);
             for (int c = 0; c < columns; c++) {
                 ParallelGroup pg = gl.createParallelGroup();
                 hg.addGroup(pg);
                 for (int r = 0; r < rows; r++) {
                     int offset = r * columns + c;
                     if (offset < count) {
                         pg.addComponent(checkBoxes[offset]);
                     }
                 }
             }
 
             SequentialGroup vg = gl.createSequentialGroup();
             gl.setVerticalGroup(vg);
             for (int r = 0; r < rows; r++) {
                 ParallelGroup pg = gl.createParallelGroup(Alignment.BASELINE);
                 vg.addGroup(pg);
                 for (int c = 0; c < columns; c++) {
                     int offset = r * columns + c;
                     if (offset < count) {
                         pg.addComponent(checkBoxes[offset]);
                     }
                 }
             }
 
             JOptionPane optionPane = new JOptionPane(panel,
                     JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
             JDialog dialog = optionPane.createDialog(component.getTable(),
                     ResourceBundle.getBundle(BUNDLE, FallbackLocaleControl.EN_US_CONTROL)
                             .getString("selectColumns"));
             dialog.pack();
             dialog.setLocationRelativeTo(SwingUtilities.getUnwrappedParent(component.getTable()));
             dialog.setVisible(true);
 
            if (optionPane.getValue().equals(JOptionPane.OK_OPTION)) {
                 TableColumnModel columnModel = component.getColumnModel();
                 for (JCheckBox checkBox : checkBoxes) {
                     TableColumn tableColumn = columnSelector.getValue(checkBox);
 
                     // Show a hidden column.
                     if (checkBox.isSelected()
                             && hiddenColumnMap.containsKey(tableColumn)) {
                         // Size back the hidden column.
                         HiddenColumnInfo info = hiddenColumnMap.remove(tableColumn);
                         tableColumn.setMinWidth(info.minWidth);
                         tableColumn.setMaxWidth(info.maxWidth);
                         tableColumn.setPreferredWidth(info.prefWidth);
                         tableColumn.setResizable(info.resizable);
 
                         // Move it to the possibly original position, and keep
                         // all hidden columns in the rightmost side of the table.
                         int index = info.index;
                         if (index >= columnModel.getColumnCount()) {
                             index = columnModel.getColumnCount() - 1;
                         }
                         int firstHiddenColumnIndex = -1;
                         for (TableColumn tc : CollectionUtils.iterable(
                                 columnModel.getColumns())) {
                             firstHiddenColumnIndex++;
                             if (hiddenColumnMap.containsKey(tc)) {
                                 break;
                             }
                         }
                         if (index >= firstHiddenColumnIndex) {
                             index = firstHiddenColumnIndex - 1;
                         }
                         columnModel.moveColumn(getColumnIndex(tableColumn), index);
                     }
 
                     // Hide a visible column.
                     if (!checkBox.isSelected()
                             && !hiddenColumnMap.containsKey(tableColumn)) {
                         int index = getColumnIndex(tableColumn);
                         hiddenColumnMap.put(tableColumn,
                                 new HiddenColumnInfo(index,
                                         tableColumn.getResizable(),
                                         tableColumn.getMinWidth(),
                                         tableColumn.getPreferredWidth(),
                                         tableColumn.getMaxWidth()));
 
                         // Make the column invisible by setting its width to 0.
                         // To make sure it does not affect visible columns, move
                         // it to the rightmost side of the table.
                         tableColumn.setMinWidth(0);
                         tableColumn.setPreferredWidth(0);
                         tableColumn.setMaxWidth(0);
                         tableColumn.setResizable(false);
                         columnModel.moveColumn(index, columnModel.getColumnCount() - 1);
                     }
                 }
             }
         }
 
         @SuppressWarnings("null")
         private void updateSelectionState() {
             JCheckBox firstSelectedCheckBox = null;
             JCheckBox disabledCheckBox = null;
             int selectedCount = 0;
             for (JCheckBox checkBox : columnSelector.getButtons()) {
                 if (checkBox.isSelected()) {
                     if (firstSelectedCheckBox == null) {
                         firstSelectedCheckBox = checkBox;
                     }
                     selectedCount++;
                 }
                 if (!checkBox.isEnabled()) {
                     disabledCheckBox = checkBox;
                 }
             }
 
             if (selectedCount == 1) {
                 firstSelectedCheckBox.setEnabled(false);
             } else if (disabledCheckBox != null) {
                 disabledCheckBox.setEnabled(true);
             }
         }
 
         private int getColumnIndex(TableColumn tableColumn) {
             int index = -1;
             int i = 0;
             for (TableColumn tc : CollectionUtils.iterable(
                     component.getColumnModel().getColumns())) {
                 if (tc == tableColumn) {
                     index = i;
                     break;
                 }
                 i++;
             }
             return index;
         }
     }
 
     private static class SelectedColumnHandler extends MouseAdapter {
         @Override
         public void mousePressed(MouseEvent e) {
             updateSelectedColumn(e);
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
             updateSelectedColumn(e);
         }
 
         private void updateSelectedColumn(MouseEvent e) {
             if (e.isPopupTrigger()) {
                 JTableHeader th = (JTableHeader) e.getSource();
                 th.putClientProperty(Key.SELECTED_COLUMN,
                         th.columnAtPoint(e.getPoint()));
             }
         }
     }
 
     private static class HiddenColumnInfo {
         private int index;
         private boolean resizable;
         private int minWidth;
         private int prefWidth;
         private int maxWidth;
 
         private HiddenColumnInfo(int index, boolean resizable,
                 int minWidth, int prefWidth, int maxWidth) {
             this.index = index;
             this.resizable = resizable;
             this.minWidth = minWidth;
             this.prefWidth = prefWidth;
             this.maxWidth = maxWidth;
         }
     }
 
     private static enum Key {
         SELECTED_COLUMN;
     }
 }
