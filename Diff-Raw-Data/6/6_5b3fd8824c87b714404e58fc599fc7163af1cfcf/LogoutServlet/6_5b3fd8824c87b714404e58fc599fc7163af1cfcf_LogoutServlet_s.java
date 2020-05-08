 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package plantgame.controllers;
 
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import plantgame.models.User;
 import plantgame.utils.Constants;
 
 /**
  *
  * @author tyler
  */
 
 public class LogoutServlet extends HttpServlet {
 
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
           
         //TODO: go store items in dB
 
         HttpSession session = request.getSession();
         
         if (session == null){
           System.out.println("LogoutServlet: Session is null");
         }
         
         User user = (User)session.getAttribute(Constants.USER);
        //session.invalidate();
         
         request.setAttribute("Message", user.getUserName()+" you have logged out.");
         
         //DEBUG
         System.out.println(user.getUserName()+" you have logged out.");
 
         
        RequestDispatcher requestDispatcher = getServletContext().getNamedDispatcher(Constants.LOGOUT_PAGE_JSP);
         
         //DEBUG
         System.out.println("Logout.java got request dispatcher ="+requestDispatcher.toString());
         
         requestDispatcher.forward(request, response);          
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
 }
