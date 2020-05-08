 package vooga.fighter.model.objects;
 
 import vooga.fighter.model.loaders.MapLoader;
 import vooga.fighter.model.utils.UpdatableLocation;
 import vooga.fighter.util.*;
 import java.util.*;
 
 
 /**
  * Map class to contain, update, and manipulate all environmental objects in a
  * particular map. Also contains the background image, music, and player start
  * locations.
  * 
  * @author James Wei, matthewparides, David Le
  * 
  */
 
 public class MapObject extends GameObject {
 
     private List<EnvironmentObject> myEnviroObjects;
     private List<UpdatableLocation> myStartingPositions;
     private Map<String,Sound> mySounds;
     private Sound myCurrentSound;
     private String myName;
 
     /**
      * Constructor for a new Map object.
      */
     public MapObject(String mapName) {
         super();
         myEnviroObjects = new ArrayList<EnvironmentObject>();
         myStartingPositions = new ArrayList<UpdatableLocation>();
         mySounds = new HashMap<String,Sound>();
         myCurrentSound = null;
         myName = mapName;
         setLoader(new MapLoader(mapName, this));
         setCurrentState("background");
         setImageData();
     }
 
     /**
      * Adds an environment object to the map object.
      */
     public void addEnviroObject(EnvironmentObject object) {
         myEnviroObjects.add(object);
         object.setImageData();
     }
     
     /**
      * removes an environment object from the map object
      * @param object - object to be removed
      */
     public void removeEnviroObject(EnvironmentObject object) {
     	myEnviroObjects.remove(object);
     }
     
     /**
      * returns the name of this map.
      * @return myName - this map's name.
      */
     public String getName() {
     	return myName;
     }
 
     /**
      * Returns the list of environment objects in the map object.
      */
     public List<EnvironmentObject> getEnviroObjects() {
         return myEnviroObjects;
     }
 
     /**
      * Clears all environment objects from the map object.
      */
     public void clearEnviroObjects() {
         myEnviroObjects.clear();
     }
     
     /**
      * Adds a starting position to the map object.
      */
    public void addStartPosition(UpdatableLocation position) {
        myStartingPositions.add(position);
     }
     
     /**
      * Returns the list of starting positions from the map object.
      */
     public List<UpdatableLocation> getStartPositions() {
         return myStartingPositions;
     }
     
     /**
      * Adds a sound to the map object. Overwrites any existing value.
      */
     public void addSound(String key, Sound sound) {
         mySounds.put(key, sound);
     }
     
     /**
      * Clears all sounds from the map object.
      */
     public void clearSounds() {
         mySounds.clear();
     }
     
     /**
      * Sets the current sound of the map object. Does nothing if the given key is
      * not found in the map.
      */
     public void setCurrentSound(String key) {
         if (mySounds.containsKey(key)) {
             myCurrentSound = mySounds.get(key);
         }
     }
     
     /**
      * Returns the current sound of the map object.
      */
     public Sound getCurrentSound() {
         return myCurrentSound;
     }
     
     /**
      * Begins playing the current sound.
      */
     public void playCurrentSound() {
         myCurrentSound.play();
     }
     
     /**
      * Updates all environmental objects in the map object.
      */
     public void completeUpdate() {                        
         if (getCurrentState().hasCompleted()) {
             getCurrentState().resetState();
         }
         if (myCurrentSound != null) {
 
         }
     }
 
     /**
      * Nothing for now, will refactor GameObject to remove this method.
      */
     public void applyCollideEffect(GameObject o) {
         
     }
 
     /**
      * Nothing for now, just return false. Never need to remove map.
      */
     public boolean shouldBeRemoved() {
         return false;
     }
 
 
     public void dispatchCollision (GameObject other) {
         
     }
 
     public void handleCollision (CharacterObject other) {
         
     }
 
 
     public void handleCollision (AttackObject other) {
         
     }
 
     public void handleCollision (EnvironmentObject other) {
         
     }
     
 }
