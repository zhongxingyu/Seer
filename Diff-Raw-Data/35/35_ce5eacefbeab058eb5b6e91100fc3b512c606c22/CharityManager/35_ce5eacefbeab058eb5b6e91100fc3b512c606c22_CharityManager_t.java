 package systemHibernateManagers;
 
import java.util.ArrayList;
import java.util.Iterator;
 import java.util.List;
 import systemHibernateEntities.Charity;
 import sharedHibernateResources.ConnectionManager;
 
 public class CharityManager {
 	
 	private ConnectionManager conn;
 	
 	public CharityManager(){
 		conn = new ConnectionManager("");
 	} 
 
 	public List<Charity> getCharitiesRequests(){
 		List<Charity> charities = (List<Charity>)conn.getTable("Charity where isVerified = 0 and isActive = 0");
 		return charities;
 	}
 	
 	public List<Charity> getCharities(){
		List<Charity> results = new  ArrayList<Charity>();
 		List<Charity> charities = (List<Charity>)conn.getTable("Charity");
		Iterator<Charity> charities_iter = charities.iterator();
		while(charities_iter.hasNext()){
			Charity char_temp = charities_iter.next();
			if(char_temp.getIsActive()&&char_temp.getIsVerified()){
				results.add(char_temp);
			}
		}
		return results;
 	}
 	
 	public Charity getCharity(int charityID){
 		Charity charity = (Charity)conn.get(Charity.class, charityID);
 		return charity;
 	}
 	public Integer addCharity (Charity newCharity) { 
 		return (Integer) conn.transaction("save",newCharity);
 	}
 }
