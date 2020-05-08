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
 package br.com.sysmap.crux.gadget.client.widget;
 
 import br.com.sysmap.crux.core.client.Crux;
 import br.com.sysmap.crux.core.client.declarative.DeclarativeFactory;
 import br.com.sysmap.crux.core.client.declarative.TagAttribute;
 import br.com.sysmap.crux.core.client.declarative.TagAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagChild;
 import br.com.sysmap.crux.core.client.declarative.TagChildAttributes;
 import br.com.sysmap.crux.core.client.declarative.TagChildren;
 import br.com.sysmap.crux.core.client.screen.HTMLContainer;
 import br.com.sysmap.crux.core.client.screen.InterfaceConfigException;
import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor;
import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.AnyTag;
 import br.com.sysmap.crux.core.client.screen.parser.CruxMetaDataElement;
 import br.com.sysmap.crux.core.rebind.widget.WidgetCreator;
 import br.com.sysmap.crux.core.rebind.widget.WidgetCreatorContext;
 import br.com.sysmap.crux.gadget.client.widget.GadgetView.View;
 import br.com.sysmap.crux.gwt.client.AbstractHTMLPanelFactory;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.Element;
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 @DeclarativeFactory(id="gadgetView", library="gadget", htmlContainer=true)
 public class GadgetViewFactory extends AbstractHTMLPanelFactory<GadgetView>
 {
 
 	/**
 	 * @author Thiago da Rosa de Bustamante
 	 *
 	 */
 	protected static class CruxGadgetView extends GadgetView implements HTMLContainer
 	{
 		/**
 		 * Constructor
 		 * @param element
 		 */
 		public CruxGadgetView(CruxMetaDataElement element)
         {
 	        super("");
 	        assert(element.containsKey("id")):Crux.getMessages().screenFactoryWidgetIdRequired();
 	        Element panelElement = WidgetCreator.getEnclosingPanelElement(element.getProperty("id"));
 	        assert Document.get().getBody().isOrHasChild(panelElement);
 	        panelElement.removeFromParent();
 	        getElement().appendChild(panelElement);
         }
 		
 		@Override
 		public void onAttach()
 		{
 		    super.onAttach();
 		}
 	}	
 	
 	@Override
 	public GadgetView instantiateWidget(CruxMetaDataElement element, String widgetId) throws InterfaceConfigException 
 	{
 		CruxGadgetView gadgetView = new CruxGadgetView(element);
 		createChildren(widgetId, element);
 		CruxGadgetView.getGadget();//initializes the gadget
 		return gadgetView;
 	}
 
 	@TagAttributes({
 		@TagAttribute(value="view", type=View.class, required=true)
 	})
 	@Override
 	public void processAttributes(WidgetCreatorContext context) throws InterfaceConfigException
 	{
 	    super.processAttributes(context);
 	}
 	
 	@Override
 	@TagChildren({
 		@TagChild(value=ContentProcessor.class, autoProcess=false)
 	})
 	public void processChildren(WidgetCreatorContext context) throws InterfaceConfigException
 	{
 	}
 	
 	@TagChildAttributes(minOccurs="0", maxOccurs="unbounded", type=AnyTag.class)
 	public static class ContentProcessor extends WidgetChildProcessor<GadgetView, WidgetCreatorContext> {}
 
 	@Override
     protected String getFactoryType()
     {
 	    return "gadget_gadgetView";
     }
 }
