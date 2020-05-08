 package es.upm.dit.gsi.shanks.model.scenario;
 
 import java.util.List;
 
 
 /**
  * Scenarios class
  * 
  * This class create the different scenarios
  * 
  * @author Daniel Lara
 * @author Álvaro Carrera
  * @version 0.1.1
  * 
  */
 public class Scenario {
 
 	public String name;
 	public List<Device> devices;
 	public List<Device> ftth;
 	public Scenario scenario;
 
 	public Scenario(String name, List<Device> devices) {
 		this.name = name;
 		this.devices = devices;
 		
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public List<Device> getDevices() {
 		return devices;
 	}
 	
 	public void addDevice(Device device) {
 		
 	}
 
 }
