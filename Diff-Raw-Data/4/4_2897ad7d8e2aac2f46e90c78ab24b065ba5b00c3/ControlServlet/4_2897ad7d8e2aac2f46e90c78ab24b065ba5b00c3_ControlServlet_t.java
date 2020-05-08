 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
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
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.ApplicationServlet;
 import uk.ac.ebi.arrayexpress.components.Files;
 import uk.ac.ebi.arrayexpress.components.JobsController;
 import uk.ac.ebi.arrayexpress.components.Users;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class ControlServlet extends ApplicationServlet
 {
     private static final long serialVersionUID = -4509580274404536983L;
 
     private transient final Logger logger = LoggerFactory.getLogger(getClass());
     
     protected boolean canAcceptRequest( HttpServletRequest request, RequestType requestType )
     {
         return (requestType == RequestType.GET || requestType == RequestType.POST);
     }
 
     // Respond to HTTP requests from browsers.
     protected void doRequest( HttpServletRequest request, HttpServletResponse response, RequestType requestType ) throws ServletException, IOException
     {
         logRequest(logger, request, requestType);
 
         String command = "";
         String params = "";
 
         String[] requestArgs = new RegexHelper("servlets/control/([^/]+)/?(.*)", "i")
                 .match(request.getRequestURL().toString());
         if (null != requestArgs) {
             command = requestArgs[0];
             params = requestArgs[1];
         }
 
         if (command.equals("reload-atlas-info") || command.equals("reload-ae2-xml")) {
             ((JobsController) getComponent("JobsController")).executeJob(command);
         } else if (command.equals("reload-ae1-xml")) {
             ((JobsController) getComponent("JobsController")).executeJobWithParam(command, "connections", params);
         } else if (command.equals("rescan-files")) {
             if (0 < params.length()) {
                 ((Files) getComponent("Files")).setRootFolder(params);
             }
             ((JobsController) getComponent("JobsController")).executeJob(command);
         } else if (command.equals("verify-login")) {
             response.setContentType("text/plain; charset=ISO-8859-1");
             // Disable cache no matter what (or we're fucked on IE side)
             response.addHeader("Pragma", "no-cache");
             response.addHeader("Cache-Control", "no-cache");
             response.addHeader("Cache-Control", "must-revalidate");
             response.addHeader("Expires", "Fri, 16 May 2008 10:00:00 GMT"); // some date in the past
 
             // Output goes to the response PrintWriter.
             PrintWriter out = response.getWriter();
             try {
                String userAgent = request.getHeader("User-Agent");
                 out.print(((Users) getComponent("Users")).hashLogin(
                         request.getParameter("u")
                         , request.getParameter("p")
                        , request.getRemoteAddr().concat(null != userAgent ? userAgent : "unknown")
                 ));
             } catch (Exception x) {
                 throw new RuntimeException(x);
             }
             out.close();
         }
     }
 }
