 package com.foogaro.nosql.moongo;
 
 import com.foogaro.nosql.moongo.mapping.MappingHelper;
 import com.foogaro.nosql.moongo.persistence.IPersistenceManager;
 import com.foogaro.nosql.moongo.query.IQueryManager;
 import com.foogaro.nosql.moongo.query.QueryObject;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author Luigi Fugaro
 * @version 1.0.1
 * @since 1.0.1
  */
 @Service
 public class Moongo {
 
     @Autowired
     private IPersistenceManager persistenceManager;
 
     @Autowired
     private IQueryManager queryManager;
 
     @Autowired
     private MappingHelper mappingHelper;
 
     public Object create(Object object) {
         DBObject dbObject = persistenceManager.create(toDBObject(object, true), object.getClass());
         return toObject(dbObject, object);
     }
 
     public Object read(Object object) {
         DBObject dbObject = persistenceManager.read(toDBObject(object, true), object.getClass());
         return toObject(dbObject, object);
     }
 
     public Object update(Object object) {
         DBObject dbObject = persistenceManager.update(toDBObject(object, true), object.getClass());
         return toObject(dbObject, object);
     }
 
     public void delete(Object object) {
         persistenceManager.delete(toDBObject(object, false), object.getClass());
     }
 
     public List find(Class classType) {
         return find(null, classType);
     }
 
     public List find(QueryObject queryObject, Class classType) {
         List results = new ArrayList();
         List<DBObject> dbObjectList = queryManager.find(scanQuery(queryObject), classType);
         for (DBObject dbObject : dbObjectList) {
             results.add(mappingHelper.toObject(dbObject, classType));
         }
         return results;
     }
 
     public Object findOne(QueryObject queryObject, Class classType) {
         DBObject dbObject = queryManager.findOne(scanQuery(queryObject), classType);
         return mappingHelper.toObject(dbObject, classType);
     }
 
 
     private DBObject toDBObject(Object object, boolean saving) {
         return mappingHelper.toDBObject(object, saving);
     }
 
     private Object toObject(DBObject dbObject, Class clazz) {
         return mappingHelper.toObject(dbObject, clazz);
     }
 
     private Object toObject(DBObject dbObject, Object instance) {
         return mappingHelper.toObject(dbObject, instance);
     }
 
     private LinkedList<DBObject> scanQuery(QueryObject queryObject) {
 
         LinkedList<DBObject> result = new LinkedList<DBObject>();
 
         if (queryObject == null) return result;
 
         List<String> operators = queryObject.getQueryOperators();
         List<DBObject> parameters = queryObject.getQueryParameters();
 
         String operator = null;
         DBObject parameter = null;
         String key = null;
         Object value = null;
 
         // probably useless check
         if (operators != null && parameters != null && parameters.size() == operators.size()) {
             for (int index = 0; index < operators.size(); index++) {
 
                 operator = operators.get(index);
                 parameter = parameters.get(index);
 
                 key = parameter.keySet().toArray(new String[1])[0];
                 value = parameter.get(key);
                 DBObject dbObject = new BasicDBObject();
 
                 if (operator == null || operator.trim().length() == 0) {
                     //that's weird!!!
                 } else if (QueryObject.Operators.OR.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.NOR.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.EQ.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.NE.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.AS.equalsIgnoreCase(operator)) {
                     dbObject.putAll(mappingHelper.toMQL(((DBObject)value).get(QueryObject.Operators.AS)));
                     result.add(dbObject);
                 } else if (QueryObject.Operators.STARTS.equalsIgnoreCase(operator)) {
                     dbObject.put(key, value);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.LIKE.equalsIgnoreCase(operator)) {
                     dbObject.put(key, value);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.ENDS.equalsIgnoreCase(operator)) {
                     dbObject.put(key, value);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.IN.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.NIN.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.GT.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.LT.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.GTE.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else if (QueryObject.Operators.LTE.equalsIgnoreCase(operator)) {
                     DBObject operatorDbObject = new BasicDBObject();
                     operatorDbObject.put(operator, value);
                     dbObject.put(key, operatorDbObject);
                     result.add(dbObject);
                 } else {
                     throw new UnsupportedOperationException("Not yet implemented");
                 }
             }
         }
 
         return result;
     }
 
 }
