 package org.skullforge.asteroidpush.utils;
 
 import java.util.ArrayList;
 
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Fixture;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 
 public class GeometryConverter {
 
    static public ArrayList<Shape> extractOutline(Body body) {
       ArrayList<Shape> outline = new ArrayList<Shape>();
       Fixture f = body.getFixtureList();
       Transform t = convertToSlickTransform(body.getTransform());
       while (f != null) {
          Shape shape = convertToSlickShape(f.getShape());
          shape = shape.transform(t);
          outline.add(shape);
          f = f.getNext();
       }
       return outline;
    }
 
    static private org.newdawn.slick.geom.Transform convertToSlickTransform(org.jbox2d.common.Transform transform) {
      Transform converted = Transform.createRotateTransform(transform.q.getAngle(), transform.p.x, transform.p.y);
      return converted;
    }
 
    static public org.newdawn.slick.geom.Shape convertToSlickShape(org.jbox2d.collision.shapes.Shape shape) {
       if (shape == null) {
          return new Circle(0.0f, 0.0f, 7.5f);
       }
 
       org.newdawn.slick.geom.Shape converted;
       switch (shape.getType()) {
       case POLYGON:
          converted = convertToPolygon(shape);
          break;
       case CIRCLE:
          converted = convertToCircle(shape);
          break;
       default:
          converted = new Circle(0.0f, 0.0f, 7.5f);
          break;
       }
 
       return converted;
    }
 
    static private org.newdawn.slick.geom.Shape convertToCircle(org.jbox2d.collision.shapes.Shape shape) {
       CircleShape circle = (CircleShape) shape;
       org.newdawn.slick.geom.Shape converted = new Circle(circle.m_p.x,
             circle.m_p.y, circle.m_radius);
       return converted;
    }
 
    static private org.newdawn.slick.geom.Shape convertToPolygon(org.jbox2d.collision.shapes.Shape shape) {
       PolygonShape poly = (PolygonShape) shape;
       Vec2[] vertices = poly.getVertices();
       float[] points = new float[poly.getVertexCount() * 2];
       for (int i = 0; i < poly.getVertexCount(); ++i) {
          points[i * 2 + 0] = vertices[i].x;
          points[i * 2 + 1] = vertices[i].y;
       }
       org.newdawn.slick.geom.Polygon converted = new Polygon(points);
       return converted;
    }
 
 }
