 package com.fatwire.cs.catalogmover.catalogs;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.TreeMap;
 
 public class TableData implements Iterable<Row> {
 
     public static final int COLTYPENUMBER = 0;
 
     public static final int COLTYPETEXT = 1;
 
     public static final int COLTYPEDATE = 2;
 
     public static final int COLTYPEBINARY = 3;
 
     public static final int COLTYPEUNKNOWN = 4;
 
     // ---
     // Current html table format
     public static final int BASETMLVERSION = 3;
 
     public static final int HTMLTABLEVERSION = 3;
 
     private String tableName;
 
     private String tableType;
 
     private String databaseType;
 
     private Map<Key, Cell> cells;
 
     private Map<Integer, Header> headers;
 
     private int rowCount;
 
     public TableData() {
         cells = new TreeMap<Key, Cell>();
         headers = new HashMap<Integer, Header>();
     }
 
     public void addCell(final int row, final int column, final String cell) {
         Header header = headers.get(column);
         if (header != null && header.getName().startsWith("url")) {
             cells.put(new Key(row, column), new Cell(row, headers.get(column),
                     stripFileNumberFromUpload(cell)));
 
         } else {
             cells.put(new Key(row, column), new Cell(row, headers.get(column),
                     cell));
         }
        rowCount = Math.max(rowCount, row+1);
     }
 
     private String stripFileNumberFromUpload(String value) {
         int dot = value.lastIndexOf('.');
         int comma = value.lastIndexOf(',');
         if (comma !=-1 && dot > comma) {
             return (value.substring(0, comma)
                     + value.substring(dot, value.length())).replace('\\', '/');
         }
         return value.replace('\\', '/');
     }
 
     public void addHeader(final int column, final String header,
             final String schema, final int value) {
         headers.put(column, new Header(column, header, schema, value));
     }
 
     public void setTableName(final String tableName) {
         this.tableName = tableName;
 
     }
 
     public void setTableType(final String tableType) {
         this.tableType = tableType;
 
     }
 
     public void setDatabaseType(final String type) {
         databaseType = type;
 
     }
 
     public Map<Key, Cell> getCells() {
         return cells;
     }
 
     public String getDatabaseType() {
         return databaseType;
     }
 
     public Map<Integer, Header> getHeaders() {
         return headers;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     public String getTableType() {
         return tableType;
     }
 
     public Cell getCell(final int k, final int j) {
         return getCells().get(new Key(k, j));
     }
 
     public int getRowCount() {
        return rowCount;
     }
 
     public boolean isTracked() {
         return false;
     }
 
     public Iterator<Row> iterator() {
         return new Iterator<Row>() {
             private int counter = 0;
 
             public boolean hasNext() {
                 return counter < rowCount;
             }
 
             public Row next() {
                 if (!hasNext()) {
                     throw new NoSuchElementException();
                 }
                 final Row row = new Row(TableData.this, counter);
                 counter++;
                 return row;
             }
 
             public void remove() {
                 assert false : "Unsupported operation remove()";
 
             }
 
         };
     }
 
     public String getTableKey() {
         // first header is the key
         return headers.get(0).getName();
     }
 
 }
