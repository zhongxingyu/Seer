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
 package br.com.sysmap.crux.core.client.screen;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import br.com.sysmap.crux.core.client.Crux;
 import br.com.sysmap.crux.core.client.collection.Array;
 import br.com.sysmap.crux.core.client.collection.CollectionFactory;
 import br.com.sysmap.crux.core.client.screen.WidgetLazyRuntimeCheckers.WidgetLazyChecker;
 import br.com.sysmap.crux.core.client.screen.parser.CruxMetaData;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.logging.client.LogConfiguration;
 import com.google.gwt.user.client.ui.LazyPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * A Factory that wraps an element with a panel which content is only rendered when it is accessed for the first time.
  * 
  * @author Thiago da Rosa de Bustamante
  */
 public class LazyPanelFactory implements HasWidgetsFactory<LazyPanel>
 {
 	private static Logger logger = Logger.getLogger(LazyPanelFactory.class.getName());
 	
 	static final String LAZY_PANEL_TYPE = "_CRUX_LAZY_PANEL_";
 	private static final String LAZY_CHILDREN_PANEL_PREFIX = "_chld_";
 	private static final String LAZY_PANEL_PREFIX = "_lazy_";
 	private static final int LAZY_PANEL_PREFIX_LENGTH = LAZY_CHILDREN_PANEL_PREFIX.length();
 	
 	private static LazyPanelFactory instance = null; 
 	
 	/**
 	 * Singleton constructor
 	 */
 	private LazyPanelFactory() 
 	{
 	}
 	
 	/**
 	 * @return
 	 */
 	public static LazyPanelFactory getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new LazyPanelFactory();
 		}
 		return instance;
 	}
 	
 	/**
 	 * @see br.com.sysmap.crux.core.client.screen.HasWidgetsFactory#add(com.google.gwt.user.client.ui.Widget, java.lang.String, com.google.gwt.user.client.ui.Widget, java.lang.String)
 	 */
 	public void add(LazyPanel parent, String parentId, Widget widget, String widgetId)
 	{
 		parent.add(widget);
 	}
 	
 
 	/**
 	 * Return the id created to the panel that wraps the given widget id.
 	 * @param wrappedWidgetId
 	 * @param wrappingType 
 	 * @return
 	 */
 	public static String getLazyPanelId(String wrappedWidgetId, LazyPanelWrappingType wrappingType)
 	{
 		if (wrappingType == LazyPanelWrappingType.wrapChildren)
 		{
 			return LAZY_CHILDREN_PANEL_PREFIX+wrappedWidgetId;
 		}
 		else
 		{
 			return LAZY_PANEL_PREFIX+wrappedWidgetId;
 		}
 	}
 	
 	/**
 	 * Return the id of the widget wrapped by the given lazy panel id.
 	 * @param lazyPanelId
 	 * @return
 	 */
 	public static String getWrappedWidgetIdFromLazyPanel(String lazyPanelId)
 	{
 		assert(lazyPanelId != null && lazyPanelId.length() > LAZY_PANEL_PREFIX_LENGTH);
 		return lazyPanelId.substring(LAZY_PANEL_PREFIX_LENGTH);
 	}
 
 	/**
 	 * Create an wrapper lazyPanel capable of creating an widget for the given CruxMetaData element. 
 	 * 
 	 * @param element CruxMetaData element that will be used to create the wrapped widget
 	 * @param panelId Identifier of the parent panel, that required the lazy wrapping operation.
 	 * @param wrappingType the lazyPanel wrapping model.
 	 * @return
 	 */
 	public static LazyPanel getLazyPanel(final CruxMetaData element, String panelId, LazyPanelWrappingType wrappingType) 
 	{
 		String widgetId = getLazyPanelId(panelId, wrappingType);
 		if (LogConfiguration.loggingIsEnabled())
 		{
 			logger.log(Level.FINE, "Delaying the widget ["+element.getProperty("id")+"] creation. Instantiating a new lazyPanel ["+widgetId+"] to wrap this widget...");
 		}
 		if (Crux.getConfig().enableRuntimeLazyWidgetsInitialization())
 		{
 			maybeBuildLazyDependencyList(element, widgetId);
 		}		
 		
 		return new CruxLazyPanel(element, widgetId);
 	}
 
 	/**
 	 * This method builds the dependency list for a given element. It is only called when 
 	 * Crux is configured to build the dependency list in runtime. Runtime strategy is only used when application is running in 
 	 * debug mode. In production, that list is created during GWT compilation, by a generator. 
 	 * 
 	 * The runtime approach exists to turn development easier, allowing Crux to make hot deployment for its .crux.xml pages
 	 * 
 	 * @param element
 	 * @param parentWidgetId
 	 */
 	private static void maybeBuildLazyDependencyList(final CruxMetaData element, String parentWidgetId)
 	{
 		if (Crux.getConfig().enableRuntimeLazyWidgetsInitialization())
 		{
 			ScreenFactory factory = ScreenFactory.getInstance();
 			if (!factory.getScreen().containsLazyDependents(parentWidgetId))
 			{
 				if (LogConfiguration.loggingIsEnabled())
 				{
 					logger.log(Level.FINE, "Building runtime lazy dependency list for widget ["+parentWidgetId+"]...");
 				}
 				
 				WidgetLazyRuntimeCheckers checkers = GWT.create(WidgetLazyRuntimeCheckers.class);
 				assert(element.containsKey("id") && factory.isValidWidget(element));
 				factory.getScreen().addLazyWidgetDependency(element.getProperty("id"), parentWidgetId);
 				/*
 				 * if the wrapped element is also an element marked to be rendered lazily, all its children must be dependent
 				 * of a wrapper lazyPanel created around it and not be dependent of the a wrapper created over its parent.
 				 */
 				String lazyId = (mustRenderChildrenLazily(element, checkers)?getLazyPanelId(element.getProperty("id"), LazyPanelWrappingType.wrapChildren):parentWidgetId);
 				buildLazyDependencyListForChildren(element, lazyId, checkers);
 
 				if (LogConfiguration.loggingIsEnabled())
 				{
 					logger.log(Level.FINE, "Runtime lazy dependency list for widget ["+parentWidgetId+"] created.");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This method recursively search the element's children to build the dependency list. It is only called when 
 	 * Crux is configured to build the dependency list in runtime. Runtime strategy is only used when application is running in 
 	 * debug mode. In production, that list is created during GWT compilation, by a generator. 
 	 * 
 	 * The runtime approach exists to turn development easier, allowing Crux to make hot deployment for its .crux.xml pages
 	 * 
 	 * @param element
 	 * @param parentWidgetId
 	 */
 	private static void buildLazyDependencyListForChildren(final CruxMetaData element, String parentWidgetId, WidgetLazyRuntimeCheckers checkers)
 	{
 		Array<CruxMetaData> children = element.getChildren();
 		if (children != null)
 		{
 			ScreenFactory factory = ScreenFactory.getInstance();
 
 			int size = children.size();
 			for (int i=0; i<size; i++)
 			{
 				CruxMetaData child = children.get(i);
 				if (child != null)
 				{
 					String lazyId = parentWidgetId;
 					boolean childIsLazyPanel = false;
 					if (factory.isValidWidget(child))
 					{
 						String childId = child.getProperty("id");
 						factory.getScreen().addLazyWidgetDependency(childId, parentWidgetId);
						if (mustRenderLazily(child, checkers))
 						{
 							childIsLazyPanel = true;
							lazyId = getLazyPanelId(childId, LazyPanelWrappingType.wrapWholeWidget);
 						}
						else if (mustRenderChildrenLazily(child, checkers))
 						{
 							childIsLazyPanel = true;
							lazyId = getLazyPanelId(childId, LazyPanelWrappingType.wrapChildren);
 						}
 					}
 					if (!childIsLazyPanel || !factory.getScreen().containsLazyDependents(lazyId))
 					{
 						buildLazyDependencyListForChildren(child, lazyId, checkers);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @param child
 	 * @param checkers
 	 * @return
 	 */
 	private static boolean mustRenderLazily(CruxMetaData child, WidgetLazyRuntimeCheckers checkers)
 	{
 		String visible = child.getProperty("visible");
 		return (!StringUtils.isEmpty(visible) && !Boolean.parseBoolean(visible));
 	}
 
 	/**
 	 * @param child
 	 * @param checkers
 	 * @return
 	 */
 	private static boolean mustRenderChildrenLazily(CruxMetaData child, WidgetLazyRuntimeCheckers checkers)
 	{
 		WidgetLazyChecker checker = checkers.getWidgetLazyChecker(ScreenFactory.getInstance().getMetaElementType(child));
 		return checker != null && checker.isLazy(child);
 	}
 	
 	/**
 	 * Contains the available lazyPanel wrapping models. {@code wrapChildren} is used 
 	 * by widgets that needs to create some of its children lazily. {@code wrapWholeWidget}
 	 * is used when the whole widget must be rendered lazily, like when {@code ScreenFactory}
 	 * is parsing the CruxMetaData and find an invisible panel.
 	 * 
 	 * @author Thiago da Rosa de Bustamante
 	 */
 	public static enum LazyPanelWrappingType{wrapChildren, wrapWholeWidget}
 	
 	/**
 	 * A default lazy panel implementation, used by Crux engine to lazily parse the entire Document.
 	 * When a panel is declaratively created with visible attribute equals to false, an instance of 
 	 * that class is used to only parse the panel element and create the widget when it is first accessed
 	 * by {@code Screen.getWidget(String)} or when it becomes visible.
 	 * 
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	public static class CruxLazyPanel extends LazyPanel implements br.com.sysmap.crux.core.client.screen.LazyPanel{
 		private boolean initialized = false;
 		private CruxMetaData metaData;
 		private final String lazyId;
 		
 		/**
 		 * Constructor
 		 * @param innerHTML
 		 * @param lazyId
 		 */
 		public CruxLazyPanel(CruxMetaData metaData, String lazyId)
 		{
 			this.metaData = metaData;
 			this.lazyId = lazyId;
 		}
 		
 		/**
 		 * @see com.google.gwt.user.client.ui.LazyPanel#ensureWidget()
 		 */
 		@Override
 		public void ensureWidget()
 		{
 			if (!initialized)
 			{
 				ScreenFactory.getInstance().getScreen().cleanLazyDependentWidgets(lazyId);
 				initialized = true;
 			}
 			super.ensureWidget();
 		}
 
 		/**
 		 * @see com.google.gwt.user.client.ui.LazyPanel#createWidget()
 		 */
 		@Override
 		protected Widget createWidget() 
 		{
 			if (LogConfiguration.loggingIsEnabled())
 			{
 				logger.log(Level.FINE, "Creating ["+lazyId+"] wrapped widget...");
 			}
 			Array<CruxMetaData> children = CollectionFactory.createArray();
 			children.add(metaData);
 			metaData = null;
 			ScreenFactory factory = ScreenFactory.getInstance();
 			factory.addToParserStack(LAZY_PANEL_TYPE, lazyId, children);
 			
 			if (!factory.isParsing())
 			{
 				factory.parseDocument();
 			}
 			if (LogConfiguration.loggingIsEnabled())
 			{
 				logger.log(Level.FINE, "["+lazyId+"] wrapped widget created.");
 			}
 			return getWidget();			
 		}			
 	}
 }
