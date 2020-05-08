 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Controller;
 
 import Models.GeoLocation;
 import Models.Record;
 import Models.Service;
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
  * @author shehan1
  */
 @WebServlet(name = "ServiceController", urlPatterns = {"/ServiceController"})
 public class ServiceController extends HttpServlet {
 
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
             if(request.getParameter("addService") != null){
                 HttpSession session = request.getSession(true);
                 String USID = session.getAttribute("USID").toString();
                 java.util.Date date = new java.util.Date();
                 
                 Service service = new Service();
                 UniqueKeyGenerator key = new UniqueKeyGenerator();
                 long GEOID = key.generateNewKey();
                 
                service.setSVID(key.generateNewKey());
                 service.setCompanyname(request.getParameter("companyname"));
                 service.setProvidername(request.getParameter("providername"));
                 service.setDescription(request.getParameter("description"));
                 service.setEmail(request.getParameter("email"));
                 service.setLandline(request.getParameter("landline"));
                 service.setMobile(request.getParameter("mobile"));
                 service.setSkype(request.getParameter("skype"));
                 service.setAddress(request.getParameter("address"));
                 service.setServicetype(request.getParameter("servicetype"));
                 service.setGEOID(GEOID);
                 service.setLOCID(0);
                 
                 Service rslt = service.insertService(service);
                 if(rslt != null){
                     request.setAttribute("insert","success");
                     request.setAttribute("Title","Adding a Service");
                     request.setAttribute("Description", "<p class='text-success'>The service details were added into the system successfully!<br/>Thank you for your support.</p>");
                     request.setAttribute("BtnValue", "Add another service");
                     request.setAttribute("BtnPath","create_service.jsp");
                     
                     Record newrec = new Record();
                     newrec.setRECID(key.generateNewKey());
                     newrec.setUSID(Long.parseLong(USID));
                     newrec.setREFID(service.getSVID());
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
                     request.setAttribute("Description", "<p class='text-error'>There was an error adding the service details into the system.<br/>Please try again soon.</p>");
                     request.setAttribute("BtnValue", "Try Again");
                     request.setAttribute("BtnPath","create_service.jsp");
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
