 package api;
 
 import java.io.IOException;
 
 import api.entity.ArenaDetails;
 import api.parser.XMLArenaDetailsParser;
 
 /**
  * @author	Jan Van Haaren
  * @date	27 June 2013
  */
 public class HattrickDownloader {
 
 	private final static String ARENA_DETAILS_VERSION = "1.5";
 
 	private final static String PLAYERS_VERSION = "2.3";
 
 	private final static String PLAYER_DETAILS_VERSION = "2.4";
 
 	public static void main(String[] arguments) throws IOException {
 		HattrickDownloader downloader = new HattrickDownloader();
		ArenaDetails arenaDetails = downloader.getArenaDetails(511721);
		System.out.println("Current capacity: " + arenaDetails.getArena().getCurrentCapacity().getTotal());
 	}
 
 	public ArenaDetails getArenaDetails(int arenaId) throws IOException {
 		return XMLArenaDetailsParser.parseArenaFromString(this.getArenaDetailsString(arenaId));
 	}
 
 	private String getArenaDetailsString(int arenaId) throws IOException {
 
 		// file and version
 		String sURL = "?file=arenadetails&version=" + ARENA_DETAILS_VERSION;
 
 		// parameters
 		if (arenaId > 0) {
 			sURL += "&arenaID=" + arenaId;
 		}
 
 		// retrieve content
 		return this.getConnector().getWebContent(sURL);
 	}
 
 	public String getPlayersString(int teamId) throws IOException {
 
 		// file and version
 		String sURL = "?file=players&version=" + PLAYERS_VERSION;
 
 		// parameters
 		if (teamId > 0) {
 			sURL += "&teamID=" + teamId;
 		}
 
 		// retrieve content
 		return this.getConnector().getWebContent(sURL);
 	}
 
 	public String getPlayerDetailsString(int playerId) throws IOException {
 
 		// file and version
 		String sURL = "?file=playerdetails&version=" + PLAYER_DETAILS_VERSION;
 
 		// parameters
 		if (playerId > 0) {
 			sURL += "&playerID=" + playerId;
 		}
 
 		// retrieve content
 		return this.getConnector().getWebContent(sURL);
 	}
 
 	private HattrickConnector getConnector() {
 		return HattrickConnector.getInstance();
 	}
 
 }
