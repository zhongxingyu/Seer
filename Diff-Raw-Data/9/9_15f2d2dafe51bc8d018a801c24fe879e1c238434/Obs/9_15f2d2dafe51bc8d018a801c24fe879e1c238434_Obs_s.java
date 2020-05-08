 package observer;
 
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import model.Model;
 
 
 public class Obs implements Observer {
 
 	@Override
 	public void update(Observable observable, Object object) {
 		System.out.println("OBS: Update called");
 		
 		/*
 		 * Checking if observable is instance of model
 		 */
 		if(observable instanceof Model){
 			System.out.println("OBS: Observable is an instanceof Model");
 			
 			/*
 			 * Checking if object is instance of int and set
 			 */
 			if(object instanceof Integer){
 				System.out.println("OBS: Object is an instanceof Integer");
 			} else if(object instanceof Set){
 				System.out.println("OBS: Object is an instanceof Set");
 			} else if(object instanceof HashSet){
 				System.out.println("OBS: Object is an instanceof HashSet");
 			}
 		} else {
 			System.out.println("OBS: I HAVE NO IDEA WHAT I AM DOING!");
 		}
 		
 		
 		
 	}
 
 }
