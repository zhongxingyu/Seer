 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.geom.Area;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.rptools.lib.GeometryUtil;
 import net.rptools.lib.GeometryUtil.PointNode;
 
 public class AreaMeta {
 
 	Area area;
 	PointNode pointNodeList;
 	Point2D centerPoint;
 	
 	// Only used during construction
 	GeneralPath path; 
 	PointNode lastPointNode;
 	
 	public AreaMeta() {
 	}
 	
 	public Point2D getCenterPoint() {
 		if (centerPoint == null) {
 			centerPoint = new Point2D.Double(area.getBounds().x + area.getBounds().width/2, area.getBounds().y + area.getBounds().height/2);
 		}
 		return centerPoint;
 	}
 	
 	public Set<Line2D> getFrontFaces(Point2D origin) {
 		
 		return GeometryUtil.getFrontFaces(pointNodeList, origin);
 	}
 	
 	public Set<Line2D> getAllFaces() {
 		
 		Set<Line2D> faceSet = new HashSet<Line2D>();
 		
 		PointNode node = pointNodeList;
 		do {
 			faceSet.add(new Line2D.Double(node.point, node.previous.point));
 			
 			node = node.next;
 			
 		} while (!node.point.equals(pointNodeList.point));
 		
 		return faceSet;
 	}
 	
 	public Area getArea() {
 		return new Area(area);
 	}
 	
 	public boolean isHole() {
 		
 		double angle = 0;
 		
 		PointNode currNode = pointNodeList.next;
 
 		while (currNode != pointNodeList) {
 			double currAngle = GeometryUtil.getAngleDelta(GeometryUtil.getAngle(currNode.previous.point, currNode.point), GeometryUtil.getAngle(currNode.point, currNode.next.point)); 
 
 			angle += currAngle;
 			currNode = currNode.next;
 		}
 		
 		return angle < 0;
 	}
 	
 	public void addPoint(float x, float y) {
 
 		PointNode pointNode = new PointNode(new Point2D.Double(x, y));
 		
 
 		// Don't add if we haven't moved
 		if (lastPointNode != null) {
 			if (lastPointNode.point.equals(pointNode.point)) {
 				return;
 			}
 		}
 		
 		if (path == null) {
 			path = new GeneralPath();
 			path.moveTo(x, y);
 			
 			pointNodeList = pointNode;
 		} else {
 			path.lineTo(x, y);
 			
 			lastPointNode.next = pointNode;
 			pointNode.previous = lastPointNode;
 		}
 		
 		lastPointNode = pointNode;
 	}
 	
 	public void close() {
 		area = new Area(path);
 
 		// Close the circle
 		lastPointNode.next = pointNodeList;
 		pointNodeList.previous = lastPointNode;
 		lastPointNode = null;
 		
		// For some odd reason, sometimes the first and last point are the same, which causes
		// bugs in the way areas are calculated
		if (pointNodeList.point.equals(pointNodeList.previous.point)) {
			// Pull out the dupe node
			PointNode trueLastPoint = pointNodeList.previous.previous;
			trueLastPoint.next = pointNodeList;
			pointNodeList.previous = trueLastPoint;
		}
		
 		path = null;
 	}
 }
