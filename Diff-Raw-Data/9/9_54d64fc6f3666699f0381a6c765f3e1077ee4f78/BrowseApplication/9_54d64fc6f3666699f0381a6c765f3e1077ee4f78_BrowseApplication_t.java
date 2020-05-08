 package uk.ac.ox.oucs.oxam;
 
 import org.apache.wicket.Page;
 import org.apache.wicket.Request;
 import org.apache.wicket.RequestCycle;
 import org.apache.wicket.Response;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.protocol.http.WebRequestCycle;
 import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 
 import uk.ac.ox.oucs.oxam.pages.AdvancedSearchPage;
 import uk.ac.ox.oucs.oxam.pages.SimpleSearchPage;
 
 /**
  * Main application class for our app
  * 
  * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
  *
  */
 public class BrowseApplication extends WebApplication {
    
 	/**
 	 * Configure your app here
 	 */
 	@Override
 	protected void init() {
 		
 		//Configure for Spring injection
 		addComponentInstantiationListener(new SpringComponentInjector(this));
 		
 		//Don't throw an exception if we are missing a property, just fallback
 		getResourceSettings().setThrowExceptionOnMissingResource(false);
 		
 		//Remove the wicket specific tags from the generated markup
 		getMarkupSettings().setStripWicketTags(true);
 		
 		//Don't add any extra tags around a disabled link (default is <em></em>)
 		getMarkupSettings().setDefaultBeforeDisabledLink(null);
 		getMarkupSettings().setDefaultAfterDisabledLink(null);
 				
 		// On Wicket session timeout, redirect to main page
 		getApplicationSettings().setPageExpiredErrorPage(SimpleSearchPage.class);
 		getApplicationSettings().setAccessDeniedPage(SimpleSearchPage.class);
 		
 		//to put this app into deployment mode, see web.xml
		// We don't use mountBookmarkablePage as it's URL coding strategy doesn't cope
		// with space in the URL.
		mount(new QueryStringUrlCodingStrategy("/search", SimpleSearchPage.class));
		mount(new QueryStringUrlCodingStrategy("/advanced", AdvancedSearchPage.class));
 
 	}
 	
 	/**
 	 *  Throw RuntimeExceptions so they are caught by the Sakai ErrorReportHandler(non-Javadoc)
 	 *  
 	 * @see org.apache.wicket.protocol.http.WebApplication#newRequestCycle(org.apache.wicket.Request, org.apache.wicket.Response)
 	 */
 	@Override
 	public RequestCycle newRequestCycle(Request request, Response response) {
 		return new WebRequestCycle(this, (WebRequest)request, (WebResponse)response) {
 			@Override
 			public Page onRuntimeException(Page page, RuntimeException e) {
 				throw e;
 			}
 		};
 	}
 	
 	/**
 	 * The main page for our app
 	 * 
 	 * @see org.apache.wicket.Application#getHomePage()
 	 */
 	public Class<? extends Page> getHomePage() {
 		return SimpleSearchPage.class;
 	}
 	
 	
 	/**
      * Constructor
      */
 	public BrowseApplication()
 	{
 	}
 	
 	
 
 }
