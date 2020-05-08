 package com.parking.main.model;
 
 import java.util.Date;
 import java.util.List;
 
 public class ParkingLotInformation {
 
	private static int counter = -1; 
	
	private int id ; //default value which will be incremented on init in constructor
 	
 	private String name;
 	
 	private String address;
 	
 	private String photoURL;
 	
 	private int totalSpots;
 	
 	private Double rate;
 	
 	private List <Spot> spaces; 
 	
 	public ParkingLotInformation(){
		counter += 1;
		this.id = counter;
 	}
 	
 	public int getId() {
 		return id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	public String getAddress() {
 		return address;
 	}
 
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 
 	public String getPhotoURL() {
 		return photoURL;
 	}
 
 
 	public void setPhotoURL(String photoURL) {
 		this.photoURL = photoURL;
 	}
 
 
 	public int getTotalSpots() {
 		return totalSpots;
 	}
 
 
 	public void setTotalSpots(int totalSpots) {
 		this.totalSpots = totalSpots;
 	}
 
 
 	public List<Spot> getSpaces() {
 		return spaces;
 	}
 
 
 	public void setSpaces(List<Spot> spaces) {
 		this.spaces = spaces;
 	}
 
 
 	public Double getRate() {
 		return rate;
 	}
 
 	public void setRate(Double rate) {
 		this.rate = rate;
 	}
 
 
 	public class Spot {
 		private int spotId = -1;
 		
 		private List<Booking> bookings;
 		
 		public Spot (){
 			this.spotId += 1;
 		}
 		
 		public int getSpotId() {
 			return spotId;
 		}
 
 		public List<Booking> getBookings() {
 			return bookings;
 		}
 
 		public void setBookings(List<Booking> bookings) {
 			this.bookings = bookings;
 		}
 		
 		public class Booking {
 			
 			private long startTime;
 			
 			private long endTime;
 			
 			public Booking (){
 				this.startTime = new Date().getTime();
 				
 				long time = System.currentTimeMillis() + 2 * 60 * 60 * 1000;
 				
 				this.endTime = new Date(time).getTime();
 				
 			}
 			
 			public long getStartTime() {
 				return startTime;
 			}
 			public void setStartTime(int startTime) {
 				this.startTime = startTime;
 			}
 			
 			public long getEndTime() {
 				return endTime;
 			}
 			public void setEndTime(int endTime) {
 				this.endTime = endTime;
 			}
 			
 		}
 		
 	}
 	
 
 }
