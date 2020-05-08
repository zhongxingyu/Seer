 /**
  * A Universe object generates a game upon initialization,
  * and contains references to all the related objects, such
  * as Planets and Fleets.
  */
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
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
     private ArrayList<Player> players;
 
     private int initialShipsNumber; // the number of ships each player starts with
 
     static private int margin = 50;   // minimum distance from edges
     static private int minPlanetSize = 20;
     static private int maxPlanetSize = 60;
     static public int minPlanetSeparation = 80;
 
     static private String backgroundImageLocation = "resources/images/backgrounds/galaxy.jpg";
 
     public static Random randomGenerator = new Random();
 
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
        players=new ArrayList<Player>();
 
 
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
 
 
     /**
      * @param a new Planet, p
      * @param the minimumPlanetSeparation
      * @return whether the current planets are all minimumPlanetSeparation from the
      *  not-yet-added Planet p.
      */
     private boolean planetsAllDistanceAwayFrom(Planet p, int minimumPlanetSeparation)
     {
         boolean ok1=false, ok2=false;
         Planet[] ps = this.getPlanets();
         if(ps.length==0)return true;
         for (int i=0; i < ps.length; i++)
         {
             if (p.isFarEnoughAwayFrom(ps[i], minimumPlanetSeparation) == false) 
             {
                 return false;
             }
 
             if(ok1==false)
                 if(p.canReach(ps[i]))ok1=true;
             if(ok2==false)
                 if(ps[i].canReach(p))ok2=true;
         }
         return (ok1&&ok2);
     }
 
 
     private Planet generatePlanet()
     {
         int planetR = Universe.randomBetween(minPlanetSize, maxPlanetSize);
         int planetX = Universe.randomBetween(Universe.margin, (this.getWidth() - Universe.margin - planetR*2));
         int planetY = Universe.randomBetween(Universe.margin, (this.getHeight() - Universe.margin - planetR*2));
         // planet must be at least minDistanceFromEdges units from the sides
 
         return new Planet(planetX, planetY, planetR);
     }
 
     static int randomBetween(int min, int max)
     {
         return min+Universe.randomGenerator.nextInt(max-min);
         //return Math.max(min, Universe.randomGenerator.nextInt(max));
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
 
     public Fleet[] getFleets()
     {
         Fleet[] fleets = new Fleet[this.fleets.size()];
         return this.fleets.toArray(fleets);
     }
 
     public Fleet[] getPlayerFleets(Player p)
     {
         ArrayList<Fleet> playerFleets = new ArrayList<Fleet>();
         for (Fleet f : this.getFleets()) {
             if (f.belongsTo(p)) playerFleets.add(f);
         }
         return (Fleet[]) playerFleets.toArray();
     }
 
     public void addFleet(Fleet f)
     {
         this.fleets.add(f);
     }
 
     public Planet[] getPlanets()
     {
         Planet[] planets = new Planet[this.planets.size()];
         return this.planets.toArray(planets);
     }
 
     public Planet[] getPlayerPlanets(Player p)
     {
         ArrayList<Planet> playerPlanets = new ArrayList<Planet>();
         for (Planet plt : this.getPlanets()) {
             if (plt.belongsTo(p)) playerPlanets.add(plt);
         }
         return (Planet[]) playerPlanets.toArray();
     }
 
     public void addPlanet(Planet p)
     {
         this.planets.add(p);
     }
 
     /**
      * @return all GamePieces within the Universe - Planets and Fleets
      */
     public GamePiece[] getGamePieces() {
         ArrayList<GamePiece> gamePieces = new ArrayList<GamePiece>(this.fleets.size() + this.planets.size());
         gamePieces.addAll(this.fleets);
         gamePieces.addAll(this.planets);
         GamePiece[] gamePieceArray = new GamePiece[gamePieces.size()];
         return gamePieces.toArray(gamePieceArray);
     }
 
     /**
      * @return the backgroundImage
      */
     public BufferedImage getBackground()
     {
         return backgroundImage;
     }
 
     static public int getMinPlanetSize()
     {
         return minPlanetSize;
     }
     static public int getMaxPlanetSize()
     {
         return maxPlanetSize;
     }
 
     /**
      * @param backgroundImage the backgroundImage to set
      */
     public void setBackground(BufferedImage backgroundImage)
     {
         this.backgroundImage = backgroundImage;
     }
 
     public Player[] getPlayers()
     {
         Player[] players = new Player[this.players.size()];
         return this.players.toArray(players);
     }
 
     /**
      * @param Player p
      * 
      * Adds the Player p to the list of players in the universe.
      * DOES NOT GIVE THEM ANY PLANETS OR FLEETS.
      */
     public void addPlayer(Player p)
     {
         this.players.add(p);
     }
 
     /**
      * Call this after all Players have been added to the universe.
      * 
      * Gives each player a single planet and (this.initialShipsNumber) ships
      */
     public void setUpPlayers()
     {
         int numberOfPlayers = this.getPlayers().length;
         Planet[] homeplanets = this.findHomeplanetsForPlayers(numberOfPlayers);
 
         for (int i=0; i < numberOfPlayers; i++) {
             Player player = this.getPlayers()[i];
             Planet homeplanet = homeplanets[i];
             homeplanet.setControllingPlayer(player);
             homeplanet.setPlayerShips(player, this.initialShipsNumber);
         }
     }
 
     /**
      * @param numberOfPlayers
      * @return an array of Planets, suitable to be home-planets for each of the n
      *  players - they are all the maximum (but similar) distances possible
      *  apart from each other, and have similar resources.
      */
     private Planet[] findHomeplanetsForPlayers(int numberOfPlayers)
     {
         Planet[] homeplanets = new Planet[numberOfPlayers];
         // TODO: write an appropriate algorithm for this method
         int i,i2;
         int n=planets.size();
         Planet a1, a2, t1, t2;
         double m,t,rd;
         a1=planets.get(0);
         a2=planets.get(1);
         rd=a1.resourcesDifference(a2);
         if(rd<1)rd=1;
         m=a1.distance2(a2)/rd;
         for(i=0;i<n-1;i++)
             for(i2=i+1;i2<n;i2++)
             {
                 t1=planets.get(i);
                 t2=planets.get(i2);
                 rd=t1.resourcesDifference(t2);
                 if(rd<1)rd=1;
                 t=t1.distance2(t2)/rd;
                 if(t>m)
                 {
                     m=t;
                     a1=t1;
                     a2=t2;
                 }
             }
         homeplanets[0]=a1;
         homeplanets[1]=a2;
         return homeplanets;
     }
 }
