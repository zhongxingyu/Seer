 package com.android.icecave.guiLogic.theme;
 
 import com.android.icecave.utils.Point;
 
 public class WallTheme extends BaseObjectTheme
 {
 	public WallTheme() {
 		// Define wall tile positions
		final Point WALL_1 = new Point(3, 0);
		final Point WALL_2 = new Point(4, 0);
		final Point WALL_3 = new Point(5, 0);
		final Point WALL_4 = new Point(6, 0);
 		
 		mTileLocations = new Point[] {WALL_1, WALL_2, WALL_3, WALL_4};
 	}
 }
