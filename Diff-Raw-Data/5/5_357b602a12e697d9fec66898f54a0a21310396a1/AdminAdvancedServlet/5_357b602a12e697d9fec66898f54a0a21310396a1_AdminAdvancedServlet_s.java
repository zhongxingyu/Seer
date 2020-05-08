 package com.eleichtenschlag.nascar;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.*;
 
 import com.eleichtenschlag.nascar.manager.MailManager;
 import com.eleichtenschlag.nascar.model.DatastoreManager;
 import com.eleichtenschlag.nascar.model.Lineup;
 import com.eleichtenschlag.nascar.model.NascarConfig;
 import com.eleichtenschlag.nascar.model.NascarConfigSingleton;
 import com.eleichtenschlag.nascar.model.Race;
 import com.eleichtenschlag.nascar.model.Result;
 import com.eleichtenschlag.nascar.model.Team;
 
 @SuppressWarnings("serial")
 public class AdminAdvancedServlet extends HttpServlet {
   private static String LAST_OPERATION_MESSAGE = "None";
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
     resp.setContentType("text/html");
     resp.getWriter().println("<a href='/'>Back to home</a><br/>");
     resp.getWriter().println("This is the admin page for making system updates.");
     resp.getWriter().println("Please expect the system to take about 15 seconds to perform these actions.");
     
     NascarConfig config = NascarConfigSingleton.get();
     Race race = config.getRace();
     resp.getWriter().println(String.format(
         "<h3>Current race: %d - %d (%s)</h3>", race.getYear(),
             race.getWeek(), race.getRaceName()));
     resp.getWriter().println("<form method='POST'>");
     resp.getWriter().println("<p>Click the following magic button after the race results are in to prepare the system for the new week.</p>");
     resp.getWriter().println("<input type='hidden' name='action' value='nextrace'/>");
     resp.getWriter().println("<button type='submit'>Calculate Results and go to next Race</button></form>");
     
     resp.getWriter().println("<form method='POST'>");
     String lineupString = config.getCanEditLineup() ? "Lineups are unlocked" : "Lineups are locked";
     resp.getWriter().println(lineupString);
     resp.getWriter().println("<input type='hidden' name='action' value='toggleeditable'/>");
     String lineupText = config.getCanEditLineup() ? "Lock lineups" : "Unlock lineups";
     resp.getWriter().println("<button type='submit'>" + lineupText + "</button></form>");
     
     /** Mailing options */
     resp.getWriter().println("<form method='POST' style='display: inline-block; border:1px solid black;'>");
     resp.getWriter().println("<b>Mailing Options:</b><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='maildrivers' value='maildrivers'/>" + 
     "<label for='maildrivers'>Mail Drivers for current week</label><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='maillineups' value='maillineups'/>" +
     "<label for='maillineups'>Mail Lineups for current week</label><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='mailresults' value='mailresults'/>" +
     "<label for='mailresults'>Mail Results for Previous week</label><br/>");
     resp.getWriter().println("<button type='submit'>Send Mail</button></form>");
     /** End Mailing options */
     
     resp.getWriter().println("<br/><a href='/admin/lineup'>Click here to administratively set team lineups</a><br/>");
     
     resp.getWriter().println("<br/><h4>Advanced administration options</h4>");
     
     resp.getWriter().println("<form method='POST' style='display: inline-block; border:1px solid black;'>");
     resp.getWriter().println("<b>Perform action on race entered:</b><br/>");
     resp.getWriter().println("Year (2012 for example):<input type='number' name='year' required/><br/>");
     resp.getWriter().println("Race number (1-36):<input type='number' name='racenumber' required/><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='setconfig' value='setconfig'/>" +
                              "<label for='setconfig'>Change current race</label><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='pulldrivers' value='pulldrivers'/>" + 
                              "<label for='pulldrivers'>Populate Driver Data (This will let you select drivers)</label><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='pullresults' value='pullresults'/>" +
                              "<label for='pullresults'>Populate Results Data</label><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='calculatescores' value='calculatescores'/>" +
                              "<label for='calculatescores'>Calculate lineup scores</label><br/>");
     resp.getWriter().println("<input type='radio' name='action' id='clearlineups' value='clearlineups'/>" + 
                              "<label for='clearlineups'>Clear lineups and results(WARNING: This option is not reversable)</label><br/>");
     resp.getWriter().println("<button type='submit'>Submit</button></form>");
     resp.getWriter().println("<br/><br/>");
     
     resp.getWriter().println("<form method='POST' style='display: inline-block; border:1px solid black;'>");
     resp.getWriter().println("<b>Delete a team</b><br/>");
     resp.getWriter().println("WARNING: This option is not reversable<br/>");
     resp.getWriter().println(this.getTeamSelectHtml());
     resp.getWriter().println("<input type='hidden' name='action' value='removeteam'/>");
     resp.getWriter().println("<button type='submit'>Delete Team</button></form>");
     
     resp.getWriter().println("<BR/>Last operation: " + LAST_OPERATION_MESSAGE);
   }
   
   public void doPost(HttpServletRequest req, HttpServletResponse resp)
       throws IOException {
     // If action is next race or toggle config, don't need any other params.
     // Also don't need params for mailing.
     String action = "";
     if (req.getParameter("action") != null) {
       action = req.getParameter("action");
     }
     if (action.equals("nextrace")) {
       this.nextRace();
     } else if (action.equals("toggleeditable")) {
       this.toggleLineupEditable();
     } else if (action.equals("maildrivers")) {
       this.mailDrivers();
     } else if (action.equals("maillineups")) {
       this.mailLineups();
     } else if (action.equals("mailresults")) {
       this.mailResults();
     }
     
     // The remove team option needs a teamname.
     if (action.equals("removeteam") &&
         req.getParameter("teamname") != null) {
       String teamName = req.getParameter("teamname");
       this.removeTeam(teamName);
     }
     
     // All other actions require a year and race number.
     int year = 0;
     int raceNum = 0;
     if (req.getParameter("year") != null) {
       year = Integer.parseInt(req.getParameter("year"));
     }
     if (req.getParameter("racenumber") != null) {
       raceNum = Integer.parseInt(req.getParameter("racenumber"));
     }
     if (year > 0 && raceNum > 0) {
       Race race = this.getRace(year, raceNum);
       if (action.equals("setconfig")) {
         this.setConfig(race);
       } else if (action.equals("pulldrivers")) {
         this.pullDrivers(race);
       } else if (action.equals("pullresults")) {
         this.pullResults(race);
       } else if (action.equals("calculatescores")) {
         this.calculateScores(race);
       } else if (action.equals("clearlineups")) {
         this.clearLineups(race);
       }
     }
     // Go back to same webpage.
     resp.sendRedirect("/admin/advanced");
   }
   
   private void nextRace() {
     // Pull results for old week, then set new week.
     NascarConfig config = NascarConfigSingleton.get();
     Race race = config.getRace();
     this.pullResults(race);
     this.calculateScores(race);
     config.goToNextWeek();
     Race newRace = config.getRace();
     
     // Pull drivers for new week.
     this.pullDrivers(newRace);
     LAST_OPERATION_MESSAGE = "Went to next race";
   }
   
   private void toggleLineupEditable() {
     NascarConfig config = NascarConfigSingleton.get();
     config.toggleCanEditLineup();
     //if (!config.getCanEditLineup()) {
     //  MailManager.sendLineupsEmail(config.getRace());
     //}
     LAST_OPERATION_MESSAGE = "Toggled editable lineup";
   }
   
   private void setConfig(Race race) {
     NascarConfig config = NascarConfigSingleton.get();
     config.setRace(race.getYear(), race.getWeek());
     LAST_OPERATION_MESSAGE = String.format("Config Set for year %d race %d",
         race.getYear(), race.getWeek());
     // Also pull drivers.
     this.pullDrivers(race);
   }
   
   /**
    * Mail drivers for upcoming race.  This should be done when drivers are
    * pulled.
    */
   private void mailDrivers() {
     NascarConfig config = NascarConfigSingleton.get();
     Race race = config.getRace();
     MailManager.sendDriversEmail(race);
   }
 
   /**
    * Mail lineups for a current race.  This should be done when lineups are
    * locked.
    */
   private void mailLineups() {
     NascarConfig config = NascarConfigSingleton.get();
     Race race = config.getRace();
     MailManager.sendLineupsEmail(race);
   }
 
   /**
    * Mails results for previous week.  This should be done when results are
    * pulled.
    */
   private void mailResults() {
     NascarConfig config = NascarConfigSingleton.get();
     Race race = config.getRace();
     int week = race.getWeek();
     Map<String, Object> filters = new HashMap<String, Object>();
     filters.put("year", race.getYear());
     filters.put("week", week - 1);
     List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
     if (races.size() > 0) {
       Race previousRace = races.get(0);
       MailManager.sendPreviousWeekResultsEmail(previousRace);
     }
   }
 
   private void pullDrivers(Race race) {
     DatastoreManager.populateDriverData(race.getYear(), race.getWeek());
     LAST_OPERATION_MESSAGE = String.format("Drivers pulled for year %d race %d",
         race.getYear(), race.getWeek());
   }
   
   private void pullResults(Race race) {
     DatastoreManager.populateResults(race);
     LAST_OPERATION_MESSAGE = String.format("Results pulled for year %d race %d",
         race.getYear(), race.getWeek());
   }
 
   private void calculateScores(Race race) {
     DatastoreManager.calculateScores(race);
     LAST_OPERATION_MESSAGE = String.format("Scores calculated for year %d race %d",
         race.getYear(), race.getWeek());
   }
   
   /** Clears all lineups and results for current week. */
   private void clearLineups(Race race) {
     Map<String, Object> filters = new HashMap<String, Object>();
     filters.put("raceKey", race.getKey());
     DatastoreManager.deleteAllObjectsWithFilters(Lineup.class, filters);
     DatastoreManager.deleteAllObjectsWithFilters(Result.class, filters);
     LAST_OPERATION_MESSAGE = String.format("Lineups and Results cleared for year %d race %d",
         race.getYear(), race.getWeek());
   }
 
   /**
    * Remove results, lineups, team, and owner for given team name.
    * NOTE: Does not recalculate weeks won for standings.
    * 
    * @param teamName - The team name of the team to delete.
    */
   private void removeTeam(String teamName) {
     Team team = this.getTeam(teamName);
     Map<String, Object> filters = new HashMap<String, Object>();
     filters.put("teamKey", team.getKey());
     DatastoreManager.deleteAllObjectsWithFilters(Lineup.class, filters);
     DatastoreManager.deleteAllObjectsWithFilters(Result.class, filters);
     // Delete owner before team.
     DatastoreManager.deleteObjectWithKey(team.getOwnerKey());
     DatastoreManager.deleteObjectWithKey(team.getKey());
     LAST_OPERATION_MESSAGE = String.format("Delete team: %s", team.getTeamName()); 
   }
   
   private Race getRace(int year, int raceNum) {
     Map<String, Object> filters = new HashMap<String, Object>();
     filters.put("year", year);
     filters.put("week", raceNum);
     List<Race> races = DatastoreManager.getAllObjectsWithFilters(Race.class, filters);
     Race race = races.get(0);
     return race;
   }
   
   private Team getTeam(String teamName) {
     Map<String, Object> filters = new HashMap<String, Object>();
     filters.put("teamName", teamName);
     List<Team> teams = DatastoreManager.getAllObjectsWithFilters(Team.class, filters);
     Team team = teams.get(0);
     return team;
   }
   
   private String getTeamSelectHtml() {
     String teamSelectHtml = "<select id='teamselect' name='teamname'>";
     List<Team> teams = DatastoreManager.getAllObjects(Team.class);
     for (Team team: teams) {
       teamSelectHtml += String.format("<option value='%s'>%s</option>", team.getTeamName(), team.getTeamName());
     }
     teamSelectHtml += "</select>";
     return teamSelectHtml;
   }
 }
