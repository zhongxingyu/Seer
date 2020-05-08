 /*
  * C5Connector.Java - The Java backend for the filemanager of corefive.
  * It's a bridge between the filemanager and a storage backend and 
  * works like a transparent VFS or proxy.
  * Copyright (C) Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU General Public License Version 2 or later (the "GPL")
  *    http://www.gnu.org/licenses/gpl.html
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  */
 package de.thischwa.c5c.requestcycle;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpMethods;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.thischwa.c5c.FilemanagerAction;
 import de.thischwa.c5c.UserObjectProxy;
 import de.thischwa.c5c.exception.C5CException;
 import de.thischwa.c5c.exception.FilemanagerException;
 
 /**
  * Maintains the base request parameter.
  */
 public class Context {
 	private static Logger logger = LoggerFactory.getLogger(Context.class);
 
 	/** action of the filemanager */
 	private FilemanagerAction mode;
 	
 	/** the given path from url pram */
 	private String urlPath;
 
 	private HttpServletRequest servletRequest;
 
 	Context(HttpServletRequest servletRequest) throws C5CException {
 		this.servletRequest = servletRequest;
 		urlPath = servletRequest.getParameter("path");
 		String paramMode;
		if(servletRequest.getMethod().equals(HttpMethods.POST)) {
 			try {
 				paramMode = IOUtils.toString(servletRequest.getPart("mode").getInputStream());
 			} catch (Exception e) {
 				logger.error("Couldn't retrieve the 'mode' parameter from multipart.");
 				throw new C5CException(UserObjectProxy.getFilemanagerErrorMessage(FilemanagerException.Key.ModeError));
 			}
 		} else {
 			paramMode = servletRequest.getParameter("mode");
 		}
 		if (paramMode == null)
 			throw new IllegalArgumentException("Missing 'mode' parameter.");
 		try {
 			mode = FilemanagerAction.valueOfIgnoreCase(paramMode);
 		} catch (IllegalArgumentException e) {
 			logger.error("Unknown 'mode': {}", mode);
 			throw new C5CException(UserObjectProxy.getFilemanagerErrorMessage(FilemanagerException.Key.ModeError));
 		}
 	}
 	
 	/**
 	 * Gets the mode (the action of the filemanager).
 	 * 
 	 * @return the mode
 	 */
 	public FilemanagerAction getMode() {
 		return mode;
 	}
 	
 	/**
 	 * Gets the 'path' parameter from the url.
 	 * 
 	 * @return the parameter 'path' from the url
 	 */
 	public String getUrlPath() {
 		return urlPath;
 	}
 	
 	/**
 	 * Gets the {@link HttpServletRequest}.
 	 * 
 	 * @return the servlet request
 	 */
 	public HttpServletRequest getServletRequest() {
 		return servletRequest;
 	}
 }
