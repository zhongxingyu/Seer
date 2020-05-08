 /*
  * Copyright (c) 2013 KloudTek Ltd
  */
 
 package com.kloudtek.systyrant;
 
 import com.kloudtek.systyrant.annotation.Inject;
 import com.kloudtek.systyrant.annotation.Provider;
 import com.kloudtek.systyrant.annotation.Service;
 import com.kloudtek.systyrant.context.*;
 import com.kloudtek.systyrant.dsl.DSLScriptingEngineFactory;
 import com.kloudtek.systyrant.exception.*;
 import com.kloudtek.systyrant.host.Host;
 import com.kloudtek.systyrant.host.LocalHost;
 import com.kloudtek.systyrant.provider.ProviderManager;
 import com.kloudtek.systyrant.provider.ProvidersManagementService;
 import com.kloudtek.systyrant.service.filestore.FileStore;
 import org.apache.commons.io.IOUtils;
 import org.jetbrains.annotations.NotNull;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.script.Bindings;
 import javax.script.ScriptEngine;
 import javax.script.ScriptException;
 import java.io.*;
 import java.lang.reflect.Field;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.locks.ReadWriteLock;
 
 import static javax.script.ScriptContext.ENGINE_SCOPE;
 
 /**
  * This is the "brains" of SysTyrant, which contains the application's state
  */
 public class STContext implements AutoCloseable {
     private static final HashMap<String, String> scriptingSupport = new HashMap<>();
     private static final Logger logger = LoggerFactory.getLogger(STContext.class);
     static ThreadLocal<STContext> ctx = new ThreadLocal<>();
     final STContextData data = new STContextData();
     private STCLifecycleExecutor lifecycleExecutor = new STCLifecycleExecutor(this, data);
 
     static {
         try {
             scriptingSupport.put("rb", IOUtils.toString(STContext.class.getResourceAsStream("context/systyrant.rb")));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public STContext() throws InvalidResourceDefinitionException, STRuntimeException {
         this(new LocalHost());
         data.host.start();
     }
 
     public STContext(Host host) throws InvalidResourceDefinitionException, STRuntimeException {
         data.host = host;
         data.scriptEngineManager.registerEngineExtension("stl", new DSLScriptingEngineFactory(this));
         data.resourceManager = new ResourceManagerImpl(this, data);
         data.serviceManager = new ServiceManagerImpl(this, data);
         registerLibrary(new Library());
         data.providersManagementService.init(data.reflections);
         inject(host);
     }
 
     // ------------------------------------------------------------------------------------------
     // Libraries
     // ------------------------------------------------------------------------------------------
 
     public void registerLibraries(File libDir) {
         logger.debug("Scanning libraries directory {}", libDir);
         if (libDir.exists()) {
             File[] childrens = libDir.listFiles();
             if (childrens != null) {
                 for (File children : childrens) {
                     try {
                         registerLibrary(children);
                     } catch (IOException e) {
                         logger.error("Failed to load module {}: {}", children.getPath(), e.getLocalizedMessage());
                     } catch (InvalidResourceDefinitionException e) {
                         logger.error("Invalid resource definition: {}", e.getLocalizedMessage(), e);
                     }
                 }
             }
         }
     }
 
     public void registerLibrary(File libraryFile) throws IOException, InvalidResourceDefinitionException {
         logger.debug("Registering module {}", libraryFile.getPath());
         Library library = new Library(libraryFile);
         registerLibrary(library);
     }
 
     private void registerLibrary(Library library) throws InvalidResourceDefinitionException {
         data.libraryLock.writeLock().lock();
         try {
             data.newLibAdded = true;
             data.libraries.add(library);
             logger.debug("Adding library {} to the module classloader", library.getLocationUrl());
             data.libraryClassloader.addURL(library.getLocationUrl());
             for (Class<?> clazz : library.getResourceDefinitionClasses()) {
                 data.resourceManager.registerJavaResource(clazz);
             }
             Set<Class<?>> services = library.getReflections().getTypesAnnotatedWith(Service.class);
             try {
                 for (Class<?> service : services) {
                     data.serviceManager.registerService(service);
                 }
             } catch (InvalidServiceException e) {
                 throw new InvalidResourceDefinitionException(e.getMessage(), e);
             }
             if (data.reflections != null) {
                 data.reflections.merge(library.getReflections());
             } else {
                 data.reflections = library.getReflections();
             }
         } finally {
             data.libraryLock.writeLock().unlock();
         }
     }
 
     public ResourceManager getResourceManager() {
         return data.resourceManager;
     }
 
     public ServiceManager getServiceManager() {
         return data.serviceManager;
     }
 
     public ProvidersManagementService getProvidersManagementService() {
         return data.providersManagementService;
     }
 
     public List<Library> getLibraries() {
         return Collections.unmodifiableList(data.libraries);
     }
 
     // ------------------------------------------------------------------------------------------
     // Resource setup / registration
     // ------------------------------------------------------------------------------------------
 
     public void runScript(String path) throws IOException, ScriptException {
         runScript(null, path);
     }
 
     public synchronized void runScript(String pkg, String path) throws IOException, ScriptException {
         URL script = getClass().getResource(path);
         if (script == null) {
             script = ClassLoader.getSystemResource(path);
         }
         if (script == null) {
             throw new IOException("Unable to find script " + path);
         }
         try {
             runScript(pkg, script.toURI());
         } catch (URISyntaxException e) {
             throw new IOException("Invalid path: " + path);
         }
     }
 
     public void runScript(@NotNull URL url) throws IOException, ScriptException {
         runScript(null, url);
     }
 
     public void runScript(String pkg, @NotNull URL url) throws IOException, ScriptException {
         try {
             runScript(pkg, url.toURI());
         } catch (URISyntaxException e) {
             throw new ScriptException("Invalid url: " + url);
         }
     }
 
     public synchronized void runScript(@NotNull URI uri) throws IOException, ScriptException {
         runScript(null, uri);
     }
 
     public synchronized void runScript(String pkg, @NotNull URI uri) throws IOException, ScriptException {
         if (!uri.isAbsolute()) {
             uri = new File(uri.getPath()).getAbsoluteFile().toURI();
         }
         InputStreamReader reader = new InputStreamReader(uri.toURL().openConnection().getInputStream());
         runScript(pkg, uri.toString(), reader);
         reader.close();
     }
 
     public synchronized void runScript(String path, Reader scriptReader) throws IOException, ScriptException {
         runScript(null, path, scriptReader);
     }
 
     public synchronized void runScript(String pkg, String path, Reader scriptReader) throws IOException, ScriptException {
         boolean ctxMissing = ctx.get() == null;
         if (ctxMissing) {
             ctx.set(this);
         }
         if (path == null) {
             path = UUID.randomUUID().toString() + ".stl";
         }
         int dotIdx = path.lastIndexOf('.');
         if (dotIdx == -1) {
             throw new IOException("path has no extension: " + path);
         }
         String ext = path.substring(dotIdx + 1).toLowerCase();
         ScriptEngine scriptEngine = getScriptEngineByExt(ext);
         Bindings bindings = scriptEngine.getBindings(ENGINE_SCOPE);
         bindings.put("package", pkg);
         bindings.put("ctx", this);
         bindings.put("stsm", getServiceManager());
         bindings.put("strm", getResourceManager());
         String support = scriptingSupport.get(ext);
         if (support != null) {
             scriptEngine.eval(support, bindings);
         }
         scriptEngine.eval(scriptReader, bindings);
         if (ctxMissing) {
             ctx.remove();
         }
     }
 
     public void runDSLScript(String dsl) throws IOException, ScriptException {
         runScript(null, new StringReader(dsl));
     }
 
     /**
      * Get a cached script engine. Please note that since we're caching them, those are not thread-safe due to the requirement
      * of passing the package as a binding.
      *
      * @param ext Extension
      * @return Script Engine
      * @throws IOException
      */
     private ScriptEngine getScriptEngineByExt(String ext) throws IOException {
         ScriptEngine scriptEngine;
         synchronized (data.scriptEnginesByExtCache) {
             scriptEngine = data.scriptEnginesByExtCache.get(ext);
             if (scriptEngine == null) {
                 scriptEngine = data.scriptEngineManager.getEngineByExtension(ext);
                 if (scriptEngine == null) {
                     throw new IOException("Unable to run scripts with the extension: " + ext);
                 }
                 data.scriptEnginesByExtCache.put(ext, scriptEngine);
             }
         }
         return scriptEngine;
     }
 
    public synchronized Stage getStage() {
        return data.stage;
    }

     public Host host() {
         return data.host;
     }
 
     public Host getHost() {
         return data.host;
     }
 
     public void setHost(Host host) throws STRuntimeException {
         if (host == null) {
             throw new IllegalArgumentException("Host cannot be null");
         }
         data.executionLock.writeLock().lock();
         try {
             data.host = host;
             inject(host);
         } finally {
             data.executionLock.writeLock().unlock();
         }
     }
 
     public FileStore files() throws InvalidServiceException {
         return data.serviceManager.getService(FileStore.class);
     }
 
     // ------------------------------------------------------------------------------------------
     // Lifecycle
     // ------------------------------------------------------------------------------------------
 
     public boolean execute() throws STRuntimeException {
         STContext.ctx.set(this);
         try {
             return lifecycleExecutor.execute();
         } finally {
             STContext.ctx.remove();
         }
     }
 
 
     @Override
     public void close() {
         ctx.remove();
         data.resourceManager.close();
         for (Library library : data.libraries) {
             library.close();
         }
         data.serviceManager.close();
     }
 
     // ------------------------------------------------------------------------------------------
     // Notification
     // ------------------------------------------------------------------------------------------
 
     public void notify(String resourceQuery, String category) throws InvalidQueryException {
         for (Resource resource : findResources(resourceQuery)) {
             notify(resource, category);
         }
     }
 
     public void notify(Resource resource) {
         notify(currentResource(), resource, null);
     }
 
     public void notify(Resource resource, String category) {
         notify(currentResource(), resource, category);
     }
 
     public void notify(Resource source, Resource resource, String category) {
         synchronized (data.notificationsPending) {
             data.notificationsPending.add(new Notification(category, source, resource));
         }
     }
 
     public void addAutoNotification(AutoNotify autoNotify) {
         data.add(autoNotify);
     }
 
     // ------------------------------------------------------------------------------------------
     // Manage scopes
     // ------------------------------------------------------------------------------------------
 
     public Resource currentResource() {
         return data.resourceScope.get();
     }
 
     public void setCurrentResource(Resource resource) {
         data.resourceScope.set(resource);
     }
 
     public List<String> getImports() {
         List<String> list = data.importPaths.get();
         if (list == null) {
             return Collections.emptyList();
         } else {
             return list;
         }
     }
 
     public void addImport(String value) {
         List<String> list = data.importPaths.get();
         if (list == null) {
             list = new ArrayList<>();
             data.importPaths.set(list);
         }
         list.add(value);
     }
 
     public void clearImports() {
         data.importPaths.remove();
     }
 
     // ------------------------------------------------------------------------------------------
     // Other runtime methods for use by resources or calling code
     // ------------------------------------------------------------------------------------------
 
     public static STContext get() {
         return ctx.get();
     }
 
     public boolean hasResources() {
         return data.resourceManager.hasResources();
     }
 
     // ------------------------------------------------------------------------------------------
     // Resources lookup
     // ------------------------------------------------------------------------------------------
 
     public List<Resource> getResources() {
         return data.resourceManager.getResources();
     }
 
     /**
      * Find a resource using the current resource's parent as a base resource.
      *
      * @param query Query.
      * @return List of resources that match the specified query.
      * @throws InvalidQueryException If the query was invalid.
      * @see #currentResource()
      */
     @NotNull
     public List<Resource> findResources(String query) throws InvalidQueryException {
         Resource baseResource = currentResource();
         if (baseResource != null) {
             baseResource = baseResource.getParent();
         }
         return data.resourceManager.findResources(query, baseResource);
     }
 
     @NotNull
     public List<Resource> findResources(String query, Resource baseResource) throws InvalidQueryException {
         return data.resourceManager.findResources(query, baseResource);
     }
 
     public Resource findResourceByUid(String uid) {
         return data.resourceManager.findResourcesByUid(uid);
     }
 
     // ------------------------------------------------------------------------------------------
     // Other
     // ------------------------------------------------------------------------------------------
 
     /**
      * Retrieve the root resource lock.
      * This is currently used to make id generation threadsafe for resources with no parents.
      *
      * @return ReadWriteLock
      */
     public ReadWriteLock getRootResourceLock() {
         return data.rootResourceLock;
     }
 
     public ClassLoader getLibraryClassloader() {
         if (data.libraryClassloader != null) {
             return data.libraryClassloader;
         } else {
             return getClass().getClassLoader();
         }
     }
 
     public void registerTempFile(File tempFile) {
         data.tempFiles.add(tempFile);
     }
 
     public synchronized Resource getDefaultParent() {
         return data.defaultParent;
     }
 
     public synchronized void setDefaultParent(Resource defaultParent) {
         this.data.defaultParent = defaultParent;
     }
 
     public List<Class<? extends Exception>> getFatalExceptions() {
         return data.fatalExceptions;
     }
 
     public void setFatalExceptions(List<Class<? extends Exception>> fatalExceptions) {
         this.data.fatalExceptions = fatalExceptions;
     }
 
     @SafeVarargs
     public final void setFatalExceptions(Class<? extends Exception>... fatalExceptions) {
         this.data.fatalExceptions = fatalExceptions != null ? Arrays.asList(fatalExceptions) : null;
     }
 
     public void clearFatalException() {
         data.fatalExceptions = null;
     }
 
     public Object invokeMethod(String name, Parameters params) throws STRuntimeException {
         return data.serviceManager.invokeMethod(name, params);
     }
 
     public void inject(Object obj) throws InjectException {
         Class<?> cl = obj.getClass();
         while (cl != null) {
             for (Field field : cl.getDeclaredFields()) {
                 Provider provider = field.getAnnotation(Provider.class);
                 Class<?> type = field.getType();
                 try {
                     if (provider != null) {
                         ProviderManager pm = data.providersManagementService.getProviderManager(type.asSubclass(ProviderManager.class));
                         if (!field.isAccessible()) {
                             field.setAccessible(true);
                         }
                         field.set(obj, pm);
                     }
                     Inject inject = field.getAnnotation(Inject.class);
                     if (inject != null) {
                         if (STContext.class.isAssignableFrom(type)) {
                             if (!field.isAccessible()) {
                                 field.setAccessible(true);
                             }
                             field.set(obj, this);
                         } else {
                             throw new InjectException("Cannot inject object " + obj.getClass().getName() + "#" + field.getName());
                         }
                     }
                 } catch (IllegalAccessException e) {
                     throw new InjectException("Cannot inject object " + obj.getClass().getName() + "#" + field.getName());
                 }
             }
             cl = cl.getSuperclass();
         }
     }
 }
 
