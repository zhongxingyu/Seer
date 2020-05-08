 package com.jayway.forest.core;
 
 import com.jayway.forest.exceptions.BadRequestException;
 import com.jayway.forest.exceptions.InternalServerErrorException;
 import com.jayway.forest.reflection.ReflectionUtil;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.JSONValue;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  */
 public class JSONHelper {
 
     @SuppressWarnings({ "unchecked" })
 	public Object toJSON(final Object dto ) {
         if ( dto == null ) return null;
         // basic types or ENUM
         if ( dto instanceof Enum ) {
             return dto.toString();
         }
 
         if ( ReflectionUtil.basicTypes.contains( dto.getClass() ) ) {
             return dto;
         }
 
         // JSONArray
         if ( dto instanceof List ) {
             JSONArray arrayResult = new JSONArray();
             for ( Object o : (List<?>)dto ) {
                 arrayResult.add( toJSON(o) );
             }
             return arrayResult;
         }
 
         // JSONObject
         if ( dto instanceof Map ) {
             JSONObject mapResult = new JSONObject();
             Map<?,?> map = (Map<?,?>) dto;
             for (Object key : map.keySet()) {
                 mapResult.put( key, toJSON(map.get(key)) );
             }
             return mapResult;
         }
 
         // JSONObject
         final JSONObject result = new JSONObject();
         for (Field field : dto.getClass().getDeclaredFields() ) {
             if (Modifier.isFinal(field.getModifiers()) ) continue;
             field.setAccessible( true );
             try {
                 Object value = field.get(dto);
                 if ( value != null ) {
                     result.put(field.getName(), value);
                 }
             } catch (IllegalAccessException e) { }
         }
         return result;
     }
 
     public Object[] handleArguments(Method m, InputStream stream) {
         int argumentCount = m.getParameterTypes().length;
         if ( argumentCount == 0 ) return new Object[0];
 
         Object parse = JSONValue.parse(new InputStreamReader(stream));
         if ( parse == null ) throw new BadRequestException();
 
         if ( argumentCount == 1 ) {
             return new Object[] { handleArgument( m.getParameterTypes()[0], m.getGenericParameterTypes()[0], parse ) };
         }
 
         // several arguments must passed in a JSON array
        if ( !(parse instanceof JSONArray) ) new BadRequestException();
         JSONArray argumentArray = (JSONArray) parse;
        if ( argumentArray.size() != argumentCount ) new BadRequestException();
 
         // more than one argument
         Object[] arguments = new Object[ argumentCount ];
         for ( int i=0; i<argumentCount; i++ ) {
             arguments[i] = handleArgument( m.getParameterTypes()[i], m.getGenericParameterTypes()[i], argumentArray.get( i ) );
         }
         return arguments;
 
     }
 
     @SuppressWarnings("unchecked")
 	public <T> T fromJSON( Class<T> clazz, Object jsonValue ) {
         return (T) handleArgument(clazz, clazz, jsonValue);
     }
 
     private Object handleArgument( Class<?> argumentClass, Type type, Object jsonValue ) {
         // basic type
         if ( ReflectionUtil.basicTypes.contains( argumentClass ) ) {
             Object value = basicType(jsonValue, argumentClass);
             if ( argumentClass.isAssignableFrom( value.getClass() )) {
                 return value;
             } else {
                 throw new BadRequestException();
             }
         }
 
         // List
         if ( List.class.isAssignableFrom( argumentClass ) ) {
             ArrayList<Object> list = new ArrayList<Object>();
             if ( jsonValue instanceof JSONArray ) {
                 Type typeArgument;
                 Class<?> typeClass;
                 if ( type instanceof ParameterizedType) {
                     typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                     if ( typeArgument instanceof ParameterizedType ) {
                         typeClass = (Class<?>) ((ParameterizedType) typeArgument).getRawType();
                     } else {
                         typeClass = (Class<?>) typeArgument;
                     }
                 } else {
                     typeArgument = Object.class;
                     typeClass = Object.class;
                 }
                 for ( Object elm : (JSONArray) jsonValue ) {
                     list.add( handleArgument( typeClass, typeArgument, elm ) );
                 }
             } else {
                 throw new BadRequestException();
             }
             return list;
         }
 
         // Map
         if ( Map.class.isAssignableFrom( argumentClass ) ) {
             HashMap<Object,Object> map = new HashMap<Object,Object>();
             if ( jsonValue instanceof JSONObject) {
                 Type keyType;
                 Type valueType;
                 Class<?> keyClass;
                 Class<?> valueClass;
                 if ( type instanceof ParameterizedType ) {
                     keyType = ((ParameterizedType) type).getActualTypeArguments()[0];
                     valueType = ((ParameterizedType) type).getActualTypeArguments()[1];
                     // if typeArgument instanceof ParameterizedType...
                     keyClass = keyType.getClass();
                     valueClass = valueType.getClass();
                 } else {
                     keyType = valueType = keyClass = valueClass = Object.class;
                 }
                 JSONObject obj = (JSONObject) jsonValue;
                 for (Object key : obj.keySet()) {
                     Object value = obj.get( key );
                     map.put( handleArgument( keyClass, keyType, key), handleArgument( valueClass, valueType, value));
                 }
             } else {
                 throw new BadRequestException();
             }
             return map;
         }
 
         // Composite
         if ( jsonValue instanceof JSONObject ) {
             JSONObject json = (JSONObject) jsonValue;
             try {
                 Object composite = argumentClass.newInstance();
                 for ( Field field: argumentClass.getDeclaredFields() ) {
                     if ( Modifier.isFinal(field.getModifiers())) continue;
                     Object value = json.get(field.getName());
                     if ( value == null) continue;
                     field.setAccessible( true );
                     field.set( composite, handleArgument( field.getType(), field.getType(), value));
                 }
                 return composite;
             } catch (InstantiationException e) {
                 throw new InternalServerErrorException();
             } catch (IllegalAccessException e) {
                 throw new InternalServerErrorException();
             }
         } else {
             throw new BadRequestException();
         }
     }
 
     private Object basicType( Object value, Class<?> type ) {
         if ( value instanceof Long && type == Integer.class ) {
             return ((Long) value).intValue();
         } else {
             return value;
         }
     }
 
 }
