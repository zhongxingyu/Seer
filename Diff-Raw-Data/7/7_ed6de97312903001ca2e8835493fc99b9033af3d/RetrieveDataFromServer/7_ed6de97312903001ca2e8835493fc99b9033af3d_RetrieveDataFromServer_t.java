 package com.application.takeacoffee;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.application.commons.Common.ReviewStatusEnum;
 import com.google.gson.Gson;
 
 import java.util.ArrayList;
 
 public class RetrieveDataFromServer {
 	// private final static String TAG ="retrieveDataFromServer";
 
 	static ArrayList<CoffeMachine> getCoffeMachineData() {
 		String JSONData = getData();
 
 		ArrayList<CoffeMachine> dataArray = parseData(JSONData);
 		return dataArray;
 	}
 
 	private static String getData() {
 		String JSONData = "{'coffe_machine_data' : [{'coffe_machine_id':'STATIC_COFFEMACHINEID_1', 'coffe_machine_name':'Fin Machine','coffe_machine_address':'Main Street - London', 'coffe_machine_reviews' : [] }, {'coffe_machine_id':'STATIC_COFFEMACHINEID_2','coffe_machine_name':'Hey Machine','coffe_machine_address':'Even village - Mexico', 'coffe_machine_reviews' : [{'review_id':'STATIC_REVIEWID_1', 'review_username':'Mike pp', 'review_comment':'this is the comment on machine', 'review_status':'NOT_BAD'}, {'review_id':'STATIC_REVIEWID_2', 'review_username':'Henry d', 'review_comment':'this is the comment on machine cos I want to say thats nothing in front of your problems didnt u agree with me?', 'review_status':'GOOD'}] }] }";
 
 		return JSONData;
 	}
 
 	private static ArrayList<CoffeMachine> parseData(String data) {
 		try {
 
 			JSONObject jsonObj = (JSONObject) new JSONObject(data);
 			JSONArray jsonArray = jsonObj.getJSONArray("coffe_machine_data");
 
 			ArrayList<CoffeMachine> dataArray = new ArrayList<CoffeMachine>();
 
 			for (int i = 0; i < jsonArray.length(); i++) {
 				JSONObject coffeMachineObj = jsonArray.getJSONObject(i);
 
 				String name = coffeMachineObj.getString("coffe_machine_name");
 				String address = coffeMachineObj
 						.getString("coffe_machine_address");
 				String coffeMachineId = coffeMachineObj
 						.getString("coffe_machine_id");
 
 				JSONArray reviewsArray = coffeMachineObj
 						.getJSONArray("coffe_machine_reviews");
 				ArrayList<Review> reviewsList = new ArrayList<Review>();
 				for (int j = 0; j < reviewsArray.length(); j++) {
 					JSONObject reviewObj = reviewsArray.getJSONObject(j);
 
 					String reviewId = reviewObj.getString("review_id");
 					String reviewUsername = reviewObj
 							.getString("review_username");
 					String reviewComment = reviewObj
 							.getString("review_comment");
//					int reviewStatus = reviewObj
	//						.getInt("review_status");
 
 					
 					
 					reviewsList.add(new Review(reviewId, reviewUsername,
							reviewComment, ReviewStatusEnum.NOT_BAD));
 				}
 
 				dataArray.add(new CoffeMachine(coffeMachineId, name, address,
 						reviewsList));
 			}
 			return dataArray;
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static String encodeToJSONData(
 			ArrayList<CoffeMachine> coffeMachineList) {
 		try {
 			Gson g = new Gson();
 			String jsonString = g.toJson(coffeMachineList);
 			return jsonString;
 /*			if (coffeMachineList.size() != 0) {
 				for (int i = 0; i < coffeMachineList.size(); i++) {
 					CoffeMachine coffeMachineObj = coffeMachineList.get(i);
 					ArrayList<Review> reviewList = coffeMachineObj
 							.getReviewList();
 					if (reviewList.size() != 0) {
 						for (int j = 0; j < reviewList.size(); j++) {
 							Review reviewObj = reviewList.get(j);
 						}
 					}
 				}
 			}*/
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
