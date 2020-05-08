 /**
  * 
  */
 package com.github.ansell.propertyutil;
 
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class PropertyUtil
 {
     
     /**
      * Defines oas.properties as the properties resource bundle name.
      * 
      * Default value: oas
      */
     public static final String DEFAULT_PROPERTIES_BUNDLE_NAME = "oas";
     
     private static String PROPERTIES_BUNDLE_NAME = PropertyUtil.DEFAULT_PROPERTIES_BUNDLE_NAME;
     
     /**
      * Internal property cache, used if and when users indicate that they want to use the cache.
      */
     private static final ConcurrentMap<String, String> INTERNAL_PROPERTY_CACHE =
             new ConcurrentHashMap<String, String>();
     
     /**
      * A constant to indicate the default preference for caching properties, or not caching
      * properties.
      */
     public static final boolean DEFAULT_USE_CACHE = true;
     
     /**
      * Clears the internal property cache.
      */
     public static final void clearPropertyCache()
     {
         PropertyUtil.INTERNAL_PROPERTY_CACHE.clear();
     }
     
     /**
      * Checks for the key first in the system vm properties, then in the localisation properties
      * file, by default, "oas.properties", then uses the defaultValue if the location is still
      * unknown.
      * 
      * By default, this method uses the value of PropertyUtils.DEFAULT_USE_CACHE to determine
      * whether to cache results.
      * 
      * @param key
      *            The key to check for first in system vm properties and then in the localisation
      *            properties file
      * @param defaultValue
      *            The value to return if the key does not match any configured value
      * @return the string matching the key
      */
     public static String get(final String key, final String defaultValue)
     {
         return PropertyUtil.getSystemOrPropertyString(key, defaultValue, PropertyUtil.DEFAULT_USE_CACHE);
     }
     
     /**
      * Checks for the key first in the system vm properties, then in the localisation properties
      * file, by default, "oas.properties", then uses the defaultValue if the location is still
      * unknown.
      * 
      * By default, this method uses the value of PropertyUtils.DEFAULT_USE_CACHE to determine
      * whether to cache results.
      * 
      * @param key
      *            The key to check for first in system vm properties and then in the localisation
      *            properties file
      * @param defaultValue
      *            The value to return if the key does not match any configured value
      * @return the string matching the key
      * @deprecated Use {@link #get(String,String)} instead
      */
     @Deprecated
     public static String getProperty(final String key, final String defaultValue)
     {
         return PropertyUtil.get(key, defaultValue);
     }
     
     /**
      * 
      * @return The property bundle name to be used for fetching properties.
      */
     public static String getPropertyBundleName()
     {
         return PropertyUtil.PROPERTIES_BUNDLE_NAME;
     }
     
     /**
      * Checks for the key first in the system vm properties, then in the localisation properties
      * file, by default, "oas.properties", then uses the defaultValue if the location is still
      * unknown.
      * 
      * @param key
      *            The key to check for first in system vm properties and then in the localisation
      *            properties file
      * @param defaultValue
      *            The value to return if the key does not match any configured value
      * @param useCache
      *            Determines whether the result will be fetched from the cache, or if found another
      *            way, will be put into the cache
      * @return the string matching the key
      */
     public static String getSystemOrPropertyString(final String key, final String defaultValue, final boolean useCache)
     {
         // if they want to use the cache, and the cache contains this key, return the value
         if(useCache && PropertyUtil.INTERNAL_PROPERTY_CACHE.containsKey(key))
         {
             return PropertyUtil.INTERNAL_PROPERTY_CACHE.get(key);
         }
         
         // Try to get the property from the system configuration, for example, from, 'java
         // -Dkey=value'
         String result = System.getProperty(key);
         
         if(result == null)
         {
             try
             {
                 // If we were unsuccessful in the cache and the system properties, try to fetch from
                 // oas.properties file on the class path
                 result = ResourceBundle.getBundle(PropertyUtil.PROPERTIES_BUNDLE_NAME).getString(key);
             }
             catch(final MissingResourceException mre)
             {
                 // Do nothing, will use defaultValue in this case
                 ;
             }
         }
         
         // Do not create anything in the cache if they show an intention not to use the cache
         if(useCache && result != null)
         {
             final String putIfAbsent = PropertyUtil.INTERNAL_PROPERTY_CACHE.putIfAbsent(key, result);
             
             if(putIfAbsent != null && !putIfAbsent.equals(result))
             {
                 PropertyUtil.INTERNAL_PROPERTY_CACHE.put(key, result);
             }
         }
         
         // if the property didn't exist, replace it with the default value
         if(result == null)
         {
             result = defaultValue;
         }
         
         return result;
     }
     
     /**
      * Sets the property bundle name to be used for fetching properties to a new value.
      * 
      * @param newPropertyBundleName
      *            The new property bundle name to use for fetching properties.
      */
     public static final void setPropertyBundleName(final String newPropertyBundleName)
     {
         if(newPropertyBundleName == null || newPropertyBundleName.isEmpty())
         {
            PropertyUtil.PROPERTIES_BUNDLE_NAME = PropertyUtil.DEFAULT_PROPERTIES_BUNDLE_NAME;
        }
        else
        {
             PropertyUtil.PROPERTIES_BUNDLE_NAME = newPropertyBundleName;
         }
         
         PropertyUtil.INTERNAL_PROPERTY_CACHE.clear();
     }
     
 }
