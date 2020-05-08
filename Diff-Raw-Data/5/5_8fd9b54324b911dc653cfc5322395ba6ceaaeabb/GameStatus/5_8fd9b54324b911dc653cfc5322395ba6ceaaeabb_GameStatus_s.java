 package com.hack.regionunlocked;
 
 import java.util.List;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 import java.net.URL;
 import java.net.HttpURLConnection;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
public class GameStatus extends extends AsyncTask<String, Void, String> {
 
 	private String upcCode;
 	private String name;
 	private String checkName;
 	private List<RegionSupportStatusSet> support;
 	private String scandItKey = "key=-rdsomoapvSlt5JjXpPNr0WfBpw-H7f5R9JJMnIbw5J";
 	private boolean found = false;
 	
 	private GameStatusCompleteListener listener;
 	private boolean success = false;
 
 	public GameStatus(String upcCode, GameStatusCompleteListener listener) {
 		this.upcCode = upcCode;
 		this.listener = listener;
 	}
 	/*
 	public void run() {
 		try {
 			this.success = false;
 			listener.setString("1");
 			if (upcCode.equals("")) {
 				listener.setString("2");
 				throw new GameStatusException("No UPC code specified");
 			} else {
 
 				listener.setString("3");
 				this.name = getUPCDatabaseName(upcCode);
 				listener.setString("4");
 				this.checkName = getScandItName(upcCode);
 				listener.setString("5");
 				found = checkNames();
 				listener.setString("6");
 				
 				if (found == true) {
 					listener.setString("7");
 					checkStatusWikia();
 				}
 				listener.setString("8");
 				
 				listener.onGameStatusComplete();
 				listener.setString("9");
 				
 			}
 		} catch (Exception ex) {
 			listener.setString("10");
 			this.success = false;
 			listener.onGameStatusError(ex);
 		}
 	}
 	*/
 	public boolean wasSuccessful() {
 		return success;
 	}
 	
 	
 	
 	
 	@Override
 	protected String doInBackground(String... urls) {
 		  
 		// params comes from the execute() call: params[0] is the url.
 		try {
 			this.success = false;
 			if (upcCode.equals("")) {
 				throw new GameStatusException("No UPC code specified");
 			} else {
 
 				this.name = getUPCDatabaseName(upcCode);
 				this.checkName = getScandItName(upcCode);
 				found = checkNames();
 				
 				if (found == true) {
 					checkStatusWikia();
 				}
 				
 				
 				
 			}
 		} catch (IOException e) {
 			return "Unable to retrieve web page. URL may be invalid.";
 		}
 	}
 	// onPostExecute displays the results of the AsyncTask.
 	@Override
 	protected void onPostExecute() {
 		listener.setString("test end");
 		if (success)
 			listener.onGameStatusComplete();
 		listener.onGameStatusError();
 	}
 	private String downloadUrl(String myurl) throws IOException {
 		InputStream is = null;
 		// Only display the first 500 characters of the retrieved
 		// web page content.
 		int len = 500;
 			
 		try {
 			URL url = new URL(myurl);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setReadTimeout(10000 /* milliseconds */);
 			conn.setConnectTimeout(15000 /* milliseconds */);
 			conn.setRequestMethod("GET");
 			conn.setDoInput(true);
 			// Starts the query
 			conn.connect();
 			int response = conn.getResponseCode();
 			Log.d(DEBUG_TAG, "The response is: " + response);
 			is = conn.getInputStream();
 
 			// Convert the InputStream into a string
 			String contentAsString = readIt(is, len);
 			return contentAsString;
 			
 		// Makes sure that the InputStream is closed after the app is
 		// finished using it.
 		} finally {
 			if (is != null) {
 				is.close();
 			} 
 		}
 	}
 	
 	
 	
 	
 	
 	private String getScandItName(String upcCode) throws GameStatusException {
 		String url = "https://api.scandit.com/v2/products/" + upcCode + "?" + scandItKey;
 
 		// String content = getWebsiteContent(url);
 		String content = downloadUrl(url);
 
 		try{
 		if (content.contains("name")) {
 			String strip = content.substring(18);
 			int check = strip.indexOf("\"");
 			strip = strip.substring(0, check);
 			check = strip.indexOf("0");
 			strip = strip.substring(0, check - 9);
 			System.out.println(strip);
 			checkName = strip;
 			return strip;
 		} else {
 			return "";
 		}
 		}catch(Exception e){
 			throw new GameStatusException("Couldn't find Game name in scandit");
 		}
 	}
 
