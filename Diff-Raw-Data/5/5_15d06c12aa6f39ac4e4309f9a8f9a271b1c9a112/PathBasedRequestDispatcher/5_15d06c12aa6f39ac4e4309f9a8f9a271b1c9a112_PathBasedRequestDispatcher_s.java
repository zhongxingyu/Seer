 /**
  * Copyright (c) 2010 Ignasi Barrera
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package org.sjmvc.web;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.sjmvc.config.Configuration;
 import org.sjmvc.config.ConfigurationException;
 import org.sjmvc.controller.Controller;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for the {@link RequestDispatcher} implementations.
  * 
  * @author Ignasi Barrera
  */
 public class PathBasedRequestDispatcher implements RequestDispatcher
 {
 	/** The logger. */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(PathBasedRequestDispatcher.class);
 
 	/** Mappings from request path to {@link ResourceMapping} objects. */
 	protected Map<String, ResourceMapping> mappings;
 
 	/**
 	 * Creates the request dispatcher.
 	 * 
 	 * @throws ConfigurationException If controller mapping configuration cannot
 	 *             be read.
 	 */
 	public PathBasedRequestDispatcher() throws ConfigurationException
 	{
 		super();
 		mappings = new HashMap<String, ResourceMapping>();
 		readConfiguration();
 	}
 
 	@Override
 	public void dispatch(final HttpServletRequest req, final HttpServletResponse resp) throws Exception
 	{
 		LOGGER.debug("Looking for a controller to handle request to: {}",
 				req.getRequestURI());
 
 		ResourceMapping mapping = null;
 		String requestedPath = getRequestedPath(req);
 
 		for (String path : mappings.keySet())
 		{
 			if (requestedPath.startsWith(path))
 			{
 				mapping = mappings.get(path);
 
 				LOGGER.debug("Using {} controller to handle request to: {}",
 						mapping.getClass().getName(), req.getRequestURI());
 			}
 		}
 
 		if (mapping != null)
 		{
 			// Instantiate the controller on each request to make it thread-safe
 			Controller controller = mapping.getControllerClass().newInstance();
 
 			// Execute controller logic and get the view to render
 			String viewName = controller.execute(req, resp);
 
 			// Publish the view and layout attributes to render the view
 			if (mapping.getLayout() != null)
 			{
 				String layoutPath = Configuration.LAYOUT_PATH + "/" + mapping.getLayout();
				req.setAttribute(Configuration.CURRENT_VIEW_ATTRIBUTE, layoutPath);
 			}
 
 			String viewPath = Configuration.VIEW_PATH + mapping.getPath()
 					+ "/" + viewName + Configuration.VIEW_SUFFIX;
			req.setAttribute(Configuration.CURRENT_LAYOUT_ATTRIBUTE, viewPath);
 		}
 		else
 		{
 			LOGGER.error("No controller was found to handle request to: {}", req.getRequestURI());
 
 			resp.sendError( HttpServletResponse.SC_NOT_FOUND,
 					"No controller was found to handle request to: "
 							+ req.getRequestURI());
 		}
 	}
 
 	/**
 	 * Get the requested path relative to the servlet path.
 	 * 
 	 * @param req The request.
 	 * @return The requested path.
 	 */
 	private String getRequestedPath(final HttpServletRequest req)
 	{
 		return req.getRequestURI().replaceFirst(req.getContextPath(), "")
 				.replaceFirst(req.getServletPath(), "");
 	}
 
 	/**
 	 * Load configured controller mappings.
 	 * 
 	 * @throws Exception If mappings cannot be loaded.
 	 */
 	protected void readConfiguration() throws ConfigurationException
 	{
 		Properties config = Configuration.getConfiguration();
 
 		LOGGER.info("Loading controller mappings...");
 
 		for (Object mappingKey : config.keySet())
 		{
 			String key = (String) mappingKey;
 
 			if (Configuration.isControllerPathProperty(key))
 			{
 				String path = config.getProperty(key);
 				String clazz = config.getProperty(key.replace(
 						Configuration.CONTROLLER_PATH_SUFFIX,
 						Configuration.CONTROLLER_CLASS_SUFFIX));
 				String layout = config.getProperty(key.replace(
 						Configuration.CONTROLLER_PATH_SUFFIX,
 						Configuration.CONTROLLER_LAYOUT_SUFFIX));
 
 				if (clazz == null)
 				{
 					throw new ConfigurationException("Missing controller class for path: " + path);
 				}
 
 				try
 				{
 					ClassLoader cl = Thread.currentThread().getContextClassLoader();
 
 					@SuppressWarnings("unchecked")
 					Class<Controller> controllerClass = (Class<Controller>) Class.forName(clazz, true, cl);
 
 					ResourceMapping mapping = new ResourceMapping();
 					mapping.setPath(path);
 					mapping.setLayout(layout);
 					mapping.setControllerClass(controllerClass);
 
 					mappings.put(path, mapping);
 
 					LOGGER.info("Mapping {} to {}", path, controllerClass.getName());
 				}
 				catch (Exception ex)
 				{
 					throw new ConfigurationException("Could not get controller class: " + clazz);
 				}
 			}
 		}
 	}
 }
