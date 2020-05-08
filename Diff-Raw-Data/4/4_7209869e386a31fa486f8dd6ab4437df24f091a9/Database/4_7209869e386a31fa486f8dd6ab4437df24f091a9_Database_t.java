 package com.edinarobotics.scouting.definitions.database;
 
 import java.util.Map;
 import java.util.Set;
 import com.edinarobotics.scouting.definitions.database.changes.Transaction;
 import com.edinarobotics.scouting.definitions.database.queries.Query;
 import com.edinarobotics.scouting.definitions.event.EventRegistrar;
 import com.edinarobotics.scouting.definitions.event.Future;
 import com.edinarobotics.scouting.definitions.event.Listener;
 
 /**
  * This class provides access to various
  * database implementations. It provides the necessary functions
  * to query and modify database values.
  * <br/>
  * Implementing a {@link Database} should be done carefully. All
  * {@link Database} implementations must behave in the same way and must
  * follow all method contracts.
  * <br/>
  * Database objects are expected to adhere to the
  * <a href=http://en.wikipedia.org/wiki/ACID>ACID</a> principles.
  * <h3>Database Metadata</h3>
  * To retrieve a list of all currently defined tables, query
  * the {@code @tables} metatable. Its results will have
  * an {@link com.edinarobotics.scouting.definitions.database.types.Integer Integer}
  * primary key value (the database table id number) and a 
  * {@link com.edinarobotics.scouting.definitions.database.types.Text Text}
  * table name in the {@code tid} and {@code tname} columns respectively.
  * <br/><br/>
  * To retrieve schema information for a table, query the {@code @schema}
  * metatable. The results will have an
  * {@link com.edinarobotics.scouting.definitions.database.types.Integer Integer}
  * column primary key id value, an
  * {@link com.edinarobotics.scouting.definitions.database.types.IntegerForeignKey IntegerForeignKey}
  * value pointing to the table id from the {@code @tables} metatable and
  * a {@link com.edinarobotics.scouting.definitions.database.types.Text Text}
  * value defining the type of the column.<br/>
  * Possible type values are:
  * <ul>
  * <li>{@code blob}</li>
  * <li>{@code boolean}</li>
  * <li>{@code integer}</li>
  * <li>{@code integer foreign key <table reference string>}</li>
  * <li>{@code integer primary key}</li>
  * <li>{@code real}</li>
  * <li>{@code text}</li>
  * </ul>
  * These values will be in the {@code cid}, {@code tid} and {@code ctype}
  * columns respectively. The results will include the schemas of the
  * {@code @tables} and {@code @schema} metatables.
  * The same information can also
  * be retrieved by sampling a {@link Row} from a table and consulting its
  * {@link Column} objects' types.
  */
 public interface Database extends EventRegistrar{
 	
 	/**
 	 * Launches a {@link Query} on this Database.
 	 * <br/>
 	 * Queries are executed in the order they are received by the database
 	 * (see the information on the ACID principles in {@link Database}).
 	 * The database access API is thread-safe and uses {@link Future} objects
 	 * to return the results of database operations. The {@link RowSet} values
 	 * can be retrieved by the Future object's {@link Future#get() get()} method.
 	 * However, this method will throw different exceptions depending on the
 	 * results of the database transaction.
 	 * @param query The Query object representing the desired parameters
 	 * for the {@link Row} objects to return.
 	 * @return A {@link Future} object containing the result of the query
 	 * (if the query succeeds).
 	 */
 	public Future<RowSet> executeQuery(Query query);
 	
 	/**
 	 * Executes a {@link Transaction} on this Database.
 	 * <br/>
 	 * Transactions are executed in the order they are received by the database
 	 * (see the information on the ACID principles in {@link Database}).
 	 * The {@link Future} object returned by this method will always return
 	 * {@code null} from its {@link Future#get() get()} method. However,
 	 * exceptions may still be thrown to indicate a failure in the execution the given
 	 * transaction. Use the {@link Future#get() Future.get()} method to join the
 	 * transaction after its completion or failure.
 	 * @param transaction The {@link Transaction} to be applied to the Database.
 	 * @return A {@link Future} object that can be used to inspect the results
 	 * of the transaction. The Future object always contains a {@code null}
 	 * result value.
 	 */
 	public Future<?> executeTransaction(Transaction transaction);
 	
 	/**
 	 * Returns the key-value storage {@link Map} with name {@code name} (case-sensitive).
 	 * If no key-value store with name {@code name} exists, one is created.<br/>
 	 * This method is designed to be used by plugins as configuration storage.
 	 * It is <em>not</em> to be used to store scouting data.<br/>
 	 * Note that this storage method is <em>not</em> thread-safe and provides
 	 * very few features.<br/>
 	 * All data entered into the returned Map is automatically stored by the Database
 	 * implementation.<br/>
 	 * Databases may discard empty key-value stores when they are closed but must create
 	 * any requested key-value storage maps.
 	 * @param name The name of the requested key-value store (case-sensitive).
 	 * @return A Map representing the key-value store named {@code name}.
 	 * @see #getKeyValueStoreNames()
 	 */
 	public Map<String, String> getKeyValueStore(String name);
 	
 	/**
 	 * Returns a Set containing the names of all currently defined key-value stores.
 	 * This list is not required to contain the names of empty key-value stores but must
 	 * contain all non-empty stores.
 	 * @return A Set containing the String names of all currently created key-value stores.
 	 * @see #getKeyValueStore(String)
 	 */
 	public Set<String> getKeyValueStoreNames();
 	
 	/**
	 * This method registers all {@link com.edinarobotics.scouting.definitions.event.EventListener EventListener}
 	 * methods in a {@link Listener} that receive events fired by this Database implementation.
 	 * These events are defined in the {@link com.edinarobotics.scouting.definitions.database.events events}
 	 * package.
 	 * These events typically relate to {@link #executeTransaction(Transaction)} objects and changes
 	 * to the contents of the Database.
 	 * <br/><br/>
 	 * <em><b>Caution:</b></em> do not register a single Listener with multiple Database implementations.
 	 * Multiple event listener registrations can lead to confusion and cause bugs.
	 * @param listener The Listener object to be registered for events.
 	 */
 	public void registerEvents(Listener listener);
 	
 	/**
 	 * Unregisters the given {@code listener} from all Database events.
 	 * If the given {@code listener} is not registered, no changes are made.
 	 * @param listener The Listener object to be unregistered from
 	 * Database events.
 	 */
 	public void unregisterListener(Listener listener);
 }
