 package controllers;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.validation.*;
 
 import play.db.jpa.Blob;
 import play.mvc.*;
 
 import models.*;
 
 public class Rentability extends Application {
 
 //  @Before
 //  static void checkUser() {
 //      if(connected() == null) {
 //          flash.error("Please log in first");
 //          Application.index();
 //      }
 //  }
 	
 	
 	//Rendering the (personalized) index page
 	public static void index() {
 		
 		/*Personalized content to be implemented!*/
 		
         render();
     }
     
 	//Rendering the create offer page using all existing categories
     public static void createOffer() {
     	List<Category> categories = Category.findAll();
     	render(categories);
     }
     
     //Creating a new Offer
     public static void saveOffer(Blob image, String articleName, String description, String name, 
     		String pickUpAddress, String startTime, String endTime, String price, String insurance) {    	
     	
     	validation.required(articleName);
     	validation.required(description);
     	validation.required(pickUpAddress);
     	validation.required(startTime);
     	//Make sure the date is entered in this format DD.MM.YYYY
     	validation.match(startTime, "\\d{2}\\.\\d{2}\\.\\d{4}").message("Please indicate the Date in the given format!");
     	validation.required(endTime);
     	validation.match(endTime, "\\d{2}\\.\\d{2}\\.\\d{4}").message("Please indicate the Date in the given format!");
     	validation.required(price);
     	//Ensures that the price field is followed by 2 digits after the point
     	validation.match(price, "\\d+\\.\\d{2}").message("Please indicate the Price in the given format!");
     	
     	if(validation.hasErrors())
     	{
     		List<Category> categories = Category.findAll();
     		render("@createOffer", articleName, description, categories, pickUpAddress,
     				startTime, endTime, price, insurance);
     	}
     	else
     	{
     		boolean insuranceRequired;
     		
     		List<Category> categories = Category.findAll();
         	Category c = categories.get(Integer.valueOf(name) - 1);
         	
         	//Test User as long as Login insn't implemented
         	User u = new User("","","","","","");
         	
         	//null value to be implemented - represents the user (ie owner)
         	Article a = new Article(articleName, description, u, c, image);
         	
         	//Conversion of String Values to Dates, Boolean, etc.
         	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
 			try {
 				Date start = sdf.parse(startTime);
 				Date end = sdf.parse(endTime);
 				
 				if(insurance == null || !insurance.equals("true"))
 					insuranceRequired = false;
 				else
 					insuranceRequired = true;
 				
 				double doublePrice = Double.parseDouble(price);
 				
 				//null value to be implemented - represents the description
 	        	new Offer(pickUpAddress, insuranceRequired, 0, doublePrice, null, start, end, a);
 	        	
 	        	flash.success("Your offer has successfully been created!");
 	        	Application.index();
 	        	
			} catch (Exception ex) {
				flash.error("Sorry an error occured, please try again!");
 			}
     	}
     }
 }
