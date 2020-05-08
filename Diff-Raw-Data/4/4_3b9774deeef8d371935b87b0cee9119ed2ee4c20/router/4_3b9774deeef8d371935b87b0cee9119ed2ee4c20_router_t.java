 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ywc.frontend.ws;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import ywc.core.settings;
 import ywc.core.xslt;
 
 public class router extends HttpServlet {
     private static final Logger logger = Logger.getLogger(router.class);
 
     protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
         request.setCharacterEncoding("UTF-8");
         response.setCharacterEncoding("UTF-8");
 
         long routerStart = System.currentTimeMillis();
 
         String uri = request.getParameter("uri");
         String action = process.getRouterAction(uri);
        
        System.out.println("Requested: "+uri);
        
         String mcHash;
         Boolean noCache = false;
         if (request.getParameter("nocache") != null) {
             noCache = true;
         }
         Boolean flushCache = false;
         if (request.getParameter("flushcache") != null) {
             flushCache = true;
         }
 
         if (action.contains("img") || action.contains("doc")) {
             ywc.frontend.image.router.choose(request, response);
             logger.debug("Transform: " + (System.currentTimeMillis() - routerStart) + "ms\t" + uri + "\timg");
 
         } else if (action.contains("inc")) {
             ywc.frontend.text.uri.aggregateIncludes(request, response, routerStart, flushCache);
 
         } else if (action.contains("url")) {
             process.proxyUrl(uri, response);
 
         } else {
             try {
 
                 String lang = ywc.frontend.text.uri.langFromUri(uri);
                 String params = ywc.frontend.text.uri.paramsToString(uri, request);
                 uri = ywc.frontend.text.uri.isolateUri(uri, lang);
                 String user = "";
                 if (request.getHeader("REMOTE_USER") != null) { user = request.getHeader("REMOTE_USER"); }
                 logger.debug("REMOTE_USER:" + user);
                 params = process.appendCookiesAndHeaders(params, request);
 
                 HashMap paramObj = new HashMap();
                 paramObj.put("uri", uri);
                 paramObj.put("params", params);
                 paramObj.put("user", user);
                 paramObj.put("domain", settings.getYwcEnvDomain());
                 paramObj.put("protocol", settings.getYwcEnvProtocol());
 
                 if (!process.isUriRedirect(paramObj, uri, routerStart, response, request)) {
 
                     StringBuilder pageOutputBuilder = new StringBuilder();
                     Boolean pageOutputProceed = true;
 
                     paramObj.put("lang", lang);
                     paramObj.put("ywc_delim_header", settings.getYwcXslDelimHeader());
                     paramObj.put("ywc_delim_outer", settings.getYwcXslDelimOuter());
                     paramObj.put("ywc_delim_inner", settings.getYwcXslDelimInner());
                     paramObj.put("ywc_delim_command", settings.getYwcXslDelimCommand());
                     paramObj.put("ywc_env_env", settings.getYwcEnv());
                     paramObj.put("ywc_env_app", settings.getYwcEnvApp());
                     paramObj.put("ywc_env_domain", settings.getYwcEnvDomain());
                     paramObj.put("ywc_env_server", settings.getYwcEnvServer());
 
                     mcHash = "ywc_" + ywc.core.str.md5sum(settings.getPathYwcCache() + "/xml/data/uri.xml" + "%" + settings.getPathYwcCoreData() + "xsl/core/render/master.xsl" + "%" + uri + "%" + params + "%" + lang + "%" + user);
 
                     if (noCache) {
                         mcHash = null;
                     }
 
                     String masterPage = xslt.exec(settings.getPathYwcCache() + "/xml/data/uri.xml", settings.getPathYwcCoreData() + "xsl/core/render/master.xsl", paramObj, mcHash, flushCache);
 
                     paramObj.remove("ywc_delim_header");
                     paramObj.remove("ywc_delim_outer");
                     paramObj.remove("ywc_delim_inner");
                     String[] masterPageSplit = masterPage.split(settings.getYwcXslDelimOuter());
                     if (masterPageSplit.length > 1) {
 
                         String requestCaching = process.xslRequestCachingInit();
                         String transformOutput;
                         long transformStart;
                         String xmlPath;
                         String xslPath;
                         Boolean doTransform;
 
                         for (int i = 0; i < masterPageSplit.length; i = i + 2) {
 
                             pageOutputBuilder.append(masterPageSplit[i]);
                             String[] transform_params;
                             xmlPath = settings.getPathYwcCoreData() + "xml/core/blank.xml";
                             xslPath = settings.getPathYwcCoreData() + "xsl/core/blank.xsl";
                             doTransform = false;
                             transformOutput = "";
                             if ((i + 1) < masterPageSplit.length) {
                                 transform_params = masterPageSplit[(i + 1)].split(settings.getYwcXslDelimInner());
                                 if (transform_params.length > 2) {
 
                                     if (!"".equals(transform_params[1].trim())) {
                                         xmlPath = settings.getPathYwcCache() + "/xml/" + transform_params[1];
                                         doTransform = true;
                                     }
                                     if (!"".equals(transform_params[2].trim())) {
                                         xslPath = settings.getPathYwcXsl() + transform_params[2];
                                         doTransform = true;
                                     }
 
                                     if (doTransform) {
                                         mcHash = "ywc_" + ywc.core.str.md5sum(xmlPath + "%" + xslPath + "%" + uri + "%" + params + "%" + lang + "%" + user);
 
                                         paramObj = process.xslRequestCachingParam(requestCaching, mcHash, paramObj);
 
                                         if (noCache) {
                                             mcHash = null;
                                         }
                                         transformOutput = xslt.exec(xmlPath, xslPath, paramObj, mcHash, flushCache);
                                     }
                                     if (process.xslCommand(transformOutput, response)) {
                                         pageOutputProceed = false;
                                         break;
                                     } else {
                                         pageOutputBuilder.append(transformOutput);
                                     }
                                 }
                             }
                         }
                         process.xslRequestCachingCleanup(requestCaching);
 
                     } else {
                         pageOutputBuilder.append(masterPage);
 
                     }
 
                     if (pageOutputProceed) {
 
                         //set response header
                         response.setDateHeader("Last-Modified", System.currentTimeMillis());
                         response.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate");
 
                         String pageOutput = pageOutputBuilder.toString();
 
                         int headerBreak = pageOutput.indexOf(settings.getYwcXslDelimHeader());
 
                         PrintWriter printOutput = response.getWriter();
 
                         if (headerBreak > 0) {
                             response.setContentType(pageOutput.substring(0, headerBreak));
                             printOutput.println(pageOutput.substring(headerBreak + settings.getYwcXslDelimHeader().length()));
                         } else {
                             response.setContentType("text/html");
 
                             if (headerBreak == 0) {
                                 printOutput.println(pageOutput.substring(settings.getYwcXslDelimHeader().length()));
                             } else if (pageOutput.trim().length() > 0) {
                                 printOutput.println(pageOutput);
                             } else {
                                 response.setStatus(404);
                                 printOutput.println("404: Page Not Found");
                             }
                         }
                     }
                     logger.debug("Tranform: " + (System.currentTimeMillis() - routerStart) + "ms\t" + uri + "\txsl");
 
                 }
             } catch (IOException ex) {
                 logger.error("Render failed for " + uri +" - Exception: " + ex);
                 
             }
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
