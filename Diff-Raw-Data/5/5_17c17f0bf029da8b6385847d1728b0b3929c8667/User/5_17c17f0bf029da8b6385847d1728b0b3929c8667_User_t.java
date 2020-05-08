 package tnm082.challenge;
 
 //import Mission.jaca;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
     * Kodad av: Flaaten	
     * Task nr: 3
     * Datum: 2012-03-22
     * Estimerad tid: 2 h
     * Faktisk tid: 3 h
     * Testad/av: Nej
     * Utcheckad: Nej
     
     */
 
 public class User {
 	private String name, pass;
 	private List<Mission> acceptedMissions, completedMissions;
 	
 	public User() {
 		this.name = "klas";
 		this.pass = "kalas";
 		
 		// En lista ver uppdrag som ska utfras
 		this.acceptedMissions = new ArrayList<Mission>();
 		
 		// En lista ver uppdrag som anvndaren har klarat
 		this.completedMissions = new ArrayList<Mission>();
 	}
 	public User(String new_name, String new_pass, List<Mission> new_accepted, List<Mission> new_completed) {
 		this.name = new_name;
 		this.pass = new_pass;
 		
 		for(int i = 0; i < new_accepted.size();i++)
 		{
 			this.acceptedMissions.add(i, new_accepted.get(i));
 		}
 		for(int i = 0; i < new_completed.size();i++)
 		{
 			this.completedMissions.add(i, new_completed.get(i));
 		}
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getPass() {
 		return pass;
 	}
 
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 	public int getId() {
 		//return hmta id frn databasen;
 		return 0;
 	}
 
 	public void completeMission(Mission the_mission)
 	{				
 		for(int i = 0; i < acceptedMissions.size();i++)
 		{
 			if(acceptedMissions.get(i).getId() == the_mission.getId())
 			{
 				acceptedMissions.remove(i); // kan bli fel isf ta fram indexet och ta bort det indexet bara.
				completedMissions.add(completedMissions.size(), the_mission);
 			}
 		}
 		 
 	}
 	public boolean hasAcceptedMission(Mission the_mission)
 	{
 		for(int i = 0; i < acceptedMissions.size();i++)
 		{
 			if(acceptedMissions.get(i).getId() == the_mission.getId())
 			{
 				return true;
 			}
 		}
 		return false;
 		
 		
 	}
 	public void acceptMission(Mission the_mission)
 	{
		acceptedMissions.add(acceptedMissions.size(), the_mission);
 	
 	}
 	public void cancelMission(Mission the_mission)
 	{
 		for(int i = 0; i < acceptedMissions.size();i++)
 		{
 			if(acceptedMissions.get(i).getId() == the_mission.getId())
 			{
 				acceptedMissions.remove(i); // kan bli fel isf ta fram indexet och ta bort det indexet bara.
 			}
 		}
 	}
 }
