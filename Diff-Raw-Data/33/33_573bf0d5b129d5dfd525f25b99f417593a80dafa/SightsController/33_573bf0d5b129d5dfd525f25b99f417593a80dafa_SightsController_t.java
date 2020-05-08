 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controller;
 
 import Models.GeoLocation;
 import Models.Record;
 import Models.Sight;
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
 @WebServlet(name = "SightsController", urlPatterns = {"/SightsController"})
 public class SightsController extends HttpServlet {
 
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
             if(request.getParameter("addSight") != null){
                 HttpSession session = request.getSession(true);
                 String USID = session.getAttribute("USID").toString();
                 java.util.Date date = new java.util.Date();
                 
                 Sight sight = new Sight();
                 UniqueKeyGenerator key = new UniqueKeyGenerator();
                 long GEOID = key.generateNewKey();
                 
                sight.setSID(key.generateNewKey3());
                 sight.setName(request.getParameter("name"));
                 sight.setBrief(request.getParameter("brief"));
                 sight.setLandline(request.getParameter("landline"));
                 sight.setMobile(request.getParameter("mobile"));
                 sight.setDescription(request.getParameter("description"));
                 sight.setEntrance(request.getParameter("entrancetype"));
                 sight.setAdult(request.getParameter("adult"));
                 sight.setChild(request.getParameter("child"));
                 sight.setGEOID(GEOID);
                 sight.setLOCID(0);
                 
                 Sight rslt = sight.insertSight(sight);
                 if(rslt != null){
                     request.setAttribute("insert","success");
                     request.setAttribute("Title","Adding a Sight");
                     request.setAttribute("Description", "<p class='text-success'>The sight details were added into the system successfully!<br/>Thank you for your support.</p>");
                     request.setAttribute("BtnValue", "Add another sight");
                     request.setAttribute("BtnPath","create_sight.jsp");
                     
                     Record newrec = new Record();
                     newrec.setRECID(key.generateNewKey());
                     newrec.setUSID(Long.parseLong(USID));
                     newrec.setREFID(sight.getSID());
                     newrec.setTask("Insert");
                     newrec.setDatetime(new Timestamp(date.getTime()).toString());
                     newrec.insertRecordStatus(newrec);
                     
                     GeoLocation geo = new GeoLocation();
                     geo.setGEOID(GEOID);
                     geo.setLongitude(request.getParameter("longitude"));
                     geo.setLattitude(request.getParameter("latitude"));
                     geo.insertGEOLocation(geo);
                 }else{
                     request.setAttribute("insert","error");
                     request.setAttribute("Title","Adding a Sight");
                     request.setAttribute("Description", "<p class='text-error'>There was an error adding the Sight details into the system.<br/>Please try again soon.</p>");
                     request.setAttribute("BtnValue", "Try again");
                     request.setAttribute("BtnPath","create_sight.jsp");
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
