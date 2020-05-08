 package com.zazarie.mvc;
 
 import java.io.IOException;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.KeyFactory;
 
 import java.io.Writer;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.zazarie.domain.DBHelper;
 import com.zazarie.domain.reddit.Children;
 import com.zazarie.domain.reddit.NewFeed;
 import com.zazarie.service.MirrorAPIHelper;
 import com.zazarie.service.RedditAPIHelper;
 
 @Controller
 public class TaskFindArticlesController {
 
 	private static final Logger LOG = Logger.getLogger(TaskFindArticlesController.class.getSimpleName());
 	
 	private MirrorAPIHelper mirrorAPIHelper;
 	
 	private RedditAPIHelper redditAPIHelper;
 	
 	@RequestMapping(value = "/taskFindArticles", method = RequestMethod.GET)
 	@ResponseBody
 	public String findArticles(ModelMap model, HttpServletRequest request, HttpServletResponse response) throws Exception {
 		
 		LOG.info("Landed inside /taskFindArticles servlet...");
 		
 		// We do this to immediately acknowlege ok to the scheduler
 	    response.setContentType("text/html");
 	    Writer writer = response.getWriter();
 	    writer.append("OK");
 	    writer.close();
 	    
 	    /* 
 	     * These are the steps I'm thinking will be followed:
 	     * 1. Scroll through each user row in the db.
 	     * 2. For each user, get the top 5 articles (for now, just using aaww). Use userid-reddit as id.
 	     * 3. Save articles to the database with null attribute for sentToGlass 
 	     */
 	    
 	    Iterable<Entity> userEntities = DBHelper.returnAllUsers();
 	    
 	    boolean firstTime = true;
 	    
 	    Entity article = null;
 	    
 	    // STEP 1 - Scroll through the user list
 	    for (Entity userEntity : userEntities) {
 	    	
 	    	// userEntity.getKey().getName()  actually contains the google id that we require for the mirror oauth requests
 	    	LOG.info("Processing user: " + userEntity.getProperty("email") + " userId is: " + userEntity.getKey().getId() + " key name: " + userEntity.getKey().getName() );
 	    	
 	    	// Let's make sure their oauth session is valid
 	    	userEntity = redditAPIHelper.checkOauthToken((String) userEntity.getProperty("email"), request);
 	    	
 	    	NewFeed feeds = null;
 	    	
	    	feeds = RedditAPIHelper.getArticlesBySubreddit("aww", 10);
	    	
 	    	// for now, I'm only going to call reddit once, instead of for each user. See their API rules
	    	/*
 	    	if (firstTime) {
 				try {
 					//feed = RedditAPIHelper.getNewArticles( ((RedditOauthSession) request.getSession().getAttribute("redditOauthSession")).getAccessToken());
 					feeds = RedditAPIHelper.getArticlesBySubreddit("aww", 10);
 					firstTime = false;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 	    	}
	    	*/
 	    		    	
 	    	LOG.info("feeds is: " + feeds);
 	    	
 	    	if (feeds != null) {
 		    	for (Children feed : feeds.getData().getChildren()) {
 		    		LOG.info("Processing..." + feed.getData().getTitle() + " id: " + feed.getData().getId() + " url:" + feed.getData().getUrl());
 		    		
 		    		// only process images for now that contain JPG or jpg
 		    		if (feed.getData().getUrl().toUpperCase().indexOf(".JPG") > 0) {
 		    			article = DBHelper.addArticle(userEntity.getKey().getName(), feed.getData().getId(), feed.getData().getUrl(), feed.getData().getTitle());
 		    		}
 		    	}
 	    	}
 	    	
 	    }
 	    
 		return null;
 	}
 	
 	public RedditAPIHelper getRedditAPIHelper() {
 		return redditAPIHelper;
 	}
 
 	@Autowired(required=true)
 	public void setRedditAPIHelper(RedditAPIHelper redditAPIHelper) {
 		this.redditAPIHelper = redditAPIHelper;
 	}
 
 	public MirrorAPIHelper getMirrorAPIHelper() {
 		return mirrorAPIHelper;
 	}
 
 	@Autowired(required=true)
 	public void setMirrorAPIHelper(MirrorAPIHelper mirrorAPIHelper) {
 		this.mirrorAPIHelper = mirrorAPIHelper;
 	}
 }
