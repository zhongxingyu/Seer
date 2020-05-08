 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hoya;
 
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 
 /**
  * These are the keys that can be added to <code>conf/hoya-client.xml</code>.
  */
 public interface HoyaXmlConfKeys {
   /**
    * pattern to identify a hoya provider
    * {@value}
    */
   String KEY_HOYA_PROVIDER = "hoya.provider.%s";
   
   /**
    * HBase provider key (derived from {@link #KEY_HOYA_PROVIDER}
    * and so not found in the hoya code itself
    * {@value}
    */
   String KEY_HOYA_PROVIDER_HBASE = "hoya.provider.hbase";
   
   /**
    * Accumulo provider key (derived from {@link #KEY_HOYA_PROVIDER}
    * and so not found in the hoya code itself
    * {@value}
    */
   String KEY_HOYA_PROVIDER_ACCUMULO = "hoya.provider.accumulo";
   
   /**
    * conf option set to point to where the config came from
    * {@value}
    */
   String KEY_HOYA_TEMPLATE_ORIGIN = "hoya.template.origin";
   
   /**
    * Original name for the default FS. This is still 
    * expected by applications deployed
    */
   String FS_DEFAULT_NAME_CLASSIC = "fs.default.name";
   
   /**
    * Hoya principal
    */
   String KEY_HOYA_KERBEROS_PRINCIPAL = "hoya.kerberos.principal";
   
   /**
    * Name of the property for ACLs for Hoya AM.
    * {@value}
    */
   String KEY_HOYA_PROTOCOL_ACL = "security.hoya.protocol.acl";
 
   /**
    * Flag which is set to indicate that security should be enabled
    * when talking to this cluster.
    */
   String KEY_HOYA_SECURITY_ENABLED = "hoya.security.enabled";
 
   /**
    * queue name
    */
   String KEY_HOYA_YARN_QUEUE = "hoya.yarn.queue";
   String DEFAULT_HOYA_YARN_QUEUE= YarnConfiguration.DEFAULT_QUEUE_NAME;
 
   /**
    * default priority
    */
   String KEY_HOYA_YARN_QUEUE_PRIORITY = "hoya.yarn.queue.priority";
   int DEFAULT_HOYA_YARN_QUEUE_PRIORITY= 1;
 
 
   /**
    * Option for the permissions for the cluster directory itself: {@value}
    */
   String HOYA_CLUSTER_DIRECTORY_PERMISSIONS =
     "hoya.cluster.directory.permissions";
   /**
    * Default value for the permissions :{@value}
    */
  String DEFAULT_HOYA_CLUSTER_DIRECTORY_PERMISSIONS = "750";
   /**: {@value}
    * Option for the permissions for the data directory itself
    */
   String HOYA_DATA_DIRECTORY_PERMISSIONS = "hoya.data.directory.permissions";
   /**
    * Default value for the data directory permissions: {@value}
    */
  String DEFAULT_HOYA_DATA_DIRECTORY_PERMISSIONS = "750";
 }
