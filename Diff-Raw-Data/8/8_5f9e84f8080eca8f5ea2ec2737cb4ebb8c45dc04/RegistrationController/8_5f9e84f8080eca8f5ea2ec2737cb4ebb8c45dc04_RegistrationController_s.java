 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pl.bitethebet.controller;
 
 import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.dao.SaltSource;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import pl.bitethebet.model.AuthorityRole;
 import pl.bitethebet.model.UserAccount;
 import pl.bitethebet.repository.UserAccountRepository;
 import pl.bitethebet.validator.UserAccountValidator;
 
 /**
  *
  * @author mrowkam
  */
 @Controller
 public class RegistrationController {
 
     @Autowired
     private UserAccountRepository userAccountRepository;
     @Autowired
     private PasswordEncoder passwordEncoder;
     @Autowired
     private SaltSource saltSource;
     @Autowired
     private UserAccountValidator userAccountValidator;
 
     @InitBinder
     protected void initBinder(WebDataBinder binder) {
         binder.setValidator(userAccountValidator);
     }
 
     @RequestMapping(value = "/registerUser", method = RequestMethod.POST)
     public String registerUser(@Valid @ModelAttribute("userToRegister") UserAccount user, BindingResult result) {
         if (result.hasErrors()){
             return "register";
         }
         user.setAuthorityRole(AuthorityRole.ROLE_USER);
         user.setPassword(passwordEncoder.encodePassword(user.getPassword(), saltSource.getSalt(user)));
         userAccountRepository.create(user);
         return "redirect:index.html";
     }
 
     @RequestMapping(value = "/register")
     public ModelAndView showRegisterForm(@ModelAttribute("userToRegister") UserAccount user, BindingResult result) {
         ModelAndView mav = new ModelAndView("register");
         mav.addObject("userToRegister", user);
         return mav;
     }
}
