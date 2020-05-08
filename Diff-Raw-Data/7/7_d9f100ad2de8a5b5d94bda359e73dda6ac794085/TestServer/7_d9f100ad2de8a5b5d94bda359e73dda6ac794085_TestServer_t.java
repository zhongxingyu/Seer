 package li.rudin.rt.test.web;
 
 import li.rudin.rt.servlet.RTServlet;
 
 import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.ServerConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TestServer
 {
 	/*
 	 * Local logger
 	 */
 	private static final Logger logger = LoggerFactory.getLogger(TestServer.class);
 
	Server server = new Server(new QueuedThreadPool(30));
 
 	public TestServer(int port, String rtId)
 	{
 		logger.info("Starting test on port: {}", port);
 
         ServerConnector connector= new ServerConnector(server);
         connector.setPort(port);
        
         server.setConnectors(new Connector[]{connector});
        
 		ServletContextHandler handler = new ServletContextHandler();
 		server.setHandler(handler);
 		
 		ServletHolder holder = new ServletHolder(RTServlet.class);
 		holder.setInitParameter("id", rtId);
 		holder.setInitOrder(1);
 		holder.setAsyncSupported(true);
 		handler.addServlet(holder, "/rt");
 
 	}
 	
 	
 	public void start() throws Exception
 	{
 		server.start();
 	}
 	
 	
 	public void stop() throws Exception
 	{
 		server.stop();
 	}
 }
