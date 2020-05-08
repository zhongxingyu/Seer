 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.sakaiproject.nakamura.api.lite.Configuration;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.lite.content.InternalContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Properties;
 
 @Component(immediate = true, metatype = true)
 @Service(value = Configuration.class)
 public class ConfigurationImpl implements Configuration {
 
     @Property(value = "ac")
     private static final String ACL_COLUMN_FAMILY = "acl-column-family";
     @Property(value = "n")
     private static final String KEYSPACE = "keyspace";
     @Property(value = "au")
     private static final String AUTHORIZABLE_COLUMN_FAMILY = "authorizable-column-family";
     @Property(value = "cn")
     private static final String CONTENT_COLUMN_FAMILY = "content-column-family";
    @Property(value = "lk")
     private static final String LOCK_COLUMN_FAMILY = "lock-column-family";
     
     protected static final String DEFAULT_INDEX_COLUMN_NAMES = "au:rep:principalName,au:type,cn:sling:resourceType," +
     		"cn:sakai:pooled-content-manager,cn:sakai:messagestore,cn:sakai:type,cn:sakai:marker,cn:sakai:tag-uuid," +
     		"cn:sakai:contactstorepath,cn:sakai:state,cn:_created,cn:sakai:category,cn:sakai:messagebox,cn:sakai:from," +
     		"cn:sakai:subject";
 
     @Property(value=DEFAULT_INDEX_COLUMN_NAMES)
     protected static final String INDEX_COLUMN_NAMES = "index-column-names";
 
     private static final String DEFAULT_INDEX_COLUMN_TYPES = "cn:sakai:pooled-content-manager=String[],cn:sakai:category=String[]";
 
     @Property(value=DEFAULT_INDEX_COLUMN_TYPES)
     private static final String INDEX_COLUMN_TYPES = "index-column-types";
 
 
     private static final String SHAREDCONFIGPATH = "org/sakaiproject/nakamura/lite/shared.properties";
 
     protected static final String SHAREDCONFIGPROPERTY = "sparseconfig";
     private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationImpl.class);
     public static final String DEFAULT_UUID_FIELD = Repository.SYSTEM_PROP_PREFIX+ "id";
     /**
      * 
      */
     @Property
     protected static final String UUID_FIELD_NAME = "uuid-field-name";
     
 
     private String aclColumnFamily;
     private String keySpace;
     private String authorizableColumnFamily;
     private String contentColumnFamily;
     private String lockColumnFamily;
     private String[] indexColumnNames;
     private Map<String, String> sharedProperties;
     private String[] indexColumnTypes;
 
     @SuppressWarnings("unchecked")
     @Activate
     public void activate(Map<String, Object> properties) throws IOException {
         aclColumnFamily = StorageClientUtils.getSetting(properties.get(ACL_COLUMN_FAMILY), "ac");
         keySpace = StorageClientUtils.getSetting(properties.get(KEYSPACE), "n");
         authorizableColumnFamily = StorageClientUtils.getSetting(properties.get(AUTHORIZABLE_COLUMN_FAMILY), "au");
         contentColumnFamily = StorageClientUtils.getSetting(properties.get(CONTENT_COLUMN_FAMILY), "cn");
         lockColumnFamily = StorageClientUtils.getSetting(properties.get(LOCK_COLUMN_FAMILY), "ln");
 
         // load defaults
         // check the classpath
         sharedProperties = Maps.newHashMap();
         InputStream in = this.getClass().getClassLoader().getResourceAsStream(SHAREDCONFIGPATH);
         if ( in != null ) {
             Properties p = new Properties();
             p.load(in);
             in.close();
             sharedProperties.putAll(Maps.fromProperties(p));
         }
         // Load from a properties file defiend on the command line
         String osSharedConfigPath = System.getProperty(SHAREDCONFIGPROPERTY);
         if ( osSharedConfigPath != null && StringUtils.isNotEmpty(osSharedConfigPath)) {
             File f = new File(osSharedConfigPath);
             if ( f.exists() && f.canRead() ) {
                 FileReader fr = new FileReader(f);
                 Properties p = new Properties();
                 p.load(fr);
                 fr.close();
                 sharedProperties.putAll(Maps.fromProperties(p));
             } else {
                 LOGGER.warn("Unable to read shared config file {} specified by the system property {} ",f.getAbsolutePath(), SHAREDCONFIGPROPERTY);
             }
         }
 
         // make the shared properties immutable.
         sharedProperties = ImmutableMap.copyOf(sharedProperties);
         indexColumnNames = StringUtils.split(getProperty(INDEX_COLUMN_NAMES,DEFAULT_INDEX_COLUMN_NAMES, sharedProperties, properties),',');
         LOGGER.info("Using Configuration for Index Column Names as              {}", Arrays.toString(indexColumnNames));
         indexColumnTypes = StringUtils.split(getProperty(INDEX_COLUMN_TYPES,DEFAULT_INDEX_COLUMN_TYPES, sharedProperties, properties),',');
 
         
         String uuidFieldName = getProperty(UUID_FIELD_NAME,DEFAULT_UUID_FIELD, sharedProperties, properties);
         InternalContent.setUuidField(uuidFieldName);
         
 
 
     }
 
     private String getProperty(String name, String defaultValue,
             Map<String, ?> ...properties  ) {
      // if present in the shared properties, load the default from there.
         String value = defaultValue;
         for ( Map<String, ?> p : properties ) {
             if ( p.containsKey(name) ) {
                 Object v  = p.get(name);
                 if ( v != null ) {
                     value = String.valueOf(v);
                     LOGGER.debug("{} is configured as {}", value);
                 }
             }
         }
         return value;
         
     }
 
     public String getAclColumnFamily() {
         return aclColumnFamily;
     }
 
     public String getKeySpace() {
         return keySpace;
     }
 
     public String getAuthorizableColumnFamily() {
         return authorizableColumnFamily;
     }
 
     public String getContentColumnFamily() {
         return contentColumnFamily;
     }
     
     public String getLockColumnFamily() {
         return lockColumnFamily;
     }
 
     public String[] getIndexColumnNames() {
         return indexColumnNames;
     }
     public Map<String, String> getSharedConfig() {
         return sharedProperties;
     }
 
     public String[] getIndexColumnTypes() {
         return indexColumnTypes;
     }
 
 }
