 package ws.wiklund.guides.db;
 
 import java.io.Serializable;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import ws.wiklund.guides.model.BaseModel;
 import ws.wiklund.guides.model.Beverage;
 import ws.wiklund.guides.model.BeverageType;
 import ws.wiklund.guides.model.Category;
 import ws.wiklund.guides.model.Cellar;
 import ws.wiklund.guides.model.Column;
 import ws.wiklund.guides.model.Country;
 import ws.wiklund.guides.model.Producer;
 import ws.wiklund.guides.model.Provider;
 import ws.wiklund.guides.model.TableName;
 import ws.wiklund.guides.util.ViewHelper;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public abstract class BeverageDatabaseHelper extends SQLiteOpenHelper implements Serializable {
 	private static final long serialVersionUID = 6561035045955612957L;
 	// Database tables
 	private static final String COUNTRY_TABLE = "country";
 	private static final String PRODUCER_TABLE = "producer"; 
 	private static final String PROVIDER_TABLE = "provider";
 	private static final String CATEGORY_TABLE = "category";
 
 	public static final String BEVERAGE_TABLE = "beverage";
 	public static final String BEVERAGE_TYPE_TABLE = "beverage_type";
 	public static final String CELLAR_TABLE = "cellar";	
 
 	// Database creation sql statements
 	public static final String DB_CREATE_BEVERAGE = "create table " + BEVERAGE_TABLE + " (_id integer primary key autoincrement, "
 			+ "name text not null, "
 			+ "no integer, "
 			+ "thumb text, "
 			+ "country_id integer, "
 			+ "year integer, "
 			+ "beverage_type_id integer, "
 			+ "producer_id integer, "
 			+ "strength float, "
 			+ "price float, "
 			+ "usage text, "
 			+ "taste text, "
 			+ "provider_id integer, "
 			+ "rating float, "
 			+ "comment text, "
 			+ "category_id integer, "
 			+ "added timestamp not null default current_timestamp, "
 			+ "foreign key (country_id) references country (_id), "
 			+ "foreign key (beverage_type_id) references beverage_type (_id), "
 			+ "foreign key (producer_id) references producer (_id), "
 			+ "foreign key (producer_id) references producer (_id), "
 			+ "foreign key (category_id) references category (_id));"; 
 
 	public static final String DB_CREATE_BEVERAGE_TYPE = "create table " + BEVERAGE_TYPE_TABLE + " (_id integer primary key, "
 			+ "name text not null);";
 
 	private static final String DB_CREATE_COUNTRY = "create table " + COUNTRY_TABLE + " (_id integer primary key autoincrement, "
 			+ "name text not null, " 
 			+ "thumb_url text);";
 
 	private static final String DB_CREATE_PRODUCER = "create table " + PRODUCER_TABLE + " (_id integer primary key autoincrement, "
 			+ "name text not null);";
 
 	private static final String DB_CREATE_PROVIDER = "create table " + PROVIDER_TABLE + " (_id integer primary key autoincrement, "
 			+ "name text not null);";	
 
 	public static final String DB_CREATE_CATEGORY = "create table " + CATEGORY_TABLE + " (_id integer primary key autoincrement, "
 			+ "name text not null);";
 
 	public static final String DB_CREATE_CELLAR = "create table " + CELLAR_TABLE + " (_id integer primary key autoincrement, "
 			+ "beverage_id integer, "
 			+ "no_bottles integer, "
 			+ "storage_location text, "
 			+ "comment text, "
 			+ "added_to_cellar integer, "
 			+ "consumption_date integer, "
 			+ "notification_id integer, "
 			+ "foreign key (beverage_id) references beverage (_id));";
 
 
 	private static final String CELLAR_COLUMNS =
 			"cellar._id, "
 			+ "cellar.beverage_id, "
 			+ "cellar.no_bottles, "
 			+ "cellar.storage_location, "
 			+ "cellar.comment, "
 			+ "cellar.added_to_cellar, "
 			+ "cellar.consumption_date, "
 			+ "cellar.notification_id ";
 	
 	private static final String BEVERAGE_COLUMNS = 
 			"beverage._id, "
 			+ "beverage.name, "
 			+ "beverage.no, "
 			+ "beverage.beverage_type_id, "
 			+ "beverage_type.name, "
 			+ "beverage.thumb, "
 			+ "beverage.country_id, "
 			+ "country.name, "
 			+ "country.thumb_url, "
 			+ "beverage.year, "
 			+ "beverage.producer_id, "
 			+ "producer.name, "
 			+ "beverage.strength, "
 			+ "beverage.price, "
 			+ "beverage.usage, "
 			+ "beverage.taste, "
 			+ "beverage.provider_id, "
 			+ "provider.name, "
 			+ "beverage.rating, "
 			+ "beverage.comment, "			
 			+ "beverage.category_id, "
 			+ "category.name, "
 			+ "(strftime('%s', added) * 1000) AS added ";
 
 	private static final String BEVERAGE_JOIN_COLUMNS = "LEFT JOIN beverage_type ON "
 			+ "beverage.beverage_type_id = beverage_type._id "
 			+ "LEFT JOIN country ON "
 			+ "beverage.country_id = country._id "
 			+ "LEFT JOIN producer ON "
 			+ "beverage.producer_id = producer._id "
 			+ "LEFT JOIN category ON "
 			+ "beverage.category_id = category._id "
 			+ "LEFT JOIN provider ON "
 			+ "beverage.provider_id = provider._id ";
 
 	public static final String SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR = "SELECT DISTINCT "
 			+ BEVERAGE_COLUMNS + ", "
 			+ "ifnull(("
 				+ "SELECT "
 					+ "sum(no_bottles) AS tot "
 				+ "FROM "
 					+ CELLAR_TABLE + " "
 				+ "WHERE "
 					+ "beverage._id = cellar.beverage_id"
 				+ "), 0) AS total " 
 			+ "FROM "
 			+ BEVERAGE_TABLE + " "
 			+ BEVERAGE_JOIN_COLUMNS;
 
 	public static final String SQL_SELECT_ALL_BEVERAGES_IN_CELLAR = SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR
 			+ "LEFT JOIN cellar ON "
 			+ "beverage._id = cellar.beverage_id " 
 			+ "WHERE cellar.no_bottles > 0";
 	
 	public static final String SQL_SELECT_BEVERAGES_IN_CELLAR = "SELECT "
 					+ BEVERAGE_COLUMNS 
 					+ ", "
 					+ CELLAR_COLUMNS
 					+ "FROM "
 					+ BEVERAGE_TABLE + " "
 					+ BEVERAGE_JOIN_COLUMNS
 					+ "LEFT JOIN cellar ON "
 					+ "beverage._id = cellar.beverage_id " 
 					+ "WHERE "
 					+ "beverage._id = ?";
 	
 	private static final String SQL_SELECT_BEVERAGE = 
 			SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR
 			+ "WHERE "
 			+ "beverage._id = ?";
 		
 	
 	//Used for testing so that db can be created and dropped with out destroying dev data
 	public BeverageDatabaseHelper(Context context, String dbName, int dbVersion) {
 		super(context, dbName, null, dbVersion);
 		
 		/*if((context.getApplicationInfo().flags & android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
 			debugSetDBVersion(1);
 		}*/
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(DB_CREATE_PRODUCER);
 		db.execSQL(DB_CREATE_PROVIDER);
 		db.execSQL(DB_CREATE_COUNTRY);
 		db.execSQL(DB_CREATE_CATEGORY);
 		db.execSQL(DB_CREATE_BEVERAGE);
 		db.execSQL(DB_CREATE_CELLAR);
 		
 		getDatabaseUpgrader(db).createAndPopulateBeverageTypeTable(db);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		if (oldVersion < newVersion) {
 			Log.d(BeverageDatabaseHelper.class.getName(),
 					"Upgrading database from version " + oldVersion + " to "
 							+ newVersion);
 			getDatabaseUpgrader(db).doUpdate(oldVersion, newVersion);
 		}
 	}
 
 	public abstract DatabaseUpgrader getDatabaseUpgrader(SQLiteDatabase db);
 
 	public Beverage getBeverage(int id) {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.rawQuery(SQL_SELECT_BEVERAGE, new String[] {String.valueOf(id)});
 
 			if (c.moveToFirst()) {
 				return ViewHelper.getBeverageFromCursor(c);
 			}
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 		
 		return null;
 	}
 	
 	public Country getCountry(int id) {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.query("country", null, "_id=", new String[]{String.valueOf(id)}, null, null, null);
 
 			if (c.moveToFirst()) {
 				return getCountryFromCursor(c);
 			}
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 		
 		return null;
 	}
 
 	public List<Beverage> getAllBeverages() {
 		List<Beverage> l = new ArrayList<Beverage>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			Cursor c = db.rawQuery(SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR, null);
 			
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				l.add(ViewHelper.getBeverageFromCursor(c));
 			}
 
 			return l;
 		} finally {
 			db.close();
 		}
 	}
 	
 	public List<Cellar> getAllActiveAlarms() {
 		List<Cellar> l = new ArrayList<Cellar>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			String sql = "select " + CELLAR_COLUMNS + "from " + CELLAR_TABLE +" where consumption_date > 0";
 			
 			Log.d(BeverageDatabaseHelper.class.getName(), "getAllActiveAlarms() SQL: " + sql);
 			
 			Cursor c = db.rawQuery(sql, null);
 			
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				l.add(ViewHelper.getCellarFromCursor(c));
 			}
 
 			return l;
 		} finally {
 			db.close();
 		}
 	}
 	
 
 	public List<Category> getCategories() {
 		List<Category> l = new ArrayList<Category>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			Cursor c = db.rawQuery("select _id, name from " + CATEGORY_TABLE, null);
 			
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				l.add(getCategoryFromCursor(c));
 			}
 
 			return l;
 		} finally {
 			db.close();
 		}
 	}
 	
 	public BeverageType getBeverageTypeFromName(String name) {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.rawQuery("select _id, name from " + BEVERAGE_TYPE_TABLE + " where name=?", new String[] {String.valueOf(name)});
 
 			if (c.moveToFirst()) {
 				return getBeverageTypeFromCursor(c);
 			}
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 		
 		return BeverageType.OTHER;
 	}
 
 	public List<BeverageType> getAllBeverageTypes() {
 		List<BeverageType> l = new ArrayList<BeverageType>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			Cursor c = db.rawQuery("select _id, name from " + BEVERAGE_TYPE_TABLE, null);
 			
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				l.add(getBeverageTypeFromCursor(c));
 			}
 
 			return l;
 		} finally {
 			db.close();
 		}
 	}
 	
 	public Cellar getOldestCellarItemForBeverage(int beverageId) {
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			String sql = "select " + CELLAR_COLUMNS + "from " + CELLAR_TABLE +" where beverage_id=? ORDER BY added_to_cellar";
 			
 			Log.d(BeverageDatabaseHelper.class.getName(), "getAllActiveAlarms() SQL: " + sql);
 			
 			Cursor c = db.rawQuery(sql, new String[]{String.valueOf(beverageId)});
 			
 			DatabaseUtils.dumpCursor(c);
 			
 			if(c.moveToFirst()) {
 				return ViewHelper.getCellarFromCursor(c);
 			}
 			
 			return null;
 		} finally {
 			db.close();
 		}
 		
 	}
 
 	public List<Cellar> getAllCellarsForBeverage(int beverageId) {
 		List<Cellar> l = new ArrayList<Cellar>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			String sql = "select " + CELLAR_COLUMNS + "from " + CELLAR_TABLE +" where beverage_id=?";
 			
 			Log.d(BeverageDatabaseHelper.class.getName(), "getAllActiveAlarms() SQL: " + sql);
 			
 			Cursor c = db.rawQuery(sql, new String[]{String.valueOf(beverageId)});
 			
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				l.add(ViewHelper.getCellarFromCursor(c));
 			}
 
 			return l;
 		} finally {
 			db.close();
 		}
 	}
 	
 	public boolean deleteCellar(int id) {
 		SQLiteDatabase db = getWritableDatabase();
 
 		try {
 			db.beginTransaction();
 			
 			boolean b = true;
 			b = db.delete(CELLAR_TABLE, "_id=?", new String[] { String.valueOf(id) }) == 1;
 			if (b) {
 				db.setTransactionSuccessful();
 			}
 			
 			return b; 
 		} finally {
 			db.endTransaction();
 			db.close();
 			close();
 		}
 	}
 
 	public boolean deleteBeverage(int id) {
 		SQLiteDatabase db = getWritableDatabase();
 
 		try {
 			db.beginTransaction();
 			
 			boolean b = true;
 			if(inCellar(id)) {
 				b = db.delete(CELLAR_TABLE, "beverage_id=?", new String[]{String.valueOf(id)}) > 0;
 			}		
 			
 			if (b) {
 				b = db.delete(BEVERAGE_TABLE, "_id=?", new String[] { String.valueOf(id) }) == 1;
 				if (b) {
 					db.setTransactionSuccessful();
 				}
 			}
 			
 			return b; 
 		} finally {
 			db.endTransaction();
 			db.close();
 			close();
 		}
 	}
 
 	public boolean updateBeverage(Beverage beverage) {
 		if (exists(beverage)) {			
 			return update(beverage);
 		}
 		
 		Log.d(BeverageDatabaseHelper.class.getName(),"Trying to update beverage that doesn't exist in db" + (beverage != null ? beverage.toString() : null));
 		return false;
 	}
 
 	public Beverage addBeverage(Beverage beverage) {
 		if (!exists(beverage)) {
 			SQLiteDatabase db = getWritableDatabase();
 			db.beginTransaction();
 
 			try {
 				ContentValues values = beverage.getAsContentValues();
 
 				//Add country if country doesn't exists in DB
 				Integer countryId = values.getAsInteger("country_id");
 				if (countryId == null) {
 					values.put("country_id", addCountry(db, beverage.getCountry()));
 				}
 				
 				//Add producer if producer doesn't exists in DB
 				Integer producerId = values.getAsInteger("producer_id");
 				if (producerId == null) {
 					values.put("producer_id", addProducer(db, beverage.getProducer()));
 				}
 				
 				//Add provider if provider doesn't exists in DB
 				Integer providerId = values.getAsInteger("provider_id");
 				if (providerId == null) {
 					values.put("provider_id", addProvider(db, beverage.getProvider()));
 				}
 				
 				//Add category if category doesn't exists in DB
 				Integer categoryId = values.getAsInteger("category_id");
 				if (categoryId == null) {
 					values.put("category_id", addCategory(db, beverage.getCategory()));
 				}
 
 				long id = db.insert(BEVERAGE_TABLE, null, values);
 				
 				//Update beverage with the newly created id
 				beverage.setId((int) id);
 				
 				db.setTransactionSuccessful();
 			} finally {
 				db.endTransaction();
 				db.close();
 				close();
 			}
 		} else {
 			update(beverage);
 		}
 
 		return beverage;
 	}
 	
 	public int getBeverageIdFromNo(int no) {
 		SQLiteDatabase db = getReadableDatabase();			
 		Cursor c = db.query(BEVERAGE_TABLE, new String[]{"_id"}, "no = ?", new String[] {String.valueOf(no)}, null, null, null);
 
 		try {					
 			if (c.moveToFirst()) {
 				return c.getInt(0);
 			}
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 		
 		return -1;
 	}
 
 	public int getVersion() {
 		SQLiteDatabase db = getReadableDatabase();
 		try {
 			return db.getVersion();
 		} finally {
 			db.close();
 			close();
 		}
 	}
 
 	public int getNoBottlesInCellar() {
 		return getNoBottlesInCellarForWine(-1);
 	}
 	
 	public int getNoBottlesInCellarForWine(int id) {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			String sql = "SELECT sum(no_bottles) as sum FROM cellar";
 			if(id != -1) {
 				sql += " WHERE beverage_id = ?";
 			}
 			
 			c = db.rawQuery(sql, id != -1 ? new String[]{String.valueOf(id)} : null);
 
 			if (c.moveToFirst()) {
 				return c.getInt(0);
 			}
 
 			return -1;
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 	}
 	
 	public int getNoBottlesForBeverage(int id) {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.rawQuery("SELECT sum(no_bottles) as sum FROM cellar WHERE beverage_id = ?", new String[]{String.valueOf(id)});
 
 			if (c.moveToFirst()) {
 				return c.getInt(0);
 			}
 
 			return -1;
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 	}
 	
 	public Cellar addToOrUpdateCellar(Cellar cellar) {
 		SQLiteDatabase db = getWritableDatabase();
 		db.beginTransaction();
 
 		try {
 			if (cellar.isNew()) {
 				//TODO change to default time stamp in DB table in coming versions
 				cellar.setAddedToCellar(new Date());
 				ContentValues values = cellar.getAsContentValues();
 	
 				long id = db.insert(CELLAR_TABLE, null, values);
 				
 				//Update cellar with the newly created id
 				cellar.setId((int) id);
 				
 				db.setTransactionSuccessful();
 			} else {
 				ContentValues values = cellar.getAsContentValues();						
 				db.update(CELLAR_TABLE, values, "_id=?", new String[]{String.valueOf(cellar.getId())});
 
 				db.setTransactionSuccessful();
 			}
 		} finally {
 			db.endTransaction();
 			db.close();
 			close();
 		}
 		
 		return cellar;
 	}
 	
 	public boolean removeSchedule(Cellar cellar) {
 		SQLiteDatabase db = getWritableDatabase();
 		db.beginTransaction();
 
 		try {
 			cellar.setConsumptionDate(null);
 			ContentValues values = cellar.getAsContentValues();
 						
 			db.update(CELLAR_TABLE, values, "_id=?", new String[]{String.valueOf(cellar.getId())});
 
 			db.setTransactionSuccessful();
 			return true;		
 		} finally {
 			db.endTransaction();
 			db.close();
 			close();
 		}
 	}
 	
 	// Stats
 	public double getCellarValue() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.rawQuery(BeverageDatabaseHelper.SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR, null);
 
 			double value = 0;
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				value += c.getDouble(13) * c.getInt(23);
 			}
 
 			return value;
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 	}
 
 	public double getAverageRating() {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.rawQuery("select avg(rating) from beverage where rating != -1", null);
 
 			if (c.moveToFirst()) {
 				return c.getDouble(0);
 			}
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 		
 		return 0;
 	}
 
 	public int getAllBeveragesForType(BeverageType type) {
 		SQLiteDatabase db = getReadableDatabase();
 		Cursor c = null;
 		
 		try {
 			c = db.rawQuery("select count(_id) from beverage where beverage_type_id = ?", new String[]{String.valueOf(type.getId())});
 
 			if (c.moveToFirst()) {
 				return c.getInt(0);
 			}
 		} finally {
 			c.close();
 			db.close();
 			close();
 		}
 		
 		return 0;
 	}
 
 	public List<Country> getCountries() {
 		List<Country> l = new ArrayList<Country>();
 		SQLiteDatabase db = getReadableDatabase();
 		
 		try {
 			Cursor c = db.rawQuery("select * from " + COUNTRY_TABLE, null);
 			
 			for (boolean b = c.moveToFirst(); b; b = c.moveToNext()) {
 				l.add(getCountryFromCursor(c));
 			}
 
 			return l;
 		} finally {
 			db.close();
 		}
 	}
 
 	// End Stats
 	
 	/**
 	 * Internal method with out check for duplicates
 	 * @param beverage
 	 * @return
 	 */
 	private boolean update(Beverage beverage) {
 		SQLiteDatabase db = getWritableDatabase();
 		db.beginTransaction();
 
 		try {
 			Country country = beverage.getCountry();
 			if (country != null && country.isNew()) {
 				int id = addCountry(db, country);
 				beverage.setCountry(new Country(id, country.getName(), country.getThumbUrl()));
 			}
 			
 			Producer producer = beverage.getProducer();
 			if (producer != null && producer.isNew()) {
 				int id = addProducer(db, producer);
 				beverage.setProducer(new Producer(id, producer.getName()));
 			}
 			
 			Provider provider = beverage.getProvider();
 			if (provider != null && provider.isNew()) {
 				int id = addProvider(db, provider);
 				beverage.setProvider(new Provider(id, provider.getName()));
 			}
 			
 			Category category = beverage.getCategory();
 			if(category != null && category.isNew()) {
 				int id = addCategory(db, category);
 				beverage.setCategory(new Category(id, category.getName()));
 			}
 			

 			ContentValues values = beverage.getAsContentValues();
 			
			int categoryId = values.getAsInteger("category_id");
			if(categoryId == Category.EMPTY_ID) {
 				values.put("category_id", -1);	
 			}
 			
 			db.update(BEVERAGE_TABLE, values, "_id=?", new String[]{String.valueOf(beverage.getId())});
 
 			db.setTransactionSuccessful();
 			return true;		
 		} finally {
 			db.endTransaction();
 			db.close();
 			close();
 		}
 	}
 
 	private Category getCategoryFromCursor(Cursor c) {
 		return new Category(c.getInt(0), c.getString(1));
 	}
 
 	private Country getCountryFromCursor(Cursor c) {
 		return new Country(c.getInt(0), c.getString(1), c.getString(2));
 	}
 	
 	private BeverageType getBeverageTypeFromCursor(Cursor c) {
 		return new BeverageType(c.getInt(0), c.getString(1));
 	}
 
 	private int addCountry(SQLiteDatabase db, Country country) {
 		return addModel(db, country);
 	}
 
 	private int addProducer(SQLiteDatabase db, Producer producer) {
 		return addModel(db, producer);
 	}
 
 	private int addProvider(SQLiteDatabase db, Provider provider) {
 		return addModel(db, provider);
 	}
 
 	private int addCategory(SQLiteDatabase db, Category category) {
 		return addModel(db, category);
 	}
 	
 	private int addModel(SQLiteDatabase db, BaseModel model) {
 		int id = -1;
 		if(model != null) {
 			//Check if exists
 			TableName tableName = model.getClass().getAnnotation(TableName.class);			
 			id = getIdFromName(db, tableName.name(), model.getName());
 			
 			if(id == -1) {				
 				try {
 					Method[] methods = model.getClass().getMethods();
 					ContentValues values = new ContentValues(methods.length);
 				
 					for(Method method : methods) {
 						Annotation[] annotations = method.getDeclaredAnnotations();
 					
 						for(Annotation annotation : annotations){
 							if(annotation instanceof Column){
 								Column column = (Column) annotation;
 							
 								//Assuming that all fields are Strings
 								values.put(column.name(), (String)method.invoke(model, new Object[0]));
 							}
 						}
 					}
 			
 					return (int) db.insert(tableName.name(), null, values);				
 				} catch (Exception e) {
 					Log.e(BeverageDatabaseHelper.class.getName(), "Failed to add model to database", e);
 				}
 			}
 		}
 		
 		return id;
 	}
 
 	private int getIdFromName(SQLiteDatabase db, String table, String name) {
 		if(name != null) {
 			Cursor c = db.query(table, new String[] {"_id"}, "name=?", new String[] {name}, null, null, null);
 			
 			try {
 				if(c.moveToFirst()) {
 					return c.getInt(0);
 				}
 			} finally {			
 				c.close();
 			}
 		}
 		
 		return -1;
 	}
 
 	private boolean exists(Beverage beverage) {
 		Log.d(BeverageDatabaseHelper.class.getName(),"Found beverage: " + (beverage != null ? beverage.toString() : null));
 		
 		if (beverage.isNew()) {
 			if (beverage.getNo() > 0) {
 				// Check if beverage is already added in earlier search
 				int id = getBeverageIdFromNo(beverage.getNo());
 				
 				Log.d(BeverageDatabaseHelper.class.getName(),"Found beverage with id[" + id + "]");
 				
 				beverage.setId(id);
 				return id != -1;
 			}
 			
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean inCellar(int id) {
 		SQLiteDatabase db = getReadableDatabase();			
 		Cursor c = db.query(CELLAR_TABLE, new String[]{"beverage_id"}, "beverage_id = ?", new String[] {String.valueOf(id)}, null, null, null);
 
 		return c.moveToFirst();		
 	}
 	
 	@SuppressWarnings("unused")
 	private void debugSetDBVersion(int version) {
 		SQLiteDatabase db = getWritableDatabase();
 
 		try {
 			db.setVersion(version);
 		} finally {
 			db.close();
 			close();
 		}
 	}
 
 }
