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
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.ConverterException;
 import javax.faces.model.SelectItem;
 
 import org.ajax4jsf.javascript.JSFunctionDefinition;
 import org.ajax4jsf.javascript.ScriptString;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.renderkit.HeaderResourcesRendererBase;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.ajax4jsf.util.InputUtils;
 import org.ajax4jsf.util.SelectUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.richfaces.component.UIInplaceSelect;
 
 /**
  * InplaceSelect base renderer implementation 
  * @author Anton Belevich
  * @since 3.2.0	
  */
 public class InplaceSelectBaseRenderer extends HeaderResourcesRendererBase {
 
 	private static Log logger = LogFactory.getLog(InplaceSelectBaseRenderer.class);
 	//TODO: move duplicate constants to superclass
 	private static final String RICH_INPLACE_SELECT_CLASSES = "rich-inplace-select-item rich-inplace-select-font";
 	private static final String CONTROLS_FACET = "controls";
 	private static final String EMPTY_DEFAULT_LABEL = "\u00a0\u00a0\u00a0";
 
 	protected static final class PreparedSelectItem implements ScriptString {
 		private String label;
 		private String convertedValue;
 
 		public PreparedSelectItem(String convertedValue, String label) {
 			super();
 			this.convertedValue = convertedValue;
 			this.label = label;
 		}
 
 		public String getLabel() {
 			return label;
 		}
 
 		public String getConvertedValue() {
 			return convertedValue;
 		}
 
 		public void appendScript(StringBuffer functionString) {
 			functionString.append(this.toScript());
 		}
 
 		public String toScript() {
 			return "[" + ScriptUtils.toScript(label) + ", " + ScriptUtils.toScript(convertedValue) + "]";
 		}
 	}
 
 	@Override
 	protected void doDecode(FacesContext context, UIComponent component) {
 		UIInplaceSelect inplaceSelect = null;
 
 		if (component instanceof UIInplaceSelect) {
 			inplaceSelect = (UIInplaceSelect) component;
 		} else {
 			if (logger.isDebugEnabled()) {
 				logger.debug("No decoding necessary since the component " + component.getId()
 						+ " is not an instance or a sub class of UIInplaceSelect");
 			}
 			return;
 		}
 
 		String clientId = inplaceSelect.getClientId(context);
 		if (clientId == null) {
 			throw new NullPointerException("component client id is NULL");
 		}
 
 		if (InputUtils.isDisabled(inplaceSelect) || InputUtils.isReadOnly(inplaceSelect)) {
 			if (logger.isDebugEnabled()) {
 				logger.debug(("No decoding necessary since the component " + component.getId() + " is disabled"));
 			}
 			return;
 		}
 
 		Map <String,String> request = context.getExternalContext().getRequestParameterMap();
 		String newValue = (String) request.get(clientId);
 		if (newValue != null) {
 			inplaceSelect.setSubmittedValue(newValue);
 		} else {
 			inplaceSelect.setSubmittedValue(null);
 		}
 	}
 
 	protected boolean isAcceptableComponent(UIComponent component) {
 		return component != null && this.getComponentClass().isAssignableFrom(component.getClass());
 	}
 
 	protected String getConvertedStringValue(FacesContext context, UIComponent component, Object value) {
 		return InputUtils.getConvertedStringValue(context, component, value);
 	}
 
 	protected void encodeSuggestion(FacesContext context, UIComponent component, String value, String classes) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		if(writer != null) {
 			writer.startElement(HTML.SPAN_ELEM, component);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, classes, null);
			if(value != null && value.trim().length() > 0) {
				writer.writeText(value, null);
			} else {
				writer.write("\u00a0");
			}
 			writer.endElement(HTML.SPAN_ELEM);
 		}
 	}	
 
 	public List<PreparedSelectItem> prepareItems(FacesContext context, UIComponent component) {
 		if (!isAcceptableComponent(component)) {
 			return null;
 		}
 
 		List<PreparedSelectItem> itemsList = new ArrayList<PreparedSelectItem>();
 
 		UIInplaceSelect inplaceSelect = (UIInplaceSelect) component;
 		List<SelectItem> selectItems = SelectUtils.getSelectItems(context, inplaceSelect);
 		for (SelectItem selectItem : selectItems) {
 			String convertedValue = getConvertedStringValue(context, inplaceSelect, selectItem.getValue());
 			String label = selectItem.getLabel().trim();
 			itemsList.add(new PreparedSelectItem(convertedValue, label));
 		}
 
 		return itemsList;
 	}
 
 	public void encodeItems(FacesContext context, UIComponent component, List<PreparedSelectItem> items) throws IOException, IllegalArgumentException {
 		if (items != null) {
 			UIInplaceSelect inplaceSelect = (UIInplaceSelect) component;
 
 			for (PreparedSelectItem preparedSelectItem : items) {
 
 				encodeSuggestion(context, inplaceSelect, preparedSelectItem.getLabel(), RICH_INPLACE_SELECT_CLASSES);
 			}
 		}
 	}
 
     public String getDefaultLabel(FacesContext context, UIComponent component) {
     	String defaultLabel = (String)component.getAttributes().get("defaultLabel");
     	if (defaultLabel == null || defaultLabel.trim().equals("")) {
     		defaultLabel = EMPTY_DEFAULT_LABEL;
     	}
     	return defaultLabel;
     }
 
 	public void encodeControlsFacet(FacesContext context, UIComponent component) throws IOException {
 		UIComponent facet = component.getFacet(CONTROLS_FACET);
 		if ((facet != null) && (facet.isRendered())) {
 			renderChild(context, facet);
 		}
 	}
 
 	public boolean isControlsFacetExists(FacesContext context, UIComponent component) {
 		UIComponent facet = component.getFacet(CONTROLS_FACET);
 		if (facet != null && facet.isRendered()) {
 			return true;
 		}
 		return false;
 	}
 
     @Override
     public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue) throws ConverterException {
     	if ("".equals(submittedValue)) {
     		return null;
     	} else {
         	return InputUtils.getConvertedValue(context, component, submittedValue);
     	}
     }
 	
 	protected Class<? extends UIComponent> getComponentClass() {
 		return UIInplaceSelect.class;
 	}
 
 	//    public String getSelectedItemLabel(FacesContext context, UIInplaceSelect component) {
 	//    String selectedItemLabel = (String)component.getSubmittedValue();
 	//    if(selectedItemLabel == null || !component.isValid()) {
 	//		Object value = component.getAttributes().get("value");
 	//		if (value == null || "".equals(value)) {
 	//			selectedItemLabel = createDefaultLabel(component);
 	//		} else {
 	//			selectedItemLabel = getItemLabel(context, component, value);
 	//		}
 	//	}
 	//	return selectedItemLabel;
 	//    }
 
 	protected String getItemLabel(FacesContext context, UIInplaceSelect component, Object value) {
 		String itemLabel = null;
 		boolean equivValues;
 		
 		// TODO: SelectUtils.getSelectItems is called minimum twice during encode
 		List<SelectItem> selectItems = SelectUtils.getSelectItems(context, component);
 		if (!selectItems.isEmpty()) {
 			for (SelectItem item : selectItems) {
 				if (value != null) {
 					equivValues = value.equals(item.getValue());
 				} else {
 					equivValues = item.getValue() == null;
 				}
 				
 				if (equivValues) {
 					itemLabel = component.isShowValueInView() ? getConvertedStringValue(context, component, item.getValue()) : item.getLabel();
 					break;
 				}
 			}
 		}
 
 		return itemLabel;
 	}
 
 	protected String createDefaultLabel(UIComponent component) {
 		String defaultLabel = (String) component.getAttributes().get("defaultLabel");
 		if (defaultLabel == null || defaultLabel.trim().equals("")) {
 			defaultLabel = EMPTY_DEFAULT_LABEL;
 		}
 		return defaultLabel;
 	}
 
 	protected boolean isEmptyDefaultLabel(String defaultLabel) {
 		if (EMPTY_DEFAULT_LABEL.equals(defaultLabel)) {
 			return true;
 		}
 		return false;
 	}
 }
