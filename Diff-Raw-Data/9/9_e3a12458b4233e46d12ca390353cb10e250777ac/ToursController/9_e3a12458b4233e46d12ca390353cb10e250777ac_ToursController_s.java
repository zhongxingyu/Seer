 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controller;
 
 import Models.Record;
 import Models.Tour;
 import Models.UniqueKeyGenerator;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Timestamp;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Shehan Tis
  */
 @WebServlet(name = "ToursController", urlPatterns = {"/ToursController"})
 public class ToursController extends HttpServlet {
 
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
         PrintWriter out = response.getWriter();
         try {
             if(request.getParameter("addTour") != null){
                 HttpSession session = request.getSession(true);
                 String USID = session.getAttribute("USID").toString();
                 java.util.Date date = new java.util.Date();
                 
                 Tour tour = new Tour();
                 UniqueKeyGenerator key = new UniqueKeyGenerator();
                 
                tour.setTRID(key.generateNewKey());
                 tour.setTitle(request.getParameter("title"));
                 tour.setItinary(request.getParameter("itinary"));
                 tour.setNoOfDays(request.getParameter("noOfDays"));
                 tour.setAccomadationType(request.getParameter("accomadationType"));
                 tour.setBasis(request.getParameter("basis"));
                 tour.setGEOID(0);
                 
                 
                 Tour rslt = tour.insertTour(tour);
                 if(rslt != null){
                     request.setAttribute("insert","success");
                     request.setAttribute("Title","Adding a Tour");
                     request.setAttribute("Description", "<p class='text-success'>The tour details were added into the system successfully!<br/>Thank you for your support.</p>");
                     request.setAttribute("BtnValue", "Add another tour");
                     request.setAttribute("BtnPath","create_tours.jsp");
                     
                     Record newrec = new Record();
                     newrec.setRECID(key.generateNewKey());
                     newrec.setUSID(Long.parseLong(USID));
                     newrec.setREFID(tour.getTRID());
                     newrec.setTask("Insert");
                     newrec.setDatetime(new Timestamp(date.getTime()).toString());
                     newrec.insertRecordStatus(newrec);
                 }else{
                     request.setAttribute("insert","error");
                     request.setAttribute("Title","Adding a Tour");
                     request.setAttribute("Description", "<p class='text-error'>There was an error adding the tour details into the system.<br/>Please try again soon.</p>");
                     request.setAttribute("BtnValue", "try again");
                     request.setAttribute("BtnPath","create_tours.jsp");
                 }
                 request.getRequestDispatcher("commonresult.jsp").forward(request, response);
                 response.sendRedirect("commonresult.jsp");
             }
         } finally {            
             out.close();
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
