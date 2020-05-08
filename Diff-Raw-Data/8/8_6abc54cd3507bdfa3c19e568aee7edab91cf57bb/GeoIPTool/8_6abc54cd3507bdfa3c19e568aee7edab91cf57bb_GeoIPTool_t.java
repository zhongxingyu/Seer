 package what.sp_parser;
 
 import java.io.IOException;
 
 import controllers.Localize;
 
 import what.sp_parser.sp_GeoIp.*;
 
 /**
  * This tool adds the City and Country of a request to a dataEntry.
  * It includes GeoLite data created by MaxMind, available from http://www.maxmind.com. It uses the
  * MaxMind Java-API, which is a little bit changed for speed-up.
  * @author Alex
  *
  */
 public class GeoIPTool {
 	
 	/**
 	 * The lookupservice which looks for country and city of every request.
 	 */
 	private static LookupService cl;
 	
 	//Cache for the last IP and location.
 	private static Location lastLoc = null;
 	private static String lastIp = null;
 		
 	/**
 	 * Sets up the IpTool and creates a new lookupService
 	 * @param pm the ParserMediator which will use this tool
 	 */
 	protected static boolean setUpIpTool(ParserMediator pm) {
 		
 		String dir = System.getProperty("user.dir");
 		String seperator = System.getProperty("file.separator");
		String lookup = dir + seperator + "data" + seperator + "geoLiteCity.dat";
 				
 		
 		try {
 			cl = new LookupService(lookup);
 		} catch (IOException e) {
 			pm.error(Localize.getString("Error.120"));
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Adds the location info to the IP of a certain dataEntry
 	 * @param pt the parsing-task which parsed the dataEntry
 	 */
 	protected static void getLocationInfo(ParsingTask pt) {
 
 		try {		
 			
 									
 			Location loc;
 			
 			//Looks if the IP is the same as the one which started the last request, uses the same location info
 			//if this is true. If not, it uses the LookupService to create a new Location-object loc.
 			if (pt.getSplitStr()[6].equals(lastIp)) {
 				loc = lastLoc;
 			} else {
 				loc = cl.getLocation((String) pt.getSplitStr()[6]);
 			}
 			
 			
 			if (loc.countryName.toLowerCase().contains("proxy")) {
 				loc.countryName = "other";
 			}
 			if (loc.city.toLowerCase().contains("proxy")) {
 				loc.city = "other";
 			}
 			
 			//Sets country and city to their spots of the DataEntry.
 			pt.getDe().setInfo(loc.countryName.trim(), 6);	
 			pt.getDe().setInfo(loc.city.trim(), 7);
 
 			if (pt.getDe().getInfo(6) == null) {
 			    pt.getDe().setInfo("other", 6);
 			}
 			 if (pt.getDe().getInfo(7) == null) {
 			    pt.getDe().setInfo("other", 7);
			 }
 			
 			lastLoc = loc;
 			lastIp = (String) pt.getDe().getInfo(6);
 					
 			
 		} catch (NullPointerException e) {
 			 //cl.getLocation(String str) throws a NullPointerException when it doens't find any location to a certain ip. 
 			 //catching it is the easiest way to deal with this problem.
 			pt.getDe().setInfo("other", 6);
			pt.getDe().setInfo("other", 7);
 		}
 	}
 }
