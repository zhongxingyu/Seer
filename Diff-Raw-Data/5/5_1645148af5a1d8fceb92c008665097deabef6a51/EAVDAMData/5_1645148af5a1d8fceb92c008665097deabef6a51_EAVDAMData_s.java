 package dk.frv.eavdam.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class EAVDAMData {
 
 	private EAVDAMUser user;
 	
 	private List<AISFixedStationData> stations = new ArrayList<AISFixedStationData>();  // TO BE REMOVED
 	
 	private List<ActiveStation> activeStations = new ArrayList<ActiveStation>();
     private List<OtherUserStations> otherUsersStations = new ArrayList<OtherUserStations>();
     private List<Simulation> simulatedStations = new ArrayList<Simulation>();
     private List<AISFixedStationData> oldStations = new ArrayList<AISFixedStationData>();
     
 	public EAVDAMUser getUser() {
 		return user;
 	}
 
 	public void setUser(EAVDAMUser user) {
 		this.user = user;
 	}
 
     public List<ActiveStation> getActiveStations() {
         return activeStations;
     }
     
     public void setActiveStations(List<ActiveStation> activeStations) {
         this.activeStations = activeStations;
     }
 
     public List<OtherUserStations> getOtherUsersStations() {
         return otherUsersStations;
     }
     
     public void setOtherUsersStations(List<OtherUserStations> otherUsersStations) {
         this.otherUsersStations = otherUsersStations;
     }    
 
     public List<Simulation> getSimulatedStations() {
         return simulatedStations;
     }
     
     public void setSimulatedStations(List<Simulation> simulatedStations) {
         this.simulatedStations = simulatedStations;
     }    
     
     public List<AISFixedStationData> getOldStations() {
         return oldStations;
     }
     
     public void setOldStations(List<AISFixedStationData> oldStations) {
         this.oldStations = oldStations;
     }
     
     // TO BE REMOVED -->
 	public AISFixedStationData[] getStations() {
 		return stations.toArray(new AISFixedStationData[stations.size()]);
 	}
 	
 	public void setStations(List<AISFixedStationData> stations) {
 		this.stations.clear();
 		if (stations != null) {
 			this.stations.addAll(stations);
 		}
 	}
 	// <-- TO BE REMOVED
 
 }
