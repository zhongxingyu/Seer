  /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
  import org.apache.commons.logging.Log;
  import org.apache.commons.logging.LogFactory;
  import org.wings.event.*;
  import org.wings.plaf.TableCG;
  import org.wings.style.*;
  import org.wings.table.*;
  import org.wings.sdnd.TextAndHTMLTransferable;
  import org.wings.sdnd.CustomDragHandler;
  import org.wings.sdnd.SDropMode;
 
  import javax.swing.event.*;
  import javax.swing.table.DefaultTableModel;
  import javax.swing.table.TableModel;
  import javax.swing.*;
  import java.awt.*;
  import java.awt.datatransfer.Transferable;
  import java.util.*;
 import java.util.List;
 
 /**
  * Displays information contained in a {@link TableModel} object.
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  */
 public class STable extends SComponent
         implements TableModelListener, Scrollable, CellEditorListener, LowLevelEventListener {
 
     /**
      * Apache jakarta commons logger
      */
     private final static Log log = LogFactory.getLog(STable.class);
 
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int NO_SELECTION = SListSelectionModel.NO_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int SINGLE_SELECTION = SListSelectionModel.SINGLE_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int SINGLE_INTERVAL_SELECTION = SListSelectionModel.SINGLE_INTERVAL_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int MULTIPLE_SELECTION = SListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
     /**
      * Table selection model. See {@link STable#setSelectionMode(int)}
      */
     public static final int MULTIPLE_INTERVAL_SELECTION = SListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
 
 
     /**
      * <p>the table model.</p>
      */
     protected TableModel model;
 
     /**
      * <p>the selection model.</p>
      */
     protected SListSelectionModel selectionModel;
 
     /**
      * <p>The default renderer is used if no other renderer is set for the
      * content of a cell.</p>
      */
     protected STableCellRenderer defaultRenderer;
 
     /**
      * <p>The <code>headerRenderer</code> is used to render the header line.</p>
      */
     protected STableCellRenderer headerRenderer;
 
     /**
      * <p>A special cell renderer, that displays the control used to select
      * a table row.</p><p>Ususally, this would be some checkbox. The plaf is the
      * last instance to decide this.</p>
      */
     protected STableCellRenderer rowSelectionRenderer;
 
     /**
      * <p>In this <code>Map</code>, the renderers for the different
      * classes of cell content are stored.</p><p>The class is treated
      * as key, the renderer as the value.</p>
      */
     protected final HashMap renderer = new HashMap();
 
     /**
      * If this table is editable, clicks on table cells will be catched and interpreted as
      * editor calls. Otherwise they may result in a selection event if {@link #isSelectable()}
      */
     protected boolean editable = true;
 
     /**
      * If this table is marked as selectable, clicks on non-editable table cells will
      * be catched and interpreted as selection calls.
      */
     protected boolean selectable = true;
 
 
     /**
      * <p>If editing, this is the <code>SComponent</code> that is handling the editing.
      */
     transient protected SComponent editorComp;
 
     /**
      * <p>The object that overwrites the screen real estate occupied by the
      * current cell and allows the user to change those contents.</p>
      */
     transient protected STableCellEditor cellEditor;
 
     transient protected LowLevelEventListener cellEditorComponent;
     /**
      * <p>Identifies the column of the cell being edited.</p>
      */
     transient protected int editingColumn = -1;
 
     /**
      * <p>Identifies the row of the cell being edited.</p>
      */
     transient protected int editingRow = -1;
 
     /**
      * <p>In this <code>Map</code>, the <code>STableCellEditor</code>s for the different
      * classes of cell content are stored.</p><p>The class is treated
      * as key, the <code>STableCellEditor</code> as the value.</p>
      */
     protected final HashMap editors = new HashMap();
 
     /**
      * <p>Determines whether the header is visible or not.</p><p>By
      * default the header is visible.</p> <p><em>CAVEAT:</em>The
      * header is not (yet) implemented like in Swing. But maybe
      * someday.  So you can disable it if you like.</p>
      */
     protected boolean headerVisible = true;
 
     /**
      * <p>Determines if horizontal lines in the table should be
      * painted.</p><p>This is off by default.</p>
      */
     protected boolean showHorizontalLines = false;
 
     /**
      * <p>Determines if vertical lines in the table should be
      * painted.</p><p>This is off by default.</p>
      */
     protected boolean showVerticalLines = false;
 
     protected SDimension intercellSpacing;
 
     protected SDimension intercellPadding = new SDimension("1", "1");
 
     /**
      * Implementation of the {@link Scrollable} interface.
      */
     protected Rectangle viewport;
 
     /**
      * Used to detect if the number of rows changed in
      * which case we might have to update the viewport.
      */
     private int rowCountBackUp;
 
     /**
      * Used to detect if the number of columns changed in
      * which case we might have to update the viewport.
      */
     private int columnCountBackUp;
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     protected boolean epochCheckEnabled = true;
 
     /**
      * The column model holds state information about the columns of the table.
      */
     protected STableColumnModel columnModel;
 
     transient protected STableColumnModelListener tableColumnModelListener;
 
     /**
      * If true, the column model is autorebuild from the table model.
      */
     private boolean autoCreateColumnsFromModel;
 
     /**
      * A Pseudo CSS selector addressing the header row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_HEADER = new Selector("HEADER");
 
     /**
      * A Pseudo CSS selector addressing the selected row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_SELECTED = new Selector("SELECTED");
 
     /**
      * A Pseudo CSS selector addressing the regular odd row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_ODD_ROWS = new Selector("ODD_ROWS");
 
     /**
      * A Pseudo CSS selector addressing the regular even row elements.
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_EVEN_ROWS = new Selector("EVEN_ROWS");
 
     /**
      * A Pseudo CSS selector addressing the cell.
      * Can be applied to the table globally and gets overridden, if applied to cell renderer components
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_CELL = new Selector("CELL");
 
     /**
      * The last low level event values this table received.
      */
     private String[] lastReceivedLowLevelEvents;
 
     /**
      * Helper variable for {@link #nameRendererComponent(SComponent, int, int)}
      */
     private StringBuilder nameBuffer = new StringBuilder();
 
     /**
      * changes in the selection model should force a reload if possible
      */
     protected final ListSelectionListener fwdSelectionEvents = new ListSelectionListener() {
 
         List<Integer> deselectedIndices;
         List<Integer> selectedIndices;
 
         public void valueChanged(ListSelectionEvent e) {
             if (isUpdatePossible() && STable.class.isAssignableFrom(STable.this.getClass())) {
                 deselectedIndices = new ArrayList<Integer>();
                 selectedIndices = new ArrayList<Integer>();
                 if (getSelectionMode() == SINGLE_SELECTION) {
                     addIndex(e.getFirstIndex());
                     addIndex(e.getLastIndex());
                 } else {
                     for (int index = e.getFirstIndex(); index <= e.getLastIndex(); ++index) {
                         addIndex(index);
                     }
                 }
                 update(((TableCG) getCG()).getSelectionUpdate(STable.this, deselectedIndices, selectedIndices));
             } else {
                 reload();
             }
         }
 
         private void addIndex(int index) {
             int visibleIndex = index;
             if (getViewportSize() != null) {
                 visibleIndex = index - getViewportSize().y;
                 if (visibleIndex < 0 || visibleIndex >= getViewportSize().height)
                     return;
             }
             if (isRowSelected(index)) {
                 selectedIndices.add(new Integer(visibleIndex));
             } else {
                 deselectedIndices.add(new Integer(visibleIndex));
             }
         }
     };
 
     /**
         * <p>
         * Creates a new <code>STable</code>.
         * </p>
         */
     public STable() {
         this(null);
     }
 
     /**
      * <p>Creates a new <code>STable</code>.</p>
      *
      * @param tm the <code>TableModel</code> for the table's contents.
      */
     public STable(TableModel tm) {
         this(tm, null);
     }
 
     /**
      * <p>Creates a new <code>STable</code>.</p>
      *
      * @param model the <code>TableModel</code> for the table's contents. May be <code>null</code>.
      * @param columnModel The column model. Implicitly created and maintained if <code>null</code>.
      */
     public STable(TableModel model, STableColumnModel columnModel) {
         setSelectionModel(new SDefaultListSelectionModel());
         createDefaultEditors();
 
         if (columnModel == null) {
             // no issue if model == null!
             columnModel = createDefaultColumnModel();
             autoCreateColumnsFromModel = true;
         }
         setColumnModel(columnModel);
 
         if (model == null)
             model = createDefaultDataModel();
         setModel(model); // the resulting tableChanged event will update the default column mdoel
 
         installTransferHandler();
         createActionMap();
     }
 
     /**
      * <p>Sets the model of the table.</p>
      *
      * @param tm the <code>TableModel</code> to set.
      */
     public void setModel(TableModel tm) {
         if (tm == null)
             throw new IllegalArgumentException("Cannot set a null TableModel");
 
       TableModel oldVal = this.model;
         if (this.model != tm) {
             if (model != null)
                 model.removeTableModelListener(this);
 
             model = tm;
             model.addTableModelListener(this);
 
             tableChanged(new TableModelEvent(tm, TableModelEvent.HEADER_ROW));
         }
         propertyChangeSupport.firePropertyChange("model", oldVal, this.model);
     }
 
     /**
      * <p>returns the model of the table</p>
      */
     public TableModel getModel() {
         return model;
     }
 
     public int getColumnCount() {
         return model.getColumnCount();
     }
 
     public int getVisibleColumnCount() {
         if (columnModel != null) {
            int columnCount = 0;
             for (Iterator iterator = columnModel.getColumns().iterator(); iterator.hasNext();) {
                 STableColumn tableColumn = (STableColumn) iterator.next();
                 if (!tableColumn.isHidden())
                     columnCount++;
             }
             return columnCount;
         }
         else
             return model.getColumnCount();
     }
 
     public String getColumnName(int col) {
         return model.getColumnName(convertColumnIndexToModel(col));
     }
 
     public Class getColumnClass(int col) {
         return model.getColumnClass(convertColumnIndexToModel(col));
     }
 
     /**
      * Convienece method / Swing compatiblity to <code>model.getRowCount()</code>
      * @return numer of rows inside the table model
      */
     public int getRowCount() {
         return model.getRowCount();
     }
 
     /**
      * Define an optional CSS class which should be applied additionally to the passed row num.
      * Override this method, if you want to give rows different attributes.
      * E.g. for displaying an alternating background color for rows.
      *
      * @return the style of a specific row number.
      */
     public String getRowStyle(int row) {
         return null;
     }
 
     public Object getValueAt(int row, int column) {
         return model.getValueAt(row, convertColumnIndexToModel(column));
     }
 
     public void setValueAt(Object v, int row, int column) {
         Object oldVal = model.getValueAt(row, column);
         model.setValueAt(v, row, convertColumnIndexToModel(column));
         propertyChangeSupport.firePropertyChange("valueAt", oldVal, model.getValueAt(row, column));
     }
 
     public int convertColumnIndexToModel(int viewColumnIndex) {
         if (viewColumnIndex < 0) {
             return viewColumnIndex;
         }
         return getColumnModel().getColumn(viewColumnIndex).getModelIndex();
     }
 
     /**
      * Maps the index of the column in the table model at
      * <code>modelColumnIndex</code> to the index of the column
      * in the view.  Returns the index of the
      * corresponding column in the view; returns -1 if this column is not
      * being displayed.  If <code>modelColumnIndex</code> is less than zero,
      * returns <code>modelColumnIndex</code>.
      *
      * @param   modelColumnIndex     the index of the column in the model
      * @return   the index of the corresponding column in the view
      *
      * @see #convertColumnIndexToModel
      */
     public int convertColumnIndexToView(int modelColumnIndex) {
         if (modelColumnIndex < 0) {
             return modelColumnIndex;
         }
         STableColumnModel cm = getColumnModel();
         int count = cm.getColumnCount();
         for (int column = 0; column < count; column++) {
             if (cm.getColumn(column).getModelIndex() == modelColumnIndex) {
                 return column;
             }
         }
         return -1;
     }
 
     /**
      * Adds the row from <i>index0</i> to <i>index0</i> inclusive to the current selection.
      */
     public void addRowSelectionInterval(int index0, int index1) {
         selectionModel.addSelectionInterval(index0, index1);
     }
 
     /**
      * Sets the container, this component resides in.
      *
      * @param p the container, this component resides in.
      */
     public void setParent(SContainer p) {
         super.setParent(p);
 
         if (getCellRendererPane() != null) {
             getCellRendererPane().setParent(p);
         }
 
         if (editorComp != null) {
             editorComp.setParent(p);
         }
     }
 
     /**
      * Sets the frame in which this component resides.
      *
      * @param f the frame in which this component resides.
      */
     protected void setParentFrame(SFrame f) {
         super.setParentFrame(f);
         if (getCellRendererPane() != null) {
             getCellRendererPane().setParentFrame(f);
         }
     }
 
     // check fireIntermediateEvents !
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
         if (action.endsWith("_keystroke"))
             return;
 
         if (isEditing() && action.indexOf("_e_") != -1 && cellEditorComponent != null) {
             cellEditorComponent.processLowLevelEvent(action, values);
         }
         else {
             if (this.lastReceivedLowLevelEvents == null) {
                 this.lastReceivedLowLevelEvents = values;
             } else if (values != null && values.length > 0) {
                 // more than one parameter targets the table. collecting parameter values
                 String[] joinedParameters = new String[this.lastReceivedLowLevelEvents.length + values.length];
                 System.arraycopy(this.lastReceivedLowLevelEvents, 0, joinedParameters, 0, this.lastReceivedLowLevelEvents.length);
                 System.arraycopy(values, 0, joinedParameters, this.lastReceivedLowLevelEvents.length, values.length);
                 this.lastReceivedLowLevelEvents = joinedParameters;
             }
         }
 
         SForm.addArmedComponent(this);
     }
 
 
     private SCellRendererPane cellRendererPane = new SCellRendererPane();
 
     public SCellRendererPane getCellRendererPane() {
         return cellRendererPane;
     }
 
     /**
      * Sets <p>The default renderer is used if no other renderer is set for the
      * content of a cell.</p>
      *
      * @param r <p>The default renderer is used if no other renderer is set for the
      */
     public void setDefaultRenderer(STableCellRenderer r) {
         STableCellRenderer oldVal = this.defaultRenderer;
         defaultRenderer = r;
         propertyChangeSupport.firePropertyChange("defaultRenderer", oldVal, this.defaultRenderer);
     }
 
     /**
      * Returns <p>The default renderer is used if no other renderer is set for the
      * content of a cell.</p>
      *
      * @return <p>The default renderer is used if no other renderer is set for the
      */
     public STableCellRenderer getDefaultRenderer() {
         return defaultRenderer;
     }
 
     public void setDefaultRenderer(Class columnClass, STableCellRenderer r) {
         if (renderer != null) {
             Object oldVal = renderer.get(columnClass);
             renderer.remove(columnClass);
             renderer.put(columnClass, r);
             propertyChangeSupport.firePropertyChange("defaultRenderer", oldVal, this.renderer.get(columnClass));
         }
     }
 
     public STableCellRenderer getDefaultRenderer(Class columnClass) {
         if (columnClass == null) {
             return defaultRenderer;
         } else {
             Object r = renderer.get(columnClass);
             if (r != null) {
                 return (STableCellRenderer) r;
             } else {
                 return getDefaultRenderer(columnClass.getSuperclass());
             }
         }
     }
 
     /**
      * The renderer component responsible for rendering the table's header cell.
      * @param headerCellRenderer
      */
     public void setHeaderRenderer(STableCellRenderer headerCellRenderer) {
         STableCellRenderer oldVal = this.headerRenderer;
         headerRenderer = headerCellRenderer;
         propertyChangeSupport.firePropertyChange("headerRenderer", oldVal, this.headerRenderer);
     }
 
     /**
      * The renderer component responsible for rendering the table's header cell.
      * @return The renderer component for the header row
      */
     public STableCellRenderer getHeaderRenderer() {
         return headerRenderer;
     }
 
     /**
      * The cell renderer used to render a special selection column needed in cases clicks on table
      * cell cannot be distinguished as 'edit' or 'selection' click.
      * @return The table cell renderer used to render the selection column, or <code>null</code>
      * if no selection row should be rendered.
      */
     public STableCellRenderer getRowSelectionRenderer() {
         return rowSelectionRenderer;
     }
 
     /**
      * The cell renderer used to render a special selection column needed in cases clicks on table
      * cell cannot be distinguished as 'edit' or 'selection' click.
      * @param rowSelectionRenderer The table cell renderer used to render the selection column.
      *  Set this to <code>null</code> if you don't want to have a selection row in any case
      */
     public void setRowSelectionRenderer(STableCellRenderer rowSelectionRenderer) {
         STableCellRenderer oldVal = this.rowSelectionRenderer;
         this.rowSelectionRenderer = rowSelectionRenderer;
         propertyChangeSupport.firePropertyChange("rowSelectionRenderer", oldVal, this.rowSelectionRenderer);
     }
 
     /**
      * Returns the cell renderer for the given table cell.
      * @param row Table row
      * @param col Table column
      * @return The cell renderer for the given table cell.
      */
     public STableCellRenderer getCellRenderer( int row, int col ) {
         STableColumnModel columnModel = getColumnModel();
         if (columnModel != null) {
             STableColumn column = columnModel.getColumn(col);
             if (column != null) {
                 STableCellRenderer renderer = column.getCellRenderer();
                 if (renderer != null)
                     return renderer;
             }
         }
         return getDefaultRenderer(getColumnClass(col));
     }
 
     /**
      * Returns the header renderer for the given header cell.
      * @param col Table column
      * @return The header renderer for the given header cell.
      */
     public STableCellRenderer getHeaderRenderer( int col ) {
         STableColumnModel columnModel = getColumnModel();
         if ( columnModel != null) {
             STableColumn column = columnModel.getColumn(col);
             if (column != null) {
                 STableCellRenderer renderer = column.getHeaderRenderer();
                 if (renderer != null)
                    return renderer;
             }
         }
         return getHeaderRenderer();
     }
 
     public SComponent prepareRenderer(STableCellRenderer r, int row, int col) {
         final SComponent tableCellRendererComponent = r.getTableCellRendererComponent(this,
                 getValueAt(row, col),
                 isRowSelected(row),
                 row, col);
         nameRendererComponent(tableCellRendererComponent, row, col);
         return tableCellRendererComponent;
     }
 
     /**
      * Generates the name (= id) of the editing component so that
      * the STable implementation knows to associate the input
      * value with the correct data row/columns
      *
      * Applies the unqique id/name for a component of the rows/column
      * to the cell component.
      *
      * @param component The edit component to rename
      * @param row Data row of this edit component
      * @param col Data column of this edit component
      */
     protected void nameRendererComponent(final SComponent component, final int row, final int col) {
         nameBuffer.setLength(0);
         nameBuffer.append(this.getName()).append('_');
         if (row == -1)
             nameBuffer.append('h');
         else
             nameBuffer.append(row);
         nameBuffer.append('_').append(col);
         component.setNameRaw(nameBuffer.toString());
     }
 
     /**
      * Prepares and returns the renderer to render the column header
      * @param col Column number to render. Starts with <code>0</code>. May be <code>-1</code> for row selection column.
      * @return The renderer to render the column header
      */
     public SComponent prepareHeaderRenderer(STableCellRenderer headerRenderer, int col) {
         Object headerValue;
         if (col < 0) {
             headerValue = null;
         } else if (getColumnModel() != null && getColumnModel().getColumn(col) != null) {
             headerValue = getColumnModel().getColumn(col).getHeaderValue();
         } else {
             headerValue = model.getColumnName(col);
         }
         return headerRenderer.getTableCellRendererComponent(this, headerValue, false, -1, col);
     }
 
     /**
      * If this table is editable, clicks on table cells will be catched and interpreted as
      * editor calls. Otherwise they may result in a selection event if {@link #isSelectable()}
      * <p>Defaults to <code>true</code>
      *
      * @return <code>true</code> if clicks on editable cell should trigger the cell editor,
      * <code>false</code> if never.
      */
     public boolean isEditable() {
         return editable;
     }
 
     /**
      * If this table is editable, clicks on table cells will be catched
      * as {@link org.wings.event.SMouseEvent}s and interpreted as
      * editor calls. Otherwise they may result in a selection event if {@link #isSelectable()}.
      * <p>Defaults to <code>true</code>
      *
      * @param editable <code>true</code> if clicks on editable cell should trigger the cell editor,
      * <code>false</code> if never.
      * @see #isEditable()
      * @see #processLowLevelEvent(String, String[])
      * @see #fireIntermediateEvents()
      */
     public void setEditable(boolean editable) {
         boolean oldVal = this.editable;
         reloadIfChange(this.editable, editable);
         this.editable = editable;
         propertyChangeSupport.firePropertyChange("editable", oldVal, this.editable);
     }
 
     /**
      * If this table is marked as selectable, clicks on a non-editable table cells
      * {@link #setEditable(boolean)} must be <code>false</code>) will be catched
      * as {@link org.wings.event.SMouseEvent}s and interpreted as selection calls.
      * <p>Defaults to <code>true</code>
      *
      * @return <code>true</code> if table cell should catch clicks on non-editable tables.
      */
     public boolean isSelectable() {
         return selectable;
     }
 
     /**
      * If this table is marked as selectable, clicks on a non-editable table cells
      * {@link #setEditable(boolean)} must be <code>false</code>) will be catched
      * as {@link org.wings.event.SMouseEvent}s and interpreted as selection calls.
      * <p>Defaults to <code>true</code>
      *
      * @param selectable <code>true</code> if table cell should catch clicks on non-editable tables.
      * @see #isEditable()
      * @see #processLowLevelEvent(String, String[])
      * @see #fireIntermediateEvents()
      */
     public void setSelectable(boolean selectable) {
         boolean oldVal = this.selectable;
         reloadIfChange(this.selectable, selectable);
         this.selectable = selectable;
         propertyChangeSupport.firePropertyChange("selectable", oldVal, this.selectable);
     }
 
     /**
      * Set a default editor to be used if no editor has been set in
      * a TableColumn. If no editing is required in a table, or a
      * particular column in a table, use the isCellEditable()
      * method in the TableModel interface to ensure that the
      * STable will not start an editor in these columns.
      * If editor is null, remove the default editor for this
      * column class.
      *
      * @see TableModel#isCellEditable
      * @see #getDefaultEditor
      * @see #setDefaultRenderer
      */
     public void setDefaultEditor(Class columnClass, STableCellEditor r) {
         if (editors != null) {
             Object oldVal = this.editors.get(columnClass);
             editors.remove(columnClass);
             editors.put(columnClass, r);
             propertyChangeSupport.firePropertyChange("defaultEditor", oldVal, this.editors.get(columnClass));
         }
     }
 
     /*
     * Returns the editor to be used when no editor has been set in
     * a TableColumn. During the editing of cells the editor is fetched from
     * a Map of entries according to the class of the cells in the column. If
     * there is no entry for this <I>columnClass</I> the method returns
     * the entry for the most specific superclass. The STable installs entries
     * for <I>Object</I>, <I>Number</I> and <I>Boolean</I> all which can be modified
     * or replaced.
     *
     * @see     #setDefaultEditor
     * @see     #getColumnClass
     */
     public STableCellEditor getDefaultEditor(Class columnClass) {
         if (columnClass == null) {
             return null;
         } else {
             Object r = editors.get(columnClass);
             if (r != null) {
                 return (STableCellEditor) r;
             } else {
                 return getDefaultEditor(columnClass.getSuperclass());
             }
         }
     }
 
     //
     // Editing Support
     //
 
     /**
      * Programmatically starts editing the cell at <I>row</I> and
      * <I>column</I>, if the cell is editable.
      *
      * @param row    the row to be edited
      * @param column the column to be edited
      * @return false if for any reason the cell cannot be edited.
      * @throws IllegalArgumentException If <I>row</I> or <I>column</I>
      *                                  are not in the valid range
      */
     public boolean editCellAt(int row, int column) {
         return editCellAt(row, column, null);
     }
 
     /**
      * Programmatically starts editing the cell at <I>row</I> and
      * <I>column</I>, if the cell is editable.
      * To prevent the STable from editing a particular table, column or
      * cell value, return false from the isCellEditable() method in the
      * TableModel interface.
      *
      * @param row    the row to be edited
      * @param column the column to be edited
      * @param e      event to pass into
      *               shouldSelectCell
      * @return false if for any reason the cell cannot be edited.
      * @throws IllegalArgumentException If <I>row</I> or <I>column</I>
      *                                  are not in the valid range
      */
     public boolean editCellAt(int row, int column, EventObject e) {
         if (isEditing()) {
             // Try to stop the current editor
             if (cellEditor != null) {
                 boolean stopped = cellEditor.stopCellEditing();
                 if (!stopped)
                     return false;       // The current editor not resigning
             }
         }
 
         if (!isCellEditable(row, column))
             return false;
 
         STableCellEditor editor = getCellEditor(row, column);
         if (editor != null) {
             // set up editor environment and make it possible for the editor, to
             // stop/cancel editing on preparation
             editor.addCellEditorListener(this);
             setCellEditor(editor);
             setEditingRow(row);
             setEditingColumn(column);
 
             // prepare editor
             editorComp = prepareEditor(editor, row, column);
 
             if (editor.isCellEditable(e) && editor.shouldSelectCell(e)) {
                 update(((TableCG)getCG()).getEditCellUpdate(this, row, column));
                 return true;
             }
            else {
                setValueAt(editor.getCellEditorValue(), row, column);
                removeEditor();
            }
         }
         return false;
     }
 
     /**
      * Returns true if the cell at <I>row</I> and <I>column</I>
      * is editable.  Otherwise, setValueAt() on the cell will not change
      * the value of that cell.
      *
      * @param row the row whose value is to be looked up
      * @param col the column whose value is to be looked up
      * @return true if the cell is editable.
      * @see #setValueAt
      */
     public boolean isCellEditable(int row, int col) {
         if (col >= getColumnCount() || row == -1)
             return false;
         else
             return getModel().isCellEditable(row, convertColumnIndexToModel(col));
     }
 
     /**
      * Returns  true is the table is editing a cell.
      *
      * @return true is the table is editing a cell
      * @see #editingColumn
      * @see #editingRow
      */
     public boolean isEditing() {
         return (cellEditor != null);
     }
 
     /**
      * If the receiver is currently editing this will return the Component
      * that was returned from the CellEditor.
      *
      * @return SComponent handling editing session
      */
     public SComponent getEditorComponent() {
         return editorComp;
     }
 
     /**
      * This returns the index of the editing column.
      *
      * @return the index of the column being edited
      * @see #editingRow
      */
     public int getEditingColumn() {
         return editingColumn;
     }
 
     /**
      * Returns the index of the editing row.
      *
      * @return the index of the row being edited
      * @see #editingColumn
      */
     public int getEditingRow() {
         return editingRow;
     }
 
     /**
      * Return the cellEditor.
      *
      * @return the STableCellEditor that does the editing
      * @see #cellEditor
      */
     public STableCellEditor getCellEditor() {
         return cellEditor;
     }
 
     /**
      * Set the cellEditor variable.
      *
      * @param anEditor the STableCellEditor that does the editing
      * @see #cellEditor
      */
     protected void setCellEditor(STableCellEditor anEditor) {
         STableCellEditor oldVal = this.cellEditor;
         cellEditor = anEditor;
         propertyChangeSupport.firePropertyChange("cellEditor", oldVal, this.cellEditor);
     }
 
     /**
      * Set the editingColumn variable.
      *
      * @see #editingColumn
      */
     public void setEditingColumn(int editingColumn) {
         int oldVal = this.editingColumn;
         this.editingColumn = editingColumn;
         propertyChangeSupport.firePropertyChange("editingColumn", oldVal, this.editingColumn);
     }
 
     /**
      * Set the editingRow variable.
      *
      * @see #editingRow
      */
     public void setEditingRow(int editingRow) {
         int oldVal = this.editingRow;
         this.editingRow = editingRow;
         propertyChangeSupport.firePropertyChange("editingRow", oldVal, this.editingRow);
     }
 
     /**
      * Return an appropriate editor for the cell specified by this row and
      * column. If the TableColumn for this column has a non-null editor, return that.
      * If not, find the class of the data in this column (using getColumnClass())
      * and return the default editor for this type of data.
      *
      * @param row    the row of the cell to edit, where 0 is the first
      * @param col the column of the cell to edit, where 0 is the first
      */
     public STableCellEditor getCellEditor( int row, int col ) {
         STableColumnModel columnModel = getColumnModel();
         if (columnModel != null) {
             STableColumn column = columnModel.getColumn(col);
             if (column != null) {
                 STableCellEditor editor = column.getCellEditor();
                 if (editor != null)
                    return editor;
             }
         }
         return getDefaultEditor(getColumnClass(col));
     }
 
     /**
      * Prepares the specified editor using the value at the specified cell.
      *
      * @param editor the TableCellEditor to set up
      * @param row    the row of the cell to edit, where 0 is the first
      * @param col    the column of the cell to edit, where 0 is the first
      */
     protected SComponent prepareEditor(STableCellEditor editor, int row, int col) {
         SComponent component = editor.getTableCellEditorComponent(this,
                                                                   getValueAt(row, col),
                                                                   isRowSelected(row), // true?
                                                                   row, col);
         nameEditorComponent(component, row, col);
         cellEditorComponent = (component instanceof LowLevelEventListener) ? (LowLevelEventListener)component : null;
 
         return component;
     }
 
     /**
      * Generates the name (= id) of the editing component so that
      * the STable implementation knows to associate the input
      * value with the correct data row/columns
      *
      * Applies the unqique id/name for a component of the rows/column
      * to the cell component.
      *
      * @param component The edit component to rename
      * @param row Data row of this edit component
      * @param col Data column of this edit component
      */
     protected void nameEditorComponent(final SComponent component, final int row, final int col) {
         nameBuffer.setLength(0);
         nameBuffer.append(this.getName()).append("_e_");
         nameBuffer.append(row);
         nameBuffer.append('_').append(col);
         component.setNameRaw(nameBuffer.toString());
     }
 
     /**
      * Discard the editor object and return the real estate it used to
      * cell rendering.
      */
     public void removeEditor() {
         STableCellEditor editor = getCellEditor();
         if (editor != null) {
             editor.removeCellEditorListener(this);
             //remove(editorComp);
             setCellEditor(null);
             int oldEditingColumn = getEditingColumn();
             int oldEditingRow = getEditingRow();
             setEditingColumn(-1);
             setEditingRow(-1);
             if (editorComp != null) {
                 editorComp.setParent(null);
             } // end of if ()
             editorComp = null;
             update(((TableCG)getCG()).getRenderCellUpdate(this, oldEditingRow, oldEditingColumn));
         }
     }
 
 
     //
     // Implementing the CellEditorListener interface
     //
 
     /**
      * Invoked when editing is finished. The changes are saved and the
      * editor object is discarded.
      *
      * @see CellEditorListener
      */
     public void editingStopped(ChangeEvent e) {
         // Take in the new value
         STableCellEditor editor = getCellEditor();
         if (editor != null) {
             Object value = editor.getCellEditorValue();
             setValueAt(value, editingRow, editingColumn);
             removeEditor();
         }
     }
 
     /**
      * Invoked when editing is canceled. The editor object is discarded
      * and the cell is rendered once again.
      *
      * @see CellEditorListener
      */
     public void editingCanceled(ChangeEvent e) {
         removeEditor();
     }
 
     /**
      * Creates default cell editors for Objects, numbers, and boolean values.
      */
     protected void createDefaultEditors() {
         editors.clear();
 
         // Objects
         setDefaultEditor(Object.class, new SDefaultCellEditor(new STextField()));
         setDefaultEditor(Number.class, new SDefaultCellEditor(new STextField()));
 
         // Numbers
         //STextField rightAlignedTextField = new STextField();
         //rightAlignedTextField.setHorizontalAlignment(STextField.RIGHT);
         //setDefaultEditor(Number.class, new SDefaultCellEditor(rightAlignedTextField));
 
         // Booleans
         SCheckBox centeredCheckBox = new SCheckBox();
         //centeredCheckBox.setHorizontalAlignment(JCheckBox.CENTER);
         setDefaultEditor(Boolean.class, new SDefaultCellEditor(centeredCheckBox));
     }
 
 
     /**
      * Returns <p>the selection model.</p>
      *
      * @return <p>the selection model.</p>
      */
     public SListSelectionModel getSelectionModel() {
         return selectionModel;
     }
 
     /**
      * Sets the row selection model for this table to <code>model</code>.
      *
      * @param model the new selection model
      * @throws IllegalArgumentException if <code>model</code>
      *                                  is <code>null</code>
      * @see #getSelectionModel
      */
     public void setSelectionModel(SListSelectionModel model) {
         if (model == null) {
             throw new IllegalArgumentException("cannot set a null SListSelectionModel");
         }
 
         SListSelectionModel oldVal = this.selectionModel;    
         if (getSelectionModel() != null) {
             removeSelectionListener(fwdSelectionEvents);
         }
 
         selectionModel = model;
 
         addSelectionListener(fwdSelectionEvents);
         propertyChangeSupport.firePropertyChange("selectionModel", oldVal, this.selectionModel);
     }
 
 
     public int getSelectedRowCount() {
         SListSelectionModel selectionModel = getSelectionModel();
         int result = 0;
         for (int i = selectionModel.getMinSelectionIndex();
              i <= selectionModel.getMaxSelectionIndex(); i++) {
             if (selectionModel.isSelectedIndex(i))
                 result++;
         }
 
         return result;
     }
 
 
     public int getSelectedRow() {
         return getSelectionModel().getMinSelectionIndex();
     }
 
     public int[] getSelectedRows() {
         int[] result = new int[getSelectedRowCount()];
 
         SListSelectionModel selectionModel = getSelectionModel();
         int index = 0;
         for (int i = selectionModel.getMinSelectionIndex();
              i <= selectionModel.getMaxSelectionIndex(); i++) {
             if (selectionModel.isSelectedIndex(i))
                 result[index++] = i;
         }
 
         return result;
     }
 
     /**
      * Deselects all selected columns and rows.
      */
     public void clearSelection() {
         if (!getSelectionModel().isSelectionEmpty()) {
             getSelectionModel().clearSelection();
             reload();
         }
     }
 
 
     public boolean isRowSelected(int row) {
         return getSelectionModel().isSelectedIndex(row);
     }
 
     /**
      * Sets the selection mode. Use one of the following values:
      * <UL>
      * <LI> {@link #NO_SELECTION}
      * <LI> {@link javax.swing.ListSelectionModel#SINGLE_SELECTION} or
      * {@link #SINGLE_SELECTION}
      * <LI> {@link javax.swing.ListSelectionModel#SINGLE_INTERVAL_SELECTION} or
      * {@link #SINGLE_INTERVAL_SELECTION}
      * <LI> {@link javax.swing.ListSelectionModel#MULTIPLE_INTERVAL_SELECTION} or
      * {@link #MULTIPLE_SELECTION}
      * </UL>
      */
     public void setSelectionMode(int s) {
         int oldVal = getSelectionModel().getSelectionMode();
         reloadIfChange(getSelectionModel().getSelectionMode(), s);
         getSelectionModel().setSelectionMode(s);
         propertyChangeSupport.firePropertyChange("selectionMode", oldVal, getSelectionModel().getSelectionMode());
     }
 
     /**
      * @return <UL>
      *         <LI> {@link #NO_SELECTION}
      *         <LI> {@link javax.swing.ListSelectionModel#SINGLE_SELECTION} or
      *         {@link #SINGLE_SELECTION}
      *         <LI> {@link javax.swing.ListSelectionModel#SINGLE_INTERVAL_SELECTION} or
      *         {@link #SINGLE_INTERVAL_SELECTION}
      *         <LI> {@link javax.swing.ListSelectionModel#MULTIPLE_INTERVAL_SELECTION} or
      *         {@link #MULTIPLE_SELECTION}
      *         </UL>
      */
     public int getSelectionMode() {
         return getSelectionModel().getSelectionMode();
     }
 
     public void addSelectionListener(ListSelectionListener listener) {
         getSelectionModel().addListSelectionListener(listener);
     }
 
     public void removeSelectionListener(ListSelectionListener listener) {
         getSelectionModel().removeListSelectionListener(listener);
     }
 
     /**
      * Adds the specified mouse listener to receive mouse events from
      * this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the component listener.
      * @see org.wings.event.SMouseEvent
      * @see org.wings.event.SMouseListener
      * @see org.wings.STable#removeMouseListener
      */
     public final void addMouseListener(SMouseListener l) {
         addEventListener(SMouseListener.class, l);
     }
 
     /**
      * Removes the specified mouse listener so that it no longer
      * receives mouse events from this component. This method performs
      * no function, nor does it throw an exception, if the listener
      * specified by the argument was not previously added to this component.
      * If l is null, no exception is thrown and no action is performed.
      *
      * @param l the component listener.
      * @see org.wings.event.SMouseEvent
      * @see org.wings.event.SMouseListener
      * @see org.wings.STable#addMouseListener
      */
     public final void removeMouseListener(SMouseListener l) {
         removeEventListener(SMouseListener.class, l);
     }
 
     /**
      * Reports a mouse click event.
      *
      * @param event report this event to all listeners
      * @see org.wings.event.SMouseListener
      */
     protected void fireMouseClickedEvent(SMouseEvent event) {
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SMouseListener.class) {
                 ((SMouseListener)listeners[i + 1]).mouseClicked(event);
             }
         }
     }
 
     private int lastSelectedIndex;
 
     protected void addSelectionEvent(int row, boolean ctrlKey, boolean shiftKey) {
         if(getModel() != null && row >= getModel().getRowCount())
             return;
         
         if ( ctrlKey == true && shiftKey == false && getSelectionModel().isSelectedIndex(row)) {
             getSelectionModel().removeSelectionInterval(row, row);
             lastSelectedIndex = row;
         } else {
             if ( shiftKey == true ) {
                 getSelectionModel().setSelectionInterval( lastSelectedIndex, row );
             } else if ( ctrlKey == true ) {
                 getSelectionModel().addSelectionInterval( row, row );
                 lastSelectedIndex = row;
             } else {
                 getSelectionModel().setSelectionInterval(row, row);
                 lastSelectedIndex = row;
             }
         }
     }
 
     protected void addSelectionEvent(int row, int col, boolean ctrlKey, boolean shiftKey) {
         addSelectionEvent(row, ctrlKey, shiftKey); 
     }
 
     public void fireIntermediateEvents() {
         if (lastReceivedLowLevelEvents == null)
             return;
 
         // delay events...
         getSelectionModel().setDelayEvents(true);
         getSelectionModel().setValueIsAdjusting(true);
 
         for (int i = 0; i < lastReceivedLowLevelEvents.length; i++) {
             String value = lastReceivedLowLevelEvents[i];
             if (value.length() > 1) {
 
                 char modus = value.charAt(0);
                 value = value.substring(1);
                 if (value.indexOf(':') == -1)
                     continue;
 
                 String[] values = value.split(";");
                 value = values[0];
 
                 try {
                     SPoint point = new SPoint(value);
                     int row = rowAtPoint(point);
                     int col = columnAtPoint(point);
 
                     SMouseEvent event = new SMouseEvent(this, 0, point);
                     fireMouseClickedEvent(event);
                     if (event.isConsumed())
                         continue;
 
                     // editor event
                     switch (modus) {
                         case 'e':
                             editCellAt(row, col, null);
                             break;
                         case 't':
                             boolean shiftKey = false;
                             boolean ctrlKey = false;
                             for(String val:values) {
                                 String[] parts = val.split("=");
                                 if(parts.length != 2)
                                     continue;
                                 if("shiftKey".equals(parts[0]))
                                     shiftKey = Boolean.parseBoolean( parts[1] );
                                 else if("ctrlKey".equals(parts[0]))
                                     ctrlKey = Boolean.parseBoolean( parts[1] );
                             }
                             addSelectionEvent(row, col, ctrlKey, shiftKey);
                             break;
                         case 's':
                             getSelectionModel().addSelectionInterval(row, row);
                             lastSelectedIndex = row;
                             break;
                         case 'd':
                             getSelectionModel().removeSelectionInterval(row, row);
                             lastSelectedIndex = row;
                             break;
                     }
                 } catch (NumberFormatException ex) {
                     log.warn("Number format exception while parsing low level events", ex);
                 }
             }
         }
         lastReceivedLowLevelEvents = null;
 
         getSelectionModel().setValueIsAdjusting(false);
         getSelectionModel().setDelayEvents(false);
 
         getSelectionModel().fireDelayedIntermediateEvents();
     }
 
 
     /**
      * Returns the table row for the passed <code>SPoint</code>
      * instance received via {@link #addMouseListener(org.wings.event.SMouseListener)}.
      * @param point The pointed retuned by the mouse event.
      * @return The row index
      */
     public int rowAtPoint(SPoint point) {
         String coordinates = point.getCoordinates();
         int colonIndex = coordinates.indexOf(':');
         if (colonIndex < 0)
             return - 1;
         return Integer.parseInt(coordinates.substring(0, colonIndex));
     }
 
     /**
      * Returns the table column for the passed <code>SPoint</code>
      * instance received via {@link #addMouseListener(org.wings.event.SMouseListener)}.
      * @param point The pointed retuned by the mouse event.
      * @return The column index
      * @see #rowAtPoint(SPoint)
      */
     public int columnAtPoint(SPoint point) {
         String coordinates = point.getCoordinates();
         int colonIndex = coordinates.indexOf(':');
         if (colonIndex < 0)
             return - 1;
         return Integer.parseInt(coordinates.substring(colonIndex + 1));
     }
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
         // fire selection events...
         getSelectionModel().fireDelayedFinalEvents();
     }
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     public boolean isEpochCheckEnabled() {
         return epochCheckEnabled;
     }
 
     /**
      * @see LowLevelEventListener#isEpochCheckEnabled()
      */
     public void setEpochCheckEnabled(boolean epochCheckEnabled) {
         boolean oldVal = this.epochCheckEnabled;
         this.epochCheckEnabled = epochCheckEnabled;
         propertyChangeSupport.firePropertyChange("epochCheckEnabled", oldVal, this.epochCheckEnabled);
     }
 
     public void tableChanged(TableModelEvent e) {
         // kill active editors
         //editingCanceled(null);
 
         if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW) {
             // The whole thing changed
             clearSelection();
             if (getAutoCreateColumnsFromModel())
                 createDefaultColumnsFromModel();
         } else {
             switch (e.getType()) {
                 case TableModelEvent.INSERT:
                     if (e.getFirstRow() >= 0)
                         getSelectionModel().insertIndexInterval(e.getFirstRow(), e.getLastRow(), true);
                     break;
 
                 case TableModelEvent.DELETE:
                     if (e.getFirstRow() >= 0)
                         getSelectionModel().removeIndexInterval(e.getFirstRow(), e.getLastRow());
                     break;
                case TableModelEvent.UPDATE:
                    /* event fire on javax.swing.table.AbstractTableModel.fireTableDataChanged() */
                    if (e.getLastRow() == Integer.MAX_VALUE)
                        clearSelection();
                    break;
             }
         }
 
         if (model.getRowCount() != rowCountBackUp) {
             rowCountBackUp = model.getRowCount();
             fireViewportChanged(false);
         }
         if (columnModel.getColumnCount() != columnCountBackUp) {
             columnCountBackUp = columnModel.getColumnCount();
             fireViewportChanged(true);
         }
 
         if (e != null &&
             e.getFirstRow() == e.getLastRow() &&
             e.getFirstRow() != TableModelEvent.HEADER_ROW &&
             e.getColumn() != TableModelEvent.ALL_COLUMNS &&
             e.getType() == TableModelEvent.UPDATE) {
             if (isUpdatePossible() && STable.class.isAssignableFrom(getClass()))
                 update(((TableCG) getCG()).getRenderCellUpdate(this, e.getFirstRow(), e.getColumn()));
             else
                 reload();
         }
 /*        else if (e != null &&
             e.getFirstRow() == 0 &&
             e.getLastRow() == Integer.MAX_VALUE &&
             e.getColumn() == TableModelEvent.ALL_COLUMNS &&
             e.getType() == TableModelEvent.UPDATE) {
             update(((TableCG)getCG()).getDataUpdate(this));
         }*/
         else {
             reload();
         }
     }
 
     /**
      * Return the background color.
      *
      * @return the background color
      */
     public Color getSelectionBackground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTED) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTED));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setSelectionBackground(Color color) {
         Color oldVal = this.getSelectionBackground();
         setAttribute(SELECTOR_SELECTED, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("selectionBackground", oldVal, this.getSelectionBackground());
     }
 
     /**
      * Return the foreground color.
      *
      * @return the foreground color
      */
     public Color getSelectionForeground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTED) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTED));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setSelectionForeground(Color color) {
         Color oldVal = this.getSelectionForeground();
         setAttribute(SELECTOR_SELECTED, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("selectionForeground", oldVal, this.getSelectionForeground());
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     public void setSelectionFont(SFont font) {
         SFont oldVal = this.getSelectionFont();
         setAttributes(SELECTOR_SELECTED, CSSStyleSheet.getAttributes(font));
         propertyChangeSupport.firePropertyChange("selectionFont", oldVal, this.getSelectionFont());
     }
 
     /**
      * Return the font.
      *
      * @return the font
      */
     public SFont getSelectionFont() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_SELECTED) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(SELECTOR_SELECTED));
     }
 
     /**
      * Return the background color.
      *
      * @return the background color
      */
     public Color getHeaderBackground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_HEADER) == null ? null : CSSStyleSheet.getBackground((CSSAttributeSet) dynamicStyles.get(SELECTOR_HEADER));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setHeaderBackground(Color color) {
         Color oldVal = this.getHeaderBackground();
         setAttribute(SELECTOR_HEADER, CSSProperty.BACKGROUND_COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("headerBackground", oldVal, this.getHeaderBackground());
     }
 
     /**
      * Return the foreground color.
      *
      * @return the foreground color
      */
     public Color getHeaderForeground() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_HEADER) == null ? null : CSSStyleSheet.getForeground((CSSAttributeSet) dynamicStyles.get(SELECTOR_HEADER));
     }
 
     /**
      * Set the foreground color.
      *
      * @param color the new foreground color
      */
     public void setHeaderForeground(Color color) {
         Color oldVal = this.getHeaderForeground();
         setAttribute(SELECTOR_HEADER, CSSProperty.COLOR, CSSStyleSheet.getAttribute(color));
         propertyChangeSupport.firePropertyChange("headerForeground", oldVal, this.getHeaderForeground());
     }
 
     /**
      * Set the font.
      *
      * @param font the new font
      */
     public void setHeaderFont(SFont font) {
         SFont oldVal = this.getHeaderFont();
         setAttributes(SELECTOR_HEADER, CSSStyleSheet.getAttributes(font));
         propertyChangeSupport.firePropertyChange("headerFont", oldVal, this.getHeaderFont());
     }
 
     /**
      * Return the font.
      *
      * @return the font
      */
     public SFont getHeaderFont() {
         return dynamicStyles == null || dynamicStyles.get(SELECTOR_HEADER) == null ? null : CSSStyleSheet.getFont((CSSAttributeSet) dynamicStyles.get(SELECTOR_HEADER));
     }
 
     /**
      * Sets <p>Determines whether the header is visible or not.</p><p>By
      * default the header is visible.</p> <p><em>CAVEAT:</em>The
      * header is not (yet) implemented like in Swing. But maybe
      * someday.  So you can disable it if you like.</p>
      *
      * @param hv <p>Determines whether the header is visible or not.</p><p>By
      */
     public void setHeaderVisible(boolean hv) {
         boolean oldHeaderVisible = headerVisible;
         headerVisible = hv;
         if (oldHeaderVisible != headerVisible) {
             reload();
         }
         propertyChangeSupport.firePropertyChange("headerVisible", oldHeaderVisible, this.headerVisible);
     }
 
     /**
      * Returns <p>Determines whether the header is visible or not.</p><p>By
      * default the header is visible.</p> <p><em>CAVEAT:</em>The
      * header is not (yet) implemented like in Swing. But maybe
      * someday.  So you can disable it if you like.</p>
      *
      * @return <p>Determines whether the header is visible or not.</p><p>By
      */
     public boolean isHeaderVisible() {
         return headerVisible;
     }
 
     public void setShowGrid(boolean b) {
         setShowHorizontalLines(b);
         setShowVerticalLines(b);
     }
 
     /**
      * Sets <p>Determines if horizontal lines in the table should be
      * painted.</p><p>This is off by default.</p>
      *
      * @param b <p>Determines if horizontal lines in the table should be
      */
     public void setShowHorizontalLines(boolean b) {
         boolean oldShowHorizontalLines = showHorizontalLines;
         showHorizontalLines = b;
         if (showHorizontalLines != oldShowHorizontalLines) {
             reload();
         }
         propertyChangeSupport.firePropertyChange("showHorizontalLines", oldShowHorizontalLines, this.showHorizontalLines);
     }
 
     /**
      * Returns <p>Determines if horizontal lines in the table should be
      * painted.</p><p>This is off by default.</p>
      *
      * @return <p>Determines if horizontal lines in the table should be
      */
     public boolean getShowHorizontalLines() {
         return showHorizontalLines;
     }
 
     /**
      * Sets <p>Determines if vertical lines in the table should be
      * painted.</p><p>This is off by default.</p>
      *
      * @param b <p>Determines if vertical lines in the table should be
      */
     public void setShowVerticalLines(boolean b) {
         boolean oldShowVerticalLines = showVerticalLines;
         showVerticalLines = b;
         if (showVerticalLines != oldShowVerticalLines) {
             reload();
         }
         propertyChangeSupport.firePropertyChange("showVerticalLines", oldShowVerticalLines, this.showVerticalLines);
     }
 
     /**
      * Returns <p>Determines if vertical lines in the table should be
      * painted.</p><p>This is off by default.</p>
      *
      * @return <p>Determines if vertical lines in the table should be
      */
     public boolean getShowVerticalLines() {
         return showVerticalLines;
     }
 
     /*
     * Implementiert das cellspacing Attribut des HTML Tables. Da dieses
     * nur eindimensional ist, wird nur der width Wert der Dimension in
     * den HTML Code uebernommen.
     */
     public void setIntercellSpacing(SDimension d) {
         SDimension oldIntercellSpacing = intercellSpacing;
         intercellSpacing = d;
         if ((intercellSpacing == null && oldIntercellSpacing != null) ||
                 intercellSpacing != null && !intercellSpacing.equals(oldIntercellSpacing))
             reload();
         propertyChangeSupport.firePropertyChange("intercellSpacing", oldIntercellSpacing, this.intercellSpacing);
     }
 
     public SDimension getIntercellSpacing() {
         return intercellSpacing;
     }
 
     /*
     * Implementiert das cellpadding Attribut des HTML Tables. Da dieses
     * nur eindimensional ist, wird nur der width Wert der Dimension in
     * den HTML Code uebernommen.
     */
 
     public void setIntercellPadding(SDimension d) {
         SDimension oldIntercellPadding = intercellPadding;
         intercellPadding = d;
         if ((intercellPadding == null && oldIntercellPadding != null) ||
                 intercellPadding != null && !intercellPadding.equals(oldIntercellPadding))
             reload();
         propertyChangeSupport.firePropertyChange("intercellPadding", oldIntercellPadding, this.intercellPadding);
     }
 
     public SDimension getIntercellPadding() {
         return intercellPadding;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getEditParameter(int row, int col) {
         return "e" + row + ":" + col;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getToggleSelectionParameter(int row, int col) {
         return "t" + row + ":" + col;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getSelectionParameter(int row, int col) {
         return "s" + row + ":" + col;
     }
 
     /**
      * wingS internal method used to create specific HTTP request parameter names.
      */
     public String getDeselectionParameter(int row, int col) {
         return "d" + row + ":" + col;
     }
 
     /**
      * The size of the component in respect to scrollable units.
      */
     public Rectangle getScrollableViewportSize() {
         return new Rectangle(0, 0, getVisibleColumnCount(), getRowCount());
     }
 
     /**
      * Returns the actual visible part of a scrollable.
      */
     public Rectangle getViewportSize() {
         return viewport;
     }
 
     /**
      * Sets the actual visible part of a scrollable.
      */
     public void setViewportSize(Rectangle newViewport) {
         Rectangle oldViewport = viewport;
         viewport = newViewport;
 
         if (isDifferent(oldViewport, newViewport)) {
             if (oldViewport == null || newViewport == null) {
                 fireViewportChanged(true);
                 fireViewportChanged(false);
             } else {
                 if (newViewport.x != oldViewport.x || newViewport.width != oldViewport.width) {
                     fireViewportChanged(true);
                 }
                 if (newViewport.y != oldViewport.y || newViewport.height != oldViewport.height) {
                     fireViewportChanged(false);
                 }
             }
             update(((TableCG)getCG()).getTableScrollUpdate(this, newViewport, oldViewport));
         }
         propertyChangeSupport.firePropertyChange("viewportSize", oldViewport, this.viewport);
     }
 
     /**
      * Adds the given <code>SViewportChangeListener</code> to the scrollable.
      *
      * @param l the listener to be added
      */
     public void addViewportChangeListener(SViewportChangeListener l) {
         addEventListener(SViewportChangeListener.class, l);
     }
 
     /**
      * Removes the given <code>SViewportChangeListener</code> from the scrollable.
      *
      * @param l the listener to be removed
      */
     public void removeViewportChangeListener(SViewportChangeListener l) {
         removeEventListener(SViewportChangeListener.class, l);
     }
 
     /**
      * Notifies all listeners that have registered interest for notification
      * on changes to this scrollable's viewport in the specified direction.
      *
      * @see javax.swing.event.EventListenerList
      */
     protected void fireViewportChanged(boolean horizontal) {
         Object[] listeners = getListenerList();
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SViewportChangeListener.class) {
                 SViewportChangeEvent event = new SViewportChangeEvent(this, horizontal);
                 ((SViewportChangeListener) listeners[i + 1]).viewportChanged(event);
             }
         }
     }
 
     public void setSelectedRow(int selectedIndex) {
         int oldVal = getSelectedRow();
         getSelectionModel().setSelectionInterval(selectedIndex, selectedIndex);
         propertyChangeSupport.firePropertyChange("selectedRow", oldVal, getSelectedRow());
     }
 
 
     /**
      * Sets this table's <code>autoCreateColumnsFromModel</code> flag.
      * This method calls <code>createDefaultColumnsFromModel</code> if
      * <code>autoCreateColumnsFromModel</code> changes from false to true.
      *
      * @param   autoCreateColumnsFromModel   true if <code>JTable</code> should automatically create columns
      * @see     #getAutoCreateColumnsFromModel
      * @see     #createDefaultColumnsFromModel
      */
     public void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel) {
         if (this.autoCreateColumnsFromModel != autoCreateColumnsFromModel) {
             boolean oldVal = this.autoCreateColumnsFromModel;
             this.autoCreateColumnsFromModel = autoCreateColumnsFromModel;
             if (autoCreateColumnsFromModel) {
                 createDefaultColumnsFromModel();
             }
             propertyChangeSupport.firePropertyChange("autoCreateColumnsFromModel", oldVal, this.autoCreateColumnsFromModel);
         }
     }
 
     /**
      * Determines whether the table will create default columns from the model.
      * If true, <code>setModel</code> will clear any existing columns and
      * create new columns from the new model.  Also, if the event in
      * the <code>tableChanged</code> notification specifies that the
      * entire table changed, then the columns will be rebuilt.
      * The default is true.
      *
      * @return  the autoCreateColumnsFromModel of the table
      * @see     #setAutoCreateColumnsFromModel
      * @see     #createDefaultColumnsFromModel
      */
     public boolean getAutoCreateColumnsFromModel() {
         return autoCreateColumnsFromModel;
     }
 
     /**
      * Returns a <code>STableColumnModel</code> that contains information
      * about all columns  of this table.
      *
      * @return  the object that provides the column state of the table
      * @see     #setColumnModel
      */
     public STableColumnModel getColumnModel() {
         return columnModel;
     }
 
 
     /**
      * Sets the model holding information about the columns for this table.
      *
      * @param   newColumnModel        the new data source for this table
      * @see     #getColumnModel
      */
     public void setColumnModel(STableColumnModel newColumnModel) {
         if (newColumnModel == null)
             throw new IllegalArgumentException("Column model must not be null");
 
         STableColumnModel oldVal = this.columnModel;
         if (columnModel != newColumnModel) {
             if (tableColumnModelListener == null)
                 tableColumnModelListener = createTableColumnModelListener();
 
             if (columnModel != null)
                 columnModel.removeColumnModelListener(tableColumnModelListener);
             columnModel = newColumnModel;
             columnModel.addColumnModelListener(tableColumnModelListener);
 
             reload();
             propertyChangeSupport.firePropertyChange("columnModel", oldVal, this.columnModel);
         }
     }
 
     /**
      * Creates the default columns of the table from the table model.
      */
     public void createDefaultColumnsFromModel() {
         TableModel tm = getModel();
 
         if (tm != null) {
             STableColumnModel columnModel = getColumnModel();
             while (columnModel.getColumnCount() > 0)
                 columnModel.removeColumn(columnModel.getColumn(0));
 
             for ( int i = 0; i < tm.getColumnCount(); i++ ) {
                 STableColumn column = new STableColumn( i );
                 String columnName = tm.getColumnName( i );
                 column.setHeaderValue( columnName );
                 this.columnModel.addColumn( column );
             }
         }
     }
 
     /**
      * Returns the default column model object, which is
      * a <code>SDefaultTableColumnModel</code>.
      * A subclass can override this method to return a different column model object.
      *
      * @return the default column model object
      */
     protected STableColumnModel createDefaultColumnModel() {
         return new SDefaultTableColumnModel();
     }
 
     /**
      * Returns a default table model object.
      * Subclasses can override this method to return a different table model objects
      *
      * @return the default table model object
      * @see javax.swing.table.DefaultTableModel
      */
     protected TableModel createDefaultDataModel() {
         return new DefaultTableModel();
     }
 
     /**
      * Creates an instance of TableColumnModelHandler.
      */
     protected TableColumnModelHandler createTableColumnModelListener() {
         return new TableColumnModelHandler();
     }
 
     /**
      * Handler that listens to the table's column model.
      */
     protected class TableColumnModelHandler implements STableColumnModelListener {
 
         public void columnAdded(STableColumnModelEvent e) {
             removeEditor();
             fireViewportChanged(true);
             reload();
         }
 
         public void columnHidden(ChangeEvent e) {
             removeEditor();
             fireViewportChanged(true);
             reload();
         }
 
         public void columnMarginChanged(ChangeEvent e) {
             reload();
         }
 
         public void columnMoved(STableColumnModelEvent e) {
             removeEditor();
             reload();
         }
 
         public void columnRemoved(STableColumnModelEvent e) {
             removeEditor();
             fireViewportChanged(true);
             reload();
         }
 
         public void columnShown(ChangeEvent e) {
             removeEditor();
             fireViewportChanged(true);
             reload();
         }
     }
 
     /**
      * Drag and Drop stuff
      */
     private SDropMode dropMode = null;
     private boolean dragEnabled = false;
 
     protected void createActionMap() {
         ActionMap map = getActionMap();
 
         map.put(STransferHandler.getCutAction().getValue(Action.NAME), STransferHandler.getCutAction());
         map.put(STransferHandler.getCopyAction().getValue(Action.NAME), STransferHandler.getCopyAction());
         map.put(STransferHandler.getPasteAction().getValue(Action.NAME), STransferHandler.getPasteAction());
     }
 
     public static class DropLocation extends STransferHandler.DropLocation {
         private int row, col;
 
         public DropLocation(STable table, SPoint point) {
             super(point);
 
             try {
                 String[] vals = point.getCoordinates().split(":");
                 row = Integer.parseInt(vals[0]);
                 col = Integer.parseInt(vals[1]);
                 Rectangle viewport = table.getViewportSize();
                 if(viewport != null) {
                     row += viewport.y;
                     col += viewport.x;
                 }
             } catch(Exception e) {
                 row = -1;
                 col = -1;
             }
         }
 
         public int getRow() {
             return row;
         }
 
         public int getCol() {
             return col;
         }
     }
 
     public void setDropMode(SDropMode dropMode) {
         this.dropMode = dropMode;
 
         getSession().getSDragAndDropManager().addDropTarget(this);
     }
 
     public SDropMode getDropMode() {
         return this.dropMode;
     }
 
     protected DropLocation dropLocationForPoint(SPoint p) {
         if(p.getCoordinates() == null)
             return null;
         return new DropLocation(this, p);
     }
 
     private void installTransferHandler() {
         if(getTransferHandler() == null) {
             setTransferHandler(new DefaultTransferHandler());
         }
     }
 
     public void setDragEnabled(boolean dragEnabled) {
         if(getSelectionModel() == null && dragEnabled == true) {
             throw new IllegalStateException("no selection model in stable " + this);
         }
 
         if(dragEnabled != this.dragEnabled) {
             if(dragEnabled) {
                 this.getSession().getSDragAndDropManager().addDragSource(this);
             } else {
                 this.getSession().getSDragAndDropManager().removeDragSource(this);
             }
 
             this.dragEnabled = dragEnabled;
         }
     }
 
     public static class DefaultTransferHandler extends STransferHandler implements CustomDragHandler {
         public DefaultTransferHandler() {
             super(null);
         }
 
         protected Transferable createTransferable(SComponent component) {
             STable table = (STable)component;
 
             String htmlText = "<html><body><table>";
             String plainText = "";
             
             for(int row:table.getSelectedRows()) {
                 htmlText += "<tr>";
                 for(int col=0; col<table.getColumnCount(); ++col) {
                     Object object = table.getModel().getValueAt(row, col);
 
                     htmlText += "<td>" + object.toString() + "</td>";
                     if(col == (table.getColumnCount()-1))
                         plainText += object.toString();
                     else
                         plainText += object.toString() + "\t";
                 }
                 
                 plainText += "\n";
                 htmlText += "</tr>";
             }
             htmlText += "</table></body></html>";
             
 
             return new TextAndHTMLTransferable(plainText, htmlText);
         }
 
         public int getSourceActions(SComponent component) {
             return COPY;
         }
 
         public boolean dragStart(SComponent source, SComponent target, int action, SMouseEvent event) {
             try {
                 String[] coords = event.getPoint().getCoordinates().split(":");
                 int row = Integer.parseInt(coords[0]);
                 int col = Integer.parseInt(coords[1]);
                 if(coords.length < 4)
                     return false;
 
                 boolean ctrlKey = false;
                 boolean shiftKey = false;
                 for(int i=2; i<coords.length; ++i) {
                     String[] keyVal = coords[i].split("=");
                     if("ctrlKey".equals(keyVal[0])) {
                         ctrlKey = Boolean.parseBoolean(keyVal[1]);
                     } else if("shiftKey".equals(keyVal[0])) {
                         shiftKey = Boolean.parseBoolean(keyVal[1]);
                     }
                 }
 
                 if(row != -1 && col != -1) {
                     if(source instanceof STable) {
                         STable table = (STable)source;
 
                         Rectangle viewport = table.getViewportSize();
                         if(viewport != null) {
                             row += viewport.y;
                             col += viewport.x;
                         }
 
                         if(table.isRowSelected(row))
                             return false;
                         
                         table.addSelectionEvent(row, col, ctrlKey, shiftKey);
                     }
                 }
             } catch(Exception e) {
                 
             }
             return false;
         }
     }
 }
