 /*
  * Copyright (c) 2007, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Sergey Gribovsky (Borland) - initial API and implementation
  *    
  */
 package org.eclipse.uml2.diagram.common.draw2d;
 
 import java.util.List;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gmf.runtime.diagram.ui.figures.BorderItemLocator;
 import org.eclipse.gmf.runtime.draw2d.ui.mapmode.MapModeUtil;
 
 /**
  * #235335, we can't call super.relocate() and then tweak the bounds to bisection location, because it fires endless layout loop. 
  * While we also can't call private locateOn methods directly, we had to copy-paste them in this class
  * @see BorderItemLocator 
  */
 public class BisectionBorderItemLocator extends BorderItemLocator {
 	public BisectionBorderItemLocator(IFigure parentFigure) {
 		super(parentFigure);
 	}
 
 	public BisectionBorderItemLocator(IFigure parentFigure, int preferredSide) {
 		super(parentFigure, preferredSide);
 	}
 
 	public BisectionBorderItemLocator(IFigure borderItem, IFigure parentFigure, Rectangle constraint) {
 		super(borderItem, parentFigure, constraint);
 	}
 	
 	@Override
 	public Rectangle getValidLocation(Rectangle proposedLocation, IFigure borderItem) {
 		int side = findClosestSideOfParent(proposedLocation, getParentBorder());
 		return getBisectionLocation(super.getValidLocation(proposedLocation, borderItem), side);
 	}
 	
 	@Override
 	public void relocate(IFigure borderItem) {
         Dimension size = getSize(borderItem);
 		Rectangle rectSuggested = new Rectangle(getPreferredLocation(borderItem), size);
 		int closestSide = findClosestSideOfParent(rectSuggested, getParentBorder());
 		setPreferredSideOfParent(closestSide);
 
 		Point ptNewLocation = locateOnBorder(getPreferredLocation(borderItem),
 			getPreferredSideOfParent(), 0, borderItem);
         
 		setCurrentSideOfParent(findClosestSideOfParent(new Rectangle(ptNewLocation, size), getParentBorder()));
 		
 		Rectangle bisectLoc = getBisectionLocation(new Rectangle(ptNewLocation, size), getCurrentSideOfParent());
 		borderItem.getBounds().setBounds(bisectLoc);
 	}
 	
 	protected Rectangle getBisectionLocation(Rectangle location, int side) {
 		Rectangle bisectingLocation = new Rectangle(location);
 		
 		switch (side) {
 		case PositionConstants.WEST:
 			bisectingLocation.x = bisectingLocation.x + bisectingLocation.width / 2;
 			break;
 
 		case PositionConstants.EAST:
 			bisectingLocation.x = bisectingLocation.x - bisectingLocation.width / 2;
 			break;
 
 		case PositionConstants.NORTH:
 			bisectingLocation.y = bisectingLocation.y + bisectingLocation.height / 2;
 			break;
 
 		case PositionConstants.SOUTH:
 			bisectingLocation.y = bisectingLocation.y - bisectingLocation.height / 2;
 			break;
 		}
 		
 		return bisectingLocation;
 	}
 	
 	/**
 	 * The preferred side takes precendence.
 	 * 
 	 * @param suggestedLocation
 	 * @param suggestedSide
 	 * @param circuitCount
 	 *            recursion count to avoid an infinite loop
 	 * @return point
 	 */
	private Point locateOnBorder(Point suggestedLocation,
 			int suggestedSide, int circuitCount, IFigure borderItem) {
 		Point recommendedLocation = locateOnParent(suggestedLocation,
 			suggestedSide, borderItem);
 
 		int vertical_gap = MapModeUtil.getMapMode(getParentFigure()).DPtoLP(8);
 		int horizontal_gap = MapModeUtil.getMapMode(getParentFigure())
 			.DPtoLP(8);
 		Dimension borderItemSize = getSize(borderItem);
 
 		if (circuitCount < 4 && conflicts(recommendedLocation, borderItem)) {
 			if (suggestedSide == PositionConstants.WEST) {
 				do {
 					recommendedLocation.y += borderItemSize.height
 						+ vertical_gap;
 				} while (conflicts(recommendedLocation, borderItem));
 				if (recommendedLocation.y > getParentBorder().getBottomLeft().y
 					- borderItemSize.height) { // off the bottom,
 					// wrap south
 					return locateOnBorder(recommendedLocation,
 						PositionConstants.SOUTH, circuitCount + 1, borderItem);
 				}
 			} else if (suggestedSide == PositionConstants.SOUTH) {
 				do {
 					recommendedLocation.x += borderItemSize.width
 						+ horizontal_gap;
 				} while (conflicts(recommendedLocation, borderItem));
 				if (recommendedLocation.x > getParentBorder().getBottomRight().x
 					- borderItemSize.width) {
 					return locateOnBorder(recommendedLocation,
 						PositionConstants.EAST, circuitCount + 1, borderItem);
 				}
 			} else if (suggestedSide == PositionConstants.EAST) {
 				// move up the east side
 				do {
 					recommendedLocation.y -= (borderItemSize.height + vertical_gap);
 				} while (conflicts(recommendedLocation, borderItem));
 				if (recommendedLocation.y < getParentBorder().getTopRight().y) {
 					// east is full, try north.
 					return locateOnBorder(recommendedLocation,
 						PositionConstants.NORTH, circuitCount + 1, borderItem);
 				}
 			} else { // NORTH
 				do {
 					recommendedLocation.x -= (borderItemSize.width + horizontal_gap);
 				} while (conflicts(recommendedLocation, borderItem));
 				if (recommendedLocation.x < getParentBorder().getTopLeft().x) {
 					return locateOnBorder(recommendedLocation,
 						PositionConstants.WEST, circuitCount + 1, borderItem);
 				}
 			}
 		}
 		return recommendedLocation;
 	}
 
 	/**
 	 * Determine if the the given point conflicts with the position of an
 	 * existing borderItemFigure.
 	 * 
 	 * @param recommendedLocation
 	 * @return <code>ture</code> or <code>false</code>
 	 */
 	private boolean conflicts(Point recommendedLocation,
 			IFigure targetBorderItem) {
 		Rectangle recommendedRect = new Rectangle(recommendedLocation,
 			getSize(targetBorderItem));
 		List<?> borderItems = targetBorderItem.getParent().getChildren();
         
         // Only check those border items that would have already been
         // relocated. See Bugzilla#214799.
         int currentIndex = borderItems.indexOf(targetBorderItem);
         for (int i = 0; i < currentIndex; i++) {
             IFigure borderItem = (IFigure) borderItems.get(i);
 			if (borderItem.isVisible()) {
 				Rectangle rect = borderItem.getBounds().getCopy();
 				if (rect.intersects(recommendedRect)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Ensure the suggested location actually lies on the parent boundary. The
 	 * side takes precendence.
 	 * 
 	 * @param suggestedLocation
 	 * @param suggestedSide
 	 * @return point
 	 */
	private Point locateOnParent(Point suggestedLocation,
 			int suggestedSide, IFigure borderItem) {
 		Rectangle bounds = getParentBorder();
 		int parentFigureWidth = bounds.width;
 		int parentFigureHeight = bounds.height;
 		int parentFigureX = bounds.x;
 		int parentFigureY = bounds.y;
 		Dimension borderItemSize = getSize(borderItem);
 		int newX = suggestedLocation.x;
 		int newY = suggestedLocation.y;
 		int westX = parentFigureX - borderItemSize.width
 			+ getBorderItemOffset().width;
 		int eastX = parentFigureX + parentFigureWidth
 			- getBorderItemOffset().width;
 		int southY = parentFigureY + parentFigureHeight
 			- getBorderItemOffset().height;
 		int northY = parentFigureY - borderItemSize.height
 			+ getBorderItemOffset().height;
 		if (suggestedSide == PositionConstants.WEST) {
 			if (suggestedLocation.x != westX) {
 				newX = westX;
 			}
 			if (suggestedLocation.y < bounds.getTopLeft().y) {
 				newY = northY + borderItemSize.height;
 			} else if (suggestedLocation.y > bounds.getBottomLeft().y
 				- borderItemSize.height) {
 				newY = southY - borderItemSize.height;
 			}
 		} else if (suggestedSide == PositionConstants.EAST) {
 			if (suggestedLocation.x != eastX) {
 				newX = eastX;
 			}
 			if (suggestedLocation.y < bounds.getTopLeft().y) {
 				newY = northY + borderItemSize.height;
 			} else if (suggestedLocation.y > bounds.getBottomLeft().y
 				- borderItemSize.height) {
 				newY = southY - borderItemSize.height;
 			}
 		} else if (suggestedSide == PositionConstants.SOUTH) {
 			if (suggestedLocation.y != southY) {
 				newY = southY;
 			}
 			if (suggestedLocation.x < bounds.getBottomLeft().x) {
 				newX = westX + borderItemSize.width;
 			} else if (suggestedLocation.x > bounds.getBottomRight().x
 				- borderItemSize.width) {
 				newX = eastX - borderItemSize.width;
 			}
 		} else { // NORTH
 			if (suggestedLocation.y != northY) {
 				newY = northY;
 			}
 			if (suggestedLocation.x < bounds.getBottomLeft().x) {
 				newX = westX + borderItemSize.width;
 			} else if (suggestedLocation.x > bounds.getBottomRight().x
 				- borderItemSize.width) {
 				newX = eastX - borderItemSize.width;
 			}
 		}
 		return new Point(newX, newY);
 	}
 	
 }
