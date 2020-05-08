 package com.github.francofabio.vraptor.serialization.jackson;
 
 import java.io.Writer;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.AbstractMap;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.vidageek.mirror.dsl.Mirror;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 
 import br.com.caelum.vraptor.serialization.Serializer;
 import br.com.caelum.vraptor.serialization.SerializerBuilder;
 import br.com.caelum.vraptor.view.ResultException;
 
 public class JacksonSerializer implements SerializerBuilder {
 
     private final ObjectMapper mapper;
     private final NamedTreeNode treeFields;
     private final Writer writer;
     private Class<?> rootClass;
     private Object object;
     private boolean recursive = false;
     private boolean withoutRoot = false;
 
     public JacksonSerializer(Writer writer, ObjectMapper mapper) {
         this.writer = writer;
         this.treeFields = new NamedTreeNode(null, null);
         this.mapper = mapper;
     }
     
     public JacksonSerializer(Writer writer, ObjectMapper mapper, boolean withoutRoot) {
         this(writer, mapper);
         this.withoutRoot = withoutRoot;
     }
 
     @SuppressWarnings("unchecked")
     private static Class<?> getTypeOf(Object obj) {
         if (Collection.class.isAssignableFrom(obj.getClass())) {
             Collection<Object> c = (Collection<Object>) obj;
             for (Object element : c) {
                 if (element != null) {
                     return element.getClass();
                 }
             }
         }
 
         return obj.getClass();
     }
     
     private static String getFieldName(Class<?> type) {
         String fieldName = type.getSimpleName();
         if (fieldName == null || "".equals(fieldName)) {
             return null;
         }
         fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
         if (isCollection(type)) {
             fieldName += "s";
         }
         return fieldName;
     }
 
     private static boolean isNonPojo(Class<?> type) {
         return type.isPrimitive() || type.isEnum() || Number.class.isAssignableFrom(type) || type.equals(String.class)
                 || Date.class.isAssignableFrom(type) || Calendar.class.isAssignableFrom(type)
                || Boolean.class.equals(type) || Character.class.equals(type) || Map.class.isAssignableFrom(type);
     }
 
     private static boolean isCollection(Type type) {
         if (type instanceof ParameterizedType) {
             ParameterizedType ptype = (ParameterizedType) type;
             return Collection.class.isAssignableFrom((Class<?>) ptype.getRawType())
                     || Map.class.isAssignableFrom((Class<?>) ptype.getRawType());
         }
         return Collection.class.isAssignableFrom((Class<?>) type);
     }
 
     protected void includePrimitiveFields(Class<?> clazz, String root) {
         for (Field field : new Mirror().on(clazz).reflectAll().fields()) {
             if (isNonPojo(field.getType())) {
                 String fieldPath = (root != null) ? root + "." + field.getName() : field.getName();
                 addField(fieldPath);
             }
         }
     }
 
     private Class<?> getFieldType(Field f) {
         Type type = f.getGenericType();
 
         if (type instanceof ParameterizedType) {
             ParameterizedType ptype = (ParameterizedType) type;
 
             if (isCollection(type)) {
                 Type atype = ptype.getActualTypeArguments()[0];
 
                 if (atype instanceof TypeVariable<?>) {
                     return (Class<?>) ptype.getRawType();
                 }
 
                 return (Class<?>) atype;
             }
         }
 
         return (Class<?>) type;
     }
 
     private Entry<Field, Object> field(String fieldName, Class<?> clazz) {
         return field(fieldName, clazz, null);
     }
 
     private Entry<Field, Object> field(String fieldName, Class<?> clazz, Object value) {
         String[] path = fieldName.split("\\.");
         Field lastField = null;
         Object lastValue = value;
 
         for (String p : path) {
             lastField = new Mirror().on(clazz).reflect().field(p);
             if (lastField == null) {
                 throw new ResultException("Field " + fieldName + " not found. Class: " + clazz);
             }
             if (value != null) {
                 try {
                     lastValue = new Mirror().on(lastValue).invoke().getterFor(lastField.getName());
                 } catch (Exception e) {
                     throw new ResultException("Unable to retrieve the value of field: " + fieldName, e);
                 }
             }
             clazz = getFieldType(lastField);
         }
 
         return new AbstractMap.SimpleEntry<Field, Object>(lastField, lastValue);
     }
 
     private void addField(String fieldName) {
         // check field
         Entry<Field, Object> fieldEntry = field(fieldName, rootClass);
 
         Class<?> fieldType = getFieldType(fieldEntry.getKey());
         if (!isNonPojo(fieldType)) {
             includePrimitiveFields(fieldType, fieldName);
         } else {
             treeFields.addChild(fieldName);
         }
     }
 
     protected void serializeCollection(ArrayNode arrayNode, NamedTreeNode node, Collection<Object> collection) {
         for (Object o : collection) {
             ObjectNode item = arrayNode.addObject();
             serialize(item, node, o);
         }
     }
 
     @SuppressWarnings({ "unchecked" })
     protected void serialize(ObjectNode jsonNode, NamedTreeNode root, Object value) {
         boolean allowNull = mapper.getSerializationConfig().getSerializationInclusion() != JsonSerialize.Inclusion.NON_NULL;
         for (NamedTreeNode node : root.getChilds()) {
             if (node.containsChilds()) {
                 Entry<Field, Object> entry = field(node.getName(), value.getClass(), value);
                 Object fieldValue = entry.getValue();
 
                 if (fieldValue != null && Collection.class.isAssignableFrom(fieldValue.getClass())) {
                     ArrayNode arrayNode = jsonNode.putArray(entry.getKey().getName());
                     Collection<Object> collection = (Collection<Object>) entry.getValue();
 
                     serializeCollection(arrayNode, node, collection);
                 } else {
                     if (fieldValue != null || (fieldValue == null && allowNull)) {
                         ObjectNode objectNode = jsonNode.putObject(entry.getKey().getName());
                         serialize(objectNode, node, fieldValue);
                     }
                 }
             } else {
                 Entry<Field, Object> entry = field(node.getName(), value.getClass(), value);
                 Object fieldValue = entry.getValue();
 
                 if (fieldValue != null || (fieldValue == null && allowNull)) {
                     jsonNode.putPOJO(entry.getKey().getName(), fieldValue);
                 }
             }
         }
     }
 
     public JacksonSerializer withoutRoot() {
         this.withoutRoot = true;
         return this;
     }
 
     @SuppressWarnings("unchecked")
     public void serialize() {
         ObjectNode rootNode = mapper.createObjectNode();
         if (recursive) {
             if (withoutRoot) {
                 rootNode.POJONode(object);
             } else {
                 rootNode.putPOJO(treeFields.getName(), object);
             }
         } else if (Collection.class.isAssignableFrom(object.getClass()) && !isNonPojo(rootClass)) {
             ArrayNode arrayNode = rootNode.putArray(treeFields.getName());
             Collection<Object> collection = (Collection<Object>) object;
 
             serializeCollection(arrayNode, treeFields, collection);
         } else if (!isNonPojo(rootClass)) {
             ObjectNode dataRoot = (withoutRoot) ? rootNode : rootNode.putObject(treeFields.getName());
             serialize(dataRoot, treeFields, object);
         } else {
             if (withoutRoot) {
                 rootNode.POJONode(object);
             } else {
                 rootNode.putPOJO(treeFields.getName(), object);
             }
         }
 
         try {
             mapper.writeValue(writer, rootNode);
         } catch (Exception e) {
             throw new ResultException("Unable to generate JSON", e);
         }
     }
 
     public Serializer exclude(String... fields) {
         for (String field : fields) {
             treeFields.removeChild(field);
         }
         return this;
     }
 
     public Serializer include(String... fields) {
         for (String fieldName : fields) {
             addField(fieldName);
         }
         return this;
     }
 
     @Override
     public Serializer recursive() {
         recursive = true;
         return this;
     }
 
     @Override
     public <T> Serializer from(T object) {
         return from(object, null);
     }
 
     @Override
     public <T> Serializer from(T object, String alias) {
         this.object = object;
 
         if (alias == null) {
             Class<?> type = getTypeOf(object);
             String name = getFieldName(type);
             if (isCollection(object.getClass())) {
                 name = name + "List";
             }
             treeFields.setName(name);
         } else {
             treeFields.setName(alias);
         }
 
         rootClass = getTypeOf(object);
 
         if (!isNonPojo(rootClass)) {
             includePrimitiveFields(rootClass, null);
         }
 
         return this;
     }
     
 }