 	private String getUPCDatabaseName(String upcCode) throws GameStatusException {
 		// 885370201215 = Gears of War 3
 		// String content = getWebsiteContent("http://www.upcdatabase.com/item/" + upcCode);
 		String content = downloadUrl("http://www.upcdatabase.com/item/" + upcCode);
 
 		String regex = "<td>Description</td><td></td><td>(.*?)</td>";
 
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(content);
 
 		try {
 
 			if (!matcher.find())
 				throw new GameStatusException("getUPCDatabaseName fail (No regex match).");
 			return matcher.group(1);
 
 		} catch (Exception ex) {
 			throw new GameStatusException("getUPCDatabaseName fail:\n\n" + ex.getMessage());
 		}
 
 	}
 
 	private boolean checkNames() throws GameStatusException {
 		if (name.contains(checkName))
 			return true;
 		else
 			return false;
 	}
 
 	private void checkStatusWikia() throws GameStatusException {
 
 		if (!this.name.equals("")) {
 			String content = getWebsiteContent("http://gaming.wikia.com/wiki/Region_Free_Xbox_360_Games");
 
 			String regex = "(?i)<td>[\\s]*<a href=\"[^\"]*\"[^>]*>"
 					+ this.name
 					+ "</a>[\\s]*</td>[\\s]*"
 					+ "<td>[\\s]*([\\w]+)[\\s]*</td>[\\s]*"
 					+ // version
 					"<td bgcolor=\"#[A-F0-9]*\">[\\s]*([\\w?]+)[\\s]*</td>[\\s]*"
 					+ // NTSC/J compatibility
 					"<td bgcolor=\"#[A-F0-9]*\">[\\s]*([\\w?]+)[\\s]*</td>[\\s]*"
 					+ // NTSC/U compatibility
 					"<td bgcolor=\"#[A-F0-9]*\">[\\s]*([\\w?]+)[\\s]*</td>"; // PAL
 																				// compatibility
 
 			Pattern pattern = Pattern.compile(regex);
 			Matcher matcher = pattern.matcher(content);
 			
 			boolean matchFound = false;
 			while (matcher.find()) {
 				matchFound = true;
 				success = true;
 
 				GameRegion region = GameRegion.UNKNOWN;
 				if (matcher.group(1).equals("NTSC/J"))
 					region = GameRegion.NTSC_J;
 				if (matcher.group(1).equals("NTSC/U")
 						|| matcher.group(1).equals("US"))
 					region = GameRegion.NTSC_U;
 				if (matcher.group(1).equals("PAL"))
 					region = GameRegion.PAL;
 				if (region != GameRegion.UNKNOWN) {
 					RegionSupportStatusSet set = new RegionSupportStatusSet(
 							region);
 
 					if (matcher.group(2).equals("Yes"))
 						set.supportStatuses.put(GameRegion.NTSC_J,
 								RegionSupportStatus.Yes);
 					if (matcher.group(2).equals("No"))
 						set.supportStatuses.put(GameRegion.NTSC_J,
 								RegionSupportStatus.No);
 					if (matcher.group(2).equals("?"))
 						set.supportStatuses.put(GameRegion.NTSC_J,
 								RegionSupportStatus.Unknown);
 
 					if (matcher.group(3).equals("Yes"))
 						set.supportStatuses.put(GameRegion.NTSC_U,
 								RegionSupportStatus.Yes);
 					if (matcher.group(3).equals("No"))
 						set.supportStatuses.put(GameRegion.NTSC_U,
 								RegionSupportStatus.No);
 					if (matcher.group(3).equals("?"))
 						set.supportStatuses.put(GameRegion.NTSC_U,
 								RegionSupportStatus.Unknown);
 
 					if (matcher.group(4).equals("Yes"))
 						set.supportStatuses.put(GameRegion.PAL,
 								RegionSupportStatus.Yes);
 					if (matcher.group(4).equals("No"))
 						set.supportStatuses.put(GameRegion.PAL,
 								RegionSupportStatus.No);
 					if (matcher.group(4).equals("?"))
 						set.supportStatuses.put(GameRegion.PAL,
 								RegionSupportStatus.Unknown);
 
 					support.add(set);
 				}
 			}
 			
 		}
 	}
 
