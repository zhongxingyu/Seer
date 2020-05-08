 package com.jpii.navalbattle.pavo.grid;
 
 import java.io.Serializable;
 
 import maximusvladimir.dagen.Rand;
 
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.pavo.Game;
 import com.jpii.navalbattle.pavo.PavoHelper;
 
 public class GridHelper implements Serializable {
 	private static final long serialVersionUID = 1L;
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
 	public Location pollNearLocation(Location l) {
 		Location ln = new Location(random.nextInt(-4, 4)+l.getRow(), random.nextInt(-4,4)+l.getCol());
 		boolean flag = Location.validate(ln);
 		if (flag)
 			System.out.println("Invalid location selected.");
 		return ln;
 	}
 	public Location pollNextWaterTile(int tolerance) {
 		return pollNextLiquidSpace(0, tolerance);
 	}
 	public Location pollNextWaterTile() {
 		return pollNextWaterTile(Game.Settings.waterThresholdBarrier);
 	}
 	
 	public Location pollNextShoreTile() {
 		boolean found = false;
 		int r = 0, c = 0;
 		while (!found) {
 			r = random.nextInt(PavoHelper.getGameHeight(man.getWorld().getWorldSize())*2);
 			c = random.nextInt(PavoHelper.getGameWidth(man.getWorld().getWorldSize())*2);
 			int b = man.getTilePercentLand(r, c);
 			if (b > 10 && b < 70)
 				found = true;
 		}
 		return new Location(r,c);
 	}
 	/**
 	 * !USE WHEN ENTITY DOES NOT EXIST PREVIOUSLY!
 	 * @param em - needed to get Tile Percent Land to check for if land is in the way
 	 * @param rotate - needed to check which direction your checking for
 	 * @param row - needed to find the starting row
 	 * @param col - needed to find the starting col
 	 * @param width - needed to know how many spaces to check
 	 * @return - returns true if the space(s) allow for this entity
 	 */
 	public static boolean canPlaceInGrid(EntityManager em,byte rotateto, int row, int col, int width) {
 		boolean flag = true;
 		if (rotateto == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row,col+c);
				if(row<0||col+c<0)
					return false;
 				if(col+c>=PavoHelper.getGameWidth(em.getWorld().getWorldSize())*2)
 					return false;
 				if (p > Game.Settings.waterThresholdBarrier) {
 					flag = false;
 					break;
 				}
 				Tile<Entity> temp = em.getTile(row,col+c);
 				if(temp!=null) {
 					flag=false;
 					break;
 				}
 			}
 		}
 		if (rotateto == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row-c,col);
				if(row-c<0||col<0)
 					return false;
 				if (p > Game.Settings.waterThresholdBarrier) {
 					flag = false;
 					break;
 				}
 				Tile<Entity> temp = em.getTile(row-c,col);
 				if(temp!=null) {
 					flag=false;
 					break;
 				}
 			}
 		}
 		return flag;
 	}
 	
 	public static boolean canMoveTo(EntityManager em,MoveableEntity e,byte position, int row, int col, int width) {
 		if (position == GridedEntityTileOrientation.ORIENTATION_LEFTTORIGHT) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row,col+c);
 				if(col+c>=PavoHelper.getGameWidth(em.getWorld().getWorldSize())*2)
 					return false;
 				if(!e.isInMoveRange(col,row)){
 					return false;
 				}
 				if (p > Game.Settings.waterThresholdBarrier){
 					return false;
 				}
 				Tile<Entity> temp = em.getTile(row,col+c);
 				if(!(temp==null||temp.getEntity().equals(e))){
 					return false;
 				}
 			}
 		}
 		if (position == GridedEntityTileOrientation.ORIENTATION_TOPTOBOTTOM) {
 			for (int c = 0; c < width; c++) {
 				int p = em.getTilePercentLand(row-c,col);
 				if(row-c<0)
 					return false;
 				if(!e.isInMoveRange(col,row))
 					return false;
 				if (p > Game.Settings.waterThresholdBarrier)
 					return false;
 				Tile<Entity> temp = em.getTile(row-c,col);
 				if(temp!=null&&!temp.getEntity().equals(e))
 					return false;
 			}
 		}
 		return true;
 	}
 	
 	public static boolean canAttackPrimaryTo(EntityManager em,MoveableEntity e, int row, int col) {
 		if(!e.isInPrimaryRange(col,row)){
 			return false;
 		}
 		Tile<Entity> temp = em.getTile(row,col);
 		if((temp==null||temp.getEntity().equals(e))){
 			return false;
 		}
 		return true;
 	}
 	
 	public static boolean canAttackSecondaryTo(EntityManager em,MoveableEntity e, int row, int col) {
 		if(!e.isInSecondaryRange(col,row)){
 			return false;
 		}
 		Tile<Entity> temp = em.getTile(row,col);
 		if((temp==null||temp.getEntity().equals(e))){
 			return false;
 		}
 		return true;
 	}
 }
