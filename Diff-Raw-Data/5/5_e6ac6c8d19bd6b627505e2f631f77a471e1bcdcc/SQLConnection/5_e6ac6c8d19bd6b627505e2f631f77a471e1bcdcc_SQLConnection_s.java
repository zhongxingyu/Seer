  package LocationMapper;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import LocationMapper.LocationMapper;
 import LocationMapper.Log;
 import LocationMapper.Record;
 
 
 public class SQLConnection 
 {
 	public final int timeout = 5;
 	public final int batchCount = 5000;
 	public final int fetchSize = 20000;
 	public int parseCount = 0;
 	
 	
 	Connection connection = null;
 	ResultSet results = null;
 	
 	public final String statement = "" +
 	"SELECT id, interaction_geo_latitude, interaction_geo_longitude, twitter_user_location, twitter_user_lang " +
	//"FROM datasift_results " +
 	"WHERE id > 33900000 " +
	"WHERE country is null " + 
 	"AND (twitter_user_location is not null or interaction_geo_latitude is not null)";
 
 	
 //	final String statement = "" +
 //			"SELECT id, interaction_geo_latitude, interaction_geo_longitude, twitter_user_location, twitter_user_lang " +	//"WHERE id > 33980000 " + // AND id < 33100000 " + //" + // id > 25800000 " + //
 //			"from datasift_results " +
 //			"WHERE interaction_created_at > '2013-04-01 00:00:00' " +
 //			"AND datasift_stream_id in (78, 88) " +
 //			"AND (twitter_user_location is not null or interaction_geo_latitude is not null)";
 	
 	
 	
 	public String address = null;
 	public String tableName = null;
 	public String port = null;
 	public String userName = null;
 	public String password = null;
 	
 	ArrayList<String> sendStrings = new ArrayList<String>();
 	
 	
 	public SQLConnection(String address, String tableName, String port, String userName, String password)
 	{
 		this.address = address;
 		this.tableName = tableName;
 		this.port = port;
 		this.userName = userName;
 		this.password = password;
 	}
 	
 	
 	public boolean updateRecord(Record record)
 	{
 		
 		String tempString = record.getUpdateStatement();
 		sendStrings.add(tempString);
 		
 		parseCount++;
 		if(parseCount % this.batchCount == 0)
 		{
 			flush(sendStrings);
 			Log.log("ParseCount = " + parseCount);
 			sendStrings.clear();
 		}
 		
 		return true;
 	}
 	
 	
 	public String printString()
 	{
 		String sendString = "";
 		for(String string : sendStrings)
 			sendString += string + "\n";
 		
 		return sendString;
 	}
 	
 	
 	
 	public void flush(List<String> stringsToBeSent)// throws SQLException
 	{
 		if(stringsToBeSent == null || stringsToBeSent.size() == 0)
 		{
 			Log.log("Error in flush: if(stringsToBeSent == null || stringsToBeSent.size() == 0) evaluated to true..." + stringsToBeSent);
 			return;
 		}
 
 		Statement stmt = null;
 		try {
 			stmt = connection.createStatement();
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			Log.log("error:  stmt = connection.createStatement();", e1);
 			LocationMapper.Exit(5);
 		}
 		
 		
 		String sendString = "";
 		
 		for(String string : stringsToBeSent)
 			sendString += string;
 		
 		
 		
 		try
 		{
 			stmt.executeUpdate(sendString);
 		}
 		catch (SQLException e)
 		{
 			
 			if(stringsToBeSent.size() == 1)
 			{
 				Log.log("ERRROR: bad send String: " + stringsToBeSent.get(0));
 				stringsToBeSent.clear();
 				return;
 			}
 			
 			List<String> list1 = new ArrayList<String>(stringsToBeSent.subList(0, stringsToBeSent.size() / 2));
 			List<String> list2 = new ArrayList<String>(stringsToBeSent.subList(stringsToBeSent.size() / 2, stringsToBeSent.size()));
 	
 			flush(list1);
 			flush(list2);
 		}
 		
 		stringsToBeSent.clear();
 	}
 	
 	
 	public void close() throws SQLException
 	{
 		flush(sendStrings);
 		connection.close();
 	}
 	
 	
 	public boolean isConnected()
 	{
 		boolean data = false;
 		if(connection == null)
 			return data;
 		
 		try 
 		{
 			data = connection.isValid(timeout);
 		} 
 		catch (SQLException e) 
 		{
 			Log.log("", e);
 		}
 		
 		return data;
 	}
 	
 	public ResultSet getData()
 	{
 		results = null;
 		if(this.Connect() == false)
 		{
 			Log.log("ERROR: unable to get data from server");
 			LocationMapper.Exit(4);
 		}
 		
 		try
 		{			
 			Statement stmt = connection.createStatement();
 			stmt.setFetchSize(fetchSize);
 			Log.log("Querying Server: " + statement);
 			results = stmt.executeQuery(statement);
 		}
 		catch (Exception e)
 		{
 			Log.log("ERROR: Query Failed: " + e.getMessage());
 			return null;
 		}
 		
 		
 		return results;
 	}
 
 	
 
 	
 	public boolean Connect()
 	{
 
 		if(isConnected())
 			return true;
 		
 		try
 		{
 			Class.forName("org.postgresql.Driver");
 		}
 		catch (ClassNotFoundException cnfe)
 		{
 			Log.log("ERROR: Could not find the JDBC driver!");
 			return false;
 		}
 
 		String connectionString = "jdbc:postgresql://" + address +":" + port + "/" + tableName;
 		Log.log("Connecting to " + connectionString);
 		try 
 		{
 			connection = DriverManager.getConnection(connectionString, userName, password); 
 			Log.log("Connection is full of success!");
 		} 
 		catch (SQLException e) 
 		{
 			Log.log("ERROR: Could not connect: " + e.getMessage());
 			return false;
 		}
 		
 		if (connection == null) //this should never happen
 		{
 			Log.log("ERROR: Could not connect: LocationMapper.getDataFromServer.sqlConnection == null...hmmmm this should never happen");
 			return false;
 		} 
 		
 		
 		
 		
 		return isConnected();
 	}
 	
 }
