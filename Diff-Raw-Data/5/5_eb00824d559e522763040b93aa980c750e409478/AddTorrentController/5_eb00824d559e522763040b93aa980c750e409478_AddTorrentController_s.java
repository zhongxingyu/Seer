 package ar.edu.utn.tacs.group5.controller;
 
 import java.util.logging.Logger;
 
 import org.slim3.controller.Controller;
 import org.slim3.controller.Navigation;
 
 import ar.edu.utn.tacs.group5.controller.Constants;
 import ar.edu.utn.tacs.group5.service.FeedService;
 
 public class AddTorrentController extends Controller {
 	
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 	private FeedService feedService = new FeedService();
 
     @Override
     public Navigation run() throws Exception {
    	String userId = sessionScope(Constants.USER_ID);
     	String link = param(Constants.LINK);
     	logger.info(link);
		feedService.addTorrent(Long.valueOf(userId), link);
 
 		if(Boolean.valueOf(param(Constants.FROM_FB))){
 			return redirect("index.jsp");
 		}
 		return null;
     }
 }
