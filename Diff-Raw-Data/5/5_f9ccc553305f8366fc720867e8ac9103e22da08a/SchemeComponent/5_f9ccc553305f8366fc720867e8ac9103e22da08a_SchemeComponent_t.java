 package de.hswt.hrm.scheme.model;
 
 import static com.google.common.base.Preconditions.*;
 
 /**
  * This class represents the position of a component in a scheme grid.
  * 
  * @author Michael Sieger
  *
  */
 public class SchemeComponent {
 
     private int id;
     private int x;
     private int y;
     
     
     public SchemeComponent(int id, int x, int y) {
         this.id = id;
         setX(x);
         setY(y);
     }
     
     public SchemeComponent(int x, int y){
         this(-1, x, y);
     }
 
     public int getId() {
         return id;
     }
 
     public int getX() {
        checkArgument(x >= 0);
         return x;
     }
 
     public void setX(int x) {
        checkArgument(y >= 0);
         this.x = x;
     }
 
     public int getY() {
         return y;
     }
 
     public void setY(int y) {
         this.y = y;
     }
     
     
 }
