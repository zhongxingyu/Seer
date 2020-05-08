 /*
  * Copyright 2013 Mobile Helix, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.helix.mobile.model;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.helix.mobile.util.EnumDataTypes;
 import static org.helix.mobile.util.EnumDataTypes.BOOLEAN;
 import static org.helix.mobile.util.EnumDataTypes.BYTE;
 import static org.helix.mobile.util.EnumDataTypes.CHAR;
 import static org.helix.mobile.util.EnumDataTypes.DOUBLE;
 import static org.helix.mobile.util.EnumDataTypes.FLOAT;
 import static org.helix.mobile.util.EnumDataTypes.INT;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_ATOMICLONG;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_AUTOMICINTEGER;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_BIGDECMIAL;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_BIGINTEGER;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_BOOLEAN;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_BYTE;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_DOUBLE;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_FLOAT;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_INTEGER;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_LONG;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_SHORT;
 import static org.helix.mobile.util.EnumDataTypes.JAVA_LANG_STRING;
 import static org.helix.mobile.util.EnumDataTypes.LONG;
 import static org.helix.mobile.util.EnumDataTypes.SHORT;
 
 /**
  * Accept as input a class that represents a JSON schema to be transmitted to
  * the client, primarily via the pm:loadCommand JSF tag. Translate this into a
  * generic JSON description of the corresponding schema. The schema differs from
  * actual data in that we are trying to show what is potentially included in a
  * returned data object, not what is returned in any particular load command. If
  * we don't explore the space of what's possible, our schema on the client side
  * would change every time particular fields are or are not present in the data
  * model.
  *
  * The resulting schema is a JSON string that fills in default values for all
  * primitive types, makes all arrays singleton arrays containing a specification
  * of the underlying object type, and makes all referenced objects into JSON
  * specifications of the schema.
  *
  * Object fields that are included in the client-side schema must be marked with
  * the
  *
  * @ClientData annotation. To avoid infinite loops, we only attempt to serialize
  * the schema for each class once.
  *
  * @author shallem
  */
 public class JSONSerializer {
 
     private static final String TYPE_FIELD_NAME = "__hx_type";
     private static final String SCHEMA_TYPE_FIELD_NAME = "__hx_schema_type";
     private static final String SCHEMA_NAME_FIELD_NAME = "__hx_schema_name";
     private static final String KEY_FIELD_NAME = "__hx_key";
     private static final String SORTS_FIELD_NAME = "__hx_sorts";
     private static final String FILTERS_FIELD_NAME = "__hx_filters";
     private static final String GLOBAL_FILTERS_FIELD_NAME = "__hx_global_filters";
     private static final String TEXT_INDEX_FIELD_NAME = "__hx_text_index";
     
     private class GlobalFilterField {
         private String displayName;
         private int[] intValues;
         private String[] stringValues;
         private String[] valueNames;
         
         public GlobalFilterField(String displayName,
                 int[] intValues,
                 String[] stringValues,
                 String[] valueNames) {
             this.displayName = displayName;
             this.intValues = intValues;
             this.stringValues = stringValues;
             this.valueNames = valueNames;
         }
         
         public void serialize(JsonGenerator gen) throws IOException {
             gen.writeStartObject();
             gen.writeStringField("display", displayName);
             
             gen.writeArrayFieldStart("values");
             if (this.intValues != null) {
                 for (int i : intValues) {
                     gen.writeString(Integer.toString(i));
                 }
             } else if (this.stringValues != null) {
                 for (String s : this.stringValues) {
                     gen.writeString(s);
                 }
             }
             gen.writeEndArray();
             
             if (this.valueNames != null) {
                 gen.writeArrayFieldStart("valueNames");
                 for (String s : this.valueNames) {
                     gen.writeString(s);
                 }
                 gen.writeEndArray();
             }
             
             gen.writeEndObject();
         }
     }
     
     
     public JSONSerializer() {
     }
 
     public String serializeObject(Object obj) throws IOException,
             IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException,
             NoSuchMethodException {
         StringWriter outputString = new StringWriter();
         JsonFactory jsonF = new JsonFactory();
         
         JsonGenerator jg = jsonF.createJsonGenerator(outputString);
         this.serializeObject(obj, jg);
         jg.close();
         
         outputString.flush();
         
         return outputString.toString();
     }
     
     public void serializeObject(Object obj,
             JsonGenerator jg) throws IOException,
             IllegalAccessException,
             IllegalArgumentException,
             InvocationTargetException,
             NoSuchMethodException {
         TreeSet<String> visitedClasses = new TreeSet<String>();
         serializeObjectFields(jg, obj, visitedClasses, null);
     }
 
     private void iterateOverObjectField(JsonGenerator jg,
             Object obj,
             Set<String> visitedClasses,
             Method getter) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchMethodException {
         /* Extract the field name. */
         String nxtFieldName = this.extractFieldName(getter.getName());
 
         /* Finally, handle arbitrary object types. Either these objects
          * encapsulate other objects (as evidenced by having ClientData-
          * annotated methods or they have a toString
          */
         Object subObj = (Object) getter.invoke(obj, new Object[]{});
         if (subObj != null && !this.serializeObjectFields(jg, subObj, visitedClasses, nxtFieldName)) {
             /* Should never happen. */
             throw new IOException("Serialization unexpectedly encountered a class with no ClientData: " + subObj.getClass().getName());
         }
     }
     
     private boolean serializeObjectFields(JsonGenerator jg,
             Object obj,
             Set<String> visitedClasses,
             String fieldName) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
         Class<?> c = obj.getClass();
         
         if (this.isSimpleType(c)) {
             if (fieldName != null) {
                 jg.writeFieldName(fieldName);
             }
             this.addSimpleData(jg, obj);
             return true;
         } else if (c.isArray()) {
             if (fieldName != null) {
                 jg.writeArrayFieldStart(fieldName);
             } else {
                 jg.writeStartArray();
             }
             for (Object elem : (Object[]) obj) {
                 this.serializeObjectFields(jg, elem, visitedClasses, null);
             }
             jg.writeEndArray();
             return true;
         } else {
             /* Next, iterate over all methods looking for property getters, of the form
              * get<prop name>. Find those annotated with the ClientData annotation. Presuming
              * the name format is right, convert the method name to a field name and add
              * to the schema. Throw an IO exception is an annotated method has the wrong
              * name format.
              */
             if (fieldName != null) {
                 jg.writeFieldName(fieldName);
             }
             if (this.isDeltaObject(c)) {
                 jg.writeStartObject();
                 
                 /* Mark as a delta object for the client code. */
                 jg.writeFieldName(TYPE_FIELD_NAME);
                 jg.writeNumber(1001);
 
                 Method m = c.getDeclaredMethod("getAdds", new Class<?>[]{});
                 Class<?> returnType = m.getReturnType();
                 jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
                 jg.writeString(returnType.getComponentType().getName());
                 this.iterateOverObjectField(jg, obj, visitedClasses, m);
                 
                 m = c.getDeclaredMethod("getDeletes", new Class<?>[]{});
                 this.iterateOverObjectField(jg, obj, visitedClasses, m);
                 
                 m = c.getDeclaredMethod("getUpdates", new Class<?>[]{});
                 this.iterateOverObjectField(jg, obj, visitedClasses, m);
                 
                 jg.writeEndObject();
                 return true;
             } else if (this.isAggregateObject(c)) {
                 jg.writeStartObject();
                 
                 /* Mark as an aggreate object for the client code. */
                 jg.writeFieldName(TYPE_FIELD_NAME);
                 jg.writeNumber(1003);
                 
                 AggregateObject a = (AggregateObject)obj;
                 for (Map.Entry<String, Object> e : a.getAggregateMap().entrySet()) {
                     this.serializeObjectFields(jg, e.getValue(), visitedClasses, e.getKey());
                 }
                 
                 jg.writeEndObject();
                 return true;
             } else if (this.hasClientDataMethods(c)) {
                 jg.writeStartObject();
                 
                 /* Write the object type so that we can get the Schema back. */
                 jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
                 jg.writeString(c.getName());
                 
                 for (Method m : c.getMethods()) {
                     Annotation clientDataAnnot = m.getAnnotation(org.helix.mobile.model.ClientData.class);
                     if (clientDataAnnot != null) {
                         this.iterateOverObjectField(jg, obj, visitedClasses, m);
                     }
                 }
                 jg.writeEndObject();
                 return true;
             } else {
                 throw new IOException("Cannot serialize an object with no ClientData fields " + obj.getClass().getName());
             }
         }
     }
 
     public String serializeObjectSchema(Class<?> cls) throws IOException {
         TreeSet<String> visitedClasses = new TreeSet<String>();
         StringWriter outputString = new StringWriter();
         JsonFactory jsonF = new JsonFactory();
         JsonGenerator jg = jsonF.createJsonGenerator(outputString);
         
         if (!serializeObjectForSchema(jg, cls, visitedClasses, null, null)) {
             throw new IOException("Attempting to generate schema for an object with no client data.");
         }
 
         jg.close();
         outputString.flush();
         return outputString.getBuffer().toString();
     }
 
     private boolean serializeObjectForSchema(JsonGenerator jg,
             Class<?> c,
             Set<String> visitedClasses,
             String fieldName,
             String alternateName) throws IOException {
         if (fieldName != null &&
                 fieldName.equals("id")) {
             throw new IOException("Object " + c.getName() + " uses the field name 'id', which is reserved for use by PersistenceJS.");
         }
         
         /* Serialize the genericized version of this return type. */
         if (this.isSimpleType(c)) {
             if (fieldName != null) {
                 jg.writeFieldName(fieldName);
             }
             this.addSimpleType(jg, c);
             return true;
         } else if (c.isArray()) {
             /* Handle arrays by recursing over the element type. */
             Class<?> componentType = c.getComponentType();
             if (fieldName != null) {
                 jg.writeArrayFieldStart(fieldName);
             } else {
                 jg.writeStartArray();
             }
             if (!this.serializeObjectForSchema(jg,
                     componentType,
                     visitedClasses,
                     null,
                     alternateName)) {
                throw new IOException("Array types returned by ClientData methods must be simple types or object types with at least one ClientData field. Class " + componentType.getName() + " does not comply.");
             }
             jg.writeEndArray();
             return true;
         } else {
             /* Check is this is a delta object. If so, just iterate over the object type of the
              * getAdds method.
              */
             if (this.isDeltaObject(c)) {
                 try {
                     Method m = c.getDeclaredMethod("getAdds", new Class<?>[]{});
                     Class<?> returnType = m.getReturnType();
                     if (!this.serializeObjectForSchema(jg, returnType, 
                             visitedClasses, fieldName, alternateName)) {
                         /* The object neither has any fields marked as ClientData nor
                          * does it have a toString method - this is not legal.
                         */
                         throw new IOException("Object types must either have fields marked ClientData or have a toString method.");
                     }
                     return true;
                 } catch (NoSuchMethodException ex) {
                     throw new IOException("Invalid contents of DeltaObject. Missing getAdds method.");
                 } catch (SecurityException  ex) {
                     throw new IOException("Invalid contents of DeltaObject. Missing getAdds method.");
                 }
             }
             
             /* Finally, handle arbitrary object types. Either these objects
              * encapsulate other objects (as evidenced by having ClientData-
              * annotated methods or they have a toString
              */
             
             /* Next, iterate over all methods looking for property getters, of the form
              * get<prop name>. Find those annotated with the ClientData annotation. Presuming
              * the name format is right, convert the method name to a field name and add
              * to the schema by recursing. Throw an IO exception is an annotated method has the wrong
              * name format.
              */
             if (this.hasClientDataMethods(c)) {
                 if (fieldName != null) {
                     /* This is an object that will exist in its own table on the client side. */
                     jg.writeFieldName(fieldName);
                 }
                 
                 Map<String, String> sortFields = new TreeMap<String, String>();
                 Map<String, String> filterFields = new TreeMap<String, String>();
                 List<String> indexFields = new LinkedList<String>();
                 Map<String, GlobalFilterField> globalFilterFields = new TreeMap<String, GlobalFilterField>();
                 String keyField = null;
                 
                 jg.writeStartObject();
                 
                 jg.writeFieldName(SCHEMA_NAME_FIELD_NAME);
                 if (alternateName == null) {
                     jg.writeString(c.getName());
                 } else {
                     jg.writeString(alternateName);
                 }
                 
                 /* Prevent infinite loops. If we have already visited this object then
                  * we have already defined its schema. Just return true. However, we do 
                  * need to put in a reference to the master schema so that the client
                  * knows that there is a schema relationship here.
                  */
                 if (visitedClasses.contains(c.getCanonicalName())) {
                     // Indicate that this is, essentially, a forward ref.
                     jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
                     jg.writeNumber(1002);
                     
                     jg.writeEndObject();
                     return true;
                 }
                 visitedClasses.add(c.getCanonicalName());
                 
                 for (Method m : c.getMethods()) {
                     Annotation clientDataAnnot = m.getAnnotation(org.helix.mobile.model.ClientData.class);
                     if (clientDataAnnot != null) {
                         /* Check the method name. Throws an IOException if the name is ill-formed. */
                         String methodName = m.getName();
                         checkMethodName(methodName);
 
                         /* Extract the field name. */
                         String nxtFieldName = this.extractFieldName(methodName);
 
                         /* Determine if this field is a sort field. */
                         Annotation sortAnnot =
                                 m.getAnnotation(org.helix.mobile.model.ClientSort.class);
                         if (sortAnnot != null) {
                             ClientSort cSortAnnot = (ClientSort)sortAnnot;
                             sortFields.put(nxtFieldName, cSortAnnot.displayName());
                         }
                         
                         /* Determine if this field is a filter field. */
                         Annotation filterAnnot =
                                 m.getAnnotation(org.helix.mobile.model.ClientFilter.class);
                         if (filterAnnot != null) {
                             ClientFilter cFilterAnnot = (ClientFilter)filterAnnot;
                             filterFields.put(nxtFieldName, cFilterAnnot.displayName());
                         }
                         
                         /* Determin if this field is a global filter field. */
                         Annotation globalFilterAnnot =
                                 m.getAnnotation(org.helix.mobile.model.ClientGlobalFilter.class);
                         if (globalFilterAnnot != null) {
                             ClientGlobalFilter cFilterAnnot = (ClientGlobalFilter)globalFilterAnnot;
                             globalFilterFields.put(nxtFieldName, new GlobalFilterField(cFilterAnnot.displayName(),
                                     cFilterAnnot.intValues(),
                                     cFilterAnnot.values(),
                                     cFilterAnnot.valueNames()
                                     ));
                         }
                         
                         /* Determine if this field is a key field. */
                         Annotation keyAnnot =
                                 m.getAnnotation(org.helix.mobile.model.ClientDataKey.class);
                         if (keyAnnot != null) {
                             if (keyField != null) {
                                 throw new IOException("Client data can only have one field annotated as a ClientDataKey.");
                             }
                             keyField = nxtFieldName;
                         }
                         
                         /* Determine if this field is an indexed field. */
                         Annotation indexedAnnot =
                                 m.getAnnotation(org.helix.mobile.model.ClientTextIndex.class);
                         if (indexedAnnot != null) {
                             indexFields.add(nxtFieldName);
                         }
                         
                         /* See if there is an annotation indicating a table name other than the class name. */
                         Annotation clientTableAnnot = m.getAnnotation(org.helix.mobile.model.ClientTableName.class);
                         String altName = null;
                         if (clientTableAnnot != null) {
                             altName = ((ClientTableName)clientTableAnnot).tableName();
                         }
 
                         /* Recurse over the method. */
                         Class<?> returnType = m.getReturnType();
                         if (!this.serializeObjectForSchema(jg, returnType, visitedClasses, nxtFieldName, altName)) {
                             /* The object neither has any fields marked as ClientData nor
                              * does it have a toString method - this is not legal.
                             */
                             throw new IOException("Object types must either have fields marked ClientData or have a toString method.");
                         }
                     }
                 }
 
                 /* Store the keys and sort fields in the object schema. */
                 if (keyField == null) {
                     throw new IOException("Client data must have at least one field annotated as a ClientDataKey.");
                 }
                 jg.writeFieldName(KEY_FIELD_NAME);
                 jg.writeString(keyField);
 
                 jg.writeObjectFieldStart(SORTS_FIELD_NAME);
                 for (Entry<String, String> e : sortFields.entrySet()) {
                     jg.writeFieldName(e.getKey());
                     jg.writeString(e.getValue());
                 }
                 jg.writeEndObject();
                 
                 jg.writeObjectFieldStart(FILTERS_FIELD_NAME);
                 for(Entry<String, String> e: filterFields.entrySet()) {
                     jg.writeFieldName(e.getKey());
                     jg.writeString(e.getValue());
                 }
                 jg.writeEndObject();
                 
                 jg.writeObjectFieldStart(GLOBAL_FILTERS_FIELD_NAME);
                 for(Entry<String, GlobalFilterField> ge : globalFilterFields.entrySet()) {
                     jg.writeFieldName(ge.getKey());
                     ge.getValue().serialize(jg);
                 }
                 jg.writeEndObject();
                 
                 jg.writeArrayFieldStart(TEXT_INDEX_FIELD_NAME);
                 for (String s : indexFields) {
                     jg.writeString(s);
                 }
                 jg.writeEndArray();
                 
                 jg.writeEndObject();
                 return true;
             }  else {
                 throw new IOException("Client data must have at least one field annotated as a ClientData field: " + c.getName());
             }
         }
     }
 
     private void checkMethodName(String methodName) throws IOException {
         /* Check the format of the method name. */
         if (!methodName.startsWith("get")
                 && !methodName.startsWith("is")) {
             throw new IOException("All methods annotated with the ClientData annotation should have the form get<field name>.");
         }
         if (methodName.startsWith("get")
                 && methodName.length() < 4) {
             throw new IOException("All getters annotated with the ClientData annotation should have the form get<field name>.");
         }
         if (methodName.startsWith("is")
                 && methodName.length() < 3) {
             throw new IOException("All getters annotated with the ClientData annotation should have the form is<field name>.");
         }
     }
     
     private boolean isNumberType(Class<?> returnType) {
         if (returnType.getSuperclass().equals(java.lang.Number.class)) {
             return true;
         }
         return false;
     }
 
     private boolean isString(Class<?> returnType) {
         if (returnType.equals(java.lang.String.class)) {
             return true;
         }
         return false;
     }
 
     private boolean isBoolean(Class<?> returnType) {
         if (returnType.equals(java.lang.Boolean.class)) {
             return true;
         }
         return false;
     }
 
     private boolean isSimpleType(Class<?> objType) {
         return objType.isPrimitive()
                 || isNumberType(objType)
                 || isString(objType)
                 || isBoolean(objType);
     }
 
     private void addSimpleData(JsonGenerator jg, Object obj)
             throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
         EnumDataTypes dtc;
         try {
             dtc = EnumDataTypes.getEnumFromString(obj.getClass().getName());
         } catch (IllegalArgumentException iae) {
             // Data type unknown
             dtc = EnumDataTypes.UNKNOWN;
         }
         switch (dtc) {
             case BOOLEAN:
             case JAVA_LANG_BOOLEAN:
                 jg.writeBoolean((Boolean) obj);
                 break;
             case BYTE:
             case JAVA_LANG_BYTE:
                 jg.writeNumber((Byte) obj);
                 break;
             case JAVA_LANG_SHORT:
             case SHORT:
                 jg.writeNumber((Short) obj);
                 break;
             case JAVA_LANG_INTEGER:
             case JAVA_LANG_AUTOMICINTEGER:
             case INT:
                 jg.writeNumber((Integer) obj);
                 break;
             case LONG:
             case JAVA_LANG_LONG:
             case JAVA_LANG_ATOMICLONG:
                 jg.writeNumber((Long) obj);
                 break;
             case CHAR:
                 jg.writeRaw('a');
                 break;
             case FLOAT :
             case JAVA_LANG_FLOAT:
                 jg.writeNumber((Float) obj);
             case DOUBLE:
             case JAVA_LANG_DOUBLE:
                 jg.writeNumber((Double) obj);
                 break;
             case JAVA_LANG_STRING:
                 jg.writeString((String) obj);
                 break;
             case JAVA_LANG_BIGINTEGER:
                 jg.writeNumber((BigInteger) obj);
                 break;
             case JAVA_LANG_BIGDECMIAL:
                 jg.writeNumber((BigDecimal) obj);
                 break;
         }
     }
 
     private void addSimpleType(JsonGenerator jg, Class<?> objType) throws IOException {
         EnumDataTypes dtc;
         try {
             dtc = EnumDataTypes.getEnumFromString(objType.getName());
         } catch (IllegalArgumentException iae) {
             dtc = EnumDataTypes.UNKNOWN; // TODO: write error message ?
         }
         switch (dtc) {
             case BOOLEAN:
             case JAVA_LANG_BOOLEAN:
                 jg.writeBoolean(true);
                 break;
             case BYTE:
             case JAVA_LANG_BYTE:
                 jg.writeNumber((int) 1);
                 break;
             case SHORT:
             case JAVA_LANG_SHORT:
                 jg.writeNumber((int) 1);
                 break;
             case JAVA_LANG_INTEGER:
             case JAVA_LANG_AUTOMICINTEGER:
             case INT:
                 jg.writeNumber((int) 1);
                 break;
             case JAVA_LANG_LONG:
             case JAVA_LANG_ATOMICLONG:
             case LONG:
                 jg.writeNumber((long) 1);
                 break;
             case CHAR:
                 jg.writeRaw('a');
                 break;
             case FLOAT:
             case JAVA_LANG_FLOAT:
                 jg.writeNumber((float) 1.0);
             case DOUBLE:
             case JAVA_LANG_DOUBLE:
                 jg.writeNumber((double) 1.0);
                 break;
             case JAVA_LANG_STRING:
                 jg.writeString("empty");
                 break;
             case JAVA_LANG_BIGINTEGER:
                 jg.writeNumber(BigInteger.ONE);
                 break;
             case JAVA_LANG_BIGDECMIAL:
                 jg.writeNumber(BigDecimal.ONE);
                 break;
 
 
         }
     }
 
     private boolean hasClientDataMethods(Class<?> c) {
         for (Method m : c.getMethods()) {
             Annotation clientDataAnnot = m.getAnnotation(org.helix.mobile.model.ClientData.class);
             if (clientDataAnnot
                     != null) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean hasToString(Class<?> c) {
         try {
             Method toStringM = c.getMethod("toString", new Class[]{});
             if (toStringM != null) {
                 return true;
             }
         } catch (Exception e) {
             /* Ignore  - we are just trying to determine if this method exists. */
         }
 
         return false;
     }
 
     private boolean isDeltaObject(Class<?> c) {
         boolean isDelta = false;
         for (Class<?> ifaces : c.getInterfaces()) {
             if (ifaces.equals(org.helix.mobile.model.DeltaObject.class)) {
                 isDelta = true;
                 break;
             }
         }
         return isDelta;
     }
     
     private boolean isAggregateObject(Class<?> c) {
         if (c.getName().equals("org.helix.mobile.model.AggregateObject")) {
             return true;
         }
         return false;
     }
     
     private String extractFieldName(String methodName) {
         int startIdx = 2;
         if (methodName.startsWith("get")) {
             startIdx = 3;
         }
         String fieldName = methodName.substring(startIdx);
         fieldName = Character.toLowerCase(fieldName.charAt(0))
                 + (fieldName.length() > 1 ? fieldName.substring(1) : "");
         return fieldName;
     }
 }
