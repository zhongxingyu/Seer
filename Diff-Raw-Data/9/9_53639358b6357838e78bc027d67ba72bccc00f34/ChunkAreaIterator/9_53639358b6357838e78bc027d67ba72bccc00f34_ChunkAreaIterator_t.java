 package me.riking.templateworlds.impl.common;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
import org.apache.commons.lang.Validate;
 import org.bukkit.util.Vector;
 
 public class ChunkAreaIterator implements Iterator<Vector> {
     private final int minX/*, minZ*/;
     private final int maxX, maxZ;
     private int curX, curZ;
 
     public ChunkAreaIterator(int minx, int minz, int maxx, int maxz) {
         Validate.isTrue(minx <= maxx);
         Validate.isTrue(minz <= maxz);
         minX = minx;
         //minZ = minz;
         maxX = maxx;
         maxZ = maxz;
         curX = minX;
         curZ = minz;
     }
 
     // Order: xxxZxxxZxxxZ
     // Create return THEN increment current
 
     public boolean hasNext() {
         return curZ <= maxZ;
     }
 
     public Vector next() {
         if (curZ > maxZ) {
             throw new NoSuchElementException("End of iteration");
         }
 
         Vector ret = new Vector(curX, 0, curZ);
         curX++;
         if (curX > maxX) {
             curX = minX;
             curZ++;
         }
         return ret;
     }
 
     public void remove() {
         throw new UnsupportedOperationException("Cannot remove from a numerical sequence");
     }
 }
