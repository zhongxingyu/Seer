package com.bettername.thepokemonone;

 import java.util.ArrayList;
 import java.util.concurrent.*;
import com.bettername.thepokemonone.monster.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Kyle
  * Date: 1/26/13
  * Time: 1:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class User {
 	
 	public String username;
 	public int userID;
 	public boolean isMale;
 	public ArrayList<Item> items;
 	public ArrayList<Monster> monsters;
 	
 	/* Current User Singleton + Static Methods*/
 	private static User currentUser = null;
 	private static boolean isLoadingUser = false;
 	
 	public static User currentUser() {
 		if (currentUser == null) {
 			// exec an asynchronous task
 			
 		}
 		return currentUser;
 	}
 	
 	/* Instance Methods + Initializer*/
 	public User() {
 	
 	}
 	
 	/*  */
 	
 }
