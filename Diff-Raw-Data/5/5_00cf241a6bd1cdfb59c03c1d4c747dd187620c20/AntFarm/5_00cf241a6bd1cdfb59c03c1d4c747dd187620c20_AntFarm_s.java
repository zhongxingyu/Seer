 package org.linesofcode.antfarm;
 
 import controlP5.Slider;
 import org.linesofcode.antfarm.exception.OutOfBoundsException;
 import org.linesofcode.antfarm.exception.PathIsBlockedException;
 import org.linesofcode.antfarm.sceneObjects.Ant;
 import org.linesofcode.antfarm.sceneObjects.BoundingBox;
 import org.linesofcode.antfarm.sceneObjects.Food;
 import org.linesofcode.antfarm.sceneObjects.Hive;
 import org.linesofcode.antfarm.sceneObjects.Overlay;
 import org.linesofcode.antfarm.sceneObjects.SceneObject;
 import processing.core.PApplet;
 import processing.core.PVector;
 
 import java.awt.Color;
 import java.util.HashSet;
 import java.util.Set;
 
 @SuppressWarnings("serial")
 public class AntFarm extends PApplet {
 
     private static final int[] HIVE_COLORS = {
             Color.BLUE.getRGB(),
             Color.RED.getRGB(),
 //            Color.YELLOW.getRGB(),
 //            Color.PINK.getRGB(),
 //            Color.MAGENTA.getRGB(),
 //            new Color(148, 56, 161).getRGB(),
 //            Color.WHITE.getRGB()
     };
     public static final float MIN_STATIC_SPAWN_DISTANCE = 150;
     public static final float BORDER_SPANW_DISTANCE = 10;
     public static final int FOOD_COUNT = 2;
 
    public static float timeLapse = 4f;
 
     private final Set<SceneObject> staticSceneObjects = new HashSet<SceneObject>(1000);
     private final Set<Ant> ants = new HashSet<Ant>(1000);
 
     private final Set<SceneObject> removeObjects = new HashSet<SceneObject>();
     private final Set<SceneObject> addObjects = new HashSet<SceneObject>();
 
     private Overlay overlay;
 
     private Slider speed;
 
 	private boolean drawViewDirection = false;
 	private int currentFoodCount = 0;
 
     @Override
     public void setup() {
         size(600, 400);
 
         for (final int HIVE_COLOR : HIVE_COLORS) {
             staticSceneObjects.add(new Hive(this, HIVE_COLOR));
         }
         for(int i=0; i<FOOD_COUNT; i++) {
         	staticSceneObjects.add(new Food(this));
         	currentFoodCount++;
         }
         overlay = new Overlay(this);
     }
 
     @Override
     public void draw() {
         background(Color.LIGHT_GRAY.getRGB());
 
         update(1 / frameRate);
         addAndRemoveSceneObjects();
 
         for (final SceneObject sceneObject: staticSceneObjects) {
             sceneObject.draw();
         }
         for (final Ant ant: ants) {
             ant.draw();
         }
         overlay.draw();
     }
 
     private void update(float delta) {
     	
         overlay.update(delta);
         
         delta *= timeLapse;
 
         for (final SceneObject sceneObject: staticSceneObjects) {
             if (removeObjects.contains(sceneObject)) {
                 continue;
             }
             sceneObject.update(delta);
         }
 
         for (final Ant ant: ants) {
             if (removeObjects.contains(ant)) {
                 continue;
             }
             ant.update(delta);
         }
     }
 
     private void addAndRemoveSceneObjects() {
         for (final SceneObject sceneObject: removeObjects) {
             if (sceneObject instanceof Ant) {
                 ants.remove(sceneObject);
             } else {
                 staticSceneObjects.remove(sceneObject);
             }
         }
 
         for (final SceneObject sceneObject: addObjects) {
             if (sceneObject instanceof Ant) {
                 ants.add((Ant) sceneObject);
             } else {
                 staticSceneObjects.add(sceneObject);
             }
         }
         removeObjects.clear();
         addObjects.clear();
     }
 
     public void spawnAnt(final Hive hive) {
     	addObjects.add(new Ant(this, hive));
     }
 
     public void removeAnt(final Ant ant) {
         removeObjects.add(ant);
     }
 
     private void spawnFood() {
         addObjects.add(new Food(this));
         currentFoodCount++;
     }
 
     public void removeFood(final Food food) {
         removeObjects.add(food);
         currentFoodCount--;
         if(currentFoodCount < FOOD_COUNT) {
         	spawnFood();
         }
     }
 
     public void removeHive(final Hive hive) {
         for (final Ant ant: ants) {
             if (ant.getHive() == hive) {
                 removeAnt(ant);
             }
         }
        removeObjects.remove(hive);
     }
 
 	public boolean isDrawViewDirectionEnabled() {
 		return drawViewDirection;
 	}
 
     public boolean isPathBlocked(final Ant me, final PVector translation) {
         return false;
     }
 
     public Ant hitsEnemyAnt(final Ant me) {
         final BoundingBox myBox = me.getBoundingBox();
         if (myBox == null) {
             return null;
         }
 
         final int team = me.getHive().getColor();
 
         for (final Ant ant : ants) {
             if (ant == me) {
                 continue;
             }
 
             if (ant.getHive().getColor() == team) {
                 continue; // not an enemy
             }
 
             if (ant.getBoundingBox() == null) {
                 continue;
             }
 
             if (ant.getBoundingBox().intersects(myBox)) {
                 return ant;
             }
         }
         return null;
     }
 
     public void moveAnt(final Ant ant, final PVector newPosition) throws OutOfBoundsException, PathIsBlockedException {
        
     	assertAntInBounds(newPosition);
 
         final Ant enemy = hitsEnemyAnt(ant);
         if (enemy != null) {
             antFight(ant, enemy);
         }
 
     	if (isPathBlocked(ant, newPosition)) {
             throw new PathIsBlockedException();
         }
 
         ant.setPosition(newPosition);
     }
 
     public void antFight(final Ant me, final Ant enemy) {
         if (random(1) < .5f) {
             enemy.die();
         } else {
             me.die();
         }
     }
 
     private void assertAntInBounds(PVector newPosition) throws OutOfBoundsException {
     	if((newPosition.x + Ant.SIZE) > width || (newPosition.x - Ant.SIZE) < 0) {
     		throw new OutOfBoundsException(OutOfBoundsException.Direction.X_AXIS);
     	}
     	if((newPosition.y + Ant.SIZE) > height || (newPosition.y - Ant.SIZE) < 0) {
     		throw new OutOfBoundsException(OutOfBoundsException.Direction.Y_AXIS);
     	}
 	}
 
 	public PVector calcStaticSpawnPosition(final SceneObject me, float size) {
         final PVector position = new PVector();
         while (true) {
             position.x = random(BORDER_SPANW_DISTANCE, width - size - BORDER_SPANW_DISTANCE);
             position.y = random(BORDER_SPANW_DISTANCE, height - size - BORDER_SPANW_DISTANCE);
             boolean correct = true;
             for (final SceneObject object: staticSceneObjects) {
                 final PVector objectPosition;
                 if (object instanceof Hive) {
                     objectPosition = ((Hive) object).getPosition();
                 } else if (object instanceof Food) {
                     objectPosition = ((Food) object).getPosition();
                 } else {
                     continue;
                 }
 
                 if (Math.abs(PVector.dist(position, objectPosition)) < MIN_STATIC_SPAWN_DISTANCE) {
                     correct = false;
                     break;
                 }
             }
             if (correct) {
                 break;
             }
         }
         return position;
     }
 
 	public Food getFoodInProximity(Ant ant) {
 		for(Object o : staticSceneObjects) {
 			if(o instanceof Food) {
 				Food food = (Food)o;
 				double distance = Math.abs(PVector.dist(ant.getPosition(), food.getPosition()));
 				distance -= food.getRelativeSize();
 				if(distance < 30.0) {
 					return food;
 				}
 			}
 		}
 		return null;
 	}
 
     public void putPheromone(final Ant me) {
         // FIXME
     }
 
     public PVector getClosePheromoneTrail(final Ant me) {
         throw new UnsupportedOperationException();
     }
 
 }
