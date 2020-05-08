 /*
  * MODIFIED BY JUSTIN KAHAL AND THOMAS DESMOND
  * SSID:860892022
  *
  * Template JAVA User Interface
  * =============================
  *
  * Database Management Systems
  * Department of Computer Science &amp; Engineering
  * University of California - Riverside
  *
  * Target DBMS: 'Postgres'
  *
  */
 
 
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.io.File;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 
 ///////////////////////////////////////////////////////
 ////////////////////////////////////////////////////
 /////////	ADD THIS IMPORT			////////////////
 import java.util.Scanner;
 
 /**
  * This class defines a simple embedded SQL utility class that is designed to
  * work with PostgreSQL JDBC drivers.
  *
  */
 public class EmbeddedSQL {
 
    
 	public static String currentUser;
 	public static boolean superUser = false;
 	// reference to physical database connection.
    private Connection _connection = null;
 
    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
                                 new InputStreamReader(System.in));
 
    /**
     * Creates a new instance of EmbeddedSQL
     *
     * @param hostname the MySQL or PostgreSQL server hostname
     * @param database the name of the database
     * @param username the user name used to login to the database
     * @param password the user login password
     * @throws java.sql.SQLException when failed to make a connection.
     */
    public EmbeddedSQL (String dbname, String dbport, String user, String passwd) throws SQLException {
 
       System.out.print("Connecting to database...");
       try{
          // constructs the connection URL
          String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
          System.out.println ("Connection URL: " + url + "\n");
 
          // obtain a physical connection
          this._connection = DriverManager.getConnection(url, user, passwd);
          System.out.println("Done");
       }catch (Exception e){
          System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
          System.out.println("Make sure you started postgres on this machine");
          System.exit(-1);
       }//end catch
    }//end EmbeddedSQL
 
    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate (String sql) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();
 
       // issues the update instruction
       stmt.executeUpdate (sql);
 
       // close the instruction
       stmt.close ();
    }//end execute update 
    
    int getVidId(String query)
    	{
 		try{
 			// creates a statement objectv
 			Statement stmt = this._connection.createStatement ();						
 													
 			// issues the query instruction
 			ResultSet rs = stmt.executeQuery(query);
 													
 			while(rs.next())
 			{
 				int vid_id = Integer.parseInt(rs.getString(1));
 				return vid_id;
 			}
 			return -1;
 		}catch(Exception e){
 			System.err.println (e.getMessage ());}					
 		return -1;
 	}//end getVidID
 
    public int executeOrder (String query, int price) throws SQLException
    {
 		// creates a statement objectv
 		Statement stmt = this._connection.createStatement ();
 
 		// issues the query instruction
 		ResultSet rs = stmt.executeQuery(query);
 
 		while(rs.next())
 		{
 			int balance = Integer.parseInt(rs.getString(1));
 			if(balance - price >= 0)
 			{
 				System.out.println("You would have negative balance if movie is ordered you cannot order movie");		
 				return -1;
 			}
 			else
 			{
 				String updatebal = "UPDATE users SET balance = balance - " + price + " WHERE user_id = '" + currentUser + "'";
 				stmt.executeUpdate(updatebal); //I changed this part might be problematic
 				String newbalance = "SELECT balance FROM users WHERE user_id='" + currentUser + "'";
 				System.out.print("New Balance if ordered: ");
 				executeStringQuery(newbalance, 1);
 				return 1;
 			}	
 		}
 		return -1;
 	}
    	
 	public void executeStringQuery (String query, int line) throws SQLException
 	{
 		// creates a statement objectv
 		Statement stmt = this._connection.createStatement ();
 
 		// issues the query instruction
 		ResultSet rs = stmt.executeQuery (query);
 
 		/*
 		** obtains the metadata object for the returned result set.  The metadata
 		** contains row and column info.
 		*/
 		System.out.println("--------------");
 		while(rs.next())
 		{
 			System.out.println(rs.getString(line));
 		}
 	}
 	
 	public int executeCheckTitle (String query) throws SQLException
 	{
 		try{
 				// creates a statement objectv
 				Statement stmt = this._connection.createStatement ();
 				System.out.println("Connection Success");
 
 				// issues the query instruction
 				ResultSet rs = stmt.executeQuery(query);
 				System.out.println("Query exectued success");
 																										
 				System.out.println("--------------");
 																									
 				while(rs.next())
 				{
 					System.out.print("Are you sure you would like to order (yes or no): ");
 					System.out.println(rs.getString(2) + " Price: " + rs.getString(3));
 					String input = in.readLine();
 					System.out.println();
 				}
 
 				return Integer.parseInt(rs.getString(3));
 			}catch(Exception e){
 				System.err.println (e.getMessage ());}
 		return -1;
 	}
 
 	public int executeLoginQuery (String query) throws SQLException
 	{
    		// creates a statement objectv
       Statement stmt = this._connection.createStatement ();
 
       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);
 
       /*
        ** obtains the metadata object for the returned result set.  The metadata
        ** contains row and column info.
        */
       ResultSetMetaData rsmd = rs.getMetaData ();
       int numCol = rsmd.getColumnCount ();
       rs.next();
 	  int resultset = Integer.parseInt(rs.getString(numCol));
 	  System.out.println(resultset);
 
 	  stmt.close();
 	  return resultset;
       // iterates through the result set and output them to standard out.
      /* boolean outputHeader = true;
       while (rs.next()){
 	 if(outputHeader){
 	    for(int i = 1; i <= numCol; i++){
 		System.out.print(rsmd.getColumnName(i) + "\t");
 	    }
 	    System.out.println();
 	    outputHeader = false;
 	 }
          for (int i=1; i<=numCol; ++i)
             System.out.print (rs.getString (i) + "\t");
          System.out.println ();
          ++rowCount;
       }//end while
       stmt.close ();
       return rowCount;
 	  */
    }
 
    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery (String query) throws SQLException {
       // creates a statement objectv
       Statement stmt = this._connection.createStatement ();
 
       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);
 
       /*
        ** obtains the metadata object for the returned result set.  The metadata
        ** contains row and column info.
        */
       ResultSetMetaData rsmd = rs.getMetaData ();
       int numCol = rsmd.getColumnCount ();
       int rowCount = 0;
 
       // iterates through the result set and output them to standard out.
       boolean outputHeader = true;
       while (rs.next()){
 	 if(outputHeader){
 	    for(int i = 1; i <= numCol; i++){
 		System.out.print(rsmd.getColumnName(i) + "\t");
 	    }
 	    System.out.println();
 	    outputHeader = false;
 	 }
          for (int i=1; i<=numCol; ++i)
             System.out.print (rs.getString (i) + "\t");
          System.out.println ();
          ++rowCount;
       }//end while
       stmt.close ();
       return rowCount;
    }//end executeQuery
 
    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup(){
       try{
          if (this._connection != null){
             this._connection.close ();
          }//end if
       }catch (SQLException e){
          // ignored.
       }//end try
    }//end cleanup
 
    /**
     * The main execution method
     *
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    public static void main (String[] args) {
       if (args.length != 4) {
          System.err.println (
             "Usage: " +
             "java [-classpath <classpath>] " +
             EmbeddedSQL.class.getName () +
             " <dbname> <port> <user> <passwd>");
          return;
       }//end if
       
       Greeting();
       EmbeddedSQL esql = null;
       try{
          // use postgres JDBC driver.
          Class.forName ("org.postgresql.Driver").newInstance ();
          // instantiate the EmbeddedSQL object and creates a physical
          // connection.
          String dbname = args[0];
          String dbport = args[1];
          String user = args[2];
          String passwd = args[3];
          esql = new EmbeddedSQL (dbname, dbport, user, passwd);
 
          boolean keepon = true;
          while(keepon) {
             // These are sample SQL statements
             System.out.println("WELCOME! Are you an existing user?");
             System.out.println("---------");
             System.out.println("0. Yes I am!");
             System.out.println("1. No, I need to register!");
             System.out.println("9. < EXIT (Stop the program)");
             
 
             switch (readChoice()){
                case 0: if(LogInQuery(esql) == 0 ) keepon = false; break;
                case 1: if(RegisterQuery(esql) == 0 ) keepon = false; break;
                case 9: return;
                default : System.out.println("Unrecognized choice!"); break;
             }//end switch
          }//end while 
          
 		//Will need to turn keepon to false after logging in 
 		
 		boolean homescreen = true;         
 		while(homescreen)
 		{
 			System.out.println("---------");
 			System.out.println("Welcome to MovieNet");
             System.out.println("---------");
             System.out.println("0. See list of movies");
             System.out.println("1. Order a movie");
 			System.out.println("2. Rate a movie NOT WORKING");
             System.out.println("3. Look at wall NOT WORKING");
 			System.out.println("4. Add to balance");
 			System.out.println("5. List favorite movie");
 			System.out.println("6. Add a favorite movie");
 
 			//these will only be visible if you are super user
 			if( superUser ){
 				System.out.println("\nSuper User Options:");
 				System.out.println("10. Register a new movie WORKING");
 				System.out.println("11. Delete an existing movie NOT WORKING");
 				System.out.println("12. Delete an existing user NOT WORKING");
 			}
 
 			/////////////////////////////////////////////////////////////CHANGES//////////
 			if( !superUser ) System.out.println("8. Login as SuperUser");
 			/////////////////////////////////////////////////////////////CHANGES//////////
 			System.out.println("9. < EXIT (Stop the program)");
 
 			switch (readChoice()){
 				case 0: ListMovies(esql); break;
 				case 1: OrderMovie(esql); break;
 				case 2: break;
 				case 3: break;
 				case 4: IncreaseBalance(esql); break;
 				case 5: ListFavs(esql); break;
 				case 6: InsertFav(esql); break;
 				/////////////////////////////////////////////////////////////CHANGES//////////
 				case 8: if( !superUser ) {LoginAsSuper(esql);}; break;
 				/////////////////////////////////////////////////////////////CHANGES//////////
 				
 				case 9: return;
 				case 10: if( superUser ) {RegisterMovie(esql);}; break;
 				case 11: break;
 				case 12: break;
 				default : System.out.println("Unrecognized choice!"); break;
 			}
 
 		
 		}
 		     
          
       }catch(Exception e) {
          System.err.println (e.getMessage ());
       }finally{
          // make sure to cleanup the created table and close the connection.
          try{
             if(esql != null) {
                System.out.print("Disconnecting from database...");
                esql.cleanup ();
                System.out.println("Done\n\nBye !");
             }//end if
          }catch (Exception e) {
             // ignored.
          }//end try
       }//end try
    }//end main
    
 
 
 	public static void ListMovies(EmbeddedSQL esql)
 	{
 		try{
 			String query = "SELECT title FROM Video";
			String output = esql.executeStringQuery(query, 1);
 
 		}catch(Exception e){
          System.err.println (e.getMessage ());
       }
 	}
 
 	public static void ListFavs(EmbeddedSQL esql)
 	{
 		try{
 				String query = "SELECT title FROM video, likes WHERE likes.user_id = '" + currentUser + 
 							"' AND likes.video_id = video.video_id";
 				esql.executeStringQuery(query, 1);
 			}catch(Exception e){
 				System.err.println (e.getMessage ());}
 	}
 
 	public static void InsertFav(EmbeddedSQL esql)
 	{
 		try{
 			System.out.print("Enter title of move you would like to add as favorite: ");
 			String title = in.readLine();
 			String query = "SELECT video_id FROM video WHERE title = '" + title + "'";
 				
 			int vid_id = esql.getVidId(query);
 				
 			System.out.println("GOT THE VID ID");
 			query = "INSERT INTO likes (user_id, video_id) VALUES ('" + currentUser + "', " + vid_id + ")";
 			esql.executeUpdate(query);
 				
 			System.out.println("New favorite added");
 			
 		}catch(Exception e){
 				System.err.println (e.getMessage ());}		
 	}
 
 	public static void OrderMovie(EmbeddedSQL esql)
 	{
 		try{		
 			System.out.print("Title of movie you would like to order: ");
			String title = in.readline();
 
 			//Grabbing title and will print it
 			String query = "SELECT * FROM video WHERE title = ";
 			query += title + "'";
 
 			int price = esql.executeCheckTitle(query);
 
 			query = "SELECT balance FROM users WHERE user_id ='" + currentUser + "';";
 			int temp = esql.executeOrder(query, price);
 
 			if( temp == 1 )
 				System.out.println("Successfully ordered: " + title);
 		}catch(Exception e){
 			System.err.println (e.getMessage ());}
 	}
 	
 
 	public static void IncreaseBalance(EmbeddedSQL esql)
 	{
 		try{
 			
 			System.out.print("How much would you like to add to balance?: ");
 			String stringbal = in.readLine();
 			int intbal = Integer.parseInt(stringbal);
 																
 			if(intbal <= 0)
 			{
 				System.out.println("Must input number greater than $0");
 				return;
 			}
 			
 			System.out.println("About to update");
 
 	
 			String query = "UPDATE users SET balance = balance +" + intbal + " WHERE user_id = '" 
 					+ currentUser + "'";
 											
 			esql.executeUpdate(query);
 	
 			System.out.println("UPDATED");
 			
 			String newbalance = "SELECT balance FROM users WHERE user_id='" + currentUser + "'";
 			System.out.print("New Balance after addition: ");
 			esql.executeStringQuery(newbalance, 1);		
 			
 			return;
 			
 		}catch(Exception e){	
 			System.err.println (e.getMessage ());}
 	}
 	
 	// NOT FULLY IMPLEMENTED NEED TO GRAB VIDEO ID AND THEN RATE MOVIE
 	public static void RateMovie(EmbeddedSQL esql)
 	{
 		try{
 			
 			System.out.print("Title of movie you would like to rate: ");
 			String title = in.readLine();
 			System.out.print("What would you like to rate the movie (0 - 10): ");
 			String rating = in.readLine();
 			
 			String query = "SELECT title FROM Video";
 			
			String output = esql.executeStringQuery(query);
 
 		}catch(Exception e){
 			System.err.println (e.getMessage ());}
 	}
    
 	public static void Greeting(){
       System.out.println(
          "\n\n*******************************************************\n" +
          "              User Interface      	               \n" +
          "*******************************************************\n");
    }//end Greeting
 
    ///////////////////////////////////////////////////////////////START//////////
 	/////////////////////////////////////////////////////////////CHANGES//////////
    public static int LoginAsSuper(EmbeddedSQL esql)
    {
 		try{
 			System.out.println("Verifying that you are a Super User");
 			String query = "SELECT COUNT(super_user_id) FROM super_user WHERE super_user_id='" 
 					+ currentUser + "';";
 		
 			int resultset = esql.executeLoginQuery(query);
 
 			if( resultset != 1 ){
 				System.out.println("I'm sorry but you are not a registered super user");
 				return 1;
 			}
 
 			else if( resultset == 1 ){
 				System.out.println("Welcome super user " + currentUser);
 				superUser = true;
 				return 0;
 			}
 
 		}catch(Exception e){
 			System.err.println( e.getMessage() );
 			return 1;
 			}
 		return 0;
    }//end LoginAsSuper
 
 	//Register a new video, creating new video_id, season_id, and series_id if necessary
    public static int RegisterMovie(EmbeddedSQL esql)
    {
 		try{
 			boolean isSeries = false;
 			System.out.println("Please enter the following information to register a new movie/series");
 			
 			System.out.println("What is the title of this video?");
 			String inputTitle = in.readLine();
 			
 			System.out.println("Is this video part of a series (y/n)?");
 			String inputSeries = in.readLine();
 			
 			String inputSeasonNum = "";
 			String inputEpNum = "";
 			String inputYear = "";
 			int inputOnlinePrice;
 			int inputDvdPrice;
 			int inputRating;
 			int series_id;
 			int season_id;
 			
 			//if this is a season get season info
 			if( inputSeries.equals("y") ){
 				isSeries = true;
 				
 				System.out.println("What season number is this?");
 				inputSeasonNum = in.readLine();
 				
 				System.out.println("What episode is this?");
 				inputEpNum = in.readLine();
 			}
 
 			System.out.println("What year did this come out?");
 			inputYear = in.readLine();
 
 			System.out.println("How much should the online price be?");
 			inputOnlinePrice = Integer.parseInt(in.readLine());
 
 			System.out.println("How much should the dvd price be?");
 			inputDvdPrice = Integer.parseInt(in.readLine());
 
 			System.out.println("How would you rate this (0-10)?");
 			inputRating = Integer.parseInt(in.readLine());
 
 			//this will get the video id
 			String query = "SELECT MAX(video_id) FROM video;";
 			Statement stmt = esql._connection.createStatement ();
 			ResultSet rs = stmt.executeQuery (query);
       		ResultSetMetaData rsmd = rs.getMetaData ();
       		int numCol = rsmd.getColumnCount ();
      		rs.next();
 	 		int video_id = Integer.parseInt(rs.getString(numCol)) + 1;
 			System.out.println(video_id);
 			stmt.close();
 	 		
 			//this will check if the series is already in record if not then it adds it
 			if( isSeries ){
 				stmt = esql._connection.createStatement();
 				String getSeriesId = "SELECT series_id FROM series WHERE title='" + inputTitle + "'";
 				rs = stmt.executeQuery (getSeriesId);
       			rsmd = rs.getMetaData ();
       			numCol = rsmd.getColumnCount ();
      			
 				//get the series id if it already exists
 				if(rs.next()) series_id = Integer.parseInt(rs.getString(numCol));
 				//if series id doesnt already exist then create a new series value
 				else{
 					getSeriesId = "SELECT MAX(series_id) FROM series;";
 					rs = stmt.executeQuery(getSeriesId);
 					rsmd = rs.getMetaData();
 					if(rs.next()) series_id = Integer.parseInt(rs.getString(1)) + 1;
 					else series_id = 1;
 
 					//add series information to database
 					String insertSeries = "INSERT INTO series (series_id, title) VALUES (" + series_id + ", '" + inputTitle + "');";
 					esql.executeUpdate(insertSeries);
 				}
 
 				String getSeasonId = "SELECT season_id FROM season WHERE series_id='" + series_id + "' AND season_number='" + inputSeasonNum + "';";
 				rs = stmt.executeQuery(getSeasonId);
 				rsmd = rs.getMetaData();
 				numCol = rsmd.getColumnCount();
 
 				//if the season id already exists grab the season id
 				if(rs.next()) season_id = Integer.parseInt(rs.getString(1));
 				//if the season id doesn't exist then place insert new season value
 				else{
 					getSeasonId = "SELECT MAX(season_id) FROM season;";
 					rs = stmt.executeQuery(getSeasonId);
 					rsmd = rs.getMetaData();
 					if(rs.next()) season_id = Integer.parseInt(rs.getString(1)) + 1;
 					else season_id = 1;
 
 					String insertSeason = "INSERT INTO season (season_id, series_id, season_number ) VALUES (" + season_id + ", " + series_id + ", '" + inputSeasonNum + "');";
 					esql.executeUpdate(insertSeason);
 				}
 
 				String insertVideo = "INSERT INTO video (video_id, title, year, online_price, dvd_price, rating, episode, season_id) VALUES " + 
 						"(" + video_id + ",'" + inputTitle + "', '" + inputYear + "', " + inputOnlinePrice + ", " + inputDvdPrice + ", " + inputRating + ", " + inputEpNum + ", " + season_id + ");";
 
 				esql.executeUpdate(insertVideo);
 
 				System.out.println("SEASON_ID " + series_id);
 				stmt.close();
 			}//End if video is a series
 
 			else if( !isSeries )
 			{
 				String insertVideo = "INSERT INTO video (video_id, title, year, online_price, dvd_price, rating) VALUES " + 
 						"(" + video_id + ",'" + inputTitle + "', '" + inputYear + "', " + inputOnlinePrice + ", " + inputDvdPrice + ", " + inputRating + ");";
 				stmt = esql._connection.createStatement ();
 				esql.executeUpdate(insertVideo);
 				stmt.close();
 			}//End if video is not series
 
 		}catch(Exception e){
 			System.err.println( e.getMessage() );
 			return 1;
 			}
 		return 0;
    }
 
 
    /////////////////////////////////////////////////////////////CHANGES//////////
   /////////////////////////////////////////////////////////////CHANGES//////////
   //////////////////////////////////////////////////////////////END//////////////
    /*
     * Reads the users choice given from the keyboard
     * @int
     **/
    public static int readChoice() {
       int input;
       // returns only if a correct value is given.
       do {
          System.out.print("Please make your choice: ");
          try { // read the integer, parse it and break.
             input = Integer.parseInt(in.readLine());
             break;
          }catch (Exception e) {
             System.out.println("Your input is invalid!");
             continue;
          }//end try
       }while (true);
       return input;
    }//end readChoice
 
   public static int LogInQuery(EmbeddedSQL esql){
       try{
 		System.out.println("You are trying to Login as an existing user, enter your Username (Make sure it is only 9 chars long) or press 9 to go back");
 		//Keep asking the user to login if they want to log in
 		while( true ){
 			System.out.println("Please specify your username or press 9 to go back:");
 			String inputUser = in.readLine();
 
 			if( inputUser.equals("9") ) return 1;  //exit the loop and go back to home screen
 			
 			String query = "SELECT COUNT(user_id) FROM users WHERE user_id='";
 			query += inputUser + "'";
 
          	int output = esql.executeLoginQuery (query);
 			System.out.println("This is output" + output);
 		 	
 			//check if username is valid, if not 1 then either output = 0 or > 2 which is a problem
 			if( output != 1 ) {
 				System.out.println("Sorry, that username isn't in our records.  Please try again.");
 				continue;
 			}//end username not found
 			
 			//if output is 1 then only 1 record with that user found (good)
 			else if( output == 1 ){
 				System.out.println("Please specify your password:");
 				String inputPass = in.readLine();
 				query = "SELECT COUNT(user_id) FROM users WHERE user_id='" + inputUser + "' AND password='" + inputPass + "'";
 				
 				int output2 = esql.executeLoginQuery(query); //returns number of query occurances
 
 				//if password doesn't match records ask for username again
 				if( output2 != 1 ){
 					System.out.println("Sorry, the username and password don't match up.  Please try again.");
 					continue;
 				}//end password fail
 
 				//if password correct then save current user info and log them in
 				else if( output2 == 1 ){
 					currentUser = inputUser;
 					System.out.println("Successfully logged in! Welcome " + currentUser + "!");
 					break;
 				}//end password success
 			}//end username found
 		}//end user login prompt
       }catch(Exception e){
          System.err.println (e.getMessage ());
 		 return 1;
       }//end catch
 	  return 0;
    }//end LoginQuery
 
     /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static int RegisterQuery(EmbeddedSQL esql){
 			System.out.println("You have selected to register yourself as a user.  Please specify the following information.");
 			try{
 				System.out.println("Enter a username you want (Username must be 9 chars or less long):");
 				String input = in.readLine();
 				
 				//if( input.length() > 9 ) { System.out.println("Invalid Username (more than 9 chars)"); continue; }
 
         		String query = "INSERT INTO Users (user_id, password, first_name, middle_name, last_name, e_mail, street1, street2, state, country, zipcode, balance)" +
 						"VALUES ('" + input + "','";
 
 				System.out.println("Enter your password:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your first name:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your middle name:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your last name:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your email:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your street1:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your street2:");
 				input = in.readLine();
 				query += input + "','";
 	
 				System.out.println("Enter your state:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your country:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your zipcode:");
 				input = in.readLine();
 				query += input + "','";
 
 				System.out.println("Enter your balance:");
 				input = in.readLine();
 				query += input + "')";
 				
 				esql.executeUpdate (query);
 		
 			}catch(Exception e){
 					System.err.println (e.getMessage ());
 					return 1;
 			}//end catch
 		return 0;
 	  }//end RegisterQuery
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 /*   
    public static void RegisterQuery(EmbeddedSQL esql){
      	try{
          String query = "SELECT C.sid, COUNT(C.pid) FROM Suppliers S, Catalog C "
 				+ "WHERE C.sid=S.sid GROUP BY C.sid;";
          
 		 int rowCount = esql.executeQuery (query);
          System.out.println ("total row(s): " + rowCount);
       }catch(Exception e){
  		System.err.println (e.getMessage ());
 	  }
    }//end Query1
 
    public static void Query2(EmbeddedSQL esql){
       try{
 		String query = "SELECT C.sid, COUNT(C.pid) FROM Suppliers S, Catalog C "
 				+ "WHERE C.sid=S.sid GROUP BY C.sid HAVING COUNT(C.pid)>=3;";
 		
 	  	int rowCount = esql.executeQuery (query);
         System.out.println ("total row(s): " + rowCount);
  
 	  }catch(Exception e){
  		System.err.println (e.getMessage ());
 	  }
    }//end Query2
 
    public static void Query3(EmbeddedSQL esql){
       try{
 		String query = "SELECT sname, COUNT(C.pid) FROM Suppliers S, Catalog C "
 				+ "WHERE pid IN (SELECT pid FROM Parts WHERE color='Green') AND S.sid=C.sid "
 				+ "GROUP BY sname";
 			
 	  	int rowCount = esql.executeQuery (query);
         System.out.println ("total row(s): " + rowCount);
 		
 	  	}catch(Exception e){
  			System.err.println (e.getMessage ());
 	  	}
    }//end Query3
 
    public static void Query4(EmbeddedSQL esql){
       try{
 		String query = "SELECT P.pname, MAX(C.cost) FROM Parts P, Catalog C "
 						+ "WHERE C.sid IN (SELECT temp.sid "
 							+ "FROM (SELECT sid, pid FROM Catalog WHERE pid IN (SELECT pid FROM Parts WHERE color='Red')) as temp, "
 							+ "(SELECT sid, pid FROM catalog WHERE PID IN (SELECT pid FROM Parts WHERE color='Green')) as temp2 "
 							+ "WHERE temp.sid = temp2.sid) "
 						+ "GROUP BY P.pname;";
 
 		int rowCount = esql.executeQuery (query);
         System.out.println ("total row(s): " + rowCount);
 		
 	  }catch(Exception e){
  		System.err.println (e.getMessage ());
 	  }
    }//end Query4
 
    public static void Query5(EmbeddedSQL esql){
      try{
 		String query = "SELECT DISTINCT pname FROM Parts "
 				+ "WHERE pid IN (SELECT pid FROM Catalog WHERE cost<";
 	  	
         System.out.print("\tEnter cost: $");
         String input = in.readLine();
 		query+=input;
 		query += ");";
 
 		int rowCount = esql.executeQuery (query);
         System.out.println ("total row(s): " + rowCount);
 		
 	 }catch(Exception e){
  		System.err.println (e.getMessage ());
 	  }
    }//end Query5
 
    public static void Query6(EmbeddedSQL esql){
       try{
 		String query = "SELECT address FROM Suppliers WHERE sid IN ("
 						+ "SELECT sid FROM Catalog WHERE pid IN ("
 						+ "SELECT pid FROM Parts WHERE pname='";
 
         System.out.print("\tEnter name of part: ");
 		String input = in.readLine();
 		query += input;
 		query += "'));";
 
 		int rowCount = esql.executeQuery (query);
         System.out.println ("total row(s): " + rowCount);
 	  	
 	  }catch(Exception e){
  		System.err.println (e.getMessage ());
 	  }
    }//end Query6
 */
 }//end EmbeddedSQL
