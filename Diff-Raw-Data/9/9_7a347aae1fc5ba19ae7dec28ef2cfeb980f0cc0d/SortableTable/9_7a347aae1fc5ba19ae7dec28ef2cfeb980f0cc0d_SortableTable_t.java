 /*
  * Copyright (c) 2000
  *      Jon Schewe.  All rights reserved
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  *
  * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
  */
 package net.mtu.eggplant.util.gui;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JTable;
 import javax.swing.table.TableModel;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableColumn;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 
 import java.util.Enumeration;
 
 public class SortableTable extends JTable {
 
   public SortableTable(final SortableTableModel tm) {
     super(tm);
     getTableHeader().addMouseListener(_columnListener);
     addPropertyChangeListener(new PropertyChangeListener() {
       public void propertyChange(final PropertyChangeEvent pce) {
         if(pce.getPropertyName().equals("tableHeader")) {
           final JTableHeader oldHeader = (JTableHeader)pce.getOldValue();
           final JTableHeader newHeader = (JTableHeader)pce.getNewValue();
           if(oldHeader != null) {
             oldHeader.removeMouseListener(_columnListener);
           }
           if(newHeader != null) {
             newHeader.addMouseListener(_columnListener);
           }
         }
       }
     });
   }
 
   /**
      Overriden to make sure that the table model set is a SortedTableModel.
      
     @throws IllegalArgumentException if tm is not an instanceof {@link SortableTableModel}
   **/
   public void setModel(final TableModel tm) {
     if(! (tm instanceof SortableTableModel) ) {
       throw new IllegalArgumentException("table model must be a SortableTableModel");
     }
     super.setModel(tm);
   }
 
     private MouseAdapter _columnListener = new MouseAdapter() {
     public void mouseClicked(final MouseEvent me) {
       final JTableHeader header = getTableHeader(); 
       final TableColumnModel colModel = getColumnModel();
       final int columnIndex = colModel.getColumnIndexAtX(me.getX());
       final int modelIndex = colModel.getColumn(columnIndex).getModelIndex();
       if(modelIndex < 0) {
         return;
       }
       
       final SortableTableModel sm = (SortableTableModel)getModel();
       sm.sort(modelIndex);
       final Enumeration columnIter = colModel.getColumns();
       final boolean ascending = sm.isAscending();
       final int sortedColumn = sm.getSortedColumn();
       for(int cindex = 0; columnIter.hasMoreElements(); cindex++) {
         final TableColumn column = (TableColumn)columnIter.nextElement();
         String columnName = sm.getColumnName(column.getModelIndex());
         if(cindex == sortedColumn) {
           columnName += ascending ? " ^" : " v";
         } 
         column.setHeaderValue(columnName);
       }
       
       header.repaint();
     }
   };
 
 }
