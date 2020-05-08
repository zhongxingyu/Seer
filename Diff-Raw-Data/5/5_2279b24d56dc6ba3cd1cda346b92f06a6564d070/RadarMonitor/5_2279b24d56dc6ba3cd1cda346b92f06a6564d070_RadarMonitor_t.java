 package vms;
 
 import java.util.*;
 
 import vms.Alert.AlertType;
 
 import common.Coord;
 import common.UpdateData;
 import common.Vessel;
 
 
 public class RadarMonitor implements ConnectionServer.Observer {
 	private int range;
 	private ArrayList<Vessel> _Vessels = new ArrayList<Vessel>();
 	private List<Observer> _Observers = new ArrayList<Observer>();
 	
 	public interface Observer {
 		public void refresh(List<Alert> alerts, List<Vessel> vessels);
 	}
 	
 	public void setRange(int range) {
 		this.range = range;
 	}
 	
 	public int getVesselCount() {
 		return _Vessels.size();
 	}
 	
 	public List<Vessel> getVessels() {
 		return _Vessels;
 	}
 	
 	public void registerObserver(Observer o) {
 		if (!_Observers.contains(o)) _Observers.add(o);
 	}
 	
 	public void unregisterObserver(Observer o) {
 		_Observers.remove(o);
 	}
 
 	@Override
 	public void update(UpdateData data) {
 		boolean isInList = false;
 		
 		for(int i = 0; i<_Vessels.size(); i++){
 			if(_Vessels.get(i).getId().equals(data.Id) && _Vessels.get(i).getType() == data.Type){
 				_Vessels.get(i).update(data);
 				isInList = true;
 			}
 		}
 		
 		if(!isInList){
 			Vessel newVessel = new Vessel(data.Id, data.Type);
 			newVessel.update(data);
 			_Vessels.add(newVessel);
 		}
 		//Must call refresh after the update is done!
 		refresh(data.Timestamp);
 	}
 
 	@Override
 	public void refresh(Calendar timestamp) {
 		ArrayList<Alert> _Alerts = new ArrayList<Alert>();
 		ArrayList<Vessel> removeVessels = new ArrayList<Vessel>();
 		
 		for(int i=0; i< _Vessels.size(); i++){
 			Vessel v1;
 			v1 = _Vessels.get(i);
 			try {
 				Coord v1Coords = v1.getCoord(timestamp);
 				
 				if(v1Coords.isInRange(range)){
 					for(int j=i+1; j<_Vessels.size(); j++){
 						String risk = "none";
 						Alert newAlert;
 						Vessel v2;
 						
 						v2 = _Vessels.get(j);
 						Coord v2Coords = v2.getCoord(timestamp);
 						
 						double deltaX = v1Coords.x() - v2Coords.x();
 						double deltaY = v1Coords.y() - v2Coords.y();
 						double distance = Math.sqrt(Math.pow(deltaX, 2.0) + Math.pow(deltaY, 2.0));
 						if (distance < 50){
 							risk = "high";
 							newAlert = createAlert(AlertType.HIGHRISK, v1, v2);
 							_Alerts.add(newAlert);
 						}
 						else if (distance < 200 && risk != "high"){
 							risk = "low";
 							newAlert = createAlert(AlertType.LOWRISK, v1, v2);
 							_Alerts.add(newAlert);
 						}
 					}
 				}
 				
 				else{
 					removeVessels.add(v1);
 				}
 			}
 			
 			catch (IllegalStateException e) {
 				System.out.println(e.getMessage());
 			}
 		}
 		
 		for(int i = 0; i < removeVessels.size(); i++){
 			_Vessels.remove(removeVessels.get(i));
 		}
 		
 		for (int i=0; i < _Observers.size(); i++) {
 			_Observers.get(i).refresh(_Alerts,_Vessels);
 		}
 	}
 	
 	private Alert createAlert(AlertType t, Vessel v1, Vessel v2) {
 		List<Vessel> pair = new ArrayList<Vessel>();
 		pair.add(v1);
 		pair.add(v2);
 		return new Alert(t, pair);
 	}
 }
