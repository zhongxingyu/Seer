 package control;
 /* This program is licensed under the terms of the GPLV3 or newer*/
 /* Written by Johannes Putzke*/
 /* eMail: die_eule@gmx.net*/
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Vector;
 
 public class Control_http_Shoutcast_3 {
 	private BufferedReader bw = null;
 	private InputStream readGenresStream = null;
 	private InputStream readStream = null;
 	private String text = "";
 	private Vector<String[]> streams = new Vector<String[]>(0, 1);
 	private Boolean stopSearching = false; // stop an action
 	private int currentPage = 0;
 	private int totalPages = 0;
 	private int maxResults = 100;
 	
 	// streaminfo[0] = Name
 	// streaminfo[1] = Website
 	// streaminfo[2] = Genre
 	// streaminfo[3] = now Playing
 	// streaminfo[4] = Listeners/MaxListeners
 	// streaminfo[5] = Bitrate
 	// streaminfo[6] = Format
 	// streaminfo[7] = Link
 
 	/**
 	 * the default constructor. Here is nothing to do at the moment
 	 */
 	public Control_http_Shoutcast_3() {
 
 	}
 	
 	/**
 	 * set the to laod page of the first one
 	 */
 	public void resetPages() {
 		currentPage=0;
 		totalPages=0;
 	}
 	
 	/**
 	 * Increases the to load page by one. This will only happen
 	 * if the current page is not the last one
 	 */
 	public void nextPage() {
 		if(totalPages > currentPage) {
 			currentPage++;
 		}
 	}
 	
 	/**
 	 * decreases the to load page by one. This will only happen
 	 * if the current page is not the first
 	 */
 	public void prevoiousPage() {
 		if( currentPage >= 1) {
 			currentPage--;
 //			
 //			if(totalPages >=2) {
 //				totalPages=totalPages-2;
 //			}
 		}
 	}
 	
 	/**
 	 * gives the current page back, which is loaded from shoutcast
 	 * @return the current page for the current loaded search
 	 */
 	public int getCurrentPage() {
 		return currentPage+1;
 	}
 	
 	/**
 	 * gives the total amount of pages back, which represents this
 	 * search which all results
 	 * @return All pages as an integer
 	 */
 	public int getTotalPages() {
 		return totalPages+1;
 	}
 	
 	/**
 	 * Returns the number of pages available with streams if 
 	 * this search. 
 	 * @return [0] returns the current page
 	 * [1] returns the max available pages
 	 */
 	public int[] getPages() {
 		int[] pages = new int[2];
 		pages[0] = currentPage;
 		pages[1] = totalPages;
 		
 		return pages;
 	}
 	
 	/**
 	 * This method look for the stream address + port in a .pls or .m3u file and
 	 * return the first one found. If it found a stream than it returns it. else
 	 * it returns an empty string
 	 * 
 	 * @param streamURL
 	 * @return
 	 */
 	public String getfirstStreamFromURL(String streamURL) {
 		String url = "";
 		String tmp = "";
 		Boolean breakLook = false;
 
 		try {
 			// create URL
 			URL stream = new URL(streamURL);
 
 			// create Stream and open it to url
 			readStream = stream.openStream();
 
 			// create an buffered reader
 			bw = new BufferedReader(new InputStreamReader(readStream));
 
 			// read data and look for first address
 			while (!breakLook && (tmp = bw.readLine()) != null) {
 
 				// if it a .pls simply look for line File
 				if (tmp.contains("File")) {
 					// after = start the address
 					int startAddress = tmp.indexOf("=");
 
 					// read into url
 					url = tmp.substring(startAddress + 1);
 
 					// stop -> run finally() and return url
 					breakLook = true;
 				}
 				// if a line contains the line http
 				// StreamRipStar think its the address
 				else if (tmp.contains("http://")) {
 					url = tmp;
 					// stop -> run finally() and return url
 					breakLook = true;
 				}
 			}
 		} catch (Exception e) {
 			SRSOutput.getInstance().log("Could not get the playlist file from server");
 			return null;
 		} finally {
 			if (readStream != null) {
 				try {
 					readStream.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 		return url;
 	}
 
 	/**
 	 * Browse the list of stream on shoutcast.com in the given genre and save it
 	 * into an Array of Strings into an vector called streaminfo. streaminfo
 	 * contains following information: 
 	 * 
 	 * streamInfo[0] = Name 
 	 * streamInfo[1] = now Playing 
 	 * streamInfo[2] = Listeners
 	 * streamInfo[3] = Bitrate 
 	 * streamInfo[4] = Format
 	 * streamInfo[5] = Link to stream (earlyer version was it the ID)
 	 * streamInfo[6] = Genres
 	 * streamInfo[7] = Link to Website
 	 * 
 	 * @param genre the keyword (most cases the genre) 
 	 * @param keyword true, if the search should be with keywords 
 	 */
 	public void getStreamsPerGenre(String genre, String keyword) {
 		// make sure, that the Vector of streams is empty
 		streams.removeAllElements();
 		streams.trimToSize();
 		
 		try {
 			int startInt = (currentPage*maxResults)+1;
 			
 			String data = "";
 			
 			//if no genre is defined - its a keyword search
 			if(!genre.trim().equals("") || genre == null) {
 			   data += URLEncoder.encode("action", "UTF-8") + "=" + URLEncoder.encode("sub", "UTF-8");								// action sub = search in the category
 			   data += "&" + URLEncoder.encode("string", "UTF-8") + "=" + URLEncoder.encode(keyword, "UTF-8");						// search string
 			   data += "&" + URLEncoder.encode("cat", "UTF-8") + "=" + URLEncoder.encode(genre, "UTF-8");							// Category=Genre
 			   data += "&" + URLEncoder.encode("start", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(startInt), "UTF-8");		//result start
 			   data += "&" + URLEncoder.encode("amount", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(maxResults), "UTF-8");	//max result per request
 			} else {
 				data += URLEncoder.encode("action", "UTF-8") + "=" + URLEncoder.encode("search", "UTF-8");				// action search/
 			    data += "&" + URLEncoder.encode("string", "UTF-8") + "=" + keyword;				// search string
 			    data += "&" + URLEncoder.encode("cat", "UTF-8") + "=" + URLEncoder.encode(genre, "UTF-8");					// Category=Genre
 			    data += "&" + URLEncoder.encode("start", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(startInt), "UTF-8");	//result start
 			    data += "&" + URLEncoder.encode("amount", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(maxResults), "UTF-8");	//max result per request
 			}
 
 		    // Send the POST request
 		    URL url = new URL("http://www.shoutcast.com/radiolist.cfm?"+data);
 		    SRSOutput.getInstance().logD("Shoutcast query for genres: "+url);
 		    URLConnection conn = url.openConnection();
 		    conn.setDoOutput(true);
 		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 		    wr.write(data);
 		    wr.flush();
 		    wr.close();
 		    
 		    // Get the response
 		    bw = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 
 			// create a stream to save the info from the website
 			String[] streamInfo = new String[8];
 			
 			while (!stopSearching && (text = bw.readLine()) != null) {
 				try {
 					
 					//read until the first <tr> comes
 					if(text.contains("<thead>")) {
 						bw.readLine();
 						bw.readLine();
 						bw.readLine();
 						
 					}
 					
 					//if the "show more" button is present,there are more result. -> make them available
 					else if(text.endsWith("Show more</a>")) {
 						totalPages = startInt / maxResults;
 						totalPages++;
 					}
 					
 					//here starts a stream
 					else if(text.contains("<tr>")) {
 						
 						//the link to the stream
						bw.readLine();
 						text = bw.readLine();
 				
 						//now find the ID for the stream
						streamInfo[5] = text.substring(text.indexOf("\" href=\"")+6, text.indexOf("\">"));
 
 						//the name
 						streamInfo[0] = readNextHtmlLine().trim();
 
 						//the genre
 						streamInfo[6] = readNextHtmlLine().trim();
 						
 						//the currentListeners
 						streamInfo[2] = readNextHtmlLine().trim().replace(",","");
 						
 						//the bitrate
 						streamInfo[3] = readNextHtmlLine().trim();
 
 						//the format
 						streamInfo[4] = readNextHtmlLine().trim();
 
 						//This stream has all information
 						streams.add(streamInfo);
 						
 						//create an new for the next one
 						streamInfo = new String[8];					
 					}
 
 				} catch (NullPointerException e) {
 					SRSOutput.getInstance().logE("Error while loading from shoutcast website: NullPointer");
 					SRSOutput.getInstance().logE("Current text string was: " + text);
 					e.printStackTrace();
 				} catch (StringIndexOutOfBoundsException e) {
 					SRSOutput.getInstance().logE("Error while loading from shoutcast website: IndexOutOfBoundsException");
 					SRSOutput.getInstance().logE("Current text string was: " + text);
 					e.printStackTrace();
 				} catch (NumberFormatException e) {
 					SRSOutput.getInstance().logE("Controled Crash in StreamBrowser");
 					SRSOutput.getInstance().logE("Current text string was: " + text);
 					e.printStackTrace();
 				}
 			}
 		} catch (Exception e) {
 			if (e.getMessage().startsWith("stream is closed")) {
 				stopSearching = true;
 			} else {
 				e.printStackTrace();
 			}
 		} finally {
 			// reset for new run
 			stopSearching = false;
 
 			if (readGenresStream != null) {
 				try {
 					readGenresStream.close();
 					
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 	
 
 	/**
 	 * Browse the list of stream on shoutcast.com in the given genre and save it
 	 * into an Array of Strings into an vector called streaminfo. streaminfo
 	 * contains following information: 
 	 * 
 	 * streamInfo[0] = Name 
 	 * streamInfo[1] = now Playing 
 	 * streamInfo[2] = Listeners
 	 * streamInfo[3] = Bitrate 
 	 * streamInfo[4] = Format
 	 * streamInfo[5] = ID
 	 * streamInfo[6] = Genres
 	 * streamInfo[7] = Link to Website
 	 * 
 	 * @param genre the keyword for searching
 	 * @param keyword true, if the search should be with keywords 
 	 */
 	public void getStreamsPerKeyword(String keyword) {
 		// make sure, that the Vector of streams is empty
 		streams.removeAllElements();
 		streams.trimToSize();
 		
 		try {
 			int startInt = (currentPage*maxResults)+1;
 			
 			// Construct the POST request
 		    String data = URLEncoder.encode("action", "UTF-8") + "=" + URLEncoder.encode("search", "UTF-8");		// action sub/search
 		    data += "&" + URLEncoder.encode("string", "UTF-8") + "=" + URLEncoder.encode(keyword, "UTF-8");				// search string
 		    data += "&" + URLEncoder.encode("cat", "UTF-8") + "=" + URLEncoder.encode("", "UTF-8");					// Category=Genre
 		    data += "&" + URLEncoder.encode("start", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(startInt), "UTF-8");	//result start
 		    data += "&" + URLEncoder.encode("amount", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(maxResults), "UTF-8");	//max result per request
 
 		    // Send the POST request
 		    URL url = new URL("http://www.shoutcast.com/radiolist.cfm?"+data);
 		    SRSOutput.getInstance().logD("Shoutcast query for genres: "+url);
 		    URLConnection conn = url.openConnection();
 		    conn.setDoOutput(true);
 		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 		    wr.write(data);
 		    wr.flush();
 		    wr.close();
 		    
 		    // Get the response
 		    bw = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 
 			// create a stream to save the info from the website
 			String[] streamInfo = new String[8];
 			
 			while (!stopSearching && (text = bw.readLine()) != null) {
 				try {
 					
 					//read until the first <tr> comes
 					if(text.contains("<thead>")) {
 						bw.readLine();
 						bw.readLine();
 						bw.readLine();
 						
 					}
 					
 					//if the "show more" button is present,there are more result. -> make them available
 					else if(text.endsWith("Show more</a>")) {
 						totalPages = startInt / maxResults;
 						totalPages++;
 					}
 					
 					//here starts a stream
 					else if(text.contains("<tr>")) {
 						
 						//the link to the stream
						bw.readLine();
 						text = bw.readLine();
 				
 						//now find the ID for the stream
						streamInfo[5] = text.substring(text.indexOf("\" href=\"")+6, text.indexOf("\">"));
 
 						//the name
 						streamInfo[0] = readNextHtmlLine().trim();
 
 						//the genre
 						streamInfo[6] = readNextHtmlLine().trim();
 						
 						//the currentListeners
 						streamInfo[2] = readNextHtmlLine().trim().replace(",","");
 						
 						//the bitrate
 						streamInfo[3] = readNextHtmlLine().trim();
 
 						//the format
 						streamInfo[4] = readNextHtmlLine().trim();
 
 						//This stream has all information
 						streams.add(streamInfo);
 						
 						//create an new for the next one
 						streamInfo = new String[8];					
 					}
 
 				} catch (NullPointerException e) {
 					SRSOutput.getInstance().logE("Error while loading from shoutcast website: NullPointer");
 					SRSOutput.getInstance().logE("Current text string was: " + text);
 					e.printStackTrace();
 				} catch (StringIndexOutOfBoundsException e) {
 					SRSOutput.getInstance().logE("Error while loading from shoutcast website: IndexOutOfBoundsException");
 					SRSOutput.getInstance().logE("Current text string was: " + text);
 					e.printStackTrace();
 				} catch (NumberFormatException e) {
 					SRSOutput.getInstance().logE("Controled Crash in StreamBrowser");
 					SRSOutput.getInstance().logE("Current text string was: " + text);
 					e.printStackTrace();
 				}
 			}
 		} catch (Exception e) {
 			if (e.getMessage().startsWith("stream is closed")) {
 				stopSearching = true;
 			} else {
 				e.printStackTrace();
 			}
 		} finally {
 			// reset for new run
 			stopSearching = false;
 
 			if (readGenresStream != null) {
 				try {
 					readGenresStream.close();
 					
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Read as long as the representing no-html String is empty from the 
 	 * incoming streamreader
 	 * @return the text after the empty line
 	 */
 	public String readNextHtmlLine() {
 		String next = "";
 		
 		try {
 			while(!stopSearching && (text = bw.readLine()) != null)
 			{
 				while(!stopSearching  && !text.trim().endsWith(">"))
 				{
 					text += bw.readLine();
 				}
 				
 				String noHTML = text.replaceAll("\\<.*?>","").trim();
 				
 				if(noHTML.length() > 0)
 				{
 					return noHTML;
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			return "";
 		}
 		return next;
 	}
 	
 	/**
 	 * This Method return an Vector of Strings witch contains all streams from a
 	 * specific genre
 	 * 
 	 * @return
 	 */
 	public Vector<String[]> getStreams() {
 		return streams;
 	}
 
 	public String getBaseAddress() {
 		String shoutcast = "http://yp.shoutcast.com/sbin/tunein-station.pls?id=";
 		return shoutcast;
 	}
 }
