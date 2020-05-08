 /*
  * Copyright 2009-2011 Prime Technology.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.primefaces.mobile.renderkit;
 
 import java.io.IOException;
 import java.util.Map;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import org.primefaces.component.commandbutton.CommandButton;
 import org.primefaces.renderkit.CoreRenderer;
 import org.primefaces.util.ComponentUtils;
 
 public class CommandButtonRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         CommandButton button = (CommandButton) component;
         if(button.isDisabled()) {
             return;
         }
 
         String param = component.getClientId(context);
         if (context.getExternalContext().getRequestParameterMap().containsKey(param)) {
             component.queueEvent(new ActionEvent(component));
         }
         
         decodeBehaviors(context, component);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         CommandButton button = (CommandButton) component;
         String clientId = button.getClientId(context);
         String type = button.getType();      
         Map<String,Object> attrs = button.getAttributes();
         String styleClass = button.getStyleClass()== null ? "" : button.getStyleClass();
         if (button.isDisabled()) styleClass = styleClass + " ui-disabled";        
 
         writer.startElement("a", button);
         writer.writeAttribute("id", clientId, "id");
         writer.writeAttribute("name", clientId, "name");
         writer.writeAttribute("type", type, "type");
         writer.writeAttribute("data-role", "button", null);      
 
         StringBuilder onclick = new StringBuilder();
         if(button.getOnclick() != null) {
             onclick.append(button.getOnclick()).append(";");
         }
         
        String onclickBehaviors = getEventBehaviors(context, button, "click");
         if(onclickBehaviors != null) {
             onclick.append(onclickBehaviors);
         }        
         
         if (!type.equals("reset") && !type.equals("button")) {
             String request;
             boolean ajax = button.isAjax();
             if (ajax) {
                 request = buildAjaxRequest(context, button, null);
             } else {
                 UIComponent form = ComponentUtils.findParentForm(context, button);
                 if (form == null) {
                     throw new FacesException("CommandButton : \"" + clientId + "\" must be inside a form element");
                 }
 
                 request = buildNonAjaxRequest(context, button, form, clientId, true);
             }                      
 
             onclick.append(request);
         }
 
         if(attrs.containsKey("swatch")) writer.writeAttribute("data-theme", attrs.get("swatch"), null);
         if (button.getIcon() != null) {
             writer.writeAttribute("data-iconpos", button.getIconPos(), null);
             writer.writeAttribute("data-icon", button.getIcon(), null);
         }
         if(button.isInline()) writer.writeAttribute("data-inline", "true", null);
         if(button.getStyle() != null) writer.writeAttribute("style", button.getStyle(), null);
         writer.writeAttribute("class", styleClass, null);
         
 	if(onclick.length() > 0) {
             if (button.requiresConfirmation()) {
                 writer.writeAttribute("data-pfconfirmcommand", onclick.toString(), null);
                 writer.writeAttribute("onclick", button.getConfirmationScript(), "onclick");
             } else {
                 writer.writeAttribute("onclick", onclick.toString(), "onclick");
             }
         }      
         
 	if(button.getValue() != null) writer.write(button.getValue().toString());
 
         writer.endElement("a");
     }
 }
