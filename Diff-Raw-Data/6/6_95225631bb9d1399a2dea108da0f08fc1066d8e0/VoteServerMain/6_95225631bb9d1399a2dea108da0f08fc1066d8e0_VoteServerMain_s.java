 package ee.ut.cs.veebirakendus2013.kurivaim.jettytest;
 
 import java.io.File;
 import java.util.EnumSet;
 
 import javax.servlet.DispatcherType;
 
 import org.eclipse.jetty.http.HttpVersion;
 import org.eclipse.jetty.server.HttpConfiguration;
 import org.eclipse.jetty.server.HttpConnectionFactory;
 import org.eclipse.jetty.server.SecureRequestCustomizer;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.ServerConnector;
 import org.eclipse.jetty.server.SessionManager;
 import org.eclipse.jetty.server.SslConnectionFactory;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.handler.ResourceHandler;
 import org.eclipse.jetty.servlet.FilterHolder;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
 import org.eclipse.jetty.servlets.gzip.GzipHandler;
 import org.eclipse.jetty.util.ssl.SslContextFactory;
 
 import ee.ut.cs.veebirakendus2013.kurivaim.jettytest.mysql.MysqlConnectionHandler;
 import ee.ut.cs.veebirakendus2013.kurivaim.jettytest.servlets.IdCardServlet;
 import ee.ut.cs.veebirakendus2013.kurivaim.jettytest.servlets.MultiPartFilterWrapper;
 import ee.ut.cs.veebirakendus2013.kurivaim.jettytest.servlets.ResourceHandlerWrapper;
 import ee.ut.cs.veebirakendus2013.kurivaim.jettytest.servlets.VoteServlet;
 
 public class VoteServerMain {
 	public static void main(String[] args) throws Exception {
 		MysqlConnectionHandler sqlHandler = new MysqlConnectionHandler();
 		
 		Server mainServer = createMainServer(sqlHandler);
 		Server idCheckServer = createIdCheckServer(sqlHandler, (SessionManager)mainServer.getAttribute("sessionManager"));
 		
 		mainServer.join();
 		idCheckServer.join();
 		
 		sqlHandler.disconnect();
 	}
 	
 	private static Server createMainServer(MysqlConnectionHandler sqlHandler) throws Exception {
 		Server server = new Server(8080);
 		
 		FilterHolder filterHolder = new FilterHolder(new MultiPartFilterWrapper());
 		filterHolder.setInitParameter("deleteFiles", "true");
 		filterHolder.setInitParameter("maxFileSize", "262144");
 		filterHolder.setInitParameter("maxRequestSize", "524288");
 		
		FilterHolder filterGzip = new FilterHolder(new GzipFilter());
		
 		ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		contextHandler.setContextPath("/dyn");
 		contextHandler.addServlet(new ServletHolder(new VoteServlet(sqlHandler)), "/*");
 		contextHandler.setAttribute("javax.servlet.context.tempdir", new File("../temp"));
 		contextHandler.addFilter(filterHolder, "/photo", EnumSet.of(DispatcherType.REQUEST));
		contextHandler.addFilter(filterGzip, "/*", EnumSet.of(DispatcherType.REQUEST));
 		
 		server.setAttribute("sessionManager", contextHandler.getSessionHandler().getSessionManager());
 		
 		ResourceHandler resourceHandler = new ResourceHandlerWrapper();
 		resourceHandler.setResourceBase("../html/");
 		resourceHandler.setCacheControl("max-age=3153600, public");
 		
 		GzipHandler gzipHandler = new GzipHandler();
 		gzipHandler.setHandler(resourceHandler);
 		
 		HandlerList handlers = new HandlerList();
 		handlers.addHandler(contextHandler);
 		handlers.addHandler(gzipHandler);
 		
 		server.setHandler(handlers);
 		server.start();
 		
 		return server;
 	}
 	
 	//works with Jetty 9.0.0.M4, but not with 9.0.0.M5 or 9.0.0.R0 (current)
 	public static Server createIdCheckServer(MysqlConnectionHandler sqlHandler, SessionManager mainSessionManager) throws Exception {
 		Server server = new Server();
 		
 		SslContextFactory contextFactory = new SslContextFactoryWrapper();
 		contextFactory.setKeyStorePath("../certs/serverCertificate.jks");
 		contextFactory.setKeyStorePassword("testpass");
 		contextFactory.setTrustStorePath("../certs/idCardCertificates.jks");
 		contextFactory.setTrustStorePassword("certPass123");
 		contextFactory.setNeedClientAuth(true);
 		contextFactory.setEnableOCSP(true);
 		contextFactory.setOcspResponderURL("http://ocsp.sk.ee");
 		
 		SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(contextFactory, HttpVersion.HTTP_1_1.toString());
 		
 		HttpConfiguration config = new HttpConfiguration();
 		config.setSecureScheme("https");
 		config.setSecurePort(8443);
 		
 		HttpConfiguration sslConfiguration = new HttpConfiguration(config);
 		sslConfiguration.addCustomizer(new SecureRequestCustomizer());
 		HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(sslConfiguration);
 		
 		ServerConnector connector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
 		connector.setPort(8443);
 		server.addConnector(connector);
 		
 		ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		contextHandler.setContextPath("/");
 		contextHandler.addServlet(new ServletHolder(new IdCardServlet(sqlHandler, mainSessionManager)), "/*");
 		
 		HandlerList handlers = new HandlerList();
 		handlers.addHandler(contextHandler);
 		
 		server.setHandler(handlers);
 		server.start();
 		
 		return server;
 	}
 }
