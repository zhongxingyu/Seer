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
 
 package org.icefaces.ace.component.combobox;
 
 import org.icefaces.ace.renderkit.InputRenderer;
 import org.icefaces.render.MandatoryResourceComponent;
 import org.icefaces.ace.util.JSONBuilder;
 import org.icefaces.util.EnvUtils;
 import org.icefaces.ace.event.TextChangeEvent;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 import javax.faces.convert.Converter;
 import javax.faces.convert.ConverterException;
 import javax.el.ELContext;
 import javax.el.ValueExpression;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.io.IOException;
 
 @MandatoryResourceComponent(tagName="comboBox", value="org.icefaces.ace.component.combobox.ComboBox")
 public class ComboBoxRenderer extends InputRenderer {
 
 	private static final String AUTOCOMPLETE_DIV = "_div";
 	private static final String LABEL_CLASS = "ui-combobox-item-label";
 	private static final String VALUE_CLASS = "ui-combobox-item-value";
 
 	public boolean getRendersChildren() {
 		return true;
 	}
 
 	public void decode(FacesContext facesContext, UIComponent uiComponent) {
 		ComboBox comboBox = (ComboBox) uiComponent;
 		comboBox.setItemList(null);
 		Map requestMap = facesContext.getExternalContext().getRequestParameterMap();
 		String clientId = comboBox.getClientId(facesContext);
 		String value = (String) requestMap.get(clientId + "_input");
 		
 		comboBox.setSubmittedValue(value);
 		
 		decodeBehaviors(facesContext, comboBox);
 	}
 
 	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		String clientId = uiComponent.getClientId(facesContext);
 		ComboBox comboBox = (ComboBox) uiComponent;
 		int width = comboBox.getWidth();
 		boolean ariaEnabled = EnvUtils.isAriaEnabled(facesContext);
 		Map paramMap = facesContext.getExternalContext().getRequestParameterMap();
 		Map<String, Object> labelAttributes = getLabelAttributes(uiComponent);
 		String inFieldLabel = (String) labelAttributes.get("inFieldLabel");
 		String inFieldLabelStyleClass = "";
 		String iceFocus = (String) paramMap.get("ice.focus");
 		String mousedownScript = (String) uiComponent.getAttributes().get("onmousedown");
 		String onfocusCombinedValue = "setFocus(this.id);";
 		String onblurCombinedValue = "";
 		Object onfocusAppValue = uiComponent.getAttributes().get("onfocus");
 		Object onblurAppValue = uiComponent.getAttributes().get("onblur");
 		Object onchangeAppValue = uiComponent.getAttributes().get("onchange");
 
 		String inputClientId = clientId + "_input";
 
 		String value = (String) comboBox.getValue();		
 		if (isValueBlank(value)) value = null;
 		boolean labelIsInField = false;
 
 		if (value == null && !isValueBlank(inFieldLabel) && !inputClientId.equals(iceFocus)) {
 			inFieldLabelStyleClass = " " + IN_FIELD_LABEL_STYLE_CLASS;
 			labelIsInField = true;
 		}
 
 		// root
 		writer.startElement("div", null);
 		writer.writeAttribute("id", clientId, null);
 		writer.writeAttribute("class", "ui-combobox " + comboBox.getStyleClass(), null);
 
 		writeLabelAndIndicatorBefore(labelAttributes);
 		
 		// value field
 		writer.startElement("span", null);
 		writer.writeAttribute("class", "ui-widget ui-corner-all ui-state-default ui-combobox-value", null);
 		writer.writeAttribute("style", "display: inline-block; width: " + width + "px;", null);
 		
 		// text input
 		writer.startElement("input", null);
 		writer.writeAttribute("type", "text", null);
 		writer.writeAttribute("name", inputClientId, null);
 		writer.writeAttribute("style", comboBox.getStyle() + "; display: inline-block; overflow: hidden;border:0;", null);
 		writer.writeAttribute("class", "ui-inputfield ui-state-default ui-corner-left " + getStateStyleClasses(comboBox) + inFieldLabelStyleClass, null);
 		if (ariaEnabled) {
 			writer.writeAttribute("role", "textbox", null);
 			final ComboBox component = (ComboBox) uiComponent;
 			Map<String, Object> ariaAttributes = new HashMap<String, Object>() {{
 				put("required", component.isRequired());
 				put("disabled", component.isDisabled());
 				put("invalid", !component.isValid());
 			}};
 			writeAriaAttributes(ariaAttributes, labelAttributes);
 		}
 		String accesskey = comboBox.getAccesskey();
 		if (accesskey != null) writer.writeAttribute("accesskey", accesskey, null);
 		String alt = comboBox.getAlt();
 		if (alt != null) writer.writeAttribute("alt", alt, null);
 		String autocomplete = comboBox.getAutocomplete();
 		if (autocomplete != null) writer.writeAttribute("autocomplete", autocomplete, null);
 		String dir = comboBox.getDir();
 		if (dir != null) writer.writeAttribute("dir", dir, null);
 		boolean disabled = comboBox.isDisabled();
 		if (disabled) writer.writeAttribute("disabled", "disabled", null);
 		String lang = comboBox.getLang();
 		if (lang != null) writer.writeAttribute("lang", lang, null);
 		int maxlength = comboBox.getMaxlength();
 		if (maxlength != Integer.MIN_VALUE) writer.writeAttribute("maxlength", maxlength, null);
 		boolean readonly = comboBox.isReadonly();
 		if (readonly) writer.writeAttribute("readonly", "readonly", null);
 		String tabindex = comboBox.getTabindex();
 		if (tabindex != null) writer.writeAttribute("tabindex", tabindex, null);
 		String title = comboBox.getTitle();
 		if (title != null) writer.writeAttribute("title", title, null);
 		writer.endElement("input");
 		
 		// down arrow span
 		writer.startElement("div", null);
 		writer.writeAttribute("class", "ui-state-default ui-corner-right", null);
 		writer.writeAttribute("style", "float:right; width:17px; border-top:0; border-right:0; border-bottom:0;", null);
 		writer.startElement("div", null);
		writer.endElement("div");
		writer.startElement("div", null);
 		writer.writeAttribute("class", "ui-icon ui-icon-triangle-1-s", null);
 		writer.endElement("div");
 		writer.endElement("div");
 		
 		writer.endElement("span");
 		
 		writeLabelAndIndicatorAfter(labelAttributes);
 
 		String divId = clientId + AUTOCOMPLETE_DIV;
 
 		writer.startElement("div", null);
 		writer.writeAttribute("id", divId, null);
 		writer.writeAttribute("class", "ui-widget ui-widget-content ui-corner-all ui-combobox-list", null);
 		writer.writeAttribute("style", "display:none;z-index:500;", null);
 		writer.endElement("div");
 
 		encodeScript(facesContext, writer, clientId, comboBox,
 				paramMap, inFieldLabel, inputClientId, labelIsInField);
 
 		writer.endElement("div");
 	}
 
 	private void encodeScript(FacesContext facesContext, ResponseWriter writer, String clientId, ComboBox comboBox, Map paramMap, String inFieldLabel, String inputClientId, boolean labelIsInField) throws IOException {
 		String divId = clientId + AUTOCOMPLETE_DIV;
 		Object sourceId = paramMap.get("ice.event.captured");
 		boolean isEventSource = sourceId != null && sourceId.toString().equals(inputClientId);
 
 		// script
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 
 		if (!comboBox.isDisabled() && !comboBox.isReadonly()) {
 			JSONBuilder jb = JSONBuilder.create();
 
 			jb.beginFunction("ice.ace.create")
 			.item("ComboBox")
 			
 			.beginArray()
 			.item(clientId)
 			.item(divId)
 			.item("ui-widget-content")
 			.item("ui-state-hover")
 			.item("ui-state-active")
 			.item(comboBox.getHeight())
 			.item(comboBox.isShowListOnInput())
 			.beginMap()
 			.entry("p", ""); // dummy property
 			encodeClientBehaviors(facesContext, comboBox, jb);
 			jb.endMap();
 			
 			jb.beginMap()
 			.entryNonNullValue("inFieldLabel", inFieldLabel)
 			.entry("inFieldLabelStyleClass", IN_FIELD_LABEL_STYLE_CLASS)
 			.entry("labelIsInField", labelIsInField);
 			jb.endMap();
 			
 			int rows = comboBox.getRows();
 			if (rows == 0) rows = Integer.MAX_VALUE;
 			FilterMatchMode filterMatchMode = getFilterMatchMode(comboBox);
 
 			jb.beginMap()
 			.entry("rows", rows)
 			.entry("filterMatchMode", filterMatchMode.toString())
 			.entry("caseSensitive", comboBox.isCaseSensitive())
 			.endMap();
 
 			jb.endArray();
 			jb.endFunction();
 
 			writer.writeText(jb.toString(), null);
 		}
 
 		writer.endElement("script");
 		
 		populateList(facesContext, comboBox);
 
 		// field update script
 		Object value = comboBox.getValue();
 		if (value == null) value = "";
 		writer.startElement("span", null);
 		writer.writeAttribute("id", clientId + "_fieldupdate", null);
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		writer.writeText("(function() {", null);
 		writer.writeText("var instance = ice.ace.ComboBoxes[\"" + clientId + "\"];", null);
 		writer.writeText("instance.updateValue('" + escapeJavascriptString(value.toString()) + "');", null);
 		writer.writeText("})();", null);
 		writer.endElement("script");
 		writer.endElement("span");
 	}
 
 	public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 
 	}
 
 	public void populateList(FacesContext facesContext, ComboBox comboBox) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		String clientId = comboBox.getClientId(facesContext);
 		boolean ariaEnabled = EnvUtils.isAriaEnabled(facesContext);
 		comboBox.populateItemList();
 		Iterator matches = comboBox.getItemListIterator();
 		writer.startElement("div", null);
 		writer.writeAttribute("id", clientId + "_update", null);
 		if (comboBox.getSelectFacet() != null) {
 
 			UIComponent facet = comboBox.getSelectFacet();
 			ValueExpression itemValue = comboBox.getValueExpression("itemValue");
 			ValueExpression itemDisabled = comboBox.getValueExpression("itemDisabled");
 			ELContext elContext = facesContext.getELContext();
 			String listVar = comboBox.getListVar();
 
 			writer.startElement("div", null);
 			writer.writeAttribute("style", "display: none;", null);
 			writer.startElement("div", null);
 			Map requestMap = facesContext.getExternalContext().getRequestMap();
 			//set index to 0, so child components can get client id from comboBox component
 			comboBox.setIndex(0);
 			while (matches.hasNext()) {
 
 				requestMap.put(listVar, matches.next());
 				Object value = itemValue.getValue(elContext);
 				boolean disabled = false;
 				
 				try {
 					disabled = (Boolean) itemDisabled.getValue(elContext);
 				} catch (Exception e) {}
 			
 				writer.startElement("div", null);
 				writer.writeAttribute("style", "border: 0;", null);
 				if (ariaEnabled) writer.writeAttribute("role", "option", null);
 				if (disabled) writer.writeAttribute("class", "ui-state-disabled", null);
 				
 				writer.startElement("span", null); // span to display
 				writer.writeAttribute("class", LABEL_CLASS, null);
 				encodeParentAndChildren(facesContext, facet);
 				writer.endElement("span");
 				writer.startElement("span", null); // value span
 				writer.writeAttribute("class", VALUE_CLASS, null);
 				writer.writeAttribute("style", "visibility:hidden;display:none;", null);
 				String itemLabel;
 				try {
 					itemLabel = (String) getConvertedValue(facesContext, comboBox, value);
 				} catch (Exception e) {
 					itemLabel = (String) value;
 				}
 				if (itemLabel != null) {
 					writer.writeText(itemLabel, null);
 				}
 				writer.endElement("span");
 				comboBox.resetId(facet);
 				writer.endElement("div");
 				
 				requestMap.remove(listVar);
 			}
 			comboBox.setIndex(-1);
 
 			writer.endElement("div");
 			String call = "ice.ace.ComboBoxes[\"" + clientId +
 					"\"].setContent(ice.ace.jq(ice.ace.escapeClientId('" + clientId + "_update')).get(0).firstChild.innerHTML);";
 			encodeDynamicScript(facesContext, comboBox, call);
 			writer.endElement("div");
 		} else {
 			if (matches.hasNext()) {
 				writer.startElement("div", null);
 				writer.writeAttribute("style", "display: none;", null);
 				writer.startElement("div", null);
 				SelectItem item = null;
 				while (matches.hasNext()) {
 					item = (SelectItem) matches.next();
 					Object value = item.getValue();
 					
 					String convertedValue;
 					try {
 						convertedValue = (String) getConvertedValue(facesContext, comboBox, value);
 					} catch (Exception e) {
 						convertedValue = (String) value;
 					}
 					
 					String itemLabel = item.getLabel();
 					if (itemLabel == null) itemLabel = convertedValue;
 					
 					writer.startElement("div", null);
 					writer.writeAttribute("style", "border: 0;", null);
 					if (item.isDisabled()) {
 						writer.writeAttribute("class", "ui-state-disabled", null);
 					}
 					if (ariaEnabled) writer.writeAttribute("role", "option", null);
 					
 					// label span
 					writer.startElement("span", null);
 					writer.writeAttribute("class", LABEL_CLASS, null);
 					writer.writeText(itemLabel, null);
 					writer.endElement("span");
 					// value span
 					writer.startElement("span", null);
 					writer.writeAttribute("class", VALUE_CLASS, null);
 					writer.writeAttribute("style", "visibility:hidden;display:none;", null);
 					writer.writeText(convertedValue, null);
 					writer.endElement("span");
 					
 					writer.endElement("div");
 				}
 				writer.endElement("div");
 				String call = "ice.ace.ComboBoxes[\"" + clientId +
 					"\"].setContent(ice.ace.jq(ice.ace.escapeClientId('" + clientId + "_update')).get(0).firstChild.innerHTML);";
 				encodeDynamicScript(facesContext, comboBox, call);
 				writer.endElement("div");
 			}
 		}
 		writer.endElement("div");
 	}
 
 	public void encodeDynamicScript(FacesContext facesContext, UIComponent uiComponent, String call) throws IOException {
 		ResponseWriter writer = facesContext.getResponseWriter();
 		String clientId = uiComponent.getClientId(facesContext);
 		
 		writer.startElement("span", null);
 		writer.startElement("script", null);
 		writer.writeAttribute("type", "text/javascript", null);
 		writer.writeText(call, null);
 		writer.endElement("script");
 		writer.endElement("span");
 	}
 		
 	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {
 
 	}
 
 	private static String escapeSingleQuote(String text) {
 		if (null == text) {
 			return "";
 		}
 		char[] chars = text.toCharArray();
 		StringBuilder buffer = new StringBuilder(chars.length);
 		for (int index = 0; index < chars.length; index++) {
 			char ch = chars[index];
 			if (ch == '\'') {
 				buffer.append("&#39;");
 			} else {
 				buffer.append(ch);
 			}
 		}
 
 		return buffer.toString();
 	}
 
 	private static String escapeJavascriptString(String str) {
 		if (str == null) return "";
 		return str.replace("\\", "\\\\").replace("\'","\\'");
 	}
 
 	// taken from com.icesoft.faces.renderkit.dom_html_basic.DomBasicRenderer
 	public static void encodeParentAndChildren(FacesContext facesContext, UIComponent parent) throws IOException {
 		parent.encodeBegin(facesContext);
 		if (parent.getRendersChildren()) {
 			parent.encodeChildren(facesContext);
 		} else {
 			if (parent.getChildCount() > 0) {
 				Iterator children = parent.getChildren().iterator();
 				while (children.hasNext()) {
 					UIComponent nextChild = (UIComponent) children.next();
 					if (nextChild.isRendered()) {
 						encodeParentAndChildren(facesContext, nextChild);
 					}
 				}
 			}
 		}
 		parent.encodeEnd(facesContext);
 	}
 	
 	private FilterMatchMode getFilterMatchMode(ComboBox comboBox) {
 		String filterMatchMode = comboBox.getFilterMatchMode();
 		if ("contains".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.contains;
 		if ("exact".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.exact;
 		if ("endsWith".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.endsWith;
 		if ("none".equalsIgnoreCase(filterMatchMode)) return FilterMatchMode.none;
 		return FilterMatchMode.startsWith;
 	}
 	
 	private enum FilterMatchMode {
 		contains,
 		exact,
 		startsWith,
 		endsWith,
 		none
 	}
 
 	@Override
 	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
 		ComboBox comboBox = (ComboBox) component;
 		String value = (String) submittedValue;
 		Converter converter = comboBox.getConverter();
 		
 			if(converter != null) {
 				return converter.getAsObject(context, comboBox, value);
 			}
 			else {
 				ValueExpression ve = comboBox.getValueExpression("value");
 
 				if(ve != null) {
 					Class<?> valueType = ve.getType(context.getELContext());
 					Converter converterForType = context.getApplication().createConverter(valueType);
 
 					if(converterForType != null) {
 						return converterForType.getAsObject(context, comboBox, value);
 					}
 				}
 			}
 		
 		return value;
 	}
 }
