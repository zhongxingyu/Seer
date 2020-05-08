 package com.jpii.navalbattle.pavo.grid;
 
 import java.io.Serializable;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 
 public class GridHelper implements Serializable {
 	EntityManager man;
 	Rand random;
 	public GridHelper(EntityManager eman) {
 		random = Game.Settings.rand;
 		man = eman;
 	}
 	public Location pollNextLiquidSpace(int amountOfWater, int tolerance) {
 		boolean found = false;
 		int r = 0, c = 0;
 		while (!found) {
			r = random.nextInt(PavoHelper.getGameHeight(man.getWorld().getWorldSize())*2);
			c = random.nextInt(PavoHelper.getGameWidth(man.getWorld().getWorldSize())*2);
 			int b = man.getTilePercentLand(r, c);
 			if (b > amountOfWater - tolerance && b < amountOfWater + tolerance)
 				found = true;
 		}
 		return new Location(r,c);
 	}
 	public Location pollNextWaterTile(int tolerance) {
 		return pollNextLiquidSpace(0, tolerance);
 	}
 	public Location pollNextWaterTile() {
 		return pollNextWaterTile(8);
 	}
 	/**
 	 * 
 	 * @param em - needed to get Tile Percent Land to check for if land is in the way
 	 * @param rotate - needed to check which direction your checking for
 	 * @param row - needed to find the starting row
 	 * @param col - needed to find the starting col
 	 * @param width - needed to know how many spaces to check
 	 * @return - returns true if the space(s) allow for this entity
 	 */
 	public static boolean canPlaceInGrid(EntityManager em,byte rotate, int row, int col, int width) {
 		boolean flag = true;
 		if (rotate == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row,col+c);
 				if (p > 5) {
 					flag = false;
 					break;
 				}
 				Tile temp = em.getTile(row,col+c);
 				if(temp!=null) {
 					flag=false;
 					break;
 				}
 			}
 		}
 		if (rotate == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row-c,col);
 				if (p > 5) {
 					flag = false;
 					break;
 				}
 				Tile temp = em.getTile(row-c,col);
 				if(temp!=null) {
 					flag=false;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	
 	public static boolean canRotate(EntityManager em,Entity e,byte rotate, int row, int col, int width) {
 		boolean flag = true;
 		if (rotate == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row,col+c);
 				if (p > 5) {
 					flag = false;
 					break;
 				}
 				Tile temp = em.getTile(row,col+c);
 				if(temp!=null&&!temp.getEntity().equals(e)){
 					flag=false;
 					break;
 				}
 			}
 		}
 		if (rotate == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row-c,col);
 				if (p > 5) {
 					flag = false;
 					break;
 				}
 				Tile temp = em.getTile(row-c,col);
 				if(temp!=null&&!temp.getEntity().equals(e)){
 					flag=false;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 }
