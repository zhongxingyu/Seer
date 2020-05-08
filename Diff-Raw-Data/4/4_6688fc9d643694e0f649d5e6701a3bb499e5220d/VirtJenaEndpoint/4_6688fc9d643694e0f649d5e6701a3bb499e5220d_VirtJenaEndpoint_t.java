 package com.computas.mediasone.backend;
 
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.query.*;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.net.HttpURLConnection;
 
 // import org.apache.log4j.Logger;
 
 /**
  * Servlet to create a simple Jena-based endpoint to serialize RDF/XML-ABBREV.
  * User: kkj
  * Date: Nov 13, 2008
  * Time: 9:48:49 AM
  */
 public class VirtJenaEndpoint extends HttpServlet {
 
     static final long serialVersionUID = 1;
 
   //  static Logger logger = Logger.getLogger(VirtJenaEndpoint.class);
  public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
      OutputStream out = res.getOutputStream();
      String endpoint = getServletConfig().getInitParameter("endpoint");
      if (endpoint == null) {
          throw new ServletException("endpoint parameter is not given in web.xml");
      }
      Query sparql;                                                        
      try {
          sparql = QueryFactory.create(
                             req.getParameterValues("query")[0]);
      } catch (QueryException e) {
          res.sendError(400, "The SPARQL query was malformed: " + e.getMessage());
          e.printStackTrace();
          return;
      }   
      if ((sparql.getQueryType() == Query.QueryTypeSelect) || (sparql.getQueryType() == Query.QueryTypeAsk)) {
          if ((req.getParameterValues("format") != null) && ("application/sparql-results+json".equals(req.getParameterValues("format")[0]))) {
              URL u = new URL(endpoint  + req.getQueryString());
              HttpURLConnection con = (HttpURLConnection) u.openConnection();
              res.setContentType("application/sparql-results+json");
              int read = 0;                  // Magic from "Head First Servlets and JSP"
              byte[] bytes = new byte[1024]; // Incredibly opaque code...
              while ((read = con.getInputStream().read(bytes)) != -1) {
                  out.write(bytes, 0, read);
              }
              out.flush();
              out.close();
              con.disconnect();
          } else {
              res.sendRedirect(endpoint  + req.getQueryString());
          }
          return;
      }
      if (sparql.getQueryType() == Query.QueryTypeUnknown) {
          res.sendError(400, "Unknown SPARQL query type");
          return;
      }
      // Now, we know we will return RDF
 
      String query = sparql.toString();
 
      if (sparql.getQueryType() == Query.QueryTypeDescribe) {
          query = "define sql:describe-mode \"SPO\"\n" + query;
      }
      String outencoding = "UTF-8";

      String url = endpoint + "query=" + URLEncoder.encode(query, outencoding);
   /*   while (req.getParameterNames().hasMoreElements()) {
          Object element = req.getParameterNames().nextElement();
          if (!element.equals("query")) {
              for (String s : req.getParameterValues(element.toString()))
                  url += element.toString() + "=" + URLEncoder.encode(s, "UTF-8") + "&";
          }
      } */
      URL u = new URL(url);
      HttpURLConnection con = (HttpURLConnection) u.openConnection();
      Model model = ModelFactory.createDefaultModel();
      model.read(con.getInputStream(), "");
      res.setContentType("application/rdf+xml;charset=utf-8");
      model.write(out, "RDF/XML-ABBREV");
      System.gc();
      con.disconnect();
      out.flush();
      out.close();
 
     }                                 
 }
