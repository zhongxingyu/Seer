 package ep.common;
 
 import ucar.ma2.Array;
 import ucar.ma2.Index;
 
 public class Grid {
   public Array surface;
 
   public float baseLon;
   public float baseLat;
 
   public float resLon;
   public float resLat;
 
   public long getSize() {
     return surface.getSize();
   }
 
   public Array getSurface() {
     return surface;
   }
 
   public void setSurface(Array surface) {
     this.surface = surface;
   }
 
   public int[] getShape() {
     return surface.getShape();
   }
 
   public Index getIndex() {
     return surface.getIndex();
   }
 
 
 
   public void globalize() {
     baseLon = 0;
     baseLat = 0;
 
     int[] shape = getShape();
     resLat = 180.0f / shape[0];
     resLon = 360.0f / shape[1];
   }
 
 
   private Grid cloneWithProperties() {
     Grid ng = new Grid();
 
     ng.baseLat = baseLat;
     ng.baseLon = baseLon;
 
     ng.resLat = resLat;
     ng.resLon = resLon;
 
     return ng;
   }
 
 
   public Grid empty() {
     Grid ng = cloneWithProperties();
     ng.surface = Array.factory(surface.getElementType(), surface.getShape());
     return ng;
   }
 
   public Grid copy() {
     Grid ng = cloneWithProperties();
     ng.surface = surface.copy();
     return ng;
   }
 
 
   public void floatScale(Grid g) {
     if (g.getSize() != getSize()) {
       throw new IllegalArgumentException("not equal gridding added: " + getSize() + " + " + g.getSize());
     }
 
     long size = getSize();
     float[] faciend = (float[])getSurface().getStorage();
     float[] factor = (float[])g.getSurface().getStorage();
 
     for (int i = 0; i < size; ++i) {
       faciend[i] *= factor[i];
     }
   }
 
 
   public void floatPlusWithFactor(Grid g, Grid factor) {
     if (g.getSize() != getSize()) {
       throw new IllegalArgumentException("not equal gridding added: " + getSize() + " + " + g.getSize());
     }
 
    float[] augend = (float[])surface.getStorage();
     float[] addend = (float[])g.getSurface().getStorage();
    float[] f = (float[])factor.getSurface().getStorage();

     int size = addend.length;
     for (int i = 0; i < size; ++i) {
       augend[i] += addend[i] * f[i];
     }
   }
 
   public void floatPlus(Grid g) {
     if (g.getSize() != getSize()) {
       throw new IllegalArgumentException("not equal gridding added: " + getSize() + " + " + g.getSize());
     }
 
     long size = getSize();
 
     float[] addend = (float[])g.getSurface().getStorage();
     float[] augend = (float[])surface.getStorage();
 
     for (int i = 0; i < size; ++i) {
       augend[i] += addend[i];
     }
   }
 
 
   public void floatScale(float f) {
     long size = getSize();
 
     float[] array = (float[])surface.getStorage();
 
     for (int i = 0; i < size; ++i) {
       array[i] *= f;
     }
   }
 
 
   public void floatScale2(Grid baseFactor, Grid targetFactor) {
     if (getSize() != baseFactor.getSize())
       throw new IllegalArgumentException("not equal grid added (base): " + getSize() + " + " + baseFactor.getSize());
     if (getSize() != targetFactor.getSize())
       throw new IllegalArgumentException("not equal grid added (target): " + getSize() + " + " + targetFactor.getSize());
 
     int size = (int)getSize();
     float[] cur = (float[])getSurface().getStorage();
     float[] base = (float[])baseFactor.getSurface().getStorage();
     float[] target = (float[])targetFactor.getSurface().getStorage();
 
     for (int i = 0; i < size; ++i) {
       if (base[i] == 0)
         continue;
 
       if (target[i] == 0) {
         cur[i] = 0;
         continue;
       }
 
       cur[i] = cur[i] * target[i] / base[i];
     }
   }
 }
