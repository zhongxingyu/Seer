 /*******************************************************************************
  * Copyright (c) 2011 IBM Corporation and others 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.orion.server.git.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.orion.internal.server.servlets.ServletResourceHandler;
 import org.eclipse.orion.server.servlets.OrionServlet;
 
 // Describe Git Servlet API here
 public class GitServlet extends OrionServlet {
 
	private static final long serialVersionUID = -6809742538472682623L;
 
 	public static final String GIT_URI = "/git";
 
 	private ServletResourceHandler<String> gitSerializer;
 
 	public GitServlet() {
 		gitSerializer = new ServletGitHandler(getStatusHandler());
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		traceRequest(req);
 		String pathInfo = req.getPathInfo();
 		if (gitSerializer.handleRequest(req, resp, pathInfo))
 			return;
 		// finally invoke super to return an error for requests we don't know
 		// how to handle
 		super.doGet(req, resp);
 	}
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 	@Override
 	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 }
