 package controllers;
 
 import static util.Dsl.*;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.AppProps;
 import models.Bundle;
 import models.OutResult;
 import models.Repo;
 import play.Logger;
 import play.Play;
 import play.cache.Cache;
 import play.cache.CacheFor;
 import play.libs.IO;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.libs.WS.WSRequest;
 import play.mvc.Before;
 import play.mvc.Router;
 import bundle.BundleRegistry;
 
 import com.google.gson.JsonObject;
 
 /**
  * Application controller for generic infrastructure stuff, for 
  * example 'robots.txt', redirections and so-on
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class Main extends CommonController {
 
 	@Before
 	static void before() { 
 		injectImplicitVars(null);
 	}
 	
 	
 	/** 
 	 * render as static page 'index.html' as main application page if exists
 	 * in the public folder, otherwise redirects to the T-Coffee bundle index page 
 	 * 
 	 * see {@link Application#index()}
 	 */
 	public static void index() { 
 
 		/* 
 		 * try to load a static home page
 		 */
 		File indexPage = new File(Play.applicationPath, "public/index.html");
 		if( indexPage.exists() ) { 
 			response.contentType = "text/html";
 			renderText( IO.readContentAsString(indexPage) );
 		}
 		
 		/* try to load a cached version of T-Coffee home page */
 		String home = (String) Cache.get("tcoffee_index_page");
 		if( home != null && Cache.get("sysmsg")==null ) { 
 			response.contentType = "text/html";
 			renderText(home);
 		}
 
 		/* fallback on the dynamic index page */
 		redirect( Router.reverse("Application.index", params("bundle=tcoffee")).toString()  );
 	}
 	
 	public static void list() { 
 		List<Bundle> bundles = BundleRegistry.instance().getBundles();
 		render(bundles);
 	}
 	
 	public static void result(String rid) { 
     	final Repo repo = new Repo(rid,false);
 		final OutResult result = repo.hasResult() ? repo.getResult() : null; 
 
     	Map<String,Object> args = new HashMap<String, Object>(2);
     	args.put("bundle", result != null ? result.bundle : "tcoffee" );
     	args.put("rid", rid);
     	redirect( Router.reverse("Application.result", args).toString() );
     	
 	}
 	
 	
 	/**
 	 * Quick access link to PSI-coffee
 	 */
 	public static void psicoffee() { 
 		redirect( Router.reverse("Application.main", params("bundle=tcoffee", "name=psicoffee")).toString() );
 	}
 
 	/**
 	 * Quick access link to TM-coffee
 	 */
 	public static void tmcoffee() { 
 		redirect( Router.reverse("Application.main", params("bundle=tcoffee", "name=tmcoffee")).toString() );
 	}
 		
 	
 	/**
 	 * Quick link to R-coffee
 	 */
 	public static void rcoffee() { 
 		redirect( Router.reverse("Application.main", params("bundle=tcoffee", "name=rcoffee")).toString() );
 	}
 	
 	/**
 	 * Quick link to M-coffee
 	 */
 	public static void mcoffee() { 
 		redirect( Router.reverse("Application.main", params("bundle=tcoffee", "name=mcoffee")).toString() );
 	}
 	
 	/**
 	 * Quick link Expresso
 	 */
 	public static void expresso() { 
 		redirect( Router.reverse("Application.main", params("bundle=tcoffee", "name=expresso")).toString() );
 	}
 	
 	/**
 	 * Return the 'robots.txt' text file SEO optimization
 	 */
 	public static void robots() {
 		final String conf = "/conf/robots.txt";
 		File robots = Play.getFile(conf);
 
 		if( !robots.exists())  { 
 			Logger.error("Missing robots.txt. It should be placed in $PLAY/conf/robots.txt folder");
 			notFound();
 		}
 		
 		renderText(IO.readContentAsString(robots));
 	} 
 	
 
     /*
      * fake page used for tests purpose only   
      */
     public static void sandbox() {
     	render("Main/_sandbox.html");
     }
     
     
 	/**
 	 * Returns the bundle favicon 
 	 */
     @CacheFor("10d")
 	public static void favicon () { 
 		File icon = new File(Play.applicationPath,"conf/favicon.ico");
 		
 		if( !icon.exists() ) { 
 			Logger.warn("Missing favicon.ico file. It should be placed in conf path: '%s'", icon.getParent());
 			notFound("Favicon.ico not available");
 		}
 		
 		response.contentType = "image/x-icon";
 		renderBinary(icon);
 	}
 
     @CacheFor("10d")
 	public static void googleSiteVerification(String siteId) { 
 		String name = "google" + siteId + ".html";
 		File file = new File(Play.applicationPath,"conf/" + name);
 		
 		if( !file.exists() ) { 
 			Logger.warn("Missing %s file. It should be placed in conf path: '%s'", name, file.getParent());
 			notFound("Google site verification file does not exist");
 		}
 		
 		response.contentType = "text/html";
 		renderText(IO.readContentAsString(file));
 	}
     
     /**
      * Authenticate the specified assertion against the BrowserId server 
      * <p>
      * See https://github.com/mozilla/browserid/wiki/How-to-Use-BrowserID-on-Your-Site
      * 
      * @param assertion
      */
     public static void browseridSignin( String assertion ) {
     	
     	WSRequest wsrequest = WS.url("https://browserid.org/verify");
     	wsrequest.setParameter("assertion", assertion);
     	wsrequest.setParameter("audience", "http://"+AppProps.instance().getHostName());
     	HttpResponse wsresponse = wsrequest.post();
     	JsonObject obj = (JsonObject) wsresponse.getJson();
     	String trusted = obj.get("email").getAsString();
 
     	// store this email as in the session 
     	session.put("trusted_user", trusted);
     	response.setCookie("email", trusted, "30d"); // <-- keep congruent with the form email cookie 
     	
     	renderText("okay");
     } 
     
     /**
      * Remove the current authenticated user
      */
     public static void browseridSignout() {
     	session.remove("trusted_user");
     	response.removeCookie("email"); 	// <-- keep congruent with the form email cookie 
     	renderText("okay");
     } 
 	
 }
