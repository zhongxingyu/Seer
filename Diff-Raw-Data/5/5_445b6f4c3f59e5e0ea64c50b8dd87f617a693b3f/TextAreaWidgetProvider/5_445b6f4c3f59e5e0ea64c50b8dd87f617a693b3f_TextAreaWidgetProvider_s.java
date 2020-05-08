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
 package org.eclipse.jst.pagedesigner.css2.widget;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.font.ICSSFont;
 import org.eclipse.jst.pagedesigner.css2.layout.TextLayoutSupport;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.css2.provider.DimensionInfo;
 import org.eclipse.jst.pagedesigner.css2.style.DefaultStyle;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author mengbo
  * @version 1.5
  */
 public class TextAreaWidgetProvider extends AbstractWidgetProvider {
 	private static final int DEFAULTCOLUMN = 20;
 
 	private static final int DEFAULTROWS = 2;
 
 	private static final int VERTICAL_PADDING = 2;
 
 	private static final int HORIZONTAL_PADDING = 2;
 
 	private static int ARRAWWIDTH = 16;
 
 	private static int ARROWHEIGHT = 16;
 
 	private int _columns = DEFAULTCOLUMN;
 
 	private int _rows = DEFAULTROWS;
 
 	private String _value;
 
 	/**
 	 * @param style
 	 */
 	public TextAreaWidgetProvider(ICSSStyle style) {
 		super(style);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.provider.ICSSWidgetProvider#getPreferredDimension(int,
 	 *      int)
 	 */
 	public DimensionInfo getPreferredDimension(int width, int height) {
 		if (width <= 0) {
 			width = getDefaultWidth();
 		}
 		if (height <= 0) {
 			height = getDefaultHeight();
 		}
 		return new DimensionInfo(width, height, -1);
 	}
 
 	/**
 	 * @return
 	 */
 	private int getDefaultHeight() {
 		ICSSStyle style = this.getCSSStyle();
 		if (style == null) {
 			style = DefaultStyle.getInstance();
 		}
 		ICSSFont font = style.getCSSFont();
 		Font swtfont = font.getSwtFont();
 		int fontHeight = FigureUtilities.getFontMetrics(swtfont).getHeight();
 
 		return (fontHeight) * _rows + VERTICAL_PADDING;
 	}
 
 	/**
 	 * @return
 	 */
 	private int getDefaultWidth() {
 		ICSSStyle style = this.getCSSStyle();
 		if (style == null) {
 			style = DefaultStyle.getInstance();
 		}
 		ICSSFont font = style.getCSSFont();
 
 		int fontWidth = FigureUtilities.getFontMetrics(font.getSwtFont())
 				.getAverageCharWidth();
 		return _columns * fontWidth + ARRAWWIDTH + HORIZONTAL_PADDING;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.provider.ICSSWidgetProvider#paintFigure(org.eclipse.draw2d.Graphics,
 	 *      org.eclipse.draw2d.geometry.Rectangle)
 	 */
 	public void paintFigure(Graphics g, Rectangle rect) {
 		if (_value != null) {
 			g.clipRect(rect);
 			ICSSStyle style = this.getCSSStyle();
 			if (style == null) {
 				style = DefaultStyle.getInstance();
 			}
 			int decoration = ((Integer) style
 					.getStyleProperty(ICSSPropertyID.ATTR_TEXTDECORATION))
 					.intValue();
 			ICSSFont font = style.getCSSFont();
 			g.setFont(font.getSwtFont());
 
 			Color newColor = null;
 			Object color = style.getColor();
 			if (color instanceof Color) {
 				g.setForegroundColor((Color) color);
 			} else if (color instanceof RGB) {
 				newColor = new Color(Display.getCurrent(), (RGB) color);
 				g.setForegroundColor(newColor);
 			} else {
 				g.setForegroundColor(ColorConstants.black);
 			}
 
 			Object textAlign = style
 					.getStyleProperty(ICSSPropertyID.ATTR_TEXTALIGN);
 			int begin = 0;
 			int end = 0;
 			int fontHeight = FigureUtilities.getFontMetrics(font.getSwtFont())
 					.getHeight();
 
 			int fontWidth = FigureUtilities.getFontMetrics(font.getSwtFont())
 					.getAverageCharWidth();
 			int columns = (rect.width - HORIZONTAL_PADDING) / fontWidth;
 
 			int i = 0;
 			while (true) {
 				int y = rect.y + VERTICAL_PADDING / 2 + fontHeight * i;
 				if (y >= rect.bottom()) {
 					break;
 				}
 				end += columns;
 				if (end > _value.length()) {
 					end = _value.length();
 				}
 				end = getTextCount(begin, end, g.getFont(), rect.width
 						- ARRAWWIDTH);
 
 				String text = _value.substring(begin, end);
 
 				int width = FigureUtilities.getTextWidth(text, g.getFont());
 				int x = TextLayoutSupport.getBeginX(textAlign, rect, width);
 				g.drawString(text, x, y);
 
 				TextLayoutSupport.paintTextDecoration(g, new Rectangle(x, y,
 						width, fontHeight), decoration);
 				begin = end;
 
 				if (end == _value.length()) {
 					break;
 				}
 				i++;
 			}
 			if (newColor != null) {
 				newColor.dispose();
 			}
 		}
 		int borderThick = 2;
 		BorderUtil
 				.drawVertialBar(g, ARRAWWIDTH, ARROWHEIGHT, borderThick, rect);
 	}
 
 	private int getTextCount(int begin, int end, Font swtFont, int textWidth) {
		while (FigureUtilities.getTextWidth(_value.substring(begin, end),
				swtFont) > textWidth) {
 			end--;
 		}
 		return end;
 	}
 
 	/**
 	 * @param columns
 	 */
 	public void setColumns(int columns) {
 		this._columns = (columns > 0 ? columns : DEFAULTCOLUMN);
 	}
 
 	/**
 	 * @param rows
 	 */
 	public void setRows(int rows) {
 		this._rows = (rows > 0 ? rows : DEFAULTROWS);
 	}
 
 	/**
 	 * @param value
 	 */
 	public void setValue(String value) {
 		this._value = value;
 	}
 }
