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
 package com.crashnote.appengine.collect.impl;
 
 import com.crashnote.appengine.util.AppengineUtil;
 import com.crashnote.core.collect.impl.EnvCollector;
 import com.crashnote.core.config.Config;
 import com.crashnote.core.model.data.DataObject;
 
 /**
  * Customized {@link EnvCollector} that collects additional information from the AppEngine
  * platform (like application id).
  */
 public class AppengineEnvCollector<C extends Config>
     extends EnvCollector<C> {
 
     private static final String PROP_APP_ID = "com.google.appengine.application.id";
     private static final String PROP_APP_VER = "com.google.appengine.application.version";
 
     private static final String PROP_RT_VER = "com.google.appengine.runtime.version";
     private static final String PROP_RT_MODE = "com.google.appengine.runtime.environment";
 
     // SETUP ======================================================================================
 
     public AppengineEnvCollector(final C config) {
         super(config);
     }
 
     // INTERFACE ==================================================================================
 
     @Override
     protected DataObject getAppData() {
         final DataObject appData = super.getAppData();
         {
             appData.put("id", getSysUtil().getProperty(PROP_APP_ID));
 
             if (appData.get("profile") == null)
                 appData.put("profile", getSysUtil().getProperty(PROP_RT_MODE));
 
            final String[] v = getSysUtil().getProperty(PROP_APP_VER).split("\\.");
             if (v.length == 2) {
                 if (appData.get("version") == null)
                     appData.put("version", v[0]);
 
                 if (appData.get("build") == null)
                     if (getSysUtil().isRunningOnAppengine())
                         appData.put("build", v[1]);
                     else
                         appData.put("build", getStartTime());
             }
         }
         return appData;
     }
 
     @Override
     protected DataObject getRtData() {
         final DataObject rtData = super.getRtData();
         {
             rtData.put("code", getSysUtil().getProperty(PROP_RT_VER));
         }
         return rtData;
     }
 
     // SHARED =====================================================================================
 
     @Override
     protected AppengineUtil getSysUtil() {
         return (AppengineUtil) super.getSysUtil();
     }
 
     @Override
     protected boolean ignoreProperty(final String name, final String value) {
         return name.startsWith("com.google.appengine") || super.ignoreProperty(name, value);
     }
 }
