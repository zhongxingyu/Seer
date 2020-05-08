 package controllers;
 
 import models.User;
 
public class Security extends Secure {
 	public static boolean authenticate(String nickname, String password) {
 		return (User.connect(nickname, password) != null);
 	}

	public static void hello() {

	}
 }
