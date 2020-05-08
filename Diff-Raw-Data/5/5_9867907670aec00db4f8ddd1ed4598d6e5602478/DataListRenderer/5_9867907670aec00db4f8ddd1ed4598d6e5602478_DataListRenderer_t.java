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
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import org.primefaces.component.datalist.DataList;
 import org.primefaces.renderkit.CoreRenderer;
 
 public class DataListRenderer extends CoreRenderer {
     
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         DataList dataList = (DataList) component;
         UIComponent header = dataList.getHeader();
         String type = dataList.getType();
        Object filterValue = dataList.getAttributes().get("filter");
 
         writer.startElement("ul", dataList);
         writer.writeAttribute("id", dataList.getClientId(context), "id");
         writer.writeAttribute("data-role", "listview", null);
        
        if(filterValue != null && Boolean.valueOf(filterValue.toString())) writer.writeAttribute("data-filter", "true", null);
         if(type != null && type.equals("inset")) writer.writeAttribute("data-inset", true, null);
         if(dataList.getStyle() != null) writer.writeAttribute("style", dataList.getStyle(), null);
         if(dataList.getStyleClass() != null) writer.writeAttribute("class", dataList.getStyleClass(), null);
 
         if(header != null) {
             writer.startElement("li", null);
             writer.writeAttribute("data-role", "list-divider", null);
             header.encodeAll(context);
             writer.endElement("li");
         }
 
         if(dataList.getVar() != null) {
             int rowCount = dataList.getRowCount();
 
             for(int i = 0; i < rowCount; i++) {
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
                     child.encodeAll(context);
                     writer.endElement("li");
                 }
             }
         }
         writer.endElement("ul");
 
         dataList.setRowIndex(-1);
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
