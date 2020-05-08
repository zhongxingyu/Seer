 package eu.dm2e.ws.grafeo.gom;
 
 import eu.dm2e.ws.grafeo.GResource;
 import eu.dm2e.ws.grafeo.GValue;
 import eu.dm2e.ws.grafeo.Grafeo;
 import eu.dm2e.ws.grafeo.annotations.Namespaces;
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.annotations.RDFId;
 import eu.dm2e.ws.grafeo.annotations.RDFProperty;
 import org.apache.commons.beanutils.PropertyUtils;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.ParameterizedType;
 import java.net.URI;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * The object mapper contains all functionality for the serialization and deserialization
  * of objects (POJOS) as RDF. The object mapper contains a cache to avoid circles
  * in the object graph, i.e., the object mapper should be reused as long as the same
  * instances of objects are used.
  *
  * User: kai
  * Date: 5/28/13
  * Time: 8:04 AM
  *
  */
 public class ObjectMapper {
     private Set<Object> alreadyAdded = new HashSet<>();
     Logger log = Logger.getLogger(getClass().getName());
     Grafeo grafeo;
 
     /**
      * The object mapper is always connected to a Grafeo where the
      * mapped objects are stored.
      *
      * @param grafeo
      */
     public ObjectMapper(Grafeo grafeo) {
         this.grafeo = grafeo;
     }
 
     /**
      * Serialized an object to RDF statements and adds the statements
      * to the internal Grafeo.
      *
      * @param object
      * @return
      */
     public GResource addObject(Object object) {
         if (alreadyAdded.contains(object)) return getGResource(object);
         alreadyAdded.add(object);
         setAnnotatedNamespaces(object);
         GResource result = getGResource(object);
 
         log.fine("Subject: " + result);
         String type = object.getClass().getAnnotation(RDFClass.class).value();
         log.fine("Type: " + type);
         result.set("rdf:type", grafeo.resource(type));
         for (Field field : object.getClass().getDeclaredFields()) {
             if (!field.isAnnotationPresent(RDFProperty.class)) {
                 continue;
             }
             log.finest("Field: " + field.getName());
             String property = field.getAnnotation(RDFProperty.class).value();
             Object value;
             try {
                 value = PropertyUtils.getProperty(object, field.getName());
             } catch (NoSuchMethodException e) {
                 log.severe("No getter/setters for " + field.getName() + " property: " + e);
                 continue;
             } catch (InvocationTargetException | IllegalAccessException e) {
                 throw new RuntimeException("An exception occurred: " + e, e);
             }
             if (null == value) {
                 continue;
             }
             // nested annotated object
             if (isAnnotatedObject(value)) {
                 addObject(value);
                 result.set(property, getGResource(value));
                 // unordered list / set items
             } else if (value instanceof Set) {
                 Iterable valueIterable = (Iterable) value;
                 for (Object setItem : valueIterable) {
                     // nested object
                     if (isAnnotatedObject(setItem)) {
                         addObject(setItem);
                         result.set(property, getGResource(setItem));
                         // literal
                     } else {
                         result.set(property, grafeo.literal(setItem));
                     }
                 }
             } else if (value instanceof List) {
                 result.set("rdf:type", grafeo.resource("co:List"));
                 List valueList = (List) value;
                 result.set("co:size", grafeo.literal(valueList.size()));
                 for (int i = 0; i < valueList.size(); i++) {
                     Object listItem = valueList.get(i);
                     String itemPrefix = field.getAnnotation(RDFProperty.class).itemPrefix();
                     GResource listItemResource = grafeo.resource(result.getUri() + "/" + itemPrefix
                             + (i + 1));
                     GResource listItemTargetResource = getGResource(listItem);
                     if (!isAnnotatedObject(listItem)) {
                         continue;
                     }
                     if (i == 0) {
                         result.set("co:first", listItemResource);
                     }
                     if (i == valueList.size() - 1) {
                         result.set("co:last", listItemResource);
                     }
                     listItemResource.set("co:index", grafeo.literal(i + 1));
                     listItemResource.set("co:itemContent", listItemTargetResource);
                     if (i < valueList.size() - 1) {
                         GResource nextlistItemResource = grafeo.resource(result.getUri() + "/"
                                 + itemPrefix + (i + 2));
                         listItemResource.set("co:next", nextlistItemResource);
                     }
                     addObject(listItem);
                     log.fine("" + i);
                 }
 
             // a URI is used, which is usually done to reference resources that are not mapped or
             // for resources that are mapped but should not get instantiated immediately or the
             // full serialization is not desired.
             } else if (value instanceof URI){
                 result.set(property, grafeo.resource((URI) value));
             // literal
             } else {
                 result.set(property, grafeo.literal(value));
             }
         }
         return result;
     }
 
     /**
      * The object cache to avoid circles in the object hierarchy.
      */
     private Map<String,Object> objectCache = new HashMap<>();
 
     private <T> T getSingleObject(Class T, GResource res, String uri) {
 
         // the built object
         T result;
 
         // Cache
         if (objectCache.containsKey(uri)) {
             log.fine("Cache hit!");
             return (T) objectCache.get(uri);
         }
         log.fine("Creating object for URI: " + uri);
 
         // instantiate the class
         try {
             result = (T) T.newInstance();
             objectCache.put(uri, result);
             log.fine("Added to cache, uri: " + uri);
         } catch (InstantiationException | IllegalAccessException | SecurityException e) {
            throw new RuntimeException("An exception occurred with class " + T + e, e);
         }
 
         // iterate fields in the class definition
         for (Field field : result.getClass().getDeclaredFields()) {
         	// Skip synthetic fileds, such as those added by JaCoCo at runtime
             if (field.isSynthetic()) {
             	continue;
             }
             log.finest("Field: " + field.getName());
 
             // if it's a RDF property field
             if (field.isAnnotationPresent(RDFProperty.class)) {
 
                 // the property this field represents
                 String prop = grafeo.expand(field.getAnnotation(RDFProperty.class).value());
 
                 // if it's a Set type
                 if (field.getType().isAssignableFrom(java.util.Set.class)) {
                     log.fine(field.getName() + " is a SET.");
                     ParameterizedType subtype = (ParameterizedType) field.getGenericType();
                     Class<?> subtypeClass = (Class<?>) subtype.getActualTypeArguments()[0];
                     Set propSet = new HashSet();
                     Set<GValue> propValues = res.getAll(prop);
                     for (GValue thisValue : propValues) {
 
                         // Sets can be composed of literals ...
                         if (thisValue.isLiteral()) {
                             Object thisValueTyped = thisValue.getTypedValue(subtypeClass);
                             propSet.add(thisValueTyped);
                             log.fine("Added literal value: " + thisValue.toString());
                         }
 
                         // or resources
                         else {
                             // TODO infinite recursion on doubly-linked resources? Is that fixed by caching?
                             Object nestedObject = getObject(subtypeClass, (GResource) thisValue);
                             propSet.add(nestedObject);
                             log.fine("Added resource value: " + thisValue.resource());
                         }
                     }
 
                     // store the property set
                     try {
                         PropertyUtils.setProperty(result, field.getName(), propSet);
                     } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                         throw new RuntimeException("An exception occurred: " + e, e);
                     }
                 }
 
                 // List type
                 else if (field.getType().isAssignableFrom(java.util.List.class)) {
                     log.fine(field.getName() + " is a LIST.");
                     ParameterizedType subtype = (ParameterizedType) field.getGenericType();
                     Class<?> subtypeClass = (Class<?>) subtype.getActualTypeArguments()[0];
                     int listSize = (Integer) ((null == res.get("co:size"))
                             ? 0
                             : res.get("co:size").getTypedValue(Integer.class));
                     if (listSize == 0) {
                         continue;
                     }
 
                     ArrayList propArray = new ArrayList();
                     GValue first = res.get("co:first");
                     GValue nextValue;
                     for (nextValue = first; nextValue != null ; nextValue = nextValue.get("co:next")) {
                         GResource nextItemRes = nextValue.resource();
                         GValue itemContentValue = nextItemRes.get("co:itemContent");
                         if (null == itemContentValue) {
                             continue;
                         }
                         GResource itemContentRes = itemContentValue.resource();
                         Object itemContentObject = getObject(subtypeClass, itemContentRes);
                         propArray.add(itemContentObject);
                     }
                     try{
                         PropertyUtils.setProperty(result, field.getName(), propArray);
                     } catch (InvocationTargetException | NoSuchMethodException  | IllegalAccessException e) {
                         throw new RuntimeException("An exception occurred: " + e, e);
                     }
 
                 }
 
                 // Literal property
                 else {
                     log.fine(field.getName() + " is a boring " + field.getType());
                     try {
                         GValue propValue = res.get(prop);
                         if (null == propValue) {
                             continue;
                         }
                         log.fine("Property " + prop + " : " + propValue);
                         PropertyUtils.setProperty(result, field.getName(), propValue.getTypedValue(field.getType()));
                     } catch (InvocationTargetException | NoSuchMethodException  | IllegalAccessException e) {
                         throw new RuntimeException("An exception occurred: " + e, e);
                     }
                 }
 
                 // RDFId fields with a prefix
             } else if (field.isAnnotationPresent(RDFId.class) && !res.isAnon()) {
                 String prefix = field.getAnnotation(RDFId.class).prefix();
                 try {
                     String id = uri.replace(prefix, "");
                     Object o = grafeo.literal(id).getTypedValue(field.getType());
                     PropertyUtils.setProperty(result, field.getName(), o);
                 } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
             }
         }
         return result;
     }
 
     /**
      * Deserializes an object from the Grafeo.
      * @param T The class of the desired object
      * @param uri The URI of the resource that is deserialized
      * @param <T>
      * @return An object of class T.
      */
     public <T> T getObject(Class T, URI uri) {
         return getObject(T, grafeo.resource(uri));
     }
 
     /**
      * Deserializes an object from the Grafeo.
      * @param T The class of the desired object
      * @param resStr The URI of the resource that is deserialized
      * @param <T>
      * @return An object of class T.
      */
     public <T> T getObject(Class T, String resStr) {
         return getObject(T, grafeo.resource(resStr));
     }
 
     /**
      * Deserializes an object from the Grafeo based on the URI of an existing GResource.
      * The existing GResource is not changed or updated!
      * @param T The class of the desired object
      * @param res An existing GResource used as URI
      * @param <T>
      * @return An object of class T.
      */
     public <T> T getObject(Class T, GResource res) {
         String uri;
         if (res.isAnon()) {
             uri = res.getAnonId();
         }  else {
             uri = grafeo.expand(res.getUri());
         }
         log.fine("Class: " +T);
         T result = getSingleObject(T, res, uri);
         setAnnotatedNamespaces(result);
         return result;
     }
 
     protected boolean isAnnotatedObject(Object object) {
 //    	System.out.println(object);
         return object.getClass().isAnnotationPresent(RDFClass.class);
     }
 
     protected GResource getGResource(Object object) {
         String uri = null;
 
         for (Field field : object.getClass().getDeclaredFields()) {
         	// Skip synthetic fileds, such as those added by JaCoCo at runtime
             if (field.isSynthetic()) {
             	continue;
             }
             log.finest("Field: " + field.getName());
             if (field.isAnnotationPresent(RDFId.class)) {
                 try {
                     Object id = PropertyUtils.getProperty(object, field.getName());
                     if (null == id || "0".equals(id.toString()) ) return grafeo.createBlank(object.toString());
                     uri = field.getAnnotation(RDFId.class).prefix() + id.toString();
                 } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                     throw new RuntimeException("An exception occurred: " + e, e);
                 }
             }
         }
         if (uri==null) {
             return grafeo.createBlank(object.toString());
         } else {
             uri = grafeo.expand(uri);
             return grafeo.resource(uri);
         }
 
     }
 
     protected void setAnnotatedNamespaces(Object object) {
         String key = null;
         Namespaces annotation = object.getClass().getAnnotation(Namespaces.class);
         if (annotation == null) return;
         for (String s : annotation.value()) {
             if (key == null) {
                 key = s;
             } else {
                 grafeo.setNamespace(key, s);
                 key = null;
             }
         }
 
     }
 }
