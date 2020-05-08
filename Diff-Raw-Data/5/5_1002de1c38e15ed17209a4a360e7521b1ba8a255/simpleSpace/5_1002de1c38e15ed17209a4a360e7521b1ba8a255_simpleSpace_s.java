 package SimpleSpace;
 
 
 /**
  * @version 0.01
  * @author Devon Smith
  */
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.SAXException;
 import sun.misc.IOUtils;
 
 public class simpleSpace {
     
     /*
      * Class variable declarations
      */
     public int gameJumpCTR; // default = 1
     public int gameJumpMAX; //default = 15
     public ArrayList<entNPC> encounters;
     public ArrayList<item> possibleItems;
     public entPlayer player;
     public Random gen = new Random();
 
     simpleSpace() throws ParserConfigurationException, SAXException, IOException {
         player = new entPlayer();
         gameJumpCTR = 0;
         gameJumpMAX = 15;
         System.out.println("In a Galaxy far far far away in the distant future,\n"
                 + "you are the Captain of an alliance battleship, trapped deep\n"
                 + "within enemy territory with Intelligence information that could\n"
                 + "finally put an end to this war. In order to make it back to Alliance\n"
                 + "space, you must successfully make " + gameJumpMAX + " FTL jumps. That is " + gameJumpMAX + " times\n"
                 + "that the enemy has the oppurtunity to stop you, and turn the tide\n"
                 + "of the war in their favor...");
         encounters = new ArrayList();
         for (int i=0; i<gameJumpMAX; i++) {
             encounters.add(new entNPC());
         }
         possibleItems = new ArrayList();
         possibleItems.add(new item(0, 100, "Missile", "This is a self-propelled seeking projectile. It has a high damage value.", 0., 300.));
         possibleItems.add(new item(1, 100, "Rail Slug", "This is a rail-propelled unguided projectile. It has a med-high damage value.", 0., 150.));
         possibleItems.add(new item(2, 100, "Repair Kit", "This is a self-deploying repair kit. Each unit repairs 1HP of damage.", 1., 0.));
         player.inventory = possibleItems;
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("data/txt_test")) {
             DocumentBuilder db;
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             db = dbf.newDocumentBuilder();
             db.parse(is);
             System.out.println(db.toString());
         }
 
     }
     /*
      * show help function. just uses system.out.println but is used multiple times.
      */
     public void showHelp() {
         System.out.println("\n\n***Usage Guidelines***\n\n" +
                     "\t-c:\tIndicate the campaign file.\n" +
                     "\t-i:\tIndicate the items file.\n" +
                     "\t-n:\tIndicate the npc file.\n" +
                     "\t-p:\tIndicate the player file. This will be used for savegames, too.\n" +
                     "\t-h:\tShow this help message. This can also be triggered with --help.\n" );
         System.exit(0);
     }
     
     /*
      *  default function for handling switches. takes 2 strings, and parses them as switches.
      *  it sets variables appropriate to the modifiers specified by the switches.
      *  returns true if successful.
      */
     public boolean switchHandler(String arg, String param) throws Exception { //arg array instruction handler
         if(arg.equalsIgnoreCase("-c")) { //if arg is -c, then ....
             return true;
         } else if(arg.equalsIgnoreCase("-f")){ //if arg is -f, ....
             return true;
         } else if(arg.equalsIgnoreCase("-l")){ //if arg is -l, ....
             return true;  
         } else if(arg.equalsIgnoreCase("-p")){ //if arg is -p, ....
             return true;
         } else if(arg.equalsIgnoreCase("-h")){ //if arg is -h, echo help      
             showHelp();
             return true;
         } else if (arg.equals("--")) {
                 if (param.equalsIgnoreCase("listen")){
                     return true;
                 } else if (param.equalsIgnoreCase("help")) {
                     showHelp();
                     return true;
                 } else {
                     System.out.println("Invalid switch detected!");
                     return false;
                 }
         }
         else return false;
     }
     
     /*
      * argument preprocessor. takes args array and parses it into chunks for the switch handler.
      * does some extra parsing / error checking to save cycles.
      */
     public void processor(String[] args) throws Exception {
     //switchHandler Processor
         for(int i = 0; i < args.length; i++) {
             if(args[i].length() > 2 && !args[i].substring(0,2).equals("--")){
                 switchHandler(args[i].substring(0,2), args[i].substring(2));
             } else if(args[i].length() == 2) {
                 switchHandler(args[i].substring(0,2), args[i+1]);
                 i++;
             } else System.out.println("Invalid switch detected!");
         }
     }
     
     public void startGameEvent(Scanner in, event ev, item item) throws InterruptedException {
         while (player.isAlive) {
             if (player.isAlive) {
                     System.out.println("|----------------|");
                     System.out.println("|  Select one:   |");
                     System.out.println("|  1. Jump.      |");
                     System.out.println("|  2. Inventory  |");
                     System.out.println("|  3. Quit.      |");
                     System.out.println("|----------------|");
 
                     int menuItem = in.nextInt();
 
                     switch (menuItem) {
                         case 1:
                             gameJumpCTR++;
                             System.out.println(encounters.get(gameJumpCTR).initSpam);
                             while (encounters.get(gameJumpCTR).isAlive) {
                                 Thread.sleep(250);
                                 player.battle(encounters.get(gameJumpCTR));
                             }
                             if (!encounters.get(gameJumpCTR).isAlive && player.isAlive) {
                                 System.out.println("Success! You vanquished " + encounters.get(gameJumpCTR).name + "! \n And now for the loot!");
                                 encounters.get(gameJumpCTR).loot(player.inventory);
                             }
                             else startGameFailEvent();
                         case 2:
 
                             break;
 
                         case 3:
                             System.exit(0);
 
                             break;
                     }
             }
             else {
                 startGameFailEvent();
             }
         }
     }
     
     public void startGameSuccessEvent() {
         System.out.println("Congratulations Captain, you have successfully brought\n"
                 + "your crew home safely, for the most part, and delivered your precious\n"
                 + "cargo to the alliance fleet headquarters. The war will soon be over because\n"
                 + "of you.");
         System.exit(0);        
     }
     
     public void startGameFailEvent() {
                 System.out.println("You have failed. GAME OVER");
                 System.exit(0);         
     }
     
     public void mnMenu() throws InterruptedException {
         Scanner in = new Scanner(System.in);
         event ev = new event();
         item item = new item();
         for (; gameJumpCTR < gameJumpMAX; gameJumpCTR++) {
                 startGameEvent(in, ev, item);
         }
         if (player.isAlive) { startGameSuccessEvent();}
         else { startGameFailEvent(); }
     }
     
     public static void main(String[] args) throws InterruptedException, Exception {
         simpleSpace game = new simpleSpace();
         game.processor(args);
         game.mnMenu();
     }
 
 
 }
