 package com.photon.connecttodoor.controller;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class AttendanceListService {
 	public String handleRequestAttendanceList (final String searchParameter, final String searchingValue, final String dateStart, final String dateEnd){
 		final HttpAdapter httpAdapter = new HttpAdapter();
 		String moduleLogin ="/attendance-list";
 		String responseString ;
 		JSONObject response = null ;
 		JSONObject postBody = new JSONObject();
 		try {	
 			if(searchParameter.equalsIgnoreCase("username")){
				postBody.put("username", searchingValue);
 				postBody.put("date_start", dateStart);
 				postBody.put("date_end", dateEnd);
 			}else if(searchParameter.equalsIgnoreCase("employeeID")){
 				postBody.put("employee_id", searchingValue);
 				postBody.put("date_start", dateStart);
 				postBody.put("date_end", dateEnd);
 			}else if(searchParameter.equalsIgnoreCase("projectID")){
 				postBody.put("project_id", searchingValue);
 				postBody.put("date_start", dateStart);
 				postBody.put("date_end", dateEnd);
 			}else{
 				postBody.put("date_start", dateStart);
 				postBody.put("date_end", dateEnd);
 			}
 
 			String postBodyString = postBody.toString();
 			final String jsonString = httpAdapter.sendPostRequest(postBodyString, moduleLogin);
 			response = new JSONObject(jsonString);
 		}catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		responseString = response.toString();
 		return responseString;
 	}
 }
