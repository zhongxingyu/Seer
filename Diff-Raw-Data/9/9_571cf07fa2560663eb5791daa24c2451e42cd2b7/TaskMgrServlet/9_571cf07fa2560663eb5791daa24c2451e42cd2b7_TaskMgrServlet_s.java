 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package TaskMgrServlet;
 
 import com.awesomedat076.task_manager_backend.EncryptPassword;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author dagf
  */
 @WebServlet(name = "TaskMgrServlet", urlPatterns = {"/TaskMgrServlet/*"})
 public class TaskMgrServlet extends HttpServlet {
 
        HttpSession session;
     
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
             throws ServletException, IOException, Exception {
         
         
         
                 session = request.getSession();
         
         try{
            // container = (ContainerNavigator) session.getAttribute("container");
             Logger.getAnonymousLogger().log(Level.INFO,"Nu är vi i try: " );
         }
         catch(Exception e){
           
            // session.setAttribute("container", container);
             Logger.getAnonymousLogger().log(Level.INFO,"Nu är vi i catch: ");
         }
                 
         String action = request.getParameter("action");
         String view = request.getParameter("view");
         
         String name = request.getParameter("name");
  
         
         Logger.getAnonymousLogger().log(Level.INFO,"ContextListener objektet är skapat med så här många items, nu är vi i ProductServlet: " );
         Logger.getAnonymousLogger().log(Level.INFO, "Action is= {0}", action);
         //Om det kom in en action
         if (action != null) {
             switch (action) {
                 case "login":
                     String username = request.getParameter("username");
                     String password = request.getParameter("password");
                     Logger.getAnonymousLogger().log(Level.INFO, "Action blev {0} username = {1} password = {2}", new Object[]{action, username, password});
                     //request.getRequestDispatcher("/index.jspx").forward(request, response);
                     if(username != null & password != null){
                         password = EncryptPassword.encryptPassword(password, username);
                         Logger.getAnonymousLogger().log(Level.INFO, "encrypted password is = {0}", password);
                         if(password.equals(EncryptPassword.encryptPassword("dag", username)))
                         {
                             //Autentication passed
                              request.setAttribute("USERNAME", username);
                              request.getRequestDispatcher("jsp/main.jspx").forward(request, response);
                         }else{
                             //Autentication failed
                            
                            session.setAttribute("subscribed",true);
                             request.getRequestDispatcher("login.jspx").forward(request, response);
                         }   
                     }else{
                         //Autentication failed, no password or no username
                         request.getRequestDispatcher("www.google.se").forward(request, response);
                     }
                 break;
                 
                 default:
                     ;
             }
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
            try {
                processRequest(request, response);
            } catch (Exception ex) {
                Logger.getLogger(TaskMgrServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
                processRequest(request, response);
            } catch (Exception ex) {
                Logger.getLogger(TaskMgrServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
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
