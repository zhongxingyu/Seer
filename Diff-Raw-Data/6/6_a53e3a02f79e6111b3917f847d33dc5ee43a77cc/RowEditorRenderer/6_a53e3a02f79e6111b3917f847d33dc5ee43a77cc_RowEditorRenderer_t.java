 /*
  * Original Code developed and contributed by Prime Technology.
  * Subsequent Code Modifications Copyright 2011 ICEsoft Technologies Canada Corp. (c)
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
  *
  * NOTE THIS CODE HAS BEEN MODIFIED FROM ORIGINAL FORM
  *
  * Subsequent Code Modifications have been made and contributed by ICEsoft Technologies Canada Corp. (c).
  *
  * Code Modification 1: Integrated with ICEfaces Advanced Component Environment.
  * Contributors: ICEsoft Technologies Canada Corp. (c)
  *
  * Code Modification 2: [ADD BRIEF DESCRIPTION HERE]
  * Contributors: ______________________
  * Contributors: ______________________
  */
 package org.icefaces.ace.component.roweditor;
 
 import org.icefaces.ace.component.datatable.DataTable;
 import org.icefaces.ace.event.RowEditCancelEvent;
 import org.icefaces.ace.event.RowEditEvent;
 import org.icefaces.ace.model.table.RowState;
 import org.icefaces.ace.model.table.TreeDataModel;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.render.MandatoryResourceComponent;
 
import javax.faces.component.NamingContainer;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.Map;
 
 @MandatoryResourceComponent("org.icefaces.ace.component.roweditor.RowEditor")
 public class RowEditorRenderer extends CoreRenderer {
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         Map<String,String> params = context.getExternalContext().getRequestParameterMap();
         RowEditor editor = (RowEditor) component;
         String clientId = editor.getClientId(context);
 
         //Decode row edit request triggered by this editor
         if(params.containsKey(clientId)) {
             DataTable table = findParentTable(context, editor);
            String tableId = table.getClientId(context);
 
            if (context.getExternalContext().getRequestParameterMap().containsKey(tableId.substring(0, tableId.lastIndexOf(NamingContainer.SEPARATOR_CHAR)) + "_rowEdit"))
                 component.queueEvent(new RowEditEvent(component, table.getRowData()));
             else
                 component.queueEvent(new RowEditCancelEvent(component, table.getRowData()));
         }
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         RowEditor editor = (RowEditor) component;
         DataTable table = findParentTable(context, editor);
         RowState state = (RowState)(context.getExternalContext().getRequestMap().get(table.getRowStateVar()));
         
         if (state.isEditable()) {
             ResponseWriter writer = context.getResponseWriter();
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute("id", component.getClientId(context), null);
             writer.writeAttribute("class", DataTable.ROW_EDITOR_CLASS, null);
     
             writer.startElement("a", null);
             writer.writeAttribute("class", "ui-icon ui-icon-pencil", null);
             writer.writeAttribute("tabindex", "0", null);
             writer.endElement("a");
     
             writer.startElement("a", null);
             writer.writeAttribute("class", "ui-icon ui-icon-check", null);
             writer.writeAttribute("tabindex", "0", null);
             writer.writeAttribute("style", "display:none", null);
             writer.endElement("a");
     
             writer.startElement("a", null);
             writer.writeAttribute("class", "ui-icon ui-icon-close", null);
             writer.writeAttribute("tabindex", "0", null);
             writer.writeAttribute("style", "display:none", null);
             writer.endElement("a");
     
             writer.endElement(HTML.SPAN_ELEM);
         }
     }
 
     protected DataTable findParentTable(FacesContext context, RowEditor editor) {
 		UIComponent parent = editor.getParent();
 
 		while(parent != null)
             if (parent instanceof DataTable) return (DataTable) parent;
             else parent = parent.getParent();
 
 		return null;
 	}
 }
