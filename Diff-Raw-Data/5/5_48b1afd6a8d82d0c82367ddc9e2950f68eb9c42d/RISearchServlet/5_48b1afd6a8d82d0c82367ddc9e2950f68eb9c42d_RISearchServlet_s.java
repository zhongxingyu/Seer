 package fedora.server.access;
 
 import java.io.*;
 import javax.servlet.*;
 
 import org.trippi.*;
 import org.trippi.server.http.*;
 
 import fedora.server.*;
 
 public class RISearchServlet extends TrippiServlet {
 
     public TriplestoreReader getReader() throws ServletException {
         TriplestoreReader reader = null;
         try {
             Server server = Server.getInstance(new File(System.getProperty("fedora.home")));
             reader = (TriplestoreReader) server.getModule("fedora.server.resourceIndex.ResourceIndex");
         } catch (Exception e) {
             throw new ServletException("Error initting RISearchServlet.", e);
         } 
         if (reader == null) {
             throw new ServletException("The Resource Index is not loaded.");
         } else {
             return reader;
         }
     }
 
     public boolean closeOnDestroy() { return false; }
    public String getErrorStylesheetLocation() { return "/ri/error.xsl"; }
    public String getFormStylesheetLocation() { return "/ri/form.xsl"; }
     public String getContext(String origContext) { return "/ri"; }
 }
