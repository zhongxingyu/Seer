 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.core.filter;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.paxle.core.queue.CommandEvent;
 import org.paxle.core.queue.ICommand;
 
 public class CommandFilterEvent extends CommandEvent {
 	/* ======================================================================
 	 * Event Topics
 	 * ====================================================================== */
 	/**
 	 * An event that is triggered before the command is passed to a {@link IFilter}
 	 */	
 	public static final String TOPIC_PRE_FILTER = TOPIC_ + "PRE_FILTER";
 	
 	/**
 	 * An event that is triggered after the command was passed to a {@link IFilter}
 	 */	
 	public static final String TOPIC_POST_FILTER = TOPIC_ + "POST_FILTER";
 	
 	/* ======================================================================
 	 * Event Properties
 	 * ====================================================================== */
 	/**
 	 * @see org.paxle.core.filter.IFilter#PROP_FILTER_TARGET
 	 */
 	public static final String PROP_FILTER_TARGET = "filterTarget";	
 	
 	/**
 	 * @see org.paxle.core.filter.IFilter#PROP_FILTER_TARGET_POSITION
 	 */
 	public static final String PROP_FILTER_TARGET_POSITION = "filterPos";
 	
 	/**
 	 * The class-name of the filter
 	 */
 	public static final String PROP_FILTER_NAME = "filterClassName";
 	
 	
 	@SuppressWarnings("unchecked")
 	private static void extractFilterContextProps(IFilterContext context, Dictionary properties) {
 		properties.put(PROP_FILTER_TARGET, context.getTargetID());
 		properties.put(PROP_FILTER_TARGET_POSITION, Integer.valueOf(context.getFilterPosition()));
 		properties.put(PROP_FILTER_NAME, context.getFilter().getClass().getName());
 	}
 	
 	public static Event createEvent(String stageID, String topic, ICommand command, IFilterContext context) {
 		return createEvent(stageID, topic, command, context, null);
 	}
 	
 	public static Event createEvent(String stageID, String topic, ICommand command, IFilterContext context, Throwable exception) {
 		Hashtable<String, Object> props = new Hashtable<String, Object>();
 		
 		// extracting filter-context-props
 		extractFilterContextProps(context, props);
 		
 		// add exception info (if any)
 		if (exception != null) {
 			props.put(EventConstants.EXCEPTION, exception);
 			props.put(EventConstants.EXCEPTION_CLASS, exception.getClass().getName());
			
			String errorMsg = exception.getMessage();
			props.put(EventConstants.EXCEPTION_MESSAGE, (errorMsg==null)?"":exception.getMessage());
 		}
 		
 		// create general and append general command-props 
 		Event event = CommandEvent.createEvent(stageID, topic, command, props);
 		return event;
 	}
 }
