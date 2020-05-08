 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package datasheet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.json.JSONObject;
 
 /**
  *
  * @author Jenhan Tao <jenhantao@gmail.com>
  */
 public class DataServlet extends HttpServlet {
 
     protected void processGetRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("application/json");
         PrintWriter out = response.getWriter();
         try {
             if(holdingData) {
                 out.write(heldData.toString());
                 holdingData = false;
             }
            
 
         } catch (Exception e) {
             e.printStackTrace();
             StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter);
             e.printStackTrace(printWriter);
             String exceptionAsString = stringWriter.toString().replaceAll("[\r\n\t]+", "<br/>");
             if (out != null) {
                 out.write("{\"data\":\"" + exceptionAsString + "\",\"status\":\"bad\"}");
             }
         } finally {
             out.close();
         }
     }
 
     protected void processPostRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("application/json");
         PrintWriter out = response.getWriter();
         String data = request.getParameter("sending");
         try {
             heldData = new JSONObject(data);
             holdingData = true;
            response.sendRedirect("/Datasheet_Generator/output.html");
 
         } catch (Exception e) {
             e.printStackTrace();
             StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter);
             e.printStackTrace(printWriter);
             String exceptionAsString = stringWriter.toString().replaceAll("[\r\n\t]+", "<br/>");
             if (out != null) {
                 out.write("{\"data\":\"" + exceptionAsString + "\",\"status\":\"bad\"}");
             }
         } finally {
             out.close();
         }
     }
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processGetRequest(request, response);
     }
 
     /**
      * Handles the HTTP <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processPostRequest(request, response);
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
     private Boolean holdingData = false;
     private JSONObject heldData;
 }
