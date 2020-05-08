 package btlshp.turns;
 
 import java.io.Serializable;
 
 import btlshp.entities.Base;
 import btlshp.entities.ConstructBlock;
 import btlshp.entities.Map;
 
 class RepairBase extends Turn implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3486672126675014858L;
 	private ConstructBlock repairBlock;
 	private Base b;
 	private boolean success = false;
	private Map m;
 
 	RepairBase(Base b, ConstructBlock repairBlock) {
 		this.b = b;
 		this.repairBlock = repairBlock;
 	}
 
 	@Override
 	public void executeTurn() {
		b = m.getBase(b.getConstructID());
 		b.AssesRepair(repairBlock);
 		success = true;
 	}
 	@Override
 	public boolean wasSuccessful() {
 		return success;
 	}
 	@Override
 	public String toString(){
 		return "repairBase";
 	}
 
 	@Override
 	public void setMap(Map m) {
		this.m = m;
 		
 	}
 }
