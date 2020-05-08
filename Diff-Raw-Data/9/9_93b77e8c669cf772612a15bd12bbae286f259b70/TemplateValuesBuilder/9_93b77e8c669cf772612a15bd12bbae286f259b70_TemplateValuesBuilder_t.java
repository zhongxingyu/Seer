 /*
  * Copyright (c) 2011 Henrik Lynggaard Hansen 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Henrik Lynggaard Hansen
  */
 package dk.hlyh.hudson.tools.jobcreator.helper;
 
 import dk.hlyh.hudson.tools.jobcreator.ImportException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 public class TemplateValuesBuilder {
 
   private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private final Map<String, Object> values;
 
   public TemplateValuesBuilder() {
     values = new HashMap<String, Object>();
   }
 
   public void setProperty(String key, String value) throws ImportException {
     LOGGER.fine("Setting property '" + key + "' , '" + value + "'");
 
     // simple Validations
     if (key == null || key.trim().length() == 0) {
       throw new ImportException("Key cannot be empty");
     }
     if (key.contains(" ")) {
       throw new ImportException("Key cannot contain spaces");
     }
     if (key.contains("..")) {
       throw new ImportException("Key cannot contain empty parts");
     }
     if (key.endsWith(".")) {
       throw new ImportException("Key cannot end in '.'");
     }
 
     String[] keyParts = key.split("\\.");
     String resolvedKey = null;
 
     Map<String, Object> currentMap = values;
     for (int i = 0; i < keyParts.length; i++) {
       String part = keyParts[i];
 
       resolvedKey = resolvedKey == null ? part : resolvedKey + "." + part;
 
       // if this is not the last part
       if (i + 1 != keyParts.length) {
         Object mapContent = currentMap.get(part);
         // if the current map has value
         if (mapContent == null) {
           Map<String, Object> newMap = new HashMap<String, Object>();
           currentMap.put(part, newMap);
           currentMap = newMap;
 
         } else if (mapContent instanceof Map) {
           currentMap = (Map<String, Object>) mapContent;
         } else {
           throw new ImportException("The property '" + resolvedKey + "' has previously been set, so cannot be used as parent in '" + key + "'");
         }
       } else {
         if (currentMap.containsKey(part) && !(currentMap.get(part) instanceof String)) {
           throw new ImportException("The property '" + key + "' has previously been used as a parent, so cannot be used as simple property");
         }
         currentMap.put(part, value);
       }
     }
 
   }
 
   public Map getValues() {
     return values;
   }
 }
