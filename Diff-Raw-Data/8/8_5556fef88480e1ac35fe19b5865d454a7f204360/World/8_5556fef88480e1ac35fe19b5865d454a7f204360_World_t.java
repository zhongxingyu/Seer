 package com.secondhand.model.sat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.secondhand.model.Vector2;
 
 
 // Uses the separating axis theorem to implement collision checking.
 // Credits: http://www.codezealot.org/archives/55
 // used to generate random level.
 public class World {
 
 	private final int width;
 	private final int height;
 
 	// the shapes in the world.
 	private final List<Shape> shapes;
 
 	public World(final int width, final int height) {
 		this.width = width;
 		this.height = height;
 		shapes = new ArrayList<Shape>();
 	}
 
 	//Returns true if the
 	public boolean addToWorld(final Shape shape) {
 		if (isUnoccupied(shape)) {
 			shapes.add(shape);
 			return true;
 		}
 		return false;
 	}
 
 	private boolean isWithinWorldBounds(final Vector2 position) {
 		return position.x >= 0 && position.x <= width &&
 				position.y >= 0 && position.y <= height;
 	}
 
 	public boolean isUnoccupied(final Shape shape) {
 		if(shape instanceof Polygon) {
 			final Polygon polygon = (Polygon)shape;
 			for(final Vector2 edge: polygon.edges) {
 				if(!isWithinWorldBounds(edge))
 					return false;
 			}
 		}
 
 
 
 		if(shape instanceof Circle) {
 
 			final Circle circle = (Circle) shape;
 			// check if circle is in world:
 			if( circle.position.x < (circle.radius) && circle.position.x < (width - circle.radius) &&
 					circle.position.y < (circle.radius) && circle.position.y < (height - circle.radius))
 				return false;
 		}
 
 		return noIntersection(shape);
 
 	}
 
 	// implements the separating axes theorem.
 	// test the shapes for intersection.
 	private boolean sat(final Shape shape1, final Shape shape2) {
 
 		if(shape1 instanceof Circle && shape2 instanceof Circle) {
 			final Circle circle1 = (Circle) shape1;
 			final Circle circle2 = (Circle) shape2;
 
			final Vector2 v = new Vector2(circle1.position.x - circle2.position.x, circle1.position.y - circle2.position.y);
 
 			// normal circle collision check.
 
 			return v.len() < circle1.radius + circle2.radius;
 		}
 
 		if(((shape1 instanceof Polygon) && (shape2 instanceof Circle)) ||
 				(shape1 instanceof Circle && shape2 instanceof Polygon)) {
 
 			Polygon polygon = null;
 			Circle circle = null;
 
 			if(shape1 instanceof Polygon) {
 				polygon = (Polygon)shape1;
 				circle = (Circle)shape2;
 			} else {
 				polygon = (Polygon)shape2;
 				circle = (Circle)shape1;
 			}
 
 			// TODO: also test for containinment.
 
 		    for (int i = 0; i < polygon.edges.size(); ++i) {
 
 		    	final Vector2 dist = new Vector2(circle.position.x - polygon.edges.get(i).x,
 		    			circle.position.y - polygon.edges.get(i).y);
 
 		        if (dist.len() < circle.radius)
 		        	return true;
 
 		    }
 
 		    return false;
 		}
 
 
 
 		// TODO :we should also test for containment.
 
 		final Polygon poly1 = (Polygon)shape1;
 		final Polygon poly2 = (Polygon)shape2;
 
 		final Vector2[] axes1 = poly1.getAxes();
 		final Vector2[] axes2 = poly2.getAxes();
 		// loop over the axes1
 		for (int i = 0; i < axes1.length; i++) {
 			final Vector2 axis = new Vector2(axes1[i]);
 			// project both shapes onto the axis
 			final  Projection p1 = poly1.project(axis);
 			final  Projection p2 = poly2.project(axis);
 			// do the projections overlap?
 			if (!p1.overlap(p2)) {
 				// then we can guarantee that the shapes do not overlap
 				return false;
 			}
 		}
 		// loop over the axes2
 		for (int i = 0; i < axes2.length; i++) {
 			final Vector2 axis = new Vector2(axes2[i]);
 			// project both shapes onto the axis
 			final  Projection p1 = poly1.project(axis);
 			final  Projection p2 = poly2.project(axis);
 			// do the projections overlap?
 			if (!p1.overlap(p2)) {
 				// then we can guarantee that the shapes do not overlap
 				return false;
 			}
 		}
 		// if we get here then we know that every axis had overlap on it
 		// so we can guarantee an intersection
 		return true;
 
 
 	}
 
 	private boolean noIntersection(final Shape shape) {
 		for(final Shape otherShape : shapes) {
 			if(sat(shape, otherShape)) {
 				return false;
 			}
 		}
 
 
 		return true;
 	}
 
 
 }
