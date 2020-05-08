 package me.heldplayer.HeldGeneration.helpers;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 
 public class BlockHelper {
 	public static boolean isLiquid(Material mat) {
 		return isLiquid(mat.getId());
 	}
 
 	public static boolean isLiquid(int id) {
 		switch (id) {
 		case 8:
 		case 9:
 		case 10:
 		case 11:
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isSolid(Material mat) {
 		return isSolid(mat.getId());
 	}
 
 	public static boolean isSolid(int id) {
 		switch (id) {
 		case 0:
		case 6:
 		case 8:
 		case 9:
 		case 10:
 		case 11:
		case 27:
		case 28:
		case 30:
		case 31:
		case 32:
		case 37:
		case 38:
		case 39:
		case 40:
		case 50:
		case 51:
		case 55:
		case 59:
		case 65:
		case 66:
		case 69:
		case 75:
		case 76:
		case 77:
		case 78:
		case 83:
		case 93:
		case 94:
		case 104:
		case 105:
		case 106:
		case 111:
		case 115:
		case 127:
		case 131:
		case 132:
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean isWater(int id) {
 		return id == 8 || id == 9;
 	}
 
 	public static boolean isOpaqueCube(Material mat) {
 		return isOpaqueCube(mat.getId());
 	}
 
 	public static boolean isOpaqueCube(int id) {
 		switch (id) {
 		case 0:
 		case 6:
 		case 8:
 		case 9:
 		case 10:
 		case 11:
 		case 18:
 		case 20:
 		case 26:
 		case 27:
 		case 28:
 		case 29:
 		case 30:
 		case 31:
 		case 32:
 		case 33:
 		case 34:
 		case 36:
 		case 37:
 		case 38:
 		case 39:
 		case 40:
 		case 44:
 		case 50:
 		case 51:
 		case 52:
 		case 53:
 		case 54:
 		case 55:
 		case 59:
 		case 60:
 		case 63:
 		case 64:
 		case 65:
 		case 66:
 		case 67:
 		case 68:
 		case 69:
 		case 70:
 		case 71:
 		case 72:
 		case 75:
 		case 76:
 		case 77:
 		case 78:
 		case 79:
 		case 81:
 		case 83:
 		case 85:
 		case 92:
 		case 93:
 		case 94:
 		case 96:
 		case 101:
 		case 102:
 		case 104:
 		case 105:
 		case 106:
 		case 107:
 		case 108:
 		case 109:
 		case 111:
 		case 113:
 		case 115:
 		case 116:
 		case 117:
 		case 118:
 		case 119:
 		case 120:
 		case 122:
 		case 126:
 		case 128:
 		case 130:
 		case 131:
 		case 132:
 		case 134:
 		case 135:
 		case 136:
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean canBlockStay(Block block, Mat mat) {
 		Block groundBlock = null;
 		switch (mat) {
 		case TallGrass:
 		case RedRose:
 		case YellowFlower:
 			// Ugly method to check if the block can see the sky
 			if (block.getLightLevel() < 8 && block.getLightFromSky() < 15)
 				return false;
 
 			groundBlock = block.getRelative(BlockFace.DOWN);
 
 			if (!(groundBlock.getTypeId() == Mat.Grass.id) && !(groundBlock.getTypeId() == Mat.Dirt.id) && !(groundBlock.getTypeId() == Mat.Farmland.id))
 				return false;
 
 			break;
 		case DeadShrub:
 			// Ugly method to check if the block can see the sky
 			if (block.getLightLevel() < 8 && block.getLightFromSky() < 15)
 				return false;
 
 			groundBlock = block.getRelative(BlockFace.DOWN);
 
 			if (!(groundBlock.getTypeId() == Mat.Sand.id))
 				return false;
 
 			break;
 		case Pumpkin:
 			groundBlock = block.getRelative(BlockFace.DOWN);
 
 			return isOpaqueCube(groundBlock.getTypeId());
 		case RedMushroom:
 		case BrownMushroom:
 			groundBlock = block.getRelative(BlockFace.DOWN);
 
 			if (block.getLightFromSky() > 12) {
 				return false;
 			}
 
 			return isOpaqueCube(groundBlock.getTypeId());
 		case SugarCane:
 			groundBlock = block.getRelative(BlockFace.DOWN);
 
 			if (groundBlock.getTypeId() == Mat.SugarCane.id)
 				return true;
 
 			if (!(groundBlock.getTypeId() == Mat.Grass.id) && !(groundBlock.getTypeId() == Mat.Dirt.id) && !(groundBlock.getTypeId() == Mat.Farmland.id))
 				return false;
 
 			if (isWater(groundBlock.getRelative(BlockFace.NORTH).getTypeId()))
 				return true;
 			if (isWater(groundBlock.getRelative(BlockFace.EAST).getTypeId()))
 				return true;
 			if (isWater(groundBlock.getRelative(BlockFace.SOUTH).getTypeId()))
 				return true;
 			if (isWater(groundBlock.getRelative(BlockFace.WEST).getTypeId()))
 				return true;
 
 			break;
 		case Cactus:
 			groundBlock = block.getRelative(BlockFace.DOWN);
 
 			if (groundBlock.getTypeId() != Mat.Sand.id && groundBlock.getTypeId() != Mat.Cactus.id)
 				return false;
 
 			if (!isSolid(groundBlock.getRelative(BlockFace.NORTH).getTypeId()))
 				return false;
 			if (!isSolid(groundBlock.getRelative(BlockFace.EAST).getTypeId()))
 				return false;
 			if (!isSolid(groundBlock.getRelative(BlockFace.SOUTH).getTypeId()))
 				return false;
 			if (!isSolid(groundBlock.getRelative(BlockFace.WEST).getTypeId()))
 				return false;
 		}
 
 		return true;
 	}
 
 	public static boolean canPlcaeVineOnSide(Block block, int side) {
 		switch (side) {
 		case 1:
 			return isOpaqueCube(block.getRelative(0, 1, 0).getTypeId());
 
 		case 2:
 			return isOpaqueCube(block.getRelative(0, 0, 1).getTypeId());
 
 		case 3:
 			return isOpaqueCube(block.getRelative(0, 0, -1).getTypeId());
 
 		case 4:
 			return isOpaqueCube(block.getRelative(1, 0, 0).getTypeId());
 
 		case 5:
 			return isOpaqueCube(block.getRelative(-1, 0, 0).getTypeId());
 
 		default:
 			return false;
 		}
 	}
 }
