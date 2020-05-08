 package routefiend;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 
 public class Intersection implements Comparable<Intersection>
 {
 	private static HashMap<Integer, Intersection> _intersections = new HashMap<Integer, Intersection>();
 	
 	public static void addIntersection(Intersection intersect)
 	{
 		_intersections.put(intersect.id(), intersect);
 	}
 	
 	public static Intersection getIntersectionForId(Integer id)
 	{
 		if(id == null)
 			return null;
 		else
 			return _intersections.get(id);
 	}
 	
 	public static void readIntersectionFrom(String address, String username, String password)
 	{
 		Connection conn = null;
 		Statement statement;
 		ResultSet result;
 		
 		try
 		{
			System.out.println("Reading Intersections from " + address + " with user " + username + " and password " + password );
 	        Class.forName("com.mysql.jdbc.Driver").newInstance();
 		    conn = DriverManager.getConnection("jdbc:mysql:" + address + "?" + "user=" + username + "&password=" + password);
 		    statement = conn.createStatement();
 		    result = statement.executeQuery("SELECT * FROM Intersections");
 		    
 			while(result.next())
 			{
 				Intersection intersect = new Intersection(result.getInt("id"));
 				intersect.setLat(result.getDouble("Lat"));
 				intersect.setLon(result.getDouble("Lon"));
 				intersect.setStreet1(Street.getStreetForId(result.getInt("Street1")));
 				intersect.setStreet2(Street.getStreetForId(result.getInt("Street2")));
 				intersect.setStreet3(Street.getStreetForId(result.getInt("Street3")));
 				intersect.setStreet4(Street.getStreetForId(result.getInt("Street4")));
 				intersect.setName(result.getString("Name"));
 				
 				Intersection.addIntersection(intersect);
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
 	private double _lat;
 	private double _lon;
 	private Street _street1;
 	private Street _street2;
 	private Street _street3;
 	private Street _street4;
 	private String _name;
 	
 	public int id()
 	{
 		return _id;
 	}
 	
 	public double lat()
 	{
 		return _lat;
 	}
 	
 	public void setLat(double value)
 	{
 		_lat = value;
 	}
 	
 	public double lon()
 	{
 		return _lon;
 	}
 	
 	public void setLon(double value)
 	{
 		_lon = value;
 	}
 	
 	public Street street1()
 	{
 		return _street1;
 	}
 	
 	public void setStreet1(Street value)
 	{
 		_street1 = value;
 	}
 	
 	public Street street2()
 	{
 		return _street2;
 	}
 	
 	public void setStreet2(Street value)
 	{
 		_street2 = value;
 	}
 	
 	public Street street3()
 	{
 		return _street3;
 	}
 	
 	public void setStreet3(Street value)
 	{
 		_street3 = value;
 	}
 	
 	public Street street4()
 	{
 		return _street4;
 	}
 	
 	public void setStreet4(Street value)
 	{
 		_street4 = value;
 	}
 	
 	public String name()
 	{
 		return _name;
 	}
 	
 	public void setName(String value)
 	{
 		_name = value;
 	}
 	
 	public Intersection(int id)
 	{
 		_id = id;
 	}
 	
 	public Intersection(int id, Street street1, Street street2, Street street3, Street street4, String name)
 	{
 		_id = id;
 		_street1 = street1;
 		_street2 = street2;
 		_street3 = street3;
 		_street4 = street4;
 		_name = name;
 	}
 	
     public int compareTo(Intersection i)
     {
         return this.id() - i.id();
     }
 }
