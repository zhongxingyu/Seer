 package wicket.contrib.scriptaculous.examples;
 
 import wicket.ISessionFactory;
import wicket.Request;
 import wicket.Session;
 import wicket.protocol.http.WebApplication;
 
 /**
  * 
  */
 public class ScriptaculousExamplesApplication extends WebApplication {
 
     protected void init() {
         super.init();
         configure("development");
         getResourceSettings().setThrowExceptionOnMissingResource(false);
         getMarkupSettings().setAutomaticLinking(true);
     }
     
     /**
      * @return class
      */
     public Class getHomePage()
     {
     	return ScriptaculousExamplesHomePage.class;
     }
 
     protected ISessionFactory getSessionFactory()
     {
     	return new ISessionFactory()
     	{
			public Session newSession(Request request)
 			{
 				return new ScriptaculousExamplesSession(ScriptaculousExamplesApplication.this);
 			}
     	};
     }
 }
