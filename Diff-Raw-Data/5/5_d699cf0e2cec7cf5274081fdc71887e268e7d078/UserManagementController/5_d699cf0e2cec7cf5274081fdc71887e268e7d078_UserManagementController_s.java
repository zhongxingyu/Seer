 package org.homebudget.controllers;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.validation.Valid;
 
 import org.homebudget.dao.UserRepository;
 import org.homebudget.model.UserDetails;
 import org.homebudget.model.UserRole;
 import org.homebudget.services.UserManagementService;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class UserManagementController {
 
 	@Resource
 	private UserRepository userRepositoryDao;
 	
 	@Resource
 	private UserManagementService userManagementService;
 
 	@RequestMapping(value = "/addUser")
 	public String addNewUser(
 			@ModelAttribute("userDetails") UserDetails userDetails) {
 
 		if (userDetails.getUserName() != null) {
                         userDetails.addUserRole(UserRole.Role.USER_ROLE);
 			userRepositoryDao.save(userDetails);
 		}
 
 		System.out.println("User Name: " + userDetails.getUserName());
 		System.out.println("User Surname: " + userDetails.getUserSurname());
 		System.out.println("User Date of Birth: "
 				+ userDetails.getUserBirthday());
 		return "addUser";
 	}
 	
 		@RequestMapping(value = "/userProfile", method = RequestMethod.GET)
 	public ModelAndView showUserProfile(Map<String, Object> model) {
 
 		final User user = (User) SecurityContextHolder.getContext()
 				.getAuthentication().getPrincipal();
 		UserDetails aUserDetails = userRepositoryDao.findByUserUsername(user.getUsername()).get(0);
 	        model.put("userDetails", aUserDetails);
 		
 		return new ModelAndView("userprofile");
 	}
 	
 	
 	@RequestMapping(value = "/updateDetails", method = RequestMethod.GET)
 	public ModelAndView showUserDetails(Map<String, Object> model) {
 
 		final User user = (User) SecurityContextHolder.getContext()
 				.getAuthentication().getPrincipal();
 		UserDetails aUserDetails = userRepositoryDao.findByUserUsername(user.getUsername()).get(0);
 	        model.put("userDetails", aUserDetails);
 		
 		return new ModelAndView("userprofile");
 	}
 		
 		
 		@RequestMapping(value = "/updateDetails", method = RequestMethod.POST)
		public ModelAndView updateUserDetails(@Valid UserDetails newUserDetails, BindingResult result, Map<String, Object> model) {
 
 			final User user = (User) SecurityContextHolder.getContext()
 					.getAuthentication().getPrincipal();
 			UserDetails oldUserDetails = userRepositoryDao.findByUserUsername(user.getUsername()).get(0);
 			getUserManagementService().updateUserDetails(oldUserDetails, newUserDetails);
 			
 	        model.put("userDetails", oldUserDetails);
 	        
			return new ModelAndView("userprofile");
 	}
 
 	@RequestMapping(value = "/usersList")
 	public String showAllUsers(Model model) {
 
 		List<UserDetails> usersList = getHibernateDaoImpl().findAll();
 		System.out.println("counted users: " + usersList.size());
 
 		model.addAttribute("usersList", usersList);
 		return "usersList";
 	}
 
 	@InitBinder
 	protected void initBinder(WebDataBinder binder) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
 		binder.registerCustomEditor(Date.class, new CustomDateEditor(
 				dateFormat, false));
 	}
 
 	public UserRepository getHibernateDaoImpl() {
 		return userRepositoryDao;
 	}
 
 	public void setHibernateDaoImpl(UserRepository hibernateDaoImpl) {
 		this.userRepositoryDao = hibernateDaoImpl;
 	}
 	
 		public UserManagementService getUserManagementService() {
 		return userManagementService;
 	}
 
 	public void setUserManagementService(UserManagementService userManagementService) {
 		this.userManagementService = userManagementService;
 	}
 }
