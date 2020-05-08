 package com.github.steingrd.bloominghollows;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServlet;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 import org.springframework.beans.BeansException;
 import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
 import org.springframework.web.context.ContextLoaderListener;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 
 import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
 
 public class App extends HttpServlet {
 	
 	public static void main(String[] args) throws Exception {
 		
 		Server server = new Server(port());
 		
 		WebApplicationContext applicationContext = createSpringApplicationContext("ImmenseBastion", AppConfiguration.class);
 		
 		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 		context.addServlet(defaultServlet(), "/*");
 		context.addServlet(jerseyServlet(), "/rest/*");
 		context.addFilter(OpenSessionInViewFilter.class, "/rest/*", null);
 		context.addEventListener(createSpringContextLoader(applicationContext));
 		
 		server.setHandler(context);
 		
 		// let's start this thing now...
 		server.start();
 		server.join();
 		
 	}
 
 	private static ContextLoaderListener createSpringContextLoader(final WebApplicationContext applicationContext) {
 		return new ContextLoaderListener() {
 			@Override
 			protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
 				return applicationContext;
 			}
 		};
 	}
 
	private static WebApplicationContext createSpringApplicationContext(final String displayName, @SuppressWarnings("rawtypes") final Class... contextConfigLocaition) {
 		return new AnnotationConfigWebApplicationContext() {
 			{
 				setDisplayName(displayName);
				register(contextConfigLocaition);
 				super.refresh();
 			}
 			
 			@Override
 			public void refresh() throws BeansException, IllegalStateException {
 				// avoid re-initializing the context once the servlet context has loaded
 			}
 		};
 	}
 	
 	private static ServletHolder defaultServlet() {
 		String resourceBase = App.class.getClassLoader().getResource("web/").toExternalForm();
 		
 		ServletHolder holder = new ServletHolder(DefaultServlet.class);
 		holder.setInitParameter("resourceBase", resourceBase);
 		holder.setInitParameter("dirAllowed", "true");
 		holder.setInitParameter("pathInfoOnly", "true");
 		
 		return holder;
 	}
 
 	private static ServletHolder jerseyServlet() {
 		ServletHolder holder = new ServletHolder(new SpringServlet());
 		holder.setInitParameter(
 				"com.sun.jersey.config.property.packages", 
 				"org.codehaus.jackson.jaxrs");
 		return holder;
 	}
 
 	private static Integer port() {
 		String port = System.getenv("PORT");
 		if (port == null) {
 			port = "9090";
 		}
 		return Integer.valueOf(port);
 	}
 }
