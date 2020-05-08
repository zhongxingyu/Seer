 package btlshp.turns;
 
 import java.io.Serializable;
 
 import btlshp.entities.Map;
 import btlshp.entities.Ship;
 import btlshp.enums.Direction;
 
 class MoveShip extends Turn implements Serializable{
 	
 	private static final long serialVersionUID = 4599021282070269467L;
 	private Ship s;
 	private Direction dir;
 	private int distance;
 	private Map m;
 	private boolean success = false;
 
 	public MoveShip(Map m, Ship s, Direction dir, int distance) {
 		this.s = s;
 		this.dir = dir;
 		this.distance = distance;
 	}
 	
 	@Override
 	public void executeTurn() {
 		try{
			System.err.println("Moving ship "+s.getConstructID()+" "+ dir.name() +" "+ distance+ " blocks");
 			m.move(s, dir,distance);
 			success = true;
 		}catch(IllegalStateException e){
 			e.printStackTrace();
 			success = false;
 		}
 		
 	}
 
 	@Override
 	public boolean wasSuccessful() {
 		return true;
 	}
 	@Override
 	public String toString(){
 		return "moveShip";
 	}
 
 	@Override
 	public void setMap(Map m) {
 		this.m = m;
 		
 	}
 }
