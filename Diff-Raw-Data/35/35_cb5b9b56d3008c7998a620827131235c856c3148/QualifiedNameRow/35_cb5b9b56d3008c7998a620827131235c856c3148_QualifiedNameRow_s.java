 package edu.rivfader.relalg;
 
 import edu.rivfader.data.Row;
 import edu.rivfader.errors.AmbiguousColumnName;
 import edu.rivfader.errors.UnknownColumnName;
 import edu.rivfader.errors.TableAmbiguous;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 
 /**
  * this implements the IQualifiedNameRow.
  * @author harald
  */
 public class QualifiedNameRow implements IQualifiedNameRow {
     /**
      * contains the name to value mapping.
      */
     private Map<IQualifiedColumnName, String> values;
 
     /**
      * constructs a new QualifiedNameRow with the given column names.
      * @param columnNames the column names to use
      */
     public QualifiedNameRow(IQualifiedColumnName... columnNames) {
         values = new HashMap<IQualifiedColumnName, String>();
         for (IQualifiedColumnName cn : columnNames) {
             values.put(cn, null);
         }
     }
 
     /**
      * constructs a new qualified name row from the column names.
      * @param column names the colum names to use
      */
     public QualifiedNameRow(Collection<IQualifiedColumnName> columnNames) {
         values = new HashMap<IQualifiedColumnName, String>();
         for(IQualifiedColumnName cn : columnNames) {
             values.put(cn, null);
         }
     }
 
     /**
      * creates the join of two qualified name rows.
      * @param first the first row to join
      * @param second the second row to join
      */
     public QualifiedNameRow(IQualifiedNameRow first, IQualifiedNameRow second) {
         values = new HashMap<IQualifiedColumnName, String>();
         copyData(first);
         copyData(second);
     }
 
     /**
      * copies the data from the source row into this row.
      * @param source where data comes from
      */
     private void copyData(IQualifiedNameRow source) {
         for(IQualifiedColumnName cn : source.columns()) {
             values.put(cn, source.getData(cn));
         }
     }
     @Override
     public Collection<IQualifiedColumnName> columns() {
         return new HashSet<IQualifiedColumnName>(values.keySet());
     }
 
     @Override
     public IQualifiedColumnName resolveUnqualifiedName(
             final String columnName) {
         IQualifiedColumnName r = null; // result
         for(IQualifiedColumnName n : values.keySet()) { // name
             if (n.getColumn().equals(columnName)) {
                 if (r == null) {
                     r = n;
                 } else {
                     throw new AmbiguousColumnName(columnName);
                 }
             }
         }
         if (r == null) {
             throw new UnknownColumnName(columnName);
         }
         return r;
     }
 
     @Override
     public String getData(final IQualifiedColumnName column) {
         if (column == null) {
             throw new IllegalArgumentException("column == null");
         }
         return values.get(column);
     }
 
     @Override
     public void setData(final IQualifiedColumnName column,
                         final String newData) {
         if (column == null) {
             throw new IllegalArgumentException("column == null");
         }
         if (newData == null) {
             throw new IllegalArgumentException("newData == null");
         }
         if (!values.containsKey(column)) {
             throw new UnknownColumnName(column.toString());
         }
         values.put(column, newData);
     }
 
    @Override //XXX: TEST
     public boolean equals(Object other) {
         if(!(other instanceof QualifiedNameRow)) {
             return false;
         }
 
         QualifiedNameRow castedOther = (QualifiedNameRow) other;
         return values.equals(castedOther.values);
     }
 
    @Override //XXX: implement
     public int hashCode() {
         return 0;
     }
 
     /**
      * constructs a QualifiedNameRow with the same columns and data as
      * the source row.
      * @param tableName the tableName of the unqualified row
      * @param source the data row to use
      */
     public static IQualifiedNameRow fromRow(final String tableName,
                                            final Row source) {
         if (tableName == null) {
             throw new IllegalArgumentException("tableName == null");
         }
 
         if (source == null) {
             throw new IllegalArgumentException();
         }
 
         Set<IQualifiedColumnName> columnNames =
             new HashSet<IQualifiedColumnName>();
         for (String cn : source.columns()) { // columnname
             columnNames.add(new QualifiedColumnName(tableName, cn));
         }
         QualifiedNameRow r = new QualifiedNameRow(columnNames); // result
         for (String cn : source.columns()) {
             r.setData(new QualifiedColumnName(tableName, cn),
                       source.getData(cn));
         }
         return r;
     }
 
     /**
      * turns a qualifiedNameRow into a regular row.
      * requires that all table names are identical.
      * @param source the sorce row
      */
     public static Row toRow(IQualifiedNameRow source) {
         Set<String> tns = new HashSet<String>(); // table names
         Set<String> cns = new HashSet<String>(); // column names
         for(IQualifiedColumnName cn : source.columns()) {
             tns.add(cn.getTable());
             cns.add(cn.getColumn());
         }
 
         if (tns.size() > 1) {
             throw new TableAmbiguous(tns);
         }
 
         Row r = new Row(cns.iterator());
 
         for (IQualifiedColumnName cn : source.columns()) {
             r.setData(cn.getColumn(), source.getData(cn));
         }
         return r;
     }
 }
