 package current.behaviors;
 
 import current.*;
 import battlecode.common.*;
 
 
 //FortifyBehavior notes:
 //- We will probably eventually want to add some way of
 //moving through our own fortification barriers
 //- Some way of communicating breach?
 //- Minesweep neutrals first? Minesweep behavior?
 
//CURRENT ISSUES: - Get stuck in support when should be in defend
 //				  - Defend doesn't work for larger fortifications (same as above?)
 
 public class FortifyBehavior extends Behavior {
 
 	private enum FortState { BUILDING, SEEKING_CAMP, DEFENDING, SUPPORT };
 	private FortState state;
 	  
 	private WallSegment[] buildQueue; //## at top
 	private WallSegment curWall;
 	private int queueIdx;
 	
 	public FortifyBehavior(SoldierRobot r){
 		super(r);
 	}
 	
 	
     public void checkBehaviorChange(){
    	// ## combat currently throwing null pointer exceptions, consistently at line 137
		// if(state != FortState.DEFENDING && r.util.senseDanger()) r.setBehavior(r.combatBehavior); 
 	}
 	
 	public void run() throws GameActionException{
 		//r.rc.setIndicatorString(0, state.toString()); //DEBUG
 		if(r.rc.isActive()){  
 		    switch(state) {
 		        case BUILDING:
 		            build();
 		            break;
 		        case SEEKING_CAMP:
 		            seekCamp();
 		            break;
 		        case DEFENDING:
 		            defend();
 		        case SUPPORT:
 		            support();
 		            break;
 		    }
 		}
 	}
 	
 	
 	
     // ------ SET FORTIFICATION GOAL ------
     
     public void buildFort(MapLocation center, int radius, Direction startEdge, int numDirs){
         buildQueue = new WallSegment[numDirs];
         
         Direction d = startEdge;
         MapLocation corner;
         for(int i=0; i < numDirs; i++){
             corner = fortCorner(center, d, radius);
             buildQueue[i] = new WallSegment(corner, d, radius);
             d = d.rotateRight().rotateRight();
         }
         initBuild();
     }
     //Returns the corner of the fortification corresponding to a direction (counterclockwise)
     private MapLocation fortCorner(MapLocation center, Direction d, int   radius){
          return center.add(d.rotateLeft(), radius);
     }
     
     public void buildWall(MapLocation leftEdge, Direction dir, int length){
         buildQueue = new WallSegment[1];
         buildQueue[0] = new WallSegment(leftEdge, dir, length);
         initBuild();
     }
     
     
     private void initBuild(){
         resetProgress();
         curWall = buildQueue[0];
         state = FortState.BUILDING;
     }
     private void resetProgress(){
         //## all important state vars
     }
 	
     
     
     // ------ FORTIFICATION LOGIC ------
     
     private MapLocation buildTarget;
     private void build() throws GameActionException{
   	  if(r.rc.getLocation().equals(buildTarget)){
   		  r.rc.layMine();
   		  buildTarget = getBuildSpot();
   	  }	  
   	  else {
   		  buildTarget = getBuildSpot(); // improvement: don't do this every time
   		  if(buildTarget != null){
   			  r.nav.tunnelTo(buildTarget);
   		  } else {
   			  state = FortState.SEEKING_CAMP;
   			  queueIdx = 0; // Reset these, as seekCamp uses them; I guess?
   			  curWall = buildQueue[0];
   			  seekCamp();
   	      }
   	  }
     }
     
     private MapLocation campTarget;
     private void seekCamp() throws GameActionException{
   	  if(r.rc.getLocation().equals(campTarget)){
   		  state = FortState.DEFENDING;
   		  defend();
   	  }	  
   	  else {
   		  campTarget = getCamp(); // definitely silly to do this every time
   		  if(campTarget != null){
   			  r.nav.tunnelTo(campTarget);
   		  } else {
 			  // All camps are (hopefully) defended; enter support mode
 			  // and replace fallen defenders (rebuild walls?)
			  state = FortState.SUPPORT;
			  support();
//			  state = FortState.DEFENDING;
//			  defend(); // ##have no support mode, defending for now
   	      }
   	  }
     }
     
     private Direction dirToCamp;
     private Robot defuser;
     private void defend() throws GameActionException{
       // Engaged
   	  if(defuser != null) {
   		  if(!r.rc.canSenseObject(defuser) || r.rc.senseRobotInfo(defuser).roundsUntilAttackIdle == 0){
   			  defuser = null;
   			  if(r.rc.canMove(dirToCamp)){
   				  r.rc.move(dirToCamp);
   				  dirToCamp = null;
   			  } //## else, what? shouldn't happen normally, mine push might fit well
   			    // doing nothing will leave it camping outside the mines
   		  }
   	  }
   	  // Camping
   	  else { 				 
   		  RobotInfo enInfo;
   		  Direction defuserDir;
   		  for(Robot enemy : r.rc.senseNearbyGameObjects(Robot.class, 8, r.enemyTeam)){ //5x5 grid
   			  enInfo = r.rc.senseRobotInfo(enemy);
   			  if(enInfo.roundsUntilAttackIdle != 0){
   				  defuserDir = r.rc.getLocation().directionTo(enInfo.location); 
   				  if(r.rc.canMove(defuserDir)){ //##not good enough; pick direction better
   					  defuser = enemy;
   					  r.rc.move(defuserDir);
   					  dirToCamp = defuserDir.opposite();
   					  break;
   				  }
   			  }
   		  }
   	  }
     }
     
     private void support(){
   	  //## TODO
     }
     
     
     
     // ------ WALL SEGMENTS ------
     
     private static class WallSegment {
   	  
   	  // mineDeltas[wall direction][in|out][x|y]
   	  private static int[][][] mineDeltas = {
   		  {{2,1}, {2,-1}}, // North
   		  {{-1,2}, {1,2}}, // East
   		  {{-2,-1}, {-2,1}}, // South
   		  {{1,-2}, {-1,-2}}  // West
   	  };
   	  
   	  public MapLocation start;
   	  public MapLocation curMine;
   	  public MapLocation curCamp;
   	  public Direction wallRight; // Parallel to the wall, your right if you're inside it
   	  public int dirOrd;
   	  public int length;
   	  
   	  private int mineIdx = 0;
   	  private int campIdx = 0;
   	  
   	  WallSegment(MapLocation s, Direction d, int l){
   		  curMine = start = s;
   		  wallRight = d.rotateRight().rotateRight();
   		  curCamp = s.add(wallRight.rotateRight()); //Interior edge
   		  dirOrd = dirIdx(d);
   		  length = l;
   	  }
   	  
   	  public boolean nextMine(){
   		  if(mineIdx < length) {
   			  int[] deltas = mineDeltas[dirOrd][mineIdx++ % 2]; // Could generalize to % appropriate deltas.length
   			  curMine = curMine.add(deltas[0],deltas[1]);
   			  return true;
   		  }
   		  return false;
   	  }
   	  
   	  public boolean nextCamp(){
   		  if(campIdx < 2*length - 1){ //##?
   			  campIdx++;
   			  curMine = curMine.add(wallRight);
   			  return true;
   		  }
   		  return false;
   	  }
   	  
   	  private int dirIdx(Direction d){
   		  return d.ordinal() / 2; //## probably wrong
   	  }
     }
     
     private MapLocation getBuildSpot() throws GameActionException{
   	  while(!needsMine(curWall.curMine) || occupied(curWall.curMine) || !r.util.inMap(curWall.curMine)){
   		  if(!nextWallLoc()) return null; // Updates curWall/curMine; returns null when done building
   	  }
   	  return curWall.curMine;
     }
     private boolean needsMine(MapLocation loc){
   	  return (r.rc.senseMineLocations(loc, 1, r.myTeam).length != 5);
     }
     private boolean occupied(MapLocation loc) throws GameActionException{
   	  return r.rc.canSenseSquare(loc) && (r.rc.senseObjectAtLocation(loc) != null);
     }
     private boolean nextWallLoc(){
   	  if(curWall.nextMine()){
   		  return true;
   	  }
   	  if(queueIdx < buildQueue.length-1){
   		  curWall = buildQueue[++queueIdx];
   		  return true;
   	  }
   	  return false;
     }
    
     // Basically a duplicate of build spot logic; not sure how to combine
     private MapLocation getCamp() throws GameActionException{
   	  while(occupied(curWall.curCamp) || !r.util.inMap(curWall.curCamp)){
   		  if(!nextCampLoc()) return null; // Updates curWall/curLoc; returns null when done building
   	  }
   	  return curWall.curCamp;
     }
     private boolean nextCampLoc(){
   	  if(curWall.nextCamp()){
   		  return true;
   	  }
   	  if(queueIdx < buildQueue.length-1){ //## using same vars as build; I guess?
   		  curWall = buildQueue[++queueIdx];
   		  return true;
   	  }
   	  return false;
     }
 }
