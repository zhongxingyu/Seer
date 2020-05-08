 /**
  *
  * Copyright 2012 Rob Mayhew
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.robmayhew.qds;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.util.*;
 
 
 /**
  * <p>The Quick Data Store</p>
  * <p>This class is not thread safe</p>
  * <p>This class is not for long term or production use</p>
  * <p>This class is for quick prototype and demo projects</p>
  * <p>For anything longer term use some form of database</p>
  */
 public class QuickDataStore implements QuickDataStoreInterface
 {
     private String filePath;
 
 
     /**
      * Get the location of the store
      *
      * @return the location
      */
     public String getFilePath()
     {
         return filePath;
     }
 
     /**
      * Build a new data store using the provided location
      *
      * @param location the location
      */
     public QuickDataStore(String location)
     {
         this.filePath = location;
     }
 
     /**
      * <p>Save an object to the store.</p>
      * <p>Note: the object must be made up of java primitives</p>
      * Can be a:
      * <ul>
      * <li>String,boolean,int,long,double</li>
      * <li>A simple java object containing only String, boolean, int,
      * long, and double primitives</li>
      * <li>A <code>java.util.List</code> containing one of the above</li>
      * </ul>
      *
      * @param key   Key to save the object
      * @param value The value to be saved, will overwrite existing if present
      */
     public void save(String key, Object value)
     {
         if (!canIWriteThisObject(value))
             throw new QDSException("I can't write the value type " +
                     value.getClass().getName());
         if (isPrimitive(value.getClass()))
         {
             savePrimitive(key, value);
         } else if (value instanceof List)
         {
             try
             {
                 save(key, (List) value);
             } catch (Exception e)
             {
                 throw new QDSException("Exception saving list ", e);
             }
         } else
         {
             saveObject(key, value);
         }
     }
 
     /**
      * Load an object from the store
      *
      * @param key key the object was saved under
      * @return The object stored or null
      */
     public Object load(String key)
     {
         String jsonString = loadValue(key);
         if (jsonString == null)
             return null;
         try
         {
             QDSJSONObject json = new QDSJSONObject(jsonString);
             String type = json.getString("type");
             if ("list".equals(type))
             {
                 QDSJSONArray array = json.getJSONArray("value");
                 List<Object> list = new ArrayList<Object>();
                 for (int i = 0; i < array.length(); i++)
                 {
                     QDSJSONObject obj = array.getJSONObject(i);
                     list.add(toObject(obj));
                 }
                 return list;
             } else if ("primitive".equals(type))
             {
                 return toPrimitive(json);
             } else if ("object".equals(type))
             {
                 return toObject(json);
             }
             return null;
         } catch (Exception e)
         {
             throw new RuntimeException("Error parsing json " + jsonString, e);
         }
     }
 
 
     //--------------------------------------------------------------------------
     // Utils
     private boolean isPrimitive(Class c)
     {
         String name = c.getName();
         return "java.lang.String".equals(name) ||
                 "double".equals(name) ||
                 "int".equals(name) ||
                 "float".equals(name) ||
                 "boolean".equals(name) ||
                 "java.lang.Integer".equals(name) ||
                 "java.lang.Float".equals(name) ||
                 "java.lang.Boolean".equals(name) ||
                 "java.lang.Double".equals(name) ||
                 "java.lang.Long".equals(name);
     }
 
     private boolean canIWriteThisObject(Object o)
     {
         if (isPrimitive(o.getClass()))
             return true;
         if (o instanceof List)
             return true;
         Class clazz = o.getClass();
         Method[] methods = clazz.getMethods();
         List<String> getter = new ArrayList<String>();
         List<String> setter = new ArrayList<String>();
         for (Method m : methods)
         {
             String g = stripName(m, true);
             String s = stripName(m, false);
             if (g != null)
             {
                 getter.add(g);
                 if (!isPrimitive(m.getReturnType()))
                     nonPrimitiveException(clazz, m);
             }
             if (s != null)
             {
                 setter.add(s);
             }
         }
         if (getter.size() != setter.size())
         {
             StringBuilder missingSetter = new StringBuilder();
             StringBuilder missingGetter = new StringBuilder();
 
             for (String s : getter)
             {
                 if (!setter.contains(s))
                     missingSetter.append(s).append(", ");
             }
             for (String s : setter)
             {
                 if (!getter.contains(s))
                     missingGetter.append(s).append(", ");
             }
             String getterList = "";
             if (missingGetter.length() != 0)
             {
                 getterList = "Missing Getter(s): " + missingGetter.toString().trim();
                 getterList = getterList.substring(0, getterList.length() - 1);
             }
             String setterList = "";
             if (missingSetter.length() != 0)
             {
                 setterList = "Missing Setter(s): " + missingSetter.toString().trim();
                 setterList = setterList.substring(0, setterList.length() - 1);
             }
 
             throw new QDSException("Must have an equal number of " +
                     "getters and setters. " + getter.size() +
                     " != " + setter.size() + "\n" +
                     getterList + " " + setterList);
         }
 
         return true;
     }
 
     private void nonPrimitiveException(Class c, Method m)
     {
         throw new QDSException("Only supports for simple classes that contain primitives." +
                 "\nClass:" + c.getName() + " Method " + m.getName() +
                 " returns " + m.getReturnType().getName() + " a non primitive");
     }
 
 
     //--------------------------------------------------------------------------
     // Save
     private void save(String key, List list)
             throws Exception
     {
         StringWriter sw = new StringWriter();
         QDSJSONWriter writer = new QDSJSONWriter(sw);
         writer.object();
         writer.key("type");
         writer.value("list");
         writer.key("value");
         writer.array();
         for (Object o : list)
         {
             writer.object();
             if (canIWriteThisObject(o))
             {
 
                 if (isPrimitive(o.getClass()))
                 {
                     writer.key("primitive");
                     writer.value(o);
                     writer.key("class");
                     writer.value(o.getClass().getName());
                 } else
                 {
                     writer.key("class");
                     writer.value(o.getClass().getName());
                     writer.key("data");
                     writer.object();
                     writeGetters(writer, o);
                     writer.endObject();
                 }
             }
             writer.endObject();
         }
         writer.endArray();
         writer.endObject();
         writeValue(key, sw.toString());
     }
 
 
     private void writeGetters(QDSJSONWriter writer, Object o)
             throws Exception
     {
         Class clazz = o.getClass();
         Method[] methods = clazz.getMethods();
         for (Method m : methods)
         {
             String g = stripName(m, true);
             if (g != null)
             {
                 writer.key(g);
                 writer.value(m.invoke(o));
             }
         }
     }
 
     private void writeValue(String key, String value) throws QDSException
     {
         File f = new File(filePath);
         File swapFile = new File(filePath + ".swap");
         if (swapFile.exists())
         {
             if (!swapFile.delete())
                 throw new QDSException("Unable to delete swap file "
                         + swapFile.getPath());
         }
         try
         {
 
             renameToSwapFile(f, swapFile);
             if (!swapFile.exists())
             {
                 // First write
                 PrintWriter writer = new PrintWriter(new FileWriter(filePath));
                 try
                 {
                     writer.println(key + "=" + value);
                 } finally
                 {
                     writer.flush();
                     writer.close();
                 }
                 return;
             }
             replaceValueInFile(key, value, swapFile);
         } catch (Exception e)
         {
             throw new RuntimeException("Error writing file", e);
         }
     }
 
     private void replaceValueInFile(String key, String value, File swapFile)
             throws IOException
     {
         PrintWriter writer = new PrintWriter(new FileWriter(filePath));
         BufferedReader reader = new BufferedReader(new FileReader(swapFile));
        boolean valueWritten = false;
         try
         {
             while (reader.ready())
             {
                 String line = reader.readLine();
                 if (line.startsWith(key + "="))
                 {
                     writer.println(key + "=" + value);
                    valueWritten = true;
                 } else
                 {
                     writer.println(line);
                 }
             }
            if(!valueWritten)
                writer.println(key + "=" + value);
         } finally
         {
             reader.close();
             writer.flush();
             writer.close();
             cleanupSwapFile(swapFile);
         }
     }
 
     private void renameToSwapFile(File f, File swapFile)
     {
         if (f.exists())
         {
             if (!f.renameTo(swapFile))
                 throw new RuntimeException("Unable to rename file " +
                         filePath + " for writing");
         }
     }
 
     private void cleanupSwapFile(File swapFile)
     {
         if (!swapFile.delete())
         {
             System.err.println("Unable to delete swap file " + swapFile.getPath());
         }
     }
 
     private String stripName(Method m, boolean getter)
     {
         String[] names = {"get", "is", "set"};
         int pos = -1;
         for (int i = 0; i < names.length; i++)
         {
             if (m.getName().equals("getClass"))
                 return null;
             if (m.getName().startsWith(names[i]))
                 pos = i;
         }
         if (pos == -1)
             return null;
         if (getter && pos == 2)
             return null;
         if (!getter && pos != 2)
             return null;
 
         if (pos == 2)
         {
             Class[] types = m.getParameterTypes();
             if (types == null || types.length != 1)
                 return null;
         } else
         {
             Class c = m.getReturnType();
             if (c == null || c.getName().equals("java.lang.Void"))
                 return null;
         }
         String s = m.getName();
         int startIdx = names[pos].length();
 
         s = s.substring(startIdx);
 
         return s;
     }
 
     private void saveObject(String key, Object value)
     {
         try
         {
             StringWriter sw = new StringWriter();
             QDSJSONWriter writer = new QDSJSONWriter(sw);
             writer.object();
             writer.key("type");
             writer.value("object");
             writer.key("class");
             writer.value(value.getClass().getName());
             writer.key("data");
             writer.object();
             writeGetters(writer, value);
             writer.endObject();
             writer.endObject();
             writeValue(key, sw.toString());
         } catch (Exception e)
         {
             throw new QDSException("Error saving " + key, e);
         }
     }
 
     private void savePrimitive(String key, Object value)
     {
         try
         {
             StringWriter sw = new StringWriter();
             QDSJSONWriter writer = new QDSJSONWriter(sw);
             writer.object();
             writer.key("type");
             writer.value("primitive");
             writer.key("class");
             writer.value(value.getClass().getName());
             writer.key("primitive");
             writer.value(value);
             writer.endObject();
             writeValue(key, sw.toString());
         } catch (Exception e)
         {
             throw new QDSException("Error saving " + key, e);
         }
     }
 
     //--------------------------------------------------------------------------
     // Load
     private Object toObject(QDSJSONObject json)
             throws Exception
     {
         String className = json.getString("class");
         if (isPrimitive(Class.forName(className)))
         {
             return toPrimitive(json);
         }
         return createAndPopulate(json);
     }
 
     private Object createAndPopulate(QDSJSONObject json)
     {
         try
         {
             String className = json.getString("class");
             Class clazz = Class.forName(className);
             Object o = clazz.newInstance();
             QDSJSONObject data = json.getJSONObject("data");
             Iterator keys = data.keys();
             Method[] methods = clazz.getMethods();
             while (keys.hasNext())
             {
                 String key = (String) keys.next();
                 String methodName = "set" + key;
                 for (Method m : methods)
                 {
                     if (m.getName().equals(methodName))
                     {
                         setObjectValue(o, m, key, data);
                     }
                 }
             }
             return o;
         } catch (Exception e)
         {
             throw new QDSException("Error creating " + json, e);
         }
     }
 
     private void setObjectValue(Object obj, Method method,
                                 String key, QDSJSONObject data)
     {
         try
         {
             Class[] types = method.getParameterTypes();
             Class c = types[0];
             String name = c.getName();
             if ("java.lang.String".equals(name))
             {
                 method.invoke(obj, data.optString(key, null));
                 return;
             }
             if ("java.lang.Integer".equals(name))
             {
                 method.invoke(obj, data.getInt(key));
                 return;
             }
             if ("int".equals(name))
             {
                 method.invoke(obj, data.getInt(key));
                 return;
             }
             if ("double".equals(name))
             {
                 method.invoke(obj, data.getDouble(key));
                 return;
             }
             if ("boolean".equals(name))
             {
                 method.invoke(obj, data.getBoolean(key));
                 return;
             }
             if ("java.lang.Boolean".equals(name))
             {
                 method.invoke(obj, data.getBoolean(key));
                 return;
             }
             if ("java.lang.Double".equals(name))
             {
                 method.invoke(obj, data.getDouble(key));
                 return;
             }
             if ("java.lang.Long".equals(name))
             {
                 method.invoke(obj, data.getLong(key));
             }
         } catch (Exception e)
         {
             throw new QDSException("Error setting method " + method.getName()
                     + " json " + data.toString(), e);
         }
     }
 
     private Object toPrimitive(QDSJSONObject json)
             throws Exception
     {
         String name = json.getString("class");
         if ("java.lang.String".equals(name))
             return json.getString("primitive");
         if ("java.lang.Integer".equals(name))
             return json.getInt("primitive");
         if ("double".equals(name))
             return json.getDouble("primitive");
         if ("int".equals(name))
             return json.getInt("primitive");
         if ("float".equals(name))
             return json.getDouble("primitive");
         if ("java.lang.Float".equals(name))
             return json.getDouble("primitive");
         if ("boolean".equals(name))
             return json.getBoolean("primitive");
         if ("java.lang.Boolean".equals(name))
             return json.getBoolean("primitive");
         if ("java.lang.Double".equals(name))
             return json.getDouble("primitive");
         if ("java.lang.Long".equals(name))
             return json.getLong("primitive");
         return null;
     }
 
     private String loadValue(String key)
     {
         File f = new File(filePath);
         if (!f.exists())
             return null;
         String jsonString = null;
         try
         {
             BufferedReader reader = new BufferedReader(new FileReader(filePath));
             try
             {
                 while (reader.ready())
                 {
                     String line = reader.readLine();
                     if (line.startsWith(key + "="))
                     {
                         int i = line.indexOf("=");
                         jsonString = line.substring(i + 1);
                     }
                 }
             } finally
             {
                 reader.close();
             }
         } catch (Exception e)
         {
             throw new RuntimeException("Error loading " + filePath, e);
         }
         return jsonString;
     }
 }
