 package birdProgram;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException; 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.*;
 
 public class BirdFavorites  {
 	private ArrayList<Bird> favorites;
 	
 	public BirdFavorites(){
 		deserializationOfFavorites();
 	}
 	
 	public void serializationOfFavorites(){
 		ArrayList<Bird> tempFavs = favorites;
 		try { 
 			FileOutputStream fos = new FileOutputStream("favorites"); 
 			ObjectOutputStream oos = new ObjectOutputStream(fos); 
 			oos.writeObject(tempFavs); 
 			oos.flush(); 
 			oos.close(); 
 		}
 		catch(Exception e) { 
 			System.out.println("Exception during serialization: " + e); 
 			System.exit(0); 
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void deserializationOfFavorites(){
 		try { 
 			FileInputStream fis = new FileInputStream("favorites"); 
 			ObjectInputStream ois = new ObjectInputStream(fis); 
 			favorites = (ArrayList<Bird>)ois.readObject(); 
			ois.close(); 
 		} 
 		catch(Exception e) { 
 			System.out.println("Exception during deserialization: " + e); 
 			favorites = new ArrayList<Bird>();
 		}
 	}
 	
 	public boolean addFavorite(Bird bird){
 		return favorites.add(bird);
 	}
 	
 	public boolean removeFavorite(Bird bird){
 		return favorites.remove(bird);
 	}
 	
 	public List<Bird> getFavorites(){
 		return favorites;
 	}
 	
 	private void writeObject(java.io.ObjectOutputStream out)throws IOException{
 		out.defaultWriteObject();
 	}
 	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
 		in.defaultReadObject();
 	}
 }
