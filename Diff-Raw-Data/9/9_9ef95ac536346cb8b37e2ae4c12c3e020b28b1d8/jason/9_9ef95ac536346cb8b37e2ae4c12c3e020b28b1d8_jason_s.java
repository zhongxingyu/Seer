 package org.vle.aid.client;
 
 import java.io.*;
 import java.util.Map;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.apache.axis.client.Call;
 import org.apache.axis.client.Service;
 
 import java.util.logging.Logger;
 
 /**
  * Servlet to function as a bridge from web-requests to SOAP webservices
  *  
  * @author wrvhage, emeij
  * @version 1.0
  */
 public class jason extends HttpServlet {
   
   /** logger for Commons logging. */
   private transient Logger log =
      Logger.getLogger("jason.class.getName()");
     
     /**
      * Substitutes some special characters and returns nicer HTML characters
      * Currently:
      * "&"
      * "<"
      * ">"
      * @param text String of text to be escaped
      * @return a String with replaced characters
     */
     public static String escape(String text) {
         text = text.replaceAll("&", "&amp;");
         text = text.replaceAll("<", "&lt;");
         text = text.replaceAll(">", "&gt;");
         text = text.replaceAll(">", "&#39;");
         text = text.replaceAll("'", "&quot;");
         return text;
     }
     
     /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request 
      * @param response servlet response
      */
     @SuppressWarnings("unchecked")
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         Map param = request.getParameterMap();
         
         if (param.containsKey("test")) {
           return;
         }
         
         try {
         
           if (((String[])param.get("target"))[0].equalsIgnoreCase("search")) {
             String query = ((String[])param.get("query"))[0];
             String index = ((String[])param.get("index"))[0];
             String field = ((String[])param.get("field"))[0];
             
             String start = "1";
             try { start = ((String[])param.get("start"))[0];
             } catch(java.lang.NullPointerException e) { } // ignore
             
             String count;
   					try {
   						count = ((String[])param.get("count"))[0];
   					}  catch(java.lang.NullPointerException e) { 
   						count = ((String[])param.get("limit"))[0];
   					}
             
             query.replaceAll("%2B","+");
             log.info("Received JSON: " + query);
   
             String endpoint = "http://localhost:8080/axis/services/SearcherWS";
   
             Service service = new Service();
             Call call = (Call) service.createCall();
   
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("searchJason"); 
   
             String res = (String) call.invoke( new Object[] {
                 index, query, start, field, count 
             } ); 
   
             out.print(res);
                   
           } else if (((String[])param.get("target"))[0].equalsIgnoreCase("fields")) {
             String index = ((String[])param.get("index"))[0];
   
             String endpoint = "http://localhost:8080/axis/services/getFields";
   
             Service service = new Service();
             Call call = (Call) service.createCall();
   
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("listFieldsJason"); 
   
             String res = (String) call.invoke( new Object[] {
                 index
             } ); 
   
             out.print(res);
             
           } else if (((String[])param.get("target"))[0].equalsIgnoreCase("indexes")) {
             String endpoint = "http://localhost:8080/axis/services/getIndexes";
   
             Service service = new Service();
             Call call = (Call) service.createCall();
   
             call.setTargetEndpointAddress(endpoint);
             call.setOperationName("listIndexesJason"); 
   
             String res = (String) call.invoke( new Object[] { } ); 
   
             out.print(res);
           } else {
             throw new IOException("Unknown operator");
           }
         } catch (Exception e) {
           StringWriter sw = new StringWriter();
           PrintWriter pw = new PrintWriter(sw);
           e.printStackTrace(pw);
           String reason = sw.toString();
           pw.close();
           log.severe(e.toString());
           out.print("{'success':false, 'errors':{'reason':'"+reason.replaceAll("\\n", "<br/>")+"'}}");
         }
         
         out.close();
     }
     
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         processRequest(request, response);
     }
     
     /** Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
     throws ServletException, IOException {
         processRequest(request, response);
     }
     
     /** Returns a short description of the servlet.
      */
     public String getServletInfo() {
         return "Short description";
     }
     // </editor-fold>
 }
