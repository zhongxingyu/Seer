 package au.edu.adelaide.physics.opticsstatusboard;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.pm.PackageManager.NameNotFoundException;
 
 public class UpdateChecker {
 	private BackgroundManager manager;
 	private BufferedReader input;
 	private String data;
 	private boolean updateServerAvailable;
 	
 	public UpdateChecker(BackgroundManager manager) {
 		this.manager = manager;
 	}
 
 	public void check(URL updateWebsite) {
 		String[] output = new String[1];
 		output[0] = "";
 		
 		try {
 			input = new BufferedReader((new InputStreamReader(WebParser.getResp(updateWebsite))));
 			
 			boolean ok = true;
 			while (ok) {
 				String test = input.readLine();
 				
 				if (test == null) {
 					ok = false;
 				} else {
 					data += test;
 				}
 			}
 			
 			input.close();
 			
 			updateServerAvailable = true;
 			
 			Document parsedData = Jsoup.parse(data);
 		    Element mainBody = parsedData.body();
 		    Elements currentVersion = mainBody.getElementsByTag("p");
 		    
 			output[0] = currentVersion.get(0).html();
 		} catch (IOException e) {
 			System.out.println("IOException");
 			e.printStackTrace();
 			updateServerAvailable = false;
 		} catch (URISyntaxException e) {
 			System.out.println("URI Conversion Exception");
 			e.printStackTrace();
			updateServerAvailable = false;
		} catch (IndexOutOfBoundsException e) {
			System.out.println("No data to parse?!");
			e.printStackTrace();
			updateServerAvailable = false;
 		}
 		
 		postExecute(output);
 	}
 
 	private void postExecute(String[] output) {
 		int currentVersion;
 		String appId = "au.edu.adelaide.physics.opticsstatusboard";
 		manager.notifyNewVersion(false);
 		
 		try {
 			currentVersion = manager.getPackageManager().getPackageInfo(appId, 0).versionCode;
 			
 			if (updateServerAvailable) {
 				try {
 					if (Integer.parseInt(output[0]) > currentVersion) {
 						manager.notifyNewVersion(true);
 					}
 				} catch (NumberFormatException e) {
 //					System.out.println("Error in parsing");
 					updateServerAvailable = false;
 					e.printStackTrace();
 				}
 			} else {
 //				System.out.println("Couldn't contact update server");
 			}
 		} catch (NameNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
