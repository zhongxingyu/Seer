 package edu.wheaton.simulator.statistics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import com.google.common.collect.ImmutableTable;
 import com.google.common.collect.TreeBasedTable;
 
 import edu.wheaton.simulator.datastructures.Field;
 import edu.wheaton.simulator.gridentities.Agent;
 import edu.wheaton.simulator.gridentities.GridEntity;
 
 /**
  * This class is an abstraction of a database which is represented by a Table,
  * provided by the Guava libraries
  * 
  * @author akonwi
  */
 
 public class Database {
 
 	/**
 	 * This table will be used to put the snapshots into
 	 */
 	private TreeBasedTable<EntityID, Integer, EntitySnapshot> treeTable;
 
 	/**
 	 * This table will be an immutable copy of treeTable in order to ensure
 	 * that nothing gets removed from the table. All get() requests will be
 	 * responded to by this table
 	 */
 	private ImmutableTable<EntityID, Integer, EntitySnapshot> database;
 
 	/**
 	 * CONSTRUCTOR An editable database will be created and the immutable copy
 	 * will be set to null and all get() requests will fail until the client
 	 * calls the finalize() method.
 	 */
 	public Database() {
 		treeTable = TreeBasedTable.create();
 		database = null;
 	}
 
 	/**
 	 * This method will put the given entity into an editable database
 	 * 
 	 * @param entity
 	 *            the entity to create snapshot from
 	 * @param step
 	 *            the int representing this turn and will be used as a column
 	 *            index in the database(table)
 	 */
 	public void putEntity(GridEntity entity, int step) {
 		EntityID id = entity.getID();
 
 		/**
 		 * all the fields of the agent
 		 */
 		ArrayList<Field> entityFields = entity.getFields();
 
 		/*
 		 * map of the fields to save in entitySnapShot
 		 */
 		HashMap<String, FieldSnapshot> fieldsForSnap = new HashMap<String, FieldSnapshot>();
 
 		/*
 		 * fill the fieldsForSnap
 		 */
 		Iterator<Field> it = entityFields.iterator();
 		while (it.hasNext()) {
 			Field current = it.next();
 			FieldSnapshot snap = new FieldSnapshot(current.getName(),
 					current.getType(), current.getValue());
 			fieldsForSnap.put(current.getName(), snap);
 		}
 
 		/*
 		 * Create protoype Because this requires me having the initial set of
 		 * fields, I think the Observer class should record those on when the
 		 * user defines them. Then have a getter method that returns the
 		 * 'default fields'
 		 * 
 		 * Observer class could also possibly create a prototype because it
 		 * will have first access to the 'categoryName' and with the Fields, it
 		 * can make a map<String, FieldSnapshot> getPrototype(String name)
 		 * could be a class method that returns a prototype from a Map<String,
 		 * EntityPrototype>
 		 */
 		EntityPrototype prototype = Observer.getPrototype(entity.getName());
 
 		EntitySnapshot snap;
 		// determine class of Entity and create snapshot accordingly
 		if (entity.getClass() == Agent.class)
 			snap = new AgentSnapshot(id, fieldsForSnap, step, prototype, null);
 		else
 			snap = new SlotSnapshot(id, fieldsForSnap, step, prototype, null);
 
 		// put snapshot in treeTable database
 		treeTable.put(id, step, snap);
 	}
 
 	/**
 	 * Once the caller is finished adding Snapshots to the database, they are
 	 * responsible for calling this method to initialize the ImmutableTable
 	 * which will be publicly available via get methods
 	 */
 	public void finalize() {
 		database = new ImmutableTable.Builder<EntityID, Integer, EntitySnapshot>()
 				.putAll(treeTable).build();
 	}
 
 	/**
 	 * Get the EntitySnapshot with given id at given step
 	 * 
 	 * @param id
 	 *            EntityID of gridEntity or row to query
 	 * @param step
 	 *            the turn in the game or column to query
 	 * @return EntitySnapshot at given row(id) and column(step)
 	 */
	public EntitySnapshot getSnapshot(EntityID id, int step)
			throws Exception {
 		try {
 			return database.get(id, step);
 		} catch (Exception e) {
 			System.out.println("The database has not been finalized");
 			return null;
 		}
 	}
	
 	/**
 	 * Get the size of the database
 	 * @return size of database
 	 */
 	public int getSize() {
 		return database.size();
 	}
 }
 
 /**
  * the following is a foobar EntitySnapshot and details
  */
 /*
  * EntityID id = new EntityID("akon", 1); HashMap<String, FieldSnapshot>
  * defaults = new HashMap<String, FieldSnapshot>(); HashMap<String,
  * FieldSnapshot> fields = new HashMap<String, FieldSnapshot>();
  * EntityPrototype prototype = new EntityPrototype("agent", defaults);
  * InteractionDescription interaction = new InteractionDescription();
  * EntitySnapshot snap = new AgentSnapshot(id, fields, 1, prototype,
  * interaction);
  * 
  * treeTable.put(id, 1, snap); // put snapshot into table
  *//**
  * once finished adding all snapshots, build Immutable table
  */
 /*
  * database = new ImmutableTable.Builder<EntityID, Integer, EntitySnapshot>()
  * .putAll(treeTable).build();
  * 
  * database.get(id, 1).id(); // Test database
  */
