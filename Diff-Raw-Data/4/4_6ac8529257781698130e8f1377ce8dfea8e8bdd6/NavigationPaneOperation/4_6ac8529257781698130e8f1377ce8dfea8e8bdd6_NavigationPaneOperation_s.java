 /**
  * Copyright (c) 2009 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle Corporation - initial API and implementation
  */
 package org.eclipse.jst.jsf.apache.trinidad.tagsupport.converter.operations;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jst.jsf.apache.trinidad.tagsupport.ITrinidadConstants;
 import org.eclipse.jst.jsf.apache.trinidad.tagsupport.Messages;
 import org.eclipse.jst.pagedesigner.converter.ConvertPosition;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * ITransformOperation implementation specifically for the "navigationPane"
  * JSF Element.
  * 
  * <br><b>Note:</b> requires ITransformOperation.setTagConverterContext(...) to
  * have been called to provide a valid ITagConverterContext instance prior to
  * a call to the transform(...) method.
  * 
  * @author Ian Trimble - Oracle
  */
 public class NavigationPaneOperation extends AbstractTrinidadTransformOperation {
 
 	private static final String STYLECLASS_OUTERDIV = "af_navigationPane"; //$NON-NLS-1$
 
 	private static final String STYLECLASS_BAR_OUTERDIV = "af_navigationPane_bar"; //$NON-NLS-1$
 	private static final String STYLECLASS_BAR_TABLE_INACTIVE_ENABLED = "af_navigationPane_bar-inactive-enabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BAR_TABLE_ACTIVE_ENABLED = "af_navigationPane_bar-active-enabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BAR_TABLE_INACTIVE_DISABLED = "af_navigationPane_bar-inactive-disabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BAR_TABLE_ACTIVE_DISABLED = "af_navigationPane_bar-active-disabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BAR_DIV_CONTENT = "af_navigationPane_bar-content"; //$NON-NLS-1$
 	private static final String STYLECLASS_BAR_DIV_SEPARATOR = "af_navigationPane_bar-separator"; //$NON-NLS-1$
 
 	private static final String STYLECLASS_BUTTONS_TABLE_INACTIVE_ENABLED = "af_navigationPane_buttons-inactive-enabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BUTTONS_TABLE_ACTIVE_ENABLED = "af_navigationPane_buttons-active-enabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BUTTONS_TABLE_INACTIVE_DISABLED = "af_navigationPane_buttons-inactive-disabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BUTTONS_TABLE_ACTIVE_DISABLED = "af_navigationPane_buttons-active-disabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_BUTTONS_DIV_CONTENT = "af_navigationPane_buttons-content"; //$NON-NLS-1$
 	private static final String STYLECLASS_BUTTONS_DIV_SEPARATOR = "af_navigationPane_buttons-separator"; //$NON-NLS-1$
 
 	private static final String STYLECLASS_CHOICE_SELECT = "af_navigationPane_choice-options"; //$NON-NLS-1$
 	private static final String STYLECLASS_CHOICE_BUTTON = "af_navigationPane_choice-button"; //$NON-NLS-1$
 
 	private static final String STYLECLASS_LIST_TABLE_INACTIVE_ENABLED = "af_navigationPane_list-inactive-enabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_LIST_TABLE_ACTIVE_ENABLED = "af_navigationPane_list-active-enabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_LIST_TABLE_INACTIVE_DISABLED = "af_navigationPane_list-inactive-disabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_LIST_TABLE_ACTIVE_DISABLED = "af_navigationPane_list-active-disabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_LIST_TD_BULLET = "af_navigationPane_list-bullet"; //$NON-NLS-1$
 	private static final String STYLECLASS_LIST_DIV_CONTENT = "af_navigationPane_list-content"; //$NON-NLS-1$
 
	private static final String STYLECLASS_TABS_OUTERDIV = "af_navigationPane_tabs"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TABLE_INACTIVE_ENABLED = "af_navigationPane_tabs-inactive"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TABLE_ACTIVE_ENABLED = "af_navigationPane_tabs-active"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TABLE_INACTIVE_DISABLED = "af_navigationPane_tabs-inactive p_AFDisabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TABLE_ACTIVE_DISABLED = "af_navigationPane_tabs-active p_AFDisabled"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_START = "af_navigationPane_tabs-start"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_START_JOIN = "af_navigationPane_tabs-start-join"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_START_JOINFROMACTIVE = "af_navigationPane_tabs-start-join-from-active"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_START_JOINFROMINACTIVE = "af_navigationPane_tabs-start-join-from-inactive"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_BOTTOM_START = "af_navigationPane_tabs-bottom-start"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_MID = "af_navigationPane_tabs-mid"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_BOTTOM_MID = "af_navigationPane_tabs-bottom-mid"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_END = "af_navigationPane_tabs-end"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_END_JOINTOINACTIVE = "af_navigationPane_tabs-end-join-to-inactive"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_BOTTOM_END = "af_navigationPane_tabs-bottom-end"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_TD_BOTTOM_END_JOIN = "af_navigationPane_tabs-bottom-end-join"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_DIV_BOTTOM_START_CONTENT = "af_navigationPane_tabs-bottom-start-content"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_DIV_BOTTOM_MID_CONTENT = "af_navigationPane_tabs-bottom-mid-content"; //$NON-NLS-1$
 	private static final String STYLECLASS_TABS_DIV_BOTTOM_END_CONTENT = "af_navigationPane_tabs-bottom-end-content"; //$NON-NLS-1$
 	
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.operations.AbstractTransformOperation#transform(org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	@Override
 	public Element transform(Element srcElement, Element curElement) {
 		Element div = null;
 		String hint = getHint(srcElement);
 		if (hint.equalsIgnoreCase("bar")) { //$NON-NLS-1$
 			div = transformAsBar(srcElement);
 		} else if (hint.equalsIgnoreCase("buttons")) { //$NON-NLS-1$
 			div = transformAsButtons(srcElement);
 		} else if (hint.equalsIgnoreCase("choice")) { //$NON-NLS-1$
 			div = transformAsChoice(srcElement);
 		} else if (hint.equalsIgnoreCase("list")) { //$NON-NLS-1$
 			div = transformAsList(srcElement);
 		} else if (hint.equalsIgnoreCase("tabs")) { //$NON-NLS-1$
 			div = transformAsTabs(srcElement);
 		} else {
 			div = transformAsBar(srcElement);
 		}
 		return div;
 	}
 
 	private Element transformAsBar(Element srcElement) {
 		return transformAsBarOrButtons(srcElement, true);
 	}
 
 	private Element transformAsButtons(Element srcElement) {
 		return transformAsBarOrButtons(srcElement, false);
 	}
 
 	private Element transformAsBarOrButtons(Element srcElement, boolean isBar) {
 		Element div = null;
 		String styleClass_outerDiv;
 		String styleClass_tableInactiveEnabled;
 		String styleClass_tableActiveEnabled;
 		String styleClass_tableInactiveDisabled;
 		String styleClass_tableActiveDisabled;
 		String styleClass_divContent;
 		String styleClass_divSeparator;
 		if (isBar) {
 			styleClass_outerDiv = STYLECLASS_BAR_OUTERDIV;
 			styleClass_tableInactiveEnabled = STYLECLASS_BAR_TABLE_INACTIVE_ENABLED;
 			styleClass_tableActiveEnabled = STYLECLASS_BAR_TABLE_ACTIVE_ENABLED;
 			styleClass_tableInactiveDisabled = STYLECLASS_BAR_TABLE_INACTIVE_DISABLED;
 			styleClass_tableActiveDisabled = STYLECLASS_BAR_TABLE_ACTIVE_DISABLED;
 			styleClass_divContent = STYLECLASS_BAR_DIV_CONTENT;
 			styleClass_divSeparator = STYLECLASS_BAR_DIV_SEPARATOR;
 		} else {
 			styleClass_outerDiv = STYLECLASS_OUTERDIV;
 			styleClass_tableInactiveEnabled = STYLECLASS_BUTTONS_TABLE_INACTIVE_ENABLED;
 			styleClass_tableActiveEnabled = STYLECLASS_BUTTONS_TABLE_ACTIVE_ENABLED;
 			styleClass_tableInactiveDisabled = STYLECLASS_BUTTONS_TABLE_INACTIVE_DISABLED;
 			styleClass_tableActiveDisabled = STYLECLASS_BUTTONS_TABLE_ACTIVE_DISABLED;
 			styleClass_divContent = STYLECLASS_BUTTONS_DIV_CONTENT;
 			styleClass_divSeparator = STYLECLASS_BUTTONS_DIV_SEPARATOR;
 		}
 		div = createElement("div"); //$NON-NLS-1$
 		setClassAndStyleAttributes(srcElement, styleClass_outerDiv, div);
 		List<Element> childCmdNavItems = getChildCmdNavItems(srcElement);
 		int index = 0;
 		int numChildCmdNavItems = childCmdNavItems.size();
 		if (numChildCmdNavItems > 0) {
 			for (Element childCmdNavItem: childCmdNavItems) {
 				String styleClass =
 					determineStyleClass(
 							childCmdNavItem,
 							styleClass_tableActiveEnabled,
 							styleClass_tableActiveDisabled,
 							styleClass_tableInactiveEnabled,
 							styleClass_tableInactiveDisabled);
 				Element tbody = appendTableAndTBody(div, true, styleClass);
 				Element tr = appendChildElement("tr", tbody); //$NON-NLS-1$
 				Element contentTD = appendChildElement("td", tr); //$NON-NLS-1$
 				Element contentDiv = appendChildElement("div", contentTD); //$NON-NLS-1$
 				appendAttribute(contentDiv, "class", styleClass_divContent); //$NON-NLS-1$
 				tagConverterContext.addChild(
 						childCmdNavItem,
 						new ConvertPosition(contentDiv, 0));
 				if (index++ < numChildCmdNavItems - 1) {
 					Element separatorTD = appendChildElement("td", tr); //$NON-NLS-1$
 					Element separatorDiv = appendChildElement("div", separatorTD); //$NON-NLS-1$
 					appendAttribute(separatorDiv, "class", styleClass_divSeparator); //$NON-NLS-1$
 					appendChildText("|", separatorDiv); //$NON-NLS-1$
 				}
 			}
 		} else {
 			setEmptyNavPaneMessage(div);
 		}
 		return div;
 	}
 
 	private Element transformAsChoice(Element srcElement) {
 		Element div = null;
 		div = createElement("div"); //$NON-NLS-1$
 		setClassAndStyleAttributes(srcElement, STYLECLASS_OUTERDIV, div);
 		List<Element> childCmdNavItems = getChildCmdNavItems(srcElement);
 		if (childCmdNavItems.size() > 0) {
 			Element select = appendChildElement("select", div); //$NON-NLS-1$
 			appendAttribute(select, "class", STYLECLASS_CHOICE_SELECT); //$NON-NLS-1$
 			for (Element childCmdNavItem: childCmdNavItems) {
 				if (!CommandNavigationItemOperation.isDisabled(childCmdNavItem)) {
 					Element option = appendChildElement("option", select); //$NON-NLS-1$
 					if (CommandNavigationItemOperation.isSelected(childCmdNavItem)) {
 						appendAttribute(option, "selected", "selected"); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 					appendChildText(CommandNavigationItemOperation.getText(childCmdNavItem), option);
 				}
 			}
 			Element span = appendChildElement("span", div); //$NON-NLS-1$
 			appendAttribute(span, "style", "width: 5px;"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendChildText(" ", span); //$NON-NLS-1$
 			Element button = appendChildElement("button", div); //$NON-NLS-1$
 			appendAttribute(button, "class", STYLECLASS_CHOICE_BUTTON); //$NON-NLS-1$
 			appendAttribute(button, "type", "button"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendChildText("Go", button); //$NON-NLS-1$
 		} else {
 			setEmptyNavPaneMessage(div);
 		}
 		return div;
 	}
 
 	private Element transformAsList(Element srcElement) {
 		Element div = null;
 		div = createElement("div"); //$NON-NLS-1$
 		setClassAndStyleAttributes(srcElement, STYLECLASS_OUTERDIV, div);
 		List<Element> childCmdNavItems = getChildCmdNavItems(srcElement);
 		if (childCmdNavItems.size() > 0) {
 			for (Element childCmdNavItem: childCmdNavItems) {
 				String styleClass =
 					determineStyleClass(
 							childCmdNavItem,
 							STYLECLASS_LIST_TABLE_ACTIVE_ENABLED,
 							STYLECLASS_LIST_TABLE_ACTIVE_DISABLED,
 							STYLECLASS_LIST_TABLE_INACTIVE_ENABLED,
 							STYLECLASS_LIST_TABLE_INACTIVE_DISABLED);
 				Element tbody = appendTableAndTBody(div, false, styleClass);
 				Element tr = appendChildElement("tr", tbody); //$NON-NLS-1$
 				Element bulletTD = appendChildElement("td", tr); //$NON-NLS-1$
 				appendAttribute(bulletTD, "class", STYLECLASS_LIST_TD_BULLET); //$NON-NLS-1$
 				Element bulletDiv = appendChildElement("div", bulletTD); //$NON-NLS-1$
 				appendChildText(" ", bulletDiv); //$NON-NLS-1$
 				Element contentTD = appendChildElement("td", tr); //$NON-NLS-1$
 				Element contentDiv = appendChildElement("div", contentTD); //$NON-NLS-1$
 				appendAttribute(contentDiv, "class", STYLECLASS_LIST_DIV_CONTENT); //$NON-NLS-1$
 				tagConverterContext.addChild(
 						childCmdNavItem,
 						new ConvertPosition(contentDiv, 0));
 			}
 		} else {
 			setEmptyNavPaneMessage(div);
 		}
 		return div;
 	}
 
 	private Element transformAsTabs(Element srcElement) {
 		Element div = null;
 		div = createElement("div"); //$NON-NLS-1$
		setClassAndStyleAttributes(srcElement, STYLECLASS_TABS_OUTERDIV, div);
 		List<Element> childCmdNavItems = getChildCmdNavItems(srcElement);
 		int index = 0;
 		int numChildCmdNavItems = childCmdNavItems.size();
 		int selectedTabIndex = determineSelectedTabIndex(childCmdNavItems);
 		if (numChildCmdNavItems > 0) {
 			for (Element childCmdNavItem: childCmdNavItems) {
 				String tableStyleClass =
 					determineStyleClass(
 							childCmdNavItem,
 							STYLECLASS_TABS_TABLE_ACTIVE_ENABLED,
 							STYLECLASS_TABS_TABLE_ACTIVE_DISABLED,
 							STYLECLASS_TABS_TABLE_INACTIVE_ENABLED,
 							STYLECLASS_TABS_TABLE_INACTIVE_DISABLED);
 				Element outerTBody = appendTableAndTBody(div, true, ""); //$NON-NLS-1$
 				Element outerTR = appendChildElement("tr", outerTBody); //$NON-NLS-1$
 				Element outerTD = appendChildElement("td", outerTR); //$NON-NLS-1$
 				Element innerTBody = appendTableAndTBody(outerTD, false, tableStyleClass);
 				appendStartColumn(innerTBody, index, selectedTabIndex);
 				outerTD = appendChildElement("td", outerTR); //$NON-NLS-1$
 				innerTBody = appendTableAndTBody(outerTD, false, tableStyleClass);
 				appendMidColumn(innerTBody, childCmdNavItem);
 				if (index + 1 != selectedTabIndex) {
 					outerTD = appendChildElement("td", outerTR); //$NON-NLS-1$
 					innerTBody = appendTableAndTBody(outerTD, false, tableStyleClass);
 					appendEndColumn(innerTBody, index, numChildCmdNavItems);
 				}
 				index++;
 			}
 		} else {
 			setEmptyNavPaneMessage(div);
 		}
 		return div;
 	}
 
 	private List<Element> getChildCmdNavItems(Element srcElement) {
 		List<Element> childCmdNavItems = new ArrayList<Element>();
 		NodeList childElements = srcElement.getElementsByTagNameNS(ITrinidadConstants.URI_CORE, "commandNavigationItem"); //$NON-NLS-1$$
 		if (childElements != null && childElements.getLength() > 0) {
 			for (int i = 0, len = childElements.getLength(); i < len; i++) {
 				childCmdNavItems.add((Element)childElements.item(i));
 			}
 		} else {
 			Element nodeStampFacet = getChildFacetByName(srcElement, "nodeStamp"); //$NON-NLS-1$
 			if (nodeStampFacet != null) {
 				childElements = nodeStampFacet.getElementsByTagNameNS(ITrinidadConstants.URI_CORE, "commandNavigationItem"); //$NON-NLS-1$
 				if (childElements != null && childElements.getLength() > 0) {
 					for (int i = 0, len = childElements.getLength(); i < len; i++) {
 						childCmdNavItems.add((Element)childElements.item(i));
 					}
 				}
 			}
 		}
 		return childCmdNavItems;
 	}
 
 	private String getHint(Element srcElement) {
 		String hint = srcElement.getAttribute("hint"); //$NON-NLS-1$
 		return hint != null ? hint : "bar"; //$NON-NLS-1$
 	}
 
 	private void setClassAndStyleAttributes(Element srcElement, String baseStyleClass, Element curElement) {
 		String styleClass = calculateStyleClass(baseStyleClass, srcElement);
 		if (styleClass != null) {
 			appendAttribute(curElement, "class", styleClass); //$NON-NLS-1$
 		}
 		String style = srcElement.getAttribute("inlineStyle"); //$NON-NLS-1$
 		if (style != null) {
 			appendAttribute(curElement, "style", style); //$NON-NLS-1$
 		}
 	}
 
 	private void setEmptyNavPaneMessage(Element curElement) {
 		appendAttribute(curElement, "style", ITrinidadConstants.STYLE_EMPTYELEMENT); //$NON-NLS-1$
 		appendChildText(Messages.NavigationPaneOperation_EmptyNavigationPaneTag, curElement);
 	}
 
 	private Element appendTableAndTBody(Element parentElement, boolean isInline, String styleClass) {
 		Element table = appendChildElement("table", parentElement); //$NON-NLS-1$
 		appendAttribute(table, "cellpadding", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(table, "cellspacing", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(table, "border", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		if (isInline) {
 			appendAttribute(table, "style", "display: inline;"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		appendAttribute(table, "class", styleClass); //$NON-NLS-1$
 		Element tbody = appendChildElement("tbody", table); //$NON-NLS-1$
 		return tbody;
 	}
 
 	private String determineStyleClass(
 			Element cmdNavItem,
 			String styleClass_ActiveEnabled,
 			String styleClass_ActiveDisabled,
 			String styleClass_InactiveEnabled,
 			String styleClass_InactiveDisabled) {
 		String styleClass;
 		if (CommandNavigationItemOperation.isSelected(cmdNavItem)) {
 			if (!CommandNavigationItemOperation.isDisabled(cmdNavItem)) {
 				styleClass = styleClass_ActiveEnabled;
 			} else {
 				styleClass = styleClass_ActiveDisabled;
 			}
 		} else {
 			if (!CommandNavigationItemOperation.isDisabled(cmdNavItem)) {
 				styleClass = styleClass_InactiveEnabled;
 			} else {
 				styleClass = styleClass_InactiveDisabled;
 			}
 		}
 		return styleClass;
 	}
 
 	private int determineSelectedTabIndex(List<Element> childCmdNavItems) {
 		int selectedTabIndex = -2;
 		int curTabIndex = 0;
 		for (Element childCmdNavItem: childCmdNavItems) {
 			if (CommandNavigationItemOperation.isSelected(childCmdNavItem)) {
 				selectedTabIndex = curTabIndex;
 				break;
 			}
 			curTabIndex++;
 		}
 		return selectedTabIndex;
 	}
 
 	private void appendStartColumn(Element parentTBody, int curTabIndex, int selectedTabIndex) {
 		String topTDStyleClass;
 		String bottomTDStyleClass;
 		String divStyleClass;
 		if (curTabIndex == 0) {
 			topTDStyleClass = STYLECLASS_TABS_TD_START;
 			bottomTDStyleClass = STYLECLASS_TABS_TD_BOTTOM_START;
 			divStyleClass = STYLECLASS_TABS_DIV_BOTTOM_START_CONTENT;
 		} else {
 			if (curTabIndex == selectedTabIndex) {
 				topTDStyleClass = STYLECLASS_TABS_TD_START_JOIN;
 				bottomTDStyleClass = STYLECLASS_TABS_TD_BOTTOM_START;
 				divStyleClass = STYLECLASS_TABS_DIV_BOTTOM_START_CONTENT;
 			} else {
 				if (curTabIndex == selectedTabIndex + 1) {
 					topTDStyleClass = STYLECLASS_TABS_TD_START_JOINFROMACTIVE;
 				} else {
 					topTDStyleClass = STYLECLASS_TABS_TD_START_JOINFROMINACTIVE;
 				}
 				bottomTDStyleClass = STYLECLASS_TABS_TD_BOTTOM_END;
 				divStyleClass = STYLECLASS_TABS_DIV_BOTTOM_MID_CONTENT;
 			}
 		}
 		Element topTR = appendChildElement("tr", parentTBody); //$NON-NLS-1$
 		Element topTD = appendChildElement("td", topTR); //$NON-NLS-1$
 		appendAttribute(topTD, "class", topTDStyleClass); //$NON-NLS-1$
 		Element bottomTR = appendChildElement("tr", parentTBody); //$NON-NLS-1$
 		Element bottomTD = appendChildElement("td", bottomTR); //$NON-NLS-1$
 		appendAttribute(bottomTD, "class", bottomTDStyleClass); //$NON-NLS-1$
 		Element div = appendChildElement("div", bottomTD); //$NON-NLS-1$
 		appendAttribute(div, "class", divStyleClass); //$NON-NLS-1$
 	}
 
 	private void appendMidColumn(Element parentTBody, Element cmdNavItem) {
 		Element topTR = appendChildElement("tr", parentTBody); //$NON-NLS-1$
 		Element topTD = appendChildElement("td", topTR); //$NON-NLS-1$
 		appendAttribute(topTD, "class", STYLECLASS_TABS_TD_MID); //$NON-NLS-1$
 		tagConverterContext.addChild(cmdNavItem, new ConvertPosition(topTD, 0));
 		Element bottomTR = appendChildElement("tr", parentTBody); //$NON-NLS-1$
 		Element bottomTD = appendChildElement("td", bottomTR); //$NON-NLS-1$
 		appendAttribute(bottomTD, "class", STYLECLASS_TABS_TD_BOTTOM_MID); //$NON-NLS-1$
 		Element div = appendChildElement("div", bottomTD); //$NON-NLS-1$
 		appendAttribute(div, "class", STYLECLASS_TABS_DIV_BOTTOM_MID_CONTENT); //$NON-NLS-1$
 	}
 
 	private void appendEndColumn(Element parentTBody, int curTabIndex, int numTabs) {
 		String topTDStyleClass;
 		String bottomTDStyleClass;
 		if (curTabIndex < numTabs - 1) {
 			topTDStyleClass = STYLECLASS_TABS_TD_END_JOINTOINACTIVE;
 			bottomTDStyleClass = STYLECLASS_TABS_TD_BOTTOM_END_JOIN;
 		} else {
 			topTDStyleClass = STYLECLASS_TABS_TD_END;
 			bottomTDStyleClass = STYLECLASS_TABS_TD_BOTTOM_END;
 		}
 		Element topTR = appendChildElement("tr", parentTBody); //$NON-NLS-1$
 		Element topTD = appendChildElement("td", topTR); //$NON-NLS-1$
 		appendAttribute(topTD, "class", topTDStyleClass); //$NON-NLS-1$
 		Element bottomTR = appendChildElement("tr", parentTBody); //$NON-NLS-1$
 		Element bottomTD = appendChildElement("td", bottomTR); //$NON-NLS-1$
 		appendAttribute(bottomTD, "class", bottomTDStyleClass); //$NON-NLS-1$
 		Element div = appendChildElement("div", bottomTD); //$NON-NLS-1$
 		appendAttribute(div, "class", STYLECLASS_TABS_DIV_BOTTOM_END_CONTENT); //$NON-NLS-1$
 	}
 
 }
