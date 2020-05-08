 // %3449686638:hoplugins.teamAnalyzer.manager%
 package ho.module.teamAnalyzer.manager;
 
 import ho.core.db.DBManager;
 import ho.core.model.match.MatchKurzInfo;
 import ho.core.module.config.ModuleConfig;
 import ho.module.matches.SpielePanel;
 import ho.module.teamAnalyzer.SystemManager;
 import ho.module.teamAnalyzer.ui.TeamAnalyzerPanel;
 import ho.module.teamAnalyzer.vo.Match;
 import ho.module.teamAnalyzer.vo.MatchDetail;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 
 /**
  * TODO Missing Class Documentation
  *
  * @author TODO Author Name
  */
 public class MatchManager {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     private static MatchList matches = null;
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static List<Match> getAllMatches() {
         return matches.getMatches();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static List<MatchDetail> getMatchDetails() {
         List<Match> filteredMatches = getSelectedMatches();
         MatchPopulator matchPopulator = new MatchPopulator();
 
         return matchPopulator.populate(filteredMatches);
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public static List<Match> getSelectedMatches() {
         if (matches == null) {
             loadActiveTeamMatchList();
         }
 
         return matches.filterMatches(TeamAnalyzerPanel.filter);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public static void clean() {
         loadActiveTeamMatchList();
     }
 
     /**
      * TODO Missing Method Documentation
      */
     public static void loadActiveTeamMatchList() {
         matches = new MatchList();
 
         SortedSet<Match> sortedMatches = loadMatchList();
 
         for (Iterator<Match> iter = sortedMatches.iterator(); iter.hasNext();) {
             Match element = iter.next();
 
             matches.addMatch(element);
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private static List<Match> getTeamMatch() {
         List<Match> teamMatches = new ArrayList<Match>();
         String oldName = SystemManager.getActiveTeamName();
 
         MatchKurzInfo[] matchKurtzInfo = DBManager.instance().getMatchesKurzInfo(SystemManager
                                                                                 .getActiveTeamId(),
                                                                                 SpielePanel.NUR_EIGENE_SPIELE,
                                                                                 false);
 
         for (int i = 0; i < matchKurtzInfo.length; i++) {
             MatchKurzInfo matchInfo = matchKurtzInfo[i];
 
             if (matchInfo.getMatchStatus() != MatchKurzInfo.FINISHED) {
                 continue;
             }
 
             Match match = new Match(matchInfo);
 
             String temp;
 
             if (match.getHomeId() == SystemManager.getActiveTeamId()) {
                 temp = match.getHomeTeam();
             } else {
                 temp = match.getAwayTeam();
             }
 
             if (ModuleConfig.instance().getBoolean(SystemManager.ISCHECKTEAMNAME)) {
                 // Fix for missing last dot!
                 String oldShort = oldName.substring(0, oldName.length() - 1);
 
                 if (oldShort.equalsIgnoreCase(temp)) {
                     temp = oldName;
                 }
 
                 if (!temp.equalsIgnoreCase(oldName)) {
                    if (match.getWeek() > 14) {
                         oldName = temp;
                     } else {
                         return teamMatches;
                     }
                 }
             }
 
             teamMatches.add(match);
         }
 
         return teamMatches;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private static SortedSet<Match> loadMatchList() {
         Map<String,Match> matchIds = new HashMap<String,Match>();
 
         for (Iterator<Match> iter = getTeamMatch().iterator(); iter.hasNext();) {
             Match match = iter.next();
 
             if (!matchIds.containsKey(match.getMatchId() + "")) {
                 matchIds.put(match.getMatchId() + "", match);
             }
         }
 
         Collection<Match> matchList = matchIds.values();
         SortedSet<Match> sorted = getSortedSet(matchList, new MatchComparator());
 
         return sorted;
     }
     
     private static<T> SortedSet<T> getSortedSet(Collection<T> beans, Comparator<T> comparator) {
         final SortedSet<T> set = new TreeSet<T>(comparator);
 
         if ((beans != null) && (beans.size() > 0)) {
             set.addAll(beans);
         }
 
         return set;
     }
 }
