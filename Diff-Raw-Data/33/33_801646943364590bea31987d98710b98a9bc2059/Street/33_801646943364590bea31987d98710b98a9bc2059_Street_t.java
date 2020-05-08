 package routefiend;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 
 public class Street 
 {
 	private static HashMap<Integer, Street> _streets = new HashMap<Integer, Street>();
 	
 	public static void addStreet(Street street)
 	{
 		_streets.put(street.id(), street);
 	}
 	
 	public static Street getStreetForId(Integer id)
 	{
 		if(id == null)
 			return null;
 		else
 			return _streets.get(id);
 	}
 	
 	// //hostname/Database
 	public static void readStreetsFrom(String address, String username, String password)
 	{
 		Connection conn = null;
 		Statement statement;
 		ResultSet result;
 		
 		try
 		{
			System.out.println("Reading Streets from " + address + " with user " + username + " and password " + password );
 	        Class.forName("com.mysql.jdbc.Driver").newInstance();
 		    conn = DriverManager.getConnection("jdbc:mysql:" + address + "?" + "user=" + username + "&password=" + password);
 		    statement = conn.createStatement();
 		    result = statement.executeQuery("SELECT * FROM Streets");
 		    
 		    while(result.next())
 			{
 				Street street = new Street(result.getInt("id"), result.getString("Name"));
 				Street.addStreet(street);
 			}
 		} catch (SQLException ex) 
 		{
 		    System.out.println("SQLException: " + ex.getMessage());
 		    System.out.println("SQLState: " + ex.getSQLState());
 		    System.out.println("VendorError: " + ex.getErrorCode());
 		} catch (Exception ex)
 		{
			System.out.println("Other Exception: " + ex.getMessage());
 		}
 	}
 	
 	private int _id;
 	private String _name;
 	
 	public int id()
 	{
 		return _id;
 	}
 	
 	public String name()
 	{
 		return _name;
 	}
 	
 	public void setName(String value)
 	{
 		_name = value;
 	}
 	
 	public Street(int id, String name)
 	{
 		_id = id;
 		_name = name;
 	}
 }
