 package org.sukrupa.platform.server;
 
 import org.apache.log4j.*;
 import org.eclipse.jetty.http.security.*;
 import org.eclipse.jetty.security.*;
 import org.eclipse.jetty.security.authentication.*;
 import org.eclipse.jetty.server.*;
 import org.eclipse.jetty.server.handler.*;
 import org.eclipse.jetty.servlet.*;
 import org.springframework.beans.factory.annotation.*;
 import org.springframework.stereotype.*;
 import org.sukrupa.platform.web.*;
 
 import java.io.*;
 import java.lang.management.*;
 
 import static java.lang.String.*;
 import static java.util.Arrays.asList;
 
 @Component
 public class WebServer {
 
     private Logger LOG = Logger.getLogger(WebServer.class);
 
     private Server server;
     private final String webRoot;
     private final String contextPath;
     private final FrontController frontController;
     private boolean authenticate;
 
     @Autowired
     public WebServer(@Value("${web.root.dir}") String webRoot,
                      @Value("${web.http.port}") int httpPort,
                      @Value("${web.context.path}") String contextPath,
                      @Value("${web.server.realm.file}") String webServerRealmFile,
                      @Value("${web.server.authenticate}") boolean authenticate,
                      FrontController frontController) throws IOException {
 
 
         this.webRoot = webRoot;
         this.contextPath = contextPath;
         this.frontController = frontController;
         this.authenticate = authenticate;
 
         server = new Server(httpPort);
         HashLoginService hashLoginService = new HashLoginService("SukrupaSchoolAdmin", webServerRealmFile);
         server.addBean(hashLoginService);
         server.setHandler(handlers());
     }
 
     public void start() {
         try {
             LOG.info(format("Starting Web Server (web root:%s)...", webRoot));
             server.start();
             writeProcessIdToFile();
             System.out.println("WebServer Started.");
             server.join();
         } catch (Exception e) {
             throw new WebServerStartupException(e);
         }
     }
 
     private static void writeProcessIdToFile() {
         FileOutputStream out = null;
         try {
             File processIdFile = new File(System.getProperty("user.home"), ".webserver.pid");
             if (processIdFile.exists()) {
                 processIdFile.delete();
             }
             processIdFile.deleteOnExit();
             processIdFile.createNewFile();
 
             out = new FileOutputStream(processIdFile);
 
             String processName = ManagementFactory.getRuntimeMXBean().getName();
             if (!processName.contains("@")) {
                 throw new RuntimeException(format("Process name [%s] is not parsable for the processId!", processName));
             }
 
             String[] parts = processName.split("@");
             String processId = parts[0];
             String hostName = parts[1];
 
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
             writer.println(processId);
             writer.flush();
 
             System.out.println(format("Starting server with PID [%s] on [%s]", processId, hostName));
             System.out.println(format("Process Id file is at [%s]", processIdFile.getAbsolutePath()));
 
         } catch (IOException e) {
             throw new RuntimeException("Could not write process Id file ~/.webserver.pid]", e);
         } finally {
             closeQuietly(out);
         }
     }
 
     private static void closeQuietly(OutputStream out) {
         if (out == null) {
             return;
         }
 
         try {
             out.close();
         } catch (IOException e) {
             throw new RuntimeException("Could not close stream (See Cause)", e);
         }
     }
 
     private HandlerList handlers() {
         HandlerList handlers = new HandlerList();
         handlers.setHandlers(new Handler[]{rootHandler(), resourceHandler(), servletHandler()});
         return handlers;
     }
 
     private Handler rootHandler() {
         return new RedirectRootHandler("/students");
     }
 
     private ResourceHandler resourceHandler() {
         ResourceHandler resourceHandler = new ResourceHandler();
         resourceHandler.setResourceBase(webRoot);
         return resourceHandler;
     }
 
     private ServletContextHandler servletHandler() {
         ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 
         addAuthentication(servletHandler);
 
         servletHandler.setContextPath(contextPath);
         servletHandler.setResourceBase(webRoot);
         ErrorHandler errorHandler = new ErrorHandler();
         errorHandler.setServer(server);
         servletHandler.setErrorHandler(errorHandler);
         servletHandler.addServlet(new ServletHolder(frontController), "/*");
         return servletHandler;
     }
 
     private void addAuthentication(ServletContextHandler servletHandler) {
         if (!authenticate) return;
 
         ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
         securityHandler.setLoginService(server.getBean(HashLoginService.class));
 
         FormAuthenticator authenticator = new FormAuthenticator("/authentication/login?success=true", "/authentication/login?success=false", true);
 
         securityHandler.setAuthenticator(authenticator);
 
         Constraint constraint = new Constraint();
         constraint.setName(Constraint.__FORM_AUTH);
         constraint.setRoles(new String[]{"SukrupaSchoolAdmin"});
         constraint.setAuthenticate(true);
 
         ConstraintMapping events = mapConstraintTo(constraint, "/events/*");
         ConstraintMapping students = mapConstraintTo(constraint, "/students/*");
         ConstraintMapping admin = mapConstraintTo(constraint, "/admin/*");
         ConstraintMapping healthCheck = mapConstraintTo(constraint, "/healthCheck");
        ConstraintMapping bigNeeds = mapConstraintTo(constraint, "/bigneeds/*");
        ConstraintMapping smallNeeds = mapConstraintTo(constraint, "/smallneeds/*");
 
        securityHandler.setConstraintMappings(asList(events, students, admin, healthCheck, bigNeeds, smallNeeds));
 
         servletHandler.setSecurityHandler(securityHandler);
     }
 
     private static ConstraintMapping mapConstraintTo(Constraint constraint, String path) {
         ConstraintMapping cm = new ConstraintMapping();
         cm.setPathSpec(path);
         cm.setConstraint(constraint);
         return cm;
     }
 
 }
