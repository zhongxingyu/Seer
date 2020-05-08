 /**
  * Copyright (c) 2012 Committers of lunifera.org.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    Florian Pirchner - initial API and implementation
  */
 package org.lunifera.runtime.web.http;
 
 import org.lunifera.runtime.web.jetty.JettyConstants;
 import org.osgi.service.cm.ManagedServiceFactory;
 
 /**
  * Constants for this bundle.
  */
 public interface HttpConstants extends org.osgi.framework.Constants {
 
 	/**
 	 * The persistence ID used for {@link ManagedServiceFactory}
 	 */
	public static final String OSGI__FACTORY_PID = "lunifera.http.application.factory";
 
 	/**
 	 * Property for the id of the http application.
 	 */
 	public static final String APPLICATION_ID = "lunifera.http.id";
 
 	/**
 	 * Property for the name of a {@link IHttpApplication}.
 	 */
 	public static final String APPLICATION_NAME = "lunifera.http.name";
 
 	/**
 	 * Property for the context path that is used for the http application.
 	 */
 	public static final String CONTEXT_PATH = "lunifera.http.contextPath";
 
 	/**
 	 * Is used to define the jetty server name the http application should
 	 * become installed at.
 	 */
 	public static final String JETTY_SERVER_NAME = JettyConstants.JETTY_SERVER_NAME;
 
 	/**
 	 * The default name of an {@link IHttpApplication}
 	 */
 	public static final String DEFAULT_APPLICATION_NAME = "defaultapplication";
 
 	/**
 	 * The default context path of an {@link IHttpApplication}
 	 */
 	public static final String DEFAULT_APPLICATION_CONTEXT_PATH = "/";
 }
