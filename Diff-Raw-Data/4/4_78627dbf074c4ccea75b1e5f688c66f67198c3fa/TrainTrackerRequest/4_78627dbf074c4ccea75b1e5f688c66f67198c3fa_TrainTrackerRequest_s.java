 package com.api.cta;
 
 public class TrainTrackerRequest {
 	public static final String NUMERIC_STATION_ID_PARAMETER = "mapid"; 
 	public static final String NUMERIC_STOP_ID_PARAMETER = "stpid";
 	public static final String MAXIMUM_RESULTS_PARAMETER = "max";
 	public static final String ROUTE_CODE_PARAMETER = "rt";
 	public String numericStationId;
 	public String numericStopId;
 	public int maxResults;
 	public String routeCode;
 	public TrainTrackerRequest(){
 		
 	}
 	public void setNumericStationId(String numericStationId){
 		this.numericStationId = numericStationId;
 	}
 	public void setNumericStopId (String numericStopId){
 		this.numericStopId = numericStopId;
 	}
 	public void setMaxResults(int maxResults){
 		this.maxResults = maxResults;
 	}
 	public void setRouteCode(String routeCode){
 		this.routeCode = routeCode;
 	}
 	public String getNumericStationId(String numericStationId)
 	{
 		return numericStationId;
 	}
 	public String getNumericStopId(String numericStopId){
 		return numericStopId;
 	}
 	public int getMaxResults(){
 		return maxResults;
 	}
 	public String getRouteCode(){
 		return routeCode;
 	}
 
 }
