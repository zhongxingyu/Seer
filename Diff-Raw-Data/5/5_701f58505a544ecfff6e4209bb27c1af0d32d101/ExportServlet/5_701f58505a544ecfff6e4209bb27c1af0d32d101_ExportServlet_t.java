 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
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
 package com.flexive.war.servlet;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.security.Role;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.URLDecoder;
 
 /**
  * XML Export servlet
  * <p/>
  * Format:
  * <p/>
  * /export/type/name
  * <p/>
  * TODO:
  * /export/content/pk
  * /export/tree/startnode
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev
  */
 public class ExportServlet implements Servlet {
     private static transient Log LOG = LogFactory.getLog(ExportServlet.class);
 
     private final static String BASEURL = "/export/";
     private ServletConfig servletConfig;
 
     /**
      * {@inheritDoc}
      */
     public void init(ServletConfig servletConfig) throws ServletException {
         this.servletConfig = servletConfig;
     }
 
     /**
      * {@inheritDoc}
      */
     public ServletConfig getServletConfig() {
         return servletConfig;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getServletInfo() {
         return "ExportServlet";
     }
 
     /**
      * {@inheritDoc}
      */
     public void destroy() {
     }
 
     public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
         HttpServletRequest request = (HttpServletRequest) servletRequest;
         HttpServletResponse response = (HttpServletResponse) servletResponse;
         String[] params = URLDecoder.decode(request.getRequestURI().substring(request.getContextPath().length() + BASEURL.length()), "UTF-8").split("/");
         if (params.length == 2 && "type".equals(params[0])) {
             exportType(request, response, params[1]);
             return;
         }
         response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
     }
 
     /**
      * Export a type
      *
      * @param request  request
      * @param response reponse
      * @param type     type name
      * @throws IOException on errors
      */
     private void exportType(HttpServletRequest request, HttpServletResponse response, String type) throws IOException {
         final UserTicket ticket = FxContext.get().getTicket();
         if (!ticket.isInRole(Role.StructureManagement)) {
             LOG.warn("Tried to export type [" + type + "] without being in role StructureManagment!");
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return;
         }
         String xml;
         try {
             xml = EJBLookup.getTypeEngine().export(CacheAdmin.getEnvironment().getType(type).getId());
         } catch (FxApplicationException e) {
             LOG.error(e);
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
             return;
         }
         response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
         response.setHeader("Content-Disposition", "attachment; filename=\"" + type + ".xml\";");
         try {
            response.getOutputStream().write(xml.getBytes("UTF-8"));
         } finally {
             response.getOutputStream().close();
         }
     }
 
 }
