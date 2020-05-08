 package pizzaProgram.database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import pizzaProgram.dataObjects.Dish;
 
 /**
  * Object for handling dishes in the database. The methods of the class handles
  * creation of the {@link java.util.ArrayList ArrayList} and a
  * {@link java.util.HashMap HashMap} of all the different
  * {@link pizzaProgram.dataObject.Dish dishes} based on a fetch from the
  * database, adding new dishes to the database, as well as tagging dishes as
  * active/inactive
  * 
  * @author IT1901 Group 03, Fall 2011
  */
 
 // TODO: Dispatch an event whenever the lists need to be updated
 
 public class DishList {
 	private final static ArrayList<Dish> dishList = new ArrayList<Dish>();
 	private final static HashMap<Integer, Dish> dishMap = new HashMap<Integer, Dish>();
 
 	/**
 	 * This method clears the old lists, and repopulates the
 	 * {@link java.util.ArrayList ArrayList} and {@link java.util.HashMap
 	 * HashMap} of all the different {@link pizzaProgram.dataObject.Dish dishes}
 	 * based on a fetch from the database. This method must be rerun each time
 	 * the Dishes table of the database is modified.
 	 */
 	public static void updateDishes() {
 		if (!DatabaseConnection.isConnected(DatabaseConnection.DEFAULT_TIMEOUT)) {
 			System.err
 					.println("No valid database connection specified; unable to update lists.");
 			return;
 		}
 		dishList.clear();
 		dishMap.clear();
 		try {
 			ResultSet results = DatabaseConnection
 					.fetchData("SELECT * FROM Dishes;");
 			while (results.next()) {
 				Dish tempDish = new Dish(results.getInt(1), results.getInt(2),
 						results.getString(3), results.getBoolean(4),
 						results.getBoolean(5), results.getBoolean(6),
 						results.getBoolean(7), results.getBoolean(8),
 						results.getString(9), results.getBoolean(10));
 				dishList.add(tempDish);
 				dishMap.put(tempDish.dishID, tempDish);
 			}
 			results.close();
 		} catch (SQLException e) {
 			System.err
 					.println("An error occured while updating the dish lists: "
 							+ e.getMessage());
 		}
 	}
 
 	public static ArrayList<Dish> getDishList() {
 		return dishList;
 	}
 
 	public static HashMap<Integer, Dish> getDishMap() {
 		return dishMap;
 	}
 
 	/**
 	 * Method for adding a new dish to the mySQL database. All newly added
 	 * dishes are active by default.
 	 * 
 	 * @param price
 	 *            - the price of the dish as an integer
 	 * @param name
 	 *            - the name of the dish as a String no longer than
 	 *            {@link pizzaProgram.database.DatabaseConnection#VARCHAR_MAX_LENGTH_LONG
 	 *            VARCHAR_MAX_LENGTH_LONG}
 	 * @param containsGluten
 	 *            - set to true if the dish contains gluten, false if not
 	 * @param containsNuts
 	 *            - set to true if the dish contains nuts, false if not
 	 * @param containsDairy
 	 *            - set to true if the dish contains dairy products, false if
 	 *            not
 	 * @param isVegetarian
 	 *            - set to true if the dish is fully vegetarian in nature, false
 	 *            if not
 	 * @param isSpicy
 	 *            - set to true if the dish is spicy in nature, false if not
 	 * @param description
 	 *            - a description of the dish as a String
 	 * @param isActive
 	 *            wether or not the Dish is to be shown in the orderGUI
 	 * @return returns true if the dish was successfully added to the database,
 	 *         false in all other cases
 	 */
 
 	public static boolean addDish(int price, String name,
 			boolean containsGluten, boolean containsNuts,
 			boolean containsDairy, boolean isVegetarian, boolean isSpicy,
 			String description, boolean isActive) {
		if (DatabaseConnection.isConnected(DatabaseConnection.DEFAULT_TIMEOUT)) {
 			System.err
 					.println("No valid database connection specified; dish not added to the database.");
 			return false;
 		}
 		if (name.length() > DatabaseConnection.VARCHAR_MAX_LENGTH_LONG) {
 			throw new IllegalArgumentException(
 					"The name of the dish cannot be more than "
 							+ DatabaseConnection.VARCHAR_MAX_LENGTH_LONG
 							+ " characters long.");
 		}
 		return DatabaseConnection
 				.insertIntoDB("INSERT IGNORE INTO Dishes (Price, Name, ContainsGluten, ContainsNuts, ContainsDairy, IsVegetarian, IsSpicy, Description, isActive) VALUES ("
 						+ price
 						+ ", '"
 						+ name
 						+ "', "
 						+ containsGluten
 						+ ", "
 						+ containsNuts
 						+ ", "
 						+ containsDairy
 						+ ", "
 						+ isVegetarian
 						+ ", "
 						+ isSpicy
 						+ ", '"
 						+ description
 						+ "', " + isActive + ");");
 	}
 
 	/**
 	 * Method to set the dish active (available in the order GUI) or inactive
 	 * (only available through the admin interface.
 	 * 
 	 * @param dish
 	 *            - the dish to change the status for.
 	 * @param newStatus
 	 *            - true if the dish is to be visible in the order GUI,
 	 *            invisible if not.
 	 * @return true if the change was made successfully, false if not.
 	 */
 	public static boolean changeDishStatus(Dish dish, boolean newStatus) {
 		if (!DatabaseConnection.isConnected(DatabaseConnection.DEFAULT_TIMEOUT)) {
 			System.err
 					.println("No valid database connection; dish status not changed.");
 			return false;
 		}
 		return DatabaseConnection.insertIntoDB("UPDATE Dishes SET isActive="
 				+ newStatus + " WHERE DishID=" + dish.dishID + ";");
 	}
 }
