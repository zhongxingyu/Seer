  package nz.co.searchwellington.controllers.models;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.feeds.RssfeedNewsitemService;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.repositories.ResourceRepository;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 
 public class ViewFeedModelBuilder extends AbstractModelBuilder implements ModelBuilder {
 
 	Logger log = Logger.getLogger(ViewFeedModelBuilder.class);
     	
 	private ResourceRepository resourceDAO;
 	private RssfeedNewsitemService rssfeedNewsitemService;
 	
 	 
	public ViewFeedModelBuilder(ResourceRepository resourceDAO, RssfeedNewsitemService rssfeedNewsitemService) {
 		this.resourceDAO = resourceDAO;
 		this.rssfeedNewsitemService = rssfeedNewsitemService;
 	}
 
 	public boolean isValid(HttpServletRequest request) {
 		return request.getAttribute("feedAttribute") != null;	
 	}
 
 	public ModelAndView populateContentModel(HttpServletRequest request, boolean showBroken) {
 		if (isValid(request)) {
 			log.info("Building view feed model");
 			Feed feed = (Feed) request.getAttribute("feedAttribute");
 			if (feed != null) {                       
 				ModelAndView mv = new ModelAndView();
 				mv.addObject("feed", feed);        
 				
 				List<FeedNewsitem> feedNewsitems = rssfeedNewsitemService.getFeedNewsitems(feed);
 				for (FeedNewsitem feedNewsitem : feedNewsitems) {
 					if (feedNewsitem.getUrl() != null) {
 						Resource localCopy = resourceDAO.loadResourceByUrl(feedNewsitem.getUrl());
 						if (localCopy != null) {
 							feedNewsitem.setLocalCopy(localCopy);
 						}
 					}
 				}
 		            
 		       if (feedNewsitems != null && feedNewsitems.size() > 0) {
 		    	   mv.addObject("main_content", feedNewsitems);
 		       } else {
 		    	   log.warn("No newsitems were loaded from feed: " + feed.getName());
 		       }
 		       
 		       setRss(mv, feed.getName(), feed.getUrl());
 		       
 		       mv.setViewName("viewfeed");
 		       return mv;            
 			}
 		}
 		return null;
 	}
 	
 	
 	public void populateExtraModelConent(HttpServletRequest request, boolean showBroken, ModelAndView mv) {
 		populateSecondaryFeeds(mv);
 	}
 	
 	
 	// TODO duplication with BaseM'E'C
 	public void populateSecondaryFeeds(ModelAndView mv) {      
         mv.addObject("righthand_heading", "Local Feeds");                
         mv.addObject("righthand_description", "Recently updated feeds from local organisations.");        
         final List<Feed> allFeeds = resourceDAO.getAllFeeds();
         if (allFeeds.size() > 0) {
             mv.addObject("righthand_content", allFeeds);       
         }
     }
 	
 }
