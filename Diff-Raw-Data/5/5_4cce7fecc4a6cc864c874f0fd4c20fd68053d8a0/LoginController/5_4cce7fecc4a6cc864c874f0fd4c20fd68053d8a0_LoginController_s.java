 /*
  * Copyright (c) 2013, 2014 Lucas Holt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 
 package com.justjournal.ctl.api;
 
 import com.fasterxml.jackson.annotation.JsonAutoDetect;
 import com.fasterxml.jackson.annotation.JsonCreator;
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.justjournal.WebLogin;
 import com.justjournal.core.Login;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.Serializable;
 
 /**
  * Log user into session
  *
  * @author Lucas Holt
  */
 @Controller
 @RequestMapping("/api/login")
 public class LoginController {
 
     private static final String JJ_LOGIN_OK = "JJ.LOGIN.OK";
     private static final String JJ_LOGIN_FAIL = "JJ.LOGIN.FAIL";
     private static final String JJ_LOGIN_NONE = "JJ.LOGIN.NONE";
 
     private static final Logger log = Logger.getLogger(LoginController.class);
     @Autowired
     private WebLogin webLogin;
 
     // Response format
     @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
     public class LoginResponse implements Serializable {
        private String status;
        private String username;
 
         @JsonCreator
         public LoginResponse() {
         }
 
         public String getUsername() {
             return username;
         }
 
         public void setUsername(String username) {
             this.username = username;
         }
 
         public String getStatus() {
             return status;
         }
 
         public void setStatus(String status) {
             this.status = status;
         }
 
         @JsonIgnore
         @Override
         public String toString() {
             return "LoginResponse{" +
                     "status='" + status + '\'' +
                     ", username='" + username + '\'' +
                     '}';
         }
     }
 
     /**
      * Check the login status of the user
      *
      * @param session HttpSession
      * @return LoginResponse with login OK or NONE
      */
     @RequestMapping(method = RequestMethod.GET, headers = "Accept=*/*", produces = "application/json;charset=UTF-8")
     public
     @ResponseBody
     LoginResponse getLoginStatus(HttpSession session) {
         LoginResponse response = new LoginResponse();
         String username = (String) session.getAttribute("auth.user");
         response.setUsername(username);
         response.setStatus(username == null ? JJ_LOGIN_NONE : JJ_LOGIN_OK);
         return response;
     }
 
 
     @RequestMapping(method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8", headers = {"Accept=*/*", "content-type=application/json"})
     public
     @ResponseBody
     ResponseEntity<LoginResponse> post(@RequestBody Login login, HttpServletRequest request) {
         LoginResponse loginResponse = new LoginResponse();
 
         try {
             // Current authentication needs to get whacked
             HttpSession session = request.getSession(true);
             if (WebLogin.isAuthenticated(session)) {
                 session.invalidate();
                 session = request.getSession(true); // reset
             }
 
             int userID = webLogin.validate(login.getUsername(), login.getPassword());
             if (userID > WebLogin.BAD_USER_ID) {
                 log.debug("LoginController.post(): Username is " + login.getUsername());
                 session.setAttribute("auth.uid", userID);
                 session.setAttribute("auth.user", login.getUsername());
             } else {
                 log.error("Login attempt failed with user: " + login.getUsername());
 
                 loginResponse.setStatus(JJ_LOGIN_FAIL);
                 return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.BAD_REQUEST);
             }
 
             loginResponse.setUsername(login.getUsername());
             loginResponse.setStatus(JJ_LOGIN_OK);
             return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
         } catch (Exception e) {
             log.error(e);
             return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.BAD_REQUEST);
         }
     }
 }
