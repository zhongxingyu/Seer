 package edu.uib.info323.controller;
 
 
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 
 import edu.uib.info323.dao.ImageDao;
 import edu.uib.info323.model.Image;
 
 /**
  * Sample controller for going to the home page with a message
  */
 @Controller
 public class HomeController {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
 
 	@Autowired
 	private ImageDao imageDao;
 
 
 	/**
 	 * Selects the home page and populates the model with a message
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home() {
 		return "home";
 	}
 
 	/**
 	 * Selects the home page and populates the model with a message
 	 */
 	@RequestMapping(value = "/search")
 	@ResponseStatus(value = HttpStatus.OK)
 	public ModelAndView search(@RequestParam(required=false) String color) {
 		LOGGER.error("Returning color: " + color);
 
 		List<Image> urls = imageDao.getAllImages();
 
 		LOGGER.debug("Number of images returned:" + urls.size());
 		Map<String, Object> model = new TreeMap<String, Object>();
 		model.put("images", urls);
 		ModelAndView mav = new ModelAndView("home", model);
 		return mav;
 	}
 
 	@RequestMapping(value = "/color", method = RequestMethod.GET)
 	@ResponseStatus(value = HttpStatus.OK)
	public @ResponseBody List<Image> color(@RequestParam(required=true) String colors){
		LOGGER.debug("Returning images for color: " + colors);
 		
 		return imageDao.getImagesWithColor("0x"+colors);
 	}
 
 	@RequestMapping("/spring")
 	public String redirectSpring() {
 		return "redirect:/search";
 	}
 
 
 }
