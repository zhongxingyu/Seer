 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageController;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import packageBusiness.Business;
 /**
  *
  * @author Emilien
  */
 public class ServletInscription extends HttpServlet {
 
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
         
         Business business = new Business();
         
         String nom = request.getParameter("nom");
         String prenom = request.getParameter("prenom");
         String rue = request.getParameter("rue");
         String numero = request.getParameter("numero");
         String boite = request.getParameter("boite");
         String localite = request.getParameter("localite");
         String codepostal = request.getParameter("codepostal");
         String mail = request.getParameter("mail");
         String pw = request.getParameter("pw");
         String pwConf = request.getParameter("pwconfirm");
         String numTel = request.getParameter("tel");
         
         try
         {
             business.ajoutUtilisateur(nom, prenom, rue, numero, boite, localite, codepostal, mail, pw,pwConf,numTel);
             RequestDispatcher redirect = request.getRequestDispatcher("inscriptionReussie.jsp");
             request.setAttribute("reponse", "Félicitation, votre compte a été correctement créé.");
             redirect.forward(request, response);
         }
         catch(Exception ex)
         {
            RequestDispatcher redirect = request.getRequestDispatcher("error.jsp");
             request.setAttribute("reponse",ex);
             redirect.forward(request, response);
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
 }
