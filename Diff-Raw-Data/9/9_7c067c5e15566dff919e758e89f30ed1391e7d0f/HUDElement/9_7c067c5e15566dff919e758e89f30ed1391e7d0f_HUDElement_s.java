 package vooga.fighter.view;
 
 import java.awt.geom.Point2D;
 import java.lang.reflect.Field;
 import java.util.Observer;
 
import vooga.fighter.util.Paintable;
 
 /**
  * Acts as a display for model data. Throws NullPointerExceptions on Update.
  * (Update method signature would not be properly overridden if it was given
  * throw statements.) ALWAYS APPEND SUBCLASSES WITH "HUD", e.g. HUDHistogram.
  * 
  * @author Wayne You
  * 
  */
 public abstract class HUDElement implements Observer, Paintable {
     public static final int  DEFAULT_TEXT_HEIGHT = 20;
     
     protected String         myName;
     protected String         myFieldName;
     protected Point2D.Double myLocation          = new Point2D.Double();
     
     public void setName(String name) {
         myName = name;
     }
     
     public void setObservedValue(String fieldName) {
         myFieldName = fieldName;
     }
     
     /**
      * Reflects on the observed object to retrieve the information.
      * 
      * @param o
      *            The observed object.
      * @return The observed member variable.
      * @throws SecurityException
      *             Thrown when attempting to change access level of a private
      *             member
      * @throws NoSuchFieldException
      *             Thrown when the saved field name is wrong
      * @throws IllegalArgumentException
      *             Thrown if the object given ceases to be the right
      *             class/extant
      * @throws IllegalAccessException
      *             Thrown when attempting to access a private member without
      *             access granted
      */
     protected Object getObservedValue(Object o) throws SecurityException,
             NoSuchFieldException, IllegalArgumentException,
             IllegalAccessException {
         Field member = o.getClass().getDeclaredField(myFieldName);
         member.setAccessible(true);
         return member.get(o);
     }
     
     public void setLocation(double x, double y) {
         myLocation.x = x;
         myLocation.y = y;
     }
     
     public void setLocation(Point2D location) {
         myLocation.x = location.getX();
         myLocation.y = location.getY();
     }
 }
