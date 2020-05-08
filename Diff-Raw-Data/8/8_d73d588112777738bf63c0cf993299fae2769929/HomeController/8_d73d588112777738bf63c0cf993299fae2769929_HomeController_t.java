 package com.vmware.armyants;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.RequestToken;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class HomeController {
 	
 	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
 	
 	@Autowired
 	private LuceneIndexer searchEngine;
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(Locale locale, Model model) {
 		logger.info("Welcome home! the client locale is "+ locale.toString());
 		
 		try {
 			searchEngine.indexDocs("civic-commons");
 			String[] results = searchEngine.search("eclipse");
 			for(String result : results) {
 				model.addAttribute("result", result);
 			}
 			searchEngine.addRFPToStore(new DocType("rfp1","dummy body"));
 		} catch (Exception e) {
 			logger.info("Exception in Lucene" + e);
 		} 
 		String environmentName = (System.getenv("VCAP_APPLICATION") != null) ? "Cloud" : "Local";
 		model.addAttribute("environmentName", environmentName);
 		return "home";
 	}
 	
 	@RequestMapping(value = "/login", method = RequestMethod.GET)
 	public void loginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException {
 	    Twitter twitter = new TwitterFactory().getInstance();
         request.getSession().setAttribute("twitter", twitter);
         try {
             StringBuffer callbackURL = request.getRequestURL();
             int index = callbackURL.lastIndexOf("/");
             callbackURL.replace(index, callbackURL.length(), "").append("/callback");
 
             RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL.toString());
             request.getSession().setAttribute("requestToken", requestToken);
             response.sendRedirect(requestToken.getAuthenticationURL());
         } catch (TwitterException e) {
         	logger.info(e.toString());
             throw new ServletException(e);
         } catch (IOException e) {
 			logger.info(e.toString());
 		}
 	}
 }
