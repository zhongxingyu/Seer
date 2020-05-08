 package com.schmal.service;
 
 import com.schmal.dao.PlayerDAO;
 import com.schmal.dao.ResultDAO;
 import com.schmal.dao.WeekDAO;
 import com.schmal.domain.Category;
 import com.schmal.domain.League;
 import com.schmal.domain.Player;
 import com.schmal.domain.Result;
 import com.schmal.domain.ScoringPeriod;
 import com.schmal.domain.Stat;
 import com.schmal.domain.Week;
 import com.schmal.util.DateUtil;
 import com.schmal.util.FantasyURLBuilder;
 import com.schmal.util.PlayerUtil;
 import com.yammer.dropwizard.hibernate.HibernateBundle;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import lombok.extern.slf4j.Slf4j;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 @Slf4j
 public class PlayerService
 {
     private final PlayerDAO dao;
     private final ResultDAO resultDAO;
     private final WeekDAO weekDAO;
 
     private final LeagueService leagueService;
     private final ScoringPeriodService scoringPeriodService;
     private final WeekService weekService;
 
     private final PlayerUtil playerUtil = new PlayerUtil();
 
     private final HibernateBundle hibernateBundle;
 
     private Timings timings;
 
     public PlayerService(HibernateBundle hibernateBundle)
     {
         dao = new PlayerDAO(hibernateBundle.getSessionFactory());
         resultDAO = new ResultDAO(hibernateBundle.getSessionFactory());
         weekDAO = new WeekDAO(hibernateBundle.getSessionFactory());
 
         leagueService = new LeagueService(hibernateBundle);
         scoringPeriodService = new ScoringPeriodService(hibernateBundle);
         weekService = new WeekService(hibernateBundle);
 
         this.hibernateBundle = hibernateBundle;
     }
 
     public void syncResults(long leagueID) throws Exception
     {
         // Get the league
         League league = leagueService.getLeagueByID(leagueID);
 
         // Determine the current max results we've created already.
         int maxScoringPeriod = resultDAO.getMaxScoringPeriod(leagueID);
 
         // Start one day before the max, just in case something was updated posthumously.
         int startPeriod = Math.max(1, maxScoringPeriod - 1);
 
         // Ending period is the last scoring period up to today.
         int endPeriod = scoringPeriodService.getMaxScoringPeriod(league.getID()).getPeriodID();
 
         syncResults(league, startPeriod, endPeriod);
     }
 
     public Week syncResults(long weekID, int maxPeriodID) throws Exception
     {
         Week week = weekDAO.getByID(weekID);
 
         int startPeriod = week.getStartPeriod().getPeriodID();
         int endPeriod = Math.min(week.getEndPeriod().getPeriodID(), maxPeriodID);
 
         syncResults(week.getLeague(), startPeriod, endPeriod);
 
         return weekService.computeScore(week, true);
     }
 
     private void syncResults(
         League league,
         int startPeriod,
         int endPeriod)
         throws Exception
     {
         Timings totalTimings = new Timings();
         int curPeriod = startPeriod;
 
         // Loop until we hit a scoring period without anything.
         while (curPeriod <= endPeriod)
         {
             this.timings = new Timings();
             setResults(league, curPeriod);
             // done = true; // TEMPORARY OBVIOUSLY
             // done = (done || period >= 1);
             curPeriod++;
             this.timings.log();
 
             totalTimings.documentLoading += this.timings.documentLoading;
             totalTimings.categoryMaps += this.timings.categoryMaps;
             totalTimings.clearingStats += this.timings.clearingStats;
             totalTimings.creatingStats += this.timings.creatingStats;
         }
 
         totalTimings.log();
     }
 
     public List<Player> createProjections(long leagueID) throws Exception
     {
         // Get the league
         League league = leagueService.getLeagueByID(leagueID);
 
         return league.getPlayers();
     }
 
     private boolean setResults(
         League league,
         int period)
         throws Exception
     {
         int statRows = 0;
 
         // 1 = Batters, 2 = Pitchers
         for (int categoryGroup = 1; categoryGroup <= 2; categoryGroup++)
         {
             int startIndex = 0;
             boolean done = false;
             while (!done)
             {
                 URL resultURL = FantasyURLBuilder.getScoringPeriodURL(league, period, categoryGroup, startIndex);
                 int rows = setResults(resultURL, league, period, categoryGroup);
 
                 done = (rows < 50);
 
                 statRows += rows;
                 startIndex += 50;
             }
         }
 
         return (statRows == 0);
     }
 
     private int setResults(
         URL resultURL,
         League league,
         int period,
         int categoryGroup)
         throws Exception
     {
         log.info(resultURL.toString());
 
         int statRows = 0;
 
         long startTime = System.nanoTime();
         Document playerDoc = Jsoup.connect(resultURL.toString()).timeout(0).get();
         this.timings.documentLoading += System.nanoTime() - startTime;
 
         startTime = System.nanoTime();
         Map<Integer,Category> categoryMap = createCategoryMap(league, playerDoc, categoryGroup);
         this.timings.categoryMaps += System.nanoTime() - startTime;
 
         Elements playerRows = playerDoc.select(".pncPlayerRow");
         for (Element playerRow : playerRows)
         {
             if (hasNoStats(playerRow))
             {
                 break;
             }
 
             statRows++;
 
             Player player = playerUtil.getPlayer(playerRow.children().first(), league);
 
             Result result = getResult(player, period, playerDoc);
 
             startTime = System.nanoTime();
             result.getStats().clear();
             this.timings.clearingStats += System.nanoTime() - startTime;
 
             setStats(league, result, categoryMap, playerRow);
         }
 
         return statRows;
     }
 
     private boolean hasNoStats(Element playerRow)
     {
         Element firstCategory = playerRow.children().select(".playertableStat").first();
 
        return getStatText(firstCategory).contains("--");
     }
 
     private String getStatText(Element cell)
     {
         boolean hasSpan = (cell.select("span").size() > 0);
         return (hasSpan) ? cell.select("span").first().ownText() : cell.ownText();
     }
 
     private Result getResult(
         Player player,
         int periodID,
         Document playerDoc)
         throws Exception
     {
         Result result = player.getResult(periodID);
 
         if (result == null)
         {
             Date date = DateUtil.getDate(
                 playerDoc.select(".playerTableBgRowHead").select("th").get(2).ownText(),
                 player.getLeague().getYear());
             ScoringPeriod scoringPeriod = player.getLeague().getScoringPeriodMap().get(periodID);
             result = new Result(player, scoringPeriod);
             result.setStats(new ArrayList<Stat>());
 
             player.addResult(result);
         }
 
         return result;
     }
 
     private void setStats(
         League league,
         Result result,
         Map<Integer,Category> categoryMap,
         Element playerRow)
         throws Exception
     {
         long startTime = System.nanoTime();
         Elements categoryElements = playerRow.select(".playertableStat");
         for (int i = 0; i < categoryElements.size(); i++)
         {
             Element categoryElement = categoryElements.get(i);
 
             if (categoryElement.hasClass("appliedPoints"))
             {
                 continue;
             }
 
            Category category = categoryMap.get(i);

            float statValue = ("H".equals(category.getCategory())) ?
                Float.parseFloat(getStatText(categoryElement).split("/")[0]) :
                Float.parseFloat(getStatText(categoryElement));
 
             if (statValue == 0f)
             {
                 continue;
             }
 
             result.getStats().add(new Stat(result, categoryMap.get(i), statValue));
         }
 
         result.calculateScore();
 
         this.timings.creatingStats += System.nanoTime() - startTime;
     }
 
     /**
      * Create map from DOM column index to Category object.
      */
     private Map<Integer,Category> createCategoryMap(League league, Document doc, int categoryGroup) throws Exception
     {
         Map<Integer,Category> categoryMap = new HashMap<Integer,Category>();
         char categoryType = (categoryGroup == 1) ? 'B' : 'P';
 
         Elements categories = doc.select(".playerTableBgRowSubhead").select(".playertableStat");
         for (int i = 0; i < categories.size(); i++)
         {
             Element categoryElement = categories.get(i);
 
             if (categoryElement.hasClass("appliedPoints"))
             {
                 continue;
             }
 
             Category category = league.getCategory(categoryType, categoryElement.select("a").first().ownText());
             categoryMap.put(i, category);
         }
 
         return categoryMap;
     }
 
     private class Timings
     {
         public long documentLoading = 0l;
         public long categoryMaps = 0l;
         public long clearingStats = 0l;
         public long creatingStats = 0l;
 
         public void log()
         {
             log.info("Document loading: " + (documentLoading / 1000000l) + " ms.");
             log.info("Category maps: " + (categoryMaps / 1000000l) + " ms.");
             log.info("Clearing stats: " + (clearingStats / 1000000l) + " ms.");
             log.info("Creating stats: " + (creatingStats / 1000000l) + " ms.");
         }
     }
 }
