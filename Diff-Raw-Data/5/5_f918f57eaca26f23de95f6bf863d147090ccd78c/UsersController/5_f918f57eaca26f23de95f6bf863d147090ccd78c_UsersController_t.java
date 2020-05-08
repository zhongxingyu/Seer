 package ch.hszt.mdp.web;
 
 import java.security.Principal;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;
 
 import ch.hszt.mdp.domain.User;
 import ch.hszt.mdp.service.UserService;
 import ch.hszt.mdp.validation.DateTimePropertyEditor;
 import javax.servlet.http.HttpSession;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 /**
  * 
  * Controller for all request beyond "/users" The controller uses the userService to get the data from hibernate. The
  * object is injected via Spring
  * 
  * @author gaba, fabian
  * 
  */
 
 @Controller
 @RequestMapping(value = "/users")
 public class UsersController {
 
 	private UserService service;
 
 	@Autowired
 	public UsersController(UserService service) {
 		this.service = service;
 	}
 
 	/**
 	 * 
 	 * Used to register the binder for the photo upload
 	 * 
 	 * @param request
 	 * @param binder
 	 * @throws ServletException
 	 */
 	@InitBinder
 	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
 
 		binder.registerCustomEditor(DateTime.class, null, new DateTimePropertyEditor("yyyy-MM-dd"));
 
 		// Convert multipart object to byte[]
 		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
 	}
 
 	/**
 	 * Displays the register form and the User object is added to the context of the JSP.
 	 * 
 	 * @param model
 	 * @return jsp view
 	 */
 	@RequestMapping(method = RequestMethod.GET)
 	public String getRegistrationForm(Model model) {
 
 		model.addAttribute(new User());
 
 		return "users/registration";
 	}
 
 	@RequestMapping(value = "{id}", method = RequestMethod.GET)
 	public String getProfileForm(@PathVariable("id") int id, Model model, Principal principal) {
 
 		User user = service.getUser(id);
                 
 		model.addAttribute("profile", user);
 
 		return "users/profile";
 	}
         @RequestMapping(value = "{id}/e", method = RequestMethod.GET)
 	public String getProfileFormEdit(@PathVariable("id") int id, Model model, Principal principal) {
 
 		User user = service.getUser(id);
                 DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
                 String str = fmt.print(user.getBirthdate());
                 model.addAttribute("birthday", str);
 		model.addAttribute("profile", user);
 
 		return "users/editprof";
 	}
 
 	@RequestMapping(value = "{id}/image", method = RequestMethod.GET)
 	public ResponseEntity<byte[]> image(@PathVariable("id") int id, Model model, Principal principal) {
 
 		User user = service.getUser(id);
 
 		HttpHeaders responseHeaders = new HttpHeaders();
 		responseHeaders.setContentType(MediaType.parseMediaType("image/png"));
 		responseHeaders.setContentLength(user.getPhoto().length);
 
 		return new ResponseEntity<byte[]>(user.getPhoto(), responseHeaders, HttpStatus.OK);
 	}
 
 	/**
 	 * 
 	 * The post request from the registration page. If there is no error the user object will be saved to the database
 	 * 
 	 * @param user
 	 * @param result
 	 * @return the jsp view
 	 */
 	@RequestMapping(method = RequestMethod.POST)
 	public String register(@Valid User user, BindingResult result) {
 
 		if (result.hasErrors()) {
 			return "users/registration";
 		}
 
 		// create user
 		service.create(user);
 
 		return "redirect:/";
 	}
         @RequestMapping(value = "{id}/e", method = RequestMethod.POST)
 	public String update(@PathVariable("id") int id, @Valid User user, BindingResult result, Model model, Principal principal,HttpSession session) {
                 User origin = service.getUser(id);
 		if (result.hasErrors()) {
                         DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
                         String str = fmt.print(origin.getBirthdate());
                         model.addAttribute("birthday", str);
                         model.addAttribute("profile", origin);
 			return "/users/editprof";
                         
 		}
                 boolean auth =false;
                 if(user.getPassword().equals("changeit")){
                     auth=true;
                 }
                 
                 
                 user = service.updateUser(origin, user);
 		
                
                 if(auth){
                      model.addAttribute("profile", user);
                    return "redirect:/";
                 }else{
                     session.invalidate();
                    return "redirect:/auth/login";
                 }
 	}
 }
