 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package xtremerobotgames;
 
 import java.util.HashMap;
 import java.util.Random;
 import java.util.ArrayList;
 
 /**
  *
  * @author s102231
  */
 public class Board {
 
     private static int width = 10;
     private static int height = 10;
     public Tile[][] tiles;
     public Controller controller;
     public RobotCoord robots;
     private HashMap<Robot, Tile> home;
     private HashMap<Robot, Rotation> robotRotation;
     private HashMap<Tile, AbsoluteCoord> location;
     public boolean[][] warshall;
 
     //TO DO constructor
     public Board(){
         tiles = new Tile[width][height];
         warshall = new boolean[width*height][width*height];
         for(int i = 0; i < (width*height); i++){
             for(int j = 0; j < (width*height); j++){
                 warshall[i][j] = false;
             }
         }
         location = new HashMap<Tile, AbsoluteCoord>();
         for(int i = 0; i < width; i++){
             for(int j = 0; j < height; j++){
                 tiles[i][j] = new NormalTile();
                 location.put(tiles[i][j], new AbsoluteCoord(i,j));
             }
         }
         robots = new RobotCoord();
         home = new HashMap<Robot, Tile>();
         robotRotation = new HashMap<Robot, Rotation>();
         controller = new Controller();
     }
 
     public void addRobot(Robot r, AbsoluteCoord abs, AbsoluteCoord hometile, Rotation rot){
         tiles[abs.getX()][abs.getY()].occupier = r;
         robots.addRobot(r, abs);
         tiles[hometile.getX()][hometile.getY()] = new HomeTile(r);
         location.put(tiles[hometile.getX()][hometile.getY()], hometile);
         home.put(r, tiles[hometile.getX()][hometile.getY()]);
         robotRotation.put(r, rot);
     }
 
     public void addConveyorTile(AbsoluteCoord abs, Rotation rot){
         tiles[abs.getX()][abs.getY()] = new ConveyorTile(rot);
         location.put(tiles[abs.getX()][abs.getY()], abs);
     }
 
     public void addBrokenRobotTile(AbsoluteCoord abs){
         tiles[abs.getX()][abs.getY()] = new BrokenRobotTile();
         location.put(tiles[abs.getX()][abs.getY()], abs);
     }
 
     public void addHintTile(AbsoluteCoord abs){
         tiles[abs.getX()][abs.getY()] = new HintTile();
         location.put(tiles[abs.getX()][abs.getY()], abs);
     }
 
     public void exchangeTiles(AbsoluteCoord abs, AbsoluteCoord abs1){
         Random rand = new Random();
         Tile help = tiles[abs.getX()][abs.getY()];
         tiles[abs.getX()][abs.getY()] = tiles[abs1.getX()][abs1.getY()];
         tiles[abs1.getX()][abs1.getY()] = help;
         location.put(tiles[abs1.getX()][abs1.getY()], abs);
         location.put(tiles[abs.getX()][abs.getY()], abs1);
         if(tiles[abs.getX()][abs.getY()].occupier != null){
             saveLocation(abs, tiles[abs.getX()][abs.getY()].occupier);
             Robot r = tiles[abs.getX()][abs.getY()].occupier;
             int i = rand.nextInt(4);
             if(i == 0){
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R0DEG);
             } else if(i == 1){
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R90DEG);
             } else if(i == 2){
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R180DEG);
             } else {
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R270DEG);
             }
         }
         if(tiles[abs1.getX()][abs1.getY()].occupier != null){
             saveLocation(abs1, tiles[abs1.getX()][abs1.getY()].occupier);
             Robot r = tiles[abs1.getX()][abs1.getY()].occupier;
             int i = rand.nextInt(4);
             if(i == 0){
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R0DEG);
             } else if(i == 1){
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R90DEG);
             } else if(i == 2){
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R180DEG);
             } else {
                 robotRotation.remove(r);
                 robotRotation.put(r, Rotation.R270DEG);
             }
         }
         if(tiles[abs.getX()][abs.getY()].getClass() == ConveyorTile.class){
             ConveyorTile conv = (ConveyorTile) tiles[abs.getX()][abs.getY()];
             int i = rand.nextInt(4);
             if(i == 0){
                 conv.changeRot(Rotation.R0DEG);
             } else if(i == 1){
                 conv.changeRot(Rotation.R90DEG);
             } else if(i == 2){
                 conv.changeRot(Rotation.R180DEG);
             } else {
                 conv.changeRot(Rotation.R270DEG);
             }
             tiles[abs.getX()][abs.getY()] = conv;
 
         }
         if(tiles[abs1.getX()][abs1.getY()].getClass()== ConveyorTile.class){
             ConveyorTile conv = (ConveyorTile) tiles[abs1.getX()][abs1.getY()];
             int i = rand.nextInt(4);
             if(i == 0){
                 conv.changeRot(Rotation.R0DEG);
             } else if(i == 1){
                 conv.changeRot(Rotation.R90DEG);
             } else if(i == 2){
                 conv.changeRot(Rotation.R180DEG);
             } else {
                 conv.changeRot(Rotation.R270DEG);
             }
             tiles[abs1.getX()][abs1.getY()] = conv;
 
         }
 
     }
 
     public boolean checkBoard(){
         adjacentWarShall();
         for(Robot r: robots.robots){
             if( !checkReachable(robots.getAbsoluteCoord(r), home.get(r))){
                 return false;
             }
         }
         return true;
     }
 
     public boolean checkReachable(AbsoluteCoord abs, Tile t){
         Tile t1 = tiles[abs.getX()][abs.getY()];
         return warshall[mapping(t1)][mapping(t)];
     }
     
     public int mapping(Tile t){
         return (location.get(t).getX()*height) + location.get(t).getY();
     }
 
     public Tile reverseMapping(int i){
         int y = i % height;
         int x = (i - y) / height;
         return tiles[x][y];
     }
     
     public void adjacentWarShall(){
         for(int m = 0; m < (width*height); m++){
             ArrayList<Tile> adjacent = getAdjacent(reverseMapping(m));
             for(Tile t: adjacent){
                 warshall[m][mapping(t)] = true;
             }            
         }
         for(int k = 0; k < (width*height); k++){
             for(int i = 0; i < (width*height); i++){
                 for(int j = 0; j < (width*height); j++){
                     warshall[i][j] = warshall[i][j] || (warshall[i][k] && warshall[k][j]);
                 }
                 
             }
         }
     }
 
     public ArrayList<Tile> getAdjacent(Tile t){
         ArrayList<Tile> adjacent = new ArrayList<Tile>();
         AbsoluteCoord loc = location.get(t);
         if((loc.getX() - 1) > -1){
             if(tiles[loc.getX()-1][loc.getY()].getClass() == ConveyorTile.class){
                 adjacent.add(adjacentViaConveyor((ConveyorTile) tiles[loc.getX()-1][loc.getY()]));
             } else if(tiles[loc.getX()-1][loc.getY()].getClass() != BrokenRobotTile.class){
                 adjacent.add(tiles[loc.getX()-1][loc.getY()]);
             }
         }          
         if((loc.getY() - 1) > -1){
            if(tiles[loc.getY()][loc.getY()-1].getClass() == ConveyorTile.class){
                 adjacent.add(adjacentViaConveyor((ConveyorTile) tiles[loc.getX()][loc.getY()-1]));
             } else if(tiles[loc.getX()][loc.getY()-1].getClass() != BrokenRobotTile.class){
                 adjacent.add(tiles[loc.getX()][loc.getY()-1]);
             }
         }
         if((loc.getY() + 1) < height){
            if(tiles[loc.getY()][loc.getY()+1].getClass() == ConveyorTile.class){
                 adjacent.add(adjacentViaConveyor((ConveyorTile) tiles[loc.getX()][loc.getY()+1]));
             } else if(tiles[loc.getX()][loc.getY()+1].getClass() != BrokenRobotTile.class){
                 adjacent.add(tiles[loc.getX()][loc.getY()+1]);
             }
         }
         if((loc.getX() + 1) < width){
             if(tiles[loc.getX()+1][loc.getY()].getClass() == ConveyorTile.class){
                 adjacent.add(adjacentViaConveyor((ConveyorTile) tiles[loc.getX()+1][loc.getY()]));
             } else if(tiles[loc.getX()+1][loc.getY()].getClass() != BrokenRobotTile.class){
                 adjacent.add(tiles[loc.getX()+1][loc.getY()]);
             }
         }
         return adjacent;
     }
     
     public Tile adjacentViaConveyor(ConveyorTile t){
         AbsoluteCoord destination = addAbstoRel(location.get(t), t.getRelativeCoord());
         if(destination == null || tiles[destination.getX()][destination.getY()].getClass() == BrokenRobotTile.class || tiles[destination.getX()][destination.getY()].occupier != null){
             return t;
         } else {
             if(tiles[destination.getX()][destination.getY()].getClass() != ConveyorTile.class){
                 return tiles[destination.getX()][destination.getY()];
             } else {
                 Tile help = adjacentViaConveyor((ConveyorTile) tiles[destination.getX()][destination.getY()]);
                 return help; 
             }
             
         }
     }
 
     
     //might be a better way to solve this
     public boolean canReset(){
         for(int i = 0; i < width; i++){
             for(int j = 0; j < height; j++){
                 if(tiles[i][j].getClass() == HomeTile.class){
                     HomeTile ht = (HomeTile) tiles[i][j];
                     if(ht.homeRobot == ht.occupier){
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     //DONE for the move..
     public BoardResponse moveRequest(RelativeCoord loc, Robot r, Rotation rot){
        /**
 
         if(rot != Rotation.R0DEG){                  //rulecheck
             if(r.r.possibleRotations.contains(rot)){
                 robotRotation.remove(r);
                 robotRotation.put(r, rot);
                 return BoardResponse.SUCCESS;
             } else {
                 return BoardResponse.FAILED;
             }
         } else if(!r.r.possibleMoves.contains(loc)){    //rulecheck
             return BoardResponse.FAILED;
         } else {
         *
         */
 
             AbsoluteCoord position = addAbstoRel(robots.getAbsoluteCoord(r), loc);
             if(position == null){
                 return BoardResponse.FAILED;
             } else if (tiles[position.getX()][position.getY()].getClass() == HomeTile.class){
                 HomeTile ht = (HomeTile) tiles[position.getX()][position.getY()];
                     if(ht.homeRobot == r){
                         saveLocation(position ,r);
                         return BoardResponse.WIN;
                     } else {
                     saveLocation(position, r);
                     return BoardResponse.SUCCESS;
                     }
             } else {
                 /*
                  if(tiles[position.getX()][position.getY()].getClass() == ConveyorTile.class){
                     AbsoluteCoord nposition = conveyorMove(position, r);
                     if(nposition != position){
                         position = nposition;
                         r.notifyAutoMovement();
                     }
                 }
                  * 
                  */
                 saveLocation(position, r);
                 return BoardResponse.SUCCESS;
             }
         
     }
 
     //DONE
     public BoardSnapshot requestSnapshot(){
         Tile[][] tiles2 = new Tile[width][height];
         for(int i = 0; i < width; i++){
             for(int j = 0; j < height; j++){
                 tiles2[i][j] = tiles[i][j].clone();
             }
         }
         BoardSnapshot snapshot = new BoardSnapshot(tiles2);
         return snapshot;
     }
 
     //should be done
     public RobotPair requestTilesExchange(){
         TilePair switchTiles = getValidTiles();
         AbsoluteCoord coord1 = null;
         AbsoluteCoord coord2 = null;
 
         //get coords of the tiles
         for(int i = 0; i < width; i++){
             for(int j = 0; j < height; j++){
                 if(tiles[i][j].equals(switchTiles.tile1)){
                     coord1 = new AbsoluteCoord(i,j);
                 }
                 if(tiles[i][j].equals(switchTiles.tile2)){
                     coord2 = new AbsoluteCoord(i,j);
                 }
             }
         }
 
         //switch the tiles
         tiles[coord1.getX()][coord1.getY()] = switchTiles.tile2;
         tiles[coord2.getX()][coord2.getY()] = switchTiles.tile1;
 
         //calculate new positions
         if (switchTiles.tile1.occupier != null){
             saveLocation(calculateLocation(coord2, switchTiles.tile1.occupier), switchTiles.tile1.occupier);
             controller.notifyAutoMovement(switchTiles.tile1.occupier);                  //always notify of automovement
         } else {
             checkAround(coord2);    //check if conveyortiles pointing towards this tile might move, since it is (now) empty
         }
         if (switchTiles.tile2.occupier != null){
             saveLocation(calculateLocation(coord1, switchTiles.tile2.occupier), switchTiles.tile2.occupier);
             controller.notifyAutoMovement(switchTiles.tile2.occupier);
         } else {
            checkAround(coord1);  
         }
 
 
 
         return null;
     }
 
     //get new position of robot
     public AbsoluteCoord calculateLocation(AbsoluteCoord absCoord, Robot r){
         if(tiles[absCoord.getX()][absCoord.getY()].getClass() == ConveyorTile.class ){
             return conveyorMove(absCoord, r);
         } else {
             return absCoord;
         }
     }
 
     //pre: robot is on a conveyorBelt
     public AbsoluteCoord conveyorMove(AbsoluteCoord absCoord, Robot r){
         AbsoluteCoord destination;
         ConveyorTile ct = (ConveyorTile) tiles[absCoord.getX()][absCoord.getY()];
         destination = addAbstoRel(absCoord, ct.getRelativeCoord());
         //if he cant move, do not move, else do move and check environment
         if(destination == null || tiles[destination.getX()][destination.getY()].getClass() == BrokenRobotTile.class || tiles[destination.getX()][destination.getY()].occupier != null){
             return absCoord;
         } else {
             destination = conveyorMove(destination, r);
             saveLocation(destination, r);
             controller.viewer.notifyStateChange();
             checkAround(absCoord);
             return destination;
         }
     }
 
     //check if conveyor tile is there and it is possible to move
     public void checkConveyor(AbsoluteCoord absCoord){
         AbsoluteCoord destination;
         if(tiles[absCoord.getX()][absCoord.getY()].getClass() == ConveyorTile.class && tiles[absCoord.getX()][absCoord.getY()].occupier != null && absCoord != null){
             Robot r = robots.getRobot(absCoord);
             destination = conveyorMove(absCoord, r);
             if(destination != absCoord){
                 controller.notifyAutoMovement(r);
                 saveLocation(destination, r);
             }
         }
     }
 
     public void checkAround(AbsoluteCoord absCoord){
         RelativeCoord checkConveyor = new RelativeCoord(0, -1);
         checkConveyor(addAbstoRel(absCoord, checkConveyor));
         checkConveyor = new RelativeCoord(0,1);
         checkConveyor(addAbstoRel(absCoord, checkConveyor));
         checkConveyor = new RelativeCoord(-1,0);
         checkConveyor(addAbstoRel(absCoord, checkConveyor));
         checkConveyor = new RelativeCoord(1,0);
         checkConveyor(addAbstoRel(absCoord, checkConveyor));
     }
 
     //need to take rotation of robot in account....
     public Hint getHint(Robot r){
         Hint hint;
         AbsoluteCoord coordRobot;
         AbsoluteCoord coordHome = null;
         coordRobot = robots.getAbsoluteCoord(r);
         ArrayList<Hint> hints = new ArrayList<Hint>();
 
         //check where the hometile lies
         for(int i = 0; i < width; i++){
             for(int j = 0; j < height; j++){
                 if( tiles[i][j].equals(home.get(r))){
                     coordHome = new AbsoluteCoord(i,j);
                     break;
                 }
             }
         }
 
         //determine hint, robot to the right of hometile, robot.x > hometile.x
         if(coordRobot.getX() > coordHome.getX()){
             if(coordRobot.getY() == coordHome.getY()){
                 hint = Hint.WEST;
             } else if(coordRobot.getY() < coordHome.getY()){
                 hints.add(Hint.WEST);
                 hints.add(Hint.SOUTH);
                 hints.add(Hint.SOUTH_WEST);
                 hint = pickHint(hints);
             } else {
                 hints.add(Hint.WEST);
                 hints.add(Hint.NORTH);
                 hints.add(Hint.NORTH_WEST);
                 hint = pickHint(hints);
             }
 
         //determine hint, robot to the left of hometile, robot.x < hometile.x
         } else if(coordRobot.getX() < coordHome.getX()){
             if(coordRobot.getY() == coordHome.getY()){
                 hint = Hint.EAST;
             } else if(coordRobot.getY() < coordHome.getY()){
                 hints.add(Hint.EAST);
                 hints.add(Hint.SOUTH);
                 hints.add(Hint.SOUTH_EAST);
                 hint = pickHint(hints);
             } else {
                 hints.add(Hint.EAST);
                 hints.add(Hint.NORTH);
                 hints.add(Hint.NORTH_EAST);
                 hint = pickHint(hints);
             }
 
         //robot north / south of hometile, same x
         } else {
             if(coordRobot.getY() > coordHome.getY()){
                 hint = Hint.SOUTH;
             } else {
                 hint = Hint.NORTH;
             }
         }
         return calculateHint(hint, robotRotation.get(r));
     }
 
     //sub function to randomly pick a hint from a list
     private Hint pickHint(ArrayList<Hint> hints){
         int pick = new Random().nextInt(hints.size());
         return hints.get(pick);
     }
 
     private Hint calculateHint(Hint hint, Rotation r){
         int hintnumber;
         if(r == Rotation.R0DEG){
             hintnumber = 0;
         } else if (r == Rotation.R90DEG){
             hintnumber = 3;
         } else if (r == Rotation.R180DEG){
             hintnumber = 2;
         } else{
             hintnumber = 1;
         }
 
         if(getInt(hint) > 3){
             return getHint(((getInt(hint) + hintnumber) % 4) + 4);
         } else {
             return getHint(getInt(hint) + hintnumber % 4);
         }
 
 
     }
 
     //maping from ints to hints
     public Hint getHint(int i){
         if(i==0){
             return Hint.NORTH;
         } else if(i == 1){
             return Hint.EAST;
         } else if(i == 2){
             return Hint.SOUTH;
         } else if(i == 3){
             return Hint.WEST;
         } else if(i == 4){
             return Hint.NORTH_EAST;
         } else if(i == 5){
             return Hint.SOUTH_EAST;
         } else if(i == 6){
             return Hint.SOUTH_WEST;
         } else if(i == 7){
             return Hint.NORTH_WEST;
         } else {
             return null;
         }
     }
 
     //mapping from hints to ints
     public int getInt(Hint h){
         if(h == Hint.NORTH){
             return 0;
         } else if(h == Hint.EAST){
             return 1;
         } else if(h == Hint.SOUTH){
             return 2;
         } else if(h == Hint.WEST){
             return 3;
         } else if(h == Hint.NORTH_EAST){
             return 4;
         } else if(h == Hint.SOUTH_EAST){
             return 5;
         } else if(h == Hint.SOUTH_WEST){
             return 6;
         } else if(h == Hint.NORTH_WEST){
             return 7;
         } else {
             return -1;
         }
     }
 
     //TO DO
     private AbsoluteCoord calculateNewLocation(RelativeCoord loc, Robot r){
         AbsoluteCoord position = robots.getAbsoluteCoord(r);
         AbsoluteCoord destination = addAbstoRel(position, loc);
         int x = destination.getX() - position.getX();
         int y = destination.getY() - position.getY();
         if( destination == null){
             return null;
         } else {
            if(x > 0 || y > 0){ //otherwise smaller than 0 for sure
                if(x == 0){
                    for(int i = 0; i <= y; i++){
                        if(tiles[position.getX()][(position.getY()+i)].getClass() == BrokenRobotTile.class){
                            return null;
                        } else if(tiles[position.getX()][(position.getY()+i)].getClass() == ConveyorTile.class){
                            RelativeCoord help = new RelativeCoord(0,i);
                            return addAbstoRel(position, help);
                        }
                    }
                } else {
                    for(int i = 0; i <= x; i++){
                        if(tiles[(position.getX()+i)][position.getY()].getClass() == BrokenRobotTile.class){
                            return null;
                        } else if(tiles[position.getX()+i][position.getY()].getClass() == ConveyorTile.class){
                            RelativeCoord help = new RelativeCoord(i,0);
                            return addAbstoRel(position, help);
                        }
                    }
                }
            } else {
                if(x == 0){
                    for(int i = 0; i >= y; i--){
                        if(tiles[position.getX()][(position.getY()+i)].getClass() == BrokenRobotTile.class){
                            return null;
                        } else if(tiles[position.getX()][(position.getY()+i)].getClass() == ConveyorTile.class){
                            RelativeCoord help = new RelativeCoord(0,i);
                            return addAbstoRel(position, help);
                        }
                    }
                } else {
                    for(int i = 0; i >= x; i--){
                        if(tiles[(position.getX()+i)][position.getY()].getClass() == BrokenRobotTile.class){
                            return null;
                        } else if(tiles[position.getX()+i][position.getY()].getClass() == ConveyorTile.class){
                            RelativeCoord help = new RelativeCoord(i,0);
                            return addAbstoRel(position, help);
                        }
                    }
                }
            }
         }
         return null; // just to be sure
     }
 
     //TO DO
     private TilePair getValidTiles(){
         return null;
     }
 
     //TO DO
     private void reset(){
         if(canReset()){
            //reset.
         }
 
     }
 
 
     //function to save robot location, done
     private void saveLocation(AbsoluteCoord abs, Robot r){
         AbsoluteCoord coord = robots.getAbsoluteCoord(r);
         tiles[coord.getX()][coord.getY()].occupier = null;
         tiles[abs.getX()][abs.getY()].occupier = r;
         robots.changePosition(r, abs);
         //System.out.println("Savend");
     }
 
     //function to add relative to absolute Coordinate, returns null if absCoord is not on the board
     private AbsoluteCoord addAbstoRel(AbsoluteCoord abs, RelativeCoord rel){
         AbsoluteCoord absCoord = new AbsoluteCoord(abs.getX() + rel.getX(), abs.getY() + rel.getY());
         if(absCoord.getX() >= width || absCoord.getY() >= height || absCoord.getX() < 0 || absCoord.getY() < 0){
             return null;
         } else {
             return absCoord;
         }
     }
 
     public Rotation getRotation(Robot r){
         return this.robotRotation.get(r);
     }
 }
