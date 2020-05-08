 package com.kokotchy.betaSeriesAPI;
 
 import java.io.File;
import java.util.Set;
 
 import com.kokotchy.betaSeriesAPI.api.xmlImpl.BetaSerieApi;
import com.kokotchy.betaSeriesAPI.model.Friend;
 
 /**
  * Start application
  * 
  * @author kokotchy
  */
 public class Main {
 
 	/**
 	 * Start the application with parameters
 	 * 
 	 * @param args
 	 *            Parameters
 	 */
 	public static void main(String[] args) {
 		new Main();
 	}
 
 	/**
 	 * Start the application
 	 */
 	public Main() {
 		UtilsXml.setDebug(true);
 		String[] loadCredentials = Utils.loadCredentials(new File(System
 				.getProperty("user.dir")
 				+ "/src/main/resources/credentials/", "kokotchy"));
 		String token = BetaSerieApi.getMembers().auth(loadCredentials[0],
 				loadCredentials[1]);
		Set<Friend> friends = BetaSerieApi.getMembers().getFriends(token);
		System.out.println("There is " + friends.size() + " friends:");
		for (Friend friend : friends) {
			System.out.println("- " + friend.getName());
 		}
 	}
 }
