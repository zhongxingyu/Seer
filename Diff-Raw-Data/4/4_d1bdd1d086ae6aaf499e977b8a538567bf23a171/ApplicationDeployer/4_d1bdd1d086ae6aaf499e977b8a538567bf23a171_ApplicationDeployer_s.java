 package org.innobuilt.fincayra;
 
 import java.io.IOException;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.innobuilt.fincayra.persistence.FincayraRepositoryProvider;
 import org.mozilla.javascript.RhinoException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Servlet context listener that is responsible for {@link FincayraRepositoryFactory#initialize(javax.servlet.ServletContext)
  * initializing} the {@link FincayraRepositoryFactory repository factory}.
  * <p>
  * This class is not thread safe, but in practice this does not matter as the servlet container must ensure that only a single
  * instance of this exists per web context and that it is only called in a single-threaded manner.
  * </p>
  * <p>
  * This class is not thread-safe.
  * </p>
  * 
  * @see FincayraRepositoryFactory
  */
 public class ApplicationDeployer implements ServletContextListener {
 
     private FincayraApplication app;
 	private String jsDir = "fincayra-lib";
 	private String pageDir = "application";
 	private static Logger LOGGER = LoggerFactory.getLogger(ApplicationDeployer.class);
 	/**
      * Alerts the repository factory that the web application is shutting down
      * 
      * @param event the servlet context event
      * @see FincayraRepositoryFactory#shutdown()
      * @see FincayraRepositoryProvider#shutdown()
      */
     @Override
     public void contextDestroyed( ServletContextEvent event ) {
         FincayraRepositoryFactory.shutdown();
         try {
 			app.getMergeEngine().destroy();
 		} catch (RhinoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
     /**
      * Initializes the repository factory
      * 
      * @param event the servlet context event
      * @see FincayraRepositoryFactory#initialize(javax.servlet.ServletContext)
      */
     @Override
     public void contextInitialized( ServletContextEvent event ) {
 		LOGGER.info("Initializing Fincayra repository.");
         FincayraRepositoryFactory.initialize(event.getServletContext());
 		this.app = FincayraApplication.get();
 		app.setJsDir(jsDir);
 		app.setPageDir(pageDir);
 		
 		app.setRootDir(event.getServletContext().getRealPath("."));
 		app.getMergeEngine().setPageDir(event.getServletContext().getRealPath(app.getPageDir()));
 		app.getMergeEngine().setJsDir(event.getServletContext().getRealPath(app.getJsDir()));
 		
 		LOGGER.info("Initializing MergeEngine");
 		try {
 			app.getMergeEngine().init(true);
 		} catch (Exception e) {
			LOGGER.error("Caught excpetion on fincayra application deployment.", e);
			LOGGER.error("Fincayra is shutting down!");	
			System.exit(1);
 		}
         
     }
 }
 
