 package wallasalkis.strategies;
 
 import org.powerbot.core.script.job.Task;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.game.api.methods.interactive.Players;
 import org.powerbot.game.api.methods.node.SceneEntities;
 import org.powerbot.game.api.methods.tab.Inventory;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.wrappers.node.SceneObject;
 
 import wallasalkis.main.WallasalkisMain;
 import wallasalkis.storage.Storage;
 
 public class PlaceCannon extends Node {
 	@Override
 	public boolean activate() {
		SceneObject cannon = SceneEntities.getNearest(6);
		return cannon == null;
 	}
 	
 	@Override
 	public void execute() {
 		WallasalkisMain.s = "PlaceCannon()";
 		try {
 		if (Players.getLocal().getLocation().equals(Storage.cannonTile.getLocation())) {
 			if (Players.getLocal().getAnimation() != 827 || !Players.getLocal().isMoving()) {
 				if (Inventory.getItem(6) != null) {
 					Inventory.getItem(Storage.baseId).getWidgetChild().click(true);
 				}
 				Task.sleep(1000,1501);
 			}
 		} else {
 			if (Storage.cannonTile.isOnScreen()) {
 				if (!Players.getLocal().isMoving()) {
 					Storage.cannonTile.interact("Walk");
 					Task.sleep(1000,1501);
 				}
 			} else {
 				Camera.setPitch(true);
 				Camera.turnTo(Storage.cannonTile);
 				Camera.setPitch(Random.nextInt(30,70));
 			}
 		}
 		} catch (Exception e) {
			System.out.println("Lol, we don't have a cannon in our inventory." +
					"\n Lets be gay and throw a ton of NPE's");
 		}
 	}
 }
