 package planning;
 
 import faction.Faction;
 import settings.Globals;
 import universe.World;
 
 public class BuyShipTask extends Task {
 
 	private int limit;
 	private World world;
 	public BuyShipTask(World world, int limit, Task parent) {
 		super(true, "Buy Ship Task", parent);
 		this.limit = limit;
 		this.world = world;
 	}
 
 	@Override
 	public int stepsToCompletion(Faction faction) {
 		return 1;
 	}
 
 	@Override
 	public Task getNextStep(Faction faction) {
 		if(faction.getNumResources() >= (limit - faction.getNumShips(world)) * Globals.SHIP_RESOURCE_BUY_COST)
 			return this;
 		else
 			return new GatherResourcesTask((limit - faction.getNumShips(world)) * Globals.SHIP_RESOURCE_BUY_COST, this);
 	}
 
 	@Override
 	public boolean isCompleted(Faction faction) {
 		return faction.getNumShips(world) >= limit;
 	}
 
 	// TODO: Meaningful canPerform here.
 	@Override
 	public boolean canPerform(Faction faction) {
		return faction.getNumResources() >= (limit - faction.getNumShips(world)) * Globals.SHIP_RESOURCE_BUY_COST;
 	}
 	
 	public void perform(Faction faction) {
 		System.out.println("Doing " + name);
 		int numShipsBought = Math.min(faction.getNumResources() / Globals.SHIP_RESOURCE_BUY_COST, limit - faction.getNumShips());
 		faction.increaseShips(numShipsBought, world);
 		faction.removeResources(Globals.SHIP_RESOURCE_BUY_COST*numShipsBought);
 	}
 
 	@Override
 	public double getFlavorMatch(Faction faction) {
 		return ((faction.getDiplomacy() * 2) + faction.getAggression() + faction.getScience()) / 4.0;
 	}
 
 }
