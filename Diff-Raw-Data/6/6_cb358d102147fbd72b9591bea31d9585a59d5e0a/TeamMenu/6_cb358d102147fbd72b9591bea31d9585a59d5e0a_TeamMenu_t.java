 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package UI;
 
 import BE.Match;
 import BE.Team;
 import BLL.MatchManager;
 import BLL.TeamManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author Rasmus, Chris, Lasse, Dennis
  */
 class TeamUIMenu extends Menu
 {
 
     private static final int EXIT_VALUE = 0;
     private TeamManager teammgr;
     private MatchManager matchmgr;
 
     /**
      * Lists the different Menu options in Team UI Menu
      *
      * @param Team UI menu
      */
     public TeamUIMenu()
     {
         super("Team UI Menu", "List All Teams", "Add A Team", "Update A Team", "Delete A Team", "Assign Teams To A Group");
         try
         {
             teammgr = new TeamManager();
             matchmgr = new MatchManager();
 
         }
         catch (Exception e)
         {
             System.out.println("ERROR - " + e.getMessage());
         }
         EXIT_OPTION = EXIT_VALUE;
     }
 
     /**
      * Enter A Number, to move to submenu on...
      *
      * @param option
      */
     @Override
     protected void doAction(int option)
     {
         switch (option)
         {
             case 1:
                 ListAllTeams();
                 break;
             case 2:
                 AddTeam();
                 break;
             case 3:
                 UpdateTeam();
                 break;
             case 4:
                 DeleteTeam();
                 break;
             case 5:
                 AssignToGroups();
                 break;
             case EXIT_VALUE:
                 doActionExit();
         }
     }
 
     /**
      *
      */
     private void ListAllTeams()
     {
         clear();
         System.out.println("SHOW ALL TEAMS");
         System.out.println();
 
         try
         {
             printShowHeader();
             ArrayList<Team> teams = teammgr.listAll();
 
             for (Team t : teams)
             {
                 System.out.println(t);
             }
         }
         catch (Exception e)
         {
             System.out.println(" ERROR - " + e.getMessage());
         }
         pause();
     }
 
     /**
      *
      */
     private void AddTeam()
     {
         clear();
         System.out.println("ADD NEW TEAM");
         System.out.println();
 
         try
         {
             int counter = teammgr.showCount();
             Team tm = teammgr.getByID(1);
            
            if (tm == null || tm.getGroupId() == 0)
             {
                 if (counter <= 16)
                 {
                     Scanner sc = new Scanner(System.in, "iso-8859-1");
 
                     System.out.println("School: ");
                     String school = sc.nextLine();
 
                     System.out.println("Team Captain: ");
                     String teamcaptain = sc.nextLine();
 
                     System.out.println("Email: ");
                     String email = sc.nextLine();
 
                     Team team = new Team(-1, school, teamcaptain, email, -1, -1);
 
                     teammgr.add(team);
 
                     System.out.println();
                     System.out.println("Team succesfully added");
                 }
                 else
                 {
                     System.out.println("Teams are limited to a total of 16 teams! Limit Reached!");
                 }
             }
             else
             {
                 System.out.println("Groups have been assigned, therefore it's not possible to add more teams!");
             }
         }
         catch (Exception e)
         {
             System.out.println("ERROR - " + e.getMessage());
            e.printStackTrace();
         }
         pause();
     }
 
     /**
      *
      */
     private void UpdateTeam()
     {
         clear();
         System.out.println("UPDATE TEAM INFORMATION:");
         System.out.println();
 
         try
         {
             TeamManager tManager = new TeamManager();
             ArrayList<Team> teams = tManager.listAll();
 
             printShowHeader();
             for (Team t : teams)
             {
                 System.out.println(t);
             }
 
             System.out.println("Select Team id: ");
             int id = new Scanner(System.in).nextInt();
             Team team = teammgr.getByID(id);
 
             new UpdateTeamMenu(team).run();
         }
         catch (Exception e)
         {
             System.out.println("ERROR - " + e.getMessage());
         }
         pause();
     }
 
     /**
      *
      */
     private void DeleteTeam()
     {
         clear();
         System.out.println("DELETE TEAM:");
         System.out.println("");
         try
         {
             int matchCount = matchmgr.showCount();
             int teamCount = teammgr.showCount();
             int maxMatchID = matchmgr.maxMatchID();
             TeamManager tManager = new TeamManager();
             ArrayList<Team> teams = tManager.listAll();
 
             printShowHeader();
             for (Team t : teams)
             {
                 System.out.println(t);
             }
 
             System.out.print("Select Team id: ");
             int id = new Scanner(System.in).nextInt();
             removePoints(id);
             matchmgr.deleteFromTeamAndMatch(id);
         }
         catch (Exception e)
         {
             System.out.println(" ERROR - " + e.getMessage());
         }
         pause();
     }
 
     /**
      *
      */
     private void AssignToGroups()
     {
         clear();
         try
         {
             int counter = teammgr.showCount();
             int tm = teammgr.getByID(1).getGroupId();
             if (tm == 0)
             {
 
                 System.out.println("CAUTION!");
                 System.out.println("If continuing with assigning groups, it wont be "
                         + "possible to add teams or assign groups again!");
                 System.out.println("Do you want to continue? Y/N");
                 Scanner sc = new Scanner(System.in, "iso-8859-1");
                 String further = sc.nextLine();
                 switch (further)
                 {
                     case "Y":
                         assignGroups(counter);
                         break;
                     case "y":
                         assignGroups(counter);
                         break;
                     case "N":
                         System.out.println("You have selected no.");
                         break;
                     case "n":
                         System.out.println("You have selected no.");
                         break;
                     default:
                         System.out.println("Y or N Required");
                         break;
                 }
             }
             else
             {
                 System.out.println("Groups have already been assigned!");
             }
         }
         catch (Exception e)
         {
             System.out.println("ERROR - " + e.getLocalizedMessage());
         }
         pause();
     }
 
     private void assignGroups(int counter)
     {
         if (counter >= 12 && counter <= 16)
         {
             System.out.println("Assigning Teams To Groups!....");
 
             try
             {
                 teammgr.assignRandomGroups();
             }
             catch (Exception e)
             {
                 System.out.println(" ERROR - " + e.getMessage());
             }
         }
         else if (counter < 12)
         {
             System.out.println("Too few teams to assign.");
         }
         else
         {
             System.out.println("Too many teams to assign.");
         }
 
     }
 
     private void removePoints(int id) throws SQLException
     {
         /*
          * Gets the number of group matches where the team id is playing or has played. 
          * The -1 is because the arraylist is not starting on 1 but 0, so 1 lower.
          */
         int matches = matchmgr.showCountTeamGroup(id) -1;
         /*
          * The i in the for loop symbolises the place in the arraylist.
          */
         for (int i = 0; i <= matches; i++)
         {
             int guestTeamID = matchmgr.getGroupMatchesByTeamID(id).get(i).getGuestTeamID();
             int homeTeamID = matchmgr.getGroupMatchesByTeamID(id).get(i).getHomeTeamID();
             /*
              * Checking whether the selected match is played, if 1 continue
              */
             if (matchmgr.getGroupMatchesByTeamID(id).get(i).getIsPlayed() == 1)
             {
                 /*
                  * Checks if the given team id is the same the hometeams id in the match.
                  */
                 if (id == homeTeamID)
                 {
                     /*
                      * Checks if the chosen team has lost or were tied
                      * if they lost, the winning team looses 3 points
                      * if tied the other team looses 1 point.
                      * if they wont we wont remove any points, since the team is getting deleted.
                      */
                     if (matchmgr.getGroupMatchesByTeamID(id).get(i).getHomeGoals() < matchmgr.getGroupMatchesByTeamID(id).get(i).getGuestGoals())
                     {
                         int currentPoints = teammgr.getByID(guestTeamID).getPoints();
                         teammgr.setPoints(currentPoints - 3, teammgr.getByID(guestTeamID));
                     }
                     else if (matchmgr.getGroupMatchesByTeamID(id).get(i).getHomeGoals() == matchmgr.getGroupMatchesByTeamID(id).get(i).getGuestGoals())
                     {
                         int currentPoints = teammgr.getByID(guestTeamID).getPoints();
                         teammgr.setPoints(currentPoints - 1, teammgr.getByID(guestTeamID));
                     }
                 }
                 /*
                  * Checks if the given team id is the same the guestteams id in the match.
                  */
                 else if (id == guestTeamID)
                 {
                     /*
                      * Checks if the chosen team has lost or were tied
                      * if they lost, the winning team looses 3 points
                      * if tied the other team looses 1 point.
                      * if they wont we wont remove any points, since the team is getting deleted.
                      */
                     if (matchmgr.getGroupMatchesByTeamID(id).get(i).getHomeGoals() > matchmgr.getGroupMatchesByTeamID(id).get(i).getGuestGoals())
                     {
                         int currentPoints = teammgr.getByID(homeTeamID).getPoints();
                         teammgr.setPoints(currentPoints - 3, teammgr.getByID(homeTeamID));
                     }
                     else if (matchmgr.getGroupMatchesByTeamID(id).get(i).getHomeGoals() == matchmgr.getGroupMatchesByTeamID(id).get(i).getGuestGoals())
                     {
                         int currentPoints = teammgr.getByID(homeTeamID).getPoints();
                         teammgr.setPoints(currentPoints - 1, teammgr.getByID(homeTeamID)); 
                     }
                 }
             }
         }
     }
 
     /**
      *
      */
     private void doActionExit()
     {
         System.out.println("YOU SELECTED EXIT !!");
     }
 }
