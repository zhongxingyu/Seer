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
 		//model.addAttribute("message", "Maven Web Project + Spring 3 MVC - welcome()");
 		//return "index";
 		return new ModelAndView("index", "command", new Query());
  
 	}
  
 	@RequestMapping(value="/welcome", method = RequestMethod.GET)
 	public String welcome(ModelMap model) {
  
 		model.addAttribute("message", "Maven Web Project + Spring 3 MVC - welcome()");
  
 		//Spring uses InternalResourceViewResolver and return back sample.jsp
 		return "sample";
  
 	}
 
 	@RequestMapping(value="/result", method = RequestMethod.POST)
 	public String result(@ModelAttribute("SpringWeb")Query q, ModelMap model) {
 		//model.addAttribute("message", "Results page.");
 		model.addAttribute("query", q.getQuery());
 		model.addAttribute("siteToCompare", q.getSiteToCompare());
 		try {
 			model.addAttribute("json", q.HTTP_Request());
 		} catch (Exception e) {
 			e.printStackTrace();
			System.err.println("Warning!! Could not connect to localhost:5000/*");
			model.addAttribute("errMsg", "Could not connect to webservice.");
			return "error";
 		}
 		return "result";
 	}
 
 	@RequestMapping(value="/student", method = RequestMethod.GET)
 	public ModelAndView student() {
 		return new ModelAndView("student", "command", new Student()); 
 	}
 
 	@RequestMapping(value = "/addStudent", method = RequestMethod.POST)
 	public String addStudent(@ModelAttribute("SpringWeb")Student student, ModelMap model) {
 		model.addAttribute("name", student.getName());
 		model.addAttribute("age", student.getAge());
 		model.addAttribute("id", student.getId());
 
 		return "studentResult";
 	}
 
 	@RequestMapping(value="/test", method = RequestMethod.GET)
 	public String test(ModelMap model) {
  
 		model.addAttribute("message", "Test completed successfully.");
  
 		//Spring uses InternalResourceViewResolver and return back sample.jsp
 		return "result";
  
 	}
  
  
 	@RequestMapping(value="/welcome/{name}", method = RequestMethod.GET)
 	public String welcomeName(@PathVariable String name, ModelMap model) {
  
 		model.addAttribute("message", "Maven Web Project + Spring 3 MVC - " + name);
 		return "sample";
  
 	}
  
 }
