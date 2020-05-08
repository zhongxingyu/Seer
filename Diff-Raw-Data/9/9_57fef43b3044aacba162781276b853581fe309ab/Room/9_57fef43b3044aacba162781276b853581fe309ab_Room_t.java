 package no.ntnu.fp.model;
 
 import no.ntnu.fp.Client;
 
 public class Room {
     private String description;
     private int capacity, roomnr;
     private boolean projector;
     
     public Room(String description, int capacity, int roomnr, boolean projector) {
         this.description = description;
         this.capacity = capacity;
         this.roomnr = roomnr;
         this.projector = projector;
     }
     
	public static Room loadRoom(int id) {
         return Client.f1;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setTitleDescription(String description) {
         this.description = description;
     }
 
     public int getCapacity() {
         return capacity;
     }
 
     public void setCapacity(int capacity) {
         this.capacity = capacity;
     }
 
 	public int getRoomnr() {
 		return roomnr;
 	}
 
 	public void setRoomnr(int roomnr) {
 		this.roomnr = roomnr;
 	}
 
 	public boolean isProjector() {
 		return projector;
 	}
 
 	public void setProjector(boolean projector) {
 		this.projector = projector;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
     
 }
