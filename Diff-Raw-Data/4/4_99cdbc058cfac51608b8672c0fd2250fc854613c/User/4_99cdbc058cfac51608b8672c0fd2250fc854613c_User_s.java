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
  * The user class has a default constructor which sets all data members to either 0 or blank space
  * It also has a intitializer constructor which gets the data member values from the test driver
  * Other methods included in this class are the getters and setters, and methods allowing the users to purchase, rate, and list media.
  * 
  * @author Joshua Thrush
  * @author Jared Bean
  */
 public class User {
 
     protected int ID; //user's ID number
 	protected String name; //name of the user
     protected String password; //user password
 	protected String city; //city where user lives
 	protected String shoppingCart; //user's current items up for purchase
     protected double balance; //user credit
     protected String history; //user purchase history
     
 
     // default constructor - sets all data members to blank or 0
     public User()
     {
         ID = 0;
         name = "";
         password = "";
 		city = "";
 		shoppingCart = "";
         balance = 0.0;
 		history = "";
     }
     
     // Initializer constructor - gets data members from the test driver
     public User(int ID,
              String name,
              String password,
 			 String city,
              double balance,
              String shoppingCart,
              String userHistory)
     {
         this.ID = ID;
         this.name = name;
         this.password = password;
 		this.city = city;
         this.balance = balance;
         this.shoppingCart = shoppingCart;
         this.history = userHistory;
     }
     
     //Auto-generated setters and getters
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
 	
 	public String getShoppingCart() {
 		return shoppingCart;
 	}
 
 	public void setShoppingCart(String shoppingCart) {
 		this.shoppingCart = shoppingCart;
 	}
 
 	public double getBalance() {
 		return balance;
 	}
 
 	public void setBalance(double balance) {
 		this.balance = balance;
 	}
 
 	public String getHistory() {
 		return history;
 	}
 
 	public void setHistory(String history) {
 		this.history = history;
 	}
     
 	//Allows the user to search for a media object
 	public void Search(Media mediaObj)
 	{
 		
 	}
 	
 	//Displays the attributes of the current media object on screen
 	public String displayItem(Media mediaObj)
 	{
 		
 		return mediaObj.toString();
 	}
 	
 	//Allows the user to make a purchase of a media object, if the user doesn't have enough credit the item is not sold and a message is printed on screen.
 	public boolean purchase(Media mediaObj, String type)
 	{
 		int purchased = 0;
 		
 		if(balance > mediaObj.getPrice()) //checks user credit
 		{
 			purchased=DBIO.addSale(mediaObj, this, 1);
 		}
 		else
 		{
 			System.out.println("Not enough money"); //item is not sold
 		}
 		return purchased>0;
 	}
 	
 	//Allows the user to rate a purchased media object
 	public void rateMovie(Media movieObj, double rating)
 	{	
 		movieObj.addRating(rating);
 	}
 	public void rateBook(Media bookObj, double rating)
 	{	
 		bookObj.addRating(rating);
 	}
 	public void rateAlbum(Media albumObj, double rating)
 	{	
 		albumObj.addRating(rating);
 	}
 	
 	//Allows the user to get a list of all available media
 	public Media [] list(String type){
 		ArrayList<Media> retVal;
 		retVal = DBIO.listOfType(type);
 		if(retVal==null){
 			return null;
		}else{
			return (Media[])retVal.toArray();
 		} 
 	}
 	
     @Override
     // toString holding the user information
     public String toString()
     {
         return String.format("::USER INFORMATION::\n"
                     + "Identification #:%20s \n"
                     + "The User:%20s \n"
                     + "Password:%20s \n"
 					+ "City:%20s \n"
                     + "Balance:%20f \n"
                     + "Shopping Cart:%20s \n"
                     + "History:%20s \n",
                     this.ID, this.name, this.password, this.city,
                     this.balance, this.shoppingCart, this.history);
         
     }
 }
 
