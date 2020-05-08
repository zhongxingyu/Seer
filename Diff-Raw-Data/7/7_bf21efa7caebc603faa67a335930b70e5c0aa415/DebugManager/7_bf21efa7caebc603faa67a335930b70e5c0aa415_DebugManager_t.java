 package JTools;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 /**
  * Manages Debug Settings
  * <p>
  * Use
  * </p>
  * <p>
  * Available Debug Levels:
  * <table style="width:100%;"><tr><td>No Debug Tools, All Logging (Finest, Finer, Fine, Info, Warning and Severe)</td><td>-1</td></tr>
  * <tr><td>No Debug Tools, Normal Logging (Info, Warning and Severe) - DEFAULT</td><td>0</td></tr>
  * <tr><td>Debug Tools, Normal Logging (Info, Warning and Severe)</td><td>1</td></tr>
  * <tr><td>Debug Tools, All Logging (Finest, Finer, Fine, Info, Warning and Severe)</td><td>2</td></tr></table>
  * </p>
  * 
  * @author Tahoma Menta
  * @version V0.1.2
  */
 public class DebugManager {
 static ConsoleManager con = new ConsoleManager();
 /**
  * Defines what the current debug level is.<br>
  * This is defaulted to <code>0</code>.
  * 
  * @since V0.1
  */
     private static int debug;
 /**
  * Defines if the Debug Manager should automatically set variables to default values.<br>
  * This is defaulted to <code>false</code>.
  * 
  * @since V0.1.2
  */
     private static boolean forceReset;
 /**
  * Only used for testing purposes to allow program to run.
  * 
  * @deprecated Only for testing purposes. Will be removed in final version.
  * @since V0.1.2
  */
     public static void main(String[] args){DebugManager dbm = new DebugManager();dbm.menu();}
     
 /**
  * Returns the current debug level.
  * 
  * @return int Debug Level Number 
  * @since V0.1
  */
     public static int getDebugLevel() {return debug;}
 /**
  * Returns true or false in relation to if the debug tools are active or not.
  * 
  * @return boolean If the debug tools are active or not.
  * @since V0.1
  */
     public static boolean getDebugStatus() {if(debug>=1){return true;}else{return false;}}
 /**
  * Changes the debug level to the level supplied.
  * 
  * @param foo int The debug level to set.
  * @since V0.1
  */
     public static void setDebugLevel(int foo) {
         if(foo>=-1&&foo<=2){debug=foo;con.con(3, "New Debug Level: " + debug);saveVars();}
         else{con.severe("Invalid Debug Level!");con.severe("Level Supplied: " + foo);con.severe("Levels Valid: -1, 0, 1 & 2");}
     }
     
 /**
  * Runs the menu to perform various debug commands.
  * <p>
  * This includes:<br>
  * setDebugLevel
  * </p>
  * 
  * @since V0.1.2
  */
     public static void menu() {
         con.con(3, "Running Debug Menu:");
         con.con(3, "1. Change Debug Level");
         con.con(3, "0. Exit");
         con.con(3, "-1. Reset To Default Settings");
        int optn = con.inputMenuOption();
        if(optn==1){con.con(3,"Current Debug Level: " + debug);con.con(3, "Please Enter New Debug Level.");setDebugLevel(con.inputMenuOption());menu();}
        else if(optn==-1){
             Scanner sc = new Scanner(System.in);
             boolean bar = false;
             while(!bar){
                 con.con(3, "ARE YOU SURE? [y/N]");
                System.out.print("> ");
                 String foo = sc.nextLine();
                 if(foo.equalsIgnoreCase("Y")){reset();bar=true;}
                 else if(foo.equalsIgnoreCase("N")||foo.isEmpty()){bar=true;}
                 else{con.con(3, "INVALID OPTION!");}
             }
             menu();
         }
     }
 /**
  * Resets debug variables to their default state.
  * <p>
  * Default states are:<br>
  * <table style="width:100px;"><tr><td>debug</td><td>0</td></tr>
  * <tr><td>forceReset</td><td>false</td></tr></table>
  * </p>
  * 
  * @since V0.1.2
  */
     private static void reset() {
         con.fine("Resetting Debug Variables!");
         con.finer("Resetting Debug Level");
         debug=0;
         con.finer("Debug Level Reset To Default (0)");
         con.finer("Disabling Force Reset");
         forceReset = false;
         con.finer("Force Reset Disabled");
         con.finer("Saving Vars");
         saveVars();
         con.finer("Finished Saving Vars");
         con.fine("Finished Variable Reset");
     }
 /**
  * Saves the current variables states to a file which can be loaded later.
  * 
  * @since V0.1.2
  */
     private static void saveVars(){
         try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("C:\\JTools\\debugSettings", false)))) {
             out.println(debug);
             out.println(forceReset);
             out.close();
         } catch (IOException ex) {
         }
     }
 /**
  * Loads saved variables from a file.
  * 
  * @since V0.1.2
  */
     private static void readVars(){
         try(BufferedReader in = new BufferedReader(new FileReader("C:\\JTools\\debugSettings"))) {
             debug = Integer.parseInt(in.readLine());
             forceReset = Boolean.parseBoolean(in.readLine());
             in.close();
         } catch (IOException ex) {
         }
     }
 /**
  * Run when a DebugManager object is created.
  * 
  * @since V0.1.2
  */
     public DebugManager(){
         readVars();
         if(forceReset){con.warn("Settings Demand A Force Reset!!!!!");reset();}
     }
 }
