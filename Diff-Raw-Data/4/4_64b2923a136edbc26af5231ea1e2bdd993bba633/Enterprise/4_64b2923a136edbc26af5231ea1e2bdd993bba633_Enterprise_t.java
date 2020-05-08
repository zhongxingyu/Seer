 /**
  * This file is part of ASxcel.
  *
  * ASxcel is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ASxcel is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with ASxcel.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.jandavid.asxcel.model;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * This application has the purpose to help the user manage
  * his enterprise by providing him with a new way to collect
  * and view data. This class is responsible for collecting
  * all information the user enters about his enterprise. 
  * 
  * @author jdno
  */
 public class Enterprise {
 	
 	/**
 	 * This is the enterprise's ID in the database.
 	 */
 	private int id;
 	
 	/**
 	 * Where the enterprise's main office is located.
 	 */
 	private Airport mainHub;
 	
 	/**
 	 * The model is needed to access the database.
 	 */
 	private Model model;
 	
 	/**
 	 * This is the name the user gave his enterprise.
 	 */
 	private String name;
 	
 	/**
 	 * This is a list of routes a user has established or
 	 * is planning to introduce.
 	 */
 	private ArrayList<Route> routes = new ArrayList<Route>();
 	
 	/**
 	 * An enterprise is the logical collection of the user's
 	 * information. It is identified by its name.
 	 * @param name The (unique) name of the enterprise.
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	public Enterprise(Model model, String name) throws SQLException, Exception {
 		this.model = model;
 		this.name = name;
 		
 		syncWithDb();
 	}
 	
 	/**
 	 * This method creates a new route and adds it to the list of routes.
 	 * @param origin The airport where the route starts.
 	 * @param destination The airport where the route ends.
 	 * @return The newly created route.
 	 * @throws SQLException If a SQL error occurs this gets thrown.
 	 */
 	public Route createRoute(Airport origin, Airport destination) throws SQLException {
 		for(Route r: routes) {
 			if(r.getOrigin().compareTo(origin) == 0) {
 				if(r.getDestination().compareTo(destination) == 0) {
 					return r;
 				}
 			}
 		}
 		
 		Route r = new Route(model, origin, destination);
 		
 		routes.add(r);
 		
 		return r;
 	}
 	
 	/**
 	 * This method checks if routes exist for a given airport. This is
 	 * useful when deleting an airport.
 	 * @param airport The airport to check for routes.
 	 * @return True of routes exists to or from the given airport, false otherwise.
 	 */
 	public boolean doRoutesExistFor(Airport airport) {
 		for(Route r: routes) {
 			if(r.getOrigin().equals(airport) || r.getDestination().equals(airport)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Delete a specific route, based on its position in the list.
 	 * @param route The position of the route to delete.
 	 * @throws SQLException If a SQL error occurs this gets thrown.
 	 */
 	public void deleteRoute(int route) throws SQLException {
 		Route r = routes.get(route);
 		
 		String query = "DELETE FROM `routes` WHERE `id` = '" + r.getId() + "'";
 		
 		model.getDatabase().executeUpdate(query, new ArrayList<Object>(0));
 		
 		routes.remove(route);
 	}
 	
 	/**
 	 * This method retrieves all destinations for a given airport.
 	 * @param origin The airport where the routes start.
 	 * @return A list of all airports to which routes go.
 	 */
 	public ArrayList<Airport> getDestinations(Airport origin) {
 		ArrayList<Airport> airports = new ArrayList<Airport>();
 		
 		for(Route r: routes) {
 			if(r.getOrigin().compareTo(origin) == 0) {
 				airports.add(r.getDestination());
 			}
 		}
 		
 		Collections.sort(airports);
 		
 		return airports;
 	}
 	
 	/**
 	 * This method loads all routes belonging to the current enterprise
 	 * from the database.
 	 * @throws SQLException If a SQL error occurs this gets thrown.
 	 */
 	public void loadRoutes() throws SQLException {
 		String query = "SELECT `a1`.`name` AS `origin`, `a2`.`name` AS `destination` FROM `routes` AS `r` " +
 				"INNER JOIN `airports` AS `a1` ON `r`.`origin` = `a1`.`id` " +
 				"INNER JOIN `airports` AS `a2` ON `r`.`destination` = `a2`.`id` " +
 				"WHERE `r`.`enterprise` = '" + id + "'";
 		
 		DatabaseResult dr = model.getDatabase().executeQuery(query);
 		
 		while(dr.next()) {
			Airport origin = model.getAirport(dr.getString(0));
			Airport destination = model.getAirport(dr.getString(1));
 			
 			Route r = new Route(model, origin, destination);
 			
 			routes.add(r);
 		}
 		
 		Collections.sort(routes);
 	}
 	
 	/**
 	 * This method synchronizes an enterprise with the database, assuming
 	 * the enterprise has been created already. If this is the case this 
 	 * instance of Enterprise gets filled with its data, else a new enterprise
 	 * gets added to the database with the given name.
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	private void syncWithDb() throws SQLException, Exception {
 		String query = "SELECT `e`.`id`, `e`.`name`, `a`.`name` FROM `enterprises` AS `e` " +
 				"INNER JOIN `airports` AS `a` ON `e`.`airport` = `a`.`id` " +
 				"WHERE `e`.`name` = ? LIMIT 1";
 		ArrayList<Object> params = new ArrayList<Object>(1);
 		params.add(name);
 		
 		DatabaseResult dr = model.getDatabase().executeQuery(query, params);
 		
 		if(dr.next()) {
 			id = dr.getInt(0);
 			name = dr.getString(1);
 			mainHub = new Airport(model, dr.getString(2));
 			
 			loadRoutes();
 		} else {
 			throw new Exception("Enterprise was not found");
 		}
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @return the mainHub
 	 */
 	public Airport getMainHub() {
 		return mainHub;
 	}
 
 	/**
 	 * @return the model
 	 */
 	public Model getModel() {
 		return model;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the routes
 	 */
 	public ArrayList<Route> getRoutes() {
 		return routes;
 	}
 
 }
