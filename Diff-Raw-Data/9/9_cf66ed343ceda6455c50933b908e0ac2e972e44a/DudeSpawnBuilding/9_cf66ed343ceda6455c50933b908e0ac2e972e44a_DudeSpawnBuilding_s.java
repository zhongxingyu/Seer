 package state;
 
 import java.util.Random;
 
 
 
 public class DudeSpawnBuilding extends Structure {
 
 	private final int SPAWN_DELAY = 80;
 
 	public DudeSpawnBuilding(int x, int y) {
 		super(x, y, 1, 1, "Assets/EnvironmentTiles/SpawnPoints/SpawnMan1.png");
 	}
 
 	int delay;
 	@Override
 	public void update() {
 		Tile t = getWorld().getTile(getX(), getY());
 		if(getWorld().isDudeSpawningEnabled() && t.getDude() == null) {
 			if(delay <= 0) {
//				if(new Random().nextBoolean())
//					getWorld().addDude(new Dude(getWorld(), getX(), getY(), 1, 1, "Assets/Characters/Man.png"));
				if (getWorld().getPlantResource() >= 50) {
					getWorld().setPlantResource(getWorld().getPlantResource()- 50);//TODO Change amount if needed
 				}
 				delay = SPAWN_DELAY;
 			} else {
 				delay--;
 			}
 		}
 	}
 }
