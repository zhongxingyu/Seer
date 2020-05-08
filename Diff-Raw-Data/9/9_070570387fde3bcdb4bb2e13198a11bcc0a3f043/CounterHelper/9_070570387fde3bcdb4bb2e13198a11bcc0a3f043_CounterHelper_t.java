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
 
 import java.util.HashMap;
 import java.util.List;
 
import org.eclipse.core.runtime.Assert;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyMeta;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSPrimitiveValue;
 import org.w3c.dom.css.CSSPrimitiveValue;
 
 /**
  * @author mengbo
  */
 public class CounterHelper {
 	public final static int LIST_T_IMAGE = 0;
 
 	public final static int LIST_T_DISC = 1;
 
 	public final static int LIST_T_CIRCLE = 2;
 
 	public final static int LIST_T_SQUARE = 3;
 
 	public final static int LIST_T_DECIMAL = 0x11;
 
 	public final static int LIST_T_DECIMAL_LEADING_ZERO = 0x12;
 
 	public final static int LIST_T_LOWER_ALPHA = 0x13;
 
 	public final static int LIST_T_LOWER_ROMAN = 0x14;
 
 	public final static int LIST_T_UPPER_ALPHA = 0x15;
 
 	public final static int LIST_T_UPPER_ROMAN = 0x16;
 
 	public final static int LIST_T_LOWER_GREEK = 0x21;
 
 	public final static int LIST_T_ARMENIAN = 0x22;
 
 	public final static int LIST_T_GEORGIAN = 0x23;
 
 	public final static int LIST_T_NONE = 0x24;
 
 	// /**
 	// * Collect counters declaration from node and its parents
 	// *
 	// * @param style
 	// * @param counters
 	// * @return
 	// */
 	// public static void getCounters(ICSSStyle style, HashMap counters)
 	// {
 	// processCounterReset(style, counters);
 	// Object content = style.getStyleProperty(ICSSPropertyID.ATTR_CONTENT);
 	// // content counter could be reference or creation of new one.
 	// if (content != null && content != ICSSPropertyMeta.NOT_SPECIFIED)
 	// {
 	// // XXX: what 's the content.
 	// ContentObject contentObject = null;
 	// Object counter = null;
 	// if (content instanceof List)
 	// {
 	// // TODO: we only deal with one currently.
 	// contentObject = (ContentObject) ((List) content).get(0);
 	// }
 	// else if (content instanceof ContentObject)
 	// {
 	// contentObject = (ContentObject) content;
 	// }
 	// if (style.getParentStyle() != null)
 	// {
 	// String identifier = contentObject.getCounter().getIdentifier();
 	// counter = style.getParentStyle().findCounter(identifier, false);
 	// if (counter == null)
 	// {
 	// // no reference, then create it.
 	// counter = contentObject.getCounter();
 	// }
 	// }
 	// Assert.isTrue(counter != null);
 	// ((Counter2) counter).regist(style);
 	// counters.put(((Counter2) counter).getIdentifier(), counter);
 	// }
 	// // counter-increment is reference.
 	// processCounterIncrement(style/* , counters */);
 	// }
 
 	/**
 	 * @param style
 	 * @param counters
 	 */
 	public static void processCounterReset(ICSSStyle style, HashMap counters) {
 		Assert.isTrue(style != null && counters != null);
 		// counter-reset will create new one.
 		Object counterResets = style
 				.getStyleProperty(ICSSPropertyID.ATTR_COUNTER_RESET);
 		if ((counterResets) != null
 				&& counterResets != ICSSPropertyMeta.NOT_SPECIFIED) {
 			if (counterResets instanceof List) {
 				List crList = (List) counterResets;
 				for (int i = 0, n = crList.size(); i < n; i++) {
 					ResetObject rObject = (ResetObject) crList.get(i);
 					String name = rObject.getCounterName();
 					Object counter = null;
 					if (counters.size() > 0 && counters.containsKey(name)) {
 						// Already resolved
 						counter = counters.get(name);
 					}
 					if (counter != null) {
 						if (rObject.getInitial() != null) {
 							((ICounterValueGenerator) counter)
 									.resetCount(rObject.getInitial().intValue());
 						} else {
 							counter = ((ICounterValueGenerator) counter)
 									.resetCount();
 						}
 						counters.put(((ICounterValueGenerator) counter)
 								.getIdentifier(), counter);
 					} else {
 						// create new one
 						Object listStyle = style
 								.getStyleProperty(ICSSPropertyID.ATTR_LIST_STYLE_TYPE);
 						if (listStyle instanceof String) {
 							counter = new CounterValueGenerator(name,
 									(String) listStyle, null, style);
 							if (rObject.getInitial() != null) {
 								((ICounterValueGenerator) counter)
 										.resetCount(rObject.getInitial()
 												.intValue());
 							} else {
 								counter = ((ICounterValueGenerator) counter)
 										.resetCount();
 							}
 						}
 						counters.put(((ICounterValueGenerator) counter)
 								.getIdentifier(), counter);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param style
 	 * @param counters
 	 */
 	public static void processCounterIncrement(ICSSStyle style/*
 																 * , HashMap
 																 * counters
 																 */) {
 		Object counterIncrements = style
 				.getStyleProperty(ICSSPropertyID.ATTR_COUNTER_INCREMENT);
 		if (counterIncrements != null
 				&& counterIncrements != ICSSPropertyMeta.NOT_SPECIFIED) {
 			if (counterIncrements instanceof List) {
 				List crList = (List) counterIncrements;
 				for (int i = 0, n = crList.size(); i < n; i++) {
 					IncrementObject rObject = (IncrementObject) crList.get(i);
 					String name = rObject.getCounterName();
 					Object counter = null;
 					counter = style.findCounter(name, true);
 					if (counter != null) {
 						if (HTMLListInfoHelper.getValueInt(style) == null) {
 							if (rObject.getIncrement() != null) {
 								((ICounterValueGenerator) counter)
 										.increase(rObject.getIncrement()
 												.intValue());
 							} else {
 								((ICounterValueGenerator) counter).increase();
 							}
 						} else {
 							((ICounterValueGenerator) counter)
 									.setCount(HTMLListInfoHelper
 											.getValueInt(style));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public static boolean isImage(ICSSStyle style) {
 		return false;
 	}
 
 	public static boolean isText(ICSSStyle style) {
 		String display = style.getDisplay();
 		Object styleType = style
 				.getStyleProperty(ICSSPropertyID.ATTR_LIST_STYLE_TYPE);
 		return (display
 				.equalsIgnoreCase(ICSSPropertyID.VAL_LIST_ITEM) //
 				&& styleType instanceof String //
 		&& !CounterValueGenerator.NON_STRING_TYPES.contains(styleType));
 	}
 
 	public static boolean isNodeImage(ICSSStyle style) {
 		return false;
 	}
 
 	public static int getType(ICSSStyle style) {
 		Object type = style
 				.getStyleProperty(ICSSPropertyID.ATTR_LIST_STYLE_TYPE);
 		if (type instanceof String) {
 			return toTypeInt((String) type);
 		}
         return -1;
 	}
 
 	public static int toTypeInt(String type) {
 
 		if (type.equalsIgnoreCase(ICSSPropertyID.VAL_DECIMAL)) {
 			return LIST_T_DECIMAL;
 		} else if (type
 				.equalsIgnoreCase(ICSSPropertyID.VAL_DECIMAL_LEADING_ZERO)) {
 			return LIST_T_DECIMAL_LEADING_ZERO;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_DISC)) {
 			return LIST_T_DISC;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_CIRCLE)) {
 			return LIST_T_CIRCLE;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_SQUARE)) {
 			return LIST_T_SQUARE;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_IMAGE)) {
 			return LIST_T_IMAGE;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_UPPER_LATIN)) {
 			return LIST_T_UPPER_ALPHA;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_UPPER_ALPHA)) {
 			return LIST_T_UPPER_ALPHA;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_LOWER_LATIN)) {
 			return LIST_T_LOWER_ALPHA;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_LOWER_ALPHA)) {
 			return LIST_T_LOWER_ALPHA;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_UPPER_ROMAN)) {
 			return LIST_T_UPPER_ROMAN;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_LOWER_ROMAN)) {
 			return LIST_T_LOWER_ROMAN;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_LOWER_GREEK)) {
 			return LIST_T_LOWER_GREEK;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_ARMENIAN)) {
 			return LIST_T_ARMENIAN;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_GEORGIAN)) {
 			return LIST_T_GEORGIAN;
 		} else if (type.equalsIgnoreCase(ICSSPropertyID.VAL_NONE)) {
 			return LIST_T_NONE;
 		}
 		return 0;
 	}
 
 	// TODO: for future use we need a new ContentObject to hold other objects
 	// declares in css style.
 
 	// public static ContentObject getContentObject(ICSSStyle style)
 	// {
 	// // TODO: currently we only fetch first counter in case there are more
 	// than one counters.
 	// if (style.getStyleProperty(ICSSPropertyID.ATTR_CONTENT) !=
 	// ICSSPropertyMeta.NOT_SPECIFIED)
 	// {
 	// Object content = style.getStyleProperty(ICSSPropertyID.ATTR_CONTENT);
 	// Object object = null;
 	// if (content instanceof List)
 	// {
 	// object = ((List) content).get(0);
 	// }
 	// else if (content instanceof ContentObject)
 	// {
 	// object = content;
 	// }
 	// if (object instanceof ContentObject)
 	// {
 	// return (ContentObject) content;
 	// }
 	// }
 	// return null;
 	// }
 
 	public static boolean isIdentifier(Object cssValue) {
 		return (cssValue instanceof ICSSPrimitiveValue)
 				&& ((ICSSPrimitiveValue) cssValue).getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
 	}
 
 	public static boolean isNumber(Object cssValue) {
 		return cssValue instanceof ICSSPrimitiveValue
 				&& ((ICSSPrimitiveValue) cssValue).getPrimitiveType() == ICSSPrimitiveValue.CSS_INTEGER;
 	}
 }
