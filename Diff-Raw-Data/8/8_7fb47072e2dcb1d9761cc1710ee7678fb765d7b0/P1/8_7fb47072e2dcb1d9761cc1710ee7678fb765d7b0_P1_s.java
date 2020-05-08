 package com.company;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class P1 {
 
     //This object represents the Team tuple in the Database. This class contains the properties and methods
     //associated with Teams.
     public class Team {
         //Capital Letters Or Digits
         public String team_ID;
         //One or two Words
         public String location;
         //A string, no validation
         public String name;
         //An upper case letter
         public char league;
 
         //Method to Make sure that the team we pulled was valid. This will be called before we save to the DB.
         public String IsTeamValid() {
             boolean errorHasOccurred = false;
             String ErrorString = "The Following Errors Occurred: ";
 
             //If the ascii value of the character is not a capital letter, league isn't valid.
             if (this.league < 65 || this.league > 90) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n League is not a capital letter. ");
             }
 
             //If the TeamId is not capital letters or numbers, it isn't valid
             for (int i = 0; i < this.team_ID.length(); i++) {
                 //If not between A-Z or between 0-9, its not valid
                 if (!((this.team_ID.charAt(i) >= 48 && this.team_ID.charAt(i) <= 57) || (this.team_ID.charAt(i) >= 65 && this.team_ID.charAt(i) <= 90))) {
                     errorHasOccurred = true;
                     ErrorString = ErrorString.concat("\n Team_ID must consist of Capital letters or digits.");
                     break;
                 }
             }
 
             //Validating location. Counting spaces.
             if (this.location.indexOf(' ') != this.location.lastIndexOf(' ')) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Location may contain only one space.");
             }
 
             //If error has occurred, return the string. Else, return an empty string.
             return errorHasOccurred ? ErrorString : "";
         }//End IsValid Method
 
         //This method will parse the string for commas, and then Create a team Object based upon the comma separated values
         public Team GetTeamFromFileString(String teamString) {
             try {
                 //This will be the array of strings we populate to our new object.
                 String[] StringArray = teamString.split(",");
                 return CreateTeamFromStringArray(StringArray);
 
 
             } catch (Exception e) {
                 return null;
             }
         }
 
         //Method to create a Team object from a provided string array
         public Team CreateTeamFromStringArray(String[] stringArray) throws Exception {
             //Sanity check. I need four values, no more no less.
             if (stringArray.length != 4)
                 throw new Exception("The Expected Number of values was 4 but found " + stringArray.length);
 
             Team returnTeam = new Team();
 
             //Populate the Team Object
             returnTeam.team_ID = stringArray[0];
             returnTeam.location = stringArray[1];
             returnTeam.name = stringArray[2];
             returnTeam.league = stringArray[3].charAt(0);
             return returnTeam;
         }
 
         //Get a list of teams from the DB. If city is supplied, then only get those matching the city.
         public ArrayList<Team> GetTeams(String city) {
             ArrayList<Team> returnList = new ArrayList<Team>();
             try {
 
                 if (city.equals("")) {
                     returnList = teamDatabase;
                 } else {
                     for (Team team : teamDatabase) {
                         if (team.location.equals(city))
                             returnList.add(team);
                     }
                 }
 
                 return returnList;
 
             } catch (Exception e) {
                 return null;
             }
         }
 
     }//End Team Class
 
     //This object represents the Coach tuple in the Database. This class contains properties and methods
     //associated with Coaches
     public class Coach {
         //String: less than 7 cap letters and 2 digits
         public String coach_ID;
         //int: 4 digits
         public int season;
         //String: no space
         public String first_name;
         //String: may have one space
         public String last_name;
         //int: greater than 0
         public int season_win;
         //int: greater than 0
         public int season_loss;
         //int: greater than 0
         public int playoff_win;
         //int: greater than 0
         public int playoff_loss;
         //String: Capital Letters or digits.
         public String team;
 
         public String IsCoachValid() {
 
             //Validation Variables
             boolean errorHasOccurred = false;
             String ErrorString = "The Following Errors Occurred: ";
             //This is the number of digits in the string. must be 2
             int numericCount = 0;
             //This is the number of letters in the string. Can be 0-6
             int alphaCount = 0;
             //This tells the loop to break immediately on error
             boolean needToBreak = false;
             //End Validation Variables
 
             //Validating Coach Id
             for (int i = 0; i < this.coach_ID.length(); i++) {
                 char characterToValidate = this.coach_ID.charAt(i);
 
                 //This is a number
                 if ((characterToValidate >= 48 && characterToValidate <= 57)) {
                     if (numericCount >= 2) {
                         errorHasOccurred = true;
                         needToBreak = true;
                         ErrorString = ErrorString.concat("\n Coach Id must have 2 Digits. ");
                     }
                     numericCount++;
                 }
                 //Capital Letter
                 else if (characterToValidate >= 65 && characterToValidate <= 90) {
                     if (alphaCount >= 7) {
                         errorHasOccurred = true;
                         needToBreak = true;
                         ErrorString = ErrorString.concat("\n Coach Id must have less than 7 Capital Letters. ");
                     }
                     alphaCount++;
                 }
                 //Not a valid character
                 else {
                     errorHasOccurred = true;
                     needToBreak = true;
                     ErrorString = ErrorString.concat("\n \"" + characterToValidate + "\" is not a valid character. ");
                 }
 
                 //Break out of the loop since there's no need to continue
                 if (needToBreak)
                     break;
             }
 
             //If we don't have two digits, throw error.
             if (numericCount < 2) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Coach Id must have 2 Digits. ");
             }
 
             //Validating season
             if (season < 1000 || season > 9999) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Season must be a four digit positive integer. ");
             }
 
             //Validating FirstName
             for (int i = 1; i < first_name.length(); i++) {
                 char charToValidate = first_name.charAt(i);
 
                 if (!((charToValidate >= 65 && charToValidate <= 90) || (charToValidate >= 97 && charToValidate <= 122))) {
                     errorHasOccurred = true;
                     ErrorString = ErrorString.concat("\n \"" + charToValidate + "\" is not a valid character. ");
                     break;
                 }
             }
 
             //Validating LastName. Just counting number of spaces
             if (this.last_name.indexOf(' ') != this.last_name.lastIndexOf(' ')) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Last Name may contain only one space.");
             }
 
             //Validating Season Wins
             if (this.season_win < 0) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Season Wins must be a positive integer.");
             }
 
             //Validating Season Losses
             if (this.season_loss < 0) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Season Losses must be a positive integer.");
             }
 
             //Validating Playoff Wins
             if (this.playoff_win < 0) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Playoff Wins must be a positive integer.");
             }
 
             //Validating Playoff Losses
             if (this.playoff_loss < 0) {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Playoff Losses must be a positive integer.");
             }
 
             //Validating TeamId
             //If the TeamId is not capital letters or numbers, it isn't valid
             for (int i = 0; i < this.team.length(); i++) {
                 //If not between A-Z or between 0-9, its not valid
                 if (!((this.team.charAt(i) >= 48 && this.team.charAt(i) <= 57) || (this.team.charAt(i) >= 65 && this.team.charAt(i) <= 90))) {
                     errorHasOccurred = true;
                     ErrorString = ErrorString.concat("\n Team must consist of Capital letters or digits.");
                     break;
                 }
             }
             //Ok, here we need to make sure that there is a team that exists with the id given
             //Note: May not need this, commenting out for right now
             /*boolean teamExists = false;
             for(Team team : teamDatabase)
             {
                 if(team.team_ID.equals(this.team))
                 {
                     teamExists = true;
                     break;
                 }
             }
             if(!teamExists)
             {
                 errorHasOccurred = true;
                 ErrorString = ErrorString.concat("\n Team with Id \"" + this.team +"\" not found in Database.");
             } */
 
             return errorHasOccurred ? ErrorString : "";
         }
 
         //Method to create a coach object from a provided string array
         public Coach CreateCoachFromStringArray(String[] stringArray) throws Exception {
             //Sanity check. I need ten values, no more no less.
             if (stringArray.length != 9)
                 throw new Exception("The Expected Number of values was 9 but found " + stringArray.length);
 
             Coach returnCoach = new Coach();
 
             //Populate the Coach Object
             returnCoach.coach_ID = stringArray[0];
             returnCoach.season = Integer.parseInt(stringArray[1]);
             returnCoach.first_name = stringArray[2];
             returnCoach.last_name = stringArray[3];
             returnCoach.season_win = Integer.parseInt((stringArray[4]));
             returnCoach.season_loss = Integer.parseInt((stringArray[5]));
             returnCoach.playoff_win = Integer.parseInt((stringArray[6]));
             returnCoach.playoff_loss = Integer.parseInt((stringArray[7]));
             returnCoach.team = stringArray[8];
 
             return returnCoach;
         }
 
         //This method will parse the string for commas, and then Create a Coach Object based upon the comma separated values
         public Coach GetCoachFromFileString(String coachString) {
             try {
                 //This will be the array of strings we populate to our new object.
                 String[] StringArray = coachString.split(",");
                 //Need to modify that array to remove the yr_order field
                 String[] modifiedArray = new String[]{StringArray[0], StringArray[1], StringArray[3], StringArray[4], StringArray[5], StringArray[6], StringArray[7], StringArray[8], StringArray[9]};
                 return CreateCoachFromStringArray(modifiedArray);
 
             } catch (Exception e) {
                 return null;
             }
         }
 
         //Get a list of coaches from the DB. If last name is supplied, then only get those matching the last name.
         public ArrayList<Coach> GetCoaches(String lastName) {
             ArrayList<Coach> returnList = new ArrayList<Coach>();
             try {
                 if (lastName.equals("")) {
                     returnList = coachDatabase;
                 } else {
                     for (Coach coach : coachDatabase) {
                         if (coach.last_name.equals(lastName))
                             returnList.add(coach);
                     }
                 }
 
                 return returnList;
             } catch (Exception e) {
                 return null;
             }
         }
     }
 
     //These are the in-memory databases to store the records
     public ArrayList<Team> teamDatabase;
     public ArrayList<Coach> coachDatabase;
 
     public P1() {
         teamDatabase = new ArrayList<Team>();
         coachDatabase = new ArrayList<Coach>();
     }
 
     @SuppressWarnings("ConstantConditions")
     public void run() {
         CommandParser parser = new CommandParser();
         System.out.println("The mini-DB of NBA coaches and teams");
         System.out.println("Please enter a command.  Enter 'help' for a list of commands.");
         System.out.println();
         System.out.print("> ");
 
         Command cmd;
         while ((cmd = parser.fetchCommand()) != null) {
 
             boolean result = false;
             String error = "";
             if (cmd.getCommand().equals("help")) {
                 result = doHelp();
 
 		/* You need to implement all the following commands */
             } else if (cmd.getCommand().equals("add_coach")) {
                 try {
                     String[] parameters = cmd.getParameters();
 
                     //Need four parameters to Add a Coach
                     if (parameters.length != 9) {
                         result = false;
                         error = "Invalid Number of parameters. Expected 9.";
                     } else {
                         //Before we pass the parameters to the creation function, replace any instances of + with a space
                         parameters[3] = parameters[3].replace("+", " ");
 
                         Coach temp = new Coach();
                         Coach coachToAdd = temp.CreateCoachFromStringArray(parameters);
 
                         String errorMessage = coachToAdd.IsCoachValid();
 
                         //Make sure the team is valid. If not, throw an error.
                         if (!errorMessage.equals("")) {
                             result = false;
                             error = errorMessage;
                         } else {
                             //Write to the file
                             AddCoach(coachToAdd);
                             System.out.println("Coach Added Successfully.");
                         }
                     }
                 } catch (Exception e) {
                     result = false;
                     error = e.getMessage();
                 }
             } else if (cmd.getCommand().equals("add_team")) {
                 try {
                     String[] parameters = cmd.getParameters();
 
                     //Need four parameters to Add a Team
                     if (parameters.length != 4) {
                         result = false;
                         error = "Invalid Number of parameters. Expected 4.";
                     } else {
                         //Before we pass the parameters to the creation function, replace any instances of + with a space
                         parameters[1] = parameters[1].replace("+", " ");
 
                         Team temp = new Team();
                         Team teamToAdd = temp.CreateTeamFromStringArray(parameters);
 
                         String errorMessage = teamToAdd.IsTeamValid();
 
                         //Make sure the team is valid. If not, throw an error.
                         if (!errorMessage.equals("")) {
                             result = false;
                             error = errorMessage;
                         } else {
                             //Write to the file
                             AddTeam(teamToAdd);
                             System.out.println("Team Added Successfully.");
                         }
                     }
                 } catch (Exception e) {
                     result = false;
                     error = e.getMessage();
                 }
             } else if (cmd.getCommand().equals("print_coaches")) {
                 try {
                     //Print the list of Coaches
                     PrintCoaches();
                 } catch (Exception ex) {
                     result = false;
                     error = ex.getMessage();
                 }
             } else if (cmd.getCommand().equals("print_teams")) {
                 try {
                     //Print the list of Teams
                     PrintTeams();
                 } catch (Exception ex) {
                     result = false;
                     error = ex.getMessage();
                 }
             } else if (cmd.getCommand().equals("coaches_by_name")) {
                 try {
                     String[] parameters = cmd.getParameters();
                     if (parameters.length != 1) {
                         result = false;
                         error = "Invalid Number of parameters. Expected 1.";
                     } else {
                         CoachesByLastName(parameters[0]);
                     }
                 } catch (Exception ex) {
                     result = false;
                     error = ex.getMessage();
                 }
             } else if (cmd.getCommand().equals("teams_by_city")) {
                 try {
                     String[] parameters = cmd.getParameters();
                     if (parameters.length != 1) {
                         result = false;
                         error = "Invalid Number of parameters. Expected 1.";
                     } else {
                         TeamsByCity(parameters[0]);
                     }
                 } catch (Exception ex) {
                     result = false;
                     error = ex.getMessage();
                 }
             } else if (cmd.getCommand().equals("load_coaches")) {
                 try {
                     ArrayList<Coach> AddList = new ArrayList<Coach>();
                     if (cmd.getParameters().length != 1) {
                         result = false;
                         error = "Expected 1 parameter, found" + cmd.getParameters().length;
                     } else {
                         BufferedReader br = new BufferedReader(new FileReader(cmd.getParameters()[0]));
                         //This is the first line in the table. It names the columns.
                         //noinspection UnusedAssignment
                         String line = br.readLine();
                         line = br.readLine();
                         Coach temp = new Coach();
                         while (line != null) {
                             AddList.add(temp.GetCoachFromFileString(line));
                             line = br.readLine();
                         }
                         br.close();
                     }
 
                     //Now that we have a list of coaches to add, need to validate them. If they are good, add them. If not, add to the error list and continue.
                     int i = 1;
                     for (Coach coach : AddList) {
                         String errors = coach.IsCoachValid();
                         if (errors.equals("")) {
                             AddCoach(coach);
                         } else {
                             error = error + "\nError Adding Coach " + i + ". " + errors;
                             result = false;
                         }
                         i++;
                     }
                     System.out.println("\nFinished Loading Coaches.");
                 } catch (Exception e) {
                     error = e.getMessage();
                     result = false;
                 }
             } else if (cmd.getCommand().equals("load_teams")) {
                 try {
                     ArrayList<Team> AddList = new ArrayList<Team>();
                     if (cmd.getParameters().length != 1) {
                         result = false;
                         error = "Expected 1 parameter, found" + cmd.getParameters().length;
                     } else {
                         BufferedReader br = new BufferedReader(new FileReader(cmd.getParameters()[0]));
                         //This is the first line in the table. It names the columns.
                         //noinspection UnusedAssignment
                         String line = br.readLine();
                         line = br.readLine();
                         Team temp = new Team();
                         while (line != null) {
                             AddList.add(temp.GetTeamFromFileString(line));
                             line = br.readLine();
                         }
                         br.close();
                     }
 
                     //Now that we have a list of teams to add, need to validate them. If they are good, add them. If not, add to the error list and continue.
                     int i = 1;
                     for (Team team : AddList) {
                         String errors = team.IsTeamValid();
                         if (errors.equals("")) {
                             AddTeam(team);
                         } else {
                             error = error + "\nError Adding Team " + i + ". " + errors;
                             result = false;
                         }
                         i++;
                     }
                    System.out.println("\nFinished Loading Team.");
                 } catch (Exception e) {
                     error = e.getMessage();
                     result = false;
                 }
             } else if (cmd.getCommand().equals("best_coach")) {
                 try {
                     if (cmd.getParameters().length != 1) {
                         result = false;
                         error = "Expected 1 parameter, found" + cmd.getParameters().length;
                     } else {
                         int mostWins = 0;
                         Coach winningCoach = new Coach();
 
                         for (Coach coach : coachDatabase) {
                             int coachWins = (coach.season_win - coach.season_loss) + (coach.playoff_win - coach.playoff_loss);
                             //If the season matches the one given
                             if (coach.season == Integer.parseInt(cmd.getParameters()[0]) && coachWins > mostWins) {
                                 mostWins = coachWins;
                                 winningCoach = coach;
                             }
                         }
 
                         System.out.println("Best Coach was " + winningCoach.first_name + " " + winningCoach.last_name);
                     }
                 } catch (Exception ex) {
                     result = false;
                     error = ex.getMessage();
                 }
             } else if (cmd.getCommand().equals("exit")) {
                 System.out.println("Leaving the database, goodbye!");
                 break;
             } else if (cmd.getCommand().equals("")) {
                 result = true;
             } else {
                 System.out.println("Invalid Command, try again!");
             }
 
             if (!result) System.out.println(error);
             System.out.print("> ");
         }
     }
 
     //Method to append a team to the teams.txt file
     private void AddTeam(Team teamToAdd) throws IOException {
         teamDatabase.add(teamToAdd);
     }
 
     //Method to append a coach to the coaches_season.txt file
     @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
     private void AddCoach(Coach coachToAdd) throws IOException {
         coachDatabase.add(coachToAdd);
     }
 
     //Method to print all Teams
     private void PrintTeams() {
         Team temp = new Team();
         ArrayList<Team> teams = temp.GetTeams("");
 
         //Print the top line
         System.out.println("Printing List of Teams: ");
 
         if (teams.size() >= 1) {
             PrintListOfTeams(teams);
         }
     }
 
     private void PrintListOfTeams(ArrayList<Team> teams) {
         //Print each Team
         for (Team team : teams) {
             System.out.println("\t" + team.team_ID + ", "
                     + team.location + ", "
                     + team.name + ", "
                     + team.league);
         }
     }
 
     //Method to print all Coaches
     private void PrintCoaches() {
         Coach temp = new Coach();
         ArrayList<Coach> coaches = temp.GetCoaches("");
 
         //Print the top line
         System.out.println("Printing List of Coaches: ");
 
         if (coaches.size() >= 1) {
             PrintListOfCoaches(coaches);
 
         }
     }
 
     //Common method to print out a list of coaches.
     private void PrintListOfCoaches(ArrayList<Coach> coaches) {
         //Print each Coach
         for (Coach coach : coaches) {
             System.out.println
                     ("\t" + coach.coach_ID + ", "
                             + coach.season + ", "
                             + coach.first_name + ", "
                             + coach.last_name + ", "
                             + coach.season_win + ", "
                             + coach.season_loss + ", "
                             + coach.playoff_win + ", "
                             + coach.playoff_loss + ", "
                             + coach.team
                     );
         }
     }
 
     //Method which Prints out the teams by a given city parameter
     private void TeamsByCity(String parameter) {
         Team temp = new Team();
         ArrayList<Team> teams = temp.GetTeams(parameter.replace('+', ' '));
 
         //Print the Top Line
         System.out.println("Printing List of Teams matching City \"" + parameter.replace('+', ' ') + "\":");
 
         if (teams.size() >= 1) {
             //Print each Team
             PrintListOfTeams(teams);
         }
     }
 
     //Method which Prints out the coaches by a given lastName parameter
     private void CoachesByLastName(String parameter) {
         Coach temp = new Coach();
         ArrayList<Coach> coaches = temp.GetCoaches(parameter.replace('+', ' '));
 
         //Print the Top Line
         System.out.println("Printing List of Coaches matching Last Name \"" + parameter.replace('+', ' ') + "\":");
 
         if (coaches.size() >= 1) {
             //Print each Team
             PrintListOfCoaches(coaches);
         }
     }
 
     //Method that displays help text
     @SuppressWarnings("SameReturnValue")
     private boolean doHelp() {
         System.out.println("add_coach ID SEASON FIRST_NAME LAST_NAME SEASON_WIN ");
         System.out.println("          SEASON_LOSS PLAYOFF_WIN PLAYOFF_LOSS TEAM - add new coach data");
         System.out.println("add_team ID LOCATION NAME LEAGUE - add a new team");
         System.out.println("print_coaches - print a listing of all coaches");
         System.out.println("print_teams - print a listing of all teams");
         System.out.println("coaches_by_name NAME - list info of coaches with the specified name");
         System.out.println("teams_by_city CITY - list the teams in the specified city");
         System.out.println("load_coach FILENAME - bulk load of coach info from a file");
         System.out.println("load_team FILENAME - bulk load of team info from a file");
         System.out.println("best_coach SEASON - print the name of the coach with the most net wins in a specified season");
         System.out.println("search_coaches field=VALUE - print the name of the coach satisfying the specified conditions");
         System.out.println("delete_coaches field=VALUE - delete the coach satisfying the specified conditions");
         System.out.println("exit - quit the program");
         return true;
     }
 
     /**
      * @param args
      */
 }
