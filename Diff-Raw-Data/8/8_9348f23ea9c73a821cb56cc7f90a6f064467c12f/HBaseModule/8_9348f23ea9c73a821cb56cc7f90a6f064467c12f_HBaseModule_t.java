 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.hbase;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.nesscomputing.config.Config;
 
 /**
  * Provides handy bindings for interacting with HBase.
  * @author steven
  */
 public class HBaseModule extends AbstractModule {
 
 
 
     @Override
     protected void configure() {
         bind(Configuration.class).toProvider(HadoopConfigurationProvider.class);
     }
 
    private static class HadoopConfigurationProvider implements Provider<Configuration> {
 
         private Config trumpetConfig;
 
         @Inject
         private HadoopConfigurationProvider(Config trumpetConfig)
         {
             this.trumpetConfig = trumpetConfig;
         }
 
         @Override
         public Configuration get()
         {
             Configuration config = HBaseConfiguration.create();
             config.set("hbase.zookeeper.quorum", trumpetConfig.getConfiguration().getString("hbase.zookeeper", "localhost"));
             config.set("zookeeper.znode.parent", trumpetConfig.getConfiguration().getString("hbase.zookeeper.path", "/hbase"));
             return config;
         }
     }
 }
