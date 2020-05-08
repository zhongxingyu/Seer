 package ar.edu.itba.paw.grupo1.controller;
 
 import java.io.File;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
 
 import ar.edu.itba.paw.grupo1.model.User;
 import ar.edu.itba.paw.grupo1.model.User.UserType;
 import ar.edu.itba.paw.grupo1.repository.UserRepository;
 import ar.edu.itba.paw.grupo1.repository.UserRepository.UserAlreadyExistsException;
 import ar.edu.itba.paw.grupo1.service.HashingService;
 import ar.edu.itba.paw.grupo1.web.LoginForm;
 import ar.edu.itba.paw.grupo1.web.RegisterForm;
 
 @Controller
 @RequestMapping(value="user")
 public class UserController extends BaseController {
 
 	private UserRepository userRepository;
 	
 	@Autowired
 	public UserController(UserRepository userRepository) {
 		this.userRepository = userRepository;
 	}
 	
 	
 	@RequestMapping(value="register", method = RequestMethod.GET)
 	protected ModelAndView registerGet(HttpServletRequest req) {
 		
 		ModelAndView mav = new ModelAndView();
 		if (isLoggedIn(req)) {
 			return redirect("/");
 		}
 
 		mav.addObject(new RegisterForm());
 		return render("register.jsp", "Register", mav);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value="register", method = RequestMethod.POST)
 	protected ModelAndView registerPost(HttpServletRequest req, @Valid RegisterForm form, Errors errors, @RequestParam("logo") MultipartFile logoFile) {
 		
 		ModelAndView mav = new ModelAndView();
 		if (isLoggedIn(req)) {
 			return redirect("/");
 		}
 
 		if (form.getUserType() == UserType.REAL_ESTATE) {
 
 			mav.addObject("isRealEstate", 1);
 
 			if (form.getRealEstateName() == null || form.getRealEstateName().trim().isEmpty()) {
 				mav.addObject("missingRealEstateNameError", 1);
 				return render("register.jsp", "Register", mav);
 			}
 
 			if (logoFile == null || logoFile.getOriginalFilename().trim().isEmpty()) {
 				mav.addObject("missingLogo", 1);
 				return render("register.jsp", "Register", mav);
 			}
 			
 			String name = logoFile.getOriginalFilename();
 
 			System.out.println("\n\n____\n\n"+name+"\n\n____\n\n");
 
 			if (!name.matches(".*\\.(jpg|png|jpeg|gif)$")) {
 				mav.addObject("extensionError", 1);
 				return render("register.jsp", "Register", mav);
 			}
 
 			String extension = name.substring(name.lastIndexOf('.'));
 
 			form.setLogoExtension(extension);
 		} else {
 			form.setRealEstateName(null);
 			form.setLogoExtension(null);
 		}
 
 		if (errors.hasErrors()) {
 			return render("register.jsp", "Register", mav);
 		}
 
 		User user;
 		
 		try {
 			user = userRepository.register(form.build());
 		} catch (UserAlreadyExistsException e) {
 			mav.addObject("usernameDuplicate", true);
 			return render("register.jsp", "Register", mav);
 		}
 
 		if (user == null) {
 			return render("register.jsp", "Register", mav);
 		}
 
 		if (user.getLogoExtension() != null) {
 			try {
 				logoFile.transferTo(new File(getServletContext().getRealPath("/images") + "/logo_" + user.getId() + user.getLogoExtension()));
 			} catch (Exception e) {
 				// TODO: delete user from db
 				mav.addObject("writeError", 1);
 				return render("register.jsp", "Register", mav);
 			}
 		}
 
 		return render("registerSuccess.jsp", "Register", mav);
 	}
 	
 	@RequestMapping(value = "login", method = RequestMethod.GET)
 	protected ModelAndView loginGet(HttpServletRequest req) {
 		
 		ModelAndView mav = new ModelAndView();
 		if (isLoggedIn(req)) {
 			return redirect("/");
 		}
 
 		mav.addObject("username", getRememberedName(req));
 		
 		return render("login.jsp", "Login", mav);
 	}
 	
 	@RequestMapping(value = "login", method = RequestMethod.POST)
 	protected ModelAndView loginPost(LoginForm loginForm, @RequestParam String from, 
 			HttpServletRequest req, HttpServletResponse resp) {
 		
 		ModelAndView mav = new ModelAndView();
 		if (isLoggedIn(req)) {
 			return redirect("/");
 		}
 		
 		String username = loginForm.getUsername();
 		String password = loginForm.getPassword();
 		
 		if (username != null && password != null) {
 			User user = userRepository.login(username, HashingService.hash(password));
 			
 			if (user != null) {
 				// We can log in now!
 				setLoggedInUser(req, user);
 				
 				if (loginForm.isRememberName()) {
 					rememberUsername(resp, user);
 				}
 				
 				if (loginForm.isRememberMe()) {
 					rememberUser(resp, user);
 				}
 				
 				if (from == null || from.isEmpty()) {
 					from = getServletContext().getContextPath();
 					
 					if (from.isEmpty()) {
 						// This may happen if we're the default context
 						from = "/";
 					}
 				}
 				
				return new ModelAndView(new RedirectView(from, false));
 			}
 		}
 		
 		mav.addObject("username", username);
 		mav.addObject("invalidCredentials", true);
 		
 		return render("login.jsp", "Login", mav);
 	}
 	
 	@RequestMapping(value = "logout", method = RequestMethod.GET)
 	protected ModelAndView logoutGet(HttpServletRequest req, HttpServletResponse resp) {
 		
 		logout(req, resp);
 		return redirect("/");
 	}
 }
