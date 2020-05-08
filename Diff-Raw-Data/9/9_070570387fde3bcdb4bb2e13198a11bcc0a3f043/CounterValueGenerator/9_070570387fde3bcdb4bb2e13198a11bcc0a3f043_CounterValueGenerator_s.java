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
 package org.eclipse.jst.pagedesigner.css2.list;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
import org.eclipse.jface.text.Assert;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyMeta;
 
 /**
  * The counter is used to generate automatic conters and numbering for list
  * item. XXX: what do we deal with psedo? and we need to refer to web tools
  * content to consult for style content.
  * 
  * @author mengbo
  */
 public class CounterValueGenerator implements ICounterValueGenerator {
 	private final static int DEFAULT_INITIAL_VALUE = 0;
 
 	public static final Set STRING_TYPES = new HashSet();
 
 	public static final Set NON_STRING_TYPES = new HashSet();
 	static {
 		NON_STRING_TYPES.add("disc");
 		NON_STRING_TYPES.add("circle");
 		NON_STRING_TYPES.add("square");
 		STRING_TYPES.add("decimal");
 		STRING_TYPES.add("decimal-leading-zero");
 		STRING_TYPES.add("lower-roman");
 		STRING_TYPES.add("upper-roman");
 		STRING_TYPES.add("lower-greek");
 		STRING_TYPES.add("lower-alpha");
 		STRING_TYPES.add("lower-latin");
 		STRING_TYPES.add("upper-alpha");
 		STRING_TYPES.add("upper-latin");
 		STRING_TYPES.add("hebrew");
 		STRING_TYPES.add("armenian");
 		STRING_TYPES.add("georgian");
 		STRING_TYPES.add("cjk-ideographic");
 		STRING_TYPES.add("hiragana");
 		STRING_TYPES.add("katakana");
 		STRING_TYPES.add("hiragana-iroha");
 		STRING_TYPES.add("katakana-iroha");
 	}
 
 	private final static int DEFAULT_INCREMENT = 1;
 
 	private boolean _first = true;
 
 	private Integer _initial;
 
 	private List _visitors;
 
 	private int _count;
 
 	private String _identifier;
 
 	private String _styleType;
 
 	private String _seperator;
 
 	private ICSSStyle _style;
 
 	public CounterValueGenerator(String identifier, String styleType,
 			String seperator, ICSSStyle style) {
 		_identifier = identifier;
 		_styleType = styleType;
 		_seperator = seperator;
 		_style = style;
 		if (HTMLListInfoHelper.getStartInt(style) != null) {
 			_count = HTMLListInfoHelper.getStartInt(style).intValue();
 		} else {
 			_count = DEFAULT_INITIAL_VALUE;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.w3c.dom.css.Counter#getIdentifier()
 	 */
 	public String getIdentifier() {
 		return _identifier;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.w3c.dom.css.Counter#getListStyle()
 	 */
 	public String getListStyle() {
 		return _styleType;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.w3c.dom.css.Counter#getSeparator()
 	 */
 	public String getSeparator() {
 		return _seperator;
 	}
 
 	// /**
 	// * @return Returns the type.
 	// */
 	// public String getType()
 	// {
 	// return _styleType;
 	// }
 
 	// /**
 	// * @return Returns the markerString.
 	// */
 	// public String getTextValue()
 	// {
 	// Assert.isTrue(this.isText());
 	// _index = getDeclaredIndex();
 	// Node container = findParentComtainer();
 	// if (container == null)
 	// {
 	// // what condition?
 	// return "";
 	// }
 	// String exp = "";
 	// int startIndex = getStartIndex(container);
 	// int maxLength = calculateMaxlength(container, 0) + startIndex - 1;
 	// Assert.isTrue(maxLength > 0);
 	// if (_index == -1)
 	// {
 	// // no declared value
 	// calculateIndex(container);
 	// Assert.isTrue(_index > -1);
 	// exp = Integer.toString(_index + startIndex);
 	// }
 	// else
 	// {
 	// exp = Integer.toString(_index);
 	// }
 	// if (getType() == CounterHelper.LIST_T_DECIMAL)
 	// {
 	// exp = appendSuffix(exp, Integer.toString(maxLength).length() -
 	// exp.length());
 	// }
 	// else if (getType() == CounterHelper.LIST_T_DECIMAL_LEADING_ZERO)
 	// {
 	// exp = addPrefix(exp, maxLength - exp.length());
 	// }
 	// return exp + ".";
 	//
 	// }
 
 	// private String addPrefix(String exp, int length)
 	// {
 	// while (length > 0)
 	// {
 	// exp = "0" + exp;
 	// length--;
 	// }
 	// return exp;
 	// }
 	//
 	// private String appendSuffix(String exp, int length)
 	// {
 	// while (length > 0)
 	// {
 	// exp = exp + " ";
 	// length--;
 	// }
 	// return exp;
 	// }
 
 	// private boolean calculateIndex(Node node)
 	// {
 	// if (node == _node)
 	// {
 	// _index++;
 	// return true;
 	// }
 	// String name = node.getNodeName();
 	// if (name != null && name.equalsIgnoreCase("li"))
 	// {
 	// _index++;
 	// }
 	// if (!node.hasChildNodes())
 	// {
 	// return false;
 	// }
 	// node = node.getFirstChild();
 	// while (node != null)
 	// {
 	// name = node.getNodeName();
 	// if (name != null && !(name.equalsIgnoreCase("ul") ||
 	// name.equalsIgnoreCase("ol")))
 	// {
 	// if (calculateIndex(node))
 	// {
 	// return true;
 	// }
 	// }
 	// node = node.getNextSibling();
 	// }
 	// return false;
 	// }
 
 	// This method may be refered for the zero-leading calculation.
 	// private int calculateMaxlength(Node node, int index)
 	// {
 	// String name = node.getNodeName();
 	// if (name != null && name.equalsIgnoreCase("li"))
 	// {
 	// index++;
 	// }
 	// if (!node.hasChildNodes())
 	// {
 	// return index;
 	// }
 	// node = node.getFirstChild();
 	// while (node != null)
 	// {
 	// name = node.getNodeName();
 	// if (name != null && !(name.equalsIgnoreCase("ul") ||
 	// name.equalsIgnoreCase("ol")))
 	// {
 	// index = calculateMaxlength(node, index);
 	// }
 	// node = node.getNextSibling();
 	// }
 	// return index;
 	// }
 	//
 
 	// private int getStartIndex(Node container)
 	// {
 	// String value = ((Element) container).getAttribute("start");
 	// try
 	// {
 	// int index = Integer.parseInt(value);
 	// if (index < 0)
 	// {
 	// return 1;
 	// }
 	// return index;
 	// }
 	// catch (Exception e)
 	// {
 	// return 1;
 	// }
 	// }
 
 	// private boolean isStringTyped(ICSSStyle style)
 	// {
 	// style.getStyleProperty("list-style-type");
 	// return true;
 	// }
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#clone()
 	 */
 	protected Object clone() throws CloneNotSupportedException {
 		CounterValueGenerator newInstance = new CounterValueGenerator(
 				_identifier, _styleType, _seperator, _style);
 		return newInstance;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.list.Counter2#increase()
 	 */
 	public void increase(int increment) {
 		if (!_first || HTMLListInfoHelper.getStartInt(_style) == null) {
 			_count += increment;
 		}
 		_first = false;
 	}
 
 	public void increase() {
 		increase(DEFAULT_INCREMENT);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.list.Counter2#setCount()
 	 */
 	public ICounterValueGenerator resetCount() {
 		try {
 			ICounterValueGenerator counter = (ICounterValueGenerator) this
 					.clone();
 			_initial = null;
 			_count = HTMLListInfoHelper.getStartInt(_style) != null ? HTMLListInfoHelper
 					.getStartInt(_style).intValue()
 					: DEFAULT_INITIAL_VALUE;
 			return counter;
 		} catch (CloneNotSupportedException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * @return Returns the _initial.
 	 */
 	public int getInitial() {
 		if (HTMLListInfoHelper.getStartInt(_style) != null) {
 			return HTMLListInfoHelper.getStartInt(_style).intValue();
 		}
         return _initial != null ? _initial.intValue()
         		: DEFAULT_INITIAL_VALUE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.list.Counter2#setCount()
 	 */
 	public ICounterValueGenerator resetCount(int initial) {
 		try {
 			CounterValueGenerator counter = (CounterValueGenerator) this
 					.clone();
 			_initial = new Integer(initial);
 			_count = initial;
 			return counter;
 		} catch (CloneNotSupportedException e) {
 			return null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.list.ICounterValueGenerator#setCount(org.eclipse.jst.pagedesigner.css2.list.HTMLListInfo)
 	 */
 	public void setCount(Integer value) {
 		if (value != null) {
 			_count = value.intValue();
 			_first = false;
 		}
 	}
 
 	// /**
 	// * The clients of this counter need to regist them.
 	// *
 	// * @see
 	// org.eclipse.jst.pagedesigner.css2.list.Counter2#regist(java.lang.Object)
 	// */
 	// public void regist(Object caller)
 	// {
 	// Assert.isTrue(caller instanceof ICSSStyle);
 	// if (_visitors == null)
 	// {
 	// _visitors = new LinkedList();
 	// }
 	// if (!_visitors.contains(caller))
 	// {
 	// _visitors.add(caller);
 	// }
 	// }
 	//
 	// /**
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// org.eclipse.jst.pagedesigner.css2.list.Counter2#unregist(java.lang.Object)
 	// */
 	// public void unregist(Object caller)
 	// {
 	// if (_visitors.contains(caller))
 	// {
 	// _visitors.remove(caller);
 	// }
 	// }
 
 	/**
 	 * Return the int value.
 	 * 
 	 * @author mengbo
 	 */
 	public int getCurrentCount() {
 		return _count;
 	}
 
 	/**
 	 * Currently we recalculate the count, to enhance the performance, we may
 	 * use _count, but this requires delicate synchronization when the
 	 * calculation is looped.
 	 */
 	public Integer getCount(Object oCaller) {
 		Assert.isTrue(oCaller instanceof ICSSStyle && _visitors != null
 				&& _visitors.size() > 0);
 		ICSSStyle caller = (ICSSStyle) oCaller;
 		if (!_visitors.contains(caller)) {
 			return null;
 		}
 		int result = getInitial();
 
 		for (int i = 0, n = _visitors.size(); i < n; i++) {
 			ICSSStyle style = (ICSSStyle) _visitors.get(i);
 			// get the count;
 			Object counterIncrements = style
 					.getStyleProperty(ICSSPropertyID.ATTR_COUNTER_INCREMENT);
 			if (counterIncrements != null
 					&& counterIncrements != ICSSPropertyMeta.NOT_SPECIFIED) {
 				if (counterIncrements instanceof List) {
 					List crList = (List) counterIncrements;
 					for (int j = 0, nn = crList.size(); j < nn; j++) {
 						IncrementObject rObject = (IncrementObject) crList
 								.get(j);
 						String name = rObject.getCounterName();
 						if (getIdentifier().equalsIgnoreCase(name)) {
 							if (rObject.getIncrement() != null) {
 								result += rObject.getIncrement().intValue();
 							} else {
 								result += DEFAULT_INCREMENT;
 							}
 						}
 					}
 				}
 			}
 			if (style == caller) {
 				return new Integer(result);
 			}
 		}
 		return null;
 	}
 }
