 package com.mymed.controller.core.requesthandler;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import ch.qos.logback.classic.Logger;
 
 import com.google.gson.Gson;
import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.session.SessionManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessage;
 import com.mymed.utils.MLogger;
 
 public abstract class AbstractRequestHandler extends HttpServlet {
   /* --------------------------------------------------------- */
   /* Attributes */
   /* --------------------------------------------------------- */
   private static final long serialVersionUID = 1L;
 
   private static final String ENCODING = "UTF-8";
 
   // The default logger for all the RequestHandler that extends this class
   protected static final Logger LOGGER = MLogger.getLogger();
 
   /** Google library to handle jSon request */
   private Gson gson;
 
   /** The response/feedback printed */
   private String responseText = null;
 
   /** Request code Map */
   protected Map<String, RequestCode> requestCodeMap = new HashMap<String, RequestCode>();
 
   /** Request codes */
   protected enum RequestCode {
     // C.R.U.D
     CREATE("0"),
     READ("1"),
     UPDATE("2"),
     DELETE("3");
 
     public final String code;
 
     RequestCode(final String code) {
       this.code = code;
     }
   }
 
   /* --------------------------------------------------------- */
   /* Constructors */
   /* --------------------------------------------------------- */
   protected AbstractRequestHandler() {
     super();
 
     gson = new Gson();
     for (final RequestCode r : RequestCode.values()) {
       requestCodeMap.put(r.code, r);
     }
   }
 
   /* --------------------------------------------------------- */
   /* protected methods */
   /* --------------------------------------------------------- */
   /**
    * @return the parameters of an HttpServletRequest
    */
  protected Map<String, String> getParameters(final HttpServletRequest request) throws AbstractMymedException {
 
     // see multipart/form-data Request
     if (request.getContentType() != null) {
       try {
         if (request.getContentType().matches("multipart/form-data")) {
           LOGGER.info("multipart/form-data REQUEST");
           for (final Part part : request.getParts()) {
             LOGGER.info("PART {} ", part);
           }
           throw new InternalBackEndException("multi-part is not yet implemented...");
         }
       } catch (final IOException e) {
         throw new InternalBackEndException(e);
       } catch (final ServletException e) {
         throw new InternalBackEndException(e);
       }
     }
 
     final Map<String, String> parameters = new HashMap<String, String>();
     final Enumeration<String> paramNames = request.getParameterNames();
 
     while (paramNames.hasMoreElements()) {
       final String paramName = paramNames.nextElement();
       final String[] paramValues = request.getParameterValues(paramName);
 
       // all the parameter should be atomic
       if (paramValues.length >= 1) {
         try {
           /*
            * Since this comes in like an HTTP request, we might have non ASCII
            * chars in the parameters, so it is better to decode them. If there
            * are only ASCII char, it is safe since ASCII < UTF-8.
            */
           final String value = URLDecoder.decode(paramValues[0], ENCODING);
           parameters.put(paramName, value);
 
           LOGGER.info("{}: {}", paramName, value);
         } catch (final UnsupportedEncodingException ex) {
           LOGGER.debug("Error decoding string from '{}'", ENCODING, ex.getCause());
         }
       }
     }
 
     if (!parameters.containsKey("code")) {
       throw new InternalBackEndException("code argument is missing!");
     }
 
     if (requestCodeMap.get(parameters.get("code")) == null) {
       throw new InternalBackEndException("code argument is not well formated");
     }
 
     return parameters;
   }
   /**
    * Print the server response in a jSon format
    * 
    * @param message
    * @param response
    */
   protected void printJSonResponse(final JsonMessage message, final HttpServletResponse response) {
     response.setStatus(message.getStatus());
     responseText = message.toString();
     printResponse(response);
   }
 
   /**
    * Print the server response
    * 
    * @param response
    * @throws IOException
    */
   private void printResponse(final HttpServletResponse response) {
     /** Init response */
     if (responseText != null) {
       response.setContentType("text/plain;charset=UTF-8");
       /** send the response */
       PrintWriter out;
       try {
         out = response.getWriter();
 
         LOGGER.info("Response sent:\n {}", responseText);
 
         out.print(responseText);
         out.close();
         responseText = null; // NOPMD to avoid code check warnings
       } catch (final IOException e) {
         LOGGER.info("IOException: {}", e.getMessage());
         LOGGER.debug("Error in printResponse()", e.getCause());
       }
     }
   }
 
   /**
    * Validate an accesstoken
    * 
    * @param accesstoken
    * @throws InternalBackEndException
    */
   protected void tokenValidation(final String accessToken) throws InternalBackEndException, IOBackEndException {
     new SessionManager().read(accessToken);
   }
 
   /* --------------------------------------------------------- */
   /* GETTER&SETTER */
   /* --------------------------------------------------------- */
   public Gson getGson() {
     return gson;
   }
 
   public void setGson(final Gson gson) {
     this.gson = gson;
   }
 
   public String getResponseText() {
     return responseText;
   }
 }
