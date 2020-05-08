 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.filter;
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.interfaces.TemplateEngine;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.tree.FxTemplateInfo;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.war.webdav.FxWebDavUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.lang.StringUtils;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 public class FxFilter implements Filter {
     private static final Log LOG = LogFactory.getLog(FxFilter.class);
 
     private final static String CATALINA_CLIENT_ABORT = "org.apache.catalina.connector.ClientAbortException";
 
     private String FILESYSTEM_WAR_ROOT = null;
     private FilterConfig config = null;
     private static String PATH_DIVISION = "division_";
     private static final String X_POWERED_BY_VALUE = "[fleXive]";
     private static volatile long lastTagChange = -1;
     private final static long CONTENT_TYPE_IMAGE = 2;//TODO
 
     /**
      * Returns the root of the war directory on the filesystem.
      *
      * @return The root of the war directory on the filesystem.
      */
     public String getFileystemWarRoot() {
         return FILESYSTEM_WAR_ROOT;
     }
 
 
     /**
      * Returns the filter configuration.
      *
      * @return the filter configuration
      */
     public FilterConfig getConfiguration() {
         return config;
     }
 
     /**
      * Destroy function
      */
     public void destroy() {
         // nothing to do
 
     }
 
 
     /**
      * @param servletRequest  -
      * @param servletResponse -
      * @param filterChain     -
      * @throws IOException
      * @throws ServletException
      */
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
         final int divisionId = getDivisionId(servletRequest, servletResponse);
         if (divisionId == FxContext.DIV_UNDEFINED) {
             return;
         }
         // Check if division could be resolved
         servletResponse.setCharacterEncoding("UTF-8");
         servletRequest.setCharacterEncoding("UTF-8");
 
         try {
             final boolean isWebdav = FxWebDavUtils.isWebDavRequest((HttpServletRequest) servletRequest);
             // Generate a FlexRequestWrapper which stores additional informations
             final FxRequestWrapper request = servletRequest instanceof FxRequestWrapper
                     ? (FxRequestWrapper) servletRequest
                     : new FxRequestWrapper((HttpServletRequest) servletRequest, divisionId, isWebdav);
             request.setCharacterEncoding("UTF-8");
 
             final FxContext si = FxContext.storeInfos(request, request.isDynamicContent(), divisionId, isWebdav);
 
             if (!isWebdav && FxWebDavUtils.isWebDavPropertyMethod((HttpServletRequest) servletRequest)) {
                 // This is an invalid webdav request - send a not allowed flag and kill the session immediatly
                 ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                 ((HttpServletRequest) servletRequest).getSession().invalidate();
                 return;
             }
 
             // Servlets are not handled by the flex filter, so we abort processing here
             if (request.isServlet()) {
                 filterChain.doFilter(request, new FxResponseWrapper(servletResponse, false));
                 return;
             }
 
             // Cache data for jsp,jsf,and xhtml pages only
             final boolean catchData = request.isDynamicContent();
 
             // Wrap the response to provide additional features (content length counting, caching)
             final FxResponseWrapper response =
                     (servletResponse instanceof FxResponseWrapper)
                             ? (FxResponseWrapper) servletResponse
                             : new FxResponseWrapper(servletResponse, catchData);
 
 //            if (isFinalPath(request)) {
             if (request.isCmsUrl()) {
                 // Resolve CMS request
                 ResolvedPath rp = processPage(request, si, FILESYSTEM_WAR_ROOT);
                 if (rp.getStatus() != HttpServletResponse.SC_OK) {
                     response.sendError(rp.getStatus());
                     return;
                 }
                 request.forward(response, rp.getPath());
             } else {
                 // resolve standard application request
                 if (needsLogin(request, si)) {
                     if (request.isSetupURL()) {
                         request.forward(response, "/" + FxRequestWrapper.PATH_SETUP + "/globalconfig.fx?action=showLogin");
                     } else {
                        final String loginPath = "/" + FxRequestWrapper.PATH_PUBLIC + "/login.jsf";
                        if( !loginPath.equals(request.getRealRequestUriNoContext()))
                            request.forward(response, loginPath);
                        else {
                            final String msg = "Multiple redirects attempted to " + loginPath;
                            LOG.warn(msg);
                            response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
                        }
                     }
                 } else if (restrictBackendAccess(request, si)) {
                     //user is in backend but may not access it
                     request.forward(response, "/" + FxRequestWrapper.PATH_PUBLIC + "/backendRestricted.jsf");
                 } else {
                     try {
                         filterChain.doFilter(request, response);
                     } catch (ServletException e) {
                         LOG.error(e, e.getRootCause());
                         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getRootCause());
                     } catch (Throwable t) {
                         // Failed to process the page
                         LOG.error(t, t);
                         response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t);
                     }
                 }
             }
             try {
                 if ("faces".equals(request.getPageType()) || "jsf".equals(request.getPageType()) || "xhtml".equals(request.getPageType())
                         || StringUtils.defaultString(response.getContentType()).contains("application/xhtml+xml")) {
                     // TODO: flag response as XHTML only if our response actually is valid XHTML (which it currently is not)
                     response.setContentType("text/html");
                 } else if ("css".equalsIgnoreCase(request.getPageType())) {
                     response.setContentType("text/css");
                 }
 
                 response.setXPoweredBy(X_POWERED_BY_VALUE);
                 if (request.browserMayCache()) {
                     response.enableBrowserCache(FxResponseWrapper.CacheControl.PRIVATE, null, false);
                 } else {
                     response.disableBrowserCache();
                 }
 
                 if (!response.isClientWriteThrough()) {
                     // Manually write the final response to the client
                     if (response.hadError()) {
                         response.getWrappedResponse().reset();
                         response.disableBrowserCache();
                         String error = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >\n" +
                                 "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                                 "<head>\n" +
                                 "<title>[fleXive] Error Report</title>\n" +
                                 //(js==null?"":js)+
                                 "</head>\n" +
                                 "<body style=\"background-color:white;\">\n" +
                                 "<h1 style=\"color:red;\"> Error Code: " + response.getStatus() + "</h1><br/>" +
                                 response.getStatusMsg().replaceAll("\n", "<br/>") + "\n" +
                                 "</body>\n" +
                                 "</html>";
                         response.writeToUnderlyingResponse(error);
                     } else {
                         response.writeToUnderlyingResponse(null);
                     }
                 } else {
                     // nothing
                 }
 //            } catch (org.apache.catalina.connector.ClientAbortException e) {
 //                LOG.debug("Client aborted transfer: " + e);
             } catch (Exception e) {
                 if (CATALINA_CLIENT_ABORT.equals(e.getClass().getCanonicalName())) {
                     LOG.debug("Client aborted transfer: " + e);
                 } else
                     LOG.error("FxFilter caught exception: " + e.getMessage(), e);
             }
         } finally {
             FxContext.cleanup();
         }
 
     }
 
     private int getDivisionId(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
         int divisionId = FxRequestUtils.getDivision((HttpServletRequest) servletRequest);
         if (FxRequestUtils.getCookie((HttpServletRequest) servletRequest, FxSharedUtils.COOKIE_FORCE_TEST_DIVISION) != null) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Test division cookie set - overriding division retrieved from request URI.");
             }
             divisionId = DivisionData.DIVISION_TEST;
         }
         if (divisionId == FxContext.DIV_UNDEFINED) {
             ((HttpServletResponse) servletResponse).
                     sendError(HttpServletResponse.SC_NOT_FOUND, "Division not defined or configuration error");
         }
         return divisionId;
     }
 
     /**
      * Init function.
      *
      * @param filterConfig -
      * @throws ServletException -
      */
     public void init(FilterConfig filterConfig) throws ServletException {
         // Remember the configuration
         this.config = filterConfig;
         // Get the war deployment directory root on the server file system
         // Eg. "/opt/jboss-4.0.3RC1/server/default/./tmp/deploy/tmp52986Demo.ear-contents/web.war/"
         this.FILESYSTEM_WAR_ROOT = filterConfig.getServletContext().getRealPath("/");
     }
 
     /**
      * Returns true if the path within the request does not need an forward processing.
      *
      * @param si      the request information
      * @param request the request
      * @return true if the path within the request does not need an forward processing
      */
     private static boolean needsLogin(final FxRequestWrapper request, final FxContext si) {
         UserTicket ticket = si.getTicket();
         if (!ticket.isGuest())
             return false;
         boolean inAdminArea = request.isAdminURL() && !request.isServletRedirectorURL();
         if (inAdminArea)
             return true;
         boolean inSetupArea = request.isSetupURL() && request.getSession().getAttribute(FxContext.ADMIN_AUTHENTICATED) == null
                 && !"login".equals(request.getParameter("action"));
         if (inSetupArea)
             return true;
         // TODO config admin: use clean routine
         return false;
     }
 
     /**
      * Should access to the backend be restricted?
      *
      * @param si      the request information
      * @param request the request
      * @return true if access should be restricted
      */
     private static boolean restrictBackendAccess(final FxRequestWrapper request, final FxContext si) {
         UserTicket ticket = si.getTicket();
         /*if( ticket.isGuest() )
             return true;*/
         boolean inAdminArea = request.isAdminURL() && !request.isServletRedirectorURL();
         return inAdminArea && !ticket.isInRole(Role.BackendAccess);
     }
 
 
     public ResolvedPath processPage(final FxRequestWrapper request, final FxContext si,
                                     final String fsRoot) {
         String sRelURI = si.getRelativeRequestURI();
         if (sRelURI.equals("/")) {
             return getNotFoundUri(si);
         }
 
         String DIVPATH = "/webdav/division_" + si.getDivisionId() + "/";
         if (sRelURI.startsWith(DIVPATH)) {
             sRelURI = sRelURI.substring(DIVPATH.length() - 1);
         }
 
         FileWriter out = null;
         try {
             long nodeId = getNode(si);
             if (nodeId == -1) {
                 LOG.error("Failed to map content URL (" + si.getRelativeRequestURI() + "): "
                         + si.getRelativeRequestURI());
                 return getNotFoundUri(si);
             }
 
             FxTemplateInfo template = EJBLookup.getTemplateEngine().getTemplate(nodeId, FxTreeMode.Edit);
             long idchain[] = EJBLookup.getTreeEngine().getIdChain(FxTreeMode.Edit, nodeId);
             request.setIdChain(idchain);
             request.setTemplateInfo(template);
             request.setNodeId(nodeId);
             si.setNodeId(nodeId);
 
             if (template == null || template.getId() == -1) {
 
                 // If this is a image, and no template is set, we just deliver it to the client
                 if (template.getTdef() == CONTENT_TYPE_IMAGE) {
                     return (new ResolvedPath("/thumbnail/pk" + template.getContentId(), HttpServletResponse.SC_OK));
                 }
 
                 // TODO: handle other case!
 
             } else {
                 // Taglib up-to-date check
                 taglibUpToDateCheck();
 
                 // Template
                 String divRootPath = fsRoot + "division_" + si.getDivisionId();
                 String nodeRootPath = "/" + nodeId + "/";
                 String contentType = template.getContentType();
                 String fileType = contentType.equalsIgnoreCase("JSF") ? "xhtml" : contentType;
                 String nodeFileNoExt = template.getModifiedAt() + "_" + template.getMasterTemplateModifiedAt();
                 File file;
 
                 // Master template
                 String masterTemplateURI = null;
                 if (template.hasMasterTemplate()) {
                     String baseDir = "division_" +
                             si.getDivisionId() + "/masterTemplates/" + template.getMasterTemplate();
                     masterTemplateURI = baseDir + "/" + template.getMasterTemplateModifiedAt() + ".xhtml";
                     file = new File(fsRoot + masterTemplateURI);
                     if (!file.exists()) {
                         recreateDir(fsRoot + baseDir);
                         file.createNewFile();
                         out = new FileWriter(file);
                         out.write(EJBLookup.getTemplateEngine().getFinalContent(template.getMasterTemplate(), null, FxTreeMode.Edit)); // TODO LIVE/EDIT
                         out.close();
                     }
                 }
 
                 // Main template
                 file = new File(divRootPath + nodeRootPath + nodeFileNoExt + "." + fileType);
                 if (!file.exists()) {
                     recreateDir(divRootPath + nodeRootPath);
                     file.createNewFile();
                     out = new FileWriter(file);
                     out.write(EJBLookup.getTemplateEngine().getFinalContent(template.getId(), masterTemplateURI, FxTreeMode.Edit)); // TODO: LIVE/EDIT
                 }
                 //System.out.println("Time needed for CMS node resolve logic: "+(System.currentTimeMillis()-time));
                 return (new ResolvedPath("/" + PATH_DIVISION + si.getDivisionId() + nodeRootPath +
                         nodeFileNoExt + "." + contentType, HttpServletResponse.SC_OK));
             }
         } catch (Exception e) {
             LOG.error(e.getMessage(), e);
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (Throwable t) {/*ignore*/}
             }
         }
         return (new ResolvedPath("/" + PATH_DIVISION + si.getDivisionId() + sRelURI, HttpServletResponse.SC_NOT_FOUND));
     }
 
     private long getNode(final FxContext si) throws FxApplicationException {
         if (si.getRelativeRequestURI(false).startsWith("/+")) {
             final String nodeId = si.getRelativeRequestURI().substring(2);
             try {
                 return Long.parseLong(nodeId);
             } catch (NumberFormatException e) {
                 throw new FxInvalidParameterException("url", "ex.cms.request.invalidId", nodeId).asRuntimeException();
             }
         } else if (si.getRelativeRequestURI(false).startsWith(FxRequestWrapper.PATH_CMS)) {
             return EJBLookup.getTreeEngine().getIdByFQNPath(FxTreeMode.Edit, FxTreeNode.ROOT_NODE,
                     si.getRelativeRequestURI().substring(FxRequestWrapper.PATH_CMS.length()));
         }
         return -1;
     }
 
 
     /**
      * Ensures that the taglibs are up-to-date
      */
     private synchronized void taglibUpToDateCheck() {
         long tagChange = EJBLookup.getTemplateEngine().getLastChange(TemplateEngine.Type.TAG, FxTreeMode.Edit);
         if (tagChange != lastTagChange) {
             lastTagChange = tagChange;
             _writeCmsTagLibs(FILESYSTEM_WAR_ROOT);
         }
     }
 
     /**
      * Rewrites all TAG files to the application servers filesystem.
      *
      * @param _FILESYSTEM_WAR_ROOT the root directory of the WAR layer
      */
     private synchronized void _writeCmsTagLibs(String _FILESYSTEM_WAR_ROOT) {
         // Determine the directory that holds all tag files
         String temlateDir = _FILESYSTEM_WAR_ROOT + "WEB-INF/cmsTemplates";
         // Delete the whole directory containing the cms tags
         recreateDir(temlateDir);
         // Retrieve all tags and write them to the filesystem
         FileWriter out = null;
         try {
             List<FxTemplateInfo> tags = EJBLookup.getTemplateEngine().list(TemplateEngine.Type.TAG);
             for (FxTemplateInfo tag : tags) {
                 String content = EJBLookup.getTemplateEngine().getFinalContent(tag.getId(), null, FxTreeMode.Edit); // TODO: LIVE/EDIT
                 File file = new File(temlateDir + "/tag" + tag.getTypeId() + ".xhtml");
                 file.createNewFile();
                 out = new FileWriter(file);
                 out.write(content);
                 out.close();
                 out = null;
             }
         } catch (Throwable t) {
             System.err.println(t.getMessage());
             t.printStackTrace();
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (Throwable t) {/*ignore*/}
             }
         }
     }
 
     private static ResolvedPath getNotFoundUri(FxContext si) {
         return new ResolvedPath("/" + PATH_DIVISION + si.getDivisionId(), HttpServletResponse.SC_NOT_FOUND);
     }
 
     public static void recreateDir(String path) {
         File _dir = new File(path);
         deleteDir(_dir);
         _dir.mkdirs();
     }
 
     public static boolean deleteDir(File dir) {
         if (!dir.exists()) {
             return false;
         }
         if (dir.isDirectory()) {
             String[] children = dir.list();
             for (String aChildren : children) {
                 boolean success = deleteDir(new File(dir, aChildren));
                 if (!success) {
                     return false;
                 }
             }
         }
         // The directory is now empty so delete it
         return dir.delete();
     }
 
 }
 
 /*
 Reminders:
 TODO: If-Modified-Since (request.getDateHeader( "If-Modified-Since" ): response.setStatus( HttpServletResponse.SC_NOT_MODIFIED );
 */
