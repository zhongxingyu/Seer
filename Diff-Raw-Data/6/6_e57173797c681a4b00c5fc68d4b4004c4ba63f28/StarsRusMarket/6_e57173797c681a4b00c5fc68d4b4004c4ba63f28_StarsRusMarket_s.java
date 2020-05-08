 import java.io.*;
 import java.sql.*;
 
 /** 
  *  Main interface for managers, customers,
  *  and administrators to interact with
  *  StarsRus movie star stock market.
  *  
  * @author Arya Pourzanjani & Justin Phang
  */
 public class StarsRusMarket
 {
    public static void main(String[] args) throws SQLException
    {
       //check for proper arg length in usage of program
       try{
          checkArgLength( args.length );
       }
       catch (ArgLengthException ale){
          System.out.println("Usage is StarsRusMarket MODE USERNAME PASSWORD");
          System.exit(1);
       }
 
       //attempt login based on mode
       LoginHandler L;
       ConnectionHandler C = null;
<<<<<<< HEAD
     UserInterfaceHandler U = null;
=======
	  UserInterfaceHandler U = null;
>>>>>>> upstream/master
       try {
       C = new ConnectionHandler();
       }
       catch (SQLException e) {
       System.out.println("Failed to connect to the database.");
       System.exit(1);
       }
       String loginMode = args[0];//set login mode to first command line arg
       switch( loginMode ){
          case "user":
             System.out.println("Attempting login as user...");
             L = new LoginHandler("user", args[1], args[2],C);
             U = new UserInterfaceHandler(args[1], C);
             break;
          case "manager":
             System.out.println("Attempting login as manager...");
             L = new LoginHandler("manager", "NA", args[2],C);
             ManagerInterfaceHandler M = new ManagerInterfaceHandler(C);
             break;
          case "admin":
             System.out.println("Attempting login as adminstrator...");
             L = new LoginHandler("admin", "NA", args[2],C);
             AdminInterfaceHandler A = new AdminInterfaceHandler(C);
             break;
          case "register":
             System.out.println("Starting registration process...");
             RegistrationHandler R = new RegistrationHandler(args[1], args[2],C);
             System.out.println("Registration complete! You are eligible to sign in as this user. Exiting.");
             break;
          default:
             try{ wrongLoginMode(); }
             catch( LoginModeException lme ){
                System.out.println("Invalid login mode. Please select either user, manager, admin, or register.");
             }
             break;
       }
 
    }
 
    /**
    * Check to make sure the user specified a mode, username, and password
    */
    private static void checkArgLength(int argsLength) throws ArgLengthException
    {
     if(argsLength == 3)
       System.out.println("Checking user mode...");
     else
       throw new ArgLengthException();
    }
 
    /*
    * Check to make sure the user selected a valid login mode
    */
    private static void wrongLoginMode() throws LoginModeException
    {
       throw new LoginModeException();
    }
 }
