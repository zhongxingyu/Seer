 /*
  * Copyright 2013 Mobile Helix, Inc.
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
 package org.helix.mobile.component.formlayout;
 
 import java.io.IOException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class FormLayoutRenderer extends CoreRenderer {
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         FormLayout layout = (FormLayout) component;
         writer.startElement("div", layout);
        writer.writeAttribute("id", layout.getClientId(context), "id"); 
        if (layout.getStyleClass() != null) {
            writer.writeAttribute("class", layout.getStyleClass(), null);
        }
         writer.endElement("div");
         
         startScript(writer, layout.getClientId(context));
         writer.write("\n(function($) {");
         
         writer.write("$(document).on('helixinit', function() {");
         writer.write("\n" + layout.resolveWidgetVar() + " =$(PrimeFaces.escapeClientId('" + layout.getClientId(context) + "')).helixFormLayout({");
         writer.write("items: [");
         boolean isFirst = true;
         for (UIComponent c : layout.getChildren()) {
             if (isFirst) {
                 isFirst = false;
             } else {
                 writer.write(",\n");
             }
             c.encodeAll(context);
         }
         writer.write("]");
         writer.write(",mode: " + Boolean.toString(layout.isEditMode()));
         writer.write(",separateElements: " + Boolean.toString(layout.isSeparateElements()));
         writer.write("}).data('helix-helixFormLayout');");
         writer.write("});");
         
         writer.write("})(jQuery);\n");
         endScript(writer);
     }
     
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Rendering happens on encodeEnd
     }
     
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
