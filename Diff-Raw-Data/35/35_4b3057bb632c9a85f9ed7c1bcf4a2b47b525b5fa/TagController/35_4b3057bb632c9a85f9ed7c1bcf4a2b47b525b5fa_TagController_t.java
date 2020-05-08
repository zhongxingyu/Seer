 package nz.co.searchwellington.controllers;
 
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
 import nz.co.searchwellington.model.Resource;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.repositories.TagDAO;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
 
 import com.sun.syndication.io.FeedException;
 
 
public class TagController extends MultiActionController {
 	
     static Logger log = Logger.getLogger(TagController.class);
    
     private ContentModelBuilderService contentModelBuilder;
     private ContentRetrievalService contentRetrievalService;
     private TagDAO tagDAO;
     private UrlStack urlStack;
 
 
     public TagController(UrlStack urlStack,
 			ContentModelBuilderService contentModelBuilder,
 			ContentRetrievalService contentRetrievalService, TagDAO tagDAO) {
 		this.urlStack = urlStack;
 		this.contentModelBuilder = contentModelBuilder;
 		this.contentRetrievalService = contentRetrievalService;
 		this.tagDAO = tagDAO;
 	}
     
     
 	public ModelAndView normal(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, FeedException, IOException {
         log.info("Starting normal content");                                  
         boolean showBroken = false;
         
 		ModelAndView mv = contentModelBuilder.populateContentModel(request);
 		if (mv != null) {
 			urlStack.setUrlStack(request);
 			addCommonModelElements(mv, showBroken);
 			return mv;
 		}
 		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 		return null;
     }
 
 	// TODO this should be in model builders
     private void addCommonModelElements(ModelAndView mv, boolean showBroken) throws IOException {
 		mv.addObject("top_level_tags", tagDAO.getTopLevelTags());
         final List<Resource> latestNewsitems = contentRetrievalService.getLatestNewsitems(5);
         mv.addObject("latest_newsitems", latestNewsitems);
 	}
     
 }
