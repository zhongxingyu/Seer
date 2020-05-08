 package uk.co.samatkins.dungeon.play;
 
 import uk.co.samatkins.dungeon.data.AssetManager;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.math.Vector2;
 
 public class Door extends DungeonEntity {
 
 	private boolean open;
 	
 	private static final Vector2 openCoords = new Vector2(2, 2);
 	private static final Vector2 closedCoords = new Vector2(1, 2);
 	
 	public Door(Dungeon dungeon, int x, int y) {
 		super(dungeon, x, y);
 		
 		this.name = "Door";
 		
 		this.sprite = new Sprite(AssetManager.getInstance().getTilesTexture(),
 				0, 0,
 				Dungeon.TILE_WIDTH, Dungeon.TILE_HEIGHT);
 		
 		this.setOpen(false);
 		
 	}
 	
 	public boolean isOpen() {
 		return this.open;
 	}
 
 	@Override
 	public boolean beUsedBy(DungeonEntity user) {
 		if (this.open) {
 			//Door already open
 			Gdx.app.log("DOOR", "Door is opened! Should let player through!");
 			return false;
 		} else {
 			Gdx.app.log("DOOR", "Open sesame!");
 			this.setOpen(true);
 			return true;
 		}
 	}
 
 	private void setOpen(boolean open) {
 		this.open = open;
 		this.solid = !this.open;
 		
 		if (this.open) {
 			this.sprite.setRegion((int) (openCoords.x * Dungeon.TILE_WIDTH),
 								(int) (openCoords.y * Dungeon.TILE_HEIGHT),
 								Dungeon.TILE_WIDTH, Dungeon.TILE_HEIGHT);
 		} else {
 			this.sprite.setRegion((int) (closedCoords.x * Dungeon.TILE_WIDTH),
 								(int) (closedCoords.y * Dungeon.TILE_HEIGHT),
 								Dungeon.TILE_WIDTH, Dungeon.TILE_HEIGHT);
 		}
 		
 		this.dungeon.setTileSolid(this.tileX, this.tileY, !this.open);
 	}
 
 }
