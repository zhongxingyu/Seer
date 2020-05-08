 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import model.SpielFeld;
 
 /**
  * this servlet acts as controller for the game.
  * it provides functionality for starting a new game and roll the players dice until the game has finished.
  * the model is stored as a session object.
  */
 @SuppressWarnings("serial")
 public class TableServlet extends HttpServlet {
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         HttpSession session = request.getSession(true);
         SpielFeld sp = (SpielFeld) session.getAttribute("spielFeld");
         if (sp == null) { // neues spiel / erster besuch
             sp = new SpielFeld();
             session.setAttribute("spielFeld", sp);
 
         } else {
 
             if (request.getParameter("newGame") != null) {
                 sp = new SpielFeld();
                 session.setAttribute("spielFeld", sp);
             } else if (request.getParameter("turn") != null &&  !sp.isFinished()) {
                 sp.move(diceRoll(), diceRoll());
             }
         }
 
         RequestDispatcher rd = getServletContext().getRequestDispatcher("/table.jsp");
         rd.forward(request, response);
     }
 
     private int diceRoll() {
         return (int) ((Math.random() * 100) % 3) + 1;
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
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
      * Handles the HTTP <code>POST</code> method.
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
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Formel 0 game";
     }
 }
