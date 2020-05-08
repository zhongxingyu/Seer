 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.util.EventObject;
 import java.util.HashMap;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.event.*;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableModel;
 
 import org.wings.plaf.*;
 import org.wings.io.Device;
 import org.wings.externalizer.ExternalizeManager;
 
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class STable
     extends SBaseTable
     implements ActionListener, CellEditorListener, SGetListener
 {
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "TableCG";
 
     protected int selectionMode = SConstants.NO_SELECTION;
 
     /**
      * TODO: documentation
      */
     protected EventListenerList listenerList = new EventListenerList();
 
     protected Selectable[] selects = null;
 
     /**
      * TODO: documentation
      */
     protected SButtonGroup selectGroup = null;
 
     /** If editing, Component that is handling the editing. */
     transient protected SComponent editorComp;
 
     /**
      * The object that overwrites the screen real estate occupied by the
      * current cell and allows the user to change those contents.
      */
     transient protected STableCellEditor cellEditor;
 
     /** Identifies the column of the cell being edited. */
     transient protected int editingColumn;
 
     /** Identifies the row of the cell being edited. */
     transient protected int editingRow;
 
     /**
      * TODO: documentation
      */
     protected HashMap editors = new HashMap();
 
     /** Icon used for buttons that start editing in a cell. */
     transient protected Icon editIcon = null;
 
     /**
      * TODO: documentation
      */
     protected Color selForeground = null;
 
     /**
      * TODO: documentation
      */
     protected Color selBackground = null;
 
     /**
      * TODO: documentation
      *
      * @param tm
      */
     public STable(TableModel tm){
         super(tm);
         createDefaultEditors();
         createDefaultIcons();
     }
 
     /**
      * TODO: documentation
      *
      * @param tm
      */
     public void setModel(TableModel tm) {
         super.setModel(tm);
         initSelectables();
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void initSelectables() {
         if (getSelectionMode() != SConstants.NO_SELECTION) {
             if (selectGroup == null)
                 selectGroup = new SButtonGroup();
             else
                 selectGroup.removeAll();
 
             if (selects != null) {
                 for (int i=0; i < selects.length; i++)
                     selects[i].setSelected(false);
 
                 if (selects.length<getRowCount()) {
                     addSelectables(selects.length, getRowCount()-1);
                 }
                 else if (selects.length>getRowCount()) {
                     deleteSelectables(getRowCount(), selects.length-1);
                 }
             }
             else {
                 selects = new Selectable[model.getRowCount()];
                 for (int i=0; i<model.getRowCount(); i++)
                     selects[i] = generateSelectable(i);
             }
         }
     }
 
     /**
      * TODO: documentation
      *
      */
     public void checkSelectables() {
         if (selects == null || getRowCount() != selects.length) {
             // System.err.println("checkSelectables() wird tatschlich gebraucht !!!");
             initSelectables();
         }
     }
 
     /**
      * TODO: documentation
      *
      * @param row
      * @return
      */
     protected Selectable generateSelectable(int row) {
         SCheckBox tmp = null;
         if (getSelectionMode() == SConstants.SINGLE_SELECTION) {
             tmp = new SRadioButton();
             selectGroup.add(tmp);
         }
         else {
             tmp = new SCheckBox();
         }
         tmp.setParent(getParent());
 
         // hiermit werden ListSelectionEvents getriggert
         tmp.addActionListener(this);
         return tmp;
     }
 
     /**
      * TODO: documentation
      *
      * @param sel
      */
     protected void deactivateSelectable(Selectable sel) {
         if (sel != null) {
             sel.setParent(null);
             sel.removeActionListener(this);
         }
     }
 
 
     public void setParent(SContainer p) {
         super.setParent(p);
 
         if (editorComp != null)
             editorComp.setParent(p);
 
         if (selects != null)
             for (int i=0; i<selects.length; i++)
                 selects[i].setParent(p);
     }
 
 
     public void getPerformed(String action, String value) {
         int row = new Integer(value.substring(0, value.indexOf(':'))).intValue();
         int col = new Integer(value.substring(value.indexOf(':') + 1)).intValue();
         editCellAt(row, col, null);
     }
 
     public STableCellRenderer getCellRenderer(int row, int column) {
         if (column >= super.getColumnCount())
             return defaultRenderer;
         return super.getCellRenderer(row, column);
     }
 
     public SComponent prepareRenderer(STableCellRenderer r, int row, int col) {
         if (col >= super.getColumnCount()
             && getSelectionMode() != SConstants.NO_SELECTION) {
             return (SComponent)selects[row];
         }
         else
             return super.prepareRenderer(r, row, col);
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
      * @see     TableModel#isCellEditable
      * @see     #getDefaultEditor
      * @see     #setDefaultRenderer
      */
     public void setDefaultEditor(Class columnClass, STableCellEditor r) {
         editors.remove(columnClass);
         if (editors != null)
             editors.put(columnClass, r);
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
      * @param columnClass
      * @return
      * @see     #setDefaultEditor
      * @see     #getColumnClass
      */
     public STableCellEditor getDefaultEditor(Class columnClass) {
         if (columnClass == null) {
             return null;
         }
         else {
             Object r = editors.get(columnClass);
             if (r != null) {
                 return (STableCellEditor)r;
             }
             else {
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
      * @param   row                             the row to be edited
      * @param   column                          the column to be edited
      * @exception IllegalArgumentException      If <I>row</I> or <I>column</I>
      *                                          are not in the valid range
      * @return  false if for any reason the cell cannot be edited.
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
      * @param   row                             the row to be edited
      * @param   column                          the column to be edited
      * @param   e                               event to pass into
      *                                          shouldSelectCell
      * @exception IllegalArgumentException      If <I>row</I> or <I>column</I>
      *                                          are not in the valid range
      * @return  false if for any reason the cell cannot be edited.
      */
     public boolean editCellAt(int row, int column, EventObject e){
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
             // prepare editor
             editorComp = prepareEditor(editor, row, column);
 
             if (editor.isCellEditable(e)) {
                 editorComp.setParent(getParent());
                 //this.add(editorComp);
                 setCellEditor(editor);
                 setEditingRow(row);
                 setEditingColumn(column);
                 editor.addCellEditorListener(this);
 
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns true if the cell at <I>row</I> and <I>column</I>
      * is editable.  Otherwise, setValueAt() on the cell will not change
      * the value of that cell.
      *
      * @param   row      the row whose value is to be looked up
      * @param   column   the column whose value is to be looked up
      * @return  true if the cell is editable.
      * @see #setValueAt
      */
     public boolean isCellEditable(int row, int col) {
         if (col >= super.getColumnCount() || row == -1)
             return false;
         else
             return getModel().isCellEditable(row, col);
     }
 
     /**
      * Returns  true is the table is editing a cell.
      *
      * @return  true is the table is editing a cell
      * @see     #editingColumn
      * @see     #editingRow
      */
     public boolean isEditing() {
         return (cellEditor == null) ? false : true;
     }
 
     /**
      * If the receiver is currently editing this will return the Component
      * that was returned from the CellEditor.
      *
      * @return  SComponent handling editing session
      */
     public SComponent getEditorComponent() {
         return editorComp;
     }
 
     /**
      * This returns the index of the editing column.
      *
      * @return  the index of the column being edited
      * @see #editingRow
      */
     public int getEditingColumn() {
         return editingColumn;
     }
 
     /**
      * Returns the index of the editing row.
      *
      * @return  the index of the row being edited
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
      * @param anEditor  the STableCellEditor that does the editing
      * @see #cellEditor
      */
     public void setCellEditor(STableCellEditor anEditor) {
         STableCellEditor oldEditor = cellEditor;
         cellEditor = anEditor;
     }
 
     /**
      * Set the editingColumn variable.
      *
      * @see #editingColumn
      */
     public void setEditingColumn(int aColumn) {
         editingColumn = aColumn;
     }
 
     /**
      * Set the editingRow variable.
      *
      * @see #editingRow
      */
     public void setEditingRow(int aRow) {
         editingRow = aRow;
     }
 
     /**
      * Return an appropriate editor for the cell specified by this row and
      * column. If the TableColumn for this column has a non-null editor, return that.
      * If not, find the class of the data in this column (using getColumnClass())
      * and return the default editor for this type of data.
      *
      * @param row       the row of the cell to edit, where 0 is the first
      * @param column    the column of the cell to edit, where 0 is the first
      */
     public STableCellEditor getCellEditor(int row, int column) {
         // TableColumn tableColumn = getColumnModel().getColumn(column);
         // STableCellEditor editor = tableColumn.getCellEditor();
         // if (editor == null) {
         STableCellEditor editor = getDefaultEditor(getColumnClass(column));
         // }
         return editor;
     }
 
 
     /**
      * Prepares the specified editor using the value at the specified cell.
      *
      * @param editor  the TableCellEditor to set up
      * @param row     the row of the cell to edit, where 0 is the first
      * @param column  the column of the cell to edit, where 0 is the first
      */
     protected SComponent prepareEditor(STableCellEditor r, int row, int col) {
         return r.getTableCellEditorComponent(this,
                                              model.getValueAt(row,col),
                                              isRowSelected(row), // true?
                                              row, col);
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
             setEditingColumn(-1);
             setEditingRow(-1);
             editorComp = null;
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
         editors = new HashMap();
 
         // Objects
         STextField textField = new STextField();
         setDefaultEditor(Object.class, new SDefaultCellEditor(textField));
         setDefaultEditor(Number.class, new SDefaultCellEditor(textField));
 
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
      * TODO: documentation
      *
      * @return
      */
     public int getColumnCount() {
         if (getSelectionMode() != SConstants.NO_SELECTION) {
             return super.getColumnCount()+1;
         }
         else {
             return super.getColumnCount();
         }
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getSelectedRowCount() {
         int count = 0;
         if (selects != null && getSelectionMode() != SConstants.NO_SELECTION) {
             for (int i=0; i<selects.length; i++) {
                 if (selects[i].isSelected())
                     count++;
             }
         }
         return count;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getSelectedRow() {
         if (selects != null && getSelectionMode() != SConstants.NO_SELECTION) {
             for (int i=0; i<selects.length; i++) {
                 if (selects[i].isSelected())
                     return i;
             }
         }
         return -1;
     }
 
     public int[] getSelectedRows() {
         int[] erg = new int[getSelectedRowCount()];
         if (selects != null && getSelectionMode() != SConstants.NO_SELECTION) {
             int index = 0;
             for (int i=0; i<selects.length; i++) {
                 if (selects[i].isSelected())
                     erg[index++]=i;
             }
         }
         return erg;
     }
 
     /**
      * Deselects all selected columns and rows.
      */
     public void clearSelection() {
         if (selects != null) {
             for (int i=0; i<selects.length; i++)
                 selects[i].setSelected(false);
         }
     }
 
 
     /**
      * TODO: documentation
      *
      * @param row
      * @return
      */
     public boolean isRowSelected(int row) {
         return getSelectionMode() != SConstants.NO_SELECTION &&
             selects != null && selects[row] != null &&
             selects[row].isSelected();
     }
 
     /**
      * Sets the selection mode. Use one of the following values:
      * <UL>
      * <LI> {@link SConstants#NO_SELECTION}
      * <LI> {@link SConstants#SINGLE_SELECTION}
      * <LI> {@link SConstants#MULTIPLE_SELECTION}
      * </UL>
      */
     public void setSelectionMode(int s) {
         clearSelection();
         selectionMode = s;
         initSelectables();
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getSelectionMode() {
         return selectionMode;
     }
 
     /**
      * TODO: documentation
      *
      * @param listener
      */
     public void addSelectionListener(ListSelectionListener listener) {
         listenerList.add(ListSelectionListener.class, listener);
     }
 
     /**
      * TODO: documentation
      *
      * @param listener
      */
     public void removeSelectionListener(ListSelectionListener listener) {
         listenerList.remove(ListSelectionListener.class, listener);
     }
 
     /**
      * Fire a SelectionEvent at each registered listener.
      */
     protected void fireSelectionValueChanged(int index) {
         ListSelectionEvent e = new ListSelectionEvent(this, index, index, false);
         // Guaranteed to return a non-null array
         Object[] listeners = listenerList.getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length-2; i>=0; i-=2) {
             if (listeners[i] == ListSelectionListener.class) {
                 ((ListSelectionListener)listeners[i+1]).valueChanged(e);
             }
         }
     }
 
 
     /*
      * Hier wird der SelectionPerformed Event gefeuert.
      */
     /**
      * TODO: documentation
      *
      * @param e
      */
     public void actionPerformed(ActionEvent e) {
         for (int i=0; i<getRowCount(); i++) {
             if (selects[i] == e.getSource()) {
                 fireSelectionValueChanged(i);
                 return;
             }
         }
     }
 
     /*
      * Fuegt eine Menge neuer Selectierbare Komponenten hinzu. Diese
      * Methode wird aufgerufen, wenn im TableModel Daten(Zeilen)
      * eingefuegt werden. Die Methode {@link #generateSelectable} wird
      * benutzt um Selectables zu erzeugen.
      */
     /**
      * TODO: documentation
      */
     protected final void addSelectables(int firstrow, int lastrow) {
         if (getSelectionMode() != SConstants.NO_SELECTION) {
             // System.out.println("add " + firstrow + " : " + lastrow);
 
             firstrow = Math.max(firstrow, 0);
             lastrow = Math.min(getRowCount(), lastrow);
 
             Selectable[] newselects = new Selectable[(lastrow+1)-firstrow];
             for (int i=0; i<=lastrow-firstrow; i++) {
                 // System.out.println("generate select " + (firstrow+i));
                 newselects[i] = generateSelectable(firstrow+i);
             }
 
             Selectable[] oldselects = selects;
 
             selects = new Selectable[newselects.length+oldselects.length];
 
             System.arraycopy(oldselects, 0, selects, 0, firstrow);
             System.arraycopy(newselects, 0, selects, firstrow, newselects.length);
             System.arraycopy(oldselects, firstrow, selects,
                              firstrow+newselects.length,
                              oldselects.length-firstrow);
 
             // System.out.println(" Prior " + oldselects.length +
             //                    " now Only " + selects.length + " selects");
         }
     }
 
     /**
      * TODO: documentation
      */
     protected final void deleteSelectables(int firstrow, int lastrow) {
         if (getSelectionMode() != SConstants.NO_SELECTION) {
             // System.out.println("delete " + firstrow + " : " +lastrow);
             Selectable[] oldselects = selects;
 
             firstrow = Math.max(firstrow, 0);
             lastrow = Math.min(selects.length-1, lastrow);
 
             selects = new Selectable[selects.length-(lastrow+1-firstrow)];
 
             System.arraycopy(oldselects, 0, selects, 0, firstrow);
             System.arraycopy(oldselects, lastrow+1, selects, firstrow,
                              oldselects.length-(lastrow+1));
 
             // die entfernten muessen natuerlich deactiviert werden.
             for (int i=firstrow; i<=lastrow; i++) {
                 // System.out.println("deactivate select " + i);
                 deactivateSelectable(oldselects[i]);
             }
             // System.out.println(" Prior " + oldselects.length +
             //                    " now Only " + selects.length + " selects");
         }
     }
 
     /**
      * TODO: documentation
      *
      * @param e
      */
     public void tableChanged(TableModelEvent e) {
         // kill active editors
         editingCanceled(null);
 
         // this could be null !!!
         if ( e!=null ) {
             switch ( e.getType() ) {
             case TableModelEvent.INSERT:
                 if (e.getFirstRow() >= 0)
                     addSelectables(e.getFirstRow(), e.getLastRow());
                 break;
                 
             case TableModelEvent.DELETE:
                 if (e.getFirstRow() >= 0)
                     deleteSelectables(e.getFirstRow(), e.getLastRow());
                 break;
                 
             case TableModelEvent.UPDATE:
                 // Falls sich die Daten geaendert haben, sonst sind keine
                 // Aenderungen noetig.
                if (e.getFirstRow() <= 0 ||
                    e.getLastRow()>selects.length)
                     initSelectables();
                 break;
             }
         } else {
             initSelectables();
         }
     }
 
     /**
      * Sets the icon used for the buttons that start editing in a cell.
      */
     public void setEditIcon(Icon newIcon) {
         editIcon = newIcon;
     }
 
     /**
      * Returns the icon used for the buttons that start editing in a cell.
      */
     public Icon getEditIcon() {
         return editIcon;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setSelectionBackground(Color c) {
         selBackground=c;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Color getSelectionBackground() {
         return selBackground;
     }
 
     /**
      * TODO: documentation
      *
      * @param c
      */
     public void setSelectionForeground(Color c) {
         selForeground=c;
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public Color getSelectionForeground() {
         return selForeground;
     }
 
     /**
      * TODO: documentation
      *
      */
     protected void createDefaultIcons() {
         setEditIcon(new ResourceImageIcon("icons/Pencil.gif"));
     }
 
     /**
      * Returns the name of the CGFactory class that generates the
      * look and feel for this component.
      *
      * @return "TableCG"
      * @see SComponent#getCGClassID
      * @see CGDefaults#getCG
      */
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(TableCG cg) {
         super.setCG(cg);
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
