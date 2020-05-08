 package com.distributed.server;
 
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.List;
 
 public class BookingUtils {
 
	private static final String[] facilityNames= new String[]{ "LT1", "LT2", "LT3", "LT4", "TR1", "SQ1", "SQ2", "TENNIS1"};
 
 	private static final List<String> facList = Arrays.asList(facilityNames);
 	
 	private BookingUtils(){
 		;
 	}
 	/**
 	 * Returns number corresponding to the day of the week
 	 * @param day
 	 * @return int
 	 */
 	public static int getDay(String day){
 		if(day.equalsIgnoreCase("Monday")){
 			return 0;
 		} else if(day.equalsIgnoreCase("Tuesday")){
 			return 1;
 		} else if(day.equalsIgnoreCase("Wednesday")){
 			return 2;
 		} else if(day.equalsIgnoreCase("Thursday")){
 			return 3;
 		} else if(day.equalsIgnoreCase("Friday")){
 			return 4;
 		} else if(day.equalsIgnoreCase("Saturday")){
 			return 5;
 		}else if(day.equalsIgnoreCase("Sunday")){
 			return 6;
 		} else {
 			return -1;
 		}
 	}
     
     public static String getString(int day){
         if (day == 0){
             return "Monday   ";
         } else if (day == 1) {
             return "Tuesday  ";
         } else if (day == 2) {
             return "Wednesday";
         } else if (day == 3) {
             return "Thursday ";
         } else if (day == 4) {
             return "Friday   ";
         } else if (day == 5) {
             return "Saturday ";
         } else if (day == 6) {
             return "Sunday   ";
         } else {
             return "invalid";
         }
     }
 	
     public static int getToday() {
 		Calendar calendar = Calendar.getInstance();
 		int today = calendar.get(Calendar.DAY_OF_WEEK);
 		if (today == 1) { //adjust to our week enumeration
 			today = 6;
 		} else {
 			today = today - 2;
 		}
 		return today;
     }
     
     public static void notifyClient(String msg, InetAddress IP, int port) throws IOException{
     	byte[] dataStream = new byte[2048];
     	dataStream = msg.getBytes();
     	DatagramPacket sendPacket = new DatagramPacket(dataStream, dataStream.length, IP, port);
     	UDPServer.sendNotification(sendPacket);
     }
     
 	public static int getFacID(String faculty){
 		return facList.indexOf(faculty.toUpperCase());
 	}
 
 }
