 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.snaps.core.internal.webapp;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.eclipse.virgo.util.io.IOUtils;
 
 
 /**
  * TODO Document StaticResourceServlet
  * <p />
  *
  * <strong>Concurrent Semantics</strong><br />
  *
  * TODO Document concurrent semantics of StaticResourceServlet
  *
  */
 public class StaticResourceServlet extends HttpServlet {
     
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private static final long serialVersionUID = 7966666229126801978L;
 
     /** 
      * {@inheritDoc}
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {                        
        String pathInfo = request.getPathInfo();
         try {
             URL resource = getServletContext().getResource(pathInfo);
             if (resource == null) {
                 logger.warn("Resource {} not found", pathInfo);
                 response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource '" + pathInfo + "' not found.");
             } else {
                 logger.info("Resource {} found", pathInfo);
                 InputStream input = null;
                 try {
                     input = new BufferedInputStream(resource.openStream());
                     OutputStream output = response.getOutputStream();
                     byte[] buffer = new byte[4096];
                     int len;
                     while ((len = input.read(buffer)) > 0) {
                         output.write(buffer, 0, len);
                     }
                     output.flush();
                 } finally {
                     if (input != null) {
                         IOUtils.closeQuietly(input);
                     }
                 }
             }
         } catch (MalformedURLException e) {
             logger.error(String.format("Malformed servlet path %s", pathInfo), e);
             throw new ServletException("Malformed servlet path " + pathInfo, e);
         }
     }
 
 }
