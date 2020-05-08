 package com.ags.pirate.model;
 
 import javax.swing.table.AbstractTableModel;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Angel
  * @since 23/11/13
  */
 public class BeanItemTableModel extends AbstractTableModel {
 
     private BeanItemContainer beanItemCollection;
     private String[] visibleColumns;
     private Map<String, String> headerNames;
 
     public BeanItemTableModel(BeanItemContainer beanItemCollection) {
         this.beanItemCollection = beanItemCollection;
         this.headerNames = new HashMap<String, String>();
     }
 
     public void setVisibleColumns(String[] visibleColumns) {
         this.visibleColumns = visibleColumns;
     }
 
     @Override
     public int getRowCount() {
         return beanItemCollection.size();
     }
 
     @Override
     public int getColumnCount() {
         if (visibleColumns == null) {
             throw new RuntimeException("not visible columns defined");
         }
         return visibleColumns.length;
     }
 
     @Override
     public String getColumnName(int column) {
         String visibleColumn = visibleColumns[column];
         if (this.headerNames.containsKey(visibleColumn)) {
             return this.headerNames.get(visibleColumn);
         } else {
             return visibleColumn;
         }
     }
 
     @Override
     public Object getValueAt(int rowIndex, int columnIndex) {
         String columnName = visibleColumns[columnIndex];
         return beanItemCollection.get(rowIndex).getValueFromProperty(columnName);
     }
 
     public void setColumnHeader(String visibleColumn, String headerName) {
         this.headerNames.put(visibleColumn,headerName);
     }
 
     public Object getRow(int rowIndex) {
         return beanItemCollection.get(rowIndex).getBeanItem();
     }
 
     public Class getColumnClass(int c) {
        if (beanItemCollection.size() > 0) {
            return getValueAt(0, c).getClass();
        } else {
            return String.class;
        }
     }
 
 }
