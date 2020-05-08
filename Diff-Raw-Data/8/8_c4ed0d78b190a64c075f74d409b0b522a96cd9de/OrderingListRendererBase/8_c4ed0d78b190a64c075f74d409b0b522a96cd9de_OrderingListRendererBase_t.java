 /**
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 package org.richfaces.renderkit;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 
 import org.ajax4jsf.component.UIDataAdaptor;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UIOrderingBaseComponent;
 import org.richfaces.component.UIOrderingList;
 import org.richfaces.component.UIOrderingBaseComponent.ItemState;
 import org.richfaces.component.util.HtmlUtil;
 
 public abstract class OrderingListRendererBase extends OrderingComponentRendererBase {
 
 	private static final String SELECTION_STATE_VAR_NAME = "selectionState";
 
 	public OrderingListRendererBase() {
 		super(MESSAGE_BUNDLE_NAME);
 	}
 
 	private static final String MESSAGE_BUNDLE_NAME = OrderingListRendererBase.class.getPackage().getName() + ".orderingList";
 
 	private final static Character ACTIVITY_MARKER = new Character('a');
 	
 	private final static Character SELECTION_MARKER = new Character('s');
 	
 	private final static String ITEM_SEPARATOR = ",";
 	
 	protected static final OrderingComponentRendererBase.ControlsHelper[] HELPERS = OrderingComponentControlsHelper.HELPERS;
 	
 	protected Class getComponentClass() {
 		return UIOrderingList.class;
 	}
 	
 	public boolean getRendersChildren() {
 		return true;
 	}
 	
 	public void encodeBegin(FacesContext context, UIComponent component)
 			throws IOException {
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, component);
 		variables.setVariable(SELECTION_STATE_VAR_NAME, new SelectionState());
 
 		super.encodeBegin(context, component);
 	}
 
 	public boolean isHeaderExists(FacesContext context, UIOrderingBaseComponent component) {
 		return isHeaderExists(context, component, "header");
 	}
 	
 	public void encodeControlsFacets(FacesContext context, UIOrderingList orderingList) 
 			throws IOException {
 		String clientId = orderingList.getClientId(context);
 		ResponseWriter writer = context.getResponseWriter();
 
 		//proper assumption about helpers ordering
 		int divider = HELPERS.length / 2;
 		
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, orderingList);
 		SelectionState selectionState = (SelectionState) variables.getVariable(SELECTION_STATE_VAR_NAME);
 
 		for (int i = 0; i < HELPERS.length; i++) {
 			boolean boundarySelection = i < divider ? selectionState.isFirstSelected() : selectionState.isLastSelected();
 			boolean enabled = selectionState.isSelected() && !boundarySelection;
 			if (i % 2 == 1) {
 				enabled = !enabled;
 			}
 			
 			if (HELPERS[i].isRendered(context, orderingList)) {
 				//proper assumption about helpers ordering
 				encodeControlFacet(context, orderingList, HELPERS[i], clientId, writer, 
 						enabled, "rich-ordering-list-button", " rich-ordering-control");
 			}
 		}
 	}
 	
 	private static final class OrderingListRendererTableHolder extends TableHolder {
 
 		private Converter converter;
 
 		public OrderingListRendererTableHolder(UIDataAdaptor table, Converter converter) {
 			super(table);
 
 			this.converter = converter;
 		}
 		
 		public Converter getConverter() {
 			return converter;
 		}
 	}
 	
 	public void encodeRows(FacesContext context, UIComponent component)
 			throws IOException {
 		
 		UIOrderingBaseComponent orderingBaseComponent = (UIOrderingBaseComponent) component;
 		
 		super.encodeRows(context, component, new OrderingListRendererTableHolder(orderingBaseComponent, 
 				getConverter(context, orderingBaseComponent, true)));
 	}
 	
 	public void encodeOneRow(FacesContext context, TableHolder holder)
 			throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		OrderingListRendererTableHolder tableHolder = (OrderingListRendererTableHolder) holder;
 		UIOrderingList table = (UIOrderingList) holder.getTable();
 		String clientId = holder.getTable().getClientId(context);
 		writer.startElement(HTML.TR_ELEMENT, table);
 		writer.writeAttribute("id",  clientId, null);
 		
 		StringBuffer rowClassName = new StringBuffer("rich-ordering-list-row");
 		
 		String rowClass = holder.getRowClass();
 		if (rowClass != null) {
 			rowClassName.append(' ');
 			rowClassName.append(rowClass);
 		}
 		
 		StringBuffer cellClassName = new StringBuffer("rich-ordering-list-cell");
 		
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(this, table);
 		ItemState state = getItemState(context, table, variables);
 		
 		boolean active = state.isActive();
 //		if (active) {
 //			rowClassName.append(" rich-ordering-list-row-active");
 //			cellClassName.append(" rich-ordering-list-cell-active");
 //		}
 		
 		boolean selected = state.isSelected();
 		if (selected) {
 			rowClassName.append(" rich-ordering-list-row-selected");
 			cellClassName.append(" rich-ordering-list-cell-selected");
 		}
 
 		SelectionState selectionState = (SelectionState) variables.getVariable(SELECTION_STATE_VAR_NAME);
 		selectionState.addState(selected);
 		
 		writer.writeAttribute("class", rowClassName.toString(), null);
 		
 		boolean columnRendered = false;
 		
 		int colCounter = 0;
 
 		for (Iterator iterator = table.columns(); iterator.hasNext();) {
 			UIComponent component = (UIComponent) iterator.next();
 			
 			if (component.isRendered()) {
 				writer.startElement(HTML.td_ELEM, table);
 				
 				//if (!iterator.hasNext()) {
 				//	cellClassName.append(" rich-ordering-list-cell-end");
 				//}
 				
 				Object width = component.getAttributes().get("width");
 				if (width != null) {
 					writer.writeAttribute("style", "width: " + HtmlUtil.qualifySize(width.toString()), null);
 				}
 				
 				String columnClass = holder.getColumnClass(colCounter);
 				if (columnClass != null) {
 					writer.writeAttribute("class", cellClassName.toString().concat(" " + columnClass), null);
 				} else {
 					writer.writeAttribute("class", cellClassName.toString(), null);
 				}
 				
 				//writer.write("&nbsp;");
 				
 				writer.startElement(HTML.IMG_ELEMENT, table);
 				writer.writeAttribute(HTML.src_ATTRIBUTE, getResource("/org/richfaces/renderkit/html/images/spacer.gif").getUri(context, null), null);
 				writer.writeAttribute(HTML.style_ATTRIBUTE, "width:1px;height:1px;", null);
 				writer.writeAttribute(HTML.alt_ATTRIBUTE, " ", null);
 				writer.endElement(HTML.IMG_ELEMENT);
 				
 				renderChildren(context, component);
 
 				if (!columnRendered) {
 					writer.startElement(HTML.INPUT_ELEM, table);
 					writer.writeAttribute(HTML.id_ATTRIBUTE, clientId + "StateInput", null);
					writer.writeAttribute(HTML.autocomplete_ATTRIBUTE, "off", null);
 					writer.writeAttribute(HTML.TYPE_ATTR, HTML.INPUT_TYPE_HIDDEN, null);
 					writer.writeAttribute(HTML.NAME_ATTRIBUTE, table.getBaseClientId(context), null);
 					
 					StringBuffer value = new StringBuffer();
 					if (selected) {
 						value.append('s');
 					}
 					
 					if (active) {
 						value.append('a');
 					}
 
 					value.append(table.getRowKey());
 					value.append(':');
 					value.append(tableHolder.getConverter().getAsString(context, table, table.getRowData()));
 					
 					writer.writeAttribute(HTML.value_ATTRIBUTE, value.toString(), null);
 					
 					writer.endElement(HTML.INPUT_ELEM);
 					
 					columnRendered = true;
 				}
 				
 				writer.endElement(HTML.td_ELEM);
 			}
 			
 			colCounter++;
 		}
 		
 		writer.endElement(HTML.TR_ELEMENT);
 	}
 	
 	public void doDecode(FacesContext context, UIComponent component) {
 		UIOrderingList orderingList = (UIOrderingList) component;
 		
 		String clientId = orderingList.getBaseClientId(context);
         ExternalContext externalContext = context.getExternalContext();
 		Map<String, String[]> requestParameterValuesMap = externalContext
         								 .getRequestParameterValuesMap();
         
 		String[] strings = (String[]) requestParameterValuesMap.get(clientId);
         
 		if (strings != null && strings.length != 0) {
 			Set selection = new HashSet();
 			Object activeItem = null;
         	Map map = new LinkedHashMap();
         	Converter converter = getConverter(context, orderingList, false);
         	for (int i = 0; i < strings.length; i++) {
 				String string = strings[i];
 				int idx = string.indexOf(':');
 				Object value = converter.getAsObject(context, orderingList, string.substring(idx + 1));
 				String substring = string.substring(0, idx);
 				
 				idx = 0;
 				
 				if (substring.charAt(idx) == 's') {
 					selection.add(value);
 					idx++;
 				}
 				
 				if (substring.charAt(idx) == 'a') {
 					activeItem = value;
 					idx++;
 				}
 
 				substring = substring.substring(idx);
 				
 				Object key = new Integer(substring);
 				map.put(key, value);
         	}
         	orderingList.setSubmittedString(map, selection, activeItem);
         }
 	}
 }
