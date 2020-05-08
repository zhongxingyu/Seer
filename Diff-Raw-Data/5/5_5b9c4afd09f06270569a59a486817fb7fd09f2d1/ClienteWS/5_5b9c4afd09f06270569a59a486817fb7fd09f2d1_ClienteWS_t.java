 package br.com.caelum.parsac.main.ws;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Scanner;
 
 public class ClienteWS {
 
 	public String consomeWebService(String webService) throws IOException {
 		URL url = new URL(webService);
 		HttpURLConnection con = (HttpURLConnection) url.openConnection();
 		InputStream stream = con.getInputStream();
 		Scanner scanner = new Scanner(stream);
 		
 		String xml = "";
 		while(scanner.hasNext()) {
			xml = xml + " " + scanner.next();
 		}
 		
		System.out.println(xml);
		
 		return xml;
 	}
 }
