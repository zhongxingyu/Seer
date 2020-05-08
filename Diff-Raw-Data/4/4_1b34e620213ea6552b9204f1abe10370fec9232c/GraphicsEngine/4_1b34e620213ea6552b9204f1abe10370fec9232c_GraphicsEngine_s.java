 package game;
 
 
 import java.awt.Polygon;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 
 /**
  * The Graphics class is responsible for defining the shape of all in-game objects, and placing said shapes at the appropriate locations based on the position of the object.
  * It uses polygons for all shapes.
  * 
  * @author Michael Smith
  */
 public class GraphicsEngine implements Runnable{
     
  private GameState memory;
  private final int WIDTH = gui.MenuGUI.WIDTH;
  private final int HEIGHT = gui.MenuGUI.HEIGHT;
     
  public GraphicsEngine(GameState gamestate)
  {
     memory = gamestate;
  }
         
  /**
   * Updates the shape of all objects in preparation for physics calculations and graphics drawing.
   */
  public void updateGraphics()
  {
      if (memory.getPlayerShip() != null)
      {
          updatePlayerShip();
      }
      if (!memory.getAsteroids().isEmpty())
      {
          updateAsteroids();
      }
      if (memory.getAlienShip() != null)
      {
          updateAlien();
      }
      if (!memory.getProjectiles().isEmpty())
      {
          updateProjectiles();
      }
      if (!memory.getBonusDrops().isEmpty())
      {
          updateBonusDrops();
      }
      if (!memory.getExplosions().isEmpty())
      {
          updateExplosions();
      }
      
  }
  
  /*
   * Shape definitions below using polygons.  Standard axis convention (does not follow GUI convention; converted later)
   */
  private static Polygon playerShape()
  {
      return new Polygon(new int[] {0,8,-8}, new int[] {10,-10,-10}, 3);
  }
  
  private static Polygon smallAsteroidShape()
  {
      return new Polygon(new int[] {-15,-7,4,19,7,2,-8},new int[] {4,12,20,3,-9,-11,-5},7);
  }
  
  private static Polygon mediumAsteroidShape()
  {
      return new Polygon(new int[] {-30,-15,-10,-5,10,21,32,25,10,-20},new int[] {2,20,25,31,29,13,4,-20,-25,-24},10);
  }
  
  private static Polygon largeAsteroidShape()
     {
         return new Polygon(new int[]{-50, -41, -33, -7, 19, 32, 46, 49, 64, 62, 47, 43, 36, -15, -14, -47, -50}, new int[]{53, 62, 65, 60, 63, 66, 57, 43, 39, 12, -8, -29, -45, -28, -15, 15, 42}, 17);
     }
  
  private static Polygon alienShape()
  {
      //TODO: give shape to alien
      return new Polygon(new int[] {-5,-5,5,5}, new int[] {5,-5,5,-5},4);
  }
  
  private static Polygon projectileShape()
  {
      //TODO: See if projectile shape OK
      return new Polygon( new int[] {-1,0,1,0}, new int[] {0,1,0,-1},4);
  }
  
  public static Polygon explosionShape()
  {
      return new Polygon(new int[] {-5,5}, new int[] {0,0}, 2);
  }
  
  private static Polygon bonusDropShape()
  {
      //TODO: Give bonus drop shape
      return new Polygon(new int[] {-3,-3,3,3}, new int[] {3,-3,3,-3},4);
  }
  
  private void updatePlayerShip()
  {
      MapObject player = memory.getPlayerShip();
      //move to appropriate position, rotate, set shape
      setPosition(playerShape(), player);
  }
  
  private void updateAlien()
  {
      AlienShip alien = memory.getAlienShip();
      //translate and rotate, set shape
      setPosition(alienShape(),alien);
  }
  
  private void updateProjectiles()
  {
      ArrayList<Projectile> projectileList = memory.getProjectiles();
      //for all projectiles
      for (Projectile aProjectile : projectileList)
      {
          setPosition(projectileShape(),aProjectile);
      }
  }
  
  private void updateExplosions()
  {
      ArrayList<MapObjectTTL> explosionList = memory.getExplosions();
      //for all explosions
      for (MapObjectTTL explosion : explosionList)
      {
          setPosition(explosionShape(),explosion);
      }
  }
  
  private void updateBonusDrops()
  {
      ArrayList<BonusDrop> bonusDropList = memory.getBonusDrops();
      //for all drops
      for (MapObject aBonusDrop : bonusDropList)
      {
          setPosition(bonusDropShape(),aBonusDrop);
      }
  }
  
  private void updateAsteroids()
  {
      ArrayList<Asteroid> asteroidList = memory.getAsteroids();
      //do for every asteroid in the game
      for (Asteroid anAsteroid : asteroidList)
      {
          //take care of different shapes for each size
          Polygon asteroidShape = smallAsteroidShape();
          if (anAsteroid.getSize() == Asteroid.LARGE_ASTEROID_SIZE)
          {
              asteroidShape = largeAsteroidShape();
          }
          else if (anAsteroid.getSize() == Asteroid.MEDIUM_ASTEROID_SIZE)
          {
              asteroidShape = mediumAsteroidShape();
          }
          setPosition(asteroidShape,anAsteroid);
      }
  }
  
  //performs rotation and translation on the shape of objects based on location and heading, and attaches the shape to the object in question once complete.
  private void setPosition(Polygon shape, MapObject gameobject)
  {
      AffineTransform transAndRot = new AffineTransform();
      
      //translate from std. math coordinate system used in shape definitons to proper location on map
      transAndRot.setToTranslation(gameobject.getX(), gameobject.getY());
      //rotate polygon by appropriate amount depending on polygon
      transAndRot.rotate(Math.toRadians(gameobject.getHeading()));
     
      //flip y axis (b/c of std. math convention used in shape definition)
      for (int i = 0 ; i < shape.ypoints.length ; i++)
      {
          shape.ypoints[i] = -shape.ypoints[i];
      }
      
      //do transformation, point by point
      for (int j = 0 ;j < shape.xpoints.length ; j++)
      {
          Point2D.Float origPoint = new Point2D.Float(shape.xpoints[j],shape.ypoints[j]);
          Point2D.Float transPoint = new Point2D.Float();
          transAndRot.transform(origPoint, transPoint);
         shape.xpoints[j] = (int) transPoint.x;
         shape.ypoints[j] = (int) transPoint.y;
      }
      gameobject.setShape(shape);
  }
 
     @Override
     public void run() {
         updateGraphics();
     }
 }
