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
 package org.helix.mobile.component.outputfield;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class OutputFieldRenderer extends CoreRenderer {
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         OutputField field = (OutputField) component;
         
         writer.startElement("div", field);
         writer.writeAttribute("id", field.getClientId(context), "id");
         writer.writeAttribute("data-role", "fieldcontain", null);
         
         if (field.getStyleClass() != null) {
             writer.writeAttribute("class", writer.getClass(), null);
         }
         
         if (field.getStyle() != null) {
             writer.writeAttribute("style", field.getStyle(), null);
         }
         
         if (field.getLabel() != null) {
             writer.startElement("label", field);
             writer.writeAttribute("for", field.getName(), null);
             writer.write(field.getLabel());
             writer.endElement("label");
         }
         
         if (field.getType() != null && field.getType().equals("textarea")) {
             writer.startElement("textarea", field);
         } else {
             writer.startElement("input", field);
             if (field.getType() != null) {
                 writer.writeAttribute("type", field.getType(), null);
             } else {
                 writer.writeAttribute("type", "text", null);
             }
         }
         writer.writeAttribute("name", field.getName(), null);
         writer.writeAttribute("id", field.getName(), null);
         writer.writeAttribute("value", field.getValue(), null);
         if (field.getType() != null && field.getType().equals("textarea")) {
             writer.endElement("textarea");
         } else {
             writer.endElement("input");
         }
         
         writer.endElement("div");
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
     }
     
     @Override
     public boolean getRendersChildren() {
         return false;
     }
 }
