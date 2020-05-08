 package org.skullforge.asteroidpush.utils;
 
 import static org.junit.Assert.*;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.junit.Test;
 
 public class GeometryVerifierTest {
    @Test
    public void testIsWoundCorrectly() {
       // jbox2d updated the PolygonShape.set() method to automatically fix
       // miswound or malformed shapes. Thus the GeometryVerifier is essentially
       // no longer needed.      
       
       PolygonShape shape = new PolygonShape();
 
       Vec2 boxVertices[] = new Vec2[] {
             new Vec2(0.25f, 0.25f),
             new Vec2(-0.25f, 0.25f),
             new Vec2(-0.25f, -0.25f),
             new Vec2(0.25f, -0.25f)
       };
       shape.set(boxVertices, boxVertices.length);
       assertTrue(GeometryVerifier.IsWoundCorrectly(shape));
 
       Vec2 miswoundBoxVertices[] = new Vec2[] {
             new Vec2(0.25f, -0.25f),
             new Vec2(-0.25f, -0.25f),
             new Vec2(-0.25f, 0.25f),
             new Vec2(0.25f, 0.25f)
       };
       shape.set(miswoundBoxVertices, miswoundBoxVertices.length);
       assertTrue(GeometryVerifier.IsWoundCorrectly(shape));
 
       Vec2 potatoVertices[] = new Vec2[] {
             new Vec2(4.0f, 4.0f),
             new Vec2(5.0f, 3.0f),
             new Vec2(4.0f, 1.0f),
             new Vec2(2.0f, 2.0f),
             new Vec2(3.0f, 3.0f),
             new Vec2(2.0f, 5.0f)
       };
       shape.set(potatoVertices, potatoVertices.length);
       assertTrue(GeometryVerifier.IsWoundCorrectly(shape));
    }
 }
