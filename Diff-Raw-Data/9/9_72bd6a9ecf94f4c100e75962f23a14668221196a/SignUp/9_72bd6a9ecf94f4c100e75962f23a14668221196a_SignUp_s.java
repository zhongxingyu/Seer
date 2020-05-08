 package Control;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import Control.Home;
 import Model.User;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Ben Gordon
  */
 public class SignUp extends HttpServlet {
 
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
             if (request.getParameter("fname") != null) {
                 // New Signup
                 User signup = new User();
                 signup.setFirstName(request.getParameter("fname"));
                 signup.setLastName(request.getParameter("lname"));
                 signup.setJobTitle(request.getParameter("jobtitle"));
                 signup.setOccupation(request.getParameter("occupation"));
                 signup.setEmail(request.getParameter("email"));
                 signup.setUsername(request.getParameter("username"));
                 signup.setPassword(request.getParameter("password"));
                 
                Home.users.add(signup);
                 request.setAttribute("msg","New User Added, Welcome <i>" + signup.getFirstName() + "</i>!");
             }
             RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/signup.jsp");
                 dispatcher.forward(request, response);
             
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
         return "Signup Page";
     }// </editor-fold>
 }
