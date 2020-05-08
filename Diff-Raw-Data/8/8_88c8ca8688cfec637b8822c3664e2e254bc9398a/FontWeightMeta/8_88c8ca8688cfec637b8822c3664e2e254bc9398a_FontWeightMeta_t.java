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
 package org.eclipse.jst.pagedesigner.css2.property;
 
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.w3c.dom.css.CSSValue;
 
 /**
  * The result value will always be integer, range 100-900 This class has already
  * translate things like "normal", "bold", "bolder", "lighter" into integer
  * value.
  * 
  * @author mengbo
  */
 public class FontWeightMeta extends CSSPropertyMeta {
 
 	/**
 	 * normal weight value
 	 */
 	public static final Integer NORMAL_WEIGHT = new Integer(400);
 
 	private static final Integer BOLD_WEIGHT = new Integer(700);
 
 	private static final String[] KEYWORDS = new String[] { ICSSPropertyID.VAL_NORMAL,
 			ICSSPropertyID.VAL_BOLD, ICSSPropertyID.VAL_BOLDER,
 			ICSSPropertyID.VAL_LIGHTER };
 
 
 	/**
 	 * Default constructor
 	 */
 	public FontWeightMeta() {
 		super(true, NORMAL_WEIGHT);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.property.CSSPropertyMeta#getKeywordValues()
 	 */
 	protected String[] getKeywordValues() {
 		return KEYWORDS;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.property.CSSPropertyMeta#calculateCSSValueResult(org.w3c.dom.css.CSSValue,
 	 *      java.lang.String,
 	 *      org.eclipse.jst.pagedesigner.css2.style.AbstractStyle)
 	 */
 	public Object calculateCSSValueResult(CSSValue value, String propertyName,
 			ICSSStyle style) {
 		String text = value.getCssText();
 		String result = checkKeywordValues(text);
 		if (result == null) {
 			try {
 				int i = Integer.parseInt(text);
 				if (i < 100) {
 					i = 100;
 				}
 				if (i > 900) {
 					i = 900;
 				}
 				return new Integer(i);
 			} catch (Exception ex) {
				// Error in integer processing
 				return NORMAL_WEIGHT;
 			}
 		} else if (ICSSPropertyID.VAL_NORMAL.equals(result)) {
 			return NORMAL_WEIGHT;
 		} else if (ICSSPropertyID.VAL_BOLD.equals(result)) {
 			return BOLD_WEIGHT;
 		} else if (ICSSPropertyID.VAL_BOLDER.equals(result)) {
 			// int i = style.getParentStyle().getCSSFont().getWeight() + 100;
 			// if (i < 100)
 			// i = 100;
 			// if (i > 900)
 			// i = 900;
 			// return new Integer(i);
 			return BOLD_WEIGHT;
 		} else if (ICSSPropertyID.VAL_LIGHTER.equals(result)) {
 			int i = style.getParentStyle().getCSSFont().getWeight() - 100;
 			if (i < 100) {
 				i = 100;
 			}
 			if (i > 900) {
 				i = 900;
 			}
 			return new Integer(i);
 		}
 		return NORMAL_WEIGHT;
 	}
 }
