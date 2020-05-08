 package chum.fp;
 
 import java.nio.IntBuffer;
 
 
 /** 
     A 2-dimensional Fixed-point vector
     Uses (u,v) because it's primarily intended to be used
     for texcoords.
  */
 public class Vec2 {
 
     public int u;
     public int v;
     
     public Vec2() {
         this.u = 0;
         this.v = 0;
     }
 
     public Vec2(Vec2 v) {
         set(v);
     }
 
     public Vec2( int u, int v ) {
         this.u = u;
         this.v = v;
     }
 
     public Vec2( float u, float v ) {
         this.u = FP.floatToFP(u);
         this.v = FP.floatToFP(v);
     }
 
     public boolean equals(Object other) {
         if (other instanceof Vec2) {
             Vec2 o = (Vec2)other;
             return (u == o.u && v == o.v);
         }
         return false;
     }
 
 
     public final void set (Vec2 o) {
         u = o.u;
         v = o.v;
     }
 
 
     public final void put ( IntBuffer buffer ) {
         buffer.put(u);
         buffer.put(v);
     }
 
 
     public final void put ( int[] vertBuffer, int offset ) {
         vertBuffer[offset++] = u;
         vertBuffer[offset++] = v;
     }
 
 
     @Override
     public String toString() {
        return "("+
            FP.toFloat(u)+","+
            FP.toFloat(v)+")";
     }
 
 }
