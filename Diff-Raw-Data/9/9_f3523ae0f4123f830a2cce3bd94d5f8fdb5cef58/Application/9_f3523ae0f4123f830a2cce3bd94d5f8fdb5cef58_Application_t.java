 /*
 *  Nokia Data Gathering
 *
 *  Copyright (C) 2011 Nokia Corporation
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/
 */
 
 package controllers;
 
 import models.NdgUser;
 import play.i18n.Lang;
 import play.libs.Crypto;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 import notifiers.securesocial.Mails;
 import play.Logger;
 import play.data.validation.Email;
 import play.data.validation.Equals;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.Router;
 import securesocial.provider.*;
 
 public class Application extends Controller {
 
     private static final String USER_NAME = "userName";
     private static final String SECURESOCIAL_USER_NAME_TAKEN = "securesocial.userNameTaken";
     private static final String SECURESOCIAL_ERROR_CREATING_ACCOUNT = "securesocial.errorCreatingAccount";
     private static final String SECURESOCIAL_ACCOUNT_CREATED = "securesocial.accountCreated";
     private static final String SECURESOCIAL_ACTIVATION_TITLE = "securesocial.activationTitle";
     private static final String SECURESOCIAL_SECURE_SOCIAL_NOTICE_PAGE_HTML = "securesocial/SecureSocial/noticePage.html";
     private static final String DISPLAY_NAME = "displayName";
     private static final String EMAIL = "email";
     private static final String SECURESOCIAL_INVALID_LINK = "securesocial.invalidLink";
     private static final String SECURESOCIAL_ACTIVATION_SUCCESS = "securesocial.activationSuccess";
     private static final String SECURESOCIAL_SECURE_SOCIAL_LOGIN = "securesocial.SecureSocial.login";
     private static final String SECURESOCIAL_ACTIVATE_TITLE = "securesocial.activateTitle";
 
     @Before(unless={"login", "authorize", "logout", "SurveyManager.upload", "createAccount", "activate"})
     public static void checkAccess() throws Throwable {
         if(!session.contains("ndgUser")) {
             login( null );
         }
     }
 
     public static void index() {
         render("Application/index.html");
     }
 
     public static void login( String lang ) {
         if( lang != null && !lang.isEmpty() ) {
             Lang.change( lang );
         }
         flash.keep("url");
         render("Application/login.html");
     }
 
     public static void authorize( String username, String pass, String lang ) {
         if( lang != null && !lang.isEmpty() ) {
             Lang.change( lang );
         }
 
         NdgUser currentUser = NdgUser.find("byUsernameAndPassword", username, Crypto.passwordHash(pass) ).first();
 
         if(currentUser != null && checkPermission(currentUser) && currentUser.userValidated == 'Y') {
             session.put("ndgUser", username);
             index();
         } else {
             flash.put("error", "wrong username / password");
             render("Application/login.html");
         }
     }
 
     public static void logout() {
         session.remove("ndgUser");
         session.clear();
         flash.put("url", "/");
         login(null);
     }
 
     private static boolean checkPermission(NdgUser user) {
         boolean retval = false;
         if(user.hasRole("Operator")) {
             session.put("operator", true);
             retval = true;
         }
         if(user.hasRole("Admin")) {
             session.put("admin", true);
             retval = true;
         }
         return retval;
     }
 
     /**
      * Creates an account
      *
      * @param userName      The username
      * @param displayName   The user's full name
      * @param email         The email
      * @param password      The password
      * @param password2     The password verification
      */
     public static void createAccount(@Required(message = "securesocial.required") String userName,
                                      @Required String firstName,
                                      @Required String lastName,
                                      @Required @Email(message = "securesocial.invalidEmail") String email,
                                      @Required String password,
                                      @Required @Equals(message = "securesocial.passwordsMustMatch", value = "password") String password2,
                                      @Required String phoneNumber,
                                      @Required String company) {
         UserId id = new UserId();
         id.id = userName;
 
         if ( NDGPersister.find(id) != null ) {
             validation.addError(USER_NAME, Messages.get(SECURESOCIAL_USER_NAME_TAKEN));
            tryAgain(Messages.get(SECURESOCIAL_USER_NAME_TAKEN), firstName, email);
         }
         SocialUser user = new SocialUser();
         user.id = id;
         user.firstName = firstName;
         user.lastName = lastName;
         user.email = email;
         user.phoneNumber = phoneNumber;
         user.company = company;
         user.password = Crypto.passwordHash(password);
         // the user will remain inactive until the email verification is done.
         user.isEmailVerified = false;
 
         try {
             NDGPersister.save(user);
         } catch ( Throwable e ) {
             Logger.error(e, "Error while invoking NDGPersister.save()");
             flash.error(Messages.get(SECURESOCIAL_ERROR_CREATING_ACCOUNT));
             tryAgain(userName, firstName, email);
         }
 
         // create an activation id
         final String uuid = NDGPersister.createActivation(user);
         Mails.sendActivationEmail(user, uuid);
         flash.success(Messages.get(SECURESOCIAL_ACCOUNT_CREATED));
         final String title = Messages.get(SECURESOCIAL_ACTIVATION_TITLE, user.firstName + " " + user.lastName);
        render("Application/login.html");
     }
 
     private static void tryAgain(String username, String displayName, String email) {
        System.out.println("no try again");
         flash.put(USER_NAME, username);
         flash.put(DISPLAY_NAME, displayName);
         flash.put(EMAIL, email);
         render("Application/login.html");
     }
 
     /**
      * The action invoked from the activation email the user receives after signing up.
      *
      * @param uuid The activation id
      */
     public static void activate(String uuid) {
         try {
             if ( NDGPersister.activate(uuid) == false ) {
                 flash.error( Messages.get(SECURESOCIAL_INVALID_LINK) );
             } else {
 //                flash.success(Messages.get(SECURESOCIAL_ACTIVATION_SUCCESS, Router.reverse(SECURESOCIAL_SECURE_SOCIAL_LOGIN)));
                 flash.success(Messages.get(SECURESOCIAL_ACTIVATION_SUCCESS, Router.reverse("Application.login")));
             }
         } catch ( Throwable t) {
             Logger.error(t, "Error while activating account");
             flash.error(Messages.get(SECURESOCIAL_ERROR_CREATING_ACCOUNT));
         }
         final String title = Messages.get(SECURESOCIAL_ACTIVATE_TITLE);
         render(SECURESOCIAL_SECURE_SOCIAL_NOTICE_PAGE_HTML, title);
     }
 }
