 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.support.components.geographic;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /*
 37.843075,-122.27787
 37.843075,-122.277869
 */
 
 
 public class GeoLocation {
 	
 	
 	//
 	// Precision: city: Frederick,Virginia
 	// country > state > city > zip* > street > address
 	//    0        1       2     3       4       5
 	// 
 	static int P_COUNTRY = 0;
 	static int P_STATE   = 1;
 	static int P_CITY    = 2;
 	static int P_ZIP     = 3;
 	static int P_STREET  = 4;
 	static int P_ADDRESS = 5;
 	static String[] pKeys = {"country", "state", "city", "zip", "street", "address"};
 	static Map<String,Integer> precisionMap = new HashMap<String, Integer>();
 	static {
 		
 		for (int i = 0; i < pKeys.length; i++) {
 			precisionMap.put(pKeys[i], i);
 		}
 	}
 	
 	
 
 	String location;
 	
 	
 	double latitude;
 	double longitude;
 
 	String city,state,country;
 	int precision;
 	
 	public GeoLocation()
 	{
 		this(-1.0f, -1.0f);
 	}
 	
 	
 	public GeoLocation(double lat, double lng) {
 
 		this.latitude  = lat;
 		this.longitude = lng;
 
 	}
 
 	public double getLatitude()  {return latitude;}
 	public double getLongitude() {return longitude;}
 
 	public void setLatitude(double l) {
 		this.latitude = l;
 	}
 	public void setLongitude(double l) {
 		this.longitude = l;
 	}
 
 	public boolean isValid()
 	{
 		return (this.latitude != -1 || this.longitude != -1);
 	}
 
 	public void setCity(String c)    {this.city = c;}
 	public void setState(String s)   {this.state = s;}
 	public void setCountry(String c) {this.country = c;}
 	public String getCity()    {return this.city;}
 	public String getState()   {return this.state;}
 	public String getCountry() {return this.country;}
 	
 	public boolean isState()  {return this.precision == P_STATE;}
 	public boolean isFoundWithin(GeoLocation other) {
 		
 		if (this.equals(other)) {
 			return true;
 		}
 		if (other.precision > this.precision ) {
 			return false;
 		}
 		
 		// e.g. other == {USA, WI}          precision == STATE [1]
 		//      this  == {USA, WI, Madison} precision == CITY  [2]
 		
 
 		// need to have the same labels
 		for (int i = 0; i <= other.precision; i++) {  // YES i mean <= 
 			String a = pKeys[this.precision];
 			String b = pKeys[other.precision];
 			if (! a.equals(b)) {
 				return false;
 			}
 		}
 		return true;
 	
 	}
 	
 	public String toString()
 	{
		return this.location + "\n" + this.latitude  + "," + 
		       this.longitude + "," +
		       this.city      + "," +
		       this.state     + "," + 
		       this.country   + ":" + getPrecisionAsString();
 	}
 	
 	public void setPrecision(String precision) 
 	{    
 		this.precision = P_COUNTRY;
 		if (precision != null) {
 			if (precision.startsWith("zip")) {
 				precision = "zip";  // could be zip+3, zip+4
 			}
 			for (int i = 0; i < pKeys.length; i++) {
 				if (precision.equals(pKeys[i])) {
 					this.precision = i;
 				}
 			}
 		}
 	}
 	
 	public String getPrecisionAsString()
 	{
 		return pKeys[this.precision];
 	}
 	
 	public boolean equals(GeoLocation other) {
 		return this.latitude == other.latitude && this.longitude == other.longitude;
 	}
 	
 	public void setQueryLocation(String l) {this.location = l;}
 	public String getQueryLocation()       {return this.location;}
 
 
 	//
 	// GeoLocation Service
 	//
 
     public static final String defaultAPIKey = "yFUeASDV34FRJWiaM8pxF0eJ7d2MizbUNVB2K6in0Ybwji5YB0D4ZODR2y3LqQ--";
 
 
     public static GeoLocation getLocation(String location)
     throws IOException
     {
     	return getLocation(location, defaultAPIKey);
     }
     
     public static GeoLocation getLocation(String location, String yahooAPIKey)
     throws IOException
     {
     	List<GeoLocation> all = getAllLocations(location, yahooAPIKey);
     	if (all.size() == 0) {
     		return new GeoLocation();
     	}
     	return all.get(0);
     }
 
     public static List<GeoLocation> getAllLocations(String location)
     throws IOException
     {
     	return getAllLocations(location, defaultAPIKey);
     }
     
     public static List<GeoLocation> getAllLocations(String location, String yahooAPIKey)
     throws IOException
 
     {
     	String param = URLEncoder.encode("location", "UTF-8") + "=" + URLEncoder.encode(location, "UTF-8");
 
     	StringBuffer sb = new StringBuffer();
         sb.append("http://local.yahooapis.com/MapsService/V1/geocode?appid=");
         sb.append(yahooAPIKey).append("&");
         sb.append(param);
 
         // System.out.println("Q: " + sb.toString());
         
         // read response from server
         URL url = new URL(sb.toString());
         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
 
         InputStream is;
         List<GeoLocation> locations = new ArrayList<GeoLocation>();
         
         try {
         	is = conn.getInputStream();
         }
         catch (IOException e) {
         	// HttpURLConnection http = (HttpURLConnection)conn;
         	// http.getResponseCode();
         	return locations;
         }
 
         BufferedReader in = new BufferedReader(new InputStreamReader(is));
 
  
 		String inputLine;
 		sb = new StringBuffer();
 		while ((inputLine = in.readLine()) != null) {
 			sb.append(inputLine);
 		}
 		in.close();
 		
 		String toParse = sb.toString();
 		int idx = toParse.indexOf("ResultSet");
 		if (idx == -1) {
 			// "no result set"
 			return locations;
 		}
 		
 		toParse = toParse.substring(idx + "ResultSet".length());
 		String[] results = toParse.split("</Result>");
 		for (String xml: results) {
 			
 			idx = xml.indexOf("<Result");
 			if (idx == -1) {
 				continue;
 			}
 			xml = xml.substring(idx);
 			
 			// System.out.println("LOOK " + xml);
 			
 			// parse the response
 			String lat = simpleXMLParse(xml, "Latitude");
 			String lng = simpleXMLParse(xml, "Longitude");
 			String city    = simpleXMLParse(xml, "City");
 			String state   = simpleXMLParse(xml, "State");
 			String country = simpleXMLParse(xml, "Country");
 
 			if (lat != null && lng != null) {
 				
 				GeoLocation geo =
 					new GeoLocation(Double.parseDouble(lat), Double.parseDouble(lng));
 
 				geo.setCity(city);
 				geo.setState(state);
 				geo.setCountry(country);
 				
 				String p = simpleXMLParseAttribute(xml, "Result", "precision");
 				geo.setPrecision(p);
 				geo.setQueryLocation(location);
 				
 				locations.add(geo);
 			}
 		}
 		
         return locations;
 
     }
 
     public static String simpleXMLParse(String xml, String token)
     {
     	String sToken = "<"  + token + ">";
 		String eToken = "</" + token + ">";
 
 		int sIdx = xml.indexOf(sToken) + sToken.length();
 		if (sIdx == -1) return null;
 		
 		int eIdx = xml.indexOf(eToken, sIdx);
 		if (eIdx == -1) return null;
 		
 		return xml.substring(sIdx, eIdx);
 
     }
     
     public static String simpleXMLParseAttribute(String xml, String token, String attr) 
     {
     	String sTok = "<" + token;
     	
     	int sIdx = xml.indexOf(sTok);
     	int eIdx = xml.indexOf(">");
     	if (sIdx == -1 || eIdx == -1) return null;
     	
     	sIdx += token.length();
     	int idx = xml.indexOf(attr, sIdx);
     	if (idx >= eIdx) {
     		return null;
     	}
     	
     	String props = xml.substring(sIdx, eIdx);
     	String regEx = attr + "=\"([A-Za-z0-9]+)\""; // assumes no white space between attr=value
     	Pattern pattern = Pattern.compile(regEx);
     	Matcher matcher = pattern.matcher(props);
     	if (matcher.find()) {
     		// group(0) is attr="blah"
     		// group(1) is blah
     		return matcher.group(1);
     	}
     	
     	return null;
     	
     }
     
 
 
 }
 
 /*
 	
 	public boolean hasSameState(GeoLocation other) 
 	{
 		if (this.equals(other)){
 			return true;
 		}
 		
 		if (other.state != null && this.state != null) {
 			return this.state.equals(other.state);
 		}
 		return false;
 		
 		// other == WI
 		// this == Milwaukee, WI  ==> true
 		// this == Milwaukee, AZ  ==> false
 		
 	}
 
 	
 	public boolean stateOnly() 
 	{
 		return "state".equals(this.precision);
 	}
 	public boolean cityOnly()
 	{
 		return "zip".equals(this.precision);
 	}
 	public boolean countryOnly()
 	{
 		return "country".equals(this.precision);
 	}
 	*/
