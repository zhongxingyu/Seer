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
 
 package com.icesoft.faces.component.ext.renderkit;
 
 import com.icesoft.faces.component.CSS_DEFAULT;
 import com.icesoft.faces.component.ExtendedAttributeConstants;
 import com.icesoft.faces.component.PORTLET_CSS_DEFAULT;
 import com.icesoft.faces.component.commandsortheader.CommandSortHeader;
 import com.icesoft.faces.component.ext.HeaderRow;
 import com.icesoft.faces.component.ext.ColumnGroup;
 import com.icesoft.faces.component.ext.HtmlDataTable;
 import com.icesoft.faces.component.ext.RowSelector;
 import com.icesoft.faces.component.ext.UIColumns;
 import com.icesoft.faces.component.ext.taglib.Util;
 import com.icesoft.faces.context.DOMContext;
 import com.icesoft.faces.context.effects.JavascriptContext;
 import com.icesoft.faces.renderkit.dom_html_basic.FormRenderer;
 import com.icesoft.faces.renderkit.dom_html_basic.HTML;
 import com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer;
 import com.icesoft.faces.renderkit.dom_html_basic.PassThruAttributeWriter;
 import com.icesoft.faces.util.CoreUtils;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import javax.faces.component.NamingContainer;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UINamingContainer;
 import javax.faces.context.FacesContext;
 import java.io.IOException;
 import java.util.*;
 
 import com.icesoft.util.pooling.ClientIdPool;
 import com.icesoft.util.pooling.CSSNamePool;
 
 public class TableRenderer
         extends com.icesoft.faces.renderkit.dom_html_basic.TableRenderer {
 
 
     private static final String SELECTED_ROWS = "sel_rows";
     
     private static final String CLICKED_ROW = "click_row";
     private static final String CLICK_COUNT = "click_count";
     private static final String[] passThruAttributes = ExtendedAttributeConstants.getAttributes(ExtendedAttributeConstants.ICE_ROWSELECTOR);
 
     public String getComponentStyleClass(UIComponent uiComponent) {
         return (String) uiComponent.getAttributes().get("styleClass");
 
     }
 
     public String getHeaderClass(UIComponent component) {
         return (String) component.getAttributes().get("headerClass");
     }
 
     public String getFooterClass(UIComponent component) {
         return (String) component.getAttributes().get("footerClass");
     }
 
     // row styles are returned by reference
     public String[] getRowStyles(UIComponent uiComponent) {
         if (((String[]) getRowStyleClasses(uiComponent)).length <= 0) {
             String[] rowStyles = new String[2];
             rowStyles[0] = Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_ROW_CLASS1);
             rowStyles[1] =Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_ROW_CLASS2);
             return rowStyles;
         } else {
             return getRowStyleClasses(uiComponent);
         }
     }
 
     public String[] getHeaderStyles(UIComponent uiComponent) {
         String headerClass = getHeaderClass(uiComponent).
         replaceAll(CSS_DEFAULT.TABLE_STYLE_CLASS + CSS_DEFAULT.TABLE_HEADER_CLASS, "");
     	if (((String[]) getHeaderStyleClasses(uiComponent)).length <= 0) {
             String[] headerStyles = new String[2];
             headerStyles[0] = CSSNamePool.get(Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_COLUMN_HEADER_CLASS1) + ((headerClass.length() > 0)
                     ? headerClass : ""));
             headerStyles[1] = CSSNamePool.get(Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_COLUMN_HEADER_CLASS2)+ ((headerClass.length() > 0)
                             ? headerClass : ""));
 
             return headerStyles;
         } else {
             return getHeaderStyleClasses(uiComponent);
         }
     }
 
     public void writeColStyles(String[] columnStyles, int columnStylesMaxIndex,
                                int columnStyleIndex, Element td,
                                UIComponent uiComponent
                                 ) {
         if (columnStyles.length > 0) {
             if (columnStylesMaxIndex >= 0) {
                 td.setAttribute("class", columnStyles[columnStyleIndex]);
             }
         }
     }
 
     protected void renderFacet(FacesContext facesContext,
                                UIComponent uiComponent,
                                DOMContext domContext, boolean header)
             throws IOException {
         String facet, tag, element, facetClass;
         if (header) {
             facet = "header";
             tag = HTML.THEAD_ELEM;
             element = HTML.TH_ELEM;
             facetClass = getHeaderClass(uiComponent);
         } else {
             facet = "footer";
             tag = HTML.TFOOT_ELEM;
             element = HTML.TD_ELEM;
             facetClass = getFooterClass(uiComponent);
         }
         HtmlDataTable uiData = (HtmlDataTable) uiComponent;
         uiData.setRowIndex(-1);
         Element root = (Element) domContext.getRootNode();
         if (isScrollable(uiComponent)) {
 
             if (header) {
                 // First table in first div path : table/tr/td/div/div0/table
                 root = getScrollableHeaderTableElement(root);
                 ((Element)root.getParentNode()).setAttribute(HTML.CLASS_ATTR,
                         Util.getQualifiedStyleClass(uiComponent,
                         CSS_DEFAULT.TABLE_SCRL_SPR));
             } else {
                 // First table in second div path table/tr/td/div/div2/table
                 if (uiData.isScrollFooter()) {
                     root = getScrollableBodyTableElement(root);
                 } else {
                     root = getScrollableFooterTableElement(root);
                     ((Element)root.getParentNode()).setAttribute(HTML.CLASS_ATTR,
                             Util.getQualifiedStyleClass(uiComponent,
                             CSS_DEFAULT.TABLE_SCRL_SPR+CSS_DEFAULT.TABLE_FOOTER_CLASS));
                 }
             }
         }
         UIComponent headerFacet = getFacetByName(uiData, facet);
         boolean childHeaderFacetExists =
                 childColumnHasFacetWithName(uiData, facet);
         Element thead = null;
         if (headerFacet != null || childHeaderFacetExists) {
             String clientId = uiData.getContainerClientId(facesContext);
             thead = domContext.createElement(tag);
             thead.setAttribute(HTML.ID_ATTR, ClientIdPool.get
                     (clientId + NamingContainer.SEPARATOR_CHAR + tag));
             root.appendChild(thead);
 
 
             if (header) {
             	if(CoreUtils.getPortletStyleClass(PORTLET_CSS_DEFAULT
             			.PORTLET_SECTION_HEADER).length() > 1) {
                 	thead.setAttribute(HTML.CLASS_ATTR, PORTLET_CSS_DEFAULT
                 			.PORTLET_SECTION_HEADER);
             	}
                 renderTableHeader(facesContext, uiComponent, headerFacet, thead, facetClass, element);
                 renderColumnGroup(facesContext, uiComponent, headerFacet, thead, facetClass, element);
                 if (childHeaderFacetExists) {
                     renderColumnHeader(facesContext, uiComponent, thead, facet, element, header);
                     if (!uiData.isClientOnly()) {
                         Element clientOnly = domContext.createElement(HTML.INPUT_ELEM);
                         clientOnly.setAttribute(HTML.TYPE_ATTR, "hidden");
                         clientOnly.setAttribute(HTML.ID_ATTR, clientId + "clientOnly");
                         clientOnly.setAttribute(HTML.NAME_ATTR, clientId + "clientOnly");
                         root.appendChild(clientOnly);
                         uiData.resetResizableTblColumnsWidthIndex();
                     }
                 }
             } else {
             	if(CoreUtils.getPortletStyleClass(PORTLET_CSS_DEFAULT
             			.PORTLET_SECTION_FOOTER).length() > 1) {
                 	thead.setAttribute(HTML.CLASS_ATTR, PORTLET_CSS_DEFAULT
                 			.PORTLET_SECTION_FOOTER);
             	}
                 if (childHeaderFacetExists) {
                     renderColumnHeader(facesContext, uiComponent, thead, facet, element, header);
                 }
                 renderColumnGroup(facesContext, uiComponent, headerFacet, thead, facetClass, element);
                 renderTableHeader(facesContext, uiComponent, headerFacet, thead, facetClass, element);
 
             }
             domContext.setCursorParent(root);
         }
     }
 
     private void renderColumnHeader(FacesContext facesContext,
                                     UIComponent uiComponent,
                                     Element thead,
                                     String facet,
                                     String element,
                                     boolean header) throws IOException {
         HtmlDataTable htmlDataTable = (HtmlDataTable)uiComponent;
         DOMContext domContext =
             DOMContext.getDOMContext(facesContext, uiComponent);
             Element tr = domContext.createElement("tr");
             thead.appendChild(tr);
             List childList = uiComponent.getChildren(); 
             Iterator childColumns = childList.iterator();
             int columnIndex = 1;
             int headerStyleLength = getHeaderStyles(uiComponent).length;
             int styleIndex = 0;
             String[] columnWidths = getColumnWidthsArray(htmlDataTable);
             htmlDataTable.setColNumber(-1);
             while (childColumns.hasNext()) {
                 UIComponent nextColumn = (UIComponent) childColumns.next();
                 if (nextColumn instanceof UIColumn) {
                     htmlDataTable.setColNumber(htmlDataTable.getColNumber()+1);
                 }
                 if (!nextColumn.isRendered()) continue;
                 if (nextColumn instanceof UIColumn) {
 //                    htmlDataTable.setColNumber(htmlDataTable.getColNumber()+1);
                     processUIColumnHeader(facesContext, uiComponent,
                                           (UIColumn) nextColumn, tr, domContext,
                                           facet, element,
                                           columnIndex,
                                           styleIndex,
                                           !childColumns.hasNext(),
                                           columnWidths);
                    columnIndex++;
                 } else if (nextColumn instanceof UIColumns) {
                     columnIndex = processUIColumnsHeader(facesContext,
                                                          uiComponent,
                                                          (UIColumns) nextColumn,
                                                          tr, domContext, facet,
                                                          element, columnIndex,
                                                          styleIndex,
                                                          headerStyleLength,
                                                          columnWidths);
                 }
 
                 if (styleIndex++ == (headerStyleLength-1)) {
                     styleIndex = 0;
                 }
             }
             if (header && isScrollable(uiComponent)) {
                 tr.appendChild(scrollBarSpacer(domContext, facesContext));
                 //being compatible with the XHTML DTD
 /*              commented out for ICE-2974
                 Element tbody = (Element) domContext.createElement(HTML.TBODY_ELEM);
                 tbody.setAttribute(HTML.STYLE_ATTR, "display:none");
                 thead.getParentNode().appendChild(tbody);
                 Element tbodyTr = (Element) domContext.createElement(HTML.TR_ELEM);
                 tbody.appendChild(tbodyTr);
                 tbodyTr.appendChild(domContext.createElement(HTML.TD_ELEM));
 */
             }
     }
 
     private void renderColumnGroup(FacesContext facesContext,
             UIComponent uiComponent,
             UIComponent headerFacet,
             Element thead,
             String facetClass,
             String element
             ) throws IOException{
 
         DOMContext domContext =
             DOMContext.getDOMContext(facesContext, uiComponent);
         if (headerFacet== null || !(headerFacet instanceof ColumnGroup)) return;
         if (!headerFacet.isRendered()) return;
         String sourceClass = CSS_DEFAULT.TABLE_HEADER_CLASS;
         if (!"th".equals(element)) {
             sourceClass = CSS_DEFAULT.TABLE_FOOTER_CLASS;
         }
         String baseClass= Util.getQualifiedStyleClass(uiComponent, "ColGrp"+sourceClass);
         HtmlDataTable htmlDataTable = (HtmlDataTable)uiComponent;
         Iterator children = headerFacet.getChildren().iterator();
         while (children.hasNext()) {
             UIComponent child = (UIComponent) children.next();
             if (child instanceof HeaderRow) {
                 if(!child.isRendered())
                     continue;
                 Element tr = domContext.createElement("tr");
                 String rowStyleClass = ((HeaderRow)child).getStyleClass();
                 if (rowStyleClass == null) {
                     rowStyleClass = baseClass.replaceAll(sourceClass, sourceClass+"Row");
                 } else {
                     rowStyleClass = baseClass.replaceAll(sourceClass, sourceClass+"Row ") + rowStyleClass;
                 }
                 tr.setAttribute(HTML.CLASS_ATTR, CSSNamePool.get(rowStyleClass));
                 String rowStyle = ((HeaderRow)child).getStyle();
                 if (rowStyle != null) {
                     tr.setAttribute(HTML.STYLE_ATTR, rowStyle);
                 }
                 thead.appendChild(tr);
                 Iterator columns = child.getChildren().iterator();
                 while (columns.hasNext()) {
                     UIComponent column = (UIComponent) columns.next();
                     if (!(column instanceof UIColumn) || !column.isRendered()) {
                         continue;
                     }
                     Element th = domContext.createElement(element);
                     tr.appendChild(th);
                     String styleClass = ((com.icesoft.faces.component.ext.UIColumn)
                             column).getStyleClass();
                     if(styleClass == null) {
                         styleClass = baseClass.replaceAll(sourceClass, sourceClass+"Col");
                     } else {
                         styleClass = baseClass.replaceAll(sourceClass, sourceClass+"Col ")+ styleClass;
                     }
                     String sortColumn = htmlDataTable.getSortColumn();
 
                     if (sortColumn != null) {
                         Iterator it = column.getChildren().iterator();
 
                         while (it.hasNext()) {
                             UIComponent columnChild = (UIComponent) it.next();
 
                             if (columnChild instanceof CommandSortHeader) {
                                 String columnName = ((CommandSortHeader) columnChild).getColumnName();
 
                                 if (sortColumn.equals(columnName)) {
                                     styleClass += CSS_DEFAULT.TABLE_ACTIVE_SORT_COLUMN ;
                                 }
 
                                 break;
                             }
                         }
                     }
                     th.setAttribute(HTML.CLASS_ATTR, CSSNamePool.get(styleClass));
                     Integer colspan = null;
                     try {
                         colspan = Integer.valueOf(((com.icesoft.faces.component.ext.UIColumn)
                                 column).getColspan());
                     } catch (Exception e) {}
 
                     if (htmlDataTable.isResizable()) {
                         if (colspan != null) {
                             colspan = new Integer(colspan.intValue()+ colspan.intValue());
                         } else {
                             colspan = new Integer(2);
                         }
                     }
 
                     if (colspan != null) {
                         th.setAttribute(HTML.COLSPAN_ATTR, colspan.toString());
                     }
 
                     String rowspan = ((com.icesoft.faces.component.ext.UIColumn)
                                                 column).getRowspan();
                     if (rowspan != null) {
                          th.setAttribute(HTML.ROWSPAN_ATTR, rowspan);
                     }
 
                     String style = ((com.icesoft.faces.component.ext.UIColumn)column)
                                                         .getStyle();
                     if (style != null) {
                         th.setAttribute(HTML.STYLE_ATTR, style);
                     }
 
                     domContext.setCursorParent(th);
                     encodeParentAndChildren(facesContext, column);
                 }
                 if (isScrollable(uiComponent)) {
                     tr.appendChild(scrollBarSpacer(domContext, facesContext));
                 }
             }
         }
     }
 
     private void renderTableHeader(FacesContext facesContext,
                                     UIComponent uiComponent,
                                     UIComponent headerFacet,
                                     Element thead,
                                     String facetClass,
                                     String element
                                     ) throws IOException{
         DOMContext domContext =
             DOMContext.getDOMContext(facesContext, uiComponent);
         if (headerFacet != null && headerFacet.isRendered()) {
             if (headerFacet instanceof ColumnGroup)return;
             resetFacetChildId(headerFacet);
             Element tr = domContext.createElement("tr");
             thead.appendChild(tr);
             Element th = domContext.createElement(element);
             tr.appendChild(th);
             if (facetClass != null) {
                 th.setAttribute("class", facetClass);
             }
             int columns = getNumberOfChildColumns(uiComponent);
             if (((HtmlDataTable)uiComponent).isResizable()) {
             	columns+=(columns-1);
             }
         	th.setAttribute("colspan",
                     String.valueOf(columns));            
             th.setAttribute("scope", "colgroup");
             domContext.setCursorParent(th);
             encodeParentAndChildren(facesContext, headerFacet);
             if (isScrollable(uiComponent)) {
                 tr.appendChild(scrollBarSpacer(domContext, facesContext));
             }
         }
     }
 
     private void processUIColumnHeader(FacesContext facesContext,
                                        UIComponent uiComponent,
                                        UIColumn nextColumn, Element tr,
                                        DOMContext domContext, String facet,
                                        String element, int columnIndex,
                                        int styleIndex,
                                        boolean lastChild,
                                        String[] columnsWidth)
             throws IOException {
         HtmlDataTable htmlDataTable = (HtmlDataTable) uiComponent;
         Element th = domContext.createElement(element);
         tr.appendChild(th);
         th.setAttribute(HTML.SCOPE_ATTR, "col");
         Element cursorParent = th;
         if (htmlDataTable.isResizable()) {
             if (!lastChild) {
                 Element handlerTd = domContext.createElement(element);
                 handlerTd.setAttribute("valign", "top");
                 handlerTd.setAttribute(HTML.CLASS_ATTR, "iceDatTblResBor");
                 handlerTd.setAttribute(HTML.ONMOUSEOVER_ATTR, "ResizableUtil.adjustHeight(this)");
                 Element resizeHandler = domContext.createElement(HTML.DIV_ELEM);
                 resizeHandler.setAttribute(HTML.STYLE_ATTR, "cursor: e-resize; display:block;  height:100%;");
                 resizeHandler.setAttribute(HTML.ONMOUSEDOWN_ATTR, "new Ice.ResizableGrid(event);");
                 resizeHandler.setAttribute(HTML.CLASS_ATTR, "iceDatTblResHdlr");
                 resizeHandler.appendChild(domContext.createTextNodeUnescaped(HTML.NBSP_ENTITY));
                 handlerTd.appendChild(resizeHandler);
                 tr.appendChild(handlerTd);
             }
             Element columnHeaderDiv = domContext.createElement(HTML.DIV_ELEM);
             columnHeaderDiv.setAttribute(HTML.ID_ATTR, ClientIdPool.get(htmlDataTable.getContainerClientId(facesContext)+
 				UINamingContainer.getSeparatorChar(facesContext)+"hdrDv"+ columnIndex)); 
             th.appendChild(columnHeaderDiv);
             if (htmlDataTable.isResizable()) {
                 String nextWidth = htmlDataTable.getNextResizableTblColumnWidth();
                 if (nextWidth != null) {
                     columnHeaderDiv.setAttribute(HTML.STYLE_ATTR, "width:"+ nextWidth +";");
                 }
             }
             cursorParent = columnHeaderDiv;
         }
         if ("header".equalsIgnoreCase(facet) ){
             String styles = this.getHeaderStyles(uiComponent)[styleIndex];
             String sortColumn = htmlDataTable.getSortColumn();
             UIComponent headerFacet = nextColumn.getFacet("header");
 
             if (sortColumn != null && (headerFacet instanceof CommandSortHeader)) {
                 String columnName = ((CommandSortHeader) headerFacet).getColumnName();
 
                 if (sortColumn.equals(columnName)) {
                     styles += CSS_DEFAULT.TABLE_ACTIVE_SORT_COLUMN;
                 }
             }
 
             th.setAttribute("class", styles);
 
         } else {
             th.setAttribute("class",getFooterClass(htmlDataTable));
         }
         String width = getWidthFromColumnWidthsArray(htmlDataTable, columnsWidth);
         if (width != null) {
             th.setAttribute("style", width);
         }
         //th.setAttribute("colgroup", "col");
         UIComponent nextFacet = getFacetByName(nextColumn, facet);
 
         if (nextFacet != null) {
             resetFacetChildId(nextFacet);
             domContext.setCursorParent(cursorParent);
             encodeParentAndChildren(facesContext, nextFacet);
         }
     }
 
     private int processUIColumnsHeader(FacesContext facesContext,
                                        UIComponent uiComponent,
                                        UIColumns nextColumn, Element tr,
                                        DOMContext domContext, String facet,
                                        String element, int columnIndex,
                                        int styleIndex,
                                        int headerStyleLength,
                                        String[] columnsWidth)
             throws IOException {
         HtmlDataTable htmlDataTable = (HtmlDataTable) uiComponent;
         int rowIndex = nextColumn.getFirst();
         //syleIndex should be increment here
         nextColumn.encodeBegin(facesContext);
         nextColumn.setRowIndex(rowIndex);
         String sortColumn = htmlDataTable.getSortColumn();
         while (nextColumn.isRowAvailable()) {
             UIComponent headerFacet = getFacetByName(nextColumn, facet);
             htmlDataTable.setColNumber(htmlDataTable.getColNumber()+1);
             if (headerFacet != null) {
                 Node oldParent = domContext.getCursorParent();
                 Element th = domContext.createElement(element);
                 tr.appendChild(th);
                 th.setAttribute(HTML.SCOPE_ATTR, "col");
                 String styleClass = getHeaderStyles(uiComponent)[styleIndex];
                 if (headerFacet instanceof CommandSortHeader) {
                     String columnName = ((CommandSortHeader) headerFacet).getColumnName();
                     if (sortColumn.equals(columnName)) {
                         styleClass += CSS_DEFAULT.TABLE_ACTIVE_SORT_COLUMN ;
                     }                    
                 }
                 th.setAttribute("class",styleClass);
                 String width = null;
                 Element cursorParent = th;
                 if (htmlDataTable.isResizable()) {
                     Element columnHeaderDiv = domContext.createElement(HTML.DIV_ELEM);
                     columnHeaderDiv.setAttribute(HTML.ID_ATTR, ClientIdPool.get(htmlDataTable.getContainerClientId(facesContext)+
 						UINamingContainer.getSeparatorChar(facesContext)+"hdrDv"+ columnIndex)); 
                     th.appendChild(columnHeaderDiv);
                     width = htmlDataTable.getNextResizableTblColumnWidth();
                     if (width != null) {
                         width = "width:"+ width +";";
                         columnHeaderDiv.setAttribute(HTML.STYLE_ATTR, width);
                     }
                     cursorParent = columnHeaderDiv;                    
                 }
                 
    
                 if (width == null) {
                     width = getWidthFromColumnWidthsArray(htmlDataTable, columnsWidth);
                 }
                 if (width != null) {
                     th.setAttribute("style", width);
                 }
                 //th.setAttribute("colgroup", "col");
                 domContext.setCursorParent(cursorParent);
                 encodeParentAndChildren(facesContext, headerFacet);
                 domContext.setCursorParent(oldParent);
             }
             if (styleIndex++ == (headerStyleLength-1)) {
                 styleIndex = 0;
             }
             rowIndex++;
             columnIndex++;
             nextColumn.setRowIndex(rowIndex);
 
             if (htmlDataTable.isResizable() && nextColumn.getRowCount() > rowIndex) {
                 Element handlerTd = domContext.createElement(element);
                 handlerTd.setAttribute("valign", "top");
                 handlerTd.setAttribute(HTML.CLASS_ATTR, "iceDatTblResBor");
                 handlerTd.setAttribute(HTML.ONMOUSEOVER_ATTR, "ResizableUtil.adjustHeight(this)");
                 Element resizeHandler = domContext.createElement(HTML.DIV_ELEM);
                 resizeHandler.setAttribute(HTML.STYLE_ATTR, "cursor: e-resize; display:block;  height:100%;");
                 resizeHandler.setAttribute(HTML.ONMOUSEDOWN_ATTR, "new Ice.ResizableGrid(event);");
                 resizeHandler.setAttribute(HTML.CLASS_ATTR, "iceDatTblResHdlr");
                 resizeHandler.appendChild(domContext.createTextNodeUnescaped(HTML.NBSP_ENTITY));
                 handlerTd.appendChild(resizeHandler);
                 tr.appendChild(handlerTd);
             }
         }
         nextColumn.setRowIndex(-1);
         return columnIndex;
     }
 
 
     public void encodeChildren(FacesContext facesContext,
                                UIComponent uiComponent) throws IOException {
         validateParameters(facesContext, uiComponent, null);
         DOMContext domContext =
                 DOMContext.getDOMContext(facesContext, uiComponent);
         Element root = (Element) domContext.getRootNode();
         Element originalRoot = root;
         String clientId = uiComponent.getContainerClientId(facesContext);
         
         boolean scrollable = isScrollable(uiComponent); 
         if (scrollable) {
         	Element hdrTbl = this.getScrollableHeaderTableElement(root);
         	hdrTbl.setAttribute(HTML.CLASS_ATTR, Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_SCRL_HDR_TBL));
             root = getScrollableBodyTableElement(root);
             root.setAttribute(HTML.CLASS_ATTR, Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_SCRL_BDY_TBL));
 
         }
         DOMContext.removeChildrenByTagName(root, HTML.TBODY_ELEM);
         Element tBody = (Element) domContext.createElement(HTML.TBODY_ELEM);
         tBody.setAttribute(HTML.ID_ATTR, ClientIdPool.get
                 (clientId + NamingContainer.SEPARATOR_CHAR + HTML.TBODY_ELEM));
 /*
         if (CoreUtils.getPortletStyleClass(PORTLET_CSS_DEFAULT.
         							PORTLET_SECTION_BODY).length() > 1) {
         	tBody.setAttribute(HTML.CLASS_ATTR, PORTLET_CSS_DEFAULT.PORTLET_SECTION_BODY);
         }
 */
         root.appendChild(tBody);
 
         HtmlDataTable uiData = (HtmlDataTable) uiComponent;
         uiData.ensureFirstRowInRange(); // ICE-2783
         int rowIndex = uiData.getFirst();
         int rowCount = uiData.getRowCount(); 
         if (rowCount == 0) {
             Element tr = (Element) domContext.createElement(HTML.TR_ELEM);
             tBody.appendChild(tr);
             int cols = 0;
             Iterator it = getRenderedChildColumnsList(uiData).iterator();
             while (it.hasNext()) {
                 UIComponent component = (UIComponent) it.next();
                 if (component instanceof UIColumns) {
                     cols += ((UIColumns) component).getRowCount();
                 } else if (component instanceof UIColumn) {
                     cols += 1;
                 }
             }
             if( cols == 0 ) cols = 1;
             for(int i = 0 ; i < cols ; i++ ){
             	Element td = (Element) domContext.createElement(HTML.TD_ELEM);
                 tr.appendChild(td);
             }
             domContext.stepOver();
             return;
         }
         if (rowCount >=0 && rowCount <= rowIndex) {
             domContext.stepOver();
             return;
         }
         uiData.setRowIndex(rowIndex);
         int numberOfRowsToDisplay = uiData.getRows();
         int countOfRowsDisplayed = 0;
         String rowStyles[] = getRowStyles(uiComponent);
         int rowStyleIndex = 0;
         int rowStylesMaxIndex = rowStyles.length - 1;
         RowSelector rowSelector = getRowSelector(uiComponent);
         boolean rowSelectorFound = rowSelector != null;
         boolean toggleOnClick = false;
         String rowSelectionFunctionName = null;
         String rowSelectionUseEvent = "false";
         boolean rowSelectorCodeAdded = false; // Row selector code needs to be added to the first TD, adding it to the table body breaks safari
         Element scriptNode = null;
         Element hiddenInputNode = null;
         Element hiddenClickedRowField = null;
         Element hiddenClickCountField = null;
 
         UIComponent form = DomBasicRenderer.findForm(uiComponent);
         String formId = form == null ? "" : form.getClientId(facesContext);
         String paramId = getSelectedRowParameterName(facesContext, uiData);
         if (rowSelectorFound) {
             FormRenderer.addHiddenField(facesContext, paramId + "ctrKy");            
             FormRenderer.addHiddenField(facesContext, paramId + "sftKy");
             
             toggleOnClick = rowSelector.getToggleOnClick().booleanValue();
             Element rowSelectedField =
                     domContext.createElement(HTML.INPUT_ELEM);
             // toggleOnInput = true/default ==> rowSelectionUseEvent = false
             // toggleOnInput = false        ==> rowSelectionUseEvent = true
             //  since we use the event to thwart toggling from an input
             boolean toggleOnInput =
                 rowSelector.getToggleOnInput().booleanValue();
             rowSelectionUseEvent = toggleOnInput ? "false" : "true";
 
             rowSelectedField.setAttribute(HTML.ID_ATTR, paramId);
             rowSelectedField.setAttribute(HTML.NAME_ATTR, paramId);
             rowSelectedField.setAttribute(HTML.TYPE_ATTR, "hidden");
             hiddenInputNode = rowSelectedField;
             rowSelectionFunctionName = "Ice.tableRowClicked";
             
 
             Element clickedRowField = domContext.createElement(HTML.INPUT_ELEM);
             String clickedRowParam = getClickedRowParameterName(facesContext, uiData);
             clickedRowField.setAttribute(HTML.TYPE_ATTR, "hidden");
             clickedRowField.setAttribute(HTML.NAME_ATTR, clickedRowParam); 
             
             Element clickCountField = domContext.createElement(HTML.INPUT_ELEM);
             String clickCountParam = getClickCountParameterName(facesContext, uiData);
             clickCountField.setAttribute(HTML.TYPE_ATTR, "hidden");
             clickCountField.setAttribute(HTML.NAME_ATTR, clickCountParam); 
             
             hiddenClickedRowField = clickedRowField;
             hiddenClickCountField = clickCountField;
         }
         
         Boolean isResizable = null;
         String columnStyles[] = getColumnStyleClasses(uiComponent);
         int columnStyleIndex;
         int columnStylesMaxIndex = columnStyles.length - 1;
         resetGroupState(uiData);
        while (uiData.isRowAvailable()) {
             columnStyleIndex = 0;
             String selectedClass = null;
             if (rowStylesMaxIndex >= 0) {
                selectedClass = rowStyles[rowStyleIndex];
             }
             Iterator childs = uiData.getChildren().iterator();
             Element tr = (Element) domContext.createElement(HTML.TR_ELEM);
 
             tr.setAttribute("tabindex", "0");
 
             if (rowSelectorFound) {
                 if (toggleOnClick) {
                     String toggleClass = ""; // Don't change the row style before the server round-trip
                     if (rowSelector.isPreStyleOnSelection()) {
                         if (Boolean.TRUE.equals(rowSelector.getValue())) {
                             toggleClass = selectedClass + " " + rowSelector.getStyleClass();
                         } else {
                             toggleClass = selectedClass + " " + rowSelector.getSelectedClass();
                         }
                     }
                     toggleClass = CSSNamePool.get(getPortletAlternateRowClass(toggleClass, rowIndex));
                     if (null == rowSelector.getClickListener() && null == rowSelector.getClickAction()) {
                     tr.setAttribute("onclick", rowSelectionFunctionName +
                             "(event, "+rowSelectionUseEvent+",'"+uiData.getRowIndex()+
                             "', '"+ formId +"', '"+ paramId +"','" + toggleClass + "', this);");
                     } else {
                         String delay = String.valueOf(rowSelector.getDblClickDelay().intValue());
                         tr.setAttribute("onclick", "Ice.registerClick(this,'"
                             + getClickedRowParameterName(facesContext, uiData) + "','"
                             + getClickCountParameterName(facesContext, uiData) + "',"
                             + "'" +uiData.getRowIndex()+ "','"+ formId +"',"+delay+",true,event,"+rowSelectionUseEvent+","
                             + "'"+ paramId +"','" + toggleClass + "');");
                         tr.setAttribute("ondblclick", "Ice.registerDblClick(this);");
                     }
                 } else {
                     if (rowSelector.getClickListener() != null || rowSelector.getClickAction() != null) {
                         String delay = String.valueOf(rowSelector.getDblClickDelay().intValue());
                         tr.setAttribute("onclick", "Ice.registerClick(this,'"
                             + getClickedRowParameterName(facesContext, uiData) + "','"
                             + getClickCountParameterName(facesContext, uiData) + "',"
                             + "'" +uiData.getRowIndex()+ "','"+ formId +"',"+delay+",false);");
                         tr.setAttribute("ondblclick", "Ice.registerDblClick(this);");
                     }
                 }
                 // disable text selection
                 if (rowSelector.getClickListener() != null 
                     || rowSelector.getClickAction() != null 
                     || rowSelector.isEnhancedMultiple()) {
                     tr.setAttribute("onmousedown", "return Ice.preventTextSelection(event);");
                 }
             }
             String id = uiComponent.getContainerClientId(facesContext);
             tr.setAttribute(HTML.ID_ATTR, ClientIdPool.get(id));
             Element anchor = domContext.createElement(HTML.ANCHOR_ELEM);
             if (rowSelectorFound) {
                 if (Boolean.TRUE.equals(rowSelector.getValue())){
                     selectedClass  += " "+ rowSelector.getSelectedClass();
                     tr.setAttribute(HTML.ONMOUSEOVER_ATTR, "this.className='"+ CoreUtils.getPortletStyleClass("portlet-section-body-hover") + " "+ rowSelector.getSelectedMouseOverClass() +"';");
                 } else {
                     selectedClass  += " "+ rowSelector.getStyleClass();
                     tr.setAttribute(HTML.ONMOUSEOVER_ATTR, "this.className='"+ CoreUtils.getPortletStyleClass("portlet-section-body-hover") + " "+ rowSelector.getMouseOverClass() +"';");
                 }
 //              tr.setAttribute(HTML.ONMOUSEOUT_ATTR, "this.className='"+ selectedClass +"'"); commented out for ICE-2571
                 tr.setAttribute(HTML.ONMOUSEOUT_ATTR, "Ice.enableTxtSelection(document.body); this.className='" +
                         getPortletAlternateRowClass(selectedClass, rowIndex) + "'"); // ICE-2571
             }
             domContext.setCursorParent(tBody);
             tBody.appendChild(tr);
             selectedClass = getPortletAlternateRowClass(selectedClass, rowIndex);
             tr.setAttribute(HTML.CLASS_ATTR, CSSNamePool.get(selectedClass));
 
             if(rowStylesMaxIndex >= 0){ // Thanks denis tsyplakov
                if (++rowStyleIndex > rowStylesMaxIndex) {
                     rowStyleIndex = 0;
                }
             }
             uiData.setColNumber(-1);
             String[] columnWidths = getColumnWidthsArray(uiData);
             while (childs.hasNext()) {
                 UIComponent nextChild = (UIComponent) childs.next();
                 if (nextChild.isRendered()) {
                     if (nextChild instanceof UIColumn) {
                
 
                     	uiData.setColNumber(uiData.getColNumber()+1);                        
                         Element td = domContext.createElement(HTML.TD_ELEM);
                         if (uiData.getColNumber() == 0) {
                         	td.setAttribute(HTML.SCOPE_ATTR, "row");
                         }                        
                         //add row focus handler for rowSelector
                         if (uiData.getColNumber() == 0 && rowSelectorFound
                                 && rowSelector.isKeyboardNavigationEnabled()) {
                             boolean singleSelection = false;
                             if(!rowSelector.isEnhancedMultiple() 
                                     && !rowSelector.getMultiple().booleanValue() 
                                     && rowSelector.isSingleRowAutoSelect()) {
                                 singleSelection = true;
                             }
                             anchor.setAttribute(HTML.ID_ATTR, clientId + "_idx_"+ countOfRowsDisplayed);                               
                             anchor.setAttribute(HTML.CLASS_ATTR, CSS_DEFAULT.FOCUS_HIDDEN_LINK_STYLE_CLASS);             
                             anchor.setAttribute(HTML.HREF_ATTR, "#"); 
                             anchor.appendChild(domContext.createTextNodeUnescaped("<img src='"+ CoreUtils.resolveResourceURL(facesContext,
                                         "/xmlhttp/css/xp/css-images/spacer.gif") + "'/>"));
                             anchor.setAttribute(HTML.ONFOCUS_ATTR, "return Ice.tblRowFocus(this, "+ singleSelection +");");
                             anchor.setAttribute(HTML.ONBLUR_ATTR, "return Ice.tblRowBlur(this);");                                   
                             td.appendChild(anchor);
                             Node oldCursorParent = domContext.getCursorParent();
                             domContext.setCursorParent(anchor);
                             PassThruAttributeWriter.renderHtmlAttributes(facesContext.getResponseWriter(), rowSelector, passThruAttributes);
                             domContext.setCursorParent(oldCursorParent);
                         }
                         String iceColumnStyle = null;
                         String iceColumnStyleClass = null;
                         //we want to perform this operation on ice:column only
                         if (nextChild instanceof com.icesoft.faces.component.ext.UIColumn) {
                             com.icesoft.faces.component.ext.UIColumn iceColumn =
                                 (com.icesoft.faces.component.ext.UIColumn)nextChild;
                             iceColumnStyle = iceColumn.getStyle();
                             iceColumnStyleClass = iceColumn.getStyleClass();
                             if (iceColumn.getGroupOn() != null) {
                                 if (iceColumn.groupFound()) {
                                     Element groupedTd = iceColumn.getGroupedTd();
                                     groupedTd.setAttribute(HTML.ROWSPAN_ATTR, String.valueOf(iceColumn.getGroupCount()));
                                     if (isResizable == null) { // Lazily get this once
                                         isResizable = Boolean.valueOf(uiData.isResizable());
                                     }
                                     if (isResizable.booleanValue() && childs.hasNext()) {
                                         Element eTd = domContext.createElement(HTML.TD_ELEM);
                                         eTd.setAttribute(HTML.CLASS_ATTR, "iceDatTblBlkTd");
                                         Element img = domContext.createElement(HTML.IMG_ELEM);
                                         img.setAttribute(HTML.SRC_ATTR, CoreUtils.resolveResourceURL(
                                                 FacesContext.getCurrentInstance(), "/xmlhttp/css/xp/css-images/spacer.gif") );
                                         eTd.appendChild(img);
                                         tr.appendChild(eTd);
                                     }
                                     continue;
                                 } else {
                                     iceColumn.setGroupedTd(td);
                                 }
                             }
                         }
 
 //                        if (uiData.isResizable()) {
 //                            td.setAttribute(HTML.COLSPAN_ATTR, "2");
 //                        }
                         if(!rowSelectorCodeAdded && scriptNode != null){
                             td.appendChild(scriptNode);
                         }
                         if (null != hiddenInputNode)  {
                             td.appendChild(hiddenInputNode);
                         }
                         if (null != hiddenClickedRowField && null != hiddenClickCountField)  {
                             td.appendChild(hiddenClickedRowField);
                             td.appendChild(hiddenClickCountField);
                         }
                         writeColStyles(columnStyles, columnStylesMaxIndex,
                                        columnStyleIndex, td, uiComponent);
 
                         String width = getWidthFromColumnWidthsArray(uiData, columnWidths);
                         if (width != null) {
                             td.setAttribute("style", width );
                         }
                         if (iceColumnStyle != null) {
                             String existingStyle = td.getAttribute(HTML.STYLE_ATTR);
                             if (existingStyle != null) {
                                 td.setAttribute(HTML.STYLE_ATTR, existingStyle + ";"+ iceColumnStyle);
                             } else {
                                 td.setAttribute(HTML.STYLE_ATTR, iceColumnStyle);
                             }
                         }
 
                         if (iceColumnStyleClass != null) {
                             String existingStyleClass = td.getAttribute(HTML.CLASS_ATTR);
                             if (existingStyleClass != null) {
                                 td.setAttribute(HTML.CLASS_ATTR, CSSNamePool.get(existingStyleClass + " "+ iceColumnStyleClass));
                             } else {
                                 td.setAttribute(HTML.CLASS_ATTR, CSSNamePool.get(iceColumnStyleClass));
                             }
                         }
 
 
                         tr.appendChild(td);
                         // if column styles exist, then apply the appropriate one
 
 
                         if (isResizable == null) { // Lazily get this once
                             isResizable = Boolean.valueOf(uiData.isResizable());
                         }
                         if (isResizable.booleanValue() && childs.hasNext()) {
                             Element eTd = domContext.createElement(HTML.TD_ELEM);
                             eTd.setAttribute(HTML.CLASS_ATTR, "iceDatTblBlkTd");
                             Element img = domContext.createElement(HTML.IMG_ELEM);
                             img.setAttribute(HTML.SRC_ATTR, CoreUtils.resolveResourceURL(
                                     FacesContext.getCurrentInstance(), "/xmlhttp/css/xp/css-images/spacer.gif") );
                             eTd.appendChild(img);
                             tr.appendChild(eTd);
                         }
 
                         if (++columnStyleIndex > columnStylesMaxIndex) {
                             columnStyleIndex = 0;
                         }
 
                         Node oldCursorParent = domContext.getCursorParent();
                         domContext.setCursorParent(td);
                         encodeParentAndChildren(facesContext, nextChild);
                         domContext.setCursorParent(oldCursorParent);
                     } else if (nextChild instanceof UIColumns) {
                         if (isResizable == null) { 
                             isResizable = Boolean.valueOf(uiData.isResizable());
                         }
                         nextChild.encodeBegin(facesContext);
                         encodeColumns(facesContext, nextChild, domContext, tr,
                                       columnStyles, columnStylesMaxIndex,
                                       columnStyleIndex,
                                       hiddenInputNode, hiddenClickedRowField, 
                                       hiddenClickCountField,
                                       columnWidths, isResizable.booleanValue());
                         nextChild.encodeEnd(facesContext);
                        
                     }
                 } else if (nextChild instanceof UIColumn) {
                     uiData.setColNumber(uiData.getColNumber() + 1);
                 }
 
             }
             rowIndex++;
             countOfRowsDisplayed++;
             if ((numberOfRowsToDisplay > 0 &&
                     countOfRowsDisplayed >= numberOfRowsToDisplay) ||
                     (rowCount >=0 && rowIndex >= rowCount)) {
                     break;
             }
 
             uiData.setRowIndex(rowIndex);
         }
         uiData.setRowIndex(-1);
         domContext.stepOver();
     }
 
     private void encodeColumns(FacesContext facesContext, UIComponent columns,
                                DOMContext domContext, Node tr,
                                String[] columnStyles, int columnStylesMaxIndex,
                                int columnStyleIndex, 
                                Element rowSelectorHiddenField,
                                Element clickEventRowField, Element clickEventCountField,
                                String[] columnWidths,
                                boolean isResizable) throws IOException {
         UIColumns uiList = (UIColumns) columns;
         HtmlDataTable table = ((HtmlDataTable) uiList.getParent());
         int rowIndex = uiList.getFirst();
         uiList.setRowIndex(rowIndex);
         int numberOfRowsToDisplay = uiList.getRows();
         int countOfRowsDisplayed = 0;
         domContext.setCursorParent(tr);
         Node oldCursorParent = domContext.getCursorParent();
         while (uiList.isRowAvailable()) {
             if ((numberOfRowsToDisplay > 0) &&
                 (countOfRowsDisplayed >= numberOfRowsToDisplay)) {
                 break;
             }
            
             table.setColNumber(table.getColNumber()+1); 
             Element td = domContext.createElement(HTML.TD_ELEM);
             if (table.getColNumber() == 0) {
             	td.setAttribute(HTML.SCOPE_ATTR, "row");
             } 
             Iterator childs;
             childs = columns.getChildren().iterator();
 
             if (null != rowSelectorHiddenField)  {
                 td.appendChild(rowSelectorHiddenField);
             }
             if (null != clickEventRowField && null != clickEventCountField)  {
                 td.appendChild(clickEventRowField);
                 td.appendChild(clickEventCountField);
             }
             String width = getWidthFromColumnWidthsArray(table, columnWidths);
             if (width != null) {
                 td.setAttribute("style", width );
             }
             domContext.setCursorParent(oldCursorParent);
             tr.appendChild(td);
             while (childs.hasNext()) {
                 UIComponent nextChild = (UIComponent) childs.next();
                 if (nextChild.isRendered()) {
                     domContext.setCursorParent(td);
                     writeColStyles(columnStyles, columnStylesMaxIndex,
                                    columnStyleIndex, td,
                                    columns.getParent());
                     if (++columnStyleIndex > columnStylesMaxIndex) {
                         columnStyleIndex = 0;
                     }
                     encodeParentAndChildren(facesContext, nextChild);
                     domContext.setCursorParent(oldCursorParent);
                 }
             }
             rowIndex++;
             countOfRowsDisplayed++;
             uiList.setRowIndex(rowIndex);
             
 
             if (isResizable && uiList.getRowCount() > rowIndex) {
                 Element eTd = domContext.createElement(HTML.TD_ELEM);
                 eTd.setAttribute(HTML.CLASS_ATTR, "iceDatTblBlkTd");
                 Element img = domContext.createElement(HTML.IMG_ELEM);
                 img.setAttribute(HTML.SRC_ATTR, CoreUtils.resolveResourceURL(
                         FacesContext.getCurrentInstance(), "/xmlhttp/css/xp/css-images/spacer.gif") );
                 eTd.appendChild(img);
                 tr.appendChild(eTd);
             }            
             
         }
 
         uiList.setRowIndex(-1);
     }
 
     protected List getRenderedChildColumnsList(UIComponent component) {
         List results = new ArrayList();
         Iterator kids = component.getChildren().iterator();
         while (kids.hasNext()) {
             UIComponent kid = (UIComponent) kids.next();
             if (((kid instanceof UIColumn) && kid.isRendered()) ||
                 kid instanceof UIColumns) {
                 results.add(kid);
             }
         }
         return results;
     }
 
     protected boolean childColumnHasFacetWithName(UIComponent component,
                                                   String facetName) {
         Iterator childColumns = getRenderedChildColumnsIterator(component);
         while (childColumns.hasNext()) {
             UIComponent nextChildColumn = (UIComponent) childColumns.next();
             if (getFacetByName(nextChildColumn, facetName) != null) {
                 return true;
             }
         }
         return false;
     }
 
     public static String getSelectedRowParameterName(FacesContext facesContext, HtmlDataTable dataTable) {
         String dataTableId = dataTable.getClientId(facesContext);
         return dataTableId + SELECTED_ROWS;
     }
     
     public static String getClickedRowParameterName(FacesContext facesContext, HtmlDataTable dataTable) {
         String dataTableId = dataTable.getClientId(facesContext);
         return dataTableId + CLICKED_ROW;
     }
     
     public static String getClickCountParameterName(FacesContext facesContext, HtmlDataTable dataTable) {
         String dataTableId = dataTable.getClientId(facesContext);
         return dataTableId + CLICK_COUNT;
     }    
 
     public static RowSelector getRowSelector(UIComponent comp) {
         if (comp instanceof RowSelector) {
             return (RowSelector) comp;
         }
         Iterator iter = comp.getChildren().iterator();
         while (iter.hasNext()) {
             UIComponent kid = (UIComponent) iter.next();
             if (kid instanceof HtmlDataTable){
                 // Nested HtmlDataTable might be a peer of
                 //  a later, valid RowSelector, so don't
                 //  traverse in, but keep looking
                 continue;
             }
             RowSelector rs = getRowSelector(kid);
             if (rs != null) {
                 if(!rs.isRendered())
                     return null;
                 return rs;
             }
         }
         return null;
     }
 
     private int rowSelectorNumber(FacesContext context){
         Map m = context.getExternalContext().getRequestMap();
         String key = RowSelector.class.getName() + "-Selector";
         Integer I = (Integer)m.get(key);
         int i = 0;
         if(I != null){
             i = I.intValue();
             i++;
         }
 
         I = new Integer(i);
         m.put(key, I);
         return i;
     }
 
     protected int getNumberOfChildColumns(UIComponent component) {
         int size = getRenderedChildColumnsList(component).size();
         Iterator it = getRenderedChildColumnsList(component).iterator();
         while (it.hasNext()) {
         	UIComponent uiComponent = (UIComponent)it.next();
         	if (uiComponent instanceof UIColumns) {
         		size +=((UIColumns)uiComponent).getRowCount();
         	}
         }
         return size;
     }
 
     protected String[] getColumnStyleClasses(UIComponent uiComponent) {
         String[] columnStyles = super.getColumnStyleClasses(uiComponent);
         if (columnStyles.length == 0) {
             columnStyles = new String[2];
             columnStyles[0] = Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_COLUMN_CLASS1);
             columnStyles[1] = Util.getQualifiedStyleClass(uiComponent,
                     CSS_DEFAULT.TABLE_COLUMN_CLASS2);
         } else {
             for (int i=0; i < columnStyles.length; i++) {
                 columnStyles[i] = Util.getQualifiedStyleClass(uiComponent,
                               columnStyles[i],
                               CSS_DEFAULT.TABLE_COLUMN_CLASS,
                               "columnClasses"
                                            );
             }
         }
         return columnStyles;
     }
 
     public String[] getRowStyleClasses(UIComponent uiComponent) {
         String[] rowClasses = super.getRowStyleClasses(uiComponent);
         for (int i=0; i < rowClasses.length; i++) {
             rowClasses[i] = Util.getQualifiedStyleClass(uiComponent,
                             rowClasses[i],
                           CSS_DEFAULT.TABLE_ROW_CLASS,
                           "rowClasses"
                                        );
         }
         return rowClasses;
     }
 
     public String[] getHeaderStyleClasses(UIComponent uiComponent) {
         String headerClass = getHeaderClass(uiComponent).
         replaceAll(CSS_DEFAULT.TABLE_STYLE_CLASS + CSS_DEFAULT.TABLE_HEADER_CLASS, "");
        String[] headerClasses = getStyleClasses(uiComponent, "headerClasses");
        for (int i=0; i < headerClasses.length; i++) {
            headerClasses[i] = CSSNamePool.get(Util.getQualifiedStyleClass(uiComponent,
                    headerClasses[i],
                          CSS_DEFAULT.TABLE_COLUMN_HEADER_CLASS,
                          "headerClasses"
                                       )+ ((headerClass.length() > 0)
                                               ? headerClass : ""));
        }
        return headerClasses;
     }
 
     String getPortletAlternateRowClass(String selectedClass, int rowIndex) {
         String rowClass = PORTLET_CSS_DEFAULT.PORTLET_SECTION_ALTERNATE;
         if ((rowIndex % 2) == 0) {
             rowClass = PORTLET_CSS_DEFAULT.PORTLET_SECTION_BODY;
         }
         if (selectedClass.indexOf(' ') > 1) {
             return selectedClass.replaceFirst(" ", " " + CoreUtils.getPortletStyleClass(rowClass + " "));
         } else {
             return selectedClass += CoreUtils.getPortletStyleClass("" + rowClass);
         }
     }
 
     void resetGroupState(UIComponent uiComponent) {
         Iterator childs = uiComponent.getChildren().iterator();
         while(childs.hasNext()) {
             UIComponent child = (UIComponent) childs.next();
             if (child instanceof com.icesoft.faces.component.ext.UIColumn) {
                 ((com.icesoft.faces.component.ext.UIColumn)child).resetGroupState();
             }
         }
     }
     
     protected String getWidthFromColumnWidthsArray(HtmlDataTable htmlDataTable, 
                                     String[] columnWidths) {
         StringBuffer width = new StringBuffer();
         //guaranteed no null
         if (columnWidths.length > 0 && htmlDataTable.getColNumber() < columnWidths.length) {
             try {
                 width.append("width:");                 
                 width.append(columnWidths[htmlDataTable.getColNumber()]);
                 width.append(";");                
             } catch (IndexOutOfBoundsException e) {
                 //width not defined for this index htmlDataTable.getColNumber()
 //                e.printStackTrace(); // for info. only
             } catch (NumberFormatException nfe) {
                 //self describing 
 //                nfe.printStackTrace(); // for info. only
             }
         } else {
             if (isScrollable(htmlDataTable)) {
                 width.append("width:100%;");
             }
         }
         
         if (isScrollable(htmlDataTable)) {
             width.append("overflow:hidden;");
         } 
         return width.length()>0? width.toString() : null;
     }
     
     protected String[] getColumnWidthsArray(HtmlDataTable htmlDataTable) {
         String columnWidths = htmlDataTable.getColumnWidths();
         return columnWidths!= null ? columnWidths.split(",") : new String[]{};
     }
 }
