 import java.io.*;
 import java.sql.*;
 import java.text.SimpleDateFormat;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.Iterator;
 
 
 public class db_helper {
 	
 	Connection server;
 	Statement stmt;
 	DatabaseMetaData dbmd;
 	/*
 	 * db_helper constructor
 	 */
 	public db_helper(){
 	    //connecting to red dwarf
 	    try {
 	    	//this is temporary.  just for debugging purposes.
 		    String red_dwarf_url = "jdbc:postgresql://reddwarf.cs.rit.edu/p48501b"; 
 			server = DriverManager.getConnection(red_dwarf_url, "p48501b", "heifohhitheihiqu");
 			stmt = server.createStatement();
 			dbmd = server.getMetaData();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			System.out.println("connection failed");
 			e.printStackTrace();
 		}
 		
 	}
 	
 	
 	/*
 	 * dumps all movie data into string except for id
 	 * @return movie_names
 	 */
 	public Vector<movie> getMoviedata(String searchTitle){
 		Vector<movie> movie_data = new Vector<movie>();
 		System.out.println("querying " + searchTitle + " in movies" );
 		
 		String query = "select * from movie where title like '%" + searchTitle + "%';";
 		
 		//S/ystem.out.println("created query for SQL");
 		try {
 			//System.out.println("query: " + query);
 			
 			ResultSet movies = stmt.executeQuery(query);
 			while(movies.next()){
 				//create temp movie object to store data
 				movie tmp_movie = new movie();
 				
 				tmp_movie.setMid(Integer.parseInt(movies.getString("mid")));
 				tmp_movie.setTitle(movies.getString("title"));
 				tmp_movie.setGenre(movies.getString("genre"));
 				tmp_movie.setRuntime(Integer.parseInt(movies.getString("runtime")));
 				tmp_movie.setRating(movies.getString("rating"));
 				tmp_movie.setPlot(movies.getString("plot"));
 				tmp_movie.setProduction_year(Integer.parseInt(movies.getString("production_year")));
 				tmp_movie.setRelease_date(movies.getString("release_date"));
 				
 				movie_data.add(tmp_movie);
 			}
 		}catch(SQLException e){
 			System.err.println("error getting movie names\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d.getStackTrace());
 		}
 		
 		return movie_data;
 	}
 	
 	/*
 	 * get people names when given a string query
 	 * @return movie_names
 	 */
 	public Vector<person> getPeopleData(String name){
 		Vector<person> people_names = new Vector<person>();
 		System.out.println("querying " + name + " in people" );
 		
 		String query = "select id,name,gender from person where name like '%" + name + "%';";
 		
 		try {
 			
 			ResultSet people = stmt.executeQuery(query);
 			System.out.println("got people names");
 			while(people.next()){
 				person tmp_person = new person();
				tmp_person.setId(Integer.parseInt(people.getString("id")));
 				tmp_person.setName(people.getString("name"));
 				
 				tmp_person.setGender(people.getString("gender"));
 				
 				people_names.add(tmp_person);
 			}
 		}catch(SQLException e){
 			System.err.println("error getting people names\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
 		return people_names;
 	}
 	
 	/*
 	 * get people names when given a string query
 	 * @return movie_names
 	 */
 	public Vector<character> getCharacterData(String name){
 		Vector<character> characterData = new Vector<character>();
 		System.out.println("querying " + name + " in characters ");
 		
 		String query = "select id,name from characters where name like '%" + name + "%';";
 		
 		try {
 		
 			
 			ResultSet people = stmt.executeQuery(query);
 			//System.out.println("got people names");
 			while(people.next()){
 				character tmp_char = new character();
				tmp_char.setId(Integer.parseInt(people.getString("id")));
 				tmp_char.setName(people.getString("name"));
 				characterData.add(tmp_char);
 			}
 		}catch(SQLException e){
 			System.err.println("error getting character names\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
 		return characterData;
 	}
 	
 	/*
 	 * get box office data for a single movie
 	 * 
 	 */
 	public String getBoxOfficeData(String movie_id){
 		String box_office = "";
 		System.out.println("querying " + movie_id + " in box_office");
 		
 		String query = "select BO.opening_gross_data,BO.total_gross from box_office as BO, opened as O where o.mid = " + movie_id +
 				" and O.bid = BO.id;";
 		
 		try {
 		
 			ResultSet results = stmt.executeQuery(query);
 			while(results.next()){
 				String owg = results.getString("opening_weekend_gross");
 				String tg = results.getString("total_gross");				
 				box_office = owg +"\t" + tg; 
 			}
 		}catch(SQLException e){
 			System.err.println("box office data for \n" + movie_id);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
 		return box_office;
 	}	
 	
 	/*
 	 * get production companies for a single movie
 	 * 
 	 */
 	public Vector<String> getProductionCompaniesbyMovie(String movie_id){
 		Vector<String>  companies = new Vector<String>();
 		String line = "";
 		System.out.println("querying " + movie_id + " in production companies");
 		
 		String query = "select PC.id,PC.name from production_companies as PC, produced as P where P.mid = " + movie_id +
 				" and P.company_id = PC.id;";
 		
 		try {
 		
 			ResultSet results = stmt.executeQuery(query);
 			while(results.next()){
 				String id = results.getString("id");
 				String tg = results.getString("name");				
 				line = id +"\t" + tg;
 				
 				companies.add(line);
 			}
 		}catch(SQLException e){
 			System.err.println("production company data error for \n" + movie_id);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
 		
 		return companies;
 	}	
 	
 
 	//lets create a table
 	// variables(name, type, not null)
 	//primary_key(name)
 	//foreign key(attr referenced_table ref att);
 	public int createTable(String table_name, Vector<String> variables, Vector<String> primary_keys, Vector<String> foreign_keys){
 	
 		//extract variable data
 		String variablesString = "";
 		String primaryString = "";
 		String tmpString = "";
 		
 		System.out.println("table name is " + table_name);
 		//check data values 
 		for(int k = 0; k< variables.size()-1; k++){
 			
 			//System.out.println("print variables " + variables.elementAt(k));
 			variablesString = variablesString + variables.elementAt(k)+", ";
 		}
 		
 		for(int k = 0; k< primary_keys.size()-1; k++){
 			
 			//System.out.println("print primary " + primary_keys.elementAt(k));
 			primaryString += primary_keys.elementAt(k);
 		}
 		
 		
 		System.out.println("Variable String: " + variablesString);
 		System.out.println("Primary Keys: " + primaryString);
 		
 		/*
 		for(int k = 0; k< foreign_keys.size()-1; k++){
 			
 			System.out.println("print variables " + foreign_keys.elementAt(k));
 			
 		}*/
 		//extract primary keys
 		//extract foreign_keys	
 		//String query = "create table " + table_name"( );";
 		
 		
 		String query = "create table " + table_name + " (" + variablesString + " primary key(" + primaryString + "));";
 		
 		System.out.println("Create table query: " + query);
 		try{	
 			stmt.executeUpdate(query); 
 		}catch(SQLException e){
 			System.err.println("ERR: couldn't create table");
 			return 0;
 		}
 		
 		
 		return 1;
 	}
 
 	/*
 	 * deletes a table
 	 */
 	public int deleteTable(String table){
 		String query = "drop table " + table;
 	
 		/*try{
 			
 			//we'll add this later.  we don't want deletion yet. 
 			//stmt.executeUpdate(query); 
 		}catch(SQLException e){
 			System.err.println("ERR: couldn't delete table " + table);
 			return 0;
 		}*/
 		return 1;
 	}	
 	
 	
 	public int truncateTable(String table){
 		String query = "truncate table " + table;
 		/*try{
 		
 		//we'll add this later.  we don't want truncation yet. 
 		//stmt.executeUpdate(query); 
 	}catch(SQLException e){
 		System.err.println("ERR: couldn't truncate table " + table);
 		return 0;
 	}*/
 		return 1;
 	}
 
     /**
      * Adds a movie and all of its corresponding data, uses the stored procedure add_movie
      * @param actorArr a vector of string arrays, each array is of length 3 and
      * formatted as follows: 0th index = Name, 1st index = gender, 2nd index = Role
      * @param directorsArr, producersArr, writersArr, production_companiesArr
      * are all vectors of strings
      * @param opening_day is a Date 
      */
     public void addMovie( String name, Vector<String[]> actorArr,
                          Vector<String[]> directorsArr, Vector<String[]> producersArr,
                          Vector<String[]> writersArr, String total_gross,
                          String opening_weekend_gross, Date opening_day,
                          Vector<String[]> production_companiesArr, String genre,
                          String rating, String plot, int runtime,
                          int production_year ){
         try{
             
             String opening_dayStr = (new SimpleDateFormat( "yyyy MM dd" )).format(opening_day);
             
             String actors = parseHelper( actorArr, 3 );
             String directors = parseHelper( directorsArr, 2 );
             String writers = parseHelper( writersArr, 2 );
             String producers = parseHelper( producersArr, 2 );
             String production_companies = parseHelper( production_companiesArr, 2 );
             
             String theQuery = "SELECT * FROM create_movie('" + name + "','" + actors +
             "','" + directors + "','" + producers + "','" + writers +
             "','" + total_gross + "','" + opening_weekend_gross +
             "', TO_DATE('" + opening_dayStr + "','YYYY MM DD')" +
             ",'" + production_companies + "','" + genre + "','" + rating +
             "','" + plot + "'," + runtime + "," + production_year + ");";
             
             System.out.println( theQuery );
             
             stmt.executeQuery( theQuery );
             
         }catch(SQLException e){
 			System.err.println("error adding movie\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" );
             d.printStackTrace();
 		}
         
     }
     
     private String parseHelper( Vector<String[]> toParse, int strArrSize ){
         String toReturn = "";
         for( int i = 0; i < toParse.size(); i++ ){
             String[] thisInner = toParse.get(i);
             for( int inner = 0; inner < strArrSize; inner++ ){
                 toReturn += "" + thisInner[inner] + "";
                 if( inner < strArrSize - 1 ){
                     toReturn += ",";
                 }
             }
             if( i < toParse.size() - 1 ){
                 toReturn += ";";
             }
         }
         return toReturn;
     }
     
     /**
      * Deletes data from a given table where the table IDs for deletion are in IDs
      * @param table the table to delete from
      * @param IDs the ids to remove 
      */
     public void deleteData(String table, Vector<Integer> IDs){
         
         String inStr = "(";
         
         for( int i = 0; i < IDs.size(); i++ ){
             if( i < IDs.size() - 1 ){
                 inStr += Integer.toString( IDs.get(i) ) + ",";
             }
         }
         inStr += ")";
         
         try{
             
             ResultSet rs = dbmd.getColumns(null, null, table, null);
             String idCol = rs.getMetaData().getColumnName( 1 );
             
             stmt.executeQuery( "delete from " + table + " where " + idCol + " in " + inStr );
             
         }catch(SQLException e){
 			System.err.println("error adding movie\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
 
     }
     
     /**
      * Inserts data to a given table with columns (or null if inserting to all columns) and values vals
      * @param table the table name to insert to
      * @param cols the columns to update, or null if updating all columns
      * @param vals the values to insert to the columns
      */
     public void insertData(String table, Vector<String> cols, Vector<String> vals){
         
         String colsStr = "";
         if( cols != null ){
             colsStr = "(";
             
             for( int i = 0; i < cols.size(); i++ ){
                 if( i < cols.size() - 1 ){
                     colsStr += cols.get(i) + ",";
                 }
             }
             colsStr += ")";
         }
         
         String valStr = "(";
         
         for( int i = 0; i < vals.size(); i++ ){
             if( i < vals.size() - 1 ){
                 valStr += vals.get(i) + ",";
             }
         }
         valStr += ")";
         
         try{
             
             stmt.executeQuery( "insert into " + table + colsStr + " values " + valStr );
             
         }catch(SQLException e){
 			System.err.println("error adding movie\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
         
     }
     
     /**
      * Updates data
      * @param table the table to update
      * @param cols the columns to update
      * @param vals the values to set the columns to
      * @param IDs the IDs to update for the given table
      */
     public void updateData(String table, Vector<String> cols, Vector<String> vals, Vector<Integer> IDs){
         String updateStr = "";
         for( int i = 0; i < cols.size(); i++ ){
             
             updateStr += cols.get(i) + "=" + vals.get(i);
             
             if( i < cols.size() - 1 ){
                 updateStr += cols.get(i) + ",";
             }
         }
         
         String inStr = "(";
         
         for( int i = 0; i < IDs.size(); i++ ){
             if( i < IDs.size() - 1 ){
                 inStr += Integer.toString( IDs.get(i) ) + ",";
             }
         }
         inStr += ")";
 
         try{
 
             ResultSet rs = dbmd.getColumns(null, null, table, null);
             String idCol = rs.getMetaData().getColumnName( 1 );
 
             stmt.executeQuery( "update " + table + " set " + updateStr + " where " + inStr );
             
         }catch(SQLException e){
 			System.err.println("error adding movie\n" + e);
 		}catch(NullPointerException d){
 			System.err.println("null pointer exception" + d);
 		}
         
     }
     
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
         String name = "this will work";
         
         Vector<String[]> actors = new Vector<String[]>();
         actors.add( new String[]{"actor", "m", "role"} );
         actors.add( new String[]{"actor1", "f", "rol1"} );
         
         Vector<String[]> directors = new Vector<String[]>();
         directors.add( new String[]{"director1 yeah", "m"} );
         directors.add( new String[]{"director2 woot", "f"} );
 
         Vector<String[]> producers = new Vector<String[]>();
         producers.add( new String[]{"producer1 j", "m"} );
         
         Vector<String[]> writers = new Vector<String[]>();
         writers.add( new String[]{"writer1 jkldfsj", "f"} );
 
         String total_gross = "$43289032";
         String opening_weekend_gross = "$9203";
         
         Date opening_day = new Date(1, 1, 1);
         
         Vector<String[]> production_companies = new Vector<String[]>();
         production_companies.add( new String[]{"dfjslk;jfdksl", "behind you"} );
 
         String genre = "THRILLA";
         String rating = "NC-17";
         String plot = "The quick brown fox jumped over the lazy dog";
         int runtime = 340;
         int production_year = 1984;
         
         
 		db_helper helper = new db_helper();
 	    System.out.println("connection success!");
 //		System.out.println("give me a query!");
 
 		String userInput = "";
 
 		
         helper.addMovie( name, actors, directors, producers, writers, total_gross, opening_weekend_gross, opening_day,production_companies, genre, rating, plot, runtime, production_year );
         
         System.exit(0);
 
         
 		InputStreamReader inReader = new InputStreamReader(System.in);
 		BufferedReader bReader = new BufferedReader( inReader );
 								
 	    while (!userInput.equals("Q") && !userInput.equals("q"))
 		{
 		
 		System.out.println("IMDB database manager\n" + "C - Create\n" + "R - Retrieve\n" + 
 			"U - Update\n" + "D - Delete\n" + "Q - Quit\n" + "Select option:");
 
 
 				try{
 					userInput = bReader.readLine();		//read in user input
 				}catch(IOException e){
 					System.err.println("trouble reading query from console\n" + e);
 				}catch(NullPointerException d){
 					System.err.println(d.getStackTrace());
 				}
 			
 
 				if (userInput.equals("R") || userInput.equals("r"))
 				{
 
 						System.out.println("Enter search term:\n");
 
 						String query = "";
 						
 						try{
 							query = bReader.readLine();		//read in movie search
 						}catch(IOException e){
 							System.err.println("trouble reading query from console\n" + e);
 						}catch(NullPointerException d){
 							System.err.println(d.getStackTrace());
 						}
 	
 						Vector<String> movie_query_results;
 					
 						//get movie results on query
 						/*movie_query_results = helper.getMoviedata(query);	    
 						if(movie_query_results != null){
 							
 							Enumeration<String> listy = movie_query_results.elements();
 							
 							//inserting data into the red dwarf db
 							System.out.println("movie results:");
 
 							while(listy.hasMoreElements()){
 								//System.out.println("Inserting movie id: " + listy.nextElement());
 								String movieString = listy.nextElement();
 								System.out.println(movieString);
 							}	    
 						}*/
 						//get people results on query
 
 						/*Vector<String> people_query_results= helper.getPeopleData(query);	    
 						if(people_query_results != null){
 							
 							Enumeration<String> listy = people_query_results.elements();
 							
 							//inserting data into the red dwarf db
 							System.out.println("people results:");
 
 							while(listy.hasMoreElements()){
 								String peopleString = listy.nextElement();
 								System.out.println(peopleString);
 							}	    
 						}  */  
 						
 						//get character results on query
 						
 						/*Vector<String> char_query_results= helper.getCharacterData(query);	    
 						if(char_query_results != null){
 							
 							Enumeration<String> listy = char_query_results.elements();
 							
 							//inserting data into the red dwarf db
 							System.out.println("character results:");
 
 							while(listy.hasMoreElements()){
 								String peopleString = listy.nextElement();
 								System.out.println(peopleString);
 							}	    
 						}*/
 				}	
 				if (userInput.equals("c") || userInput.equals("C"))
 				{
 					String table_name_query = "";
 					Vector<String> variables = new Vector<String>();
 					Vector<String> primary_keys = new Vector<String>(); 
 					Vector<String> foreign_keys = new Vector<String>();;
 					
 					System.out.print("table name:");		
 
 					String query = "";
 						
 						try{
 							query = bReader.readLine();		//read in table name 
 							table_name_query = query;
 						}catch(IOException e){
 							System.err.println("trouble reading query from console\n" + e);
 						}catch(NullPointerException d){
 							System.err.println(d.getStackTrace());
 						}
 					
 					System.out.println("input variable data in this format (name type not null)");
 					System.out.println("name - any format\n type - sql data types\n not mull - write out not null");
 					while(!query.equals("")){
 						System.out.print("variable:");
 						try{
 							query = bReader.readLine();
 						}catch(IOException e){
 							System.err.println("trouble reading query from console\n" + e);
 						}catch(NullPointerException d){
 
 							System.err.println(d.getStackTrace());
 						}
 							//System.out.println("variable entered: " + query);
 							variables.add(query);
 							//System.out.println("after");
 					}
 					query = "1";
 					System.out.println("define a primary keys in format [attr name]");
 					while(!query.equals("")){
 						System.out.print("primary_key:");
 						try{
 							query = bReader.readLine();
 						}catch(IOException e){
 							System.err.println("trouble reading query from console\n" + e);
 						}catch(NullPointerException d){
 							System.err.println(d.getStackTrace());
 						}
 							primary_keys.add(query);
 					}
 
 					query = "1";
 					
 					/*
 					System.out.println("define a foreign key [attr name, table reference, reference attr]");
 					while(!query.equals("")){
 						System.out.print("foreign_key:");
 						try{
 							query = bReader.readLine();
 						}catch(IOException e){
 							System.err.println("trouble reading query from console\n" + e);
 						}catch(NullPointerException d){
 							System.err.println(d.getStackTrace());
 						}
 							foreign_keys.add(query);
 					}*/
 					
 					int x = helper.createTable(table_name_query, variables, primary_keys, foreign_keys);
 					
 					if(x == 1)
 						System.out.println("created table successfully");
 					else
 						System.out.println("error creating table");
 
 				}
 
 				if (userInput.equals("u") || userInput.equals("U"))
 				{
 					
 				}
 
 
 				if (userInput.equals("d") || userInput.equals("D"))
 				{
 					System.out.println("Which table do you want to delete?\n");
 
 						String qq= "";
 						
 						try{
 							qq= bReader.readLine();		//read in  table name
 						}catch(IOException e){
 							System.err.println("trouble reading query from console\n" + e);
 						}catch(NullPointerException d){
 							System.err.println(d.getStackTrace());
 						}
 					helper.deleteTable(qq);
 					System.out.println("THIS WILL DELETE A TABLE BUT WE DONT WANT THAT TO FUNCTION YET");
 				}
 
 	    }
    
 	   System.out.println("end of program"); 
 	}
 }
