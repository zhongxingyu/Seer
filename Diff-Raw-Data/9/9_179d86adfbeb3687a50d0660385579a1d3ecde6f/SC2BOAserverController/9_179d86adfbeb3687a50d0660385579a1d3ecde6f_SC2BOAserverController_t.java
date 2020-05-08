 package com.ALC.SC2BOAserver.web;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.ALC.SC2BOAserver.dao.SC2BOADAO;
 import com.ALC.SC2BOAserver.entities.OnlineBuildOrder;
 import com.ALC.SC2BOAserver.entities.User;
 import com.ALC.SC2BOAserver.util.DEBUG;
 
 
 @Controller
 public class SC2BOAserverController {
 
     private SC2BOADAO dao;
     private static final Logger logger=Logger.getLogger(SC2BOAserverController.class.getName());
 
 
     /**
      * AWS Elastic Beanstalk checks your application's health by periodically
      * sending an HTTP HEAD request to a resource in your application. By
      * default, this is the root or default resource in your application,
      * but can be configured for each environment.
      *
      * Here, we report success as long as the app server is up, but skip
      * generating the whole page since this is a HEAD request only. You
      * can employ more sophisticated health checks in your application.
      *
      * @param model the spring model for the request
      */
     @RequestMapping(value="/index", method=RequestMethod.HEAD)
     public void doHealthCheck(HttpServletResponse response) {
     	DEBUG.d("health check index head");
         response.setContentLength(0);
         response.setStatus(HttpServletResponse.SC_OK);
     }
 
     /**
      * sets up the about page
      */
     @RequestMapping(value="/about", method={RequestMethod.GET, RequestMethod.POST})
     public ModelAndView setupAboutPage (ModelMap model) {
     	DEBUG.d("setupAboutPage");
 		return new ModelAndView("about");
     }
     
     /**
      * sets up the about page
      */
     @RequestMapping(value="/builds", method={RequestMethod.GET, RequestMethod.POST})
     public ModelAndView setbuildsPage (ModelMap model) {
     	DEBUG.d("setupbuildsPage");
 		return new ModelAndView("builds");
     }
     
     /**
      * sets up the about page
      */
     @RequestMapping(value="/addbuildorder", method={RequestMethod.GET})
     public ModelAndView setupAddBuildOrderPage (ModelMap model) {
     	DEBUG.d("add build order page");
     	OnlineBuildOrder buildorder = new OnlineBuildOrder();
 		model.addAttribute("buildorder", buildorder);
 		return new ModelAndView("addbuildorder", "buildorder", buildorder);
 	}
 	
     @RequestMapping(value="/addbuildorder", method={RequestMethod.POST})
 	public ModelAndView setupAddBuildOrderReply(@ModelAttribute OnlineBuildOrder buildorder) {
 		DEBUG.d("add build order reply page: "+buildorder);
 		dao.addOnlineBuildOrder(buildorder);
		return new ModelAndView("builds");
 	}
     /**
      * sets up the rest
      */
     @RequestMapping(value="/restfulframework", method={RequestMethod.GET, RequestMethod.POST})
     public ModelAndView setupRestPage (ModelMap model) {
     	DEBUG.d("setupRestPage");
 		return new ModelAndView("restfulframework");
     }
     
     /**
      * sets up the test page
      */
     @RequestMapping(value="/displayallbuilds", method={RequestMethod.GET, RequestMethod.POST})
     public ModelAndView setupDisplayAllBuildsPage (ModelMap model) {
     	DEBUG.d("setupDisplayAllBuildsPage");
     	
     	SC2BOADAO dao = this.dao;
 		model.addAttribute("dao", dao);
 		return new ModelAndView("DisplayAllBuilds", "dao", dao);
 		//return new ModelAndView();
     }
 
 
     @Autowired
     public void setSC2BOADAO (SC2BOADAO dao) {
         this.dao = dao;
     }
 
     /**
      * Method establishes the transformation of incoming date strings into Date objects
      * @param binder the spring databinder object that we connect to the date editor
      */
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         dateFormat.setLenient(true);
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
     }
 
 
 }
