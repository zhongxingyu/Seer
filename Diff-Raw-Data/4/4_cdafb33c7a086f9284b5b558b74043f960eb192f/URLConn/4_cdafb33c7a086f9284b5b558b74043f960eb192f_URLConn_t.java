 package com.dierkers.schedule.tools.http;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Scanner;
 
 public class URLConn {
	public static String getPage(String url) {
 		Scanner in;
 		url = url.replace(" ", "%20");
 		System.out.println(url);
 		String response = "";
 		try {
 			in = new Scanner(new URI(url).toURL().openStream());
 
 			while (in.hasNext()) {
 				response += in.nextLine() + "\n";
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return response;
 	}
 }
