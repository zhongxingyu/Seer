 package main;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 /**
  * This is where the user can query the database
  * for users.
  * 
 * @author George Coomber and minor bug fix by Michael Feist
  */
 
 public class UserSearchCallback extends PageView
 {
 	public UserSearchCallback() {}
 	
 	// Prompts the user for input for menu choices
 	public void view()
 	{	
 		boolean run = true;
 		
 		while(run)
 		{
 			// User can search for other users by email or name
 			System.out.println("\nUser Search");
 			System.out.println("e: search by email");
 			System.out.println("n: search by name");
 			System.out.println("q: to exit");
 			
 			String command = Menu.getKeyBoard();
 			
 			if (command.length() == 0)
 			{
 				System.out.println("Error: No command given.");
 			} else {
 				switch (command.toCharArray()[0])
 				{
 					case 'e':
 						rows = searchEmail();
 						break;
 					
 					case 'n':
 						rows = searchName();
 						pageView(5);
 						break;
 					
 					case 'q':
 						run = false;
 						break;
 						
 					default:
 						System.out.println("Error: Unknown command.");
 						break;
 				}
 			}
 		}
 	}
 	
 	// Query database using input email.
 	public ArrayList<DatabaseRow> searchEmail()
 	{
 		ArrayList<DatabaseRow> rows = new ArrayList<DatabaseRow>();
 		
 		System.out.println("Enter Email Address");
 		String keyword = Menu.getKeyBoard();
 		
 		// Get connection to database
 		Connection m_con = Database.getInstance().getConnection();
 
 		// Check connection
 		if (m_con == null)
 		{
 			System.out.println("Unable to Connect to Server");
 			return rows;
 		}
 
 		// Create Statement
 		Statement stmt;
 
 		try
 		{
 			stmt = m_con.createStatement();
 			
 			// Query the database and look for emails matching the keyword
 			String query = "Select u.email, u.name, u.pass, u.last_login, COUNT(distinct a.aid), round(AVG(r.rating), 2)"
 					+ " From (users u LEFT OUTER JOIN ads a ON u.email = a.poster) LEFT OUTER JOIN reviews r ON r.reviewee = u.email" 
 					+ " WHERE trim(email) LIKE '" + keyword.trim() + "'"
 					+ " GROUP BY u.email, u.name, u.pass, u.last_login";
 			
 			ResultSet rs = stmt.executeQuery(query);
 			
 			while (rs.next())
 			{
 				UserData row = new UserData(
 						rs.getString(1).trim(),
 						rs.getString(2).trim(),
 						rs.getString(3),
 						rs.getString(4),
 						rs.getString(5),
 						rs.getString(6));
 
 				rows.add(row);
 			}
 			
 			rs.close();
 			stmt.close();
 			
 			// Print out the columns returned by the query (if any)
 			if(rows.size() > 0)
 			{
 				String format = String.format("%1$21s %2$21s %3$10s %4$10s",
 						"Email", "Name", "Ad_Count", "AVG_Rating");
 				
 				System.out.println(format);
 				System.out.println(rows.get(0).toString());
 			}
 			else {
 				System.out.println("No users exist matching that email");
 			}
 			
 		} catch (SQLException e)
 		{
 			System.err.println("SQLException: " + e.getMessage());
 		}
 
 		return rows;
 	}
 	
 	// Query database using input name.
 	public ArrayList<DatabaseRow> searchName()
 	{
 		ArrayList<DatabaseRow> rows = new ArrayList<DatabaseRow>();
 		
 		System.out.println("Enter Name");
 		String keyword = Menu.getKeyBoard();
 		
 		// Get connection to database
 		Connection m_con = Database.getInstance().getConnection();
 
 		// Check connection
 		if (m_con == null)
 		{
 			System.out.println("Unable to Connect to Server");
 			return rows;
 		}
 
 		// Create Statement
 		Statement stmt;
 
 		try
 		{
 			stmt = m_con.createStatement();
 			
 			// Search for users with name like the keyword (case insensitive)
 			String query = "Select u.email, u.name, u.pass, u.last_login, COUNT(distinct a.aid), round(AVG(r.rating), 2)"
 					+ " From (users u LEFT OUTER JOIN ads a ON u.email = a.poster) LEFT OUTER JOIN reviews r ON r.reviewee = u.email" 
 					+ " WHERE UPPER(trim(name)) LIKE '%" + keyword.toUpperCase().trim() + "%'"
 					+ " GROUP BY u.email, u.name, u.pass, u.last_login";
 			
 			ResultSet rs = stmt.executeQuery(query);
 			
 			while (rs.next())
 			{
 				UserData row = new UserData(
 						rs.getString(1).trim(),
 						rs.getString(2).trim());
 
 				rows.add(row);
 			}
 			
 			rs.close();
 			stmt.close();
 			
 		} catch (SQLException e)
 		{
 			System.err.println("SQLException: " + e.getMessage());
 		}
 
 		return rows;
 	}
 	
 	// The obtain column data for the row selected by the user.
 	public void getRowInfo(String id)
 	{
 		
 		// Get connection to database
 		Connection m_con = Database.getInstance().getConnection();
 
 		// Check connection
 		if (m_con == null)
 		{
 			System.out.println("Unable to Connect to Server");
 			return;
 		}
 
 		// Create Statement
 		Statement stmt;
 
 		try
 		{
 			stmt = m_con.createStatement();
 			
 			// query the database for a user with email equal to id
 			String query = "Select u.email, u.name, u.pass, u.last_login, COUNT(distinct a.aid), round(AVG(r.rating), 2)"
 					+ " From (users u LEFT OUTER JOIN ads a ON u.email = a.poster) LEFT OUTER JOIN reviews r ON r.reviewee = u.email" 
 					+ " WHERE trim(email) LIKE '" + id + "'"
 					+ " GROUP BY u.email, u.name, u.pass, u.last_login";
 
 			ResultSet rs = stmt.executeQuery(query);
 
 			// Print results to the screen
 			if (rs.next())
 			{
 				System.out.println("Email: " + rs.getString(1));
 				System.out.println("Name: " + rs.getString(2));
 				System.out.println("Pass: " + rs.getString(3));
 				System.out.println("Last_Login: " + rs.getString(4));
 				System.out.println("Ad_Count " + rs.getString(5));
 				System.out.println("Avg_Rating: " + rs.getString(6));
 				
 			} else
 			{
 				System.out.println("ERROR: Could not find user.");
 			}
 
 			rs.close();
 			stmt.close();
 			
 			boolean run = true;
 			
 			while(run)
 			{
 				// The user can list review texts or write a review.
 				System.out.println("\nOptions");
 				System.out.println("l: list review texts");
 				System.out.println("w: write a review for the user");
 				System.out.println("q: to exit");
 				
 				String command = Menu.getKeyBoard();
 				
 				if (command.length() == 0)
 				{
 					System.out.println("Error: No command given.");
 				} else
 				{
 					switch (command.toCharArray()[0])
 					{
 						case 'l':
 							showReviewTexts(id);
 							break;
 						
 						case 'w':
 							writeReview(id);
 							break;
 						
 						case 'q':
 							run = false;
 							break;
 							
 						default:
 							System.out.println("Error: Unknown command.");
 							break;
 					}
 				}
 			}
 			
 		} catch (SQLException e)
 		{
 			System.err.println("SQLException: " + e.getMessage());
 		}
 	}
 	
 	// Query the Database for the texts from all the reviews reviewing the user with id
 	public void showReviewTexts(String id) {
 		ArrayList<String> reviewTexts = new ArrayList<String>();
 		String text;
 		
 		// Get connection to database
 		Connection m_con = Database.getInstance().getConnection();
 
 		// Check connection
 		if (m_con == null)
 		{
 			System.out.println("Unable to Connect to Server");
 			return;
 		}
 
 		// Create Statement
 		Statement stmt;
 
 		try
 		{
 			stmt = m_con.createStatement();
 			
 			// Search for users with name like the keyword (case insensitive)
 			String query = "Select text"
 					+ " From reviews" 
 					+ " WHERE UPPER(trim(reviewee)) LIKE '" + id.toUpperCase().trim() + "'";
 			
 			ResultSet rs = stmt.executeQuery(query);
 			
 			while (rs.next())
 			{
 				text = rs.getString(1).trim();
 				reviewTexts.add(text);
 			}
 			
 			rs.close();
 			stmt.close();
 			
 		} catch (SQLException e)
 		{
 			System.err.println("SQLException: " + e.getMessage());
 		}
 
 		if( reviewTexts.size() > 0 )
 		{
 			for( String rText : reviewTexts)
 			{
 				System.out.println(rText);
 			}
 		}
 		else {
 			System.out.println("User currently has no reviews");
 		}
 		
 		System.out.println("\nPress enter to continue");
 		Menu.getKeyBoard();
 	}
 	
 	// Allows the user to write a review for the user with email id
 	public void writeReview(String id) {
 		// Create a new rno for the review
 		String rno = createNewRno();
 		int rating = -1;
 		String reviewText = "";
 		
 		// Get reviewer email
 		User user = User.getInstance();
 		String email = user.getEmail();
 		
 		// Boolean to check if the  user has entered an int for rating
 		boolean isInt = false;
 		// Boolean to check if the  rating is between 1 and 5
 		boolean isValid = false;
 		
 		System.out.println("New Review:");
 		
 		// Prompt the user for an int rating between 1 and 5
 		while (isInt == false || isValid == false)
 		{
 			isInt = false;
 			isValid = false;
 			System.out.println("Enter a rating between 1 and 5:");
 			
 			String input = Menu.getKeyBoard();
 			
 			// Check if the input is an integer
 			try
 			{
 				rating = Integer.parseInt(input);
 			} catch (NumberFormatException e)
 			{
 				// Print error message to user
 				System.out.println("Invalid input: Rating must be an interger value");
 				System.out.println("Please try again");
 				continue;
 			}
 			
 			isInt = true;
 			
 			// If the input is an int, check if between 1 and 5
 			if( rating >= 1 && rating <= 5 )
 			{
 				isValid = true;
 			}
 			
 			// Print error messages if the input is not in bounds
 			if( !isValid )
 			{
 				System.out.println("Invalid input: Rating must be between 1 and 5 inclusive");
 				System.out.println("Please try again");
 			}
 		}
 		
 		// Prompt the user for review text.
 		System.out.println("Please enter review text:");
 		String inputText = Menu.getKeyBoard();
 		reviewText = inputText.trim();
 		
 		// Get connection to database
 		Connection m_con = Database.getInstance().getConnection();
 
 		// Check connection
 		if (m_con == null) {
 			System.out.println("Unable to Connect to Server");
 			return;
 		}
 
 		// Create Statement
 		Statement stmt;
 
 		try {
 			stmt = m_con.createStatement();
 			
 			// Write the review to the database in the reviews table
 			String query = "INSERT INTO reviews VALUES ('" + rno + "','"
 					+ String.valueOf(rating) + "','" + reviewText + "','"
 					+ email + "','" + id + "', sysdate)";
 			
 			stmt.executeUpdate(query);
 			stmt.close();
 
 		} catch (SQLException e) {
 			System.err.println("SQLException: " + e.getMessage());
 		}
 		
 	}
 	
 	// Creates a new review number for the newly written review
 	public String createNewRno() {
 		String newRno = "1";
 		int intRno = 0;
 		
 		// Get connection to database
 		Connection m_con = Database.getInstance().getConnection();
 
 		// Check connection
 		if (m_con == null)
 		{
 			System.out.println("Unable to Connect to Server");
 			return "";
 		}
 
 		// Create Statement
 		Statement stmt;
 
 		try
 		{
 			stmt = m_con.createStatement();
 			
 			// Search for users with name like the keyword (case insensitive)
 			String query = "Select MAX(rno)"
 					+ " From reviews";
 			
 			ResultSet rs = stmt.executeQuery(query);
 			
 			// If the result set is not empty (there is at least one review)
 			// set the new rno to he max rno plus 1
 			if (rs.next())
 			{
 				intRno = Integer.parseInt(rs.getString(1).trim()) + 1;
 				newRno = String.valueOf(intRno);
 			}
 			
 			rs.close();
 			stmt.close();
 			
 		} catch (SQLException e)
 		{
 			System.err.println("SQLException: " + e.getMessage());
 		}
 		
 		return newRno;
 	}
 	
 	@Override
 	public int callback() {
 		view();
 		return Callback.OK;
 	}
 }
