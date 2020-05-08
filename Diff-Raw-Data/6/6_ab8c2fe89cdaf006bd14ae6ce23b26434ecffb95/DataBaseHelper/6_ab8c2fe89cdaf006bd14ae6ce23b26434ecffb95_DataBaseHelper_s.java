 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser GeneralGeneral Public License as published by
  * the Free Software Foundation, either version 3 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See tGNU Lesser GeneralLesser General Public License for more details.
  *
  * You should have received GNU Lesser Generalhe GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.ybo.database;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import fr.ybo.database.modele.Base;
 
 /**
  * Class use to access to the database.
  * 
  * @author ybonnel
  * 
  */
 public abstract class DataBaseHelper extends SQLiteOpenHelper {
 
 	/**
 	 * Object define the database.
 	 */
 	private final Base base;
 
 	/**
 	 * Use to know if a transaction is open.
 	 */
 	private boolean transactionOpen;
 
 	/**
 	 * Constructeur.
 	 * 
 	 * @param context
 	 *            context android.
 	 * @param classes
 	 *            entities.
 	 * @param dataBaseName
 	 *            name of the database (ex : database.db).
 	 * @param dataBaseVersion
 	 *            current version of the database.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public DataBaseHelper(Context context, List<Class<?>> classes, String dataBaseName, int dataBaseVersion)
 			throws DataBaseException {
 		super(context, dataBaseName, null, dataBaseVersion);
 		base = new Base(classes);
 	}
 
 	/**
 	 * begin a transaction.
 	 */
 	public void beginTransaction() {
 		endTransaction();
 		getWritableDatabase().beginTransaction();
 		transactionOpen = true;
 	}
 
 	/**
 	 * Delete an entity.
 	 * 
 	 * @param <Entity>
 	 *            entity.
 	 * @param entity
 	 *            the entity to delete.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public <Entity> void delete(Entity entity) throws DataBaseException {
 		base.delete(getWritableDatabase(), entity);
 	}
 
 	/**
 	 * Delete all entities of a table.
 	 * 
 	 * @param <Entity>
 	 *            entity.
 	 * @param clazz
 	 *            the class of entities to delete.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public <Entity> void deleteAll(Class<Entity> clazz) throws DataBaseException {
 		base.deleteAll(getWritableDatabase(), clazz);
 	}
 
 	/**
 	 * End a transaction.
 	 */
 	public void endTransaction() {
 		if (transactionOpen) {
			getWritableDatabase().setTransactionSuccessful();
			getWritableDatabase().endTransaction();
 		}
 		transactionOpen = false;
 	}
 
 	/**
 	 * Execute a select.
 	 * 
 	 * @param query
 	 *            the query to exectute.
 	 * @param selectionArgs
 	 *            the args of the select.
 	 * @return the cursor.
 	 */
 	public Cursor executeSelectQuery(String query, List<String> selectionArgs) {
 		return getReadableDatabase().rawQuery(query,
 				selectionArgs == null ? null : selectionArgs.toArray(new String[selectionArgs.size()]));
 	}
 
 	/**
 	 * Get the database modele.
 	 * 
 	 * @return the database modele.
 	 */
 	public Base getBase() {
 		return base;
 	}
 
 	/**
 	 * Insert an entity.
 	 * 
 	 * @param <Entity>
 	 *            entity.
 	 * @param entity
 	 *            the entity to insert.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public <Entity> Entity insert(Entity entity) throws DataBaseException {
 		return base.insert(getWritableDatabase(), entity);
 	}
 
 	/**
 	 * Create the database.
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		base.createDataBase(db);
 	}
 
 	/**
 	 * Interface use tu upgrade the database to a version.
 	 * 
 	 * @author ybonnel
 	 * 
 	 */
 	static protected interface UpgradeDatabase {
 		void upgrade(SQLiteDatabase db);
 	}
 
 	/**
 	 * 
 	 * @return return a map of {@link UpgradeDatabase} for each version.
 	 */
 	protected abstract Map<Integer, UpgradeDatabase> getUpgrades();
 
 	/**
 	 * Upgrade the database. Execute the
 	 * {@link UpgradeDatabase#upgrade(SQLiteDatabase)} for each version from
 	 * oldVersion+1 to newVersion. If there is an exception during the upgrade,
 	 * the entire database is drop and re-create.
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		try {
 			for (int version = oldVersion + 1; version <= newVersion; version++) {
 				if (getUpgrades().containsKey(version)) {
 					getUpgrades().get(version).upgrade(db);
 				}
 			}
 		} catch (Exception exception) {
 			Cursor cursor = db.query("sqlite_master", Collections.singleton("name").toArray(new String[1]),
 					" type = 'table'", null, null, null, null);
 			while (cursor.moveToNext()) {
 				String tableName = cursor.getString(0);
 				if (!"android_metadata".equals(tableName)) {
 					db.execSQL("DROP TABLE " + tableName);
 				}
 			}
 			cursor.close();
 			base.createDataBase(db);
 		}
 	}
 
 	/**
 	 * Select all entities of a table represent by the clazz.
 	 * 
 	 * @param <Entity>
 	 *            entity.
 	 * @param clazz
 	 *            the clazz.
 	 * @return all entities found.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public <Entity> List<Entity> selectAll(Class<Entity> clazz) throws DataBaseException {
 		return base.selectAll(getReadableDatabase(), clazz);
 	}
 
 	/**
 	 * Select from an exemple entity.
 	 * 
 	 * @param <Entity>
 	 *            entity
 	 * @param entity
 	 *            the exemple entity to use for the select.
 	 * @return the entities correspond to the exemple.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public <Entity> List<Entity> select(Entity entity) throws DataBaseException {
 		return base.select(getReadableDatabase(), entity, null, null, null);
 	}
 
 	/**
 	 * Select from an exemple entity.
 	 * 
 	 * @param <Entity>
 	 *            entity
 	 * @param entity
 	 *            the exemple entity to use for the select.
 	 * @param orderBy
 	 *            the order by clause.
 	 * @return the entities correspond to the exemple.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem).
 	 */
 	public <Entity> List<Entity> select(Entity entity, String orderBy) throws DataBaseException {
 		return base.select(getReadableDatabase(), entity, null, null, orderBy);
 	}
 
 	/**
 	 * Select from an exemple entity.
 	 * 
 	 * @param <Entity>
 	 *            entity
 	 * @param entity
 	 *            the exemple entity to use for the select.
 	 * @return the entitiy correspond to the exemple.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem). of
 	 *             multiple results.
 	 */
 	public <Entity> Entity selectSingle(Entity entity) throws DataBaseException {
 		List<Entity> entities = select(entity);
 		if (entities.size() > 1) {
 			throw new DataBaseException("Multiple results found for an selectSingle.");
 		}
 		if (entities.isEmpty()) {
 			return null;
 		}
 		return entities.get(0);
 	}
 
 	/**
 	 * Update an entity.
 	 * 
 	 * @param <Entity>
 	 *            entity
 	 * @param entity
 	 *            the entity to update.
 	 * @throws DataBaseException
 	 *             if there is a problem (may be a development problem). of
 	 *             multiple results.
 	 */
 	public <Entity> void update(Entity entity) throws DataBaseException {
 		base.update(getWritableDatabase(), entity);
 	}
 
 }
