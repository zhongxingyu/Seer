 /**
  * A Universe object generates a game upon initialization,
  * and contains references to all the related objects, such
  * as Planets and Fleets.
  */
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 /**
  * @author jeffbr
  *
  */
 public class Universe
 {
     private int width;
     private int height;
     private BufferedImage backgroundImage;
 
     private ArrayList<Fleet> fleets; 
     private ArrayList<Planet> planets;
 
 
     static private int margin = 100;   // minimum distance from edges
     static private int minPlanetSize = 20;
     static private int maxPlanetSize = 60;
    static public int minPlanetSeparation = 80;
 
     static private String backgroundImageLocation = "resources/images/backgrounds/galaxy1.jpg";
 
     private static Random randomGenerator = new Random();
 
     /*
      * Generate a universe with the given width and height.
      * 
      * The universe will have 2 planets and be at least Universe.minPlanetSeparation apart.
      */
     public Universe(int width, int height)
     {
         this(width, height, 2, Universe.minPlanetSeparation);
     }
 
     /*
      * Generate a universe with the given width, height, and number of planets.
      * 
      * The planets will all be at least Universe.minPlanetSeparation apart.
      * 
      * @author jeffbr
      */
     public Universe(int width, int height, int initialNumberOfPlanets)
     {
         this(width, height, initialNumberOfPlanets, Universe.minPlanetSeparation);
     }
 
     /*
      * This generates a universe of the appropriate width, height, and number
      * of planets present, and at least minimumPlanetSeparation away from each other.
      * 
      * @author jeffbr
      */
     public Universe(int width, int height, int initialNumberOfPlanets, int minimumPlanetSeparation)
     {
         this.width = width;
         this.height = height;
         this.fleets = new ArrayList<Fleet>();
         this.planets = new ArrayList<Planet>();
        
         for (int i=0; i < initialNumberOfPlanets; i++) {
 
             Planet p = this.generatePlanet();
            while (this.planetsAllDistanceAwayFrom(p, minimumPlanetSeparation) != true)
            {
                 p = this.generatePlanet();
             }
             this.addPlanet(p);
         }
 
         try {
             this.setBackground(ImageIO.read(new File(Universe.backgroundImageLocation)));
         }
         catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
 
    private boolean planetsAllDistanceAwayFrom(Planet p, int minimumPlanetSeparation)
     {
         ArrayList<Planet> ps = this.getPlanets();
         for (int i=0; i < ps.size(); i++) {
            if (p.isFarEnoughAwayFrom(ps.get(i), minimumPlanetSeparation) == false) 
            {
                 return false;
             }
         }
         return true;
     }
 
 
     private Planet generatePlanet()
     {
         int planetX = Universe.randomBetween(Universe.margin, (this.getWidth() - Universe.margin));
         int planetY = Universe.randomBetween(Universe.margin, (this.getHeight() - Universe.margin));
         // planet must be at least minDistanceFromEdges units from the sides
 
         return new Planet(planetX, planetY);
     }
 
 
     static int randomBetween(int min, int max)
     {
         return Math.max(min, Universe.randomGenerator.nextInt(max));
     }
 
     /**
      * @return the width
      */
     public int getWidth()
     {
         return this.width;
     }
 
     /**
      * @param width the width to set
      */
     public void setWidth(int width)
     {
         this.width = width;
     }
 
     /**
      * @return the height
      */
     public int getHeight()
     {
         return height;
     }
 
     /**
      * @param height the height to set
      */
     public void setHeight(int height)
     {
         this.height = height;
     }
 
     public void addFleet(Fleet f)
     {
         this.fleets.add(f);
     }
 
     public void addPlanet(Planet p)
     {
         this.planets.add(p);
     }
 
     public ArrayList<Fleet> getFleets()
     {
         return this.fleets;
     }
     public ArrayList<Planet> getPlanets()
     {
         return this.planets;
     }
 
     /**
      * @return the backgroundImage
      */
     public BufferedImage getBackground()
     {
         return backgroundImage;
     }
 
     /**
      * @param backgroundImage the backgroundImage to set
      */
     public void setBackground(BufferedImage backgroundImage)
     {
         this.backgroundImage = backgroundImage;
     }
 
 }
