 package com.aavu.server.web.controllers;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 import com.aavu.client.domain.User;
 import com.aavu.server.service.UserService;
 
 public class BasicController extends AbstractController {
 	private static final Logger log = Logger.getLogger(BasicController.class);
 
 	private String view;
 	private UserService userService;	
 
 	public String getView() {
 		return view;
 	}
 	public void setView(String view) {
 		this.view = view;
 	}
 
 	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest req,
 			HttpServletResponse arg1) throws Exception {
 
		log.debug("SERVLET PATH: "+req.getServletPath());

 		User su = null;
 		try{
 			su = userService.getCurrentUser();	
 			return new ModelAndView(getView(),"user",su);
 		}catch(UsernameNotFoundException e){
 			log.debug("No user logged in.");
 		}
 
 		return new ModelAndView(getView());
 		
 	}
 
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 
 }
