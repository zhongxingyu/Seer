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
 package org.primefaces.component.selectonemenu;
 
 import java.io.IOException;
 import java.util.List;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UISelectOne;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.faces.model.SelectItem;
 import org.primefaces.component.column.Column;
 import org.primefaces.renderkit.SelectOneRenderer;
 
 public class SelectOneMenuRenderer extends SelectOneRenderer {
 
     @Override
 	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
         return context.getRenderKit().getRenderer("javax.faces.SelectOne", "javax.faces.Menu").getConvertedValue(context, component, submittedValue);
 	}
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         SelectOneMenu menu = (SelectOneMenu) component;
 
         encodeMarkup(context, menu);
         encodeScript(context, menu);
     }
 
     protected void encodeMarkup(FacesContext context, SelectOneMenu menu) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         List<SelectItem> selectItems = getSelectItems(context, menu);
         String clientId = menu.getClientId(context);
         Converter converter = getConverter(context, menu);
         Object values = getValues(menu);
         Object submittedValues = getSubmittedValues(menu);
                 
         String style = menu.getStyle();
         String styleclass = menu.getStyleClass();
         styleclass = styleclass == null ? SelectOneMenu.STYLE_CLASS : SelectOneMenu.STYLE_CLASS + " " + styleclass;
         styleclass = menu.isDisabled() ? styleclass + " ui-state-disabled" : styleclass;
 
         writer.startElement("div", menu);
         writer.writeAttribute("id", clientId, "id");
         writer.writeAttribute("class", styleclass, "styleclass");
         if(style != null) {
             writer.writeAttribute("style", style, "style");
         }
 
         encodeInput(context, menu, clientId, selectItems, values, submittedValues, converter);
         encodeLabel(context, menu, selectItems);
         encodeMenuIcon(context, menu);
         encodePanel(context, menu, selectItems);
 
         writer.endElement("div");
     }
 
     protected void encodeInput(FacesContext context, SelectOneMenu menu, String clientId, List<SelectItem> selectItems, Object values, Object submittedValues, Converter converter) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String inputId = clientId + "_input";
         
         writer.startElement("div", menu);
         writer.writeAttribute("class", "ui-helper-hidden", null);
 
         writer.startElement("select", menu);
         writer.writeAttribute("id", inputId, "id");
         writer.writeAttribute("name", inputId, null);
         if(menu.getOnchange() != null) writer.writeAttribute("onchange", menu.getOnchange(), null);
         if(menu.isDisabled()) writer.writeAttribute("disabled", "disabled", null);
 
         encodeSelectItems(context, menu, selectItems, values, submittedValues, converter);
 
         writer.endElement("select");
 
         writer.endElement("div");
     }
 
     protected void encodeLabel(FacesContext context, SelectOneMenu menu, List<SelectItem> selectItems) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         
         writer.startElement("a", null);
         writer.writeAttribute("href", "#", null);
         writer.writeAttribute("class", SelectOneMenu.LABEL_CONTAINER_CLASS, null);
         if(menu.getTabindex() != null) {
             writer.writeAttribute("tabindex", menu.getTabindex(), null);
         }
         
         writer.startElement("label", null);
         writer.writeAttribute("class", SelectOneMenu.LABEL_CLASS, null);
         writer.write("&nbsp;");     //will be updated by widget on load
         writer.endElement("label");
         writer.endElement("a");
     }
 
     protected void encodeMenuIcon(FacesContext context, SelectOneMenu menu) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         
         writer.startElement("div", menu);
         writer.writeAttribute("class", SelectOneMenu.TRIGGER_CLASS, null);
 
         writer.startElement("span", menu);
         writer.writeAttribute("class", "ui-icon ui-icon-triangle-1-s", null);
         writer.endElement("span");
 
         writer.endElement("div");
     }
 
     protected void encodePanel(FacesContext context, SelectOneMenu menu, List<SelectItem> selectItems) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         boolean customContent = menu.getVar() != null;
         int height = calculatePanelHeight(menu, selectItems.size());
 
         writer.startElement("div", null);
         writer.writeAttribute("id", menu.getClientId(context) + "_panel", null);
         writer.writeAttribute("class", SelectOneMenu.PANEL_CLASS, null);
         
         if(height != -1) {
             writer.writeAttribute("style", "height:" + height + "px", null);
         }
 
         if(customContent) {
             writer.startElement("table", menu);
             writer.writeAttribute("class", SelectOneMenu.TABLE_CLASS, null);
             writer.startElement("tbody", menu);
             encodeOptionsAsTable(context, menu, selectItems);
             writer.endElement("tbody");
             writer.endElement("table");
         } 
         else {
             writer.startElement("ul", menu);
             writer.writeAttribute("class", SelectOneMenu.LIST_CLASS, null);
             encodeOptionsAsList(context, menu, selectItems);
             writer.endElement("ul");
         }
         
         writer.endElement("div");
     }
 
     protected void encodeOptionsAsTable(FacesContext context, SelectOneMenu menu, List<SelectItem> selectItems) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String var = menu.getVar();
         List<Column> columns = menu.getColums();
         Object value = menu.getValue();
 
         for(SelectItem selectItem : selectItems) {
             Object itemValue = selectItem.getValue();
             
             context.getExternalContext().getRequestMap().put(var, selectItem.getValue());
             
             writer.startElement("tr", null);
             writer.writeAttribute("class", SelectOneMenu.ROW_CLASS, null);
 
             if(itemValue instanceof String) {
                 writer.startElement("td", null);
                 writer.writeAttribute("colspan", columns.size(), null);
                 writer.writeText(selectItem.getLabel(), null);
                 writer.endElement("td");
             } 
             else {
                 for(Column column : columns) {
                     writer.startElement("td", null);
                     column.encodeAll(context);
                     writer.endElement("td");
                 }
             }
 
             writer.endElement("tr");
         }
 
         context.getExternalContext().getRequestMap().put(var, null);
     }
 
     protected void encodeOptionsAsList(FacesContext context, SelectOneMenu menu, List<SelectItem> selectItems) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         Object value = menu.getValue();
 
         for(int i = 0; i < selectItems.size(); i++) {
             SelectItem selectItem = selectItems.get(i);
             String itemLabel = selectItem.getLabel();
             itemLabel = isValueBlank(itemLabel) ? "&nbsp;" : itemLabel;
             
             writer.startElement("li", null);
             writer.writeAttribute("class", SelectOneMenu.ITEM_CLASS, null);
             
             if(itemLabel.equals("&nbsp;"))
                 writer.write(itemLabel);
             else
                 writer.writeText(itemLabel, null);
 
             writer.endElement("li");
         }
     }
 
     protected void encodeScript(FacesContext context, SelectOneMenu menu) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = menu.getClientId(context);
 
         startScript(writer, clientId);
         
         writer.write("PrimeFaces.cw('SelectOneMenu','" + menu.resolveWidgetVar() + "',{");
         writer.write("id:'" + clientId + "'");
         writer.write(",effect:'" + menu.getEffect() + "'");
         
         if(menu.getEffectDuration() != 400)
             writer.write(",effectDuration:" + menu.getEffectDuration());
 
         encodeClientBehaviors(context, menu);
 
        writer.write("});");
 
         endScript(writer);
     }
 
     protected void encodeSelectItems(FacesContext context, SelectOneMenu menu, List<SelectItem> selectItems, Object values, Object submittedValues, Converter converter) throws IOException {
         for(SelectItem selectItem : selectItems) {
             encodeOption(context, menu, selectItem, values, submittedValues, converter);
         }
     }
     
     protected void encodeOption(FacesContext context, SelectOneMenu menu, SelectItem option, Object values, Object submittedValues, Converter converter) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String itemValueAsString = getOptionAsString(context, menu, converter, option.getValue());
         boolean disabled = option.isDisabled() || menu.isDisabled();
 
         Object valuesArray;
         Object itemValue;
         if(submittedValues != null) {
             valuesArray = submittedValues;
             itemValue = itemValueAsString;
         } else {
             valuesArray = values;
             itemValue = option.getValue();
         }
 
         boolean selected = isSelected(context, menu, itemValue, valuesArray, converter);
         if(option.isNoSelectionOption() && values != null && !selected) {
             return;
         }
 
         writer.startElement("option", null);
         writer.writeAttribute("value", itemValueAsString, null);
         if(disabled) writer.writeAttribute("disabled", "disabled", null);
         if(selected) writer.writeAttribute("selected", "selected", null);
 
         if(option.isEscape())
             writer.write(option.getLabel());
         else
             writer.writeText(option.getLabel(), "value");
 
         writer.endElement("option");
     }
 
     protected int calculatePanelHeight(SelectOneMenu menu, int itemSize) {
         int height = menu.getHeight();
         
         if(height != Integer.MAX_VALUE) {
             return height;
         } else if(itemSize > 10) {
             return 200;
         }
         
         return -1;
     }
 
     @Override
     public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
 		//Rendering happens on encodeEnd
 	}
 
     @Override
 	public boolean getRendersChildren() {
 		return true;
 	}
 
     @Override
     protected String getSubmitParam(FacesContext context, UISelectOne selectOne) {
         return selectOne.getClientId(context) + "_input";
     }
 }
