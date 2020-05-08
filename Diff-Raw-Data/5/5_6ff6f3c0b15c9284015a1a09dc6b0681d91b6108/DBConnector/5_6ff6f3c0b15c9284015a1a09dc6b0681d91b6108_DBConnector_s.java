 /**
  * @contributor(s): Freerider Team (Group 4, IT2901 Fall 2012, NTNU)
  * @version: 		1.0
  *
  * Copyright (C) 2012 Freerider Team.
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  */
 package no.ntnu.idi.freerider.backend;
 
 
 import java.net.InetAddress;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import no.ntnu.idi.freerider.model.Car;
 import no.ntnu.idi.freerider.model.Journey;
 import no.ntnu.idi.freerider.model.Location;
 import no.ntnu.idi.freerider.model.MapLocation;
 import no.ntnu.idi.freerider.model.Notification;
 import no.ntnu.idi.freerider.model.NotificationType;
 import no.ntnu.idi.freerider.model.Route;
 import no.ntnu.idi.freerider.model.TripPreferences;
 import no.ntnu.idi.freerider.model.User;
 import no.ntnu.idi.freerider.model.Visibility;
 
 import org.postgis.LineString;
 import org.postgis.PGgeometry;
 import org.postgis.Point;
 
 /** A utility class for connecting to the project's database */
 public class DBConnector {
 	private static final String JDBC_DRIVER = "org.postgresql.Driver";
 	private static final String DRIVER_WRAPPER = "org.postgis.DriverWrapper";
 
 	private Connection conn;
 
 	/** Attempt to create a connection to the server.
 	 * @throws SQLException if connection fails.*/
 	public void init() throws SQLException{
 		try {
 			Class.forName(JDBC_DRIVER);
 			Class.forName(DRIVER_WRAPPER);
 		} catch (ClassNotFoundException e) {
 			System.err.println("Could not find JDBC driver.");
 			e.printStackTrace();
 		}
 		try {
 			conn = DriverManager.getConnection(DBConfigurator.getDatabaseUrl(),DBConfigurator.getDatabaseUsername(), DBConfigurator.getDatabasePassword());
 		} catch (SQLException e) {
 			System.err.println("Error establishing database connection.");
 			e.printStackTrace();
 			throw e;
 		}
 	}
 
 	//========================  Things to do with Routes ====================// 
 
 	/** Save a Route, then return it with its new, correct serial value. */
 	public Route addRoute(Route route) throws SQLException{
 
 		
 		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO routes(name,route, owner,maplocations,addresses,date_modified) VALUES (?,?,?,?,?,?)");
 		pstmt.setString(1, route.getName());
 		LineString test = route.getRouteDataAsLineString();
 		pstmt.setObject(2, test ,Types.OTHER);
 		pstmt.setString(3, route.getOwner().getID());
 		pstmt.setObject(4, route.getMapPointsAsLineString(),Types.OTHER);
 		pstmt.setString(5, route.getAddressString());
 		java.util.Date now = new java.util.Date();
 		Date sqlNow = new Date(now.getTime());
 		pstmt.setDate(6, sqlNow);
 		pstmt.executeUpdate();
 		return getRoute(route.getName(),route.getOwner().getID());
 	}
 
 	/** Save a Route as an ad-hoc Route and return it. 
 	 * The Route will not appear in GET_ROUTES requests.
 	 * Intended for Routes the user only expects to take once.
 	 */
 	public Route addAdhocRoute(Route route) throws SQLException {
 		PreparedStatement pstmt = conn.prepareStatement("SELECT nextval('ad_hoc_routes')");
 		ResultSet rs = pstmt.executeQuery();
 		rs.next();
 		String name = "ADHOC:" + rs.getInt(1);
 		rs.close();
 		pstmt.close();
 		pstmt = conn.prepareStatement("INSERT INTO routes(name,route, owner,maplocations,addresses,date_modified,ad_hoc) VALUES (?,?,?,?,?,?,true)");
 		pstmt.setString(1, name);
 		LineString test = route.getRouteDataAsLineString();
 		pstmt.setObject(2, test ,Types.OTHER);
 		pstmt.setString(3, route.getOwner().getID());
 		pstmt.setObject(4, route.getMapPointsAsLineString(),Types.OTHER);
 		pstmt.setString(5, route.getAddressString());
 		java.util.Date now = new java.util.Date();
 		Date sqlNow = new Date(now.getTime());
 		pstmt.setDate(6, sqlNow);
 		pstmt.executeUpdate();
 		return getRoute(name,route.getOwner().getID());
 	}
 
 	public Route updateRoute(Route route) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT EXISTS (SELECT 1 FROM journeys WHERE route_used=? AND starttime >= (current_timestamp + interval'-1d'))");
 		stmt.setInt(1, route.getSerial());
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		if(rs.getBoolean(1)){
 			throw new SQLException("Cannot alter a route with active journeys.");
 		}
 		rs.close();
 		stmt.close();
 		stmt = conn.prepareStatement("UPDATE routes SET name=?, route=?, maplocations=?, addresses=? WHERE serial=?");
 		stmt.setString(1, route.getName());
 		stmt.setObject(2, route.getRouteDataAsLineString(),Types.OTHER);
 		stmt.setObject(3, route.getMapPointsAsLineString(),Types.OTHER);
 		stmt.setString(4, route.getAddressString());
 		stmt.setInt(5, route.getSerial());
 		stmt.executeUpdate();
 		return getShortformRoute(route.getSerial());
 	}
 
 	public Route getRoute(String name, String ownerID) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT name, route::geometry, owner, serial,maplocations::geometry,addresses,frequency FROM routes WHERE name=? AND owner=?");
 		stmt.setString(1, name);
 		stmt.setString(2,ownerID);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()){
 			return null;
 		}
 		Route ret = readRoute(rs);
 		return ret;
 
 	}
 
 	public Route getShortformRoute(String name, String ownerID) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT name, owner, serial,maplocations::geometry,addresses,frequency FROM routes WHERE name=? AND owner=?");
 		stmt.setString(1, name);
 		stmt.setString(2,ownerID);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()){
 			return null;
 		}
 		Route ret = readShortFormRoute(rs);
 		return ret;
 
 	}
 
 	public Route getRoute(int serial) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT name, route::geometry, owner, serial,maplocations::geometry,addresses,frequency FROM routes WHERE serial=?");
 		stmt.setInt(1, serial);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()){
 			return null;
 		}
 		return readRoute(rs);
 	}
 
 	public Route getShortformRoute(int serial) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT name, owner, serial,maplocations::geometry,addresses,frequency FROM routes WHERE serial=?");
 		stmt.setInt(1, serial);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()){
 			return null;
 		}
 		return readShortFormRoute(rs);
 	}
 
 	/** Read a route from a result set, in full. */
 	private Route readRoute(ResultSet rs) throws SQLException {
 		Route ret = readShortFormRoute(rs);
 		List<Location> routeData = readRouteData(rs);
 		ret.setRouteData(routeData);
 		return ret;
 	}
 
 	/** Read a route from a result set, in short form (i.e. without route but with maplocations data). */
 	private Route readShortFormRoute(ResultSet rs) throws SQLException {
 		List<Location> routeData = new ArrayList<Location>();
 		User user = getUser(rs.getString("owner"));
 		Route ret = new Route(user, rs.getString("name"), routeData, rs.getInt("serial"));
 		String addressList = rs.getString("addresses");
 		String[] addresses = addressList.split(Route.ADDRESS_STRING_DELIMITER);
 		List<MapLocation> mapLocations = readMapLocationData(rs, addresses);
 		ret.setFrequency(rs.getInt("frequency"));
 		//ServerLogger.write("" + ret.getFrequency());
 		ret.setMapPoints(mapLocations);
 		return ret;
 	}
 
 	public List<Route> getRoutes(String ownerID) throws SQLException {
 		ServerLogger.write("getRoutes TOP");
 		List<Route> ret = new ArrayList<Route>();
 		//Changed
 		PreparedStatement stmt = conn.prepareStatement("SELECT name, route::geometry, owner, serial, maplocations::geometry,addresses,frequency FROM routes WHERE owner=? AND ad_hoc=false ORDER BY frequency");
 		stmt.setString(1,ownerID);
 		ResultSet rs = stmt.executeQuery();
 		ServerLogger.write("Before while");
 		while(rs.next()){
 			List<Location> routeData = readRouteData(rs);
 			User user = getUser(rs.getString("owner"));
 			Route toAdd = new Route(user, rs.getString("name"), routeData, rs.getInt("serial"));
 			String[] addresses = rs.getString("addresses").split(Route.ADDRESS_STRING_DELIMITER); 
 			List<MapLocation> mapLocations = readMapLocationData(rs, addresses);
 			toAdd.setMapPoints(mapLocations);
 			toAdd.setFrequency(rs.getInt("frequency"));
 			ServerLogger.write("Before try");
 			ServerLogger.write("After setFreq");
 			ret.add(toAdd);
 			ServerLogger.write("After ret.add(toadd)");
 		}
 		return ret;
 	}
 	public List<SimpleRoute> getAllSimpleRoutes() throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT serial,date_modified FROM routes");
 		ResultSet rs = stmt.executeQuery();
 		ArrayList<SimpleRoute> ret = new ArrayList<SimpleRoute>();
 		
 		while(rs.next()){
 			int serial = rs.getInt("serial");
 			java.sql.Date date = rs.getDate("date_modified");
 			ret.add(new SimpleRoute(serial, date));
 		}
 		return ret;
 	}
 
 
 
 public void deleteRouteBySerial(int serial) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("DELETE FROM routes WHERE serial=?");
 		stmt.setInt(1, serial);
 		if(stmt.executeUpdate() !=1){
 			throw new SQLException("No such route found.");
 		}
 	}
 
 	public void deleteTestRoute() throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("DELETE from routes WHERE name='testroute' AND owner='bobbytables'"); //LOL
 		stmt.execute();
 	}
 
 	/**This is a workaround for a problem with the PostGIS JDBC driver.
 	 * It can save geography as postGIS objects, but insists on retrieving them
 	 * as postgreSQL PGobjects. For now either cast them to geometry in the SQL query
 	 * (allowing them to be retrieved as PGgeometry objects),
 	 * or parse the values out of the ST_AsText() result.
 	 * @param rs A ResultSet containing PGgeometry objects, under the column name "route". 
 	 * @return The List<Location> representation of the route data.
 	 * @throws SQLException if called on a closed ResultSet, or one not containing a route
 	 * @see http://trac.osgeo.org/postgis/ticket/530
 	 */
 
 	private List<Location> readRouteData(ResultSet rs) throws SQLException {
 		PGgeometry routeLine = (PGgeometry) rs.getObject("route");
 		LineString line = (LineString) routeLine.getGeometry();
 		List<Location> routeData = new ArrayList<Location>();
 		for(Point point : line.getPoints()){
 			routeData.add(new Location(point.x, point.y));
 		}
 		return routeData;
 	}
 
 	private List<MapLocation> readMapLocationData(ResultSet rs, String[] addresses) throws SQLException {
 		PGgeometry mapLocationLine = (PGgeometry) rs.getObject("maplocations");
 		LineString line = (LineString) mapLocationLine.getGeometry();
 		Point[] points = line.getPoints();
 		List<MapLocation> routeData = new ArrayList<MapLocation>();
 		try{
 			for (int i = 0; i < points.length; i++) {
 				routeData.add(new MapLocation(points[i].getX(),points[i].getY(),addresses[i]));
 			}
 		}catch(IndexOutOfBoundsException e){
 			throw new SQLException("Route MapLocations and addresslist are inconsistent.");
 		}
 		return routeData;
 	}
 
 	private static Point convertToPoint(Location point) {
 		if(point==null) return null;
 		return new Point(point.getLatitude(), point.getLongitude());
 	}
 
 	private static Location convertToLocation(PGgeometry geom){
 		if(geom==null) return null;
 		Point point = (Point) geom.getGeometry();
 		return new Location(point.getX(),point.getY());
 	}
 
 	public void deleteRoute(Route route) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("DELETE FROM routes WHERE serial=?");
 		stmt.setInt(1, route.getSerial());
 		if(stmt.executeUpdate() !=1){
 			throw new SQLException("No such route found.");
 		}
 	}
 
 
 
 	// ============================= Things to do with Users ===============================//
 
 	public User getUser(String ID) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id=?");
 		stmt.setString(1, ID);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()){
 			return null;			
 		}
 		User ret = new User(rs.getString("name"), ID);
 		ret.setSurname(rs.getString("surname"));
 		ret.setIp((InetAddress) rs.getObject("ip"));
 		ret.setRating(rs.getDouble("rating"));
 		ret.setGender(rs.getString("gender"));
 		ret.setAbout(rs.getString("about"));
 		ret.setAge(rs.getInt("age"));
 		ret.setPhone(rs.getString("phone"));
 		ret.setCarId(rs.getInt("carid"));
 		return ret;
 	}
 	
 	public void updateUser(User user) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE users SET (rating,gender,phone,age,about) = (?,?,?,?,?) WHERE id=?");
 		stmt.setDouble(1, user.getRating());
 		stmt.setString(2, user.getGender());
 		stmt.setString(3, user.getPhone());
 		stmt.setInt(4, user.getAge());
 		stmt.setString(5, user.getAbout());
 		stmt.setString(6, user.getID());
 		stmt.executeUpdate();
 		
 	}
 	public void loginUser(User user) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE users SET (name, surname, gender) = (?,?,?) WHERE id=?");
 		stmt.setString(1, user.getFirstName());
 		stmt.setString(2, user.getSurname());
 		stmt.setString(3, user.getGender());
 		stmt.setString(4, user.getID());
 		stmt.executeUpdate();
 	}
 	public void addUser(User newUser) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(name, surname, id, rating, gender) VALUES (?,?,?,0,?)");
 		stmt.setString(1, newUser.getFirstName());
 		stmt.setString(2, newUser.getSurname());
 		stmt.setString(3, newUser.getID());
 		stmt.setString(4, newUser.getGender());
 		stmt.executeUpdate();
 	}
 
 	public void deleteTestUser() throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id='testuser'");
 		stmt.executeUpdate();
 	}
 
 	public String getAccessToken(String searcherID) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT access_token FROM users WHERE id=?");
 		stmt.setString(1, searcherID);
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		return rs.getString(1);
 	}
 
 	public void setAccessToken(String userID,String token) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("UPDATE users SET access_token=? WHERE id=?");
 		stmt.setString(1, token);
 		stmt.setString(2, userID);
 		stmt.executeUpdate();
 	}
 
 	//==============================  Things to do with Journeys  ===========================//
 
 
 	/** Search for Journeys matching given preferences.  */
 	public List<Journey> search(Location start, Location stop, Calendar time, User searcher) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM search_for_journeys(?::geography,?::geography,?,?)");
 		ServerLogger.write("Search: " + "SELECT * FROM search_for_journeys(" + Double.toString(start.getLatitude()) + "::geography," + Double.toString(start.getLongitude()) + "::geography," + searcher.getID() + "," + convertToTimestamp(time) + ")");
 		stmt.setObject(1, new Point(start.getLatitude(),start.getLongitude()),Types.OTHER);
 		stmt.setObject(2, new Point(stop.getLatitude(),stop.getLongitude()),Types.OTHER);
 		stmt.setString(3, searcher.getID());
 		stmt.setTimestamp(4, convertToTimestamp(time));
 		ResultSet rs = stmt.executeQuery();
 		List<Journey> ret = new ArrayList<Journey>();
 		while(rs.next()){
 			ServerLogger.write("Got result");
 			Journey journey = new Journey(rs.getInt("journeyserial"));
 			User user = new User(rs.getString("Name"),rs.getString("id"));
 			user.setSurname(rs.getString("Surname"));
 			user.setRating(rs.getDouble("Rating"));
 			//journey.setHitchhiker(user);
 			journey.setHitchhikers(getHitchhikers(rs.getInt("journeyserial")));
 			List<Location> routeData = readRouteData(rs);
 			user = getUser(user.getID());
 			Route route = new Route(user, "", routeData, rs.getInt("routeserial"));
 			String[] addresses = rs.getString("addresses").split(Route.ADDRESS_STRING_DELIMITER);
 			List<MapLocation> mapPoints = readMapLocationData(rs, addresses);
 			route.setMapPoints(mapPoints);
 			//route.setFrequency(rs.getInt("frequency"));
 			journey.setRoute(route);
 			journey.setStart(convertToCalendar(rs.getTimestamp("starttime")));
 			journey.setVisibility(Visibility.valueOf(rs.getString("visibility")));
			TripPreferences pref = new TripPreferences(7, true, true, true, true, true);
			pref.setPrefId(2); //Hardkoda bullshit
			journey.setTripPreferences(pref);
 			ret.add(journey);
 		}
 		return ret;
 	}
 
 
 	private static Calendar convertToCalendar(Timestamp timestamp){
 		Calendar ret = new GregorianCalendar();
 		ret.setTime(timestamp);
 		return ret;
 	}
 
 	private static Timestamp convertToTimestamp(Calendar calendar){
 		return new Timestamp(calendar.getTimeInMillis());
 	}
 
 
 	public Journey addJourney(Journey journey) throws SQLException{
 		//Insertion
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO journeys(route_used, starttime, preferenceid, hitchhiker, visibility) VALUES (?, ?, ?, NULL,?::visibility);");
 		stmt.setInt(1, journey.getRoute().getSerial());
 		stmt.setTimestamp(2, convertToTimestamp(journey.getStart()));
 		int prefId = createPreference(journey.getTripPreferences());
 		stmt.setInt(3, prefId);
 		stmt.setString(4, journey.getVisibility().toString());
 		stmt.executeUpdate();
 		stmt.close();
 
 		//Retrieve serial and return the newly inserted Journey.
 		stmt = conn.prepareStatement("SELECT serial FROM journeys WHERE route_used = ? AND starttime = ?");
 		stmt.setInt(1, journey.getRoute().getSerial());
 		stmt.setTimestamp(2, convertToTimestamp(journey.getStart()));
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()) return null;
 		int serial = rs.getInt("serial");
 		Journey ret = new Journey(serial);
 		ret.setRoute(getShortformRoute(journey.getRoute().getSerial()));
 		ret.setStart(journey.getStart());
 		ret.setVisibility(journey.getVisibility());
 		ret.setTripPreferences(journey.getTripPreferences());
 		rs.close();
 		stmt.close();
 		setDateModified(journey.getRoute());
 		incrementFrequency(journey.getRoute());
 		ServerLogger.write("END Add journey");
 		return ret;
 	}
 
 	public Journey updateJourney(Journey journey) throws SQLException {
 		//if(getHitchhikerID(journey.getSerial()) != null) throw new SQLException("Cannot alter Journey with assigned hitchhiker.");
 		PreparedStatement stmt = conn.prepareStatement("UPDATE journeys SET route_used=?, starttime=?, visibility=?::visibility WHERE serial=?");
 		stmt.setInt(1, journey.getRoute().getSerial());
 		stmt.setTimestamp(2, convertToTimestamp(journey.getStart()));
 		stmt.setString(3, journey.getVisibility().toString());
 		stmt.setInt(4, journey.getSerial());
 		stmt.executeUpdate();
 		updatePreference(journey.getTripPreferences());
 		return getShortformJourney(journey.getSerial());
 	}
 
 
 	public void deleteJourneyWithoutCheck(Journey journey) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("DELETE FROM journeys WHERE serial=?");
 		stmt.setInt(1, journey.getSerial());
 		if(stmt.executeUpdate() !=1){
 			throw new SQLException("No such journey found.");
 		}
 	}
 
 	public void deleteJourney(Journey journey) throws SQLException {
 		//if(getHitchhikerID(journey.getSerial())!=null) throw new SQLException("Cannot delete journey with assigned hitchhiker. Send them a notification of cancellation instead.");
 		deleteJourneyWithoutCheck(journey);
 	}
 
 	private final static String GETJOURNEYS_SQL = "SELECT DISTINCT ON (journeys.serial)" +
 			" route_used," +
 			" journeys.serial," +
 			" starttime," +
 			" hitchhiker," +
 			" preferenceid," +
 			" hitchhikers.journeyid," +
 			" hitchhikers.userid," +
 			" visibility " +
 			"FROM journeys, routes, hitchhikers " +
 			"WHERE " +
 			" route_used=routes.serial" +
 			" AND (" +
 			"	routes.owner=?" +
 			" 	OR (journeys.serial=hitchhikers.journeyid AND hitchhikers.userid=?) " +
 			"	) " +
 			" AND NOT EXISTS ( " +
 			"	SELECT 1 " +
 			"	FROM notifications" +
 			"	WHERE concerning_journey=journeys.serial AND " +
 			"	type='DRIVER_CANCEL'::notification_type" +
 			" )";
 	/** Get every Journey involving a given User, in short form.
 	 * This includes every Journey he owns, and ones he is the hitchhiker for. 
 	 * @param user The user in question.
 	 * @return
 	 * @throws SQLException 
 	 */
 	public List<Journey> getJourneys(User user) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement(GETJOURNEYS_SQL);
 		stmt.setString(1, user.getID());
 		stmt.setString(2, user.getID());
 		ResultSet rs = stmt.executeQuery();
 		List<Journey> ret = new ArrayList<Journey>();
 		while(rs.next()){
 			Journey journey = readShortformJourney(rs);
 			ret.add(journey);
 		}
 		
 		return ret;
 	}
 
 	public Journey getJourney(int serial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT serial, route_used,starttime,hitchhiker,visibility,preferenceid FROM journeys WHERE serial=?");
 		stmt.setInt(1, serial);
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		return readJourney(rs);
 	}
 
 	public Journey getShortformJourney(int serial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT serial, route_used,starttime,hitchhiker,visibility,preferenceid FROM journeys WHERE serial=?");
 		stmt.setInt(1, serial);
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		return readShortformJourney(rs);
 	}
 
 	private Journey readShortformJourney(ResultSet rs) throws SQLException {
 		int serial = rs.getInt("serial");
 		Route route = getShortformRoute(rs.getInt("route_used"));
 		Calendar start = convertToCalendar(rs.getTimestamp("starttime"));
 		//User hitchhiker = getUser(rs.getString("hitchhiker"));
 		Visibility visibility = Visibility.valueOf(rs.getString("visibility"));
 		List<User> hitchhikers = getHitchhikers(serial);
 		Journey journey = new Journey(serial, route, start, hitchhikers, visibility);
 		TripPreferences preference = getPreference(rs.getInt("preferenceid"));
 		journey.setTripPreferences(preference);
 		ServerLogger.write("Num hitch: " + hitchhikers.size());
 		return journey;
 	}
 	public List<User> getHitchhikers(int journeySerial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM hitchhikers WHERE journeyid=?");
 		stmt.setInt(1, journeySerial);
 		ResultSet rs = stmt.executeQuery();
 		List<User> ret = new ArrayList<User>();
 		while(rs.next()){
 			ret.add(getUser(rs.getString("userid")));
 		}
 		return ret;
 	}
 
 	private Journey readJourney(ResultSet rs) throws SQLException {
 		int serial = rs.getInt("serial");
 		Route route = getRoute(rs.getInt("route_used"));
 		Calendar start = convertToCalendar(rs.getTimestamp("starttime"));
 		//User hitchhiker = getUser(rs.getString("hitchhiker"));
 		Visibility visibility = Visibility.valueOf(rs.getString("visibility"));
 		List<User> hitchhikers = getHitchhikers(serial);
 		Journey journey = new Journey(serial, route, start, hitchhikers, visibility);
 		
 		TripPreferences preference = getPreference(rs.getInt("preferenceid"));
 		journey.setTripPreferences(preference);
 		return journey;
 	}
 /*
 	public void addHitchhiker(String hikerID, int journeySerial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE journeys SET hitchhiker=? WHERE serial=?");
 		stmt.setString(1, hikerID);
 		stmt.setInt(2, journeySerial);
 		if(stmt.executeUpdate() != 1) throw new SQLException("No such journey found.");
 	}
 */
 	public void addHitchhiker(String hikerID, int journeySerial) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO hitchhikers(journeyid,userid) VALUES(?,?)");
 		stmt.setInt(1, journeySerial);
 		stmt.setString(2, hikerID);
 		stmt.executeUpdate();
 	}
 	/*
 	public void removeHitchhiker(int journeySerial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE journeys SET hitchhiker=NULL WHERE serial=?");
 		stmt.setInt(1, journeySerial);
 		if(stmt.executeUpdate() != 1){
 			throw new SQLException("Failed to NULL hitchhiker of journey " + journeySerial);
 		}
 	}
 */
 	public void removeHitchhiker(String hikerID, int journeySerial) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("DELETE FROM hitchhikers WHERE journeyid=? AND userid=?");
 		stmt.setInt(1, journeySerial);
 		stmt.setString(2, hikerID);
 		stmt.executeUpdate();
 	}
 	public void incrementSeats(int journeySerial, int value) {
 		try {
 			Journey j = getJourney(journeySerial);
 			j.getTripPreferences().setSeatsAvailable(j.getTripPreferences().getSeatsAvailable() + value);
 			updatePreference(j.getTripPreferences());
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			ServerLogger.write("SQLError:" + e.getMessage());
 		}
 	}
 	public boolean hasAvailableSeats(int journeySerial) {
 		try {
 			Journey j = getJourney(journeySerial);
 			if(j.getTripPreferences().getSeatsAvailable() > 0) {
 				return true;
 			} else {
 				return false;
 			}
 		} catch(SQLException e){
 			return false;
 		}
 	}
 	public String getJourneyDriverID(int serial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT routes.owner FROM journeys, routes WHERE journeys.route_used=routes.serial AND journeys.serial=?");
 		stmt.setInt(1, serial);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()) throw new SQLException("Attempted to find driver ID of a nonexistent Journey " + serial);
 		return rs.getString(1).trim();
 	}
 /*
 	public String getHitchhikerID(int journeySerial) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT hitchhiker FROM journeys WHERE journeys.serial=?");
 		stmt.setInt(1, journeySerial);
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()) throw new SQLException("Attempted to find hiker ID of a nonexistent Journey " + journeySerial);
 		String ret = rs.getString(1);
 		return (ret == null ? ret : ret.trim());
 	}
 */
 
 	// ====================== Things to do with Notifications ======================= //
 
 	public void addNotification(Notification note) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO notifications(time_sent, recipient,sender, concerning_journey, type, startpoint, endpoint, comment) VALUES ( current_timestamp, ?, ?, ?, ?::notification_type, ?, ?,?)");
 		stmt.setString(1, note.getRecipientID());
 		stmt.setString(2, note.getSenderID());
 		stmt.setInt(3, note.getJourneySerial());
 		stmt.setString(4,note.getType().toString());
 		stmt.setObject(5, convertToPoint(note.getStartPoint()),Types.OTHER);
 		stmt.setObject(6, convertToPoint(note.getStopPoint()), Types.OTHER);
 		stmt.setString(7,note.getComment());
 		stmt.executeUpdate();
 	}
 
 	public List<Notification> getNotifications(User user) throws SQLException {
 		List<Notification> ret = new ArrayList<Notification>();
 		PreparedStatement stmt = conn.prepareStatement("SELECT time_sent, concerning_journey, type,	sender,	startpoint::geometry, endpoint::geometry, comment, recipient, name || ' ' || surname AS fullname, is_read FROM notifications INNER JOIN users ON sender=id WHERE recipient=? ORDER BY time_sent");
 		stmt.setString(1, user.getID());
 		ResultSet rs = stmt.executeQuery();
 		while(rs.next()){
 			String senderID = rs.getString("sender").trim();
 			String recipientID = rs.getString("recipient").trim();
 			String senderName = rs.getString("fullname");
 			if(senderName != null) senderName = senderName.trim();
 			String comment = rs.getString("comment");
 			if(comment != null) comment = comment.trim();
 			Calendar timeSent = convertToCalendar(rs.getTimestamp("time_sent"));
 			int journeySerial = rs.getInt("concerning_journey");
 			NotificationType type = NotificationType.valueOf(rs.getString("type"));
 			Location startPoint = convertToLocation((PGgeometry) rs.getObject("startpoint"));
 			Location endPoint = convertToLocation((PGgeometry) rs.getObject("endpoint"));
 			boolean is_read = rs.getBoolean("is_read");
 			Notification note = new Notification(senderID,recipientID,senderName,comment,journeySerial,type,startPoint,endPoint,timeSent);
 			note.setRead(is_read);
 			ret.add(note);
 		}
 		return ret;
 	}
 
 
 	/** Returns whether or not notifications exist from recipientID to senderID concerning the journey with serial journeySerial and of the type specified. */
 	public boolean checkForNotifications(String senderID, String recipientID,
 			int journeySerial, NotificationType expectedType) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT notifications_exist(?,?,?,?)");
 		stmt.setString(1, senderID);
 		stmt.setString(2, recipientID);
 		stmt.setInt(3, journeySerial);
 		stmt.setString(4, expectedType.toString());
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		return rs.getBoolean(1);
 	}
 	
 	public TripPreferences getPreference(int id) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM preferences WHERE id=?");
 		stmt.setInt(1, id);
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		TripPreferences ret = new TripPreferences(rs.getInt("seats"), rs.getBoolean("music"), rs.getBoolean("animals"), rs.getBoolean("breaks"), rs.getBoolean("talking"), rs.getBoolean("smoking"));
 		ret.setPrefId(rs.getInt("id"));
 		return ret;
 	}
 	
 	public TripPreferences getPreference(String userid) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM preferences,users WHERE preferences.id=users.preferenceid AND users.id=?");
 		stmt.setString(1, userid);
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		TripPreferences ret = new TripPreferences(rs.getInt("seats"), rs.getBoolean("music"), rs.getBoolean("animals"), rs.getBoolean("breaks"), rs.getBoolean("talking"), rs.getBoolean("smoking"));
 		ret.setPrefId(rs.getInt("id"));
 		return ret;
 	}
 	
 	public int createPreference(TripPreferences preference) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO preferences(animals,breaks,music,seats,smoking,talking) VALUES (?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);
 		stmt.setBoolean(1, preference.getAnimals());
 		stmt.setBoolean(2, preference.getBreaks());
 		stmt.setBoolean(3, preference.getMusic());
 		stmt.setInt(4,preference.getSeatsAvailable());
 		stmt.setBoolean(5, preference.getSmoking());
 		stmt.setBoolean(6, preference.getTalking());
 		stmt.executeUpdate();
 		ResultSet keys = stmt.getGeneratedKeys();
 		keys.next();
 		int id = keys.getInt(1);
 		keys.close();
 		return id;
 	}
 	public void updatePreference(TripPreferences preference) throws SQLException{
 		PreparedStatement stmt = conn.prepareStatement("UPDATE preferences SET (animals,breaks,music,seats,smoking,talking) = (?,?,?,?,?,?) WHERE id=?");
 		stmt.setBoolean(1, preference.getAnimals());
 		stmt.setBoolean(2, preference.getBreaks());
 		stmt.setBoolean(3, preference.getMusic());
 		stmt.setInt(4,preference.getSeatsAvailable());
 		stmt.setBoolean(5, preference.getSmoking());
 		stmt.setBoolean(6, preference.getTalking());
 		stmt.setInt(7, preference.getPrefId());
 		stmt.executeUpdate();
 		ServerLogger.write("\n\n\n\n\n\n\n\n\n\nBLARGH: \nSeats: " + Integer.toString(preference.getSeatsAvailable()) + "\nID: " + preference.getPrefId().toString() + "\n\n\n\n");
 	}
 	public Car getCar(int carId) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM car WHERE id=?");
 		stmt.setInt(1, carId);
 		ResultSet rs = stmt.executeQuery();
 		rs.next();
 		Car ret = new Car(carId,rs.getString("name"),rs.getDouble("comfort"));
 		ret.setPhotoAsBase64(rs.getString("picture"));
 		return ret;
 	}
 	public int createCar(Car car,User user) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("INSERT INTO car(name,comfort,picture) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS);
 		stmt.setString(1, car.getCarName());
 		stmt.setDouble(2, car.getComfort());
 		stmt.setString(3, car.getPhotoAsBase64());
 		stmt.executeUpdate();
 		ResultSet keys = stmt.getGeneratedKeys();
 		keys.next();
 		int id = keys.getInt(1);
 		keys.close();
 		PreparedStatement stmt2 = conn.prepareStatement("UPDATE users SET carid=? WHERE id=?");
 		stmt2.setInt(1, id);
 		stmt2.setString(2, user.getID());
 		stmt2.executeUpdate();
 		return id;
 		
 		
 	}
 	public void updateCar(Car car) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE car SET (name,comfort,picture) = (?,?,?) WHERE id=?");
 		stmt.setString(1, car.getCarName());
 		stmt.setDouble(2, car.getComfort());
 		stmt.setString(3, car.getPhotoAsBase64());
 		stmt.setInt(4, car.getCarId());
 		stmt.executeUpdate();
 	}
 
 	public void setNotificationRead(Notification note) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE notifications SET is_read=true WHERE recipient=? AND sender=? AND concerning_journey=? AND type=?::notification_type");
 		stmt.setString(1, note.getRecipientID());
 		stmt.setString(2,note.getSenderID());
 		stmt.setInt(3, note.getJourneySerial());
 		stmt.setString(4, note.getType().toString());
 		stmt.executeUpdate();
 	}
 	private void setDateModified(Route route) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE routes SET date_modified=? WHERE serial=?");
 		java.util.Date now = new java.util.Date();
 		Date sqlNow = new Date(now.getTime());
 		stmt.setDate(1,sqlNow);
 		stmt.setInt(2, route.getSerial());
 		stmt.executeUpdate();
 	}
 	private void incrementFrequency(Route route) throws SQLException {
 		int freq = getFrequency(route);
 		freq++;
 		setFrequency(route, freq);
 	}
 	private int getFrequency(Route route) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("SELECT frequency FROM routes WHERE serial=?");
 		stmt.setInt(1, route.getSerial());
 		ResultSet rs = stmt.executeQuery();
 		if(!rs.next()) throw new SQLException("Couldn't find route with serial: " + route.getSerial());
 		int ret = rs.getInt(1);
 		return ret;
 	}
 	private void setFrequency(Route route, int frequency) throws SQLException {
 		PreparedStatement stmt = conn.prepareStatement("UPDATE routes SET frequency=? WHERE serial=?");
 		stmt.setInt(1, frequency);
 		stmt.setInt(2,route.getSerial());
 		stmt.executeUpdate();
 	}
 
 }
