 package uk.ac.cam.jk510.part2project.settings;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.SocketException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.LinkedList;
 import java.util.Random;
 
 import org.apache.http.client.CircularRedirectException;
 
 import uk.ac.cam.jk510.part2project.location.GCS;
 import uk.ac.cam.jk510.part2project.network.DataConnectionManager;
 import uk.ac.cam.jk510.part2project.network.Transport;
 import uk.ac.cam.jk510.part2project.protocol.Proto;
 import uk.ac.cam.jk510.part2project.session.SessionEnum;
 import uk.ac.cam.jk510.part2project.store.Coords;
 import uk.ac.cam.jk510.part2project.store.CoordsType;
 import uk.ac.cam.jk510.part2project.store.HistoryType;
 import android.graphics.Color;
 
 public class Config {
 
 	/*
 	 * Currently used to store all settings and values.
 	 * Will eventually be used only for hard coded app data, with PreferenceManager catering for the adjustable settings.
 	 */
 
 	private static final int MinUpdateRedrawSize = 1;	//number of new points to recieve before redrawing
 	private static int MapLineThickness = 3;	//thickness of paths drawn in MapDisplayScreen
 	private static int arrayBlockSize = 600;		// At 1 per second, thats 10 mins per chunk
 	private static int arrayIndexFrequency = 10;	// Finest granularity of points. In samples per 10 sec.
 
 	private static String name = String.valueOf(Math.random());	//TODO temp fix: random name for each device
 	private static final int keepAlivePeriod = 10000;	//TODO actually use this!!!
 	private static final int targetCoordsPerPacket = 5;	//target number of points per packet sent. (send less on timeout)
 	private static final long sendTimeout = 1000;	//max t=time to wait before sending an incomplete packet.
 
 	//Missing Data / Requests
 	private static final int missingDataThreshold = 1;	//number of missing points before request is sent to other devices.
 	private static final int missingDataTimeThreshold = 20*1000;	//millisec max time between checks for missing data.
 	private static final int missingDataCheckThreshold = 10;	//number of network points received that triggers missing check.
 	private static final boolean replyToRequestsToMultiplePeers = false;	/*	when set, whenever a device responds to a missing request, it will send its response
 																		to all requestable peers and not just the requester. */
 	//Colors
 	private static int bgColor = Color.WHITE;
 	private static int[] colors = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.BLACK, Color.DKGRAY, Color.MAGENTA, Color.LTGRAY, Color.YELLOW};	//TODO add more colours
 	private static float posIndicatorSize = 10;	//Radius of circle
 
 	//Map Displaying
 	private static final int charsOfNameToShow = 6;	//number of characters of player's name that is shown next to position.
 
 	//Protocol
 	private static Proto protocol = Proto.clientServer;
 	private static SessionEnum sesh = SessionEnum.quickstart;
 	private static CoordsType coordsType = CoordsType.TXYA;
 	private static HistoryType historyType = HistoryType.XYA;
 	private static boolean localOnly = (protocol == Proto.p2p);	//Only operate over local network. (e.g wifi) - so dont use external IP addresses.
 	private static final Transport transportProtocol = Transport.UDP;
 	private static final ResponseDecider decider = ResponseDecider.always;
 	private static boolean responseDecisionProbabilityInfluencedByRequestSize = true;
 	private static boolean dontSendPointsToOwner = true;	//Never send coordinates about device d, to device d.
 
 	//GPS Updates
 	private static final int gpsUpdateTime = 0;	//minTime between GPS position updates
 	private static final int gpsUpdateDistance = 0;	//min distance between GPS position updates
 	//moved to app settings
 	@Deprecated
 	private static final GCS gcs = GCS.UTM;		//Geographical Coordinate System for translating Lat/Long to cartesian x/y
 
 	//Hard-coded app data
 	private static final String UUIDString = "fa87c0d0-afac-11de-8a39-0800200c9a66"; //Randomly created string for use with this app
 
	//private static final String localServerIP = "192.168.137.1";	???
	//private static final String localServerIP = "192.168.1.20";
	private static final String localServerIP = "131.111.250.210";
 	private static final String globalServerIP = "jknoxville.no-ip.org";
 	private static final int serverPort = 60000;
 	private static final int defaultClientPort = 60001;
 	private static final int clientTCPPort = 60000;
 
 	//Logging options
 	private static final boolean takeScreenShots = false;
 	private static final long screenShotTimer = 5*1000;	//time between saving screenshots
 	private static final boolean loggingEnabled = true;
 
 	//Simulation
 	public static final boolean droppingEnabled = false;
 	public static final boolean markovPacketDroppingSimulation = false;
 	private static boolean currentlyDropping = false;
 	public static final double loseConnectionRate = 0.08;	//chance you lose connection in a given second
 	public static final double reconnectRate = 0.4;
 	public static final double dropRate = 0.6;
 	private static final long fakeGPSPeriod = 1*1000;	//1 second
 	private static final int testDataSize = 60;
 
 	//Datagram Format
 	private static final int nameSize = 4;
 
 	//Debug
 	private static final boolean debugModeOn = true;	//Send all p2p packets to server as well
 	private static final int sampleXDerby = 712026;
 	private static final int sampleYDerby = 9828785;
 	private static final int sampleXCam = 11195;
 	private static final int sampleYCam = 634959;
 	private static final boolean testingInCam = true; 	//if currently testing the app in cambridge, uk
 	private static final boolean sendOnTimeout = true;	//allows sending of not full packets if timer expires
 
 	//Config own variables
 	private static boolean checkedForLocalServer = false;
 
 	//Getter methods
 	public static int getMapLineThickness() {
 		return MapLineThickness;
 	}
 	public static int getArrayBlockSize() {
 		return arrayBlockSize;
 	}
 	public static int getArrayIndexFreq() {
 		return arrayIndexFrequency;
 	}
 	public static String getName() {
 		return name;
 	}
 	public static int getBackgroundColor() {
 		return bgColor;
 	}
 	public static Proto getProtocol() {
 		return protocol;
 	}
 	public static SessionEnum getSesh() {
 		return sesh;
 	}
 	public static CoordsType getCoordsType() {
 		return coordsType;
 	}
 	public static HistoryType getHistoryType() {
 		return historyType;
 	}
 	public static int getMinUpdateRedrawSize() {
 		return MinUpdateRedrawSize;
 	}
 	public static int getGPSUpdateTime() {
 		return gpsUpdateTime;
 	}
 	public static int getGPSUpdateDistance() {
 		return gpsUpdateDistance;
 	}
 	public static String getUUIDString() {
 		return UUIDString;
 	}
 	public static int getColor(int p) {
 		return colors[p%colors.length];
 	}
 	public static float getPosIndicatorSize() {
 		return posIndicatorSize;
 	}
 	public static String getServerIP() {
 		
 		String myIP;
 		if(!checkedForLocalServer) {
 			checkedForLocalServer = true;
 			try {
 				myIP = DataConnectionManager.getMyIP();
 				InetAddress server = InetAddress.getByName(globalServerIP);
 				if(myIP.equalsIgnoreCase(server.getHostAddress())) {
 					System.out.println("Detected server on local network, using local address");
 					localOnly = true;
 				}
 			} catch (SocketException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return (localOnly?localServerIP:globalServerIP);
 	}
 	public static int getServerPort() {
 		return serverPort;
 	}
 	public static int getDefaultClientPort() {
 		return defaultClientPort;
 	}
 	public static int getClientTCPPort() {
 		return clientTCPPort;
 	}
 	public static int getDatagramMetadataSize() {
 		return nameSize;	//TODO keep up to date with other metadata fields other than coordinates.
 	}
 	public static int getNameSize() {
 		return nameSize;
 	}
 	public static void setName(String name2) {
 		name = name2;
 
 	}
 	public static long getKeepAlivePeriod() {
 		return keepAlivePeriod;
 	}
 	public static boolean debugMode() {
 		return debugModeOn;
 	}
 	public static int getMinCoordsPerPacket() {
 		return targetCoordsPerPacket;
 	}
 	public static Transport transportProtocol() {
 		return transportProtocol;
 	}
 	public static int missingDataThreshold() {
 		return missingDataThreshold;
 	}
 	public static long missingCheckTimer() {
 		return missingDataTimeThreshold;
 	}
 	public static long missingDataCheckThreshold() {
 		return missingDataCheckThreshold;
 	}
 	public static boolean replyToRequestsToMultiplePeers() {
 		return replyToRequestsToMultiplePeers;
 	}
 	public static long getSendTimeout() {
 		return sendTimeout;
 	}
 	public static int getSampleX() {
 		return testingInCam?sampleXCam:sampleXDerby;
 	}
 	public static int getSampleY() {
 		return testingInCam?sampleYCam:sampleYDerby;
 	}
 	public static int charsOfNameToShow() {
 		return charsOfNameToShow;
 	}
 	public static boolean sendOnTimeout() {
 		return sendOnTimeout;
 	}
 	public static ResponseDecider responseDecider() {
 		return decider;
 	}
 	public static boolean responseDecisionProbabilityWithRequestSize() {
 		return responseDecisionProbabilityInfluencedByRequestSize;
 	}
 	public static boolean dontSendPointsToOwner() {
 		return dontSendPointsToOwner;
 	}
 	//	public static String getExternalServerIP() {
 	//		//Only to be used for session setup. For main server comms should use getServerIP().
 	//		return globalServerIP;
 	//	}
 	public static boolean takeScreenShots() {
 		return takeScreenShots;
 	}
 	public static long getScreenShotTimer() {
 		return screenShotTimer;
 	}
 	public static boolean droppingPackets() {
 		if(droppingEnabled) {
 			if(markovPacketDroppingSimulation) {
 
 				if(Math.random() <= (currentlyDropping?reconnectRate:loseConnectionRate)) {
 					currentlyDropping = !currentlyDropping;
 				}
 				return currentlyDropping;
 			} else {
 				if(Math.random() <= dropRate) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	public static long fakeGPSPeriod() {
 		return fakeGPSPeriod;
 	}
 	//moved to app preferences
 	@Deprecated
 	public static GCS getGCS() {
 		return gcs;
 	}
 	public static int testDataSize() {
 		return testDataSize;
 	}
 	public static boolean loggingEnabled() {
 		return loggingEnabled;
 	}
 }
