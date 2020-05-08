 package com.mattkula.se350.elevatorsimulator.building;
 
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.Date;
 import java.util.Random;
 
 import com.mattkula.se350.elevatorsimulator.elevator.Elevator;
 import com.mattkula.se350.elevatorsimulator.elevator.ElevatorFactory;
 import com.mattkula.se350.elevatorsimulator.exceptions.InvalidArgumentException;
 
 
 
 public class Building {
 	
 	ArrayList<Elevator> elevators;
 	
 	Random r; // TODO remove
 	
	private static Date timer;
	
 	public Building(int numOfFloors, int numOfElevators) throws InvalidArgumentException{
 		FloorManager.initialize(numOfFloors);
 		
		timer = new Date();
 		elevators = new ArrayList<Elevator>();
 		
 		for(int i=1; i <= numOfElevators; i++){
 			elevators.add(ElevatorFactory.build(i));
 		}
 	}
 	
 	public void startSimulation() throws Exception{
 		for(Elevator e : elevators){
 			Thread t = new Thread(e);
 			t.start();
 		}
 		
 	}
 	
 	public void sendRequestToElevator(int elevatorId, int i){
 		System.out.printf("%s Sending Elevator %d to Floor %d.\n", Building.getTimeString(), elevatorId, i);
 		Elevator e = elevators.get(elevatorId - 1);
 		synchronized(e){
 			e.notify();
 			e.addDestination(i);
 		}
 	}
 	
 	public static String getTimeString(){
 		Calendar c = Calendar.getInstance();
 		return String.format("%02d:%02d:%02d ", c.get(Calendar.HOUR), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
 	}
 	
 }
