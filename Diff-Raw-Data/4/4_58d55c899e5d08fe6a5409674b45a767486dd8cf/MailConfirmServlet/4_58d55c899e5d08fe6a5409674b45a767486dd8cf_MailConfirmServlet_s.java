 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Mail;
 
 import EJB.UserRegistry;
 import Model.Account;
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author kristofferskjutar
  */
 @WebServlet(name="BasicServlet",  urlPatterns={"/confirm"})
 public class MailConfirmServlet extends HttpServlet {
     
     public MailConfirmServlet()
     {
         super();
     }
     
     @EJB
     private UserRegistry reg;
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response)  
         throws ServletException, IOException {  
         doPost(request, response);  
     }  
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response)   
         throws ServletException, IOException {  
         String token = request.getParameter("token");
         Account a = reg.find(Long.parseLong(token));
         a.setActivated(true);
         reg.update(a);
         
          response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println("<html>");
             out.println("<head>");
             out.println("<title>OVE confirm</title>");
             out.println("</head>");
             out.println("<body>");
            out.println("<h1>You have successfully created your account" + a.getPerson().getName() +" !</h1>");
            out.println("<hlink>http://localhost:8080/OVE/<hlink>");
             out.println("</body>");
             out.println("</html>");
         } finally {
             out.close();
         }
     }
     
     
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
 
         String token = request.getParameter("token");
         Account a = reg.find(Long.parseLong(token));
         a.setActivated(true);
         reg.update(a);
         
          response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             out.println("<html>");
             out.println("<head>");
             out.println("<title>OVE confirm</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>You have successfully created your account!</h1>");
             out.println("</body>");
             out.println("</html>");
         } finally {
             out.close();
         }
     }
     
 }
