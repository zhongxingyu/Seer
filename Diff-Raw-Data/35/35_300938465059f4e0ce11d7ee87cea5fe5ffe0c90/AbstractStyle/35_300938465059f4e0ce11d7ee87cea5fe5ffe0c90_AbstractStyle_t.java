 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.css2.style;
 
import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.draw2d.geometry.Insets;
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
import org.eclipse.jst.pagedesigner.converter.ITagConverter;
 import org.eclipse.jst.pagedesigner.css2.CSSUtil;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.font.CSSFontManager;
 import org.eclipse.jst.pagedesigner.css2.font.ICSSFont;
 import org.eclipse.jst.pagedesigner.css2.font.ICSSFontManager;
 import org.eclipse.jst.pagedesigner.css2.list.CounterHelper;
 import org.eclipse.jst.pagedesigner.css2.list.CounterValueGenerator;
 import org.eclipse.jst.pagedesigner.css2.list.ICounterValueGenerator;
 import org.eclipse.jst.pagedesigner.css2.property.CSSMetaRegistry;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyMeta;
 import org.eclipse.jst.pagedesigner.css2.value.Length;
 import org.eclipse.jst.pagedesigner.utils.DOMUtil;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.css.CSSStyleDeclaration;
 import org.w3c.dom.css.CSSValue;
 
 /**
  * @author mengbo
  */
 public class AbstractStyle implements ICSSStyle 
 {
 	private final Element _element;
 
 	private Map _cachedValues = new HashMap();
 
 	private ICSSFont _font = null;
 
 	private CSSStyleDeclaration _cache;
 
 	private CSSStyleDeclaration _defaultCache;
 
 	private boolean _cssDeclareWasSearched = false;
 
 	private boolean _cssDefaultDeclareWasSearched = false;
 
 	private Insets _borderInsets, _marginInsets, _paddingInsets;
 
 	private ICSSStyle _parentStyle;
 
 	private HashMap _counters = null;
 
 	/**
 	 * @return the element this style if for
 	 */
 	public Element getElement() {
 		return _element;
 	}
 
 	/**
 	 * @param element
 	 */
 	public AbstractStyle(Element element) {
 		_element = element;
 	}
 
 	/**
 	 * reset all the cache.
 	 */
 	public void reset() {
 		_cachedValues.clear();
 		_font = null;
 		_cache = null;
 		_defaultCache = null;
 		_cssDeclareWasSearched = false;
 		_cssDefaultDeclareWasSearched = false;
 		// if (_counters != null)
 		// {
 		// unregistCounter();
 		// _counters.clear();
 		// _counters = null;
 		// }
 		_borderInsets = _marginInsets = _paddingInsets = null;
 	}
 
 	/**
 	 * this is a hook method so caller can use it to override default
 	 * calculation. Note, after the call to <code>reset</code>, it will be
 	 * lost.
 	 * 
 	 * @param property
 	 * @param value
 	 */
 	public void setStyleProperty(String property, Object value) {
 		_cachedValues.put(property, value);
 	}
 
 	/**
 	 * get a style property value.
 	 * 
 	 * @param property
 	 * @return the style property
 	 */
 	public Object getStyleProperty(String property) {
 		Object value = _cachedValues.get(property);
 		if (value == null) {
 			value = calculateProperty(property);
 			if (value != null) {
 				_cachedValues.put(property, value);
 			}
 		}
 		return value;
 	}
 
 	/**
 	 * in this method, should first check the "style" attribute, then combine
 	 * that with document style.
 	 * 
 	 * @return the style
 	 */
 	protected CSSStyleDeclaration calculateDeclaration() {
 		String name = getHtmlElement().getAttribute("id"); //$NON-NLS-1$
 		if (name == null || name.length() == 0) {
 			name = getHtmlElement().getAttribute("name"); //$NON-NLS-1$
 		}
 		return CSSUtil.getCSSDeclaration(this.getHtmlElement(), name);
 	}
 
 	/**
 	 * @return the style
 	 */
 	protected CSSStyleDeclaration calculateDefaultDeclaration() {
 		return CSSUtil.getDefaultCSSDeclaration(this.getHtmlElement(), null);
 	}
 
 	/**
 	 * @return the style declaration
 	 */
 	public CSSStyleDeclaration getDeclaration() {
 		// FIXME:may need to be change, boolean variable is not a best way.
 		if (!_cssDeclareWasSearched) {
 			_cache = calculateDeclaration();
 			_cssDeclareWasSearched = true;
 		}
 		return _cache;
 	}
 
 	/**
 	 * @return the default declaration
 	 */
 	public CSSStyleDeclaration getDefaultDeclaration() {
 		// FIXME:may need to be change, boolean variable is not a best way.
 		if (!_cssDefaultDeclareWasSearched) {
 			_defaultCache = calculateDefaultDeclaration();
 			_cssDefaultDeclareWasSearched = true;
 		}
 		return _defaultCache;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getHTMLelementInitValue(java.lang.String)
 	 */
 	public Object getHTMLelementInitValue(String propertyName) {
 		ICSSPropertyMeta meta = getPropertyMeta(propertyName);
 		if (meta != null) {
 			Object obj = meta.getHTMLElementInitialValue(_element,
 					getHTMLTag(), propertyName);
 			if (obj == null) {
 				obj = meta.getInitialValue(propertyName, this);
 			}
 			return obj;
 		}
 		return ICSSPropertyMeta.NOT_SPECIFIED;
 	}
 
 	/**
 	 * @param propertyName
 	 * @return the property
 	 */
 	protected Object calculateProperty(String propertyName) {
 		ICSSPropertyMeta meta = getPropertyMeta(propertyName);
 		Object result = null;
 		// get declaration
 		CSSStyleDeclaration decl = getDeclaration();
 		CSSValue value = decl == null ? null : decl
 				.getPropertyCSSValue(propertyName);
 		if (value == null) {
 			if (meta != null) {
 				result = meta.calculateHTMLAttributeOverride(_element,
 						getHTMLTag(), propertyName, this);
 				if (result != null) {
 					return result;
 				}
 			}
 			decl = getDefaultDeclaration();
 		}
 		value = decl == null ? null : decl.getPropertyCSSValue(propertyName);
 
 		if (value != null && value.getCssValueType() == CSSValue.CSS_INHERIT) {
 			result = getParentResultValue(meta, propertyName);
 		} else if (value == null) {
 			if (meta != null) {
 				result = meta.calculateHTMLAttributeOverride(_element,
 						getHTMLTag(), propertyName, this);
 			}
 			if (result == null) {
 				result = calculateLocalOverride(meta, propertyName);
 			}
 			if (result == null) {
 				if (meta == null) {
 					result = ICSSPropertyMeta.NOT_SPECIFIED;
 				} else {
 					if (meta.isInherited()) {
 						result = getParentResultValue(meta, propertyName);
 					} else {
 						result = meta.getInitialValue(propertyName, this);
 					}
 				}
 			}
 		} else {
 			result = calculateCSSValueResult(meta, value, propertyName);
 		}
 		return result;
 	}
 
 	/**
 	 * get the corresponding HTML tag for this style. This is for certain HTML
 	 * tag can also provide style information.
 	 * 
 	 * @return the html tag
 	 */
 	protected String getHTMLTag() {
 		return _element.getTagName();
 	}
 
 	/**
 	 * @param propertyName
 	 * @return the property meta for property name
 	 */
 	protected ICSSPropertyMeta getPropertyMeta(String propertyName) {
 		return CSSMetaRegistry.getInstance().getMeta(propertyName);
 	}
 
 	/**
 	 * convert the CSSValue to the property type specified data result.
 	 * 
 	 * @param meta 
 	 * @param value
 	 * @param propertyName
 	 * @return should not return null.
 	 */
 	protected Object calculateCSSValueResult(ICSSPropertyMeta meta,
 			CSSValue value, String propertyName) {
 		if (meta == null) {
 			return ICSSPropertyMeta.NOT_SPECIFIED;
 		}
         return meta.calculateCSSValueResult(value, propertyName, this);
 	}
 
 	/**
 	 * it is possible that some attribute of the element may provide style
 	 * information. child class should override this method. Also, some element
 	 * type may have internally build style, such as input-submit may use
 	 * special border. NOTE: it is very important that in calculateLocalOverride
 	 * you don't directly or indirectly call getStyleProperty() to avoid
 	 * deadloop.
 	 * 
 	 * @param meta 
 	 * @param propertyName
 	 * @return null means no style information in other attributes. Otherwise
 	 *         return property specific data result -- normally will use meta to
 	 *         convert the attribute.
 	 */
 	protected Object calculateLocalOverride(ICSSPropertyMeta meta,
 			String propertyName) {
 		// // do some default margin thing.
 		// if (ICSSPropertyID.ATTR_MARGIN_RIGHT.equalsIgnoreCase(propertyName)
 		// || ICSSPropertyID.ATTR_MARGIN_BOTTOM.equalsIgnoreCase(propertyName))
 		// {
 		// return MARGIN_LENGTH;
 		// }
 		// else if
 		// (ICSSPropertyID.ATTR_MARGIN_LEFT.equalsIgnoreCase(propertyName))
 		// {
 		// // to make a little room, so it is possible for user to position the
 		// // mouse before the first element in a block.
 		// return MARGIN_LEFT;
 		// }
 		return null;
 	}
 
 	/**
 	 * This is only called when inherit value from parent.
 	 * 
 	 * @param meta 
 	 * @param propertyName
 	 * @return the result value
 	 */
 	protected Object getParentResultValue(ICSSPropertyMeta meta,
 			String propertyName) {
 		ICSSStyle style = getParentStyle();
 		return style.getStyleProperty(propertyName);
 	}
 
 	/**
 	 * @param parentStyle
 	 */
 	public void setParentStyle(ICSSStyle parentStyle) {
 		this._parentStyle = parentStyle;
 		reset();
 	}
 
 	public ICSSStyle getParentStyle() {
 		if (_parentStyle != null) {
 			return _parentStyle;
 		}
 		Node node = _element.getParentNode();
 		while (node instanceof Element && node instanceof INodeNotifier) {
 			ICSSStyle parentStyle = (ICSSStyle) ((INodeNotifier) node)
 					.getAdapterFor(ICSSStyle.class);
 			if (parentStyle != null) {
 				return parentStyle;
 			}
             node = node.getParentNode();
 		}
 		return DefaultStyle.getInstance();
 	}
 
 	/**
 	 * Will not return null
 	 * 
 	 * @return the font
 	 */
 	public ICSSFont getCSSFont() {
 		if (_font == null) {
 			_font = getFontManager().createFont(this);
 		}
 		return _font;
 	}
 
 	/**
 	 * @return
 	 */
 	private ICSSFontManager getFontManager() {
 		return CSSFontManager.getInstance();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#dispose()
 	 */
 	public void dispose() {
         // TODO: anything to dispose?
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getMarginInsets()
 	 */
 	public Insets getMarginInsets() {
 		if (_marginInsets == null) {
 			int top = getInsetProperty(ICSSPropertyID.ATTR_MARGIN_TOP);
 			int left = getInsetProperty(ICSSPropertyID.ATTR_MARGIN_LEFT);
 			int bottom = getInsetProperty(ICSSPropertyID.ATTR_MARGIN_BOTTOM);
 			int right = getInsetProperty(ICSSPropertyID.ATTR_MARGIN_RIGHT);
 			_marginInsets = new Insets(top, left, bottom, right);
 		}
 		return _marginInsets;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getPaddingInsets()
 	 */
 	public Insets getPaddingInsets() {
 		if (_paddingInsets == null) {
			int top = getInsetProperty(ICSSPropertyID.ATTR_PADDING_TOP);
			int left = getInsetProperty(ICSSPropertyID.ATTR_PADDING_LEFT);
			int bottom = getInsetProperty(ICSSPropertyID.ATTR_PADDING_BOTTOM);
			int right = getInsetProperty(ICSSPropertyID.ATTR_PADDING_RIGHT);
			//add extra padding only for the top element of a source tag's rendering
			if (elementIsTagConverted()) {
				top += ARTIFICIAL_BORDER_OFFSET;
				left += ARTIFICIAL_BORDER_OFFSET;
				bottom += ARTIFICIAL_BORDER_OFFSET;
				right += ARTIFICIAL_BORDER_OFFSET;
			}
 			_paddingInsets = new Insets(top, left, bottom, right);
 		}
 		return _paddingInsets;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getBorderInsets()
 	 */
 	public Insets getBorderInsets() {
 		if (_borderInsets == null) {
 			int top = getInsetProperty(ICSSPropertyID.ATTR_BORDER_TOP_WIDTH);
 			int left = getInsetProperty(ICSSPropertyID.ATTR_BORDER_LEFT_WIDTH);
 			int bottom = getInsetProperty(ICSSPropertyID.ATTR_BORDER_BOTTOM_WIDTH);
 			int right = getInsetProperty(ICSSPropertyID.ATTR_BORDER_RIGHT_WIDTH);
 			_borderInsets = new Insets(top, left, bottom, right);
 		}
 		return _borderInsets;
 	}
 
 	/**
 	 * @param border_top_width
 	 * @return
 	 */
 	private int getInsetProperty(String propertyName) {
 		Object obj = this.getStyleProperty(propertyName);
 		if (obj instanceof Length) {
 			Length l = (Length) obj;
 			if (l.isPercentage()) {
 				return 0; // FIXME:
 			}
             return l.getValue();
 		}
 
 		return 0;
 	}
 
 	public boolean isAdapterForType(Object type) {
 		return (type == ICSSStyle.class);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#isSizeIncludeBorderPadding()
 	 */
 	public boolean isSizeIncludeBorderPadding() {
 		String display = this.getDisplay();
 		if ("table-cell".equalsIgnoreCase(display)) { //$NON-NLS-1$
 			return false;
 		}
 		String tag = this.getHTMLTag();
 		if ("img".equalsIgnoreCase(tag)) { //$NON-NLS-1$
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.ibm.sse.model.INodeAdapter#notifyChanged(com.ibm.sse.model.INodeNotifier,
 	 *      int, java.lang.Object, java.lang.Object, java.lang.Object, int)
 	 */
 	public void notifyChanged(INodeNotifier notifier, int eventType,
 			Object changedFeature, Object oldValue, Object newValue, int pos) {
         // do nothing
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getBackgroundColor()
 	 */
 	public Object getColor() {
 		Object _color = null;
 		if (_color == null) {
 			_color = getStyleProperty(ICSSPropertyID.ATTR_COLOR);
 			if (_color == null) {
 				_color = getStyleProperty(ICSSPropertyID.ATTR_TEXTCOLOR);
 			}
 		}
 		return _color;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getColor()
 	 */
 	public Object getBackgroundColor() {
 		Object _backgroundColor = null;
 		if (_backgroundColor == null) {
 			_backgroundColor = getStyleProperty(ICSSPropertyID.ATTR_BACKGROUND_COLOR);
 		}
 		return _backgroundColor;
 	}
 
 	/**
 	 * @return the html element
 	 */
 	public Element getHtmlElement() {
 		// if (_element instanceof IDOMElement)
 		// {
 		// EditPart part = (EditPart) ((IDOMElement)
 		// _element).getAdapterFor(EditPart.class);
 		// if (part instanceof ElementEditPart)
 		// {
 		// ElementEditPart elementPart = (ElementEditPart) part;
 		// ITagHandler h = elementPart.getTagHandler();
 		// if (h != null)
 		// {
 		// return h.mapCustomElement(_element);
 		// }
 		// }
 		//
 		// }
 		return _element;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getDisplay()
 	 */
 	public String getDisplay() {
 		Object display = this.getStyleProperty(ICSSPropertyID.ATTR_DISPLAY);
 		String displayStr;
 		if (display == null) {
 			displayStr = ICSSPropertyID.VAL_INLINE;
 		} else if (display instanceof String) {
 			displayStr = (String) display;
 		} else {
 			displayStr = display.toString();
 		}
 		if (ICSSPropertyID.VAL_INLINE.equalsIgnoreCase(displayStr)
 				&& IHTMLConstants.TAG_TABLE.equalsIgnoreCase(getHTMLTag())) {
 			return ICSSPropertyID.VAL_INLINE_TABLE;
 		}
 		if (ICSSPropertyID.VAL_INLINE.equalsIgnoreCase(displayStr)) {
 			Object width = this.getStyleProperty(ICSSPropertyID.ATTR_WIDTH);
 			if (width instanceof Length) {
 				return ICSSPropertyID.VAL_INLINE_BLOCK;
 			}
 			Object height = this.getStyleProperty(ICSSPropertyID.ATTR_HEIGHT);
 			if (height instanceof Length) {
 				return ICSSPropertyID.VAL_INLINE_BLOCK;
 			}
 			return displayStr;
 		}
         return displayStr;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getListStyle()
 	 */
 	public Map getCounters() {
 		if (_counters == null) {
 			_counters = new HashMap();
 			CounterHelper.processCounterReset(this, _counters);
 		}
 		return _counters;
 	}
 
 	/**
 	 * Get named counter from counters.
 	 * 
 	 * see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getCounter(java.lang.String)
 	 */
 	public ICounterValueGenerator findCounter(String name, boolean must) {
 		Map counters = getCounters();
 		if (counters == null || !counters.containsKey(name)) {
 			if (getParentStyle() != null
 					&& !(getParentStyle() instanceof DefaultStyle)) {
 				// ensure it is registered somewhere.
 				return getParentStyle().findCounter(name, must);
 			}
 			// must is called by counter-increment
 			else if (must) {
 				// the caller should do the other setting.
 				ICounterValueGenerator counter = new CounterValueGenerator(
 						name, null, null, this);
 				counter.resetCount();
 				counters.put(name, counter);
 			}
 		}
 		return (ICounterValueGenerator) counters.get(name);
 	}
 
 	/**
 	 * @param buffer
 	 */
 	public void dumpDebugInfo(StringBuffer buffer) {
 		if (_cache != null) {
 			buffer.append("cache:\n"); //$NON-NLS-1$
 			buffer.append(_cache.getCssText()).append("\n"); //$NON-NLS-1$
 		}
 		if (_defaultCache != null) {
 			buffer.append("default cache:\n"); //$NON-NLS-1$
 			buffer.append(_defaultCache.getCssText()).append("\n"); //$NON-NLS-1$
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getColSpan()
 	 */
 	public int getColSpan() {
 		int colspan = DOMUtil.getIntAttributeIgnoreCase(getHtmlElement(),
 				"colspan", 1); //$NON-NLS-1$
 		// if span == 0, means span all col from the current column to end
 		// colume
 		if (colspan < 0) {
 			return 1;
 		}
         return colspan;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#getRowSpan()
 	 */
 	public int getRowSpan() {
 		int rowspan = DOMUtil.getIntAttributeIgnoreCase(getHtmlElement(),
 				"rowspan", 1); //$NON-NLS-1$
 		if (rowspan < 0) {
 			return 1;
 		}
         return rowspan;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#isInSelection()
 	 */
 	public boolean isInSelection() {
 		IRangeSelectionProxy proxy = (IRangeSelectionProxy) getAdapter(IRangeSelectionProxy.class);
 		if (proxy != null) {
 			return proxy.isRangeSelected();
 		}
 		ICSSStyle parentStyle = getParentStyle();
 		if (parentStyle != null) {
 			return parentStyle.isInSelection();
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (this._element instanceof INodeNotifier) {
 			Object ret = ((INodeNotifier) _element).getAdapterFor(adapter);
 			if (ret != null && adapter.isInstance(ret)) {
 				return ret;
 			}
 		}
 		return null;
 	}
 
 	// private void unregistCounter()
 	// {
 	// if (_counters != null)
 	// {
 	// Collection c = _counters.values();
 	// Iterator iter = c.iterator();
 	// while (iter.hasNext())
 	// {
 	// Counter2 counter = (Counter2) iter.next();
 	// counter.unregist(this);
 	// }
 	// }
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.ICSSStyle#resetCounters()
 	 */
 	public void processCounters() {
 		this._counters = null;
 		CounterHelper.processCounterIncrement(this);
 	}

	private boolean elementIsTagConverted() {
		boolean isTagConverted = false;
		if (_element instanceof INodeNotifier) {
			Collection nodeAdapters = ((INodeNotifier)_element).getAdapters();
			for (Object nodeAdapter: nodeAdapters) {
				if (nodeAdapter instanceof ITagConverter) {
					isTagConverted = true;
					break;
				}
			}
		}
		return isTagConverted;
	}

 }
