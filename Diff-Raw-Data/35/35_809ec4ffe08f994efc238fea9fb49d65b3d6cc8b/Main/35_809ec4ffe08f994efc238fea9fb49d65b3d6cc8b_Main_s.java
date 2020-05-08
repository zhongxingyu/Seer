 package rky.portfolio;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import rky.portfolio.Player.Players;
 import rky.portfolio.gambles.Gamble;
 import rky.portfolio.gambles.Gambles;
 import rky.portfolio.io.FileManager;
 import rky.portfolio.io.GameData;
 import rky.portfolio.io.IoManager;
 import rky.util.CommandLineUtils;
 import rky.util.SetMap;
 
 public class Main
 {
     // For generating random gambles, should be removed?
     static final double AVERAGE_EXPECTED = 2.0;
     static final double HALF_PROBABILITY_OF_LINK = 0.2;
     static final int NUM_GAMBLES = (int) (Math.random() * 20) + 190;
     
     public static void main(String[] args)
     {
         Map<String,String> argMap = CommandLineUtils.simpleCommandLineParser(args);
         validate(argMap);
         
         Integer port = Integer.parseInt(argMap.get("-p"));
         IoManager io = new IoManager(port);
         
         Integer numPlayers = Integer.parseInt(argMap.get("-n"));
        Players players = io.connect(numPlayers);
         
         List<Gamble> gambles = generateGambles();
         SetMap<Integer, Integer> links = generateLinks(gambles);
         
         String clientInputFile = FileManager.writeInputFile(gambles, links);
         GameData gameData = FileManager.readGameData(clientInputFile);
         
         GameLoop gameLoop = new GameLoop( gameData, players, ScoreBoard.GameMode.mode1 );
     }
     
     private static void validate(Map<String, String> args)
     {
         try
         {
             if (args.size() < 3 || args.size() > 6) 
             {
                 throw new Exception("Invalid number of arguments.");
             }
             
             // port
             String p = args.get("-p");
             if (p == null || !p.matches("[\\d]+"))
             {
                 throw new Exception("Invalid port.");
             }
 
             // num players
             String n = args.get("-n");
             if (n == null || !n.matches("[\\d]+"))
             {
                 throw new Exception("Invalid number of expected players.");
             }
 
             // game mode
             String m = args.get("-m");
             if (m == null || (!m.equals("1") && !m.equals("2")))
             {
                 throw new Exception("Invalid game mode. Accepted values are 1 or 2.");
             }
             
             // gui
             String g = args.get("-g");
             if (g == null)
             {
                 args.put("-g", "false");
             }
             else if (!g.equals("true") && !g.equals("false"))
             {
                 throw new Exception("Invalid gui param. Accepted values are true or false.");
             }
 
             // optional client input file (gambles and links)
             String f = args.get("-f");
             if (f != null && !(new File(f).isFile()))
             {
                 throw new Exception("Invalid client input file specified. Maybe use absolute an path? (Or omit for a new random file.)");
             }
             
             // class favorability file 
             String c = args.get("-c");
             if (c != null && !(new File(c).isFile()))
             {
                 throw new Exception("Invalid class favorability file specified. Maybe use absolute an path? (Or omit for the standard file.)");
             }
         }
         catch (Exception e)
         {
             usage(e.getMessage());
             System.exit(-1);
         }
     }
 
     private static void usage(String message)
     {
         System.err.println("\nInvalid/missing arguments. " + message);
         System.err.println("\t -p The port number to listen on.");
         System.err.println("\t -n The number of expected players.");
         System.err.println("\t -m The game mode. Accepted values are 1 or 2.");
         System.err.println("\t -c Optional. For game mode 2. The class favorability file as specified by Prof. Shasha. ex http://cs.nyu.edu/courses/Fall12/CSCI-GA.2965-001/portattformat");
         System.err.println("\t -g Optional. Display the gui? Accepted values are true or false. Defaults to false.");
         System.err.println("\t -f Optional. Use this file of gambles and links. If absent a new file is generated. ");
         System.err.println("\nex: java -jar portfolio-1.0.0.jar -p 54321 -n 5 -m 2 -c /favorability/file.txt -g false -f /gambles/file.txt");
     }
     
     private static List<Gamble> generateGambles()
     {
 
         ArrayList<Double> gambleExpectedReturns = new ArrayList<Double>(NUM_GAMBLES);
 
         double sum = 0;
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             double randomValue = Math.random();
             gambleExpectedReturns.add(randomValue);
             sum += randomValue;
         }
 
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             gambleExpectedReturns.set(i, gambleExpectedReturns.get(i) * AVERAGE_EXPECTED / sum);
         }
 
         ArrayList<Gamble> gambles = new ArrayList<Gamble>(NUM_GAMBLES);
 
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             gambles.add(Gambles.randomGamble(gambleExpectedReturns.get(i)));
         }
 
         return gambles;
     }
     
     private static SetMap<Integer, Integer> generateLinks(List<Gamble> gambles)
     {
         Map<Gamble, Integer> gambleIds = new HashMap<Gamble, Integer>();
 
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             gambleIds.put(gambles.get(i), i);
         }
 
         SetMap<Integer, Integer> links = new SetMap<Integer, Integer>();
 
         for (Gamble g1 : gambles)
         {
             for (Gamble g2 : gambles)
             {
                 if (g1 == g2) continue;
 
                 if (Math.random() < HALF_PROBABILITY_OF_LINK)
                 {
                     links.put(gambleIds.get(g1), gambleIds.get(g2));
                     links.put(gambleIds.get(g2), gambleIds.get(g1));
                 }
             }
         }
         
         return links;
     }
     
 }
