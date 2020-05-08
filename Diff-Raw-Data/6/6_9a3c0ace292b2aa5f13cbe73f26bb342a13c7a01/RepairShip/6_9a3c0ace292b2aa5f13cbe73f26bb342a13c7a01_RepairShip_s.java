 package btlshp.turns;
 
 import java.io.Serializable;
 
 import btlshp.entities.ConstructBlock;
 import btlshp.entities.Map;
 import btlshp.entities.Ship;
 
 class RepairShip extends Turn implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 89817140305258661L;
 	private Ship s;
 	private ConstructBlock repairBlock;
 	private boolean success = false;
 
 	RepairShip(Ship s, ConstructBlock repairBlock) {
 		this.s = s;
 		this.repairBlock = repairBlock;
 	}
 
 	@Override
 	public void executeTurn() {
 		s.AssesRepair(repairBlock);
 		success = true;
 	}
 
 	
 
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	
 	@Override
 	public String toString(){
 		return "repairShip";
 	}
 
 	@Override
 	public void setMap(Map m) {
		// TODO Auto-generated method stub
 		
 	}
 }
