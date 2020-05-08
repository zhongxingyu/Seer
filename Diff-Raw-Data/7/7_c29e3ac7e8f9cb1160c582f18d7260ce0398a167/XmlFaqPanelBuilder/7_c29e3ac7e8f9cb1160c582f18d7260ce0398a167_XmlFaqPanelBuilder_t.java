 /*
  *   Copyright 2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 
 package com.thruzero.common.web.model.container.builder.xml;
 
 import java.util.Iterator;
 
 import com.thruzero.common.core.infonode.InfoNodeElement;
 import com.thruzero.common.core.locator.ConfigLocator;
 import com.thruzero.common.web.model.container.AbstractPanel;
 import com.thruzero.common.web.model.container.FaqPanel;
 import com.thruzero.common.web.model.container.builder.xml.AbstractXmlPanelBuilder.XmlPanelBuilderAnnotation;
 
 
 /**
  * An instance represents a builder of ListPanel objects (a panel containing a list of items).
  * <p/>
  * The following XML, if used by an XmlFaqPanelBuilder, would build a simple FAQ panel:
  *
  * <pre>
  * {@code
  * <faqPanel title="UML" id="uml">
  *   <dataList>
  *     <faqtoid title="Indicate a Static method">
  *       <![CDATA[
  *       Use <u>underline</u> to indicate a static method or data member.
  *       ]]>
  *     </faqtoid>
  *     <faqtoid title="Indicate an Abstract method">
  *       <![CDATA[
  *       Use <i>italics</i> to indicate an abstract class or method.
  *       ]]>
  *     </faqtoid>
  *   </dataList>
  * </faqPanel>
  * }
  * </pre>
  *
  * @author George Norman
  */
 
 @XmlPanelBuilderAnnotation(panelTypeName = "faqPanel")
 public class XmlFaqPanelBuilder extends AbstractXmlPanelBuilder {
   private static final String CHILD_DATALIST_ID = ConfigLocator.locate().getValue(XmlFaqPanelBuilder.class.getName(), "dataList", "dataList");
 
   /**
    * Name of the attribute representing an optional description for all FAQs in the FAQ panel. The default name is "description" and can be changed via config.
    */
   public static final String DESCRIPTION_ID = ConfigLocator.locate().getValue(AbstractXmlPanelBuilder.class.getName(), "description", "description");
 
   public XmlFaqPanelBuilder(InfoNodeElement panelNode) {
     super(panelNode);
   }
 
   @Override
   public AbstractPanel build() throws Exception {
     FaqPanel result = new FaqPanel(getPanelId(), getPanelTitle(), getPanelTitleLink(), getCollapseDirection(), getPanelHeaderStyleClass(), getToolbar(), getDescription());
     InfoNodeElement dataListNode = getPanelNode().findElement(CHILD_DATALIST_ID);
 
     if (dataListNode != null) {
       for (Iterator<? extends InfoNodeElement> iter = dataListNode.getChildNodeIterator(); iter.hasNext(); ) {
         result.addItem(iter.next());
       }
     }
 
     return result;
   }
 
   protected String getDescription() throws Exception {
    String result = null;
     InfoNodeElement dataListNode = getPanelNode().findElement(DESCRIPTION_ID);
    
    if (dataListNode != null) {
      result = dataListNode.getText();
    }
 
     return result;
   }
 
 }
