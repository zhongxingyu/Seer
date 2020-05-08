 package com.mgs.fantasi.polygon;
 
 import java.awt.*;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 public class HexagonShape implements PolygonPointsIterator {
 	@Override
 	public boolean isRectangular() {
 		return false;
 	}
 
 	@Override
 	public List<Point2D.Double> getPointListFromBottomLeftCorner(Dimension size) {
 		List<Point2D.Double> hexPoints = new ArrayList<Point2D.Double>();
 		double quarterHeight = size.getHeight() / 4;
 		double halfWidth = size.getWidth() / 2;
 
 		double yBottom = 0;
 		double yFirstAnchor 	= quarterHeight;
 		double ySecondAnchor 	= yFirstAnchor * 3;
 		double yTop 			= size.getHeight();
 
		double xOrigin   = 0;
 		double xCenter   = halfWidth;
 		double xEndPoint = size.getWidth();
 
 		hexPoints.add(new Point2D.Double(xOrigin, 		yFirstAnchor));
 		hexPoints.add(new Point2D.Double(xOrigin, 		ySecondAnchor));
 		hexPoints.add(new Point2D.Double(xCenter,			yTop));
 
 		hexPoints.add(new Point2D.Double(xEndPoint,		ySecondAnchor));
 		hexPoints.add(new Point2D.Double(xEndPoint, 		yFirstAnchor));
 		hexPoints.add(new Point2D.Double(xCenter,			yBottom));
 		return hexPoints;
 	}
 }
