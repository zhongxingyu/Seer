 package com.ajitgeorge.flags;
 
 import com.google.common.base.Predicate;
 import org.reflections.Reflections;
 import org.reflections.scanners.FieldAnnotationsScanner;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.nio.charset.Charset;
import java.util.*;
 
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 import static com.google.common.io.Files.newReader;
 
 /**
  * TODO - generate usage
  * TODO - don't require '='
  * TODO = support unspecified value for boolean flag means true
  * TODO - RequiredFlag
  */
 public class Flags {
     private Map<Class, Parser> parsers = Parsers.all();
     private Set<Field> flaggedFields;
     private Map<String, String> properties = new HashMap<String, String>();
 
     public Flags(String packagePrefix) {
         Reflections reflections = new Reflections(packagePrefix, new FieldAnnotationsScanner());
         flaggedFields = reflections.getFieldsAnnotatedWith(Flag.class);
         initializePropertiesMapWithDefaults();
     }
 
     private void initializePropertiesMapWithDefaults() {
         for (Field field : flaggedFields) {
             try {
                 Object value = field.get(null);
 
                 if (value != null) {
                     properties.put(field.getName(), field.get(null).toString());
                 }
             } catch (IllegalAccessException e) {
                 throw new RuntimeException("Field " + field.getName() + " cannot be accessed statically", e);
             }
         }
     }
 
     public List<String> parse(Properties[] propertiesInstances, String[] argv) {
         parse(propertiesInstances);
         return parse(argv);
     }
 
     public List<String> parse(String... argv) {
         List<String> nonFlagArguments = newArrayList();
 
         for (String s : argv) {
             if (s.startsWith("--")) {
                 String[] parts = s.split("=", 2);
                 final String name = parts[0].substring(2);
                 String value = parts[1];
 
                 set(name, value);
             } else if (new File(s).isFile()) {
                 parse(loadProperties(s));
             } else {
                 nonFlagArguments.add(s);
             }
         }
         return nonFlagArguments;
     }
 
     private Properties loadProperties(String s) {
         try {
             Properties properties = new Properties();
             properties.load(newReader(new File(s), Charset.defaultCharset()));
             return properties;
         } catch (IOException e) {
             throw new RuntimeException("couldn't load properties from file " + s, e);
         }
     }
 
     public void parse(Properties... propertiesInstances) {
         for (Properties properties : propertiesInstances) {
             for (String name : properties.stringPropertyNames()) {
                 set(name, properties.getProperty(name));
             }
         }
     }
 
     public Map<String, String> getAllProperties() {
         return Collections.unmodifiableMap(properties);
     }
 
     public String getProperty(String key) {
         return properties.get(key);
     }
 
     private void set(final String name, String value) {
         properties.put(name, value);
 
         Iterable<Field> fields = filter(flaggedFields, new Predicate<Field>() {
             @Override
             public boolean apply(Field elem) {
                 return elem.getAnnotation(Flag.class).value().equals(name);
             }
         });
         for (Field field : fields) {
             try {
                 Parser parser = parsers.get(field.getType());
 
                 if (parser == null) {
                     throw new IllegalArgumentException("flagged field is of unknown type " + field.getType());
                 }
 
                 field.set(null, parser.parse(value));
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
         }
     }
 }
