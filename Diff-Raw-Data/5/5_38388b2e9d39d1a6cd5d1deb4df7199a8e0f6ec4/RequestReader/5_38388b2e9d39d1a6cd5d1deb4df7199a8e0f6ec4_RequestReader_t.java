 package edu.luc.clearing;
 
 import java.io.Reader;
 import java.lang.reflect.Type;
 import java.util.HashMap;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class RequestReader {
 
	private static final long  TWENTY_EIGHT_SECONDS = 28 * 1000;
 	private CheckParser checkParser;
 	private DataStoreAdapter dataStore;
 	private Clock clock;
 	
 	public RequestReader(DataStoreAdapter dataStore, Clock clock) {
 		this.dataStore = dataStore;
 		this.clock = clock;
 		checkParser = new CheckParser();
 	}
 
 	public String respond(Reader requestData) {
 		Gson gson = new Gson();
 		HashMap<String,Integer> map = new HashMap<String, Integer>();
 		long startTime = clock.currentTime();
 		List<String> checks = gson.fromJson(requestData, requestType());
 		for(String amount: checks){
 			Integer parsedValue = checkParser.parseAmount(amount);
 			if (parsedValue == null){
 				System.err.println("Could not parse amount:  " + amount);
 			}
 			map.put(amount, parsedValue);
 			dataStore.saveCheck("Amount", amount);
			if (timeSince(startTime) > TWENTY_EIGHT_SECONDS) {
 				System.err.println("Ran out of time!");
 				return gson.toJson(map);
 			}
 		}
 		return gson.toJson(map);
 	}
 	
 	private long timeSince (long startTime) {
 		return clock.currentTime() - startTime;
 	}
 
 	private Type requestType() {
 		return new TypeToken<List<String>>(){}.getType();
 	}
 }
