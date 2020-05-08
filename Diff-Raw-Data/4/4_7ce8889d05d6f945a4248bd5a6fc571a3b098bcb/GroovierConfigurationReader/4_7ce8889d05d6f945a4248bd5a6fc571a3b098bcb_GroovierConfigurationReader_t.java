 package com.github.enr.clap.impl;
 
 import groovy.util.ConfigObject;
 import groovy.util.ConfigSlurper;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.groovy.runtime.GStringImpl;
 
 import com.github.enr.clap.api.ConfigurationReader;
 import com.google.common.base.Preconditions;
 
 /*
  * read configurations in groovy files, using internally a ConfigSlurper
  */
 public class GroovierConfigurationReader implements ConfigurationReader {
 
     private Map<String, Object> properties = new HashMap<String, Object>();
 
     /**
      * Binded variables in configuration file.
      */
     private Map<String, Object> bindings = new HashMap<String, Object>();
 
     private final String DEFAULT_ENVIROMENT = "development";
 
     private String enviroment = DEFAULT_ENVIROMENT;
 
     private ConfigObject configurationObject;
 
     private List<URL> configurations = new ArrayList<URL>();
 
     /**
      * ogni file aggiunto sovrascrive le proprieta' gia' presenti
      */
     @Override
     public void addConfiguration(URL configuration) {
         configurations.add(configuration);
     }
 
     @Override
     public void reset() {
         configurations.clear();
     }
 
     @Override
     public boolean build() {
         return build(enviroment);
     }
 
     @Override
     public boolean build(String enviroment) {
         ConfigSlurper slurper = new ConfigSlurper(enviroment);
         slurper.setBinding(bindings);
         for (URL configUrl : configurations) {
             ConfigObject processingConfiguration = slurper.parse(configUrl);
             if (processingConfiguration != null) {
                 if (this.configurationObject != null) {
                     this.configurationObject.merge(processingConfiguration);
                 } else {
                     this.configurationObject = processingConfiguration;
                 }
             }
         }
         if (this.configurationObject != null) {
             this.properties = flatConfiguration(configurationObject);
         }
         return true;
     }
 
     /*
      * public <T> T get(String configurationKey, Class<T> type) { String key =
      * Preconditions.checkNotNull(configurationKey);
      * Preconditions.checkState(this.configuration != null); if
      * (configuration.containsKey(key)) { Object property =
      * configuration.get(key); if (property == null) { return null; } Object o =
      * javize(property); return Casts.castOrNull(o, type); } return null; }
      */
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T get(String key) {
         if (key == null) {
             return null;
         }
         Object property = properties.get(key);
         if (property == null) {
             return null;
         }
         Object o = javize(property);
         return (T) o;
     }
 
     /*
      * internally used method to create a flat configuration as map with String
      * keys and raw Object as value.
      */
     @SuppressWarnings("unchecked")
     private Map<String, Object> flatConfiguration(ConfigObject config) {
         Map<String, Object> flatten = new HashMap<String, Object>();
         if (config != null) {
             flatten = config.flatten();
         }
         return flatten;
     }
 
    /*
      * A utility method to load and cast to the given type a configuration data.
      * 
      * @param <T>
      * @param configurationObject
      * @param key
      * @param clazz
      * 
      * 
      *            private <T> T load(ConfigObject configurationObject, String
      *            key, Class<T> clazz) { ConfigObject confObject =
      *            Preconditions.checkNotNull(configurationObject); String
      *            keyString = Preconditions.checkNotNull(key); if
      *            (confObject.containsKey(keyString)) { Object property =
      *            confObject.getProperty(keyString); if (property == null) {
      *            return null; } Object o = javize(property); return
      *            Casts.castOrNull(o, clazz); } return null; }
      */
 
     @Override
     public void addBinding(String key, Object reference) {
         Object value = Preconditions.checkNotNull(reference);
         bindings.put(key, value);
     }
 
     /**
      * Returns a javized reference for the passed object, for some common cases
      * where we don't want the groovy gdk class (ie GStringImpl)
      * 
      * @param reference
      * 
      */
     @SuppressWarnings("unchecked")
     private <T> T javize(Object reference) {
         if (reference == null) {
             return null;
         }
         if (reference instanceof GStringImpl) {
             return (T) reference.toString();
         }
         return (T) reference;
     }
 
     /*
      * class specific method (not in ConfigurationReader) maybe a
      * EnvironmentAwareConfigurationReader interface?
      */
     public void setEnviroment(String enviroment) {
         this.enviroment = enviroment;
     }
 
     @Override
     public Map<String, Object> getAllProperties() {
         return properties;
     }
 
     @Override
     public Map<String, Object> getBulk(String prefix) {
         String startKey = prefix + ".";
         Map<String, Object> bulk = new HashMap<String, Object>();
         for (String key : properties.keySet()) {
             if (key.startsWith(startKey)) {
                 String bulkKey = key.replace(startKey, "");
                 bulk.put(bulkKey, properties.get(key));
             }
         }
         return bulk;
     }
     /*
      * private Map<String, Object> getBulk(String prefix, ConfigObject
      * configObject) { ConfigObject conf =
      * Preconditions.checkNotNull(configObject); String preKey =
      * Preconditions.checkNotNull(prefix); ConfigObject bulkObject = load(conf,
      * preKey, ConfigObject.class); Map<String, Object> bulk = new
      * HashMap<String, Object>(); if (bulkObject != null) { Map<?, ?>
      * bulkpropertiesMap = bulkObject.flatten(); for (Map.Entry<?, ?> entry:
      * bulkpropertiesMap.entrySet()) { String key = entry.getKey().toString();
      * Object value = entry.getValue(); bulk.put(key, value); } } return bulk; }
      */
 
     /*
      * @Override public void parseAppConfiguration(String appConfigurationPath)
      * { }
      * 
      * @Override public void parseDatasetConfiguration(String
      * datasetConfigurationPath) { }
      */
 }
