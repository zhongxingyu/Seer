 package org.espn.app;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonParser;
 
 /**
 * @author David Gray This class is simply an example of how to parse the Json
  *         API for ESPN.
  */
 public class EspnParser {
 
 	private static final String APIKEY = "apiKey";
 	private static final String APIURL = "http://api.espn.com/v1/sports/soccer/eng.1/athletes?apikey=";
 
 	/**
 	 * @param arguments
 	 *            for main method.
 	 */
 	public static void main(final String[] args) {
		try {
			final JsonArray athletes = getAthletesJsonArray();
			for (final JsonElement athlete : athletes) {
				System.out.println(athlete.getAsJsonObject().get("fullName"));
 			}
		} catch (Exception e) {
			e.printStackTrace();
 		}
 	}
 
	private static JsonArray getAthletesJsonArray() throws Exception {
		final String json = readUrl(getUrl());
 		final JsonArray sports = getSportsJsonArray(json);
 		final JsonElement league = sports.get(0);
 		return league.getAsJsonObject().get("leagues").getAsJsonArray().get(0)
 				.getAsJsonObject().get("athletes").getAsJsonArray();
 	}
 
 	private static JsonArray getSportsJsonArray(final String json) {
 		final JsonArray sports = new JsonParser().parse(json).getAsJsonObject()
 				.get("sports").getAsJsonArray();
 		return sports;
 	}
 
	private static String getUrl() {
		return APIURL + APIKEY;
 	}
 
 	private static String readUrl(final String urlString) throws Exception {
 		BufferedReader reader = null;
 		try {
 			final URL url = new URL(urlString);
 			reader = new BufferedReader(new InputStreamReader(url.openStream()));
 			final StringBuffer buffer = new StringBuffer();
 			int read;
 			final char[] chars = new char[1024];
 			while ((read = reader.read(chars)) != -1) {
 				buffer.append(chars, 0, read);
 			}
 			return buffer.toString();
 		} finally {
 			if (reader != null) {
 				reader.close();
 			}
 		}
 	}
 
 }
