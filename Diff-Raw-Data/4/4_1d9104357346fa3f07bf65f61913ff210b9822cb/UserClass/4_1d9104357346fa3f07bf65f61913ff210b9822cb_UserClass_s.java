 package classes;
 
 import java.util.ArrayList;
 
public class UserClass {
 	String mail;
 	String name;
 	
 	public UserClass(String n, String m) {
 		mail = m;
 		name = n;
 	}
 	
 	public UserClass(String m) {
 		mail = m;
 		name = "Unknown user";
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getMail() {
 		return mail;
 	}
 	
 	public void setName(String n) {
 		if(n != null)
 			name = n;
 	}
 	
 	public void setMail(String m) {
 		mail = m;
 	}
 	
 	public String toString() {
 		return new String(name+" : "+mail);
 	}
 	
 	public boolean isInArray(ArrayList<UserClass> array) {
 		for(int i=0;i<array.size();i++) {
 			if(mail.equals(array.get(i).getMail()))
 				return true;
 		}
 		return false;
 	}
 }
