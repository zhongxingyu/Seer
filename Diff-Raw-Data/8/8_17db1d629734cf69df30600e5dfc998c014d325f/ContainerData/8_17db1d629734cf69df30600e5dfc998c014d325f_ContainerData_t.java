 package fr.enseirb.odroidx.movieselector;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.content.Context;
 import android.widget.Toast;
 
 public class ContainerData {	
 	
 	static public Context context;
 	
 	public static ArrayList<Movie> getMovies(String ip, Context context){
 		SAXParserFactory fabrique = SAXParserFactory.newInstance();
 		SAXParser parser = null;
 		ArrayList<Movie> movies = null;
 		try {
 			parser = fabrique.newSAXParser();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		}
 		
		URL url;
 		try {
			url = new URL("http://" + ip + ":8080/dash-manager/vod");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
			Toast.makeText(context, "Malformed URL or unreachable XML", Toast.LENGTH_LONG).show();
 			return null;
 		}
 
 		DefaultHandler handler = new ParserXMLHandler();
 		try {
 			parser.parse(url.openConnection().getInputStream(), handler);
 			
 			movies = ((ParserXMLHandler) handler).getData();
 					
 		} catch (SAXException e) {
 			e.printStackTrace();
 			Toast.makeText(context, "Impossible to parse vod.xml", Toast.LENGTH_LONG).show();
 			return null;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Toast.makeText(context, "Connection to server impossible", Toast.LENGTH_LONG).show();
 			return null;
 		}
 
 		return movies;
 	}
 }
