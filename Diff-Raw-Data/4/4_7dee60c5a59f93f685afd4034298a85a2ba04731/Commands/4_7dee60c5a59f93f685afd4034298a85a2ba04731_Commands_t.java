 package bgpp2011;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 /*
  * This class needs alot of explanation.
  * Basically this is class designed to keep every bit of SQL-syntax-related code separated from the class that 
  * delivers the data from the database to the controlling part of the system. This is done in a somewhat 
  * cumbersome manner but the idea is that you should be able to change database fairly easy if you edit this class
  * and the CONNECTION part of the 'data-delivering-system' (here called Nexus). This class need not be instantiated anywhere
  * as it because of its 'translate'-like nature can be replaced as long as the parameters are the same.
  * @authors mainly msta & tbrj.
  */
 
 
 public class Commands {
 		/*
 		 * The first methods takes in objects of different kinds and creates a SQL-command that creates the post in the 
 		 * designated table. These commands are returned as strings to be used further in the Nexus.
 		 */
 	    public static String createVehicle(Vehicle v)
 	    {
 	       return "INSERT INTO Vehicle VALUES (null, \"" + v.getMake() + 
 	        "\", \"" + v.getModel() + "\"," + v.getYear() + ","
 	        +  v.getType().getId() + ");";
 	     
 	    }         
 	    public static String createVehicleType(VehicleType t)
 	    {
 	            return "INSERT INTO VehicleType VALUES (null, " + t.getPrice()
 	            		+ ",\"" + t.getName() + "\");";
 	    }
 	    public static String createCustomer(Customer c)
 	    {
 	       return "INSERT INTO Customer VALUES (null, \"" + c.getName() 
 	                + "\"," + c.getNumber() + ",\""  + c.getAddress() + "\", \""
 	                + c.getBankAccount() + "\");";   
 	    
 	    }
 	    public static String createReservation(Reservation r)
 	    {
 	          return "INSERT INTO Reservation VALUES(null,"
 	                    + r.getCustomer().getId() + "," + r.getVehicle().getId()
 	                    + ",\"" + r.getStartdate() + "\",\"" + r.getEnddate()
 	                            + "\");";
 	    }	    
 	    /*
 	     * The following methods are SQL-command for retrieving entire tables as ResultSets, usable in the Nexus.
 	     */
 	    
 	    public static String getReservations()
 	    {
 	          return "SELECT * FROM Reservation;";
 	    }
 	    public static String getVehicles()
 	    {
 	        return "SELECT * FROM Vehicle;";
 	    }
 	    public static String getCustomers()
 	    {
 	        return "SELECT * FROM Customer;";
 	    }
 	    public static String getTypes()
 	    {
 	        return "SELECT * from VehicleType;";
 	    }
 	    /*
 	     * These methods should be used when closing the program. They are intended to update the database with the newest content
 	     * created while the program was open. It should be noticed that any type of overwriting is allowed; if you have an object
 	     * with a corresponding id you will delete the former post in the table.
 	     */
 	    public static String updateVehicle(Vehicle v)
 	    {
 	        return "UPDATE Vehicle SET make = \"" + v.getMake() + "\", "
 	                + "model = \"" + v.getModel() + "\", typeID = " + 
 	                v.getType().getId() + ", year = " + v.getYear() + 
 	                " where id = " + v.getId();
 	    }
 	    public static String updateCustomer(Customer c)
 	    {
 	        return "UPDATE Customer SET name = \"" + c.getName() + "\", "
 	                + "address = \"" + c.getAddress() + "\", phone = " + 
 	                c.getNumber() + " where id = " + c.getId();
 	    }
 	    public static String updateReservation(Reservation r)
 	    {
	        return "UPDATE Reservation SET customerID = " + r.getCustomer().getId() +
	                ", vehicleID = " + r.getVehicle().getId() + ", startdate = \""
 	                + r.getStartdate() + "\", enddate = \"" + r.getEnddate()
 	                + "\" where id = " + r.getId();
 	    }
 	    public static String updateVehicleType(VehicleType t)
 	    {   
 	              return "UPDATE VehicleType SET name = \"" + t.getName() +
 	              "\", price = " + t.getPrice() + " where id = " + t.getId();
 	    }
 	    /*
 	     * These methods are responsible for taking in ResultSets from the database and returning them as HashMaps, usable
 	     * in the Nexus and especially for further use in the controller. This stage of the 'translation' concludes the Nexus' 
 	     * usability when opening the program.
 	     */
 	    public static HashMap<Integer, Reservation> makeMapReservation(ResultSet r, HashMap<Integer, Customer> cmap, HashMap<Integer, Vehicle> vmap)
 	    {
 	         HashMap<Integer, Reservation> returnmap = new HashMap<Integer, Reservation>();
 	        try{     
 	        
 	          while(r.next())
 	          {
 	            int cid = r.getInt("Customerid");
 	            int vid = r.getInt("VehicleId");
 	            Date startDate = new Date(r.getString("startdate"));
 	            Date endDate = new Date(r.getString("enddate"));
 	            int id = r.getInt("id");
 	            Reservation tmp = new Reservation(id,cmap.get(cid), 
 	                              vmap.get(vid), startDate, endDate);
 	                                              
 	            returnmap.put(id,tmp);
 	        
 	          } 
 	         
 	            return returnmap;  
 	          }
 	         
 	          catch(SQLException e)
 	          {
 	            System.out.println("Non valid resultset at reservation creation :" + e);
 	            return null;
 	          }
 	    }
 	    public static HashMap<Integer, Customer> makeMapCustomer(ResultSet r)
 	    {
 	         HashMap<Integer, Customer> returnmap = new HashMap<Integer, Customer>();
 	        try{     
 	        
 	          while(r.next())
 	          {
 	        	 int id = r.getInt("id");
 	            Customer tmp = new Customer(id,r.getString("name"),
 	                           r.getInt("phonenumber"), r.getString("address"),
 	                           r.getString("bankaccount"));
 	                                              
 	            returnmap.put(id,tmp);
 	            
 	          } 
 	           
 	            return returnmap;  
 	          }
 	         
 	          catch(SQLException exn)
 	          {
 	            System.out.println("Non valid resultset" + exn);
 	            return null;
 	          }
 	    }
 	    public static HashMap<Integer, Vehicle> makeMapVehicles(ResultSet r, HashMap<Integer, VehicleType> types)
 	    {
 	         HashMap<Integer, Vehicle> returnmap = new HashMap<Integer, Vehicle>();
 	          try{     
 	        	 
 	          while(r.next())
 	          {
 	        	 
 	        	int vid = r.getInt("id");
 	        	
 	            int id = r.getInt("typeId");
 	         
 	            Vehicle tmp = new Vehicle(vid,r.getString("make"),
 	                          r.getString("model"), r.getInt("year"),types.get(id));
 	           
 	                                              
 	            returnmap.put(vid, tmp);
 	           
 	          } 
 	          
 	            return returnmap;  
 	          }
 	         
 	          catch(SQLException exn)
 	          {
 	            System.out.println("Non valid resultset at vehicle map creation" + exn);
 	            return returnmap;
 	          }
 	    }
 	    public static HashMap<Integer, VehicleType> makeMapTypes(ResultSet r)
 	    {
 	    
 		        HashMap<Integer, VehicleType> vtmap = new HashMap<Integer, VehicleType>();
 		        try{     
 		        
 		          while(r.next())
 		          {
 		        	int id = r.getInt("id");
 		            VehicleType v = new VehicleType(id, r.getString("name") ,r.getDouble("price"));
 		                                              
 		            vtmap.put(id,v);
 		            
 		          }
 		            return vtmap;  
 		          }
 		         
 		          catch(SQLException exn)
 		          {
 		            System.out.println("Non valid resultset at vehicletype creation: " + exn);
 		            return null;
 		          }     
 	        
 	    }
   	    /*
   	     * This method 
   	     */
 	    
 	    public static int getDbID(ResultSet r)
   	    {
   	    	try {
   	    		if(r == null)
   	    			System.out.println("The resultset is not valid at getDBID request.");
   	    		r.next();
   	    		
   	    		return r.getInt(1);
   	    	}
   	    	catch(SQLException exn) {
   	  
   	    		System.out.println("The system could not fetch the ID for the requested object:" + exn);
   	    		return -1;
   	    	}
   	    }
 	    
 	    
 }
