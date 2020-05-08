 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nz.ac.massey.cs.capstone.server;
 
 import nz.ac.massey.cs.capstone.player.Bot;
 import nz.ac.massey.cs.capstone.server.util.BotManager;
 import nz.ac.massey.cs.capstone.server.util.GameManager;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Map;
 import java.util.Set;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author luke
  */
 public class GetBots extends HttpServlet {
 
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
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         response.setContentType("text/event-stream");
         response.setCharacterEncoding ("UTF-8");
         PrintWriter out = response.getWriter();
         out.append("data: ");
         
         //get all bots
         Map<String, Bot> bots = BotManager.getAllBots();
         //get player emails
         Map<String, HttpSession> emails = SocialLogin.getEmailDetails();
         //compare emails to bots in order to find user the bot relates to
         Set botIDs = bots.keySet();
         
         String botDetails = "";
         
         for (Map.Entry<String, HttpSession> entry : emails.entrySet()) {
             String email = entry.getKey();
             HttpSession session = entry.getValue();
             email = email.replace('.', '_').replace('@', '_');
             if(botIDs.contains("C"+email)) {
                 String botsPlayerName = session.getAttribute("user").toString();
                botDetails = botDetails + botsPlayerName + ", " + email + ", ";
             }
         }
         
         out.append(botDetails);
         out.append("\n\n");
         response.flushBuffer();
     }
 
     @Override
     public String getServletInfo() {
         return "fetch bot list";
     }
 }
