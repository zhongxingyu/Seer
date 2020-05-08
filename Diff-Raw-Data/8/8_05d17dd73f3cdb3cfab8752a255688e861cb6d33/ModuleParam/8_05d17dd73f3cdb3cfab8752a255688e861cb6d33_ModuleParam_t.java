 /**
  * XWeb project
  * Created by Hamed Abdollahpour
  * https://github.com/abdollahpour/xweb
  */
 
 package ir.xweb.module;
 
 import ir.xweb.util.Tools;
 
 import java.io.File;
 import java.lang.String;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class ModuleParam implements Map<String, Object> {
 
     /**
      * We have 3 data, String, Colletion and ModuleParam
      */
     private final Map<String, Object> data;
 
     private final List<String> defaults;
 
     protected ModuleParam() {
         this(null, null);
     }
 
     protected ModuleParam(final Map<String, String> data) {
         this(data, null);
     }
 
     protected ModuleParam(final Map<String, String> data, final List<String> defaults) {
         this.data = data != null ? new HashMap<String, Object>(data) : new HashMap<String, Object>();
         this.defaults = defaults != null ? defaults : new ArrayList<String>();
     }
 
     protected ModuleParam(final ModuleParam m) {
         this.data = m.data;
         this.defaults = m.defaults;
     }
 
     protected void put(final String name, final String value, final boolean isDefault) {
         final Object oldValue = data.put(name, value);
         if(isDefault) {
             defaults.add(name);
         } else if(oldValue != null) {
             defaults.remove(name);
         }
     }
 
     public String get(String name, String def) {
         return getString(name, def);
     }
 
     private <T> T get(final Class<T> clazz, final String name, final T def) {
         final Object o = this.data.get(name);
         if(o != null) {
             if(clazz.isInstance(o)) {
                 return clazz.cast(o);
             }
         }
         return def;
     }
 
     public String getString(final String name, final String def) {
         return getString(name, null, def);
     }
 
     public String getString(final String name, final String glue, final String def) {
         final Object o = this.data.get(name);
         if(o != null) {
             if(o instanceof String) {
                 final String s = (String)o;
                 if(s.length() > 0) {
                     return s;
                 }
             }
             if(glue != null) {
                 if(o instanceof Collection) {
                     return Tools.implode(glue, (Collection) o);
                 }
             }
 
             return o.toString();
         }
         return def;
     }
 
     public String getString(final String name) {
         return getString(name, null);
     }
 
     public String[] getStrings(final String name, final char... separator) {
         return getStrings(name, new String[0], separator);
     }
 
     public String[] getStrings(final String name, final String[] def, final char... separator) {
         final Collection list = get(Collection.class, name, null);
         if(list != null && list.size() > 0) {
             final String[] strings = new String[list.size()];
 
             final Iterator iterator = list.iterator();
             for(int i=0; i<strings.length; i++) {
                 strings[i] = iterator.next().toString();
             }
 
             return strings;
         }
 
         final String value = getString(name);
         if(value != null && value.length() > 0) {
             final String imploded = new String(separator);
             final String regex = "[" + imploded.replaceAll("([^a-zA-z0-9])", "\\\\$1") + "]+";
             final String[] strings = value.split(regex);
             return strings;
         }
 
         return def;
     }
 
     public Integer getInt(final String name, final Integer def) {
        final Integer i = get(Integer.class, name, null);
         if(i != null) {
             return i;
         }
         final String s = getString(name);
         if(s != null && s.length() > 0) {
             return Integer.parseInt(s);
         }
         return def;
     }
 
     public Integer getInt(final String name) {
         return getInt(name, null);
     }
 
     public Float getFloat(final String name, final Float def) {
         final Float f = get(Float.class, name, null);
         if(f != null) {
             return f;
         }
         final String s = getString(name);
         if(s != null && s.length() > 0) {
             return Float.parseFloat(s);
         }
         return def;
     }
 
     public Float getFloat(final String name) {
         return getFloat(name, null);
     }
 
     public Double getDouble(final String name, final Double def) {
         final Double d = get(Double.class, name, null);
         if(d != null) {
             return d;
         }
         final String s = getString(name);
         if(s != null && s.length() > 0) {
             return Double.parseDouble(s);
         }
         return def;
     }
 
     public Double getDouble(final String name) {
         return getDouble(name, null);
     }
 
     public Long getLong(final String name, final Long def) {
         final Long l = get(Long.class, name, null);
         if(l != null) {
             return l;
         }
         final String s = getString(name);
         if(s != null && s.length() > 0) {
             return Long.parseLong(s);
         }
         return def;
     }
 
     public Long getLong(final String name) {
         return getLong(name, (Long)null);
     }
 
     public Long[] getLongs(final String name, final char... separator) {
         return getLongs(name, new Long[0], separator);
     }
 
     public Long[] getLongs(final String name, final Long[] def, final char... separator) {
         final Collection list = get(Collection.class, name, null);
         if(list != null && list.size() > 0) {
             final Iterator iterator = list.iterator();
             final Object o = iterator.next();
             if(o instanceof Long) {
                 final Long[] longs = new Long[list.size()];
                 longs[0] = (Long)o;
 
                 for(int i=1; i<longs.length; i++) {
                     longs[i] = (Long)iterator.next();
                 }
             }
         }
 
         final String[] strings = getStrings(name, separator);
         if(strings != null && strings.length > 0) {
             final Long[] longs = new Long[strings.length];
             for(int i=0; i<strings.length; i++) {
                 longs[i] = Long.parseLong(strings[i]);
             }
             return longs;
         }
 
         return def;
     }
 
     public Double[] getDoubles(final String name, final char... separator) {
         return getDoubles(name, new Double[0], separator);
     }
 
     public Double[] getDoubles(final String name, final Double[] def, final char... separator) {
         final Collection list = get(Collection.class, name, null);
         if(list != null && list.size() > 0) {
             final Iterator iterator = list.iterator();
             final Object o = iterator.next();
             if(o instanceof Long) {
                 final Double[] doubles = new Double[list.size()];
                 doubles[0] = (Double)o;
 
                 for(int i=1; i<doubles.length; i++) {
                     doubles[i] = (Double)iterator.next();
                 }
             }
         }
 
         final String[] strings = getStrings(name, separator);
         if(strings != null && strings.length > 0) {
             final Double[] doubles = new Double[strings.length];
             for(int i=0; i<strings.length; i++) {
                 doubles[i] = Double.parseDouble(strings[i]);
             }
         }
         return def;
     }
 
     public Byte getByte(final String name, final Byte def) {
        final Byte b = get(Byte.class, name, null);
         if(b != null) {
             return b;
         }
         final String s = getString(name);
         if(s != null && s.length() > 0) {
             return Byte.parseByte(s);
         }
         return def;
     }
 
     public Byte getByte(final String name) {
         return getByte(name, null);
     }
 
     public Boolean getBoolean(final String name, final Boolean def) {
        final Boolean b = get(Boolean.class, name, null);
         if(b != null) {
             return b;
         }
         final String s = getString(name);
         if(s != null && s.length() > 0) {
             return Boolean.parseBoolean(s);
         }
         return def;
     }
 
     public Boolean getBoolean(final String name) {
         return getBoolean(name, null);
     }
 
     public URL getURL(final String name, final URL def) throws IllegalArgumentException {
         final String s = getString(name);
         try {
             return (s == null || s.length() == 0) ? def : new URL(s);
         } catch (Exception ex) {
             throw new IllegalArgumentException("Illegal URL: " + s);
         }
     }
 
     public File getFile(final String name, final File def) {
         String path = getString(name, null);
         return path == null ? def : new File(path);
     }
 
     public File getFile(final String name) {
         return getFile(name, null);
     }
 
     public Locale getLocale(final String name, final Locale def) {
         final String s = getString(name);
         if((s == null || s.length() == 0)) {
             return def;
         }
 
         Locale locale = parseLocale(s);
         if(!isValid(locale)) {
             throw new IllegalArgumentException("Locale is not valid. Name:" + name + ", Value: " + s);
         }
         return locale;
     }
 
     public Locale getLocale(final String name) {
         return getLocale(name, null);
     }
 
     public Date getDate(final String name, final String pattern, final Date def) {
         return getDate(name, pattern, null, def);
     }
 
     public Date getDate(final String name, final String pattern, final TimeZone zone, final Date def) {
         final SimpleDateFormat format = new SimpleDateFormat(pattern);
         if(zone != null) {
             format.setTimeZone(zone);
         }
         final String s = getString(name);
         try {
             return (s == null || s.length() == 0) ? def : format.parse(s);
         } catch (Exception ex) {
             throw new IllegalArgumentException("Value for " + name + " (" + s + ") is not valid for " + pattern + " pattern", ex);
         }
     }
 
     /**
      * http://stackoverflow.com/questions/3684747/how-to-validate-a-locale-in-java
      * @param locale
      * @return
      */
     private boolean isValid(Locale locale) {
         try {
             return locale.getISO3Language() != null && locale.getISO3Country() != null;
         } catch (MissingResourceException e) {
             return false;
         }
     }
 
     /**
      * http://stackoverflow.com/questions/3684747/how-to-validate-a-locale-in-java
      * @param locale
      * @return
      */
     private Locale parseLocale(String locale) {
         if(locale == null) {
             throw new IllegalArgumentException("null locale");
         }
 
         String[] parts = locale.split("_");
         switch (parts.length) {
             case 3: return new Locale(parts[0], parts[1], parts[2]);
             case 2: return new Locale(parts[0], parts[1]);
             case 1: return new Locale(parts[0]);
             default: throw new IllegalArgumentException("Invalid locale: " + locale);
         }
     }
 
     /*public ValidModuleParam validate2(String name, Collection<?> values, boolean required) throws ModuleException {
         if(name == null) {
             throw new IllegalArgumentException("null name");
         }
         String value = data.get(name);
         if(required && (value == null || value.length() == 0)) {
             throw new ModuleException("Illegal parameter: " + name);
         }
 
         if(values != null && (value != null && value.length() > 0)) {
             if(!values.contains(value)) {
                 throw new ModuleException("Invalid parameter. name: " + name + ", value: " + value);
             }
         }
 
         return new ValidModuleParam(data, name);
     }*/
 
     public ValidModuleParam exists(String name) throws ModuleException {
         return validate(name, null, true);
     }
 
     public ValidModuleParam validate(final String name, final String regex, final boolean required) throws ModuleException {
         if(name == null) {
             throw new IllegalArgumentException("null name");
         }
 
         if(required) {
             if(!hasValueFor(name)) {
                 throw new ModuleException("Parameter required but not found: " + name);
             }
         }
 
         final String value = getString(name);
         if(regex != null && (value != null && value.length() > 0)) {
             if(!value.matches(regex)) {
                 throw new ModuleException("Invalid parameter. name: " + name + ", value: " + value);
             }
         }
 
         return new ValidModuleParam(this, name);
     }
 
     @Override
     public int size() {
         return data.size();
     }
 
     @Override
     public boolean isEmpty() {
         return data.isEmpty();
     }
 
     @Override
     public boolean containsKey(Object key) {
         return data.containsKey(key);
     }
 
     public boolean hasValueFor(final Object key) {
         final Object o = data.get(key);
         if(o != null) {
             // if string type, we also check empty string
             if(o instanceof String) {
                 return ((String)o).length() > 0;
             }
             return true;
         }
         return false;
     }
 
     @Override
     public boolean containsValue(Object value) {
         return data.containsValue(value);
     }
 
     /**
      * This method just return string values not sub ModuleParam(s)
      * @param key
      * @return
      */
     @Override
     public Object get(Object key) {
         return data.get(key);
     }
 
     @Override
     public Object put(String key, Object value) {
         return data.put(key, value);
     }
 
     @Override
     public Object remove(Object key) {
         return data.remove(key);
     }
 
     @Override
     public void putAll(Map<? extends String, ? extends Object> m) {
         data.putAll(m);
     }
 
     @Override
     public void clear() {
         data.clear();
     }
 
     @Override
     public Set<String> keySet() {
         return data.keySet();
     }
 
     public boolean isDefaultProperties(final String name) {
         return defaults.contains(name);
     }
 
     @Override
     public Collection<Object> values() {
         return data.values();
     }
 
     @Override
     public Set<Entry<String, Object>> entrySet() {
         return data.entrySet();
     }
 }
