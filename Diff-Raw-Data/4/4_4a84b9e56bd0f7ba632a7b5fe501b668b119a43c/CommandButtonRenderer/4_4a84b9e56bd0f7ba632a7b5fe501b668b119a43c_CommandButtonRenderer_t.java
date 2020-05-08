 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 package org.icefaces.mobi.component.button;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIParameter;
 import javax.faces.component.behavior.ClientBehaviorHolder;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 
 import org.icefaces.mobi.component.panelconfirmation.PanelConfirmationRenderer;
 import org.icefaces.mobi.component.submitnotification.SubmitNotificationRenderer;
 import org.icefaces.mobi.renderkit.CoreRenderer;
 import org.icefaces.mobi.utils.HTML;
 import org.icefaces.mobi.utils.JSFUtils;
 import org.icefaces.mobi.utils.MobiJSFUtils;
 
 
 public class  CommandButtonRenderer extends CoreRenderer {
     private static Logger logger = Logger.getLogger(CommandButtonRenderer.class.getName());
     List <UIParameter> uiParamChildren;
 
     public void decode(FacesContext facesContext, UIComponent uiComponent) {
         Map requestParameterMap = facesContext.getExternalContext().getRequestParameterMap();
         CommandButton commandButton = (CommandButton) uiComponent;
         String source = String.valueOf(requestParameterMap.get("ice.event.captured"));
         String clientId = commandButton.getClientId();
   //      logger.info("source = "+source);
         if (clientId.equals(source)) {
             try {
                 if (!commandButton.isDisabled()) {
                     uiComponent.queueEvent(new ActionEvent(uiComponent));
                     decodeBehaviors(facesContext, uiComponent);
                 }
             } catch (Exception e) {
                 logger.warning("Error queuing CommandButton event");
             }
         }
     }
 
     public void encodeBegin(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         CommandButton commandButton = (CommandButton) uiComponent;
 
         // apply button type style classes
         StringBuilder baseClass = new StringBuilder(CommandButton.BASE_STYLE_CLASS);
         String buttonType = commandButton.getButtonType();
         String styleClass = commandButton.getStyleClass();
         if (styleClass != null) {
             baseClass.append(" ").append(styleClass);
         }
         // apply selected state if any
         if (commandButton.isSelected()) {
             baseClass.append(CommandButton.SELECTED_STYLE_CLASS);
         }
         // apply disabled style state if specified.
         if (commandButton.isDisabled()) {
             baseClass.append(" ").append(CommandButton.DISABLED_STYLE_CLASS);
         }
         String style = commandButton.getStyle();
         if( style != null ){
             style = style.trim();
             if( style.length() == 0){
                 style = null;
             }
         }
         // assign button type
         if (CommandButton.BUTTON_TYPE_DEFAULT.equals(buttonType)) {
             baseClass.append(CommandButton.DEFAULT_STYLE_CLASS);
         } else if (CommandButton.BUTTON_TYPE_BACK.equals(buttonType)) {
             baseClass.append(CommandButton.BACK_STYLE_CLASS);
         } else if (CommandButton.BUTTON_TYPE_ATTENTION.equals(buttonType)) {
             baseClass.append(CommandButton.ATTENTION_STYLE_CLASS);
         } else if (CommandButton.BUTTON_TYPE_IMPORTANT.equals(buttonType)) {
             baseClass.append(CommandButton.IMPORTANT_STYLE_CLASS);
         } else if (logger.isLoggable(Level.FINER)) {
             baseClass.append(CommandButton.DEFAULT_STYLE_CLASS);
         }
         
         String type = commandButton.getType();
         // button type for styling purposes, otherwise use pass through value.
         if (type == null){
             type = "button";
         }
 
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         if (CommandButton.BUTTON_TYPE_BACK.equals(buttonType)){
             writer.startElement(HTML.DIV_ELEM, commandButton);
             writer.writeAttribute(HTML.ID_ATTR, clientId+"_ctr", HTML.ID_ATTR);
             writer.writeAttribute(HTML.CLASS_ATTR, baseClass.toString(), null);
             // should be auto base though
             if (style != null ) {
                 writer.writeAttribute(HTML.STYLE_ATTR, style, HTML.STYLE_ATTR);
             }
             writer.startElement(HTML.SPAN_ELEM, commandButton);
             writer.endElement(HTML.SPAN_ELEM);
         }
         writer.startElement(HTML.INPUT_ELEM, uiComponent);
         writer.writeAttribute(HTML.ID_ATTR, clientId, HTML.ID_ATTR);
         //style and class written to ctr div when back button
         if (!CommandButton.BUTTON_TYPE_BACK.equals(buttonType)){
             writer.writeAttribute(HTML.CLASS_ATTR, baseClass.toString(), null);
             // should be auto base though
             if (style != null ) {
                 writer.writeAttribute(HTML.STYLE_ATTR, style, HTML.STYLE_ATTR);
             }
         }
         writer.writeAttribute(HTML.TYPE_ATTR, type, null);
         writer.writeAttribute(HTML.NAME_ATTR, clientId, null);
         String value = type;
         Object oVal = commandButton.getValue();
         if (null != oVal) {
             value = oVal.toString();
         }
         writer.writeAttribute(HTML.VALUE_ATTR, value, HTML.VALUE_ATTR);
         
         
         
     }
 
     public void encodeEnd(FacesContext facesContext, UIComponent uiComponent)
             throws IOException {
         ResponseWriter writer = facesContext.getResponseWriter();
         String clientId = uiComponent.getClientId(facesContext);
         CommandButton commandButton = (CommandButton) uiComponent;
         uiParamChildren = JSFUtils.captureParameters( commandButton );
         if (commandButton.isDisabled()) {
             writer.writeAttribute("disabled", "disabled", null);
             writer.endElement(HTML.INPUT_ELEM);
            //end ctr div for back button
            if (CommandButton.BUTTON_TYPE_BACK.equals(commandButton.getButtonType())){
                writer.endElement(HTML.DIV_ELEM);
            }
             return;
         }
         boolean singleSubmit = commandButton.isSingleSubmit();
         String idAndparams = "'"+clientId+"'";
         String params="";
         if (uiParamChildren != null) {
             params =  MobiJSFUtils.asParameterString(uiParamChildren);
             idAndparams += ","+ params;
         }
         ClientBehaviorHolder cbh = (ClientBehaviorHolder)uiComponent;
         boolean hasBehaviors = !cbh.getClientBehaviors().isEmpty();
         StringBuilder seCall = new StringBuilder("ice.se(event, ").append(idAndparams).append(");");
         StringBuilder sCall = new StringBuilder("ice.s(event, ").append(idAndparams).append(");");
 
         /**
              *  panelConfirmation?  Then no singleSubmit
              *     then add the confirmation panel ID.  panelConfrmation confirm will
              *  submitNotification?
              *     if no panelConfirmation then just display it when commandButton does submit
              *         have behaviors? , ice.se or ice.s doesn't matter.
              *
              *
              *
         */
         StringBuilder builder = new StringBuilder(255);
         String panelConfId=commandButton.getPanelConfirmation();
         String subNotId = commandButton.getSubmitNotification();
         String submitNotificationId = null;
 
      //   builder.append("{ elVal: this");
         builder.append("{ event: event");
         if (singleSubmit){
             builder.append(", singleSubmit:").append(singleSubmit);
         }
         if (null != subNotId  && subNotId.length()> 0) {
             submitNotificationId = SubmitNotificationRenderer.findSubmitNotificationId(uiComponent,subNotId);
             if (null != submitNotificationId ){
                 builder.append(",snId: '").append(submitNotificationId).append("'");
             } else {
                 logger.warning("no submitNotification id found for commandButton id="+clientId);
             }
         }
         if (uiParamChildren != null){
            //include params for button
             builder.append(",params: ").append(params);
         }
         if (null != panelConfId && panelConfId.length()>0){
             ///would never use this with singleSubmit so always false when using with panelConfirmation
             //panelConf either has ajax request behaviors or regular ice.submit.
             if (hasBehaviors){
                 String behaviors = this.encodeClientBehaviors(facesContext, cbh, "click").toString();
                 behaviors = behaviors.replace("\"", "\'");
                 builder.append(behaviors);
             }
             StringBuilder pcBuilder = PanelConfirmationRenderer.renderOnClickString(uiComponent, builder );
             if (null != pcBuilder){
                 //has panelConfirmation and it is found
                 writer.writeAttribute(HTML.ONCLICK_ATTR, pcBuilder.toString(),HTML.ONCLICK_ATTR);
             } else { //no panelConfirmation found so commandButton does the job
                 logger.warning("panelConfirmation of "+panelConfId+" NOT FOUND:- resorting to standard ajax form submit");
                 StringBuilder noPanelConf = this.getCall(clientId, builder.toString());
                 noPanelConf.append("});");     ///this is the cfg if commandButton does the submit
                 writer.writeAttribute(HTML.ONCLICK_ATTR, noPanelConf.toString(), HTML.ONCLICK_ATTR);
             }
         } else {  //no panelConfirmation requested so button does job
             StringBuilder noPanelConf = this.getCall(clientId, builder.toString());
             if (hasBehaviors){
                 String behaviors = this.encodeClientBehaviors(facesContext, cbh, "click").toString();
                 behaviors = behaviors.replace("\"", "\'");
                 noPanelConf.append(behaviors);
             }
             noPanelConf.append("});");
             writer.writeAttribute(HTML.ONCLICK_ATTR, noPanelConf.toString(), HTML.ONCLICK_ATTR);
         }
         writer.endElement(HTML.INPUT_ELEM);
         //end ctr div for back button
         if (CommandButton.BUTTON_TYPE_BACK.equals(commandButton.getButtonType())){
             writer.endElement(HTML.DIV_ELEM);
         }
     }
 
     private StringBuilder getCall(String clientId, String builder ) {
         StringBuilder noPanelConf = new StringBuilder("mobi.button.select('").append(clientId).append("',");
         noPanelConf.append(builder);
         return noPanelConf;
     }
 }
