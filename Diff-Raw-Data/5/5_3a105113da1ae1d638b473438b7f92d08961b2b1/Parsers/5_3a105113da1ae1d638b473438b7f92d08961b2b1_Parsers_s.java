 package com.objects;
 
 import java.io.IOException;
 
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 public class Parsers {
 	// User parameters
 	private static final String PARAM_UID = "uid";
 	private static final String PARAM_PARKSTATE = "parkstate";
 
 	// Response code parameter
 	private static final String PARAM_RESP_CODE = "responsecode";
 	private static final String RESP_CODE_OK = "OK";
 
 	// Park rate parameters
 	private static final String PARAM_LAT = "lat";
 	private static final String PARAM_LONG = "lon";
 	private static final String PARAM_LOCATION = "location";
 	private static final String PARAM_SPOT = "spot";
 	private static final String PARAM_MIN_TIME = "minTime";
 	private static final String PARAM_MAX_TIME = "maxTime";
 	private static final String PARAM_DEFAULT_RATE = "defaultRate";
 	private static final String PARAM_MIN_INCREMENT = "minIncrement";
 
 	private static final String PARAM_PARKING_INSTANCE_ID = "parkingInstanceId";
 	private static final String PARAM_END_TIME = "endTime";
 
 	// {"fname":"xia@umd.edu","lname":"Mikey","phone":"1337"}
 	public static UserObject parseUser(JsonParser jp) throws IOException {
 		long uid = -1;
 		boolean parkState = false;
 
 		JsonToken t = jp.nextToken();
 		String curr;
 		while (t != null && t != JsonToken.END_OBJECT) {
 			if (t == JsonToken.VALUE_NUMBER_INT) {
 				curr = jp.getCurrentName();
 				if (PARAM_UID.equals(curr)) {
 					uid = jp.getIntValue();
 				} else if (PARAM_PARKSTATE.equals(curr)) {
 					parkState = jp.getIntValue() == 1;
 				}
 			}
 			t = jp.nextToken();
 		}
 		return new UserObject(uid, parkState);
 	}
 
 	public static boolean parseResponseCode(JsonParser jp) throws IOException {
 		JsonToken t = jp.nextToken();
 		String curr;
 		while (t != null && t != JsonToken.END_OBJECT) {
 			if (t == JsonToken.VALUE_STRING) {
 				curr = jp.getCurrentName();
 				if (PARAM_RESP_CODE.equals(curr)) {
 					return RESP_CODE_OK.equals(jp.getText());
 				}
 			}
 		}
 		return false;
 	}
 
 	public static SpotObject parseRate(JsonParser jp) throws IOException {
 		double lat = 0;
 		double lon = 0;
 		String location = "";
 		int spot = 0;
 		int minTime = 1;
 		int maxTime = 3;
 		int defaultRate = 1;
 		int minIncrement = 30;
 
 		JsonToken t = jp.nextToken();
 		String curr;
 		while (t != null && t != JsonToken.END_OBJECT) {
 			curr = jp.getCurrentName();
 			switch (t) {
 				case VALUE_NUMBER_INT: {
 					if (PARAM_SPOT.equals(curr)) {
 						spot = jp.getIntValue();
 					} else if (PARAM_MIN_TIME.equals(curr)) {
 						minTime = jp.getIntValue();
 					} else if (PARAM_MAX_TIME.equals(curr)) {
 						maxTime = jp.getIntValue();
 					} else if (PARAM_DEFAULT_RATE.equals(curr)) {
 						defaultRate = jp.getIntValue();
 					} else if (PARAM_MIN_INCREMENT.equals(curr)) {
 						minIncrement = jp.getIntValue();
 					}
 				}
 				case VALUE_NUMBER_FLOAT: {
 					if (PARAM_LAT.equals(curr)) {
 						lat = jp.getDoubleValue();
 					} else if (PARAM_LONG.equals(curr)) {
 						lon = jp.getDoubleValue();
 					}
 				}
 				case VALUE_STRING: {
 					if (PARAM_LOCATION.equals(curr)) {
 						location = jp.getText();
 					}
 				}
 			}
 		}
 		return new SpotObject(lat, lon, location, spot, minTime, maxTime,
 				defaultRate, minIncrement);
 	}
 
 	public static ParkInstanceObject parseParkInstance(JsonParser jp) throws IOException {
 		long parkInstanceId = 0;
 		long endTime = 0;
 
 		JsonToken t = jp.nextToken();
 		String curr;
 		while (t != null && t != JsonToken.END_OBJECT) {
 			curr = jp.getCurrentName();
 			if (t == JsonToken.VALUE_NUMBER_INT) {
 				if (PARAM_PARKING_INSTANCE_ID.equals(curr)) {
 					parkInstanceId = jp.getLongValue();
 				} else if (PARAM_END_TIME.equals(curr)) {
 					endTime = jp.getLongValue();
 				}
 			}
 		}
 		return new ParkInstanceObject(parkInstanceId, endTime);
 	}
 }
