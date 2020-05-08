 package model;
<<<<<<< HEAD
 
=======
>>>>>>> morten/gui
 import java.awt.Image;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
<<<<<<< HEAD
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 
 /**
 * A class for the Google Maps API
  * 
  * @author Ole J. Pettersen
  *
  */
 
 public class GoogleMaps extends JFrame {
 	
 	/**
 	 * Returns a map as a JLabel showing the delivery address for a specific order.
 	 * 
 	 * @param input
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public static JLabel map(String input) throws MalformedURLException, IOException {
 		String address = 
 				"http://maps.googleapis.com/maps/api/staticmap?markers=size:mid%7Ccolor:red%7C|"
 				+ validateAddress(input)
				+ "&zoom=14&size=250x250&maptype=roadmap&&sensor=false";
=======
import javax.imageio.ImageIO;
import javax.swing.*;

@SuppressWarnings("serial")
public class GoogleMaps extends JFrame {
	
	public static JLabel map(String input) throws MalformedURLException, IOException {
		String address = "http://maps.googleapis.com/maps/api/staticmap?markers=size:mid%7Ccolor:red%7C" + validateAddress(input) + "&zoom=14&size=380x455&maptype=roadmap&&sensor=false";
>>>>>>> morten/gui
 		
 		Image image = ImageIO.read(new URL(address));
 		JLabel label = new JLabel(new ImageIcon(image));
 
 		return label;
 	}
 	
 	
<<<<<<< HEAD
 	/**
 	 * Runs a few tests and stuff to format the String after the Google Maps API's liking. 
 	 * Otherwise it gets sad :(
 	 * 
 	 * @param input
 	 */
=======
>>>>>>> morten/gui
 	public static String validateAddress(String input) {
 		String output = "";
 		
 	
 		
 		for (int i = 0; i < input.length(); i++) {
 			
 			int value = (char)input.charAt(i);
 			
 			if ((value >= 65) && (value <= 93))
 				output += input.charAt(i);
 			else if ((value >= 97) && (value <= 125))
 				output += input.charAt(i);
 			else if ((value >= 48) && (value <= 57))
 				output += input.charAt(i);
 			else if (value == 32)
 				output += "%20";
 		}
 			
 		
 		return output;
 		
 	}
<<<<<<< HEAD
 	
 	
 	/**
 	 * Returns a map as a JLabel showing the delivery address for all orders.
 	 * 
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public static JLabel mainMap() throws MalformedURLException, IOException {
 		String marker = "&markers=color:red%7C";
 		String address = "";
 		//Sample ArrayList. Remove it later.
 		ArrayList<String> addressList = new ArrayList<String>();
 		
 		// TODO Implement ArrayList with orders
 		
 		
 		// Just some sample data. Remove it later.
 		addressList.add("hogskoleringen1%20trondheim");
 		addressList.add("munkegata12%20trondheim");
 		addressList.add("dronningensgate9%20trondheim");
 		
 		/*
 		 *  TODO In case there's no orders to show, the map will probably show a bit too much water.
 		 *  Might wanna do a check to make sure there's land in sight.
 		 */
 		
 		address = 
 				"http://maps.googleapis.com/maps/api/staticmap?center=Trondheim%20Norge&zoom=12&size=250x250&maptype=roadmap&markers=color:red%7C"
 				+ addressList.get(0);
 		
			for (int i = 0; i < addressList.size(); i++) {
 				address += marker;
 				address += addressList.get(i);
 			}
 				
 			address += "&sensor=false";
 				
 			Image image = ImageIO.read(new URL(address));
 			JLabel label = new JLabel(new ImageIcon(image));
 
 			return label;
 	}
=======
>>>>>>> morten/gui
 }
