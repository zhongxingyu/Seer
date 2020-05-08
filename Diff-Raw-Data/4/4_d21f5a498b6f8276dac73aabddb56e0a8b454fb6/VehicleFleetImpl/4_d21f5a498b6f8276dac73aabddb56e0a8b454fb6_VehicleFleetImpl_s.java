 package com.kspichale.mock_demo;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 
 public class VehicleFleetImpl implements VehicleFleet {
 
 	private final Collection<Vehicle> availableVehicles = new HashSet<Vehicle>();
 	private final Collection<Vehicle> notAvailableVehicles = new HashSet<Vehicle>();
 
 	@Override
 	public void add(Vehicle vehicle) {
 		availableVehicles.add(vehicle);
 	}
 
 	@Override
 	public Collection<Vehicle> getAvailableVehicles() {
 		return Collections.unmodifiableCollection(availableVehicles);
 	}
 
 	@Override
 	public void setAvailability(Vehicle vehicle, boolean available) {
 		if (available) {
 			availableVehicles.add(vehicle);
 			notAvailableVehicles.remove(vehicle);
 		} else {
 			availableVehicles.remove(vehicle);
 			notAvailableVehicles.add(vehicle);
 		}
 	}
 
 	@Override
 	public boolean isAvailable(Vehicle vehicle) {
		
		//return availableVehicles.contains(vehicle);
		return false;
 	}
 
 }
