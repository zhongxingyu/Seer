 /*
  *  $Id: PWQuery.java 2013/09/21 3:03:36 Masamitsu Oikawa $
  *
  *  ===============================================================================
  *
  *   Copyright (C) 2013  Masamitsu Oikawa  <oicawa@gmail.com>
  *   
  *   Permission is hereby granted, free of charge, to any person obtaining a copy
  *   of this software and associated documentation files (the "Software"), to deal
  *   in the Software without restriction, including without limitation the rights
  *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *   copies of the Software, and to permit persons to whom the Software is
  *   furnished to do so, subject to the following conditions:
  *   
  *   The above copyright notice and this permission notice shall be included in
  *   all copies or substantial portions of the Software.
  *   
  *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *   THE SOFTWARE.
  *
  *  ===============================================================================
  */
 
 package pw.core.accesser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pw.core.PWField;
 import pw.core.item.PWItem;
 
 public class PWQuery {
 
 	public static final String COMMA = ",";
 	public static final String AND = " and ";
 	
 	private String query;
 	
 	private List<Object> values = new ArrayList<Object>();
 	
 	public PWQuery(String query) {
 		this.query = query;
 	}
 	
 	public String getQuery() {
 		return query;
 	}
 	
 	public List<Object> getValues() {
 		return values;
 	}
 
 	public void addValue(Object value) {
 		values.add(value);
 	}
 	
 	public static <TItem extends PWItem> String getTableName(Class<TItem> type) {
 		
 		String tableName = PWItem.getTableName(type);
 		return tableName;
 	}
 	
 	public static <TItem extends PWItem> PWQuery getCreateTableQuery(Class<TItem> itemType) {
     	List<PWField> fields = PWItem.getFields(itemType);
     	
     	// Create the fields part of query
     	StringBuffer fieldsBuffer = new StringBuffer();
     	StringBuffer primaryBuffer = new StringBuffer();
     	StringBuffer uniqueBuffer = new StringBuffer();
     	for (PWField field : fields) {
     		// Field buffer
     		fieldsBuffer.append(COMMA);
     		fieldsBuffer.append(field.getName());
     		fieldsBuffer.append(" ");
     		fieldsBuffer.append(field.getType());
     		
     		// Primary buffer
     		if (field.isPrimary()) {
     			primaryBuffer.append(COMMA);    			
     			primaryBuffer.append(field.getName());
     		}
     		
     		// Unique buffer
     		if (field.isUnique()) {
     			uniqueBuffer.append(COMMA);
     			uniqueBuffer.append(field.getName());
     		}
     	}
     	String fieldQuery = fieldsBuffer.substring(COMMA.length());
     	String primaryQuery = primaryBuffer.length() == 0 ? "" : String.format(",primary key(%s)", primaryBuffer.substring(COMMA.length()));
     	String uniqueQuery = uniqueBuffer.length() == 0 ? "" : String.format(",unique(%s)", uniqueBuffer.substring(COMMA.length()));
     	
     	// Create all query
     	String allQuery = String.format("create table %s (%s%s%s);", PWQuery.getTableName(itemType), fieldQuery, primaryQuery, uniqueQuery);
     	
     	// Create PWQuery
     	PWQuery query = new PWQuery(allQuery);
     	return query;
 	}
 	
 	public static <TItem extends PWItem> PWQuery getSelectQueryByKeys(Class<TItem> itemType, PWField.KeyType keyType, Object... keyValues) {
 		// Create all query
 		String allQuery;
 		if (keyValues.length == 0) {
 			allQuery = String.format("select * from %s;",
 					PWQuery.getTableName(itemType));
 		} else {
 			allQuery = String.format("select * from %s where %s;",
 					PWQuery.getTableName(itemType),
 					getWhereQueryByKeys(itemType, keyType));
 		}
 		
 		// Create query and add parameters
 		PWQuery query = new PWQuery(allQuery);
 		for (Object keyValue : keyValues) {
 			query.addValue(keyValue);
 		}
 		
 		return query;
 	}
 	
 	public static <TItem extends PWItem> PWQuery getInsertQueryOnlyKeyValues(Class<TItem> itemType, Object... keyValues) {
     	List<PWField> fields = PWItem.getFields(itemType);
     	
     	// Create the fields part of query
     	StringBuffer fieldsBuffer = new StringBuffer();
     	for (PWField field : fields) {
     		if (field.isPrimary()) {
         		fieldsBuffer.append(COMMA);
         		fieldsBuffer.append("?");
     		} else {
         		fieldsBuffer.append(COMMA);
     			fieldsBuffer.append("NULL");
     		}
     	}
     	String fieldsQuery = fieldsBuffer.substring(COMMA.length());
     	
     	// Create all query
     	String allQuery = String.format("insert into %s values (%s);",
     			PWQuery.getTableName(itemType),
     			fieldsQuery);
     	
     	// Create PWQuery
     	PWQuery query = new PWQuery(allQuery);
     	for (Object keyValue : keyValues) {
         	query.addValue(keyValue);
     	}
     	
     	return query;
 	}
 	
 	public static <TItem extends PWItem> PWQuery getUpdateQueryByKey(TItem item, PWField.KeyType keyType) {
 		@SuppressWarnings("unchecked")
 		Class<TItem> itemType = (Class<TItem>)item.getClass() ;
     	List<PWField> fields = PWItem.getFields(itemType);
     	
     	// Create the fields part of query
     	StringBuffer fieldsBuffer = new StringBuffer();
     	for (PWField field : fields) {
      		if (field.isKey(keyType)) {
      			continue;
      		}
  			fieldsBuffer.append(COMMA);
  			fieldsBuffer.append(field.getName());
  			fieldsBuffer.append(" = ?");
     	}
     	String fieldsQuery = fieldsBuffer.substring(COMMA.length());
     	
     	
     	// Create all query
     	String allQuery = String.format("update %s set %s where %s;",
     			PWQuery.getTableName(itemType),
     			fieldsQuery,
     			getWhereQueryByKeys(itemType, keyType));
     	
     	// Create PWQuery & add parameters
     	// for fields
     	PWQuery query = new PWQuery(allQuery);
     	for (PWField field : fields) {
      		if (field.isKey(keyType)) {
     			continue;
      		}
      		query.addValue(field.getValue(item));
     	}
     	// for where
     	for (PWField field : fields) {
      		if (field.isKey(keyType)) {
          		query.addValue(field.getValue(item));
      		}
     	}
     	
     	return query;
 	}
 
 	public static <TItem extends PWItem> PWQuery getDeleteQueryByKey(Class<TItem> itemType, PWField.KeyType keyType, Object... keyValues) {
     	// Create all query
 		String allQuery = String.format("delete from %s where %s;",
 				PWQuery.getTableName(itemType),
 				getWhereQueryByKeys(itemType, keyType));
 		
     	// Create PWQuery
     	PWQuery query = new PWQuery(allQuery);
     	for (Object keyValue : keyValues) {
         	query.addValue(keyValue);
     	}
     	return query;
 	}
 	
 	private static <TItem extends PWItem> String getWhereQueryByKeys(Class<TItem> itemType, PWField.KeyType keyType) {
     	StringBuffer buffer = new StringBuffer();
     	for (PWField field : PWItem.getFields(itemType, keyType)) {
  			buffer.append(AND);
  			buffer.append(field.getName());
  			buffer.append(" = ?");
     	}
     	String wheresQuery = buffer.substring(AND.length());
     	return wheresQuery;
 	}
 }
