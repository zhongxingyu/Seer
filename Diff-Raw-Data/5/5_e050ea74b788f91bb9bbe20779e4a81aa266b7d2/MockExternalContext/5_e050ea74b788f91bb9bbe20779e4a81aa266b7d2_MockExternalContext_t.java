 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
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
 package com.flexive.tests.embedded.jsf.util;
 
 import javax.faces.context.ExternalContext;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.Servlet;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.Principal;
 import java.util.*;
 
 /**
  * Mock external faces context for unit testing. If your code needs a method,
  * add a reasonable (dummy) implementation.
  * 
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  *
  */
 public class MockExternalContext extends ExternalContext {
     public static class MockServletContext implements ServletContext {
         public void setAttribute(String s, Object o) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void removeAttribute(String s) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getServletContextName() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getContextPath() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public ServletContext getContext(String s) {
            return this;
         }
 
         public int getMajorVersion() {
             return 0;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public int getMinorVersion() {
             return 0;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getMimeType(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public Set getResourcePaths(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public URL getResource(String s) throws MalformedURLException {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public InputStream getResourceAsStream(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public RequestDispatcher getRequestDispatcher(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public RequestDispatcher getNamedDispatcher(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public Servlet getServlet(String s) throws ServletException {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public Enumeration getServlets() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public Enumeration getServletNames() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void log(String s) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void log(Exception e, String s) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public void log(String s, Throwable throwable) {
             //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getRealPath(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getServerInfo() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public String getInitParameter(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public Enumeration getInitParameterNames() {
            return Collections.enumeration(new ArrayList());
         }
 
         public Object getAttribute(String s) {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         public Enumeration getAttributeNames() {
             return null;  //To change body of implemented methods use File | Settings | File Templates.
         }
     }
     private Map requestParameterMap = new HashMap();
 	private Map requestMap = new HashMap();
 	private Map applicationMap = new HashMap();
 	private Map sessionMap = new HashMap();
 
 	@Override
 	public void dispatch(String path) throws IOException {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String encodeActionURL(String url) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String encodeNamespace(String name) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String encodeResourceURL(String url) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getApplicationMap() {
 		return applicationMap;
 	}
 
 	@Override
 	public String getAuthType() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Object getContext() {
 		return new MockServletContext();
 	}
 
 	@Override
 	public String getInitParameter(String name) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getInitParameterMap() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String getRemoteUser() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Object getRequest() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String getRequestContextPath() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getRequestCookieMap() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getRequestHeaderMap() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getRequestHeaderValuesMap() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Locale getRequestLocale() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Iterator getRequestLocales() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getRequestMap() {
 		return requestMap;
 	}
 
 	@Override
 	public Map getRequestParameterMap() {
 		return requestParameterMap;
 	}
 
 	@Override
 	public Iterator getRequestParameterNames() {
 		return requestParameterMap.keySet().iterator();
 	}
 
 	@Override
 	public Map getRequestParameterValuesMap() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String getRequestPathInfo() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public String getRequestServletPath() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public URL getResource(String path) throws MalformedURLException {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public InputStream getResourceAsStream(String path) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Set getResourcePaths(String path) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Object getResponse() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Object getSession(boolean create) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public Map getSessionMap() {
 		return sessionMap;
 	}
 
 	@Override
 	public Principal getUserPrincipal() {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public boolean isUserInRole(String role) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public void log(String message, Throwable exception) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public void log(String message) {
 		throw new RuntimeException("not implemented");
 	}
 
 	@Override
 	public void redirect(String url) throws IOException {
 		throw new RuntimeException("not implemented");
 	}
 
 }
