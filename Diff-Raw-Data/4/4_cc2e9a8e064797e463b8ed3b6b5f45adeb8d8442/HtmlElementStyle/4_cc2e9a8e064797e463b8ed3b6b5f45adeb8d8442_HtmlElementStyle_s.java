 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsnative;
 
 import org.eclipse.vjet.dsf.jsnative.anno.Alias;
 import org.eclipse.vjet.dsf.jsnative.anno.BrowserSupport;
 import org.eclipse.vjet.dsf.jsnative.anno.BrowserType;
 import org.eclipse.vjet.dsf.jsnative.anno.Function;
 import org.eclipse.vjet.dsf.jsnative.anno.JsMetatype;
 import org.eclipse.vjet.dsf.jsnative.anno.JstMultiReturn;
 import org.eclipse.vjet.dsf.jsnative.anno.OverLoadFunc;
 import org.eclipse.vjet.dsf.jsnative.anno.Property;
 import org.mozilla.mod.javascript.IWillBeScriptable;
 
 /**
  * This interface is a synthesized style type for browser dom element. It is not
  * a true node or element. It acts as a proxy to set style on HtmlElement.
  */
 @Alias("HTMLElementStyle")
 @JsMetatype
 public interface HtmlElementStyle extends IWillBeScriptable {
 
 	@Property
 	String getVisibility();
 
 	@Property
 	void setVisibility(String value);
 
 	@Property
 	String getWidth();
 
 	@Property
 	void setWidth(String width);
 
 	@Property
 	String getHeight();
 
 	@Property
 	void setHeight(String height);
 
 	@Property
 	String getColor();
 
 	@Property
 	void setColor(String color);
 
 	@Property
 	String getContent();
 
 	@Property
 	void setContent(String content);
 
 	@Property
 	String getCounterIncrement();
 
 	@Property
 	void setCounterIncrement(String counterIncrement);
 
 	@Property
 	String getCounterReset();
 
 	@Property
 	void setCounterReset(String counterReset);
 
 	@Property
 	String getCssFloat();
 
 	@Property
 	void setCssFloat(String cssFloat);
 
 	@Property
 	String getBackgroundColor();
 
 	@Property
 	void setBackgroundColor(String color);
 
 	@Property
 	String getFontStyle();
 
 	@Property
 	void setFontStyle(String fontStyle);
 
 	@Property
 	String getFontWeight();
 
 	@Property
 	void setFontWeight(String fontWeight);
 
 	@Property
 	String getDisplay();
 
 	@Property
 	void setDisplay(String display);
 
 	@Property
 	String getAccelerator();
 
 	@Property
 	void setAccelerator(String accelerator);
 
 	@Property
 	String getBackground();
 
 	@Property
 	void setBackground(String background);
 
 	@Property
 	String getBackgroundAttachment();
 
 	@Property
 	void setBackgroundAttachment(String backgroundAttachment);
 
 	@Property
 	String getBackgroundImage();
 
 	@Property
 	void setBackgroundImage(String backgroundImage);
 
 	@Property
 	String getBackgroundPosition();
 
 	@Property
 	void setBackgroundPosition(String backgroundPosition);
 
 	@Property
 	String getBackgroundPositionX();
 
 	@Property
 	void setBackgroundPositionX(String backgroundPositionX);
 
 	@Property
 	String getBackgroundPositionY();
 
 	@Property
 	void setBackgroundPositionY(String backgroundPositionY);
 
 	@Property
 	String getBackgroundRepeat();
 
 	@Property
 	void setBackgroundRepeat(String backgroundRepeat);
 
 	@Property
 	String getBehavior();
 
 	@Property
 	void setBehavior(String behavior);
 
 	@Property
 	String getBorder();
 
 	@Property
 	void setBorder(String border);
 
 	@Property
 	String getBorderBottom();
 
 	@Property
 	void setBorderBottom(String borderBottom);
 
 	@Property
 	String getBorderBottomColor();
 
 	@Property
 	void setBorderBottomColor(String borderBottomColor);
 
 	@Property
 	String getBorderBottomStyle();
 
 	@Property
 	void setBorderBottomStyle(String borderBottomStyle);
 
 	@Property
 	String getBorderBottomWidth();
 
 	@Property
 	void setBorderBottomWidth(String borderBottomWidth);
 
 	@Property
 	String getBorderColor();
 
 	@Property
 	void setBorderColor(String borderColor);
 
 	@Property
 	String getBorderLeft();
 
 	@Property
 	void setBorderLeft(String borderLeft);
 
 	@Property
 	String getBorderLeftColor();
 
 	@Property
 	void setBorderLeftColor(String borderLeftColor);
 
 	@Property
 	String getBorderLeftStyle();
 
 	@Property
 	void setBorderLeftStyle(String borderLeftStyle);
 
 	@Property
 	String getBorderLeftWidth();
 
 	@Property
 	void setBorderLeftWidth(String borderLeftWidth);
 
 	@Property
 	String getBorderRight();
 
 	@Property
 	void setBorderRight(String borderRight);
 
 	@Property
 	String getBorderRightColor();
 
 	@Property
 	void setBorderRightColor(String borderRightColor);
 
 	@Property
 	String getBorderRightStyle();
 
 	@Property
 	void setBorderRightStyle(String borderRightStyle);
 
 	@Property
 	String getBorderRightWidth();
 
 	@Property
 	void setBorderRightWidth(String borderRightWidth);
 
 	@Property
 	String getBorderStyle();
 
 	@Property
 	void setBorderStyle(String borderStyle);
 
 	@Property
 	String getBorderTop();
 
 	@Property
 	void setBorderTop(String borderTop);
 
 	@Property
 	String getBorderTopColor();
 
 	@Property
 	void setBorderTopColor(String borderTopColor);
 
 	@Property
 	String getBorderTopStyle();
 
 	@Property
 	void setBorderTopStyle(String borderTopStyle);
 
 	@Property
 	String getBorderTopWidth();
 
 	@Property
 	void setBorderTopWidth(String borderTopWidth);
 
 	@Property
 	String getBorderWidth();
 
 	@Property
 	void setBorderWidth(String borderWidth);
 
 	@Property
 	String getBottom();
 
 	@Property
 	void setBottom(String bottom);
 
 	@Property
 	String getClear();
 
 	@Property
 	void setClear(String clear);
 
 	@Property
 	String getClip();
 
 	@Property
 	void setClip(String clip);
 
 	@Property
 	String getCssText();
 
 	@Property
 	void setCssText(String cssText);
 
 	@Property
 	String getCursor();
 
 	@Property
 	void setCursor(String cursor);
 
 	@Property
 	String getDirection();
 
 	@Property
 	void setDirection(String direction);
 
 	@Property
 	String getFilter();
 
 	@Property
 	void setFilter(String filter);
 
 	@Property
 	String getFont();
 
 	@Property
 	void setFont(String font);
 
 	@Property
 	String getFontFamily();
 
 	@Property
 	void setFontFamily(String fontFamily);
 
 	@Property
 	String getFontSize();
 
 	@Property
 	void setFontSize(String fontSize);
 
 	@Property
 	String getFontSizeAdjust();
 
 	@Property
 	void setFontSizeAdjust(String fontSizeAdjust);
 
 	@Property
 	String getFontStretch();
 
 	@Property
 	void setFontStretch(String fontStretch);
 
 	@Property
 	String getFontVariant();
 
 	@Property
 	void setFontVariant(String fontVariant);
 
 	@Property
 	String getImeMode();
 
 	@Property
 	void setImeMode(String imeMode);
 
 	@Property
 	String getLayoutFlow();
 
 	@Property
 	void setLayoutFlow(String layoutFlow);
 
 	@Property
 	String getLayoutGrid();
 
 	@Property
 	void setLayoutGrid(String layoutGrid);
 
 	@Property
 	String getLayoutGridChar();
 
 	@Property
 	void setLayoutGridChar(String layoutGridChar);
 
 	@Property
 	String getLayoutGridLine();
 
 	@Property
 	void setLayoutGridLine(String layoutGridLine);
 
 	@Property
 	String getLayoutGridMode();
 
 	@Property
 	void setLayoutGridMode(String layoutGridMode);
 
 	@Property
 	String getLayoutGridType();
 
 	@Property
 	void setLayoutGridType(String layoutGridType);
 
 	@Property
 	String getLeft();
 
 	@Property
 	void setLeft(String left);
 
 	@Property
 	String getLetterSpacing();
 
 	@Property
 	void setLetterSpacing(String letterSpacing);
 
 	@Property
 	String getLineBreak();
 
 	@Property
 	void setLineBreak(String lineBreak);
 
 	@Property
 	String getLineHeight();
 
 	@Property
 	void setLineHeight(String lineHeight);
 
 	@Property
 	String getListStyle();
 
 	@Property
 	void setListStyle(String listStyle);
 
 	@Property
 	String getListStyleImage();
 
 	@Property
 	void setListStyleImage(String listStyleImage);
 
 	@Property
 	String getListStylePosition();
 
 	@Property
 	void setListStylePosition(String listStylePosition);
 
 	@Property
 	String getListStyleType();
 
 	@Property
 	void setListStyleType(String listStyleType);
 
 	@Property
 	String getMargin();
 
 	@Property
 	void setMargin(String margin);
 
 	@Property
 	String getMarginBottom();
 
 	@Property
 	void setMarginBottom(String marginBottom);
 
 	@Property
 	String getMarginLeft();
 
 	@Property
 	void setMarginLeft(String marginLeft);
 
 	@Property
 	String getMarginRight();
 
 	@Property
 	void setMarginRight(String marginRight);
 
 	@Property
 	String getMarginTop();
 
 	@Property
 	void setMarginTop(String marginTop);
 
 	@Property
 	String getMarkerOffset();
 
 	@Property
 	void setMarkerOffset(String markerOffset);
 
 	@Property
 	String getMarks();
 
 	@Property
 	void setMarks(String marks);
 
 	@Property
 	String getMaxHeight();
 
 	@Property
 	void setMaxHeight(String maxHeight);
 
 	@Property
 	String getMaxWidth();
 
 	@Property
 	void setMaxWidth(String maxWidth);
 
 	@Property
 	String getMinHeight();
 
 	@Property
 	void setMinHeight(String minHeight);
 
 	@Property
 	String getMinWidth();
 
 	@Property
 	void setMinWidth(String minWidth);
 
 	@Property
 	String getVerticalAlign();
 
 	@Property
 	void setVerticalAlign(String verticalAlign);
 
 	@Property
 	String getOutline();
 
 	@Property
 	void setOutline(String outline);
 
 	@Property
 	String getOutlineColor();
 
 	@Property
 	void setOutlineColor(String outlineColor);
 
 	@Property
 	String getOutlineStyle();
 
 	@Property
 	void setOutlineStyle(String outlineStyle);
 
 	@Property
 	String getOutlineWidth();
 
 	@Property
 	void setOutlineWidth(String outlineWidth);
 
 	@Property
 	String getPadding();
 
 	@Property
 	void setPadding(String padding);
 
 	@Property
 	String getPaddingBottom();
 
 	@Property
 	void setPaddingBottom(String paddingBottom);
 
 	@Property
 	String getPaddingLeft();
 
 	@Property
 	void setPaddingLeft(String paddingLeft);
 
 	@Property
 	String getPaddingRight();
 
 	@Property
 	void setPaddingRight(String paddingRight);
 
 	@Property
 	String getPaddingTop();
 
 	@Property
 	void setPaddingTop(String paddingTop);
 
 	@Property
 	String getPosition();
 
 	@Property
 	void setPosition(String position);
 
 	@Property
 	String getQuotes();
 
 	@Property
 	void setQuotes(String quotes);
 
 	@Property
 	String getRight();
 
 	@Property
 	void setRight(String right);
 
 	@Property
 	String getOverflow();
 
 	@Property
 	void setOverflow(String overflow);
 
 	@Property
 	String getTextAlign();
 
 	@Property
 	void setTextAlign(String textAlign);
 
 	@Property
 	String getTextDecoration();
 
 	@Property
 	void setTextDecoration(String textDecoration);
 
 	@Property
 	String getTextIndent();
 
 	@Property
 	void setTextIndent(String textIndent);
 
 	@Property
 	String getTextShadow();
 
 	@Property
 	void setTextShadow(String textShadow);
 
 	@Property
 	String getTextTransform();
 
 	@Property
 	void setTextTransform(String textTransform);
 
 	@Property
 	String getUnicodeBidi();
 
 	@Property
 	void setUnicodeBidi(String unicodeBidi);
 
 	@Property
 	String getWhiteSpace();
 
 	@Property
 	void setWhiteSpace(String whiteSpace);
 
 	@Property
 	String getWordSpacing();
 
 	@Property
 	void setWordSpacing(String wordSpacing);
 
 	@Property
 	String getTop();
 
 	@Property
 	void setTop(String top);
 
 	@Property
	String getZIndex();

 	@Property
 	void setZIndex(Object zIndex);
 
 	@BrowserSupport(BrowserType.FIREFOX_1P)
 	@Property
 	String getMozOpacity();
 
 	@BrowserSupport(BrowserType.FIREFOX_1P)
 	@Property
 	void setMozOpacity(String mozOpacity);
 
 	@BrowserSupport({ BrowserType.FIREFOX_1P, BrowserType.OPERA_7P,
 			BrowserType.SAFARI_3P })
 	@Function
 	Object getPropertyValue(String name);
 
 	@BrowserSupport(BrowserType.FIREFOX_1P)
 	@Property
 	String getOpacity();
 
 	@BrowserSupport(BrowserType.FIREFOX_1P)
 	@Property
 	void setOpacity(String Opacity);
 
 	@BrowserSupport(BrowserType.IE_6P)
 	@Property
 	String getZoom();
 
 	@BrowserSupport(BrowserType.IE_6P)
 	@Property
 	void setZoom(String zoom);
 
 	/**
 	 * Only for Rhino support
 	 * 
 	 * @param type
 	 * @return
 	 */
 	@Function
 	Object valueOf(String type);
 
 	/**
 	 * Retrieves the value of the specified attribute.
 	 * 
 	 * Syntax
 	 * 
 	 * vAttrValue = object.getAttribute(sAttrName [, iFlags])
 	 * 
 	 * Parameters
 	 * 
 	 * @param sAttrName
 	 *            Required. String that specifies the name of the attribute.
 	 * @param iFlags
 	 *            Optional. Integer that specifies one or more of the following
 	 *            flags:
 	 * 
 	 *            0 Default. Performs a property search that is not
 	 *            case-sensitive, and returns an interpolated value if the
 	 *            property is found. 1 Performs a case-sensitive property
 	 *            search. To find a match, the uppercase and lowercase letters
 	 *            in sAttrName must exactly match those in the attribute name. 2
 	 *            Returns attribute value as a String. This flag does not work
 	 *            for event properties. 4 Returns attribute value as a fully
 	 *            expanded URL. Only works for URL attributes.
 	 * 
 	 *            Return Value
 	 * 
 	 *            Variant that returns a String, Integer, or Boolean value as
 	 *            defined by the attribute. If the attribute is not present,
 	 *            this method returns null.
 	 */
 	@JstMultiReturn({ String.class, int.class, boolean.class })
 	@OverLoadFunc
 	Object getAttribute(String sAttrName, int iFlags);
 
 	@JstMultiReturn({ String.class, int.class, boolean.class })
 	@OverLoadFunc
 	String getAttribute(String sAttrName);
 
 	/**
 	 * Retrieves the expression for the given property.
 	 * 
 	 * Syntax
 	 * 
 	 * vExpression = object.getExpression(sPropertyName)
 	 * 
 	 * Parameters
 	 * 
 	 * @param sPropertyName
 	 *            Required. String that specifies the name of the property from
 	 *            which to retrieve the expression.
 	 *            
 	 *            @return     Variant. Returns a variant value representing the expression of the property.
 
 	 */
 	@BrowserSupport(BrowserType.IE_6P)
 	@Function Object getExpression(String sPropertyName);
 	
 }
