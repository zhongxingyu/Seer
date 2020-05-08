 package com.codeminer42.dismantle;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class Model {
     public Map<String, String> externalRepresentationKeyPaths() {
         return null;
     }
 
     protected Model() {
 
     }
 
     protected Model(Map<String, Object> externalRepresentation) {
         Map<String, String> selfRepresentation = this.completeExternalRepresentationKeyPaths();
         for (String property : selfRepresentation.keySet()) {
             String path = selfRepresentation.get(property);
             Object transformable = getData(externalRepresentation, path);
             assignProperty(property, transformable);
         }
     }
 
     private Map<String, String> completeExternalRepresentationKeyPaths() {
         Map<String, String> selfRepresentation = this.externalRepresentationKeyPaths();
         if (selfRepresentation == null)
             selfRepresentation = new HashMap<String, String>();
         final Field[] fields =  this.getClass().getDeclaredFields();
         for(Field f : this.getClass().getDeclaredFields()) {
             if (!selfRepresentation.containsKey(f.getName())) {
                 if (!f.getName().startsWith("this$")) //Ignore nested-class reference
                     selfRepresentation.put(f.getName(), f.getName());
             }
 
         }
         return selfRepresentation;
     }
 
     private static Object getData(Map<String, Object> externalRepresentation, String path) {
         if (path.contains(".")) {
             String[] subPaths = path.split("\\.");
             Object nestedObject = externalRepresentation.get(subPaths[0]);
             if (nestedObject instanceof Map) {
                 try {
                     Map<String, Object> nestedRepresentation = (Map<String, Object>) nestedObject;
                     return getData(nestedRepresentation, path.substring(subPaths[0].length()+1));
                 } catch (ClassCastException e) {
                     // Well, not a Map<String, Object> :/
                     e.printStackTrace();
                 }
             }
             return null;
         } else {
             return externalRepresentation.get(path);
         }
     }
 
     public Map<String, Object> externalRepresentation() {
         Map<String, String> selfRepresentation = this.completeExternalRepresentationKeyPaths();
         Map<String, Object> representation = new HashMap<String, Object>();
         for (String property : selfRepresentation.keySet()) {
             representation.put(selfRepresentation.get(property), getProperty(property));
         }
         return representation;
     }
 
     private void assignProperty(String property, Object mapValue) {
         Object result = null;
         try {
             try {
                 result = tryToInvokeTransformationTo(property, mapValue);
             } catch (NoSuchMethodException e) {
                 // try to setTheField with the result we got
                 result = mapValue;
             } catch(Exception e) {
                 // Unexpected error while trying to invoke the transform method.
             } finally {
                 this.tryToSetField(property, result);
             }
         } catch (SecurityException e) {
             e.printStackTrace();
         }
     }
 
     private Object getProperty(String property) {
         Object result = null;
         Object propertyData = null;
         try {
             Field propertyField = getField(this.getClass(), property);
             propertyData = tryToGetField(propertyField);
             result = tryToInvokeTransformationFrom(propertyField, propertyData);
         } catch (NoSuchMethodException e) {
             result = propertyData;
         } catch (NoSuchFieldException ignored) {
         }
         return result;
     }
 
     private Object tryToInvokeTransformationTo(String property, Object mapValue) throws NoSuchMethodException {
         try {
            Method method = this.getClass().getDeclaredMethod("transformTo" + capitalize(property), Object.class);
             method.setAccessible(true);
             return method.invoke(this, mapValue);
         } catch (InvocationTargetException ignored) {
         } catch (IllegalAccessException ignored) {
         }
         return null;
     }
 
     private Object tryToInvokeTransformationFrom(Field property, Object propertyValue) throws NoSuchMethodException {
         try {
            Method method = this.getClass().getDeclaredMethod("transformFrom" + capitalize(property.getName()), property.getType());
             method.setAccessible(true);
             return method.invoke(this, propertyValue);
         } catch (InvocationTargetException ignored) {
         } catch (IllegalAccessException ignored) {
         }
         return null;
     }
 
     private void tryToSetField(String property, Object data) {
         try {
             Field field = getField(this.getClass(), property);
             field.setAccessible(true);
             field.set(this, data);
         } catch (NoSuchFieldException ignored) {
         } catch (IllegalAccessException ignored) {
         }
     }
 
     /**
      * Get the field through the super classes
      * @param property
      * @return property data
      */
     private final Object tryToGetField(Field property) {
         try {
             property.setAccessible(true);
             return property.get(this);
         } catch (IllegalAccessException ignored) {
         }
         return null;
     }
 
    private String capitalize(String word) {
         return word.substring(0, 1).toUpperCase() + word.substring(1);
     }
 
     private static Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
         try {
             return clazz.getDeclaredField(fieldName);
         } catch (NoSuchFieldException e) {
             Class superClass = clazz.getSuperclass();
             if (superClass == null) {
                 throw e;
             } else {
                 return getField(superClass, fieldName);
             }
         }
     }
 }
