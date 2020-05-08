 package rsmg.model.tile;
 
 
 public class GroundTile extends Tile {
 
 	private boolean solid;
 
 	/**
 	 * Constructor of GroundTile. Sets the solid value
 	 */
 	public GroundTile(String groundType) {
 		super(groundType);
 		solid = true;
 	}
 
 	/**
 	 * @return if an GroundTile is solid or not
 	 */
 	public boolean isSolid() {
 		return solid;
 	}
 }
