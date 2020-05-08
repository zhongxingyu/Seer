 /*
  * Created on Aug 5, 2005
  */
 package uk.org.ponder.rsf.servlet;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.org.ponder.rsf.processor.support.RootHandlerBeanBase;
 import uk.org.ponder.rsf.renderer.RenderUtil;
 import uk.org.ponder.rsf.viewstate.AnyViewParameters;
 import uk.org.ponder.rsf.viewstate.NoViewParameters;
 import uk.org.ponder.rsf.viewstate.RawViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.servletutil.ServletResponseWriter;
 import uk.org.ponder.streamutil.write.OutputStreamPOS;
 import uk.org.ponder.streamutil.write.PrintOutputStream;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * The RootHandlerBean is the main entry point for handling of the
  * HttpServletRequest cycle. Virtually all servlet-dependent logic is placed in
  * this class, including initiating parameter decoding (eventually done by
  * RSAC), setting up the response writer and issuing client redirects. From
  * here, handling forks to the GetHandler and PostHandler in the
  * <code>processor</code> package.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class ServletRootHandlerBean extends RootHandlerBeanBase {
   private HttpServletResponse response;
   private HttpServletRequest request;
 
   public void setHttpServletRequest(HttpServletRequest request) {
     this.request = request;
   }
 
   public void setHttpServletResponse(HttpServletResponse response) {
     this.response = response;
   }
 
   // If this is a web service request, send the required redirect URL
   // to the client via the body of the POST response. Otherwise, issue
   // the redirect directly to the client via this connection.
   // TODO: This method might need some state, depending on the client.
   // maybe we can do this all with "request beans"?
   public void issueRedirect(AnyViewParameters viewparamso, PrintOutputStream pos) {
     if (viewparamso instanceof NoViewParameters) return;
     
     String path = viewparamso instanceof RawViewParameters ? ((RawViewParameters) viewparamso).URL
         : viewstatehandler.getFullURL((ViewParameters) viewparamso);
     path = RenderUtil.appendAttributes(path, RenderUtil
         .makeURLAttributes(outgoingparams));
     // TODO: This is a hack, pending a bit more thought.
 
     Logger.log.info("Redirecting to " + path);
     try {
       if (contenttypeinfo.directRedirects && viewparamso instanceof ViewParameters)  {
         ViewParameters viewparams = (ViewParameters) viewparamso;
         lazarusRedirector.lazarusRedirect(viewparams);
       }
       else {
         response.sendRedirect(path);
       }
     }
     catch (IOException ioe) {
       Logger.log.warn("Error redirecting to URL " + path, ioe);
     }
   }
 
 
   public static PrintOutputStream setupResponseWriter(HttpServletRequest request, 
        HttpServletResponse response, String contenttype) {
     try {
      response.setContentType(contenttype);
 
       ServletResponseWriter srw = new ServletResponseWriter(response);
       OutputStream os = srw.getOutputStream();
       PrintOutputStream pos = new OutputStreamPOS(os, "UTF-8");
 
       String acceptHeader = request.getHeader("Accept");
 
       // Work-around for JSF 1.0 RI bug: failing to accept "*/*" and
       // and "text/*" as valid replacements for "text/html"
       if (acceptHeader != null
           && (acceptHeader.indexOf("*/*") != -1 || acceptHeader
               .indexOf("text/*") != -1)) {
         acceptHeader += ",text/html";
       }
 
       String responseencoding = response.getCharacterEncoding();
      response.setHeader("Accept", acceptHeader);
       return pos;
     }
     catch (Exception ioe) {
       throw UniversalRuntimeException.accumulate(ioe,
           "Error setting up response writer");
     }
   }
 
   public PrintOutputStream setupResponseWriter() {
     return setupResponseWriter(request, response, contenttypeinfo.contentTypeHeader);
   }
 
 }
