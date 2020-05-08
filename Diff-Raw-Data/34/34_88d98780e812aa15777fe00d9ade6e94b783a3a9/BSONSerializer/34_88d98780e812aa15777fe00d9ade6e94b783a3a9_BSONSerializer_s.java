 package org.jai.BSON;
 
 import sun.reflect.ReflectionFactory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Date;
 import java.util.List;
 
 public class BSONSerializer {
     public static BSONDocument serialize(Object o) {
         if (o == null) {
             return null;
         }
 
         if (o.getClass().isArray() || (o instanceof List)) {
             return new BSONArray(o);
         }
 
         BSONDocument serializedObject = new BSONDocument();
         try {
             for (Field field : o.getClass().getDeclaredFields()) {
                 BSONSerializable annotation = field.getAnnotation(BSONSerializable.class);
                 if (annotation != null) {
                     String fieldName = annotation.name().equals("") ? field.getName() : annotation.name();
                     Object fieldValue;
                     Class fieldType = field.getType();
 
                     field.setAccessible(true);
 
                     if (fieldType.equals(int.class)) {
                         fieldValue = field.getInt(o);
                     } else if (fieldType.equals(boolean.class)) {
                         fieldValue = field.getBoolean(o);
                     } else if (fieldType.equals(byte.class)) {
                         fieldValue = field.getByte(o);
                     } else if (fieldType.equals(char.class)) {
                         fieldValue = field.getChar(o);
                     } else if (fieldType.equals(double.class)) {
                         fieldValue = field.getDouble(o);
                     } else if (fieldType.equals(short.class)) {
                         fieldValue = field.getShort(o);
                     } else if (fieldType.equals(long.class)) {
                         fieldValue = field.getLong(o);
                     } else if (fieldType.equals(float.class)) {
                         fieldValue = field.getFloat(o);
                     } else if (fieldType.equals(String.class) ||
                             fieldType.equals(Integer.class) ||
                             fieldType.equals(Boolean.class) ||
                             fieldType.equals(Byte.class) ||
                             fieldType.equals(Character.class) ||
                             fieldType.equals(Double.class) ||
                             fieldType.equals(Short.class) ||
                             fieldType.equals(Long.class) ||
                             fieldType.equals(Float.class) ||
                             fieldType.equals(Date.class) ||
                             fieldType.equals(BSONArray.class) ||
                             fieldType.isArray() ||
                             fieldType.equals(BSONDocument.class)) {
 
                         fieldValue = field.get(o);
                     } else {
                         fieldValue = serialize(field.get(o));
                     }
 
                     field.setAccessible(false);
 
                     serializedObject.add(fieldName, fieldValue);
                 }
             }
 
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
 
 
         return serializedObject;
     }
 
     public static <T> T deserialize(Class<T> c, BSONDocument serializedObject) {
 
         if (serializedObject == null) {
             return null;
         }
 
         T instance = null;
         ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
 
         Constructor<T> constructor;
         try {
             constructor = reflectionFactory.newConstructorForSerialization(c, Object.class.getConstructor());
         } catch (NoSuchMethodException e) {
             return null;
         }
 
         try {
             if ((instance = constructor.newInstance()) == null) {
                 throw new InstantiationException();
             }
 
             Field[] allFields = c.getDeclaredFields();
 
             for (Field field : allFields) {
                 BSONSerializable a = field.getAnnotation(BSONSerializable.class);
                 if (a != null) {
                    String fieldName = a.name().equals("") ? field.getName() : a.name();
                     Object fieldValue = serializedObject.get(fieldName);
                     Class<?> fieldType = field.getType();
 
 
                     field.setAccessible(true);
 
                     if (fieldType.equals(int.class)) {
                         field.setInt(instance, (int) fieldValue);
                     } else if (fieldType.equals(boolean.class)) {
                        field.setBoolean(instance, (boolean) fieldValue);
                     } else if (fieldType.equals(byte.class)) {
                        field.setByte(instance, (byte) fieldValue);
                     } else if (fieldType.equals(char.class)) {
                        field.setChar(instance, (char) fieldValue);
                     } else if (fieldType.equals(double.class)) {
                         field.setDouble(instance, (double) fieldValue);
                     } else if (fieldType.equals(short.class)) {
                         field.setShort(instance, (short) fieldValue);
                     } else if (fieldType.equals(long.class)) {
                         field.setLong(instance, (long) fieldValue);
                     } else if (fieldType.equals(float.class)) {
                         field.setFloat(instance, (float) fieldValue);
                     } else if (fieldType.equals(String.class) ||
                             fieldType.equals(Integer.class) ||
                             fieldType.equals(Boolean.class) ||
                             fieldType.equals(Byte.class) ||
                             fieldType.equals(Character.class) ||
                             fieldType.equals(Double.class) ||
                             fieldType.equals(Short.class) ||
                             fieldType.equals(Long.class) ||
                             fieldType.equals(Float.class) ||
                             fieldType.equals(Date.class) ||
                             fieldType.equals(BSONArray.class) ||
                             fieldType.equals(BSONDocument.class)) {
 
                         field.set(instance, fieldValue);
                     } else {
                         field.set(instance, deserialize(fieldType, (BSONDocument) fieldValue));
                     }
 
                     field.setAccessible(false);
                 }
             }
 
         } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
             e.printStackTrace();
         }
 
 
         return instance;
     }
 }
