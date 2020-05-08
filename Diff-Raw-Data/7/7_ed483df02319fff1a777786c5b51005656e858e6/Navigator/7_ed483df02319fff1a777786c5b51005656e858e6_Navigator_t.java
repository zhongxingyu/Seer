 package dinaBOT.navigation;
 
 import dinaBOT.mech.*;
 
 import lejos.nxt.*;
 
 
 public class Navigator implements Navigation, MechConstants {
 
 	Odometer odometer;
 	Movement movement;
 
 	Map map;
 	Pathing pather;
 
 	int node; //the current location in the path array
 	double[][] path;
 	
 	boolean active, hard_interrupt, soft_interrupt;
 	
 	public Navigator(Pathing pather, Movement movement, Map map, Odometer odometer) {
 		this.odometer = odometer;
 		this.movement = movement;
 
 		this.map = map;
 		this.pather = pather;
 
 		map.registerListener(this);
 	}
 
 	public int goTo(double x, double y) {
 		hard_interrupt = false;
 		while(repath(x, y)) {
 			soft_interrupt = false;
 			for(node = 0; node < path.length; node++) {
 				active = true;
 				if(hard_interrupt || soft_interrupt || !movement.goTo(path[node][0], path[node][1], SPEED_MED)) break;
 				active = false;
 				if(node == path.length-1) return 0;
 			}
 			
 			if(hard_interrupt) return 1;
 		}
 		
 		return -1;
 	}
 	
 	boolean repath(double x, double y) {
 		double[] position = odometer.getPosition();
 		path = pather.generatePath(position[0], position[1], position[2], x, y);
 		if(path == null) return false;
 		else return true;
 	}
 
 	public void interrupt() {
 		hard_interrupt = true;
 		movement.stop();
 	}
 
 	public void newObstacle(int x, int y) {
 		if(active) {
 			for(int i = node ; i < path.length; i++) {
 				// check if new obstacle lies on current path
 				if((path[i][0] == x*UNIT_TILE) && (path[i][1] == y*UNIT_TILE)) {
 					soft_interrupt = true;
 					movement.stop();
 				}
 			}
 		}
 	}
 }
