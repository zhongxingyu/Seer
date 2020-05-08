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
 package br.com.sysmap.crux.widgets.rebind.wizard;
 
 import org.json.JSONObject;
 
 import br.com.sysmap.crux.core.client.utils.EscapeUtils;
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.GeneratorMessages;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreator;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreatorContext;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.screen.widget.creator.children.WidgetChildProcessor;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChild;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagChildren;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvent;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagEvents;
 import br.com.sysmap.crux.widgets.client.wizard.Wizard.NoData;
 import br.com.sysmap.crux.widgets.client.wizard.WizardPage;
 
 /**
  * @author Thiago da Rosa de Bustamante -
  *
  */
 @DeclarativeFactory(id="wizardPage", library="widgets", targetWidget=WizardPage.class)
 @TagAttributesDeclaration({
 	@TagAttributeDeclaration(value="wizardId", required=true),
 	@TagAttributeDeclaration("wizardContextObject")
 })
 @TagEvents({
 	@TagEvent(EnterEvtBind.class),
 	@TagEvent(LeaveEvtBind.class)
 })
 @TagChildren({
 	@TagChild(WizardPageFactory.CommandsProcessor.class)
 })
 public class WizardPageFactory extends WidgetCreator<WidgetCreatorContext>
 {
 	private static GeneratorMessages messages = (GeneratorMessages)MessagesFactory.getMessages(GeneratorMessages.class);
 
 	@Override
 	public String instantiateWidget(SourcePrinter out, JSONObject metaElem, String widgetId) throws CruxGeneratorException
 	{
 		String varName = createVariableName("widget");
 	    String wizardContextObject = metaElem.optString("wizardContextObject");
 		String className = getGenericSignature(wizardContextObject);
		out.println("final "+className + " " + varName+" = new "+className+"("+EscapeUtils.quote(widgetId)+", "+EscapeUtils.quote(wizardContextObject)+");");
 		return varName;
 	}
 
 	/**
 	 * @param wizardContextObject
 	 * @return
 	 */
 	public String getGenericSignature(String wizardContextObject)
 	{
 	    String className = getWidgetClassName();
 	    String wizardData = WizardDataObjects.getWizardData(wizardContextObject);
 	    if (StringUtils.isEmpty(wizardData))
 	    {
 	    	return className+"<"+NoData.class.getCanonicalName()+">";
 	    }
 		return className +"<"+wizardData+">";
 	}
 	
 	@TagChildAttributes(tagName="commands", minOccurs="0")
 	@TagChildren({
 		@TagChild(WizardCommandsProcessor.class)
 	})
 	public static class CommandsProcessor extends WidgetChildProcessor<WidgetCreatorContext> {}
 	
 	@TagChildAttributes(tagName="command", maxOccurs="unbounded")
 	@TagAttributesDeclaration({
 		@TagAttributeDeclaration(value="id", required=true),
 		@TagAttributeDeclaration(value="label", required=true, supportsI18N=true),
 		@TagAttributeDeclaration(value="order", required=true, type=Integer.class),
 		@TagAttributeDeclaration("styleName"),
 		@TagAttributeDeclaration("width"),
 		@TagAttributeDeclaration("height"),
 		@TagAttributeDeclaration(value="onCommand", required=true)
 	})
 	public static class WizardCommandsProcessor extends WidgetChildProcessor<WidgetCreatorContext>
 	{
 		@Override
 		public void processChildren(SourcePrinter out, WidgetCreatorContext context) throws CruxGeneratorException 
 		{
 			String id = context.readChildProperty("id");
 			if (StringUtils.isEmpty(id))
 			{
 				throw new CruxGeneratorException(messages.screenFactoryWidgetIdRequired());
 			}
 			
 			String label = getWidgetCreator().getDeclaredMessage(context.readChildProperty("label"));
 			int order = Integer.parseInt(context.readChildProperty("order"));
 			
 			String onCommand = context.readChildProperty("onCommand");
 			String commandEvent = getWidgetCreator().createVariableName("evt");
 			out.println("final Event "+commandEvent+" = Events.getEvent("+EscapeUtils.quote("onCommand")+", "+ EscapeUtils.quote(onCommand)+");");
 			
 			String rootWidget = context.getWidget();
 			out.println(rootWidget+".addCommand("+EscapeUtils.quote(id)+", "+EscapeUtils.quote(label)+", "+commandEvent+", "+order+");");
 			
 			String styleName = context.readChildProperty("styleName");
 			if (!StringUtils.isEmpty(styleName))
 			{
 				out.println(rootWidget+".getCommand("+EscapeUtils.quote(id)+").setStyleName("+EscapeUtils.quote(styleName)+");");
 			}
 			String width = context.readChildProperty("width");
 			if (!StringUtils.isEmpty(width))
 			{
 				out.println(rootWidget+".getCommand("+EscapeUtils.quote(id)+").setWidth("+EscapeUtils.quote(width)+");");
 			}
 			String height = context.readChildProperty("height");
 			if (!StringUtils.isEmpty(height))
 			{
 				out.println(rootWidget+".getCommand("+EscapeUtils.quote(id)+").setHeight("+EscapeUtils.quote(height)+");");
 			}
 		}
 	}
 	
 	@Override
     public WidgetCreatorContext instantiateContext()
     {
 	    return new WidgetCreatorContext();
     }
 }
