 package liv.ac.uk.vomssnooper;
 /**
  * Reads in an XML file and a site-info.def file (and related vo.d files) and compares them.
  * 
  * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
  * @since 2012-07-20
  * 
  */
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import liv.ac.uk.snooputils.Utils;
 
 public class SiteChecker {
 
 	private String xmlUrl;    // The URL to get the XML from
 	private String sidFile;   // The location of the site-info.def file (vo.d should be in same directory)
 	
 	private ArrayList<VirtOrgInfo> voInfoXml;       // XML VO Info
 	private HashMap<String, VirtOrgInfo> voInfoSid; // SID VO Info
 
 	/**
 	 * Constructor
 	 * 
 	 * @param xmlurl url to get XML from
 	 * @param sidfile site-info.def file
 	 */
 
 	public SiteChecker(String xmlurl, String sidfile ) {
 		this.xmlUrl = xmlurl;
 		this.sidFile = sidfile ;
 		
 		this.voInfoXml = new ArrayList<VirtOrgInfo>();
 		this.voInfoSid = new HashMap<String, VirtOrgInfo>();
 	}
   /**
    * Parse an XML file 
    */
 	private void parseXml() {
 
 		VoidCardXmlParser xmlParser = new VoidCardXmlParser(xmlUrl, (ArrayList<VirtOrgInfo>) voInfoXml);
 		xmlParser.parseDocument();
 
 		// I'm interested in them all, nominally
 		for (VirtOrgInfo voi: voInfoXml) {
 			voi.setAtMySite(true);
 			voi.checkComplete();
 		}
 	}
 	
   /**
    * Parse a SID file 
    */
 	private void parseSid() {
 		// Storage for the record I consider
 		String caDnLine = null;
 		String vomsServersLine = null;
 		String vomsesLine = null;
 
 		File sid = new File(sidFile);
 		if (!sid.isFile()) {
 			System.out.println("site-info.def file " + sid + " not found");
 			System.exit(1);
 		}
 
 		// Get the Yaim variables
 		ArrayList<String> yaimVariables = new ArrayList<String>();
 		try {
 			String cmd = "bash -x " + sid.toString();
 			yaimVariables = Utils.cmdExec(cmd);
 		}
 		catch (Exception e) {
 			System.out.println("Problem while while reading site-info.def " + e.getMessage());
 			System.exit(1);
 		}
 
 		// Sort so we can depend on the order
 		Collections.sort(yaimVariables, String.CASE_INSENSITIVE_ORDER);
 
 		// Go over the Yaim variables, selecting VO lines
 		Pattern pattern = Pattern.compile("VO_(.*)_VOMS.*");
 
 		for (String yaimVariable : yaimVariables) {
 
 			Matcher matcher = pattern.matcher(yaimVariable);
 			if (matcher.find()) {
 
 				String voName = matcher.group(1).toLowerCase();
 
 				// Collect all the lines first to overcome any order problems
 
 				if (yaimVariable.matches(".*CA_DN.*")) {
 					if (voInfoSid.containsKey(voName) == true) {
 						System.out.println("Warning: the " + voName + " sid records are duplicated! Results may be chaotic.");
 					}
 					else {
 						
 						// Make a new set of records
 						voInfoSid.put(voName, new VirtOrgInfo());
 					}
 
 					// Initial values
 					voInfoSid.get(voName).setVoNameAndVoNickName(voName);
 					voInfoSid.get(voName).setVodStyle(false);
 					voInfoSid.get(voName).setAtMySite(true);
 
 					// Store the CA DNs
 					caDnLine = yaimVariable;
 				}
 
 				if (yaimVariable.matches(".*VOMS_SERVERS.*")) {
 					// Store the VOMS Servers
 					vomsServersLine = yaimVariable;
 				}
 
 				if (yaimVariable.matches(".*VOMSES.*")) {
 					// Store the VOMSES
 					vomsesLine = yaimVariable;
 
 					// As it is sorted, this triggers the end of a run, so now do more parsing
 					// Break that CA DN variable up and go setting fields
 					ArrayList<String> elements = breakString(caDnLine);
 
 					for (String caDn : elements) {
 						VomsServer theVomsServer = new VomsServer();
 						theVomsServer.setMembersListUrl("dummy");
 						theVomsServer.setCaDn(caDn);
 						voInfoSid.get(voName).addVomsServer(theVomsServer);
 					}
 
 					// VOMSES found - break the variable up and go setting fields
 					elements = breakString(vomsesLine);
 					int ii = -1;
 					for (String vomses : elements) {
 						ii++;
 						ArrayList<VomsServer> vomsServers = voInfoSid.get(voName).getVomsServers();
 
 						// More pattern matching to save a lot of tinkering
						Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(.+)\\s+(\\S+)\\s*$");
 						Matcher m = p.matcher(vomses);
 						if (m.find()) {
 							String vo1 = m.group(1);
 							String host = m.group(2);
 							String vomsServerPort = m.group(3);
 							String dn = m.group(4);
 							String vo2 = m.group(5);
 
 							vomsServers.get(ii).setHostname(host);
 							vomsServers.get(ii).setVomsServerPort(Integer.parseInt(vomsServerPort));
 							vomsServers.get(ii).setDn(dn);
 						}
 					}
 
 					// Voms Servers found - break the variable up and go setting fields
 					elements = breakString(vomsServersLine);
 					for (String el : elements) {
 						ArrayList<VomsServer> vomsServers = voInfoSid.get(voName).getVomsServers();
 
 						// Pattern matching to save a lot of tinkering
 						Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
 						Matcher m = p.matcher(el);
 						if (m.find()) {
 							String hostPart = m.group(1);
 							Integer httpsPort = Integer.parseInt(m.group(2));
 
 							// Find the voms server that this record applies to
 							Boolean setPort = false;
 
 							for (VomsServer v : vomsServers) {
 								String h = v.getHostname();
 								if (h.equalsIgnoreCase(hostPart)) {
 									// This is the one
 									v.setHttpsPort(httpsPort);
 
 									// If it's in the VOMS_SERVERS line, assume it's an admin server
 									v.setIsVomsAdminServer(true);
 									setPort = true;
 								}
 							}
 							if (!setPort) {
 								System.out.println("Warning: Unable to find a VOMSES record matching (one of) these: " + vomsServersLine);
 							}
 						}
 						else {
 							System.out.println("Warning: Weird VOMS_SERVER line: " + el.toString());
 						}
 					}
 				}
 			}
 		}
 
 		// Next get the vod files, that lie in files in the vo.d directory
 
 		File dir = new File(sid.getParent() + "/vo.d");
 		String[] vodFiles = dir.list();
 		ArrayList<String> usableVodFiles = new ArrayList<String>();
 		for (String s : vodFiles) {
 			if (!s.startsWith(".")) {
 				usableVodFiles.add(s);
 			}
 		}
 
 		for (String vodFile : usableVodFiles) {
 
 			// Read the yaim variables for each files found
 			ArrayList<String> vodYaimVariables = new ArrayList<String>();
 			try {
 				String cmd = "bash -x " + dir.toString() + "/" + vodFile;
 				vodYaimVariables = Utils.cmdExec(cmd);
 			}
 			catch (Exception e) {
 				System.out.println("Problem while while reading vod. file " + vodFile + ", " + e.getMessage());
 				System.exit(1);
 			}
 			// Sort it so we can depend on the order
 			Collections.sort(vodYaimVariables, String.CASE_INSENSITIVE_ORDER);
 
 			// Go over the lines, look for VO ones
 			Pattern vodVomsPattern = Pattern.compile("^\\+ VOMS.*");
 
 			for (String vodYaimVariable : vodYaimVariables) {
 
 				Matcher matcher = vodVomsPattern.matcher(vodYaimVariable);
 				if (matcher.find()) {
 
 					// Name is same as file, for VODs
 					String voName = vodFile.toLowerCase();
 					// VirtOrgInfo thisVo = voidInfoFromSid.get(voName);
 
 					// Get all the fields in advance
 					if (vodYaimVariable.matches(".*CA_DN.*")) {
 						caDnLine = vodYaimVariable;
 						// Make a new record, if we need to
 						if (voInfoSid.containsKey(voName.toLowerCase()) == true) {
 							System.out.println("Warning: the " + voName + " vod records are duplicated! Results may be chaotic.");
 						}
 						else {
 							voInfoSid.put(voName.toLowerCase(), new VirtOrgInfo());
 						}
 						voInfoSid.put(voName, new VirtOrgInfo());
 						voInfoSid.get(voName).setVoNameAndVoNickName(voName);
 						voInfoSid.get(voName).setVodStyle(true);
 						voInfoSid.get(voName).setAtMySite(true);
 
 					}
 					if (vodYaimVariable.matches(".*VOMS_SERVERS.*")) {
 						vomsServersLine = vodYaimVariable;
 					}
 					if (vodYaimVariable.matches(".*VOMSES.*")) {
 						vomsesLine = vodYaimVariable;
 
 						// Last one found, so parse the fields
 
 						// Break up the CA DN variable
 						ArrayList<String> elements = breakString(caDnLine);
 
 						for (String caDn : elements) {
 							VomsServer theVomsServer = new VomsServer();
 							theVomsServer.setCaDn(caDn);
 							theVomsServer.setMembersListUrl("dummy");
 							voInfoSid.get(voName).addVomsServer(theVomsServer);
 						}
 
 						// VOMSES found - break the variable up and go setting fields
 						elements = breakString(vomsesLine);
 
 						int ii = -1;
 						for (String vomses : elements) {
 							ii++;
 							ArrayList<VomsServer> vomsServers = voInfoSid.get(voName).getVomsServers();
 
 							// Pattern matching to save a lot of tinkering
							Pattern p = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\d+)\\s+(.+)\\s+(\\S+)\\s*$");
 							Matcher m = p.matcher(vomses);
 							if (m.find()) {
 								String vo1 = m.group(1);
 								String host = m.group(2);
 								String vomsServerPort = m.group(3);
 								String dn = m.group(4);
 								String vo2 = m.group(5);
 								vomsServers.get(ii).setHostname(host);
 								vomsServers.get(ii).setVomsServerPort(Integer.parseInt(vomsServerPort));
 								vomsServers.get(ii).setDn(dn);
 							}
 						}
 
 						// Voms Servers found - break the variable up and go setting
 						// fields
 						elements = breakString(vomsServersLine);
 
 						ii = -1;
 						for (String el : elements) {
 							ii++;
 							ArrayList<VomsServer> vomsServers = voInfoSid.get(voName).getVomsServers();
 
 							// Pattern matching to save a lot of tinkering
 							Pattern p = Pattern.compile("vomss:\\/\\/(\\S+)\\:(\\d+).*");
 							Matcher m = p.matcher(el);
 							if (m.find()) {
 								String hostToFind = m.group(1);
 								Integer httpsPort = Integer.parseInt(m.group(2));
 
 								// Go over all the VOMS Servers, finding the one that matches this record
 								Boolean setPort = false;
 								for (VomsServer v : vomsServers) {
 									String h = v.getHostname();
 									if (h.equalsIgnoreCase(hostToFind)) {
 										v.setHttpsPort(httpsPort);
 										v.setIsVomsAdminServer(true);
 										setPort = true;
 									}
 								}
 								if (!setPort) {
 									System.out.println("Warning: Unable to find a voms server for one of these: " + vomsServersLine);
 								}
 							}
 							else {
 								System.out.println("Warning: Weird VOMS_SERVER line: " + el.toString());
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Finally, sort those voms servers
 		ArrayList<VirtOrgInfo> v = new ArrayList<VirtOrgInfo>(voInfoSid.values());
 
 		for (VirtOrgInfo voi : v) {
 			voi.sortVomsServers();
 		}
 		// end of sid
 	}
 
 
 	/**
 	 * Compare a set of XML and SID records
 	 */
 	public void compare() {
 
 
 		HashSet<String> keys = new HashSet<String>();
 		keys.addAll(voInfoSid.keySet());
 		
 		ArrayList<String> matches = new ArrayList<String>(); 
 		int discrepancies = 0;
 		for (VirtOrgInfo xmlVo: voInfoXml) {
 			
 			if (voInfoSid.containsKey(xmlVo.getVoName())) {
         keys.remove(xmlVo.getVoName());				
 				VirtOrgInfo sidVo = voInfoSid.get(xmlVo.getVoName());
 				String xmlVoString = xmlVo.toString();
 				String sidVoString = sidVo.toString();
 				if (!xmlVoString.equals(sidVoString)) {
 					discrepancies ++;
 					System.out.println("\n##################################");
 					System.out.println("Discrepancy in VO:" + xmlVo.getVoName());
 					System.out.println("----------------------------------");
 					System.out.println("XML Data : \n" + xmlVoString);
 					System.out.println("Site-info: \n" + sidVoString);
 					System.out.println("##################################");
 					System.out.println(" ");
 				}
 				else {
 					matches.add(sidVo.getVoName());
 				}
 			}
 		}
 		System.out.println("Note: a total of " + discrepancies + " discrepancies were found");
 		
 		if (matches.isEmpty()) {
 			System.out.println("Warning: No matches were found\n");
 		}
 		else {
 			System.out.print("These VOs matched OK: ");
 			for (String m : matches) {
 				System.out.print(" " + m);
 			}
 			System.out.println();
 		}
 		
 		if (!keys.isEmpty()) {
 			System.out.print("Warning: These VOs at your site are not represented in the CIC Portal XML: ");
 
 			for (String key : keys) {
 				System.out.print(" " + key);
 			}
 			System.out.println();
 		}
 		else {
 			System.out.println("All the VOs at your site are represented in the CIC Portal XML.");
 		}
 		
 		System.out.println("");
 	}
 
 	/**
 	 * Main
 	 * 
 	 * @return null
 	 */
 
 	// List of the CLI options
 	private enum OptList {
 		xmlurl, sidfile, help,
 	}
 
 	public static void printHelpPage() {
 		System.out.println("");
 		System.out.println("The SiteChecker tool checks if a site complies with the CIC portal XML");
 		System.out.println("");
 		System.out.println("Mandatory arguments: ");
 		System.out.println("  --sidfile f       # Location of site-info.def file");
 		System.out.println("Optional arguments: ");
 		System.out.println("  --xmlurl  f       # URL of XML file (def: http://operations-portal.egi.eu/xml/voIDCard/public/all/true)");
 		System.out.println("  --help            # Prints this info");
 	}
 
 	public static void main(String[] args) {
 
 		String xmlUrl = null;
 		String sidFile = null;
 
 		// Arg processing
 		StringBuffer sb = new StringBuffer();
 		String arg;
 		LongOpt[] longopts = new LongOpt[OptList.values().length];
 		longopts[OptList.help.ordinal()] = new LongOpt(OptList.help.name(), LongOpt.NO_ARGUMENT, sb, OptList.help.ordinal());
 		longopts[OptList.xmlurl.ordinal()] = new LongOpt(OptList.xmlurl.name(), LongOpt.REQUIRED_ARGUMENT, sb, OptList.xmlurl.ordinal());
 		longopts[OptList.sidfile.ordinal()] = new LongOpt(OptList.sidfile.name(), LongOpt.REQUIRED_ARGUMENT, sb,
 				OptList.sidfile.ordinal());
 
 		Getopt g = new Getopt("testprog", args, "", longopts);
 		g.setOpterr(false);
 
 		int c;
 		while ((c = g.getopt()) != -1) {
 
 			arg = g.getOptarg();
 
 			// It only takes long options
 			if (c != 0) {
 				System.out.print("Some option was given that I don't understand, " + sb.toString() + " \n");
 				printHelpPage();
 				System.exit(1);
 			}
 
 			if ((char) (new Integer(sb.toString())).intValue() == OptList.help.ordinal()) {
 				printHelpPage();
 				System.exit(1);
 			}
 			if ((char) (new Integer(sb.toString())).intValue() == OptList.xmlurl.ordinal()) {
 				xmlUrl = ((arg != null) ? arg : "null");
 			}
 			else if ((char) (new Integer(sb.toString())).intValue() == OptList.sidfile.ordinal()) {
 				sidFile = ((arg != null) ? arg : "null");
 			}
 		}
 
 		// Set default xml url if not given
 	  if (xmlUrl == null) {
 	    xmlUrl = "http://operations-portal.egi.eu/xml/voIDCard/public/all/true";
     }
 
 	  // Validate sidFile
 		if (sidFile == null) {
 			System.out.print("The --sidfile  argument must be given\n");
 			System.exit(1);
 		}
 
 		if (!(new File(sidFile)).isFile()) {
 			System.out.print("The --sidfile (" + sidFile + ") doesn't exist\n");
 			System.exit(1);
 		}
 
 		// Make the Controller class
 		SiteChecker vs = new SiteChecker(xmlUrl, sidFile);
 
 		// Parse the XML File
 		vs.parseXml();
 		
 		// Parse the SID File
 		vs.parseSid();
 
 		
 		// Compare the XML to the SID
 		vs.compare();
 
 	}
 
 
 	/**
 	 * Breaks up a yaim variable string, according to this contract Contract: + NAME= then: Nothing - string is empty Space - string
 	 * is empty String, without leading ' char - whole content is one element
 	 * 
 	 * String with leading ' char: then: without '\''' - string is simple string bounded by '', one element with '\''' ... string is a
 	 * sequence of elements separated by that
 	 * 
 	 * @param cmdLine command to execute
 	 * @return output from command
 	 */
 
 	private static ArrayList<String> breakString(String s) {
 
 		String payload = s;
 
 		payload = payload.substring(s.indexOf('=') + 1);
 		payload = payload.trim();
 		if (payload.isEmpty()) {
 			// No elements
 			return new ArrayList<String>();
 		}
 
 		if (!payload.startsWith("'")) {
 			// One element
 			ArrayList<String> res = new ArrayList<String>();
 			res.add(payload);
 			return res;
 		}
 		else {
 			// String with leading ' char.
 			// Cut off bounding chars
 			payload = payload.substring(1, payload.length() - 1);
 			if (payload.startsWith("'\\''")) {
 				// Some elements ...
 
 				// This is what '\'' looks like in Java as a regex. Don't blame me.
 				String[] tokens = payload.split("\\'\\\\\'\'");
 				ArrayList<String> res = new ArrayList<String>();
 				for (int ii = 0; ii < tokens.length; ii++) {
 					if (!tokens[ii].matches("^\\s*$")) {
 						res.add(tokens[ii]);
 					}
 				}
 				return res;
 			}
 			else {
 				// One element
 				ArrayList<String> res = new ArrayList<String>();
 				res.add(payload);
 				return res;
 			}
 		}
 	}
 }
