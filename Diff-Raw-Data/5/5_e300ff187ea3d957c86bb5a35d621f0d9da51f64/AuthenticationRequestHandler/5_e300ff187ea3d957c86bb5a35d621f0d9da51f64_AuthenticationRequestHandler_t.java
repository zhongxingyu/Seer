 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.authentication.AuthenticationManager;
 import com.mymed.controller.core.manager.authentication.IAuthenticationManager;
 import com.mymed.controller.core.manager.profile.IProfileManager;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.manager.session.ISessionManager;
 import com.mymed.controller.core.manager.session.SessionManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.model.data.session.MAuthenticationBean;
 import com.mymed.model.data.session.MSessionBean;
 import com.mymed.model.data.user.MUserBean;
 import com.mymed.utils.MLogger;
 
 import edu.lognet.core.tools.HashFunction;
 
 /**
  * Servlet implementation class AuthenticationRequestHandler
  */
 public class AuthenticationRequestHandler extends AbstractRequestHandler {
   /* --------------------------------------------------------- */
   /* Attributes */
   /* --------------------------------------------------------- */
   private static final long serialVersionUID = 1L;
 
   private IAuthenticationManager authenticationManager;
   private ISessionManager sessionManager;
   private IProfileManager profileManager;
 
   /* --------------------------------------------------------- */
   /* Constructors */
   /* --------------------------------------------------------- */
   /**
    * @see HttpServlet#HttpServlet()
    */
   public AuthenticationRequestHandler() throws ServletException {
     super();
 
     try {
       authenticationManager = new AuthenticationManager();
       sessionManager = new SessionManager();
       profileManager = new ProfileManager();
     } catch (final InternalBackEndException e) {
       throw new ServletException("AuthenticationManager is not accessible because: " + e.getMessage());
     }
   }
 
   /* --------------------------------------------------------- */
   /* extends AbstractRequestHandler */
   /* --------------------------------------------------------- */
   @Override
   public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
       IOException {
 
     final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
     try {
       final Map<String, String> parameters = getParameters(request);
       final RequestCode code = requestCodeMap.get(parameters.get("code"));
       final String login = parameters.get("login");
       final String password = parameters.get("password");
 
       switch (code) {
         case READ :
           message.setMethod("READ");
           if (login == null) {
            throw new InternalBackEndException("Argument 'login' is missing!");
           } else if (password == null) {
            throw new InternalBackEndException("Argument 'password' is missing!");
           } else {
             message.addData("warning", "METHOD DEPRECATED - Post method should be used instead of Get!");
             final MUserBean userBean = authenticationManager.read(login, password);
             message.setDescription("Successfully authenticated");
             message.addData("user", getGson().toJson(userBean));
           }
           break;
         case DELETE :
           throw new InternalBackEndException("not implemented yet...");
         default :
           throw new InternalBackEndException("AuthenticationRequestHandler(" + code + ") not exist!");
       }
     } catch (final AbstractMymedException e) {
       e.printStackTrace();
       MLogger.info("Error in doGet operation");
       MLogger.debug("Error in doGet operation", e.getCause());
       message.setStatus(e.getStatus());
       message.setDescription(e.getMessage());
     }
 
     printJSonResponse(message, response);
   }
 
   @Override
   public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
       IOException {
 
     final JsonMessage message = new JsonMessage(200, this.getClass().getName());
 
     try {
       final Map<String, String> parameters = getParameters(request);
       final RequestCode code = requestCodeMap.get(parameters.get("code"));
       final String authentication = parameters.get("authentication");
       final String user = parameters.get("user");
       final String login = parameters.get("login");
       final String password = parameters.get("password");
       final String id = parameters.get("id");
 
       switch (code) {
         case CREATE :
           message.setMethod("CREATE");
           if (authentication == null) {
             throw new InternalBackEndException("authentication argument missing!");
           } else if (user == null) {
             throw new InternalBackEndException("user argument missing!");
           } else {
             try {
               MUserBean userBean = getGson().fromJson(user, MUserBean.class);
               userBean.setSocialNetworkID("MYMED");
               userBean.setSocialNetworkName("myMed");
 
               final MAuthenticationBean authenticationBean = getGson().fromJson(authentication,
                   MAuthenticationBean.class);
 
               MLogger.info("Trying to create a new user:\n {}", userBean.toString());
               userBean = authenticationManager.create(userBean, authenticationBean);
 
               MLogger.info("User created");
               message.setDescription("User created");
               message.addData("user", getGson().toJson(userBean));
             } catch (final JsonSyntaxException e) {
               throw new InternalBackEndException("User/Authentication jSon format is not valid");
             }
           }
           break;
         case READ :
           message.setMethod("READ");
           if (login == null) {
             throw new InternalBackEndException("login argument missing!");
           } else if (password == null) {
             throw new InternalBackEndException("password argument missing!");
           } else {
             final MUserBean userBean = authenticationManager.read(login, password);
             message.setDescription("Successfully authenticated");
             // TODO Remove this parameter
             message.addData("user", getGson().toJson(userBean));
             // Create a new session
             final MSessionBean sessionBean = new MSessionBean();
             sessionBean.setIp(request.getRemoteAddr());
             sessionBean.setUser(userBean.getId());
             sessionBean.setCurrentApplications("");
             sessionBean.setP2P(false);
             // TODO Use The Cassandra Timeout mechanism
             sessionBean.setTimeout(System.currentTimeMillis());
             final HashFunction h = new HashFunction("myMed");
             final String accessToken = h.SHA1ToString(login + password + sessionBean.getTimeout());
             sessionBean.setAccessToken(accessToken);
             sessionBean.setId(accessToken);
             sessionManager.create(sessionBean);
 
             // Update the profile with the new session
             userBean.setSession(accessToken);
             profileManager.update(userBean);
 
             MLogger.info("Session {} created -> LOGIN", accessToken);
             // TODO Find a better way to get the URL
             message.addData("url", "http://" + InetAddress.getLocalHost().getHostAddress() + "/mobile");
             message.addData("accessToken", accessToken);
           }
           break;
         case UPDATE :
           if (id == null) {
             throw new InternalBackEndException("Missing id argument!");
           } else if (authentication == null) {
             throw new InternalBackEndException("Missing authentication argument!");
           } else {
             try {
               final MAuthenticationBean authenticationBean = getGson().fromJson(authentication,
                   MAuthenticationBean.class);
 
               MLogger.info("Trying to update authentication:\n {}", authenticationBean.toString());
               authenticationManager.update(id, authenticationBean);
               MLogger.info("Authentication updated!");
 
             } catch (final JsonSyntaxException e) {
               throw new InternalBackEndException("Authentication jSon format is not valid");
             }
           }
           break;
         default :
           throw new InternalBackEndException("AuthenticationRequestHandler(" + code + ") not exist!");
       }
     } catch (final AbstractMymedException e) {
       e.printStackTrace();
       MLogger.info("Error in doPost operation");
       MLogger.debug("Error in doPost operation", e.getCause());
       message.setStatus(e.getStatus());
       message.setDescription(e.getMessage());
     }
 
     printJSonResponse(message, response);
   }
 }
