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
 package br.com.sysmap.crux.gwt.rebind;
 
 import java.util.LinkedList;
 
 import org.json.JSONObject;
 
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.screen.widget.AttributeProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreator;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreatorContext;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasAllFocusHandlersFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasAllKeyHandlersFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasAllMouseHandlersFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasAnimationFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasCloseHandlersFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasOpenHandlersFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.HasSelectionHandlersFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.ChoiceChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.HasPostProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.WidgetChildProcessor.AnyWidget;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttribute;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributes;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChild;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChildren;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEventDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEventsDeclaration;
 import br.com.sysmap.crux.gwt.client.LoadImagesEvent;
 
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.Tree.Resources;
 
 
 class TreeContext extends WidgetCreatorContext
 {
 	LinkedList<String> itemStack;
 }
 
 /**
  * A factory for Tree widgets
  * @author Thiago da Rosa de Bustamante
  */
 @DeclarativeFactory(id="tree", library="gwt", targetWidget=Tree.class)
 @TagAttributes({
 	@TagAttribute(value="tabIndex", type=Integer.class),
 	@TagAttribute(value="accessKey", type=Character.class),
 	@TagAttribute(value="openSelectedItem", type=Boolean.class, processor=TreeFactory.OpenSelectedItemAttributeParser.class),
 	@TagAttribute(value="focus", type=Boolean.class)
 })
 @TagAttributesDeclaration({
 	@TagAttributeDeclaration(value="useLeafImages", type=Boolean.class)
 })
 @TagEventsDeclaration({
 	@TagEventDeclaration("onLoadImage")
 })
 @TagChildren({
 	@TagChild(TreeFactory.TreeItemProcessor.class)
 })
 public class TreeFactory extends WidgetCreator<TreeContext> 
        implements HasAnimationFactory<TreeContext>, HasAllFocusHandlersFactory<TreeContext>,
                   HasOpenHandlersFactory<TreeContext>, HasCloseHandlersFactory<TreeContext>, 
                   HasAllMouseHandlersFactory<TreeContext>, HasAllKeyHandlersFactory<TreeContext>,
                   HasSelectionHandlersFactory<TreeContext>
 {
 	protected GWTMessages messages = MessagesFactory.getMessages(GWTMessages.class);
 	
 	@Override
 	public String instantiateWidget(SourcePrinter out, JSONObject metaElem, String widgetId) throws CruxGeneratorException
 	{
 		String varName = createVariableName("widget");
 		String className = Tree.class.getCanonicalName();
 		
 		String eventLoadImage = metaElem.optString("onLoadImage");
 		
		if (!StringUtils.isEmpty(eventLoadImage))
 		{
 			String loadEvent = createVariableName("evt");
 			String event = createVariableName("evt");
 			String treeImages = createVariableName("treeImages");
 			
 			out.println("final Event "+event+" = Events.getEvent("+EscapeUtils.quote("onLoadImage")+", "+ EscapeUtils.quote(eventLoadImage)+");");
 			out.println(LoadImagesEvent.class.getCanonicalName()+"<"+className+"> "+loadEvent+
 					" = new "+LoadImagesEvent.class.getCanonicalName()+"<"+className+">("+EscapeUtils.quote(widgetId)+");");
 			out.println(Resources.class.getCanonicalName()+" "+treeImages+
 					" = ("+Resources.class.getCanonicalName()+") Events.callEvent("+event+", "+loadEvent+");");
 
 			String useLeafImagesStr = metaElem.optString("useLeafImages");
 			boolean useLeafImages = true;
 			if (useLeafImagesStr != null && useLeafImagesStr.length() > 0)
 			{
 				useLeafImages = (Boolean.parseBoolean(useLeafImagesStr));
 			}
 			
 			out.println(className + " " + varName+" = new "+className+"("+treeImages+", "+useLeafImages+");");
 		}
 		else
 		{
 			out.println(className + " " + varName+" = new "+className+"();");
 		}
 		
 		return varName;
 	}
 	
 	@Override
 	public void processAttributes(SourcePrinter out, TreeContext context) throws CruxGeneratorException 
 	{
 		super.processAttributes(out, context);
 	}
 
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 */
 	public static class OpenSelectedItemAttributeParser extends AttributeProcessor<TreeContext>
 	{
 		public void processAttribute(SourcePrinter out, TreeContext context, String propertyValue) 
 		{
 			if(Boolean.parseBoolean(propertyValue))
 			{
 				String widget = context.getWidget();
 				out.println(widget+".ensureSelectedItemVisible();");
 			}
 		}
 	}
 	
 	@TagChildAttributes(tagName="item", minOccurs="0", maxOccurs="unbounded")
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration(value="state", type=Boolean.class),
 		@TagAttributeDeclaration(value="selected", type=Boolean.class)
 	})
 	@TagChildren({
 		@TagChild(TreeCaptionProcessor.class),
 		@TagChild(TreeItemProcessor.class)
 	})
 	public static class TreeItemProcessor extends WidgetChildProcessor<TreeContext> implements HasPostProcessor<TreeContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, TreeContext context) throws CruxGeneratorException 
 		{
 			context.itemStack = new LinkedList<String>();
 		}
 		
 		public void postProcessChildren(SourcePrinter out, TreeContext context) throws CruxGeneratorException 
 		{
 			context.itemStack.removeFirst();
 		}
 	}
 	
 	@TagChildren({
 		@TagChild(TextCaptionProcessor.class),
 		@TagChild(WidgetCaptionProcessor.class)
 	})
 	public static class TreeCaptionProcessor extends ChoiceChildProcessor<TreeContext> {}
 	
 	@TagChildAttributes(tagName="textTitle", type=String.class)
 	public static class TextCaptionProcessor extends WidgetChildProcessor<TreeContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, TreeContext context) throws CruxGeneratorException 
 		{
 			String textCaption = ensureTextChild(context.getChildElement(), true);
 			
 			String parent = context.itemStack.peek();
 			String rootWidget = context.getWidget();
 			String currentItem = getWidgetCreator().createVariableName("item");
 			out.println(TreeItem.class.getCanonicalName()+" "+currentItem+";");
 			
 			if (parent == null)
 			{
 				out.println(currentItem+" = "+rootWidget+".addItem("+EscapeUtils.quote(textCaption)+");");
 			}
 			else
 			{
 				out.println(currentItem+" = "+parent+".addItem("+EscapeUtils.quote(textCaption)+");");
 			}
 			context.itemStack.addFirst(currentItem);
 		}
 	}	
 
 	@TagChildAttributes(tagName="widgetTitle")
 	@TagChildren({
 		@TagChild(WidgetCaptionWidgetProcessor.class)
 	})
 	public static class WidgetCaptionProcessor extends WidgetChildProcessor<TreeContext> {}	
 
 	@TagChildAttributes(type=AnyWidget.class)
 	public static class WidgetCaptionWidgetProcessor extends WidgetChildProcessor<TreeContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, TreeContext context) throws CruxGeneratorException 
 		{
 			String child = getWidgetCreator().createChildWidget(out, context.getChildElement());
 
 			String parent = context.itemStack.peek();
 			String currentItem = getWidgetCreator().createVariableName("item");
 			out.println(TreeItem.class.getCanonicalName()+" "+currentItem+";");
 			if (parent == null)
 			{
 				String rootWidget = context.getWidget();
 				out.println(currentItem+" = "+rootWidget+".addItem("+child+");");
 			}
 			else
 			{
 				out.println(currentItem+" = "+parent+".addItem("+child+");");
 			}
 			context.itemStack.addFirst(currentItem);
 		}
 	}
 
 	@Override
     public TreeContext instantiateContext()
     {
 	    return new TreeContext();
     }
 }
