 package core.db;
 
 public class DB 
 {
 	private static DB instance;
 	private Database database;
 	
 	public void use(Database database)
 	{
 		this.database = database;
 	}
 	
 	public boolean isConnected()
 	{
 		if (database == null)
 		{
 			return false;
 		}
 		
 		return database.isConnected();
 	}
 
 	public void put(String collectionId, String objectId, DatabaseObject object)
 	{
		if (database == null || !database.isConnected())
 		{
 			return;
 		}
 		
 		database.put(collectionId, objectId, object);
 	}
 	
 	public DatabaseObject get(String collectionId, String objectId)
 	{
		if (database == null || !database.isConnected())
 		{
 			return null;
 		}
 
 		return database.get(collectionId, objectId);
 	}
 	
 	public boolean exists(String collectionId, String objectId)
 	{
		if (database == null || !database.isConnected())
 		{
 			return false;
 		}
 		
 		return database.exists(collectionId, objectId);
 	}
 	
 	public static DB getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new DB();
 		}
 		
 		return instance;
 	}
 }
