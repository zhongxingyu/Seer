 package com.hitsoft.mongo.managed;
 
 import com.hitsoft.types.Currency;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class ManagedService {
 
   private static final Logger LOG = LoggerFactory.getLogger(ManagedService.class);
 
   public static DBObject toDBObject(Object object) {
     BasicDBObject result = new BasicDBObject();
     Class clazz = object.getClass();
     while (clazz != null) {
       for (Field field : clazz.getDeclaredFields()) {
         try {
           field.setAccessible(true);
           Object fieldValue = field.get(object);
           String fieldName = field.getName();
           if (fieldValue != null) {
             if (field.isAnnotationPresent(EnumField.class)) {
               fieldValue = ((Enum) fieldValue).name();
             } else if (field.isAnnotationPresent(EnumListField.class)) {
               List<String> val = new ArrayList<String>();
               for (Enum eVal : (List<Enum>) fieldValue) {
                 val.add(eVal.name());
               }
               fieldValue = val;
             } else if (field.isAnnotationPresent(EnumSetField.class)) {
               Set<String> val = new HashSet<String>();
               for (Enum eVal : (Set<Enum>) fieldValue) {
                 val.add(eVal.name());
               }
               fieldValue = val;
             } else if (field.isAnnotationPresent(ObjectField.class)) {
               fieldValue = toDBObject(fieldValue);
             } else if (field.isAnnotationPresent(ObjectListField.class)) {
               List<DBObject> val = new ArrayList<DBObject>();
               for (Object eVal : (List<Object>) fieldValue) {
                 val.add(toDBObject(eVal));
               }
               fieldValue = val;
             } else if (field.getType().equals(Currency.class)) {
               fieldValue = ((Currency) fieldValue).longValue();
             }
             result.put(fieldName, fieldValue);
           }
         } catch (IllegalAccessException e) {
           throw new RuntimeException(e);
         }
       }
       clazz = clazz.getSuperclass();
     }
     return result;
   }
 
   @SuppressWarnings("unchecked")
   public static <T> T fromDBObject(DBObject obj, Class<T> clazz) {
     T result = null;
     if (obj != null) {
       Constructor ctor = getDefaultConstructor(clazz);
       try {
         result = (T) ctor.newInstance((Object[]) null);
         Class currentClass = clazz;
         while (currentClass != null) {
           for (Field field : currentClass.getDeclaredFields()) {
             Object fieldValue = obj.get(field.getName());
             if (fieldValue != null) {
               if (field.isAnnotationPresent(EnumField.class)) {
                 fieldValue = Enum.valueOf(field.getAnnotation(EnumField.class).type(), (String) fieldValue);
               } else if (field.isAnnotationPresent(ObjectField.class)) {
                 fieldValue = fromDBObject((DBObject) fieldValue, field.getAnnotation(ObjectField.class).type());
               } else if (field.isAnnotationPresent(EnumListField.class)) {
                 List<Enum> res = (List<Enum>) field.get(result);
                 for (String dbValue : (List<String>) fieldValue) {
                   res.add(Enum.valueOf(field.getAnnotation(EnumListField.class).type(), dbValue));
                 }
                 fieldValue = res;
               } else if (field.isAnnotationPresent(EnumSetField.class)) {
                 Set<Enum> res = (Set<Enum>) field.get(result);
                 for (String dbValue : (Iterable<String>) fieldValue) {
                   res.add(Enum.valueOf(field.getAnnotation(EnumSetField.class).type(), dbValue));
                 }
                 fieldValue = res;
               } else if (field.isAnnotationPresent(ObjectListField.class)) {
                 List<Object> res = (List<Object>) field.get(result);
                 for (DBObject dbValue : (List<DBObject>) fieldValue) {
                   res.add(fromDBObject(dbValue, field.getAnnotation(ObjectListField.class).type()));
                 }
                 fieldValue = res;
               }
             } else {
               if (field.isAnnotationPresent(EnumListField.class)) {
                 fieldValue = field.get(result);
               } else if (field.isAnnotationPresent(EnumSetField.class)) {
                 fieldValue = field.get(result);
               } else if (field.isAnnotationPresent(ObjectListField.class)) {
                 fieldValue = field.get(result);
               }
             }
             field.setAccessible(true);
             if (field.getType().equals(Integer.class) && (fieldValue instanceof Double))
               field.set(result, ((Double) (fieldValue)).intValue());
             else if (field.getType().equals(Currency.class) && (fieldValue instanceof Double))
               field.set(result, Currency.valueOf(((Double) (fieldValue)).intValue()));
             else if (field.getType().equals(Currency.class) && (fieldValue instanceof Long))
               field.set(result, Currency.valueOf((Long) fieldValue));
             else
               field.set(result, fieldValue);
           }
           currentClass = currentClass.getSuperclass();
         }
       } catch (InstantiationException e) {
         LOG.error("Problem on object's loading from DBObject", e);
       } catch (IllegalAccessException e) {
         LOG.error("Problem on object's loading from DBObject", e);
       } catch (InvocationTargetException e) {
         LOG.error("Problem on object's loading from DBObject", e);
       }
     }
     return result;
   }
 
   private static <T> Constructor getDefaultConstructor(Class<T> clazz) {
     try {
       Constructor ctor = clazz.getDeclaredConstructor((Class[]) null);
       ctor.setAccessible(true);
       return ctor;
     } catch (NoSuchMethodException e) {
       throw new IllegalArgumentException("Default constructor not found for class: " + clazz.getName());
     }
   }
 }
