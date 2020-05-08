 package vehicleShepard;
 
 /*
  * This class is controlling the methods of our users
  * This implies: 
  * 		creating a new user
  * 		getting a user by his or her ID
  * 		getting the number of customers
  * 		getting a list of customers
  * 		getting a list of customers (from a search)
  */
 
 import java.sql.*;
 
 public class UserDB 
 {
 	/**
 	 * This method creates a new user, 
 	 * 		either customer or mechanic.
 	 * We use a boolean to check this
 	 * 		and all the info we get from an array
 	 * It returns the userID, which can be used for JUnit testing
 	 * @param customer 
 	 * @param info
 	 * @return userID Returns the userID, which can be used for JUnit testing
 	 */
	public int newUser(Boolean customer, Object[] infoCust, Object[] infoMech)
 	{
 		int userID = getNumberOfUsers(customer) + 1;
 		
 		//We get the connection object from the Controller class
 		Connection conn = Controller.getConnection();
 		 
 		Statement s;
 		try 
 		{
 			//This is a class from our driver
 			s = conn.createStatement();
 			
 			/*
			 * Here we want to insert our new user
 			 * 		into the right table
 			 * We do this by checking if we are making a new
 			 * 		customer or mechanic
 			 */
 			
 			try 
 			{
 				if(customer = true)
 				{
					s.executeUpdate("INSERT INTO Customer (`userID`, `phone`, `phoneCode`, `address`, `country`, `firstName`, `lastName`, `licenceNumber`, `licenceExpDate`) VALUES ('" + userID + "', '" + infoCust[0] + "', '" + infoCust[1] + "', '" + infoCust[2] + "', '" + infoCust[3] + "', '" + infoCust[4] + "', '" + infoCust[5] + "', '" + infoCust[6] + "', '" + infoCust[7] + "')");
 				}
 				else
 				{
					s.executeUpdate("INSERT INTO Mechanic (`userID`, `phone`, `phoneCode`, `address`, `country`, `firmName`) VALUES ('" + userID + "', '" + infoMech[0] + "', '" + infoMech[1] + "', '" + infoMech[2] + "', '" + infoMech[3] + "', '" + infoMech[4] + "')");
 				}
 				
 			} 
 			
 			catch (SQLException e) 
 			{
 				e.printStackTrace();
 			}
 			
 			finally
 			{
 				s.close();
 			}
 		} 
 		
 		catch (SQLException e1) 
 		{
 			e1.printStackTrace();
 		}
 		
 		return userID;
 		
 	}
 	
 	/**
 	 * This method gets a user by their ID
 	 * We use boolean again to see if its a customer or mechanic
 	 * UserID talks for itself
 	 * @param customer
 	 * @param userID
 	 * @return user An array of the desired user
 	 */
 	public Object[] getUserByID(Boolean customer, int userID)
 	{
 		Object[] user;
 		
 		//We determine the length of the array depending on the user-type
 		if(customer = true)
 		{
 			user = new Object[9]; 
 		}
 		else
 		{
 			user = new Object[6]; 
 		}
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		Statement s;
 		
 		/*
 		 * Here we want to select our user 
 		 * 		from the right table
 		 * We do this by checking if we are taking a
 		 * 		customer or mechanic
 		 */
 		
 		try 
 		{
 			s = conn.createStatement();
 			
 			if(customer = true)
 			{
 				s.executeQuery("SELECT * FROM Customer WHERE userID=" + userID);
 			}
 			else
 			{
 				s.executeQuery("SELECT * FROM Mechanic WHERE userID=" + userID);
 			}
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		put a result in our array
 			 */
 			
 			while(rs.next())
 			{
 				if(customer = true)
 				{
 					user[0] = rs.getInt("userID");
 					user[1] = rs.getInt("phone");
 					user[2] = rs.getInt("phoneCode");
 					user[3] = rs.getString("address");
 					user[4] = rs.getString("country");
 					user[5] = rs.getString("firstName");
 					user[6] = rs.getString("lastName");
 					user[7] = rs.getString("licenceNumber");
 					user[8] = rs.getDate("licenceExpDate");
 				}
 				else
 				{
 					user[0] = rs.getInt("userID");
 					user[1] = rs.getInt("phone");
 					user[2] = rs.getInt("phoneCode");
 					user[3] = rs.getString("address");
 					user[4] = rs.getString("country");
 					user[5] = rs.getString("firmName");
 				}
 			}
 			
 			s.close();
 		} 
 		
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		return user;
 	}
 	
 	/**
 	 * This method gets the number of users in a table
 	 * Again we need to know if it is the number of
 	 * 		customers or mechanics
 	 * @param customer
 	 * @return number
 	 */
 	public int getNumberOfUsers(Boolean customer) //TODO Set this to private after testing
 	{	
 		int count = 0;
 
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			
 			/*
 			 * Here we want to select our users
 			 * 		from the right table
 			 * We do this by checking if we are taking a
 			 * 		customer or mechanic
 			 */
 			
 			if(customer)
 			{
 				s.executeQuery("SELECT userID FROM Customer");
 			}
 			else
 			{
 				s.executeQuery("SELECT userID FROM Mechanic");
 			}
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		count +1 each time
 			 */
 			
 			while(rs.next())
 			{
 				count++;
 			}
 			
 			s.close();
 			System.out.println("count: " + count);
 		} 
 		catch (SQLException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return count;
 	}
 	
 	/**
 	 * This method gives us a list of our customers
 	 * Again we need to know if it is the list consists
 	 * 		of customers or mechanics
 	 * @param customer
 	 * @return userList
 	 */
 	public Object[][] getList(Boolean customer)
 	{
 		int number = getNumberOfUsers(customer);
 		int count = 0;
 		
 		Object[][] userList;
 		
 		//We determine the length of the array depending on the user-type
 		if(customer = true)
 		{
 			userList = new Object[number][9]; 
 		}
 		else
 		{
 			userList = new Object[number][6]; 
 		}
 		
 		//We get the connection from the Controller class
 		Connection conn = Controller.getConnection();
 		
 		try 
 		{
 			Statement s = conn.createStatement();
 			
 			/*
 			 * Here we want to select our data
 			 * 		from the right table
 			 * We do this by checking if we are taking a
 			 * 		customer or mechanic
 			 */
 			
 			if(customer)
 			{
 				s.executeQuery("SELECT * FROM Customer");
 			}
 			else
 			{
 				s.executeQuery("SELECT * FROM Mechanic");
 			}
 			
 			ResultSet rs = s.getResultSet();
 			
 			/*
 			 * The result is put in a resultset rs
 			 * We then take each line of the resultset and 
 			 * 		put a result in our array
 			 */
 			
 			while(rs.next())
 			{
 				if(customer)
 				{
 					userList[count][0] = rs.getInt("userID");
 					userList[count][1] = rs.getInt("phone");
 					userList[count][2] = rs.getInt("phoneCode");
 					userList[count][3] = rs.getString("address");
 					userList[count][4] = rs.getString("country");
 					userList[count][5] = rs.getString("firstName");
 					userList[count][6] = rs.getString("lastName");
 					userList[count][7] = rs.getString("licenceNumber");
 					userList[count][8] = rs.getDate("licenceExpDate");
 				}
 				else
 				{
 					userList[count][0] = rs.getInt("userID");
 					userList[count][1] = rs.getInt("phone");
 					userList[count][2] = rs.getInt("phoneCode");
 					userList[count][3] = rs.getString("address");
 					userList[count][4] = rs.getString("country");
 					userList[count][5] = rs.getString("firmName");
 				}
 				
 				//Next row
 				count++;
 			}
 			
 			s.close();
 		} 
 		
 		catch (SQLException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return userList;
 	}
 	
 	/**
 	 * This method is used to get a list of users, that 
 	 * 		meet the requirements of a seacrh
 	 * @param customer
 	 * @param searchString
 	 * @return users
 	 */
 	public Object[][] getUsers(Boolean customer, String searchString)
 	{		
 		//We make it easier to analyze
 		String searchTerm = searchString.toLowerCase().trim();
 		
 		Object[][] userList = getList(customer);
 		
 		int number = getNumberOfUsers(customer);
 		
 		/*
 		 * We use our search method, by giving the needed parameters
 		 * 		and it returns an array
 		 */
 		Object[][] users = Search.stringSearch(searchTerm, userList, number, 9);
 		
 		return users;
 	}
 }
