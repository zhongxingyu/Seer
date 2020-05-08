 /*******************************************************************************
  *  Copyright (C) 2013 Justin Stoecker
  *  The MIT License. See LICENSE in project root.
  *******************************************************************************/
 package roboviz.draw.drawable;
 
 import javax.media.opengl.GL2;
 
 import jgl.math.vector.Vec3f;
 import jgl.scene.geometry.Geometry;
 import jgl.scene.geometry.SphereGeometry;
 import roboviz.util.GeometryCache;
 
 public class Sphere extends Drawable implements GeometryCache.Generator {
 
   public static final GeometryCache<Sphere> cache = new GeometryCache<Sphere>(10);
 
   public final Vec3f                         position;
   public final float                         radius;
   public final Vec3f                         color;
 
   public Sphere(Vec3f position, float radius, Vec3f color, String setName) {
     super(setName);
     this.position = position;
     this.radius = radius;
     this.color = color;
   }
 
   @Override
   public Geometry newGeometry() {
     return new SphereGeometry(radius, 8);
   }
   
   @Override
   public void render(GL2 gl) {
     gl.glPushMatrix();
     gl.glTranslatef(position.x, position.y, 0);
     gl.glColor3f(color.x, color.y, color.z);
     cache.render(gl, this);
     gl.glPopMatrix();
   }
 }
