 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hackeurope.feedspeak;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
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
 @WebServlet(name = "AuthorizeServlet", urlPatterns = {"/auth"})
 public class AuthorizeServlet extends HttpServlet {
 
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
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         User user = new User();
         //Set user session bean to be inserted to database.  Minus the twitter tokens.
         user.setName(request.getParameter("name"));
         user.setPhoneNumber(request.getParameter("phoneNumber"));
        user.setIncludeTwitter(request.getParameter("twitter") == "on" ? true : false);//Does thi give a boolean?
        user.setIncludeBBC(request.getParameter("bbc") == "on" ? true : false);
 
         request.getSession().setAttribute("user", user);
 
         if (user.isIncludeTwitter()) {
 
             try {
                 /*            ConfigurationBuilder cb = new ConfigurationBuilder();
                  cb.setDebugEnabled(true)
                  .setOAuthConsumerKey("aTR1FAEsR0hAj9w47ko9Tg")
                  .setOAuthConsumerSecret("XDSn1TTobWDBy46gZAfm6ya2kYkmli30B2vD2ixxpMA");
 
                  TwitterFactory tf = new TwitterFactory(cb.build());*/
 
                 //Twitter twitter = TwitterFactory.getSingleton();
                 ConfigurationBuilder builder = new ConfigurationBuilder();
                 builder.setOAuthConsumerKey("aTR1FAEsR0hAj9w47ko9Tg");
                 builder.setOAuthConsumerSecret("XDSn1TTobWDBy46gZAfm6ya2kYkmli30B2vD2ixxpMA");
                 Configuration configuration = builder.build();
                 TwitterFactory factory = new TwitterFactory(configuration);
                 Twitter twitter = factory.getInstance();
                 //TwitterFactory tf = new TwitterFactory();
                 //tf.setOAuthConsumer(null, null);
 
                 try {
                     //twitter.setOAuthConsumer("aTR1FAEsR0hAj9w47ko9Tg", );
                 } catch (IllegalStateException ise) {
                 }
 
                 RequestToken requestToken = twitter.getOAuthRequestToken();
                 request.getSession().setAttribute("requestToken", requestToken);
                 //twitter.getOAuthAccessToken();
                 AccessToken accessToken = null;
 
                 response.sendRedirect(requestToken.getAuthenticationURL());
 
                 //request.getRequestDispatcher(requestToken.getAuthorizationURL()).forward(request, response);
 
                 /*while (null == accessToken) {
                 
 
                  System.out.println("Open the following URL and grant access to your account:");
                  System.out.println(requestToken.getAuthorizationURL());
                  System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
 
                  try {
                  accessToken = twitter.getOAuthAccessToken();
                  } catch (TwitterException te) {
                  if (401 == te.getStatusCode()) {
                  System.out.println("Unable to get the access token.");
                  } else {
                  te.printStackTrace();
                  }
                  }
                  }*/
 
             } catch (TwitterException ex) {
                 Logger.getLogger(AuthorizeServlet.class.getName()).log(Level.SEVERE, null, ex);
             }
         }else{
             ConcreteDBConnector dbConnection = new ConcreteDBConnector();
             dbConnection.addUser(user, user.isIncludeTwitter(), user.isIncludeBBC());
         }
     }
 
     private static void storeAccessToken(int useId, AccessToken accessToken) {
         System.out.println(accessToken.getToken());
         System.out.println(accessToken.getTokenSecret());
     }
 }
