 import java.io.*;
 import java.sql.*;
 import java.util.Enumeration;
 import java.util.Vector;
 
 
 public class db_importer {
 
 	public static String flipName(String name){
 		int comma_location = name.lastIndexOf(",");
 		String last_name = name.substring(0, comma_location);
 		String first_name = name.substring(comma_location+1);
 		
 		name = first_name+" " + last_name;
 		
 		//System.out.println("new name: " + name);
 		
 		return name;
 	}
 	
 	public static String convert_date(String date){
 		
 		String day = "";
 		String month = "";
 		String year = "";
 		
 		System.out.println("converting_date: " + date);
 		
 		
 		year = date.substring(date.length()-4, date.length());
 		
 		if(date.contains("January")){
 			month = "01";
 		}else if(date.contains("February")){
 			month = "02";
 		}else if(date.contains("March")){
 			month = "03";
 		}else if(date.contains("April")){
 			month = "04";
 		}else if(date.contains("May")){
 			month = "05";
 		}else if(date.contains("June")){
 			month = "06";
 		}else if(date.contains("July")){
 			month = "07";
 		}else if(date.contains("August")){
 			month = "08";
 		}else if(date.contains("September")){
 			month = "09";
 		}else if(date.contains("October")){
 			month = "10";
 		}else if(date.contains("November")){
 			month = "11";
 		}else if(date.contains("December")){
 			month = "12";
 		}
 		
 		day = date.substring(0, date.indexOf(" "));
 		
 		
 		
 		if(day.length() == 1){
 			day = "0" + day;
 		}else if(day.length() == 4){
 			return "00/00/" + year;
 		}
 		else if(day.length() > 2){
 			return month + "/01/" + year;
 		}
 		
 		
 		
 		return month + "/" + day + "/" + year;
 		
 	}
 
 	/*
 	 * return id, name, gender of people associated with movie_id
 	 * */
 	public static ResultSet getPeopleAndIdsAndGenderbyMovieId(Statement stmt, String movie_id) throws SQLException{
 		
 		//returns names.  role_id as 1 sets it for actors names being returned.  
 		String query = "select N.id,N.name,N.gender from title as T, name as N, cast_info as C where C.movie_id = " + movie_id + " and N.id = C.person_id limit 100;";
 		
 	    return stmt.executeQuery(query);
 	}
 	
 	/*
 	 * return actors/actresses (role_id = 1).  just returns names.
 	 */
 	public static ResultSet getActorsNamebyMovieId(Statement stmt, String movie_id) throws SQLException{
 
 		
 		//returns names.  role_id as 1 sets it for actors names being returned.  
 		String query = "select N.name from title as T, name as N, cast_info as C where C.movie_id = " + movie_id + " and N.id = C.person_id  and C.role_id = 1 limit 100";
 		
 	    return stmt.executeQuery(query);
 	}
 	
 	//get roles in a movie
 	public static ResultSet getRolesandId(Statement stmt, String movie_id) throws SQLException{
 		
 		String query = "select CN.id,CN.name from char_name as CN, cast_info as CI where CI.movie_id = " + movie_id + " and CI.person_role_id = CN.id;";
 		
 		return stmt.executeQuery(query);
 	}
 	
 	
 	/*
 	 * returns director information (id, name) 
 	 */
 	public static ResultSet getDirectorsbyMovieId(Statement stmt, String movie_id) throws SQLException{
 		
 		//returns names.  role_id as 1 sets it for actors names being returned.  
 		String query = "select N.id,N.name from name as N, cast_info as C where C.movie_id = '" + movie_id + "' and N.id = C.person_id  and C.role_id = 8";
 		
 	    return stmt.executeQuery(query);
 	}
 	/*
 	 * returns producer information
 	 */
 	public static ResultSet getProducersbyMovieId(Statement stmt, String movie_id) throws SQLException{
 		
 		//returns names.  role_id as 3 sets it for actors names being returned.  
 		String query = "select N.id,N.name from name as N, cast_info as C where C.movie_id = '" + movie_id + "' and N.id = C.person_id  and C.role_id = 3";
 		
 	    return stmt.executeQuery(query);
 	}
 	
 	/*
 	 * returns writer information
 	 */
 	public static ResultSet getWritersbyMovieId(Statement stmt, String movie_id) throws SQLException{
 		
 		//returns names.  role_id as 4 sets it for actors names being returned.  
 		String query = "select N.id,N.name from name as N, cast_info as C where C.movie_id = '" + movie_id + "' and N.id = C.person_id  and C.role_id = 4";
 		
 	    return stmt.executeQuery(query);
 	}
 	
 	
 	/*
 	 * get roles in movies
 	 * person_type = 1 for actor
 	 * person_type = 2 for actress
 	 */
 	public static ResultSet getRolesinMoviesbyMovieId(Statement stmt, String movie_id, int person_type) throws SQLException{
 		
 		//finding actor roles
 		String query = "select person_id,person_role_id from cast_info where movie_id = " + movie_id + " and role_id =" + person_type +";";
 		ResultSet rs = stmt.executeQuery(query);
 	
 		return rs;
 	}
 	
 	
 
 	//gets movie genres
 	public static String getMovieGenre(Statement fromServer, String movie_id) throws SQLException{
 		String query = "select MI.info from movie_info as MI where MI.movie_id = '" + movie_id + "' and MI.info_type_id = 3;";
 		ResultSet rs = fromServer.executeQuery(query);
 		
 		String genre = "";
 		
 	    while(rs.next()){
 	    	String i = rs.getString("info");
 	    	//System.out.println(i);
 	    	
 	    	genre = genre + i + "/";
 	    }		
 		
 	    //System.out.println("genre is " + genre);
 		
 	    return genre;
 	}
 	
 	//grabs a movie plot.  combines them if multiple.
 	public static String getMoviePlot(Statement fromServer, String movie_id) throws SQLException{
 		
 		String plot = "";
 		String query = "select MI.info from movie_info as MI where MI.movie_id = '" + movie_id + "' and MI.info_type_id = 98;";
 		ResultSet rs = fromServer.executeQuery(query);
 				
 	    //while(rs.next()){
 		if(rs.next()){
 	    	String i = rs.getString("info");
 	    	//System.out.println(i);
 	    	
 	    	plot = i;
 	    }
 
 	
 		return plot;	
 	}
 	
 	public static String getMovieRuntime(Statement fromServer, String movie_id) throws SQLException{
 		String runtime = "";
 		String query = "select info from movie_info where info_type_id = 1 and movie_id = " + movie_id + ";";
 		
 		ResultSet rs = fromServer.executeQuery(query);
 		//System.out.println("got runtime query");	
 
 	    if(rs.next()){
 	    	runtime = rs.getString("info");
 	    }
 	    	
 		
 		return runtime;
 	}
 	
 	public static String getMovieMPAA(Statement fromServer, String movie_id) throws SQLException{
 		String rating = "";
 		String query = "select info from movie_info where info_type_id = 97 and movie_id = " + movie_id + ";";
 		
 		ResultSet rs = fromServer.executeQuery(query);
 				
 	    if(rs.next()){
 	    	rating = rs.getString("info");
 	    }
 	    
 	    if(rating == ""){
 	    	return "N/A";
 	    }
 	    
 	    //System.out.println("rating before extraction: " + rating);
 	    
 	    //grabs the rating from a string
 	    rating = rating.substring(6, rating.indexOf(" ", 6));
 		//System.out.println("length of rating is " + rating.length());
 		return rating;
 	}
 	
 
 	public static String getMovieUSAReleaseDate(Statement fromServer, String movie_id) throws SQLException{
 		String release_date = "";
 		String query = "select info from movie_info where info_type_id = 16 and movie_id = " + movie_id + " and info like '%USA%' and note is null;";
 		
 		ResultSet rs = fromServer.executeQuery(query);
 				
 	    if(rs.next()){
 	    	release_date = rs.getString("info");
 	    }
 	    
 	    System.out.println("release_date before extraction: " + release_date + "with length " + release_date.length());
 	    if(release_date == ""){
 	    	return null;
 	    }
 	    
 	    //grabs the release date from a string
 	    release_date = release_date.substring(release_date.indexOf(":")+1, release_date.length());
 	    System.out.println("release_date before extraction: " + release_date + "with length " + release_date.length());
 	    if(release_date.length() > 4){
 		    System.out.println("pre-converted date: " + release_date);
 		    release_date = convert_date(release_date);
 			System.out.println("converted date to: " + release_date);
 	    }
 	    
 		return release_date;
 	}
 	
 	
 	
 	/*
 	 * inserts movie and person data
 	 */
 	public static void insertPeopleandMovieData(Statement fromServer, Statement toServer, String movie_id) throws SQLException{
 		
 		//insert movie_data(id, Name) into movie
 	    System.out.println("inserting into movie");
 	    String query = "select T.id, T.title, T.production_year from title as T where id = '" + movie_id + "'";
 	    ResultSet movie = fromServer.executeQuery(query);
 	    
 	    int ii = 0;
 	    movie.next();
 	    //while(movie.next()){
 	    if(true){
 	    	ii++;
 	    	//System.out.println("loop #" + ii);
 	    	
 	    	String id = movie.getString("id");
 	        String title = movie.getString("title");
 	        String production_year = movie.getString("production_year");
 	        
 	        //updating  genre data
 	        String genre = getMovieGenre(fromServer, id);
 	        
 	       //query = "insert into movie (mid, title, genre, plot, production_year) values ('" + i + "','" + t + "','" + genre + "','" + plot + "','" + p + "');";       
 	       query = "insert into movie (mid, title) values ('" + id + "','" + title + "');";
 	        
 	        
 	       try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted movie " + title + "(" + production_year + ") into red dwarf movie");
 	        }
 	        catch(SQLException d){
 	        	System.out.println("movie: " + title + " already exists in movie.");
 	        }
 	       
 	       
 	       //update production_year
 	       query = "update movie set production_year = '" + production_year + "' where mid = '" + movie_id + "';";
 	       try{
 	    	   toServer.executeUpdate(query);
 		       System.out.println("updated movie " + title + " with production_year: " + production_year);
 	        }
 	        catch(SQLException d){
 	        	System.out.println("couldn't update movie " + title + " with production_year: " + production_year);
 	        }
 	       
 
 	        //update genre 
 	        query = "update movie set genre = '" + genre + "' where mid = '" + movie_id + "';";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("updated movie " + title + " with genre: " + genre);
 	        }
 	        catch(SQLException d){
 	        	System.out.println("couldn't update movie " + title + " with genre: " + genre);
 	        }
 	        
 	        
 	        //update plot
 	        String plot = getMoviePlot(fromServer, id);
 	        
 	        query = "update movie set plot = '" + plot + "' where mid = '" + movie_id + "';";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("updated movie " + title + " with plot:" + plot);
 	        }
 	        catch(SQLException d){
 	        	System.out.println("couldn't update movie " + title + " with plot:" + plot);
 	        }
 	        
 	        //update runtime
 	        
 	        String runtime = getMovieRuntime(fromServer, movie_id);
 	        query = "update movie set runtime = '" + runtime + "' where mid = '" + movie_id + "';";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("updated movie " + title + " with runtime " + runtime);
 	        }
 	        catch(SQLException d){
 	        	System.out.println("couldn't update movie " + title + " with runtime " + runtime);
 	        }	        
 	        
        
 	        //update MPAA
 	        String mpaa = getMovieMPAA(fromServer, movie_id);
 	        query = "update movie set rating = '" + mpaa + "' where mid = '" + movie_id + "';";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("updated movie " + title + " with rating " + mpaa);
 	        }
 	        catch(SQLException d){
 	        	System.out.println("couldn't update movie " + title + "with rating " + mpaa);
 	        }	
 	        
 	        
 	        //update release day
 	        String release_day = getMovieUSAReleaseDate(fromServer, movie_id);
 	        //System.out.println("release day for title is " + release_day);
 	        query = "update movie set release_date = to_date('" + release_day + "','MM/DD/YYYY') where mid = '" + movie_id + "';";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("updated movie " + title + " with release_day " + release_day);
 	        }
 	        catch(SQLException d){
 	        	System.out.println("couldn't update movie " + title + "with release_day " + release_day);
 	        }		        	        		
 	    }
 	    
 	    
 		//insert people_data(id, Name, gender) into people
 		System.out.println("before getting people");
 	    ResultSet actors = getPeopleAndIdsAndGenderbyMovieId(fromServer, movie_id);
 		System.out.println("after getting people");
 	    while (actors.next()) {
 	    	String i = actors.getString("id");
 	        String n = actors.getString("name");
 	        String g = actors.getString("gender");
 	        
 	        //rotates name from lastname, first to first last
 	        if(n.contains(",")){
 	        	n = flipName(n);
 	        }
 	        
 	        //System.out.format("%-10s\t%4s%10s \n", i, n, g);
 	        query = "insert into person (pid, name, gender) values ('" + i + "','" + n + "','" + g + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted " + n + " into red dwarf person");
 	        }catch(SQLException e){
 	        	System.out.println("person: " + n + " already exists in person")	;        	
 	        }
 
 	    }
 	    
 	    //insert roles into characters(id,name)
 	    ResultSet roles = getRolesandId(fromServer, movie_id);
 	    while(roles.next()){
 	    	String i = roles.getString("id");
 	        String t = roles.getString("name");
 	        
 	        System.out.println("got roles id:" + i + " \t name: " + t);
 	        query = "insert into characters(rid, name) values ('" + i + "','" + t + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted " + t + " into red dwarf roles");
 	        }catch(SQLException e){
 	        	System.out.println("person: " + t + " already exists in roles")	;        	
 	        }	        
 	        
 	    }
 	    
 		//insert actors(id, foreign key(movie_id), foreign key(person_id), foreign_key(rid) into acts_in
 	    ResultSet actor_roles = getRolesinMoviesbyMovieId(fromServer, movie_id , 1); //returns actors
 	    while (actor_roles.next()) {
 	    	String p = actor_roles.getString("person_id");
 	        String r = actor_roles.getString("person_role_id");
 	        
 	        //System.out.format("%-10s\t%4s%10s \n", i, n, g);
 	        query = "insert into acts_in (mid, pid,rid) values ('" + movie_id + "','" + p + "','" + r + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted actor " + p + "/" + movie_id + "/" + r +"into red dwarf acts_in");
 	        }catch(SQLException e){
 	        	System.out.println("acts_in: actor " + p + "/" + movie_id + "/" + r);        	
 	        }
 	    }	    
 	    
 	    ResultSet actress_roles = getRolesinMoviesbyMovieId(fromServer, movie_id , 2); //returns actress
 	    while (actress_roles.next()) {
 	    	String p = actress_roles.getString("person_id");
 	        String r = actress_roles.getString("person_role_id");
 	        
 	        //System.out.format("%-10s\t%4s%10s \n", i, n, g);
 	        query = "insert into acts_in (mid, pid,rid) values ('" + movie_id + "','" + p + "','" + r + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted actor " + p + "/" + movie_id + "/" + r +"into red dwarf acts_in");
 	        }catch(SQLException e){
 	        	System.out.println("acts_in: actor " + p + "/" + movie_id + "/" + r);        	
 	        }
 	    }
 	    
 	    
 	    
 		//insert directors into is_director
 	    ResultSet directors = getDirectorsbyMovieId(fromServer, movie_id);
 	    while (directors.next()) {
 	    	String i = directors.getString("id");
 	        String n = directors.getString("name");
 	        
 	        //System.out.format("%-10s\t%4s%10s \n", i, n, g);
 	        query = "insert into is_director (mid, pid) values ('" + movie_id + "','" + i + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted " + n + " into red dwarf is_director");
 	        }catch(SQLException e){
 	        	System.out.println("person: " + n + " already exists in is_director")	;        	
 	        }
 	    }	    
 	    
 		//insert producers into is_producer
 	    ResultSet producers = getProducersbyMovieId(fromServer, movie_id);
 	    while (producers.next()) {
 	    	String i = producers.getString("id");
 	        String n = producers.getString("name");
 	        
 	        //System.out.format("%-10s\t%4s%10s \n", i, n, g);
 	        query = "insert into is_producer (mid, pid) values ('" + movie_id + "','" + i + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted " + n + " into red dwarf is_producer");
 	        }catch(SQLException e){
 	        	System.out.println("person: " + n + " already exists in is_producer")	;        	
 	        }
 	    }
 	    
 		//insert writers into is_writer
 	    ResultSet writers = getWritersbyMovieId(fromServer, movie_id);
 	    while (writers.next()) {
 	    	String i = writers.getString("id");
 	        String n = writers.getString("name");
 	        
 	        //System.out.format("%-10s\t%4s%10s \n", i, n, g);
 	        query = "insert into is_writer (mid, pid) values ('" + movie_id + "','" + i + "');";
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted " + n + " into red dwarf is_writer");
 	        }catch(SQLException e){
 	        	System.out.println("person: " + n + " already exists in is_writer")	;        	
 	        }
 	    }		
 	}
 
 	public static ResultSet getCompanyData(Statement fromServer, String movie_id) throws SQLException {
 		String query = "select MC.company_id, CN.name, CN.country_code from movie_companies as MC, company_name as CN where MC.movie_id = " + movie_id + " and MC.company_id = CN.id and MC.company_type_id = 2;";
 
 		return  fromServer.executeQuery(query);		
 	}
 	
 	
 	public static ResultSet getOpeningWeekendGrossData(Statement fromServer, String movie_id) throws SQLException {
 		String query = "select id,info from movie_info where info_type_id = 108 and info like '%USA%' and movie_id = " + movie_id + ";";	
 		return  fromServer.executeQuery(query);		
 	}
 	
 	public static ResultSet getWorldTotalGross(Statement fromServer, String movie_id) throws SQLException {
 		
 		//get worldwide total gross
 		String query = "select id,info from movie_info where info_type_id = 107 and info like '%Worldwide%' and movie_id = " + movie_id+";";
 		
 		return  fromServer.executeQuery(query);		
 	}
 	
 	public static ResultSet getUSATotalGross(Statement fromServer, String movie_id) throws SQLException {
 		
 		//get worldwide total gross
 		String query = "select id,info from movie_info where info_type_id = 107 and info like '%USA%' and movie_id = " + movie_id+";";
 		
 		return  fromServer.executeQuery(query);		
 	}
 	
 	public static String removeComma(String x){
 		
 		String tmp_array[] = x.split(",");
 		String z = "";
 		for(int y = 0; y < tmp_array.length ; y++){
 			z = z + tmp_array[y];
 		}
 		
 		return x;
 	}
 	
 	public static void insertBoxOfficeData(Statement fromServer, Statement toServer, String movie_id){
 		ResultSet weekendGrossData;
 		ResultSet totalGross;
 		
 		String openingID = "", totalID = "";
 		String openingInfo = "", totalInfo = "";
 		boolean weekendEmpty = true, grossEmpty = true;
 		
 		try{
 			weekendGrossData = getOpeningWeekendGrossData(fromServer, movie_id);
 			
 			//finds if weekendGross exists
 			if(weekendGrossData.last()){
 				int rowCount = weekendGrossData.getRow();
 				System.out.println("rowCount = " + rowCount);
 				weekendEmpty = false;
 				
 				openingID = weekendGrossData.getString("id");
 				openingInfo = weekendGrossData.getString("info");
 				openingInfo = openingInfo.substring(openingInfo.indexOf("$")+1, openingInfo.indexOf(" ") );
 				openingInfo = removeComma(openingInfo);
 				
 			}
 			else{
 				System.out.println("rowCount = 0");
 			}
 			
 			totalGross = getWorldTotalGross(fromServer, movie_id);
 			//finds if totalGross exists
 			if(totalGross.last()){ //not empty
 				int rowCount = totalGross.getRow();
 				System.out.println("worldGross rowCount = " + rowCount);
 				
 			}
 			else{ //worldwide gross empty
 				System.out.println("rowCount = 0");
 				totalGross = getUSATotalGross(fromServer, movie_id);
 				if(totalGross.last()){ //not empty
 					int rowCount = totalGross.getRow();
 					System.out.println("USAGross rowCount = " + rowCount);
 					
 					totalID = totalGross.getString("id");
 					totalInfo = totalGross.getString("info");
 					totalInfo = totalInfo.substring(totalInfo.indexOf("$")+1, totalInfo.indexOf(" ") );
 					totalInfo = removeComma(totalInfo);
 					
 					grossEmpty = false;
 				}
 				else{ //usa gross empty
 					
 				}
 			}			
 			String query = "", query2 = ""; 
 			if(weekendEmpty && grossEmpty){
 				System.out.println("no box office data");
 				return ;
 			}else{
 				if(weekendEmpty){ //no opening weekend data
 					query = "insert into box_office (id, opening_weekend_gross, total_gross) values ('" + totalID + "','" + openingInfo + "','" + totalInfo + "');";
 			        query2 = "insert into opened(mid, bid) values ('" + movie_id + "','" + totalID + "');";
 				}else{ //has opening weekend data
 					query = "insert into box_office (id, opening_weekend_gross, total_gross) values ('" + openingID + "','" + openingInfo + "','" + totalInfo + "');";
 					query2 = "insert into opened(mid, bid) values ('" + movie_id + "','" + openingID + "');";
 				}
 			}
 			
 			
 	        try{
 	        	toServer.executeUpdate(query);
 		        System.out.println("inserted into box_office");
 	        }catch(SQLException e){
 	        	System.out.println("box office: already exists in box_office");        	
 		        }
 			
 			//insert into opened
 	        try{
 	        	toServer.executeUpdate(query2);
 		        System.out.println("inserted into opened");
 	        }catch(SQLException e){
 	        	System.out.println("opened entry already exists for " + movie_id + " and " + openingID + " or " + totalID);	
 	        }
 			
 		}catch(SQLException e){
 			System.out.println("sql error");
 		}
 	}
 	
 	
 
 	public static void insertCompanyData(Statement fromServer, Statement toServer, String movie_id){
 	
 			ResultSet companies;
 			String company_name = "";
 			try{
 				companies = getCompanyData(fromServer, movie_id);
 				
 				while(companies.next()){
 					String id = companies.getString("company_id");
 					company_name = companies.getString("name");
 					String country_code = companies.getString("country_code");
 
 					System.out.println("inserting company: " + id + "/" + company_name + "/" + country_code );
 
 
 					String query = "insert into production_companies(id, name, location) values ('"  + id + "','" + company_name + "','" + country_code  + "');" ;
					//toServer.executeUpdate(query);
					System.out.println("inserted " + company_name + " into production_companies");
 					
 					try{
 						query = "insert into produced(mid, company_id) values ('"  + movie_id + "','" + id + "');" ;
 						toServer.executeUpdate(query);
 					}catch(SQLException e){
 						System.out.println("error inserting into produced for " + movie_id + " and " + company_name);
 					}
 					
 					
 					
 				}
 				
 			}catch(SQLException e){
 				System.out.println("company: " + company_name + " already exists in production_companies");
 			}
 			
 			//insert into produced table
 		
 
 	}			
 	
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println(args[0]);
 		try {
 		    Connection conFrom, conTo;
 		    Statement fromServer,toServer;
 		    String local_url = "jdbc:postgresql://blackbird.student.rit.edu/imdb";
 		    String red_dwarf_url = "jdbc:postgresql://reddwarf.cs.rit.edu/p48501b"; 
 		    // DATABASE CONNECTION MAGIC :-)
 		    
 		    //connection from blackbird
 			System.out.println("here");
 		    Class.forName("org.postgresql.Driver");
 			System.out.println("setting class");
 
 		    conFrom = DriverManager.getConnection(local_url, "paul", "imdb");
 			System.out.println("get connection");
 		    fromServer = conFrom.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
 		    	    ResultSet.CONCUR_READ_ONLY);
 		     
 		    System.out.println("connected to blackbird");
 
 			
 		    //connection to red dwarf
 		    Class.forName("org.postgresql.Driver");
 		    conTo = DriverManager.getConnection(red_dwarf_url, "p48501b", "heifohhitheihiqu");
 		    toServer = conTo.createStatement();
 		    System.out.println("connected to red dwarf");
 		    
 		    //movie file location
 				BufferedReader reader = new BufferedReader(new FileReader(args[0]));
 				//list of movie ids to be read in
 				Vector<String> movie_list =new Vector<String>();
 				
 				String line = null;
 				//reading movie ids
 				try {
 					while ((line = reader.readLine()) != null) {
 						System.out.println(line);
 						int divider = line.indexOf("|");
 						String movie_id = line.substring(1, divider-1);
 						String movie_name = line.substring(divider+1, line.length());
 						//System.out.println("\t id =" + movie_id);
 						//System.out.println("\t movie = " + movie_name);
 						movie_list.add(movie_id);
 						
 						
 					}
 					reader.close();
 					
 					Enumeration<String> listy = movie_list.elements();
 					
 					//inserting data into the red dwarf db
 					while(listy.hasMoreElements()){
 						//System.out.println("Inserting movie id: " + listy.nextElement());
 						String movieString = listy.nextElement();
 						insertPeopleandMovieData(fromServer, toServer, movieString);
 						//System.out.println("\nINSERTING INTO BOX OFFICE");
 						insertBoxOfficeData(fromServer, toServer, movieString);
 						//System.out.println("inserting into company");
 						insertCompanyData(fromServer, toServer, movieString);
 
 					}
 					
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					System.out.println("exception thrown");
 				}catch (StringIndexOutOfBoundsException e){
 					
 					System.out.println("out of bounds");
 				}
 		    
 		    
 		    //}
 		    
 		    System.out.println("closing connections");
 		    // DATABASE CLOSE/CLEANUP
 		    fromServer.close();
 		    conFrom.close();
 		    
 		    toServer.close();
 		    conTo.close();
 		}
 		catch(SQLException e) {
 			//e.printStackTrace();
 			System.out.println("caught sql exception on the outside");
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		
 		finally {
 
 		}
 		System.out.println("fin\n");
 	}
 
 }
