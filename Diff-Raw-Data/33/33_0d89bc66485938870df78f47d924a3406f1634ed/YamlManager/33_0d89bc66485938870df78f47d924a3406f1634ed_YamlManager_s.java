 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.craftfire.commons.util.LoggingManager;
 import com.craftfire.commons.util.Util;
 
 public class YamlManager {
     //TODO: Make it possible to save to a file
     private LoggingManager loggingManager = new LoggingManager("CraftFire.YamlManager", "[YamlManager]");
     private Map<String, Object> yaml = new HashMap<String, Object>();
    private File file;
 
     public YamlManager () {
         //TODO: Nothing?
     }
 
     public YamlManager(File file) throws IOException {
        if (file == null) {
            getLogger().error("File '" + file + "' is null, nodes could not be loaded from the yaml file.");
            return;
        } else if (!file.exists()) {
            getLogger().error("File '" + file.toString() + "' could not be found, nodes could not be loaded from the yaml file.");
            return;
        }
        getLogger().debug("Loading nodes from file '" + file.getAbsoluteFile() + "'.");
        this.file = file;
        load(new FileInputStream(file));
     }
 
     public YamlManager(String path) throws IOException {
        InputStream yaml = getClass().getClassLoader().getResourceAsStream(path);
        getLogger().debug("Loading nodes from local file '" + path + "' = '" + yaml + "'.");
        load(yaml);
     }
 
    public File getFile() {
        return this.file;
     }
 
     public LoggingManager getLogger() {
         if (this.loggingManager == null) {
             this.loggingManager = new LoggingManager("CraftFire.YamlManager", "[YamlManager]");
         }
         return this.loggingManager;
     }
 
     public void setLoggingManager(LoggingManager loggingManager) {
         if (loggingManager == null) {
             throw new IllegalArgumentException("Parameter 'loggingManager' cannot be null.");
         }
         this.loggingManager = loggingManager;
     }
 
     public boolean exist(String node) {
         getLogger().debug("Checking if node '" + node.toLowerCase() + "' exists: '" + this.yaml.containsKey(node.toLowerCase()) + "'.");
         return this.yaml.containsKey(node.toLowerCase());
     }
 
     public boolean getBoolean(String node) {
         return getBoolean(node, false);
     }
 
     public boolean getBoolean(String node, boolean defaultValue) {
         String newNode = node.toLowerCase();
         if (exist(newNode)) {
             Object value = this.yaml.get(newNode);
             if (value instanceof Boolean) {
                 getLogger().debug("Found node '" + newNode + "' with Boolean value '" + value +
                                   "', default value is '" + defaultValue + "'.");
                 return (Boolean) value;
             } else {
                 getLogger().debug("Found node '" + newNode + "' but value is not a Boolean '" + value +
                                   "', returning default value instead '" + defaultValue + "'.");
                 return defaultValue;
             }
         }
         getLogger().debug("Could not find node '" + newNode + "', returning default value instead '" + defaultValue + "'.");
         return defaultValue;
     }
 
     public String getString(String node) {
         return getString(node, null);
     }
 
     public String getString(String node, String defaultValue) {
         String newNode = node.toLowerCase();
         if (exist(newNode)) {
             Object value = this.yaml.get(newNode);
             if (value instanceof String) {
                 getLogger().debug("Found node '" + newNode + "' with String value '" + value +
                                   "', default value is '" + defaultValue + "'.");
                 return (String) value;
             } else {
                 getLogger().debug("Found node '" + newNode + "' but value is not a String '" + value +
                                   "', returning default value instead '" + defaultValue + "'.");
                 return defaultValue;
             }
         }
         getLogger().debug("Could not find node '" + newNode + "', returning default value instead '" + defaultValue + "'.");
         return defaultValue;
     }
 
     public int getInt(String node) {
         return getInt(node, 1);
     }
 
     public int getInt(String node, int defaultValue) {
         String newNode = node.toLowerCase();
         if (exist(newNode)) {
             Object value = this.yaml.get(newNode);
             if (value instanceof Integer) {
                 getLogger().debug("Found node '" + newNode + "' with Integer value '" + value +
                                   "', default value is '" + defaultValue + "'.");
                 return (Integer) value;
             } else {
                 getLogger().debug("Found node '" + newNode + "' but value is not an Integer '" + value +
                                   "', returning default value instead '" + defaultValue + "'.");
                 return defaultValue;
             }
         }
         getLogger().debug("Could not find node '" + newNode + "', returning default value instead '" + defaultValue + "'.");
         return defaultValue;
     }
 
     public Long getLong(String node) {
         return getLong(node, null);
     }
 
     public Long getLong(String node, Long defaultValue) {
         String newNode = node.toLowerCase();
         if (exist(newNode)) {
             Object value = this.yaml.get(newNode);
             if (value instanceof Long) {
                 getLogger().debug("Found node '" + newNode + "' with Long value '" + value +
                                   "', default value is '" + defaultValue + "'");
                 return (Long) value;
             } else {
                 getLogger().debug("Found node '" + newNode + "' but value is not a Long '" + value +
                                   "', returning default value instead '" + defaultValue + "'.");
                 return defaultValue;
             }
         }
         getLogger().debug("Could not find node '" + newNode + "', returning default value instead '" + defaultValue + "'.");
         return defaultValue;
     }
 
     public Map<String, Object> getNodes() {
         return this.yaml;
     }
 
     public void addNodes(YamlManager yamlManager) {
         getLogger().debug("Adding node list to current node list: '" + yamlManager.getNodes().toString() + "'.");
         this.yaml.putAll(yamlManager.getNodes());
     }
 
     public void addNodes(Map<String, Object> map) {
         getLogger().debug("Adding node list to current node list: '" + map.toString() + "'.");
         this.yaml.putAll(map);
     }
 
     public void setNode(String node, Object value) {
         getLogger().debug("Setting node '" + node + "' to value '" + value + "'.");
         this.yaml.put(node, value);
     }
 
     public void save() {
         //TODO: save
     }
 
     public void load(File file) throws IOException {
         if (file == null) {
             getLogger().error("File '" + file + "' is null, nodes could not be loaded from the yaml file.");
             return;
         } else if (!file.exists()) {
             getLogger().error("File '" + file.toString() + "' could not be found, nodes could not be loaded from the yaml file.");
             return;
         }
         getLogger().debug("Loading nodes from file '" + file.getAbsoluteFile() + "'.");
        this.file = file;
         load(new FileInputStream(file));
     }
 
     public void load(String path) throws IOException {
         getLogger().debug("Loading nodes from local file '" + path + "'.");
         load(getClass().getClassLoader().getResourceAsStream(path));
     }
 
     private void load(InputStream yamlStream) throws IOException {
        this.yaml = new HashMap<String, Object>();
         if (yamlStream != null) {
             try {
                 InputStreamReader yamlStreamReader = new InputStreamReader(yamlStream);
                 BufferedReader buffer = new BufferedReader(yamlStreamReader);
                 String line, node = "";
                 int lastBlank = 0;
                 boolean isNode = false;
                 while  ((line = buffer.readLine()) != null) {
                     int blank = 0;
                     if (line.length() > 0 && line.charAt(line.length() - 1) == ':') {
                         for (int i = 0; i < line.length(); i++) {
                             if (Character.isWhitespace(line.charAt(i))) {
                                 blank++;
                             }
                         }
                         line = line.replaceAll("\\s+", "");
                         line = line.replaceAll(":", "");
                         if (blank == 0) {
                             node = line + ".";
                         } else if (blank > lastBlank) {
                             node += line + ".";
                         } else if (blank <= lastBlank) {
                             String[] split = node.split("\\.");
                             if ((lastBlank - blank) > 0) {
                                 for (int i = 1; ((lastBlank - blank) / 4) >= i; i++) {
                                     node = node.replace("." + split[split.length - i], "");
                                 }
                             } else {
                                 node = node.replace("." + split[split.length - 1], "");
                             }
                             node += line + ".";
                         }
                         lastBlank = blank;
                         isNode = true;
                     } else if (line.length() > 0) {
                         boolean set = false;
                         for (int i=0; i < line.length() && ! set; i++) {
                             if (Character.isWhitespace(line.charAt(i))) {
                                 blank++;
                             } else {
                                 set = true;
                             }
                         }
 
                         String[] split = line.split(":");
                         String finalNode = split[0].replaceAll("\\s+", "");
                         if (finalNode.startsWith("#")) {
                             continue;
                         }
                         if (!isNode && blank > lastBlank) {
                             node += finalNode + ".";
                         } else if (!isNode && blank < lastBlank) {
                             String[] spl = node.split("\\.");
                             node = node.replace("." + spl[spl.length - 1], "");
                         }
                         lastBlank = blank;
                         String temp = split[1].substring(1);
                         if (split.length > 1) {
                             for(int i=2; split.length > i; i++){
                                 temp += ":" + split[i];
                             }
                         }
                         int index = temp.lastIndexOf('#');
                         if (index != -1 && Character.isWhitespace(temp.charAt(index - 1))) {
                             temp = temp.substring(0, index - 1);
                         }
                         String value = "";
                         char last = 0;
                         for (int i = 0; i < temp.length(); i++) {
                             if (Character.isWhitespace(temp.charAt(i)) && Character.isWhitespace(last)) {
                                 continue;
                             }
                             value += temp.charAt(i);
                             last = temp.charAt(i);
                         }
                         if (Character.isWhitespace(value.charAt(value.length() - 1))) {
                             value = value.substring(0, value.length() - 1);
                         }
                         //System.out.println(node + finalNode + " = " + value);
                         if (value.equalsIgnoreCase("true")) {
                             getLogger().debug("Adding node '" + node + finalNode + "' " +
                                               "to the node list with Boolean value 'true'.");
                             this.yaml.put(node + finalNode, true);
                         } else if (value.equalsIgnoreCase("false")) {
                             getLogger().debug("Adding node '" + node + finalNode + "' " +
                                               "to the node list with Boolean value 'false'.");
                             this.yaml.put(node + finalNode, false);
                         } else if (Util.isInteger(value)) {
                             getLogger().debug("Adding node '" + node + finalNode + "' " +
                                               "to the node list with Integer value '" + value + "'.");
                             this.yaml.put(node + finalNode, Integer.parseInt(value));
                         } else if (Util.isLong(value)) {
                             getLogger().debug("Adding node '" + node + finalNode + "' " +
                                               "to the node list with Long value '" + value + "'.");
                             this.yaml.put(node + finalNode, Long.parseLong(value));
                         } else if (value instanceof String && value != null && !value.equalsIgnoreCase("null")) {
                             getLogger().debug("Adding node '" + node + finalNode + "' " +
                                               "to the node list with String value '" + value + "'.");
                             this.yaml.put(node + finalNode, value);
                         } else {
                             getLogger().error("Could not add node '" + node + finalNode + "' " + "to the node list because the value is '" + value + "'.");
                         }
                         isNode = false;
                     }
                 }
             } finally  {
                 yamlStream.close();
             }
         }
     }
 }
