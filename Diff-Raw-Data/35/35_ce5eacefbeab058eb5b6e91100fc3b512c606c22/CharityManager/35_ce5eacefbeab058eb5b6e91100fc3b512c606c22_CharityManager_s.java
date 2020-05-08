 package systemHibernateManagers;
 
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
 		List<Charity> charities = (List<Charity>)conn.getTable("Charity");
		return charities;
 	}
 	
 	public Charity getCharity(int charityID){
 		Charity charity = (Charity)conn.get(Charity.class, charityID);
 		return charity;
 	}
 	public Integer addCharity (Charity newCharity) { 
 		return (Integer) conn.transaction("save",newCharity);
 	}
 }
