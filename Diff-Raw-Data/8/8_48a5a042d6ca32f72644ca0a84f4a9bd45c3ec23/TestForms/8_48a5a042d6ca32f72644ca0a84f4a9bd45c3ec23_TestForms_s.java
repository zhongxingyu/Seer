 /*
  * This library is part of the Acacia Editor -
  * an open source inline and form based content editor for GWT.
  *
  * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.acacia.client;
 
 import com.alkacon.acacia.client.widgets.FormWidgetWrapper;
 import com.alkacon.acacia.client.widgets.I_EditWidget;
 import com.alkacon.acacia.client.widgets.I_FormEditWidget;
 import com.alkacon.acacia.client.widgets.StringWidget;
 import com.alkacon.acacia.shared.AttributeConfiguration;
 import com.alkacon.acacia.shared.ContentDefinition;
 import com.alkacon.vie.client.I_Vie;
 import com.alkacon.vie.client.Vie;
 import com.alkacon.vie.shared.I_Entity;
 import com.alkacon.vie.shared.I_Type;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.gwt.event.dom.client.FocusHandler;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.junit.client.GWTTestCase;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Tests the forms.<p>
  */
 public class TestForms extends GWTTestCase {
 
     /**
      * @see com.google.gwt.junit.client.GWTTestCase#getModuleName()
      */
     @Override
     public String getModuleName() {
 
         return "com.alkacon.acacia.Acacia";
     }
 
     /**
      * Tests the form renderer.<p>
      */
     public void testFormRenderer() {
 
         String simpleTypeId = "cms:simple";
         String complexTypeId = "cms:complex";
         String attributeName = "http:opencms/simpleAttribute";
         I_Vie vie = getVieInstance();
         vie.createType(simpleTypeId);
         I_Type complex = vie.createType(complexTypeId);
         complex.addAttribute(attributeName, simpleTypeId, 1, 1);
         I_Entity entity = vie.createEntity("myEntity", complexTypeId);
         entity.setAttributeValue(attributeName, "my attribute value");
         WidgetService service = new WidgetService();
         I_EntityRenderer defaultRenderer = new Renderer(vie, service);
         service.setDefaultRenderer(defaultRenderer);
         I_EntityRenderer renderer = service.getRendererForType(complex);
         FlowPanel context = new FlowPanel();
         RootPanel.get().add(context);
         renderer.renderForm(entity, context, new RootHandler(), 0);
         assertEquals(
             "The forms inner HTML should match the exspected.",
             "<div typeof=\"cms:complex\" about=\"myEntity\" class=\"entity\"><div title=\"\" class=\"label\">http:opencms/simpleAttribute</div><div class=\"widgetHolder\"><div style=\"color: red;\" contenteditable=\"true\" property=\"http:opencms/simpleAttribute\">my attribute value</div></div></div>",
             context.getElement().getInnerHTML());
     }
 
     /**
      * Tests the widget service implementation.<p>
      */
     public void testWidgetService() {
 
         WidgetService service = new WidgetService();
         final I_FormEditWidget widget1 = new I_FormEditWidget() {
 
             public HandlerRegistration addFocusHandler(FocusHandler handler) {
 
                 // dummy method
                 return null;
             }
 
             public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
 
                 return null;
             }
 
             public Widget asWidget() {
 
                 // TODO: Auto-generated method stub
                 return null;
             }
 
             public void fireEvent(GwtEvent<?> event) {
 
                 // dummy method
             }
 
             public String getValue() {
 
                 return null;
             }
 
             public boolean isActive() {
 
                 // TODO: Auto-generated method stub
                 return false;
             }
 
             public void onAttachWidget() {
 
                 // TODO: Auto-generated method stub
 
             }
 
             public void setActive(boolean active) {
 
                 // TODO: Auto-generated method stub
 
             }
 
             /**
              * @see com.alkacon.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
              */
             public void setName(String name) {
 
                 // no input field so nothing to do
 
             }
 
             public void setValue(String value) {
 
                 // dummy method
             }
 
             public void setValue(String value, boolean fireEvents) {
 
                 // dummy method
             }
 
             public void setWidgetInfo(String label, String help) {
 
                 // TODO: Auto-generated method stub
 
             }
         };
         Map<String, AttributeConfiguration> configs = new HashMap<String, AttributeConfiguration>();
         configs.put("attribute1", new AttributeConfiguration("label", "help", "widget1", "", "", "wide"));
         configs.put("attribute2", new AttributeConfiguration("label", "help", "widget2", "", "", "wide"));
         ContentDefinition definition = new ContentDefinition(
             null,
             configs,
             Collections.<String, I_Type> emptyMap(),
             null,
             false,
             "en");
         service.init(definition);
         service.addWidgetFactory("widget1", new I_WidgetFactory() {
 
             public I_FormEditWidget createFormWidget(String configuration) {
 
                 return widget1;
             }
 
             public I_EditWidget createInlineWidget(String configuration, com.google.gwt.user.client.Element element) {
 
                 // TODO: Auto-generated method stub
                 return null;
             }
         });
         service.addWidgetFactory("widget2", new I_WidgetFactory() {
 
             public I_FormEditWidget createFormWidget(String configuration) {
 
                 return new FormWidgetWrapper(new StringWidget());
             }
 
             public I_EditWidget createInlineWidget(String configuration, com.google.gwt.user.client.Element element) {
 
                 // TODO: Auto-generated method stub
                 return null;
             }
         });
         assertEquals(widget1, service.getAttributeFormWidget("attribute1"));
         assertTrue(
             "Should be instance of FormWidgetWrapper",
             service.getAttributeFormWidget("attribute2") instanceof FormWidgetWrapper);
         assertTrue(
             "Should be instance of FormWidgetWrapper as the default widget",
             service.getAttributeFormWidget("some other") instanceof FormWidgetWrapper);
     }
 
     /**
      * Returns the {@link Vie} instance.<p>
      * 
      * @return the {@link Vie} instance
      */
     private I_Vie getVieInstance() {
 
         return Vie.getInstance();
     }
 }
