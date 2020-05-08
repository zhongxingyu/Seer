 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.screen.grid;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JComponent;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import org.jbundle.model.DBException;
 import org.jbundle.model.db.Convert;
 import org.jbundle.model.db.Field;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.db.FieldInfo;
 import org.jbundle.thin.base.db.FieldList;
 import org.jbundle.thin.base.db.FieldTable;
 import org.jbundle.thin.base.db.buff.BaseBuffer;
 import org.jbundle.thin.base.db.buff.VectorBuffer;
 import org.jbundle.thin.base.screen.AbstractThinTableModel;
 import org.jbundle.thin.base.screen.JScreenConstants;
 import org.jbundle.thin.base.screen.grid.sort.SortableHeaderRenderer;
 
 
 
 /**
  * Creates a table model that talks to a thin table.
  * <p>To use this class, write the following code:
  * <pre>
  * ThinTableModel model = new ThinTableModel(null);
  * JTable table = new JTable(model);
  * model.setGridScreen(table, false); // Update when selection changes!
  * If you want custom column widths, do the following:
  * m_jTableScreen = new JTable(); // Do not supply the model because JTable would set up a default grid.
  * m_thinTableModel.setGridScreen(m_jTableScreen, true);
  * </pre>
  */
 public class ThinTableModel extends AbstractThinTableModel
 {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Increasing this will increase the granularity of the jumps in row value.
      */
     private final static double POWER = 4.0;
     /**
      * Starting estimate as to the number of rows.
      */
     private final static int START_ROWS = 64;
     /**
      * This constant means the record number is unknown.
      */
     public final static int RECORD_UNKNOWN = -2;
     /**
      * Theoretical EOF value for table scroller (Actual when known).
      */
     protected int m_iRowCount = START_ROWS;
     /**
      * Largest valid record accessed so far.
      */
     protected int m_iLargestValidRecord = -1;
     /**
      * When EOF is known.
      */
     protected int m_iLastRecord = RECORD_UNKNOWN;
     /**
      * The row of the current record.
      */
     protected int m_iCurrentRowIndex = -1;
     /**
      * The row of the currently locked record.
      */
     protected int m_iCurrentLockedRowIndex = -1;
     /**
      * Data for the currently locked record (Each array location is a column).
      */
     protected BaseBuffer m_buffCurrentLockedData = null;
     /**
      * Always start here.
      */
     protected int m_iSelectedRow = -1;
 
     private Set<ThinTableModelListener> m_modelListeners = null;
 
     /**
      * ThinTableModel constructor.
      */
     public ThinTableModel()
     {
         super();
     }
     /**
      * ThinTableModel constructor.
      * @param table  The table for this model.
      */
     public ThinTableModel(FieldTable table)
     {
         this();
         this.init(table);
     }
     /**
      * ThinTableModel constructor.
      * @param table  The table for this model.
      */
     public void init(FieldTable table)
     {
         super.init(table);
 
         m_iRowCount = START_ROWS;       // Theoretical EOF value for table scroller (Actual when known).
     }
     /**
      * I'm done with the model, free the resources.
      */
     public void free()
     {
         try {
             this.updateIfNewRow(-1);    // Update any locked record.
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         if (m_modelListeners != null)
         {
             for (ThinTableModelListener thinTableModelListener : m_modelListeners)
             {
                 thinTableModelListener.m_table.getSelectionModel().removeListSelectionListener(thinTableModelListener);
             }
             m_modelListeners = null;
         }
         super.free();
     }
     /**
      * Get the number of rows in the physical table.
      * Fakes the row count until EOF is hit.
      * @return The theoretical row count.
      */
     public int getRowCount()
     {
         return this.getRowCount(true);
     }
     /**
      * Get the number of rows in the physical table.
      * Fakes the row count until EOF is hit.
      * @return The theoretical row count.
      */
     public int getRowCount(boolean bIncludeAppendedRow)
     {
         if (bIncludeAppendedRow)
             if (this.isAppending())
                 return m_iRowCount + 1;      // Add an empty row to the end for appending
         return m_iRowCount;
     }
     /**
      * Set the new theoretical row count.
      * @param iRowCount The new row count.
      */
     public int setRowCount(int iRowCount)
     {
         m_iRowCount = iRowCount;
         return m_iRowCount;
     }
     /**
      * Is this cell editable.
      * @return true unless this is a deleted record.
      */
     public boolean isCellEditable(int iRowIndex, int iColumnIndex)
     {
         FieldList fieldList = this.makeRowCurrent(iRowIndex, false);
         if (fieldList != null) if (fieldList.getEditMode() == Constants.EDIT_NONE)
             if ((m_iLastRecord == RECORD_UNKNOWN) || (iRowIndex != m_iLastRecord + 1))
                 return false; // Don't allow changes to deleted records
         return true;        // All my fields are editable
     }
     /**
      * Set the value at this location.
      * @param aValue The raw-data value to set.
      * @param iRowIndex The row.
      * @param iColumnIndex The column.
      */
     public void setValueAt(Object aValue, int iRowIndex, int iColumnIndex)
     {
         try   {
             this.updateIfNewRow(iRowIndex);   // Update old if new row
             boolean bPhysicallyLockRecord = true;   // Fix this?
             if (this.getFieldInfo(iColumnIndex) == null)
                 bPhysicallyLockRecord = false;  // No need to lock if this control isn't tied into a field.
             // Note: The record has to be locked in case there are any behaviors that change other fields and lock the record.
             // This code was originally designed to change the fields in the buffer, then update the record when the row changed
             // Unfortunately, if there was a listener that locked the record, the changes to that point would be lost since the record is not locked.
             FieldList fieldList = this.makeRowCurrent(iRowIndex, bPhysicallyLockRecord);
             boolean bFieldChanged = this.setColumnValue(iColumnIndex, aValue, Constants.DISPLAY, Constants.SCREEN_MOVE);
 //            if (this.isRecordChanged())
             if (bFieldChanged)
             {
                 this.cacheCurrentLockedData(iRowIndex, fieldList);
                 this.fireTableRowsUpdated(iRowIndex, iRowIndex);
             }
             int iRowCount = this.getRowCount(false);
             if ((this.isAppending()) && (iRowIndex == m_iLastRecord + 1)    // Changing a new last row
                 && (iRowCount == m_iLastRecord + 1) && (m_iLastRecord != RECORD_UNKNOWN))   // Last row = new row
             {   // Changing the last row (If changes, add a row)
                 bFieldChanged = this.isRecordChanged();
                 if ((fieldList.getEditMode() == Constants.EDIT_CURRENT) || (fieldList.getEditMode() == Constants.EDIT_IN_PROGRESS))
                 {
                     m_iLastRecord++;    // Special case (A listener possibly wrote and refreshed the record)
                     bFieldChanged = true;
                 }
                 if (bFieldChanged)
                 {   // Currently adding a new record to the end, so add a blank for a new next record
                     this.setRowCount(iRowCount + 1);          // new EOF
                     this.fireTableRowsInserted(iRowCount + 1, iRowCount + 1);
                 }
             }
             if (this.getSelectedRow() != iRowIndex)
                 this.updateIfNewRow(this.getSelectedRow());   // If no longer selected, update it!
         } catch (Exception ex)  {
             ex.printStackTrace();
         }
     }
     /**
      * Get the array of changed values for the current row.
      * I use a standard field buffer and append all the screen items which
      * are not included in the field.
      * @param iRowIndex The row to get the data for.
      * @return The array of data for the currently locked row.
      */
     public BaseBuffer cacheCurrentLockedData(int iRowIndex, FieldList fieldList)
     {
         int iColumnCount = this.getColumnCount();
         if ((m_buffCurrentLockedData == null) || (m_iCurrentLockedRowIndex != iRowIndex))
             m_buffCurrentLockedData = new VectorBuffer(null, BaseBuffer.ALL_FIELDS);
         m_buffCurrentLockedData.fieldsToBuffer(fieldList);
         for (int i = 0; i < iColumnCount; i++)
         {   // Cache the non-field data
             Field field = null;
             if (this.getFieldInfo(i) != null)
                 field = this.getFieldInfo(i).getField();
             if ((field != null) && (field.getRecord() != fieldList))
                 m_buffCurrentLockedData.addNextString(field.toString());
         }
         m_iCurrentLockedRowIndex = iRowIndex;
         return m_buffCurrentLockedData;
     }
     /**
      * Restore this fieldlist with items from the input cache.
      * I use a standard field buffer and append all the screen items which
      * are not included in the field.
      * @param fieldList The record to fill with the data.
      */
     public void restoreCurrentRecord(FieldList fieldList)
     {
         m_buffCurrentLockedData.bufferToFields(fieldList, Constants.DONT_DISPLAY, Constants.READ_MOVE);
 
         int iColumnCount = this.getColumnCount();
         for (int i = 0; i < iColumnCount; i++)
         {   // Cache the non-field data
             Field field = null;
             if (this.getFieldInfo(i) != null)
                 field = this.getFieldInfo(i).getField();
             if ((field != null) && (field.getRecord() != fieldList))
                 field.setString(m_buffCurrentLockedData.getNextString(), Constants.DONT_DISPLAY, Constants.READ_MOVE);
         }
     }
     /**
      * Set the value at the field at the column.
      * Since this is only used by the restore current record method,
      * pass dont_display and read_move when you set the data.
      * This is NOT a TableModel override, this is my method.
      * @param iColumnIndex The column.
      * @param value The raw data value or string to set.
      * @return True if the value at this cell changed.
      */
     public boolean setColumnValue(int iColumnIndex, Object value, boolean bDisplay, int iMoveMode)
     {
         Convert fieldInfo = this.getFieldInfo(iColumnIndex);
         if (fieldInfo != null)
         {
             Object dataBefore = fieldInfo.getData();
             if (!(value instanceof String))
                 fieldInfo.setData(value, bDisplay, iMoveMode);
             else
                 fieldInfo.setString((String)value, bDisplay, iMoveMode);
             Object dataAfter =  fieldInfo.getData();
             if (dataBefore == null)
                 return (dataAfter != null);
             else
                 return (!dataBefore.equals(dataAfter));
         }
         return false;
     }
     /**
      * Get the value at this location.
      * @param iRowIndex The row.
      * @param iColumnIndex The column.
      * @return The raw data at this location.
      */
     public Object getValueAt(int iRowIndex, int iColumnIndex)
     {
         int iEditMode = Constants.EDIT_NONE;
         try   {
             if (iRowIndex >= 0)
             {
                 FieldList fieldList = this.makeRowCurrent(iRowIndex, false);
                 if (fieldList != null)
                     iEditMode = fieldList.getEditMode();
             }
         } catch (Exception ex)  {
             ex.printStackTrace();
         }
         return this.getColumnValue(iColumnIndex, iEditMode);
     }
     /**
      * Get the value of the field at the column.
      * This is NOT a TableModel override, this is my method.
      * @param iColumnIndex The column.
      * @return The string at this location.
      */
     public Object getColumnValue(int iColumnIndex, int iEditMode)
     {
         Convert fieldInfo = this.getFieldInfo(iColumnIndex);
         if (fieldInfo != null)
             return fieldInfo.getString();
         return Constants.BLANK;
     }
     /**
      * This is called when the model is no longer need by the JTable,
      * so update any current record.
      * @param l The table model listener to remove.
      */
     public void removeTableModelListener(TableModelListener l)
     {
         try   {
             this.updateIfNewRow(-1);    // Update any locked record.
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         super.removeTableModelListener(l);
     }
     /**
      * If you have not hit EOF yet, calc a theoretical EOF value.
      * This "guesses" at an EOF value, and when the user hits the theoretical EOF, bumps it to a higher value.
      * The value is increased logrithmically (ie., 0-3 ->4, 4-15 ->16, 16-63 ->64 depending on the POWER constant).
      * @param iCurrentRow This row is a valid record, do I need to adjust my theoretical maximum?
      */
     public void checkRowValue(int iCurrentRow)
     {
         if (m_iLastRecord != RECORD_UNKNOWN)
             return;     // No need to calc an eof if we have the real thing.
         m_iLargestValidRecord = Math.max(m_iLargestValidRecord, iCurrentRow);
         if (iCurrentRow < this.getRowCount(true) - 1)
             return;     // No need to recalc if less than the estimated EOF
         int iRoot = (int)(Math.pow(iCurrentRow, 1.0 / POWER)) + 1;
         this.setRowCount((int)(Math.pow(iRoot, POWER)) + 1);    // New estimated EOF
     }
     /**
      * The underlying query changed, reset the model.
      */
     public void resetTheModel()
     {
         this.setRowCount(START_ROWS);       // Theoretical EOF value for table scroller (Actual when known).
         m_iLastRecord = RECORD_UNKNOWN;                 // When EOF is known
         m_iLargestValidRecord = -1;
         this.setCurrentRow(-1);
         m_iCurrentLockedRowIndex = -1;
         this.fireTableChanged(new TableModelEvent(this)); // Entire table changed
     }
     /**
      * Return the currently selected row number (from listening to selection events).
      * @return The selected row number.
      */
     public int getSelectedRow()
     {
         return m_iSelectedRow;
     }
     /**
      * Physically read this row.
      * @param iRowIndex Row to read (-1 to invaliate the current row.)
      * @param bLockRecord If true, call edit() on the record.
      * @return The record for this row.
      */
     public synchronized FieldList makeRowCurrent(int iRowIndex, boolean bLockRecord)
     {
         FieldList fieldList = null;
         if (m_table == null)
             return null;
         if (iRowIndex == -1)
         {
             this.setCurrentRow(iRowIndex);     // Invalidate the current row
             if (bLockRecord)
                 m_iCurrentLockedRowIndex = iRowIndex;
             return null;
         }
         try   {
             if ((iRowIndex == this.getCurrentRow()) && (!bLockRecord))
                 fieldList = m_table.getRecord();   // Already current record
             else
             {
                 if ((m_iLastRecord == RECORD_UNKNOWN) || (iRowIndex <= m_iLastRecord))
                 {
                     fieldList = (FieldList)m_table.get(iRowIndex);  // Read this record
                     if ((fieldList == null) || (fieldList.getEditMode() == Constants.EDIT_ADD) || (fieldList.getEditMode() == Constants.EDIT_NONE))
                         if (m_iCurrentLockedRowIndex == iRowIndex)
                     {
                         m_iCurrentLockedRowIndex = -1;
                         m_buffCurrentLockedData = null; // This record was deleted, clear the buffer!                 
                     }
                     if (bLockRecord)
                         if (fieldList != null)
                             if (fieldList.getEditMode() == Constants.EDIT_CURRENT)
                                 m_table.edit();
                 }
                 else
                 {   // Past the EOF, just supply a blank record
                     m_table.addNew();
                     fieldList = m_table.getRecord();
                 }
                 int iOldRowCount = this.getRowCount(true);
                 if (fieldList != null) if (m_iLastRecord == RECORD_UNKNOWN)
                 {
                     this.checkRowValue(iRowIndex);
                     if (this.getRowCount(true) > iOldRowCount)
                         this.fireTableRowsInserted(iOldRowCount, this.getRowCount(true) - 1);
                     else if (this.getRowCount(true) < iOldRowCount)
                         this.fireTableRowsDeleted(iOldRowCount, this.getRowCount(true) - 1);
                 }
                 if (fieldList == null)
                 { // Got to end of file!
                     if ((m_iLastRecord == RECORD_UNKNOWN) && (iRowIndex > m_iLargestValidRecord))
                     {
                         if (iRowIndex == m_iLargestValidRecord + 1)
                         {
                             m_iLastRecord = iRowIndex - 1;  // Now I know the actual EOF
                         }
                         else if (iRowIndex > m_iLargestValidRecord + 1)
                         {
                             int iEOF = m_table.size();
                             if (iEOF != -1)
                             {
                                 m_iLastRecord = iEOF - 1;
                                 iRowIndex = m_iLastRecord + 1;
                             }
                             else
                             {   // Okay, the EOF is somewhere between m_iLargestValidRecord+1 and iRowIndex.. find it!
                                 int iTargetIndex = (m_iLargestValidRecord + 1 + iRowIndex) / 2;
                                 while (iTargetIndex > m_iLargestValidRecord + 1)
                                 {
                                     fieldList = (FieldList)m_table.get(iTargetIndex);  // Read this record
                                     if (fieldList != null)
                                         m_iLargestValidRecord = iTargetIndex;
                                     else
                                         iRowIndex = iTargetIndex;
                                     iTargetIndex = (m_iLargestValidRecord + 1 + iRowIndex) / 2;
                                 }
                                 m_iLastRecord = m_iLargestValidRecord;
                                 iRowIndex = m_iLastRecord + 1;
                             }
                         }
                         this.setRowCount(iRowIndex);        // new EOF
                         if (iOldRowCount > this.getRowCount(true))
                             this.fireTableRowsDeleted(this.getRowCount(true), iOldRowCount - 1);
                         if (this.getSelectedRow() != -1)
                         {
                             if (this.getSelectedRow() > this.getRowCount())
                             {
                                 this.makeRowCurrent(this.getRowCount() - 1, false);
 //                                this.selectionChanged(this, this.getRowCount() - 1, this.getRowCount() -1 , DBConstants.SELECT_TYPE);
                             }
                         }
                     }
                     m_table.addNew();
                     fieldList = m_table.getRecord();
                 }
             }
             if ((m_iCurrentLockedRowIndex != -1) && (m_iCurrentLockedRowIndex == iRowIndex)
                 && (m_buffCurrentLockedData != null))
                     this.restoreCurrentRecord(fieldList); // Restore current edit record
         } catch (Exception ex)  {
             ex.printStackTrace();
         }
         this.setCurrentRow(iRowIndex);     // Successfully read
         return fieldList;
     }
     /**
      * Update the currently updated record if the row is different from this row.
      * @param iRowIndex Row to read... If different from current record, update current. If -1, update and don't read.
      */
     public void updateIfNewRow(int iRowIndex)
         throws DBException
     {
         if ((m_iCurrentLockedRowIndex != -1) && (m_iCurrentLockedRowIndex != iRowIndex))
             if (m_buffCurrentLockedData != null)
         {
             {
                 FieldList fieldList = this.makeRowCurrent(m_iCurrentLockedRowIndex, false);     // Read this row if it isn't current
                 if (this.isRecordChanged())
                 {   // Only update if there are changes.
                     synchronized (this)
                     {   // This time do the physical read (last time I asked for the cache)
                         DBException ex = null;
                         fieldList = this.makeRowCurrent(m_iCurrentLockedRowIndex, true);        // Read this row if it isn't current
                         if ((this.isAppending())
                             && ((m_iCurrentLockedRowIndex == m_iLastRecord + 1) && (m_iLastRecord != RECORD_UNKNOWN)))
                         {
                             m_table.add(fieldList);   // New record = add
                             this.setCurrentRow(-1);     // Make sure I don't try to use this unknown record without re-reading it.
                             if (m_iLastRecord != RECORD_UNKNOWN)    // Could have been set in 'add'
                                 m_iLastRecord++;
                         }
                         else
                         {
                             if ((fieldList.getEditMode() == Constants.EDIT_CURRENT) || (fieldList.getEditMode() == Constants.EDIT_IN_PROGRESS))
                             {
                                 try {
                                     m_table.set(fieldList);   // Current record = set
                                 } catch (DBException e) {
                                     ex = e;
                                 }
                             }
                             this.setCurrentRow(-1);    // Can't use current record anymore
                         }
                         m_iCurrentLockedRowIndex = -1;
                         m_buffCurrentLockedData = null;
                         if (ex != null)
                             throw ex;
                     }
                 }
             }
         }
     }
     /**
      * Is the record modified?
      * Note: Since I don't know, I always return true (override to change this).
      * @return True if this record is changed.
      */
     public boolean isRecordChanged()
     {
         return true;    // Override if you have a way to know for sure.
     }
     /**
      * Do any linkage that the grid screen may need to work with this model.
      * Note: This is generally called after the panel is added to its parent, so I can correct
      * column widths, etc.
      * @param gridScreen The gridscreen.
      * @param bSetupTable If true, set up the JTable from this model.
      */
     public void setGridScreen(JComponent gridScreen, boolean bSetupJTable)
     {
         if (gridScreen instanceof JTable)
         {
             JTable control = (JTable)gridScreen;
             ThinTableModelListener thinTableModelListener = new ThinTableModelListener(control, this);
             if (m_modelListeners == null)
                 m_modelListeners = new HashSet<ThinTableModelListener>();
             m_modelListeners.add(thinTableModelListener);
             control.getSelectionModel().addListSelectionListener(thinTableModelListener);   // Listen for selection change to update
             if (bSetupJTable)
             {
                int iCharWidth = control.getFontMetrics(control.getFont()).charWidth('x');
                 control.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 //              control.setAutoCreateColumnsFromModel(false);
                 control.setColumnSelectionAllowed(false); // Don't allow column selections
                 control.setModel(this);
                 JTableHeader tableHeader = control.getTableHeader();
                 TableColumnModel columnModel = tableHeader.getColumnModel();
                 for (int iIndex = 0; iIndex < this.getColumnCount(); iIndex++)
                 {
                     Convert field = this.getFieldInfo(iIndex);
                     int iWidth = JScreenConstants.ICON_SIZE.width;
                     if (field != null)
                         iWidth = field.getMaxLength() * iCharWidth;
                     TableColumn tableColumn = columnModel.getColumn(iIndex);
                     TableCellRenderer cellRenderer = this.createColumnCellRenderer(iIndex);
                     if (cellRenderer == null)
                         if (field instanceof FieldInfo)
                     {
                         if (Number.class.isAssignableFrom(((FieldInfo)field).getDataClass()))
                         {
                             tableColumn.setCellRenderer(new JCellTextField(field.getMaxLength(), true));    // Align right, use calc length.
                             tableColumn.setCellEditor(new JCellTextField(field.getMaxLength(), true));  // Align right, use calc length.
                         }
                         if (Boolean.class.isAssignableFrom(((FieldInfo)field).getDataClass()))
                         {
                             cellRenderer = new JCellCheckBox(null);
                             tableColumn.setCellEditor(new JCellCheckBox(null));
                         }
                     }
                     if (cellRenderer != null)
                     {
                         tableColumn.setCellRenderer(cellRenderer);
                         if (cellRenderer instanceof JComponent)
                             iWidth = ((JComponent)cellRenderer).getPreferredSize().width;
                         if (field != null)
                             field.addComponent(cellRenderer);
                     }
                     TableCellEditor cellEditor = this.createColumnCellEditor(iIndex);
                     if (cellEditor != null)
                     {
                         tableColumn.setCellEditor(cellEditor);
                         if (field != null)
                             field.addComponent(cellEditor);
                     }
                     tableColumn.setPreferredWidth(iWidth);  // Fixed column width
 
                     String strFieldName = this.getColumnName(iIndex);
                     tableColumn.setHeaderValue(strFieldName);
                 }
         //      this.addMouseListenerToHeaderInTable(control);      // Notify model of row order changed (clicks in the header bar)
             }
             if (control.getModel() == null)
                 control.setModel(this);
             
             control.getModel().addTableModelListener(new FixScrollersOnNewTable(control));
         }
     }
     /**
      * For a new model, scroll to 0.
      * @author don
      *
      */
     public class FixScrollersOnNewTable extends Object
         implements TableModelListener
     {
         protected JTable m_control = null;
         
         public FixScrollersOnNewTable(JTable control)
         {
             m_control = control;
         }
         
         public void tableChanged(TableModelEvent e)
         {
             if (e.getColumn() == TableModelEvent.ALL_COLUMNS)
                 if (e.getFirstRow() == 0)
                     if (e.getLastRow() == Integer.MAX_VALUE)
                     {   // Everything changed - Scroll to 0
                         Component comp = m_control.getParent();
                         if (comp != null)
                             comp = comp.getParent();
                         if (comp instanceof JScrollPane)
                         {
                             JScrollBar scrollBar = ((JScrollPane)comp).getVerticalScrollBar();
                             scrollBar.setValue(0);
                         }
                     }
         }
         
     }
     /**
      * There is no-where else to put this. 
      * Add a mouse listener to the Table to trigger a table sort 
      * when a column heading is clicked in the JTable.
      * @param table The table to listen for a header mouse click.
      */
     public void addMouseListenerToHeaderInTable(JTable table)
     { 
         table.setColumnSelectionAllowed(false); 
         MouseListener mouseListener = new MouseAdapter()
         {
             public void mouseClicked(MouseEvent e)
             {
                 if (e.getSource() instanceof JTableHeader)
                 {   // Always
                     JTableHeader tableHeader = (JTableHeader)e.getSource();
                     TableColumnModel columnModel = tableHeader.getColumnModel();
                     int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
                     int column = tableHeader.getTable().convertColumnIndexToModel(viewColumn); 
                     if(e.getClickCount() == 1 && column != -1)
                     {
                         boolean order = Constants.ASCENDING;
                         if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0)
                             order = !order;
                         if (!(tableHeader.getDefaultRenderer() instanceof SortableHeaderRenderer))
                             tableHeader.setDefaultRenderer(new SortableHeaderRenderer(tableHeader.getDefaultRenderer()));   // Set up header renderer the first time
                         if ((((SortableHeaderRenderer)tableHeader.getDefaultRenderer()).getSortedByColumn() == viewColumn)
                                 && (((SortableHeaderRenderer)tableHeader.getDefaultRenderer()).getSortedOrder() == order))
                                     order = !order;
                         column = columnToFieldColumn(column);
                         boolean bSuccess = sortByColumn(column, order);
                         if (bSuccess)
                             setSortedByColumn(tableHeader, viewColumn, order);
                     }
                 }
              }
          };
         table.getTableHeader().addMouseListener(mouseListener);
     }
     /**
      * Convert the table column to the 1 based field column
      * @param iViewColumn
      * @return The field column.
      */
     public int columnToFieldColumn(int iViewColumn)
     {
         return iViewColumn;     // Override this to do something
     }
     /**
      * Convert the 1 based field column to the table column
      * @param iFieldColumn
      * @return The field column.
      */
     public int columnToViewColumn(int iFieldColumn)
     {   // Note: this is overidden in GridTable Model
         return iFieldColumn;     // Override this to do something
     }
     /**
      * Change the tableheader to display this sort column and order.
      */
     public void setSortedByColumn(JTableHeader tableHeader, int iViewColumn, boolean bOrder)
     {
         if (!(tableHeader.getDefaultRenderer() instanceof SortableHeaderRenderer))
             tableHeader.setDefaultRenderer(new SortableHeaderRenderer(tableHeader.getDefaultRenderer()));   // Set up header renderer the first time
         ((SortableHeaderRenderer)tableHeader.getDefaultRenderer()).setSortedByColumn(tableHeader, iViewColumn, bOrder);
     }
     /**
      * Sort this table by this column.
      * Override this to sort by column.
      * @param iColumn The column to sort by.
      * @param bAscending True if ascending.
      * @return true if successful.
      */
     public boolean sortByColumn(int iColumn, boolean bOrder)
     {
         return false;   // For now
     }
     /**
      * Get the current row.
      * @return The current row.
      */
     public int getCurrentRow()
     {
         return m_iCurrentRowIndex;
     }
     /**
      * Set the current row.
      * @param iCurrentRowIndex The current row.
      */
     public void setCurrentRow(int iCurrentRowIndex)
     {
         m_iCurrentRowIndex = iCurrentRowIndex;
     }
     /**
      * Get the current row.
      * @return The current row.
      */
     public int getLockedRow()
     {
         return m_iCurrentLockedRowIndex;
     }
     /**
      * The underlying table has increased in size, change the model to access these extra record(s).
      * Note: This is not implemented correctly.
      * @param iNewSize The new table size.
      */
     public void bumpTableSize(int iBumpIncrement, boolean bInsertRowsInModel)
     {
         int iRowCount = this.setRowCount(this.getRowCount(false) + iBumpIncrement);          // Theoretical EOF value for table scroller (Actual when known).
         if (m_iLastRecord != RECORD_UNKNOWN)
             m_iLastRecord = iRowCount - 1;     // Sync
         if (bInsertRowsInModel)
             this.fireTableRowsInserted(this.getRowCount(true) - iBumpIncrement, this.getRowCount(true) - 1);
     }
     /**
      * User selected a new row.
      * From the ListSelectionListener interface.
      * @param source The source.
      * @param iStartRow The starting row.
      * @param iEndRow The ending row..
      * @param iSelectType The type of selection.
      * @return boolean If the selection changed, return true
      */
     public boolean selectionChanged(Object source, int iStartRow, int iEndRow, int iSelectType)
     {
         int iOldSelection = m_iSelectedRow;
         if (m_iSelectedRow == iEndRow)
             m_iSelectedRow = iStartRow;
         else
             m_iSelectedRow = iEndRow;
         
         if (source instanceof JTable)
             m_iSelectedRow = ((JTable)source).getSelectedRow();
         
         try {
             this.updateIfNewRow(m_iSelectedRow);
         } catch (Exception ex)  {
             ex.printStackTrace();
             if (source instanceof JTable)
             {   // Always
                 org.jbundle.thin.base.screen.BaseApplet baseApplet = null;
                 Container panel = (JTable)source;
                 while ((panel = panel.getParent()) != null)
                 {
                     if (panel instanceof org.jbundle.thin.base.screen.JBasePanel)
                     {
                         baseApplet = ((org.jbundle.thin.base.screen.JBasePanel)panel).getBaseApplet();
                         break;
                     }
                 }
                 if (baseApplet != null)
                 {
                     baseApplet.setLastError(ex.getMessage());
                     baseApplet.setStatusText(ex.getMessage());
                 }
             }
         }
         return (iOldSelection != m_iSelectedRow);
     }
     /**
      * This class notifies the model of selection changes.
      */
     class ThinTableModelListener extends Object
         implements ListSelectionListener
     {
         /**
          * The table for this listener.
          */
         protected JTable m_table = null;
         /**
          * The model for this listener.
          */
         protected AbstractThinTableModel m_model = null;
 
         /**
          * Constructor.
          * @param table The table for this listener.
          * @param model The model for this listener.
          */
         public ThinTableModelListener(JTable table, AbstractThinTableModel model)
         {
             super();
             m_table = table;
             m_model = model;
         }
         /**
          * User selected a new row.
          * From the ListSelectionListener interface.
          * Notify the model of the change by calling selectionChanged().
          * @param evt The ListSelectionEvent.
          */
         public void valueChanged(ListSelectionEvent evt)
         {
             if (!evt.getValueIsAdjusting())
             {
                 int iStartRow = evt.getFirstIndex();
                 int iEndRow = evt.getLastIndex();
                 m_model.selectionChanged(m_table, iStartRow, iEndRow, 1); // Selected row
             }
         }
     }
 }
