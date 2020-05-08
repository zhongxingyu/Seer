 package pl.ug.airport.messages;
 
 public class AgentAddresses {
 	
 	private static String baseAddress = "";
 	
	private static String planeAgentAddress = "plane_";
	private static String passangerAgentAddress = "passanger_";
	private static String passangerServiceAgentAddress = "service_passanger";
	private static String techServiceAgentAddress = "service_tech";
	private static String flightAgentAddress = "control_flight";
	private static String staffAgentAddress = "control_staff";
 	
 	private static String lang = "Polish"; 
 	
 	public static String getLang() {
 		return lang;
 	}
 	public static String getBaseAddress() {
 		return baseAddress;
 	}
 	public static String getPlaneAgentAddress(int num) {
 		return planeAgentAddress + num;
 	}
 	public static String getPassangerAgentAddress(int num) {
 		return passangerAgentAddress + num;
 	}
 	public static String getPassangerServiceAgentAddress() {
 		return passangerServiceAgentAddress;
 	}
 	public static String getTechServiceAgentAddress() {
 		return techServiceAgentAddress;
 	}
 	public static String getFlightAgentAddress() {
 		return flightAgentAddress;
 	}
 	public static String getStaffAgentAddress() {
 		return staffAgentAddress;
 	}
 }
