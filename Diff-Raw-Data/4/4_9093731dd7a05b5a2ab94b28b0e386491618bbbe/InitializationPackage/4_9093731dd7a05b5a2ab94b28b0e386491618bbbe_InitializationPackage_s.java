 package communication;
 
 import client.Ant;
 import engine.BlockSnapshot;
 import engine.Grid;
 
 public class InitializationPackage extends AbstractPackage
 {
	private static final long serialVersionUID = -3091198855152175406L;
	
 	public BlockSnapshot[][]startingSnapshot;
 
 	@Override
 	public void initialize(Ant ant) 
 	{
 		// Skipped in this package; initialized server-side.
 	}
 
 	@Override
 	public void updateGrid(Grid mainGrid)
 	{
 		startingSnapshot = mainGrid.getSnapshotForAnt(mainGrid.getAntHoleLocation(), 3);
 	}
 
 	@Override
 	public void processReturn(Ant a) 
 	{
 		a.setCurrentLocation(startingSnapshot);
 	}
 
 }
