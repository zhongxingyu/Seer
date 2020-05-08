 package mosquito.g4;
 
 import java.awt.geom.Line2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import mosquito.g4.voronoi.SectionLineDrawer;
 import mosquito.g4.voronoi.Sections;
 import mosquito.g4.voronoi.Voronoi;
 import mosquito.sim.Collector;
 import mosquito.sim.Light;
 import mosquito.sim.MoveableLight;
 import mosquito.sim.Player;
 
 import org.apache.log4j.Logger;
 
 public class VoronoiPlayer extends Player {
 
     private static final Logger log = Logger.getLogger(G4Player.class); // for
                                                                         // logging
 
     private int numLights;
     private Set<Light> lights;
     private Voronoi v;
     private Sweeper s;
     private Sections sections;
 
     private boolean[] issweeping;
 
     public VoronoiPlayer() {
         lights = new HashSet<Light>();
     }
 
     @Override
     public String getName() {
         return "Voronoi Player";
     }
 
     /*
      * This is called when a new game starts. It is passed the set of lines that
      * comprise the different walls, as well as the maximum number of lights you
      * are allowed to use.
      * 
      * The return value is a set of lines that you would like to have drawn on
      * the screen. These lines don't actually affect gameplay, they're just
      * there so you can have some visual clue as to what's happening in the
      * simulation.
      */
     @Override
     public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
         this.numLights = numLights;
         issweeping = new boolean[numLights];
         this.v = new Voronoi(numLights, 100, walls);
         v.doVoronoi();
 
         if (numLights == v.getNumSections()) {
             Arrays.fill(issweeping, true);
         }
 
         // ArrayList<Line2D> list = new ArrayList<Line2D>();
         //
         // for (Line2D wall : walls) {
         // list.add(Utils.getPerpendicularLine(wall, 10));
         // }
 
         sections = v.getSections();
        s = new Sweeper(v.getNumSections(), sections.getSectionBoard());
         return new SectionLineDrawer(v.getSectionIdBoard()).createLines();
     }
 
     /*
      * This is used to determine the initial placement of the lights. It is
      * called after startNewGame. The board tells you where the mosquitoes are:
      * board[x][y] tells you the number of mosquitoes at coordinate (x, y)
      */
     public Set<Light> getLights(int[][] board) {
 
        for (int i = 0; i < v.getNumSections(); i++) {
            lights.add(new MoveableLight(s.getStartingPoints().get(i).getX(), s
                    .getStartingPoints().get(i).getY(), true));
        }

        while (lights.size() < numLights) {
            // just for reference.
            lights.add(new MoveableLight(27, 1, true));
        }
         return lights;
     }
 
     /*
      * This is called at the beginning of each step (before the mosquitoes have
      * moved) If your Set contains additional lights, an error will occur. Also,
      * if a light moves more than one space in any direction, an error will
      * occur. The board tells you where the mosquitoes are: board[x][y] tells
      * you the number of mosquitoes at coordinate (x, y)
      */
     public Set<Light> updateLights(int[][] board) {
         for (Light l : lights) {
             MoveableLight ml = (MoveableLight) l;
             s.doSweep(ml, sections.getSection((int) ml.getX(), (int) ml.getY()));
         }
         return lights;
     }
 
     /*
      * Currently this is only called once (after getLights), so you cannot move
      * the Collector.
      */
     @Override
     public Collector getCollector() {
         return new Collector(50, 50);
     }
 }
