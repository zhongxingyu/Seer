 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2009 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.rapidcontext.app.plugin.Plugin;
 import org.rapidcontext.app.plugin.PluginClassLoader;
 import org.rapidcontext.app.plugin.PluginDataStore;
 import org.rapidcontext.app.plugin.PluginException;
 import org.rapidcontext.app.proc.AppletListProcedure;
 import org.rapidcontext.app.proc.PluginInstallProcedure;
 import org.rapidcontext.app.proc.PluginListProcedure;
 import org.rapidcontext.app.proc.PluginLoadProcedure;
 import org.rapidcontext.app.proc.PluginUnloadProcedure;
 import org.rapidcontext.app.proc.ProcedureListProcedure;
 import org.rapidcontext.app.proc.ProcedureReadProcedure;
 import org.rapidcontext.app.proc.ProcedureTypesProcedure;
 import org.rapidcontext.app.proc.ProcedureWriteProcedure;
 import org.rapidcontext.app.proc.ResetProcedure;
 import org.rapidcontext.app.proc.RoleChangeProcedure;
 import org.rapidcontext.app.proc.RoleListProcedure;
 import org.rapidcontext.app.proc.SessionCurrentProcedure;
 import org.rapidcontext.app.proc.SessionListProcedure;
 import org.rapidcontext.app.proc.SessionTerminateProcedure;
 import org.rapidcontext.app.proc.StatusProcedure;
 import org.rapidcontext.app.proc.ThreadContextProcedure;
 import org.rapidcontext.app.proc.ThreadCreateProcedure;
 import org.rapidcontext.app.proc.ThreadInterruptProcedure;
 import org.rapidcontext.app.proc.ThreadListProcedure;
 import org.rapidcontext.app.proc.UserChangeProcedure;
 import org.rapidcontext.app.proc.UserCheckAccessProcedure;
 import org.rapidcontext.app.proc.UserListProcedure;
 import org.rapidcontext.app.proc.UserPasswordChangeProcedure;
 import org.rapidcontext.core.data.Data;
 import org.rapidcontext.core.data.DataStore;
 import org.rapidcontext.core.data.DataStoreException;
 import org.rapidcontext.core.env.Environment;
 import org.rapidcontext.core.env.EnvironmentException;
 import org.rapidcontext.core.js.JsCompileInterceptor;
 import org.rapidcontext.core.js.JsProcedure;
 import org.rapidcontext.core.proc.CallContext;
 import org.rapidcontext.core.proc.Interceptor;
 import org.rapidcontext.core.proc.Library;
 import org.rapidcontext.core.proc.ProcedureException;
 import org.rapidcontext.core.security.SecurityContext;
 import org.rapidcontext.core.security.SecurityInterceptor;
 import org.rapidcontext.util.FileUtil;
 
 /**
  * The application context. This is a singleton object that contains
  * references to global application settings and objects. It also
  * provides simple procedure execution and resource and plug-in
  * initialization and deinitialization.
  *
  * @author   Per Cederberg, Dynabyte AB
  * @version  1.0
  */
 public class ApplicationContext {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(ApplicationContext.class.getName());
 
     /**
      * The singleton application context instance.
      */
     private static ApplicationContext instance = null;
 
     /**
      * The configuration data store.
      */
     private PluginDataStore dataStore;
 
     /**
      * The application configuration.
      */
     private Data config;
 
     /**
      * The map of plug-in instances. The map is indexed by the
      * plug-in identifier.
      */
     private HashMap plugins = new HashMap();
 
     /**
      * The plug-in directory. This is the base directory from which
      * plug-ins are loaded.
      */
     private File pluginDir = null;
 
     /**
      * The plug-in class loader.
      */
     private PluginClassLoader pluginClassLoader = new PluginClassLoader();
 
     /**
      * The active environment.
      */
     private Environment env = null;
 
     /**
      * The procedure library.
      */
     private Library library = null;
 
     /**
      * The thread call context map.
      */
     private Map threadContext = Collections.synchronizedMap(new HashMap());
 
     /**
      * Initializes the application context by loading the plug-ins, procedures
      * and the environment configuration. If the context has already been
      * initialized, no action is taken.
     *
     * @param baseDir        the base application directory
     *
     * @return the application context created or found
      */
     protected static synchronized ApplicationContext init(File baseDir) {
         if (instance == null) {
             instance = new ApplicationContext(baseDir);
             instance.initAll();
         }
         return instance;
     }
 
     /**
      * Destroys the application context and frees all resources used.
      */
     protected static synchronized void destroy() {
         if (instance != null) {
             instance.destroyAll();
             instance = null;
         }
     }
 
     /**
      * Returns the singleton application context instance.
      *
      * @return the singleton application context instance
      */
     public static ApplicationContext getInstance() {
         return instance;
     }
 
     /**
      * Creates a new application context. This constructor should
      * only be called once in the application and it will store away
      * the instance created.
      *
      * @param baseDir        the base application directory
      */
     private ApplicationContext(File baseDir) {
         this.pluginDir = new File(baseDir, "plugins");
         this.dataStore = new PluginDataStore(this.pluginDir);
         this.library = new Library(this.dataStore);
         instance = this;
     }
 
     /**
      * Initializes this context by loading the plug-ins, procedures
      * and the environment configuration.
      */
     private void initAll() {
         try {
             config = dataStore.readData(null, "config");
         } catch (DataStoreException e) {
             LOG.severe("failed to load application config: " + e.getMessage());
         }
         initLibrary();
         initPlugins();
         try {
             env = Environment.init(dataStore);
         } catch (EnvironmentException e) {
             LOG.severe("Failed to load environment: " + e.getMessage());
         }
         try {
             SecurityContext.init(dataStore);
         } catch (DataStoreException e) {
             LOG.severe("Failed to load security config: " + e.getMessage());
         }
     }
 
     /**
      * Initializes the library in this context.
      */
     private void initLibrary() {
         Interceptor  i;
 
         // Register default procedure types
         try {
             Library.registerType("javascript", JsProcedure.class);
         } catch (ProcedureException e) {
             LOG.severe("failed to register javascript procedure type: " +
                        e.getMessage());
         }
 
         // Add default interceptors
         i = library.getInterceptor();
         i = new JsCompileInterceptor(i);
         i = new SecurityInterceptor(i);
         library.setInterceptor(i);
 
         // Add default built-in procedures
         try {
             library.addBuiltIn(new AppletListProcedure());
             library.addBuiltIn(new PluginInstallProcedure());
             library.addBuiltIn(new PluginListProcedure());
             library.addBuiltIn(new PluginLoadProcedure());
             library.addBuiltIn(new PluginUnloadProcedure());
             library.addBuiltIn(new ProcedureListProcedure());
             library.addBuiltIn(new ProcedureReadProcedure());
             library.addBuiltIn(new ProcedureTypesProcedure());
             library.addBuiltIn(new ProcedureWriteProcedure());
             library.addBuiltIn(new ResetProcedure());
             library.addBuiltIn(new RoleChangeProcedure());
             library.addBuiltIn(new RoleListProcedure());
             library.addBuiltIn(new SessionCurrentProcedure());
             library.addBuiltIn(new SessionListProcedure());
             library.addBuiltIn(new SessionTerminateProcedure());
             library.addBuiltIn(new StatusProcedure());
             library.addBuiltIn(new ThreadContextProcedure());
             library.addBuiltIn(new ThreadCreateProcedure());
             library.addBuiltIn(new ThreadInterruptProcedure());
             library.addBuiltIn(new ThreadListProcedure());
             library.addBuiltIn(new UserChangeProcedure());
             library.addBuiltIn(new UserCheckAccessProcedure());
             library.addBuiltIn(new UserListProcedure());
             library.addBuiltIn(new UserPasswordChangeProcedure());
         } catch (ProcedureException e) {
             LOG.severe("failed to create built-in procedures: " +
                        e.getMessage());
         }
     }
 
     /**
      * Loads all plug-ins listed in an application specific plug-in
      * configuration file. Also loads any jar libraries found in the
      * plug-in "lib" directories.
      */
     private void initPlugins() {
         Data    list;
         String  pluginId;
 
         list = config.getData("plugins");
         for (int i = 0; i < list.arraySize(); i++) {
             try {
                 pluginId = list.getString(i, null);
                 loadPlugin(pluginId);
             } catch (PluginException e) {
                 LOG.warning("failed to load plugin " +
                             list.getString(i, null) + ": " +
                             e.getMessage());
             }
         }
     }
 
     /**
      * Destroys this context and frees all resources.
      */
     private void destroyAll() {
         if (env != null) {
             env.removeAllPools();
             env = null;
         }
         destroyPlugins();
         Library.unregisterType("javascript");
         library = new Library(this.dataStore);
     }
 
     /**
      * Destroys all loaded plug-ins.
      */
     private void destroyPlugins() {
         String[]  ids;
 
         ids = new String[plugins.size()];
         plugins.keySet().toArray(ids);
         for (int i = 0; i < ids.length; i++) {
             try {
                 destroyPlugin(ids[i]);
             } catch (PluginException e) {
                 LOG.warning("failed to unload " + ids[i] +
                             " plugin: " + e.getMessage());
             }
         }
         pluginClassLoader = new PluginClassLoader();
     }
 
     /**
      * Resets this context and reloads all resources.
      */
     public void reset() {
         destroyAll();
         initAll();
     }
 
     /**
      * Returns the application configuration.
      *
      * @return the application configuration
      */
     public Data getConfig() {
         return this.config;
     }
 
     /**
      * Returns the application data store. This is the global data
      * store that contains all loaded plug-ins and maps requests to
      * them in order.
      *
      * @return the application data store
      */
     public PluginDataStore getDataStore() {
         return this.dataStore;
     }
 
     /**
      * Returns the environment used.
      *
      * @return the environment used
      */
     public Environment getEnvironment() {
         return this.env;
     }
 
     /**
      * Returns the procedure library used.
      *
      * @return the procedure library used
      */
     public Library getLibrary() {
         return library;
     }
 
     /**
      * Returns the plug-in base directory.
      *
      * @return the plug-in base directory
      */
     public File getPluginDir() {
         return pluginDir;
     }
 
     /**
      * Returns the plug-in class loader.
      *
      * @return the plug-in class loader
      */
     public PluginClassLoader getClassLoader() {
         return pluginClassLoader;
     }
 
     /**
      * Installs a plug-in from the specified file. If an existing
      * plug-in with the same id already exists, it will be
      * replaced without warning. After installation, the new plug-in
      * will also be loaded and added to the default configuration
      * for automatic launch on the next restart.
      *
      * @param file           the plug-in ZIP file
      *
      * @return the unique plug-in id
      *
      * @throws PluginException if the plug-in couldn't be installed
      *             correctly
      */
     public String installPlugin(File file) throws PluginException {
         ZipFile      zip = null;
         ZipEntry     entry;
         Properties   props;
         InputStream  is;
         String       pluginId;
         File         dir;
         String       msg;
 
         try {
             zip = new ZipFile(file);
             entry = zip.getEntry("plugin.properties");
             if (entry == null) {
                 msg = "missing plugin.properties inside zip file " + file.getName();
                 LOG.warning(msg);
                 throw new PluginException(msg);
             }
             is = zip.getInputStream(entry);
             props = new Properties();
             try {
                 props.load(is);
             } finally {
                 try {
                     is.close();
                 } catch (Exception ignore) {
                     // Ignore exception on closing file
                 }
             }
             pluginId = props.getProperty("id");
             if (pluginId == null || pluginId.trim().length() < 0) {
                 msg = "missing plug-in identifier in plugin.properties";
                 throw new PluginException(msg);
             }
             dir = new File(pluginDir, pluginId);
             if (dir.exists()) {
                 unloadPlugin(pluginId);
                 // TODO: perhaps backup the old directory instead?
                 FileUtil.deleteTree(dir);
             }
             FileUtil.unpackZip(zip, dir);
             loadPlugin(pluginId);
         } catch (IOException e) {
             msg = "IO error while reading zip file " + file.getName() + ": " +
                   e.getMessage();
             LOG.warning(msg);
             throw new PluginException(msg);
         } finally {
             if (zip != null) {
                 try {
                     zip.close();
                 } catch (IOException ignore) {
                     // Do nothing
                 }
             }
         }
         return pluginId;
     }
 
     /**
      * Loads a plug-in. If the plug-in was loaded successfully, it will
      * also be added to the default configuration for automatic
      * launch on the next restart.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if no plug-in instance could be created
      *             or if the plug-in initialization failed
      */
     public void loadPlugin(String pluginId) throws PluginException {
         DataStore    pluginStore;
         Data         pluginData;
         Data         pluginList;
         String       className;
         Class        cls;
         Object       obj;
         Plugin       plugin;
         String       msg;
 
         if (PluginDataStore.DEFAULT_PLUGIN.equals(pluginId) ||
             PluginDataStore.LOCAL_PLUGIN.equals(pluginId)) {
 
             msg = "cannot force loading of default or local plug-ins";
             throw new PluginException(msg);
         }
         pluginStore = dataStore.addPlugin(pluginId);
         try {
             pluginData = pluginStore.readData(null, "plugin");
             if (pluginData == null) {
                 throw new DataStoreException("file not found");
             }
         } catch (DataStoreException e) {
             dataStore.removePlugin(pluginId);
             msg = "couldn't load " + pluginId + " plugin config file: " +
                   e.getMessage();
             LOG.warning(msg);
             throw new PluginException(msg);
         }
         pluginClassLoader.addPluginJars(new File(this.pluginDir, pluginId));
         className = pluginData.getString("className", null);
         if (className == null || className.trim().length() <= 0) {
             plugin = new Plugin();
         } else {
             try {
                 cls = getClassLoader().loadClass(className);
             } catch (Throwable e) {
                 msg = "couldn't load " + pluginId + " plugin class " +
                       className + ": " + e.getMessage();
                 LOG.warning(msg);
                 throw new PluginException(msg);
             }
             try {
                 obj = cls.newInstance();
             } catch (Throwable e) {
                 msg = "couldn't create " + pluginId +
                       " plugin instance for " + className + ": " +
                       e.getMessage();
                 LOG.warning(msg);
                 throw new PluginException(msg);
             }
             if (obj instanceof Plugin) {
                 plugin = (Plugin) obj;
             } else {
                 msg = pluginId + " plugin class " + className +
                       " doesn't implement the Plugin interface";
                 LOG.warning(msg);
                 throw new PluginException(msg);
             }
         }
         plugin.setData(pluginData);
         try {
             plugin.init();
         } catch (Throwable e) {
             msg = pluginId + " plugin class " + className +
                   " threw exception on init: " + e.getMessage();
             LOG.warning(msg);
             throw new PluginException(msg);
         }
         plugins.put(pluginId, plugin);
         pluginList = config.getData("plugins");
         if (!pluginList.containsValue(pluginId)) {
             pluginList.add(pluginId);
             try {
                 dataStore.writeData(null, "config", config);
             } catch (DataStoreException e) {
                 msg = "failed to update application config: " +
                       e.getMessage();
                 throw new PluginException(msg);
             }
         }
     }
 
     /**
      * Unloads a plug-in. The plug-in will also be removed from the
      * default configuration for automatic launches.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if the plug-in deinitialization failed
      */
     public void unloadPlugin(String pluginId) throws PluginException {
         Data    pluginList;
         int     pos;
         String  msg;
 
         if (PluginDataStore.DEFAULT_PLUGIN.equals(pluginId) ||
             PluginDataStore.LOCAL_PLUGIN.equals(pluginId)) {
 
             msg = "cannot unload default or local plug-ins";
             throw new PluginException(msg);
         }
         destroyPlugin(pluginId);
         pluginList = config.getData("plugins");
         pos = pluginList.indexOf(pluginId);
         if (pos >= 0) {
             pluginList.remove(pos);
             try {
                 dataStore.writeData(null, "config", config);
             } catch (DataStoreException e) {
                 msg = "failed to update application config: " +
                       e.getMessage();
                 throw new PluginException(msg);
             }
         }
     }
 
 
     /**
      * Destroys a plug-in (as a part of the unloading). The plug-in
      * will only be stopped and removed from in-memory data
      * structures by this method.
      *
      * @param pluginId       the unique plug-in id
      *
      * @throws PluginException if the plug-in deinitialization failed
      */
     private void destroyPlugin(String pluginId) throws PluginException {
         Plugin  plugin;
 
         plugin = (Plugin) plugins.get(pluginId);
         if (plugin != null) {
             plugin.destroy();
         }
         dataStore.removePlugin(pluginId);
         plugins.remove(pluginId);
         library.clearCache();
     }
 
     /**
      * Executes a procedure within this context.
      *
      * @param name           the procedure name
      * @param args           the procedure arguments
      * @param source         the call source information
      * @param trace          the trace buffer or null for none
      *
      * @return the result of the call, or
      *         null if the call produced no result
      *
      * @throws ProcedureException if the procedure execution failed
      */
     public Object execute(String name,
                           Object[] args,
                           String source,
                           StringBuffer trace)
         throws ProcedureException {
 
         CallContext  cx = new CallContext(dataStore, env, library);
 
         threadContext.put(Thread.currentThread(), cx);
         cx.setAttribute(CallContext.ATTRIBUTE_USER,
                         SecurityContext.currentUser());
         cx.setAttribute(CallContext.ATTRIBUTE_SOURCE, source);
         if (trace != null) {
             cx.setAttribute(CallContext.ATTRIBUTE_TRACE, Boolean.TRUE);
             cx.setAttribute(CallContext.ATTRIBUTE_LOG_BUFFER, trace);
         }
         try {
             return cx.execute(name, args);
         } finally {
             threadContext.remove(Thread.currentThread());
         }
     }
 
     /**
      * Executes a procedure asynchronously within this context. This
      * method will sleep for 10 minutes after terminating the
      * procedure execution, allowing the results to be fetched from
      * the context by another thread.
      *
      * @param name           the procedure name
      * @param args           the procedure arguments
      * @param source         the call source information
      */
     public void executeAsync(String name, Object[] args, String source) {
         CallContext  cx = new CallContext(dataStore, env, library);
         Object       res;
 
         threadContext.put(Thread.currentThread(), cx);
         cx.setAttribute(CallContext.ATTRIBUTE_USER,
                         SecurityContext.currentUser());
         cx.setAttribute(CallContext.ATTRIBUTE_SOURCE, source);
         try {
             res = cx.execute(name, args);
             cx.setAttribute(CallContext.ATTRIBUTE_RESULT, res);
         } catch (Exception e) {
             cx.setAttribute(CallContext.ATTRIBUTE_ERROR, e.getMessage());
         } finally {
             // Delay thread context removal for 10 minutes
             try {
                 Thread.sleep(600000);
             } catch (InterruptedException ignore) {
                 // Allow thread interrupt to remove context
             }
             threadContext.remove(Thread.currentThread());
         }
     }
 
     /**
      * Finds the currently active call context for a thread.
      *
      * @param thread         the thread to search for
      *
      * @return the call context found, or
      *         null if no context was active
      */
     public CallContext findContext(Thread thread) {
         return (CallContext) threadContext.get(thread);
     }
 
     /**
      * Finds the currently active call context for a thread id. The
      * thread id is identical to the hash code for the thread.
      *
      * @param threadId       the thread id to search for
      *
      * @return the call context found, or
      *         null if no context was active
      */
     public CallContext findContext(int threadId) {
         Iterator   iter;
         Object     obj;
 
         synchronized (threadContext) {
             iter = threadContext.keySet().iterator();
             while (iter.hasNext()) {
                 obj = iter.next();
                 if (obj.hashCode() == threadId) {
                     return (CallContext) threadContext.get(obj);
                 }
             }
         }
         return null;
     }
 }
