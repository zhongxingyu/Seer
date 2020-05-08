 package edu.victone.scrabblah.logic.common;
 
import com.google.common.base.*;

 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/17/13
  * Time: 7:13 PM
  */
 
 public class Coordinate {

  public static final Coordinate CENTER = new Coordinate(7, 7);

   private final int x, y;
 
   public static int characterToInt(Character value) {
     return Character.getNumericValue(Character.toLowerCase(value)) - 10;
   }
 
   public Coordinate(int x, int y) {
     if ((x < 0) || (x > 14)) {
       throw new IllegalArgumentException("Illegal X Coordinate value: " + x);
     } else if ((y < 0) || (y > 14)) {
       throw new IllegalArgumentException("Illegal Y Coordinate value: " + y);
     } else {
       this.x = x;
       this.y = y;
     }
   }
 
   public Integer getX() {
     return x;
   }
 
   public Integer getY() {
     return y;
   }
 
   @Override
   public String toString() {
     return "(" + (char) (x + 65) + ", " + (y + 1) + ')';
   }
 
   @Override
   public boolean equals(Object o) {
     if (o == null) {
       return false;
     }
 
     if (getClass() != o.getClass()) {
       return false;
     }
 
     final Coordinate other = (Coordinate) o;
     return x == other.x && y == other.y;
   }
 
   @Override
   public int hashCode() {
    return Objects.hashCode(x, y);
   }
 }
