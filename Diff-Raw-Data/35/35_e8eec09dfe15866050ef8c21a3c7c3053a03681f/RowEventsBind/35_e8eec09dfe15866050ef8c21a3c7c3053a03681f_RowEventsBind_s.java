 /*
  * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.sysmap.crux.widgets.rebind.event;
 
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.rebind.screen.widget.EvtProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.widgets.client.event.row.BeforeRowSelectEvent;
 import br.com.sysmap.crux.widgets.client.event.row.BeforeRowSelectHandler;
 import br.com.sysmap.crux.widgets.client.event.row.RowClickEvent;
 import br.com.sysmap.crux.widgets.client.event.row.RowClickHandler;
 import br.com.sysmap.crux.widgets.client.event.row.RowDoubleClickEvent;
 import br.com.sysmap.crux.widgets.client.event.row.RowDoubleClickHandler;
 import br.com.sysmap.crux.widgets.client.event.row.RowRenderEvent;
 import br.com.sysmap.crux.widgets.client.event.row.RowRenderHandler;
 
 /**
  * All event binders for grid row events
  * @author Gesse S. F. Dafe
  */
 public class RowEventsBind
 {
 	/**
 	 * @author Gesse S. F. Dafe
 	 */
 	public static class RowClickEvtBind extends EvtProcessor
 	{
		private static final String EVENT_NAME = "onrowclick";
 
 		@Override
 	    public void processEvent(SourcePrinter out, String eventValue, String widget, String widgetId)
 	    {
 			String event = ViewFactoryCreator.createVariableName("evt");
 			
 			out.println("final Event "+event+" = Events.getEvent("+EscapeUtils.quote(getEventName())+", "+ EscapeUtils.quote(eventValue)+");");
 			out.println(widget+".addRowClickHandler(new "+RowClickHandler.class.getCanonicalName()+"(){");
 			out.println("public void onRowClick("+RowClickEvent.class.getCanonicalName()+" event){");
 			out.println("Events.callEvent("+event+", event);");
 			out.println("}");
 			out.println("});");
 	    }		
 
 		/**
 		 * @see br.com.sysmap.crux.core.rebind.screen.widget.EvtProcessor#getEventName()
 		 */
 		public String getEventName()
 		{
 			return EVENT_NAME;
 		}		
 	}
 	
 	/**
 	 * @author Gesse S. F. Dafe
 	 */
 	public static class RowDoubleClickEvtBind extends EvtProcessor
 	{
		private static final String EVENT_NAME = "onrowdoubleclick";
 
 		@Override
 	    public void processEvent(SourcePrinter out, String eventValue, String widget, String widgetId)
 	    {
 			String event = ViewFactoryCreator.createVariableName("evt");
 			
 			out.println("final Event "+event+" = Events.getEvent("+EscapeUtils.quote(getEventName())+", "+ EscapeUtils.quote(eventValue)+");");
 			out.println(widget+".addRowDoubleClickHandler(new "+RowDoubleClickHandler.class.getCanonicalName()+"(){");
 			out.println("public void onRowDoubleClick("+RowDoubleClickEvent.class.getCanonicalName()+" event){");
 			out.println("Events.callEvent("+event+", event);");
 			out.println("}");
 			out.println("});");
 	    }		
 		
 		/**
 		 * @see br.com.sysmap.crux.core.rebind.screen.widget.EvtProcessor#getEventName()
 		 */
 		public String getEventName()
 		{
 			return EVENT_NAME;
 		}		
 	}
 	
 	/**
 	 * @author Gesse S. F. Dafe
 	 */
 	public static class RowRenderEvtBind extends EvtProcessor
 	{
		private static final String EVENT_NAME = "onrowrender";
 
 		@Override
 	    public void processEvent(SourcePrinter out, String eventValue, String widget, String widgetId)
 	    {
 			String event = ViewFactoryCreator.createVariableName("evt");
 			
 			out.println("final Event "+event+" = Events.getEvent("+EscapeUtils.quote(getEventName())+", "+ EscapeUtils.quote(eventValue)+");");
 			out.println(widget+".addRowRenderHandler(new "+RowRenderHandler.class.getCanonicalName()+"(){");
 			out.println("public void onRowRender("+RowRenderEvent.class.getCanonicalName()+" event){");
 			out.println("Events.callEvent("+event+", event);");
 			out.println("}");
 			out.println("});");
 	    }		
 
 		/**
 		 * @see br.com.sysmap.crux.core.rebind.screen.widget.EvtProcessor#getEventName()
 		 */
 		public String getEventName()
 		{
 			return EVENT_NAME;
 		}		
 	}
 	
 	/**
 	 * @author Gesse S. F. Dafe
 	 */
 	public static class BeforeRowSelectEvtBind extends EvtProcessor
 	{
		private static final String EVENT_NAME = "onbeforerowselect";
 
 		@Override
 	    public void processEvent(SourcePrinter out, String eventValue, String widget, String widgetId)
 	    {
 			String event = ViewFactoryCreator.createVariableName("evt");
 			
 			out.println("final Event "+event+" = Events.getEvent("+EscapeUtils.quote(getEventName())+", "+ EscapeUtils.quote(eventValue)+");");
 			out.println(widget+".addBeforeRowSelectHandler(new "+BeforeRowSelectHandler.class.getCanonicalName()+"(){");
 			out.println("public void onBeforeRowSelect("+BeforeRowSelectEvent.class.getCanonicalName()+" event){");
 			out.println("Events.callEvent("+event+", event);");
 			out.println("}");
 			out.println("});");
 	    }		
 
 		/**
 		 * @see br.com.sysmap.crux.core.rebind.screen.widget.EvtProcessor#getEventName()
 		 */
 		public String getEventName()
 		{
 			return EVENT_NAME;
 		}		
 	}
 }
