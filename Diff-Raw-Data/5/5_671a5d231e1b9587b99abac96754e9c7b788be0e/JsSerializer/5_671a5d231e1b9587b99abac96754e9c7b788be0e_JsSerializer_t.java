 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2011 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.js;
 
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.Undefined;
 import org.mozilla.javascript.WrappedException;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.storage.StorableObject;
 import org.rapidcontext.core.type.Channel;
 
 /**
  * An object serializer and unserializer for the JavaScript object
  * notation (JSON) format. This class also provides methods for
  * wrapping dictionary and array object for access inside the
  * JavaScript engine. The object mapping to JavaScript is not exact,
  * and may omit serialization of data in some cases. The following
  * basic requirements must be met in order to serialize an object:<p>
  *
  * <ul>
  *   <li>No circular references are permitted.
  *   <li>String, Integer, Boolean, Array or Dict objects are supported.
  * </ul>
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class JsSerializer {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(JsSerializer.class.getName());
 
     /**
      * Serializes an object into a JavaScript literal. I.e. the
      * string returned can be used as a constant inside JavaScript
      * code.
      *
      * @param obj            the object to convert, or null
      *
      * @return a JavaScript literal
      */
     public static String serialize(Object obj) {
         StringBuffer  buffer = new StringBuffer();
 
         serialize(unwrap(obj), buffer);
         return buffer.toString();
     }
 
     /**
      * Serializes an object into a JavaScript literal. I.e. the
      * serialized result can be used as a constant inside JavaScript
      * code. The serialized result will be written into the specified
      * string buffer.
      *
      * @param obj            the object to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Object obj, StringBuffer buffer) {
         if (obj == null) {
             buffer.append("null");
         } else if (obj instanceof Dict) {
             serialize((Dict) obj, buffer);
         } else if (obj instanceof Array) {
             serialize((Array) obj, buffer);
         } else if (obj instanceof Boolean) {
             buffer.append(obj.toString());
         } else if (obj instanceof Number) {
             serialize((Number) obj, buffer);
         } else if (obj instanceof Date) {
             serialize("@" + ((Date) obj).getTime(), buffer);
         } else if (obj instanceof Class) {
             serialize(((Class) obj).getName(), buffer);
         } else if (obj instanceof StorableObject) {
             serialize(((StorableObject) obj).serialize(), buffer);
         } else {
             serialize(obj.toString(), buffer);
         }
     }
 
     /**
      * Serializes a dictionary into a JavaScript literal. I.e. the
      * serialized result can be used as a constant inside JavaScript
      * code. The serialized result will be written into the specified
      * string buffer.
      *
      * @param dict           the dictionary to convert
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Dict dict, StringBuffer buffer) {
         String[]  keys = dict.keys();
 
         buffer.append("{");
         for (int i = 0; i < keys.length; i++) {
             if (i > 0) {
                 buffer.append(",");
             }
             buffer.append(" ");
             serialize(keys[i], buffer);
             buffer.append(": ");
             serialize(dict.get(keys[i]), buffer);
         }
         if (keys.length > 0) {
             buffer.append(" ");
         }
         buffer.append("}");
     }
 
     /**
      * Serializes an array into a JavaScript literal. I.e. the
      * serialized result can be used as a constant inside JavaScript
      * code. The serialized result will be written into the specified
      * string buffer.
      *
      * @param arr            the array to convert
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Array arr, StringBuffer buffer) {
         buffer.append("[");
         for (int i = 0; i < arr.size(); i++) {
             if (i > 0) {
                 buffer.append(", ");
             }
             serialize(arr.get(i), buffer);
         }
         buffer.append("]");
     }
 
     /**
      * Serializes a string into a JavaScript string literal. I.e.
      * the serialized result can be used as a string constant inside
      * JavaScript code. The serialized result will be written into
      * the specified string buffer.
      *
      * @param str            the string to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(String str, StringBuffer buffer) {
         if (str == null) {
             buffer.append("null");
         } else {
            buffer.append('"');
             for (int i = 0; i < str.length(); i++) {
                 char chr = str.charAt(i);
                 switch (chr) {
                 case '\\':
                     buffer.append("\\\\");
                     break;
                 case '\'':
                     buffer.append("\\'");
                     break;
                 case '\"':
                     buffer.append("\\\"");
                     break;
                 case '\n':
                     buffer.append("\\n");
                     break;
                 case '\r':
                     buffer.append("\\r");
                     break;
                 case '\t':
                     buffer.append("\\t");
                     break;
                 default:
                     if (chr != '<' && 32 <= chr && chr < 127) {
                         buffer.append(chr);
                     } else {
                         buffer.append("\\u");
                         if (chr <= 0x000F) {
                             buffer.append("000");
                         } else if (chr <= 0x00FF) {
                             buffer.append("00");
                         } else if (chr <= 0x0FFF) {
                             buffer.append("0");
                         }
                         buffer.append(Integer.toHexString(chr).toUpperCase());
                     }
                 }
             }
            buffer.append('"');
         }
     }
 
     /**
      * Serializes a number into a JavaScript number literal. I.e.
      * the serialized result can be used as a number constant inside
      * JavaScript code. The serialized result will be written into
      * the specified string buffer.
      *
      * @param num            the number to convert, or null
      * @param buffer         the string buffer to append into
      */
     private static void serialize(Number num, StringBuffer buffer) {
         int     i = num.intValue();
         double  d = num.doubleValue();
 
         if (i == d) {
             buffer.append(i);
         } else {
             // TODO: proper number formatting should be used
             buffer.append(num);
         }
     }
 
     /**
      * Unserializes a JavaScript literal into a Java object. I.e.
      * this method converts a JSON object into the corresponding
      * String, Number, Boolean and/or Data objects.
      *
      * @param str            the string to convert, or null
      *
      * @return the corresponding Java object
      *
      * @throws JsException if the unserialization failed
      */
     public static Object unserialize(String str) throws JsException {
         Context  cx;
         Object   obj;
         String   msg;
 
         cx = ContextFactory.getGlobal().enterContext();
         try {
             str = "(" + str + ")";
             obj = cx.evaluateString(cx.initStandardObjects(),
                                     str,
                                     "unserialize",
                                     1,
                                     null);
             return unwrap(obj);
         } catch (WrappedException e) {
             msg = "Caught unserialization exception for text: " + str;
             LOG.log(Level.WARNING, msg, e);
             throw new JsException(msg, e.getWrappedException());
         } catch (Exception e) {
             msg = "Caught unserialization exception for text: " + str;
             LOG.log(Level.WARNING, msg, e);
             throw new JsException(msg, e);
         } finally {
             Context.exit();
         }
     }
 
     /**
      * Wraps a Java object for JavaScript access. This method only
      * handles String, Number, Boolean and Data instances.
      *
      * @param obj            the object to wrap
      * @param scope          the parent scope
      *
      * @return the wrapped object
      *
      * @see org.rapidcontext.core.data.Array
      * @see org.rapidcontext.core.data.Dict
      */
     public static Object wrap(Object obj, Scriptable scope) {
         if (obj instanceof Dict || obj instanceof Array) {
             return new DataWrapper(obj, scope);
         } else if (obj instanceof Channel) {
             return new ConnectionWrapper((Channel) obj, scope);
         } else {
             return obj;
         }
     }
 
     /**
      * Removes all JavaScript classes and replaces them with the
      * corresponding Java objects. This method will use instances of
      * Dict and Array to replace native JavaScript objects and arrays.
      * Also, it will replace both JavaScript "null" and "undefined"
      * with null. Any Dict or Array object encountered will be
      * traversed and copied recursively. Other objects will be
      * returned as-is.
      *
      * @param obj            the object to unwrap
      *
      * @return the unwrapped object
      *
      * @see org.rapidcontext.core.data.Array
      * @see org.rapidcontext.core.data.Dict
      */
     public static Object unwrap(Object obj) {
         if (obj instanceof DataWrapper) {
             return ((DataWrapper) obj).getData();
         } else if (obj instanceof ConnectionWrapper) {
             return ((ConnectionWrapper) obj).getConnection();
         } else if (obj instanceof NativeArray) {
             NativeArray nativeArr = (NativeArray) obj;
             int length = (int) nativeArr.getLength();
             Array arr = new Array(length);
             for (int i = 0; i < length; i++) {
                 arr.set(i, unwrap(nativeArr.get(i, nativeArr)));
             }
             return arr;
         } else if (obj instanceof Scriptable) {
             Scriptable scr = (Scriptable) obj;
             Object[] keys = scr.getIds();
             Dict dict = new Dict(keys.length);
             for (int i = 0; i < keys.length; i++) {
                 String str = keys[i].toString();
                 Object value = null;
                 if (keys[i] instanceof Integer) {
                     value = scr.get(((Integer) keys[i]).intValue(), scr);
                 } else {
                     value = scr.get(str, scr);
                 }
                 dict.set(str, unwrap(value));
             }
             return dict;
         } else if (obj instanceof Undefined || obj == Scriptable.NOT_FOUND) {
             return null;
         } else if (obj instanceof Array) {
             Array oldArr = (Array) obj;
             Array newArr = new Array(oldArr.size());
             for (int i = 0; i < oldArr.size(); i++) {
                 newArr.set(i, unwrap(oldArr.get(i)));
             }
             return newArr;
         } else if (obj instanceof Dict) {
             Dict oldDict = (Dict) obj;
             Dict newDict = new Dict(oldDict.size());
             String[] keys = oldDict.keys();
             for (int i = 0; i < keys.length; i++) {
                 String key = keys[i].toString();
                 newDict.set(key, unwrap(oldDict.get(key)));
             }
             return newDict;
         } else {
             return obj;
         }
     }
 }
