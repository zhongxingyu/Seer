 /*
  * This is a utility project for wide range of applications
  * 
  * Copyright (C) 8  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.bean.spring;
 
 import com.smartitengineering.util.bean.PropertiesLocator;
 import java.io.IOException;
 import java.util.Properties;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.core.PriorityOrdered;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 
 /**
  * This class will mainly search for a designated properties file at locations
  * predefined by system (that is this module) and user through app context. <p />
  * Preconfigured locations according to ascending priority is -
  * <ul>
  *  <li>System properties (if enabled)</li>
  *  <li>Classpath for default resource (defaultResourceSuffix appended to the path)</li>
  *  <li>Current working directory</li>
  *  <li>Home directory</li>
  *  <li>User specified directory in order specified</li>
  * </ul>
  * 
  * @author imyousuf
  */
 public class PropertiesLocatorConfigurer
     extends PropertyPlaceholderConfigurer
     implements BeanFactoryPostProcessor,
                PriorityOrdered,
                BeanNameAware,
                BeanFactoryAware {
 
     private boolean ignoreResourceNotFound = false;
     private final PropertiesLocator locator = new PropertiesLocator();
 
     public PropertiesLocatorConfigurer() {
     }
 
     /**
      * Loads properties file from locations as it is supposed.
      * @param props The properties object that is filled.
      * @throws java.io.IOException If failed to load the properties or error
      *                              reading a resoure
      */
     @Override
     protected void loadProperties(Properties props)
         throws IOException {
         boolean resourceFound;
         resourceFound = locator.loadProperties(props);
         if (!resourceFound && !this.ignoreResourceNotFound) {
            throw new IOException();
         }
     }
 
     /**
      * This operation is restricted from this configurer.
      * @param location
      */
     protected String getDefaultResourceSuffix() {
         return locator.getDefaultResourceSuffix();
     }
 
     /**
      * Set the suffix for the default resource file
      * @param defaultResourceSuffix The suffix of he default resource
      */
     public void setDefaultResourceSuffix(String defaultResourceSuffix) {
         locator.setDefaultResourceSuffix(defaultResourceSuffix);
     }
 
     /**
      * Retrieves the context of the search. The context will be added before the
      * for every resource search. It is primarily useful if you multiple config
      * group for single application.
      * @return The context for current configurer
      */
     protected String getResourceContext() {
         if (locator.getResourceContext() == null) {
             return "";
         }
         return locator.getResourceContext();
     }
 
     /**
      * Sets the context for the resource context for this config group lookup.
      * @param resourceContext The context to search the current configs.
      */
     public void setResourceContext(String resourceContext) {
         locator.setResourceContext(resourceContext);
     }
 
     /**
      * Load the current resource into the provided properties file. It respects
      * type of properties and encoding if set.
      * @param props Properties file to fill
      * @param resource Resource to load if present
      * @return The input stream of the resource.
      */
     @Override
     public void setLocation(Resource location) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * This operation is restricted from this configurer.
      * @param locations
      */
     @Override
     public void setLocations(Resource[] locations) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * Set the encoding of the resource file to read in. It basically delegates
      * through to parents method, but also sets the value in current method to
      * used for reading the input stream.
      * @param encoding Encoding of the resource
      */
     @Override
     public void setFileEncoding(String encoding) {
         locator.setFileEncoding(encoding);
         super.setFileEncoding(encoding);
     }
 
     /**
      * Set the single custom resource to search at.
      * @param smartLocation The custom resource
      */
     public void setSmartLocation(String smartLocation) {
         locator.setSmartLocations(new String[]{smartLocation});
         super.setLocation(new ClassPathResource(smartLocation));
     }
 
     /**
      * The custom resources as CSV. Its main intended use would be to supply
      * custom resources through another properties file to keep the resources
      * dynamic.
      * @param smartLocationsAsCsv The resources as comma separated values (csv)
      */
     public void setSmartLocationsAsCsv(String smartLocationsAsCsv) {
         setSmartLocations(smartLocationsAsCsv.split(","));
     }
 
     /**
      * The custom resources as array, its main intended use case would be from
      * an application context XML file.
      * @param smartLocations The resources as an array
      */
     public void setSmartLocations(String[] smartLocations) {
         locator.setSmartLocations(smartLocations);
         Resource[] resources = new Resource[smartLocations.length];
         for (int i = 0; i < smartLocations.length; ++i) {
             String smartLocation = StringUtils.trim(smartLocations[i]);
             if (StringUtils.isNotEmpty(smartLocation)) {
                 resources[i] = new ClassPathResource(smartLocation);
             }
         }
         super.setLocations(resources);
     }
 
     /**
      * Retrieves whether search in classpath is enabled or not.
      * @return True if search is enabled in classpath
      */
     protected boolean isClasspathSearchEnabled() {
         return locator.isClasspathSearchEnabled();
     }
 
     /**
      * Sets whether search in classpath is enabled or not
      * @param classpathSearchEnabled True if search is enabled for classpath
      */
     public void setClasspathSearchEnabled(boolean classpathSearchEnabled) {
         locator.setClasspathSearchEnabled(classpathSearchEnabled);
     }
 
     /**
      * Retrieves whether search in current directory is enabled or not.
      * @return True if search is enabled in current directory
      */
     protected boolean isCurrentDirSearchEnabled() {
         return locator.isCurrentDirSearchEnabled();
     }
 
     /**
      * Sets whether search in current directory is enabled or not
      * @param currentDirSearchEnabled True if search is enabled for current dir
      */
     public void setCurrentDirSearchEnabled(boolean currentDirSearchEnabled) {
         locator.setCurrentDirSearchEnabled(currentDirSearchEnabled);
     }
 
     /**
      * Retrieves whether search in classpath for default is enabled or not.
      * @return True if search is enabled for enabled
      */
     protected boolean isDefaultSearchEnabled() {
         return locator.isDefaultSearchEnabled();
     }
 
     /**
      * Sets whether search in default classpath is enabled or not
      * @param defaultSearchEnabled True if search is enabled for default cp
      */
     public void setDefaultSearchEnabled(boolean defaultSearchEnabled) {
         locator.setDefaultSearchEnabled(defaultSearchEnabled);
     }
 
     /**
      * Retrieves whether search in user home directory is enabled or not.
      * @return True if search is enabled in user home directory
      */
     protected boolean isUserHomeSearchEnabled() {
         return locator.isUserHomeSearchEnabled();
     }
 
     /**
      * Sets whether search in user home directory is enabled or not
      * @param userHomeSearchEnabled True if search is enabled for user home dir
      */
     public void setUserHomeSearchEnabled(boolean userHomeSearchEnabled) {
         locator.setUserHomeSearchEnabled(userHomeSearchEnabled);
     }
 
     /**
      * Get configured custom search locations
      * @return Custom search locations
      */
     protected String[] getSearchLocations() {
         return locator.getSmartLocations();
     }
 
     /**
      * The custom search location for the current configurer.
      * @param searchLocation The custom search location
      */
     public void setSearchLocation(String searchLocation) {
         if (StringUtils.isNotEmpty(searchLocation)) {
             setSearchLocations(new String[]{searchLocation});
         }
     }
 
     /**
      * The custom search locations as comma separated values (csv). It will
      * primarily split the search locations by ',' and its intended use case is
      * to inject the search locations via another properties configurer.
      * @param searchLocationAsCsv The search locations as CSV
      */
     public void setSearchLocationsAsCsv(String searchLocationAsCsv) {
         if (StringUtils.isNotEmpty(searchLocationAsCsv)) {
             setSearchLocations(searchLocationAsCsv.split(","));
         }
     }
 
     /**
      * The custom search locations intended to be mainly used via application
      * context XML.
      * @param searchLocations The search locations
      */
     public void setSearchLocations(String[] searchLocations) {
         locator.setSearchLocations(searchLocations);
     }
 
     /**
      * Set whether to ignore if resource is not found
      * @param ignoreResourceNotFound Flag to note whether to ignore missing rsrc
      */
     @Override
     public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
         this.ignoreResourceNotFound = ignoreResourceNotFound;
         super.setIgnoreResourceNotFound(ignoreResourceNotFound);
     }
 
 }
