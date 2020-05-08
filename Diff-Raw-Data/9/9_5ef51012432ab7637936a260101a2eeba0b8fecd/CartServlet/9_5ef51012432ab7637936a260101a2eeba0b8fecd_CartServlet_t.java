 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package store.servlets;
 
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Bill
  */
 public class CartServlet extends HttpServlet {
 
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
         String action = request.getParameter("action");
         Integer reqQuantity = null;
         String reqItem = null;
         try {
             reqQuantity = Integer.parseInt((String) request.getParameter("quantity"));
             reqItem = (String) request.getParameter("item");
             if (reqItem == null)
                 action = null;
         }
         catch (Exception e)
         {
             action = null;
         }
         HttpSession session = request.getSession();
 //        request.setAttribute("message", "unknown action");
         if (action != null)
         {
 //            request.setAttribute("message", "non-null action");
             order.Order order = (order.Order) session.getAttribute("order");
             if (order == null)
             {
                 order = new order.Order();
             }
             if (action.equals("add"))
             {
                 if (reqQuantity > 0)
                 {
                     Item.Item item = store.data.DB.getItem(reqItem);
                     if (item != null)
                     {
                         order.addItem(item, reqQuantity);
                         session.setAttribute("order", order);
                         request.setAttribute("message", "The item has been added to your cart.");
                     }
                 }
             } else if (action.equals("update")) {
                 if (reqQuantity >= 0)
                 {
                     Item.Item item = store.data.DB.getItem(reqItem);
                     if (item != null)
                     {
                         order.updateItem(item, reqQuantity);
                         session.setAttribute("order", order);
                         request.setAttribute("message", "Your cart has been updated.");
                     }
                 }
             }
         }
         String url = "/displaycart.jsp";
         RequestDispatcher dispatcher =
         getServletContext().getRequestDispatcher(url);
         dispatcher.forward(request, response);
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
