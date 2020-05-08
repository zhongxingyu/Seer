 package edu.uib.info323.controller;
 
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * Sample controller for going to the home page with a message
  */
 @Controller
 public class HomeController {
 
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HomeController.class);
 
 
 	/**
 	 * Selects the home page and populates the model with a message
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public ModelAndView home() {
 		
		EmbeddedDatabase database = new EmbeddedDatabaseBuilder().addScript("schema.sql").addScript("test-data.sql").build();
 		List<String> urls = new LinkedList<String>();
 		File imageFolder = new File("src/main/webapp/resources/testimg");
 		for(File f :imageFolder.listFiles()){
 			urls.add(f.getName());
 		}
		LOGGER.debug("Returning file list: " + urls);
 		Map<String, Object> model = new TreeMap<String, Object>();
 		model.put("images", urls);
 		ModelAndView mav = new ModelAndView("home", model);
 		return mav;
 	}
 	
 	public static void main(String[] args) {
 		HomeController controller = new HomeController();
 		controller.home();
 	}
 
 }
