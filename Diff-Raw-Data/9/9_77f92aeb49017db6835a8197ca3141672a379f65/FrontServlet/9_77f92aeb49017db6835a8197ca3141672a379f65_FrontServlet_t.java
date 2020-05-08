 package fi.helsinki.cs.web;
 
 import fi.helsinki.cs.okkopa.database.OracleConnector;
 import fi.helsinki.cs.okkopa.database.Settings;
 import fi.helsinki.cs.okkopa.model.CourseDbModel;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class FrontServlet extends HttpServlet {
 
     private ArrayList<String> list;
     private String courcePeriod;
     private String courceType;
     private String courceNumber;
     private String value;
     private String courceCode;
     private String courceName;
     private String courceYear;
     private String valueName;
     private List<CourseDbModel> cources;
     private OracleConnector oc;
 
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
             throws ServletException, IOException, SQLException {
         request.setAttribute("message", Settings.instance.getProperty("gui.front.header"));
         request.setAttribute("help", Settings.instance.getProperty("gui.front.help"));
         request.setAttribute("info", Settings.instance.getProperty("gui.front.infofield"));
         request.setAttribute("email", Settings.instance.getProperty("gui.front.emailfield"));
         
         oc = connectToDB();
         
         cources = formatAndGetCources();
 
         for (int i = 0; i < cources.size(); i++) {
             
             getValues(i);
 
             parseValue();
             parseForNameValue(i);
             
             list.add("<option value=\"" + value+ "\">" + valueName + "</option>");
         }
 
         setCourcesForForm( request); 
 
         RequestDispatcher dispatcher = request.getRequestDispatcher("WEB-INF/front.jsp");
         dispatcher.forward(request, response);
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
         try {
             processRequest(request, response);
         } catch (SQLException ex) {
             Logger.getLogger(FrontServlet.class.getName()).log(Level.SEVERE, null, ex);
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
         try {
             processRequest(request, response);
         } catch (SQLException ex) {
             Logger.getLogger(FrontServlet.class.getName()).log(Level.SEVERE, null, ex);
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
 
     private void parseName(int i) {
         courceName = getParseName(i);
     }
     
     private String getParseName(int size) {
         if (courceName.length() >= size) {
             return courceName.substring(0, size-3) + "...";
         }
         return courceName;
     }
 
     private void parsePeriod() {
         if (courcePeriod.equals("K")) {
             courcePeriod = "kevät";
         } else if (courcePeriod.equals("S")) {
             courcePeriod = "syksy";
         } else if (courcePeriod.equals("V")) {
             courcePeriod = "kesä";
         }
     }
 
     private void parseType() {
         if (courceType.equals("L")) {
             courceType = "Luento";
         } else if (courceType.equals("K")) {
             courceType = "Koe";
         } else if (courceType.equals("A")) {
             courceType = "harjoitustyö";
         } else if (courceType.equals("S")) {
             courceType = "Seminaari";
         }
     }
 
     private void parseNumber() {
         if (courceNumber.equals("1")) {
             courceNumber = "Kurssi";
         } else if (courceNumber.equals("2")) {
             courceNumber = "Erilliskoe";
         }
     }
 
     private void getValues(int i) {
         courceCode = cources.get(i).getCourseCode();
         courceName = cources.get(i).getName();
         courceNumber = "" + cources.get(i).getCourseNumber();
         courcePeriod = cources.get(i).getPeriod();
         courceType = cources.get(i).getType();
         courceYear = "" + cources.get(i).getYear();
     }
 
     private void parseForNameValue(int i) {
         parseName(70);
         parsePeriod();
         parseType();
         parseNumber();
         
         valueName = courceName + " (" + courceCode + "), " + courceYear + ", " + courcePeriod + ", " + courceType + ", " + courceNumber;
     }
 
     private List<CourseDbModel> formatAndGetCources() throws SQLException {
         list = new ArrayList<String>();
         return oc.getCourseList();
     }
 
     private OracleConnector connectToDB() throws SQLException {
         OracleConnector oc = new OracleConnector(Settings.instance);
         oc.connect();
         return oc;
     }
 
     private void parseValue() {
        value =  courceCode + ":" + courcePeriod + ":" + courceYear + ":" + courceType + ":" + courceNumber + ":" + getParseName(55);
     }
 
     private void setCourcesForForm(HttpServletRequest request) {
         Collections.sort(list);
 
         request.setAttribute("courceCodes", list);
     }
 }
