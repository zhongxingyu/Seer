 // %1667190662:hoplugins.teamAnalyzer.ht%
 package hoplugins.teamAnalyzer.ht;
 
 import hoplugins.Commons;
 import hoplugins.teamAnalyzer.SystemManager;
 import hoplugins.teamAnalyzer.manager.PlayerDataManager;
 import hoplugins.teamAnalyzer.vo.PlayerInfo;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import plugins.IXMLParser;
 
 
 /**
  * Hattrick Download Helper class
  *
  * @author <a href=mailto:draghetto@users.sourceforge.net>Massimiliano Amato</a>
  */
 public class HattrickManager {
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Method that download from Hattrick the available matches for the team
      *
      * @param teamId teamid to download matches for
      */
     public static void downloadMatches(final int teamId) {
     	final HashSet<String> matches = new HashSet<String>();
     	try {
     		boolean download = true;
     		String xml;
     		final Calendar start = Calendar.getInstance();
			final Date oneyearold = new Date(start.getTimeInMillis() - 31536000000l);
     		final Calendar end = Calendar.getInstance();
     		start.setLenient(true);
     		start.add(Calendar.DAY_OF_YEAR, -(7*7)); // 7 week = half season
     		end.setLenient(true);
     		end.add(Calendar.DAY_OF_YEAR, 1);
     		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
     		String url;
 
     		while (download) {
     			url = "/chppxml.axd?file=matchesarchive&teamID=" + teamId + "&FirstMatchDate=" + sdf.format(start.getTime()) + "&LastMatchDate=" + sdf.format(end.getTime());
     			log(url + " / " + matches.size());
     			xml = Commons.getModel().getDownloadHelper().getHattrickXMLFile(url);
 
     			IXMLParser parser = Commons.getModel().getXMLParser();
     			Document dom = parser.parseString(xml);
     			Node matchesList = dom.getElementsByTagName("MatchList").item(0);
 
     			for (int i = 0; i < matchesList.getOwnerDocument().getElementsByTagName("MatchID").getLength(); i++) {
     				if (matchesList.getOwnerDocument().getElementsByTagName("MatchID").item(i) == null) {
     					continue;
     				}
     				String matchId = matchesList.getOwnerDocument().getElementsByTagName("MatchID").item(i).getFirstChild().getNodeValue();
     				if (!matches.contains(matchId)) {
     					Commons.getModel().getHelper().downloadMatchData(Integer.parseInt(matchId));
     					matches.add(matchId);
     					if (matches.size() >= Math.max(20, SystemManager.getFilter().getNumber() * 3)) { // [3 x limit] matches ought to be enough for anybody.
     						download = false;
     						return;
     					}
     				} else {
     					continue;
     				}
     			}
     			// next 7 weeks
     			end.setTime(start.getTime());
         		start.add(Calendar.DAY_OF_YEAR, -(7*7));
         		if (end.getTime().before(oneyearold)) {
         			download = false;
         		}
     		}
         } catch (Exception e) {
             log("Error(downloadAdditionalMatches): " + e.getMessage());
         }
     }
 
     /**
      * Method that download from Hattrick the current players for the team
      *
      * @param teamId teamid to download players for
      */
     public static void downloadPlayers(int teamId) {
         String xml = "";
 
         try {
 			xml = Commons.getModel().getDownloadHelper().getHattrickXMLFile("/common/chppxml.axd?file=players&TeamID=" + teamId);
         } catch (Exception e) {
             return;
         }
 
         List<PlayerInfo> players = new ArrayList<PlayerInfo>();
         IXMLParser parser = Commons.getModel().getXMLParser();
         Document dom = parser.parseString(xml);
         Node matchesList = dom.getElementsByTagName("PlayerList").item(0);
 
         for (int i = 0; i < (matchesList.getChildNodes().getLength() / 2); i++) {
             PlayerInfo player = new PlayerInfo();
             player.setTeamId(teamId);
 
             int id = getIntValue(matchesList, i, "PlayerID");
             player.setPlayerId(id);
 
             int card = getIntValue(matchesList, i, "Cards");
             int injury = getIntValue(matchesList, i, "InjuryLevel");
             int status = PlayerDataManager.AVAILABLE;
 
             if (card == 3) {
                 status = PlayerDataManager.SUSPENDED;
             }
 
             if (injury > 0) {
                 status = PlayerDataManager.INJURED;
             }
 
             player.setStatus(status);
 
             int se = getIntValue(matchesList, i, "Specialty");
             player.setSpecialEvent(se);
 
             int form = getIntValue(matchesList, i, "PlayerForm");
             player.setForm(form);
 
             int exp = getIntValue(matchesList, i, "Experience");
             player.setExperience(exp);
 
             int age = getIntValue(matchesList, i, "Age");
             player.setAge(age);
 
             int tsi = getIntValue(matchesList, i, "TSI");
             player.setTSI(tsi);
 
             String name = getValue(matchesList, i, "PlayerName");
             player.setName(name);
 
             players.add(player);
         }
 
         PlayerDataManager.update(players);
         SystemManager.getPlugin().getMainPanel().getRosterPanel().reload();
     }
 
     /**
      * Method that download from Hattrick the team name
      *
      * @param teamId Tteamid to download name for
      *
      * @return Team Name
      *
      * @throws Exception if error occurs
      */
     public static String downloadTeam(int teamId) throws Exception {
 		String xml = Commons.getModel().getDownloadHelper().getHattrickXMLFile("/common/chppxml.axd?file=team&teamID=" + teamId);
         IXMLParser parser = Commons.getModel().getXMLParser();
         Document dom = parser.parseString(xml);
         Document teamDocument = dom.getElementsByTagName("Team").item(0).getOwnerDocument();
         String teamName = teamDocument.getElementsByTagName("TeamName").item(0).getFirstChild().getNodeValue();
 
         return teamName;
     }
 
     /**
      * Helper method to get a value from a Node.
      */
     private static int getIntValue(Node node, int i, String tag) {
         try {
             String value = getValue(node, i, tag);
             return Integer.parseInt(value);
         } catch (NumberFormatException e) {
         }
 
         return 0;
     }
 
     /**
      * Helper method to get a value from a Node.
      */
     private static String getValue(Node node, int i, String tag) {
         String value = node.getOwnerDocument().getElementsByTagName(tag).item(i).getFirstChild()
                            .getNodeValue();
         return value;
     }
 
     /**
      * Log to HOLogger.
      */
 	public final static void log(String message) {
 		try {
 			Commons.getModel().log(HattrickManager.class, message);
 		} catch (Exception e) {
 			System.out.println(HattrickManager.class + " - Error during log(): " + e);
 		}
 	}
 }
