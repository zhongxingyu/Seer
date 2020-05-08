 package vooga.towerdefense.attributes;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import util.Location;
 
 
 /**
  * Shell of an attribute class, allows for tracking and modification.
  * Value is in double, can be reset, and add temporary value, can also paint a bar
  * representing its value
  * 
  * @author Matthew Roy
  * @author XuRui
  * @author Zhen Gou
  * 
  */
 public class Attribute {
     private String myName;
     private double myOriginalValue;
     private double myCurrentValue;
     private double myTemporaryBuffValue;
 
     public Attribute (String attributeName, Double attributeValue) {
         myName = attributeName;
         myOriginalValue = attributeValue;
         myCurrentValue = myOriginalValue;
     }
 
     /**
      * Creates a copy of an existing attribute
      * Note: It sets its default value to the current value
      * 
      * @param toCopy
      */
     public Attribute (Attribute toCopy) {
         this(toCopy.getName(), toCopy.getValue());
     }
 
     /**
      * Applies a new attribute to the current attribute.
      * This default behavior is defined by the attribute.
      * Default is to set value equal to attribute toApply's value
      * if they are of the same attribute type
      * 
      * @return the new value
      */
     public Object applyAttribute (Attribute toApply) {
         if (toApply.getClass().equals(this)) {
             myCurrentValue = toApply.getValue();
         }
         return myCurrentValue;
     }
 
     /**
      * should be called by update in attributeManager
      */
     public void update () {
         resetBuffValue();
     }
 
     /**
      * Formats a string for displaying the value.
      * Set to a formatted toString as default
      * 
      * @return
      */
     public String getDisplayableInfo () {
         return toString();
     }
 
     /**
      * Sets toString to be formatted nicely for attributes
      */
     @Override
     public String toString () {
         String info = myName + ": " + String.valueOf(myCurrentValue);
         return info;
     }
 
     /**
      * Returns name of the stat
      * 
      * @return
      */
     public String getName () {
         return myName;
     }
 
     /**
      * Returns value of the stat
      * 
      * @return
      */
     public double getValue () {
         return myCurrentValue + myTemporaryBuffValue;
     }
 
     public void setValue (double newValue) {
         myCurrentValue = newValue;
     }
 
     /**
      * Adds the given parameter to the value.
      * Negative values subtract
      * 
      * @param valueToAdd
      */
     public void modifyValue (double valueToAdd) {
         myCurrentValue += valueToAdd;
     }
 
     /**
      * Multiplies the current value. Values over 1 increase it.
      * Values < 1 decrease
      * 
      * @param multiplierToApply
      */
     public void applyMultiplier (double multiplierToApply) {
         myCurrentValue *= multiplierToApply;
     }
 
     /**
      * paints a bar representing this attribute
      */
     public void paintBar (Graphics2D pen, Location where, Dimension size) {
         pen.fillRect((int) where.getX() - size.width / 2, (int) where.getY() - size.height / 2,
                      (int) (size.getWidth() * (getValue() /
                     getOriginalValue())), (int) 10);
     }
 
     /**
      * Returns the original value of this attribute
      * @return 
      */
     public double getOriginalValue () {
         return myOriginalValue;
     }
 
     /**
      * check whether this stat is different from its original value
      * 
      * @return
      */
     public boolean isChanged () {
         return myOriginalValue != myCurrentValue;
     }
 
     /**
      * reset current value to original value;
      */
     public void reset () {
         myCurrentValue = myOriginalValue;
         resetBuffValue();
     }
 
     /**
      * reset buff value to 0;
      */
     public void resetBuffValue () {
         myTemporaryBuffValue = 0;
     }
 
     /**
      * increase the current buff of this attribute
      * 
      * @param buff
      */
     public void addToBuffValue (double buff) {
         myTemporaryBuffValue += buff;
     }
 
 }
