 package com.incra.controllers;
 
 import java.util.ArrayList;
import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.incra.domain.User;
 import com.incra.domain.propertyEditor.UserPropertyEditor;
 import com.incra.services.LevelService;
 import com.incra.services.PageFrameworkService;
 import com.incra.services.UserService;
 import com.incra.services.dto.MyUserDetails;
 
 /**
  * The <i>UserController</i> controller defines operations on users, including
  * registration.
  * 
  * @author Jeffrey Risberg
  * @since 09/10/11
  */
 @Controller
 public class UserController implements ApplicationContextAware {
     protected static Logger logger = LoggerFactory.getLogger(UserController.class);
 
     @Autowired
     private UserService userService;
     @Autowired
     private LevelService levelService;
     @Autowired
     private PageFrameworkService pageFrameworkService;
     @Autowired
     private PasswordEncoder passwordEncoder;
 
     ApplicationContext applicationContext;
 
     public UserController() {
     }
 
     @InitBinder
     protected void initBinder(WebDataBinder dataBinder) throws Exception {
         dataBinder.registerCustomEditor(User.class, new UserPropertyEditor(userService));
     }
 
     @RequestMapping(value = "/user/**")
     public String index() {
         return "redirect:/user/list";
     }
 
     // REGISTRATION
 
     @RequestMapping(value = "/user/register", method = RequestMethod.POST)
     public String register(final @ModelAttribute("command") @Valid User user, BindingResult result,
             Model model, HttpSession session) {
 
         String password = user.getPassword();
         String confirmPassword = user.getConfirmPassword();
 
         logger.info("password=" + password + ", confirmPassword=" + confirmPassword);
 
         if (password.equals(confirmPassword)) {
             String encPassword = passwordEncoder.encodePassword(password, null);
 
             user.setPassword(encPassword);
             user.setLevel(levelService.computeLevel(user.getPoints()));
            user.setLoginCount(1);
            user.setLastLoggedIn(new Date());
             userService.save(user);
 
             // Perform Programmatic login
             List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
             authList.add(new GrantedAuthorityImpl("ROLE_USER"));
 
             int userId = user.getId();
             String fullName = user.getFirstName() + " " + user.getLastName();
             UserDetails userDetails = new MyUserDetails(user.getEmail(), password, false, true,
                     true, true, authList, userId, fullName, user.getEmail());
 
             SecurityContextHolder.getContext().setAuthentication(
                     new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(),
                             userDetails.getAuthorities()));
 
             return "redirect:/home/index";
         } else {
             pageFrameworkService.setFlashMessage(session, "Passwords must match");
             pageFrameworkService.setIsRedirect(session, Boolean.TRUE);
             return "redirect:/home/index";
         }
     }
 
     // ADMIN
 
     @RequestMapping(value = "/user/list")
     public ModelAndView list(Object criteria) {
 
         List<User> userList = userService.findEntityList();
 
         ModelAndView modelAndView = new ModelAndView("user/list");
         modelAndView.addObject("userList", userList);
         return modelAndView;
     }
 
     @RequestMapping(value = "/user/show/{id}", method = RequestMethod.GET)
     public String show(@PathVariable int id, Model model, HttpSession session) {
 
         User user = userService.findEntityById(id);
         if (user != null) {
             model.addAttribute(user);
             return "user/show";
         } else {
             pageFrameworkService.setFlashMessage(session, "No User with that id");
             pageFrameworkService.setIsRedirect(session, Boolean.TRUE);
             return "redirect:/user/list";
         }
     }
 
     @RequestMapping(value = "/user/create", method = RequestMethod.GET)
     public ModelAndView create() {
 
         User user = new User();
 
         ModelAndView modelAndView = new ModelAndView("user/create");
         modelAndView.addObject("command", user);
         return modelAndView;
     }
 
     @RequestMapping(value = "/user/edit/{id}", method = RequestMethod.GET)
     public ModelAndView edit(@PathVariable int id) {
         User user = userService.findEntityById(id);
 
         ModelAndView modelAndView = new ModelAndView("user/edit");
         modelAndView.addObject("command", user);
 
         return modelAndView;
     }
 
     @RequestMapping(value = "/user/save", method = RequestMethod.POST)
     public String save(final @ModelAttribute("command") @Valid User user, BindingResult result,
             Model model, HttpSession session) {
 
         if (result.hasErrors()) {
             return "user/edit";
         }
 
         try {
             userService.save(user);
         } catch (RuntimeException re) {
             pageFrameworkService.setFlashMessage(session, re.getMessage());
             pageFrameworkService.setIsRedirect(session, Boolean.TRUE);
             return "redirect:/user/list";
         }
         return "redirect:/user/list";
     }
 
     @RequestMapping(value = "/user/delete/{id}", method = RequestMethod.GET)
     public String delete(@PathVariable int id, HttpSession session) {
 
         User user = userService.findEntityById(id);
         if (user != null) {
             try {
                 userService.delete(user);
             } catch (RuntimeException re) {
                 pageFrameworkService.setFlashMessage(session, re.getMessage());
                 pageFrameworkService.setIsRedirect(session, Boolean.TRUE);
                 return "redirect:/user/show/" + id;
             }
         } else {
             pageFrameworkService.setFlashMessage(session, "No User with that id");
             pageFrameworkService.setIsRedirect(session, Boolean.TRUE);
         }
 
         return "redirect:/user/list";
     }
 
     @Override
     public void setApplicationContext(ApplicationContext arg0) throws BeansException {
         this.applicationContext = arg0;
     }
 }
