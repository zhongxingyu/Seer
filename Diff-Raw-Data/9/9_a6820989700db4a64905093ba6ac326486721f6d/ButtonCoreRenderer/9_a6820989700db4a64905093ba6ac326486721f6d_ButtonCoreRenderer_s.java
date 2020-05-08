 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.renderkit;
 
 import org.icemobile.component.IButton;
 import org.icemobile.component.IFragment;
 import org.icemobile.component.ISplitPane;
 import org.icemobile.util.CSSUtils;
 import org.icemobile.util.ClientDescriptor;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static org.icemobile.util.HTML.*;
 
 public class ButtonCoreRenderer extends BaseCoreRenderer {
     private static final Logger logger =
             Logger.getLogger(ButtonCoreRenderer.class.toString());
 
     public void encodeEnd(IButton component, IResponseWriter writer)
             throws IOException {
         IButton  button = (IButton) component;
         ClientDescriptor client = button.getClient();
         String clientId = button.getClientId();
      //   logger.info("clientId for button="+clientId);
         // apply button type style classes
         StringBuilder baseClass = new StringBuilder(CSSUtils.STYLECLASS_BUTTON);
         String buttonType = button.getButtonType();
         /* apply selected state if any
          * if openContentPane is used, then this may not work correctly as
          * the commandButtonGroup requires a submit and openContentPane does
          * not guarantee one.
          */
         if (button.isSelectedButton() || button.isSelected()) {
             baseClass.append(button.SELECTED_STYLE_CLASS);
         }
         // apply disabled style state if specified.
         if (button.isDisabled()) {
             baseClass.append(" ").append(button.DISABLED_STYLE_CLASS);
         }
         String style = button.getStyle();
         if( style != null ){
             style = style.trim();
             if( style.length() == 0){
                 style = null;
             }
         }
         // assign button type
         if( buttonType != null && !"".equals(buttonType)){
             if (button.BUTTON_TYPE_UNIMPORTANT.equals(buttonType)) {
                 baseClass.append(button.UNIMPORTANT_STYLE_CLASS);
             } else if (button.BUTTON_TYPE_BACK.equals(buttonType)) {
                 baseClass.append(button.BACK_STYLE_CLASS);
             } else if (button.BUTTON_TYPE_ATTENTION.equals(buttonType)) {
                 baseClass.append(button.ATTENTION_STYLE_CLASS);
             } else if (button.BUTTON_TYPE_IMPORTANT.equals(buttonType)) {
                 baseClass.append(button.IMPORTANT_STYLE_CLASS);
             } else if( !"default".equalsIgnoreCase(buttonType) && logger.isLoggable(Level.WARNING)) {
                 logger.warning("unsupported button type: '" + buttonType + "' for "+ clientId);
             }
         }
 
         String styleClass = button.getStyleClass();
         if (styleClass != null) {
             baseClass.append(" ").append(styleClass);
         }
 
         String type = button.getType();
         // button type for styling purposes, otherwise use pass through value.
         if (type == null){
             type = IButton.BUTTON_DEFAULT;
         }
 
         if (button.BUTTON_TYPE_BACK.equals(buttonType) && client.isIOS()){
             writer.startElement(DIV_ELEM, button);
             writer.writeAttribute(ID_ATTR, clientId+"_ctr");
             writer.writeAttribute(CLASS_ATTR, baseClass.toString());
             // should be auto base though
             if (style != null ) {
                 writer.writeAttribute(STYLE_ATTR, style);
             }
             writer.startElement(SPAN_ELEM, button);
             writer.endElement(SPAN_ELEM);
         }
         writer.startElement(INPUT_ELEM, component);
         writer.writeAttribute(ID_ATTR, clientId);
        Object oVal = button.getValue();
         //style and class written to ctr div when back button
         if (!IButton.BUTTON_TYPE_BACK.equals(buttonType) || !client.isIOS()){
             writer.writeAttribute(CLASS_ATTR, baseClass.toString());
             // should be auto base though
             if (style != null ) {
                 writer.writeAttribute(STYLE_ATTR, style);
             }
            if (null != oVal) {
                String value = oVal.toString();
                writer.writeAttribute(VALUE_ATTR, value);
            }
         }
         /*
         check on type?
          */
         writer.writeAttribute(TYPE_ATTR, type);
         writer.writeAttribute(NAME_ATTR, clientId);
 
         if (button.isDisabled()) {
             writer.writeAttribute(DISABLED_ATTR, "disabled");
             writer.endElement(INPUT_ELEM);
             //end ctr div for back button
             if (IButton.BUTTON_TYPE_BACK.equals(button.getButtonType()) && client.isIOS()){
                 writer.endElement(DIV_ELEM);
             }
             return;
         }
         /*
          *  if button manipulates contentStack all other attributes except those for
          *  styling apply --don't bother putting this in the core renderer.
          */
         if (button.getOpenContentPane()!=null){
             writer.writeAttribute(ONCLICK_ATTR, button.getJsCall().toString());
             writer.endElement(INPUT_ELEM);
         }else if ( !button.getType().trim().toLowerCase().equals(IButton.BUTTON_SUBMIT)){
             //logger.info(" type of button = "+button.getType());
             StringBuilder sb = new StringBuilder("ice.mobi.button.select('");
             sb.append(clientId).append("', event, {");
             sb.append("singleSubmit: ").append(button.isSingleSubmit());
             if (null !=button.getParams()) {
                 sb.append(", params: '").append(button.getParams()).append("'");
             }
             if (null != button.getGroupId()){
                 sb.append(", groupId: '").append(button.getGroupId()).append("'");
             }
             if (null != button.getPanelConfirmation()){
                 sb.append(", pcId: '").append(button.getPanelConfirmationId()).append("'");
             }
             if (null != button.getSubmitNotification()){
                 sb.append(", snId: '").append(button.getSubmitNotificationId()).append("'");
             }
             if (button.isParentDisabled()){
                 sb.append(", pDisabled: ").append(button.isParentDisabled());
             }
             if (null != button.getBehaviors()){
                 sb.append(button.getBehaviors());
             }
             sb.append("});");
             writer.writeAttribute(ONCLICK_ATTR, sb.toString());
         }/* else {
             logger.info(" NO ON CLICK HANDLER ");
         } */
         writer.endElement(INPUT_ELEM);
         /*
         last thing to do is to check if back button and then end the ctr div with b elem.
          */
         if (IButton.BUTTON_TYPE_BACK.equals(button.getButtonType()) && client.isIOS()){
             writer.startElement("b", button);
             writer.writeAttribute(CLASS_ATTR, "mobi-button-placeholder");
             Object oVal2 = button.getValue();
            String val = null;
             if (null != oVal2) {
                 String value = oVal2.toString();
                 writer.writeText(value);
             }
             writer.endElement("b");
             writer.endElement(DIV_ELEM);
         }
 
     }
 
 
 }
