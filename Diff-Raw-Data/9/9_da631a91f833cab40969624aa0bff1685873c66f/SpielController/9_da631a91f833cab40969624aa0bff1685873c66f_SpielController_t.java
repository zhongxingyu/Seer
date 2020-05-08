 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ewa2.controller;
 
 import ewa2.util.SpielUtil;
 import java.io.IOException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import model.Feld;
 import model.Spiel;
 import model.Spieler;
 
 /**
  *
  * @author bernhard
  */
 public class SpielController extends HttpServlet {
 
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
         HttpSession session;
         RequestDispatcher dispatcher;
         
        // check ob eine action übergeben wurde
        if (request.getParameter("action") == null) {
            dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
         if (request.getParameter("action").equals("start")) {
             // Start a new game
             session = request.getSession(true);
             Spiel game = new Spiel();
             session.setAttribute("spiel", game);
         }
         else if (request.getParameter("action").equals("restart")) {
             // Restart an existing game
             session = request.getSession(true);
             Spiel game = new Spiel();
             session.setAttribute("spiel", game);
         }
         else if (request.getParameter("action").equals("wuerfeln")) {
             // Process a game turn
             session = request.getSession(true);
             
             // get the current game
             Spiel game = (Spiel) session.getAttribute("spiel");
             Spieler humanPlayer = game.Player.get(0);
             Spieler computerPlayer = game.Player.get(1);
             
             // wuerfeln
             int wurf = SpielUtil.wuerfle();
             
             // spielzug
             this.spielzug(game, humanPlayer, wurf);
             humanPlayer.LastDies.offer(wurf);
             
             // if wuerfel == 6, zurueck zur view, da Spieler nochmals an der Reihe ist
             if ((wurf != 6) && (!game.isOver())) {
                 do {
                     // Computer player - getWuerfel
                     wurf = SpielUtil.wuerfle();
                     
                     // spielzug Computer
                     this.spielzug(game, computerPlayer, wurf);
                 
                     // if wuerfel == 6, nochmals
                     computerPlayer.LastDies.offer(wurf);
                 } while ((wurf == 6) && (!game.isOver()));
             
                 // neue Runde - zurueck zur view
                 game.newRound();
             }
         }
         else {
             // action not recognized by controller - do nothing!
             dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
             dispatcher.forward(request, response);
            return;
         }
         // Forward to the table
         dispatcher = getServletContext().getRequestDispatcher("/table.jsp");
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
 
     private void spielzug(Spiel game, Spieler player, int wurf) {
         
         // Spielstart pruefen:
         if (game.Playarea.isPlayerInStart(player)) {
             if (wurf != 6) {
                 return;
             }
         }
         
         Feld newField = game.Playarea.getNewField(player, Integer.valueOf(wurf));
         
         // Gegner geschlagen?
         if (newField.getContent() != null) {
           // Set the other player on a start field
           game.Playarea.resetPlayer(newField.getContent());
         }
         
         // Spielzug durchfuehren
         game.Playarea.setPlayerToField(player, newField);
         
         // gewonnen?
         if (game.Playarea.isPlayerFinished(player)) {
           game.gameOver();
         }
     }
 }
