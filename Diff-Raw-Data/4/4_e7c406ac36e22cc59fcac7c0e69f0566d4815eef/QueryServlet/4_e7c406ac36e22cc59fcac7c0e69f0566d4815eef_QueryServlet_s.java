 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2010 Functional Genomics Group, European Bioinformatics Institute
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.apache.commons.lang.text.StrSubstitutor;
 import org.apache.lucene.queryParser.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
 import uk.ac.ebi.arrayexpress.components.Experiments;
 import uk.ac.ebi.arrayexpress.components.SaxonEngine;
 import uk.ac.ebi.arrayexpress.components.SearchEngine;
 import uk.ac.ebi.arrayexpress.components.Users;
 import uk.ac.ebi.arrayexpress.utils.CookieMap;
 import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class QueryServlet extends ApplicationServlet
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
     {
         return (requestType == RequestType.GET || requestType == RequestType.POST);
     }
 
     // Respond to HTTP requests from browsers.
     protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType ) throws ServletException, IOException
     {
         logRequest(logger, request, requestType);
 
         String type = "xml";
         String stylesheet = "default";
 
         String[] requestArgs = new RegexHelper("servlets/query/([^/]+)/?([^/]*)", "i")
                 .match(request.getRequestURL().toString());
         if (null != requestArgs) {
             if (!requestArgs[0].equals("")) {
                 stylesheet = requestArgs[0];
             }
             if (!requestArgs[1].equals("")) {
                 type = requestArgs[1];
             }
         }
 
         if (type.equals("xls")) {
             // special case for Excel docs
             // we actually send tab-delimited file but mimick it as XLS doc
             String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
             response.setContentType("application/vnd.ms-excel");
             response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".xls\"");
             type = "tab";
         } else if (type.equals("tab")) {
             // special case for tab-delimited files
             // we send tab-delimited file as an attachment
             String timestamp = new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
             response.setContentType("text/plain; charset=ISO-8859-1");
             response.setHeader("Content-disposition", "attachment; filename=\"ArrayExpress-Experiments-" + timestamp + ".txt\"");
             type = "tab";
         } else {
             // Set content type for HTML/XML/plain
             response.setContentType("text/" + type + "; charset=ISO-8859-1");
         }
         // tell client to not cache the page unless we want to
         if (!"true".equalsIgnoreCase(request.getParameter("cache"))) {
             response.addHeader("Pragma", "no-cache");
             response.addHeader("Cache-Control", "no-cache");
             response.addHeader("Cache-Control", "must-revalidate");
             response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past
         }
 
         // Output goes to the response PrintWriter.
         PrintWriter out = response.getWriter();
         try {
             Experiments experiments = (Experiments)getComponent("Experiments");
             if (stylesheet.equals("arrays-select")) {
                 out.print(experiments.getArrays());
             } else if (stylesheet.equals("species-select")) {
                 out.print(experiments.getSpecies());
             } else {
                 String stylesheetName = new StringBuilder(stylesheet).append('-').append(type).append(".xsl").toString();
 
                 HttpServletRequestParameterMap params = new HttpServletRequestParameterMap(request);
                 // to make sure nobody sneaks in the other value w/o proper authentication
                 params.put("userid", "1");
 
                 // adding "host" request header so we can dynamically create FQDN URLs
                 params.put("host", request.getHeader("host"));
                 params.put("basepath", request.getContextPath());
 
                 CookieMap cookies = new CookieMap(request.getCookies());
                 if (cookies.containsKey("AeLoggedUser") && cookies.containsKey("AeLoginToken")) {
                     Users users = (Users)getComponent("Users");
                    String user = cookies.get("AeLoggedUser").getValue();
                     String passwordHash = cookies.get("AeLoginToken").getValue();
                     if (users.verifyLogin(user, passwordHash, request.getRemoteAddr().concat(request.getHeader("User-Agent")))) {
                         if (0 != users.getUserRecord(user).getId()) { // 0 - curator (superuser) -> remove user restriction
                             params.put("userid", String.valueOf(users.getUserRecord(user).getId()));
                         } else {
                             params.remove("userid");
                         }
                     } else {
                         logger.warn("Removing invalid session cookie for user [{}]", user);
                         // resetting cookies
                         Cookie userCookie = new Cookie("AeLoggedUser", "");
                         userCookie.setPath("/");
                         userCookie.setMaxAge(0);
 
                         response.addCookie(userCookie);
                     }
                 }
 
                 try {
                     Integer queryId = ((SearchEngine)getComponent("SearchEngine")).getController().addQuery(experiments.EXPERIMENTS_INDEX_ID, params, request.getQueryString());
                     params.put("queryid", String.valueOf(queryId));
 
                     SaxonEngine saxonEngine = (SaxonEngine)getComponent("SaxonEngine");
                     if (!saxonEngine.transformToWriter(
                             experiments.getDocument(),
                             stylesheetName,
                             params,
                             out)) {
                         throw new Exception("Transformation returned an error");
                     }
                 } catch (ParseException x) {
                     logger.error("Caught lucene parse exception:", x);
                     reportQueryError(out, "query-syntax-error.txt", request.getParameter("keywords"));
                 }
             }
         } catch (Exception x) {
             throw new RuntimeException(x);
         }
         out.close();
     }
 
     private void reportQueryError( PrintWriter out, String templateName, String query )
     {
         try {
             URL resource = Application.getInstance().getResource("/WEB-INF/server-assets/templates/" + templateName);
             String template = StringTools.streamToString(resource.openStream());
             Map<String, String> params = new HashMap<String, String>();
             params.put("variable.query", query);
             StrSubstitutor sub = new StrSubstitutor(params);
             out.print(sub.replace(template));
         } catch (Exception x) {
             logger.error("Caught an exception:", x);
         }
     }
 }
 
