 package fr.utbm.vi51.agent;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.janusproject.kernel.status.Status;
 import org.janusproject.kernel.status.StatusFactory;
 
 import fr.utbm.vi51.configs.Consts;
 import fr.utbm.vi51.environment.Direction;
 import fr.utbm.vi51.environment.DropFood;
 import fr.utbm.vi51.environment.EatFood;
 import fr.utbm.vi51.environment.Food;
 import fr.utbm.vi51.environment.InsectBody;
 import fr.utbm.vi51.environment.Message;
 import fr.utbm.vi51.environment.MobileObject;
 import fr.utbm.vi51.environment.Move;
 import fr.utbm.vi51.environment.Pheromone;
 import fr.utbm.vi51.environment.Side;
 import fr.utbm.vi51.environment.Square;
 import fr.utbm.vi51.environment.TakeFood;
 import fr.utbm.vi51.environment.WorldObject;
 import fr.utbm.vi51.util.PathFinder;
 import fr.utbm.vi51.util.Point3D;
 
 /**
  * @author Top-K
  *   
  */
 enum WorkerBehaviour {
     GO_HOME, SEARCH_FOOD,
 }
 
 /**
  * @author Top-K
  * 
  */
 public class Worker extends Ant {
     private WorkerBehaviour currentBehaviour;
     private Point3D lastPosition;
     private Point3D relativeStartingPointPosition; // Remembers the position of
                                                    // the starting point (where
                                                    // food was taken, where home
                                                    // was...)
     private Logger log = Logger.getLogger(MobileObject.class.getName());
 
     public Worker(Point3D position, int speed, Side side) {
         super("img/Ants/worker.png", position, speed, side);
         currentBehaviour = WorkerBehaviour.GO_HOME;
         lastPosition = new Point3D(position);
     }
 
     @Override
     public Status activate(Object... params) {
         return StatusFactory.ok(this);
     }
 
     @Override
     public Status live() {
         super.live();
         InsectBody body = this.getBody();
         if(body != null){
             System.out.println(body.getHunger());
         }
         
         //If their is no body, the agent is waiting to die
         if(body == null) {
             System.out.println("dying");
             return null;
         }
         
         // If an action is already planned, wait for it to be resolved
         if (body.getAction() != null) {
             return null;
         }
 
         // Worker will only act every ANTACTIONDELAY milliseconds
         if (this.getTimeManager().getCurrentDate().getTime() - lastTime < Consts.ANTACTIONDELAY
                 && lastTime != 0) {
             return null;
         }
 
         if (relativeStartingPointPosition != null) {
             relativeStartingPointPosition.x -= body.getPosition().x
                     - lastPosition.x;
             relativeStartingPointPosition.y -= body.getPosition().y
                     - lastPosition.y;
         }
         lastPosition = new Point3D(body.getPosition());
 
         currentPerception = body.getPerception();
 
         if (dropPheromoneIfNeeded()) {
             return null;
         }
 
         if (movementPath != null && !movementPath.isEmpty()) {
             lastTime = this.getTimeManager().getCurrentDate().getTime();
             Move m = new Move(body, movementPath.removeFirst());
             body.setAction(m);
             return null;
         }
         
         if(body.isHungry()) {
             currentBehaviour = WorkerBehaviour.GO_HOME;
         }
 
         switch (currentBehaviour) {
         case GO_HOME:
             //System.out.println("I go home");
             goHome();
             break;
         case SEARCH_FOOD:
             //System.out.println("I look for food");
             searchFood();
             
             break;
         default:
             break;
         }
 
         /*
          * If after behavior functions, no action has been defined and movement
          * path is empty, the worker doesn't know what to do, so move randomly .
          */
         if (body.getAction() == null
                 && (movementPath == null || movementPath.isEmpty())) {
             Square[][][] perceivedMap = currentPerception.getPerceivedMap();
             // Find a crossable square
             int x;
             int y;
             do {
                 x = (int) Math.floor(Math.random() * perceivedMap.length);
                 y = (int) Math.floor(Math.random() * perceivedMap[0].length);
             } while (!perceivedMap[x][y][0].getLandType().isCrossable());
             movementPath = PathFinder.findPath(currentPerception
                     .getPositionInPerceivedMap(), new Point3D(x, y, 0),
                     perceivedMap);
         }
         return null;
 
     }
 
     /**
      * Drops a pheromone if the closest pheromone with a strenght/maxStrength. >
      * 0.5 is at a euclidian distance > 2
      * 
      * @return true if a pheromone will be dropped, false else.
      */
     private boolean dropPheromoneIfNeeded() {
 
         // Variables for a pheromone validity
         final float acceptedOldPh = 0.5f;
         final int acceptedDistancePh = 2;
 
         // If we are looking for home and the body is not carrying food, no need
         // to create pheromone
         if (currentBehaviour == WorkerBehaviour.GO_HOME
                 && this.getBody().getCarriedObject() == null) {
             return false;
         }
 
         Square[][][] perceivedMap = currentPerception.getPerceivedMap();
         // Represents the targets position, the nature of the target depends on
         // the current behaviour (food for search food...)
         Point3D targetPosition = null;
         for (int i = 0; i < perceivedMap.length; ++i) {
             for (int j = 0; j < perceivedMap[0].length; ++j) {
                 for (WorldObject wo : perceivedMap[i][j][0].getObjects()) {
                     if (currentBehaviour == WorkerBehaviour.GO_HOME
                             && wo instanceof Food
                             || currentBehaviour == WorkerBehaviour.SEARCH_FOOD
                             && wo.getTexturePath().equals("img/Ants/queen.png") && ((InsectBody) wo).getSide() == this.getBody().getSide()) {
                         targetPosition = new Point3D(wo.getPosition());
                     }
                     if (wo instanceof Pheromone) {
                         Pheromone p = (Pheromone) wo;
                         // Check validity of the pheromone : strength is
                         // sufficient and is of correct type
                         if (p.getStrength() / Consts.STARTINGPHEROMONEVALUE > acceptedOldPh
                                 && ((currentBehaviour == WorkerBehaviour.GO_HOME && p
                                         .getMessage() == Message.FOOD)
                                 || (currentBehaviour == WorkerBehaviour.SEARCH_FOOD && p
                                         .getMessage() == Message.HOME))) {
                             // If the pheromone is valid and close enough to the
                             // body's position, no need to create one
                             if (Point3D.euclidianDistance(p.getPosition(), this
                                     .getBody().getPosition()) <= acceptedDistancePh) {
                                 return false;
                             }
                         }
                     }
                 }
             }
         }
 
         // If no close and valid pheromone has been found, create one
         Message m;
         // System.out.println("nothingfound, working on it" + currentBehaviour);
         switch (currentBehaviour) {
         case GO_HOME:
             m = Message.FOOD;
             break;
         case SEARCH_FOOD:
             m = Message.HOME;
             break;
         default:
             m = null;
         }
         assert m != null;
 
         // If the target position is visible, place a pheromone pointing to it.
         // Else, point the pheromone to the position of the insect a few moves
         // ago.
         // System.out.println(relativeStartingPointPosition);
         if (targetPosition != null) {
             new Pheromone(this.getBody().getPosition(), m,
                     Direction.toDirection(this.getBody().getPosition(),
                             targetPosition),
                     (int) Consts.STARTINGPHEROMONEVALUE, this.getBody().getSide());
             return true;
         } else if (relativeStartingPointPosition != null) {
             new Pheromone(this.getBody().getPosition(), m,
                     Direction.toDirection(new Point3D(0, 0, 0),
                             relativeStartingPointPosition),
                     (int) Consts.STARTINGPHEROMONEVALUE, this.getBody().getSide());
             return true;
         }
         return false;
 
     }
 
     private void searchFood() {
         System.out.println("I look for food");
         Square[][][] perceivedMap = currentPerception.getPerceivedMap();
         Point3D positionInPerceivedMap = currentPerception
                 .getPositionInPerceivedMap();
         Food foodFound = null;
         Pheromone currentBestPheromone = null;
         Point3D currentBestPheromonePositionInPerceivedMap = null;
 
         // If the body is already carrying an object, go home
         if (this.getBody().getCarriedObject() != null) {
             currentBehaviour = WorkerBehaviour.GO_HOME;
             goHome();
             return;
         }
 
         // If there is food on the same square as the body, take it
         boolean foodOnSameSquare = false;
         for (WorldObject wo : perceivedMap[positionInPerceivedMap.x][positionInPerceivedMap.y][0]
                 .getObjects()) {
             if (wo.getTexturePath().equals("img/Ants/queen.png") && ((InsectBody) wo).getSide() == this.getBody().getSide()) {
                 foodOnSameSquare = false;
                 break;
             }
             if (wo instanceof Food) {
                 foodOnSameSquare = true;
             }
         }
         if (foodOnSameSquare) {
             this.getBody().setAction(new TakeFood(this.getBody()));
             currentBehaviour = WorkerBehaviour.GO_HOME;
             relativeStartingPointPosition = new Point3D(0, 0, 0);
             return;
         }
         Point3D currentBestFoodPheromonePositionInPerceivedMap = null;
         Pheromone currentBestFoodPheromone = null;
 
         // Searching the map for food or food pheromones
         for (int i = 0; i < perceivedMap.length; ++i) {
             for (int j = 0; j < perceivedMap[0].length; ++j) {
                 for (WorldObject wo : perceivedMap[i][j][0].getObjects()) {
                     // Avoid perceiving the food on the queen's square as it is
                     // already where it needs to be
                     if (wo.getTexturePath().equals("img/Ants/queen.png") && ((InsectBody) wo).getSide() == this.getBody().getSide()) {
                         foodFound = null;
                         break;
                     }
                     if (foodFound == null && wo instanceof Food) {
                         foodFound = (Food) wo;
                     } else if (wo instanceof Pheromone) {
                         Pheromone p = (Pheromone) wo;
                         if (p.getMessage() == Message.FOOD) {
                             currentBestPheromone = Pheromone.closestToSubject(
                                     p, currentBestPheromone);
                             if (currentBestPheromone == p && p.getSide() == this.getBody().getSide()) {
                                 currentBestPheromonePositionInPerceivedMap = new Point3D(
                                         i, j, 0);
                             }
                         } else if (p.getMessage() == Message.HOME){
                             currentBestFoodPheromone = Pheromone.closestToSubject(
                                     p, currentBestPheromone);
                             if (currentBestFoodPheromone == p && p.getSide() == this.getBody().getSide()) {
                                 currentBestFoodPheromonePositionInPerceivedMap = new Point3D(
                                         i, j, 0);
                             }
                         } else {
                             System.out.println("ceci n'est pas une phéromone");
                         }
                     }
                 }
                 if (foodFound != null) {
                     movementPath = PathFinder.findPath(
                             currentPerception.getPositionInPerceivedMap(),
                             new Point3D(i, j, 0), perceivedMap);
                     return;
                 }
             }
         }
         if (currentBestPheromone == null && currentBestFoodPheromone != null){
             currentBestPheromone = currentBestFoodPheromone;
             currentBestPheromonePositionInPerceivedMap = currentBestFoodPheromonePositionInPerceivedMap;
         }
         
         if (currentBestPheromone != null && currentBestPheromonePositionInPerceivedMap != null && currentBestPheromone.getMessage() == Message.FOOD) {
             movementPath = PathFinder.findPath(
                     currentPerception.getPositionInPerceivedMap(),
                     currentBestPheromonePositionInPerceivedMap, perceivedMap);
         } else if (currentBestPheromone != null && currentBestPheromone.getMessage() == Message.HOME){
             //Moves to anywhere but the pheromone direction
             int randChoice;
             int x = 0;
             int y = 0;
             do {
                 randChoice = (int) Math.floor(Math.random() *  3);
                 int diff = 0;
                 //diff = (int) Math.floor(Math.random()*4)-2;
                 if (randChoice == 0){
                     x = currentBestPheromonePositionInPerceivedMap.x * currentBestPheromone.getDirection().opposite().dx;
                     y = currentBestPheromonePositionInPerceivedMap.y * currentBestPheromone.getDirection().opposite().dy;
                 } else if (randChoice == 1){
                     x = currentBestPheromonePositionInPerceivedMap.x * currentBestPheromone.getDirection().dx;
                     y = currentBestPheromonePositionInPerceivedMap.y * currentBestPheromone.getDirection().opposite().dy;
                 } else if (randChoice == 2){
                     x = currentBestPheromonePositionInPerceivedMap.x * currentBestPheromone.getDirection().opposite().dx;
                     y = currentBestPheromonePositionInPerceivedMap.y * currentBestPheromone.getDirection().dy;
                 }
                 //x += diff;
                 //y += diff;
                 if(x < 0){
                     x = 0 + (Math.abs(diff));
                 } else if (x > Consts.WINWIDTH){
                     x = Consts.WINWIDTH - (Math.abs(diff));
                 }
                 if(y < 0){
                     y = 0 + (Math.abs(diff));
                 } else if (y > Consts.WINHEIGHT){
                     y = Consts.WINHEIGHT -(Math.abs(diff));
                 }
                 
             } while (!perceivedMap[x][y][0].getLandType().isCrossable());
             movementPath = PathFinder.findPath(currentPerception
                     .getPositionInPerceivedMap(), new Point3D(x, y, 0),
                     perceivedMap);
             if(currentBestPheromonePositionInPerceivedMap != null){
             movementPath = PathFinder.findPath(
                 currentPerception.getPositionInPerceivedMap(),
                 currentBestPheromonePositionInPerceivedMap, perceivedMap);
             }
         }
 
     }
 
     private void goHome() {
         System.out.println("I'm going home motherfucker");
         Square[][][] perceivedMap = currentPerception.getPerceivedMap();
         Pheromone currentBestPheromone = null;
         Point3D currentBestPheromonePositionInPerceivedMap = null;
 
         // If the body is carrying food and we are on the queen's square, drop
         // the food
         // If the body is hungry and there is food on the same square, eat it 
         for (WorldObject wo : perceivedMap[currentPerception
                 .getPositionInPerceivedMap().x][currentPerception
                 .getPositionInPerceivedMap().y][0].getObjects()) {
             if(this.getBody().isHungry() && wo instanceof Food) {
                 this.getBody().setAction(new EatFood(this.getBody()));
                 return;
             }
             if (wo.getTexturePath().equals("img/Ants/queen.png") && ((InsectBody) wo).getSide() == this.getBody().getSide()) {
                 if (this.getBody().getCarriedObject() != null) {
                     this.getBody().setAction(new DropFood(this.getBody()));
                 }
                 currentBehaviour = WorkerBehaviour.SEARCH_FOOD;
                 relativeStartingPointPosition = new Point3D(0, 0, 0);
                 return;
             }
         }
 
         // Look for the queen, for home pheromones, or if hungry for food
         for (int i = 0; i < perceivedMap.length; ++i) {
             for (int j = 0; j < perceivedMap[0].length; ++j) {
                 List<WorldObject> objects = perceivedMap[i][j][0].getObjects();
                 synchronized (objects) {
                     for (WorldObject wo : objects) {
                         if (wo.getTexturePath().equals("img/Ants/queen.png") && ((InsectBody) wo).getSide() == this.getBody().getSide()) {
                             movementPath = PathFinder.findPath(
                                     currentPerception
                                             .getPositionInPerceivedMap(),
                                     new Point3D(i, j, 0), perceivedMap);
                             return;
                         } else if (wo instanceof Pheromone) {
                             Pheromone p = (Pheromone) wo;
                            if (p.getMessage() == Message.HOME) {
                                 currentBestPheromone = Pheromone
                                         .closestToSubject(p,
                                                 currentBestPheromone);
                                if (currentBestPheromone == p && p.getSide() == this.getBody().getSide()) {
                                     currentBestPheromonePositionInPerceivedMap = new Point3D(
                                             i, j, 0);
                                 }
                             }
                         } else if (this.getBody().isHungry() && wo instanceof Food) {
                             movementPath = PathFinder.findPath(
                                     currentPerception
                                             .getPositionInPerceivedMap(),
                                     new Point3D(i, j, 0), perceivedMap);
                             return;
                         }
                     }
                 }
             }
         }
         if (currentBestPheromone != null && currentBestPheromonePositionInPerceivedMap != null) {
             movementPath = PathFinder.findPath(//TODO c'était ici que currentBestPheromoneblbla était null
                     currentPerception.getPositionInPerceivedMap(),
                     currentBestPheromonePositionInPerceivedMap, perceivedMap);
         }
     }
 }
