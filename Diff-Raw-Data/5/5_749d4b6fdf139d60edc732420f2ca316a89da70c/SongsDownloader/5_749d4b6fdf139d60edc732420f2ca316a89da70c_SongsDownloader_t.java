 /**
  *
  * songs - Command line tool to download songs with
  * Copyright (c) 2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/songs
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.songs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.client.params.CookiePolicy;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.sangupta.jerry.http.WebInvoker;
 import com.sangupta.jerry.http.WebRequest;
 import com.sangupta.jerry.http.WebResponse;
 import com.sangupta.jerry.util.AssertUtils;
 import com.sangupta.jerry.util.ConsoleUtils;
 import com.sangupta.jerry.util.UriUtils;
 
 /**
  * A command-line tool to download movie songs from the site
  * http://songspk.name - this allows you to skip all advertisements
  * and other stuff.
  * 
  * @author sangupta
  *
  */
 public class SongsDownloader {
 	
 	/**
 	 * Holds in-memory cache of anchors that we have found for the first letter
 	 * of the movie title.
 	 */
 	private static final Map<String, Elements> letterCache = new HashMap<String, Elements>();
 	
 	/**
 	 * Main entry handler.
 	 * 
 	 * @param args
 	 */
     public static void main( String[] args ) {
     	System.out.println("Welcome to Song Downloader v1.0.");
     	do {
 	    	String input = ConsoleUtils.readLine("\nEnter the first few letters of the movie name: ", true);
 	        if(AssertUtils.isEmpty(input)) {
 	        	System.out.println("No movie title provided... exiting!");
 	        	return;
 	        }
 	        
 	        downloadMovieSongs(input);
     	} while(true);
     }
     
     /**
      * Download the songs for the given movie prefix.
      * 
      * @param input
      */
     private static void downloadMovieSongs(String input) {
         // find list of all titles with that letter
         input = input.toLowerCase();
         
         Element table;
         WebResponse response;
         Elements anchors;
         Document document;
 
         // letter cache check
         final String letter = String.valueOf(input.charAt(0));
         
         if(!letterCache.containsKey(letter)) {
     		String url;
         	if(Character.isDigit(input.charAt(0))) {
         		url = "http://www.songspk.name/numeric_list.html";
         	} else {
 				url = "http://www.songspk.name/" + input.charAt(0) + "_list.html";
         	}
         	
 			response = WebInvoker.getResponse(url);
 	        if(response == null || !response.isSuccess()) {
 	        	System.out.println("Unable to fetch a list of all movie titles... exiting!");
 	        	return;
 	        }
 	        
 	        // find all anchors in table191
 			document = Jsoup.parse(response.getContent());
 	        if(document == null) {
 	        	System.out.println("Unable to parse web page... exiting!");
 	        	return;
 	        }
 	        
 			table = document.getElementById("table191");
 			anchors = table.getElementsByTag("a");
 	        letterCache.put(letter, anchors);
 	        
 	        if(anchors == null) {
 	        	System.out.println("No movie title found... exiting!");
 	        	return;
 	        }
         } else {
         	anchors = letterCache.get(letter);
         }
         
         // get a movie list of all anchors
         Map<String, String> movies = new HashMap<String, String>();
         for(int index = 0; index < anchors.size(); index++) {
         	Element anchor = anchors.get(index);
         	
         	String title = anchor.text().toLowerCase();
         	String href = anchor.attr("href");
         	if(AssertUtils.isEmpty(href)) {
         		continue;
         	}
         	
         	if(href.endsWith(".html") && title.startsWith(input)) {
         		movies.put(title, href);
         	}
         }
         
         // show the user a list
         if(AssertUtils.isEmpty(movies)) {
         	System.out.println("No movie titles found... exiting!");
         	return;
         }
         
         List<String> titles = new ArrayList<String>(movies.keySet());
         for(int index = 0; index < titles.size(); index++) {
         	System.out.println(index + ": " + titles.get(index));
         }
         
         String download = ConsoleUtils.readLine("\nEnter movie title index to download: ", true);
         if(AssertUtils.isEmpty(download)) {
         	System.out.println("No movie chosen to download songs of... exiting!");
         	return;
         }
         
         int page = -1;
         try {
         	page = Integer.parseInt(download);
         } catch(Exception e) {
         	// eat up
         }
         
         if(page == -1) {
         	System.out.println("Not a valid number!");
         	return;
         }
         
        if(page < 0 || page >= titles.size()) {
        	System.out.println("Number not in range... exiting!");
        	return;
        }
        
         String title = titles.get(page);
         if(AssertUtils.isEmpty(title)) {
         	System.out.println("Wrong index... exiting!");
         	return;
         }
         
         final String movieName = title;
         
         String songsURL = UriUtils.addWebPaths("http://songspk.name", movies.get(title));
         System.out.println("Downloading songs for movie '" + title + "' from URL: " + songsURL);
         
         // start downloading the songs
         response = WebInvoker.getResponse(songsURL);
         if(response == null || !response.isSuccess()) {
         	System.out.println("Unable to fetch songs information for movie... exiting!");
         	return;
         }
         
         // parse html
         document = Jsoup.parse(response.getContent());
         
         // fetch all songs that are embedded in table190
         table = document.getElementById("table190");
         if(table == null) {
         	System.out.println("Document structure mismatch... exiting!");
         	return;
         }
         
         anchors = table.getElementsByTag("a");
         if(anchors == null) {
         	System.out.println("No songs found... exiting!");
         	return;
         }
         
         // loop through all links
         Map<String, String> songs = new HashMap<String, String>();
         for(int index = 0; index < anchors.size(); index++) {
         	Element anchor = anchors.get(index);
         	
         	String songURL = anchor.attr("href");
         	String songName = anchor.text();
         	
         	if(!songURL.contains("?songid=")) {
         		// not a valid link
         		continue;
         	}
         	
         	songs.put(songName, songURL);
         }
         
         // start downloading all songs
         Set<String> songNames = songs.keySet();
         for(String songName : songNames) {
         	String songURL = songs.get(songName);
         	// start downloading and saving
         	downloadSong(movieName, songName, songURL);
         }
     }
     
     private static void downloadSong(String movieName, String songName, String songURL) {
     	System.out.print("Downloading song: " + songName + "...");
     	String folderName = sanitize(movieName);
     	
     	File folder = new File(folderName);
     	folder.mkdirs();
     	
     	String sanitizedName = sanitize(songName) + ".mp3";
 		File song = new File(folder, sanitizedName);
 		if(song.exists() && song.length() > 0) {
 			System.out.println(" song already downloaded with file length greater than zero.. skipping!");
 			return;
 		}
 		
     	try {
     		WebResponse response;
     		do {
 				response = WebRequest.get(songURL).cookiePolicy(CookiePolicy.BEST_MATCH).noRedirects().execute().webResponse();
 	    		if(response == null || response.isClientError() || response.isServerError()) {
 	    			System.out.println("unable to download song '" + songName + "' from URL: " + songURL);
 	    			System.out.print("Response: " + response + " for URL: " + songURL);
 	    			System.out.println("  *** RETRY may work");
 	    			return;
 	    		}
 	    		
 	    		if(response.isRedirect()) {
 	    			songURL = response.getHeaders().get("Location");
 	    			int lastSlash = songURL.lastIndexOf('/');
 	    			songURL = songURL.substring(0, lastSlash) + "/" + UriUtils.encodeURIComponent(songURL.substring(lastSlash + 1));
 	    			continue;
 	    		}
 	    		
 	    		// non-redirect
 	    		break;
     		} while(true);
     		
     		// we have the data stream save it out
     		FileUtils.writeByteArrayToFile(song, response.asBytes());
         	
     		System.out.println("done!");
         	return;
     	} catch(Exception e) {
     		System.out.println("Unable to download file due to reason: ");
     		e.printStackTrace();
     	}
     	
     }
 
 	private static String sanitize(String songName) {
 		char[] array = songName.toCharArray();
 		for(int index = 0; index < array.length; index++) {
 			char c = array[index];
 			if(Character.isDigit(c) || Character.isLetter(c)) {
 				continue;
 			}
 			
 			array[index] = '-';
 		}
 		
 		return new String(array);
 	}
 }
