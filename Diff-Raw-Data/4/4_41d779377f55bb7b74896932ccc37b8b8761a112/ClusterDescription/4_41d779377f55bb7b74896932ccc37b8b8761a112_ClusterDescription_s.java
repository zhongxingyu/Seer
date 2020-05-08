 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package org.apache.hadoop.hoya.api;
 
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.exceptions.BadConfigException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.providers.HoyaProviderFactory;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.yarn.client.HoyaClient;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.SerializationConfig;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Represents a cluster specification; designed to be sendable over the wire
  * and persisted in JSON by way of Jackson.
  * As a wire format it is less efficient in both xfer and ser/deser than 
  * a binary format, but by having one unified format for wire and persistence,
  * the code paths are simplified.
  */
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class ClusterDescription {
   protected static final Logger
     log = LoggerFactory.getLogger(ClusterDescription.class);
 
   private static final String UTF_8 = "UTF-8";
 
   /**
    * version counter
    */
   public String version = "1.0";
 
   /**
    * Name of the cluster
    */
   public String name;
 
   /**
    * Type of cluster
    */
   public String type = HoyaProviderFactory.DEFAULT_CLUSTER_TYPE;
 
   /**
    * State of the cluster
    */
   public int state;
   
   /*
    State list for both clusters and nodes in them. Ordered so that destroyed follows
    stopped.
    
    Some of the states are only used for recording
    the persistent state of the cluster and are not
    seen in node descriptions
    */
 
   /**
    * Specification is incomplete & cannot
    * be used: {@value}
    */
   public static final int STATE_INCOMPLETE = 0;
 
   /**
    * Spec has been submitted: {@value}
    */
   public static final int STATE_SUBMITTED = 1;
   /**
    * Cluster created: {@value}
    */
   public static final int STATE_CREATED = 2;
   /**
    * Live: {@value}
    */
   public static final int STATE_LIVE = 3;
   /**
    * Stopped
    */
   public static final int STATE_STOPPED = 4;
   /**
    * destroyed
    */
   public static final int STATE_DESTROYED = 5;
   /**
    * When was the cluster created?
    */
   public long createTime;
   
   /**
    * When was the cluster last started?
    */
   public long startTime;
 
   /**
    * When was the cluster last updated
    */
   public long updateTime;
   
   /**
    * when was this status document created
    */
   public long statusTime;
 
   /**
    * The path to the original configuration
    * files; these are re-read when 
    * restoring a cluster
    */
 
   public String originConfigurationPath;
   public String generatedConfigurationPath;
   public String zkHosts;
   public int zkPort;
   public String zkPath;
   /**
    * This is where the data goes
    */
   public String dataPath;
 
   /**
    * HBase home: if non-empty defines where a copy of HBase is preinstalled
    */
   public String applicationHome;
 
   /**
    * The path in HDFS where the HBase image must go
    */
   public String imagePath;
 
   /**
    * cluster-specific options
    */
   public Map<String, String> options =
     new HashMap<String, String>();
 
   /**
    * Statistics
    */
   public Map<String, Map<String, Integer>> stats =
     new HashMap<String, Map<String, Integer>>();
 
   /**
    * Instances: role->count
    */
   public Map<String, Integer> instances =
     new HashMap<String, Integer>();
 
 
   /**
    * Role options, 
    * role -> option -> value
    */
   public Map<String, Map<String, String>> roles =
     new HashMap<String, Map<String, String>>();
 
 
   /**
    * List of key-value pairs to add to a client config to set up the client
    */
   public Map<String, String> clientProperties =
     new HashMap<String, String>();
 
 
   public ClusterDescription() {
   }
   
 
   /**
    * Verify that a cluster specification exists
    * @param clustername name of the cluster (For errors only)
    * @param fs filesystem
    * @param clusterSpecPath cluster specification path
    * @throws IOException IO problems
    * @throws HoyaException if the cluster is not present
    */
   public static void verifyClusterSpecExists(String clustername,
                                              FileSystem fs,
                                              Path clusterSpecPath) throws
                                                                    IOException,
                                                                    HoyaException {
     if (!fs.exists(clusterSpecPath)) {
       log.debug("Missing cluster specification file {}", clusterSpecPath);
       throw new HoyaException(HoyaExitCodes.EXIT_UNKNOWN_HOYA_CLUSTER,
                               HoyaClient.E_UNKNOWN_CLUSTER + clustername +
                               "\n (cluster definition not found at " +
                               clusterSpecPath);
     }
   }
 
   @Override
   public String toString() {
     try {
       return toJsonString();
     } catch (Exception e) {
       log.debug("Failed to convert CD to JSON ",e);
       return super.toString();
     }
   }
 
   /**
    * Shallow clone
    * @return a shallow clone
    * @throws CloneNotSupportedException
    */
   @Override
   public Object clone() throws CloneNotSupportedException {
     return super.clone();
   }
 
   /**
    * A deep clone of the spec. This is done inefficiently with a ser/derser
    * @return the cluster description
    */
   public ClusterDescription deepClone() {
     try {
       return fromJson(toJsonString());
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
   
   
   /**
    * Save a cluster description to a hadoop filesystem
    * @param fs filesystem
    * @param path path
    * @param overwrite should any existing file be overwritten
    * @throws IOException IO excpetion
    */
   public void save(FileSystem fs, Path path, boolean overwrite) throws
                                                                 IOException {
     String json = toJsonString();
     FSDataOutputStream dataOutputStream = fs.create(path, overwrite);
     byte[] b = json.getBytes(UTF_8);
     try {
       dataOutputStream.write(b);
     } finally {
       dataOutputStream.close();
     }
   }
 
   /**
    * Load from the filesystem
    * @param fs filesystem
    * @param path path
    * @return a loaded CD
    * @throws IOException IO problems
    */
   public static ClusterDescription load(FileSystem fs, Path path)
     throws IOException, JsonParseException, JsonMappingException {
     FileStatus status = fs.getFileStatus(path);
     byte[] b = new byte[(int) status.getLen()];
     FSDataInputStream dataInputStream = fs.open(path);
     int count = dataInputStream.read(b);
     String json = new String(b, 0, count, UTF_8);
     return fromJson(json);
   }
 
   /**
    * Make a deep copy of the class
    * @param source source
    * @return the copy
    */
   public static ClusterDescription copy(ClusterDescription source) {
     //currently the copy is done by a generate/save. Inefficient but it goes
     //down the tree nicely
     try {
       return fromJson(source.toJsonString());
     } catch (IOException e) {
       throw new RuntimeException("ClusterDescription copy failed " + e, e);
     }
   }
 
   /**
    * Convert to a JSON string
    * @return a JSON string description
    * @throws IOException Problems mapping/writing the object
    */
   public String  toJsonString() throws IOException,
                                        JsonGenerationException,
                                        JsonMappingException {
     ObjectMapper mapper = new ObjectMapper();
     mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
     return mapper.writeValueAsString(this);
   }
 
 
   /**
    * Convert from JSON
    * @param json input
    * @return the parsed JSON
    * @throws IOException IO
    */
   public static ClusterDescription fromJson(String json)
     throws IOException, JsonParseException, JsonMappingException {
     ObjectMapper mapper = new ObjectMapper();
     try {
       return mapper.readValue(json, ClusterDescription.class);
     } catch (IOException e) {
       log.error("Exception while parsing json : " + e + "\n" + json, e);
       throw e;
     }
   }
 
   /**
    * Set a cluster option
    * @param key
    * @param val
    */
   public void setOption(String key, String val) {
     options.put(key, val);
   }
 
   public void setOption(String option, int val) {
     setOption(option, Integer.toString(val));
   }
 
   public void setOption(String option, boolean val) {
     setOption(option, Boolean.toString(val));
   }
   
   /**
    * Get a cluster option or value
    * 
    * @param key
    * @param defVal
    * @return
    */
   public String getOption(String key, String defVal) {
     String val = options.get(key);
     return val != null ? val : defVal;
   }
   
   /**
    * Get a cluster option or value
    * 
    * @param key
   * @param defVal
    * @return
    */
   public String getMandatoryOption(String key) throws BadConfigException {
     String val = options.get(key);
    if (key == null) {
       throw new BadConfigException("Missing option " + key);
     }
     return val ;
   }
 
 
   /**
    * Get a role opt; use {@link Integer#decode(String)} so as to take hex
    * oct and bin values too.
    *
    * @param option option name
    * @param defVal default value
    * @return parsed value
    * @throws NumberFormatException if the role could not be parsed.
    */
   public int getOptionInt(String option, int defVal) {
     String val = getOption(option, Integer.toString(defVal));
     return Integer.decode(val);
   }
 
   public void verifyOptionSet(String key) throws BadConfigException {
     if (HoyaUtils.isUnset(getOption(key, null))) {
       throw new BadConfigException("Unset cluster option %s", key);
     }
   }
 
 
   /**
    * Get an option as a boolean. Note that {@link Boolean#valueOf(String)}
    * is used for parsing -its policy of what is true vs false applies.
    * @param option name
    * @param defVal default
    * @return the option.
    */
   public boolean getOptionBool(String option, boolean defVal) {
     return Boolean.valueOf(getOption(option,Boolean.toString(defVal)));
   }
 
   /**
    * Get a role option
    * @param role role to get from
    * @param option option name
    * @param defVal default value
    * @return resolved value
    */
   public String getRoleOpt(String role, String option, String defVal) {
     Map<String, String> options = getRole(role);
     if (options == null) {
       return defVal;
     }
     String val = options.get(option);
     return val != null ? val : defVal;
   }
 
   /**
    * look up a role
    * @param role role
    * @return role mapping or null
    */
   public Map<String, String> getRole(String role) {
     return roles.get(role);
   }
 
   /**
    * Get a role -adding it to the roleopts map if
    * none with that name exists
    * @param role role
    * @return role mapping
    */
   public Map<String, String> getOrAddRole(String role) {
     Map<String, String> map = roles.get(role);
     if (map==null) {
       map = new HashMap<String, String>();
     }
     roles.put(role, map);
     return map;
   }
 
   public Map<String, String> getMandatoryRole(String role) throws
                                                            BadConfigException {
     Map<String, String> roleOptions = roles.get(role);
     if (roleOptions == null) {
       throw new BadConfigException("Missing options for role " + role);
     }
     return roleOptions;
   }
 
   /**
    * Get a role opt; use {@link Integer#decode(String)} so as to take hex
    * oct and bin values too.
    *
    * @param role role to get from
    * @param option option name
    * @param defVal default value
    * @return parsed value
    * @throws NumberFormatException if the role could not be parsed.
    */
   public int getRoleOptInt(String role, String option, int defVal) {
     String val = getRoleOpt(role, option, Integer.toString(defVal));
     return Integer.decode(val);
   }
 
   public void setRoleOpt(String role, String option, String val) {
     Map<String, String> options = getOrAddRole(role);
     options.put(option, val);
   }
 
   public void setRoleOpt(String role, String option, int val) {
     setRoleOpt(role, option, Integer.toString(val));
   }
 
   /**
    * Set the desired instance count
    * @param role role
    * @param val value
    */
   public void setDesiredInstanceCount(String role, int val) {
     setRoleOpt(role, RoleKeys.ROLE_INSTANCES, val);
   }
 
   /**
    * Get the desired instance count;
    * @param role role
    * @return the desired count -falling back to the default value
    */
   public int getDesiredInstanceCount(String role, int defVal) {
     return getRoleOptInt(role, RoleKeys.ROLE_INSTANCES, defVal);
   }
   
   /**
    * Set the actual instance count
    * @param role role
    * @param val value
    */
   public void setActualInstanceCount(String role, int val) {
     setRoleOpt(role, RoleKeys.ROLE_ACTUAL_INSTANCES, val);
   }
 
   /**
    * Get the actual instance count;
    * @param role role
    * @return the current count -falling back to 0
    */
   public int getActualInstanceCount(String role) {
     return getRoleOptInt(role, RoleKeys.ROLE_ACTUAL_INSTANCES, 0);
   }
 }
