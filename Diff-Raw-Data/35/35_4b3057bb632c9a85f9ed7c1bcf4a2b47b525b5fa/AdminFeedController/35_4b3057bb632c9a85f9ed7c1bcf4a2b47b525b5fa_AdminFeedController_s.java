 package nz.co.searchwellington.controllers.admin;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.feeds.FeedReaderRunner;
 import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.urls.UrlBuilder;
 
 import org.apache.log4j.Logger;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import com.sun.syndication.io.FeedException;
 
 // TODO move to admin.
public class AdminFeedController {
     
     Logger log = Logger.getLogger(AdminFeedController.class);
     
     private AdminRequestFilter requestFilter;
     private FeedReaderRunner feedReaderRunner;    
 	private RssNewsitemPrefetcher rssPrefetcher;
     private UrlBuilder urlBuilder;
     private EditPermissionService permissionService;
     
     
     public AdminFeedController(AdminRequestFilter requestFilter,
 			FeedReaderRunner feedReaderRunner,
 			RssNewsitemPrefetcher rssPrefetcher, UrlBuilder urlBuilder,
 			EditPermissionService permissionService) {
 		this.requestFilter = requestFilter;
 		this.feedReaderRunner = feedReaderRunner;
 		this.rssPrefetcher = rssPrefetcher;
 		this.urlBuilder = urlBuilder;
 		this.permissionService = permissionService;
 	}
 
     
     @Transactional   
     public ModelAndView decachefeed(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {          
         requestFilter.loadAttributesOntoRequest(request);
         Feed feed = null;
         if (request.getAttribute("feedAttribute") != null) {
             feed = (Feed) request.getAttribute("feedAttribute");
             
             if (!permissionService.canDecache(feed)) {
             	log.warn("Not allowed to decache this feed"); // TODO return http auth error
             	return null;            	
             }
                         
             log.info("Decaching feed: " + feed.getName());
             rssPrefetcher.decacheAndLoad(feed);
         } else {
             log.info("No feed seen on request; nothing to decache.");            
         }        
         return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));
     }
     
         
     @Transactional
     public ModelAndView readfeed(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
         requestFilter.loadAttributesOntoRequest(request);
         Feed feed = null;
         if (request.getAttribute("feedAttribute") != null) {
             feed = (Feed) request.getAttribute("feedAttribute");
             
             if (!permissionService.canRead(feed)) {
             	log.warn("Not allowed to read this feed"); // TODO return http auth error
             	return null; 	
             }
             
             log.info("Reading feed: " + feed.getName());           
             feedReaderRunner.readSingleFeed(feed);
             
         } else {
             log.info("No feed seen on request; nothing to reread.");
         }        
         return new ModelAndView(new RedirectView(urlBuilder.getFeedUrl(feed)));   
     }
     
 }
