 package org.touchirc;
 
 import org.touchirc.db.Database;
 import org.touchirc.model.Profile;
 import org.touchirc.model.Server;
 
 import android.content.Context;
 import android.util.SparseArray;
 
 public class TouchIrc{
 	
 	private static TouchIrc instance;
 	
 	private SparseArray<Server> availableServers;
 	private SparseArray<Profile> availableProfiles;
 	
 	private int idDefaultProfile;
 	private boolean loaded;
 	
 	public TouchIrc(){
 		idDefaultProfile = -1; // set to null
 		loaded = false;
 	}
 	
 	public void load(Context c) {
 		// always load Profiles before Servers !
 		if(!loaded){
 			Database db = new Database(c);
 			// Profiles have to be load before Servers (linkedProfile in Servers)
 			availableProfiles = db.getProfileList();
 			availableServers = db.getServerList();
 			db.close();		
 
 			// TODO Load default profile
 			
 			loaded = true;
 		}
 	}
 
 	public static TouchIrc getInstance(){
 		if(instance == null)
 			instance = new TouchIrc();
 		
 		return instance;
 	}
 	
 
 	/*   SERVERS   */
 	public SparseArray<Server> getAvailableServers(){
 		return availableServers;
 	}
 	
 	public boolean addServer(Server server, Context c){
 		for(int i = 0 ; i < availableServers.size() ; i++){
 			if(availableServers.valueAt(i).equals(server))
 					return false;
 		}
 		Database db = new Database(c);
 		int idServer = db.addServer(server);
 		db.close();
 		availableServers.put(idServer, server);
 		return true;
 	}
 	
 	public void updateServer(int idServer, Server server, Context c){
 		Database db = new Database(c);
 		if(db.updateServer(idServer, server))
 			System.out.println("Update success");
 		db.close();	
 		availableServers.put(idServer,server);
 	}
 	
 	public boolean deleteServer(int idServer, Context c){
 		Database db = new Database(c);
 		if(!db.deleteServer(idServer))
 			return false;
 		db.close();	
 		availableServers.remove(idServer);
 		return true;
 	}
 	
 	/*   PROFILES   */
 	public SparseArray<Profile> getAvailableProfiles(){
 		return availableProfiles;
 	}
 	
 	public boolean addProfile(Profile profile, Context c){
 		for(int i = 0 ; i < availableProfiles.size() ; i++){
 			if(availableProfiles.valueAt(i).equals(profile))
 				return false;
 		}
 		
 		Database db = new Database(c);
 		int idProfile = db.addProfile(profile);
 		db.close();
 		availableProfiles.put(idProfile, profile);
 		return true;
 	}
 	
 	public void updateProfile(int idProfile, Profile profile, Context c){
 		Database db = new Database(c);
 		db.updateProfile(idProfile, profile);
 		db.close();	
 		availableProfiles.put(idProfile, profile);
 			
 	}
 	
 	public boolean deleteProfile(int idProfile, Context c){
 		Database db = new Database(c);
 		if(!db.deleteProfile(idProfile))
 			return false;
		db.close();
 		this.availableProfiles.delete(idProfile);
		if (getIdDefaultProfile() == idProfile)
			setDefaultProfile(this.availableProfiles.keyAt(0));
 		return true;
 	}
 	
 	public int getIdDefaultProfile(){
 		return (idDefaultProfile != -1) ? idDefaultProfile : 1;
 	}
 	
 	// Use idProfile = 0 to unset profile
 	public void setProfile(int idServer, int idProfile, Context c){
 		Server server = availableServers.get(idServer);
 		if(idProfile == 0){
 			server.setProfile(null);
 		}else{
 			server.setProfile(availableProfiles.get(idProfile));
 		}
 		System.out.println("New profile linked = " + server.getProfile());
 		updateServer(idServer, server, c);
 	}
 	
 	public Profile getDefaultProfile(){
 		return this.availableProfiles.get(getIdDefaultProfile());
 	}
 
 
 	public boolean setDefaultProfile(int id) {
 		idDefaultProfile = id;
 		// TODO 
 		return true;
 	}
 
 
 	
 }
