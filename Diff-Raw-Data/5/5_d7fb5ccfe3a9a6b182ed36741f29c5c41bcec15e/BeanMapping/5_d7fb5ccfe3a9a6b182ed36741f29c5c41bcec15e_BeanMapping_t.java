 /*
  * BeanMapping.java
  *
  *  Copyright (C) 2006  Robert "kebernet" Cooper <cooper@screaming-penguin.com>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  */
 package com.totsp.gwittir.mapping;
 
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 
 /**
  * This is a class that uses reflection to map similar sets of beans to each other.
  * <p>
  *     It has a single public static method, convert, that takes in a
  *     properties object with mapping information and a bean to
  *     convert to another kind of bean.
  * </p>
  * <p>
  *     The properties object is in the format of:
  *     com.package.SomeClass=com.otherpackage.OtherClass
  *
  *     or if you have two packages with like named sets of classes,
  *     you can simply specify:
  *     com.package.*=com.otherpackage.*
  *
  *     This will mean that com.package.Bean maps to com.otherpackage.Bean.
  * </p>
  * <p>
  *     Once a class is passed in, and a mapping match is found,
  *     properties and public non-final attributes on the bean
  *     will be matched to properties and non-final attributes
  *     on the mapped object. Any properties that do not exist on
  *     both classes will be ignored. Properties and attributes are
  *     considered interchangable, so a public attribute on one bean
  *     can be a property on another.
  * </p>
  * @author <a href="cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
  */
 public class BeanMapping {
     static final Class[] BASE_TYPES = {
         java.lang.Integer.class, java.lang.Long.class, java.lang.Byte.class,
         java.lang.Character.class, java.lang.Boolean.class,
         java.lang.Double.class, java.lang.Float.class,
         java.lang.String.class, java.lang.Number.class,
         java.lang.CharSequence.class
     };
     private static final HashMap<Class,HashMap<String,Object>> inspections = new HashMap<Class,HashMap<String,Object>>();
     
     /** Creates a new instance of BeanMapping */
     private BeanMapping() {
         super();
     }
     
     /**
      * Converts a bean to its mapped type.
      * @param mappings The mappings properties
      * @param bean The bean to convert to its mapped type
      * @throws java.beans.IntrospectionException
      * @throws java.lang.ClassNotFoundException
      * @throws java.lang.InstantiationException
      * @throws java.lang.IllegalAccessException
      * @throws java.lang.reflect.InvocationTargetException
      * @throws com.totsp.gwt.beans.server.MappingException
      * @return An object of the appropriate mapped type.
      */
     public static Object convert(Properties mappings, Object bean)
     throws IntrospectionException, ClassNotFoundException,
             InstantiationException, IllegalAccessException,
             InvocationTargetException, MappingException {
         IdentityHashMap<Object,Object> instances = new IdentityHashMap<Object,Object>();
         
         return convertInternal(instances, mappings, bean);
     }
     
     private static boolean isInterface(Class interfaceClass, Class check) {
         return check.equals(interfaceClass) ||
                 arrayContains(check.getInterfaces(), interfaceClass);
     }
     
     private static boolean arrayContains(Object[] array, Object find) {
         for(Object match : array) {
             if((match == find) || match.equals(find)) {
                 return true;
             }
         }
         
         return false;
     }
     
     private static void convertCollection(
             IdentityHashMap<Object,Object> instances, Properties mappings,
             Collection source, Collection destination)
             throws IntrospectionException, ClassNotFoundException,
             InstantiationException, IllegalAccessException,
             InvocationTargetException, MappingException {
         if(source == null) {
             return;
         }
         
         for(Iterator it = source.iterator(); it.hasNext();) {
             Object o = it.next();
             
             if(!arrayContains(BASE_TYPES, o.getClass())) {
                 o = convertInternal(instances, mappings, o);
             }
             
             destination.add(o);
         }
     }
     
     private static Object convertInternal(
             IdentityHashMap<Object,Object> instances, Properties mappings,
             Object bean)
             throws IntrospectionException, ClassNotFoundException,
             InstantiationException, IllegalAccessException,
             InvocationTargetException, MappingException {
         // if null
         if(bean == null) {
             return null;
         }
         
         // if we have already seen this instance.
         if(instances.containsKey(bean)) {
             return instances.get(bean);
         }
         
         // if this is an array, backstep the array and return it.
         if(bean.getClass().isArray()) {
             Object[] beans = (Object[]) bean;
             Class arrayClass = resolveArray(mappings, bean);
             Object[] destination = (Object[]) Array.newInstance(
                     arrayClass, beans.length);
             
             for(int i = 0; i < beans.length; i++) {
                 destination[i] = convertInternal(instances, mappings, beans[i]);
             }
             
             return destination;
         }
         
         // if this is a primitve or a common type, just return it.
         if(
                 bean.getClass().isPrimitive() ||
                 arrayContains(BASE_TYPES, bean.getClass())) {
             return bean;
         }
         
         if(
                 isInterface(Map.class, bean.getClass() )) {
             Map map = (Map) resolveMapType(
                     bean.getClass(), bean.getClass() ).newInstance();
             convertMap(instances, mappings, (Map) bean, map);
             return map;
         } else if(
                 isInterface(List.class, bean.getClass() )) {
             List list = (List) resolveListType(
                     bean.getClass(), bean.getClass()).newInstance();
             convertCollection(
                     instances, mappings, (List) bean, list);
             return list;
         } else if(
                 isInterface(Set.class, bean.getClass() )) {
             Set list = (Set) resolveSetType(
                     bean.getClass(), bean.getClass()).newInstance();
             convertCollection(
                     instances, mappings, (Set) bean, list);
             return list;
         } else if(
                 isInterface(Collection.class, bean.getClass() ) ) {
             Collection collection = (Collection) resolveCollecitonType(
                     bean.getClass(), bean.getClass() ).newInstance();
             convertCollection(
                     instances, mappings, (Collection) bean,
                     collection);
             return collection;
         }
         
         // if we have gotten here,
         // this is a class that requires resolution mapping.
         Class destinationClass = resolveClass(mappings, bean.getClass());
        //System.out.println("Got "+destinationClass+" for "+bean.getClass() );
         if(destinationClass == null) {
             throw new MappingException(
                     "Unable to resolve class" + bean.getClass().getName());
         }
         
         Object dest = destinationClass.newInstance();
         
         // store the instance so it is there when we recurse into the properties.
         instances.put(bean, dest);
         
         HashMap<String,Object> sourceProperties = inspectObject(bean);
         HashMap<String,Object> destinationProperties = inspectObject(dest);
         
         for(
                 Iterator<String> it = sourceProperties.keySet().iterator();
         it.hasNext();) {
             String propertyName = it.next();
             
             if(!destinationProperties.containsKey(propertyName)) {
                 continue;
             }
             
             //System.out.println(destinationClass.getName() + " " + propertyName);
             
             Object sourceAccessor = sourceProperties.get(propertyName);
             Object destinationAccessor = destinationProperties.get(
                     propertyName);
             Class valueClass = null;
             Object valueObject = null;
             
             if(sourceAccessor instanceof Field) {
                 Field f = (Field) sourceAccessor;
                 valueClass = f.getType();
                 valueObject = f.get(bean);
             } else {
                 PropertyDescriptor pd = (PropertyDescriptor) sourceAccessor;
                 valueClass = pd.getPropertyType();
                 valueObject = pd.getReadMethod().invoke(bean);
             }
             
             Class valueDestinationClass = null;
             
             if(destinationAccessor instanceof Field) {
                 Field f = (Field) destinationAccessor;
                 valueDestinationClass = f.getType();
                 
                 if(
                         isInterface(Map.class, valueClass) &&
                         isInterface(Map.class, valueDestinationClass)) {
                     Map map = (Map) resolveMapType(
                             valueClass, valueDestinationClass).newInstance();
                     convertMap(instances, mappings, (Map) valueObject, map);
                     f.set(dest, map);
                 } else if(
                         isInterface(List.class, valueClass) &&
                         isInterface(List.class, valueDestinationClass)) {
                     List list = (List) resolveListType(
                             valueClass, valueDestinationClass).newInstance();
                     convertCollection(
                             instances, mappings, (List) valueObject, list);
                     f.set(dest, list);
                 } else if(
                         isInterface(Set.class, valueClass) &&
                         isInterface(Set.class, valueDestinationClass)) {
                     Set set = (Set) resolveSetType(
                             valueClass, valueDestinationClass).newInstance();
                     convertCollection(
                             instances, mappings, (Set) valueObject, set);
                     f.set(dest, set);
                 } else if(
                         isInterface(Collection.class, valueClass) &&
                         isInterface(Collection.class, valueDestinationClass)) {
                     Collection collection = (Collection) resolveCollecitonType(
                             valueClass, valueDestinationClass).newInstance();
                     convertCollection(
                             instances, mappings, (Collection) valueObject,
                             collection);
                     f.set(dest, collection);
                 } else if(valueClass == valueDestinationClass) {
                     f.set(dest, valueObject);
                 } else if(
                         (valueDestinationClass == resolveClass(
                         mappings, valueClass)) ||
                         (valueDestinationClass.isArray() &&
                         valueClass.isArray())) {
                     f.set(
                             dest, convertInternal(instances, mappings, valueObject));
                 } else {
                     continue;
                 }
             } else { // Destination accessor is a property
                 PropertyDescriptor pd = (PropertyDescriptor) destinationAccessor;
                 valueDestinationClass = pd.getPropertyType();
                 
                 if(
                         isInterface(Map.class, valueClass) &&
                         isInterface(Map.class, valueDestinationClass)) {
                     Map map = (Map) resolveMapType(
                             valueClass, valueDestinationClass).newInstance();
                     convertMap(instances, mappings, (Map) valueObject, map);
                     pd.getWriteMethod().invoke(dest, map);
                 } else if(
                         isInterface(List.class, valueClass) &&
                         isInterface(List.class, valueDestinationClass)) {
                     List list = (List) resolveListType(
                             valueClass, valueDestinationClass).newInstance();
                     convertCollection(
                             instances, mappings, (List) valueObject, list);
                     pd.getWriteMethod().invoke(dest, list);
                 } else if(
                         isInterface(Set.class, valueClass) &&
                         isInterface(Set.class, valueDestinationClass)) {
                     Set set = (Set) resolveSetType(
                             valueClass, valueDestinationClass).newInstance();
                     convertCollection(
                             instances, mappings, (Set) valueObject, set);
                     pd.getWriteMethod().invoke(dest, set);
                 } else if(
                         isInterface(Collection.class, valueClass) &&
                         isInterface(Collection.class, valueDestinationClass)) {
                     Collection collection = (Collection) resolveCollecitonType(
                             valueClass, valueDestinationClass).newInstance();
                     convertCollection(
                             instances, mappings, (Collection) valueObject,
                             collection);
                     pd.getWriteMethod().invoke(dest, collection);
                 } else if(valueClass == valueDestinationClass) {
                     pd.getWriteMethod().invoke(dest, valueObject);
                 } else if(
                         (valueDestinationClass == resolveClass(
                         mappings, valueClass)) ||
                         (valueDestinationClass.isArray() &&
                         valueClass.isArray())) {
                     pd.getWriteMethod().invoke(
                             dest, convertInternal(instances, mappings, valueObject));
                 } else {
                     continue;
                 }
             }
         }
         
         return dest;
     }
     
     private static void convertMap(
             IdentityHashMap<Object,Object> instances, Properties mappings,
             Map source, Map destination)
             throws IntrospectionException, ClassNotFoundException,
             InstantiationException, IllegalAccessException,
             InvocationTargetException, MappingException {
         if(source == null) {
             return;
         }
         
         for(
                 Iterator<Entry<Object,Object>> it = source.entrySet().iterator();
         it.hasNext();) {
             Entry<Object,Object> entry = it.next();
             Object key = entry.getKey();
             
             if(!arrayContains(BASE_TYPES, key.getClass())) {
                 key = convertInternal(instances, mappings, key);
             }
             
             Object value = entry.getValue();
             
             if(!arrayContains(BASE_TYPES, value.getClass())) {
                 value = convertInternal(instances, mappings, value);
             }
             
             destination.put(key, value);
         }
     }
     
     private static HashMap<String,Object> inspectObject(Object o)
     throws IntrospectionException {
         if(inspections.containsKey(o.getClass())) {
             return inspections.get(o.getClass());
         }
         
         PropertyDescriptor[] pds = Introspector.getBeanInfo(o.getClass())
         .getPropertyDescriptors();
         HashMap<String,Object> values = new HashMap<String,Object>();
         
         for(PropertyDescriptor pd : pds) {
             if(
                     pd.getName().equals("class") ||
                     (pd.getReadMethod() == null) ||
                     (pd.getWriteMethod() == null) ||
                     ((pd.getReadMethod().getModifiers() & Modifier.PUBLIC) == 0) ||
                     ((pd.getWriteMethod().getModifiers() & Modifier.PUBLIC) == 0)) {
                 continue;
             }
             
             values.put(pd.getName(), pd);
         }
         
         for(Field field : o.getClass().getFields()) {
             if(
                     ((field.getModifiers() & Modifier.PUBLIC) != 0) &&
                     ((field.getModifiers() & Modifier.FINAL) == 0) &&
                     ((field.getModifiers() & Modifier.STATIC) == 0) &&
                     (values.get(field.getName()) == null)) {
                 values.put(field.getName(), field);
             }
         }
         
         inspections.put(o.getClass(), values);
         
         return values;
     }
     
     private static Class resolveArray(Properties mappings, Object bean)
     throws ClassNotFoundException {
         int arrayDepth = 0;
         Class clazz = bean.getClass().getComponentType();
         
         while(clazz.isArray()) {
             clazz = clazz.getComponentType();
             arrayDepth++;
         }
         
         if(!clazz.isPrimitive() && !arrayContains(BASE_TYPES, clazz)) {
             clazz = resolveClass(mappings, clazz);
             
             if(clazz == null) {
                 return null;
             }
         }
         
         Object array = null;
         
         for(int i = 0; i < arrayDepth; i++) {
             array = Array.newInstance(clazz, 0);
             clazz = array.getClass();
         }
         
         return clazz;
     }
     
     private static Class resolveClass(Properties mappings, Class clazz)
     throws ClassNotFoundException {
         //System.out.println("Resolving class: " + clazz.getName());
         assert ((mappings != null) && (mappings.size() > 0));
         assert (clazz != null);
         
         if(mappings.containsKey(clazz.getName())) {
             return Class.forName(mappings.getProperty(clazz.getName()));
         } else if(mappings.containsValue(clazz.getName())) {
             for(
                     Iterator<Entry<Object,Object>> it = mappings.entrySet()
                     .iterator();
             it.hasNext();) {
                 Entry entry = it.next();
                 
                 if(entry.getValue().equals(clazz.getName())) {
                    return Class.forName(entry.getKey().toString());
                 }
             }
         } else if(mappings.containsKey(trimPackage(clazz.getName()) + ".*")) {
             String newClass = trimPackage(
                     mappings.getProperty(trimPackage(clazz.getName()) + ".*")) +
                     "." + clazz.getSimpleName();
             //System.out.println("Mapped to: " + newClass);
             
             return Class.forName(newClass);
         } else if(mappings.containsValue(trimPackage(clazz.getName()) + ".*")) {
             for(
                     Iterator<Entry<Object,Object>> it = mappings.entrySet()
                     .iterator();
             it.hasNext();) {
                 Entry entry = it.next();
                 
                 if(entry.getValue().equals(trimPackage(clazz.getName()) + ".*")) {
                     String newClass = trimPackage(entry.getKey().toString()) + "." +
                             clazz.getSimpleName();
                     //System.out.println( "Mapped to: "+newClass);
                     return Class.forName( newClass );
                 }
             }
         }
         
         return null;
     }
     
     private static Class resolveCollecitonType(Class source, Class destination) {
         if(
                 source.equals(Collection.class) &&
                 destination.equals(Collection.class)) {
             return ArrayList.class;
         } else if(destination.equals(Collection.class)) {
             return source;
         } else {
             return destination;
         }
     }
     
     private static Class resolveListType(Class source, Class destination) {
         if(source.equals(List.class) && destination.equals(List.class)) {
             return ArrayList.class;
         } else if(destination.equals(List.class)) {
             return source;
         } else {
             return destination;
         }
     }
     
     private static Class resolveMapType(Class source, Class destination) {
         if(source.equals(Map.class) && destination.equals(Map.class)) {
             return HashMap.class;
         } else if(destination.equals(Map.class)) {
             return source;
         } else {
             return destination;
         }
     }
     
     private static Class resolveSetType(Class source, Class destination) {
         if(source.equals(Set.class) && destination.equals(Set.class)) {
             return HashSet.class;
         } else if(destination.equals(Set.class)) {
             return source;
         } else {
             return destination;
         }
     }
     
     private static String trimPackage(String packageString) {
         return packageString.substring(0, packageString.lastIndexOf("."));
     }
 }
