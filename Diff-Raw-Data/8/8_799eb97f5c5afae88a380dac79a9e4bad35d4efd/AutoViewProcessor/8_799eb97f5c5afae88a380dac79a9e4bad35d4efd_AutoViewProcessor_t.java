 /*
  * AutoViewProcessor.java (weberknecht)
  *
  * Copyright 2012 Patrick Mairif.
  * The program is distributed under the terms of the Apache License (ALv2).
  * 
  * tabstop=4, charset=UTF-8
  */
 package de.highbyte_le.weberknecht.request.view;
 
 import java.io.IOException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.highbyte_le.weberknecht.request.ContentProcessingException;
 import de.highbyte_le.weberknecht.request.Executable;
 import de.highbyte_le.weberknecht.request.actions.ActionExecutionException;
 
 /**
  * Let the executable choose on its own which view processor to use.
  * 
  * @author pmairif
  */
 public class AutoViewProcessor implements ActionViewProcessor {
 	
 	private final Log log = LogFactory.getLog(AutoViewProcessor.class);
 	
 	private ServletContext servletContext = null;
 	
 	private ActionViewProcessorFactory factory = null;
 	
 	/* (non-Javadoc)
 	 * @see de.highbyte_le.weberknecht.request.ActionProcessor#processAction(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, de.highbyte_le.weberknecht.request.ExecutableAction)
 	 */
 	@Override
 	public void processView(HttpServletRequest request, HttpServletResponse response, Executable executable)
 			throws ServletException, IOException, ContentProcessingException, ActionExecutionException {
 		
 		if (log.isDebugEnabled())
 			log.debug("processView() - processing action "+executable.getClass().getSimpleName());
 
		if (!(executable instanceof AutoView))
 			throw new IllegalArgumentException("Executable not applicable here.");
 
 		String viewProcessorName = ((AutoView) executable).getViewProcessorName();
 		ActionViewProcessor processor = factory.createActionProcessor(viewProcessorName, servletContext);
 		
 		if (processor instanceof AutoViewProcessor)		//avoid endless loops
 			throw new IllegalArgumentException("auto view recursivly requested");
 
 		if (null == processor)
 			log.error("processView() - no view processor for " + executable.getClass().getName());
 		else
 			processor.processView(request, response, executable);
 	}
 	
 	@Override
 	public void setServletContext(ServletContext servletContext) {
 		this.servletContext = servletContext;
 	}
 	
 	/* (non-Javadoc)
 	 * @see de.highbyte_le.weberknecht.request.view.ActionViewProcessor#setActionViewProcessorFactory(de.highbyte_le.weberknecht.request.view.ActionViewProcessorFactory)
 	 */
 	@Override
 	public void setActionViewProcessorFactory(ActionViewProcessorFactory factory) {
 		this.factory = factory;
 	}
 }
