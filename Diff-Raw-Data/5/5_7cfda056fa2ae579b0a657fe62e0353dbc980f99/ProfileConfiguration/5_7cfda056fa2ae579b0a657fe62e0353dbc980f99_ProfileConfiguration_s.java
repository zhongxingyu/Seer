 /*
 * ProfileConfiguration.java            $Revision: 1.4 $ $Date: 2002/08/27 16:43:12 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  * Copyright (c) 2002 Huston Franklin  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.beepcore.org/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.profile;
 
 
 import java.util.Enumeration;
 import java.util.Properties;
 
 
 /**
  * The Configuration class (which can be extended of course)
  * is designed to allow for the provision of profile-specific
  * settings, as well as the storage of profile-specific data.
  *
  * It can be extended by those implementing profile libraries
  * at will.
  */
 public class ProfileConfiguration {
     private Properties props = new Properties();
     
     public ProfileConfiguration() {
     }
 
     /**
      * Searches for the property with the specified key in this
      * ProfileConfiguration.
      *
      * @param key the property key.
      * @return the value in this configuration list with the
      *         specified key value or null if it is not found.
      */
     public String getProperty(String key) {
         return props.getProperty(key);
     }
 
     /**
      * Searches for the property with the specified key in this
      * ProfileConfiguration. If the key is not found in this
      * ProfileConfiguration <code>defaultValue</code> is returned.
      *
      * @param key the property key.
      * @param defaultValue a default value.
      * @return the value in this configuration list with the
      *         specified key value or <code>defaultValue</code>
      *         if it is not found.
      */
     public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, String defaultValue);
     }
 
     /**
      * Returns an enumeration of all the keys in this ProfileConfiguration.
      */
     public Enumeration propertyNames() {
         return props.propertyNames();
     }
 
     /**
      * Stores the value with the associated key in this ProfileConfiguration.
      *
      * @return the previous value of the specified key in this
      * ProfileConfiguration, or <code>null</code> if it did not have
      * one.
      */
     public String setProperty(String key, String value) {
         return (String)props.setProperty(key, value);
     }
 }
