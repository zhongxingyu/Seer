 package org.zkoss.fiddler.executor.resources;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 
 import org.mortbay.resource.FileResource;
 import org.mortbay.resource.Resource;
 import org.zkoss.fiddler.executor.classloader.ByteClass;
 import org.zkoss.fiddler.executor.classloader.FiddleClass;
 import org.zkoss.fiddler.executor.classloader.FiddleClassUtil;
 import org.zkoss.fiddler.executor.exceptions.JavaSecurityException;
 import org.zkoss.fiddler.executor.resources.fetch.FetchResource;
 import org.zkoss.fiddler.executor.resources.fetch.FetchedToken;
 import org.zkoss.fiddler.executor.resources.fetch.FiddleResourceFetcher;
 import org.zkoss.fiddler.executor.server.Configs;
 import org.zkoss.fiddler.executor.server.SandboxWebappContext;
 
 @SuppressWarnings("restriction")
 public class FiddleWebappResource extends FiddleResourceBase {
	private static final long serialVersionUID = 3210954444083435699L;

 	private static final int TYPE_JAVA = 1;
 	
 	private File baseFile;
 
 	private SandboxWebappContext context;
 
 	private FiddleResourceFetcher fetcher;
 
 	private Pattern parser = Pattern.compile("/([0-9a-zA-Z]+)(/([0-9]+))?.*");
 
 	private HashMap<String, Resource> resourcePool = new HashMap<String, Resource>();
 	private FileResource base ;
 
 	public FiddleWebappResource(SandboxWebappContext webcontext, FiddleResourceFetcher fetcher ,File f) {
 		this.fetcher = fetcher;
 
 		context = webcontext;
 		try {
 			base = new FileResource(f.toURI().toURL());
 		} catch (MalformedURLException e1) {
 			if(Configs.isLogMode()) e1.printStackTrace();
 		} catch (IOException e1) {
 			if(Configs.isLogMode()) e1.printStackTrace();
 		} catch (URISyntaxException e1) {
 			if(Configs.isLogMode()) e1.printStackTrace();
 		}
 		
 		try {
 			baseFile = File.createTempFile("jtr", "hello");
 		} catch (IOException e) {
 
 		}
 	}
 
 	public boolean isDirectory() {
 		return true;
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public URL getURL() {
 		if (baseFile != null) {
 			try {
 				return baseFile.toURI().toURL();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public File getFile() throws IOException {
 		return baseFile;
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public String getName() {
 		return base.getName();
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public InputStream getInputStream() throws IOException {
 		return base.getInputStream();
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public OutputStream getOutputStream() throws IOException, SecurityException {
 		return base.getOutputStream();
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public boolean delete() throws SecurityException {
 		throw new UnsupportedOperationException("Unsupported");
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public boolean renameTo(Resource dest) throws SecurityException {
 		throw new UnsupportedOperationException("Unsupported");
 	}
 
 	/**
 	 * @throws UnsupportedOperationException
 	 */
 	public String[] list() {
 		throw new UnsupportedOperationException("Unsupported");
 	}
 	
 	
 	public Resource addPath(String path) throws IOException, MalformedURLException {
 		if (Configs.isLogMode())
 			System.out.println("fetching:" + path);
 
 		if (resourcePool.containsKey(path))
 			return resourcePool.get(path);
 		
 		if (path == null) {
 			return new EmptyResource();
 		} else if (path.endsWith(".gz") || path.indexOf("favicon.ico") != -1) {
 			return new EmptyResource();
 		} else if ("/WEB-INF/zk.xml".equals(path)) {
 			try {
 				return new FileResource(getClass().getClassLoader().getResource("zk.xml"));
 			} catch (URISyntaxException e) {
 				return new EmptyResource();
 			}
 		} else if ("WEB-INF/".equals(path)) {
 			return new FiddleWEBINFResourceHandler();
 		} else if ("/".equals(path)) {
 			return this;
 		} else if (path.startsWith(("/WEB-INF"))) {
 			return new EmptyResource();
 		} else if (path.matches("/([0-9a-zA-Z]+)(/[0-9]+)?.*")) {
 			handleResourceFetching(path);
 			// fetcher.
 		}
 		if (resourcePool.containsKey(path)) {
 			return resourcePool.get(path);
 		}
 		return base.addPath(path);
 	}
 
 	private void handleResourceFetching(String path) throws IOException {
 
 		Matcher match = parser.matcher(path);
 		if (match.find()) {
 
 			String token = match.group(1);
 			String ver = match.groupCount() > 1 ? match.group(2) : "";
 			ver = ver != null ? ver.replaceAll("/","") : null;
 
 			if (Configs.isLogMode()) {
 				System.out.println(token + ":" + ver);
 			}
 
 			FetchedToken ft = new FetchedToken(token, ver);
 
 			String key = "/" + ft.getToken() + "/" + ft.getVersion() ;
 			if (!fetcher.isFetched(ft)) {
 				List<FetchResource> resources = fetcher.fetch(ft);
 				try {
 					Resource r = fetcher.getTokenHolder(ft);
 					resourcePool.put(key +"/", r);
 				} catch (URISyntaxException e1) {
 					if (Configs.isLogMode())
 						e1.printStackTrace();
 				}
 				if (resources != null) {
 
 					try {
 						context.getClassLoader().addAllResourceClasses(compile(resources));
 					} catch (IllegalStateException e) {
 						if(Configs.isLogMode()){
 							System.err.println("Because your sample have java compile error so it can't show up ,\n"
 									+ e.getMessage());
 						}
 						for (FetchResource fr : resources) {
 							fr.setFileName("index.html");
 							fr.setContent("<pre>Because your sample have java compile error so it can't show up ,\n"
 									+ e.getMessage() +"</pre>");
 						}
 					}catch(JavaSecurityException e){
 						if(Configs.isLogMode()){
 							System.err.println("Because your sample violate our security rule so it can't show up ,\n"
 									+ e.getMessage());
 						}
 						for (FetchResource fr : resources) {
 							fr.setFileName("index.html");
 							fr.setContent("<pre>Because your sample violate our security rule so it can't show up ,\n"
 									+ e.getMessage() +"</pre>");
 						}
 					}
 
 					for (FetchResource fetchedResource : resources) {
 						if (Configs.isLogMode())
 							System.out.println("adding to pool:" + (key +"/" + fetchedResource.getFileName()));
 						fetchedResource.saveContent();
 						try {
 							resourcePool.put(key +"/" + fetchedResource.getFileName(), new FileResource(fetchedResource.getStoreURL()));
 						} catch (URISyntaxException e) {
 							if (Configs.isLogMode())
 								e.printStackTrace();
 						}
 					}
 
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @throws IllegalStateException
 	 *             when Compile Error
 	 * @param resources
 	 * @return
 	 */
	@SuppressWarnings("rawtypes")
 	public List<Class> compile(List<FetchResource> resources) {
 		List<Class> ret = new ArrayList<Class>();
 		List<FiddleClass> fiddleClass = new ArrayList<FiddleClass>();
 
 		for (FetchResource fr : resources) {
 			if (fr.getType() == TYPE_JAVA) {
 				fiddleClass.add(new FiddleClass(fr.getFileName(), fr.getContent()));
 				if (fr.getContent().indexOf("System.exit") != -1) {
 					throw new JavaSecurityException("ZK Fiddle Sandbox don't allow System.exit in your java class:\n"
 							+ fr.getContent());
 				} else if (fr.getContent().indexOf("getRuntime()") != -1) {
 					throw new JavaSecurityException("ZK fiddle don't allow you to run system command.");
 				}
 			}
 		}
 
 		if (fiddleClass.size() != 0) {
 			StringBuffer sw = new StringBuffer();
 
 			DiagnosticCollector<Diagnostic> diagnostics = new DiagnosticCollector<Diagnostic>();
 			List<ByteClass> classlist = FiddleClassUtil.compile(fiddleClass, null, diagnostics,context.getClassLoader() );
 
 			boolean error = false;
 			for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
 				if (Diagnostic.Kind.ERROR == diagnostic.getKind()) {
 					error = true;
 					sw.append(diagnostic.toString() + "\n\n");
 				}
 			}
 			if (error) {
 				throw new IllegalStateException("Compile Error:" + sw.toString());
 			}
 
 			/*
 			 * Note that one resource might mapping to multiple resource , so we
 			 * didn't record resource-class mapping.
 			 */
 			for (ByteClass bc : classlist) {
 				ret.add(bc.getCls());
 			}
 		}
 		return ret;
 	}
 
 }
