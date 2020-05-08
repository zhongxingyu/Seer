 package Models;
 
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import Util.Logger;
 import Util.MySQLConnection;
 
 /**
  * Model - Reservation.
  * As data-representation of reservations in the database, this class provides 
  * several methods for dealing with reservations, i.e. creating reservations, 
  * listing reservations, updating and deleting reservations.
  * 
  * <code>
  * 	Reservation res = new Reservation();
  * 	// Create reservation
  * 	int res1 = reservation.create(int customerId, int carType, Date startDate, Date endDate);
  * 	// Delete reservation
  * 	reservation.delete(res1);
  * </code>
  */
 public class Reservation extends Model {
 	
 	/**
 	 * Creates a new reservation in the database, with the provided information. 
 	 * If the reservation fails (eg. invalid customer-ID or invalid car-type-ID), 
 	 * -1 is returned. On success the ID of the new reservation is returned.
 	 * 
 	 * @param createVars Map containing data to be stored.
 	 * 			key			=> description
 	 * 			customerId	=> The ID of the customer to book a car.
 	 * 			carType		=> The ID of the car-type to be booked.
 	 * 			startDate	=> The start date of the booking.
 	 * 			endDate		=> The end date of the booking.
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (Map<String, Object> createVars) {
 		int customerId = Integer.parseInt(createVars.get("customerId").toString());
 		int carType = Integer.parseInt(createVars.get("carType").toString());
 		Date startDate = (Date) createVars.get("startDate");
 		Date endDate = (Date) createVars.get("endDate");
 		
 		if (customerId <= 0 || carType <= 0 || startDate == null || endDate == null)
 			throw new NullPointerException();
 		
 		int freeCar = findFreeCar(carType, startDate, endDate); // Find available car
 		Customer Customer = new Customer(); // Verify customer-ID
 		if (freeCar <= 0 || Customer.read(customerId) == null)
 			return -1;
 		
 		createVars.remove("carType");
 		createVars.put("carId", freeCar);
 		
 		return super.create (createVars);
 	}
 	
 	/**
 	 * Creates a new reservation in the database, with the provided information. 
 	 * If the reservation fails (eg. invalid customer-ID or invalid car-type-ID), 
 	 * -1 is returned. On success the ID of the new reservation is returned.
 	 * 
 	 * @param customer The ID of the customer to book a car.
 	 * @param carType The ID of the car-type to be booked.
 	 * @param startDate The start date of the booking (format: YYYY-MM-DD).
 	 * @param endDate The end date of the booking (format: YYYY-MM-DD).
 	 * @return ID on success; -1 on failure.
 	 */
 	public int create (int customerId, int carType, Date startDate, Date endDate) {
 		Map<String, Object> createVars = new HashMap<String, Object>();
 		createVars.put("customerId", 	customerId);
 		createVars.put("carType", 		carType);
 		createVars.put("startDate", 	startDate);
 		createVars.put("endDate", 		endDate);
 		
 		return this.create(createVars);
 	}
 	
 	/**
 	 * Reads the reservation with the provided ID, and returns a Map<String, Object> 
 	 * containing the data about that reservation. If no reservation is found 
 	 * using that ID, null is returned.
 	 * 
 	 * @param id The id of the entry to read.
 	 * @return Map containing data on success; null on failure.
 	 * 			key			=> Description
 	 * 			id			=> The ID of the reservation.
 	 * 			car			=> The ID of the car booked.
 	 * 			customer	=> The ID of the customer booking the car.
 	 * 			startDate	=> Start date of reservation (YYYY-MM-DD).
 	 * 			endDate		=> End date of reservation (YYYY-MM-DD).
 	 */
 	public Map<String, Object> read (int id) {
 		if (id <= 0)
 			throw new NullPointerException();
 		
 		try {
 			String query =	"SELECT reservationId, carId, customerId, startDate, endDate " +
 							"FROM Reservation " +
 							"WHERE reservationId = " + id + " " +
 							"LIMIT 1";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			
 			result.next();
 			
 			Map<String, Object> returnMap = new HashMap<String, Object>();
 			returnMap.put("id", 			result.getInt	("reservationId"));
 			returnMap.put("car", 			result.getInt	("carId"));
 			returnMap.put("customer",		result.getInt	("customerId"));
 			returnMap.put("startDate",		result.getDate	("startDate"));
 			returnMap.put("endDate",		result.getDate	("endDate"));
 			
 			return returnMap;
 		} catch (SQLException e) {
 			Logger.write("Couldn't read from the database: " + e.getMessage());
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Updates the reservation with the provided ID-number. The fields to be 
 	 * updated, are the keys in the map, and the new data is the values in the 
 	 * map. All data-fields are required. 
 	 * If the update is successful, true will be returned; otherwise false 
 	 * will be returned.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param updateVars Map containing the data to be updated.
 	 * 			key			=> description
 	 * 			customerId	=> The ID of the customer to book a car.
 	 * 			carId		=> The ID of the car to be booked.
 	 * 			startDate	=> The start date of the booking.
 	 * 			endDate		=> The end date of the booking.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update (int id, Map<String, Object> updateVars) {
 		int customer	= Integer.parseInt(updateVars.get("customerId").toString());
 		int car 		= Integer.parseInt(updateVars.get("carId").toString());
 		Date startDate	= (Date) updateVars.get("startDate");
 		Date endDate	= (Date) updateVars.get("endDate");
 		
 		if (id <= 0 || customer <= 0 || car <= 0 || startDate == null || endDate == null)
 			throw new NullPointerException();
 		if (!checkAvailability(car, startDate, endDate))
 			return false;
 		
 		return super.update(id, updateVars, "reservationId");
 	}
 	
 	/**
 	 * Updates the reservation with the provided ID-number. If the update is 
 	 * successful, true will be returned; otherwise false will be returned.
 	 * 
 	 * @param id The ID of the entry to be updated.
 	 * @param customer The ID of the customer to book a car.
 	 * @param carType The ID of the car-type to be booked.
 	 * @param startDate The start date of the booking.
 	 * @param endDate The end date of the booking.
 	 * @return true on success; false on failure.
 	 */
 	public boolean update (int id, int customer, int car, Date startDate, Date endDate) {
 		Map<String, Object> updateVars = new HashMap<String, Object>();
 		updateVars.put("customerId", customer);
 		updateVars.put("carId", 	 car);
 		updateVars.put("startDate",  startDate);
 		updateVars.put("endDate", 	 endDate);
 		
 		return this.update(id, updateVars);
 	}
 	
 	/**
 	 * Deletes the reservation with the provided ID-number. If the deletion 
 	 * fails, false is returned. Otherwise true is returned. Please note: If no 
 	 * reservation is found with the provided ID, true will still be returned, 
 	 * as an entry with that ID isn't in the database after this method-call.
 	 * 
 	 * @param id The ID of the entry to be deleted.
 	 * @return true on success; false on failure.
 	 */
 	public boolean delete (int id) {
 		return super.delete(id, "reservationId");
 	}
 	
 	/**
 	 * Lists the reservations from the database, ordered by start date.
 	 * 
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list () { return list ("startDate", "ASC"); }
 	
 	/**
 	 * Lists the reservations from the database, ordered by the column specified.
 	 * 
 	 * @param orderColumn The column to order by.
 	 * @param orderDirection The ordering direction (ASC for ascending; DESC for descending).
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (String orderColumn, String orderDirection) { 
 		return list (orderColumn, orderDirection, 0);
 	}
 	
 	/**
 	 * Lists the reservations from the database, from the customer with the 
 	 * provided ID. The list is ordered by start date.
 	 * 
 	 * @param id The ID of the customer.
 	 * @return A list with all data from the database.
 	 */
 	public List<Map<String, Object>> list (int id) { return list ("startDate", "ASC", id); }
 	
 	/**
 	 * Lists the reservations from the database, ordered by the column provided. 
 	 * If a customer-ID is provided (third parameter > 0), only reservations 
 	 * from this customer is returned.
 	 * 
 	 * @param orderColumn The column to order by.
 	 * @param orderDirection The ordering direction (ASC for ascending; DESC for descending).
 	 * @param customerId The ID of the customer to find reservations from.
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (String orderColumn, String orderDirection, int customerId) {
 		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
 		
 		try {
 			String  customerQuery = "";
 			if (customerId > 0)	
 					customerQuery = "WHERE customerId = " + customerId + " ";
 			
 			String query =	"SELECT reservationId, carId, customerId, " +
 							"startDate, endDate " +
 							"FROM Reservation " +
 							 customerQuery + 
 							"ORDER BY " + orderColumn + " " + orderDirection;
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			Map<String, Object> curr;
 			while (result.next()) {
 				curr = new HashMap<String, Object>();
 				curr.put("id", 			result.getInt	("reservationId"));
 				curr.put("carId", 		result.getInt	("carId"));
 				curr.put("customerId", 	result.getInt	("customerId"));
 				curr.put("startDate", 	result.getDate	("startDate"));
 				curr.put("endDate", 	result.getDate	("endDate"));
 				
 				list.add(curr);
 			}
 		} catch (SQLException e) {
 			Logger.write("Failed to list items from database: " + e.getMessage());
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Lists the reservations from the database, in the time period provided.
 	 * 
 	 * @param startDate The start date
 	 * @param endDate The end date
 	 * @return A list with all data from the data-source.
 	 */
 	public List<Map<String, Object>> list (Date startDate, Date endDate) {
 		List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
 		
 		try {	
 			String query =	"SELECT reservationId, carId, customerId, " +
 							"startDate, endDate " +
 							"FROM Reservation " +
 							"WHERE " +
 								"(('"+startDate+"' 	>= startDate && '"+startDate+"' <= endDate) " + 
 								"OR ('"+endDate+"' 	>= startDate && '"+endDate+"' 	<= endDate) " + 
 								"OR ('"+startDate+"' 	<= startDate && '"+endDate+"' 	>= endDate)) " + 
 							"ORDER BY startDate ASC ";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result == null)
 				return null;
 			Map<String, Object> curr;
 			while (result.next()) {
 				curr = new HashMap<String, Object>();
 				curr.put("id", 			result.getInt	("reservationId"));
 				curr.put("carId", 		result.getInt	("carId"));
 				curr.put("customerId", 	result.getInt	("customerId"));
 				curr.put("startDate", 	result.getDate	("startDate"));
 				curr.put("endDate", 	result.getDate	("endDate"));
 				
 				list.add(curr);
 			}
 		} catch (SQLException e) {
 			Logger.write("Failed to list items from database: " + e.getMessage());
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Finds a free car in the specified period, with the specified car-type. 
 	 * Car ID is returned if car is found; Otherwise -1 is returned.
 	 * 
 	 * @param carType The ID of the car-type to be booked.
 	 * @param startDate The start date of the booking.
 	 * @param endDate The end date of the booking.
 	 * @return Car ID on success; -1 on failure.
 	 */
 	public int findFreeCar (int carType, Date startDate, Date endDate) {
 		if (carType <= 0 || startDate == null || endDate == null)
 			throw new NullPointerException();
 		
 		try {
 			String query =	
 				"SELECT carId " + 
 				"FROM Car " + 
 				"WHERE carType = " + carType + " " + 
 				"AND NOT EXISTS ( " + 
 					"SELECT reservationId " + 
 					"FROM Reservation " + 
 					"WHERE Reservation.carId = Car.carId " +
 					   "AND (('"+startDate+"' 	>= startDate && '"+startDate+"' <= endDate) " + 
 					     "OR ('"+endDate+"' 	>= startDate && '"+endDate+"' 	<= endDate) " + 
 					     "OR ('"+startDate+"' 	<= startDate && '"+endDate+"' 	>= endDate)) " + 
 				")";
 			MySQLConnection conn = MySQLConnection.getInstance();
 			ResultSet result = conn.query(query);
 			if (result != null) {
 				result.next();
 				return result.getInt(1);
 			}
 		} catch (SQLException e) {
 			Logger.write("Couldn't find free car: " + e.getMessage());
 		}
 		
 		return -1;
 	}
 	
 	/**
 	 * Checks whether or not a car is available in the provided period.
 	 * 
 	 * @param carId The ID of the car.
 	 * @param startDate The start date of the booking.
 	 * @param endDate The end date of the booking.
 	 * @return true on available; false on unavailable.
 	 */
 	public boolean checkAvailability (int carId, Date startDate, Date endDate) {
 		String query =	
 			"SELECT carId " + 
 			"FROM Car " + 
 			"WHERE carId = " + carId + " " + 
 			"AND NOT EXISTS ( " + 
 				"SELECT reservationId " + 
 				"FROM Reservation " + 
 				"WHERE Reservation.carId = Car.carId " +
 				   "AND (('"+startDate+"' 	>= startDate && '"+startDate+"' <= endDate) " + 
 				     "OR ('"+endDate+"' 	>= startDate && '"+endDate+"' 	<= endDate) " + 
 				     "OR ('"+startDate+"' 	<= startDate && '"+endDate+"' 	>= endDate)) " + 
 			")";
 		MySQLConnection conn = MySQLConnection.getInstance();
 		ResultSet result = conn.query(query);
 		if (result != null) {
 			return true;
 		}
 		
 		return false;
 	}
 }
