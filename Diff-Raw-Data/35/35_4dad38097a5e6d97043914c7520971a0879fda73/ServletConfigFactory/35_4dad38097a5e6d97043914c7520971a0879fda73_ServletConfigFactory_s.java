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
 package com.crashnote.servlet.config;
 
 import com.crashnote.logger.config.LoggerConfigFactory;
 
 import javax.servlet.FilterConfig;
 import java.util.*;
 
 public class ServletConfigFactory<C extends ServletConfig>
     extends LoggerConfigFactory<C> {
 
     private final FilterConfig filterConfig;
 
     // SETUP ======================================================================================
 
     public ServletConfigFactory(final FilterConfig filterConfig) {
         this(filterConfig, (C) new ServletConfig());
     }
 
     protected ServletConfigFactory(final FilterConfig filterConfig, final C internalConfig) {
         super(internalConfig);
         this.filterConfig = filterConfig;
     }
 
     @Override
     protected void loadExt() {
         // first: gather properties from filter config
         if (filterConfig != null) {
             final Properties props = new Properties();
             final Enumeration params = filterConfig.getInitParameterNames();
             while (params.hasMoreElements()) {
                 final String name = (String) params.nextElement();
                 final String value = filterConfig.getInitParameter(name);
                 props.setProperty(name, value);
             }
             applyProperties(props, false);
         }
 
         // then: load other props
         super.loadExt();
     }
 
     // SHARED =====================================================================================
 
     @Override
     protected void applyProperties(final Properties props, final boolean strict) {
         super.applyProperties(props, strict);
 
         config.setSkipHeaderData(getProperty(props, ServletConfig.PROP_REP_HEADER_SKIP, strict));
         config.setSkipSessionData(getProperty(props, ServletConfig.PROP_REP_SESSION_SKIP, strict));
        config.setSkipRemoteIP(getProperty(props, ServletConfig.PROP_REP_IP_SKIP, strict));
         config.setMaxRequestParameterSize(getProperty(props, ServletConfig.PROP_REP_REQ_PARAM_SIZE, strict));
 
         final String filterProp = getProperty(props, ServletConfig.PROP_REP_REQ_PARAM_FILTER, strict);
         if (filterProp != null) {
             final String[] filters = filterProp.split(",");
             for (final String filter : filters) {
                 config.addRequestFilter(filter);
             }
         }
     }
 
 }
