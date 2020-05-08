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
 package org.eclipse.jst.pagedesigner.css2.layout;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.css2.provider.ICSSTextProvider;
 import org.eclipse.jst.pagedesigner.css2.style.DefaultStyle;
 import org.eclipse.jst.pagedesigner.css2.style.StyleUtil;
 import org.eclipse.jst.pagedesigner.viewer.CaretPositionResolver;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 
 /**
  * @author mengbo
  */
 public class CSSTextFigure extends FlowFigure implements ICSSFigure {
 	private ICSSTextProvider _provider;
 
 	private List _fragments = new ArrayList(1);
 
 	/**
 	 * @param provider
 	 */
 	public CSSTextFigure(ICSSTextProvider provider) {
 		_provider = provider;
 		this.setLayoutManager(createDefaultFlowLayout());
 	}
 
 	public ICSSStyle getCSSStyle() {
 		IFigure parentFigure = this.getParent();
 		if (parentFigure instanceof ICSSFigure) {
 			ICSSStyle style = ((ICSSFigure) parentFigure).getCSSStyle();
 			if (style != null) {
 				return style;
 			}
 		}
 		return DefaultStyle.getInstance();
 	}
 
 	/**
 	 * @see org.eclipse.draw2d.IFigure#containsPoint(int, int)
 	 */
 	public boolean containsPoint(int x, int y) {
 		if (!super.containsPoint(x, y)) {
 			return false;
 		}
 		List frags = getFragments();
 		for (int i = 0, n = frags.size(); i < n; i++) {
 			if (((FlowBox) frags.get(i)).containsPoint(x, y)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @return the default flow layout
 	 * 
 	 */
 	protected FlowFigureLayout createDefaultFlowLayout() {
 		return new CSSTextLayout(this);
 	}
 
 	/**
 	 * Returns the <code>LineBox</code> fragments contained in this InlineFlow
 	 * 
 	 * @return The fragments
 	 */
 	public List getFragments() {
 		return _fragments;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.layout.ICSSFigure#getFragmentsForRead()
 	 */
 	public List getFragmentsForRead() {
 		return getFragments();
 	}
 
 	/**
 	 * @return the text
 	 */
 	public String getText() {
 		return _provider.getTextData();
 	}
 
 	/**
 	 * @see FlowFigure#postValidate()
 	 */
 	public void postValidate() {
 		List list = getFragments();
 		FlowBox box;
 		int left = Integer.MAX_VALUE, top = left;
 		int right = Integer.MIN_VALUE, bottom = right;
 		for (int i = 0, n = list.size(); i < n; i++) {
 			box = (FlowBox) list.get(i);
 			left = Math.min(left, box._x);
 			right = Math.max(right, box._x + box._width);
 			top = Math.min(top, box._y);
 			bottom = Math.max(bottom, box._y + box._height);
 		}
 		setBounds(new Rectangle(left, top, right - left, bottom - top));
 		list = getChildren();
 		for (int i = 0, n = list.size(); i < n; i++) {
 			((FlowFigure) list.get(i)).postValidate();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.draw2d.Figure#paintBorder(org.eclipse.draw2d.Graphics)
 	 */
 	protected void paintBorder(Graphics graphics) {
 		if (Debug.DEBUG_TEXTBORDER) {
 			if (_fragments != null) {
 				graphics.setForegroundColor(ColorConstants.darkBlue);
 				for (int i = 0, size = _fragments.size(); i < size; i++) {
 					FlowBox box = (FlowBox) _fragments.get(i);
 					BoxUtil.drawBox(graphics, box);
 				}
 				graphics.restoreState();
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.draw2d.Figure#paintFigure(Graphics)
 	 */
 	protected void paintFigure(Graphics g) {
 		Object result = this.getCSSStyle().getColor();
 		Color color;
 		if (result instanceof Color) {
 			color = (Color) result;
 		} else if (result instanceof RGB) {
 			color = new Color(null, (RGB) result);
 		} else {
 			color = null;
 		}
 		int[] range = null;
 		if (!StyleUtil.isInWidget(this.getCSSStyle())) {
 			range = _provider.getSelectedRange();
 		}
 		if (range == null || range[0] == range[1]) {
 			// we are not in selection
 			TextLayoutSupport.paintTextFigure(g, _fragments, getCSSStyle()
 					.getCSSFont().getSwtFont(), color, ((Integer) getCSSStyle()
 					.getStyleProperty(ICSSPropertyID.ATTR_TEXTDECORATION))
 					.intValue());
 		} else {
 			TextLayoutSupport.paintTextFigureWithSelection(g, _fragments,
 					_provider.getTextData(), getCSSStyle().getCSSFont()
 							.getSwtFont(), color, ((Integer) getCSSStyle()
 							.getStyleProperty(
 									ICSSPropertyID.ATTR_TEXTDECORATION))
 							.intValue(), range[0], range[1],
					ColorConstants.white, ColorConstants.blue);
 		}
 		if (color != result && color != null) {
 			color.dispose();
 		}
 	}
 
 	/**
 	 * Find out lines which has closer y coordinate to point, and then line
 	 * which has closer x coordinate.
 	 * 
 	 * @param relative
 	 * @return return the offset
 	 */
     // TODO: refactoring?
 	public int getNewInsertionOffset(Point relative) {
 		TextFragmentBox closestBox = null;
 		// if there is one which are at the same line with relative, calculate
 		// that line first;
 		for (int i = 0, n = _fragments.size(); i < n; i++) {
 			TextFragmentBox box = (TextFragmentBox) _fragments.get(i);
 			if (box.containsPoint(relative.x, relative.y)) {
 				int index = FlowUtilities.getTextInWidth(box.getTextData(),
 						getCSSStyle().getCSSFont().getSwtFont(), relative.x
 								- box._x, TextLayoutSupport
 								.getAverageCharWidth(box));
 				return box._offset + index;
 			}
             if (closestBox == null) {
             	closestBox = box;
             } else {
             	// box is above point
             	TextFragmentBox tempBox = box;
             	int offset1 = Math
             			.abs(CaretPositionResolver.getYDistance(
             					new Rectangle(tempBox._x, tempBox._y,
             							tempBox._width, tempBox._height),
             					relative));
             	tempBox = closestBox;
             	int offset2 = Math
             			.abs(CaretPositionResolver.getYDistance(
             					new Rectangle(tempBox._x, tempBox._y,
             							tempBox._width, tempBox._height),
             					relative));
             	if (offset1 < offset2) {
             		closestBox = box;
             	}
             }
             // at the same line
             if (box.containsPoint(box._x, relative.y)) {
             	TextFragmentBox tempBox = box;
             	int offset1 = Math
             			.abs(CaretPositionResolver.getXDistance(
             					new Rectangle(tempBox._x, tempBox._y,
             							tempBox._width, tempBox._height),
             					relative));
             	tempBox = closestBox;
             	int offset2 = Math
             			.abs(CaretPositionResolver.getXDistance(
             					new Rectangle(tempBox._x, tempBox._y,
             							tempBox._width, tempBox._height),
             					relative));
             	if (offset1 < offset2) {
             		closestBox = box;
             	}
             }
 		}
 
 		if (closestBox.containsPoint(closestBox._x, relative.y)
 				|| closestBox.containsPoint(relative.x, closestBox._y)) {
 			int offset = relative.x - closestBox._x;
 			int index = FlowUtilities.getTextInWidth(closestBox.getTextData(),
 					getCSSStyle().getCSSFont().getSwtFont(), offset,
 					TextLayoutSupport.getAverageCharWidth(closestBox));
 			return closestBox._offset + index;
 		}
         return -1;
 	}
 
 	/**
 	 * @param relative
 	 * @return the insertion offset
 	 */
 	public int getInsertionOffset(Point relative) {
 		for (int i = 0, n = _fragments.size(); i < n; i++) {
 			TextFragmentBox box = (TextFragmentBox) _fragments.get(i);
 			if (box.containsPoint(relative.x, relative.y)) {
 				int index = FlowUtilities.getTextInWidth(box.getTextData(),
 						getCSSStyle().getCSSFont().getSwtFont(), relative.x
 								- box._x, TextLayoutSupport
 								.getAverageCharWidth(box));
 				return box._offset + index;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * the returned rectangle will be relative to this text figure.
 	 * 
 	 * @param offset
 	 * @return the caret position
 	 */
 	public Rectangle calculateCaretPosition(int offset) {
 		// search reverse order, find the latest box that has _offset small than
 		// the specified one
 		if (offset > 0) {
 			for (int i = _fragments.size() - 1; i >= 0; i--) {
 				TextFragmentBox box = (TextFragmentBox) _fragments.get(i);
 				if (box._offset <= offset) {
 					// ok, we find the box.
 					if (box._offset + box._length < offset) {
 						return new Rectangle(box._x + box._width, box._y, 1,
 								box._height);
 					}
                     String s = box.getTextData().substring(0,
                     		offset - box._offset);
                     int width = FlowUtilities.getTextExtents(s,
                     		getCSSStyle().getCSSFont().getSwtFont()).width;
                     return new Rectangle(box._x + width, box._y, 1,
                     		box._height);
 				}
 			}
 		} else {
 			if (_fragments.size() > 0) {
 				TextFragmentBox box = (TextFragmentBox) _fragments.get(0);
 				return new Rectangle(box._x, box._y, 1, box._height);
 			}
 		}
 		// should only reach here when there is no fragments.
 		return new Rectangle(getBounds().x, getBounds().y, 1, getBounds().height);
 	}
 
 }
