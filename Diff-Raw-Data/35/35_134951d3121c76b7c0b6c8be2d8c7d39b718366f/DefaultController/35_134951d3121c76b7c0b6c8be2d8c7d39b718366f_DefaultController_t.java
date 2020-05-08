 package cz.muni.fi.pa165.pujcovnaStroju.web.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import cz.muni.fi.pa165.pujcovnastroju.dto.SystemUserDTO;
 import cz.muni.fi.pa165.pujcovnastroju.service.SystemUserService;
 
 /**
  * Controller handling root request
  * 
  * @author Michal Merta
  * 
  */
 @Controller
 public class DefaultController {
 
 	private static SystemUserService userService;
 
 	@Autowired
 	public DefaultController(SystemUserService userService) {
 		this.userService = userService;
 	}
 
 	@RequestMapping("/")
 	public String viewIndes(ModelMap model) {
 		model.addAttribute("pageTitle", "lang.listMachinesTitle");
 		addHeaderFooterInfo(model);
 		return "index";
 	}
 
 	/**
 	 * adds header and footer content to rendered page
 	 */
 	static ModelMap addHeaderFooterInfo(ModelMap model) {
 		model.addAttribute("pageFooter", "lang.footer");
 		model.addAttribute("pageHeader", "lang.header");
 		addLeftMenuSecurity(model);
 		return model;
 	}
 
 	/**
 	 * adds static left menu content to rendered page
 	 * 
 	 * @param model
 	 * @return
 	 */
 	static ModelMap addLeftMenu(ModelMap model) {
 		return model;
 	}
 
 	static ModelMap addLeftMenuSecurity(ModelMap model) {
		SystemUserDTO user = getLoggedUser();
		String type = "";
		if (user != null) {
			type = user.getType().getTypeLabel();
			model.addAttribute("userID", user.getId());
		}
		model.addAttribute("userType", type);
 		return model;
 	}
 
	static SystemUserDTO getLoggedUser() {
		SystemUserDTO user = null;
 		if (SecurityContextHolder.getContext().getAuthentication() != null
 				&& SecurityContextHolder.getContext().getAuthentication()
 						.isAuthenticated()) {
 			if (SecurityContextHolder.getContext().getAuthentication() != null) {
 				String authUserString = SecurityContextHolder.getContext()
 						.getAuthentication().getName();
					user = userService
 						.getSystemUserByUsername(authUserString);
 			}
 		}
		return user;
 	}
 }
