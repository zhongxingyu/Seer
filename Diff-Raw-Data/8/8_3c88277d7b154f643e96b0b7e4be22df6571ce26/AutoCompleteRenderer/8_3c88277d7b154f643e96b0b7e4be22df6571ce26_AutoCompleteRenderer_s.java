 /*
  * Copyright 2009-2012 Prime Teknoloji.
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
 import java.util.List;
 import java.util.Map;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.event.PhaseId;
 import org.primefaces.component.autocomplete.AutoComplete;
 import org.primefaces.event.AutoCompleteEvent;
import org.primefaces.renderkit.InputRenderer;
 import org.primefaces.util.WidgetBuilder;
 
public class AutoCompleteRenderer extends InputRenderer {
 
     public void decode(FacesContext context, UIComponent component) {
         AutoComplete ac = (AutoComplete) component;
         String clientId = ac.getClientId(context);
         Map<String, String> params = context.getExternalContext().getRequestParameterMap();
 
         if (ac.isDisabled() || ac.isReadonly()) {
             return;
         }
 
         decodeBehaviors(context, ac);
 
         //AutoComplete event
         String query = params.get(clientId + "_query");
         if (query != null && !query.isEmpty()) {
             AutoCompleteEvent autoCompleteEvent = new AutoCompleteEvent(ac, query);
             autoCompleteEvent.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
             ac.queueEvent(autoCompleteEvent);
         }
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         AutoComplete autoComplete = (AutoComplete) component;
 
         encodeMarkup(context, autoComplete);
         encodeScript(context, autoComplete);
 
     }
 
     protected void encodeMarkup(FacesContext context, AutoComplete ac) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = ac.getClientId(context);
         String var = ac.getVar();
         Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        Converter converter = findConverter(context, ac);
         boolean pojo = var != null;
 
         writer.startElement("div", ac);
         writer.writeAttribute("id", clientId, "id");
         writer.startElement("ul", null);
         writer.writeAttribute("data-role", "listview", null);
         writer.writeAttribute("data-inset", "true", null);
         writer.writeAttribute("data-filter", "true", null);
 
         if (ac.getStyleClass() != null) {
             writer.writeAttribute("class", ac.getStyleClass(), "styleClass");
         }
 
         if (ac.getStyle() != null) {
             writer.writeAttribute("style", ac.getStyle(), "style");
         }
 
         int maxResults = ac.getMaxResults();
         List results = ac.getSuggestions();
 
         if (results != null) {
 
             if (maxResults != Integer.MAX_VALUE && results.size() > maxResults) {
                 results = results.subList(0, ac.getMaxResults());
             }
 
             for (Object item : results) {
                 writer.startElement("li", null);
                 writer.startElement("a", null);                
                 if (pojo) {
                     requestMap.put(var, item);
                     String value = converter == null ? (String) ac.getItemValue() : converter.getAsString(context, ac, ac.getItemValue());
                     writer.writeAttribute("item-value", value, null);
                     writer.writeText(ac.getItemLabel(), null);
                 } else {
                     writer.writeAttribute("item-value", item, null);
                     writer.writeText(item, null);
                 }
                 writer.endElement("a");
                 writer.endElement("li");
             }
         }
 
         writer.endElement("ul");
         writer.endElement("div");
     }
 
     protected void encodeScript(FacesContext context, AutoComplete ac) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         String clientId = ac.getClientId(context);
         WidgetBuilder wb = getWidgetBuilder(context);
         wb.widget("AutoComplete", ac.resolveWidgetVar(), clientId, true);
         wb.attr("minLength", ac.getMinQueryLength(), 1)
                 .attr("delay", ac.getQueryDelay(), 300);
 
         encodeClientBehaviors(context, ac, wb);
 
         startScript(writer, clientId);
         writer.write(wb.build());
         endScript(writer);
     }
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Do nothing
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
 }
