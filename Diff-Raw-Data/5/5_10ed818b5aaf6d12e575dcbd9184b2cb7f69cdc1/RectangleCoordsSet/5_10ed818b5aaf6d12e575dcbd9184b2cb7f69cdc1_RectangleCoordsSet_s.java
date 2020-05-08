 package org.sankozi.rogueland.model.coords;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.util.AbstractSet;
 import java.util.Iterator;
 import static com.google.common.base.Preconditions.checkArgument;
 
 /**
  * Immutable set containing int coordinates inside rectangle.
  *
  * @author sankozi
  */
 final class RectangleCoordsSet extends AbstractSet<Coords> {
     private final static Logger LOG = LogManager.getLogger(RectangleCoordsSet.class);
     /** Lowest x coordinate of coords inside set */
     private final int xMin;
     /** Lowest y coordinate of coords inside set */
     private final int yMin;
     /** Highest x coordinate of coords inside set */
     private final int xMax;
     /** Highest y coordinate of coords inside set */
     private final int yMax;
 
     private final int size;
 
     RectangleCoordsSet(int xMin, int yMin, int xMax, int yMax) {
         checkArgument(xMax > xMin, "xMax (%s) must be larger than xMin (%s)", xMax, xMin);
         checkArgument(yMax > yMin, "yMax (%s) must be larger than yMin (%s)", yMax, yMin);
         this.xMin = xMin;
         this.yMin = yMin;
         this.xMax = xMax;
         this.yMax = yMax;
 
         this.size = (xMax - xMin + 1) * (yMax - yMin + 1);
     }
 
     @Override
     public String toString() {
         return "{(x,y) :  " + xMin + " <= x <= "+ xMax + " , " + yMin +  " <= y <= " + yMax + " }";
     }
 
     @Override
     public boolean contains(Object o) {
         if(o instanceof Coords){
             Coords coords = (Coords) o;
             return xMin <= coords.x && coords.x <= xMax && yMin <= coords.y && coords.y <= yMax;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean isEmpty() {
         return false;
     }
 
     @Override
     public Iterator<Coords> iterator() {
         return new HorizontalThenVerticalIterator();
     }
 
     @Override
     public int size() {
         return size;
     }
 
    private final class HorizontalThenVerticalIterator implements Iterator{
         int x = xMin;
         int y = yMin;
 
         @Override
         public boolean hasNext() {
             return x <= xMax && y <= yMax;
         }
 
         @Override
        public Object next() {
             Coords ret = new Coords(x, y);
             if(x == xMax) {
                 x = xMin;
                 y++;
             } else {
                 x++;
             }
             return ret;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 }
