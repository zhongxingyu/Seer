 package com.crowdstore.web.auth;
 
 import com.crowdstore.models.auth.Credentials;
 import com.crowdstore.models.users.AuthenticatedUser;
 import com.crowdstore.service.user.AuthenticationError;
 import com.crowdstore.service.user.UserService;
 import com.crowdstore.web.common.annotations.PublicSpace;
 import com.crowdstore.web.common.session.SessionHolder;
 import com.crowdstore.web.common.util.HttpResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.*;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * @author fcamblor
  */
 @Controller
 @RequestMapping("/auth")
 public class AuthenticationController {
 
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class);

     @Inject
     UserService userService;
 
     @PublicSpace
     @RequestMapping(value="/authenticate", method=RequestMethod.POST)
     public @ResponseBody AuthenticatedUser authenticate(HttpServletRequest request, HttpServletResponse response,
                                                         @Validated @RequestBody Credentials credentials) throws AuthenticationError {
         AuthenticatedUser authenticatedUser = userService.authenticate(credentials);
         SessionHolder.setAuthenticatedUser(request, response, authenticatedUser);
         return authenticatedUser;
     }
 
     @ExceptionHandler(AuthenticationError.class)
     @ResponseStatus(value=HttpStatus.FORBIDDEN)
     public void handleAuthenticationError(AuthenticationError authenticationError, HttpServletRequest request, HttpServletResponse response)
             throws IOException {
        LOG.info("Authentication error !");
         HttpResponses.sendJSONRedirect("/", request, response);
     }
 }
