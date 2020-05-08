 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.buglabs.osgi.http;
 
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogService;
 
 /**
  * Manages the list of Servlets in the runtime.
  * 
  * @author ken
  * 
  */
 class HttpServiceImpl implements HttpService {
 
 	private SharedStateManager sm;
 
 	private HttpContext defaultHttpContext = null;
 
 	private Map httpContexts = null;
 
 	private final Dictionary bundleHeaders;
 
 	public HttpServiceImpl(SharedStateManager sm, Dictionary headers) {
 		this.sm = sm;
 		this.bundleHeaders = headers;
		httpContexts = new Hashtable();
 	}
 
 	public HttpContext createDefaultHttpContext() {
 
 		return new DefaultHttpContext();
 	}
 
 	public void registerResources(String alias, String name, HttpContext context) throws NamespaceException {
 		throw new RuntimeException("registerResources is not implemented.");
 	}
 
 	public void registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext context) throws ServletException,
 			NamespaceException {
 
 		SharedStateManager.validateAlias(alias);
 
 		// OSGi R3 Section 14.2 - Only one servlet is allowed to occupy a
 		// specific part of the namespace.
 		if (sm.hasServlet(alias)) {
 			throw new NamespaceException("Alias " + alias + " has already been registered.");
 		}
 
 		// OSGi R3 Section 14.2 - A default HttpContext should be created if
 		// none is passed from client.
 		if (context == null) {
 			if (defaultHttpContext == null) {
 				// Initialize contexts
 				defaultHttpContext = new DefaultHttpContext();
 			}
 			context = defaultHttpContext;
 		}
		httpContexts.put(context, new ServletContextImpl(initparams, bundleHeaders, alias, context, sm));
 
 		sm.addHttpContext(alias, context);
 		sm.addServletConfig(alias, (ServletConfig) httpContexts.get(context));
 
 		// OSGi R3 Section 14.2 - Each unique HttpContext should have a
 		// corresponding ServletContext
 		ServletContext servletContext = (ServletContext) httpContexts.get(context);
 
 		if (servletContext == null) {
 			servletContext = new ServletContextImpl(initparams, bundleHeaders, alias, defaultHttpContext, sm);
 			httpContexts.put(context, servletContext);
 		}
 
 		ServletConfig sc = new BaseServletConfig(alias, servletContext, initparams);
 		initializeServlet(servlet, sc);
 
 		sm.addServlet(alias, servlet);
 		sm.log(LogService.LOG_INFO, "Registered servlet " + alias);
 
 	}
 
 	private void initializeServlet(Servlet servlet, ServletConfig sc) throws ServletException {
 		servlet.init(sc);
 	}
 
 	public void unregister(String alias) {
 		if (sm.removeServlet(alias)) {
 			sm.log(LogService.LOG_INFO, "Unregistered servlet " + alias);
 			return;
 		}
 
 		throw new IllegalArgumentException("Unknown servlet: " + alias);
 	}
 
 	private class BaseServletConfig implements ServletConfig {
 		private final ServletContext context;
 
 		private final Dictionary params;
 
 		private final String alias;
 
 		public BaseServletConfig(String alias, ServletContext context, Dictionary params) {
 			this.alias = alias;
 			this.context = context;
 			this.params = params;
 		}
 
 		public String getInitParameter(String arg0) {
 			return (String) params.get(arg0);
 		}
 
 		public Enumeration getInitParameterNames() {
 			return params.keys();
 		}
 
 		public ServletContext getServletContext() {
 
 			return context;
 		}
 
 		public String getServletName() {
 
 			return alias;
 		}
 
 	}
 }
