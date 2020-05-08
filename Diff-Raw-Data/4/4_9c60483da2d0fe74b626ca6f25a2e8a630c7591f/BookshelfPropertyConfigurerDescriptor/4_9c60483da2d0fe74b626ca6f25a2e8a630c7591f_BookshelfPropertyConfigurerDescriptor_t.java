 package com.bookshelf.client.spring.properties;
 
 import static java.util.Arrays.asList;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.springframework.core.io.InputStreamResource;
 import org.springframework.core.io.Resource;
 
 import com.bookshelf.client.connector.RESTConnector;
 import com.despegar.library.properties.PropertyConfigurerDescriptor;
 
 public class BookshelfPropertyConfigurerDescriptor
     extends PropertyConfigurerDescriptor {
 
     private static final Logger LOGGER = Logger.getLogger(BookshelfPropertyPlaceholderConfigurer.class);
 
     private BookshelfConfigurer bookshelfConfigurer;
 
     public BookshelfPropertyConfigurerDescriptor(String projectName, String moduleName) {
         this(BookshelfConfigurer.DEFAULT_REST_CONNECTOR, projectName, moduleName);
     }
 
     public BookshelfPropertyConfigurerDescriptor(String bookshelfDomain, String projectName, String moduleName,
         String enviroment) {
         this(BookshelfConfigurer.DEFAULT_REST_CONNECTOR, bookshelfDomain, projectName, moduleName, System
             .getProperty(BookshelfConfigurer.ENVIROMENT_SYSTEM_PROPERTY_KEY) != null ? System
             .getProperty(BookshelfConfigurer.ENVIROMENT_SYSTEM_PROPERTY_KEY) : enviroment);
     }
 
     public BookshelfPropertyConfigurerDescriptor(String bookshelfDomain, String projectName, String moduleName) {
         this(BookshelfConfigurer.DEFAULT_REST_CONNECTOR, bookshelfDomain, projectName, moduleName, System
             .getProperty(BookshelfConfigurer.ENVIROMENT_SYSTEM_PROPERTY_KEY));
     }
 
     public BookshelfPropertyConfigurerDescriptor(Class<? extends RESTConnector> restConnector, String projectName,
         String moduleName) {
         this(restConnector, System.getProperty(BookshelfConfigurer.HOST_SYSTEM_PROPERTY_KEY), projectName, moduleName,
             System.getProperty(BookshelfConfigurer.ENVIROMENT_SYSTEM_PROPERTY_KEY));
     }
 
     public BookshelfPropertyConfigurerDescriptor(Class<? extends RESTConnector> restConnector, String bookshelfDomain,
         String projectName, String moduleName, String enviroment) {
 
         this.bookshelfConfigurer = new BookshelfConfigurer(restConnector, bookshelfDomain, projectName, moduleName,
             enviroment) {
             @Override
             public Logger getLogger() {
                 return LOGGER;
             }
         };
     }
 
     @Override
     public List<Resource> getApplicationResources() {
         return this.getEnvironmentResources();
     }
 
     @Override
     public List<Resource> getEnvironmentResources() {
 
         Resource enviromentPropertiesResource = this.getRemotePropertiesAsResource();
 
         if (enviromentPropertiesResource == null) {
             return Collections.emptyList();
         }
 
         return asList(enviromentPropertiesResource);
     }
 
     public Properties getRemoteProperties() {
         Properties dummyProps = new Properties();
 
         for (Entry<String, String> entry : this.bookshelfConfigurer.getPropertiesFromServer().entrySet()) {
 
            if (entry.getValue() != null && entry.getValue().length() > 0) {
                 dummyProps.setProperty(entry.getKey(), entry.getValue());
             }
         }
 
         return dummyProps;
     }
 
     public Resource getRemotePropertiesAsResource() {
         Properties dummyProps = this.getRemoteProperties();
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         try {
             dummyProps.store(out, "");
         } catch (IOException e) {
             return null;
         }
 
         return new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
     }
 
     public String getServerUrl() {
         return this.bookshelfConfigurer.getServerUrl();
     }
 
     public void setContinueWithConnectionErrors(boolean continueWithConnectionErrors) {
         this.bookshelfConfigurer.setContinueWithConnectionErrors(continueWithConnectionErrors);
     }
 
     @Override
     public int getOrder() {
         return HIGHEST_PRECEDENCE;
     }
 }
