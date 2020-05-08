 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.bnl.racf.ps.dashboard.db.servlets;
 
 import gov.bnl.racf.ps.dashboard.db.data_objects.PsHost;
 import gov.bnl.racf.ps.dashboard.db.data_objects.PsService;
 import gov.bnl.racf.ps.dashboard.db.data_store.PsDataStore;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.JsonConverter;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsObjectShredder;
 import gov.bnl.racf.ps.dashboard.db.session_factory_store.PsSessionFactoryStore;
 import gov.bnl.racf.ps.dashboard.db.utils.UrlUnpacker;
 import gov.racf.bnl.ps.dashboard.PsApi.PsApi;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  *
  * @author tomw
  */
 public class PsServicesServlet extends HttpServlet {
 
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
             /*
              * TODO output your page here. You may use following sample code.
              */
             out.println("<html>");
             out.println("<head>");
             out.println("<title>Servlet PsServicesServlet</title>");
             out.println("</head>");
             out.println("<body>");
             out.println("<h1>Servlet PsServicesServlet at " + request.getContextPath() + "</h1>");
             out.println("</body>");
             out.println("</html>");
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
         //processRequest(request, response);
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         //boilerplate code to open session
         SessionFactory sessionFactory =
                 PsSessionFactoryStore.getSessionFactoryStore().getSessionFactory();
         Session session = sessionFactory.openSession();
 
         try {
 
             session.beginTransaction();
 
 
 
             ArrayList<String> parameters = UrlUnpacker.unpack(request.getPathInfo());
 
             if (parameters.size() > 0) {
                 // get info about a concrete service
 
                 //get url parameters
                 String detailLevel = request.getParameter(PsApi.DETAIL_LEVEL_PARAMETER);
                 if (detailLevel == null || "".equals(detailLevel)) {
                     // default detail level
                    detailLevel = PsApi.DETAIL_LEVEL_LOW;
                 }
 
                 String idAsString = parameters.get(0);
                 Integer serviceIdInteger = Integer.parseInt(idAsString);
                 int serviceId = serviceIdInteger.intValue();
                 PsService service = PsDataStore.getService(session, serviceId);
                 JSONObject serviceJson = JsonConverter.toJson(service, detailLevel);
                 out.println(serviceJson.toString());
             } else {
                 // get list of services
                 
                 //get url parameters
                 String detailLevel = request.getParameter(PsApi.DETAIL_LEVEL_PARAMETER);
                 if (detailLevel == null || "".equals(detailLevel)) {
                     // default detail level
                    detailLevel = PsApi.DETAIL_LEVEL_HIGH;
                 }
                 
                 List<PsService> listOfServices = PsDataStore.getAllServices(session);
                 JSONArray jsonArray = new JSONArray();
                 for (PsService service : listOfServices) {
                     JSONObject serviceJson = JsonConverter.toJson(service, detailLevel);
                     jsonArray.add(serviceJson);
                 }
                 out.println(jsonArray.toString());
             }
 
             // commit transaction 
             session.getTransaction().commit();
 
 
 
         } catch (Exception e) {
             Logger.getLogger(PsServicesServlet.class).error("error occured: " + e);
         } finally {
             session.close();
             out.close();
         }
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
         Logger.getLogger(PsServicesServlet.class).error("POST method is not implemented yet");
         throw new ServletException("POST method noit implemented yet");
     }
 
     /**
      * Handles the HTTP
      * <code>DELETE</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doDelete(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         try {
             // first order of business is to open session
             //boilerplate code to open session
             SessionFactory sessionFactory =
                     PsSessionFactoryStore.getSessionFactoryStore().getSessionFactory();
             Session session = sessionFactory.openSession();
             session.beginTransaction();
 
             // second order of business is to unpack parameters from url
             ArrayList<String> parameters = UrlUnpacker.unpack(request.getPathInfo());
 
             //if there are parameters
             if (parameters.size() > 0) {
                 String idAsString = parameters.get(0);
                 Integer serviceIdInteger = Integer.parseInt(idAsString);
                 int serviceId = serviceIdInteger.intValue();
                 PsService service = PsDataStore.getService(session, serviceId);
 
                 PsObjectShredder.delete(session, service);
             }
 
             // commit transaction and close session
             session.getTransaction().commit();
             session.close();
         } catch (Exception e) {
             System.out.println(new Date() + " Error in " + getClass().getName() + " " + e);
             Logger.getLogger(PsServicesServlet.class).error(e);
         } finally {
             out.close();
         }
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
