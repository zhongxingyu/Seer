 /*******************************************************************************
  *  Copyright (C) 2013 Justin Stoecker
  *  The MIT License. See LICENSE in project root.
  *******************************************************************************/
 package roboviz.draw.drawable;
 
 import javax.media.opengl.GL2;
 
 import jgl.math.vector.Vec2f;
 import jgl.math.vector.Vec3f;
 import jgl.scene.geometry.CircleGeometry;
 import jgl.scene.geometry.Geometry;
 import roboviz.util.GeometryCache;
 
 public class Circle extends Drawable implements GeometryCache.Generator {
 
   public static final GeometryCache<Circle> cache = new GeometryCache<Circle>(10);
 
   public final Vec2f                  position;
   public final float                  radius;
   public final float                  thickness;
   public final Vec3f                  color;
 
   public Circle(Vec2f position, float radius, float thickness, Vec3f color, String setName) {
     super(setName);
     this.position = position;
     this.radius = radius;
     this.thickness = thickness;
     this.color = color;
   }
 
   @Override
   public Geometry newGeometry() {
     return CircleGeometry.aroundZ(radius - thickness, radius, 32);
   }
 
   @Override
   public void render(GL2 gl) {
    
    System.out.println("circle draw " + position);
    
     gl.glPushMatrix();
     gl.glTranslatef(position.x, position.y, 0);
     gl.glColor3f(color.x, color.y, color.z);
     cache.render(gl, this);
     gl.glPopMatrix();
   }
 
   @Override
   public int hashCode() {
     final int prime = 31;
     int result = 1;
     result = prime * result + Float.floatToIntBits(radius);
     result = prime * result + Float.floatToIntBits(thickness);
     return result;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (this == obj)
       return true;
     if (obj == null)
       return false;
     if (getClass() != obj.getClass())
       return false;
     Circle other = (Circle) obj;
     if (Float.floatToIntBits(radius) != Float.floatToIntBits(other.radius))
       return false;
     if (Float.floatToIntBits(thickness) != Float.floatToIntBits(other.thickness))
       return false;
     return true;
   }
 }
