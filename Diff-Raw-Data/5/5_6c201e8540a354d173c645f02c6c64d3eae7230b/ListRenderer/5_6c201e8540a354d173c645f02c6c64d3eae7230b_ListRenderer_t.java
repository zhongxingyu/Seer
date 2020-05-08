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
 
 package org.icefaces.ace.component.list;
 
 import org.icefaces.ace.json.JSONException;
 import org.icefaces.ace.renderkit.CoreRenderer;
 import org.icefaces.ace.util.HTML;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.render.MandatoryResourceComponent;
 
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UINamingContainer;
 import javax.faces.component.UISelectItem;
 import javax.faces.component.UISelectItems;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 @MandatoryResourceComponent(tagName="list", value="org.icefaces.ace.component.list.ACEList")
 public class ListRenderer extends CoreRenderer {
     public static final String containerStyleClass = "if-list ui-widget ui-widget-content ui-corner-all";
     public static final String controlsContainerStyleClass = "if-list-ctrls";
     public static final String pointerStyleClass = "if-pntr";
     public static final String bodyStyleClass = "if-list-body";
     public static final String miniClass = "if-mini";
     public static final String itemStyleClass = "if-list-item ui-state-default";
     public static final String selectedItemStyleClass = "ui-state-active";
     public static final String disabledItemStyleClass = "disabled";
     public static final String controlsItemStyleClass = "if-list-ctrl";
     public static final String controlsItemSpacerClass = "if-list-ctrl-spcr";
     public static final String headerStyleClass = "if-list-head ui-widget-header";
     public static final String footerStyleClass = "if-list-foot ui-widget-content";
     public static final String placeholderStyleClass = "if-list-plhld if-list-item ui-state-default";
 
     @Override
     public void decode(FacesContext context, UIComponent component) {
         ACEList list = (ACEList)component;
         String id = list.getClientId(context);
         String select = id + "_selections";
         String deselect = id + "_deselections";
         String reordering = id + "_reorderings";
         String immigration = id + "_immigration";
         String emigration = id + "_emigration";
         Map<String, String> params = context.getExternalContext().getRequestParameterMap();
 
         String selectInput = params.get(select);
         String deselectInput = params.get(deselect);
         String reorderingInput = params.get(reordering);
         String immigrationInput = params.get(immigration);
         String emigrationInput = params.get(emigration);
 
         ListDecoder decoder = new ListDecoder(list);
 
         try {
             decoder.processSelections(selectInput)
                     .processDeselections(deselectInput)
                      // If source, find outgoing objects and pass them
                      // to their destination list
                      .attachEmigrants(context, emigrationInput)
                       .processReorderings(reorderingInput)
                        /// If destination, fetch incoming objects if not
                        /// already passed by decoding source list
                        .fetchImmigrants(context, immigrationInput)
                         //// If destination, insert immigrant objects from
                         //// records attached to List
                         .insertImmigrants()
                          ///// If source, check destination list for attached
                          ///// immigrants (if we did not earlier put them there)
                          ///// and remove them from our attached list
                          .removeEmigrants(context, emigrationInput);
         } catch (JSONException e) {
             throw new FacesException(e);
         }
 
         decodeBehaviors(context, component);
     }
 
     @Override
     public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ACEList list = (ACEList) component;
         String clientId = component.getClientId(context);
         String styleClass = list.getStyleClass();
         String style = list.getStyle();
         Boolean mini = list.isCompact();
 
         list.getDataModel(); // DataModel init determines if some features are available
 
         styleClass = styleClass == null ? containerStyleClass : styleClass + " " + containerStyleClass;
         if (mini) styleClass += " " + miniClass;
 
         writer.startElement(HTML.DIV_ELEM, component);
         writer.writeAttribute(HTML.ID_ATTR, clientId, "clientId");
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "styleClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "style");
 
         if (list.getFacet("header") != null)
             encodeHeader(context, writer, list);
 
         if (list.isControlsEnabled())
             encodeControls(context, writer, list);
     }
 
     private void encodeControls(FacesContext context, ResponseWriter writer, ACEList component) throws IOException {
         String styleClass = component.getControlsContainerClass();
         styleClass = styleClass == null ? controlsContainerStyleClass : styleClass + " " + controlsContainerStyleClass;
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "controlsContainerClass");
 
         for (String buttonCode : component.getControlsFormat().split(" "))
             encodeControl(context, writer, component, buttonCode);
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeControl(FacesContext context, ResponseWriter writer, ACEList component, String buttonCode) throws IOException {
         String itemStyleClass = "if-list-ctrl-" + buttonCode + " " + component.getControlsItemClass();
         String iconStyleClass;
         String property;
         UIComponent facet;
 
         itemStyleClass = itemStyleClass != null
                 ? itemStyleClass + " " + controlsItemStyleClass
                 : controlsItemStyleClass;
 
         if (buttonCode.equals("top")) {
             iconStyleClass = component.getTopButtonClass();
             property = "topButtonClass";
             facet = component.getFacet("topButton");
         } else if (buttonCode.equals("up")) {
             iconStyleClass = component.getUpButtonClass();
             property = "upButtonClass";
             facet = component.getFacet("upButton");
         } else if (buttonCode.equals("dwn")) {
             iconStyleClass = component.getDownButtonClass();
             property = "downButtonClass";
             facet = component.getFacet("downButton");
         } else if (buttonCode.equals("btm")) {
             iconStyleClass = component.getBottomButtonClass();
             property = "bottomButtonClass";
             facet = component.getFacet("bottomButton");
         } else return;
 
         if (facet != null) {
             facet.encodeAll(context);
         } else {
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, controlsItemSpacerClass, null);
 
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, itemStyleClass, "controlsItemClass");
             writer.startElement(HTML.SPAN_ELEM, null);
             writer.writeAttribute(HTML.CLASS_ATTR, iconStyleClass, property);
             writer.endElement(HTML.SPAN_ELEM);
             writer.endElement(HTML.SPAN_ELEM);
 
             writer.endElement(HTML.SPAN_ELEM);
         }
     }
 
     private void encodeHeader(FacesContext context, ResponseWriter writer, ACEList component) throws IOException {
         String styleClass = component.getHeaderClass();
         styleClass = styleClass == null ? headerStyleClass : styleClass + " " + headerStyleClass;
         String style = component.getHeaderStyle();
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "headerClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "headerStyle");
 
         UIComponent facet = component.getFacet("header");
         if (facet != null)
             facet.encodeAll(context);
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     @Override
     public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ACEList list = (ACEList)component;

        //reset data model
        list.setDataModel(null);
        list.getDataModel();

         String dropGroup = list.getDropGroup();
         String style = list.getBodyStyle();
         String bodyHeight = list.getHeight();
         String styleClass = list.getBodyClass();
 
         styleClass = styleClass == null
                 ? bodyStyleClass
                 : styleClass + " " + bodyStyleClass;
 
         if (dropGroup != null) styleClass += " dg-" + dropGroup;
 
         writer.startElement(HTML.UL_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "bodyClass");
 
         if (bodyHeight != null) {
             bodyHeight = " height:"+bodyHeight+";";
             style = style == null ? bodyHeight : style + "; " + bodyHeight;
         }
 
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "bodyStyle");
 
         encodeChildren(context, writer, list);
 
         writer.endElement(HTML.UL_ELEM);
     }
 
     private void encodeChildren(FacesContext context, ResponseWriter writer, ACEList list) throws IOException {
         final Collection<Object> selections = list.isSelectItemModel() ? (Collection)list.getValue() : list.getSelections();
         String style = list.getItemStyle();
         String styleClass = list.getItemClass();
         String selectionMode = list.getSelectionMode();
         boolean pointerTable = ("single".equals(selectionMode) || "multiple".equals(selectionMode)) || list.isDragging();
 
         styleClass = styleClass == null ? itemStyleClass : styleClass + " " + itemStyleClass;
 
         list.setRowIndex(0);
 
         boolean selectItems = false;
         if (list.isRowAvailable())
             selectItems = list.getRowData() instanceof SelectItem;
 
         while (list.isRowAvailable()) {
             String itemStyleClass = new String(styleClass);
             SelectItem item = selectItems ? (SelectItem)list.getRowData() : null;
             Object val = selectItems && list.isSelectItemModel() ? item.getValue() : list.getRowData();
 
             boolean selected = selections == null ? false : selections.contains(val);
             boolean disabled = selectItems ? item.isDisabled() : false;
 
             if (selected) itemStyleClass = itemStyleClass + " " + selectedItemStyleClass;
             if (disabled) itemStyleClass = itemStyleClass + " " + disabledItemStyleClass;
             else if (pointerTable) itemStyleClass = itemStyleClass + " " + pointerStyleClass;
 
             if (selectItems)
                 encodeStringChild(context, writer, list, item, itemStyleClass, style);
             else
                 encodeCompositeChild(context, writer, list, itemStyleClass, style);
 
             list.setRowIndex(list.getRowIndex()+1);
         }
 
         list.setRowIndex(-1);
     }
 
     private void encodeStringChild(FacesContext context, ResponseWriter writer, ACEList list, SelectItem item, String styleClass, String style) throws IOException {
         writer.startElement(HTML.LI_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, list.getContainerClientId(context), null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "itemClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "itemStyle");
 
         writer.write(item.getLabel());
 
         writer.endElement(HTML.LI_ELEM);
     }
 
     private void encodeCompositeChild(FacesContext context, ResponseWriter writer, ACEList list, String styleClass, String style) throws IOException {
         writer.startElement(HTML.LI_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, list.getContainerClientId(context), null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "itemClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "itemStyle");
 
         // List has implicit UIColumn child to wrap composite children
         for (UIComponent component : list.getChildren()) {
             if (!(component instanceof UISelectItem ||
                     component instanceof UISelectItems))
                 component.encodeAll(context);
         }
         writer.endElement(HTML.LI_ELEM);
     }
 
     @Override
     public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
         ResponseWriter writer = context.getResponseWriter();
         ACEList list = (ACEList) component;
 
         if (component.getFacet("header") != null)
             encodeFooter(context, writer, list);
 
         encodeHiddenFields(context, writer, list);
         encodeScript(context, writer, list);
 
         writer.endElement(HTML.DIV_ELEM);
     }
 
     private void encodeHiddenFields(FacesContext context, ResponseWriter writer, ACEList list) throws IOException {
         // Write fields intended for delayed communications of changes to list
         String id = list.getClientId(context);
 
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, id + "_selections", null);
         writer.writeAttribute(HTML.NAME_ATTR, id + "_selections", null);
         writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
         writer.endElement(HTML.INPUT_ELEM);
 
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, id + "_deselections", null);
         writer.writeAttribute(HTML.NAME_ATTR, id + "_deselections", null);
         writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
         writer.endElement(HTML.INPUT_ELEM);
 
         writer.startElement(HTML.INPUT_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, id + "_reorderings", null);
         writer.writeAttribute(HTML.NAME_ATTR, id + "_reorderings", null);
         writer.writeAttribute(HTML.TYPE_ATTR, "hidden", null);
         writer.endElement(HTML.INPUT_ELEM);
     }
 
     private void encodeScript(FacesContext context, ResponseWriter writer, ACEList component) throws IOException {
         String widgetVar = resolveWidgetVar(component);
         String clientId = component.getClientId(context);
 
         writer.startElement(HTML.SPAN_ELEM, null);
         writer.writeAttribute(HTML.ID_ATTR, clientId+"_script", null);
         writer.startElement(HTML.SCRIPT_ELEM, null);
         writer.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
 
         String styleClass = component.getPlaceholderClass();
         styleClass = styleClass == null ? placeholderStyleClass : styleClass + " " + placeholderStyleClass;
         String selectionMode = component.getSelectionMode();
         String dropGroup = component.getDropGroup();
         boolean selectItemModel = component.isSelectItemModel();
 
         JSONBuilder cfgBuilder = JSONBuilder.create().initialiseVar(widgetVar)
                 .beginFunction("ice.ace.create").item("List").beginArray()
                 .item(clientId);
 
         cfgBuilder.beginMap();
         cfgBuilder.entry("separator", UINamingContainer.getSeparatorChar(context));
 
         if (component.isPlaceholder())
             cfgBuilder.entry("placeholder", styleClass);
 
         if ("single".equals(selectionMode) || "multiple".equals(selectionMode))
             cfgBuilder.entry("selection", selectionMode);
 
         // Select item model doesn't allow reordering or migration
         if (!selectItemModel) {
             if (dropGroup != null)
                 cfgBuilder.entry("connectWith", ".dg-"+dropGroup);
 
             if (component.isDragging())
                 cfgBuilder.entry("dragging", true);
 
             if (component.isControlsEnabled())
                 cfgBuilder.entry("controls", true);
 
             if (component.isDoubleClickMigration())
                 cfgBuilder.entry("dblclk_migrate", true);
         }
 
         encodeClientBehaviors(context, component, cfgBuilder);
 
         cfgBuilder.endMap().endArray().endFunction();
 
         writer.write(cfgBuilder.toString());
         writer.endElement(HTML.SCRIPT_ELEM);
         writer.endElement(HTML.SPAN_ELEM);
     }
 
     private void encodeFooter(FacesContext context, ResponseWriter writer, ACEList component) throws IOException {
         String styleClass = component.getFooterClass();
         styleClass = styleClass == null ? footerStyleClass : styleClass + " " + footerStyleClass;
         String style = component.getFooterStyle();
 
         writer.startElement(HTML.DIV_ELEM, null);
         writer.writeAttribute(HTML.CLASS_ATTR, styleClass, "footerClass");
         if (style != null)
             writer.writeAttribute(HTML.STYLE_ATTR, style, "footerStyle");
 
         UIComponent facet = component.getFacet("footer");
         if (facet != null)
             facet.encodeAll(context);
 
         writer.endElement(HTML.DIV_ELEM);
     }
 }
