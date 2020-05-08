 package com.github.jamescarter.hexahop.core.tile;
 
 import com.github.jamescarter.hexahop.core.grid.TileGrid;
 import com.github.jamescarter.hexahop.core.level.Location;
 import com.github.jamescarter.hexahop.core.player.Direction;
 
 public class TrampolineTile extends Tile {
 	private TileGrid<?> tileGrid;
 
 	public TrampolineTile(TileGrid<?> tileGrid, Location location) {
 		super(location, TileImage.TRAMPOLINE);
 
 		this.tileGrid = tileGrid;
 	}
 
 	@Override
 	public Location stepOn(Direction direction) {
 		Location location = getLocation().clone();
 
 		// attempt to move the player up to two places in the same direction
 		for (int i=0; i<2; i++) {
 			Location newLocation = location.clone();
 			newLocation.move(direction);
 
 			Tile statusTile = tileGrid.statusAt(newLocation);
 
 			if (tileGrid.canMove(location, direction) || statusTile == null || !statusTile.isActive()) {
				if (!(statusTile != null && statusTile.isWall())) {
					location.move(direction);
				}
 			} else if (i == 0) {
 				return null;
 			}
 		}
 
 		return location;
 	}
 }
