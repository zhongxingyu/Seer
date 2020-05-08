 package com.angelini.fly;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Fly {
 	
 	private int port;
 	private FlyDB db;
 	private boolean requireAuth = false;
 	private ServletContextHandler context;
 	private Authentication auth;
 	
 	private String html;
 	private Map<String, String> components;
 	private Map<String, Class<?>> servlets;
 	
 	private static Logger log = LoggerFactory.getLogger(Fly.class);
 	
 	public Fly(int port, FlyDB db, String html) throws IOException {
 		this.port = port;
 		this.db = db;
 		this.html = html;
 		
 		this.auth = new Authentication();
 		this.servlets = new HashMap<String, Class<?>>();
 		this.components = new HashMap<String, String>();
 		
 		context = new ServletContextHandler();
 	    context.setContextPath("/");
 	    
 	    this.addStaticFolder("/htdocs", "/js/*");
 		this.addStaticFolder("/htdocs", "/images/*");
 		this.addStaticFolder("/htdocs", "/img/*");
 		this.addStaticFolder("/htdocs", "/css/*");
 	}
 	
 	public void addStaticFolder(String base, String content) {
 		context.addServlet(new ServletHolder(new ClasspathFilesServlet(base)), content);
 	}
 	
 	public void addServlet(Class<?> servlet, String base) throws RouterException {
 		servlets.put(base, servlet);
 	}
 	
 	public void addComponent(String name, String path) throws IOException {
 		components.put(name, Utils.readFile(path));
 	}
 	
 	public void requireAuth(Class<?> authClass) throws IOException {
 		if (requireAuth) {
 			throw new RuntimeException("Can only require authentication once");
 		}
 		
 		requireAuth = true;
 		context.addServlet(new ServletHolder(new AuthServlet(db, auth, authClass)), "/auth/*");
 	}
 
 	public void start() throws Exception {
 		if (!requireAuth) {
 			auth = null;
 		}
 		
 		for (Map.Entry<String, Class<?>> servlet : servlets.entrySet()) {
 			context.addServlet(new ServletHolder(new FlyRouter(db, servlet.getValue(), auth)), servlet.getKey());
 		}
 		
 		LayoutTemplater layout = new LayoutTemplater(html, components, auth);
 		
 		context.addServlet(new ServletHolder(layout), "/*");
 		
 		Server server = new Server(port);
 		server.setHandler(context);
 		
 		log.info("Server starting on port {}", port);
 		server.start();
 		server.join();
 	}
 	    
 }
