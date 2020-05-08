 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.config.impl;
 
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.eclipse.jgit.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.yaml.snakeyaml.TypeDescription;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.Constructor;
 
 import com.meltmedia.cadmium.core.config.CadmiumConfig;
 import com.meltmedia.cadmium.core.config.ConfigurationNotFoundException;
 import com.meltmedia.cadmium.core.config.ConfigurationParser;
 
 /**
  * <p>A Yaml based implementation of the {@link ConfigurationParser}.</p>
  * <p>The yaml configuration file need to have the root keys be the same 
  * as the environment name.  The next level key needs to match the name 
  * of a key that the configuration uses in the {@link CadmiumConfig} annotation. 
  * All configuration elements below that level need to match the field names 
  * and types or the pojo class that was wired in.</p>
  * <p>The following is an example of a yaml configuration file:<br />
  * <pre>
  * default:
  *   email: !email
 *     &email
  *     jndiName: 'java:/Mail'
  *     sessionStrategy: 'com.meltmedia.cadmium.email.JndiSessionStrategy'
  *     messageTransformer: 'com.meltmedia.cadmium.email.IdentityMessageTransformer'
  * production:
  *   email: !email
 *     <<: *email
  *     jndiName: 'java/ProdMail'
  * </pre>
  * </p>
  * @author John McEntire
  *
  */
 public class YamlConfigurationParser implements ConfigurationParser {
   
   /**
    * The environment key to be used for all default values.
    */
   public static final String DEFAULT = "default";
 
   private final Logger logger = LoggerFactory.getLogger(getClass());
   
   @SuppressWarnings("rawtypes")
   protected List<Class> configurationClasses = new ArrayList<Class>();
   protected Map<String, Map<String, ?>> configuration = new HashMap<String, Map<String, ?>>();
   protected String environment;
   
   public YamlConfigurationParser() {}
 
   /**
    * This will only parse the files with the extensions of <code>.yml</code> or <code>.yaml</code> in the directory specified.
    */
   @Override
   public void parseDirectory(File configurationDirectory) throws Exception {
     if(configurationDirectory != null && configurationDirectory.isDirectory() && configurationDirectory.canRead()) {
       Collection<File> configFiles = FileUtils.listFiles(configurationDirectory, new String[] {"yml", "yaml"}, true);
       Map<String, Map<String, ?>> configurationMap = new HashMap<String, Map<String, ?>>();
       Yaml yamlParser = new Yaml(getClassTags());
       for(File configFile : configFiles) {
         FileReader reader = null;
         try {
           reader = new FileReader(configFile);
           for(Object parsed : yamlParser.loadAll(reader)){
             mergeConfigs(configurationMap, parsed);
           }
         } finally {
           IOUtils.closeQuietly(reader);
         }
       }
       this.configuration = configurationMap;
     } else if(configurationDirectory != null && configurationDirectory.isDirectory()){
       logger.warn("Directory {} cannot be read.", configurationDirectory);
     } else if(configurationDirectory != null){
       logger.warn("Directory {} is not a directory or does not exist.", configurationDirectory);
     } else {
       throw new IllegalArgumentException("The configurationDirectory must be specified.");
     }
   }
 
   /**
    * Merges configurations from an Object into an existing Map.
    * 
    * @param configurationMap
    * @param parsed 
    */
   @SuppressWarnings("unchecked")
   private void mergeConfigs(Map<String, Map<String, ?>> configurationMap,
       Object parsed) {
     if(parsed instanceof Map) {
       Map<?, ?> parsedMap = (Map<?, ?>) parsed;
       for(Object key : parsedMap.keySet()) {
         if(key instanceof String) {
           Map<String, Object> existingValue = (Map<String, Object>) configurationMap.get((String) key);
           if(!configurationMap.containsKey((String) key)) {
             existingValue = new HashMap<String, Object>();
             configurationMap.put((String) key, existingValue);
           }
           Object parsedValue = parsedMap.get(key);
           if(parsedValue instanceof Map) {
             Map<?, ?> parsedValueMap = (Map<?, ?>) parsedValue;
             for(Object parsedKey : parsedValueMap.keySet()) {
               if(parsedKey instanceof String) {
                 existingValue.put((String) parsedKey, parsedValueMap.get(parsedKey));
               }
             }
           }
         }
       }
     }
   }
   
   /**
    * 
    * @return A new Representer Instance with the tags specified by the {@link configurationClasses} list.
    */
   private Constructor getClassTags() {
     Constructor constructor = new YamlLenientConstructor();
     if(configurationClasses != null) {
       for(Class<?> configClass : configurationClasses) {
         CadmiumConfig configAnnotation = configClass.getAnnotation(CadmiumConfig.class);
         if(configAnnotation != null) {
           String key = configAnnotation.value();
           if(StringUtils.isEmptyOrNull(key)) {
             key = configClass.getCanonicalName();
           }
           
           if(key != null) {
             constructor.addTypeDescription(new TypeDescription(configClass, "!" + key));
             logger.debug("Adding configuration tag {} for class {}", "!"+key, configClass);
           }
         }
       }
     }
     return constructor;
   }
 
   @Override
   public <T> T getConfiguration(String key, Class<T> type)
       throws ConfigurationNotFoundException {
     Map<String, ?> envMap = configuration.get(environment);
     Map<String, ?> defaultMap = configuration.get(DEFAULT);
     if(envMap != null && envMap.containsKey(key)) {
       T config = getEnvConfig(envMap, key, type);
       if(config != null) {
         return config;
       }
     }
     if(defaultMap != null && defaultMap.containsKey(key)) {
       T config = getEnvConfig(defaultMap, key, type);
       if(config != null) {
         return config;
       }
     }
     
     throw new ConfigurationNotFoundException("No configuration with the key \""+key+"\" and type \""+type+"\" were found in environment \""+environment+"\".");
   }
   
   /**
    * Helper method used to retrieve the value from a given map if it matches the type specified.
    * 
    * @param configs The map to pull from.
    * @param key The key to pull.
    * @param type The expected type of the value.
    * @return
    */
   private <T> T getEnvConfig(Map<String, ?> configs, String key, Class<T> type) {
     if(configs.containsKey(key)) {
       Object config = configs.get(key);
       if(type.isAssignableFrom(config.getClass())) {
         return type.cast(config);
       }
     }
     return null;
   }
 
   @SuppressWarnings("rawtypes")
   @Override
   public void setConfigurationClasses(Collection<Class> configurationClasses) {
     if(configurationClasses != null) {
       this.configurationClasses.addAll(configurationClasses);
     }
   }
 
   @Override
   public void setEnvironment(String environment) {
     this.environment = environment;
   }
 
 }
