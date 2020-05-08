 package vooga.fighter.model.objects;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import util.Location;
 import util.Vector;
 import vooga.fighter.model.ModelConstants;
 import vooga.fighter.model.loaders.AttackObjectLoader;
 import vooga.fighter.model.utils.Counter;
 import vooga.fighter.model.utils.Effect;
 import vooga.fighter.model.utils.State;
 import vooga.fighter.model.utils.UpdatableLocation;
 
 /**
  * Object that can inflict damage on other moveable game objects
  * 
  * @author James Wei, alanni, David Le
  *  
  */
 public class AttackObject extends GameObject {
 
     private Counter myCounter;
     private GameObject myOwner;
     private List<Effect> myEffects;
     
     /**
      * Constructs an AttackObject with the given owner.
      * 
      * @param name of the attack object as labeled in its respective xml
      * @param pathHierarchy is the path to the game resources folder
      */
     public AttackObject (String name, String pathHierarchy) {
         super();
         myEffects = new ArrayList<Effect>();
         myCounter = new Counter();
         setLoader(new AttackObjectLoader(name, this, pathHierarchy));
         setToDefaultState();
     }
     
     /**
      * Creates a deep copy of attack object based on other attack object and character location.
      * 
      * @param other is the other AttackObject to deep copy
      * @param center is the location of the new AttackObject
      */
     public AttackObject (AttackObject other, UpdatableLocation center) {
         super();
         addProperty(ModelConstants.ATTACK_PROPERTY_SPEED, 
                     other.getProperty(ModelConstants.ATTACK_PROPERTY_SPEED));
         addProperty(ModelConstants.ATTACK_PROPERTY_DIRECTION, 
                     other.getProperty(ModelConstants.ATTACK_PROPERTY_DIRECTION));
         addProperty(ModelConstants.ATTACK_PROPERTY_DAMAGE, 
                     other.getProperty(ModelConstants.ATTACK_PROPERTY_DAMAGE));
         addProperty(ModelConstants.ATTACK_PROPERTY_DURATION, 
                     other.getProperty(ModelConstants.ATTACK_PROPERTY_DURATION));
         this.myEffects = other.myEffects;
         this.myOwner = other.myOwner;
        this.myCounter = new Counter(getProperty(ModelConstants.ATTACK_PROPERTY_DURATION));   
         setLocation(center);
         copyStates(other);
         setCurrentState(other.getCurrentStateKey());
         addStartingAcceleration();
         setImageData();
     }
 
     /**
      * Creates a deep copy of another AttackObject's state map and sets it as this
      * object's state map.
      * @param other is the AttackObject to copy states from.
      */
     public void copyStates(AttackObject other) {
         Map<String, State> otherStates = other.getStates();
         for (String key : otherStates.keySet()) {
             State otherState = otherStates.get(key);
             State newState = new State(otherState);
             newState.setOwner(this);
             addState(key, newState);
         }
     }
 
     /**
      * Move the attack object to the position of its owner.
      */
     public void moveToOwner() {
         UpdatableLocation copyLocation = myOwner.getLocation();
         UpdatableLocation myLocation = getLocation();
         Location newLocation = new Location(copyLocation.getLocation());
         myLocation.setLocation(newLocation);
     }
     
     
     /**
      * Updates the attack object by decreasing its counter.
      */
     @Override
 	public void completeUpdate(){
     	myCounter.decrementCounter();
     }
     
     /**
      * Adds the initial acceleration of the attack object to the location's list
      * of accelerations.
      */
     public void addStartingAcceleration() {
         int direction = getProperty(ModelConstants.ATTACK_PROPERTY_DIRECTION);
         int speed = getProperty(ModelConstants.ATTACK_PROPERTY_SPEED);
         Vector acceleration = new Vector(direction, speed);
         getLocation().addAcceleration(acceleration);
     }
     
     /**
      * Adds an effect to myEffects
      * @param toAdd is the effect to add to the list
      */
     public void addEffect(Effect toAdd) {
         myEffects.add(toAdd);
     }
     
     /**
      * Returns the list of effects carried by this object.
      */
     public List<Effect> getEffects() {
         return myEffects;
     }
     
     /**
      * Gets the character who created the attack
      */
     public GameObject getOwner() {
         return myOwner; 
     }
     
     /**
      * Sets the owner to the creator of the attack
      * @param owner is the ownern of this attack
      */
     public void setOwner(CharacterObject owner) {
         myOwner = owner;
     }
     
     /**
      * Sets the owner for effects
      * @param owner is the owner for the effects
      */
     public void setOwnerForEffects(CharacterObject owner) {
         for (Effect effect: myEffects) {
             effect.setOwner(owner);
         }
     }
     /**
      * Inflicts damage upon a target player.
      * @param target is the character taking damage from this object
      */
     public int inflictDamage(CharacterObject target) {
         int damage = getProperty(ModelConstants.ATTACK_PROPERTY_DAMAGE);
         return target.changeHealth(-damage);
     }
     
     /**
      * Designates a character as a target of this attack's effects.
      * @param target is the target to add this attack's effects to
      */
     public void addTargetForEffects(CharacterObject target) {
         for (Effect effect : myEffects) {
             Effect copyOfEffect = effect.getCloneOfEffect();
             target.addActiveEffect(copyOfEffect);
         }
     }        
     
     /**
      * Sets the counter to the current amount
      * @param amount is the value to set the counter to 
      */
     public void setCounter(int amount) {
         myCounter.setCounter(amount); 
     }
     
     /**
      * Sets the counter to zero. 
      */    
     public void endCounter() {
         setCounter(ModelConstants.ATTACK_COUNTER_ZERO);
     }
     
     /**
      * Returns true if this attack object has expired.
      */
     @Override
 	public boolean shouldBeRemoved() {
         return !myCounter.hasCountRemaining();
     }      
 
 }
