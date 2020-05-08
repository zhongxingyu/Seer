 /**
  * Copyright (C) 2011 - 101loops.com <dev@101loops.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.crashnote.core.collect.impl;
 
 import com.crashnote.core.collect.BaseCollector;
 import com.crashnote.core.config.*;
 import com.crashnote.core.model.data.DataObject;
 
 import static com.crashnote.core.util.FilterUtil.doFilter;
 
 /**
  * Collector to transform an application's environment data (e.g. version, system hardware etc.)
  * into a structured form.
  */
 public class EnvCollector<C extends Config>
     extends BaseCollector<C> implements IConfigChangeListener<C> {
 
     // configuration settings:
     private Long startTime;
     private String profile;
     private String version;
     private String build;
     private String clientInfo;
     private String[] envFilters;
 
     // SETUP ======================================================================================
 
     public EnvCollector(final C config) {
         super(config);
         updateConfig(config);
     }
 
     public void updateConfig(final C config) {
         config.addListener(this);
         this.profile = config.getAppProfile();
        this.build = config.getAppBuild();
         this.version = config.getVersion();
         this.startTime = config.getStartTime();
         this.clientInfo = config.getClientInfo();
         this.envFilters = config.getEnvironmentFilters();
     }
 
     // INTERFACE ==================================================================================
 
     public DataObject collect() {
         final DataObject data = createDataObj();
         {
             data.put("started", startTime);
 
             data.putObj("app", getAppData());
             data.putObj("runtime", getRtData());
             data.putObj("system", getSysData());
             data.putObj("device", getDevData());
         }
         return data;
     }
 
     // SHARED =====================================================================================
 
     protected DataObject getAppData() {
         final DataObject appData = createDataObj();
         {
             appData.put("build", build);
             appData.put("profile", profile);
             appData.put("version", version);
             appData.put("client", clientInfo);
         }
         return appData;
     }
 
     protected DataObject getRtData() {
         final DataObject rtData = createDataObj();
         {
             rtData.put("type", "java");
             rtData.put("name", getSysUtil().getRuntimeName());
             rtData.put("version", getSysUtil().getRuntimeVersion());
 
             // properties
             final DataObject props = createDataObj();
             {
                 for (final Object key : getSysUtil().getPropertyKeys()) {
                     final String name = key.toString();
                     final String value = getSysUtil().getProperty(name);
                     if (!ignoreProperty(name, value)) props.put(name, value);
                 }
             }
             rtData.put("props", props);
         }
         return rtData;
     }
 
     protected DataObject getSysData() {
         final DataObject sysData = createDataObj();
         {
             // identity
             sysData.put("id", getSysUtil().getSystemId());
             sysData.put("ip", getSysUtil().getHostAddress());
             sysData.put("name", getSysUtil().getHostName());
 
             // settings
             sysData.put("timezone", getSysUtil().getTimezoneId());
             sysData.put("timezone_offset", getSysUtil().getTimezoneOffset());
             //sysData.put("language", getSysUtil().getLanguage());
 
             // OS
             sysData.put("os_name", getSysUtil().getOSName());
             sysData.put("os_version", getSysUtil().getOSVersion());
 
             // environment properties
             final DataObject props = createDataObj();
             {
                 for (final String name : getSysUtil().getEnvKeys())
                     props.put(name, doFilter(name, envFilters) ? "#" : getSysUtil().getEnv(name));
             }
             sysData.putObj("props", props);
         }
         return sysData;
     }
 
     protected DataObject getDevData() {
         final DataObject devData = createDataObj();
         {
             // CPU
             devData.put("cores", getSysUtil().getAvailableProcessors());
 
             // RAM
             /*
             devData.put("ram", getSysUtil().getTotalMemorySize());
             devData.put("ram_free", getSysUtil().getAvailableMemorySize());
             */
         }
         return devData;
     }
 
     protected boolean ignoreProperty(final String name, final String value) {
         return value.length() > 255
             || name.startsWith("java.")
             || name.startsWith("user.")
             || name.startsWith("os.")
             || name.startsWith("awt.")
             || name.startsWith("jna.")
             || name.startsWith("file.")
             || name.startsWith("sun.")
             || name.endsWith(".separator");
     }
 
     // GET / SET ==================================================================================
 
     public Long getStartTime() {
         return startTime;
     }
 }
