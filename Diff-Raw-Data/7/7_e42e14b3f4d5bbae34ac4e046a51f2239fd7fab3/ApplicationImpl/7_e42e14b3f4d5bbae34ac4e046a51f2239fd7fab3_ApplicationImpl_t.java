 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.application;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 
 import com.nginious.http.HttpException;
 import com.nginious.http.HttpMethod;
 import com.nginious.http.HttpRequest;
 import com.nginious.http.HttpResponse;
 import com.nginious.http.HttpStatus;
 import com.nginious.http.annotation.Controller;
 import com.nginious.http.annotation.Service;
 import com.nginious.http.server.Header;
 import com.nginious.http.xsp.CompilableXspService;
 import com.nginious.http.xsp.XspCompiler;
 import com.nginious.http.xsp.XspException;
 import com.nginious.http.xsp.XspService;
 
 /*
  * A concrete application implementation.
  */
 class ApplicationImpl implements Application {
 	
 	private static final byte[] FAVICON_EMPTY = { 0x0d, 0x0a };
 	
 	private static HashSet<HttpMethod> methods;
 	
 	static {
 		methods = new HashSet<HttpMethod>();
 		methods.add(HttpMethod.HEAD);
 		methods.add(HttpMethod.GET);
 		methods.add(HttpMethod.POST);
 		methods.add(HttpMethod.PUT);
 		methods.add(HttpMethod.DELETE);
 	}
 	
 	private String name;
 	
 	private Date publishTime;
 	
 	private boolean directory;
 	
 	private boolean war;
 	
 	private boolean memory;
 	
 	private File baseDir;
 	
 	private ControllerServiceFactory controllerFactory;
 	
 	private ConcurrentHashMap<String, HttpService> executableControllers;
 	
 	private ConcurrentHashMap<String, ServiceRunner> services;
 	
 	private HashSet<Object> addedControllers;
 	
 	private TreeSet<HttpControllerFilter> filterControllers = new TreeSet<HttpControllerFilter>();
 	
 	private HashMap<String, String> allowedControllerMethods = new HashMap<String, String>();
 	
 	private ApplicationClassLoader classLoader;
 	
 	/*
 	 * Constructs a new application with the specified name.
 	 */
 	ApplicationImpl(String name) {
 		this.name = name;
 		this.publishTime = new Date();
 		this.classLoader = new ApplicationClassLoader(Thread.currentThread().getContextClassLoader());
 		this.controllerFactory = new ControllerServiceFactory(this.classLoader);
 		controllerFactory.setApplication(this);
 		
 		this.executableControllers = new ConcurrentHashMap<String, HttpService>();
 		this.services = new ConcurrentHashMap<String, ServiceRunner>();
 		this.addedControllers = new HashSet<Object>();
 		this.allowedControllerMethods = new HashMap<String, String>();
 		this.filterControllers = new TreeSet<HttpControllerFilter>();
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	void setName(String name) {
 		this.name = name;
 	}
 	
 	public File getBaseDir() {
 		return this.baseDir;
 	}
 
 	public void setBaseDir(File baseDir) {
 		this.baseDir = baseDir;
 		classLoader.setWebAppDir(baseDir);
 	}
 	
 	void setClassLoader(ApplicationClassLoader classLoader) {
 		this.classLoader = classLoader;
 	}
 	
 	ClassLoader getClassLoader() {
 		return this.classLoader;
 	}
 	
 	boolean isDirectory() {
 		return this.directory;
 	}
 	
 	void setDirectory(boolean directory) {
 		this.directory = directory;
 	}
 	
 	boolean isWar() {
 		return this.war;
 	}
 	
 	void setWar(boolean war) {
 		this.war = war;
 	}
 	
 	boolean isMemory() {
 		return this.memory;
 	}
 	
 	void setMemory(boolean memory) {
 		this.memory = memory;
 	}
 	
 	boolean isUnpacked() {
 		return !this.war && !this.memory;
 	}
 	
 	Date getPublishTime() {
 		return this.publishTime;
 	}
 	
 	public void addController(Object controller) throws ApplicationException {
 		addController(controller, null, false);
 	}
 	
 	void addController(Object controller, File classFile, boolean reloadable) throws ApplicationException {
 		try {
 			Controller mapping = controller.getClass().getAnnotation(Controller.class);
 			String path = mapping.path();
 			String pattern = mapping.pattern();
 			ControllerService invokerService = controllerFactory.createControllerService(controller);
 			
 			if(reloadable) {
 				String className = controller.getClass().getName();
 				HttpService reloadableService = new ReloadableControllerService(controllerFactory, invokerService, this.classLoader, className, classFile);
 				addHttpService(path, reloadableService, invokerService.getHttpMethods(), pattern, mapping);
 			} else {
 				addHttpService(path, invokerService, invokerService.getHttpMethods(), pattern, mapping);
 			}
 
 			addedControllers.add(controller);
 		} catch(ControllerServiceFactoryException e) {
 			throw new ApplicationException("Can't add controller '" + controller.getClass() + "'", e);
 		}		
 	}
 		
 	private void addHttpService(String path, HttpService service, String methods, String pattern, Controller mapping) throws ApplicationException {
 		if(executableControllers.containsKey(path)) {
 			throw new ApplicationException("Another HTTP service is already bound to path '" + path + "'");
 		}
 		
 		if(path != null && !path.equals("")) {
 			executableControllers.put(path, service);
 			allowedControllerMethods.put(path, methods);
 		} else if(pattern != null && !pattern.equals("")) {
 			validateFilterMethods(service.getClass().getName(), methods);
 			filterControllers.add(new HttpControllerFilter(service, mapping));
 		}
 	}
 
 	public Object removeController(Object controller) {
 		Controller mapping = controller.getClass().getAnnotation(Controller.class);
 		
 		if(mapping != null) {
 			String path = mapping.path();
 			
 			if(path != null && !path.equals("")) {
 				return removeController(path);
 			} else {
 				if(addedControllers.remove(controller)) {
 					return controller;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public Object removeController(String path) {
 		HttpService service = executableControllers.remove(path);
 		
 		if(service != null) {
 			if(service instanceof ControllerChain) {
 				HttpService[] chainedServices = ((ControllerChain)service).getServices();
 				
 				for(int i = chainedServices.length - 1; i > 0; i--) {
 					if(chainedServices[i] instanceof ControllerService) {
 						ControllerService controllerService = (ControllerService)chainedServices[i];
 						Object controller = controllerService.getController();
 						addedControllers.remove(controller);						
 					}
 				}
 			} else {
 				ControllerService controllerService = (ControllerService)service;
 				Object controller = controllerService.getController();
 				addedControllers.remove(controller);
 			}
 			
 			allowedControllerMethods.remove(path);
 		}
 		
 		return service;
 	}
 
 	public List<Object> getControllers() {
 		int size = addedControllers.size();
 		ArrayList<Object> outControllers = new ArrayList<Object>(size);
 		outControllers.addAll(this.addedControllers);
 		return outControllers;
 	}
 	
 	public void addService(Object service) throws ApplicationException {
 		addService(service, null);
 	}
 	
 	void addService(Object service, File classFile) throws ApplicationException {
 		Class<?> serviceClazz = service.getClass();
 		Service mapping = service.getClass().getAnnotation(Service.class);
 		
 		if(mapping == null) {
 			throw new ApplicationException("Service class " + serviceClazz.getName() + " is not annotated with a service mapping");			
 		}
 		
 		String name = mapping.name();
 		ServiceRunner runner = new ServiceRunner(this.classLoader, classFile, service);
 		services.put(name, runner);
 		runner.start();
 	}
 		
 	public Object removeService(Object service) {
 		Class<?> serviceClazz = service.getClass();
 		Service mapping = serviceClazz.getAnnotation(Service.class);
 		
 		if(mapping == null) {
 			return null;
 		}
 		
 		String name = mapping.name();
 		return removeService(name);
 	}
 	
 	public Object removeService(String name) {
 		ServiceRunner runner = services.remove(name);
 		
 		if(runner != null) {
 			return runner.stop();
 		}
 		
 		return null;
 	}
 	
 	public Object getService(String name) {
 		ServiceRunner runner = services.get(name);
		
		if(runner != null) {
			return runner.getService();
		}
		
		return null;
 	}
 	
 	void publish() {
 		applyFilters();
 	}
 	
 	private void applyFilters() {
 		for(HttpControllerFilter filter : this.filterControllers) {
 			Set<String> paths = executableControllers.keySet();
 			
 			for(String path : paths) {
 				applyFilter(filter, path);
 			}
 		}		
 	}
 	
 	private void applyFilter(HttpControllerFilter filter, String path) {
 		if(path.matches(filter.getMapping().pattern())) {
 			HttpService service = executableControllers.get(path);
 			
 			if(service instanceof ControllerChain) {
 				ControllerChain chain = (ControllerChain)service;
 				chain.addServiceFirst(filter.getService());
 			} else {
 				ControllerChain chain = new ControllerChain();
 				chain.addServiceLast(filter.getService());
 				chain.addServiceLast(service);
 				executableControllers.put(path, chain);
 			}
 		}		
 	}
 	
 	void unpublish() {
 		if(isWar()) {
 			cleanup(this.baseDir);
 		}
 		
 		for(ServiceRunner runner : services.values()) {
 			runner.stop();
 		}
 	}
 	
 	private void cleanup(File dir) {
 		File[] files = dir.listFiles();
 		
 		for(File file : files) {
 			if(file.isDirectory()) {
 				cleanup(file);
 			} else if(file.isFile()) {
 				file.delete();
 			}
 		}
 		
 		dir.delete();
 	}
 	
 	HttpServiceResult execute(String localPath, HttpRequest request, HttpResponse response) throws HttpException, IOException {
 		HttpService httpService = executableControllers.get(localPath);
 		HttpMethod method = request.getMethod();
 		
 		if(httpService != null && !method.equals(HttpMethod.OPTIONS)) {
 			try {
 				return httpService.invoke(request, response);
 			} catch(HttpControllerRemovedException e) {
 				executableControllers.remove(localPath);
 				throw new HttpException(e.getStatus(), e.getMessage(), e.getCause());
 			}
 		}
 		
 		if(httpService != null && method.equals(HttpMethod.OPTIONS)) {
 			String allowed = allowedControllerMethods.get(localPath);
 			response.setStatus(HttpStatus.OK);
 			response.setContentLength(0);
 			response.addHeader("Allow", allowed);
 			return HttpServiceResult.DONE;
 		}
 		
 		if(localPath.endsWith(".xsp") && !isMemory()) {
 			return compileAndExecuteHttpService(localPath, request, response);
 		}
 		
 		if(isUnpacked() && !staticContentExists(localPath)) {
 			if(findController(localPath)) {
 				execute(localPath, request, response);
 			} else if(localPath.equals("/favicon.ico")) {
 				sendEmptyFavicon(response);
 			} else {
 				throw new HttpException(HttpStatus.NOT_FOUND, "/" + this.name + localPath);	
 			}
 		} else {
 			executeStaticContent(request, response, localPath);
 		}
 		
 		return HttpServiceResult.DONE;		
 	}
 	
 	static void sendEmptyFavicon(HttpResponse response) throws IOException {
 		response.setStatus(HttpStatus.OK);
 		response.setContentType("image/vnd.microsoft.icon");
 		response.setContentLength(2);
 		long oneHourFromNow = System.currentTimeMillis() + 3600000L; 
 		response.addHeader("Expires", Header.formatDate(new Date(oneHourFromNow)));
 		OutputStream out = response.getOutputStream();
 		out.write(FAVICON_EMPTY);
 	}
 	
 	private boolean findController(String localPath) {
 		List<String> files = new ArrayList<String>();
 		File classesBaseDir = new File(this.baseDir, "WEB-INF/classes");
 		createFileList(classesBaseDir, classesBaseDir, files);
 		
 		for(String file : files) {
 			
 			if(file.endsWith(".class")) {
 				String className = file.substring(1, file.length() - 6).replace('/', '.');
 				
 				try {
 					Class<?> clazz = classLoader.loadClass(className);
 					Controller mapping = clazz.getAnnotation(Controller.class);
 					
 					if(mapping != null) {
 						String path = mapping.path();
 						
 						if(localPath.equals(path)) {
 							Object controller = clazz.newInstance();
 							File classFile = new File(classesBaseDir, file);
 							addController(controller, classFile, true);
 							return true;
 						}
 						
 					}
 				} catch(ClassNotFoundException e) {
 				} catch(Exception e) {}
 			}
 		}
 		
 		return false;
 	}
 	
 	private void createFileList(File baseDir, File dir, List<String> files) {
 		if(!dir.exists()) {
 			return;
 		}
 		
 		File[] subFiles = dir.listFiles();
 		
 		for(File subFile : subFiles) {
 			String subFileName = subFile.getAbsolutePath();
 			String baseDirName = baseDir.getAbsolutePath();
 			files.add(subFileName.substring(baseDirName.length()));
 			
 			if(subFile.isDirectory()) {
 				createFileList(baseDir, subFile, files);
 			}
 		}
 	}
 	
 	private HttpServiceResult compileAndExecuteHttpService(String localPath, HttpRequest request, HttpResponse response) throws IOException, HttpException {
 		try {
 			XspCompiler compiler = new XspCompiler(this.classLoader);
 			File webInfDir = new File(this.baseDir, "WEB-INF");
 			File xspFile = new File(webInfDir, localPath);
 			
 			if(!xspFile.exists()) {
 				throw new HttpException(HttpStatus.NOT_FOUND, localPath);
 			}
 			
 			File classesDir = new File(this.baseDir, "WEB-INF/classes");
 			
 			if(!classesDir.exists()) {
 				throw new HttpException(HttpStatus.NOT_FOUND, localPath);			
 			}
 			
 			XspService service = compiler.compileService(webInfDir.getAbsolutePath(), xspFile.getAbsolutePath(), classesDir.getPath());
 			
 			if(service == null) {
 				throw new HttpException(HttpStatus.NOT_FOUND, localPath);
 			}
 			
 			HttpService compilableService = new CompilableXspService(this.classLoader, service, webInfDir, xspFile, classesDir);
 			executableControllers.put(localPath, compilableService);
 			return service.invoke(request, response);
 		} catch(XspException e) {
 			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, "Compilation failed", e);
 		}
 	}
 	
 	private void executeStaticContent(HttpRequest request, HttpResponse response, String localPath) throws IOException, HttpException {
 		File contentFile = new File(this.baseDir, localPath);
 		
 		if(!contentFile.exists()) {
 			throw new HttpException(HttpStatus.NOT_FOUND, "/" + this.name + localPath);			
 		}
 		
 		HttpMethod method = request.getMethod();
 		
 		if(!method.isSupportedByStaticContent()) {
 			throw new HttpException(HttpStatus.METHOD_NOT_ALLOWED, "method not allowed " + method);
 		}
 		
 		if(method.equals(HttpMethod.OPTIONS)) {
 			executeStaticContentOptions(response);
 			return;
 		}
 		
 		StaticContent content = new StaticContent(this.baseDir, localPath);
 		content.execute(request, response);
 	}
 	
 	private boolean staticContentExists(String localPath) {
 		File contentFile = new File(this.baseDir, localPath);
 		return contentFile.exists();
 	}
 	
 	private void executeStaticContentOptions(HttpResponse response) {
 		response.setStatus(HttpStatus.OK);
 		response.setContentLength(0);
 		response.addHeader("Allow", "GET, HEAD, OPTIONS");
 	}
 	
 	private void validateFilterMethods(String serviceName, String methodSpec) throws ApplicationException {
 		HttpMethod[] expectedMethods = { HttpMethod.HEAD, HttpMethod.GET, HttpMethod.POST, 
 				HttpMethod.PUT, HttpMethod.DELETE };
 		
 		for(HttpMethod method : expectedMethods) {
 			if(methodSpec.indexOf(method.toString()) == -1) {
 				throw new ApplicationException("Filters " + serviceName + " does not support method " + 
 						method + ", filters must support all methods");
 			}
 		}
 	}
 	
 	private class HttpControllerFilter implements Comparable<HttpControllerFilter> {
 		
 		private HttpService service;
 		
 		private Controller mapping;
 		
 		private HttpControllerFilter(HttpService service, Controller mapping) {
 			this.service = service;
 			this.mapping = mapping;
 		}
 		
 		HttpService getService() {
 			return this.service;
 		}
 		
 		Controller getMapping() {
 			return this.mapping;
 		}
 		
 		public int hashCode() {
 			String id = mapping.pattern() + mapping.index();
 			return id.hashCode();
 		}
 		
 		public boolean equals(Object o) {
 			if(o instanceof HttpControllerFilter) {
 				HttpControllerFilter other = (HttpControllerFilter)o;
 				Controller otherMapping = other.getMapping();
 				return otherMapping.pattern().equals(mapping.pattern()) && otherMapping.index() == mapping.index();
 			}
 			
 			return super.equals(o);
 		}
 		
 		public int compareTo(HttpControllerFilter filter) {
 			Controller filterMapping = filter.getMapping();
 			
 			if(mapping.index() == filterMapping.index()) {
 				return mapping.pattern().compareTo(filterMapping.pattern());
 			}
 			
 			return mapping.index() - filterMapping.index();
 		}
 	}	
 }
