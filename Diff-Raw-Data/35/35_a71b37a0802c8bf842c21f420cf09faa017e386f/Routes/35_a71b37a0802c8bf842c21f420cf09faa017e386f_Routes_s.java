 package utils;
 
 /*
  * Must maintain exact mapping with columns in table "routes" in database
  */
 public class Routes {
 	
 	private String start;
 	private String end;
 	private int routeID;
 	
 	
 	public Routes(String start, String end, int routeID) {
 		super();
 		this.start = start;
 		this.end = end;
 		this.routeID = routeID;
 	}
 	
 	public String getStart() {
 		return start;
 	}
 	public void setStart(String start) {
 		this.start = start;
 	}
 	public String getEnd() {
 		return end;
 	}
 	public void setEnd(String end) {
 		this.end = end;
 	}
 	public int getRouteID() {
 		return routeID;
 	}
 	public void setRouteID(int routeID) {
		this.routeID = routeID;
 	}
 	
 
 }
