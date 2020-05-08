 package org.wicketstuff.dojo.examples;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.ISessionFactory;
 import org.apache.wicket.Request;
 import org.apache.wicket.Response;
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.wicketstuff.dojo.skin.manager.SkinManager;
 import org.wicketstuff.dojo.skin.windows.WindowsDojoSkin;
 
 /**
  * Runs the ExampleApplication when invoked from command line.
  */
 public class ExampleApplication extends WebApplication {
	/** Logging */
	private static final Log log = LogFactory.getLog(ExampleApplication.class);

 	/**
 	 * Constructor
 	 */
 	public ExampleApplication() {
 			SkinManager.getInstance().setupSkin(new WindowsDojoSkin());
 	}
 
 	/**
 	 * @see wicket.Application#getHomePage()
 	 */
 	public Class getHomePage() {
 		return Index.class;
 	}
 
 	/**
 	 * @see wicket.protocol.http.WebApplication#getSessionFactory()
 	 */
 	public ISessionFactory getSessionFactory() {
 		return new ISessionFactory() {
 			public Session newSession(Request request, Response response) {
 				return new ExampleSession(ExampleApplication.this, request, response);
 			}
 		};
 	}
 }
