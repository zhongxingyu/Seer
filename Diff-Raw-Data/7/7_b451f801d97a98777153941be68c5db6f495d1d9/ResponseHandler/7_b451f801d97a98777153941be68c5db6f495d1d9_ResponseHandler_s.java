 package polly.core.http;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 
 
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.http.HttpEvent;
 import de.skuzzle.polly.sdk.http.HttpParameter;
 import de.skuzzle.polly.sdk.http.HttpParameter.ParameterType;
 import de.skuzzle.polly.sdk.http.HttpSession;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 import de.skuzzle.polly.sdk.http.HttpTemplateException;
 
 
 public class ResponseHandler implements HttpHandler {
     
     private final static Logger logger = Logger.getLogger(ResponseHandler.class
         .getName());
     
     /**
      * Regex for splitting GET style parameters from the request uri
      */
     public final static Pattern GET_PARAMETERS = Pattern.compile(
         "(\\w+)=([^&]+)");
     
     
     private static void parseParameters(String in, Map<String, HttpParameter> params, 
             ParameterType type) {
         Matcher m = GET_PARAMETERS.matcher(in);
         
         while (m.find()) {
             String key = in.substring(m.start(1), m.end(1));
             String value = in.substring(m.start(2), m.end(2));
             try {
                 value = URLDecoder.decode(value, "ISO-8859-1");
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
             if (params.containsKey(key)) {
                 HttpParameter current = params.get(key);
                 params.put(key, new HttpParameter(
                         key, current.getValue() + ";" + value, type));
             } else {
                 params.put(key, new HttpParameter(key, value, type));
             }
         }
     }
     
     
     
     private static void parsePostParameters(HttpExchange t, 
             Map<String, HttpParameter> result, String encoding) throws IOException {
         
         BufferedReader r = new BufferedReader(new InputStreamReader(t.getRequestBody(), 
             encoding));
         String line = null;
         while ((line = r.readLine()) != null) {
             if (!line.equals("")) {
                 parseParameters(line, result, ParameterType.POST);
             }
         }
     }
     
     
     private HttpManagerImpl webServer;
     
     
     
     public ResponseHandler(HttpManagerImpl webServer) {
         this.webServer = webServer;
     }
     
     
 
     @Override
     public void handle(HttpExchange t) throws IOException {
         this.webServer.cleanUpSessions();
         
         HttpSession session = this.webServer.getSession(
             t.getRemoteAddress().getAddress());
         long now = System.currentTimeMillis();
         
         // kill the session if a user is logged in on it and it is expired
         if (session.isLoggedIn() && session.isTimedOut(
                 this.webServer.getSessionTimeOut())) {
             this.webServer.closeSession(session);
             HttpTemplateContext c = this.webServer.errorTemplate("Session expired", 
                 "Your session has automatically been killed due to inactivity. " +
                 "Please login and try again.", session);
             this.respond(c, t);
             return;
         }
         
         session.setLastAction(now);
         
         String uri = t.getRequestURI().toString();
         logger.trace(session + " requested " + uri);
         session.setLastUri(uri);
         
         
         // extract GET parameters
         Map<String, HttpParameter> parameters = new HashMap<String, HttpParameter>();
         if (uri.contains("?")) {
             String[] parts = uri.split("\\?");
             parseParameters(parts[1], parameters, ParameterType.GET);
             uri = parts[0];
         }
         
         logger.trace("Evaluated URI: " + uri);
         
         // extract POST parameters
         if (t.getRequestMethod().equals("POST")) {
             parsePostParameters(t, parameters, this.webServer.getEncoding());
         }
         
         
         HttpEvent e = new HttpEvent(this.webServer, session, uri);
         e.getProperties().putAll(parameters);
         
         HttpTemplateContext c = null;
         try {
             c = this.webServer.executeAction(e);
         } catch (HttpTemplateException e1) {
             c = this.webServer.errorTemplate(e1.getHeading(), e1.getMessage(), session);
             logger.warn("Template Exception: ", e1);
         } catch (InsufficientRightsException e1) {
             c = this.webServer.errorTemplate(
                 "Permission denied", 
                "You have insufficient permissions to acces this page/action." +
                            "<br/><br/>Missing permission(s): " + 
                        e1.getObject().getRequiredPermission(), e.getSession());
         } catch (Exception e1) {
             StringWriter w = new StringWriter();
             PrintWriter pw = new PrintWriter(w);
             e1.printStackTrace(pw);
             logger.error("Exception while executing Action", e1);
             c = this.webServer.errorTemplate("Exception while executing action", 
                     w.getBuffer().toString(), session);
         }
         
         if (c != null) {
             this.respond(c, t);
         } else {
             // there is no action for the given uri, so treat it as a file request
             File dest = this.webServer.getPage(uri);
             if (!dest.exists()) {
                 c = this.webServer.errorTemplate("404 - Not Found", 
                     "Your request could not be processed because the requested " +
                     "page/action was not found.", session);
                 this.respond(c, t);
             } else {
                 this.respond(dest, t);
             }
         }
     }
     
     
     
     private void respond(File dest, HttpExchange t) throws IOException {
         t.sendResponseHeaders(200, 0);
         
         FileInputStream inp = null;
         OutputStream out = null;
         try {
             out = t.getResponseBody();
             inp = new FileInputStream(dest);
             byte[] buffer = new byte[1024];
             int len = 0;
             
             while ((len = inp.read(buffer)) > 0) {
                 out.write(buffer, 0, len);
             }
         } finally {
             t.close();
             if (inp != null) {
                 inp.close();
             }
             if (out != null) {
                 out.close();
             }
         }
     }
     
     
     
     private void respond(HttpTemplateContext c, HttpExchange t) throws IOException {
         t.sendResponseHeaders(200, 0);
         OutputStream out = null;
         try {
             out = t.getResponseBody();
             this.generateTemplate(c, out);
         } catch (IOException e) {
             throw e;
         } catch (Exception e) {
             logger.error("Error while sending http response", e);
         } finally {
             t.close();
             out.close();
         }
     }
     
     
     
     private void generateTemplate(HttpTemplateContext c, OutputStream out) 
             throws IOException {
         Velocity.init();
         VelocityContext context = new VelocityContext(c);
         
         File dest = new File(this.webServer.getTemplateRoot(), "index.html");
         Template template = Velocity.getTemplate(dest.getPath(), 
                 this.webServer.getEncoding());
         //OutputStreamWriter writer = new OutputStreamWriter(out);
         StringWriter writer = new StringWriter();
         template.merge(context, writer);
         out.write(writer.toString().getBytes(Charset.forName(
                 this.webServer.getEncoding())));
     }
 }
