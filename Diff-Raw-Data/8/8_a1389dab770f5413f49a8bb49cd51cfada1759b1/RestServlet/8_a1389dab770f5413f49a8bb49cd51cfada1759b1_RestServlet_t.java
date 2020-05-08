 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nz.ac.otago.orest;
 
 import nz.ac.otago.orest.enums.HttpMethod;
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import orest.Configuration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author mark
  */
 public class RestServlet extends HttpServlet {
 
    private final static Logger logger = LoggerFactory.getLogger(RestServlet.class);
 
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, HttpMethod method)
            throws ServletException, IOException {
 
       logger.debug("Processing request.");
 
       String contentType = request.getContentType();
 
       logger.debug("Request Content-Type is '{}'", contentType);
 
      if(contentType != null && !contentType.isEmpty()) {
         // strip any crap off the content-type (like charsets)
         contentType = contentType.replaceFirst("[^(?:\\w*/\\w*)](.*)", "");
      }
 
       logger.debug("Stripped Content-Type is '{}'", contentType);
 
       // see if we have a REST session yet - if not create one and add it to the standard session
       RestSession session = (RestSession) request.getSession().getAttribute("session");
       if (session == null) {
          session = new RestSession();
 
          RestConfiguration configuration = new Configuration();
 
          session.setConfiguration(configuration);
 
          request.getSession().setAttribute("session", session);
 
          logger.debug("Created new REST session.");
 
       } else {
          logger.debug("Using existing REST session.");
       }
 
       String path = request.getPathInfo();
       logger.debug("Path = '{}'", path);
       logger.debug("Method = '{}'", method);
 
       if (!path.equals("/")) {
          try {
             session.processRequest(path, method, request, response, contentType);
          } catch(ORestException ex) {
             response.sendError(ex.getHttpStatus(), ex.getMessage());
          } catch (Exception ex) {
             logger.error("Exception occurred processing request", ex);
          }
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       processRequest(request, response, HttpMethod.GET);
    }
 
    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       processRequest(request, response, HttpMethod.POST);
    }
 
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       processRequest(request, response, HttpMethod.DELETE);
    }
 
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       processRequest(request, response, HttpMethod.PUT);
    }
 
    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
       return "Main OREST servlet";
    }// </editor-fold>
 }
