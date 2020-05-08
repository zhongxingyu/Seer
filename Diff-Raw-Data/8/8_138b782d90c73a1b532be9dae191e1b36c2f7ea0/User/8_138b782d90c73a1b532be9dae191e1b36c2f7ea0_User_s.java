 package store;
 
 import java.util.ArrayList;
 
 /**
  * Name: Joshua Thrush, Jared Bean
  * Section: 1
  * Program: Java Store Project
  * Date: 2/15/2013
  * Description: This file holds the user class which is the base class of customer and manager. It contains methods which give
  * customer and manager the abitity to purchase, rate, and list all media items.
  * 
  */
 
 /**
  * The user class has a default constructor which sets all data members to
  * either 0 or blank space It also has a intitializer constructor which gets the
  * data member values from the test driver Other methods included in this class
  * are the getters and setters, and methods allowing the users to purchase,
  * rate, and list media.
  * 
  * @author Joshua Thrush
  * @author Jared Bean
  */
 public class User {
 
 	protected int ID; // user's ID number
 	protected String name; // name of the user
 	protected String password; // user password
 	protected String city; // city where user lives
 	protected Order[] shoppingCart; // user's current items up for purchase
 	protected double balance; // user credit
 	protected Order[] history; // user purchase history
 
 	// default constructor - sets all data members to blank or 0
 	public User() {
 		ID = 0;
 		name = "";
 		password = "";
 		city = "";
 		shoppingCart = null;
 		balance = 0.0;
 		history = null;
 	}
 
 	// Initializer constructor - gets data members from the test driver
 	public User(int ID, String name, String password, String city,
 			double balance) {
 		this.ID = ID;
 		this.name = name;
 		this.password = password;
 		this.city = city;
 		this.balance = balance;
 		this.shoppingCart = DBIO.getShoppingCart(this.ID);
 		this.history = DBIO.getOrderHistory(this.ID);
 	}
 
 	// Auto-generated setters and getters
 	public int getID() {
 		return ID;
 	}
 
 	public void setID(int iD) {
 		ID = iD;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getcity() {
 		return city;
 	}
 
 	public void setcity(String city) {
 		this.city = city;
 	}
 
 	public Order[] getShoppingCart() {
 		return shoppingCart;
 	}
 
 	public double getBalance() {
 		return balance;
 	}
 
 	public void setBalance(double balance) {
 		this.balance = balance;
 	}
 
 	public Order[] getHistory() {
 		return history;
 	}
 
 	/**
 	 * Searches on a field in the inventory for a string, filtering on a
 	 * specific media type.
 	 * 
 	 * @param searchStr
 	 *            String to match on -- *searchStr* or in SQL %searchStr% is the
 	 *            pattern
 	 * @param searchField
 	 *            String name of field to search on
 	 * @param mediaType
 	 *            String name of media type to filter with
 	 * @return ArrayList of Media obect results, an empty ArrayList is returned
 	 *         if there are none or there was an error
 	 */
 	public static ArrayList<Media> search(String searchStr,
 			DBIO.SearchField searchField, DBIO.Types mediaType) {
 		ArrayList<Media> retVal = DBIO.searchInventory(searchStr, searchField,
 				mediaType);
 		if (retVal == null) {
 			retVal = new ArrayList<Media>();
 		}
 		return retVal;
 	}
 
 	// Displays the attributes of the current media object on screen
 	public String displayItem(Media mediaObj) {
 
 		return mediaObj.toString();
 	}
 
 	// Allows the user to make a purchase of a media object, if the user doesn't
 	// have enough credit the item is not sold and a message is printed on
 	// screen.
 	public int purchase(Media mediaObj, int numToBuy) {
 		int purchased = 0;
 		User tempUser;
 
 		if (balance > mediaObj.getPrice()) // checks user credit
 		{
 			purchased = DBIO.addSale(mediaObj, this, numToBuy);
 		} else {
 			purchased=0;
 		}
 		
 		//Refresh User
 		tempUser= DBIO.getUser(this.ID);
 		this.balance=tempUser.balance;
 		this.shoppingCart = tempUser.getShoppingCart();
 		this.history = tempUser.getHistory();
 		return purchased;
 	}
 
 	// Allows the user to rate a purchased media object
 	public void rateMedia(Media mediaObj, double rating) {
 		mediaObj.addRating(rating);
 	}
 
 	// Allows the user to get a list of all available media
 	public Media[] list(String type) {
 		ArrayList<Media> retVal;
 		retVal = DBIO.listOfType(type);
 		if (retVal == null) {
 			return null;
 		} else {
 			return (Media[]) (retVal.toArray(new Media[retVal.size()]));
 		}
 	}
 
 	@Override
 	// toString holding the user information
 	public String toString() {
 		return String.format("::USER INFORMATION::\n"
 				+ "Identification #:%20s \n" + "The User:%20s \n"
 				+ "Password:%20s \n" + "City:%20s \n" + "Balance:%20f \n"
 				+ "Shopping Cart:%20s \n" + "History:%20s \n", this.ID,
 				this.name, this.password, this.city, this.balance,
 				User.ordersToString(this.shoppingCart), User.ordersToString(this.history));
 
 	}
 	/**
 	 * Utility function to turn an order array into a JSON-esque string representation
 	 * @param orders Array of orders create the string from
 	 * @return	String representation of the orders array
 	 */
 	public static String ordersToString(Order[] orders){
 		String retVal="[";
 		for(Order oObj : orders){
			String.format("{\"media\":%d, \"numBought\":%d,"+
					"\"date\":\"%s\"}", oObj.getMedia().getId(), oObj.getNumBought(),
 					oObj.getDate().toString());
 		}
 		retVal+="]";
 		return retVal;
 	}
 }
