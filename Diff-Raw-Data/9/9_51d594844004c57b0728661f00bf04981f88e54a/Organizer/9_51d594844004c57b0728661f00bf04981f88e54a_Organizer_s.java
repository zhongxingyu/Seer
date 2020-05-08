 package models;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.Observable;
 
 
 
 public class Organizer extends Observable {
     private static Organizer instance;
     private static UserManager users;
     private static String savePath = "savefile";
 
     private Organizer() {
     	if(!loadFromFile(savePath))
     		users = new UserManager();
     }
     
     public void saveToFile(){
     	try {
     		Files.deleteIfExists(Paths.get("save/" + savePath));
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("save/" + savePath));
			oos.writeObject(users);
			oos.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public void saveToFile(String savePath){
     	try {
     		Files.deleteIfExists(Paths.get("save/" + savePath));
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("save/" + savePath));
 			oos.writeObject(users);
 			oos.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public boolean loadFromFile(String loadPath){
     	try{
     		if(Files.exists(Paths.get("save/" + loadPath))){
     			ObjectInputStream ois = new ObjectInputStream(new FileInputStream("save/" + loadPath));
     			users = (UserManager)ois.readObject();
     			ois.close();
     			return true;
     		}
     	}
     	catch(Exception e){
     		e.printStackTrace();
     	}
     	return false;
     }
 
     public User getCurrentUser() {
         return users.getCurrentUser();
     }
 
     public UserManager getUsers() {
         return users;
     }
 
     public static Organizer getInstance() {
         if ( instance == null )
             instance = new Organizer();
         return instance;
     }
 
     public void update() {
         setChanged();
     }
 }
