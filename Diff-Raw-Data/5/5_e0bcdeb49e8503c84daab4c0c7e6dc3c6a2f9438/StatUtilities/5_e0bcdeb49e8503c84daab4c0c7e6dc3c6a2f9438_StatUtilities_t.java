 package pcricketstats;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * This is the class responsible for calculating statistics from the data in the
  * CSV file.The outputSort method handles the sorting - by providing the user
  * with options specified by an integer value.The sorting itself is handled by
  * the Comparator classes.
  * 
  * NOTE: Default sort of CSV file is for "wickets taken". Later on, I'll implement 
  * code that sorts the output beforehand (specified by the user), then the user can 
  * return list of players (or if a better idea is found, I'll implement that).
  *
  * @author Rob Attfield
  */
 public class StatUtilities {
 
     private String csvHeader = "Player ID | Player | Country | Career Span | Matches Played | Innings Played | Balls Bowled | Runs Conceded | "
             + "Wickets Taken | Bowling Average | Economy Rate | Strike Rate | 5 Wickets/Match";
     private ArrayList<String> countries = new ArrayList<String>();
 
     /**
      * This method will round a double number to 2 decimal places.
      *
      * @param value The double number to be formatted.
      * @return The double value, formatted to 2 decimal places.
      */
     public double toDoubleTwoDP(double value) {
         //This will round calculation results to 2 decimal places
         DecimalFormat toTwoDP = new DecimalFormat("#.##");
 
         return Double.parseDouble(toTwoDP.format(value));
     }
 
     /**
      * This method prints out the number of players specified by the user.
      *
      * @param players The list of players to be printed out.
      * @param noOfPlayers The number of players to be listed.
      * @return A list of players, with the number of players specified by
      * 'noOfPlayers'.
      */
     public String listNPlayers(ArrayList<Player> players, int noOfPlayers) {
         StringBuilder sb = new StringBuilder();
         sb.append(csvHeader + "\n");
         for (int i = 0; i < noOfPlayers; i++) {
             sb.append(players.get(i).toString() + "\n");
         }
 
         return sb.toString();
     }
 
     /**
      * This overloaded method will print out the details of each player - that
      * appear between "start" and "end" indexes of the players list.
      *
      * @param players The list of players to be printed out.
      * @param start The list position of the first player.
      * @param end The list position of the last player.
      * @return The list of players - from 'start' to 'end'
      */
     public String listNPlayers(ArrayList<Player> players, int start, int end) {
         //Some assistance and feedback for 'if' and 'for' loop from 
         //http://stackoverflow.com/questions/16411691/printing-out-values-between-two-indexes-in-for-loop/16412114
         
         //This reassignment takes into consideration that counting starts at
         //0 in Java. The first player has an I.D number of 1, not 0...
         start -= 1;
         end -= 1;
         
         StringBuilder sb = new StringBuilder();
         
         int i;
         if (start < 0) {
             sb.append("Your 'start' value must be greater than or equal to 1.");
         } 
         else if (end > players.size()) {
             sb.append("Your 'end' value cannot be greater than the size of your 'players' list.");
         } 
         else {
             sb.append(csvHeader + "\n");
             for (i = start; i <= end; i++) {
                 sb.append(players.get(i).toString() + "\n");
             }
         }
         return sb.toString();
     }
     
     /**
      * This method returns the details for a single player, 
      * as determined by the playerID parsed through.
      * @param players
      * @param playerID
      * @return A String object representing the player's details.
      */
     public String listSinglePlayer(ArrayList<Player> players, int playerID)
     {
         //This reassignment takes into consideration that counting starts at
         //0 in Java. The first player has an I.D number of 1, not 0...
         playerID -= 1;
         
         StringBuilder sb = new StringBuilder();
         
         if(playerID > players.size() || playerID == players.size())
         {
             return "'playerID' must be less than or equal to " + players.size() + ".";
         }
         else if(playerID < 0) //this is 0 because of earlier message
         {
             return "'playerID' must be greater than or equal to 1.";
         }
         else
         {
             return sb.append(players.get(playerID)).toString();
         }
     }
     
     /**
      * This method retrieves all the details for a single player, 
      * according to the name requested.
      * 
      * @param players The ArrayList of players to search through.
      * @param playerName The player name to retrieve details for.
      * @return A String representing the player's details.
      */
     public String listSinglePlayer(ArrayList<Player> players, String playerName)
     {
        
        //this is a temporary Player ArrayList which will hold only 1 player
        ArrayList<Player> player = new ArrayList<Player>();
         
        for(Player p: players)
        {
            if(p.getPlayerName().equalsIgnoreCase(playerName))
            {
                //add the matching player to the 'player' ArrayList
                player.add(p);
            }
        }
        
        //return the matched player's details
        if(player.size() == 1)
        {
            return player.get(0).toString();
        }
        else if(player.size() > 1)
        {
            return "Duplicate player has been found";
        }
        else
        {
            return "The player with the name \"" + playerName + "\" does not exist.";
        }
     }
 
     /**
      * This overloaded method calculates the average amount of innings per
      * player played, in a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the average innings played per player.
      * @return The average innings played per player, from the defined country.
      */
     public double aveInningsPerCountry(ArrayList<Player> players, String country) {
         int inningsByCountry = 0;
         int playersFromCountry = 0;
 
         /*
          * The following code fragment gets all the country names 
          * in the dataset, and gets the unique country names. This 
          * will be used to check if the country being searched for 
          * by the user exists.
          * 
          * With thanks to http://stackoverflow.com/questions/13429119/get-unique-values-from-arraylist-in-java
          */
         Set<String> countryNames = new HashSet<String>();
         for (Player p : players) {
             countryNames.add(p.getCountryName());
         }
 
         /*End code fragment */
         if (countryNames.contains(country)) {
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getCountryName().compareTo(country) == 0) {
                     inningsByCountry += players.get(i).getInningsPlayed();
                     playersFromCountry++;
                 }
             }
 
             double aveInningsByCountry = (inningsByCountry * 1.0) / playersFromCountry;
             return toDoubleTwoDP(aveInningsByCountry);
         } else {
             return 0.0;
         }
     }
 
     /**
      * This method gets the average innings played by each player, over the
      * whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average innings played per player.
      */
     public double aveInningsPerPlayer(ArrayList<Player> players) {
         int inningsPlayed = 0;
         for (int i = 0; i < players.size(); i++) {
             inningsPlayed += players.get(i).getInningsPlayed();
         }
 
         double aveInningsPerPlayer = (inningsPlayed * 1.0) / players.size();
         return toDoubleTwoDP(aveInningsPerPlayer);
     }
     
     /**
      * This method gets the average matches played by each player, over the
      * whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average matches played per player.
      */
     public double aveMatchesPerPlayer(ArrayList<Player> players) {
         int matchesPlayed = 0;
         for (int i = 0; i < players.size(); i++) {
             matchesPlayed += players.get(i).getMatchesPlayed();
         }
 
         double aveMatchesPerPlayer = (matchesPlayed * 1.0) / players.size();
         return toDoubleTwoDP(aveMatchesPerPlayer);
     }
     
     /**
      * This method gets the average matches played by each player, 
      * in a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the stats from.
      * @return The average matches played per player.
      */
     public double aveMatchesPerPlayer(ArrayList<Player> players, String country) {
         int matchesPlayed = 0;
         int playersFromCountry = 0;
         for (int i = 0; i < players.size(); i++) {
             if (players.get(i).getCountryName().compareTo(country) == 0) {
                 playersFromCountry++;
                 matchesPlayed += players.get(i).getMatchesPlayed();
             }
         }
 
         double aveMatchesPerPlayer = (matchesPlayed * 1.0) / playersFromCountry;
         return toDoubleTwoDP(aveMatchesPerPlayer);
     }
     
     /**
      * This method gets the average runs conceded by each player, over the
      * whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average matches played per player.
      */
     public double aveRunsConceded(ArrayList<Player> players) {
         int runsConceded = 0;
         for (int i = 0; i < players.size(); i++) {
             runsConceded += players.get(i).getRunsConceded();
         }
 
         double aveRunsConceded = (runsConceded * 1.0) / players.size();
         return toDoubleTwoDP(aveRunsConceded);
     }
     
     /**
      * This method gets the average runs conceded by each player, for
      * a given country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the stats for.
      * @return The average matches played per player, from h=the country parsed through.
      */
     public double aveRunsConceded(ArrayList<Player> players, String country) {
         int runsConceded = 0;
         int playersFromCountry = 0;
         for (int i = 0; i < players.size(); i++) {
             if (players.get(i).getCountryName().compareTo(country) == 0) {
                 playersFromCountry++;
                 runsConceded += players.get(i).getRunsConceded();
             }
         }
 
         double aveRunsConceded = (runsConceded * 1.0) / playersFromCountry;
         return toDoubleTwoDP(aveRunsConceded);
     }
     
     /**
      * This method gets the average of each player's bowling average, over the
      * whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average matches played per player.
      */
     public double aveBowlingAverage(ArrayList<Player> players) {
         double bowlingAverage = 0;
         for (int i = 0; i < players.size(); i++) {
             bowlingAverage += players.get(i).getBowlingAverage();
         }
 
         double aveBowlingAverage = bowlingAverage / players.size();
         return toDoubleTwoDP(aveBowlingAverage);
     }
     
     /**
      * This method gets the average of each player's bowling average, over the
      * whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the statistics from.
      * @return The average matches played per player.
      */
     public double aveBowlingAverage(ArrayList<Player> players, String country) {
         double bowlingAverage = 0.0;
         int playersFromCountry = 0;
         for (int i = 0; i < players.size(); i++) {
             if (players.get(i).getCountryName().compareTo(country) == 0) {
                 playersFromCountry++;
                 bowlingAverage += players.get(i).getBowlingAverage();
             }
             else
             {
                 playersFromCountry+=0;
                 bowlingAverage+=0;
             }
         }
 
         double aveBowlingAverage = bowlingAverage / playersFromCountry;
         return toDoubleTwoDP(aveBowlingAverage);
     }
     
     /**
      * This method gets the average number of 5 wicket hauls 
      * per ODI per player, over the whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average number of 5 wickets hauls per player.
      */
     public double aveFiveWicketsInns(ArrayList<Player> players) {
         int fiveWickets = 0;
         for (int i = 0; i < players.size(); i++) {
             fiveWickets += players.get(i).getFiveWicketsInnings();
         }
 
         double aveFiveWickets = (fiveWickets * 1.0) / players.size();
         return toDoubleTwoDP(aveFiveWickets);
     }
     
     /**
      * This method gets the average number of 5 wicket hauls 
      * per ODI per player, for a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the statistics from.
      * @return The average number of 5 wickets hauls per player.
      */
     public double aveFiveWicketsInns(ArrayList<Player> players, String country) {
         int fiveWickets = 0;
         int playersFromCountry = 0;
         for (int i = 0; i < players.size(); i++) {
             if (players.get(i).getCountryName().compareTo(country) == 0) {
                 playersFromCountry++;
                 fiveWickets += players.get(i).getFiveWicketsInnings();
             }
         }
 
         double aveFiveWickets = (fiveWickets * 1.0) / playersFromCountry;
         return toDoubleTwoDP(aveFiveWickets);
     }
 
     /**
      * This method gets the average number of wickets taken by each player, over
      * the whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average amount of wickets taken per player.
      */
     public double aveWickets(ArrayList<Player> players) {
         int totalWicketsTaken = 0;
         for (int i = 0; i < players.size(); i++) {
             totalWicketsTaken += players.get(i).getWicketsTaken();
         }
 
         double aveWicketsTaken = (totalWicketsTaken * 1.0) / players.size();
         return toDoubleTwoDP(aveWicketsTaken);
     }
 
     /**
      * This overloaded method calculates the average amount wickets taken per
      * player, in a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the average wickets taken per player.
      * @return The average wickets taken per player, from the defined country.
      */
     public double aveWickets(ArrayList<Player> players, String country) {
         double totalWicketsTaken = 0.0;
         int playersFromCountry = 0;
 
         for (Player p : players) {
             countries.add(p.getCountryName());
         }
 
         Set<String> countryNames = new HashSet<String>(countries);
         /*End code fragment */
 
         if (countryNames.contains(country)) {
 
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getCountryName().compareTo(country) == 0) {
                     totalWicketsTaken += players.get(i).getWicketsTaken();
                     playersFromCountry++;
                 }
             }
 
             double aveWicketsTaken = (totalWicketsTaken * 1.0) / playersFromCountry;
             return toDoubleTwoDP(aveWicketsTaken);
         } else {
             return 0;
         }
     }
 
     /**
      * This method gets the average bowling economy rate for each player, over
      * the whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average bowling economy rate per player.
      */
     public double aveEconRate(ArrayList<Player> players) {
         double totalEconomyRate = 0;
         for (int i = 0; i < players.size(); i++) {
             totalEconomyRate += players.get(i).getEconomyRate();
         }
 
         return toDoubleTwoDP(totalEconomyRate / players.size());
     }
 
     /**
      * This overloaded method calculates the average bowling economy rate per
      * player, in a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the average economy rate per player.
      * @return The average economy rate per player, from the defined country.
      */
     public double aveEconRate(ArrayList<Player> players, String country) {
         double totalEconRate = 0.0;
         int playersFromCountry = 0;
 
         for (Player p : players) {
             countries.add(p.getCountryName());
         }
 
         Set<String> countryNames = new HashSet<String>(countries);
         /*End code fragment */
 
         if (countryNames.contains(country)) {
 
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getCountryName().compareTo(country) == 0) {
                     totalEconRate += players.get(i).getEconomyRate();
                     playersFromCountry++;
                 }
             }
 
             double aveEconRate = (totalEconRate * 1.0) / playersFromCountry;
             return toDoubleTwoDP(aveEconRate);
         } else {
             return 0;
         }
     }
 
     /**
      * This method gets the average number of balls bowled for each player, over
      * the whole data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average number of balls bowled per player.
      */
     public double aveBallsBowled(ArrayList<Player> players) {
         double totalBallsBowled = 0;
         for (int i = 0; i < players.size(); i++) {
             totalBallsBowled += players.get(i).getBallsBowled();
         }
 
         return toDoubleTwoDP(totalBallsBowled / players.size());
     }
 
     /**
      * This overloaded method calculates the average amount of balls bowled per
      * player, in a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the average number of balls bowled per
      * player.
      * @return The average number of balls bowled per player, from the defined
      * country.
      */
     public double aveBallsBowled(ArrayList<Player> players, String country) {
         double totalBallsBowled = 0.0;
         int noOfPlayers = 0;
 
         for (Player p : players) {
             countries.add(p.getCountryName());
         }
 
         Set<String> countryNames = new HashSet<String>(countries);
         /*End code fragment */
 
         if (countryNames.contains(country)) {
 
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getCountryName().compareTo(country) == 0) {
                     totalBallsBowled += players.get(i).getBallsBowled();
                     noOfPlayers++;
                 }
             }
 
             double aveBallsBowled = (totalBallsBowled * 1.0) / noOfPlayers;
             return toDoubleTwoDP(aveBallsBowled);
         } else {
             return 0;
         }
     }
 
     /**
      * This method gets the average strike rate for each player, over the whole
      * data set.
      *
      * @param players The list of players to perform the calculation.
      * @return The average bowling strike rate per player.
      */
     public double aveStrikeRate(ArrayList<Player> players) {
         double totalStrikeRate = 0;
         for (int i = 0; i < players.size(); i++) {
             totalStrikeRate += players.get(i).getStrikeRate();
         }
 
         return toDoubleTwoDP(totalStrikeRate / players.size());
     }
 
     /**
      * This overloaded method gets the average strike rate for each player, from
      * a given country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country to get the average bowling strike rate per
      * player.
      * @return The average strike rate rate per player, from a particular
      * country.
      */
     public double aveStrikeRate(ArrayList<Player> players, String country) {
         double totalStrikeRate = 0.0;
         int playersFromCountry = 0;
 
         for (Player p : players) {
             countries.add(p.getCountryName());
         }
 
         Set<String> countryNames = new HashSet<String>(countries);
         /*End code fragment */
 
         if (countryNames.contains(country)) {
 
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getCountryName().compareTo(country) == 0) {
                     totalStrikeRate += players.get(i).getStrikeRate();
                     playersFromCountry++;
                 }
             }
 
             double aveStrikeRate = (totalStrikeRate * 1.0) / playersFromCountry;
             return toDoubleTwoDP(aveStrikeRate);
         } else {
             return 0;
         }
     }
     
     /**
      * This method returns the average career length for 
      * the specified players parsed through.
      * @param players The players to get the stats from.
      * @return The average career length of the players parsed through.
      */
     public double aveCareerLength(ArrayList<Player> players)
     {
         int totalCareerLength = 0;
         for(int i = 0; i < players.size(); i++)
         {
             totalCareerLength += players.get(i).calcCareerSpan();
         }
         
         return toDoubleTwoDP((totalCareerLength * 1.0)/players.size());
     }
     
     /**
      * This method returns the average career length for 
      * the specified players parsed through, given their country.
      * @param players The players to get the stats from.
      * @param country 
      * @return The average career length of the players parsed through.
      */
     public double aveCareerLength(ArrayList<Player> players, String country)
     {
         int totalCareerLength = 0;
         int playersFromCountry = 0;
         for(int i = 0; i < players.size(); i++)
         {
             if (players.get(i).getCountryName().compareTo(country) == 0) {
                 playersFromCountry++;
                 totalCareerLength += players.get(i).calcCareerSpan();
             }
         }
         
         return toDoubleTwoDP((totalCareerLength * 1.0)/playersFromCountry);
     }
 
     /**
      * This method returns a list of players from a particular country.
      *
      * @param players The list of players to perform the calculation.
      * @param country The country where the players would be from.
      * @return The player's name and country they play for.
      */
     public ArrayList<Player> listPlayersCountry(ArrayList<Player> players, String country) {
 
         for (Player p : players) {
             countries.add(p.getCountryName());
         }
 
 
         //this is used to get a unique list of countries (i.e no repeating values)
         Set<String> countryNames = new HashSet<String>(countries);
         
         ArrayList<Player> playersCountry = new ArrayList<Player>();
         
         if (countryNames.contains(country)) {
 
             for (int i = 0; i < players.size(); i++) {
                 if (players.get(i).getCountryName().compareTo(country) == 0) {
                     playersCountry.add(new Player(
                             players.get(i).getPlayerID(),
                             players.get(i).getPlayerName(),
                             players.get(i).getCountryName(),
                             players.get(i).getCareerSpan(),
                             players.get(i).getMatchesPlayed(),
                             players.get(i).getInningsPlayed(),
                             players.get(i).getBallsBowled(),
                             players.get(i).getRunsConceded(),
                             players.get(i).getWicketsTaken(),
                             players.get(i).getBowlingAverage(),
                             players.get(i).getEconomyRate(),
                             players.get(i).getStrikeRate(),
                             players.get(i).getFiveWicketsInnings()
                     ));
                     
                 }
             }
         } 
          return playersCountry;
     }
 
     /**
      * This method is responsible for sorting the entire list of players.
      *
      *
      * @param players The list of players to be sorted
      * @param chosenOption An integer value to choose a sorting option. 
      * || 0 = "Sort by balls bowled" 
      * || 1 = "Sort by bowling average" 
      * || 2 = "Sort by career span" 
      * || 3 = "Sort by country name" 
      * || 4 = "Sort by economy rate"
      * || 5 = "Sort by number of 5 wicket bags" 
      * || 6 = "Sort by matches played"
      * || 7 = "Sort by innings played" 
      * || 8 = "Sort by player name" 
      * || 9 = "Sort by runs conceded" 
      * || 10 = "Sort by strike rate" 
      * || 11 = "Sort by wickets taken" ||
      * @return The sorted list of players, according to the option chosen.
      */
     public String outputSort(ArrayList<Player> players, int chosenOption) {
         StringBuilder sb = new StringBuilder();
         sb.append(csvHeader + "\n");
         switch (chosenOption) {
             case 0:
                 Collections.sort(players, new SortByBallsBowled());
                 break;
             case 1:
                 Collections.sort(players, new SortByBowlingAverage());
                 break;
             case 2:
                 Collections.sort(players, new SortByCareerSpan());
                 break;
             case 3:
                 Collections.sort(players, new SortByCountryName());
                 break;
             case 4:
                 Collections.sort(players, new SortByEconomyRate());
                 break;
             case 5:
                 Collections.sort(players, new SortByFiveWicketsInn());
                 break;
             case 6:
                 Collections.sort(players, new SortByMatchesPlayed());
                 break;
             case 7:
                 Collections.sort(players, new SortByInningsPlayed());
                 break;
             case 8:
                 Collections.sort(players, new SortByPlayerName());
                 break;
             case 9:
                 Collections.sort(players, new SortByRunsConceded());
                 break;
             case 10:
                 Collections.sort(players, new SortByStrikeRate());
                 break;
             case 11:
                 Collections.sort(players, new SortByWicketsTaken());
                 break;
         }
 
         for (Player p : players) {
             sb.append(p.toString() + "\n");
         }
         return sb.toString();
     }
 }
