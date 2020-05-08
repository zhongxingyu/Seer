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
 package br.com.sysmap.crux.core.rebind.screen;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import br.com.sysmap.crux.core.client.collection.FastMap;
 import br.com.sysmap.crux.core.client.declarative.TagChild;
 import br.com.sysmap.crux.core.client.declarative.TagChildLazyCondition;
 import br.com.sysmap.crux.core.client.declarative.TagChildLazyConditions;
 import br.com.sysmap.crux.core.client.declarative.TagChildren;
 import br.com.sysmap.crux.core.client.screen.DeclaredLazyWidgets;
 import br.com.sysmap.crux.core.client.screen.LazyPanelFactory;
 import br.com.sysmap.crux.core.client.screen.LazyPanelFactory.LazyPanelWrappingType;
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.rebind.AbstractInterfaceWrapperProxyCreator;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.scanner.module.Module;
 import br.com.sysmap.crux.core.rebind.scanner.module.Modules;
 import br.com.sysmap.crux.core.rebind.scanner.screen.Screen;
 import br.com.sysmap.crux.core.rebind.scanner.screen.Widget;
 import br.com.sysmap.crux.core.rebind.scanner.screen.config.WidgetConfig;
 import br.com.sysmap.crux.core.rebind.widget.WidgetFactoryHelper;
 
 import com.google.gwt.core.ext.GeneratorContext;
 import com.google.gwt.core.ext.TreeLogger;
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JMethod;
 import com.google.gwt.core.ext.typeinfo.NotFoundException;
 import com.google.gwt.logging.client.LogConfiguration;
 import com.google.gwt.user.rebind.SourceWriter;
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 public class DeclaredLazyWidgetsProxyCreator extends AbstractInterfaceWrapperProxyCreator
 {
 	private WidgetLazyChecker defaultLazyChecker = new WidgetLazyChecker() 
 	{
 		public boolean isLazy(Widget widget) 
 		{
 			String visible = widget.getProperty("visible");
 			return visible != null && !Boolean.parseBoolean(visible);
 		}
 	};
 	private Map<String, WidgetLazyChecker> lazyWidgetCheckers = new HashMap<String, WidgetLazyChecker>();
 	
 	/**
 	 * Constructor
 	 * @param logger
 	 * @param context
 	 */
 	public DeclaredLazyWidgetsProxyCreator(TreeLogger logger, GeneratorContext context, JClassType invokerIntf)
     {
 	    super(logger, context, invokerIntf);
     }
 	
 	/**
 	 * @see br.com.sysmap.crux.core.rebind.AbstractProxyCreator#generateProxyContructor(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateProxyContructor(SourceWriter srcWriter) throws CruxGeneratorException
 	{
 	}
 
 	/**
 	 * @see br.com.sysmap.crux.core.rebind.AbstractProxyCreator#generateProxyFields(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateProxyFields(SourceWriter srcWriter) throws CruxGeneratorException
 	{
 		srcWriter.println("private static Logger logger = Logger.getLogger(DeclaredLazyWidgets.class.getName());");
 		
 	}
 
 	/**
 	 * @see br.com.sysmap.crux.core.rebind.AbstractProxyCreator#generateProxyMethods(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateProxyMethods(SourceWriter srcWriter) throws CruxGeneratorException
 	{
 		srcWriter.println("public FastMap<String> getLazyWidgets(String screenId){");
 		srcWriter.indent();
 		srcWriter.println("FastMap<String> result = new FastMap<String>();");
 		
 		List<Screen> screens = getScreens();
 		boolean first = true;
 		for (Screen screen : screens)
 		{
 			if (!first)
 			{
 				srcWriter.print("else ");
 			}
 			first = false;
 			generateGetLazyBlock(srcWriter, screen);
 		}
 		
 		srcWriter.println("return result;");
 		srcWriter.outdent();
 		srcWriter.println("}");
 	}
 
 	/**
 	 * @see br.com.sysmap.crux.core.rebind.AbstractProxyCreator#generateSubTypes(com.google.gwt.user.rebind.SourceWriter)
 	 */
 	@Override
 	protected void generateSubTypes(SourceWriter srcWriter) throws CruxGeneratorException
 	{
 	}
 
 	/**
 	 * @see br.com.sysmap.crux.core.rebind.AbstractInterfaceWrapperProxyCreator#getImports()
 	 */
 	@Override
     protected String[] getImports()
     {
 		String[] imports = new String[] {
 				FastMap.class.getCanonicalName(),
 				DeclaredLazyWidgets.class.getCanonicalName(),
 				Logger.class.getCanonicalName(), 
 				Level.class.getCanonicalName(), 
 				LogConfiguration.class.getCanonicalName()
 		};
 		return imports;       
     }
 
 	/**
 	 * Return true if the parent widget informed, must render its children lazily.
 	 * 
 	 * @param parent
 	 * @return
 	 * @throws NotFoundException 
 	 */
 	private boolean checkLazy(Widget parent) throws NotFoundException 
 	{
 		return defaultLazyChecker.isLazy(parent);
 	}
 
 	/**
 	 * Return true if the parent widget informed, must render its children lazily.
 	 * 
 	 * @param parent
 	 * @return
 	 * @throws NotFoundException 
 	 */
 	private boolean checkChildrenLazy(Widget parent) throws NotFoundException 
 	{
 		if (!lazyWidgetCheckers.containsKey(parent.getType()))
 		{
 			initializeLazyChecker(parent.getType());
 		}
 		WidgetLazyChecker checker = lazyWidgetCheckers.get(parent.getType());
 		return checker != null && checker.isLazy(parent);
 	}
 
 	/**
 	 * @param srcWriter
 	 * @param widget
 	 * @param lazyId
 	 */
 	private void generateAddLazyMapEntry(SourceWriter srcWriter, String widgetId, String lazyId)
     {
 	    srcWriter.println("if (LogConfiguration.loggingIsEnabled()){");
 	    srcWriter.indent();
 		srcWriter.println("logger.log(Level.FINE, "+EscapeUtils.quote("Adding lazy dependency (resolved at compile time). Widget["+EscapeUtils.quote(widgetId)+"] depends on ["+EscapeUtils.quote(lazyId)+"].")+");");
 	    srcWriter.outdent();
 	    srcWriter.println("}");
 	    srcWriter.println("result.put("+EscapeUtils.quote(widgetId)+", "+EscapeUtils.quote(lazyId)+");");
     }
 
 	/**
 	 * @param srcWriter
 	 * @param screen
 	 */
 	private void generateGetLazyBlock(SourceWriter srcWriter, Screen screen)
 	{
 		try
         {
 	        srcWriter.println("if (screenId.endsWith("+EscapeUtils.quote(getScreenId(screen))+")){");
 	        srcWriter.indent();
 
 	        Iterator<Widget> widgets = screen.iterateWidgets();
 	        while (widgets.hasNext())
 	        {
 	        	Widget widget = widgets.next();
 	        	Widget parent = widget.getParent();
 	        	while (parent != null)
 	        	{
	        		if(checkChildrenLazy(parent))
 	        		{
	        			String lazyId = LazyPanelFactory.getLazyPanelId(parent.getId(), LazyPanelWrappingType.wrapChildren);
 	        			generateAddLazyMapEntry(srcWriter, widget.getId(), lazyId);
 	        			break;
 	        		}
	        		else if(checkLazy(parent))
 	        		{
	        			String lazyId = LazyPanelFactory.getLazyPanelId(parent.getId(), LazyPanelWrappingType.wrapWholeWidget);
 	        			generateAddLazyMapEntry(srcWriter, widget.getId(), lazyId);
 	        			break;
 	        		}
 	        		else
 	        		{
 	        			parent = parent.getParent();
 	        		}
 	        	}
 	        }
 	        srcWriter.outdent();
 	        srcWriter.println("}");
         }
         catch (NotFoundException e)
         {
         	throw new CruxGeneratorException(e);//TODO message
         }
 	}
 
 	/**
 	 * @param screen
 	 * @return
 	 */
 	private String getScreenId(Screen screen)
 	{
 		Module module = Modules.getInstance().getModule(screen.getModule());
 		return Modules.getInstance().getRelativeScreenId(module, screen.getId());
 	}
 
 	/**
 	 * @param childrenMethod
 	 * @param factoryHelper
 	 * @param declaredCheckers
 	 * @throws NotFoundException
 	 */
 	private void initializeLazyChecker(JMethod childrenMethod, WidgetFactoryHelper factoryHelper, List<WidgetLazyChecker> declaredCheckers) throws NotFoundException
     {
 		TagChildren tagChildren = childrenMethod.getAnnotation(TagChildren.class);
 		if (tagChildren != null)
 		{
 			for (TagChild child : tagChildren.value())
             {
 				JClassType childProcessor = context.getTypeOracle().getType(child.value().getCanonicalName());
 				TagChildLazyConditions lazyConditions = childProcessor.getAnnotation(TagChildLazyConditions.class);
 				if (lazyConditions != null)
 				{
 					WidgetLazyChecker lazyChecker = initializeLazyChecker(lazyConditions);
 					if (lazyChecker != null)
 					{
 						declaredCheckers.add(lazyChecker);
 					}
 				}
 				JMethod childProcessorMethod = factoryHelper.getChildProcessorMethod(childProcessor);
 				initializeLazyChecker(childProcessorMethod, factoryHelper, declaredCheckers);
             }
 		}
     }
 
 	/**
 	 * @param type
 	 * @throws NotFoundException 
 	 */
 	private void initializeLazyChecker(String type) throws NotFoundException 
 	{
 		String widgetFactoryClass = WidgetConfig.getClientClass(type);
 		JClassType factoryType = context.getTypeOracle().getType(widgetFactoryClass);
 		WidgetFactoryHelper factoryHelper = new WidgetFactoryHelper(factoryType);
 		
 		JMethod childrenMethod = factoryHelper.getProcessChildrenMethod();
 		
 		final List<WidgetLazyChecker> declaredCheckers = new ArrayList<WidgetLazyChecker>();
 		initializeLazyChecker(childrenMethod, factoryHelper, declaredCheckers);
 		if (declaredCheckers.size() == 0)
 		{
 			lazyWidgetCheckers.put(type, null);
 		}
 		else if (declaredCheckers.size() == 1)
 		{
 			lazyWidgetCheckers.put(type, declaredCheckers.get(0));
 		}
 		else
 		{
 			lazyWidgetCheckers.put(type, new WidgetLazyChecker()
 			{
 				public boolean isLazy(Widget widget)
 				{
 					boolean ret = false;
 					for (WidgetLazyChecker widgetLazyChecker : declaredCheckers)
                     {
 	                    ret = ret || widgetLazyChecker.isLazy(widget);
                     }
 					return ret;
 				}
 			});
 		}
 		
 	}
 
 	/**
 	 * @param lazyConditions
 	 * @return
 	 */
 	private WidgetLazyChecker initializeLazyChecker(final TagChildLazyConditions lazyConditions)
     {
 	    if (lazyConditions.all().length > 0)
 	    {
 	    	return new WidgetLazyChecker()
 			{
 				public boolean isLazy(Widget widget)
 				{
 					boolean lazy = true;
 					for (TagChildLazyCondition lazyCondition : lazyConditions.all())
                     {
 						String property = widget.getProperty(lazyCondition.property());
 						if (lazyCondition.equals().length() > 0)
 						{
 							lazy = lazy && (property != null && property.equals(lazyCondition.equals()));
 						}
 						else if (lazyCondition.notEquals().length() > 0)
 						{
 							lazy = lazy && (property == null || !property.equals(lazyCondition.notEquals()));
 						}
 						if (!lazy)
 						{
 							break;
 						}
                     }
 					
 					return lazy;
 				}
 			}; 
 	    }
 	    else if (lazyConditions.any().length > 0)
 	    {
 	    	return new WidgetLazyChecker()
 			{
 				public boolean isLazy(Widget widget)
 				{
 					boolean lazy = false;
 					for (TagChildLazyCondition lazyCondition : lazyConditions.any())
                     {
 						String property = widget.getProperty(lazyCondition.property());
 						if (lazyCondition.equals().length() > 0)
 						{
 							lazy = lazy || (property != null && property.equals(lazyCondition.equals()));
 						}
 						else if (lazyCondition.notEquals().length() > 0)
 						{
 							lazy = lazy || (property == null || !property.equals(lazyCondition.notEquals()));
 						}
 						if (lazy)
 						{
 							break;
 						}
                     }
 					
 					return lazy;
 				}
 			}; 
 	    }
 	    return null;
     }
 	
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	private static interface WidgetLazyChecker
 	{
 		boolean isLazy(Widget widget);
 	}	
 }
