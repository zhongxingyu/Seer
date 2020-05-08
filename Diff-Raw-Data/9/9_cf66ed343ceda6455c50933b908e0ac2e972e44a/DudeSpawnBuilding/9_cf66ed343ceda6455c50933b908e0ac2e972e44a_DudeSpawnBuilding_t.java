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
				if(new Random().nextBoolean()){
					if (getWorld().getPlantResource() >= 50) {
						getWorld().addDude(new Dude(getWorld(), getX(), getY(), 1, 1, "Assets/Characters/Man.png"));
						getWorld().setPlantResource(getWorld().getPlantResource()- 40);
					}
 				}
 				delay = SPAWN_DELAY;
 			} else {
 				delay--;
 			}
 		}
 	}
 }
