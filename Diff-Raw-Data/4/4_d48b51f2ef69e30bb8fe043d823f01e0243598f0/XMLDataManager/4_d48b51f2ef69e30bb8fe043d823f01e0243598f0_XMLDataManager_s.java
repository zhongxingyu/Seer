 package AuctionHouse.DataContext;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.List;
 
 import AuctionHouse.Mediator.Mediator;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.StaxDriver;
 
 public class XMLDataManager implements DataManager {
 	AuctionHouseDB db;
 	String filename;
 	Person identity;
 
 	public XMLDataManager(String filename) {
 		this.filename = filename;
 	}
 
 	public void init() {
 		XStream xstream = new XStream(new StaxDriver());
 		identity = null;
 		
 		try {
 			db = (AuctionHouseDB) xstream.fromXML(new FileReader(
 					filename));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean isLoginValid(String user, String password, int role) {
 		for (Person p : db.getPeople()) {
 			if (p.getName().equals(user) && p.getRole()==role && p.getPassword().equals(password) ) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public int getRole() {
 		if (identity == null)
 			return -1;
 		else 
 			return identity.getRole();
 	}
 	
 	public List<Service> doLogin(String user, String password, int role) {
 		for (Person p : db.getPeople()) {
			if (p.getName() == user && p.getRole() == role && p.getPassword() == password)
 				identity = p;
 				return p.getServices();
 		}
 		return null;
 	}
 }
