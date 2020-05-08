 package be.uclouvain.sinf1225.gourmet.models;
 
 import java.text.SimpleDateFormat;
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
 	private static final int DATABASE_VERSION = 88;
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
 		int[] ressources = {R.drawable.picture1, R.drawable.picture2, R.drawable.picture3, R.drawable.alterezvous1, R.drawable.alterezvous2, R.drawable.alterezvous3, R.drawable.alterezvous4,
 				 R.drawable.mousse_alterezvous, R.drawable.quichepoireaux_alterezvous, R.drawable.soupe_alterezvous, R.drawable.armes1, R.drawable.armes2, R.drawable.armes3, R.drawable.armes4, 
 				 R.drawable.quai1, R.drawable.quai2, R.drawable.quai3, R.drawable.quai_asp_saum, R.drawable.quai_couteaux, R.drawable.quai_feuille, R.drawable.quai_gourmand, R.drawable.quai_grandlarge, 
 				 R.drawable.quai_turbo, R.drawable.madzebu_carpboeuf, R.drawable.madzebu_salade, R.drawable.madzebu_simenthal, R.drawable.madzebu_vitello, R.drawable.madzebu_dame, R.drawable.madzebu_creme};
 		String[] links = {"img/picture1.png", "img/picture2.png", "img/picture3.png", "img/alterezvous1.png", "img/alterezvous2.png", "img/alterezvous3.png", "img/alterezvous4.png", 
 				 "img/mousse_alterezvous.png", "img/quichepoireaux_alterezvous.png", "img/soupe_alterezvous.png", "img/armes1.png", "img/armes2.png", "img/armes3.png", "img/armes4.png", 
 				 "img/quai1.png", "img/quai2.png", "img/quai3.png", "img/quai_asp_saum.png", "img/quai_couteaux.png", "img/quai_feuille.png", "img/quai_gourmand.png", "img/quai_grandlarge.png",
 				 "img/quai_turbo.png", "img/madzebu_carpboeuf.png", "img/madzebu_salade.png", "img/madzebu_simenthal.png", "img/madzebu_vitello.png", "img/madzebu_dame.png", "img/madzebu_creme.png" };
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
 		boolean ans = cursor != null && cursor.getCount() != 0;
 
 		/* close database */
 	    db.close();
 		
 		return ans;
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
 		{
 			/* close database */
 		    db.close();
 
 		    return null;
 		}
 		
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
 		
 		/* close database */
 	    db.close();
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
 	 * delete image with this path
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
 	 * Return images representing this restaurant
 	 * @param restaurant
 	 * @return list of images
 	 */
 	public List<Image> getImages(Restaurant restaurant)
 	{
 		return getImages("restaurant", restaurant.getId());
 	}
 	
 	/**
 	 * Return images representing this dish
 	 * @param dish
 	 * @return list of images
 	 */
 	public List<Image> getImages(Dish dish)
 	{
 		return getImages("dish", dish.getDishId());
 	}
 	
 	/**
 	 * Return images corresponding to this object.
 	 * Private, because models should not be aware of the sqlite database and how data is stocked inside.
 	 * @param objectType
 	 * @param objectId
 	 * @return list of images
 	 */
 	private List<Image> getImages(String objectType, int objectId)
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
 		{
 			db.close();
 			return null;
 		}
 			
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
 		db.close();
 		return images;
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
 	    values.put("restoId", dish.getRestoId());
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
 	    db.update("dish", values, "`dishId`= ? " , new String[] {Integer.toString(dish.getDishId())});
 	    
 	    db.close(); // Closing database connection
 	}
 	
 	/**
 	 * Delete a dish from database
 	 * @param dish
 	 */
 	public void deleteDish(Dish dish)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		db.delete("dish", "`dishId` = ?", new String[] {Integer.toString(dish.getDishId())});
 	    db.close();
 	}
 	
 	/**
 	 * Get a dish from the database by its dishId
 	 * @param dishId
 	 * @return dish object or null
 	 */
 	public Dish getDish(int dishId)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query("dish", //table to select on
 				new String[]{"dishId","name","restoId","description","price", "spicy","vegan","available","allergen","category"},//column to get
 				"`dishId` = ?", //where clause, ?s will be replaced by...
 				new String[]{""+dishId}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		if(cursor == null)
 		{
 			db.close();
 			return null;
 		}
 		
 		cursor.moveToFirst();
 		Restaurant resto = getRestaurant(cursor.getInt(2));
 		List<Image> imgs = getImages("dish", cursor.getInt(0));
 		Image img = imgs.size() == 0 ? null : imgs.get(0);
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
 				resto, // restaurant
 				img); // image
 
 	    db.close();
 		return dish;
 	}
 	public Dish getDish(String name, int restoId)
 	{
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor cursor = db.query("dish", //table to select on
 				new String[]{"dishId","name","restoId","description","price", "spicy","vegan","available","allergen","category"},//column to get
 				"`name` = ? AND `restoId` = ?", //where clause, ?s will be replaced by...
 				new String[]{name, ""+restoId}, //... these values
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		if(cursor == null)
 		{
 			db.close();
 			return null;
 		}
 		
 		cursor.moveToFirst();
 		Restaurant resto = getRestaurant(cursor.getInt(2));
 		List<Image> imgs = getImages("dish", cursor.getInt(0));
 		Image img = imgs.size() == 0 ? null : imgs.get(0);
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
 				resto, // restaurant
 				img); // image
 
 	    db.close();
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
 				new String[]{Integer.valueOf(restaurant.getId()).toString()}, 
 				null,
 				null,
 				null,
 				null);
 	    
 		if(cursor == null)
 		{
 		    db.close();
 			return null;
 		}
 			
 		cursor.moveToFirst();
 
 		List<Dish> dishes = new ArrayList<Dish>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			List<Image> imgs = getImages("dish", cursor.getInt(0));
 			Image img = imgs.size() == 0 ? null : imgs.get(0);
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
 					restaurant,
 					img);
 
 			dishes.add(dish);
 			cursor.moveToNext();
 		}
 		
 	    db.close();
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
 		{
 		    db.close();
 		    return null;
 		}
 			
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
 				cursor.getString(13)); // email
 		
 		/* close database */
 	    db.close();
 		
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
 		db.close();
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
 		{
 		    return null;
 		}
 			
 		cursor.moveToFirst();
 
 		List<Restaurant> restaurants = new ArrayList<Restaurant>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			//PriceCategory pricecate = 
 			Location loc = new Location("Database");
 			loc.setLongitude(cursor.getDouble(5));
 			loc.setLatitude(cursor.getDouble(6));
 			
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
 					cursor.getString(8));
 
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
 		{
 		    /* close database */
 		    db.close();
 		    return null;
 		}
 			
 		
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
 		 /* close database */
 	    db.close();
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
 		{
 		    /* close database */
 		    db.close();
 		    
 		    return null;
 		}
 			
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
 		/* close database */
 	    db.close();
 	    
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
 		
 	    /* close database */
 	    db.close();
 	    
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
 	    
 	    int ans = db.update("city", values, "`name` = ? AND `country` = ?", new String[] {city.getName(), city.getCountry()});
 
 	    db.close();
 	    
 	    return ans;
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
 	 * @return new reservation id.
 	 */
 	public int addReservation(Reservation reservation)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    ContentValues values1 = new ContentValues();
 	    values1.put("userEmail", reservation.getUserEmail());
 	    values1.put("restoId", reservation.getRestaurant().getId());
 	    values1.put("nbrReservation", Integer.toString(reservation.getnbrReservation()));
 	    values1.put("date", reservation.getDate().toString());
 	    
 	    long resvId = db.insert("reservation", null, values1);
 	    
 	    for(int id : reservation.getDish())
 	    {
 	    	ContentValues values2 = new ContentValues();
 	    	values2.put("dishId", id);
 	    	values2.put("resvId", resvId);
 	    	db.insert("reservationDish", null, values2);
 	    }
 	    db.close();
 	    
 	    return (int) resvId;
 	}
 	
 	/**
 	 * Remove the reservation from the database
 	 * @param reservation
 	 */
 	public void deleteReservation(Reservation reservation)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    db.delete("reservation", "`resvId` = ?", new String[] {Integer.toString(reservation.getId())});
 	    db.delete("reservationDish", "`resvId` = ?", new String[] {Integer.toString(reservation.getId())});
 	    db.close();
 	}
 	
 	/**
 	 * Update reservation in database
 	 * @param reservation
 	 */
 	public void updateReservation(Reservation reservation)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    ContentValues values1 = new ContentValues();
 	    
 	    values1.put("userEmail", reservation.getUserEmail());
 	    values1.put("resto", reservation.getRestaurant().getName());
 	    values1.put("nbrReservation", Integer.toString(reservation.getnbrReservation()));
 	    values1.put("date", reservation.getDate().toString());
 	    db.update("reservation", values1, "`resvId` = ?", new String[] {Integer.toString(reservation.getId())});
 	    
 	    //delete reservationDish dishes
 	    db.delete("reservationDish", "`resvId` = ?", new String[] {Integer.toString(reservation.getId())});
 	    //and re-add them
 	    for(int id : reservation.getDish())
 	    {
 	    	ContentValues values = new ContentValues();
 	    	values.put("dishId", id);
 	    	values.put("resvId", reservation.getId());
 	    	db.insert("reservationDish", null, values);
 	    }
 	    db.close();
 	}
 
 	/**
 	 * Get reservation in database
 	 */
 	public Reservation getReservation(String userEmail, int restoId, Date date)
 	{
 		/* open database */
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		/* query - get all the rows concerned */
 		String str[] = {userEmail, Integer.toString(restoId), date.toString()};
 		Cursor cursor = db.query("reservation", //table to select on
 				new String[]{"resvId","nbrReservation"},
 				"`userEmail` = ? AND `restoId` = ? AND `date` = ?", //where
 				str, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		if(cursor == null || cursor.getCount() != 1) 
 		{
 			db.close();
 			return null;
 		}
 		cursor.moveToFirst();
 		
 		/* missing data */
 		int resvId = cursor.getInt(0);
 		int nbrResv = cursor.getInt(1);
 		
 		/* Reservation to return */
 		Reservation resv = new Reservation(userEmail, Restaurant.getRestaurant(restoId), nbrResv, date);
 		resv.setId(resvId);
 		
 		/* get the dishes */
 		cursor = db.query("reservationDish", //table to select on
 				new String[]{"dishId"},
 				"`resvId` = ?", //where
 				new String[] {Integer.toString(resvId)}, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		if(cursor == null) 
 		{
 			db.close();
 			return null;
 		}
 		
 		/* add the dishes to the list of dishes */
 		cursor.moveToFirst();
 		while(cursor.isAfterLast())
 		{
 			resv.addDish(cursor.getInt(0));
 			cursor.moveToNext();
 		}
 		
 	    /* close database */
 	    db.close();
 	    
 		return resv;
 	}
 
 	
 	// TODO
 	public List<Reservation> getReservationInRestaurant(Restaurant restaurant) { return null; }
 	
 	/**
 	 * 
 	 * @param user The user from who you want all the reservations
 	 * */
 	public List<Reservation> getReservationByUser(User user) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query(true,"reservation", //table to select on
 				new String[]{"resvId","userEmail", "restoId", "nbrReservation","date"}, //column to get WARNING MISSING TIME
 				"`UserEmail` = ?", 
 				new String[]{user.getEmail()}, 
 				null,
 				null,
 				null,
 				null);
 		if(cursor == null)
 		{
 		    return null;
 		}
 		
 		System.out.println("Cursor length = "+cursor.getCount());
 		
 		cursor.moveToFirst();
 
 		List<Reservation> reservations = new ArrayList<Reservation>();
 		
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			//getting all necessary informations to construct a reservation object.
 			int resvId = cursor.getInt(0);
 			String userEmail = cursor.getString(1);
 			int restoId = cursor.getInt(2);
 			Restaurant restaurant = Restaurant.getRestaurant(restoId);
 			int nbrReservation = cursor.getInt(3);
 			List<Integer> dishes = null; //TODO GET DISHES
 			String dateText = cursor.getString(4);
 			
 		    @SuppressWarnings("deprecation")
 			Date date = new Date(dateText);
 			Reservation rsv = new Reservation(resvId,userEmail,restaurant,nbrReservation,dishes,date);
 			System.out.println(rsv);
 
 			reservations.add(rsv);
 			cursor.moveToNext();
 
 		}
 		cursor.close();
 		db.close();
 		return reservations;
 	}
 	public void updateTimeTable(TimeTable table)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 	    ContentValues values1 = new ContentValues();
 	    
 	    values1.put("day", table.getDay());
 	    values1.put("morningopening", table.getMorningOpening());
 	    values1.put("morningclosing", table.getMorningClosing());
 	    values1.put("eveningopening", table.getEveningOpening());
 	    values1.put("eveningclosing", table.getEveningClosing());
 	    values1.put("close", table.getClose());
 	    db.update("horaire", values1, "`restoId` = ? AND `day` = ? ", new String[] {Integer.toString(table.getRestoId()), table.getDay()});
 	    db.close();
 	}
 	public List<TimeTable> getTimeTable(int restoId){
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("horaire", //table to select on
 				new String[]{"day","morningopening","morningclosing","eveningopening", "eveningclosing", "restoId", "close"}, //column to get
 				"`restoId` = ?", //where
 				new String[]{restoId + ""}, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		if(cursor == null || cursor.getCount() == 0)
 		{
 		    /* close database */
 		    db.close();
 		    
 		    return null;
 		}
 			
 		cursor.moveToFirst();
 		
 		List<TimeTable> timeTable = new ArrayList<TimeTable>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			if (cursor.getInt(6) ==0){
 			timeTable.add(new TimeTable(cursor.getString(1),cursor.getString(2), cursor.getString(3), cursor.getString(4),cursor.getString(0), cursor.getInt(6), cursor.getInt(5)));}
 			cursor.moveToNext();
 		}
 		/* close database */
 	    db.close();
 	    
 		return timeTable;
 		
 	}
 	public List<TimeTable> getFullTimeTable(int restoId){
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor cursor = db.query("horaire", //table to select on
 				new String[]{"day","morningopening","morningclosing","eveningopening", "eveningclosing", "restoId", "close"}, //column to get
 				"`restoId` = ?", //where
 				new String[]{restoId + ""}, //where string
 				null, //group by
 				null, //having
 				null); //orderby
 		
 		if(cursor == null || cursor.getCount() == 0)
 		{
 		    /* close database */
 		    db.close();
 		    
 		    return null;
 		}
 			
 		cursor.moveToFirst();
 		
 		List<TimeTable> timeTable = new ArrayList<TimeTable>();
 		for(int j = 0; j < cursor.getCount(); j++)
 		{
 			timeTable.add(new TimeTable(cursor.getString(1),cursor.getString(2), cursor.getString(3), cursor.getString(4),cursor.getString(0), cursor.getInt(6), cursor.getInt(5)));
 			cursor.moveToNext();
 		}
 		/* close database */
 	    db.close();
 	    
 		return timeTable;
 	}
 	public void addTimeTable(TimeTable table)
 	{
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 	    values.put("day", table.getDay());
 	    values.put("morningopening", table.getMorningOpening());
 	    values.put("morningclosing", table.getMorningClosing());
 	    values.put("eveningopening", table.getEveningOpening());
 	    values.put("eveningclosing", table.getEveningClosing());
 	    values.put("restoId", table.getRestoId());
 	    values.put("close", table.getClose());
 	    db.insert("horaire", null, values);
 	    
 	    db.close(); // Closing database connection
 	}
 	
 }
