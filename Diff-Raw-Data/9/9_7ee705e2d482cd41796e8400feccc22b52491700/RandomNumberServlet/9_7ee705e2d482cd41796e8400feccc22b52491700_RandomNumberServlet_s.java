 package gov.usgs.cida.ncetl.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author jwalker
  */
 public class RandomNumberServlet extends HttpServlet {
     
    public static final int RANDOM_NUMBER = (int)(Math.random() * 1000);
     public static final long TIME_TO_SLEEP = 1000 * 10;
     private static final long serialVersionUID = 1L;
 
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request,
                                   HttpServletResponse response)
             throws ServletException, IOException {
        
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             Thread.sleep(TIME_TO_SLEEP);
             out.println("<html>");
             out.println("<head>");
             out.println("<title>RandomNumberServlet</title>");  
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>" + RANDOM_NUMBER + "</h1>");
             out.println("</body>");
             out.println("</html>");
             
         }
         catch (InterruptedException ex) {
             out.println("<html>");
             out.println("<head>");
             out.println("<title>INTERRUPTED</title>");  
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>" + RANDOM_NUMBER + "</h1>");
             out.println("</body>");
             out.println("</html>");
         }
         finally {            
             out.close();
         }
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
     protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
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
     protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
