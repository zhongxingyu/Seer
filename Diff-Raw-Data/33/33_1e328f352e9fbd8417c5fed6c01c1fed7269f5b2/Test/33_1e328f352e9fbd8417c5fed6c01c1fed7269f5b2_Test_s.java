 package debug;
 
 import interfaces.Model;
 
 import java.util.ArrayList;
 
 import dao.Receipes;
 import dao.Users;
 import beans.Receipe;
 import beans.User;
 
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//int id, String title, String sumup, String desc, String imgRef, Integer nbPers, String cookType, Integer cookExp, Integer duration
		Receipes Dao = new Receipes();
		ArrayList<Receipe> receipes;
		Receipe i = new Receipe(0, "Carpacio de banane", "Bah... Cest un carpacio quoi, mais avec des bananes", "lorem ipsum & co", "cookpony", 6, "Dessert", 2, 20);
 		
 		Dao.create(i);
 		
 		System.out.println("-------");
 		i = Dao.fromRow(Dao.getLast());
 		System.out.println(i);
 		System.out.println("#######");
		i.setCooktype("WTF BBQ");
 		Dao.update(i);
 		i = Dao.fromRow(Dao.getLast());
 		System.out.println(i);
 		System.out.println("#######");
 		
 		
 		
		receipes = Dao.getReceipes(180, 5, 3, "Salad");	
 		Dao.delete(i);
 		//users = Dao.getItems();
 		//*
		for(Model item : receipes) {
 			System.out.println(item);
 		}
 		//*/
 		
 	}
 	
 	
 
 }
