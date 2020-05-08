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
 package com.crashnote.core.config;
 
 import com.crashnote.About;
 import com.crashnote.core.build.Builder;
 import com.crashnote.core.collect.Collector;
 import com.crashnote.core.log.*;
 import com.crashnote.core.model.types.*;
 import com.crashnote.core.report.Reporter;
 import com.crashnote.core.send.Sender;
 import com.crashnote.core.util.*;
 
 import java.util.*;
 
 /**
  * Main configuration object, home to all configurable settings of the notifier like
  * authentication and behaviour properties. It is initialized in the according appender/handler,
  * user customizations are applied and then each important class receives a copy.
  * <p/>
  * It assumes that each 'set' method receives a String and converts it to the actual data type by
  * manual parsing/converting, thus being independent of the way the concrete logger handles it.
  */
 public class Config<C extends Config<C>> {
 
     private final LogLog logger;
     
     /**
      * default prefix for the libraries properties (e.g. in command line)
      */
     public static final String LIB_NAME = "crashnote";
     public static final String LIB_URL = "https://crashnote.com";
     public static final String LIB_URL_BOARD = LIB_URL + "/dashboard";
 
     /**
      * time of JVM deployment / start up
      */
     private long startTime;
 
     /**
      * unique identifier for environment
      */
     private long uid;
 
     /**
      * map to store the configuration settings
      */
     private volatile Map<String, Object> settings = new HashMap<String, Object>();
 
     /**
      * list of listeners that are notified on any change to the configuration
      */
     private volatile List<IConfigChangeListener> listeners = new ArrayList<IConfigChangeListener>();
 
     /**
      * default prefix for the libraries properties (e.g. in command line)
      */
     public static final String PROP_PREFIX = LIB_NAME;
 
     /**
      * parser to convert configuration properties from String to desired type
      */
     protected final ConfigParser parser;
 
     /**
      * factory to create an instance of the internal log
      */
     protected LogLogFactory<C> logFactory;
 
     /**
      * Property names of internal library settings
      */
     public static final String PROP_SYNC = "sync";
     public static final String PROP_DEBUG = "debug";
     public static final String PROP_ENABLED = "enabled";
     public static final String PROP_CLIENT = "client";
 
     public static final String PROP_APP_TYPE = "appType";
     public static final String PROP_APP_VERSION = "version";
     public static final String PROP_APP_BUILD = "build";
     public static final String PROP_APP_PROFILE = "profile";
 
     public static final String PROP_REP_LEVEL = "logLevel";
     public static final String PROP_REP_ENV_FILTER = "envFilter";
 
     public static final String PROP_API_KEY = "key";
     public static final String PROP_API_HOST = "host";
     public static final String PROP_API_PORT = "port";
     public static final String PROP_API_PORT_SSL = "sslPort";
     public static final String PROP_API_SECURE = "secure";
     public static final String PROP_API_TIMEOUT = "timeout";
 
     // SETUP ======================================================================================
 
     public Config() {
         uid = IDUtil.createUID();
         parser = new ConfigParser();
         logger = getLogger(this.getClass());
         startTime = new Date().getTime();
         initDefaults();
     }
 
     public void initDefaults() {
 
         setLogLevel(LogLevel.ERROR);
         setClientInfo(About.NAME + ":" + About.VERSION);
 
         setHost("api.crashnote.com");
         setSecure(true);
         setConnectionTimeout(30);
         setPort(80);
         setSSLPort(443);
 
         addEnvironmentFilter(".*aws.*");
         addEnvironmentFilter(".*key.*");
         addEnvironmentFilter(".*password.*");
     }
 
     // INTERFACE ==================================================================================
 
     public void print() {
         if(logger.isDebug()) {
             logger.debug("final configuration:");
             for(final String key : settings.keySet()) {
                 logger.debug(" -> {} = {}", key, settings.get(key));
             }
         }
     }
 
     // ==== Config Listener
 
     public void addListener(final IConfigChangeListener<C> listener) {
         if (!listeners.contains(listener)) listeners.add(listener);
     }
 
     public void removeListener(final IConfigChangeListener<C> listener) {
         listeners.remove(listener);
     }
 
     public void removeAllListeners() {
         listeners.clear();
     }
 
     /**
      * Notifies all listeners about changes to the config
      */
     public void updateComponentsConfig() {
         for (final IConfigChangeListener l : listeners) {
             if (l != null) l.updateConfig(this);
         }
     }
 
     // ==== Config Key
 
     public static String getConfigKey(final String propName) {
         return getConfigKey(propName, '.');
     }
 
    public static String getConfigKey(final String propName, final char separation) {
        return (propName.startsWith(PROP_PREFIX) ? propName : PROP_PREFIX + separation + propName).toLowerCase();
     }
 
     // FACTORY ====================================================================================
 
     /**
      * Create an instance of module 'Reporter'
      */
     public Reporter<C> getReporter() {
         return new Reporter(this);
     }
 
     /**
      * Create an instance of module 'Sender'
      */
     public Sender<C> getSender() {
         return new Sender(this);
     }
 
     /**
      * Create an instance of module 'Collector'
      */
     public Collector<C> getCollector() {
         return new Collector(this);
     }
 
     /**
      * Create an instance of module 'Builder'
      */
     public Builder getBuilder() {
         return new Builder();
     }
 
     /**
      * Create an instance of the system utility
      */
     public SystemUtil getSystemUtil() {
         return new SystemUtil();
     }
 
     /**
      * Create an instance of the internal logger
      */
     public LogLog getLogger(final String name) {
         return getLogFactory().getLogger(name);
     }
 
     /**
      * Create an instance of the internal logger
      */
     public LogLog getLogger(final Class clazz) {
         return getLogFactory().getLogger(clazz);
     }
 
     // SHARED =====================================================================================
 
     /**
      * Create an instance of a log factory
      */
     protected LogLogFactory<C> getLogFactory() {
         if (logFactory == null) logFactory = new LogLogFactory(this);
         return logFactory;
     }
 
     // ==== WRITE
 
     protected void addSetting(final String name, final String value) {
         if (value != null) {
             logger.debug("added config setting '{}' with value '{}'", name, value);
             settings.put(getConfigKey(name), parser.parseString(value));
         }
     }
 
     protected void addIntSetting(final String name, final String value) {
         if (value != null)
             try {
                 logger.debug("added config setting '{}' with value '{}'", name, value);
                 addSetting(name, parser.parseInt(value));
             } catch (IllegalArgumentException e) {
                 throw new IllegalArgumentException("parameter [" + name + "] is not a valid number: '" + value + "'", e);
             }
     }
 
     protected void addSetting(final String name, final int value) {
         logger.debug("added config setting '{}' with value '{}'", name, value);
         settings.put(getConfigKey(name), value);
     }
 
     protected void addBoolSetting(final String name, final String value) {
         if (value != null) {
             logger.debug("added config setting '{}' with value '{}'", name, value);
             addSetting(name, parser.parseBool(value));
         }
     }
 
     protected void addSetting(final String name, final boolean value) {
         logger.debug("added config setting '{}' with value '{}'", name, value);
         settings.put(getConfigKey(name), value);
     }
 
     protected void addSetting(final String name, final Object value) {
         if (value != null) {
             logger.debug("added config setting '{}' with value '{}'", name, value);
             settings.put(getConfigKey(name), value);
         }
     }
 
     // ==== READ
 
     protected boolean getBoolSetting(final String name) {
         final Object res = settings.get(getConfigKey(name));
         return (Boolean) ((res == null) ? false : res);
     }
 
     protected int getIntSetting(final String name) {
         final Integer res;
         try {
             res = (Integer) settings.get(getConfigKey(name));
         } catch (ClassCastException e) {
             throw new IllegalStateException("property " + name + " is not a number: " + getStringSetting(name));
         }
         return res;
     }
 
     protected String getStringSetting(final String name) {
         final Object res = settings.get(getConfigKey(name));
         return (res == null) ? null : res.toString();
     }
 
     protected Object getSetting(final String name) {
         return settings.get(getConfigKey(name));
     }
 
     // INTERNALS ==================================================================================
 
     private String getBaseUrl() {
         final boolean ssl = isSecure();
         return (ssl ? "https://" : "http://") + getHost() + ":" + (ssl ? getSslPort() : getPort());
     }
 
     // GET+ =======================================================================================
 
     public String getPostUrl() {
         final String url = getBaseUrl() + "/errors?key=" + getKey();
         logger.debug("resolved POST target URL: {}", url);
         return url;
     }
 
     public LogLevel getLogLevel() {
         final LogLevel maxLvl = LogLevel.INFO;
         final LogLevel reportLvl = getReportLogLevel();
         //final LogLevel historyLvl = getReportHistoryLevel();
         return LogLevel.getMaxLevel(maxLvl, reportLvl);
     }
 
     // GET ========================================================================================
 
     public String getClientInfo() {
         return getStringSetting(PROP_CLIENT);
     }
 
     public boolean isSync() {
         return getBoolSetting(PROP_SYNC);
     }
 
     public LogLevel getReportLogLevel() {
         return (LogLevel) getSetting(PROP_REP_LEVEL);
     }
 
     public String getKey() {
         return getStringSetting(PROP_API_KEY);
     }
 
     public boolean isEnabled() {
         return getBoolSetting(PROP_ENABLED);
     }
 
     public String[] getEnvironmentFilters() {
         final String filters = getStringSetting(PROP_REP_ENV_FILTER);
         if (filters == null || filters.length() == 0) return new String[0];
         else return filters.split(":");
     }
 
     public String getAppProfile() {
         return getStringSetting(PROP_APP_PROFILE);
     }
 
     public String getVersion() {
         return getStringSetting(PROP_APP_VERSION);
     }
 
     public String getBuild() {
         return getStringSetting(PROP_APP_VERSION);
     }
 
     public int getConnectionTimeout() {
         return getIntSetting(PROP_API_TIMEOUT);
     }
 
     public int getPort() {
         return getIntSetting(PROP_API_PORT);
     }
 
     public int getSslPort() {
         return getIntSetting(PROP_API_PORT_SSL);
     }
 
     public Boolean isSecure() {
         return getBoolSetting(PROP_API_SECURE);
     }
 
     public String getHost() {
         return getStringSetting(PROP_API_HOST);
     }
 
     public boolean isDebug() {
         return getBoolSetting(PROP_DEBUG);
     }
 
     public ApplicationType getApplicationType() {
         return (ApplicationType) getSetting(PROP_APP_TYPE);
     }
 
     // SET+ =======================================================================================
 
     public void addEnvironmentFilter(final String filter) {
         String filters = getStringSetting(PROP_REP_ENV_FILTER);
         if (filters == null || filters.length() == 0) filters = filter;
         else filters += ":" + filter;
         addSetting(PROP_REP_ENV_FILTER, filters.toLowerCase());
     }
 
     // SET ========================================================================================
 
     public void setClientInfo(final String info) {
         addSetting(PROP_CLIENT, info);
     }
 
     public void setSync(final String sync) {
         addBoolSetting(PROP_SYNC, sync);
     }
 
     private void setSync(final boolean sync) {
         addSetting(PROP_SYNC, sync);
     }
 
     public void setEnabled(final String enabled) {
         addBoolSetting(PROP_ENABLED, enabled);
     }
 
     public void setSSLPort(final String sslPort) {
         addIntSetting(PROP_API_PORT_SSL, sslPort);
     }
 
     public void setSecure(final String secure) {
         addBoolSetting(PROP_API_SECURE, secure);
     }
 
     public void setProfile(final String profile) {
         addSetting(PROP_APP_PROFILE, profile);
     }
 
     public void setBuild(final String build) {
         addSetting(PROP_APP_BUILD, build);
     }
 
     public void setAppType(final ApplicationType type) {
         addSetting(PROP_APP_TYPE, type);
     }
 
     public void setPort(final int port) {
         addSetting(PROP_API_PORT, port);
     }
 
     public void setSSLPort(final int sslPort) {
         addSetting(PROP_API_PORT_SSL, sslPort);
     }
 
     public void setHost(final String host) {
         addSetting(PROP_API_HOST, host);
     }
 
     public void setDebug(final Boolean debug) {
         addSetting(PROP_DEBUG, debug);
     }
 
     public void setEnabled(final boolean enabled) {
         addSetting(PROP_ENABLED, enabled);
     }
 
     public void setSecure(final Boolean secure) {
         addSetting(PROP_API_SECURE, secure);
     }
 
     public void setKey(final String key) {
         addSetting(PROP_API_KEY, key);
     }
 
     public void setPort(final String port) {
         addIntSetting(PROP_API_PORT, port);
     }
 
     public void setDebug(final String debug) {
         addBoolSetting(PROP_DEBUG, debug);
     }
 
     public void setVersion(final String version) {
         addSetting(PROP_APP_VERSION, version);
     }
 
     public void setConnectionTimeout(final String timeout) {
         addIntSetting(PROP_API_TIMEOUT, timeout);
     }
 
     public void setConnectionTimeout(final int timeout) {
         addSetting(PROP_API_TIMEOUT, timeout);
     }
 
     public void setLogLevel(final LogLevel level) {
         addSetting(PROP_REP_LEVEL, level);
     }
 
     public long getStartTime() {
         return startTime;
     }
 
     public List<IConfigChangeListener> getListeners() {
         return listeners;
     }
 }
