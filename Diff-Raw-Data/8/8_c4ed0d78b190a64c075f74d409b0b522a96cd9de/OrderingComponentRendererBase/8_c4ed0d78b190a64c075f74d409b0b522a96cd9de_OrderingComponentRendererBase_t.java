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
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIViewRoot;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.convert.Converter;
 
 import org.ajax4jsf.component.UIDataAdaptor;
 import org.ajax4jsf.javascript.JSFunctionDefinition;
 import org.ajax4jsf.javascript.JSReference;
 import org.ajax4jsf.javascript.ScriptString;
 import org.ajax4jsf.javascript.ScriptUtils;
 import org.ajax4jsf.renderkit.ComponentVariables;
 import org.ajax4jsf.renderkit.ComponentsVariableResolver;
 import org.ajax4jsf.renderkit.RendererUtils.HTML;
 import org.richfaces.component.UIOrderingBaseComponent;
 import org.richfaces.component.UIOrderingBaseComponent.ItemState;
 import org.richfaces.component.util.MessageUtil;
 
 /**
  * @author Nick Belaevski
  * 
  */
 public abstract class OrderingComponentRendererBase extends
 		AbstractRowsRenderer {
 
 	private static final Converter DEFAULT_CONVERTER = new Converter() {
 
 		public Object getAsObject(FacesContext context, UIComponent component,
 				String value) {
 
 			return value;
 		}
 
 		public String getAsString(FacesContext context, UIComponent component,
 				Object value) {
 			if (value instanceof String) {
 				return (String) value;
 			}
 
 			if (null == value) {
 				return "";
 			}
 
 			return value.toString();
 		}
 
 	};
 
 	private static final String ITEM_STATE_VAR_NAME = "itemState";
 
 	protected final static String SHOW_LABELS_ATTRIBUTE_NAME = "showButtonLabels";
 
 	protected final static String ATTRIBUTE_CE_ONHEADERCLICK = "onheaderclick";
 
 	protected final static String CONTROL_TYPE_LINK = "link";
 
 	protected final static String CONTROL_TYPE_BUTTON = "button";
 
 	protected final static String CONTROL_TYPE_NONE = "none";
 
 	protected final static String ATTRIBUTE_CONTROLS_TYPE = "controlsType";
 
 	protected static abstract class ControlsHelper {
 		private String name;
 
 		private String bundlePropertyName;
 
 		private String imageURI;
 
 		private String facetName;
 
 		private String styleClassName;
 
 		private String idSuffix;
 
 		String customEvent;
 
 		String styleFromAttribute;
 
 		private String buttonStyleClass;
 
 		boolean enable;
 
 		private String defaultText;
 
 		private String labelAttributeName;
 		
 		private String title;
 
 		public abstract boolean isRendered(FacesContext context,
 				UIComponent component);
 
 		public ControlsHelper(String name, String bundlePropertyName,
 				String defaultText, String imageURI, String facetName,
 				String styleClassName, String styleFromAttribute,
 				String buttonStyleClass, String idSuffix, String customEvent,
 				boolean isEnable, String labelAttributeName, String  title) {
 			super();
 			this.name = name;
 			this.bundlePropertyName = bundlePropertyName;
 			this.defaultText = defaultText;
 			this.imageURI = imageURI;
 			this.facetName = facetName;
 			this.styleClassName = styleClassName;
 			this.styleFromAttribute = styleFromAttribute;
 			this.idSuffix = idSuffix;
 			this.customEvent = customEvent;
 			this.buttonStyleClass = buttonStyleClass;
 			this.enable = isEnable;
 			this.labelAttributeName = labelAttributeName;
 			this.title = title;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public String getBundlePropertyName() {
 			return bundlePropertyName;
 		}
 
 		public String getImageURI() {
 			return imageURI;
 		}
 
 		public String getFacetName() {
 			return facetName;
 		}
 
 		public String getStyleClassName() {
 			return styleClassName;
 		}
 
 		public String getIdSuffix() {
 			return idSuffix;
 		}
 
 		public String getCustomEvent() {
 			return customEvent;
 		}
 
 		public String getStyleFromAttribute() {
 			return styleFromAttribute;
 		}
 
 		public String getButtonStyleClass() {
 			return buttonStyleClass;
 		}
 
 		public boolean isEnable() {
 			return enable;
 		}
 
 		public String getDefaultText() {
 			return defaultText;
 		}
 
 		public String getLabelAttributeName() {
 			return labelAttributeName;
 		}
 
         public String getTitle() {
             return title;
         }
 
         public void setTitle(String title) {
             this.title = title;
         }
 	}
 
 	protected static final class SelectionState {
 
 		private boolean firstSelected = false;
 		private boolean firstSelectedLatch = false;
 
 		private boolean selectedLatch = false;
 
 		private boolean itemsExist = false;
 
 		private boolean lastSelected = false;
 
 		public SelectionState() {
 			super();
 		}
 
 		public void addState(boolean selected) {
 			itemsExist = true;
 
 			if (!firstSelectedLatch) {
 				firstSelected = selected;
 				firstSelectedLatch = true;
 			}
 
 			if (selected) {
 				selectedLatch = true;
 			}
 
 			lastSelected = selected;
 		}
 
 		public boolean isFirstSelected() {
 			return firstSelected;
 		}
 
 		public boolean isSelected() {
 			return selectedLatch;
 		}
 
 		public boolean isItemExist() {
 			return itemsExist;
 		}
 
 		public boolean isLastSelected() {
 			return lastSelected;
 		}
 	}
 
 	private final String bundleName;
 
 	public OrderingComponentRendererBase(String bundleName) {
 		super();
 
 		this.bundleName = bundleName;
 	}
 
 	public void encodeCaption(FacesContext context, UIComponent component)
 			throws IOException {
 		encodeCaption(context, component,
 				OrderingComponentControlsHelper.FACET_CAPTION,
 				"rich-ordering-list-caption");
 	}
 
 	protected void encodeCaption(FacesContext context, UIComponent component,
 			String facetCaption, String captionStyle, String attributeName)
 			throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		UIComponent facetEl = component.getFacet(facetCaption);
 		boolean renderFacet = ((facetEl != null) && facetEl.isRendered());
 		String captionAttr = (String) component.getAttributes().get(
 				attributeName);
 
 		if (renderFacet || (captionAttr != null)) {
 			writer.startElement(HTML.DIV_ELEM, component);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, captionStyle, null);
 			if (renderFacet) {
 				renderChild(context, facetEl);
 			} else {
 				writer.write(captionAttr);
 			}
 			writer.endElement(HTML.DIV_ELEM);
 		}
 	}
 
 	protected void encodeCaption(FacesContext context, UIComponent component,
 			String facetCaption, String captionStyle) throws IOException {
 		encodeCaption(context, component, facetCaption, captionStyle,
 				OrderingComponentControlsHelper.ATTRIBUTE_CAPTION_LABEL);
 	}
 
 	public void encodeHeader(FacesContext context,
 			UIOrderingBaseComponent component) throws IOException {
 		encodeHeader(context, component, "rich-ordering-list-table-header",
 				"rich-ordering-list-table-header-cell", "headerClass");
 	}
 
 	protected void encodeHeader(FacesContext context,
 			UIOrderingBaseComponent component, String rowClass,
 			String cellClass, String headerClassAttr) throws IOException {
 		ResponseWriter writer = context.getResponseWriter();
 		// UIComponent header = orderingList.getHeader();
 		Iterator headers = component.columns();
 
 		if (headers.hasNext()) {
 			writer.startElement("thead", component);
 			String headerClass = (String) component.getAttributes().get(
 					headerClassAttr);
 
 			writer.startElement("tr", component);
 			encodeStyleClass(writer, null, rowClass, null, headerClass);
 			encodeHeaderFacets(context, writer, headers, cellClass,
 					headerClass, "header", "th", component);
 
 			writer.endElement("tr");
 
 			writer.endElement("thead");
 		}
 	}
 
 	protected void renderDefaultControl(FacesContext context,
 			UIOrderingBaseComponent orderingList, ResponseWriter writer,
 			boolean useFacet,
 			OrderingComponentRendererBase.ControlsHelper helper,
 			String clientId, ResourceBundle bundleExternal,
 			ResourceBundle bundleApplication, boolean enabled,
 			String baseStyle, String baseControlStyle) throws IOException {
 		UIComponent facet = orderingList.getFacet(helper.getFacetName());
 		String customEvent = null;
 		Map attributes = orderingList.getAttributes();
 		if (helper.customEvent != null) {
 			customEvent = (String) attributes.get(helper.customEvent);
 		}
 
 		String styleFromAttribute = (String) attributes
 				.get(helper.styleFromAttribute);
 
 		String baseStyleLight = baseStyle.concat("-light");
 		String baseStylePress = baseStyle.concat("-press");
 
 		String currentStyle = baseControlStyle + helper.getStyleClassName();
 		if (styleFromAttribute != null) {
 			currentStyle = styleFromAttribute.concat(currentStyle);
 		}
 
 		writer.startElement(HTML.DIV_ELEM, orderingList);
 		String controlId = clientId + helper.getIdSuffix();
 		writer.writeAttribute(HTML.id_ATTRIBUTE, controlId, null); // FIXME:
 		writer.writeAttribute(HTML.class_ATTRIBUTE, currentStyle, null);
 		String style = null;
 		if (enabled) {
 			style = "display:block;";
 		} else {
 			style = "display:none;";
 		}
 		writer.writeAttribute(HTML.style_ATTRIBUTE, style, null);
 		if (!useFacet) {
 
 			writer.startElement(HTML.DIV_ELEM, orderingList);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle
 					+ helper.getButtonStyleClass(), null);
 			if (helper.enable) {
 				writer.writeAttribute(HTML.onmouseover_ATTRIBUTE,
 						"this.className='" + baseStyleLight + "'", null);
 				writer.writeAttribute(HTML.onmousedown_ATTRIBUTE,
 						"this.className='" + baseStylePress + "'", null);
 				writer.writeAttribute(HTML.onmouseup_ATTRIBUTE,
 						"this.className='" + baseStyle + "'", null);
 				writer.writeAttribute(HTML.onmouseout_ATTRIBUTE,
 						"this.className='" + baseStyle + "'", null);
 				
 				writer.startElement(HTML.a_ELEMENT, orderingList);
 				writer.writeAttribute(HTML.id_ATTRIBUTE, controlId + "link", null); // FIXME:
 				//writer.writeAttribute(HTML.HREF_ATTR, "#", null);
 				writer.writeAttribute(HTML.onclick_ATTRIBUTE, "return false;", null);
 			
 				writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle
 						+ "-selection", null);
 				writer.writeAttribute(HTML.onblur_ATTRIBUTE,
 						"Richfaces.Control.onblur(this);", null);
 				writer.writeAttribute(HTML.onfocus_ATTRIBUTE,
 						"Richfaces.Control.onfocus(this);", null);
 			}
 
 			writer.startElement(HTML.DIV_ELEM, orderingList);
 			writer.writeAttribute(HTML.class_ATTRIBUTE, baseStyle + "-content",
 					null);
 
 		}
 
 		/*if (customEvent != null) {
 			writer.writeAttribute(HTML.onclick_ATTRIBUTE, customEvent, null);
 		}*/
 
 		if (useFacet) {
 			renderChild(context, facet);
 		} else {
 			writer.startElement(HTML.IMG_ELEMENT, orderingList);
 			//buttons sometimes don't work under IE if mouse cursor 
 			//is above some special areas of the button
 			writer.writeAttribute(HTML.class_ATTRIBUTE, "rich-ordering-control-img", null);
 		
 			//writer.writeAttribute(HTML.width_ATTRIBUTE, "15", null);
 			//writer.writeAttribute(HTML.height_ATTRIBUTE, "15", null);
 			writer.writeAttribute(HTML.alt_ATTRIBUTE, 
 			        orderingList.getAttributes().get(helper.getTitle()), null);
 			writer.writeAttribute(HTML.src_ATTRIBUTE, getResource(
 					helper.getImageURI()).getUri(context, null), null);
 
 			writer.endElement(HTML.IMG_ELEMENT);
 
 			if (getUtils().isBooleanAttribute(orderingList,
 					SHOW_LABELS_ATTRIBUTE_NAME)) {
 				String label = (String) attributes.get(helper
 						.getLabelAttributeName());
 
 				if (label == null) {
 					if (null != bundleApplication) {
 						try {
 							label = bundleApplication.getString(helper
 									.getBundlePropertyName());
 						} catch (MissingResourceException e) {
 							try {
 								if (null != bundleExternal) {
 									label = bundleExternal.getString(helper
 											.getBundlePropertyName());
 								}
 							} catch (MissingResourceException exc) {
 								// Key wasn't found, ignore this.
 							}
 						}
 
 					} else if (null != bundleExternal) {
 						try {
 							label = bundleExternal.getString(helper
 									.getBundlePropertyName());
 						} catch (MissingResourceException exc) {
 							// Key wasn't found, ignore this.
 						}
 
 					}
 
 				}
 
 				if (label == null) {
 					label = helper.getDefaultText();
 				}
 				writer.writeText(label, null);
 			}
 		}
 		// writer.writeAttribute(HTML.id_ATTRIBUTE, clientId +
 		// helper.getIdSuffix(), null);
 
 		// writer.writeAttribute(HTML.class_ATTRIBUTE, currentStyle, null);
 
 		if (!useFacet) {
 			writer.endElement(HTML.DIV_ELEM);
 			if (helper.enable) {
 				writer.endElement(HTML.a_ELEMENT);
 			}
 			writer.endElement(HTML.DIV_ELEM);
 		}
 		writer.endElement(HTML.DIV_ELEM);
 	}
 
 	protected boolean isHeaderExists(FacesContext context,
 			UIOrderingBaseComponent component, String facetName) {
 		Iterator headers = component.columns();
 		while (headers.hasNext()) {
 			UIComponent column = (UIComponent) headers.next();
 			UIComponent facet = column.getFacet(facetName);
 			if (facet != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected void encodeHeaderFacets(FacesContext context,
 			ResponseWriter writer, Iterator headers, String skinCellClass,
 			String headerClass, String facetName, String element,
 			UIOrderingBaseComponent orderingList) throws IOException {
 		while (headers.hasNext()) {
 			UIComponent column = (UIComponent) headers.next();
 			String classAttribute = facetName + "Class";
 			String columnHeaderClass = (String) column.getAttributes().get(
 					classAttribute);
 
 			writer.startElement(element, column);
 			if (!headers.hasNext()) {
 				skinCellClass = skinCellClass + "-last";
 			}
 			encodeStyleClass(writer, null, skinCellClass, headerClass,
 					columnHeaderClass);
 			getUtils().encodeAttribute(context, column, "colspan");
 			writer.startElement("div", column);
 			writer.writeAttribute("style",
 					"overflow:hidden;white-space: nowrap;", null);
 			String onHeaderClickEvent = (String) orderingList.getAttributes()
 					.get(ATTRIBUTE_CE_ONHEADERCLICK);
 			if (onHeaderClickEvent != null) {
 				writer.writeAttribute(HTML.onclick_ATTRIBUTE,
 						onHeaderClickEvent, null);
 			}
 
 			UIComponent facet = column.getFacet(facetName);
 			if (facet != null && facet.isRendered()) {
 				renderChild(context, facet);
 			} else {
				writer.write("&#160;");
 			}
 
 			writer.endElement("div");
 			writer.endElement(element);
 		}
 	}
 
 	protected void encodeControlFacet(FacesContext context,
 			UIOrderingBaseComponent orderingList,
 			OrderingComponentRendererBase.ControlsHelper helper,
 			String clientId, ResponseWriter writer, boolean enabled,
 			String baseStyle, String baseControlStyle) throws IOException {
 		Locale locale = null;
 
 		UIViewRoot viewRoot = context.getViewRoot();
 		if (viewRoot != null) {
 			locale = viewRoot.getLocale();
 		}
 
 		ClassLoader contextClassLoader = Thread.currentThread()
 				.getContextClassLoader();
 		ResourceBundle bundleExternal = null;
 		ResourceBundle bundleApplication = null;
 		String messageBundle = context.getApplication().getMessageBundle();
 		if (locale != null) {
 			if (null != messageBundle) {
 				bundleApplication = ResourceBundle.getBundle(messageBundle,
 						locale, contextClassLoader);
 			}
 
 			try {
 				bundleExternal = ResourceBundle.getBundle(bundleName, locale,
 						contextClassLoader);
 			} catch (MissingResourceException e) {
 				// No external bundle was found, ignore this exception.
 			}
 		}
 
 		Map attributes = orderingList.getAttributes();
 		UIComponent facet = orderingList.getFacet(helper.getFacetName());
 		boolean useFacet = (facet != null && facet.isRendered());
 		String controlType = (String) attributes.get(ATTRIBUTE_CONTROLS_TYPE);
 
 		if (CONTROL_TYPE_NONE.equals(controlType)) {
 		    return;
 		} else {
 			renderDefaultControl(context, orderingList, writer, useFacet,
 					     helper, clientId, bundleExternal, bundleApplication,
 					     enabled, baseStyle, baseControlStyle);
 		    
 		}
 	}
 
 	public void encodeBegin(FacesContext context, UIComponent component)
 			throws IOException {
 		UIOrderingBaseComponent orderingComponent = (UIOrderingBaseComponent) component;
 		ComponentVariables variables = ComponentsVariableResolver.getVariables(
 				this, component);
 		variables.setVariable(ITEM_STATE_VAR_NAME, orderingComponent
 				.getItemState());
 
 		super.encodeBegin(context, component);
 	}
 
 	protected ItemState getItemState(FacesContext context,
 			UIComponent component, ComponentVariables variables)
 			throws IOException {
 		return (ItemState) variables.getVariable(ITEM_STATE_VAR_NAME);
 	}
 
 	protected Converter getConverter(FacesContext context,
 			UIOrderingBaseComponent component, boolean warnOnDefaultConverter) {
 		Converter converter = component.getConverter();
 
 		if (converter == null) {
 			converter = component.getConverterForValue(context);
 		}
 
 		if (converter == null) {
 			converter = DEFAULT_CONVERTER;
 			
 			if (warnOnDefaultConverter) {
 				Object componentLabel = MessageUtil.getLabel(context, component);
 				context.getExternalContext().log("Converter for component [" + componentLabel + 
 					"] cannot be discovered, so default implementation of converter will be used." +
 					" Component items will be converted to String on decoding.");
 			}
 		}
 
 		return converter;
 	}
 
 	public String getCaptionDisplay(FacesContext context, UIComponent component) {
 		Object caption = component.getAttributes().get(
 				OrderingComponentControlsHelper.ATTRIBUTE_CAPTION_LABEL);
 		UIComponent facet = component
 				.getFacet(OrderingComponentControlsHelper.FACET_CAPTION);
 
 		if ((null != caption && !"".equals(caption))
 				|| (null != facet && facet.isRendered())) {
 			return "";
 		}
 		return "display: none;";
 	}
 
 	public String getAsEventHandler(FacesContext context,
 			UIComponent component, String attributeName) {
 		String event = (String) component.getAttributes().get(attributeName);
 		ScriptString result = JSReference.NULL;
 
 		if (event != null) {
 			event = event.trim();
 
 			if (event.length() != 0) {
 				JSFunctionDefinition function = new JSFunctionDefinition();
 				function.addParameter("event");
 				function.addToBody(event);
 
 				result = function;
 			}
 		}
 
 		return ScriptUtils.toScript(result);
 	}
 
 	public String getColumnClassesAsJSArray(FacesContext context,
 			UIComponent component) {
 		return ScriptUtils.toScript(getClassesAsList(context, component,
 				"columnClasses"));
 	}
 
 	public String getRowClassesAsJSArray(FacesContext context,
 			UIComponent component) {
 		return ScriptUtils.toScript(getClassesAsList(context, component,
 				"rowClasses"));
 	}
 
 	protected List getClassesAsList(FacesContext context,
 			UIComponent component, String attr) {
 
 		String value = (String) ((UIDataAdaptor) component).getAttributes()
 				.get(attr);
 		if (value != null && (value.length() != 0)) {
 			return Arrays.asList(value.split(","));
 		}
 		return null;
 	}
 }
