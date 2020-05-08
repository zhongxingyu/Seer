 package com.tda.presentation.controller;
 
 import java.beans.PropertyEditorSupport;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.google.gson.Gson;
 import com.tda.model.applicationuser.ApplicationUser;
 import com.tda.model.applicationuser.Authority;
 import com.tda.persistence.paginator.Paginator;
 import com.tda.presentation.params.ParamContainer;
 import com.tda.service.api.ApplicationUserService;
 import com.tda.service.api.AuthorityService;
 import com.tda.service.exception.NoDataFoundException;
 import com.tda.service.exception.SingleResultExpectedException;
 
 @Controller
 @RequestMapping(value = "/applicationUser")
 @SessionAttributes("applicationUser")
 public class ApplicationUserController {
 
 	private static final String USER_FORM_DELETE_ERROR = "user.form.deleteError";
 	private static final String USER_FORM_NOT_FOUND = "user.form.notFound";
 	private static final String USER_FORM_MESSAGE = "message";
 	private static final String USER_FORM_ADD_SUCCESSFUL = "user.form.addSuccessful";
 	private static final String USER_FORM_EDIT_SUCCESSFUL = "user.form.editSuccessful";
 	private static final String PASSWORD_FORM_EDIT_SUCCESSFUL = "user.form.passwordEditSuccessful";
 	private static final String REDIRECT_TO_USER_LIST = "redirect:/applicationUser/";
 	private static final String USER_CREATE_FORM = "applicationUser/createForm";
 	private static final String USER_EDIT_FORM = "applicationUser/editForm";
 	private static final String PASSWORD_EDIT_FORM = "applicationUser/passwordForm";
 	private static final String USER_LIST = "applicationUser/list";
 
 	private Paginator paginator;
 	private ParamContainer params;
 	private ApplicationUserService applicationUserService;
 	private AuthorityService authorityService;
 
 	public ApplicationUserController() {
 		params = new ParamContainer();
 	}
 
 	@Autowired
 	public void setApplicationUserService(
 			ApplicationUserService applicationUserService) {
 		this.applicationUserService = applicationUserService;
 	}
 
 	@Autowired
 	public void setAuthorityService(AuthorityService authorityService) {
 		this.authorityService = authorityService;
 	}
 
 	@Autowired
 	public void setPaginator(Paginator paginator) {
 		this.paginator = paginator;
 		paginator.setOrderField("id");
 	}
 
 	@ModelAttribute("allAuthorities")
 	public List<Authority> populateAuthorities() {
 		return authorityService.findAll();
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.GET)
 	public String getCreateForm(Model model) {
 		model.addAttribute(new ApplicationUser());
 
 		return USER_CREATE_FORM;
 	}
 	
 	@RequestMapping(value = "/getUsers", method = RequestMethod.GET)
 	public @ResponseBody
 	String getOnlineUsers() {
 		// find all users
 		List<ApplicationUser> users = applicationUserService.findAll();
 		
 		//set admin authority
 		Authority adminAuth = new Authority();
 		adminAuth.setAuthority("ROLE_ADMIN");
 		
 		//ROLE_USER
 		Authority userAuth = new Authority();
 		userAuth.setAuthority("ROLE_USER");
 		
 		Iterator<ApplicationUser> iter = users.iterator();
 		ApplicationUser user;
 		
 		while(iter.hasNext()){
 			user = iter.next();
 			Collection<Authority> auths = user.getMyAuthorities();
 			
 			if(auths.size() == 2 && auths.contains(adminAuth) && auths.contains(userAuth)){
 				iter.remove();
 			}
 		}
 		
 		Gson gson = new Gson();
 		
 		return gson.toJson(users);
 	}
 
 	private void validateUserPasswords(ApplicationUser applicationUser,
 			BindingResult result) {
 		// Checking if passwords are the same
 		// FIXME: We should do something similar to this:
 		// http://stackoverflow.com/questions/1972933/cross-field-validation-with-hibernate-validator-jsr-303
 		// http://stackoverflow.com/questions/3503798/handling-password-confirmations-on-spring-mvc
 		if (!applicationUser.getPassword().equals(
 				applicationUser.getConfirmPassword())) {
 			result.addError(new FieldError("applicationUser",
 					"confirmPassword", "Las contraseñas no son iguales"));
 		}
 	}
 
 	@RequestMapping(value = "/passwordEdit/{id}", method = RequestMethod.POST)
 	public ModelAndView passwordEdit(@PathVariable Long id, Model model,
 			@Valid @ModelAttribute ApplicationUser applicationUser,
 			BindingResult result) {
 		ModelAndView modelAndView = new ModelAndView();
 
 		validateUserPasswords(applicationUser, result);
 
 		// TODO if we're editing and not adding a new item the message
 		// seems somewhat... misleading, CHANGE IT :D
 		if (result.hasErrors()) {
 			modelAndView.setViewName(PASSWORD_EDIT_FORM);
 		} else {
 			modelAndView.setViewName(REDIRECT_TO_USER_LIST);
 			modelAndView.addObject(USER_FORM_MESSAGE,
 					PASSWORD_FORM_EDIT_SUCCESSFUL);
 			applicationUserService.save(applicationUser);
 		}
 
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
 	public ModelAndView edit(@PathVariable Long id, Model model,
 			@Valid @ModelAttribute ApplicationUser applicationUser,
 			BindingResult result) {
 		ModelAndView modelAndView = new ModelAndView();
 
 		// TODO if we're editing and not adding a new item the message
 		// seems somewhat... misleading, CHANGE IT :D
		if (result.hasErrors() && result.getFieldErrorCount() > 2) {
 			modelAndView.setViewName(USER_EDIT_FORM);
 		} else {
 			modelAndView.setViewName(REDIRECT_TO_USER_LIST);
 			modelAndView
 					.addObject(USER_FORM_MESSAGE, USER_FORM_EDIT_SUCCESSFUL);
 			applicationUserService.save(applicationUser);
 		}
 
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/add", method = RequestMethod.POST)
 	public ModelAndView create(Model model,
 			@Valid @ModelAttribute ApplicationUser applicationUser,
 			BindingResult result) {
 		ModelAndView modelAndView = new ModelAndView();
 
 		validateUserPasswords(applicationUser, result);
 
 		// TODO if we're editing and not adding a new item the message
 		// seems somewhat... misleading, CHANGE IT :D
 		if (result.hasErrors()) {
 			modelAndView.setViewName(USER_CREATE_FORM);
 		} else {
 			modelAndView.setViewName(REDIRECT_TO_USER_LIST);
 			modelAndView.addObject(USER_FORM_MESSAGE, USER_FORM_ADD_SUCCESSFUL);
 			applicationUserService.save(applicationUser);
 		}
 
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "/passwordEdit/{id}", method = RequestMethod.GET)
 	public String getPassForm(@PathVariable Long id, Model model) {
 		ApplicationUser applicationUser = applicationUserService.findById(id);
 		model.addAttribute("applicationUser", applicationUser);
 
 		return PASSWORD_EDIT_FORM;
 	}
 
 	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
 	public String getUpdateForm(@PathVariable Long id, Model model) {
 		ApplicationUser applicationUser = applicationUserService.findById(id);
 		model.addAttribute("applicationUser", applicationUser);
 
 		return USER_EDIT_FORM;
 	}
 
 	@RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
 	public ModelAndView deleteItem(@PathVariable Long id) {
 		ModelAndView modelAndView = new ModelAndView(REDIRECT_TO_USER_LIST);
 		ApplicationUser applicationUser = applicationUserService.findById(id);
 
 		if (applicationUser == null) {
 			modelAndView.addObject(USER_FORM_MESSAGE, USER_FORM_NOT_FOUND);
 		} else {
 
 			try {
 				applicationUserService.delete(applicationUser);
 			} catch (Exception e) {
 				modelAndView.addObject(USER_FORM_MESSAGE,
 						USER_FORM_DELETE_ERROR);
 			}
 		}
 		return modelAndView;
 	}
 
 	@RequestMapping(value = "search", method = RequestMethod.GET)
 	public ModelAndView getList(
 			@ModelAttribute ApplicationUser aUser,
 			BindingResult result,
 			@RequestParam(value = "page", required = false) Integer pageNumber,
 			@RequestParam(value = "orderField", required = false) String orderField,
 			@RequestParam(value = "orderAscending", required = false) Boolean orderAscending) {
 		ModelAndView modelAndView = new ModelAndView(USER_LIST);
 
 		// set first page paginator
 		paginator.setPageIndex(1);
 
 		// filter params
 		params.setParam("username", aUser.getUsername());
 
 		if (aUser.getMyAuthorities() != null)
 			params.setParam("myAuthorities", aUser.getMyAuthorities()
 					.toString());
 
 		modelAndView = processRequest(modelAndView, aUser, pageNumber,
 				orderField, orderAscending);
 
 		return modelAndView;
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView getList(
 			@RequestParam(value = "page", required = false) Integer pageNumber,
 			@RequestParam(value = "orderField", required = false) String orderField,
 			@RequestParam(value = "orderAscending", required = false) Boolean orderAscending) {
 		ModelAndView modelAndView = new ModelAndView(USER_LIST);
 
 		modelAndView = processRequest(modelAndView, new ApplicationUser(),
 				pageNumber, orderField, orderAscending);
 
 		return modelAndView;
 	}
 
 	private ModelAndView processRequest(ModelAndView modelAndView,
 			ApplicationUser item, Integer pageNumber, String orderField,
 			Boolean orderAscending) {
 		List<ApplicationUser> ApplicationUserList = null;
 
 		// Pagination
 		if (pageNumber != null) {
 			paginator.setPageIndex(pageNumber);
 		}
 
 		// Order
 		if (orderField == null || orderAscending == null) {
 			orderField = "username";
 			orderAscending = true;
 		}
 
 		paginator.setOrderAscending(orderAscending);
 		paginator.setOrderField(orderField);
 
 		List<String> excludedFields = new ArrayList<String>();
 		excludedFields.add("accountNonExpired");
 		excludedFields.add("accountNonLocked");
 		excludedFields.add("credentialsNonExpired");
 		excludedFields.add("enabled");
 
 		ApplicationUserList = applicationUserService.findByExamplePaged(item,
 				paginator, excludedFields);
 
 		modelAndView.addObject("applicationUser", new ApplicationUser());
 		modelAndView.addObject("applicationUserList", ApplicationUserList);
 		modelAndView.addObject("paginator", paginator);
 		modelAndView.addObject("orderField", orderField);
 		modelAndView.addObject("orderAscending", orderAscending.toString());
 		modelAndView.addObject("params", params);
 
 		return modelAndView;
 	}
 
 	@InitBinder
 	public void initBinder(WebDataBinder b) {
 		b.registerCustomEditor(Authority.class, new AuthorityEditor());
 	}
 
 	private class AuthorityEditor extends PropertyEditorSupport {
 
 		@Override
 		public void setAsText(String text) throws IllegalArgumentException {
 			try {
 				setValue(authorityService.findByAuthority(text));
 			} catch (SingleResultExpectedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (NoDataFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public String getAsText() {
 			return ((Authority) getValue()).getName();
 		}
 	}
 
 }
