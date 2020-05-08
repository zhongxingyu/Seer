 package com.atlassian.sal.core.pluginsettings;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.*;
 import java.util.Map.Entry;
 
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import org.apache.commons.lang.Validate;
 import org.apache.log4j.Logger;
 
 /**
  * PluginSettings implementation for datastores that only support Strings.  Handles converting Strings into Lists and
  * Properties objects using a '#TYPE_IDENTIFIER' header on the string.
  */
 public abstract class AbstractStringPluginSettings implements PluginSettings
 {
     private static final Logger log = Logger.getLogger(AbstractStringPluginSettings.class);
 
     private static final String PROPERTIES_ENCODING = "ISO8859_1";
     private static final String PROPERTIES_IDENTIFIER = "java.util.Properties";
     private static final String LIST_IDENTIFIER = "#java.util.List";
     private static final String MAP_IDENTIFIER = "#java.util.Map";
     private static final String VERTICAL_TAB = "\f";
 
     /**
      * Puts a setting value.
      *
      * @param key Setting key.  Cannot be null
      * @param value Setting value.  Must be one of {@link String}, {@link List<String>}, {@link Properties}, {@link
      * Map<String, String>}, or null. null will remove the item from the settings.
      * @return The setting value that was over ridden. Null if none existed.
      * @throws IllegalArgumentException if value is not {@link String}, {@link List<String>}, {@link Properties}, {@link
      * Map<String, String>}, or null.
      */
     public Object put(String key, Object value)
     {
         Validate.notNull(key, "The plugin settings key cannot be null");
         if (value == null)
         {
             return remove(key);
         }
 
         final Object oldValue = get(key);
         if (value instanceof Properties)
         {
             final ByteArrayOutputStream bout = new ByteArrayOutputStream();
             try
             {
 
                 final Properties properties = (Properties) value;
                 properties.store(bout, PROPERTIES_IDENTIFIER);
                 putActual(key, new String(bout.toByteArray(), PROPERTIES_ENCODING));
             }
             catch (final IOException e)
             {
                 throw new IllegalArgumentException("Unable to serialize properties", e);
             }
         }
         else if (value instanceof String)
         {
             putActual(key, (String) value);
         }
         else if (value instanceof List)
         {
             final StringBuilder sb = new StringBuilder();
             sb.append(LIST_IDENTIFIER).append("\n");
             for (final Iterator i = ((List) value).iterator(); i.hasNext();)
             {
                 sb.append(i.next().toString());
                 if (i.hasNext())
                     sb.append('\n');
             }
             putActual(key, sb.toString());
         }
         else if (value instanceof Map)
         {
             final StringBuilder sb = new StringBuilder();
             sb.append(MAP_IDENTIFIER).append("\n");
             for (final Iterator<Entry> i = ((Map) value).entrySet().iterator(); i.hasNext();)
             {
                 final Entry entry = i.next();
                 sb.append(entry.getKey().toString());
                 sb.append(VERTICAL_TAB);
                 sb.append(entry.getValue().toString());
                 if (i.hasNext())
                     sb.append('\n');
             }
             putActual(key, sb.toString());
         }
         else
         {
             throw new IllegalArgumentException("Property type: " + value.getClass() + " not supported");
         }
         return oldValue;
     }
 
     /**
      * Gets a setting value. The setting returned should be specific to this context settings object and not cascade the
      * value to a global context.
      *
      * @param key The setting key.  Cannot be null
      * @return The setting value. May be null
      */
     public Object get(String key)
     {
         Validate.notNull(key, "The plugin settings key cannot be null");
         final String val = getActual(key);
         if (val != null && val.startsWith("#" + PROPERTIES_IDENTIFIER))
         {
             final Properties p = new Properties();
             try
             {
                 p.load(new ByteArrayInputStream(val.getBytes(PROPERTIES_ENCODING)));
             }
             catch (final IOException e)
             {
                 throw new IllegalArgumentException("Unable to deserialize properties", e);
             }
             return p;
         }
         else if (val != null && val.startsWith(LIST_IDENTIFIER))
         {
             final String[] items = val.split("\n");
             final ArrayList<String> list = new ArrayList<String>(items.length - 1);
             list.addAll(Arrays.asList(items).subList(1, items.length));
 
             return list;
         }
         else if (val != null && val.startsWith(MAP_IDENTIFIER))
         {
             String nval = val.substring(MAP_IDENTIFIER.length() + 1);
             final HashMap<String, String> map = new HashMap<String, String>();
             final String[] items = nval.split("\n");
             for (String item : items)
             {
                if (item.length() > 0)
                 {
                    String[] pair = item.split(VERTICAL_TAB);
                    if (pair.length != 2)
                    {
                        log.error("Could not parse map element: << " + item + " >> \n" +
                            "Full list: \n" + nval);
                    }
                    else
                    {
                        map.put(pair[0], pair[1]);
                    }    
                 }
             }
 
             return map;
         }
         else
         {
             return val;
         }
     }
 
     /**
      * Removes a setting value
      *
      * @param key The setting key
      * @return The setting value that was removed. Null if nothing was removed.
      */
     public Object remove(String key)
     {
         Validate.notNull(key, "The plugin settings key cannot be null");
         Object oldValue = get(key);
         if (oldValue != null)
         {
             removeActual(key);
         }
         return oldValue;
     }
 
     /**
      * Put the actual value.
      *
      * @param key The key to put it at.
      * @param val The value
      */
     protected abstract void putActual(String key, String val);
 
     /**
      * Get the actual value
      *
      * @param key The key to get
      * @return The value
      */
     protected abstract String getActual(String key);
 
     /**
      * Do the actual remove.  This will only be called if the value already exists.
      *
      * @param key The key to remove
      */
     protected abstract void removeActual(String key);
 }
