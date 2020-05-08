 package com.tsekhan.rssreader.web;
 
 import com.tsekhan.rssreader.dao.InvalidAccountDataException;
 import com.tsekhan.rssreader.dao.NonexistentAccountException;
 import com.tsekhan.rssreader.web.services.AdminService;
 import java.security.Principal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Handles requests directed to the admin page.
  * @author Mikola Tsekhan <tsekhan@gmail.com>
  */
 @Controller
 @RequestMapping ("/admin")
@Secured("ROLE_ADMIN")
 public class AdminController {
     
     /**
      * Service for this controller.
      */
     @Autowired
     private AdminService adminService;
     
     private static final Logger logger = Logger
             .getLogger(AdminController.class.getName());
 
     /**
      * Rendering admin page.
      * @param model
      * @param principal 
      */
     @RequestMapping
     public void renderPage(Model model, Principal principal) {
         model.addAttribute("users", adminService.listAccounts());
     }
     
     /**
      * Changes name of specified account.
      * @param login account login.
      * @param name new name.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If changing was
      * success, parameter {@code message} will contain string with new name.
      */
     @RequestMapping(method = RequestMethod.POST,
             params = {"change=name", "user", "value"})
     public @ResponseBody Response modifyName(
             @RequestParam("user") String login,
             @RequestParam("value") String name) {
         Response response = new Response();
         try {
             adminService.modifyName(login, name);
             response.setMessage(name);
         } catch (NonexistentAccountException | InvalidAccountDataException ex) {
             response.addError(ex.getClass().getSimpleName());
             logger.log(Level.WARNING, "Cannot modify name ('" + name
                     + "') for '" + login + "'.", ex);
         }
         return response;
     }
     
     /**
      * Changes email of specified account.
      * @param login account login.
      * @param email new email.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If changing was
      * success, parameter {@code message} will contain string with new email.
      */
     @RequestMapping(method = RequestMethod.POST,
             params = {"change=email", "user", "value"})
     public @ResponseBody Response modifyEmail(
             @RequestParam("user") String login,
             @RequestParam("value") String email) {
         Response response = new Response();
         try {
             adminService.modifyEmail(login, email);
             response.setMessage(email);
         } catch (NonexistentAccountException | InvalidAccountDataException ex) {
             response.addError(ex.getClass().getSimpleName());
             logger.log(Level.WARNING, "Cannot modify email ('" + email
                     + "') for '" + login + "'.", ex);
         }
         return response;
     }
     
     /**
      * Changes user password to random.
      * @param login account login.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If user exists, parameter
      * {@code message} will contains new password.
      */
     @RequestMapping(method = RequestMethod.POST,
             params={"change=password", "user"})
     public @ResponseBody Response resetPassword(
             @RequestParam("user") String login) {
         Response response = new Response();
         try {
             String generatedPassword = adminService.resetPassword(login);
             response.setMessage(generatedPassword);
         } catch (NonexistentAccountException ex) {
             response.addError(ex.getClass().getSimpleName());
             logger.log(Level.WARNING,
                     "Cannot reset password of '" + login + "'.",ex);
         }
         return response;
     }
     
     /**
      * Removes user account.
      * @param login account login.
      * @return Returns JSON-packed class
      * {@link com.tsekhan.rssreader.web.Response}. If removing was success,
      * parameter {@code message} will be {@code boolean}, and will be set to
      * {@code true}, if no&nbsp;&mdash; to {@code false}.
      */
     @RequestMapping(method = RequestMethod.POST,
             params={"change=delete", "user"})
     public @ResponseBody Response removeAccount(
             @RequestParam("user") String login) {
         Response response = new Response();
         try {
             adminService.removeUser(login);
             response.setMessage(true);
         } catch (NonexistentAccountException ex) {
             response.addError(ex.getClass().getSimpleName());
             logger.log(Level.WARNING,
                     "Trying to remove noexistent account.",ex);
         }
         return response;
     }
     
 }
