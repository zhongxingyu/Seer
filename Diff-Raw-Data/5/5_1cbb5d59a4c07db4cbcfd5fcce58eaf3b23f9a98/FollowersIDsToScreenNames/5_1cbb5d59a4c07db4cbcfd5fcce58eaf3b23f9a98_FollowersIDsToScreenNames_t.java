 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * @author Fernando José Iglesias García
  */
 
 public class FollowersIDsToScreenNames {
 
 	/**
 	 * Auxiliary structure to keep track of the IDs that have already been 
 	 * associated with a screen name and those that have not
 	 */
 	public static HashMap<String, String> idToScreenName = 
 			new HashMap<String, String>();
 
 	private static int nIDsResolved = 0;
 
 	private static int nIDsTotal = 0;
 	
 	private static int nIDsNew = 0;
 	
 	/**
 	 * Show some debugging information
 	 */
 	public static final boolean DEBUG_OUT = false;
 	
 	/**
 	 * TODO explanation
 	 */
 	public static void main(String[] args) {
 
 		if (args.length < 1) {
 			System.err.println("usage: FollowersIDsToScreenNames " + 
 					"<followersFile>");
 			return;
 		}
 		
 		// Read the available information from the file and populate the 
 		// HashMap
 		try {
 
 			File file = new File(args[0]);
 			
 			if (file.exists()) {
 				
 				BufferedReader br = new BufferedReader(new FileReader(file));
 				String line;
 				while ( (line = br.readLine()) != null ) {
 					String[] tokens = line.split(";");
 					
 					if (tokens.length < 2) {
 						idToScreenName.put(tokens[0], null);
 					} else {
 						idToScreenName.put(tokens[0], tokens[1]);
 						nIDsResolved++;
 					}
 					
 					nIDsTotal++;
 				}
 				br.close();
 				
 			} else {
 				System.err.println("File " + args[0] + "not found");
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Followers file loaded, " + nIDsResolved + 
 				" IDs out of " + nIDsTotal + " are associated");
 		
 		if (DEBUG_OUT) {
 			System.out.println(">>>> State of the HashMap after reading");
 			
 			// Display the state of the HashMap
 			Iterator<String> it = idToScreenName.keySet().iterator();
 			while (it.hasNext()) {
 				String key = it.next();
 				String value = idToScreenName.get(key);
 				if (value != null)
 					System.out.println(key + " -> " + value);
 				else
 					System.out.println(key);
 			}
 		}
 		
 		// Get the screen names for the IDs that have not been associated yet
 		Iterator<String>  it = idToScreenName.keySet().iterator();
 		while (it.hasNext()) {
 			
 			String key = it.next();
 			String value = idToScreenName.get(key);
 			
 			if (value == null) {
 				
 				// Get the screen_name associated to this follower ID
 				
 				try {
 					// Get user information in .json
 					URL url = new URL("http://api.twitter.com/1/users/" +
 							"lookup.json?sonoa&user_id=" + key);
 				
 					BufferedReader urlInput = new BufferedReader(
 								new InputStreamReader(url.openStream()));
 					
 					// Create JSON array, it will have just one element
 					JSONArray jsonArray = new JSONArray(urlInput.readLine());
 					urlInput.close();
 					
 					// Get user's screen_name
 					value = ((JSONObject) jsonArray.get(0) ).get("screen_name")
 								       .toString();
 
 					// Overwrite the previous value associated to this key
 					idToScreenName.put(key, value);
 
 					nIDsNew++;
				} catch (FileNotFoundException e) {
					System.out.println("ID " + key + " not found!!");
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					System.out.println("Limit reached, " + nIDsNew + 
 						" new IDs associated, saving HashMap ...");
 					saveIDsToScreenNames(args[0]);
 					return;
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}
 		
 		// All the IDs have been associated
 		System.out.println("Assocation complete! Saving HashMap ...");
 		saveIDsToScreenNames(args[0]);
 	}
 
 	private static void saveIDsToScreenNames(String fname) {
 
 		if (DEBUG_OUT) {
 			System.out.println(">>>> State of the HashMap before saving");
 
 			// Display the state of the HashMap
 			Iterator<String> it = idToScreenName.keySet().iterator();
 			while (it.hasNext()) {
 				String key = it.next();
 				String value = idToScreenName.get(key);
 				if (value != null)
 					System.out.println(key + " -> " + value);
 				else
 					System.out.println(key);
 			}
 		}
 		
 		try {
 			File file = new File(fname);
 			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 			
 			// Write the associations in the file
 			Iterator<String> it = idToScreenName.keySet().iterator();
 			while (it.hasNext()) {
 				String key = it.next();
 				String value = idToScreenName.get(key);
 				if (value != null)
 					bw.write(key + ";" + value + ((it.hasNext() ? "\n" : "")));
 				else
 					bw.write(key + ";" + ((it.hasNext() ? "\n" : "")));
 			}
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
