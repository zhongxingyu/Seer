 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hackeurope.feedspeak;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.http.HttpRequest;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
 import twitter4j.conf.Configuration;
 import twitter4j.conf.ConfigurationBuilder;
 
 
 /**
  *
  * @author Neil
  */
 @WebServlet(name = "TwitterAuthCallbackServlet", urlPatterns = {"/callback"})
 public class TwitterAuthCallbackServlet extends HttpServlet {
 
     private ConcreteDBConnector dbConnection;
     
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
 
 
 
         PrintWriter out = response.getWriter();
         try {
             /* TODO output your page here. You may use following sample code. */
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet TwitterAuthCallbackServlet</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet TwitterAuthCallbackServlet at " + request.getContextPath() + "</h1>");
 
             out.println("<h2>");
 
            String oauth_verifier = request.getParameter("oauth_verifier"); 
             
             ConfigurationBuilder builder = new ConfigurationBuilder();
             builder.setOAuthConsumerKey("aTR1FAEsR0hAj9w47ko9Tg");
             builder.setOAuthConsumerSecret("XDSn1TTobWDBy46gZAfm6ya2kYkmli30B2vD2ixxpMA");
             Configuration configuration = builder.build();
             TwitterFactory factory = new TwitterFactory(configuration);
             Twitter twitter = factory.getInstance();
 
             AccessToken accessToken = null;
             try {
                RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
                accessToken = twitter.getOAuthAccessToken(requestToken, oauth_verifier);
 
                 out.println(accessToken.getToken());
                 out.println(accessToken.getTokenSecret());
                 storeUserDetails(request, accessToken.getToken(), accessToken.getTokenSecret());
             } catch (TwitterException ex) {
                 out.println("error");
                 Logger.getLogger(TwitterAuthCallbackServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             out.println("</h2>");
             out.println("</body>");
             out.println("</html>");
 
         } finally {
             out.close();
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 
     private void storeUserDetails(HttpServletRequest request, String oauthToken, String oauthTokenSecret) {
         User user = (User) request.getSession().getAttribute("user");
         user.setOauthToken(oauthToken);
         user.setOauthTokenSecret(oauthTokenSecret);
         
         
         dbConnection.addUser(user, user.isIncludeTwitter(), user.isIncludeBBC());
     }
 }
