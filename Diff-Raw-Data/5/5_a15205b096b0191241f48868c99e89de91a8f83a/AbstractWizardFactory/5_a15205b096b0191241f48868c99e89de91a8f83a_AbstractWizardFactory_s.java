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
 
 import br.com.sysmap.crux.core.client.utils.StringUtils;
 import br.com.sysmap.crux.core.i18n.MessagesFactory;
 import br.com.sysmap.crux.core.rebind.CruxGeneratorException;
 import br.com.sysmap.crux.core.rebind.GeneratorMessages;
 import br.com.sysmap.crux.core.rebind.dto.DataObjects;
 import br.com.sysmap.crux.core.rebind.screen.widget.ViewFactoryCreator.SourcePrinter;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreator;
 import br.com.sysmap.crux.core.rebind.screen.widget.WidgetCreatorContext;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributeDeclaration;
 import br.com.sysmap.crux.core.rebind.screen.widget.declarative.TagAttributesDeclaration;
 import br.com.sysmap.crux.widgets.client.wizard.Wizard.NoData;
 import br.com.sysmap.crux.widgets.client.wizard.WizardDataSerializer;
 
 class WizardContext extends WidgetCreatorContext
 {
 
 	public String stepId;
 	public String stepLabel;
 	public String stepOnEnter;
 	public String stepOnLeave;
 	public String enabled;
 	public String wizardObject;
 }
 
 
 /**
  * @author Thiago da Rosa de Bustamante -
  *
  */
 @TagAttributesDeclaration({
 	@TagAttributeDeclaration("wizardContextObject")
 })
 public abstract class AbstractWizardFactory extends WidgetCreator<WizardContext>
 {
 	protected static GeneratorMessages messages = (GeneratorMessages)MessagesFactory.getMessages(GeneratorMessages.class);
 
 	/**
 	 * @param wizardData
 	 * @param wizardContextObject
 	 * @return
 	 * @throws CruxGeneratorException
 	 */
 	protected String getWizardSerializerInterface(String wizardContextObject) throws CruxGeneratorException
     {
 		String wizardData;
 		if (StringUtils.isEmpty(wizardContextObject))
 		{
 			wizardContextObject = "Null";
 			wizardData = NoData.class.getCanonicalName();
 		}
 		else
 		{
 			wizardData = DataObjects.getDataObject(wizardContextObject);
 		}
 		String subType = wizardContextObject+"_Serializer";
 		
 		SourcePrinter printer = getSubTypeWriter(subType, null, 
 				new String[]{WizardDataSerializer.class.getCanonicalName()+"<"+wizardData+">"}, null, true);
		printer.commit();
 		return subType;
     }
 
 	/**
 	 * @param wizardContextObject
 	 * @return
 	 */
 	public String getGenericSignature(String wizardContextObject)
 	{
 	    String className = getWidgetClassName();
 	    String wizardData = DataObjects.getDataObject(wizardContextObject);
 	    if (StringUtils.isEmpty(wizardData))
 	    {
 	    	return className+"<"+NoData.class.getCanonicalName()+">";
 	    }
 		return className +"<"+wizardData+">";
 	}
 
 	@Override
 	public void processAttributes(SourcePrinter out, WizardContext context) throws CruxGeneratorException
 	{
 	    super.processAttributes(out, context);
 	    
 	    String wizardContextObject = context.getChildElement().optString("wizardContextObject");
 	    String wizardData =DataObjects.getDataObject(wizardContextObject);
 	    if (StringUtils.isEmpty(wizardData))
 	    {
 	    	context.wizardObject = NoData.class.getCanonicalName();
 	    }
 	    else
 	    {
 	    	context.wizardObject = wizardData;
 	    }
 	}
 	
 	@Override
     public WizardContext instantiateContext()
     {
 	    return new WizardContext();
     }
 }
