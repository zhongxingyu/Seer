 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.xins.util.servlet.ServletUtils;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Servlet that forwards request to an <code>API</code>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public final class APIServlet
 extends HttpServlet {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>APIServlet</code> object.
     */
    public APIServlet() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API that this servlet forwards requests to.
     */
    private API _api;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public void init(ServletConfig config)
    throws ServletException {
       String apiClass = config.getInitParameter("api.class");
       if (apiClass == null || apiClass.equals("")) {
          throw new ServletException("Unable to initialize servlet \"" + config.getServletName() + "\", API class should be set in init parameter \"api.class\".");
       }
 
       // TODO: Better error handling
 
       try {
          _api = (API) Class.forName(apiClass).newInstance();
       } catch (Exception e) {
          throw new ServletException("Unable to initialize servlet \"" + config.getServletName() + "\", unable to instantiate an object of type " + apiClass + ", or unable to convert it to an API instance.");
       }
    }
 
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
       handleRequest(req, resp);
    }
 
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
       handleRequest(req, resp);
    }
 
    private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
 
       // TODO: Be less memory-intensive
 
       // Set the content output type to XML
       resp.setContentType("text/xml");
 
       // Reset the XMLOutputter
       StringWriter stringWriter = new StringWriter();
       XMLOutputter xmlOutputter = new XMLOutputter(stringWriter, "UTF-8");
 
       // Stick all parameters in a map
       Map map = new HashMap();
       Enumeration names = req.getParameterNames();
       while (names.hasMoreElements()) {
          String name = (String) names.nextElement();
          String value = req.getParameter(name);
          map.put(name, value);
       }
 
       // Create a new call context
       CallContext context = new CallContext(xmlOutputter, map);
 
       // Forward the call
       PrintWriter out = resp.getWriter();
       boolean succeeded = false;
       try {
          _api.handleCall(context);
          succeeded = true;
       } catch (Throwable exception) {
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", "InternalError");
          xmlOutputter.startTag("param");
          xmlOutputter.attribute("name", "_exception.class");
          xmlOutputter.pcdata(exception.getClass().getName());
 
          String message = exception.getMessage();
         if (message != null && !message.length() == 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.message");
             xmlOutputter.pcdata(message);
          }
 
          StringWriter stWriter = new StringWriter();
          PrintWriter printWriter = new PrintWriter(stWriter);
          exception.printStackTrace(printWriter);
          String stackTrace = stWriter.toString();
         if (stackTrace != null && !message.length() == 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.stacktrace");
             xmlOutputter.pcdata(stackTrace);
          }
          xmlOutputter.close();
       }
 
       if (succeeded) {
          out.print(stringWriter.toString());
       }
       out.flush();
    }
 }
