 package no.niths.application.rest.auth;
 
 import no.niths.application.rest.RESTConstants;
 import no.niths.application.rest.RESTExceptionHandler;
 import no.niths.application.rest.auth.interfaces.RestDeveloperAccessController;
 import no.niths.application.rest.exception.BadRequestException;
 import no.niths.domain.development.Application;
 import no.niths.domain.development.Developer;
 import no.niths.security.ApplicationToken;
 import no.niths.security.DeveloperToken;
 import no.niths.services.auth.interfaces.AuthenticationService;
 
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * 
  * Handles developers wanting access to the API
  * <p>
  * Developers must register with the API to be able to
  * develop applications that can access the restricted
  * resources in the API
  * </p>
  * <p>
  * Developers register, then confirms their identity and
  * then they can register and enable applications
  * </p>
  * 
  */
 @Controller
 @RequestMapping("register")
 public class RestDeveloperAccessControllerImpl extends RESTExceptionHandler
         implements RestDeveloperAccessController {
 
     Logger logger = org.slf4j.LoggerFactory
             .getLogger(RestDeveloperAccessControllerImpl.class);
 
     @Autowired
     private AuthenticationService service;
 
     private final static String VIEW_NAME = "developerConfirmation";
 
     /**
      * Register a developer and generates a developer token that the developer
      * uses in future requests
      * <p>
      * 
      * <pre>
      * {@code
      * How to use:
      * POST: niths/register/
      * 
      * Header:
      * Content-type: application/xml
      * Accept: application/xml || application/json
      * 
      * Body:
      * <developer>
      * <email>youremail@mail.com</email>
      * <name>Your developer name</name>
      * </developer>
      * }
      * </pre>
      * 
      * @param developer
      *            the developer to persist
      * @return DeveloperToken an object containing the developer key and a
      *         confirmation message
      */
     @Override
     @RequestMapping(
             method  = RequestMethod.POST,
             headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public DeveloperToken requestAccess(@RequestBody Developer developer) {
         logger.debug("A developer requests access. Email: "
                 + developer.getEmail());
         if (developer.getApps() != null) {
             throw new BadRequestException(
                     "You must register as a developer before registering apps");
         }
 
         DeveloperToken devToken = service.registerDeveloper(developer);
         logger.debug("Request success, sending email");
         return devToken;
     }
 
     /**
      * Enables already registred developers. Returns a new developer token to
      * use in all future requests
      * <p>
      * How to use: Paste the url to the server +
      * /niths/register/enableDeveloper/<your_key> into your favourite browser
      * <p>
      * 
      * @param developerKey
      *            the token returned from requestAccess(Developer)
      * @return a page with confirmation or error message
      */
     @Override
     @RequestMapping(
            value  = "enableDeveloper/{developerKey:.+}",
             method = RequestMethod.GET)
     public ModelAndView enableDeveloper(@PathVariable String developerKey) {
         logger.debug("Developer wants to be enabled with developer-key: "
                 + developerKey);
         ModelAndView view = new ModelAndView(VIEW_NAME);
         try {
             Developer dev = service.enableDeveloper(developerKey);
 
             view.addObject("msg", "Your developer account is enabled!");
 
             // Returns a view with the new token
             view.addObject("token",
                     "Developer-token: " + dev.getDeveloperToken());
             view.addObject("key", "Developer-key: " + dev.getDeveloperKey());
 
         } catch (AuthenticationException e) {
             view.addObject("error", e.getMessage());
         }
 
         return view;
     }
 
     /**
      * Registers an application
      * <p>
      * Developer must have been authorized for a successful request
      * <p>
      * 
      * @param app
      *            the application to add
      * @param developerKey
      *            the developer key
      * @return an application key to use to enable the application
      * 
      */
     @Override
     @RequestMapping(
            value  = "addApp/{developerKey}",
             method = RequestMethod.POST, headers = RESTConstants.ACCEPT_HEADER)
     @ResponseBody
     public ApplicationToken addApplicationToDeveloper(
             @RequestBody Application app, @PathVariable String developerKey) {
         logger.debug("Developer wants to registrate an application");
         ApplicationToken token = new ApplicationToken("Could not register app");
 
         token = service.registerApplication(app, developerKey);
         return token;
     }
 
     /**
      * Enables an application
      * 
      * @param applicationKey
      * @return a view with confirmation
      */
     @Override
     @RequestMapping(
            value  = "enableApp/{applicationKey:.+}",
             method = RequestMethod.GET)
     public ModelAndView enableApplication(@PathVariable String applicationKey) {
         logger.debug("Application wants to be enabled with application-key: "
                 + applicationKey);
         ModelAndView view = new ModelAndView(VIEW_NAME);
 
         try {
             Application app = service.enableApplication(applicationKey);
             view.addObject("msg", "Your app is enabled!");
 
             // Returns a view with the new token
             view.addObject("token",
                     "Application-token: " + app.getApplicationToken());
             view.addObject("key", "Application-key: " + app.getApplicationKey());
 
         } catch (AuthenticationException e) {
             view.addObject("error", e.getMessage());
         }
 
         return view;
     }
 }
