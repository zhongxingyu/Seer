 package vooga.fighter.objects;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import vooga.fighter.input.AlertObject;
 import vooga.fighter.input.Input;
 import vooga.fighter.input.InputClassTarget;
 import vooga.fighter.input.InputMethodTarget;
 import vooga.fighter.objects.utils.Effect;
 import vooga.fighter.objects.utils.Health;
 
 import vooga.fighter.util.Location;
 import vooga.fighter.util.Pixmap;
 import vooga.fighter.util.Vector;
 import vooga.fighter.objects.utils.UpdatableLocation;
 
 
 /**
  * Represents a character in the game, as well as the character's current state
  * in the given session.
  * 
  * @author alanni, james
  * 
  */
 public class CharacterObject extends GameObject {
 
     private Map<String, AttackObject> myAttacks;
     private List<Effect> myActiveEffects;
     private Health myHealth;
     private int mySpeed;  
     private long myInstanceId;
     private Pixmap myImage;
     private Location myCenter;
     private Dimension mySize;
     
 
     /**
      * Constructs a new CharacterObject.
      * 
      * Note: Dayvid once the object loader is functional we will replace this
      * constructor to take in just an ID, then we will load parameters from XML.
      */
     public CharacterObject (long instanceId, int objectId, UpdatableLocation center) {
 
 //        myLoader = new CharacterObjectLoader(objectId);
         super();
        setLoader(new CharacterLoader(objectId, this));
         setLocation(center);
 
     }
 
     /**
      * Updates the character for one game loop cycle. Applies movement from acceleration
      * forces acting on the character.
      */
 
     public void update () {
         super.update();
         for (Effect effect : myActiveEffects) {
             effect.update();
         }
     }
 
     /**
      * Adds an effect to this character's list of active effects.
      */
     public void addActiveEffect (Effect effect) {
         effect.setOwner(this);
         myActiveEffects.add(effect);
     }
 
     /**
      * Removes an effect from this character's list of active effects.
      */
     public void removeActiveEffect (Effect effect) {
         myActiveEffects.remove(effect);
     }
 
     /**
      * Returns list of currently active effects on this character.
      */
     public List<Effect> getActiveEffects () {
         return myActiveEffects;
     }
 
     /**
      * Adds an AttackObject to the list of attacks available for this character.
      * Note that this attack will not be added to the list of game objects in a
      * level, thus it will not update and should be used solely for generating
      * other attack objects as needed. Overwrites any existing attack.
      */
     public void addAttack (String key, AttackObject object) {
         myAttacks.put(key, object);
     }
 
     /**
      * Creates and returns an attack object based on a given identifier. Returns null
      * if the specified attack does not exist.
      * 
      * Note: For now just using String to represent attack types, but this is obviously
      * subject to change.
      */
     private AttackObject createAttack (String key) {
         if (myAttacks.containsKey(key)) {
             return myAttacks.get(key);
         }
         else {
             return null;
         }
     }
 
     /**
      * Returns the health of the character.
      */
     public Health getHealth () {
         return myHealth;
     }
 
     /**
      * Returns whether or not the character has remaining health.
      */
     public boolean hasHealthRemaining () {
         return myHealth.hasHealthRemaining();
     }
 
     /**
      * Changes the player's health by a given amount. Positive input raises it, and
      * negative input decreases it. Returns health remaining.
      */
     public int changeHealth (int amount) {
         return myHealth.changeHealth(amount);
     }
     
     /**
      * Creates a new attack based on type of attack on current 
      * @param attack
      */
     public void attack(String attack){
     	if (myAttacks.containsKey(attack)){
     		new AttackObject(myAttacks.get(attack), getLocation());
     	}
     }
     
     /**
      * Moves in given direction at speed of character
      * @param direction
      */
     public void move(int direction){
     	getLocation().translate(new Vector(direction,mySpeed));
     }
     
     /**
      * Will add jump method 
      */
     public void jump(){
     	
     }
 }
