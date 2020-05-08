 package pl.ug.airport.messages;
 
 public class AgentAddresses {
 	
 	private static String baseAddress = "";
 	
	private static String planeAgentAddress = "jade_plane_";
	private static String passangerAgentAddress = "jade_passanger_";
	private static String passangerServiceAgentAddress = "jade_service_passanger";
	private static String techServiceAgentAddress = "jade_service_tech";
	private static String flightAgentAddress = "jade_control_flight";
	private static String staffAgentAddress = "jade_control_staff";
 	
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
