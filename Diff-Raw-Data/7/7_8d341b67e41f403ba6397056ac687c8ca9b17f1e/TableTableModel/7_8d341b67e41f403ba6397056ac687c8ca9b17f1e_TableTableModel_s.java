 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Table;
 
 import java.util.List;
 import java.util.function.BiFunction;
 
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 /**
  * A Swing {@link TableModel} that uses a Guava {@link Table} as a backing store.
  */
 public class TableTableModel extends AbstractTableModel {
 
     private final List<String> headers;
     private final Table<Integer, Integer, String> table;
     private final BiFunction<Integer, Integer, Boolean> editableFunction;
 
     /**
      * Creates a new table model.
      *
      * @param headers          The headers to use for the table.
      * @param table            The table backing this table.
      * @param editableFunction The function to decide if a cell will be editable
      */
     public TableTableModel(final Iterable<String> headers,
             final Table<Integer, Integer, String> table,
             final BiFunction<Integer, Integer, Boolean> editableFunction) {
         this.headers = Lists.newArrayList(headers);
         this.table = HashBasedTable.create(table);
         this.editableFunction = editableFunction;
     }
 
     @Override
     public int getRowCount() {
         return table.rowKeySet().size();
     }
 
     @Override
     public int getColumnCount() {
         return table.columnKeySet().size();
     }
 
     @Override
     public Object getValueAt(final int rowIndex, final int columnIndex) {
         return table.get(rowIndex, columnIndex);
     }
 
     @Override
     public String getColumnName(final int columnIndex) {
         return headers.get(columnIndex);
     }
 
     @Override
     public Class<?> getColumnClass(final int columnIndex) {
         return String.class;
     }
 
     @Override
     public boolean isCellEditable(final int rowIndex, final int columnIndex) {
         return editableFunction.apply(rowIndex, columnIndex);
     }
 
     @Override
     public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
         table.put(rowIndex, columnIndex, (String) aValue);
         fireTableCellUpdated(rowIndex, columnIndex);
     }
 }
