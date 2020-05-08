 package ru.hh.school.example.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import ru.hh.school.example.Logger;
 import ru.hh.school.example.exceptions.login.LoginException;
 import ru.hh.school.example.exceptions.mail.EmailAlreadyBoundException;
 import ru.hh.school.example.exceptions.mail.InvalidEmailException;
 
 @Controller
 @RequestMapping(value = "/users")
 public class UserController {
 
   private final UserFacade userFacade;
   private final Logger logger = new Logger(this);
     
   void log(String s) {
     System.out.println(s);
   }
 
   @Autowired
   public UserController(UserFacade userFacade) {
     log("UserController");
     this.userFacade = userFacade;
   }
 
   @RequestMapping(method = RequestMethod.GET)
   public String list(Model model) {
     logger.log("list");
     model.addAttribute("users", userFacade.listUsers());
     return "listUsers";
   }
 
   @RequestMapping(value = "register", method = RequestMethod.GET)
   public String create(Model model) {
     logger.log("register");
     model.addAttribute("userFormRegister", new UserFormRegister());
     return "register";
   }
 
   @RequestMapping(value = "login", method = RequestMethod.GET)
   public String login(Model model) {
     logger.log("login");
     model.addAttribute("userFormLogin", new UserFormLogin());
     return "login";
   }
 
   @RequestMapping(value = "/register", method = RequestMethod.POST)
   public String doCreate(Model model, @ModelAttribute("userFormRegister") UserFormRegister userFormRegister) {
     logger.log("doCreate");
     try {
       userFacade.registerUser(userFormRegister.getEmail(),
                               userFormRegister.getPassword(),
                               userFormRegister.getFullName());
     } catch (InvalidEmailException e) {
      model.addAttribute("var", "<a href=\"register\">Register</a>");
       model.addAttribute("error", "Email is not valid: " + e.getEmail());
       return "error";
     } catch (EmailAlreadyBoundException e) {
       model.addAttribute("error", "Email already bound: " + e.getEmail());
      model.addAttribute("var", "<a href=\"register\">Register</a>");
       return "error";
     }
     return "redirect:/users";
   }
 
   @RequestMapping(value = "/login", method = RequestMethod.POST)
   public String doLogin(Model model, @ModelAttribute("userFormLogin") UserFormLogin userFormLogin) {
     logger.log("doLogin");
     try {
       userFacade.loginUser(userFormLogin.getEmail(), userFormLogin.getPassword());
     } catch (LoginException e) {
       model.addAttribute("error", "The username or password you entered is incorrect.");
      model.addAttribute("var", "<a href=\"login\">Login</a>");
       return "error";
     }
     model.addAttribute("userFormLogin", new UserFormLogin());
     return "redirect:/users";
   }
 }
