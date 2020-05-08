 /**
  *
  * Copyright 2008-2009 Elements. All Rights Reserved.
  *
  * License version: CPAL 1.0
  *
  * The Original Code is mysimpledb.com code. Please visit mysimpledb.com to see how
  * you can contribute and improve this software.
  *
  * The contents of this file are licensed under the Common Public Attribution
  * License Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  *
  *    http://mysimpledb.com/license.
  *
  * The License is based on the Mozilla Public License Version 1.1.
  *
  * Sections 14 and 15 have been added to cover use of software over a computer
  * network and provide for attribution determined by Elements.
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing permissions and limitations under the
  * License.
  *
  * Elements is the Initial Developer and the Original Developer of the Original
  * Code.
  *
  * Based on commercial needs the contents of this file may be used under the
  * terms of the Elements End-User License Agreement (the Elements License), in
  * which case the provisions of the Elements License are applicable instead of
  * those above.
  *
  * You may wish to allow use of your version of this file under the terms of
  * the Elements License please visit http://mysimpledb.com/license for details.
  *
  */
 package ac.elements.sdb;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.concurrent.Future;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import ac.elements.concurrency.AsyncOperation;
 import ac.elements.conversion.TypeConverter;
 import ac.elements.parser.SimpleDBConverter;
 import ac.elements.parser.SimpleDBParser;
 import ac.elements.sdb.collection.SimpleDBDataList;
 import ac.elements.sdb.collection.SimpleDBMap;
 
 /**
  * The Class ASimpleDBCustom is an abstract class implementing the
  * ISimpleDBCustom interface asynchronously.
  */
 public abstract class ASimpleDBCustomAsync extends ASimpleDBApiExtended
         implements ISimpleDBCustom {
 
     /** The Constant log. */
     private final static Log log =
             LogFactory.getLog(ASimpleDBCustomAsync.class);
 
     /**
      * Instantiates a new simple db to issue requests to Amazon's ASimpleDBApi.
      * 
      * <p>
      * Construction requires authentication to verify that the subscriber is
      * authorized to perform the requested action. Authentication ensures that
      * you don't get charged for operations you did not authorize and that
      * nobody else sees your private data..
      * 
      * @param id
      *            the Access Key ID is associated with your AWS account.
      * @param key
      *            the Secret Access Key
      */
     public ASimpleDBCustomAsync(final String id, final String key) {
         super(id, key);
     }
 
     /**
      * The Delete operation is a custom utility method that first selects a set
      * of ItemNames that match the select query expression, and subsequently
      * deletes this set with the deleteItem method. Delete is similar to the
      * standard SQL DELETE statement.
      * 
      * <code>
      * delete from test where onekey='avalue'
      * </code>
      * 
      * @param deleteExpression
      *            the delete expression used to query the domain for itemNames.
      * 
      * @return a SimpleDBDataList of itemNames deleted
      */
     public SimpleDBDataList setDelete(final String deleteExpression) {
 
         long t0 = System.currentTimeMillis();
         log.trace("Entering delete");
 
         // modify expression to first perform select
         if (deleteExpression.trim().toLowerCase().indexOf("delete ") != 0) {
             throw new RuntimeException("Illegal deleteExpression: "
                     + deleteExpression + ", index 0 !="
                     + deleteExpression.trim().toLowerCase().indexOf("delete "));
         }
         String selectExpression =
                 "SELECT itemName() "
                         + deleteExpression.substring("delete ".length()).trim();
         log.trace("selectExpression: " + selectExpression);
         String nextToken = null;
         SimpleDBDataList sdbList;
         ArrayList<Object> itemNames = new ArrayList<Object>();
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         // does the delete statement have a limit clause?
         int limit =
                 TypeConverter.getInt(SimpleDBParser
                         .getLimitClause(selectExpression), -1);
         do {
             // there is no point of doing this asynchronous, as the select
             // expression's next token is only known after completion
             sdbList = setSelect(selectExpression, nextToken);
             for (int i = 0; i < sdbList.size(); i++) {
                 Object itemName = sdbList.get(i).getItemName();
                 itemNames.add(itemName);
 
                 Future<String> future =
                         AsyncOperation.deleteItem(sdbList.getDomainName(),
                                 SimpleDBConverter.encodeValue(itemName));
                 futures.add(future);
                 // should we limit the number of items deleted?
                 if (limit != -1 && itemNames.size() >= limit) {
                     sdbList.setNextToken(null);
                     break;
                 }
             }
             nextToken = sdbList.getNextToken();
             log.trace("nextToken: " + nextToken);
         } while (sdbList.getNextToken() != null);
 
         // return a select * from domain deleted attributes list
         SimpleDBDataList list = new SimpleDBDataList();
         Iterator<Object> iterator = itemNames.iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
         list.setResponseTime(responseTime);
         String domainName = SimpleDBParser.getDomain(selectExpression);
         list.setDomainName(domainName);
 
         // todo what to do with futures?
         return list;
 
     }
 
     /**
      * The Delete attribute operation is a custom utility method that first
      * selects a set of ItemNames that match the query expression, and
      * subsequently deletes the attributes with the deleteAttributes method.
      * Delete is a special case for deleting attributes, and resembles the
      * standard SQL DELETE statement syntax by augmenting the key attributes
      * that will be deleted.
      * 
      * <code>
      * delete (key1, `key2`) from test where onekey='avalue'
      * </code>
      * 
      * @param deleteExpression
      *            the delete expression used to query the domain for itemNames.
      * 
      * @return a SimpleDBDataList of itemNames deleted
      */
     public SimpleDBDataList setDeleteAttributeWhere(String deleteExpression) {
 
         long t0 = System.currentTimeMillis();
         log.trace("Entering deleteAttribute");
         deleteExpression = ExtendedFunctions.trimSentence(deleteExpression);
 
         // modify expression to first perform select
         if (deleteExpression.trim().toLowerCase().indexOf("delete (") != 0) {
             throw new RuntimeException("Illegal deleteExpression: "
                     + deleteExpression);
         }
 
         // get the key syntax without brackets
         String keySyntax =
                 deleteExpression.substring(deleteExpression.indexOf("(") + 1,
                         deleteExpression.indexOf(")")).trim();
         log.error("keySyntax: " + keySyntax);
 
         Object[] keys = SimpleDBParser.parseList(keySyntax, "`");
         if (keys.length == 0) {
             throw new RuntimeException("Illegal deleteExpression");
         }
 
         String keysToSearch = "itemName()";
         boolean replaced = false;
         for (int j = 0; j < keys.length; j++) {
             if (((String) keys[j]).indexOf('=') == -1) {
                 if (!replaced) {
                     keysToSearch = (String) keys[j];
                     replaced = true;
                 } else {
                     keysToSearch += ", " + (String) keys[j];
                 }
             }
         }
         String selectExpression =
                 "SELECT "
                         + keysToSearch
                         + " "
                         + deleteExpression.substring(
                                 deleteExpression.indexOf(")") + 1).trim();
         log.error("selectExpression: " + selectExpression);
 
         String nextToken = null;
         SimpleDBDataList sdbList;
         ArrayList<Object> itemNames = new ArrayList<Object>();
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         // does the delete statement have a limit clause?
         int limit =
                 TypeConverter.getInt(SimpleDBParser
                         .getLimitClause(selectExpression), -1);
 
         do {
             // there is no point of doing this asynchronous, as the select
             // expression's next token is only known after completion
             sdbList = setSelect(selectExpression, nextToken);
             SimpleDBMap sdbMap = new SimpleDBMap();
 
             for (int i = 0; i < sdbList.size(); i++) {
                 Object itemName = sdbList.get(i).getItemName();
                 itemNames.add(itemName);
                 for (int j = 0; j < keys.length; j++) {
                     if (((String) keys[j]).indexOf('=') != -1) {
                         String keyValue[] = ((String) keys[j]).split("=");
                         sdbMap.put(keyValue[0], SimpleDBConverter
                                 .getStringOrNumber(keyValue[1]));
                         log.error("got = " + sdbMap);
                     } else {
 
                         Object key = keys[j];
                         Object value = sdbList.get(i).get(key);
                         if (value == null) {
                             value = "";
                         } else {
                             // loop through value list
                             for (int k = 0; k < sdbList.get(i).get(key).size(); k++) {
                                 Object valueInList =
                                         sdbList.get(i).get(key).toArray()[k];
                                 sdbMap.put(key, valueInList);
                                 log.error("got no = " + key + " , "
                                         + valueInList);
                             }
                         }
 
                     }
                 }
                 sdbMap.setItemName(itemName);
 
                 // async
                 Future<String> future =
                         AsyncOperation.deleteAttributes(
                                 sdbList.getDomainName(), sdbMap);
                 futures.add(future);
 
                 // should we limit the number of items deleted?
                 if (limit != -1 && itemNames.size() >= limit) {
                     sdbList.setNextToken(null);
                     break;
                 }
             }
             nextToken = sdbList.getNextToken();
             log.trace("nextToken: " + nextToken);
         } while (sdbList.getNextToken() != null);
 
         // return a select * from domain deleted attributes list
         SimpleDBDataList list = new SimpleDBDataList();
         list.setDomainName(sdbList.getDomainName());
         Iterator<Object> iterator = itemNames.iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
         list.setResponseTime(responseTime);
         // todo what to do with futures?
         return list;
 
     }
 
     /**
      * The Delete attribute operation is a mapping to the deleteAttribute action
      * and deletes the attributes expression with the deleteAttributes method.
      * Delete is a special case of the standard SQL DELETE statement.
      * 
      * <code>
      * delete itemName(key1=1.0f, `key2`='myvalue') from domain
      * </code>
      * 
      * @param deleteExpression
      *            the delete expression used to query the domain for itemNames.
      * 
      * @return the number of itemNames deleted
      */
     public SimpleDBDataList setDeleteAttribute(String deleteExpression) {
 
         long t0 = System.currentTimeMillis();
         log.trace("Entering setDeleteAttribute");
 
         // make sure spaces are ok and we have no enters
         deleteExpression = ExtendedFunctions.trimSentence(deleteExpression);
 
         log.trace("Got delete expression: " + deleteExpression);
         // basic check if expression has good syntax
         if (deleteExpression.trim().toLowerCase().indexOf("delete") != 0
                 && deleteExpression.trim().toLowerCase().indexOf("(") != 0
                 && deleteExpression.trim().toLowerCase().indexOf(")") != 0) {
             throw new RuntimeException("Illegal deleteExpression: "
                     + deleteExpression);
         }
 
         String parseString = deleteExpression.substring("delete ".length());
         String domain = SimpleDBParser.getDomain(deleteExpression);
 
         log.error("Found domain: " + domain);
 
         String itemSyntax =
                 parseString.substring(0,
                         parseString.toLowerCase().indexOf(" from ")).trim();
 
         log.error("Parsing itemSyntax: " + itemSyntax);
         ArrayList<Object> itemNames = new ArrayList<Object>();
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         StringBuffer itemBuffer = new StringBuffer(itemSyntax);
         int pointer = 0;
         // strip things to contain only item names
         while (pointer < itemBuffer.length()) {
             if (itemBuffer.charAt(pointer) == '(') {
                 itemBuffer.deleteCharAt(pointer);
                 while (itemBuffer.length() > 0
                         && itemBuffer.charAt(pointer) != ')') {
                     itemBuffer.deleteCharAt(pointer);
                 }
                 if (itemBuffer.charAt(pointer) == ')')
                     itemBuffer.deleteCharAt(pointer);
             }
             pointer++;
         }
         log.error("itemBuffer: " + itemBuffer.toString());
         Object[] items = SimpleDBParser.parseList(itemBuffer.toString(), "`");
 
         if (items.length == 0) {
             throw new RuntimeException("wrong syntax, no itemName given.");
         } else {
             for (int i = 0; i < items.length; i++) {
                 Object itemName = items[i];
                 if (itemName == null || itemName.toString().equals("")) {
                     throw new RuntimeException("Item is null or empty");
                 }
 
                 itemNames.add(itemName);
             }
         }
         log.info("Parsed itemSyntax: " + itemNames);
 
         log.trace("Parsing dataSyntax: " + itemSyntax);
 
         StringBuffer dataBuffer = new StringBuffer(itemSyntax);
         pointer = 0;
         // strip things to contain only data lists
         while (pointer < dataBuffer.length()) {
 
             if (dataBuffer.charAt(pointer) == '(') {
                 while (dataBuffer.length() > 0
                         && dataBuffer.charAt(pointer) != ')') {
                     pointer++;
                 }
                 pointer++;
                 if (dataBuffer.length() < pointer)
                     dataBuffer.insert(pointer, ',');
                 pointer++;
             } else {
                 dataBuffer.deleteCharAt(pointer);
             }
         }
 
         // replace ( and ) with | and then parse like its a | delimited file
         // todo make beter
         String data = dataBuffer.toString().replace('(', '|').replace(')', '|');
         Object[] valueSequence = SimpleDBParser.parseList(data, "|");
 
         SimpleDBMap sdbMap = new SimpleDBMap();
         if (valueSequence.length == 0 || valueSequence.length > 1) {
             throw new RuntimeException("Illegal deleteExpression in values: "
                     + deleteExpression);
         } else {
             log.error("vs: " + valueSequence[0]);
             String valueString = (String) valueSequence[0];
             Object[] values = SimpleDBParser.parseList(valueString, "");
             for (int j = 0; j < values.length; j++) {
                 String[] keyValue = ((String) values[j]).split("=");
                 if (keyValue.length != 2) {
                     throw new RuntimeException(
                             "Illegal deleteExpression key value pairs must "
                                     + "have syntax key=value: "
                                     + deleteExpression);
                 }
                 sdbMap.put(keyValue[0], SimpleDBConverter
                         .getStringOrNumber(keyValue[1]));
             }
             sdbMap.setItemName(itemNames.get(0));
         }
 
         // async
         Future<String> future = AsyncOperation.deleteAttributes(domain, sdbMap);
         futures.add(future);
 
         // return a select count(*) from domain formatted list
         SimpleDBDataList list = new SimpleDBDataList();
         list.setDomainName(domain);
 
         Iterator<Object> iterator = itemNames.iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
         list.setResponseTime(responseTime);
 
         // todo what to do with futures?
         return list;
     }
 
     /**
      * The Insert operation is a custom utility method that inserts a set of key
      * value pairs, by parsing the insert statement and uses the
      * batchPutAttributes method to insert the key value pairs. Unlike standard
      * SQL, the item name used is also given. If there is already an item name,
      * the key - value pair is added to the item name.
      * <p>
      * The quoting rules are the same as for the select statement.
      * <p>
      * If no itemName is given then a universally unique identifiers (UUID) is
      * generated for the item name, a guarantee for no collisions of the items
      * inserted. For the rest the insert is similar to the standard SQL INSERT
      * statement. Example strings follow (no new line characters should be
      * used):
      * 
      * <code>
      * insert into domain (`keyone`, keytwo) 
      *   values aItemName('value1a', 'value2a');
      * insert into domain (keyone, `keytwo`) 
      *   values ('value1a', 'value2a'), ('value1b', 'value2b');
      * </code>
      * 
      * @param insertExpression
      *            the insert expression used to generate key value pairs for the
      *            domain and the itemName.
      * 
      * @return a SimpleDBDataList of itemNames inserted
      */
     @SuppressWarnings("unchecked")
     public SimpleDBDataList setInsert(String insertExpression) {
         long t0 = System.currentTimeMillis();
         if (log.isDebugEnabled())
             log.debug("Entering insert");
         SimpleDBDataList dataList = extractSimpleDBList(insertExpression);
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         // async
         Future<String> future = AsyncOperation.batchPutAttributes(dataList);
         futures.add(future);
 
         // return a select count(*) from domain formatted list
         SimpleDBDataList list = new SimpleDBDataList();
         list.setDomainName(dataList.getDomainName());
 
         Iterator<String> iterator = dataList.getItemNames().iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
 
         // todo what to do with futures?
         list.setResponseTime(responseTime);
         return list;
     }
 
     /**
      * The Insert operation is a custom utility method that inserts a set of key
      * value pairs, by parsing the insert statement and uses the
      * batchPutAttributes method to insert the key value pairs. Unlike standard
      * SQL, the item name used is also given. If there is already an item name,
      * the key - value pair is added to the item name.
      * <p>
      * The quoting rules are the same as for the standard select statement
      * syntax.
      * <p>
      * If no itemName is given then a universally unique identifiers (UUID) is
      * generated for the item name, a guarantee for no collisions of the items
      * inserted. For the rest the insert is similar to the standard SQL INSERT
      * statement. Example strings follow (no new line characters should be
      * used):
      * 
      * <code>
      * insert into domain (`keyone`, keytwo) 
      *   values ('value1a', 'value2a') WHERE key1='value';
      * </code>
      * 
      * @param insertExpression
      *            the insert expression used to generate key value pairs for the
      *            domain and the itemName.
      * 
      * @return a SimpleDBDataList of itemNames inserted
      */
     public SimpleDBDataList setInsertWhere(String insertExpression) {
 
         long t0 = System.currentTimeMillis();
 
         log.trace("Entering setInsertWhere");
         insertExpression = ExtendedFunctions.trimSentence(insertExpression);
 
         // modify expression to first perform select
         if (insertExpression.trim().toLowerCase().indexOf("insert") != 0) {
             throw new RuntimeException("Illegal insertExpression: "
                     + insertExpression);
         }
 
         String parseString =
                 insertExpression.substring("insert into ".length());
         String domain = parseString.substring(0, parseString.indexOf(" "));
 
         log.trace("Found domain: " + domain);
 
         String selectExpression =
                 "SELECT itemName() FROM "
                         + domain
                         + " "
                         + insertExpression
                                 .substring(
                                         insertExpression.toLowerCase().indexOf(
                                                 "where")).trim();
         log.trace("selectExpression: " + selectExpression);
 
         String nextToken = null;
         SimpleDBDataList sdbList;
         ArrayList<Object> itemNames = new ArrayList<Object>();
         int size = 0;
         do {
             sdbList = setSelect(selectExpression, nextToken);
             size += sdbList.size();
            if (size == 0) {
                SimpleDBDataList list = new SimpleDBDataList();
                list.setDomainName(domain);
            }
             for (int i = 0; i < sdbList.size(); i++) {
                 Object itemName = sdbList.get(i).getItemName();
                 itemNames.add(SimpleDBConverter.encodeValue(itemName));
                 log.error("item: " + itemNames.get(i));
             }
             nextToken = sdbList.getNextToken();
             log.trace("nextToken: " + nextToken);
         } while (sdbList.getNextToken() != null);
 
         // get the key syntax without brackets
         String keySyntax =
                 insertExpression.substring(
                         insertExpression.indexOf("(") + 1,
                         SimpleDBParser.indexOfIgnoreCaseRespectQuotes(1,
                                 insertExpression, ")", '`')).trim();
 
         Object[] keys = SimpleDBParser.parseList(keySyntax, "`");
         if (keys.length == 0) {
             throw new RuntimeException("Illegal insertExpression");
         }
 
         String valueSyntax =
                 insertExpression.substring(
                         insertExpression.toLowerCase().indexOf("values")
                                 + "values".length(),
                         insertExpression.toLowerCase().indexOf("where")).trim();
 
         log.trace("Parsing valueSyntax: " + valueSyntax);
         SimpleDBDataList dataList = new SimpleDBDataList();
         dataList.setDomainName(domain);
         StringBuffer dataBuffer = new StringBuffer(valueSyntax);
         int pointer = 0;
         // strip things to contain only data lists
         while (pointer < dataBuffer.length()) {
 
             if (dataBuffer.charAt(pointer) == '(') {
                 while (dataBuffer.length() > 0
                         && dataBuffer.charAt(pointer) != ')') {
                     pointer++;
                 }
                 pointer++;
                 if (dataBuffer.length() < pointer)
                     dataBuffer.insert(pointer, ',');
                 pointer++;
             } else {
                 dataBuffer.deleteCharAt(pointer);
             }
         }
         // replace ( and ) with | and then parse like its a | delimited file
         // todo make beter
         String data = dataBuffer.toString().replace('(', '|').replace(')', '|');
         Object[] valueSequence = SimpleDBParser.parseList(data, "|");
 
         String valueString = (String) valueSequence[0];
         Object[] values = SimpleDBParser.parseList(valueString, "'\"");
 
         if (valueSequence.length == 0) {
             throw new RuntimeException("Illegal insertExpression in values: "
                     + insertExpression);
         } else {
             for (int i = 0; i < itemNames.size(); i++) {
 
                 SimpleDBMap sdbMap = new SimpleDBMap();
                 for (int j = 0; j < values.length; j++) {
                     Object value = values[j];
                     sdbMap.put(keys[j], value);
 
                 }
                 sdbMap.setItemName(itemNames.get(i));
                 dataList.add(sdbMap);
             }
         }
 
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         // async
         Future<String> future = AsyncOperation.batchPutAttributes(dataList);
         futures.add(future);
 
         // return a select count(*) from domain formatted list
         SimpleDBDataList list = new SimpleDBDataList();
         list.setDomainName(domain);
 
         Iterator<Object> iterator = itemNames.iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
         list.setResponseTime(responseTime);
 
         // todo what to do with futures?
         return list;
     }
 
     /**
      * The Replace operation is a custom utility method that inserts a set of
      * key value pairs, if the key already exists it replaces it with the given
      * key value pair.
      * <p>
      * The code does this by parsing the replace statement and uses the
      * batchPutReplaceAttributes method to insert/ replace the key value pairs.
      * Unlike standard SQL, the item name used can also be given. If there is
      * already an item name, the key - value pair is replaced in the item name.
      * <p>
      * The quoting rules are the same as for the standard select statement
      * syntax.
      * <p>
      * If no itemName is given then a universally unique identifiers (UUID) is
      * generated for the item name, a guarantee for no collisions of the items
      * inserted. For the rest the replace is similar to the standard SQL REPLACE
      * statement. Example strings follow (no new line characters should be
      * used):
      * 
      * <code>
      * replace into domain (`keyone`, keytwo) 
      *   values aItemName('value1a', 'value2a');
      * replace into domain (keyone, `keytwo`) 
      *   values ('value1a', 'value2a'), ('value1b', 'value2b');
      * </code>
      * 
      * @param replaceExpression
      *            the replace expression used to generate key value pairs for
      *            the domain and the itemName.
      * 
      * @return a SimpleDBDataList of itemNames modified/ inserted
      */
     @SuppressWarnings("unchecked")
     public SimpleDBDataList setReplace(String replaceExpression) {
         long t0 = System.currentTimeMillis();
         log.trace("Entering replace");
         SimpleDBDataList dataList = extractSimpleDBList(replaceExpression);
 
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         // async
         Future<String> future =
                 AsyncOperation.batchPutReplaceAttributes(dataList);
         futures.add(future);
 
         // return a select count(*) from domain formatted list
         SimpleDBDataList list = new SimpleDBDataList();
         list.setDomainName(dataList.getDomainName());
 
         Iterator<String> iterator = dataList.getItemNames().iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
         list.setResponseTime(responseTime);
 
         // todo what to do with futures?
         return list;
     }
 
     /**
      * The Replace operation is a custom utility method that replaces a set of
      * key value pairs, by parsing the replace statement and uses the
      * batchPutReplaceAttributes method to replace the key value pairs. Unlike
      * standard SQL.
      * <p>
      * The quoting rules are the same as for the standard select statement
      * syntax.
      * <p>
      * The replace is similar to the standard MYSQL REPLACE statement except
      * that it expects a where clause to select the itemNames to work on.
      * Example strings follow:
      * 
      * <code>
      * replace into domain (`keyone`, keytwo) 
      *   values ('value1a', 'value2a') WHERE key1='value';
      * </code>
      * 
      * @param replaceExpression
      *            the replace expression used to replace key value pairs.
      * 
      * @return a SimpleDBDataList of itemNames modified/ inserted
      */
     public SimpleDBDataList setReplaceWhere(String replaceExpression) {
         long t0 = System.currentTimeMillis();
 
         log.error("Entering setReplaceWhere");
         replaceExpression = ExtendedFunctions.trimSentence(replaceExpression);
 
         // modify expression to first perform select
         if (replaceExpression.trim().toLowerCase().indexOf("replace") != 0) {
             throw new RuntimeException("Illegal replaceExpression: "
                     + replaceExpression);
         }
 
         String parseString =
                 replaceExpression.substring("replace into ".length());
         String domain = parseString.substring(0, parseString.indexOf(" "));
 
         log.trace("Found domain: " + domain);
 
         String selectExpression =
                 "SELECT itemName() FROM "
                         + domain
                         + " "
                         + replaceExpression.substring(
                                 replaceExpression.toLowerCase().indexOf(
                                         " where ")).trim();
         log.trace("selectExpression: " + selectExpression);
 
         String nextToken = null;
         SimpleDBDataList sdbList;
         ArrayList<Object> itemNames = new ArrayList<Object>();
         int size = 0;
         do {
             sdbList = setSelect(selectExpression, nextToken);
             size += sdbList.size();
             if (size == 0) {
                 SimpleDBDataList list = new SimpleDBDataList();
                 list.setDomainName(domain);
             }
             for (int i = 0; i < sdbList.size(); i++) {
                 Object itemName = sdbList.get(i).getItemName();
                 itemNames.add(SimpleDBConverter.encodeValue(itemName));
                 log.error("item: " + itemNames.get(i));
             }
             nextToken = sdbList.getNextToken();
             log.trace("nextToken: " + nextToken);
         } while (sdbList.getNextToken() != null);
 
         // get the key syntax without brackets
         String keySyntax =
                 replaceExpression.substring(
                         replaceExpression.indexOf("(") + 1,
                         SimpleDBParser.indexOfIgnoreCaseRespectQuotes(1,
                                 replaceExpression, ")", '`')).trim();
 
         Object[] keys = SimpleDBParser.parseList(keySyntax, "`");
         if (keys.length == 0) {
             throw new RuntimeException("Illegal replaceExpression");
         }
 
         String valueSyntax =
                 replaceExpression.substring(
                         replaceExpression.toLowerCase().indexOf("values")
                                 + "values".length(),
                         replaceExpression.toLowerCase().indexOf("where"))
                         .trim();
 
         log.trace("Parsing valueSyntax: " + valueSyntax);
         SimpleDBDataList dataList = new SimpleDBDataList();
         dataList.setDomainName(domain);
         StringBuffer dataBuffer = new StringBuffer(valueSyntax);
         int pointer = 0;
         // strip things to contain only data lists
         while (pointer < dataBuffer.length()) {
 
             if (dataBuffer.charAt(pointer) == '(') {
                 while (dataBuffer.length() > 0
                         && dataBuffer.charAt(pointer) != ')') {
                     pointer++;
                 }
                 pointer++;
                 if (dataBuffer.length() < pointer)
                     dataBuffer.insert(pointer, ',');
                 pointer++;
             } else {
                 dataBuffer.deleteCharAt(pointer);
             }
         }
         // replace ( and ) with | and then parse like its a | delimited file
         // todo make beter
         String data = dataBuffer.toString().replace('(', '|').replace(')', '|');
         Object[] valueSequence = SimpleDBParser.parseList(data, "|");
 
         String valueString = (String) valueSequence[0];
         Object[] values = SimpleDBParser.parseList(valueString, "'\"");
 
         if (valueSequence.length == 0) {
             throw new RuntimeException("Illegal replaceExpression in values: "
                     + replaceExpression);
         } else {
             for (int i = 0; i < itemNames.size(); i++) {
 
                 SimpleDBMap sdbMap = new SimpleDBMap();
                 for (int j = 0; j < values.length; j++) {
                     Object value = values[j];
                     sdbMap.put(keys[j], value);
 
                 }
                 sdbMap.setItemName(itemNames.get(i));
                 dataList.add(sdbMap);
             }
         }
 
         ArrayList<Future<String>> futures = new ArrayList<Future<String>>();
 
         // async
         Future<String> future =
                 AsyncOperation.batchPutReplaceAttributes(dataList);
         futures.add(future);
 
         // return a select count(*) from domain formatted list
         SimpleDBDataList list = new SimpleDBDataList();
         list.setDomainName(domain);
 
         Iterator<Object> iterator = itemNames.iterator();
         while (iterator.hasNext()) {
 
             Object itemName = iterator.next();
             SimpleDBMap map = new SimpleDBMap();
             map.setItemName(itemName);
             list.add(map);
 
         }
         long responseTime = System.currentTimeMillis() - t0;
         list.setResponseTime(responseTime);
 
         // todo what to do with futures?
         return list;
     }
 
     public SimpleDBDataList setSelect(final String selectExpression) {
         // todo async
         return setSelect(selectExpression, null);
     }
 
 }
