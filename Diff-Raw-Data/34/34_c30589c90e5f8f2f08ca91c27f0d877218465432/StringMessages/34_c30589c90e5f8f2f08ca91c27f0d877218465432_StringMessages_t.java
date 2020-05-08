 package pl.ug.airport.messages;
 
 import org.omg.CORBA.DynAnyPackage.Invalid;
 
 public enum StringMessages {
	
 	RESPONSE_OK,
 	RESPONSE_REPEAT,
 	//flight control agent
 	LEAVING_AT,
 	FLIGHTPLAN_WAS_CHANGED,
 	//passanger agent
 	RESERVATION,
 	//passanger service agent
 	INFORM_ABOUT_FLIGHT,
 	INFORM_ABOUT_CHANGES,
 	//plane agent
 	CLOSE_TO_AIRPORT,
 	REQUEST_LANDING,
 	PASSANGERS_LEFT,
 	REQUEST_INSPECTION,
 	//staff assignment agent
 	BOARD_PLANE,
 	//tech service agent
 	PLANE_READY;
 	
 	public static StringMessages parseString(String str){
 		switch(str){
 			case "RESPONSE_OK": return RESPONSE_OK; 
 			case "RESPONSE_REPEAT": return RESPONSE_REPEAT;
 			case "LEAVING_AT": return LEAVING_AT;
 			case "FLIGHTPLAN_WAS_CHANGED": return FLIGHTPLAN_WAS_CHANGED;
 			case "RESERVATION": return RESERVATION;
 			case "INFORM_ABOUT_FLIGHT": return INFORM_ABOUT_FLIGHT;
 			case "INFORM_ABOUT_CHANGES": return INFORM_ABOUT_CHANGES ;
 			case "CLOSE_TO_AIRPORT": return CLOSE_TO_AIRPORT;
 			case "REQUEST_LANDING": return REQUEST_LANDING;
 			case "PASSANGERS_LEFT": return PASSANGERS_LEFT;
 			case "REQUEST_INSPECTION": return REQUEST_INSPECTION;
 			case "BOARD_PLANE": return BOARD_PLANE;
 			case "PLANE_READY": return PLANE_READY;
 			default: throw new IllegalArgumentException("String " + str + " is an illegal enum");
 		}
 	}
 }
