 package ui;
 import java.util.Scanner;
 import java.sql.SQLException;
 import lib.*;
 
 /**
  * Description here.
  */
 
 public class ODLCLI
 {
     static final String HP = "HP";
     static final String PATIENT = "patient";
     DBAPI api;
     String id="";
 
     public ODLCLI() 
     {
         preStart();
         startMenu();
     }
 
     public static void main(String [] args)
     {
         new ODLCLI();
     }
 
     public void preStart() {
         System.out.println("Please enter an oracle DB " + 
                             "username and password.\n");
 
         Scanner sc = new Scanner(System.in);
         System.out.println("Username (ie \"jsmith\"): ");
         String user = sc.nextLine().trim();
         //System.out.print("Password: ");
         //String passwd = sc.nextLine().trim();
         //Use hidden password - comment out only for local testing
         String passwd = new String(System.console().readPassword("Password: "));
         
         api = new DBAPI();
         if (!api.authDB(user, passwd)) {
             System.out.println("\nError connecting to Oracle database. Please try again.\n");
             preStart();
         }
         else
             promptReinit();
     }
 
     public void promptReinit() {
         System.out.print("\nWould you like to delete any existing tables and " + 
                     "re-initialize tables with the sample data? (y/n)");
         Scanner sc = new Scanner(System.in);
         String in = sc.nextLine().trim();
         switch (in) {
             case "y":
                 api.dropTables();
                 api.initTables();
                 break;
             case "n":
                 break;
             default:
                 System.out.println("Please enter \'y\' or \'n\'.");
                 promptReinit();
         }
     }
 
     public void startMenu()
     {
         Scanner in = new Scanner(System.in);
         System.out.println("\n\n\n===== Observations of Dailing Living -- Start Menu =====\n");
         System.out.println("Available Options:");
         System.out.println(" 1.  Login");
         System.out.println(" 2.  Create User");
         System.out.println(" 3.  Exit");
         System.out.print("\nInput: ");
         String input = in.nextLine().trim();
         switch (input) {
             case "1":
                 login();
                 break;
             case "2":
                 createUser();
                 break;
             case "3":
                 terminate(0, "Exiting.");
             default:
                 System.out.println("Invalid input. Please Try again.");
                 startMenu();
         }
     }
 
     public void login()
     {
         String role="";
         //Take username and password from the user
         Scanner s=new Scanner(System.in);
         String uname="", password="";
         System.out.print("\nUsername: ");
         uname=s.nextLine().trim();
         id=uname;
         //System.out.print("Password: ");
         //Use hidden password - comment out only for local testing
         password = new String(System.console().readPassword("Password: "));
         //password=s.nextLine().trim();
         //call to DBAPI.authLogin for validation
         try {
             role=api.authLogin(uname,password);
         }
         catch(SQLException e )
         {
             System.out.println("ERROR");
         }
         switch (role) {
             case PATIENT:
                 patientMenu(id);
                 break;
             case HP:
                 healthProfMenu();
                 break;
             default:
                 invalidUserLogin();
         }
     }
     public void invalidUserLogin() {
         Scanner s = new Scanner(System.in);
         System.out.println("\nLogin incorrect. Would you like to try again?");
         System.out.print("1. Try again\n2. Create User\n3. Back\nInput: ");
         String choice = s.nextLine().trim();
         if (choice.equals("1"))
             login();
         else if (choice.equals("2"))
             createUser();
         else if (choice.equals("3"))
             startMenu();
         else {
             System.out.println("Invalid option.  Please try again.");
             login();
         }
     }
 
     public void createUser()
     {
         System.out.println("Create user screen not yet implemented, returning to start menu.");
         startMenu();
     }
     public void terminate(int status, String msg)
     {
         System.out.println(msg + "\n");
         System.exit(status);
     }
     
     public void patientMenu(String id)
     {
         while(true)
         {
             Scanner in = new Scanner(System.in);
             System.out.println("\n\n\n\n===== Observations of Dailing Living -- Observation Menu =====\n");
             System.out.println("Available Options:");
             System.out.println(" 1.  Enter Observations");
             System.out.println(" 2.  View Observations");
             System.out.println(" 3.  Add a new Observation Type");
             System.out.println(" 4.  View my Alerts");
             System.out.println(" 5. Manage HealthFriends");
             System.out.println(" 6.  Back (log out)");
             System.out.print("\nInput: ");
             String input = in.nextLine().trim();
             switch (input) {
                 case "1":
                     recordObservation();
                     break;
                 case "2":
                     viewObservations(id);
                     break;
                 case "3":
                     addObservationType(PATIENT);
                     break;
                 case "4":
                     //createUser();//write alert function name here --createUser() shouldn't be here, should it?
                     break;
                 case "5":
                     healthFriendsMenu();
                     break;
                 case "6":
                     startMenu();
                     break;
                 default:
                     System.out.println("Invalid input. Please Try again.");
                     patientMenu(id);
             }
         }
     }
     public void recordObservation()
     {
         String obsType,obsDate,obsTime;
         Scanner sc = new Scanner(System.in);
         System.out.println("Enter observations for the following available types based on your Illness :");
         api.observationMenu("ggeorge");
         System.out.print("Enter your type of Observation : ");
         obsType= sc.nextLine().trim();
         System.out.println(obsType +":\nEnter :");
         System.out.println("Enter Date of Observation in mm/dd/yyyy format :");
         obsDate= sc.nextLine().trim();
         System.out.println("Enter Time of Observation in HH:mm:ss format :");
         obsTime= sc.nextLine().trim();
         api.enterObservation("ggeorge",obsType,obsDate,obsTime);
     }
 
     public void viewObservations(String patientId)
     {
         api.displayObservations(patientId);
     }
 
     public void addObservationType(String userType)
     {
         Scanner sc = new Scanner(System.in);
         System.out.print("Enter your Type of Observation: ");
         String type= sc.nextLine().trim();
         System.out.print("Enter your Category of Observation: ");
         String category= sc.nextLine().trim();
         System.out.print("Enter your Additional Information about the Observation: ");
         String additionalInfo= sc.nextLine().trim();
         if (userType.equals(PATIENT)) {
            if (api.addNewType(type, category, additionalInfo, "general"))
                System.out.println("New general observation type successfully added!");
             else
                 System.out.println("Failed to add new observation type!");
         }
         else if (userType.equals(HP))
             addAssocTypeIll(type, category, additionalInfo);
     }
 
     /**
      *  For associating an observation type with an illness at the time of insertion.
      */
     public void addAssocTypeIll(String type, String category, String additionalInfo) {
         Scanner in = new Scanner(System.in);
         System.out.print("Enter a Patient/illness Class to associate the observation\n" +
             "    type \"" + type + "\" with (N/A for General): ");
         String illness = in.nextLine().trim();
         if(api.addNewType(type, category, additionalInfo, illness)) {
             System.out.println("Association between type \"" + type + "\" and patient class \"" +
                 illness + "\" successfully added!");
         }
         else
             System.out.println("Failed to add association.");
     }
 
     /**
      *  For adding an association to a pre-existing type of observation.
      */
     public void addAssocTypeIll() {
         Scanner in = new Scanner(System.in);
         System.out.println("Add an association between observation type and illness: ");
         System.out.print("Observation type: ");
         String type = in.nextLine().trim();
         System.out.print("Patient Class/Illness (N/A for General): ");
         String illness = in.nextLine().trim();
         if (illness.equals("N/A"))
             illness = "General";
         if (api.addAssoc(type, illness)) { //associate illness with type, overwrite general.  Else, create new?
             System.out.println("Association between type \"" + type + "\" and patient class \"" +
                 illness + "\" successfully added!");
         }
         else
             System.out.println("Failed to add association.");
     }
 
     public void healthProfMenu()
     {
         while(true)
         {
             Scanner in = new Scanner(System.in);
             System.out.println("\n\n\n\n===== Observations of Dailing Living -- Health Professional Menu =====\n");
             System.out.println("Available Options:");
             System.out.println(" 1.  Add a New Observation Type");
             System.out.println(" 2.  Add an Association Between Observation Type and Illness");
             System.out.println(" 3.  View Patients");
             System.out.println(" 4.  Back (log out)");
             System.out.print("\nInput: ");
             String input = in.nextLine().trim();
             switch (input) {
                 case "1":
                     addObservationType(HP);
                     break;
                 case "2":
                     addAssocTypeIll();
                     break;
                 case "3":
                     //viewPatients();
                     break;
                 case "4":
                     startMenu();
                     break;
                 default:
                     System.out.println("Invalid input. Please Try again.");
                     patientMenu(id);
             }
         }
     }
 
     public void healthFriendsMenu()
     {
         String selectHF="";
         boolean hasHF=true;
         System.out.println("Select an option");
         Scanner in=new Scanner(System.in);
         System.out.println("1. View existing Health Friends");
         System.out.println("2. Find a new Health Friend");
         System.out.println("3. Find a Health Friend at risk");
         System.out.println("4. Back");
         String input = in.nextLine().trim();
 
         switch (input) {
             case "1":
 
             try{
                 hasHF=api.viewHF(id);
             }
             catch(SQLException e)
             {}
 
             if(hasHF) {
                 System.out.println("***End of Health Friends List***\n\n");
                 System.out.println("Select a friend by Health Friend ID  or '0' to go back");
 
                 selectHF=in.nextLine().trim();
 
                 if(selectHF.matches("0"))
                     healthFriendsMenu();
                 else
                     existingHFMenu(selectHF);
             }
             else
                 healthFriendsMenu(); 
             break;
             case "2":
                 boolean existnewfriend=true;
                 try{
                     existnewfriend=api.findNewHF(id);
                 }
                 catch(SQLException e)
                 {}
                 Scanner s=new Scanner(System.in);
                 String option="y";
                 String addFriend="";
                 while(option.matches("y")&&existnewfriend)
                 {
                     System.out.println("\n\nAdd new HealthFriend? (y/n) ");
                     option=s.nextLine().trim();
                     switch(option)
                     {
                         case "y":
                             System.out.println("Enter PATIENT ID to add him as your friend: ");
                             addFriend=s.nextLine().trim();
                             try{
                                 api.addNewHF(id,addFriend);
                                 existnewfriend=api.findNewHF(id);
                             }
                             catch(SQLException e)
                             {}
                             break;
                         case "n": 
                         healthFriendsMenu();
                         break;
                         default:
                             System.out.println("Invalid input. Please Try again.");
                     }
                 }
                 healthFriendsMenu();
                 break;
             case "3":
                 Scanner sc=new Scanner(System.in);
                 boolean atRisk=true;
                 try {
                     atRisk=api.viewRiskHF(id);
                 }
                 catch(SQLException e)
                 {}
                 option="y";
                 String riskFriend="";
                 while(option.matches("y")&&atRisk)
                 {
                     System.out.println("\n\nSend message to health friend at risk ? (y/n) ");
                     option=sc.nextLine().trim();
                     switch(option)
                     {
                         case "y":
                             System.out.println("Enter HealthFriend ID to message healthfriend: ");
                             riskFriend=sc.nextLine().trim();
                             try {
                                 api.msgRiskHF(id,riskFriend);
                                 atRisk=api.viewRiskHF(id);
                             }
                             catch(SQLException e)
                             {}
                             break;
                         case "n": 
                             healthFriendsMenu();
                             break;
                         default:
                             System.out.println("Invalid input. Please Try again.");
                     }
                 }
                 healthFriendsMenu(); 
                 break;
             case "4":
                 patientMenu(id);
                 break;
            default:
                System.out.println("Invalid input. Please Try again.");
         }
     }
 
     public void existingHFMenu(String selectedHF) 
     {
         System.out.println("Select an option");
         Scanner in=new Scanner(System.in);
 
         System.out.println("1. View a list of the friend's active (unviewed) alerts");
         System.out.println("2. View observations of the friend");
         System.out.println("3. Back");
         String input = in.nextLine().trim();
         switch (input) {
             case "1":
                 try{
                 api.viewHFAlerts(selectedHF);
                 existingHFMenu(selectedHF);
                 }
                 catch(SQLException e)
                             {}
                 break;
             case "2":
                 api.viewHFobs(selectedHF);
                 existingHFMenu(selectedHF);
                 break;
             case "3":
                 healthFriendsMenu();
         }
     }
 }
