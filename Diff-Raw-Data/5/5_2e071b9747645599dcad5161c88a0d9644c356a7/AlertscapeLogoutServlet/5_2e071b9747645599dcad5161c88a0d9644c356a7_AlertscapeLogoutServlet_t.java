 /**
  * 
  */
 package com.alertscape.web.ui.auth;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author josh
  * 
  */
 public class AlertscapeLogoutServlet extends HttpServlet {
 
   private static final long serialVersionUID = -1236258882886193313L;
 
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     logout(req, resp);
   }
 
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     logout(req, resp);
   }
 
   /**
    * @param req
    * @param resp
    * @throws IOException
    * @throws ServletException
    */
   private void logout(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     req.getSession().removeAttribute("authUser");
    resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath() + "/login"));
   }
 
 }
