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
 
 /**
  * An airport in AirlineSim has five important parameters:
  * - name
  * - IATA code
  * - size
  * - passengers
  * - cargo
  * These parameters are helpful to the user to choose new
  * routes and where to expand to.
  * 
  * @author jdno
  */
 public class Airport implements Comparable<Airport> {
 	
 	/**
 	 * This indicates how much cargo volume this airport has.
 	 * Values range from 0 (lowest) to 10 (highest).
 	 */
 	private int cargo = 0;
 	
 	/**
 	 * This is the country the airport is located in.
 	 */
 	private Country country;
 	
 	/**
 	 * This is the IATA code of the airport.
 	 */
 	private String iataCode = "";
 	
 	/**
 	 * This is the ID the airports has in the database.
 	 */
 	private int id;
 	
 	/**
 	 * This is necessary to access the database.
 	 */
 	private Model model;
 	
 	/**
 	 * This is the name of the airport.
 	 */
 	private String name;
 	
 	/**
 	 * This indicated how much passenger volume this airport has.
 	 * Values range from 0 (lowest) to 10 (highest).
 	 */
 	private int passengers = 0;
 	
 	/**
 	 * This is the size of the airport as it is indicated in
 	 * the game (e.g. "Small airport").
 	 */
 	private String size = "";
 	
 	/**
 	 * At some airports transfer is not possible, which means no passenger
 	 * can change flights at this location.
 	 */
 	private boolean transferPossible = true;
 	
 	/**
 	 * An airport gets initialized with its name, all the other parameters
 	 * can be set later or they can be retrieved from the database if the
 	 * airport has been created already.
 	 * @param name The name of the airport.
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	protected Airport(Model model, String name) throws SQLException {
 		this.model = model;
 		this.name = name;
 		
 		syncWithDb();
 	}
 	
 	/**
 	 * This constructor initializes an airport with all its attributes. No
 	 * synchronization with the database happens! This is meant as a help
 	 * for the model to load a set of airports with one query from the
 	 * database and initialize them all at once without further queries.
 	 * @param model The model to use
 	 * @param id The ID of the airport (from the database)
 	 * @param name The name of the airport
 	 * @param country The country the airport is in
 	 * @param iata The IATA code of the airport
 	 * @param pax The airports passenger size
 	 * @param cargo The airports cargo size
 	 * @param transferPossible Is a transfer possible
 	 */
 	protected Airport(Model model, int id, String name, Country country, String iata, String size, int pax, int cargo, boolean transferPossible) {
 		this.model = model;
 		this.id = id;
 		this.name = name;
 		this.country = country;
 		this.iataCode = iata;
 		this.size = size;
 		this.passengers = pax;
 		this.cargo = cargo;
 		this.transferPossible = transferPossible;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(Airport o) {
 		return name.compareTo(o.getName());
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if(obj instanceof Airport) {
 			if(((Airport) obj).compareTo(this) == 0) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return super.equals(obj);
 		}
 	}
 
 	/**
 	 * This method loads an airport from the database, assuming the airport
 	 * has been created already. If this is the case this instance if Airport
 	 * gets filled with its data, else a new airport gets added to the database
 	 * with the given name.
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	private void syncWithDb() throws SQLException {
 		String query = "SELECT `a`.`id`, `a`.`name`, `a`.`iata`, `a`.`passengers`, " +
 				"`a`.`cargo`, `a`.`size`, `a`.`transfer`, `c`.`name` FROM `airports` AS `a` " +
 				"INNER JOIN `countries` AS `c` ON `a`.`country` = `c`.`id` " +
				"WHERE `a`.`name` = ? LIMIT 1";
 		ArrayList<Object> params = new ArrayList<Object>(1);
 		params.add(name);
 		
 		DatabaseResult dr = model.getDatabase().executeQuery(query, params);
 		
 		if(dr.next()) {
 			id = dr.getInt(0);
 			name = dr.getString(1);
 			iataCode = dr.getString(2);
 			passengers = dr.getInt(3);
 			cargo = dr.getInt(4);
 			size = dr.getString(5);
 			transferPossible = dr.getInt(6) == 1 ? true : false;
 			country = new Country(model, dr.getString(7));
 		} else {
 			query = "INSERT OR IGNORE INTO `airports` (`name`) VALUES (?)";
 			model.getDatabase().executeUpdate(query, params);
 			query = "INSERT OR IGNORE INTO `enterprise_has_airport` (`enterprise` , `airport`) " +
 					"SELECT '" + model.getEnterprise().getId() + "' AS `enterprise`, " +
 							"`id` FROM `airports` WHERE `name` = ?";
 			model.getDatabase().executeUpdate(query, params);
 			syncWithDb();
 		}
 	}
 	
 	/**
 	 * This is an auxiliary method used by the setter to change values in the
 	 * database without the need to create an own query first.
 	 * @param field The name of the column in the database.
 	 * @param value The value to set.
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	private void updateField(String field, Object value) throws SQLException {
 		String query = "UPDATE `airports` SET `" + field + "` = ? WHERE `id` = ?";
 		ArrayList<Object> params = new ArrayList<Object>(2);
 		params.add(value);
 		params.add(id);
 		
 		model.getDatabase().executeUpdate(query, params);
 	}
 
 	/**
 	 * @return the cargo
 	 */
 	public int getCargo() {
 		return cargo;
 	}
 
 	/**
 	 * @param cargo the cargo to set
 	 * @throws SQLException If an SQL error occurs this gets thrown. 
 	 */
 	public void setCargo(int cargo) throws SQLException {
 		updateField("cargo", cargo);
 		this.cargo = cargo;
 	}
 
 	/**
 	 * @return the country
 	 */
 	public Country getCountry() {
 		return country;
 	}
 
 	/**
 	 * @param country the country to set
 	 */
 	public void setCountry(Country country) {
 		this.country = country;
 	}
 
 	/**
 	 * @return the iataCode
 	 */
 	public String getIataCode() {
 		return iataCode;
 	}
 
 	/**
 	 * @param iataCode the iataCode to set
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	public void setIataCode(String iataCode) throws SQLException {
 		updateField("iata", iataCode);
 		this.iataCode = iataCode;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	public void setName(String name) throws SQLException {
 		updateField("name", name);
 		this.name = name;
 	}
 
 	/**
 	 * @return the passengers
 	 */
 	public int getPassengers() {
 		return passengers;
 	}
 
 	/**
 	 * @param passengers the passengers to set
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	public void setPassengers(int passengers) throws SQLException {
 		updateField("passengers", passengers);
 		this.passengers = passengers;
 	}
 
 	/**
 	 * @return the size
 	 */
 	public String getSize() {
 		return size;
 	}
 
 	/**
 	 * @param size the size to set
 	 * @throws SQLException If an SQL error occurs this gets thrown.
 	 */
 	public void setSize(String size) throws SQLException {
 		updateField("size", size);
 		this.size = size;
 	}
 
 	/**
 	 * @return the transferPossible
 	 */
 	public boolean isTransferPossible() {
 		return transferPossible;
 	}
 
 	/**
 	 * @param transferPossible the transferPossible to set
 	 * @throws SQLException  If an SQL error occurs this gets thrown.
 	 */
 	public void setTransferPossible(boolean transferPossible) throws SQLException {
 		updateField("transfer", transferPossible ? 1 : 0);
 		this.transferPossible = transferPossible;
 	}
 
 }
