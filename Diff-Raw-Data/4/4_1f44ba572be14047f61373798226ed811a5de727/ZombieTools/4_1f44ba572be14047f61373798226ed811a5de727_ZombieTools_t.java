 package zombiefu.util;
 
 import java.util.HashMap;
 
 import jade.core.World;
 import jade.ui.TermPanel;
 import jade.util.Dice;
 import jade.util.Guard;
 import jade.util.datatype.Coordinate;
 import jade.util.datatype.Direction;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.logging.Logger;
 import zombiefu.creature.Player;
 import zombiefu.items.Teleporter;
 import zombiefu.level.Level;
 import zombiefu.ui.ZombieFrame;
 
 public class ZombieTools {
 
     public static Player activePlayer;
 
     public static void createStoryForPlayer(Player player) {
         Level world = createWorld();
         world.addActor(player);
     }
 
     public static void createBidirectionalTeleporter(World world1,
             Coordinate from1, Coordinate to2, World world2, Coordinate from2,
             Coordinate to1) {
         /*
          * fromi: Wo befindet sich der Teleporter in Welt i? toi: Wo soll der
          * Player in Welt i hinteleportiert werden?
          */
         Teleporter tel1 = new Teleporter(world2, to2);
         Teleporter tel2 = new Teleporter(world1, to1);
         world1.addActor(tel1, from1);
         world2.addActor(tel2, from2);
     }
 
     private static Level createWorld() {
         String srcs = "src/sources/";
         String[] levels = Screen.getStrings(srcs + "levels.txt");
         String[] teles = Screen.getStrings(srcs + "teleporters.txt");
         HashMap<String, Level> nameOfLevels = new HashMap<String, Level>();
         for (String s : levels) {
             nameOfLevels.put(s, Level.levelFromFile(srcs + s + ".txt"));
         }
         for (String s : teles) {
             try {
                 String[] d = s.split(" ");
                 Level world1 = nameOfLevels.get(d[0]);
                 Coordinate from1 = new Coordinate(Integer.decode(d[1]),
                         Integer.decode(d[2]));
                 Coordinate to2 = new Coordinate(Integer.decode(d[3]),
                         Integer.decode(d[4]));
                 Level world2 = nameOfLevels.get(d[5]);
                 Coordinate from2 = new Coordinate(Integer.decode(d[6]),
                         Integer.decode(d[7]));
                 Coordinate to1 = new Coordinate(Integer.decode(d[8]),
                         Integer.decode(d[9]));
                 createBidirectionalTeleporter(world1, from1, to2, world2,
                         from2, to1);
             } catch (Exception e) {
             }
         }
         return nameOfLevels.get(levels[0]);
     }
 
     public static List<Direction> getAllowedDirections() {
         return Arrays.asList(Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.NORTH);
     }
 
     public static Direction getRandomDirection() {
         return Dice.global.choose(getAllowedDirections());
     }
 
     public static void setTopTermContent(String s, ZombieFrame frame) {
         frame.topTerm().clearBuffer();
         frame.topTerm().bufferString(0, 0, s);
         frame.topTerm().refreshScreen();    
     }
     
     public static void clearTopTerm(ZombieFrame frame) {
         frame.topTerm().clearBuffer();
         frame.topTerm().refreshScreen();
     }
     
     public static Direction askForDirection(ZombieFrame frame) {
         setTopTermContent("Bitte gib die Richtung an.", frame);
         Direction d = null;
         try {
            while (d == null) {
                 d = Direction.keyToDir(frame.mainTerm().getKey());
             }
         } catch (InterruptedException ex) {
         }
         clearTopTerm(frame);
         return d;
     }
     
     public static void sendMessage(String s, ZombieFrame frame) {
         activePlayer.refreshWorld();
         setTopTermContent(s,frame);
         char key = 0;
         try {
             while (key != '\n') {
                 key = frame.mainTerm().getKey();
             }
         } catch (InterruptedException ex) {
         }
         clearTopTerm(frame);
     }
 
     public static void sendMessage(String string) {
         Guard.argumentIsNotNull(activePlayer);
         sendMessage(string, activePlayer.frame);
     }
 
     public static void registerPlayer(Player pl) {
         activePlayer = pl;
     }
     
     public static Color getRandomColor(Dice d) {
         return new Color(d.nextInt(0, 255),d.nextInt(0, 255),d.nextInt(0, 255));
     }
     
     public static Color getRandomColor() {
         return getRandomColor(Dice.global);
     }
 }
