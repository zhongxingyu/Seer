 package com.fauxwerd.web.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.fauxwerd.model.Content;
 import com.fauxwerd.model.User;
 import com.fauxwerd.service.ContentService;
 import com.fauxwerd.service.UserService;
 
 @Controller
 @RequestMapping(value="/content")
 public class ContentController {
 	
     final Logger log = LoggerFactory.getLogger(getClass());
     
     @Autowired 
     private ContentService contentService; 
     @Autowired
     private UserService userService;
 	
 	@RequestMapping(value="/{contentIdString}", method=RequestMethod.GET)
 	public ModelAndView getContent(@PathVariable("contentIdString") String contentIdString) {
 		Long contentId = null;
 
 		try {
 			contentId = Long.valueOf(contentIdString);
 		}
 		catch (NumberFormatException e) {
 			if(log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}
 		
 		Content content = contentService.getContentById(contentId);
 		ModelAndView modelAndView = new ModelAndView("content/view", "content", content);		
 		return modelAndView;		
 	}
 	
 	@RequestMapping(method=RequestMethod.POST)
 	public String addContent(HttpServletRequest req, HttpServletResponse res, Model model) {
 
 		if (log.isDebugEnabled()) log.debug(String.format("character encoding = %s", req.getCharacterEncoding()));
 		
 		String urlString = req.getParameter("url");
 		URL url = null;
 		String userIdString = req.getParameter("userId");
 		Long userId = null;
 		String title = req.getParameter("title");
 				
 		try {
 			urlString = URLDecoder.decode(urlString, "utf-8");
 			title = URLDecoder.decode(title, "utf-8");
 			
 			//TODO improve this to work for twitter (e.g. http://twitter.com/#!/srcasm)
 			//drop any url fragments (#)
 			int fragmentIndex = urlString.indexOf('#');
 			if (fragmentIndex > 0) {
 				urlString = urlString.substring(0, fragmentIndex);
 			}
 			
 			url = new URL(urlString);
 			userId = Long.valueOf(userIdString);
 		}
 		catch (UnsupportedEncodingException e) {
 			if (log.isErrorEnabled()) {
 				log.error("", e);				
 			}
 		}
 		catch (MalformedURLException e) {
 			if (log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}
 		catch (NumberFormatException e) {
 			if(log.isErrorEnabled()) {
 				log.error("", e);
 			}
 		}
 		
 		if(log.isDebugEnabled()) {
 			log.debug(String.format("url = %s, id = %d, title = %s", url, userId, title));
 		}
 		
 		User user = userService.getUserById(userId);
 		
 		contentService.addContent(url, user, title);
 						
 		return "content/saved";
 	}
 
 }
