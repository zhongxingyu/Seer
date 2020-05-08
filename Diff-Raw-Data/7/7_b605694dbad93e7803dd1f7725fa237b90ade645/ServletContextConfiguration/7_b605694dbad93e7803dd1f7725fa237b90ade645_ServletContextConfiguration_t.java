 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush.servlet;
 
 import org.icepush.Configuration;
 import org.icepush.ConfigurationException;
 
 import javax.servlet.ServletContext;
 
 public class ServletContextConfiguration extends Configuration {
     private final String name;
     private ServletContext context;
 
    public ServletContextConfiguration(ServletContext context) {
        this(null, context);
    }

     public ServletContextConfiguration(String prefix, ServletContext context) {
         this.name = prefix;
         this.context = context;
     }
 
     public String getName() {
         return name;
     }
 
     public Configuration getChild(String child) throws ConfigurationException {
         String childName = postfixWith(child);
         String value = context.getInitParameter(childName);
         if (value == null) {
             throw new ConfigurationException("Cannot find parameter: " + childName);
         } else {
             return new ServletContextConfiguration(childName, context);
         }
     }
 
     public Configuration[] getChildren(String name) throws ConfigurationException {
         return new Configuration[]{getChild(name)};
     }
 
     public String getAttribute(String paramName) throws ConfigurationException {
         String attributeName = postfixWith(paramName);
         String value = context.getInitParameter(attributeName);
         if (value == null) {
             throw new ConfigurationException("Cannot find parameter: " + attributeName);
         } else {
             return value;
         }
     }
 
     public String getValue() throws ConfigurationException {
         String value = context.getInitParameter(name);
         if (value == null) {
             throw new ConfigurationException("Cannot find parameter: " + name);
         } else {
             return value;
         }
     }
 
     private String postfixWith(String child) {
        return (name == null || name.trim().length() == 0) ? child : name + '.' + child;
     }
 }
