 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.Properties;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author Max
  */
 public class PlayerJoin extends HttpServlet {
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String userName = request.getParameter("userName");
         String password = request.getParameter("password");
         
         try {
         Properties prop = new Properties();
         prop.load(new FileInputStream(System.getProperty("user.home") + "/mydb.cfg"));
         String dbhost = prop.getProperty("host").toString();
         String dbusername = prop.getProperty("username").toString();
         String dbpassword = prop.getProperty("password").toString();
         String dbdriver = prop.getProperty("driver").toString();
         
         Class.forName(dbdriver);
         java.sql.Connection con = DriverManager.getConnection(dbhost, dbusername, dbpassword);
         Statement st = con.createStatement();
         
         ResultSet rs = st.executeQuery("SELECT * FROM players WHERE user ='"+userName+"'");
         if(rs.next()) {
             if(rs.getString(3).equals(password)) {
                 GameManager.newPlayer(request.getSession(), userName);
             } else {
                 String message = "User name or password don't match";
                 response.sendRedirect("index.jsp?error="+message);
             }
         } else {
             String message = "User name or password don't match";
             response.sendRedirect("index.jsp?error="+message);
         }
         st.close();
     }
     catch (Exception e) {
         e.printStackTrace();
         String message = "There was a problem logging you in";
         response.sendRedirect("index.jsp?error="+message);
     }
         
         
         
         // forward player to game lobby
         this.getServletContext().getRequestDispatcher("/lobby.jsp").forward(request, response);
     }
 
     @Override
     public String getServletInfo() {
         return "Log on to the server";
     }
     
 }
