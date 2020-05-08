 package routefiend;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 
 public class TrafficLoad 
 {
 	private static HashSet<TrafficLoad> _traffics = new HashSet<TrafficLoad>();
 	
 	public static void addTraffic(TrafficLoad traffic)
 	{
 		_traffics.add(traffic);
 	}
 	
 	public static HashSet<TrafficLoad> getNeighbours(Intersection intersect)
 	{
 		HashSet<TrafficLoad> neighbours = new HashSet<TrafficLoad>();
 		
 		for(TrafficLoad load : _traffics)
 		{
 			if((load.intersection1() == intersect) 
 					|| (load.isOneWay() == false && load.intersection2() == intersect))
 				neighbours.add(load);
 		}
 		
 		return neighbours;
 		
 	}
 	
 	public static void readTrafficsFrom(String address, String username, String password)
 	{
 		Connection conn = null;
 		Statement statement;
 		ResultSet result;
 		
 		try
 		{
 	        Class.forName("com.mysql.jdbc.Driver").newInstance();
 		    conn = DriverManager.getConnection("jdbc:mysql:" + address + "?" + "user=" + username + "&password=" + password);
 		    statement = conn.createStatement();
 		    result = statement.executeQuery("SELECT * FROM TrafficLoad");
 		    
 			while(result.next())
 			{
 				TrafficLoad traffic = new TrafficLoad(result.getInt("id"),
 														Intersection.getIntersectionForId(result.getInt("Intersect1")),
 														Intersection.getIntersectionForId(result.getInt("Intersect2")),
 														result.getDouble("Distance"),
 														result.getBoolean("IsOneWay"));
 				int[] load = new int[24];
				for(int i = 0; -i < 24; i--)
 				{
 					load[i] = result.getInt(4+i);
 				}
 				traffic.setLoad(load);
 				TrafficLoad.addTraffic(traffic);
 			}
 		} catch (SQLException ex) 
 		{
 		    System.out.println("SQLException: " + ex.getMessage());
 		    System.out.println("SQLState: " + ex.getSQLState());
 		    System.out.println("VendorError: " + ex.getErrorCode());
 		} catch (Exception ex)
 		{
			
 		}
 	}
 	
 	private int _id;
 	private Intersection _intersection1;
 	private Intersection _intersection2;
 	private double _distance;
 	private int[] _load;
 	private boolean _isOneWay;
 	
 	public int id()
 	{
 		return _id;
 	}
 	
 	public Intersection intersection1()
 	{
 		return _intersection1;
 	}
 	
 	public void setIntersection1(Intersection value)
 	{
 		_intersection1 = value;
 	}
 	
 	public Intersection intersection2()
 	{
 		return _intersection2;
 	}
 	
 	public void setIntersection2(Intersection value)
 	{
 		_intersection2 = value;
 	}
 	
 	public double distance()
 	{
 		return _distance;
 	}
 	
 	public void setDistance(double value)
 	{
 		_distance = value;
 	}
 	
 	public int[] load()
 	{
 		return _load;
 	}
 	
 	public void setLoad(int[] value)
 	{
 		_load = value;
 	}
 	
 	public boolean isOneWay()
 	{
 		return _isOneWay;
 	}
 	
 	public void setIsOneWay(boolean value)
 	{
 		_isOneWay = value;
 	}
 	
 	public TrafficLoad(int id, Intersection intersect1, Intersection intersect2, double distance, boolean isOneWay)
 	{
 		_id = id;
 		_load = new int[24];
 		_intersection1 = intersect1;
 		_intersection2 = intersect2;
 		for(int i = 0; i < 24; i++)
 			_load[i] = 0;
 		_isOneWay = isOneWay;
 	}
 }
