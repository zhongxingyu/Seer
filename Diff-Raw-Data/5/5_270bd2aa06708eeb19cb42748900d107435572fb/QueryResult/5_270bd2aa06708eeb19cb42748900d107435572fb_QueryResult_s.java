 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.arco;
 
 import com.sun.grid.arco.model.*;
 import com.sun.grid.arco.sql.*;
 import com.sun.grid.arco.export.PivotModel;
 import java.util.*;
 import javax.swing.event.EventListenerList;
 import com.sun.grid.logging.SGELog;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 
 
 /**
  * This class is the abstract base class of all QueryResults
  * 
  */
 public abstract class QueryResult implements java.io.Serializable {
    
    private QueryType query;
    
    private List rows = new ArrayList();
    
    private int rowCount = -1;
    
    private transient PivotModel pivotModel;
    
    private transient EventListenerList listeners = new EventListenerList();
    
    private int  lastBindingCount;
 
    private Map lateBindingMap = new HashMap();
    
    /** Creates a new instance of QueryResult */
    protected QueryResult(QueryType query) {
       this.query = query;
       
      //clusterName must be set on QueryType, before the query si executed
      if (query == null || query.getClusterName() == null || query.getClusterName().length() == 0) {
         throw new IllegalStateException("query.contractViolation");
      }
      
       if ( query instanceof Query ) {
          List filterList = query.getFilter();
          Iterator iter = filterList.iterator();
          Filter filter = null;
          lastBindingCount = 0;
          while( iter.hasNext() ) {
             filter = (Filter)iter.next();
             if( filter.isActive() && filter.isLateBinding() ) {
                lateBindingMap.put(filter.getName(),null);
                lastBindingCount++;
                break;
             }
          }
       }
    }
    
    private void readObject(java.io.ObjectInputStream in)
       throws java.io.IOException, ClassNotFoundException {
       rowCount = -1;
       listeners = new EventListenerList();
       in.defaultReadObject();
    }
    
    public void addQueryResultListener(QueryResultListener lis) {      
       listeners.add(QueryResultListener.class,lis);
    }
    
    public void removeQueryResultListener(QueryResultListener lis) {      
       listeners.remove(QueryResultListener.class,lis);
    }
    
    protected void fireRowCountChanged(int rowCount) {
       Object [] lis = listeners.getListeners(QueryResultListener.class);
       for( int i = 0; i < lis.length; i++){
          ((QueryResultListener)lis[i]).rowCountChanged(rowCount);
       }
    }
    
    /**
     *   Activate the query
     *   If the query is active the call of <code>createValuesForRow</code>
     *   will be able
     *   @throws QueryResultException if the activation has failed
     */
    protected abstract void activate() throws QueryResultException;
    
    /**
     *  Passivate the query
     */
    protected abstract void passivate();
 
    /**
     *  get a list of all available column names
     *  @return list of column names (String)
     */
    public abstract List getColumns();
    
    /**
     * Is it possible to edit the query of this result
     */
    public abstract boolean isEditable();
    
    /**
     *  create an array of objects which contains the values of a row
     *  @return array with the values
     *  @param  row  the row index
     *  @throws QueryResultException if the row values could not be read
     */
    protected abstract Object[] createValuesForNextRow() throws QueryResultException;
    
    /**
     *  Execute the query
     *  After the call of this method the getValue method will return results
     *  @throws QueryResultException if the execution of the query has been failed
     */
    public void execute() throws QueryResultException {      
 
       long start  = System.currentTimeMillis();
       activate();
       try {
          rows.clear();
          int rowIndex = 0;
          for( Object[] row = createValuesForNextRow();
               row != null;
               row = createValuesForNextRow() ) {
             rows.add(row);
          }
       } finally {
          passivate();
       }
       if( SGELog.isLoggable(Level.FINE)) {
          double sec = (((double)System.currentTimeMillis()) - start) / 1000;
          SGELog.fine("query executed in {0}s", new Double(sec));
       }
       fireRowCountChanged(getRowCount());
    }
    
    
    public boolean hasLateBinding() {
       return lastBindingCount > 0;      
    }
    
    public int getLateBindingCount() {
       return lastBindingCount;
    }
    
    public void setLateBinding( String name, Object value ) {
       lateBindingMap.put(name, value);
    }
    
    public Collection getLateBindingsNames() {
       return Collections.unmodifiableCollection(lateBindingMap.keySet());
    }
    
    public Object getLateBinding(String name) {
       return lateBindingMap.get(name);
    }
    
    public Map getLateBinding() {
       return lateBindingMap;
    }
    
    public void parseAdvancedSQL() 
        throws java.text.ParseException {
       parseAdvancedSQL(getQuery());
    }
    
    public static void parseAdvancedSQL(QueryType query) 
         throws java.text.ParseException {
       
       
          if( ArcoConstants.ADVANCED.equals(query.getType()) ) {
       
             try {
                ObjectFactory faq = new ObjectFactory();
 
                SQLParser parser = new SQLParser(query.getSql());
 
                parser.parse();
                List fieldList = query.getField();
                Iterator iter = parser.getFieldList().iterator();
 
                SQLParser.Field field = null;
                Field xmlField = null;
                fieldList.clear();
                String name = null;
 
                while(iter.hasNext()) {
                   field = (SQLParser.Field)iter.next();
                   xmlField = faq.createField();
                   name = field.getName();
                   if( name == null ) {
                      name = field.getExpr();
                   }
                   xmlField.setDbName(name);
                   xmlField.setReportName(name);
                   fieldList.add(xmlField);
                }
 
 
                List filterList = query.getFilter();
                filterList.clear();
                iter = parser.getLateBindingList().iterator();
                LateBinding lb = null;
                Filter filter = null;
 
                while(iter.hasNext()) {
                   lb = (LateBinding)iter.next();
                   filter = faq.createFilter();
                   filter.setName(lb.getName());
                   filter.setLateBinding(true);                  
                   filter.setParameter(lb.getParams());
                   if( lb.getOperator() != null ) {
                      filter.setCondition(lb.getOperator());
                   }
                   filter.setActive(true);
                   filter.setStartOffset(lb.getStart());
                   filter.setEndOffset(lb.getEnd());
                   filterList.add(filter);
                }
             } catch( javax.xml.bind.JAXBException jaxbe ) {
                IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage() );
                ilse.initCause(jaxbe);
                throw ilse;
             }
          }      
    }
 
    
     /**
      * Get the class of a column
      * @param index  index of the column
      * @return  the class of a column
      */
     public abstract Class getColumnClass(int index);
    
    
     /**
      * Get the class of a column.
      * @param columnName  the name of the column
      * @return  the class of the column
      * @throws  IllegalArgumentException if the column name was not found
      */
     public Class getColumnClass( String columnName ) {
        int columnIndex = getColumnIndex(columnName);
        return getColumnClass(columnIndex);
     }
     
    /**
     *  get the the query
     *  @return the query object
     */
    public QueryType getQuery() {
       return query;
    }
    
    
    /**
     * Create a pivot model of the QueryResult
     * @param locale  locale for the formation of the pivot table
     * @return  the pivot model
     */
    public PivotModel createPivotModel(java.util.Locale locale) {
       return new PivotModel(this, locale);
    }
    
    /**
     * Get a value of a result
     * @param row  row index
     * @param col  column index
     * @return  the value
     */
    public Object getValue(int row, int col) {
       Object[] values = getValuesForRow(row);
       if (values == null) {
          return null;
       }
       return values[col];
    }
    
    
    /** 
     *  Create an iterator which iterrates through the rows of 
     *  the result
     */
    public RowIterator rowIterator() {
       return new RowIterator();
    }
    
    /**
     * Get the values of a row. This method uses
     * the internal cache. 
     * @param row index of the row
     * @return the row values
     */
    protected Object[] getValuesForRow(int row) {
       if( row >= 0 && row < getRowCount()) {
          return (Object[])rows.get(row);
       } else {
          return null;
       }
    }
    
    /**
     *   get the number of rows of this result
     *   @return the number of rows
     */
    public int getRowCount() {
       return rows.size();
    }
    
    /**
     * Determine if the result is empty
     * @return true if the result is empty
     */
    public boolean isEmpty() {
       return rows.isEmpty();
    }
    
     /**
      * Get the index of a column
      * @param columnName  the name of the column
      * @return the index of the column
      * @throws IllegalArgumentException if the column is unknown
      */   
     public int getColumnIndex( String columnName ) 
        throws IllegalArgumentException {
        List columns = getColumns();
        String col = null;
        
        for( int i = 0; i < columns.size(); i++ ) {
           col = (String)columns.get(i);
           if( columnName.equals(col) ) {
              return i;
           }
        }
        throw new IllegalArgumentException("column '" + columnName + "' not found");
     }   
     
     /**
      * Get the name of a column
      * @param index  index of the column
      * @return the name of the column
      */
     public String getColumnName(int index) {
        List columns = getColumns();
        return (String)columns.get(index);
     }
     
     
     public java.text.Format createFormater(int columnIndex, String formatString, Locale locale) {
        Class clazz = getColumnClass(columnIndex);
        return createFormater(clazz, formatString, locale);
     }
     
     public static java.text.Format createFormater( Class clazz, String formatString, Locale locale ) {
        
        if( Number.class.isAssignableFrom(clazz)) {
           if( formatString == null ) {
              return java.text.NumberFormat.getInstance(locale);
           } else {
              return new java.text.DecimalFormat(formatString, new java.text.DecimalFormatSymbols(locale));
           }
        } else if ( java.sql.Time.class.isAssignableFrom(clazz) ) {
           if(formatString == null ) {
              formatString = "HH:mm:ss";
           }
           return new java.text.SimpleDateFormat(formatString,locale);
        } else if ( Date.class.isAssignableFrom(clazz)) {
           
           if(formatString == null) {
              return java.text.DateFormat.getDateTimeInstance(
                         java.text.DateFormat.MEDIUM,
                         java.text.DateFormat.MEDIUM, locale );
           } else {
              return new java.text.SimpleDateFormat(formatString,locale);
           }
        } else {
           return null;
        }
     }
     
     public Result createResult() throws javax.xml.bind.JAXBException {
        
        ObjectFactory faq = new ObjectFactory();
        QueryType query = getQuery();
        
        Result ret = faq.createResult();
        
        ret.setName( query.getName() );
        ret.setCategory( query.getCategory() );
        ret.setImgURL( query.getImgURL() );
        ret.setDescription( query.getDescription() );
        ret.setLimit( query.getLimit() );
        ret.setSql(query.getSql());
        
        try {
           ret.setView( (ViewConfiguration)Util.clone(query.getView() ));
        } catch( CloneNotSupportedException cnse ) {
           IllegalStateException ilse = new IllegalStateException("Can't clone view configuration: " + cnse.getMessage());
           ilse.initCause(cnse);
           throw ilse;
        }      
 
        Iterator iter  = null;
        List fieldList = query.getField();
        if( fieldList != null && !fieldList.isEmpty() ) {
           Field field = null;
           iter = fieldList.iterator();
           while( iter.hasNext() ) {
              field = (Field)iter.next();
              try {
                 ret.getField().add( (Field)Util.clone(field) );
              } catch( CloneNotSupportedException cnse ) {
                 IllegalStateException ilse = new IllegalStateException("Can't clone view field: " + cnse.getMessage());
                 ilse.initCause(cnse);
                 throw ilse;
              }
           }
        }
        // Update the filters
        List filterList = query.getFilter();
        
        if( filterList != null && !filterList.isEmpty() ) {
           iter = filterList.iterator();
           Filter filter = null;
           Filter newFilter = null;
           Map lateBinding = getLateBinding();
           String value = null;
           
           while(iter.hasNext()) {
              filter = (Filter)iter.next();
              value = (String)lateBinding.get(filter.getName());             
              try {
                 newFilter = (Filter)Util.clone(filter);
              } catch( CloneNotSupportedException cnse ) {
                 IllegalStateException ilse = new IllegalStateException("Can't clone filter : " + cnse.getMessage());
                 ilse.initCause(cnse);
                 throw ilse;
              }                
              if( value != null ) {
                 newFilter.setParameter(value);
              }
              ret.getFilter().add(newFilter);
           }
        }
        List colList = getColumns();
        iter = colList.iterator();
        ResultColumn column = null;
 
        String colName = null;
        int colCount = colList.size();
        
        for( int i = 0; i < colCount; i++ ) {
           colName = (String)iter.next();
           column = faq.createResultColumn();
           column.setIndex(i);
           column.setName( colName );
           column.setType( ResultConverter.getColumnType(getColumnClass(i)));
           ret.getColumn().add(column);
 
        }
             
        int rowCount = getRowCount();
        ret.setRowCount(rowCount);
        ResultRow rowObj = null;
        int row = 0;
        int col = 0;
        List valueList = null;
        List rowList = ret.getRow();
        for(row = 0; row < rowCount; row++) {
          rowObj = faq.createResultRow();
          rowList.add(rowObj);
          valueList = rowObj.getValue();
           for(col = 0; col < colCount; col++) {
             valueList.add(ResultConverter.objToStr(getValue(row,col)));
           }
 
        }
        return ret;
     }
     
     /**
      *   This class iterates through a query result
      */
     public class RowIterator {
        private int rowIndex = -1;
        private Object [] row = null;
        
        public boolean next() {
           return seek(rowIndex+1);
        }
        
        public boolean seek( int rowIndex) {
           if( rowIndex < 0 ) {
              rowIndex = -1;
              row = null;
              return true;
           } else {
              Object [] newRow = getValuesForRow(rowIndex);
              if( newRow != null ) {
                 this.rowIndex = rowIndex;
                 row = newRow;
                 return true;
              } else {
                 return false;
              }          
           }
        }
 
        public boolean prev() {
           if( rowIndex > 0 ) {
              rowIndex--;
              row = getValuesForRow(rowIndex);
              return true;
           } else {
              return false;
           }
        }
 
        public int getRowIndex() {
           return rowIndex;
        }
        
        public Object getValue(int column) {
           if( row != null ) {
              return row[column];
           } else {
              return null;
           }
        }
     }
 }
