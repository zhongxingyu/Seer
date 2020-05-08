 /*
  * Smart GWT (GWT for SmartClient)
  * Copyright 2008 and beyond, Isomorphic Software, Inc.
  *
  * Smart GWT is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License version 3
  * as published by the Free Software Foundation.  Smart GWT is also
  * available under typical commercial license terms - see
  * http://smartclient.com/license
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  */
 
 package org.vaadin.smartgwt.server.form.fields;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.vaadin.smartgwt.server.core.DataClass;
 import org.vaadin.smartgwt.server.form.DynamicForm;
 import org.vaadin.smartgwt.server.form.fields.events.FormItemClickHandler;
 import org.vaadin.smartgwt.server.form.fields.events.FormItemIconClickEvent;
 import org.vaadin.smartgwt.server.form.fields.events.HasFormItemClickHandlers;
 
 import com.google.common.collect.Sets;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.ClientWidget;
 
 /**
  * Form item icon descriptor objects used by Form Items to specify the appearance and  behavior of icons displayed after
  * the item in the page flow.
  * @see com.smartgwt.client.widgets.form.fields.FormItem#getIcons
  */
 @ClientWidget(org.vaadin.smartgwt.client.ui.form.fields.VFormItemIcon.class)
 public class FormItemIcon extends DataClass implements HasFormItemClickHandlers {
 	private Set<FormItemClickHandler> formItemClickHandlers = Sets.newHashSet();
 
 	/**
 	 * If set, this property determines the height of this icon in px.      If unset the form item's <code>iconHeight</code>
 	 * property will be used instead.
 	 *
 	 * @param height height Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setIconHeight
 	 */
 	public void setHeight(Integer height) {
 		setAttribute("height", height);
 	}
 
 	/**
 	 * Identifier for this form item icon. This identifier (if set) should be unique within this form item and may be used to
 	 * get a pointer to the icon object via {@link com.smartgwt.client.widgets.form.fields.FormItem#getIcon FormItem.getIcon}.
 	 *
 	 * @param name name Default value is null
 	 */
 	public void setName(String name) {
 		setAttribute("name", name);
 	}
 
 	/**
 	 * Identifier for this form item icon. This identifier (if set) should be unique within this form item and may be used to
 	 * get a pointer to the icon object via {@link com.smartgwt.client.widgets.form.fields.FormItem#getIcon FormItem.getIcon}.
 	 *
 	 *
 	 * @return String
 	 */
 	public String getName() {
 		return getAttributeAsString("name");
 	}
 
 	/**
 	 * If <code>icon.neverDisable</code> is true, when this form item is disabled, the  icon will remain enabled.  Note that
 	 * disabling the entire form will disable all items, together with their  icons including those marked as neverDisable -
 	 * this property only has an effect  if the form is enabled and a specific item is disabled within it.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param neverDisable neverDisable Default value is null
 	 */
 	public void setNeverDisable(Boolean neverDisable) {
 		setAttribute("neverDisable", neverDisable);
 	}
 
 	/**
 	 * If <code>icon.neverDisable</code> is true, when this form item is disabled, the  icon will remain enabled.  Note that
 	 * disabling the entire form will disable all items, together with their  icons including those marked as neverDisable -
 	 * this property only has an effect  if the form is enabled and a specific item is disabled within it.
 	 *
 	 *
 	 * @return Boolean
 	 */
 	public Boolean getNeverDisable() {
 		return getAttributeAsBoolean("neverDisable");
 	}
 
 	/**
 	 * If set, this property will be displayed as a prompt (and tooltip text) for this form item icon. <P> If unset the form
 	 * item's <code>iconPrompt</code> property will be used instead.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param prompt prompt Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setIconPrompt
 	 */
 	public void setPrompt(String prompt) {
 		setAttribute("prompt", prompt);
 	}
 
 	/**
 	 * If set, this property will be displayed as a prompt (and tooltip text) for this form item icon. <P> If unset the form
 	 * item's <code>iconPrompt</code> property will be used instead.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#getIconPrompt
 	 */
 	public String getPrompt() {
 		return getAttributeAsString("prompt");
 	}
 
 	/**
 	 * Should this icon's image switch to the appropriate "focused" source when the user puts focus on the form item or icon?
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param showFocused showFocused Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setShowFocusedIcons
 	 * @see com.smartgwt.client.widgets.form.fields.FormItemIcon#setShowFocusedWithItem
 	 */
 	public void setShowFocused(Boolean showFocused) {
 		setAttribute("showFocused", showFocused);
 	}
 
 	/**
 	 * Should this icon's image switch to the appropriate "focused" source when the user puts focus on the form item or icon?
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#getShowFocusedIcons
 	 * @see com.smartgwt.client.widgets.form.fields.FormItemIcon#getShowFocusedWithItem
 	 */
 	public Boolean getShowFocused() {
 		return getAttributeAsBoolean("showFocused");
 	}
 
 	/**
 	 * If this icon will be updated to show focus (see {@link
 	 * com.smartgwt.client.widgets.form.fields.FormItemIcon#getShowFocused showFocused},  {@link
 	 * com.smartgwt.client.widgets.form.fields.FormItem#getShowFocusedIcons showFocusedIcons}), this property governs whether
 	 * the focused state should be shown when the item as a whole receives focus or just if the icon receives focus. If this
 	 * property is unset, default behavior is to show focused state when the item receives focus.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param showFocusedWithItem showFocusedWithItem Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setShowFocusedIcons
 	 * @see com.smartgwt.client.widgets.form.fields.FormItemIcon#setShowFocused
 	 */
 	public void setShowFocusedWithItem(Boolean showFocusedWithItem) {
 		setAttribute("showFocusedWithItem", showFocusedWithItem);
 	}
 
 	/**
 	 * If this icon will be updated to show focus (see {@link
 	 * com.smartgwt.client.widgets.form.fields.FormItemIcon#getShowFocused showFocused},  {@link
 	 * com.smartgwt.client.widgets.form.fields.FormItem#getShowFocusedIcons showFocusedIcons}), this property governs whether
 	 * the focused state should be shown when the item as a whole receives focus or just if the icon receives focus. If this
 	 * property is unset, default behavior is to show focused state when the item receives focus.
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#getShowFocusedIcons
 	 * @see com.smartgwt.client.widgets.form.fields.FormItemIcon#getShowFocused
 	 */
 	public Boolean getShowFocusedWithItem() {
 		return getAttributeAsBoolean("showFocusedWithItem");
 	}
 
 	/**
 	 * Should this icon's image switch to the appropriate "over" source when the user rolls over or focuses on the icon?
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param showOver showOver Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setShowOverIcons
 	 */
 	public void setShowOver(Boolean showOver) {
 		setAttribute("showOver", showOver);
 	}
 
 	/**
 	 * Should this icon's image switch to the appropriate "over" source when the user rolls over or focuses on the icon?
 	 *
 	 *
 	 * @return Boolean
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#getShowOverIcons
 	 */
 	public Boolean getShowOver() {
 		return getAttributeAsBoolean("showOver");
 	}
 
 	/**
 	 * If set, this property determines this icon's image source. If unset the form item's <code>defaultIconSrc</code> property
 	 * will be used instead.<br> As with <code>defaultIconSrc</code> this URL will be modified by adding "_Over" or "_Disabled"
 	 * if appropriate to show the icons over or disabled state.
 	 *
 	 * @param src src Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setDefaultIconSrc
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#form_details_icons" target="examples">Icons Example</a>
 	 */
 	public void setSrc(String src) {
 		setAttribute("src", src);
 	}
 
 	/**
 	 * If set, this property determines this icon's image source. If unset the form item's <code>defaultIconSrc</code> property
 	 * will be used instead.<br> As with <code>defaultIconSrc</code> this URL will be modified by adding "_Over" or "_Disabled"
 	 * if appropriate to show the icons over or disabled state.
 	 *
 	 *
 	 * @return String
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#getDefaultIconSrc
 	 * @see <a href="http://www.smartclient.com/smartgwt/showcase/#form_details_icons" target="examples">Icons Example</a>
 	 */
 	public String getSrc() {
 		return getAttributeAsString("src");
 	}
 
 	/**
 	 * TabIndex for this formItemIcon. <P> Set to -1 to remove the icon from the tab order, but be cautious doing so: if the
 	 * icon triggers important application functionality that cannot otherwise be accessed via the keyboard, it would be a
 	 * violation of accessibility standard to remove the icon from the tab order. <P> Any usage other than setting to -1 is
 	 * extremely advanced in the same way as using {@link com.smartgwt.client.widgets.form.fields.FormItem#getGlobalTabIndex
 	 * globalTabIndex}.
 	 * <p><b>Note : </b> This is an advanced setting</p>
 	 *
 	 * @param tabIndex tabIndex Default value is null
 	 */
 	public void setTabIndex(Integer tabIndex) {
 		setAttribute("tabIndex", tabIndex);
 	}
 
 	/**
 	 * TabIndex for this formItemIcon. <P> Set to -1 to remove the icon from the tab order, but be cautious doing so: if the
 	 * icon triggers important application functionality that cannot otherwise be accessed via the keyboard, it would be a
 	 * violation of accessibility standard to remove the icon from the tab order. <P> Any usage other than setting to -1 is
 	 * extremely advanced in the same way as using {@link com.smartgwt.client.widgets.form.fields.FormItem#getGlobalTabIndex
 	 * globalTabIndex}.
 	 *
 	 *
 	 * @return Integer
 	 */
 	public Integer getTabIndex() {
 		return getAttributeAsInt("tabIndex");
 	}
 
 	/**
 	 * If set, this property determines the width of this icon in px.      If unset the form item's <code>iconWidth</code>
 	 * property will be used instead.
 	 *
 	 * @param width width Default value is null
 	 * @see com.smartgwt.client.widgets.form.fields.FormItem#setIconWidth
 	 */
 	public void setWidth(Integer width) {
 		setAttribute("width", width);
 	}
 
 	protected Set<FormItemClickHandler> getFormItemClickHandlers() {
 		return Sets.newHashSet(formItemClickHandlers);
 	}
 
 	@Override
 	public HandlerRegistration addFormItemClickHandler(final FormItemClickHandler handler) {
 		formItemClickHandlers.add(handler);
 		return new HandlerRegistration() {
 			@Override
 			public void removeHandler() {
 				formItemClickHandlers.remove(handler);
 			}
 		};
 	}
 
 	@Override
 	public void paintContent(PaintTarget target) throws PaintException {
 		if (!formItemClickHandlers.isEmpty()) {
			target.addAttribute("*hasFormItemClickHandlers", true);
 		}
 
 		super.paintContent(target);
 	}
 
 	@Override
 	public void changeVariables(Object source, Map<String, Object> variables) {
 		final Map<String, Object> formItemIconClickEventVariables = filterComplexVariable(variables, "formItemIconClickEvent");
 
 		if (!formItemIconClickEventVariables.isEmpty()) {
 			final DynamicForm form = (DynamicForm) formItemIconClickEventVariables.get("form");
 			final FormItem item = (FormItem) formItemIconClickEventVariables.get("item");
 			final FormItemIcon icon = (FormItemIcon) formItemIconClickEventVariables.get("icon");
 			final FormItemIconClickEvent event = new FormItemIconClickEvent(form, item, icon);
 			for (FormItemClickHandler handler : formItemClickHandlers) {
 				handler.onFormItemClick(event);
 			}
 		}
 
 		super.changeVariables(source, variables);
 	}
 
 	private static Map<String, Object> filterComplexVariable(Map<String, Object> variables, String complexVariableName) {
 		final Map<String, Object> complexVariables = new HashMap<String, Object>();
 
 		for (Entry<String, Object> variable : variables.entrySet()) {
 			if (variable.getKey().startsWith(complexVariableName + ".")) {
 				complexVariables.put(variable.getKey().replace(complexVariableName + ".", ""), variable.getValue());
 			}
 		}
 
 		return complexVariables;
 	}
 }
