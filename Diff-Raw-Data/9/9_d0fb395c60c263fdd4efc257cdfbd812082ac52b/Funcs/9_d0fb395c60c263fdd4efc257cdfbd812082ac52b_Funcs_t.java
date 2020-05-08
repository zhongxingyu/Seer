 package infinitealloys.util;
 
 import net.minecraft.block.Block;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class Funcs {
 
 	public static Block getBlock(int id) {
 		return Block.blocksList[id];
 	}
 
 	public static Block getBlock(World world, int x, int y, int z) {
 		return getBlock(world.getBlockId(x, y, z));
 	}
 
 	/** Translate a number n into a radix, add leading zeros to make it length strlen, then get the digit at pos
 	 * 
 	 * @param radix the radix of the number being given, e.g. 10 (decimal) or 2 (binary)\
	 * @param strlen the length to make the number (will be filled in with leading zeros) before finding the digit
 	 * @param n the number that is being used
 	 * @param pos the position of the digit to be found
 	 * @return the digit at pos, after adding leading zeros to make it length strlen */
 	public static int intAtPos(int n, int radix, int strlen, int pos) {
 		return Character.digit(addLeadingZeros(Integer.toString(n, radix), strlen).charAt(pos), radix);
 	}
 
 	/** Take the log base n of num
 	 * 
 	 * @param base the base of the logarithm
 	 * @param n the number to be used */
 	public static double logn(int base, double num) {
 		return Math.log(num) / Math.log(base);
 	}
 
 	/** Add leading zeros to a number to make it a certain length
 	 * 
 	 * @param s the string to be extended
 	 * @param finalSize the length to be acheived */
 	public static String addLeadingZeros(String s, int finalSize) {
 		s.trim();
 		int length = s.length();
 		for(int i = 0; i < finalSize - length; i++)
 			s = "0" + s;
 		return s;
 	}
 
 	/** Get a localization or series of localization with keys. Add '/' to the start of a key to have it added to the final string without being localized. e.g.
 	 * getLoc("general.off", "/is not", "general.on") would return "Off is not On"
 	 * 
 	 * @param keys the list of keys to be localized and spliced together into a final string */
 	public static String getLoc(String... keys) {
 		String finalKey = "";
 		for(String key : keys) {
 			if(key.length() == 0)
 				continue;
 			if(key.charAt(0) == '/')
 				finalKey += key.substring(1);
 			else
 				finalKey += LanguageRegistry.instance().getStringLocalization(key);
 		}
 		return finalKey;
 	}
 
 	/** Convert a Vanilla MC block face int to a ForgeDirection */
 	public static ForgeDirection numToFDSide(int num) {
 		switch(num) {
 			case Consts.TOP:
 				return ForgeDirection.UP;
 			case Consts.BOTTOM:
 				return ForgeDirection.DOWN;
 			case Consts.EAST:
 				return ForgeDirection.EAST;
 			case Consts.WEST:
 				return ForgeDirection.WEST;
 			case Consts.NORTH:
 				return ForgeDirection.NORTH;
 			case Consts.SOUTH:
 				return ForgeDirection.SOUTH;
 			default:
 				return ForgeDirection.UNKNOWN;
 		}
 	}
 
	/** Convert a ForgeDirection to a Vanilla MC block face int */
 	public static int fdToNumSide(ForgeDirection fd) {
 		switch(fd) {
 			case UP:
 				return Consts.TOP;
 			case DOWN:
 				return Consts.BOTTOM;
 			case EAST:
 				return Consts.EAST;
 			case WEST:
 				return Consts.WEST;
 			case NORTH:
 				return Consts.NORTH;
 			case SOUTH:
 				return Consts.SOUTH;
 			default:
 				return -1;
 		}
 	}
 
 	/** Convert an entity's yaw to a Vanilla MC block face int */
 	public static int yawToNumSide(int rotation) {
 		switch(rotation) {
 			case 0:
 				return Consts.SOUTH;
 			case 1:
 				return Consts.WEST;
 			case 2:
 				return Consts.NORTH;
 			case 3:
 				return Consts.EAST;
 			default:
 				return -1;
 		}
 	}
 
 	/** See if the running side is client
 	 * 
 	 * @return true if the running side is client */
 	public static boolean isClient() {
 		return FMLCommonHandler.instance().getEffectiveSide().isClient();
 	}
 
 	/** See if the running side is server
 	 * 
 	 * @return true if the running side is server */
 	public static boolean isServer() {
 		return FMLCommonHandler.instance().getEffectiveSide().isServer();
 	}
 
 	/** Check if the block at x, y, z is of a certain type
 	 * 
 	 * @return true if block at x, y, z is equals id and metadata */
 	public static boolean blocksEqual(World world, int id, int metadata, int x, int y, int z) {
 		return world.getBlockId(x, y, z) == id && world.getBlockMetadata(x, y, z) == metadata;
 	}
}
