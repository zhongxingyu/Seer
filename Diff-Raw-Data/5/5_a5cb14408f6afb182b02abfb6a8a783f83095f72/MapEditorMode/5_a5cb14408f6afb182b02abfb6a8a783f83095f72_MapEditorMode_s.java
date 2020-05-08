 package vooga.fighter.model;
 
 import java.awt.Dimension; 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import util.Location;
 import util.input.src.input.PositionObject;
 import vooga.fighter.model.loaders.EnvironmentObjectLoader;
 import vooga.fighter.model.objects.CharacterObject;
 import vooga.fighter.model.objects.EnvironmentObject;
 import vooga.fighter.model.objects.GameObject;
 import vooga.fighter.model.objects.MapObject;
 import vooga.fighter.model.objects.MouseClickObject;
 import vooga.fighter.model.utils.ImageDataObject;
 import vooga.fighter.model.utils.State;
 import vooga.fighter.model.utils.UpdatableLocation;
 import vooga.fighter.util.CollisionManager;
 
 
 /**
  * 
 * @author matthewparides, james
  * 
  */
 public class MapEditorMode extends Mode {
 	//at this point we assume the max # of players is 4 (so 4 starting locations)
 	private static final int NUM_PLAYERS = 4;
 
     private String myMapName;
     private MapObject myMap;
     private List<EnvironmentObject> myEnviroObjects; //all environmental objects that can be placed
     private EnvironmentObject myCurrentSelection;
     private int myEnviroIndex; //the list index of the current environment object selected
     private int currentPlayer;
     private int numPlayers;
     private List<String> myBackgroundPaths; //filepath to background image
     private String mySoundPath; //filepath to sound
 
     public MapEditorMode (CollisionManager cd) {
         super(cd);
         currentPlayer = 0;
         myEnviroIndex = 0;
         numPlayers = NUM_PLAYERS;
         //myMapName = mapName;
         myMap = null;
         myEnviroObjects = new ArrayList<EnvironmentObject>();
         myCurrentSelection = null;
         EnvironmentObjectLoader loader = new EnvironmentObjectLoader();
         myEnviroObjects = (ArrayList<EnvironmentObject>)loader.getEnvironmentObjects();
         initializeEnviroObjects();
         myBackgroundPaths = new ArrayList<String>();
         
     }
 
     /**
      * Overrides superclass initialize method by creating all objects in the level.
      */
     public void initializeMode () {
         loadMap(myMapName);
     }
     
     public void setMap(MapObject map){
     	myMap = map;
     	addObject(map);
     }
     
     /**
      * initializes all of the environment objects for this editor to be displayed in the upper left hand corner
      * when selected. Also selects the first object as the current object.
      */
     public void initializeEnviroObjects() {
     	for(EnvironmentObject enviro: myEnviroObjects) {
     		double xOffset = enviro.getImageData().getSize().getWidth()/2;
     		double yOffset = enviro.getImageData().getSize().getHeight()/2;
     		ImageDataObject newImageLocation = new ImageDataObject(enviro.getImageData().getImage(),
     				new Location(xOffset, yOffset), enviro.getImageData().getSize(), new ArrayList<Integer>());
     		enviro.setImageData(newImageLocation);
     	}
     	addObject(myEnviroObjects.get(0));
     }
 
     /**
      * Updates level mode by calling update in all of its objects.
      */
     public void update (double stepTime, Dimension bounds) {
         List<GameObject> myObjects = getMyObjects();
         handleCollisions();
         for (int i = 0; i < myObjects.size(); i++) {
             GameObject object = myObjects.get(i);
             State state = object.getCurrentState();
             // System.out.printf("Updating %s:\n", object.getClass().toString());
             // System.out.printf("Object current state:\ncurrentFrame: %d\nnumFrames: %d\nNull checks:\nImage: %b\nRectangle: %b\nSize: %b\n",
             // state.myCurrentFrame, state.myNumFrames, (state.getCurrentImage()==null),
             // (state.getCurrentRectangle()==null),
             // (state.getCurrentSize()==null));
             object.update();
             if (object.shouldBeRemoved()) {
                 myObjects.remove(object);
                 i--;
             }
         }
     }
 
     /**
      * Loads the environment objects for a map using the ObjectLoader.
      */
     public void loadMap (String mapName) {
         myMap = new MapObject(mapName);
         addObject(myMap);
         List<EnvironmentObject> mapObjects = myMap.getEnviroObjects();
         for (EnvironmentObject object : mapObjects) {
             addObject(object);
         }
     }
 
 
     /**
      * Creates the list of image data objects and returns it.
      */
     public List<ImageDataObject> getImageData () {
         List<ImageDataObject> result = new ArrayList<ImageDataObject>();
         for (GameObject object : getMyObjects()) {
             result.add(object.getImageData());
         }
         return result;
     }
 
     /**
      * Carries out actions associated with a user selecting a location on the map.
      * If they pressed on an existing environment object, that object will be removed.
      * If they pressed an open space, the currently selected environment object will be
      * placed in that location.
      * @param point
      */
     public void objectSelect (Point2D point) {
     	MouseClickObject click = new MouseClickObject(point);
     	addObject(click);
     	handleCollisions();
     	removeObject(click);
     	boolean removeExecuted = false;
     	for(GameObject obj: getMyObjects()) {
     		if(obj.shouldBeRemoved()) {
     			removeObject(obj);
     			myMap.removeEnviroObject((EnvironmentObject)obj);
     			removeExecuted = true;
     		}
     		
     	}
     	if(!removeExecuted) {
 	    	UpdatableLocation currentLoc = new UpdatableLocation(point.getX(), point.getY());
 	    	EnvironmentObject newObj = new EnvironmentObject(myCurrentSelection.getName(), currentLoc);
 	    	myMap.addEnviroObject(newObj);
 	    	addObject(newObj);
     	}
     }
     
     /**
      * sets the starting position of the currently selected player
      * @param loc
      */
     public void startLocSelect(Point2D loc) {
     	myMap.setStartPosition(currentPlayer, new UpdatableLocation(loc.getX(), loc.getY()));
     }
     
     /**
      * writes this mode's map to the xml file
      */
     public void writeMap() {
    	MapWriter writer = new MapWriter(myMap, myBackgroundPaths, mySoundPath);
     }
 
     /**
      * selects the next environment object in the list of environment objects (myEnviroObjects)
      */
     public void nextObject () {
     	removeObject(myCurrentSelection);
     	myEnviroIndex++;
     	if(myEnviroIndex == myEnviroObjects.size()) {
     		myEnviroIndex = 0;
     	}
     	myCurrentSelection = myEnviroObjects.get(myEnviroIndex);
     	addObject(myCurrentSelection);
     }
 
     /**
      * selects the previous environment object in the list of environment objects (myEnviroObjects)
      */
     public void prevObject () {
     	removeObject(myCurrentSelection);
     	myEnviroIndex--;
     	if(myEnviroIndex == -1) {
     		myEnviroIndex = (myEnviroObjects.size() - 1);
     	}
     	myCurrentSelection = myEnviroObjects.get(myEnviroIndex);
     	addObject(myCurrentSelection);
     }
     
     /**
      * selects the next player's information to be edited
      * (currently only used for starting locations)
      */
     public void nextPlayer() {
     	currentPlayer++;
     	if(currentPlayer==numPlayers) {
     		currentPlayer = 0;
     	}
     }
     
     /**
      * selects the previous player's information to be edited
      */
     public void prevPlayer() {
     	currentPlayer--;
     	if(currentPlayer<0) {
     		currentPlayer = numPlayers-1;
     	}
     }
     
     /**
      * sets the filepath of the background image. if filepath is null, does nothing.
      * @param filepath
      */
     public void setBackground(String filepath) {
     	if(filepath.equals(null)) {
     		return;
     	}
     	myBackgroundPaths.clear();
     	myBackgroundPaths.add(filepath);
     }
     
     /**
      * adds the filepath for an additional background image state to 
      * myBackgroundPaths. If the filepath is null, does nothing.
      * @param filepath
      */
     public void addBackground(String filepath) {
     	if(filepath.equals(null)) {
     		return;
     	}
     	myBackgroundPaths.add(filepath);
     }
     
     /**
      * sets the filepath of the sound file for music
      * @param filepath
      */
     public void setSound(String filepath) {
     	mySoundPath = filepath;
     }
 
     /**
      * gets this mapEditorMode's map
      * @return
      */
     public MapObject getMap () {
         return myMap;
     }
 }
