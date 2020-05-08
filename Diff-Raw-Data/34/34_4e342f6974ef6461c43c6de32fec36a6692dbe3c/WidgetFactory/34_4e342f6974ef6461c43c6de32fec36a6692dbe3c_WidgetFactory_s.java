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
 
 import br.com.sysmap.crux.core.client.Crux;
 import br.com.sysmap.crux.core.client.collection.Array;
 import br.com.sysmap.crux.core.client.declarative.TagAttribute;
 import br.com.sysmap.crux.core.client.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.client.declarative.TagEvent;
 import br.com.sysmap.crux.core.client.declarative.TagEvents;
 import br.com.sysmap.crux.core.client.event.bind.AttachEvtBind;
 import br.com.sysmap.crux.core.client.event.bind.DettachEvtBind;
 import br.com.sysmap.crux.core.client.event.bind.LoadWidgetEvtBind;
 import br.com.sysmap.crux.core.client.screen.parser.CruxMetaDataElement;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.client.utils.StyleUtils;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Factory for gwt widgets. It creates widgets based on a meta data array contained
  * in the host HTML page. It provides a declarative way to create widgets.
  * 
  * @author Thiago da Rosa de Bustamante
  */
 public abstract class WidgetFactory <T extends Widget, C extends WidgetFactoryContext>
 {
 	private static int currentId = 0;
 	
 	private static ScreenFactory factory = null;
 	private static Screen screen = null;
 	
 	public WidgetFactory() 
 	{
 		// For better performance, reduce the javascript scope search for those variables;
 		if (factory == null)
 		{
 			factory = ScreenFactory.getInstance();
 			screen = factory.getScreen();
 		}
 	}
 	
 	/**Retrieve the widget child element name
 	 * @param childElement the span element representing the child
 	 * @return child name
 	 */
 	public static String getChildName(CruxMetaDataElement childElement)
 	{
 		return childElement.getProperty("childTag");
 	}
 	
 	/**
	 * Used by widgets that need to create new widgets as children, like tree. 
 	 * 
 	 * @param element
 	 * @param widgetId
 	 * @return
 	 * @throws InterfaceConfigException
 	 */
 	protected static Widget createChildWidget(CruxMetaDataElement metaElem) throws InterfaceConfigException
 	{
 		assert(metaElem.containsKey("id")):Crux.getMessages().screenFactoryWidgetIdRequired();
 		String widgetId = metaElem.getProperty("id");
 		return factory.newWidget(metaElem, widgetId, factory.getMetaElementType(metaElem));
 	}
 
 	/**
	 * Used by widgets that need to create new widgets as children, like tree. 
 	 * 
 	 * @param element
 	 * @param widgetId
 	 * @param widgetType
 	 * @return
 	 * @throws InterfaceConfigException
 	 */
 	protected static Widget createChildWidget(CruxMetaDataElement metaElem, String widgetId, String widgetType) throws InterfaceConfigException
 	{
 		return factory.newWidget(metaElem, widgetId, widgetType);
 	}
 	
 	/**
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 */
 	protected static Array<CruxMetaDataElement> ensureChildren(CruxMetaDataElement metaElem, boolean acceptsNoChild) 
 	{
 		assert(acceptsNoChild || metaElem.containsKey("children")):Crux.getMessages().widgetFactoryEnsureChildrenSpansEmpty();
 		
 		Array<CruxMetaDataElement> children = metaElem.getChildren();
 		if (acceptsNoChild && children == null)
 		{
 			return null;
 		}
 		assert(acceptsNoChild || (children != null && children.size()>0 && children.get(0)!=null)):Crux.getMessages().widgetFactoryEnsureChildrenSpansEmpty();
 		return children;
 	}
 
 	/**
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 */
 	protected static CruxMetaDataElement ensureFirstChild(CruxMetaDataElement metaElem, boolean acceptsNoChild) throws InterfaceConfigException
 	{
 		assert(acceptsNoChild || metaElem.containsKey("children")):Crux.getMessages().widgetFactoryEnsureChildrenSpansEmpty();
 		Array<CruxMetaDataElement> children = metaElem.getChildren();
 		if (acceptsNoChild && children == null)
 		{
 			return null;
 		}
 		assert(acceptsNoChild || (children != null && children.size()>0)):Crux.getMessages().widgetFactoryEnsureChildrenSpansEmpty();
 		CruxMetaDataElement firstChild = children.get(0);
 		assert(acceptsNoChild || firstChild != null):Crux.getMessages().widgetFactoryEnsureChildrenSpansEmpty();
 		return firstChild;
 	}
 
 	/**
 	 * 
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 */
 	protected static String ensureTextChild(CruxMetaDataElement metaElem, boolean acceptsNoChild)
 	{
 		String result = metaElem.getProperty("_text");
 		assert(acceptsNoChild || (result != null && result.length() > 0)):Crux.getMessages().widgetFactoryEnsureTextChildEmpty();
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @param acceptsNoChild
 	 * @return
 	 */
 	protected static String ensureHtmlChild(CruxMetaDataElement metaElem, boolean acceptsNoChild)
 	{
 		String result = metaElem.getProperty("_html");
 		assert(acceptsNoChild || (result != null && result.length() > 0)):Crux.getMessages().widgetFactoryEnsureHtmlChildEmpty();
 		return result;
 	}
 
 	/**
 	 * Creates a sequential id
 	 * @return
 	 */
 	protected static String generateNewId() 
 	{
 		return "_crux_" + (++currentId );
 	}
 	
 	/**
 	 * @param widgetId
 	 * @return
 	 */
 	protected static Element getEnclosingPanelElement(String widgetId)
 	{
 		return factory.getEnclosingPanelElement(widgetId);
 	}
 	
 	/**
 	 * @return
 	 */
 	protected static String getEnclosingPanelPrefix()
 	{
 		return ScreenFactory.ENCLOSING_PANEL_PREFIX;
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	protected static boolean hasHeight(CruxMetaDataElement metaElem)
 	{
 		String width = metaElem.getProperty("height");
 		return width != null && (width.length() > 0);
 	}
 
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	protected static boolean hasWidth(CruxMetaDataElement metaElem)
 	{
 		String width = metaElem.getProperty("width");
 		return width != null && (width.length() > 0);
 	}
 	
 	/**
 	 * 
 	 * @param metaElem
 	 * @return
 	 */
 	protected static boolean isWidget(CruxMetaDataElement metaElem)
 	{
 		return factory.isValidWidget(metaElem);
 	}
 
 	/**
 	 * 
 	 * @param element
 	 * @param widgetId
 	 * @return
 	 * @throws InterfaceConfigException
 	 */
 	public final T createWidget(CruxMetaDataElement metaElem, String widgetId) throws InterfaceConfigException
 	{
 		return createWidget(metaElem, widgetId, true);
 	}
 	
 	/**
 	 * 
 	 * @param element
 	 * @param widgetId
 	 * @param addToScreen
 	 * @return
 	 * @throws InterfaceConfigException
 	 */
 	@SuppressWarnings("unchecked")
 	public T createWidget(CruxMetaDataElement metaElem, String widgetId, boolean addToScreen) throws InterfaceConfigException
 	{
 		C context = createContext(metaElem, widgetId, addToScreen);
 		if (context != null)
 		{
 			processAttributes(context);
 			processEvents(context);
 			processChildren(context);
 			postProcess(context);
 			return (T)context.getWidget();
 		}
 		return null;
 	}
 	
 	public abstract T instantiateWidget(CruxMetaDataElement metaElem, String widgetId) throws InterfaceConfigException;
 
 	/**
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected C instantiateContext()
 	{
 		return (C)new WidgetFactoryContext();
 	}
 
 	/**
 	 * Process element children
 	 * @param widget
 	 * @param parentElement
 	 * @param widgetId
 	 * @throws InterfaceConfigException 
 	 */
 	public void postProcess(C context) throws InterfaceConfigException
 	{
 	}
 	
 	/**
 	 * Process widget attributes
 	 * @param element page DOM element representing the widget (Its &lt;span&gt; tag)
 	 * @throws InterfaceConfigException 
 	 */
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration(value="id", required=true)
 	})
 	@TagAttributes({
 		@TagAttribute("width"),
 		@TagAttribute("height"),
 		@TagAttribute("styleName"),
 		@TagAttribute(value="visible", type=Boolean.class),
 		@TagAttribute(value="tooltip", supportsI18N=true, property="title"),
 		@TagAttribute(value="style", parser=StyleProcessor.class)
 	})
 	public void processAttributes(C context) throws InterfaceConfigException
 	{
 	}
 	
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	public static class StyleProcessor implements AttributeParser<WidgetFactoryContext>
 	{
 		public void processAttribute(WidgetFactoryContext context, String style)
 		{
 			String[] styleAttributes = style.split(";");
 			Element element = context.getWidget().getElement();
 			for (int i=0; i<styleAttributes.length; i++)
 			{
 				String[] attr = styleAttributes[i].split(":");
 				if (attr != null && attr.length == 2)
 				{
 					StyleUtils.addStyleProperty(element, attr[0], attr[1]);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Process element children
 	 * @param widget
 	 * @param parentElement
 	 * @param widgetId
 	 * @throws InterfaceConfigException 
 	 */
 	public void processChildren(C context) throws InterfaceConfigException
 	{
 	}
 	
 	/**
 	 * Process widget events
 	 * @param element 
 	 * @throws InterfaceConfigException
 	 */
 	@TagEvents({
 		@TagEvent(LoadWidgetEvtBind.class),
 		@TagEvent(AttachEvtBind.class),
 		@TagEvent(DettachEvtBind.class)
 	})
 	public void processEvents(C context) throws InterfaceConfigException
 	{
 	}
 	
 	/**
 	 * Adds an event handler, called when the screen is completely loaded
 	 * @param loadHandler
 	 */
 	protected void addScreenLoadedHandler(ScreenLoadHandler loadHandler)
 	{
 		factory.addLoadHandler(loadHandler);
 	}
 	
 	/**
 	 * @param parentType
 	 * @param parentId
 	 * @param parserElements
 	 */
 	protected void addToParserStack(String parentType, String parentId, Array<CruxMetaDataElement> parserElements)
 	{
 		factory.addToParserStack(parentType, parentId, parserElements);
 	}
 	
 	/**
 	 * @param element
 	 * @param widgetId
 	 * @param addToScreen
 	 * @return
 	 * @throws InterfaceConfigException
 	 */
 	protected C createContext(CruxMetaDataElement metaElem, String widgetId, boolean addToScreen) throws InterfaceConfigException
 	{
 		T widget = instantiateWidget(metaElem, widgetId);
 		if (widget != null)
 		{
 			if(addToScreen)
 			{
 				screen.addWidget(widgetId, widget);
 			}			
 			if (Crux.getConfig().renderWidgetsWithIDs())
 			{
 				updateWidgetElementId(widgetId, widget);
 			}
 			C context = instantiateContext();
 			context.setWidget(widget);
 			context.setWidgetElement(metaElem);
 			context.setWidgetId(widgetId);
 			context.setChildElement(metaElem);
 			return context;
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param element
 	 * @return
 	 */
 	protected CruxMetaDataElement ensureWidget(CruxMetaDataElement metaElem) 
 	{
 		assert(isWidget(metaElem)):Crux.getMessages().widgetFactoryEnsureWidgetFail();
 		return metaElem;
 	}
 	
 	/**
 	 * @param widgetId
 	 * @param widget
 	 */
 	protected void updateWidgetElementId(String widgetId, T widget)
     {
 	    Element element = widget.getElement();
 	    if (StringUtils.isEmpty(element.getId()))
 	    {
 	    	element.setId(widgetId);
 	    }
     }
 }