 	private String getWebsiteContent(String urlString) throws GameStatusException {
 		
 		int i = 0;
 		try {
 			listener.setString("3.1");
 			//InputStream inStream = retrieveStream(urlString); i++;
 			URL url = new URL(urlString);
 			HttpURLConnection con = (HttpURLConnection) url.openConnection();
 			con.setReadTimeout(10000 /* milliseconds */);
 			con.setConnectTimeout(15000 /* milliseconds */);
 			con.setRequestMethod("GET");
 			con.setDoInput(true);
 			con.addRequestProperty("Referer", "http://blog.dahanne.net");
 			con.connect();
 			InputStream inStream = con.getInputStream();
 			// URL url = new URL(urlString);
 			// InputStream inStream = url.openStream();
 			listener.setString("3.2");
 			BufferedReader br = new BufferedReader(new InputStreamReader(
 					inStream)); i++;
 			listener.setString("3.3");
 			String content = "";
 			String line;
 			while ((line = br.readLine()) != null) {
 				content += line; i++;
 			}
 			return content;
 		} catch (Exception ex) {
 			throw new GameStatusException("getWebsiteContent fail:\n\n" + ex);
 		}
 		
 	}
 	
 	private InputStream retrieveStream(String url) throws GameStatusException {
 
 		listener.setString("3.1.1");
         DefaultHttpClient client = new DefaultHttpClient();
 
 		listener.setString("3.1.2");
         HttpGet httpRequest = new HttpGet(url);
 
         try {
 
 			listener.setString("3.1.3");
            HttpResponse httpResponse = client.execute(httpRequest);
 			listener.setString("3.1.4");
            final int statusCode = httpResponse.getStatusLine().getStatusCode();
 
 			listener.setString("3.1.5");
            if (statusCode != HttpStatus.SC_OK) {
         	   throw new GameStatusException("retrieveStream fail:\n\n status code: " + statusCode + 
         			   "\nurl: " + url);
            }
 
 			listener.setString("3.1.6");
            HttpEntity httpEntity = httpResponse.getEntity();
            return httpEntity.getContent();
 
         }
         catch (Exception e) {
         	httpRequest.abort();
         	throw new GameStatusException("retrieveStream fail:\n\n url: " + url + "\n\n" + e);
         }
 
      }
 
 	public String getSupportAsText() {
 		if ((support == null) || (support.size() == 0)) {
 			return "No Results";
 		} else {
 			String result = "";
 			for (int i = 0; i < support.size(); i++) {
 				result += "Version: "
 						+ GameRegionToString(support.get(i).gameRegion) + "\n";
 				result += "\tNTSC/J: "
 						+ RegionSupportStatusToString(support.get(i).supportStatuses
 								.get(GameRegion.NTSC_J)) + "\n";
 				result += "\tNTSC/U: "
 						+ RegionSupportStatusToString(support.get(i).supportStatuses
 								.get(GameRegion.NTSC_U)) + "\n";
 				result += "\tPAL:    "
 						+ RegionSupportStatusToString(support.get(i).supportStatuses
 								.get(GameRegion.PAL)) + "\n";
 				result += "\n";
 			}
 			result += "\n";
 			return result;
 		}
 	}
 
 	public List<RegionSupportStatusSet> getSupport() {
 		return support;
 	}
 
 	private String GameRegionToString(GameRegion region) {
 		switch (region) {
 		case NTSC_J:
 			return "NTSC/J";
 		case NTSC_U:
 			return "NTSC/U";
 		case PAL:
 			return "PAL";
 		default:
 			return "Unknown";
 		}
 	}
 
 	private String RegionSupportStatusToString(RegionSupportStatus status) {
 		switch (status) {
 		case Yes:
 			return "Yes";
 		case No:
 			return "No";
 		default:
 			return "Unknown";
 		}
 	}
 	
 }
