 package eo.frontend.httpserver;
 
 import java.lang.*;
 import java.util.*;
 import java.io.*;
 
 import javax.servlet.http.*;
 import javax.servlet.*;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.*;
 
 
 import org.eclipse.jetty.server.*;
 import org.eclipse.jetty.server.handler.*;
 
 import com.thoughtworks.xstream.XStream;
 
 // ================================================================================
 
 public class DynamicHandlerCollection extends AbstractHandler {
     private Map<String, DynamicHandler> handlers_;
     private String base_;
 
     
     public void setHandlers(final Map<String, DynamicHandler> handlers) {
         handlers_ = handlers;
     }
 
     public void setBase(final String base) {
         base_ = base;
     }
 
     public void handle(String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) 
         throws IOException, ServletException {
 
         try {
             //System.out.println(path);
             
             //String path = target;
             
             /* DIRTY HACK!!!*/
             //String hn = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("."));
            String hn = baseRequest.getServletName();
 
            //System.out.println(hn);
 
             if (handlers_.containsKey(hn)) {
                 DynamicHandler.Response resp = handlers_.get(hn).handle(baseRequest);
                 
                 /* Serialize handler response */
                 XStream xs = new XStream();
                 for (Map.Entry e : resp.aliases.entrySet()) {
                     xs.alias((String)e.getKey(), (Class)e.getValue());
                 }
                 String xml = xs.toXML(resp.result);
                 String html;
 
                 if (baseRequest.getParameterValues("_ox") == null) {
                     /* Apply XSLT */
                     Source xml_source = new StreamSource(new StringReader(xml));
                     Source xsl_source = new StreamSource(new File(base_ + "/" + hn + ".xsl"));
                     Writer writer = new StringWriter();
                     Result transform_result = new StreamResult(writer);
                     Transformer trans = TransformerFactory.newInstance().newTransformer(xsl_source);
                     trans.transform(xml_source, transform_result);
                     writer.flush();
 
                     html = writer.toString();
                     response.setContentType("text/html");
                 } else {
                     html = xml;
                     response.setContentType("application/xml");
                 }
                 
                 response.setStatus(HttpServletResponse.SC_OK);                
                 response.setCharacterEncoding("UTF-8");
                 response.setContentLength(html.getBytes().length);
 
                 PrintWriter pw = response.getWriter();
                 pw.print(html + "\n\n");
                 pw.flush();
 
                 //System.out.println(html);
             }
         }  catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
