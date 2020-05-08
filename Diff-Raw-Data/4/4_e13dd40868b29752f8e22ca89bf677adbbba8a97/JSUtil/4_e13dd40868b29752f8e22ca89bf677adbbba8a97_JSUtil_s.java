 package js;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.reflect.Array;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Vector;
 
 /**
  * This class provides some useful time-saving methods, mostly for working with text.
  * 
  * @author Josh Sunshine
  * 
  * @version 1.0.1
  *
  */
 public class JSUtil {
 	
 	/**
 	 * Rounds a double to the specified number of decimal places.
 	 * 
 	 * @param value the double to be rounded
 	 * @param places the number of decimal places to round to
 	 * @return the double, rounded to the correct number of places.
 	 */
 	public static double round(double value, int places) {
 	    DecimalFormat n = new DecimalFormat();
 	    n.setMaximumFractionDigits(places);
 	    n.setMinimumFractionDigits(places);
 	    return Double.parseDouble(n.format(value));
 	}
 	
 	/**
 	 * Increases the length of an array by adding empty indexes to the end of the array.
 	 * 
 	 * @param array the array to increase the size of
 	 * @param newLength the new length of the array
 	 * @return the original <code>array</code> with extra empty indexes so it has length <code>newLength</code>.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T[] increaseArrayLength(T[] array, int newLength) {
 		if (newLength > array.length) {
 			Object[] newArray = new Object[newLength];
 			System.arraycopy(array, 0, newArray, 0, array.length);
 			try {
 				return (T[]) newArray;
 			} catch (ClassCastException e) {
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 			try {
 				throw new IllegalArgumentException("The new length of the array must be greater than its old length. (" 
 												+ newLength + " <= " + array.length + ")");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 		
 	/**
 	 * Generates a random colour.
 	 * 
 	 * @return the generated colour.
 	 */
 	public static Color randomColor() {
 		int r = (int) (Math.random() * 255);
 		int g = (int) (Math.random() * 255);
 		int b = (int) (Math.random() * 255);
 		Color randomColor = new Color(r, g, b);
 		return randomColor;
 	}
 		
 	/**
 	 * Generates a random integer between two limits.
 	 * 
 	 * @param low the lower limit of the range the number should be from.
 	 * @param high the upper limit of the range the number should be from.
 	 * @return the generated integer.
 	 */
 	public static int randIntBetween(int low, int high) {
 		int random = 0;
 		int diff = high - low;
 		random = (int) (Math.random() * diff) + low;
 		return random;
 	}
 	
 	/**
 	 * Formats a UK postcode into the standard format.
 	 * 
 	 * @param postcode the postcode to change the format of.
 	 * @return	the <code>postcode</code> in the correct format.
 	 */
 	public static String formatPostcode(String postcode) {
 		String newPostcode = "";
 		
 		postcode = removeAll(" ", postcode);
 		
 		if (postcode.length() < 5 || postcode.length() > 7) {
 			System.out.println("Invalid postcode.");
 			return null;
 		}
 		else {
 			newPostcode = postcode.substring(0, postcode.length() - 3).toUpperCase()
 			+ " " + postcode.substring(postcode.length() - 3).toUpperCase();
 			return newPostcode;
 		}
 	}
 	
 	/**
 	 * Formats a phone number into the standard UK format.
 	 * 
 	 * @param phoneNumber the number to change the format of.
 	 * @return	the <code>phoneNumber</code> in the correct format.
 	 */
 	public static String formatPhoneNumber(String phoneNumber) {
 	//	Boolean newStyle = false;
 		String newNumber = "";
 		
 		phoneNumber = removeAll(" ", phoneNumber);
		phoneNumber = removeAll("(", phoneNumber);
		phoneNumber = removeAll(")", phoneNumber);
 		
 		if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {
 			System.out.println("Invalid phone number.");
 			return null;
 		}
 		else {
 		
 			if (phoneNumber.substring(0, 2).equals("02")) {
 			//	newStyle = true;
 				newNumber = "(" + phoneNumber.substring(0, 3) + ") ";
 				newNumber += phoneNumber.substring(3, 7);
 				newNumber += " ";
 				newNumber += phoneNumber.substring(7);
 			}
 			else {
 				newNumber = phoneNumber.substring(0, 5) + " " + phoneNumber.substring(5);
 			}
 		
 			return newNumber;
 		}
 	}
 	
 	/**
 	 * Removes all instances of a substring from within a string.
 	 * 
 	 * @param needle the substring to remove.
 	 * @param haystack the string to remove the substring from.
 	 * @return the <code>haystack</code> with all <code>needle</code>s removed.
 	 */
 	public static String removeAll(String needle, String haystack) {
 		haystack = haystack.replaceAll(needle, "");
 		return haystack;
 	}
 	
 	/**
 	 * Uses the Google Maps API to lookup the full address of a postcode.
 	 * Note: This is not 100% accurate and should not be relied on without a backup system.
 	 * 
 	 * @param postcode the postcode to lookup the address for.
 	 * @return an array containing: {street name, area, town, county} in that order.
 	 * @throws Exception if something goes wrong contacting Google.
 	 */
 	public static String[] lookupAddress(String postcode) throws Exception {
 		String postcodeArea = postcode.substring(0, postcode.indexOf(' '));
 		String postcodeStreet = postcode.substring((postcode.indexOf(' ') + 1));
 	//	int start = 0;
 		String output = "";
 	//	String output2 = "";
 
 		URL url = new URL("http://maps.googleapis.com/maps/api/geocode/xml?address=" + postcodeArea + "+" + postcodeStreet + ",+UK&sensor=false");
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 
 		String inputLine;
 
 		while ((inputLine = in.readLine()) != null) {
 			//	System.out.println(inputLine);
 			output += inputLine + "\n";
 		}
 
 		in.close();
 		
 		int geometry = output.indexOf("<geometry>");
 		int location = output.indexOf("<location>", geometry);
 		int lat = output.indexOf("<lat>", location);
 		int slashLat = output.indexOf("</lat>", lat);
 		int lng = output.indexOf("<lng>", lat);
 		int slashLng = output.indexOf("</lng>", lng);
 		
 		String latitude = output.substring(lat + 5, slashLat);
 		String longitude = output.substring(lng + 5, slashLng);
 		
 		URL newURL = new URL("http://maps.googleapis.com/maps/api/geocode/xml?latlng=" + latitude + "," + longitude + "&sensor=false");
 		
 		BufferedReader in2 = new BufferedReader(new InputStreamReader(newURL.openStream()));
 
 		String inputLine2;
 		output = "";
 
 		while ((inputLine2 = in2.readLine()) != null) {
 			//	System.out.println(inputLine2);
 			output += inputLine2 + "\n";
 		}
 
 		in2.close();
 		
 		int street_address = output.indexOf("<type>street_address</type>");
 		int formatted_address = output.indexOf("<formatted_address>", street_address);
 		int slashFormatted_address = output.indexOf("</formatted_address>", formatted_address);
 		
 		String formattedAddress = output.substring(formatted_address + 19, slashFormatted_address);
 		
 	//	System.out.println(formattedAddress);
 		
 		int commas = JSUtil.countInstancesOf(",", formattedAddress);
 		String line1 = "", line2 = "", town = "", county = "";
 		
 		int firstComma = formattedAddress.indexOf(",");
 		int secondComma = formattedAddress.indexOf(",", firstComma + 1);
 		int thirdComma = formattedAddress.indexOf(",", secondComma + 1);
 	//	int fourthComma;
 		
 		if (commas < 3) {
 			System.out.println("Error finding address.");
 			return null;
 		}
 		else {
 			if (commas == 4) {
 			//	fourthComma = formattedAddress.indexOf(",", thirdComma + 1);
 				line1 = formattedAddress.substring(formattedAddress.indexOf(' ') + 1, firstComma);
 				line2 = formattedAddress.substring(firstComma + 2, secondComma);
 				town = formattedAddress.substring(secondComma + 2, thirdComma);
 				county = formattedAddress.substring(thirdComma + 2, formattedAddress.indexOf(postcodeArea) - 1);
 			}
 			else {
 				line1 = formattedAddress.substring(formattedAddress.indexOf(' ') + 1, firstComma);
 				town = formattedAddress.substring(firstComma + 2, secondComma);
 				county = formattedAddress.substring(secondComma + 2, formattedAddress.indexOf(postcodeArea) - 1);
 			}
 			
 			String[] address = {line1, line2, town, county};
 			return address;
 		}
 	}
 	
 	/**
 	 * Counts the number of times a substring appears in a string.
 	 * 
 	 * @param needle the substring to search for and count.
 	 * @param haystack the string to search in.
 	 * @return the number of times the <code>needle</code> appears in the <code>haystack</code> as an integer.
 	 */
 	public static int countInstancesOf(String needle, String haystack) {
 		int instances = 0;
 		String sub;
 		
 		for (int pos = 0; pos < haystack.length(); pos ++) {
 			sub = haystack.substring(pos, pos + needle.length());
 			if (sub.equals(needle))
 				instances ++;
 		}
 		
 		return instances;
 	}
 	
 	/**
 	 * Adds a 'ranking' suffix to a number - "st", "nd" etc. for creating rankings such as "1st" or "2nd" from an integer.
 	 * 
 	 * @param value the number to add the suffix to
 	 * @return a string containing the number and the suffix
 	 */
 	public static String addRankingSuffix(int value) {
 		String ranking = Integer.toString(value);
 		if ((ranking.length() == 1 && ranking.charAt(ranking.length() - 1) == '1') || (ranking.length() > 1 && ranking.charAt(ranking.length() - 1) == '1' && ranking.charAt(ranking.length() - 2) != '1'))
 			ranking += "st";
 		else if (ranking.charAt(ranking.length() - 1) == '2')
 			ranking += "nd";
 		else if (ranking.charAt(ranking.length() - 1) == '3')
 			ranking += "rd";
 		else
 			ranking += "th";
 		return ranking;
 	}
 	
 	public static int[] generateRandomIntegerArray(int low, int high, int n) {
 		int[] array = new int[n];
 		for (int i = 0; i < n; i ++) {
 			int x = randIntBetween(low, high);
 			array[i] = x;
 		}
 		return array;
 	}
 	
 	public static Object[] concatenateArrays(Object[] a, Object[] b){
 	    // Use reflection to find the super class of both arrays
 	    Class<?> commonSuperClass = Object.class;
 	    boolean foundcommonSuperClass=false;
 	    for (Class<?> c1 = a.getClass().getComponentType(); !foundcommonSuperClass && !c1.equals(Object.class); c1 = c1.getSuperclass()){
 	      for (Class<?> c2 = b.getClass().getComponentType(); !foundcommonSuperClass && !c2.equals(Object.class); c2 = c2.getSuperclass()){
 	        if (c2.equals(c1)){
 	          foundcommonSuperClass = true;
 	          commonSuperClass = c1;
 	        }
 	      }
 	    }
 	    // Create a new array of the correct type
 	    Object[] result = (Object[]) Array.newInstance(commonSuperClass, a.length + b.length);
 	    // Copy the two arrays into the large array
 	    System.arraycopy(a, 0, result, 0, a.length);
 	    System.arraycopy(b, 0, result, a.length, b.length);
 	    return result;
 	  }
 	
 	/**
 	 * Formats a number to show the specified number of place values. For instance, if you wish to display all values 
 	 * in a program as double-digit numbers (13, 24, 07, etc.) then use 2 as the <code>places</code> parameter.
 	 * 
 	 * @param number the integer to reformat
 	 * @param places the number of place values to use
 	 * @return a String containing the reformatted number
 	 */
 	public static String formatWithPlaceValues(int number, int places) {
 		String n = Integer.toString(number);
 		while (n.length() < places) {
 			n = "0" + n;
 		}
 		return n;
 	}
 }
