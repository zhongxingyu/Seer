 package de.uniluebeck.itm.servicepublisher.cxf;
 
 import com.google.common.util.concurrent.AbstractService;
 import de.uniluebeck.itm.servicepublisher.ServicePublisherService;
 import org.apache.jasper.servlet.JspServlet;
 import org.eclipse.jetty.server.session.SessionHandler;
 import org.eclipse.jetty.servlet.DefaultServlet;
 import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.net.URI;
 
 public class ServicePublisherServletService extends AbstractService implements ServicePublisherService {
 
 	private static final Logger log = LoggerFactory.getLogger(ServicePublisherServletService.class);
 
 	private final ServicePublisherImpl servicePublisher;
 
 	private final String contextPath;
 
 	private final String resourceBase;
 
 	private ServletContextHandler contextHandler;
 
 	public ServicePublisherServletService(final ServicePublisherImpl servicePublisher,
 										  final String contextPath,
 										  final String resourceBase) {
 		this.servicePublisher = servicePublisher;
 		this.contextPath = contextPath;
 		this.resourceBase = resourceBase;
 	}
 
 	@Override
 	protected void doStart() {
 		try {
 
			// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=364936
			Resource.setDefaultUseCaches(false);

 			contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
 			contextHandler.setSessionHandler(new SessionHandler());
 			contextHandler.setContextPath(contextPath);
 			contextHandler.setResourceBase(resourceBase);
 			contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
 			contextHandler.addServlet(DefaultServlet.class, "/");
 			contextHandler.addServlet(JspServlet.class, "*.jsp").setInitParameter(
 					"classpath",
 					contextHandler.getClassPath()
 			);
 			servicePublisher.addHandler(contextHandler);
 			contextHandler.start();
 
 			log.info("Published servlet service under {} with resource base: {}", contextPath, resourceBase);
 
 			notifyStarted();
 
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	protected void doStop() {
 		try {
 
 			if ("/".equals(contextPath)) {
 				log.warn("Not possible to remove paths from resource collection currently.");
 			} else {
 				servicePublisher.removeHandler(contextHandler);
 			}
 
 			notifyStopped();
 
 		} catch (Exception e) {
 			notifyFailed(e);
 		}
 	}
 
 	@Override
 	public URI getURI() {
 		return URI.create(servicePublisher.getAddress(contextPath));
 	}
 }
