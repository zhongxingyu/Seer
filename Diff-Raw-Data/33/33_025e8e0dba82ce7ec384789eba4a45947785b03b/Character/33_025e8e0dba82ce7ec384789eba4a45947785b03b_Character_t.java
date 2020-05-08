 /**
  * @author Kuang Han
  */
 
 package core.characters;
 
 import java.util.HashMap;
 import java.util.Map;
 import levelio.Modifiable;
 
 import com.golden.gamedev.GameObject;
 
 @SuppressWarnings("serial")
 public abstract class Character extends GameElement {
 
    @Modifiable(classification = Modifiable.Classification.GAMEPLAY)
     protected double myHitPoints;
 
     protected transient Map<String, Double> myStateValues, myBaseValues;
 
     public Character(GameObject game) {
 	super(game);
 	myBaseValues = new HashMap<String, Double>();
 	myStateValues = new HashMap<String, Double>();
     }
 
     public Character() {
 	super();
     }
 
     public void addState(String attribute, double defaultValue) {
 	myBaseValues.put(attribute, defaultValue);
 	myStateValues.put(attribute, myBaseValues.get(attribute));
 
     }
 
     public void updateBaseValues(String attribute, double newValue) {
 	if (!myBaseValues.containsKey(attribute)) {
 	    myBaseValues.put(attribute, (double) 0);
 	}
 	myBaseValues.put(attribute, myBaseValues.get(attribute) + newValue);
 	myStateValues.put(attribute, myBaseValues.get(attribute));
     }
 
     public double getMyBaseValue(String attribute) {
 	return myBaseValues.get(attribute);
     }
 
     public void updateStateValues(String attribute, double newValue) {
 	if (!myBaseValues.containsKey(attribute)) {
 	    myBaseValues.put(attribute, (double) 0);
 	}
 	myStateValues.put(attribute, myBaseValues.get(attribute) + newValue);
     }
 
     public double getMyStateValue(String attribute) {
 	return myStateValues.get(attribute);
     }
 
     public double getMyHP() {
 	return myHitPoints;
     }
 
     public void setMyHP(double hp) {
 	myHitPoints += hp;
     }
 
 }
