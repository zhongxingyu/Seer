 package be.uclouvain.sinf1225.gourmet.models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.location.Location;
 import android.util.Log;
 import be.uclouvain.sinf1225.gourmet.Gourmet;
 import be.uclouvain.sinf1225.gourmet.R;
 import be.uclouvain.sinf1225.gourmet.enums.PriceCategory;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetFiles;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 
 /**
  * Layer between the models classes and the database.
  * ONLY models should access to this class!
  * NO verification will be done on rights!
  * @author guillaumederval
  */
 class GourmetDatabase extends SQLiteOpenHelper
 {
	private static final int DATABASE_VERSION = 27;
 
     private static final String DATABASE_NAME = "gourmet";
     private Context context;
     
     /**
      * Initialize database
      * @param context Context which call the database
      */
 	public GourmetDatabase(Context context)
 	{
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = context;
 	}
 	
 	/**
 	 * Initialize database
 	 */
 	public GourmetDatabase()
 	{
 		super(Gourmet.getAppContext(), DATABASE_NAME, null, DATABASE_VERSION);
 		this.context = Gourmet.getAppContext();
 	}
 	
 	private void initImages()
 	{
 		int[] ressources = {R.drawable.picture1, R.drawable.picture2, R.drawable.picture3};
 		String[] links = {"img/picture1.png", "img/picture2.png", "img/picture3.png"};
 		for(int i = 0; i < ressources.length; i++)
 			GourmetFiles.exportFileFromResToDisk(links[i], ressources[i]);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db)
 	{
 		if(db.isReadOnly())
 		{
 			return;
 		}
 		else
 		{
 			String sql = GourmetUtils.readRawTextFile(this.context, R.raw.dbv1);
 			StringTokenizer st = new StringTokenizer(sql, ";");
 			while(st.hasMoreElements())
 			{
 				String t = st.nextToken();
 				db.execSQL(t);
 			}
 			initImages();
 		}
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
 	{
 		onCreate(db);
 	}
 
 	/* User */
 	/**
 	 * Return true if an user with this email exists in database
 	 */
 	public boolean haveUserWithEmail(String email)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("users", //table to select on
 				new String[]{"email"}, //TODO add missing columns
 				"`email` = ?", //where clause, ?s will be replaced by...
 				new String[]{email}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		return cursor != null && cursor.getCount() != 0;
 	}
 	
 	/**
 	 * Add an user to the database
 	 * @param user the user to add
 	 */
 	public void addUser(User user)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put("email", user.getEmail());
 	    values.put("password", user.getPasswordHash());
 	    values.put("name", user.getName());
 	    values.put("surname", user.getSurname());
 	    db.insert("users", null, values);
 	    
 	    db.delete("users_manages", "`email` = ?", new String[] {user.getEmail()});
 	    db.close();
 	    
 	    if(user instanceof Restaurator)
 	    {
 	    	Restaurator r = (Restaurator)user;
 	    	for(Integer v : r.getRestoIds())
 	    	{
 	    		ContentValues v2 = new ContentValues();
 	    		v2.put("email", r.getEmail());
 	    		v2.put("restoId", v);
 	    		db.insert("users_manages", null, v2);
 	    	}
 	    }
 	    
 	    db.close(); // Closing database connection
 	}
 	
 	/**
 	 * Get the user with this email and password
 	 * @param email
 	 * @param password
 	 * @param isHash is password a SHA1 hash?
 	 * @return User object if a correspondent user exists, null else.
 	 */
 	public User getUser(String email, String password, boolean isHash)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("users", //table to select on
 				new String[]{"name","surname"}, //TODO add missing columns
 				"`email` = ? AND `password` = ?", //where clause, ?s will be replaced by...
 				new String[]{email, (isHash ? password : GourmetUtils.sha1(password))}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null || cursor.getCount() == 0)
 			return null;
 		
 		cursor.moveToFirst();
 		
 		Cursor cursorResto = db.query("users_manages", //table to select on
 				new String[]{"restoId"}, //TODO add missing columns
 				"`email` = ?", //where clause, ?s will be replaced by...
 				new String[]{email}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		User user = null;
 		if(cursorResto == null || cursorResto.getCount() == 0)
 		{
 			user = new User(email, password, cursor.getString(0), cursor.getString(1));
 		}
 		else
 		{
 			List<Integer> restoIds = new ArrayList<Integer>();
 			cursorResto.moveToFirst();
 			for(int i = 0; i < cursorResto.getCount(); i++)
 			{
 				restoIds.add(cursorResto.getInt(0));
 				cursorResto.moveToNext();
 			}
 			user = new Restaurator(email, password, cursor.getString(0), cursor.getString(1),restoIds);
 		}
 		return user;
 	}
 
 	/**
 	 * Update an existing user in database.
 	 * @param user
 	 */
 	public void updateUser(User user)
 	{
 		//TODO implement. Must works with Restaurator too.
 	}
 	//TODO implement. Commented lines are not essential
 
 	public User getUser(String username) { return null; }
 	//public List<User> getAllUsers() { return null; }
 	//public void deleteUser(User user) {}
 
 	
 	/*Image*/
 	/**
 	 * add an image
 	 * @param img
 	 */
 	public void addImage(Image img)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put("path", img.getPath());
 	    values.put("objectType", img.getObjectType());
 	    values.put("objectId", img.getObjectId());
 	    values.put("legend", img.getLegend());
 	    db.insert("image", null, values);
 	    
 	    db.close(); // Closing database connection
 	}
 	/**
 	 * YOU CAN ONLY USE THIS METHOD WITH A DISH! A RESTAURANT CAN HAVE MORE THAN ONE IMAGE
 	 * @param type
 	 * @param id
 	 */
 	public void deleteImage(String type, int id)
 	{
 		Log.d("es", type + " --->" + id);
 		SQLiteDatabase db = this.getWritableDatabase();
 	    int i = db.delete("image", "`objectType` = ? AND `objectId` = ?", new String[] {type, ""+id});
 	    Log.d("es", i + "");
 	    db.close();
 	}
 	/**
 	 * delete one image from the path
 	 * @param path
 	 */
 	public void deleteImage(String path)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    int i = db.delete("image", "`path` = ?", new String[] {path});
 	    Log.d("MyApp", "------->"+i);
 	    db.close();
 	}
 	/**
 	 * 
 	 * @return imageList
 	 */
 	public List<Image> getAllImages(String objectType, int objectId)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("image", //table to select on
 				new String[]{"legend","path","objectType","objectId"}, //column to get
 				 "`objectType` = ? AND `objectId` = ?", //where
 				new String[] {objectType, ""+objectId}, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null)
 			return null;
 		cursor.moveToFirst();
 		
 		List<Image> images = new ArrayList<Image>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			Image image = new Image(
 					cursor.getString(0),
 					cursor.getString(1),
 					cursor.getString(2),
 					cursor.getInt(3));
 
 		
 			images.add(image);
 			cursor.moveToNext();
 		}
 		return images;
 	}
 	/**
 	 * 
 	 * @param objectId
 	 * @param objectType
 	 * @return image link to an object
 	 */
 	public Image getImage(int id, String type)
 	{
 		List<Image> images =getAllImages(type, id);
 		if ( images == null || images.size() == 0) return null;
 		else return images.get(0);
 		
 	}
 	/**
 	 * 
 	 * @param imageId
 	 * @return image with the id
 	 */
 	public Image getImage(int imageId)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("image", //table to select on
 				new String[]{"legend","path","objectType","objectId"}, //column to get
 				 "`rowId` = ?", //where
 				new String[] {""+imageId}, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null)
 			return null;
 		cursor.moveToFirst();
 		Image image = new Image(
 				cursor.getString(0),
 				cursor.getString(1),
 				cursor.getString(2),
 				cursor.getInt(3)
 				);
 		
 		return image;
 	}
 	/* Dish */
 	/**
 	 * Add a dish to database
 	 * @param dish
 	 */
 	public void addDish(Dish dish)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put("name", dish.getName());
 	    values.put("category", dish.getCategory());
 	    values.put("description", dish.getDescription());
 	    values.put("price", dish.getPrice());
 	    values.put("spicy", dish.getSpicy());
 	    values.put("vegan", dish.getVegan());
 	    values.put("available", dish.getAvailable());
 	    values.put("allergen", dish.getAllergen());
 	    db.insert("dish", null, values);
 	    
 	    db.close(); // Closing database connection
 	}
 	
 	/**
 	 * Update a dish in database
 	 * @param dish
 	 */
 	public void updateDish(Dish dish)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 
 	    ContentValues values = new ContentValues();
 	    values.put("name", dish.getName());
 	    values.put("category", dish.getCategory());
 	    values.put("description", dish.getDescription());
 	    values.put("price", dish.getPrice());
 	    values.put("spicy", dish.getSpicy());
 	    values.put("vegan", dish.getVegan());
 	    values.put("available", dish.getAvailable());
 	    values.put("allergen", dish.getAllergen());
 	    values.put("restoId", dish.getRestoId());
 	    db.update("dish", values, "`dishId`= ? " , new String[] {""+dish.getDishId()});
 	    
 	   // db.close(); // Closing database connection
 	}
 	
 	/**
 	 * Delete a dish from database
 	 * @param dish
 	 */
 	public void deleteDish(Dish dish)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    db.delete("dish", "`name` = ?", new String[] {""+dish.getDishId()});
 	    db.close();
 	}
 	
 	/**
 	 * Get a dish from the database by its dishId
 	 * @param dishId
 	 * @return dish object or null
 	 */
 	public Dish getDish(int dishId)
 	{
 		//TODO implements
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query("dish", //table to select on
 				new String[]{"dishId","name","restoId","description","price", "spicy","vegan","available","allergen","category"},//column to get
 				"`dishId` = ?", //where clause, ?s will be replaced by...
 				new String[]{""+dishId}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null)
 			return null;
 		
 		cursor.moveToFirst();
 		Restaurant resto = getRestaurant(cursor.getInt(2));
 		/**Image img = getImage(cursor.getInt(0), "dish");**/
 		Dish dish= new Dish(
 				cursor.getInt(0), //dishId
 				cursor.getString(1), //name
 				cursor.getInt(2), // restoId
 				cursor.getString(3), //description
 				cursor.getDouble(4), //price
 				cursor.getInt(5), //spicy
 				cursor.getInt(6), //vegan
 				cursor.getInt(7), //available
 				cursor.getInt(8), //allergen
 				cursor.getString(9),//category
 				resto // restaurant
 				/**img*/); // image
 		
 		return dish;
 	}
 	
 	/**
 	 * Return all dishes availables in a certain restaurant
 	 * @param restaurant
 	 * @return List of dishes
 	 */
 	public List<Dish> getDishInRestaurant(Restaurant restaurant)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query(true,"dish", //table to select on
 				new String[]{"dishId",
 				"name",
 				"restoId",
 				"description",
 				"price",
 				"spicy",
 				"vegan",
 				"available",
 				"allergen",
 			 	"category"},//column to get
 				"`restoId` = ?", 
 				new String[]{String.valueOf(restaurant.getId())}, 
 				null,
 				null,
 				null,
 				null);
 		if(cursor == null)
 			return null;
 		cursor.moveToFirst();
 
 		List<Dish> dishes = new ArrayList<Dish>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			Dish dish = new Dish(cursor.getInt(0), 
 					cursor.getString(1), 
 					cursor.getInt(2), 
 					cursor.getString(3),
 					cursor.getDouble(4),
 					cursor.getInt(5), 
 					cursor.getInt(6), 
 					cursor.getInt(7), 
 					cursor.getInt(8),
 					cursor.getString(9), 
 					restaurant);
 
 			dishes.add(dish);
 			cursor.moveToNext();
 
 		}
 		
 		return dishes;
 	}
 	
 	/* Restaurant */
 	
 	/**
 	 * Get a restaurant by its id
 	 * @param restoId
 	 * @return a Restaurant object, or null if it does not exist in database
 	 */
 	public Restaurant getRestaurant(int restoId)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("restaurant", //table to select on
 				new String[]{"restoId","name","cityName","cityCountry","address", "priceCat","longitude","latitude","phone","seats","website","description","stars","email"},//column to get
 				"`restoId` = ?", //where clause, ?s will be replaced by...
 				new String[]{""+restoId}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null || cursor.getCount() == 0)
 			return null;
 		
 		cursor.moveToFirst();
 		Location loc = new Location("Database");
 		loc.setLongitude(cursor.getDouble(6));
 		loc.setLatitude(cursor.getDouble(7));
 		
 		Restaurant resto = new Restaurant(
 				cursor.getInt(0), //id
 				City.getCity(cursor.getString(2), cursor.getString(3)), //city 
 				cursor.getString(1), //name
 				cursor.getString(4), // address
 				PriceCategory.values()[cursor.getInt(5)], //priceCategory
 				loc, //location
 				cursor.getString(8), //phone
 				cursor.getInt(9), //seats
 				cursor.getString(10), //website
 				cursor.getString(11), //description
 				cursor.getInt(12), //stars
 				cursor.getString(13), // email
 				null); //dishesID
 		
 		return resto;
 	}
 	
 	/**
 	 * Update restaurant in database
 	 * @param restaurant
 	 */
 	public void updateRestaurant(Restaurant restaurant)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put("name", restaurant.getName());
 		values.put("cityName", restaurant.getCity().getName());
 		values.put("cityCountry", restaurant.getCity().getCountry());
 		values.put("address", restaurant.getAddress());
 		values.put("longitude", restaurant.getLocation().getLongitude());
 		values.put("latitude", restaurant.getLocation().getLatitude());
 		values.put("description", restaurant.getDescription());
 		values.put("email", restaurant.getEmail());
 		values.put("stars", restaurant.getStars());
 		values.put("phone", restaurant.getPhone());
 		values.put("website", restaurant.getWebsite());
 		values.put("seats", restaurant.getSeats());
 		values.put("priceCat", restaurant.getPriceCategory().ordinal());
 		db.update("restaurant", values, "`restoId` = ?", new String[]{ ""+restaurant.getId()});
 	}
 	
 	/**
 	 * Get all restaurant available in a certain city
 	 * @param city
 	 * @return List of restaurants in city
 	 */
 	public List<Restaurant> getRestaurantsInCity(City city)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query(true,"restaurant", //table to select on
 				new String[]{"restoId","name","cityName","cityCountry","address","longitude","latitude","description","email","stars","phone","website","seats","priceCat"}, //column to get
 				"`cityName` = ? AND `cityCountry` = ?", 
 				new String[]{city.getName(),city.getCountry()}, 
 				null,
 				null,
 				null,
 				null);
 		if(cursor == null)
 			return null;
 		cursor.moveToFirst();
 
 		List<Restaurant> restaurants = new ArrayList<Restaurant>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			//PriceCategory pricecate = 
 			Location loc = new Location("Database");
 			loc.setLongitude(cursor.getDouble(5));
 			loc.setLatitude(cursor.getDouble(6));
 
 			List<Integer> dishesID = new ArrayList<Integer>();
 			Cursor dishes = db.query(true,"dish", 
 					new String[]{"dishId"},
 					"`restoId` = ?", 
 					new String[]{String.valueOf(cursor.getInt(0))}, 
 					null,
 					null, 
 					null,
 					null);
 			if(dishes != null && cursor.getCount() != 0)
 			{
 				dishes.moveToFirst();
 				for(int i = 0; i < dishes.getCount(); i++)
 				{
 					dishesID.add(dishes.getInt(0));
 					dishes.moveToNext();
 				}
 			}	
 			Restaurant resto = new Restaurant(cursor.getInt(0), 
 					city, 
 					cursor.getString(1), 
 					cursor.getString(4), 
 					PriceCategory.values()[cursor.getInt(13)], 
 					loc, 
 					cursor.getString(10), 
 					cursor.getInt(12), 
 					cursor.getString(11),
 					cursor.getString(7),
 					cursor.getInt(9), 
 					cursor.getString(8), 
 					dishesID);
 
 			restaurants.add(resto);
 			cursor.moveToNext();
 
 		}
 		
 		return restaurants;
 	}
 	
 	/* Cities */
 	
 	/**
 	 * Add a city to the database
 	 * @param city
 	 */
 	public void addCity(City city)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put("name", city.getName());
 	    values.put("country", city.getCountry());
 	    values.put("longitude", city.getLocation().getLongitude());
 	    values.put("latitude", city.getLocation().getLatitude());
 	    db.insert("city", null, values);
 	    
 	    db.close(); // Closing database connection
 	}
 	
 	/**
 	 * Get a city from the database
 	 * @param name Name of the city
 	 * @param country Country of the city
 	 * @return a City object if object exists, null else
 	 */
 	public City getCity(String name, String country)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("city", //table to select on
 				new String[]{"name","country","longitude","latitude"}, //column to get
 				"`name` = ? AND `country` = ?", //where clause, ?s will be replaced by...
 				new String[]{name,country}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null || cursor.getCount() == 0)
 			return null;
 		
 		cursor.moveToFirst();
 		
 		Location loc = new Location("Database");
 		loc.setLongitude(cursor.getDouble(2));
 		loc.setLatitude(cursor.getDouble(3));
 		
 		List<Integer> restaurantsID = new ArrayList<Integer>();
 		cursor = db.query(true, "restaurant", new String[]{"restoId"}, "`cityName` = ? AND `cityCountry` = ?", new String[]{name, country}, null, null, null,null);
 		if(cursor != null && cursor.getCount() != 0)
 		{
 			cursor.moveToFirst();
 			for(int i = 0; i < cursor.getCount(); i++)
 			{
 				restaurantsID.add(cursor.getInt(0));
 				cursor.moveToNext();
 			}
 		}
 		
 		return new City(name,country,loc, restaurantsID);
 	}
 	
 	/**
 	 * Return all cities available in database
 	 * @return all cities available in database
 	 */
 	public List<City> getAllCities()
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("city", //table to select on
 				new String[]{"name","country","longitude","latitude"}, //column to get
 				null, //where
 				null, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null || cursor.getCount() == 0)
 			return null;
 		cursor.moveToFirst();
 		
 		List<City> cities = new ArrayList<City>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			Location loc = new Location("Database");
 			loc.setLongitude(cursor.getDouble(2));
 			loc.setLatitude(cursor.getDouble(3));
 		
 			List<Integer> restaurantsID = new ArrayList<Integer>();
 			Cursor restos = db.query(true, "restaurant", new String[]{"restoId"}, "`cityName` = ? AND `cityCountry` = ?", new String[]{cursor.getString(0),cursor.getString(1)}, null, null, null,null);
 			if(restos != null && cursor.getCount() != 0)
 			{
 				restos.moveToFirst();
 				for(int i = 0; i < restos.getCount(); i++)
 				{
 					restaurantsID.add(restos.getInt(0));
 					restos.moveToNext();
 				}
 			}
 		
 			cities.add(new City(cursor.getString(0),cursor.getString(1),loc, restaurantsID));
 			cursor.moveToNext();
 		}
 		return cities;
 	}
 	
 	/**
 	 * Return number of cities available in the database
 	 * @return number of cities available in the database
 	 */
 	public int getCitiesCount()
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("city", //table to select on
 				new String[]{"name","country"}, //column to get
 				null, //where
 				null, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		if(cursor == null)
 			return 0;
 		return cursor.getCount();
 	}
 	
 	/**
 	 * Update city 
 	 * @param city
 	 * @return number of row updated
 	 */
 	public int updateCity(City city)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put("name", city.getName());
 	    values.put("country", city.getCountry());
 	    values.put("longitude", city.getLocation().getLongitude());
 	    values.put("latitude", city.getLocation().getLatitude());
 	    db.insert("city", null, values);
 	    
 	    return db.update("city", values, "`name` = ? AND `country` = ?", new String[] {city.getName(), city.getCountry()});
 	}
 	
 	/**
 	 * Delete a city
 	 * @param city
 	 */
 	public void deleteCity(City city)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    db.delete("city", "`name` = ? AND `country` = ?", new String[] {city.getName(), city.getCountry()});
 	    db.close();
 	}
 	
 	/* Reservation */
 	
 	/** 
 	 * Add a reservation in the database
 	 * @param reservation
 	 */
 	public int addReservation(Reservation reservation)
 	{
 		// ouverture de la base de données
 		SQLiteDatabase db = this.getWritableDatabase();
 	    ContentValues values1 = new ContentValues();
 	    
 	    // valeur des diffŽrents champs
 	    values1.put("userEmail", reservation.getUserEmail());
 	    values1.put("resto", reservation.getRestaurant().getName());
 	    values1.put("nbrReservation", Integer.toString(reservation.getnbrReservation()));
 	    values1.put("date", reservation.getDate().toString());
 	    
 	    // insertion dans la base de donnŽes
 	    long resvId = db.insert("reservation", null, values1);
 	    
 	    // insertion des diffŽrents plats commandŽs dans la base de donnŽes
 	    ContentValues values2 = new ContentValues();
 	    for(DishNode node : reservation.getDish())
 	    {
 	    	values2.put("nameDish", node.dish.getName());
 	    	values2.put("nbrDish", node.nbrDishes);
 	    	values2.put("resvId", resvId);
 	    	db.insert("reservationDish", null, values2);
 	    }
 	    db.close();
 	    return 1;
 	}
 	
 	/**
 	 * Insert a new reservation into the database
 	 * @param restoID
 	 * @param email
 	 * @param nbr
 	 * @param date
 	 * @return the rowID of the new reservation (-1 in case of error)
 	 */
 	public int addReservation(int restoID, String email, int nbr, Date date)
 	{
 		/* open database */
 		SQLiteDatabase db = this.getWritableDatabase();
 	    ContentValues values = new ContentValues();
 	    
 	    /* put values */
 	    values.put("userEmail", email);
 	    values.put("restoId", restoID);
 	    values.put("nbrReservation", Integer.toString(nbr));
 	    values.put("date", date.toString());
 	    
 	    /* insert the new reservation into the database */
 	    int rowID = (int) db.insert("reservation", null, values);
 	    
 	    /* Close database */
 	    db.close();
 	    
 	    /* return the rowID */
 	    return rowID; 
 	}
 	
 	/**
 	 * Remove the reservation from the database
 	 * @param resvID
 	 * @return 0 in case of error, 
 	 * 			the rowID otherwise
 	 */
 	public int deleteReservation(int resvID)
 	{
 		/* open database */
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		/* remove the reservation */
 	    int rowID = db.delete("reservation", "`resvId` = ?", new String[] {Integer.toString(resvID)});
 	    
 	    /* close database */
 	    db.close();
 	    
 	    /* return the rowID */
 		return rowID;
 	}
 	
 	/**
 	 * @param email
 	 * @param restoID
 	 * @param date
 	 * @return success : the resvID
 	 * 			error : -1
 	 */
 	public int getResvID(String email, int restoID, Date date)
 	{
 		/* Open database */
 	    SQLiteDatabase db = this.getReadableDatabase();
 	    
 	    /* Get the resvID */
 	    String str[] = new String[] {email, Integer.toString(restoID), date.toString()};
 	    Cursor cursor = db.rawQuery("select resvId from reservation where userEmail = ? AND restoId = ? AND date = ?", str);
 	    if(cursor == null || cursor.getCount() != 1) return (-1);
 	    cursor.moveToFirst();
 	    int resvID = cursor.getInt(0);
 	    
 	    /* close database */
 	    db.close();
 	    
 	    /* return the resvId of the reservation */
 		return resvID;
 	}
 	
 	/**
 	 * add a dish to a reservation
 	 * @param resvID
 	 * @param dish
 	 * @param nbr
 	 * @return success : rowID where the new dish has been added
 	 * 			error : -1
 	 */
 	public int addReservationDish (int resvID, String dish, int nbr)
 	{
 		/* open database */
 		SQLiteDatabase db = this.getWritableDatabase();
 	    ContentValues values = new ContentValues();
 	    
 	    /* put values */
 	    values.put("resvId", Integer.toString(resvID));
 	    values.put("nameDish", dish);
 	    values.put("nbrDish", Integer.toString(nbr));
 	    
 	    /* insert the new reservation into the database */
 	    int rowID = (int) db.insert("reservationDish", null, values);
 	    
 	    /* Close database */
 	    db.close();
 	    
 	    /* return the rowID */
 	    return rowID; 
 	}
 	
 	/**
 	 * remove a dish from a reservation
 	 * @param resvID
 	 * @param dish
 	 * @param nbr
 	 * @return success : rowID from the removed element
 	 * 			error : 0
 	 */
 	public int deleteResvervationDish (int resvID, String dish, int nbr)
 	{
 		/* open database */
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		/* remove the reservation */
 		String str[] = new String[] {Integer.toString(resvID), dish, Integer.toString(nbr)};
 	    int rowID = db.delete("reservationDish", "`resvId` = ? AND `nameDish`= ? AND `nbrDish`= ?", str);
 	    
 	    /* close database */
 	    db.close();
 	    
 	    /* return the rowID */
 		return rowID;
 	}
 	
 	// TODO
 	//public List<Reservation> getAllReservations() { return null; }
 	public List<Reservation> getReservationInRestaurant(Restaurant restaurant) { return null; }
 	public List<Reservation> getReservationByUser(User user) { return null; }
 	public void updateReservation(Reservation reservation) {}	
 }
