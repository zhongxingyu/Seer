 package nz.co.searchwellington.controllers;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.controllers.admin.AdminRequestFilter;
 import nz.co.searchwellington.controllers.admin.EditPermissionService;
 import nz.co.searchwellington.feeds.RssfeedNewsitemService;
 import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
 import nz.co.searchwellington.htmlparsing.SnapshotBodyExtractor;
 import nz.co.searchwellington.model.Feed;
 import nz.co.searchwellington.model.FeedNewsitem;
 import nz.co.searchwellington.model.Newsitem;
 import nz.co.searchwellington.model.PublishedResource;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.model.Tag;
 import nz.co.searchwellington.model.TwitteredNewsitem;
 import nz.co.searchwellington.model.UrlWordsGenerator;
 import nz.co.searchwellington.model.User;
 import nz.co.searchwellington.modification.ContentDeletionService;
 import nz.co.searchwellington.modification.ContentUpdateService;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.repositories.HandTaggingDAO;
 import nz.co.searchwellington.repositories.ResourceRepository;
 import nz.co.searchwellington.spam.SpamFilter;
 import nz.co.searchwellington.tagging.AutoTaggingService;
 import nz.co.searchwellington.twitter.TwitterNewsitemBuilderService;
 import nz.co.searchwellington.widgets.AcceptanceWidgetFactory;
 import nz.co.searchwellington.widgets.TagWidgetFactory;
 
 import org.apache.log4j.Logger;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 
 @Transactional
 public class ResourceEditController extends BaseMultiActionController {
     
     Logger log = Logger.getLogger(ResourceEditController.class);
            
     private RssfeedNewsitemService rssfeedNewsitemService;
     private AdminRequestFilter adminRequestFilter;    
     private TagWidgetFactory tagWidgetFactory;
     private AutoTaggingService autoTagger;
     private AcceptanceWidgetFactory acceptanceWidgetFactory;
     private RssNewsitemPrefetcher rssPrefetcher;
     private EditPermissionService editPermissionService;
     private TwitterNewsitemBuilderService twitterNewsitemBuilderService;
     private SubmissionProcessingService submissionProcessingService;
     private ContentUpdateService contentUpdateService;
 	private ContentDeletionService contentDeletionService;
     private SnapshotBodyExtractor snapBodyExtractor;
     private AnonUserService anonUserService;
     private ResourceRepository resourceDAO;
 	private HandTaggingDAO tagVoteDAO;
     
     public ResourceEditController(
 			RssfeedNewsitemService rssfeedNewsitemService,
 			AdminRequestFilter adminRequestFilter,
 			TagWidgetFactory tagWidgetFactory, AutoTaggingService autoTagger,
 			AcceptanceWidgetFactory acceptanceWidgetFactory,
 			RssNewsitemPrefetcher rssPrefetcher,
 			LoggedInUserFilter loggedInUserFilter,
 			EditPermissionService editPermissionService, UrlStack urlStack,
 			TwitterNewsitemBuilderService twitterNewsitemBuilderService,
 			SubmissionProcessingService submissionProcessingService,
 			ContentUpdateService contentUpdateService,
 			ContentDeletionService contentDeletionService,
 			ResourceRepository resourceDAO,
 			SnapshotBodyExtractor snapBodyExtractor,
 			AnonUserService anonUserService,
 			ContentRetrievalService contentRetrievalService,
 			HandTaggingDAO tagVoteDAO) {
 		this.rssfeedNewsitemService = rssfeedNewsitemService;
 		this.adminRequestFilter = adminRequestFilter;
 		this.tagWidgetFactory = tagWidgetFactory;
 		this.autoTagger = autoTagger;
 		this.acceptanceWidgetFactory = acceptanceWidgetFactory;
 		this.rssPrefetcher = rssPrefetcher;
 		this.loggedInUserFilter = loggedInUserFilter;
 		this.editPermissionService = editPermissionService;
 		this.urlStack = urlStack;
 		this.twitterNewsitemBuilderService = twitterNewsitemBuilderService;
 		this.submissionProcessingService = submissionProcessingService;
 		this.contentUpdateService = contentUpdateService;
 		this.contentDeletionService = contentDeletionService;
 		this.resourceDAO = resourceDAO;
 		this.snapBodyExtractor = snapBodyExtractor;
 		this.anonUserService = anonUserService;
 		this.contentRetrievalService = contentRetrievalService;
 		this.tagVoteDAO = tagVoteDAO;
 	}
    
     
        
     @Transactional
     public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) {
         response.setCharacterEncoding("UTF-8");
         
     	adminRequestFilter.loadAttributesOntoRequest(request);    	
     	Resource resource = (Resource) request.getAttribute("resource");    	
     	if (request.getAttribute("resource") == null) { 
     		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         	return null;
     	}
     	    	
     	User loggedInUser = loggedInUserFilter.getLoggedInUser();
     	if (!userIsAllowedToEdit(resource, request, loggedInUser)) {
     		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         	return null;
     	}
     	
     	    	
     	ModelAndView mv = new ModelAndView("editResource");
     	populateCommonLocal(mv);
     	mv.addObject("heading", "Editing a Resource");
     		
         mv.addObject("resource", resource);
         mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, resource)));
         mv.addObject("show_additional_tags", 1);
             
         boolean userIsLoggedIn = loggedInUser != null;
         populatePublisherField(mv, userIsLoggedIn, resource);
                 
         if (resource.getType().equals("F")) {            
         	mv.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect (((Feed)resource).getAcceptancePolicy()));                   
         }               
         return mv;        
     }
 
 
     
 	@Transactional
     public ModelAndView viewSnapshot(HttpServletRequest request, HttpServletResponse response) {
     	
 		adminRequestFilter.loadAttributesOntoRequest(request);    	
     	if (request.getAttribute("resource") == null) { 
     		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         	return null;
     	}
     	Resource resource = (Resource) request.getAttribute("resource");    	
     	    	
     	User loggedInUser = loggedInUserFilter.getLoggedInUser();
     	if (!userIsAllowedToEdit(resource, request, loggedInUser)) {
     		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         	return null;
     	}
     	
     	Resource editResource = (Resource) request.getAttribute("resource");    	
     	if (request.getAttribute("resource") != null && userIsAllowedToEdit(editResource, request, loggedInUser)) {    		
     		ModelAndView mv = new ModelAndView("viewSnapshot");
     		populateCommonLocal(mv);
     		mv.addObject("heading", "Resource snapshot");
     		
             mv.addObject("resource", editResource);
             mv.addObject("body", snapBodyExtractor.extractSnapshotBodyTextFor(editResource));
             
             mv.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(tagVoteDAO.getHandpickedTagsForThisResourceByUser(loggedInUser, editResource)));
             mv.addObject("show_additional_tags", 1);
             
             return mv;
         }
        
     	return new ModelAndView(new RedirectView(urlStack.getExitUrlFromStack(request)));
     }
     
     
     
     @Transactional
     public ModelAndView accept(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, IOException {
         response.setCharacterEncoding("UTF-8");
             
     	User loggedInUser = loggedInUserFilter.getLoggedInUser();
     	if (!editPermissionService.canAcceptFeedItems(loggedInUser)) {
     		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
         	return null;
     	}
     	
         ModelAndView modelAndView = new ModelAndView("acceptResource");       
         populateCommonLocal(modelAndView);
         modelAndView.addObject("heading", "Accepting a submission");        
         
         Newsitem newsitem = null;
         if (request.getParameter("item") != null) {
         	int item = (Integer) request.getAttribute("item");    	  
         	if (request.getAttribute("feedAttribute") != null) {
         		newsitem = getRequestedFeedItemByFeedAndItemNumber(request, newsitem, item);
         	}
       
         } else if (request.getParameter("url") != null) {
         	newsitem = getRequestedFeedItemByUrl(request.getParameter("url"));        	
         }
         
         if (newsitem != null) {        	      
         	boolean newsitemHasNoDate = (newsitem.getDate() == null);
             if (newsitemHasNoDate) {
             	final Date today = Calendar.getInstance().getTime();
             	newsitem.setDate(today);
             }
             
             modelAndView.addObject("resource", newsitem);
             modelAndView.addObject("publisher_select", "1");
             modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(new HashSet<Tag>()));
         }
         return modelAndView;
     }
 
 
     
     @Transactional
     public ModelAndView twitteraccept(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, IOException {
         ModelAndView modelAndView = new ModelAndView("acceptResource");       
         
         adminRequestFilter.loadAttributesOntoRequest(request);
         if (request.getAttribute("twitterId") != null) {        
             Long twitterId = (Long) request.getAttribute("twitterId");
             TwitteredNewsitem twittedNewsitem = twitterNewsitemBuilderService.getPossibleSubmissionByTwitterId(twitterId);
             
             if (twittedNewsitem != null) {
             	log.info("Attempting to accept newsitem from twitter id: " + twitterId);
             	final Newsitem newsitem = twitterNewsitemBuilderService.makeNewsitemFromTwitteredNewsitem(twittedNewsitem);
             	
             	if (newsitem != null) {            		
             		modelAndView.addObject("resource", newsitem);
             		
             	} else {
             		log.info("Could not extract a newsitem from this twit");            		
             	}
             	
             } else {
             	log.warn("Could not find twitter with id: " + twitterId);            	
             }
             
         } else {
         	log.warn("No twitted id found on request");
         }
         
 		return modelAndView;
     }
 
 
     @Transactional
     public ModelAndView submitWebsite(HttpServletRequest request, HttpServletResponse response) {    
         ModelAndView modelAndView = new ModelAndView("submitWebsite");
         modelAndView.addObject("heading", "Submitting a Website");        
         Resource editResource = resourceDAO.createNewWebsite();
         modelAndView.addObject("resource", editResource);
        
         populateSubmitCommonElements(request, modelAndView);
         
         modelAndView.addObject("publisher_select", null);
         return modelAndView;
     }
 
     
     @Transactional
     public ModelAndView submitNewsitem(HttpServletRequest request, HttpServletResponse response) {
         ModelAndView modelAndView = new ModelAndView("submitNewsitem");
         modelAndView.addObject("heading", "Submitting a Newsitem");
         Resource editResource = resourceDAO.createNewNewsitem();
         modelAndView.addObject("resource", editResource);
         
         populateSubmitCommonElements(request, modelAndView);        
         return modelAndView;
     }
 
 
     @Transactional
     public ModelAndView submitCalendar(HttpServletRequest request, HttpServletResponse response) {        
         ModelAndView modelAndView = new ModelAndView("submitCalendar");
         modelAndView.addObject("heading", "Submitting a Calendar");
         Resource editResource = resourceDAO.createNewCalendarFeed("");
         modelAndView.addObject("resource", editResource);
         
         populateSubmitCommonElements(request, modelAndView);        
         return modelAndView;
     }
     
     
     
     @Transactional
     public ModelAndView submitFeed(HttpServletRequest request, HttpServletResponse response) {
         ModelAndView modelAndView = new ModelAndView("submitFeed");
         modelAndView.addObject("heading", "Submitting a Feed");
         Resource editResource = resourceDAO.createNewFeed();
         modelAndView.addObject("resource", editResource);
         modelAndView.addObject("acceptance_select", acceptanceWidgetFactory.createAcceptanceSelect(null));
         
         populateSubmitCommonElements(request, modelAndView);
         
         return modelAndView;
     }
     
     
     @Transactional
     public ModelAndView submitWatchlist(HttpServletRequest request, HttpServletResponse response) {
         ModelAndView modelAndView = new ModelAndView("submitWatchlist");
         modelAndView.addObject("heading", "Submitting a Watchlist Item");
         Resource editResource = resourceDAO.createNewWebsite();
         modelAndView.addObject("resource", editResource);
         
         populateSubmitCommonElements(request, modelAndView);
         
         return modelAndView;
     }
    
     
     
     @Transactional
     public ModelAndView delete(HttpServletRequest request, HttpServletResponse response) {    
         ModelAndView modelAndView = new ModelAndView("deletedResource");
         populateCommonLocal(modelAndView);
         modelAndView.addObject("heading", "Resource Deleted");
         
         adminRequestFilter.loadAttributesOntoRequest(request);    
         Resource editResource = (Resource) request.getAttribute("resource");
         
 		if (editResource != null && editPermissionService.canDelete(editResource)) {
             modelAndView.addObject("resource", editResource);
             editResource = (Resource) request.getAttribute("resource");            
             contentDeletionService.performDelete(editResource);
             
     		if (editResource.getType().equals("F")) { 
     			urlStack.setUrlStack(request, "/index");
     		}
         }
 		
         // TODO need to given failure message if we didn't actually remove the item.
         return modelAndView;
     }
 
 
     
 	@Transactional
     public ModelAndView save(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {       
         request.setCharacterEncoding("UTF-8");
         response.setCharacterEncoding("UTF-8");
 
         ModelAndView modelAndView = new ModelAndView("savedResource");
         populateCommonLocal(modelAndView);       
         modelAndView.addObject("heading", "Resource Saved");
         
         User loggedInUser = loggedInUserFilter.getLoggedInUser();
         
         Resource editResource = null;
         adminRequestFilter.loadAttributesOntoRequest(request);   
         
         
         if (request.getAttribute("resource") != null) {
             editResource = (Resource) request.getAttribute("resource");
             
         } else {
             log.info("Creating new resource.");
             if (request.getParameter("type") != null) {
                 String type = request.getParameter("type");
                 if (type.equals("W")) {
                     editResource = resourceDAO.createNewWebsite(); 
                 } else if (type.equals("N")) {
                     editResource = resourceDAO.createNewNewsitem(); 
                 } else if (type.equals("F")) {
                     editResource = resourceDAO.createNewFeed();                     
                 } else if (type.equals("L")) {                    
                     editResource = resourceDAO.createNewWatchlist();                   
                 } else if (type.equals("C")) {
                     editResource = resourceDAO.createNewCalendarFeed("");                   
                 } else {
                     // TODO this should be a caught error.
                     editResource = resourceDAO.createNewWebsite();
                 }
             }
                         
         }
        
         log.info("In save");
         if (editResource != null) {                      
             boolean newSubmission = editResource.getId() == 0;
                    
             submissionProcessingService.processUrl(request, editResource);
             submissionProcessingService.processTitle(request, editResource);
             submissionProcessingService.processGeocode(request, editResource);
             submissionProcessingService.processDate(request, editResource);
             submissionProcessingService.processHeld(request, editResource);
             submissionProcessingService.processEmbargoDate(request, editResource);
             submissionProcessingService.processDescription(request, editResource);
             submissionProcessingService.processPublisher(request, editResource);
             
             if (editResource.getType().equals("N")) {
             	submissionProcessingService.processImage(request, (Newsitem) editResource, loggedInUser);            
             }
                         
             processFeedAcceptancePolicy(request, editResource);
                              
             // Update urlwords.
             if (editResource.getType().equals("W") || editResource.getType().equals("F")) {
             	editResource.setUrlWords(UrlWordsGenerator.makeUrlWordsFromName(editResource.getName()));            	
             }
                         
                       
             SpamFilter spamFilter = new SpamFilter();
             boolean isSpamUrl = spamFilter.isSpam(editResource);
                      
             boolean isPublicSubmission = loggedInUser == null;
             if (isPublicSubmission) {
             	log.info("This is a public submission; marking as held");
             	editResource.setHeld(true);
             }
             
             boolean okToSave = !newSubmission || !isSpamUrl || loggedInUser != null;
             // TODO validate. - what exactly?
             if (okToSave) {
             	// TODO could be a collection?  			 
                 
             	if (loggedInUser == null) {
             		log.info("Creating new anon user for resource submission");
             		loggedInUser = anonUserService.createAnonUser();
             		setUser(request, loggedInUser);
             	}
             	
            	saveResource(request, loggedInUser, editResource);
            	log.info("Saved resource; id is now: " + editResource.getId());
             	submissionProcessingService.processTags(request, editResource, loggedInUser);
            	
                 if (newSubmission) {
                     log.info("Applying the auto tagger to new submission.");
                     autoTagger.autotag(editResource);
                 }
 
             } else {
                 log.info("Could not save resource. Spam question not answered?");                
             }
            
             modelAndView.addObject("item", editResource);
             
         } else {
             log.warn("No edit resource could be setup.");
         }
        
         return modelAndView;
     }
 
 
     
     private void setUser(HttpServletRequest request, User user) {
 		request.getSession().setAttribute("user", user);		
 		request.getSession().setAttribute("login_prompt", null);
 	}
     
     
    // TODO move to submission handling service.
    private void processFeedAcceptancePolicy(HttpServletRequest request, Resource editResource) {
 	   if (editResource.getType().equals("F")) {		   
 		   ((Feed) editResource).setAcceptancePolicy("ignore");
 		   if (request.getParameter("acceptance") != null) {
 			   ((Feed) editResource).setAcceptancePolicy(request.getParameter("acceptance"));
 			   log.debug("Feed acceptance policy set to: " + ((Feed) editResource).getAcceptancePolicy());
 		   }
 		   rssPrefetcher.decacheAndLoad((Feed) editResource);
 	   }
    }
 
 
 	private void saveResource(HttpServletRequest request, User loggedInUser, Resource editResource) {		
 		contentUpdateService.update(editResource, loggedInUser, request);
 	}
 
 	
 	
     
     private boolean userIsAllowedToEdit(Resource editResource, HttpServletRequest request, User loggedInUser) {    
     	return editPermissionService.canEdit(editResource);
     }
 
     
     private void populateSubmitCommonElements(HttpServletRequest request, ModelAndView modelAndView) {
         populateCommonLocal(modelAndView);
         modelAndView.addObject("tag_select", tagWidgetFactory.createMultipleTagSelect(new HashSet<Tag>()));
    
         User loggedInUser = loggedInUserFilter.getLoggedInUser();
         boolean userIsLoggedIn = loggedInUser != null;       
         modelAndView.addObject("publisher_select", "1");
         
         if (userIsLoggedIn) {
             // TODO duplication - also - what does this do?
             modelAndView.addObject("show_additional_tags", 1);
         }
     }
     
     
 
 	private Newsitem getRequestedFeedItemByFeedAndItemNumber(
 			HttpServletRequest request, Newsitem newsitem, int item) {
 		log.info("Looking for feeditem by feed and item number: " + item);
 		Feed feed = (Feed) request.getAttribute("feedAttribute");   	     	  
 		
 		List <FeedNewsitem> feednewsItems = rssfeedNewsitemService.getFeedNewsitems(feed);
 		if (item > 0 && item <= feednewsItems.size()) {                    
 			FeedNewsitem feednewsitem = feednewsItems.get(item-1);
 			newsitem = rssfeedNewsitemService.makeNewsitemFromFeedItem(feednewsitem, feed);
 		}
 		return newsitem;
 	}
     
     
     
     private Newsitem getRequestedFeedItemByUrl(String url) {
     	return rssfeedNewsitemService.getFeedNewsitemByUrl(url);    	
     }
     
     protected void populatePublisherField(ModelAndView modelAndView, boolean userIsLoggedIn, Resource editResource) {
         boolean isPublishedResource = editResource instanceof PublishedResource;  
         if (isPublishedResource) {
             modelAndView.addObject("publisher_select", "1");   
         } else {
             log.info("Edit resource is not a publisher resource.");
         }
     }
 
 }
