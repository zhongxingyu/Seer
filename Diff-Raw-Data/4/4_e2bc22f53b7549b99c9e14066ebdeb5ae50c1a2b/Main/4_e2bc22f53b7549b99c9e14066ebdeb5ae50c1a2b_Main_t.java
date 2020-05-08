 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package main;
 
 // Default Libraries
 import java.io.*;
 import java.util.Vector;
 import java.util.Arrays;
 import java.util.Random;
 
 // Libraries
import ui.MENU;
 import arena.*;
 import players.*;
 import exception.*;
 import stackable.*;
 import parameters.*;
 import robot.Command;
 
 // Configs interfaces
 import ui.Interfaces;
 import random.Weather;
 
 // External Libraries (.jar)
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 /**
  * <b>Main class</b><br>
  * Controls all the user interactions,
  * the start and end of the program.
  */
 public class Main 
 {
     final private static String USAGE = 
         "USAGE: java -jar dist/MAC0242.jar <prog1> <prog2> <prog3> [-v|-d]";
 
     // Options
     static private int     port   = 3742;
     static private boolean help   = false;
     static private boolean usage  = false;
     static private boolean multi  = false;
     static private Interfaces UI = Interfaces.GRAPHICAL;
     static private MENU MENU = new ui.graphical.Menu();
     
     /**
      * <b>Main method</b><br>
      * Gets options from command line, interacts
      * with user and starts the game.
      */
     public static void main(String[] argv)
         throws InvalidOperationException
     {
         String[] args = getopt(argv); // Get options
         System.out.println("");
         
         // Help and Usage
         if(help) { help(); return; }
         if(args.length > Game.ROBOTS_NUM_INITIAL) 
         { System.err.println(USAGE); return; }
         
         if(multi) 
         {
             System.out.println("Multiplayer mode: not creating maps...");
             return;
         }
         
         while(menu(args));
         System.exit(0);
     }
     
     /**
      * Call menu and deal whith its options.
      * @param  args File names from stdin
      * @return Boolean to indicate if the game
      *         may or not continue
      */
     private static boolean menu(String[] args)
         throws InvalidOperationException
     {
         if(Debugger.debugging()) newGame(args);
         else switch(MENU.exhibit())
         {
             case NEW_GAME: newGame(args); break;
             case EXIT:     return false;
         }
         return true;
     }
     
     /**
      * Create a new game.
      * @param args File names from stdin
      */    
     private static void newGame(String[] args)
         throws InvalidOperationException
     {
         // Generate map
         Player[] p = World.genesis(2, 1, Game.WEATHER, UI, Game.RAND);
         
         // Menu
         // TODO: automate inserction of programs
         String ROOT = "/data/behaviors/";
         if(args.length > 1 && !multi)
         {
             p[0].insertArmy("Caprica Six"     , ROOT + "Protector.asm");
             p[0].insertArmy("Number Seventeen", ROOT + "Protector.asm");
             p[0].insertArmy("Megatron"        , ROOT + "Carrier.asm"  );
         }
         
         String[] names = { "Boomer", "Number Eighteen", "Optimus Prime" };
     
         for(int i = 0; i < args.length && i < Game.ROBOTS_NUM_INITIAL; i++)
             p[1].insertArmy(names[i], args[i]);
         
         // Game main loop
         if(Debugger.debugging()) 
         {
             for(int ts = 0; ts < 1000 && World.timeStep(); ts++);
             System.exit(0);
         }
         
         // Run ad infinitum if not debugging
         while(World.timeStep());
     }
     
     /**
      * Encapsulates the use of the library
      * GetOpt to get the options for the program.
      * @param  argv Argument vector (with options)
      * @return Argument vector without options (args)
      */
     private static String[] getopt(String[] argv)
     {
         String arg;
                 
         LongOpt[] longopts = 
         {
             // Help and Debug
             new LongOpt("help"   , LongOpt.NO_ARGUMENT, null, 'h'),
             new LongOpt("debug"  , LongOpt.NO_ARGUMENT, null, 'd'),
             new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v'),
 
             // Weather options
             new LongOpt("artical"     , LongOpt.NO_ARGUMENT, null, 1),
             new LongOpt("desertic"    , LongOpt.NO_ARGUMENT, null, 2),
             new LongOpt("tropical"    , LongOpt.NO_ARGUMENT, null, 3),
             new LongOpt("continental" , LongOpt.NO_ARGUMENT, null, 4),
             
             // Interfaces
             new LongOpt("textual", LongOpt.NO_ARGUMENT, null, 't'),
             
             // Size
             new LongOpt("small", LongOpt.NO_ARGUMENT, null, 's'),
             new LongOpt("large", LongOpt.NO_ARGUMENT, null, 'l'),
             
             // Seed
             new LongOpt("seed", LongOpt.REQUIRED_ARGUMENT, null, 5),
             
             // Server-Client structure
             new LongOpt("port",   LongOpt.REQUIRED_ARGUMENT, null, 'p'),
             new LongOpt("multi",  LongOpt.NO_ARGUMENT,       null,  6 ),
             new LongOpt("single", LongOpt.NO_ARGUMENT,       null,  7 ),
         };
         //
         Getopt g = new Getopt("MAC0242-Project",argv,"hdvtslp:",longopts);
         
         int c;
         while ((c = g.getopt()) != -1)
         {
             switch(c)
             {
                 case 'h': // --help
                     help = true;
                     break;
                 //
                 case 'v': // --verbose
                     Debugger.init();
                     break;
                 //
                 case 'd': // --debug
                     Debugger.init();
                     UI = Interfaces.NONE;
                     break;
                 //
                 case 1: Game.WEATHER = Weather.ARTICAL;     break;
                 case 2: Game.WEATHER = Weather.DESERTIC;    break;
                 case 3: Game.WEATHER = Weather.TROPICAL;    break;
                 case 4: Game.WEATHER = Weather.CONTINENTAL; break;
                 //
                 case 5: // --seed
                     arg = g.getOptarg();
                     Game.RAND = new Random(Integer.valueOf(arg)); 
                     break;
                 //
                 case 'p': // --port
                     arg = g.getOptarg();
                     port = Integer.valueOf(arg);
                     break;
                 //
                 case 6: multi = true;   break; // --multi
                 case 7: multi = false;  break; // --single
                 //
                 case 't': // --textual
                     UI = Interfaces.TEXTUAL;
                     MENU = new ui.textual.Menu();
                     break;
                 //
                 case 's': Game.MAP_SIZE = 16; break; // --small
                 case 'l': Game.MAP_SIZE = 50; break; // --large
                 //
                 case '?': // getopt() will print the error
                     break;
                 //
                 default:
                     System.out.println("getopt() returned " + c);
             }
         }
         
         // Return array without the options
         return Arrays.copyOfRange(argv, g.getOptind(), argv.length);
     }
     
     /** 
      * Execute man page to show the help
      */
     private static void help()
     {
         // Generate and run process
         try {
             Process p = Runtime.getRuntime().exec(
                 new String[] {
                     "sh", "-c", "man ./doc/robots.6 < /dev/tty > /dev/tty"
                 }
             );
             
             try{ p.waitFor(); }
             catch(InterruptedException e) {
                 System.err.println("Execution interrupted!");
             }
         }
         catch(IOException e)
         {
             System.err.print("[MAIN] Impossible to print man output ");
         }
     }
 }
