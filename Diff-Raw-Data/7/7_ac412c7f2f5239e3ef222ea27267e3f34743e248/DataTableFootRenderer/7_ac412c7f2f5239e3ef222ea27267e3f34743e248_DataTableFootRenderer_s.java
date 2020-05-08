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
 
 package org.icefaces.ace.component.datatable;
 
 import org.icefaces.ace.component.column.Column;
 import org.icefaces.ace.component.columngroup.ColumnGroup;
 import org.icefaces.ace.component.row.Row;
 import org.icefaces.ace.util.HTML;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import java.io.IOException;
 import java.util.List;
 
 public class DataTableFootRenderer {
     protected static void encodeTableFoot(FacesContext context, DataTableRenderingContext tableContext) throws IOException {
         DataTable table = tableContext.getTable();
         List<Column> columns = tableContext.getColumns();
         ResponseWriter writer = context.getResponseWriter();
         ColumnGroup group = table.getColumnGroup("footer");
         boolean shouldRender = table.hasFooterColumn(columns) || group != null;
 
         if (!shouldRender) return;
 
         if (tableContext.isStaticHeaders() && !table.isInDuplicateSegment()) {
             writer.startElement(HTML.DIV_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.SCROLLABLE_FOOTER_CLASS, null);
             writer.startElement(HTML.TABLE_ELEM, null);
         }
 
         writer.startElement(HTML.TFOOT_ELEM, null);
 
         if (table.isInDuplicateSegment())
             writer.writeAttribute(HTML.STYLE_ATTR, "display:none;", null);
 
         if (group != null) {
             for (UIComponent child : group.getChildren()) {
                 if (child.isRendered() && child instanceof Row) {
                     Row footerRow = (Row) child;
                     writer.startElement(HTML.TR_ELEM, null);
 
                     List<UIComponent> footerRowChildren = footerRow.getChildren();
                     for (UIComponent footerRowChild : footerRowChildren)
                         if (footerRowChild.isRendered() && footerRowChild instanceof Column)
                             encodeColumnFooter(context, table, footerRowChildren,
                                     (Column) footerRowChild, true);
 
                     writer.endElement(HTML.TR_ELEM);
                 }
             }
         } else {
             writer.startElement(HTML.TR_ELEM, null);
            for (Column column : columns) {
                encodeColumnFooter(context, table, columns, column, false);
            }
             writer.endElement(HTML.TR_ELEM);
         }
         writer.endElement(HTML.TFOOT_ELEM);
 
         if (tableContext.isStaticHeaders() && !table.isInDuplicateSegment()) {
             writer.endElement(HTML.TABLE_ELEM);
             writer.endElement(HTML.DIV_ELEM);
         }
     }
 
     private static void encodeColumnFooter(FacesContext context, DataTable table, List columnSiblings, Column column, boolean subRows) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
 
         Column nextColumn = DataTableRendererUtil.getNextColumn(column, columnSiblings);
         boolean isCurrStacked = DataTableRendererUtil.isCurrColumnStacked(columnSiblings, column);
         boolean isNextStacked = (nextColumn == null) ? false
                 : (nextColumn.isRendered() && nextColumn.isStacked());
 
         if (!isCurrStacked) {
             String style = column.getStyle();
             String styleClass = column.getStyleClass();
             String footerClass = styleClass != null
                     ? DataTableConstants.COLUMN_FOOTER_CLASS + " " + styleClass
                     : DataTableConstants.COLUMN_FOOTER_CLASS;
 
             writer.startElement(HTML.TD_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, footerClass, null);
 
             if (style != null)
                 writer.writeAttribute(HTML.STYLE_ELEM, style, null);
             if (column.getRowspan() != 1)
                 writer.writeAttribute(HTML.ROWSPAN_ATTR, column.getRowspan(), null);
             if (column.getColspan() != 1)
                 writer.writeAttribute(HTML.COLSPAN_ATTR, column.getColspan(), null);
         }
         else {
             writer.startElement("hr", null);
             writer.endElement("hr");
         }
 
         //Container
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, DataTableConstants.COLUMN_FOOTER_CONTAINER_CLASS, null);
 
         //Footer content
         UIComponent facet = column.getFacet("footer");
         String text = column.getFooterText();
         if (facet != null) {
             facet.encodeAll(context);
         } else if (text != null) {
             writer.write(text);
         } else if (subRows)
             for (UIComponent c : column.getChildren())
                 c.encodeAll(context);
 
         writer.endElement(HTML.DIV_ELEM);
 
         if (!isNextStacked) {
             writer.endElement(HTML.TD_ELEM);
         } else if (subRows) {
             // If in a multirow footer case, and using stacked, enforce these restrictions
             if (!DataTableRendererUtil.areBothSingleColumnSpan(column, nextColumn))
                 throw new FacesException("DataTable : \"" + table.getClientId(context) + "\" must not have stacked footer columns, with colspan values greater than 1.");
             if (!DataTableRendererUtil.isNextColumnRowSpanEqual(column, nextColumn))
                 throw new FacesException("DataTable : \"" + table.getClientId(context) + "\" must not have stacked footer columns, with unequal rowspan values.");
         }
     }
 }
