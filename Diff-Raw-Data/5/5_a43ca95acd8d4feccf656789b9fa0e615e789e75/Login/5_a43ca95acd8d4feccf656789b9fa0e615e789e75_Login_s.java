 package org.melati;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 import org.melati.poem.*;
 import org.webmacro.*;
 import org.webmacro.util.*;
 import org.webmacro.servlet.*;
 import org.webmacro.engine.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 public class Login extends MelatiServlet {
   static final String
       TRIGGERING_REQUEST_PARAMETERS =
           "org.melati.Login.triggeringRequestParameters",
       TRIGGERING_EXCEPTION =
           "org.melati.Login.triggeringException";
 
   protected Template loginTemplate() throws WebMacroException {
     return getTemplate("Login.wm");
   }
 
   protected Template usernameUnknownTemplate() throws WebMacroException {
     return getTemplate("LoginFailure.wm");
   }
 
   protected Template passwordIncorrectTemplate() throws WebMacroException {
     return getTemplate("LoginFailure.wm");
   }
 
   protected Template loginSuccessTemplate() throws WebMacroException {
     return getTemplate("LoginSuccess.wm");
   }
 
   protected Template handle(WebContext context)
       throws PoemException, WebMacroException {
 
     HttpSession session = context.getSession();
 
     AccessPoemException triggeringException =
         (AccessPoemException)session.getValue(TRIGGERING_EXCEPTION);
 
     if (triggeringException != null)
       context.put("triggeringException", triggeringException);
 
     String username = context.getForm("field-login");
     String password = context.getForm("field-password");
 
     UserTable users = PoemThread.database().getUserTable();
    context.put("login", new ColumnField(username, users.getLoginColumn()));
     context.put("password",
                new ColumnField(password, users.getPasswordColumn()));
 
     if (username == null)
       return loginTemplate();
 
     User user = (User)PoemThread.database().getUserTable().getLoginColumn().
                     firstWhereEq(username);
     if (user == null)
       return usernameUnknownTemplate();
 
     if (!user.getPassword().equals(context.getForm("field-password")))
       return passwordIncorrectTemplate();
 
     // Authenticated successfully.
 
     // Arrange for the original parameters from the request that triggered the
     // login to be overlaid on the next request that comes in if it's a match
     // (this allows POSTed fields to be recovered without converting the
     // request into a GET that the browser will repeat on reload with giving
     // any warning).
 
     HttpServletRequestParameters triggeringParams =
         (HttpServletRequestParameters)session.getValue(
             TRIGGERING_REQUEST_PARAMETERS);
 
     if (triggeringParams != null) {
       session.putValue(HttpSessionAccessHandler.OVERLAY_PARAMETERS,
 		       triggeringParams);
       session.removeValue(TRIGGERING_REQUEST_PARAMETERS);
       session.removeValue(TRIGGERING_EXCEPTION);
       context.put("continuationURL", triggeringParams.continuationURL());
     }
 
     session.putValue(HttpSessionAccessHandler.USER, user);
 
     return loginSuccessTemplate();
   }
 }
