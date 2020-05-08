 package se.chalmers.dat255.sleepfighter.persist;
 
 import java.sql.SQLException;
 
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.AlarmsManager;
 import android.content.Context;
 
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.field.DataPersisterManager;
 
 /**
  * Handles all reads and writes to persistence.<br/>
  * There should be no reason to keep more than 1 instance of this object.
  * 
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 21, 2013
  */
 public class PersistenceManager {
 	/**
 	 * Constructs the PersistenceManager.
 	 */
 	public PersistenceManager() {
 	}
 
 	private volatile OrmHelper ormHelper = null;
 
 	private static boolean init = false;
 
 	/**
 	 * Rebuilds all data-structures. Any data is lost.
 	 *
 	 * @param context android context.
 	 */
 	public void cleanStart( Context context ) {
 		this.getHelper( context ).rebuild();
 	}
 
 	/**
 	 * Fetches an AlarmsManager from database, it is sorted on ID.
 	 *
 	 * @param context android context.
 	 * @return the fetched AlarmsManager.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public AlarmsManager fetchAlarms( Context context ) throws PersistenceException {
 		OrmHelper helper = this.getHelper( context );
 		PersistenceExceptionDao<Alarm, Integer> dao = helper.getAlarmDao();
 		return new AlarmsManager( dao.queryForAll() );
 	}
 
 	/**
 	 * Fetches an AlarmsManager from database, it is sorted on names.
 	 *
 	 * @param context android context.
 	 * @return the fetched AlarmsManager.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public AlarmsManager fetchAlarmsSortedNames( Context context ) throws PersistenceException {
 		OrmHelper helper = this.getHelper( context );
 		PersistenceExceptionDao<Alarm, Integer> dao = helper.getAlarmDao();
 
 		try {
 			return new AlarmsManager( dao.queryBuilder().orderBy( "name", true ).query() );
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Fetches a single alarm from database by its id.
 	 *
 	 * @param context android context.
 	 * @param id the ID of the alarm to fetch.
 	 * @return the fetched Alarm.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public Alarm fetchAlarmById( Context context, int id ) throws PersistenceException {
 		OrmHelper helper = this.getHelper( context );
 		PersistenceExceptionDao<Alarm, Integer> dao = helper.getAlarmDao();
 		return dao.queryForId( id );
 	}
 
 	/**
 	 * Updates an alarm to database.
 	 *
 	 * @param context android context
 	 * @param alarm the alarm to update.
 	 */
 	public void updateAlarm( Context context, Alarm alarm ) {
 		OrmHelper helper = this.getHelper( context );
 		PersistenceExceptionDao<Alarm, Integer> dao = helper.getAlarmDao();
 		dao.update( alarm );
 	}
 
 	/**
 	 * Stores/adds an alarm to database.
 	 *
 	 * @param context android context.
 	 * @param alarm the alarm to store.
 	 */
 	public void addAlarm( Context context, Alarm alarm ) {
 		OrmHelper helper = this.getHelper( context );
 		PersistenceExceptionDao<Alarm, Integer> dao = helper.getAlarmDao();
 		dao.create( alarm );
 	}
 
 	/**
 	 * Removes an alarm from database.
 	 *
 	 * @param context android context.
 	 * @param alarm the alarm to remove.
 	 */
 	public void removeAlarm( Context context, Alarm alarm ) {
 		OrmHelper helper = this.getHelper( context );
 		PersistenceExceptionDao<Alarm, Integer> dao = helper.getAlarmDao();
 		dao.delete( alarm );
 	}
 
 	/**
 	 * Releases any resources held such as the OrmHelper.
 	 */
 	public void release() {
 		if ( this.ormHelper != null ) {
 			OpenHelperManager.releaseHelper();
 			this.ormHelper = null;
 		}
 	}
 
 	/**
 	 * Returns the OrmHelper.
 	 *
 	 * @param context the context to use to get helper.
 	 * @return the helper.
 	 */
 	public OrmHelper getHelper( Context context ) {
 		if ( this.ormHelper == null ) {
 			if ( context == null ) {
 				throw new PersistenceException( "There is no helper set and context == null" );
 			}
 
 			this.loadHelper( context );
 		}
 
 		return this.ormHelper;
 	}
 
 	/**
 	 * Loads the OrmHelper.
 	 */
 	private void loadHelper( Context context ) {
 		this.ormHelper = OpenHelperManager.getHelper( context, OrmHelper.class );
 
 		this.init();
 	}
 
 	/**
 	 * Initialization code goes here.
 	 */
 	private void init() {
 		// Run Once guard.
 		if ( init ) {
 			return;
 		}
 		init = true;
 
 		// Initialization code goes here.
 		DataPersisterManager.registerDataPersisters( BooleanArrayType.getSingleton() );
 	}
 }
