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
 import java.util.Iterator;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.component.datalist.DataList;
 import org.primefaces.component.separator.Separator;
 import org.primefaces.renderkit.CoreRenderer;
 import org.primefaces.util.WidgetBuilder;
 
 public class DataListRenderer extends CoreRenderer {
              
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         DataList dataList = (DataList) component;                       
         
         if (dataList.isPaginationRequest(context)) {
             dataList.updatePaginationData(context, dataList);
             
             if(dataList.isLazy()) {
                 dataList.loadLazyData();
             }            
 
             encodeLoadMore(context, dataList);
         } else {
             encodeMarkup(context, dataList);
             encodeScript(context, dataList);
         }
     }
     
     protected void encodeMarkup(FacesContext context, DataList dataList) throws IOException {
         if(dataList.isLazy()) {
             dataList.loadLazyData();
         }        
         
         ResponseWriter writer = context.getResponseWriter();        
         UIComponent header = dataList.getHeader();
         UIComponent footer = dataList.getFooter();
         String type = dataList.getType();
         Object filterValue = dataList.getAttributes().get("filter");          
         Object placeholder = dataList.getAttributes().get("placeholder");
         Object autodividers = dataList.getAttributes().get("autoDividers");
         Object autoComplete = dataList.getAttributes().get("autoComplete");
         Object icon = dataList.getAttributes().get("icon");
         Object iconSplit = dataList.getAttributes().get("iconSplit");
         Object swatch = (String) dataList.getAttributes().get("swatch");
         Object dividerSwatch = (String) dataList.getAttributes().get("dividerSwatch");        
         String iconType = (iconSplit != null && Boolean.valueOf(iconSplit.toString())) ? "data-split-icon" : "data-icon";
         
         writer.startElement("ul", dataList);                        
         writer.writeAttribute("id", dataList.getClientId(context), "id");        
         if(dataList.getStyle() != null) writer.writeAttribute("style", dataList.getStyle(), null);
         if(dataList.getStyleClass() != null) writer.writeAttribute("class", dataList.getStyleClass(), null);                        
         writer.writeAttribute("data-role", "listview", null);
         
         if(filterValue != null && Boolean.valueOf(filterValue.toString())) writer.writeAttribute("data-filter", "true", null);
         if(placeholder != null) writer.writeAttribute("data-filter-placeholder", placeholder, null);
         if(autodividers != null && Boolean.valueOf(autodividers.toString())) writer.writeAttribute("data-autodividers", "true", null);
         if(autoComplete != null && Boolean.valueOf(autoComplete.toString())) writer.writeAttribute("data-filter-reveal", "true", null);        
         if(icon != null) writer.writeAttribute(iconType, icon, null);        
         if(type != null && type.equals("inset")) writer.writeAttribute("data-inset", true, null);
         if(swatch != null) writer.writeAttribute("data-theme", swatch, null); 
         if(dividerSwatch != null) writer.writeAttribute("data-divider-theme", dividerSwatch, null);         
 
         if(header != null) {
             writer.startElement("li", null);
             writer.writeAttribute("data-role", "list-divider", null);
             header.encodeAll(context);
             writer.endElement("li");
         }
         
         int rowCount = dataList.getRowCount();
         
         if(dataList.getVar() != null) {            
             if (dataList.isPaginator() && rowCount > dataList.getRows()) {
                 rowCount = dataList.getRows();
             }
 
             for (int i = 0; i < rowCount; i++) {
                 dataList.setRowIndex(i);
                 writer.startElement("li", null);
                 renderChildren(context, dataList);
                 writer.endElement("li");
             }
         }
         else {
             for(UIComponent child : dataList.getChildren()) {
                 if(child.isRendered()) {
                     writer.startElement("li", dataList);
                     
                     if(child instanceof Separator) {
                         writer.writeAttribute("data-role", "list-divider", null);
                         renderChildren(context, child);
                     }
                     else {
                         Object iconLi = child.getAttributes().get("icon");
                         Object filterText = child.getAttributes().get("filterText"); 
                         if(iconLi != null) writer.writeAttribute("data-icon", iconLi, null);
                         if (filterText != null) writer.writeAttribute("data-filtertext", filterText, null);              
                         child.encodeAll(context);
                     }
                     
                     writer.endElement("li");
                 }
             }
         }
         
         if (footer != null) {
             writer.startElement("div", null); 
             writer.writeAttribute("style", "margin-top: 20px;text-align: center;", null);                        
             footer.encodeAll(context);
             writer.endElement("div");
         }            
         
         
         if (dataList.isPaginator() && (dataList.getRows() < dataList.getRowCount())) {
             encodePaginatorButton(context, dataList);
         }                
 
         writer.endElement("ul");
                                                  
         dataList.setRowIndex(-1);        
     }
         
     protected void encodePaginatorButton(FacesContext context, DataList dataList) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId =  dataList.getClientId(context) + "_btn";
         Object paginatorText = (dataList.getAttributes().get("paginatorText") == null) ? "More" : dataList.getAttributes().get("paginatorText");
         
         writer.startElement("a", dataList);
         writer.writeAttribute("id", clientId, null);        
         writer.writeAttribute("data-role", "button", null);
         writer.writeAttribute("style", "margin-top: 20px", null);        
         writer.writeText(paginatorText, null);       
         writer.endElement("a");        
     }  
     
     protected void encodeLoadMore(FacesContext context, DataList dataList) throws IOException {
         ResponseWriter writer = context.getResponseWriter();        
                              
         for (int i = dataList.getFirst(); i < (dataList.getFirst() + dataList.getRows()); i++) {
             dataList.setRowIndex(i);
             
             if (dataList.isRowAvailable()) {
                 writer.startElement("li", null);
                 renderChildren(context, dataList);
                 writer.endElement("li");
             }
         }
         
         dataList.setRowIndex(-1); 
     }    
     
     protected void encodeScript(FacesContext context, DataList dataList) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         String clientId = dataList.getClientId(context);
         WidgetBuilder wb = getWidgetBuilder(context);                 
         wb.widget("DataList", dataList.resolveWidgetVar(), clientId, true)
                 .attr("isPaginator", dataList.isPaginator())                
                 .attr("scrollStep", dataList.getRows())
                 .attr("scrollLimit", dataList.getRowCount());
         startScript(writer, clientId);
         writer.write(wb.build());
         endScript(writer);
     }    
     
     @Override
     protected void renderChildren(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         for (Iterator<UIComponent> iterator = component.getChildren().iterator(); iterator.hasNext();) {
             UIComponent child = (UIComponent) iterator.next();
             Object iconLi = child.getAttributes().get("icon");
             Object filterText = child.getAttributes().get("filterText");            
             if (iconLi != null) {
                 writer.writeAttribute("data-icon", iconLi, null);
             }
             if (filterText != null) {
                 writer.writeAttribute("data-filtertext", filterText, null);
             }            
             renderChild(context, child);
         }
     }
     
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         //Do Nothing
     }
 
     @Override
     public boolean getRendersChildren() {
         return true;
     }
}
