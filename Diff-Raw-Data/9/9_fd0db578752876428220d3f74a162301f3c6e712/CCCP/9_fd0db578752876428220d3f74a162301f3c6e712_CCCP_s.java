 package org.illithid.cccp;
 
 import net.slashie.libjcsi.CSIColor;
 import net.slashie.libjcsi.ConsoleSystemInterface;
 import net.slashie.libjcsi.jcurses.JCursesConsoleInterface;
 import net.slashie.libjcsi.wswing.WSwingConsoleInterface;
 
 import org.illithid.cccp.bestiary.Dalek;
 import org.illithid.cccp.bestiary.Hero;
 import org.illithid.cccp.intelligence.HumanIntelligence;
 import org.illithid.cccp.world.Level;
 import org.illithid.cccp.world.Occupier;
 import org.illithid.cccp.world.RandomLevel;
 
 public class CCCP extends Thread {
     private static ConsoleSystemInterface csi;
     private static int                    turns  = 0;
     public static ActorList               actors = new ActorList();
 
     private static Level                  level;
 
     public static void main(String args[]) throws InterruptedException {
 
         if (args.length > 0 && args[0].equalsIgnoreCase("-c"))
             initCurses();
         else
             initSwing();
 
         actors.add(new Hero(new HumanIntelligence(csi)));
         for (int i = 0; i < 10; i++)
             actors.add(new Dalek());
 
         level = new RandomLevel();
        // level.add(actors.getAll());
 
         while (true) {
             drawScreen();
             getInput();
 
             turns++;
         }
 
     }
 
     private static void initCurses() {
         try {
             csi = new JCursesConsoleInterface();
         } catch (ExceptionInInitializerError eiie) {
             System.out.println("Fatal Error Initializing JCurses");
             eiie.printStackTrace();
             System.exit(-1);
         }
 
     }
 
     private static void initSwing() {
         try {
             csi = new WSwingConsoleInterface("Illithid CCCP 0.0.3");
         } catch (ExceptionInInitializerError eiie) {
             System.out.println("Fatal Error Initializing Swing Console Box");
             eiie.printStackTrace();
             System.exit(-1);
         }
     }
 
     private static void getInput() {
         actors.act();
     }
 
     private static void drawScreen() {
 
         csi.cls();
 <<<<<<< HEAD:src/main/java/org/illithid/cccp/CCCP.java
         drawActors();
 =======
         drawAwesome();
         drawStuff();
 >>>>>>> 16ffb12... branchsfins:src/main/java/org/illithid/cccp/CCCP.java
         drawUI();
 
         csi.refresh();
     }
 
     private static void drawStuff() {
         for (int x = 0; x < level.getX(); x++) {
             for (int y = 0; y < level.getY(); y++) {
                 Occupier a = level.getCell(x, y).getOccupier();
                 csi.print(x, y, a.getFace(), a.getColor());
             }
         }
     }
 
     private static void drawUI() {
         csi.print(0, 0, "*", CSIColor.WHEAT);
         csi.print(79, 0, "*", CSIColor.WHEAT);
         csi.print(0, 24, "*", CSIColor.WHEAT);
         csi.print(79, 24, "*", CSIColor.WHEAT);
         csi.print(1, 0, "CCCP Sandbox 1", CSIColor.GRAY);
         csi.print(1, 1, "T:" + turns, CSIColor.GRAY);
 
     }
 
     public static void quit() {
         System.out.println("Exited by user");
         System.exit(0);
 
     }
 
 }
