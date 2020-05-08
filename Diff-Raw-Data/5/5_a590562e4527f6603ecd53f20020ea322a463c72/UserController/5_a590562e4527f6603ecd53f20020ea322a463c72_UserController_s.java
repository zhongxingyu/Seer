 package com.financial.pyramid.web;
 
 import com.financial.pyramid.domain.User;
 import com.financial.pyramid.service.EmailService;
 import com.financial.pyramid.service.RegistrationService;
 import com.financial.pyramid.service.exception.SendingMailException;
 import com.financial.pyramid.service.exception.UserAlreadyExistsException;
 import com.financial.pyramid.service.validators.RegistrationFormValidator;
 import com.financial.pyramid.web.form.AuthenticationForm;
 import com.financial.pyramid.web.form.PageForm;
 import com.financial.pyramid.web.form.QueryForm;
 import com.financial.pyramid.web.form.RegistrationForm;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Danil
  * Date: 29.05.13
  * Time: 22:45
  */
 @Controller
 @RequestMapping("/user")
 public class UserController extends AbstractController {
 
     @Autowired
     private com.financial.pyramid.service.UserService userService;
 
     @Autowired
     private RegistrationService registrationService;
 
     @Autowired
     private EmailService emailService;
 
     @Autowired
     private PasswordEncoder passwordEncoder;
 
     private Validator registrationFormValidator = new RegistrationFormValidator();
 
     @RequestMapping(value = "/registration", method = RequestMethod.POST)
     public String registration(RedirectAttributes redirectAttributes, ModelMap model, @ModelAttribute("registration") final RegistrationForm registration) {
         BeanPropertyBindingResult result = new BeanPropertyBindingResult(registration, "registration");
         registrationFormValidator.validate(registration, result);
         if (result.getErrorCount() > 0) {
             model.addAttribute("registration", registration);
             model.put("errors", result.getAllErrors());
             return "/tabs/user/registration-form";
         }
         boolean success = false;
         try {
             success = registrationService.registration(registration);
         } catch (UserAlreadyExistsException e) {
             return "redirect:/pyramid/office";
         } catch (SendingMailException e) {
             e.printStackTrace();
         }
         if (!success) {
             model.addAttribute("registration", registration);
             model.put(AlertType.ERROR.getName(), getMessage("exception.serviceIsNotAvailable"));
             return "tabs/user/registration-form";
         }
         redirectAttributes.addFlashAttribute(AlertType.SUCCESS.getName(), getMessage("alert.registrationIsSuccessful"));
         return "redirect:/pyramid/office";
     }
 
     @RequestMapping(value = "/authentication", method = RequestMethod.POST)
     public String authentication(ModelMap model, @ModelAttribute("authentication") final AuthenticationForm authentication) {
         logger.info("Successfully authenticated. Security context contains: " + SecurityContextHolder.getContext().getAuthentication());
         return "/tabs/office";
     }
 
     @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
     @ResponseBody
     public PageForm list(ModelMap model, @ModelAttribute("queryForm") final QueryForm queryForm) {
         List<RegistrationForm> result = new ArrayList<RegistrationForm>();
         List<User> users = userService.findByQuery(queryForm);
         for (User user : users) {
             RegistrationForm registrationForm = new RegistrationForm();
             registrationForm.setId(user.getId().toString());
             registrationForm.setName(user.getName());
             registrationForm.setSurname(user.getSurname());
             registrationForm.setPatronymic(user.getPatronymic());
             registrationForm.setDateOfBirth(user.getDateOfBirth().toString());
             registrationForm.setPhoneNumber(user.getPhoneNumber());
             if (user.getPassport() != null) {
                 registrationForm.setPassportSerial(user.getPassport().getSerial());
                 registrationForm.setPassportNumber(user.getPassport().getNumber());
                 if (user.getPassport().getDate() != null) {
                     registrationForm.setPassportDate(user.getPassport().getDate().toString());
                 }
                 registrationForm.setPassportIssuedBy(user.getPassport().getIssuedBy());
                 registrationForm.setRegisteredAddress(user.getPassport().getRegisteredAddress());
                 registrationForm.setResidenceAddress(user.getPassport().getResidenceAddress());
             }
             result.add(registrationForm);
         }
         return new PageForm<RegistrationForm>(result);
     }
 
     @RequestMapping(value = "/add", method = RequestMethod.POST)
     public String add(@ModelAttribute("registration") final RegistrationForm registration) {
         if (registration.getId() == null || registration.getId().isEmpty()) {
             registrationService.registration(registration);
         } else {
             userService.update(registration);
         }
         return "redirect:/pyramid/admin/user_settings";
     }
 
     @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
     public String delete(@PathVariable("id") final Long id) {
         userService.delete(id);
         return "redirect:/pyramid/admin/user_settings";
     }
 
     @RequestMapping(value = "/forgot", method = RequestMethod.GET)
     public String forgot(ModelMap model) {
         return "/tabs/forgot";
     }
 
     @RequestMapping(value = "/restore", method = RequestMethod.POST)
     public String restore(ModelMap model, @RequestParam(value = "email") String email) {
         User user = userService.findByEmail(email.trim());
         boolean result = false;
         if (user != null) {
             String newPassword = userService.createPassword(15);
             user.setPassword(passwordEncoder.encode(newPassword));
             userService.save(user);
             Map map = new HashMap();
             map.put("username", user.getName());
             map.put("password", newPassword);
             emailService.setTemplate("password-restore-template");
             result = emailService.sendEmail(user, map);
         }
         model.addAttribute("result", result);
         model.addAttribute("email", email);
         return "redirect:/user/forgot";
     }
 
     @RequestMapping(value = "/settings", method = RequestMethod.GET)
     public String profile(ModelMap model) {
         User current = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
         User user = userService.findById(current.getId());
         RegistrationForm registrationForm = new RegistrationForm();
         registrationForm.setId(user.getId().toString());
         registrationForm.setName(user.getName());
         registrationForm.setSurname(user.getSurname());
         registrationForm.setPatronymic(user.getPatronymic());
         registrationForm.setDateOfBirth(user.getDateOfBirth().toString());
         registrationForm.setPhoneNumber(user.getPhoneNumber());
         if (user.getPassport() != null) {
             registrationForm.setPassportSerial(user.getPassport().getSerial());
             registrationForm.setPassportNumber(user.getPassport().getNumber());
             registrationForm.setPassportDate(user.getPassport().getDate().toString());
             registrationForm.setPassportIssuedBy(user.getPassport().getIssuedBy());
             registrationForm.setRegisteredAddress(user.getPassport().getRegisteredAddress());
             registrationForm.setResidenceAddress(user.getPassport().getResidenceAddress());
         }
         model.addAttribute("registration", registrationForm);
         return "/tabs/user/user-settings";
     }
 
     @RequestMapping(value = "/change_password", method = RequestMethod.POST)
     public String changePassword(ModelMap model,
                                  @RequestParam("new_password") String newPassword,
                                  @RequestParam("old_password") String oldPassword) {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         User current = (User) auth.getDetails();
         String password = passwordEncoder.encode(newPassword);
         User user = userService.findById(current.getId());
         if (passwordEncoder.matches(oldPassword, user.getPassword())) {
             user.setPassword(password);
             userService.save(user);
             model.addAttribute("changesSaved", true);
         } else {
             model.addAttribute("invalidPassword", true);
         }
         return "redirect:/user/settings";
     }
 
     @RequestMapping(value = "/change_email", method = RequestMethod.POST)
     public String changeEmail(ModelMap model,
                               @RequestParam("new_email") String email,
                               @RequestParam("new_email_confirm") String emailConfirmed,
                               @RequestParam("password") String password) {
         User current = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
         User user = userService.findById(current.getId());
         boolean valid = true;
        String providedPassword = passwordEncoder.encode(password);
        String oldPassword = user.getPassword();
         if (!email.equals(emailConfirmed)) {
             model.addAttribute("invalidEmail", true);
             valid = false;
         }
        if (!providedPassword.equals(oldPassword)) {
             model.addAttribute("invalidPassword", true);
             valid = false;
         }
         if (valid) {
             user.setEmail(email);
             userService.save(user);
             model.addAttribute("changesSaved", true);
         }
         return "redirect:/user/settings";
     }
 
     @RequestMapping(value = "/confirm_email", method = RequestMethod.GET)
     public String confirmEmail(ModelMap model){
         User current = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
         emailService.setTemplate("email-confirmation");
         Map map = new HashMap();
         map.put("name", current.getName());
         emailService.sendEmail(current, map);
         model.addAttribute("emailConfirmed", true);
         return "redirect:/user/settings";
     }
 
     @RequestMapping(value = "/save_profile", method = RequestMethod.POST)
     public String saveProfile(ModelMap model, @ModelAttribute("user") User user) {
         User existingUser = userService.findById(user.getId());
         existingUser.setSurname(user.getSurname());
         existingUser.setName(user.getName());
         existingUser.setPatronymic(user.getPatronymic());
         existingUser.setPhoneNumber(user.getPhoneNumber());
         userService.save(existingUser);
         model.addAttribute("changesSaved", true);
         return "redirect:/user/settings";
     }
 
     @RequestMapping(value = "/logout", method = RequestMethod.GET)
     public void logout() {
         SecurityContextHolder.clearContext();
     }
 }
