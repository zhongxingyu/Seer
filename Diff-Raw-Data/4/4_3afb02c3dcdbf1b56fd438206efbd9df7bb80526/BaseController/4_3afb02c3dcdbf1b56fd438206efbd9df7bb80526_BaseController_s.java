 package com.seo.webapp;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
  
 @Controller
 public class BaseController {
 	
 	@RequestMapping(value="/search", method = RequestMethod.GET)
 	public ModelAndView search() {
 		return new ModelAndView("index", "command", new Query());
 	}
 
 	@RequestMapping(value="/result", method = RequestMethod.POST)
 	public String result(@ModelAttribute("SpringWeb")Query q, ModelMap model) {
 		// Query's m_query always seems to have a ',' (comma) that is appended--strip this. 
		if (q.getQuery().charAt(q.getQuery().length() - 1) == ',')
			q.setQuery(q.getQuery().substring(0, q.getQuery().length() - 1));
 		
 		try 
 		{
 			q.HTTP_Request();
 			q.setSiteArray();
 			q.setRecommendation();
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 			System.err.println(Query.LOG_ERR_MSG);
 			model.addAttribute("errMsg", Query.PAGE_ERR_MSG);
 			return "error";
 		}
 		// Adds query string, compare string, SiteMetrics array, and Recommendation to results.jsp
 		model.addAttribute("query", q.getQuery());
 		model.addAttribute("siteToCompare", q.getSiteToCompare());
 		model.addAttribute("sites", q.getSites());
 		model.addAttribute("recs", q.getRecommendation());
 		
 		return "result";
 	}
 	
 	/*
 	 * Default handler for links that have no mappings goto the error page. 
 	 */
 	@RequestMapping("/**")
     public String unmappedRequest( ModelMap model) {
 		model.addAttribute("errMsg", "Failed to find URL mapping of current request." );
 		return "error";
     }
 
  
 	@RequestMapping(value="/welcome/{name}", method = RequestMethod.GET)
 	public String welcomeName(@PathVariable String name, ModelMap model) {
  
 		model.addAttribute("message", "Maven Web Project + Spring 3 MVC - " + name);
 		return "sample";
  
 	}
 	
  
 }
