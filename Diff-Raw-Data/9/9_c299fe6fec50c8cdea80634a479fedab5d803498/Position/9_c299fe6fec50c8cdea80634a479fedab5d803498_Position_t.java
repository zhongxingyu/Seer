 /*
  */
 
 package tajmi.data.som;
 
 /**
  * Used to index the position within the Field.
  * Should only be used withing the tajmi.data.clusterable.som namespace
  * and any classes that the implement the interfaces therein.
  * 
  * @author badi
  */
 public class Position {
 
    private int x, y;
 
     public Position (int x, int y) {
         this.x = x;
         this.y = y;
     }
 
     public int x() {
         return x;
     }
 
     public int y() {
         return y;
     }
 }
