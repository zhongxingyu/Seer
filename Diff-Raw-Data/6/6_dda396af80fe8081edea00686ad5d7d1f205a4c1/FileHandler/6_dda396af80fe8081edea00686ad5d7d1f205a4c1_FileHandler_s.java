 package en.trudan.ConstructionSites.Data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Properties;
 
 public class FileHandler {
 
 	//FileHandler-part
 	//global stuff
 	//cs = ConstructionSite
 	private static HashMap<String,ConstructionSite> cs = null;
 	private static Properties pl = null;
 	//Folder-variables
 	static String mainDirectory = "plugins/Construction Sites"; //sets the main directory for easy reference
 	static File Storage = new File(mainDirectory + File.separator + "Storage.dat");
 	static File Prices = new File(mainDirectory + File.separator + "Prices.prop");
 	//onEnable
 	public FileHandler(){
 
 		new File(mainDirectory).mkdir();
 		if(Storage.exists()){
 			load();
 		}else{
 			cs = new HashMap<String,ConstructionSite>();
 		}	
 		if(Prices.exists()){
 			load();
 		}else{
 			pl = new Properties();
 		}
 	}
 	public void save(){
 		try{
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Storage));
 			oos.writeObject(cs);
 			oos.flush();
 			oos.close();
 			oos = new ObjectOutputStream(new FileOutputStream(Prices));
 			pl.store(oos, "Pricelist");
 			oos.flush();
 			oos.close();
 			//Handle I/O exceptions
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void load(){
 		try{
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Storage));
 			Object result = ois.readObject();
 			//As long as it is not corrupted, we can just cast the object to hashmap
 			cs = (HashMap<String, ConstructionSite>)result;
 			ois = new ObjectInputStream(new FileInputStream(Prices));
 			pl.load(ois);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	//Here i will add your interface:
 	public ConstructionSite getConstructionSite(String name){
 		if(cs.containsKey(name)){
 			return cs.get(name);
 		}
 		return null;
 	}
 	
 	public Boolean hasConstructionSite(String name){
 		return cs.containsKey(name);
 	}
 
	public void addConstructionSite(String name){
 		if(!cs.containsKey(name)){
 			cs.put(name, new ConstructionSite());
 		}
 	}
 	
 	public int getPrice(int blockID){
 
 		return Integer.parseInt(pl.getProperty("" + blockID, "0"));
 		
 	}
 	
 	public void setPrice(int blockID, int price){
 
 		pl.setProperty("" + blockID, "" + price);
 		
 	}
 }
