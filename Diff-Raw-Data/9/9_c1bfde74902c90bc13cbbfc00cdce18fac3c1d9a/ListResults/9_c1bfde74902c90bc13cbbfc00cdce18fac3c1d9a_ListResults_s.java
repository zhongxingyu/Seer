 package Fabflix;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 
 // Servlet implementation class listResults
 public class ListResults extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	public ListResults() {
 		super();
 	}
 	
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		if (Login.kickNonUsers(request, response)){return;}// kick if not logged in
		if (Login.kickNonAdmin(request, response)){return;}// kick if not admin
 
 		response.setContentType("text/html"); // Response mime type
 
 		// Output stream to STDOUT
 		PrintWriter out = response.getWriter();
 
 		ServletContext context = getServletContext();
 		HttpSession session = request.getSession();
 		try {
 
 			Connection dbcon = Database.openConnection();
 
 			String searchBy = request.getParameter("by");// title,letter,genre,year,director
 			String arg = request.getParameter("arg");// search string
 			String order = request.getParameter("order");// t_a,t_d,y_a,y_d
 			Integer page;
 			Integer resultsPerPage;
 
 			// ===Search By
 			try {
 				if (!(searchBy.equals("title") || searchBy.equals("letter") || searchBy.equals("genre") || searchBy.equals("year")
 						|| searchBy.equals("director") || searchBy.equals("first_name") || searchBy.equals("last_name"))) {
 					searchBy = "title";
 				}
 			} catch (NullPointerException e) {
 				searchBy = "title";
 			}
 
 			// ===Argument value
 			if (arg == null) {
 				arg = "";
 			}
 
 			if (searchBy.equals("letter")) {
 				if (arg.isEmpty()) {
 					arg = " ";
 				} else {
 					arg = arg.substring(0, 1);// Only take first character for
 					// character search
 				}
 			}
 
 			if (searchBy.equals("title")) {
 				try {
 					Pattern.compile(arg);
 				} catch (PatternSyntaxException exception) {
 					arg = "";
 				}
 			}
 
 			
 			// ===SORT
 			String sortBy = "";
 			try {
 				if (order.equals("t_d")) {
 					sortBy = "ORDER BY title DESC";
 				} else if (order.equals("y_d")) {
 					sortBy = "ORDER BY year DESC";
 				} else if (order.equals("y_a")) {
 					sortBy = "ORDER BY year";
 				} else {
 					sortBy = "ORDER BY title"; // DEFAULT to title ascending
 					order = "t_a";
 				}
 			} catch (NullPointerException e) {
 				sortBy = "ORDER BY title"; // DEFAULT to title ascending
 				order = "t_a";
 			}
 
 			// ===Paging
 			try {
 				page = Integer.valueOf(request.getParameter("page"));
 				if (page < 1) {
 					page = 1;
 				}
 			} catch (NumberFormatException e) {
 				page = 1;
 			} catch (NullPointerException e) {
 				page = 1;
 			}
 
 			// ===Results per page
 			try {
 				resultsPerPage = Integer.valueOf(request.getParameter("rpp"));
 				if (resultsPerPage < 1) {
 					resultsPerPage = 5;
 				}
 			} catch (NumberFormatException e) {
 				resultsPerPage = 5;
 			} catch (NullPointerException e) {
 				resultsPerPage = 5;
 			}
 
 			int listStart;
 			if (page > 0) {
 				listStart = (page - 1) * resultsPerPage;
 			} else {
 				listStart = 0;
 				page = 1;
 			}
 
 			String cleanArg = Database.cleanSQL(arg);//CLEAN FOR SQL
 			
 			// Declare our statement
 			Statement statement = dbcon.createStatement();
 			Statement fullStatement = dbcon.createStatement();
 			String query;
 			String fullQuery;// full search to count results
 			if (cleanArg.isEmpty()) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT * FROM movies) AS results";
 			} else if (searchBy.equals("genre")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres gr ON g.genre_id=gr.id WHERE name = '"
 						+ cleanArg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN genres_in_movies g ON g.movie_id=m.id LEFT OUTER JOIN genres gr ON g.genre_id=gr.id WHERE name = '"
 						+ cleanArg + "') as results";
 			} else if (searchBy.equals("letter")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE title REGEXP '^" + cleanArg + "' " + sortBy + " LIMIT " + listStart
 						+ "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '^" + cleanArg + "') as results";
 			} else if (searchBy.equals("title")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE title REGEXP '" + cleanArg + "' " + sortBy + " LIMIT " + listStart
 						+ "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE title REGEXP '" + cleanArg + "') as results";
 			} else if (searchBy.equals("first_name") || searchBy.equals("last_name")) {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "
 						+ searchBy + " = '" + cleanArg + "' " + sortBy + " LIMIT " + listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m LEFT OUTER JOIN stars_in_movies s ON movie_id=m.id LEFT OUTER JOIN stars s1 ON s.star_id=s1.id WHERE "
 						+ searchBy + " = '" + cleanArg + "') as results";
 			} else {
 				query = "SELECT DISTINCT m.id,title,year,director,banner_url FROM movies m WHERE " + searchBy + " = '" + cleanArg + "' " + sortBy + " LIMIT "
 						+ listStart + "," + resultsPerPage;
 				fullQuery = "SELECT count(*)  FROM (SELECT DISTINCT m.id FROM movies m WHERE " + searchBy + " = '" + cleanArg + "') as results";
 			}
 
 			
 			// Get results for this page's display
 			ResultSet searchResults = statement.executeQuery(query);
 
 			// Find total number of results
 			ResultSet fullCount = fullStatement.executeQuery(fullQuery);
 			fullCount.next();
 			int numberOfResults = fullCount.getInt(1);
 			int numberOfPages = numberOfResults / resultsPerPage + (numberOfResults % resultsPerPage == 0 ? 0 : 1);
 
 			// Adjust page if beyond scope of the results; redirect to last page
 			// of search
 			if (numberOfResults > 0 && page > numberOfPages) {
 				response.sendRedirect("ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + numberOfPages + "&rpp="
 						+ resultsPerPage + "&order=" + order);
 			}
 
 			// ===Start Writing Page===========================================
 
 			// TITLE
 
 			session.setAttribute("title", "Search by " + searchBy + ": " + arg);
 
 			out.println(Page.header(context, session));
 			// BODY
 
 			out.println("<H2>Search by " + searchBy + ": " + arg + "</H2><BR>");
 
 			if (numberOfResults > 0) {// if results exist
 				out.println("( " + numberOfResults + " Results )");
 				showRppOptions(out, searchBy, arg, order, page, resultsPerPage);
 				out.println("<BR><BR>");
 				if (numberOfPages > 1) {
 					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
 					out.println("<BR><BR>");
 				}
 				showSortOptions(out, searchBy, arg, order, page, resultsPerPage);
 				out.println("<BR>");
 			}
 
 			while (searchResults.next()) {// For each movie, DISPLAY INFORMATION
 				Integer movieID;
 				try {
 					movieID = Integer.valueOf(searchResults.getString("id"));
 				} catch (Exception e) {
 					movieID = 0;
 				}
 				String title = searchResults.getString("title");
 				Integer year = searchResults.getInt("year");
 				String bannerURL = searchResults.getString("banner_url");
 				String director = searchResults.getString("director");
 
 				out.println("<BR><a href=\"MovieDetails?id=" + movieID + "\"><h2>" + title + " (" + year + ")</h2><img src=\"" + bannerURL + "\" height=\"200\"></a><BR><BR>");
 
 				addToCart(out, movieID);
 
 				out.println("<BR><BR>ID: <a href=\"MovieDetails?id=" + movieID + "\">" + movieID + "</a><BR>");
 				listByYearLink(out, year, resultsPerPage);
 
 				out.println("<BR>");
 
 				listByDirectorLink(out, director, resultsPerPage);
 
 				out.println("<BR>");
 
 				listGenres(out, dbcon, resultsPerPage, movieID);
 
 				out.println("<BR>");
 
 				listStars(out, dbcon, resultsPerPage, movieID);
 
 				// String target = (String) session.getAttribute("user.dest");
 
 				out.println("<BR><BR><HR>");
 			}
 
 			if (numberOfResults > 0) {
 				// show prev/next
 				if (numberOfPages > 1) {
 					showPageControls(out, searchBy, arg, order, page, resultsPerPage, numberOfPages);
 					out.println("<BR>");
 				}
 
 				// Results per page Options
 				showRppOptions(out, searchBy, arg, order, page, resultsPerPage);
 
 				out.println("<BR>");
 
 			} else {
 				out.println("<H3>No Results.</H3>");
 			}
 
 			Page.footer(out, dbcon, resultsPerPage);
 
 			searchResults.close();
 			statement.close();
 			fullStatement.close();
 			dbcon.close();
 
 		} catch (SQLException ex) {
 			out.println(Page.header(context, session));
 			while (ex != null) {
 				out.println("SQL Exception:  " + ex.getMessage());
 				ex = ex.getNextException();
 			} // end while
 			out.println("</DIV></BODY></HTML>");
 		} // end catch SQLException
 		catch (java.lang.Exception ex) {
 			out.println(Page.header(context, session));
 			out.println("<P>SQL error in doGet: " + ex.getMessage() + "<br>"
 					+ ex.toString() + "</P></DIV></BODY></HTML>");
 			return;
 		}
 		out.close();
 	}
 
 	public static void addToCart(PrintWriter out, Integer movieID) {
 		out.println("<a href=\"cart?add=" + movieID + "\">Add to Cart</a>");
 	}
 
 	
 	public static void listByYearLink(PrintWriter out, Integer year) {
 		listByYearLink(out, year, 0);
 	}
 
 	public static void listByYearLink(PrintWriter out, Integer year, Integer rpp) {
 		out.println("Year: <a href=\"ListResults?by=year&arg=" + year + "&rpp=" + rpp + "\">" + year + "</a>");
 	}
 
 	public static void listByDirectorLink(PrintWriter out, String director) throws UnsupportedEncodingException {
 		listByDirectorLink(out, director, 0);
 	}
 
 	public static void listByDirectorLink(PrintWriter out, String director, Integer rpp) throws UnsupportedEncodingException {
 		out.println("Director: <a href=\"ListResults?by=director&arg=" + java.net.URLEncoder.encode(director, "UTF-8") + "&rpp=" + rpp + "\">" + director
 				+ "</a>");
 	}
 
 	private void showPageControls(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage, Integer numberOfPages)
 			throws UnsupportedEncodingException {
 		// ===Paging
 
 		if (page != 1) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=1&rpp=" + resultsPerPage
 					+ "&order=" + order + "\">First</a>");
 		} else {
 			out.println("Last");
 		}
 
 		out.println(" | ");
 
 		if (page > 1) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + (page - 1) + "&rpp="
 					+ resultsPerPage + "&order=" + order + "\">Prev</a>");
 		} else {
 			out.println("Prev");
 		}
 
 		out.println("| Page: " + page + " of " + numberOfPages + " |");
 
 		if (page >= numberOfPages) {
 			out.println("Next");
 		} else {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + (page + 1) + "&rpp="
 					+ resultsPerPage + "&order=" + order + "\">Next</a>");
 		}
 
 		out.println(" | ");
 
 		if (page < numberOfPages) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + numberOfPages + "&rpp="
 					+ resultsPerPage + "&order=" + order + "\">Last</a>");
 		} else {
 			out.println("Last");
 		}
 	}
 
 	private void showSortOptions(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage)
 			throws UnsupportedEncodingException {
 		// sorting and results per page options
 		out.println("Sort by: Title(");
 
 		if (!order.equals("t_a")) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
 					+ resultsPerPage + "&order=t_a\">asc</a>");
 		} else {
 			out.println("asc");
 		}
 
 		out.println(")(");
 
 		if (!order.equals("t_d")) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
 					+ resultsPerPage + "&order=t_d\">des</a>");
 		} else {
 			out.println("des");
 		}
 
 		out.println(") Year(");
 
 		if (!order.equals("y_a")) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
 					+ resultsPerPage + "&order=y_a\">asc</a>");
 		} else {
 			out.println("asc");
 		}
 
 		out.println(")(");
 
 		if (!order.equals("y_d")) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp="
 					+ resultsPerPage + "&order=y_d\">des</a>");
 		} else {
 			out.println("des");
 		}
 
 		out.println(")");
 	}
 
 	public static void searchTitlesBox(PrintWriter out) {
 		searchTitlesBox(out, 0);
 	}
 
 	public static void searchTitlesBox(PrintWriter out, Integer resultsPerPage) {
 		// ===Search Box
 		out.println("<FORM ACTION=\"ListResults\" METHOD=\"GET\">  Search Titles (RegEx): <INPUT TYPE=\"TEXT\" NAME=\"arg\">"
 				+ "<INPUT TYPE=\"HIDDEN\" NAME=rpp VALUE=\"" + resultsPerPage + "\"><INPUT TYPE=\"SUBMIT\" VALUE=\"Search\">");
 		AdvancedSearch.advancedSearchButton(out);
 		out.println("</FORM>");
 	}
 
 	private void showRppOptions(PrintWriter out, String searchBy, String arg, String order, Integer page, Integer resultsPerPage)
 			throws UnsupportedEncodingException {
 		// ===Results per page
 		out.println("Results per page: ");
 
 		if (!(resultsPerPage == 5)) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=5&order=" + order
 					+ "\">5</a>");
 		} else {
 			out.println("5");
 		}
 
 		if (!(resultsPerPage == 25)) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=25&order="
 					+ order + "\">25</a>");
 		} else {
 			out.println("25");
 		}
 
 		if (!(resultsPerPage == 100)) {
 			out.println("<a href=\"ListResults?by=" + searchBy + "&arg=" + java.net.URLEncoder.encode(arg, "UTF-8") + "&page=" + page + "&rpp=100&order="
 					+ order + "\">100</a>");
 		} else {
 			out.println("100");
 		}
 	}
 
 	public static void browseGenres(PrintWriter out, Connection dbcon) throws SQLException, UnsupportedEncodingException {
 		browseGenres(out, dbcon, 0);// Default results per page
 	}
 
 	public static void browseGenres(PrintWriter out, Connection dbcon, Integer resultsPerPage) throws SQLException, UnsupportedEncodingException {
 		Statement statement = dbcon.createStatement();
 		// ===GENRE browser
 		out.println("Browse Genres: <BR>");
 		int col = 0; // fix width of display
 		ResultSet allGenre = statement.executeQuery("SELECT DISTINCT name FROM genres g, genres_in_movies gi WHERE gi.genre_id=g.id ORDER BY name");
 		if (allGenre.next()) {
 			String genreName = allGenre.getString("name");
 			col += genreName.length();
 			out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
 			while (allGenre.next()) {
 				genreName = allGenre.getString("name");
 				col += genreName.length();
 				out.println(" | <a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
 				if (col >= 75 && allGenre.next()) { // column character width
 					genreName = allGenre.getString("name");
 					out.println("<br><a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>");
 					col = genreName.length();
 				}// 10 items per row
 			}
 		}
 		allGenre.close();
 		statement.close();
 	}
 	
 	public static String browseGenres(Integer resultsPerPage) throws SQLException, UnsupportedEncodingException, NamingException {
 		String rtn = "";
 		Connection dbcon = Database.openConnection();
 		Statement statement = dbcon.createStatement();
 		// ===GENRE browser
 		rtn += "Browse Genres: <BR>";
 		int col = 0; // fix width of display
 		ResultSet allGenre = statement.executeQuery("SELECT DISTINCT name FROM genres g, genres_in_movies gi WHERE gi.genre_id=g.id ORDER BY name");
 		if (allGenre.next()) {
 			String genreName = allGenre.getString("name");
 			col += genreName.length();
 			rtn += "<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>";
 			while (allGenre.next()) {
 				genreName = allGenre.getString("name");
 				col += genreName.length();
 				rtn += " | <a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>";
 				if (col >= 75 && allGenre.next()) { // column character width
 					genreName = allGenre.getString("name");
 					rtn += "<br><a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genreName, "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + genreName + "</a>";
 					col = genreName.length();
 				}// 10 items per row
 			}
 		}
 		allGenre.close();
 		statement.close();
 		dbcon.close();
 		return rtn;
 	}
 
 	public static void browseTitles(PrintWriter out) throws UnsupportedEncodingException {
 		browseTitles(out, 0);// Default results per page
 	}
 
 	public static void browseTitles(PrintWriter out, Integer resultsPerPage) throws UnsupportedEncodingException {
 		// ===Letter Browser
 		out.println(browseTitles(resultsPerPage));
 	}
 	
 	public static String browseTitles(Integer resultsPerPage) throws UnsupportedEncodingException{
 		String rtn = "Browse Titles: <BR>";
 		String alphaNum = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 		for (int i = 0; i < alphaNum.length(); i++) {
 			if (i != 0) {
 				rtn += " - ";
 			}
 			rtn += "<a href=\"ListResults?by=letter&arg=" + java.net.URLEncoder.encode(alphaNum.substring(i,i+1), "UTF-8") + "&page=1&rpp=" + resultsPerPage + "\">" + alphaNum.charAt(i) + "</a>";
 		}
 		return rtn;
 	}
 
 	public static void listStars(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException {
 		listStars(out, dbcon, 0, movieID);// Default results per page
 	}
 
 	public static void listStars(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException {
 		Statement statement = dbcon.createStatement();
 		// ===STARS; comma separated list
 		out.println("Stars: ");
 		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
 				+ "AND s.star_id=s1.id " + "AND m.id = '" + movieID + "' ORDER BY last_name");
 		if (stars.next()) {
 			String starName = stars.getString("first_name") + " " + stars.getString("last_name");
 			String starID = stars.getString("star_id");
 			out.println("<a href=\"StarDetails?id=" + starID + "\">" + starName + "</a>");
 			while (stars.next()) {
 				starName = stars.getString("first_name") + " " + stars.getString("last_name");
 				starID = stars.getString("star_id");
 				out.println(", <a href=\"StarDetails?id=" + starID + "\">" + starName + "</a>");
 			}
 		}
 		stars.close();
 		statement.close();
 	}
 
 	public static void listStarsIMG(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException {
 		listStarsIMG(out, dbcon, 0, movieID, false);
 	}
 
 	public static void listStarsIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException {
 		listStarsIMG(out, dbcon, rpp, movieID, false);
 	}
 
 	public static void listStarsIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID, Boolean edit) throws SQLException {
 		Statement statement = dbcon.createStatement();
 		// ===STARS; list of images
 		out.println("Stars: ");
 		if (edit) {
 			EditMovie.addStarGenreLink(out, movieID, "star");
 		}
 		out.println("<BR><BR>");
 		ResultSet stars = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 WHERE s.movie_id=m.id AND s.star_id=s1.id AND m.id = '" + movieID
 						+ "' ORDER BY last_name");
 		while (stars.next()) {
 			String starName = stars.getString("first_name") + " " + stars.getString("last_name");
 			String starIMG = stars.getString("photo_url");
 			int starID = stars.getInt("star_id");
 			out.println("<a href=\"StarDetails?id=" + starID + "\">" + "<img src=\"" + starIMG + "\" height=\"120\">" + starName + "</a>");
 			if (edit) {
 				EditMovie.removeStarGenreLink(out, movieID, starID, "star", starName);
 			}
 			out.println("<BR><BR>");
 		}
 		stars.close();
 		statement.close();
 	}
 
 	public static void listMoviesIMG(PrintWriter out, Connection dbcon, Integer starID) throws SQLException {
 		listMoviesIMG(out, dbcon, 0, starID);
 	}
 
 	public static void listMoviesIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer starID) throws SQLException {
 		
 	}
 	public static void listMoviesIMG(PrintWriter out, Connection dbcon, Integer rpp, Integer starID, Boolean edit) throws SQLException {
 		Statement statement = dbcon.createStatement();
 		out.println("Starred in:");
 		if (edit) {
 			EditStar.addMovieLink(out, starID, "movie");
 		}
 		out.println("<BR><BR>");
 		ResultSet movies = statement.executeQuery("SELECT DISTINCT * FROM movies m, stars_in_movies s, stars s1 " + "WHERE s.movie_id=m.id "
 				+ "AND s.star_id=s1.id " + "AND s1.id = '" + starID + "' ORDER BY year DESC");
 
 		while (movies.next()) {
 			String title = movies.getString("title");
 			Integer year = movies.getInt("year");
 			Integer movieID = movies.getInt("movie_id");
 			String bannerURL = movies.getString("banner_url");
 
 			out.println("<a href=\"MovieDetails?id=" + movieID + "\"><img src=\"" + bannerURL + "\" height=\"200\">" + title + " (" + year + ")" + "</a>");
 			if (edit){
 				EditStar.removeMovieLink(out, starID, movieID, title + " ("+year+")");
 			}else{
 				out.println(" (");
 				ListResults.addToCart(out, movieID);
 				out.println(")");
 			}
 			out.println("<BR><BR>");
 		}
 
 	}
 
 	public static void listGenres(PrintWriter out, Connection dbcon, Integer movieID) throws SQLException, UnsupportedEncodingException {
 		listGenres(out, dbcon, 0, movieID, false);// Default results per page
 	}
 
 	public static void listGenres(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID) throws SQLException, UnsupportedEncodingException {
 		listGenres(out, dbcon, rpp, movieID, false);
 	}
 
 	public static void listGenres(PrintWriter out, Connection dbcon, Integer rpp, Integer movieID, Boolean edit) throws SQLException,
 			UnsupportedEncodingException {
 		// ===GENRES; comma separated list
 		out.println("Genre: ");
 		if (edit) {
 			EditMovie.addStarGenreLink(out, movieID, "genre");
 			out.println("<BR>");
 		}
 		Statement statement = dbcon.createStatement();
 		ResultSet genres = statement.executeQuery("SELECT DISTINCT name,genre_id FROM movies m, genres_in_movies g, genres g1 WHERE g.movie_id=m.id AND g.genre_id=g1.id AND m.id ='"
 						+ movieID + "' ORDER BY name");
 		if (genres.next()) {
 			String genre = genres.getString("name").trim();
 			Integer delID = genres.getInt("genre_id");
 			out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genre, "UTF-8") + "&rpp=" + rpp + "\">" + genre + "</a>");
 			if (edit) {
 				EditMovie.removeStarGenreLink(out, movieID, delID, "genre", genre);
 				out.println("<BR>");
 			}
 			while (genres.next()) {
 				genre = genres.getString("name").trim();
 				delID = genres.getInt("genre_id");
 				if (!edit) {
 					out.println(", ");
 				}
 				out.println("<a href=\"ListResults?by=genre&arg=" + java.net.URLEncoder.encode(genre, "UTF-8") + "&rpp=" + rpp + "\">" + genre + "</a>");
 				if (edit) {
 					EditMovie.removeStarGenreLink(out, movieID, delID, "genre", genre);
 					out.println("<BR>");
 				}
 			}
 		}
 		genres.close();
 		statement.close();
 	}
 
 }
