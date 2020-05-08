 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageController;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import packageException.ChangeQuantityException;
 import packageModel.AlbumCart;
 import packageModel.Utilisateur;
 
 /**
  *
  * @author Joachim
  */
 public class ServletChangeQuantity extends HttpServlet {
 
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
 
         HttpSession sess = request.getSession();
         Utilisateur util = (Utilisateur)sess.getAttribute("user");
         
         try{
             if(util.getHasmMapPanier().isEmpty())
             {
                 throw new ChangeQuantityException("cartEmpty");                
             }
             else 
             {                
                 for(Entry<Integer, AlbumCart> entry : util.getHasmMapPanier().entrySet()) 
                 {
                     Integer idAlbum = entry.getKey();
                     try{
                         int qte = Integer.parseInt(request.getParameter("quantity" + idAlbum.toString()));     
                         if(qte < 0) 
                         {
                             throw new ChangeQuantityException("qteInvalid");
                         }
                         
                         if(qte == 0)
                         {
                             util.getHasmMapPanier().remove(idAlbum);
                         }
                         else
                         {                        
                             AlbumCart alb = entry.getValue();
                             alb.setQte(qte);
                             entry.setValue(alb);
                         }
                     }
                     catch(NumberFormatException e)
                     {
                         throw new ChangeQuantityException("qteInvalid");  
                     }
                     
                     RequestDispatcher rd = request.getRequestDispatcher("Cart");
                     rd.forward(request, response);
                 }
             }
         }
         catch(ChangeQuantityException e)
         {
            RequestDispatcher rd = request.getRequestDispatcher("cart.jsp");
             request.setAttribute("message",e);
             rd.forward(request, response);
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
