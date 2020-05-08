 package uk.ac.cam.groupproj.racethewild;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 
 public class Engine {
 	
 	public final static String NODEPASS_MESSAGE = "uk.ac.cam.groupproj.racethewild.NODEPASS";
 	public final static String ANIMAL_NUMBER_MESSAGE = "uk.ac.cam.groupproj.racethewild.ANIMAL_NUMBER";
 
 	private PlayerStats stats;
 	private List<Node> nodes;
 	private Map<Integer, Animal> animalDictionary;
 	private Resources resources;
 	
 	private static Engine engine;
 	
 	/** Returns the instance of this singleton class. */
 	public static Engine get() { return engine; }
 
 	public PlayerStats getStats() { return stats; }
 	
 	public List<Node> getNodeList() {
 		return nodes;
 	}
 	
 
 	/*
 	 *   Get data from the satellite navigation
 	 */
 	public SatNavUpdate fetchSatNavData(Context context) {
 		SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.gps_main_file_key), Context.MODE_PRIVATE);
 		int movementPoints = sharedPref.getInt("movement_points",100);
 		int distance       = sharedPref.getInt("distance",0);
 		
 		return new SatNavUpdate(distance, movementPoints);
 	}
 	
 	/*
 	 *   Reset data from the satellite navigation
 	 *   Currently resets your movement points to 100, rather than 0
 	 *   to allow for testing of the other components without having to move around a lot
 	 */
 	public void resetSatNavData(Context context) {
 		SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.gps_main_file_key), Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = sharedPref.edit();
 		editor.putInt("movement_points",100);
 		editor.putInt("distance",0);
 		editor.commit();
 	}
 	
 	/** Returns a List of all Animals, sorted by ID.*/
 	public List<Animal> getAllAnimals() {
 		Collection<Animal> animalCollection = animalDictionary.values();
 		List<Animal> animals = new ArrayList<Animal>();
 		for(Animal animal : animalCollection) {
 			animals.add(animal);
 		}
 		Collections.sort(animals);
 		return animals;
 	}
 	
 	/** Returns the animal corresponding to the input ID */
 	public Animal getAnimal(int ID) {
 		return animalDictionary.get(ID);
 	}
 
 	/** Called at start-up to create the engine and initialise it. */
 	public static void initialise(Context c) { // Initial setup when app is started.
 		if (engine != null) return; // Ignore subsequent calls.
 		engine = new Engine(c);
 		// TODO: Start the sat-nav process, if not running.
 		// Load node data.
 		try {
 			engine.nodes = XmlParser.createNodes(engine.resources.getXml(R.xml.nodedata));
 		} catch(XmlReadException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Failed to load nodes, killing the app.");
 			System.exit(-1);
 		}
 		
 		// Load animal data.
 		try {
 			engine.animalDictionary = XmlParser.createDictionary(engine.resources.getXml(R.xml.animaldata));
 		} catch(XmlReadException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Failed to load animal dictionary, killing the app.");
 			System.exit(-2);
 		}
 		// Load the player's stats.
 		engine.stats = PlayerStats.load(c);
 		// Update animal colours based on loaded data.
 		for(Integer id : engine.stats.getGreyAnimals()) 
 			Engine.get().changeColour(id, Colour.Grey, c);
 		for(Integer id : engine.stats.getBlackAnimals()) 
 			Engine.get().changeColour(id, Colour.Black, c);
 	}
 	
 	/** Sets the colour of the animal with the given ID to the given colour. */
 	public void changeColour(int animalID, Colour colour, Context c) {
 		// Update the Animal object.
 		Animal animal = this.getAnimal(animalID);
 		animal.setColour(colour);
 		// Populate the animal in the world.
 		populateAnimal(animal);
 		if(colour == Colour.Grey) {
 			// Update in PlayerStats.
 			stats.addGreyAnimal(animalID);
 		}
 		else if(colour == Colour.Black) {
 			// Update in PlayerStats.
 			stats.addBlackAnimal(animalID);
 		}
 		// Save.
 		stats.save(c);
 	}
 	
 	/** Releases the given animal and resets distance and movement point accumulation. */
 	public void checkIn(Animal animal, Context c) {
 		if(animal != null) {
 			this.changeColour(animal.getID(), Colour.Grey, c);
 		}
 		// Update playerstats.
		stats.checkIn();
 		stats.save(c);
 	}
 	
 	/** Takes the name of a node, and returns the object associated with it. */
 	public Node lookupNode(String name) throws NodeNotFoundException {
 		for (Node node : nodes) {
 			if (node.hasName(name)) return node;
 		}
 		// Fail if the node isn't found.
 		throw new NodeNotFoundException();
 	}
 	
 	/** Adds an animal to its associated nodes. */
 	protected void populateAnimal(Animal animal) {
 		// Don't populate undiscovered animals.
 		if (animal.getColour() == Colour.White) return;
 		
 		Node[] animalNodes = animal.getNodes();
 		for (int i = 0; i < animalNodes.length; i++) {
 			if (!animalNodes[i].getAnimalList().contains(animal)) // Only populate once.
 				animalNodes[i].addAnimal(animal);
 		}
 	};
 	
 	/** Do not call this. Ever.
 	 *  Used for debugging to reset save data. */
 	public static void reset(Context c) {
 		engine.stats = new PlayerStats();
 		engine.stats.save(c);
 		engine = null;
 		Engine.initialise(c);
 	}
 
 	/** Create the engine at app start.
 	 *  Takes a Context to access the filesystem. */
 	private Engine(Context c) {
 		this.resources = c.getResources();
 	}
 	
 }
