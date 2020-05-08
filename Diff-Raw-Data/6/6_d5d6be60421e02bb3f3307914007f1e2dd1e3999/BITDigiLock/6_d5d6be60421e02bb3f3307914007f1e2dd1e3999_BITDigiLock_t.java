 package dk.gabriel333.BukkitInventoryTools.DigiLock;
 
 import java.net.MalformedURLException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Dispenser;
 
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Button;
 import org.bukkit.material.Door;
 import org.bukkit.material.Lever;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.SpoutBlock;
 import org.getspout.spoutapi.block.SpoutChest;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericItemWidget;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.PopupScreen;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import dk.gabriel333.BukkitInventoryTools.BIT;
 import dk.gabriel333.Library.BITConfig;
 import dk.gabriel333.Library.BITMessages;
 
 public class BITDigiLock {
 
 	@SuppressWarnings("unused")
 	private BIT plugin;
 
 	public BITDigiLock(BIT plugin) {
 		this.plugin = plugin;
 	}
 
 	protected SpoutBlock sBlock;
 	protected String pincode;
 	protected String owner;
 	protected int closetimer;
 	protected String coOwners;
 	protected String users;
 	protected int typeId;
 	protected String connectedTo;
 	protected int useCost;
 
 	/**
 	 * Constructs a new BITDigiLock
 	 * 
 	 * @param block
 	 * @param pincode
 	 * @param owner
 	 * @param closetimer
 	 * @param coowners
 	 * @param users
 	 * @param typeId
 	 * @param connectedTo
 	 * @param useCost
 	 */
 	BITDigiLock(SpoutBlock block, String pincode, String owner, int closetimer,
 			String coowners, String users, int typeId, String connectedTo,
 			int useCost) {
 		this.sBlock = block;
 		this.pincode = pincode;
 		this.owner = owner;
 		this.closetimer = closetimer;
 		this.coOwners = coowners;
 		this.users = users;
 		this.typeId = typeId;
 		this.connectedTo = connectedTo;
 		this.useCost = useCost;
 	}
 
 	// public static Map<Integer, Boolean> isLocked = new HashMap<Integer,
 	// Boolean>();
 
 	/**
 	 * Saves the DigiLock to the database.
 	 * 
 	 * @param sPlayer
 	 *            the player who is interactions with the lock.
 	 * @param block
 	 *            The block of the DigiLock.
 	 * @param pincode
 	 *            of the DigiLock. "fingerprint" or "" or a 10 digits code.
 	 * @param owner
 	 *            is the owner of the DigiLock
 	 * @param closetimer
 	 *            is number of seconds before the door closes.
 	 * @param coowners
 	 *            is the list of co - owners of the DigiLock.
 	 * @param typeId
 	 *            is the type of the block.
 	 * @param connectedTo
 	 *            - not used yet.
 	 * @param useCost
 	 *            is the cost to use the block.
 	 */
 	public static void SaveDigiLock(SpoutPlayer sPlayer, SpoutBlock block,
 			String pincode, String owner, Integer closetimer, String coowners,
 			String users, int typeId, String connectedTo, int useCost) {
 		String query;
 		boolean createlock = true;
 		boolean newLock = true;
 		int cost = BITConfig.DIGILOCK_COST;
 		block = getDigiLockBlock(block);
 		if (isLocked(block)) {
 			newLock = false;
 			query = "UPDATE " + BIT.digilockTable + " SET pincode='" + pincode
 					+ "', owner='" + owner + "', closetimer=" + closetimer
 					+ " , coowners='" + coowners + "' , users='" + users
 					+ "', typeid=" + typeId + ", connectedto='" + connectedTo
 					+ "', usecost=" + useCost
 
 					+ " WHERE x = " + block.getX() + " AND y = " + block.getY()
 					+ " AND z = " + block.getZ() + " AND world='"
 					+ block.getWorld().getName() + "';";
 		} else {
 			// NEW DIGILOCK
 			query = "INSERT INTO " + BIT.digilockTable
 					+ " (pincode, owner, closetimer, "
 					+ "x, y, z, world, coowners, users, "
 					+ "typeid, connectedto, usecost) VALUES ('" + pincode
 					+ "', '" + owner + "', " + closetimer + ", " + block.getX()
 					+ ", " + block.getY() + ", " + block.getZ() + ", '"
 					+ block.getWorld().getName() + "', '" + coowners + "', '"
 					+ users + "', " + block.getTypeId() + ", '" + connectedTo
 					+ "', " + useCost + " );";
 			if (BIT.useEconomy) {
 				if (BIT.plugin.Method.hasAccount(sPlayer.getName()) && cost > 0) {
 					if (BIT.plugin.Method.getAccount(sPlayer.getName())
 							.hasEnough(cost)) {
 						BIT.plugin.Method.getAccount(sPlayer.getName())
 								.subtract(cost);
 						sPlayer.sendMessage("Your account ("
 								+ BIT.plugin.Method.getAccount(
 										sPlayer.getName()).balance()
 								+ ") has been deducted "
 								+ BIT.plugin.Method.format(cost) + ".");
 					} else {
 						sPlayer.sendMessage("You dont have enough money ("
 								+ BIT.plugin.Method.getAccount(
 										sPlayer.getName()).balance()
 								+ "). Cost is:"
 								+ BIT.plugin.Method.format(cost));
 						createlock = false;
 					}
 				}
 			}
 		}
 		if (createlock) {
 			if (BITConfig.DEBUG_SQL)
 				sPlayer.sendMessage(ChatColor.YELLOW + "Updating lock: "
 						+ query);
 			if (BITConfig.STORAGE_TYPE.equals("MYSQL")) {
 				try {
 					BIT.manageMySQL.insertQuery(query);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			} else {
 				BIT.manageSQLite.insertQuery(query);
 			}
 			if (newLock) {
 				BITMessages.sendNotification(sPlayer, "DigiLock created.");
 			} else {
 				BITMessages.sendNotification(sPlayer, "DigiLock updated.");
 			}
 		} else {
 			sPlayer.sendMessage("You dont have enough money. Cost is:" + cost);
 		}
 	}
 
 	/**
 	 * Checks if the block is locked
 	 * 
 	 * @param block
 	 * @return true if it is locked, false if not
 	 */
 	public static Boolean isLocked(SpoutBlock block) {
 		// TODO: Implement a HASHMAP for testing if the block is locked.
 		// BITMessages.showInfo("isLocked was called");
 		if (block != null)
 			if (isLockable(block)) {
 				block = getDigiLockBlock(block);
 				String query = "SELECT * FROM " + BIT.digilockTable
 						+ " WHERE (x = " + block.getX() + " AND y = "
 						+ block.getY() + " AND z = " + block.getZ()
 						+ " AND world='" + block.getWorld().getName() + "');";
 				ResultSet result = null;
 				if (BITConfig.STORAGE_TYPE.equals("MYSQL")) {
 					try {
 						result = BIT.manageMySQL.sqlQuery(query);
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					} catch (InstantiationException e) {
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						e.printStackTrace();
 					}
 				} else { // SQLLITE
 					result = BIT.manageSQLite.sqlQuery(query);
 				}
 				try {
 					if (result != null && result.next()) {
 						return true;
 					} else {
 						return false;
 					}
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		return false;
 	}
 
 	/**
 	 * Method to find the the next block which is connected to a lever or a
 	 * stonebutton.
 	 * 
 	 * @param sBlock
 	 *            SpoutBlock
 	 * @return SpoutBlock
 	 */
 	public static SpoutBlock getDigiLockBlock(SpoutBlock sBlock) {
 		if (isDoor(sBlock)) {
 			if (isDoubleDoor(sBlock)) {
 				sBlock = getLeftDoubleDoor(sBlock);
			} 
 			Door door = (Door) sBlock.getState().getData();
 			if (door.isTopHalf()) {
 				sBlock = sBlock.getRelative(BlockFace.DOWN);
 			}
 		} else if (isChest(sBlock)) {
 			SpoutChest sChest1 = (SpoutChest) sBlock.getState();
 			if (sChest1.isDoubleChest()) {
 				SpoutChest sChest2 = sChest1.getOtherSide();
 				SpoutBlock sBlock2 = (SpoutBlock) sChest2.getBlock();
 				if (sChest1.getX() == sChest2.getX()) {
 					if (sChest1.getZ() < sChest2.getZ()) {
 						return sBlock;
 					} else {
 						return sBlock2;
 					}
 				} else {
 					if (sChest1.getX() < sChest2.getX()) {
 						return sBlock;
 					} else {
 						return sBlock2;
 					}
 				}
 			}
 		}
 		return sBlock;
 	}
 
 	/**
 	 * Checks if sPlayer is co Owner of the DigiLock.
 	 * 
 	 * @param sPlayer
 	 * @return true or false
 	 */
 	public boolean isCoowner(SpoutPlayer sPlayer) {
 		if (coOwners.toLowerCase().contains(sPlayer.getName().toLowerCase())
 				|| coOwners.toLowerCase().contains("everyone"))
 			return true;
 		return false;
 	}
 
 	public boolean isUser(SpoutPlayer sPlayer) {
 		if (users.toLowerCase().contains(sPlayer.getName().toLowerCase())
 				|| users.toLowerCase().contains("everyone"))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Checks if sPlayer is Owner of the DigiLock.
 	 * 
 	 * @param sPlayer
 	 * @return true or false
 	 */
 	public boolean isOwner(SpoutPlayer sPlayer) {
 		if (owner.toLowerCase().equals(sPlayer.getName().toLowerCase()))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Checks if sPlayer is Owner of the DigiLock placed on sBlock.
 	 * 
 	 * @param sPlayer
 	 * @return true or false
 	 */
 	public static boolean isOwner(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		if (sBlock != null)
 			if (BITDigiLock.loadDigiLock(sBlock).getOwner().toLowerCase()
 					.equals(sPlayer.getName().toLowerCase()))
 				return true;
 		return false;
 	}
 
 	public void addCoowner(String name) {
 		this.coOwners = coOwners.concat("," + name);
 	}
 
 	public boolean removeCoowner(String name) {
 		if (coOwners.toLowerCase().contains(name.toLowerCase())) {
 			this.coOwners = coOwners.replace(name, "");
 			this.coOwners = coOwners.replace(",,", ",");
 			return true;
 		}
 		return false;
 	}
 
 	public void addUser(String name) {
 		this.users = users.concat("," + name);
 	}
 
 	public boolean removeUser(String name) {
 		if (users.toLowerCase().contains(name.toLowerCase())) {
 			this.users = users.replace(name, "");
 			this.users = users.replace(",,", ",");
 			return true;
 		}
 		return false;
 	}
 
 	private static final Material lockablematerials[] = { Material.CHEST,
 			Material.LOCKED_CHEST, Material.IRON_DOOR,
 			Material.IRON_DOOR_BLOCK, Material.WOODEN_DOOR, Material.WOOD_DOOR,
 			Material.FURNACE, Material.DISPENSER, Material.LEVER,
 			Material.STONE_BUTTON, Material.BOOKSHELF, Material.TRAP_DOOR,
 			Material.SIGN, Material.SIGN_POST, Material.WALL_SIGN,
 			Material.FENCE_GATE, Material.JUKEBOX };
 
 	// check if material is a lockable block
 	/**
 	 * Check if the Block is made of a lockable material.
 	 * 
 	 * @param block
 	 * @return true or false
 	 */
 	public static boolean isLockable(Block block) {
 		if (block != null)
 			for (Material i : lockablematerials) {
 				if (i == block.getType())
 					return true;
 			}
 		return false;
 	}
 
 	static boolean isLockable(Material material) {
 		if (material != null)
 			for (Material i : lockablematerials) {
 				if (i == material)
 					return true;
 			}
 		return false;
 	}
 
 	public String getPincode() {
 		return pincode;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public int getClosetimer() {
 		return closetimer;
 	}
 
 	public String getCoOwners() {
 		return coOwners;
 	}
 
 	public String getUsers() {
 		return users;
 	}
 
 	public SpoutBlock getBlock() {
 		return sBlock;
 	}
 
 	public int getUseCost() {
 		return useCost;
 	}
 
 	public String getConnectedTo() {
 		return connectedTo;
 	}
 
 	public void setPincode(String pincode) {
 		this.pincode = pincode;
 	}
 
 	public void setBlock(SpoutBlock block) {
 		this.sBlock = block;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public void setClosetimer(int closetimer) {
 		this.closetimer = closetimer;
 	}
 
 	public void setCoowners(String coowners) {
 		this.coOwners = coowners;
 	}
 
 	public void setUsers(String users) {
 		this.users = users;
 	}
 
 	public void setUseCost(int useCost) {
 		this.useCost = useCost;
 	}
 
 	public void setConnectedTo(String connectedTo) {
 		this.connectedTo = connectedTo;
 	}
 
 	public void setDigiLock(SpoutBlock block, String pincode, String owner,
 			int closetimer, String coowners, String users, String connectedTo,
 			int useCost) {
 		this.sBlock = block;
 		this.pincode = pincode;
 		this.owner = owner;
 		this.closetimer = closetimer;
 		this.coOwners = coowners;
 		this.users = users;
 		this.typeId = block.getTypeId();
 		this.connectedTo = connectedTo;
 		this.useCost = useCost;
 	}
 
 	public static BITDigiLock loadDigiLock(SpoutBlock block) {
 		// TODO: fasten up the load of DigiLock, select from a HASHMAP second
 		// time
 		block = getDigiLockBlock(block);
 		String query = "SELECT * FROM " + BIT.digilockTable + " WHERE (x = "
 				+ block.getX() + " AND y = " + block.getY() + " AND z = "
 				+ block.getZ() + " AND world='" + block.getWorld().getName()
 				+ "');";
 		ResultSet result = null;
 		if (BITConfig.STORAGE_TYPE.equals("MYSQL")) {
 			try {
 				result = BIT.manageMySQL.sqlQuery(query);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
 		} else { // SQLLITE
 			result = BIT.manageSQLite.sqlQuery(query);
 		}
 
 		try {
 			if (result != null && result.next()) {
 				String pincode = result.getString("pincode");
 				String owner = result.getString("owner");
 				int closetimer = result.getInt("closetimer");
 				String coowners = result.getString("coowners");
 				String users = result.getString("users");
 				int typeId = result.getInt("typeId");
 				String connectedTo = result.getString("connectedto");
 				int useCost = result.getInt("usecost");
 				BITDigiLock digilock = new BITDigiLock(block, pincode, owner,
 						closetimer, coowners, users, typeId, connectedTo,
 						useCost);
 				return digilock;
 			} else {
 				return null;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void RemoveDigiLock(SpoutPlayer sPlayer) {
 		boolean deletelock = true;
 		if (BIT.useEconomy) {
 			if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 				if (BIT.plugin.Method.getAccount(sPlayer.getName()).hasEnough(
 						BITConfig.DIGILOCK_DESTROYCOST)
 						|| BITConfig.DIGILOCK_DESTROYCOST < 0) {
 					BIT.plugin.Method.getAccount(sPlayer.getName()).subtract(
 							BITConfig.DIGILOCK_DESTROYCOST);
 					sPlayer.sendMessage("Your account ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance()
 							+ ") has been deducted "
 							+ BIT.plugin.Method
 									.format(BITConfig.DIGILOCK_DESTROYCOST)
 							+ ".");
 				} else {
 					sPlayer.sendMessage("You dont have enough money ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance()
 							+ "). Cost is:"
 							+ BIT.plugin.Method
 									.format(BITConfig.DIGILOCK_DESTROYCOST));
 					deletelock = false;
 				}
 			}
 		}
 		String query = "DELETE FROM " + BIT.digilockTable + " WHERE (x = "
 				+ sBlock.getX() + " AND y = " + sBlock.getY() + " AND z = "
 				+ sBlock.getZ() + " AND world='" + sBlock.getWorld().getName()
 				+ "');";
 		if (deletelock) {
 			if (BITConfig.DEBUG_SQL)
 				sPlayer.sendMessage(ChatColor.YELLOW + "Removeing lock: "
 						+ query);
 			if (BITConfig.STORAGE_TYPE.equals("MYSQL")) {
 				try {
 					BIT.manageMySQL.deleteQuery(query);
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (InstantiationException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				}
 			} else { // SQLLITE
 				BIT.manageSQLite.deleteQuery(query);
 			}
 			BITMessages.sendNotification(sPlayer, "DigiLock removed.");
 		} else {
 			BITMessages.sendNotification(sPlayer, "You need more money ("
 					+ BITConfig.DIGILOCK_DESTROYCOST + ")");
 		}
 
 	}
 
 	public static void playDigiLockSound(SpoutBlock sBlock) {
 		SpoutManager.getSoundManager().playGlobalCustomSoundEffect(BIT.plugin,
 				BITConfig.DIGILOCK_SOUND, true, sBlock.getLocation(), 5);
 	}
 
 	public static boolean isNeighbourLocked(SpoutBlock block) {
 		if (block != null)
 			for (BlockFace bf : BlockFace.values()) {
 				if (isLocked(block.getRelative(bf))) {
 					return true;
 				}
 			}
 		return false;
 	}
 
 	public static boolean isNeighbourSameOwner(SpoutBlock block, String owner) {
 		for (BlockFace bf : BlockFace.values()) {
 			if (isLocked(block.getRelative(bf))) {
 				BITDigiLock digilock = BITDigiLock.loadDigiLock(block
 						.getFace(bf));
 				if (digilock.getOwner().equalsIgnoreCase(owner)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public SpoutBlock getNextLockedBlock(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		for (int i = -1; i < 1 + 1; i++) {
 			for (int j = -1; j < +1; j++) {
 				for (int k = -1; k < +1; k++) {
 					if (!(i == 0 && j == 0 && k == 0)) {
 						SpoutBlock sb = sBlock.getRelative(i, j, k);
 						if (
 						// BITDigiLock.isLockable(sb)
 						BITDigiLock.isLocked(sb)
 								&& (BITDigiLock.isDoubleDoor(sb)
 										|| BITDigiLock.isDoor(sb)
 										|| BITDigiLock.isTrapdoor(sb) || BITDigiLock
 											.isDispenser(sb))) {
 							return sb;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public SpoutBlock getNextLockableBlock(SpoutPlayer sPlayer,
 			SpoutBlock sBlock) {
 		for (int i = -1; i < 1 + 1; i++) {
 			for (int j = -1; j < +1; j++) {
 				for (int k = -1; k < +1; k++) {
 					if (!(i == 0 && j == 0 && k == 0)) {
 						SpoutBlock sb = sBlock.getRelative(i, j, k);
 						if (BITDigiLock.isLockable(sb)
 						// BITDigiLock.isLocked(sb)
 								&& (BITDigiLock.isDoubleDoor(sb)
 										|| BITDigiLock.isDoor(sb)
 										|| BITDigiLock.isTrapdoor(sb) || BITDigiLock
 											.isDispenser(sb))) {
 							return sb;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	// *******************************************************
 	//
 	// GUI for the DigiLock
 	//
 	// *******************************************************
 	// USERDATA DigiLock
 	public static Map<Integer, PopupScreen> popupScreen = new HashMap<Integer, PopupScreen>();
 	public static Map<Integer, Integer> userno = new HashMap<Integer, Integer>();
 	public static Map<Integer, GenericTextField> pincodeGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> ownerGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> closetimerGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> coOwnersGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> usersGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> useCostGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, GenericTextField> connectedToGUI = new HashMap<Integer, GenericTextField>();
 	public static Map<Integer, SpoutBlock> clickedBlock = new HashMap<Integer, SpoutBlock>();
 
 	// Buttons for DigiLock
 	public static HashMap<UUID, String> BITDigiLockButtons = new HashMap<UUID, String>();
 
 	/**
 	 * 
 	 * @param sPlayer
 	 */
 	public static void cleanupPopupScreen(SpoutPlayer sPlayer) {
 		int playerId = sPlayer.getEntityId();
 		if (popupScreen.containsKey(playerId)) {
 			popupScreen.get(playerId).removeWidgets(BIT.plugin);
 			popupScreen.get(playerId).setDirty(true);
 			sPlayer.getMainScreen().removeWidgets(BIT.plugin);
 			BITDigiLock.clickedBlock.remove(sPlayer.getEntityId());
 		}
 	}
 
 	/**
 	 * 
 	 * @param sBlock
 	 * @return
 	 */
 	public static int getPincodeBlock(SpoutBlock sBlock) {
 		switch (sBlock.getTypeId()) {
 		case 23:
 			return 23; // Dispenser - looks nice.
 		case 47:
 			return 47; // Bookshelf - looks nice.
 		case 54:
 			return 95; // Chest - looks nice.
 		case 61:
 			return 61; // Furnace - looks nice.
 		case 62:
 			return 62; // Burning Furnace
 		case 63:
 			return 95; // SIGN_POST
 		case 64:
 			// return 324; // Wooden door
 			return 95;
 		case 68:
 			return 68;
 		case 69:
 			// return 69; // Lever
 			return 95;
 		case 71:
 			// return 330; // Iron door
 			return 95;
 		case 77:
 			// return 77; // Stone button
 			return 95;
 		case 96:
 			return 95; // Trap_door
 		case 107:
 			return 95; // FENCEGATE
 		}
 		return 95;
 	}
 
 	/**
 	 * 
 	 * @param sBlock
 	 * @return
 	 */
 
 	// ***************************************************************
 	//
 	// getPincode: Open GenericPopup and ask for pincode before to
 	// unlock the inventory.
 	//
 	// ***************************************************************
 	/**
 	 * Open Generic PopupScreen and ask for the pincode.
 	 * 
 	 * @param sPlayer
 	 * @param sBlock
 	 * 
 	 * @author Gabriel333 / Rocologo
 	 */
 	public static void getPincode(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		int y = 50, height = 20, width = 100;
 		int x = 170;
 		int id = sPlayer.getEntityId();
 		addUserData(id);
 		clickedBlock.put(id, sBlock);
 
 		GenericItemWidget itemwidget = new GenericItemWidget(new ItemStack(
 				getPincodeBlock(sBlock)));
 		itemwidget.setX(x + 2 * height).setY(y);
 		itemwidget.setHeight(height * 2).setWidth(height * 2)
 				.setDepth(height * 2);
 		itemwidget.setTooltip("Locked inventory");
 		itemwidget.setMargin(5).setFixed(false);
 		popupScreen.get(id).attachWidget(BIT.plugin, itemwidget);
 		y = y + 3 * height;
 
 		pincodeGUI.get(id).setText("");
 		pincodeGUI.get(id).setTooltip("Enter the pincode and press unlock.");
 		pincodeGUI.get(id).setCursorPosition(1).setMaximumCharacters(20);
 		pincodeGUI.get(id).setX(x).setY(y);
 		pincodeGUI.get(id).setHeight(height).setWidth(width);
 		pincodeGUI.get(id).setPasswordField(true);
 		pincodeGUI.get(id).setFocus(true);
 		popupScreen.get(id).attachWidget(BIT.plugin, pincodeGUI.get(id));
 		y = y + height;
 
 		GenericButton unlockButton = new GenericButton("Unlock");
 		unlockButton.setAuto(false).setX(x).setY(y).setHeight(height)
 				.setWidth(width);
 		BITDigiLockButtons.put(unlockButton.getId(), "getPincodeUnlock");
 		popupScreen.get(id).attachWidget(BIT.plugin, unlockButton);
 
 		GenericButton cancelButton = new GenericButton("Cancel");
 		cancelButton.setAuto(false).setX(x + width + 10).setY(y)
 				.setHeight(height).setWidth(width);
 		popupScreen.get(id).attachWidget(BIT.plugin, cancelButton);
 		BITDigiLockButtons.put(cancelButton.getId(), "getPincodeCancel");
 
 		// Open Window
 		popupScreen.get(id).setTransparent(true);
 		sPlayer.getMainScreen().attachPopupScreen(popupScreen.get(id));
 	}
 
 	/**
 	 * setPincode - Open GenericPopup and enter a pincode to lock the inventory.
 	 * 
 	 * @param sPlayer
 	 * @param sBlock
 	 */
 	public static void setPincode(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		int id = sPlayer.getEntityId();
 		int height = 20;
 		int x, y, w1, w2, w3, w4;
 		addUserData(id);
 		clickedBlock.put(id, sBlock);
 		if (BITDigiLock.isLocked(sBlock)) {
 			BITDigiLock digilock = BITDigiLock.loadDigiLock(sBlock);
 			pincodeGUI.get(id).setText(digilock.getPincode());
 			ownerGUI.get(id).setText(digilock.getOwner());
 			coOwnersGUI.get(id).setText(digilock.getCoOwners());
 			usersGUI.get(id).setText(digilock.getUsers());
 			closetimerGUI.get(id).setText(
 					Integer.toString(digilock.getClosetimer()));
 			useCostGUI.get(id).setText(Integer.toString(digilock.getUseCost()));
 		} else {
 			pincodeGUI.get(id).setText("");
 			ownerGUI.get(id).setText(sPlayer.getName());
 			coOwnersGUI.get(id).setText("");
 			usersGUI.get(id).setText("");
 			closetimerGUI.get(id).setText("0");
 			useCostGUI.get(id).setText("0");
 		}
 
 		// GIF
 		x = 170;
 		y = 50;
 		GenericItemWidget itemwidget = new GenericItemWidget(new ItemStack(
 				getPincodeBlock(sBlock)));
 		itemwidget.setX(x + 2 * height).setY(y);
 		itemwidget.setHeight(height * 2).setWidth(height * 2)
 				.setDepth(height * 2);
 		if (!BITDigiLock.isLocked(sBlock)) {
 			itemwidget.setTooltip("Unlocked inventory");
 		} else {
 			itemwidget.setTooltip("Locked inventory");
 		}
 		popupScreen.get(id).attachWidget(BIT.plugin, itemwidget);
 		y = y + 3 * height;
 
 		GenericLabel costToCreate = new GenericLabel("CostToCreate: "
 				+ String.valueOf(BITConfig.DIGILOCK_COST));
 		costToCreate.setAuto(true).setX(175).setY(y - 10).setHeight(10)
 				.setWidth(140);
 		costToCreate.setTooltip("The cost to create a new DigiLock");
 		popupScreen.get(id).attachWidget(BIT.plugin, costToCreate);
 
 		// first row -------- x=20-170-------------------------------------
 		x = 10;
 		w1 = 60;
 		w2 = 80;
 		w3 = 50;
 		w4 = 50;
 
 		y = 165;
 		// ownerButton
 		GenericButton ownerButton = new GenericButton("Owner");
 		ownerButton.setAuto(false).setX(x).setY(y).setHeight(height)
 				.setWidth(w1);
 		ownerButton.setTooltip("Set Owner");
 		popupScreen.get(id).attachWidget(BIT.plugin, ownerButton);
 		BITDigiLockButtons.put(ownerButton.getId(), "OwnerButton");
 		// owner1
 		ownerGUI.get(id).setTooltip("Owner of the DigiLock");
 		ownerGUI.get(id).setCursorPosition(1).setMaximumCharacters(20);
 		ownerGUI.get(id).setX(x + w1 + 1).setY(y);
 		ownerGUI.get(id).setHeight(height).setWidth(w2);
 		popupScreen.get(id).attachWidget(BIT.plugin, ownerGUI.get(id));
 
 		// closetimerButton
 		GenericButton closetimerButton = new GenericButton("Closetimer");
 		closetimerButton.setAuto(false).setX(x + w1 + w2 + 10).setY(y)
 				.setHeight(height).setWidth(w1);
 		closetimerButton.setTooltip("Set closetimer");
 		popupScreen.get(id).attachWidget(BIT.plugin, closetimerButton);
 		BITDigiLockButtons.put(closetimerButton.getId(), "ClosetimerButton");
 		// closetimer
 		closetimerGUI.get(id).setTooltip("Autoclosing time in sec.");
 		closetimerGUI.get(id).setCursorPosition(1).setMaximumCharacters(4);
 		closetimerGUI.get(id).setX(x + w1 + 1 + w2 + 10 + w1 + 1).setY(y);
 		closetimerGUI.get(id).setHeight(height).setWidth(w3);
 		popupScreen.get(id).attachWidget(BIT.plugin, closetimerGUI.get(id));
 
 		// useCostButton
 		GenericButton useCostButton = new GenericButton("Use cost");
 		useCostButton.setAuto(false).setX(x + w1 + w2 + 10 + w1 + w3 + 10)
 				.setY(y).setHeight(height).setWidth(w1);
 		useCostButton.setTooltip("Set cost");
 		popupScreen.get(id).attachWidget(BIT.plugin, useCostButton);
 		BITDigiLockButtons.put(useCostButton.getId(), "UseCostButton");
 		// useCost1
 		useCostGUI.get(id).setTooltip("This is the cost to use the DigiLock");
 		useCostGUI.get(id).setCursorPosition(1).setMaximumCharacters(4);
 		useCostGUI.get(id).setX(x + w1 + w2 + 10 + w1 + w3 + 10 + w1 + 1)
 				.setY(y);
 		useCostGUI.get(id).setHeight(height).setWidth(w4);
 		popupScreen.get(id).attachWidget(BIT.plugin, useCostGUI.get(id));
 		y = y + height + 1;
 
 		// setCoOwnerButton
 		GenericButton CoOwnerButton = new GenericButton("CoOwners");
 		CoOwnerButton.setAuto(false).setX(x).setY(y).setHeight(height)
 				.setWidth(w1);
 		CoOwnerButton.setTooltip("CoOwners must be seperated by a comma.");
 		popupScreen.get(id).attachWidget(BIT.plugin, CoOwnerButton);
 		BITDigiLockButtons.put(CoOwnerButton.getId(), "CoOwnerButton");
 		// listOfCoOwners
 		coOwnersGUI.get(id).setX(x + w1 + 1).setY(y).setWidth(340)
 				.setHeight(height);
 		coOwnersGUI.get(id).setMaximumCharacters(200);
 		coOwnersGUI.get(id).setText(coOwnersGUI.get(id).getText());
 		popupScreen.get(id).attachWidget(BIT.plugin, coOwnersGUI.get(id));
 		y = y + height;
 
 		// setUsersButton
 		GenericButton usersButton = new GenericButton("Users");
 		usersButton.setAuto(false).setX(x).setY(y).setHeight(height)
 				.setWidth(w1);
 		usersButton.setTooltip("users must be seperated by a comma.");
 		popupScreen.get(id).attachWidget(BIT.plugin, usersButton);
 		BITDigiLockButtons.put(usersButton.getId(), "usersButton");
 		// listOfUsers
 		usersGUI.get(id).setX(x + w1 + 1).setY(y).setWidth(340)
 				.setHeight(height);
 		usersGUI.get(id).setMaximumCharacters(200);
 		usersGUI.get(id).setText(usersGUI.get(id).getText());
 		popupScreen.get(id).attachWidget(BIT.plugin, usersGUI.get(id));
 		y = y + height;
 
 		// Second row ------------X=170-270-370------------------------------
 		y = 110;
 		x = 180;
 		w1 = 80;
 		w2 = 80;
 		// pincode3
 		pincodeGUI.get(id).setTooltip("Enter/change the pincode...");
 		pincodeGUI.get(id).setCursorPosition(1).setMaximumCharacters(20);
 		pincodeGUI.get(id).setX(x).setY(y);
 		pincodeGUI.get(id).setHeight(height).setWidth(w1);
 		pincodeGUI.get(id).setPasswordField(false);
 		pincodeGUI.get(id).setFocus(true);
 		popupScreen.get(id).attachWidget(BIT.plugin, pincodeGUI.get(id));
 		y = y + height;
 
 		// lockButton
 		GenericButton lockButton = new GenericButton("Lock");
 		lockButton.setAuto(false).setX(x).setY(y).setHeight(height)
 				.setWidth(w1);
 		lockButton.setTooltip("Enter/change the pincode and press lock.");
 		popupScreen.get(id).attachWidget(BIT.plugin, lockButton);
 		BITDigiLockButtons.put(lockButton.getId(), "setPincodeLock");
 
 		// cancelButton
 		GenericButton cancelButton2 = new GenericButton("Cancel");
 		cancelButton2.setAuto(false).setX(x + w1 + 10).setY(y)
 				.setHeight(height).setWidth(w1);
 		popupScreen.get(id).attachWidget(BIT.plugin, cancelButton2);
 		BITDigiLockButtons.put(cancelButton2.getId(), "setPincodeCancel");
 
 		// removeButton
 		if (BITDigiLock.isLocked(sBlock)) {
 			GenericButton removeButton = new GenericButton("Remove");
 			removeButton.setAuto(false).setX(x - w1 - 10).setY(y)
 					.setHeight(height).setWidth(w1);
 			removeButton.setTooltip("Press Remove to delete the lock.");
 			removeButton.setEnabled(true);
 			BITDigiLockButtons.put(removeButton.getId(), "setPincodeRemove");
 			popupScreen.get(id).attachWidget(BIT.plugin, removeButton);
 		}
 
 		// Open Window
 		// popupScreen.get(id).setDirty(true);
 		popupScreen.get(id).setTransparent(true);
 		sPlayer.getMainScreen().attachPopupScreen(popupScreen.get(id));
 
 	}
 
 	public static void removeUserData(int id) {
 		if (userno.containsKey(id)) {
 			// DigiLock
 			popupScreen.remove(id);
 			pincodeGUI.remove(id);
 			ownerGUI.remove(id);
 			coOwnersGUI.remove(id);
 			usersGUI.remove(id);
 			closetimerGUI.remove(id);
 			useCostGUI.remove(id);
 			connectedToGUI.remove(id);
 			userno.remove(id);
 			clickedBlock.remove(id);
 		}
 	}
 
 	public static void addUserData(int id) {
 		if (!userno.containsKey(id)) {
 			// DigiLock
 			userno.put(id, new Integer(id));
 			popupScreen.put(id, new GenericPopup());
 			pincodeGUI.put(id, new GenericTextField());
 			ownerGUI.put(id, new GenericTextField());
 			coOwnersGUI.put(id, new GenericTextField());
 			usersGUI.put(id, new GenericTextField());
 			closetimerGUI.put(id, new GenericTextField());
 			useCostGUI.put(id, new GenericTextField());
 			connectedToGUI.put(id, new GenericTextField());
 			clickedBlock.put(id, null);
 
 		}
 	}
 
 	// *******************************************************
 	//
 	// CHEST
 	//
 	// *******************************************************
 	public static boolean isChest(Block block) {
 		if (block != null)
 			if (block.getType().equals(Material.CHEST)
 					|| block.getType().equals(Material.LOCKED_CHEST))
 				return true;
 		return false;
 	}
 
 	// *******************************************************
 	//
 	// SIGN
 	//
 	// *******************************************************
 	public static boolean isSign(Block block) {
 		if (block != null)
 			if (block.getType().equals(Material.SIGN)
 					|| block.getType().equals(Material.WALL_SIGN)
 					|| block.getType().equals(Material.SIGN_POST))
 				return true;
 		return false;
 	}
 
 	// *******************************************************
 	//
 	// BOOKSHELF
 	//
 	// *******************************************************
 	public static boolean isBookshelf(SpoutBlock sBlock) {
 		if (sBlock != null)
 			if (sBlock.getType().equals(Material.BOOKSHELF))
 				return true;
 		return false;
 	}
 
 	// *******************************************************
 	//
 	// STONE_BUTTON
 	//
 	// *******************************************************
 	/**
 	 * Check if sBlock is a STONE_BUTTON
 	 * 
 	 * @param sBlock
 	 * @return true or false
 	 */
 	public static boolean isButton(SpoutBlock sBlock) {
 		if (sBlock != null)
 			if (sBlock.getType().equals(Material.STONE_BUTTON))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Check if the STONE_BUTTON is on.
 	 * 
 	 * @param block
 	 * @return
 	 */
 	public static boolean isButtonOn(SpoutBlock block) {
 		Button button = (Button) block.getState().getData();
 		return button.isPowered();
 	}
 
 	/**
 	 * Handle the actions when a player presses the STONE_BUTTON
 	 * 
 	 * @param sPlayer
 	 *            SpoutPlayer
 	 * @param sBlock
 	 *            SpoutBlock
 	 * @param cost
 	 *            the cost the player is charged when the button is pressed.
 	 */
 	public static void pressButtonOn(SpoutPlayer sPlayer, SpoutBlock sBlock,
 			int cost) {
 		boolean doTheWork = false;
 		BITDigiLock digilock = BITDigiLock.loadDigiLock(sBlock);
 		SpoutBlock nextBlock = digilock.getNextLockedBlock(sPlayer, sBlock);
 		if (nextBlock != null) {
 			BITDigiLock nextDigilock = BITDigiLock.loadDigiLock(nextBlock);
 			if (digilock.getOwner().equalsIgnoreCase(nextDigilock.getOwner())) {
 				doTheWork = true;
 			} else {
 				sPlayer.sendMessage("You are not the owner of the "
 						+ nextBlock.getType());
 			}
 			boolean pressButton = true;
 			if (BIT.useEconomy
 					&& cost > 0
 					&& doTheWork
 					&& digilock.isUser(sPlayer)
 					&& !(digilock.isOwner(sPlayer) || digilock
 							.isCoowner(sPlayer))) {
 				if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 					if (BIT.plugin.Method.getAccount(sPlayer.getName())
 							.hasEnough(cost)) {
 						BIT.plugin.Method.getAccount(sPlayer.getName())
 								.subtract(cost);
 						if (BIT.plugin.Method.hasAccount(nextDigilock
 								.getOwner())) {
 							BIT.plugin.Method.getAccount(
 									nextDigilock.getOwner()).add(cost);
 						}
 
 						sPlayer.sendMessage("Your account ("
 								+ BIT.plugin.Method.getAccount(
 										sPlayer.getName()).balance()
 								+ ") has been deducted "
 								+ BIT.plugin.Method.format(cost) + ".");
 					} else {
 						sPlayer.sendMessage("You dont have enough money ("
 								+ BIT.plugin.Method.getAccount(
 										sPlayer.getName()).balance()
 								+ "). Cost is:"
 								+ BIT.plugin.Method.format(cost));
 						pressButton = false;
 					}
 				}
 			}
 			if (pressButton && doTheWork) {
 				Button button = (Button) sBlock.getState().getData();
 				button.setPowered(true);
 				// x | 8 ^ 8 = 0
 				// sBlock.setData((byte) ((lever.getData() | 8) ^ 8));
 				if (BITDigiLock.isDoubleDoor(nextBlock)) {
 					BITDigiLock.openDoubleDoor(sPlayer, nextBlock, 0);
 					BITDigiLock.scheduleCloseDoubleDoor(sPlayer, nextBlock, 5,
 							0);
 				} else if (BITDigiLock.isDoor(nextBlock)) {
 					BITDigiLock.openDoor(sPlayer, nextBlock, 0);
 					BITDigiLock.scheduleCloseDoor(sPlayer, nextBlock, 5, 0);
 				} else if (BITDigiLock.isTrapdoor(nextBlock)) {
 					BITDigiLock.openTrapdoor(sPlayer, nextBlock, 0);
 					BITDigiLock.scheduleCloseTrapdoor(sPlayer, nextBlock, 5);
 				} else if (BITDigiLock.isDispenser(nextBlock)) {
 					Dispenser dispenser = (Dispenser) nextBlock.getState();
 					dispenser.dispense();
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * Check if sBlock is a DISPENSER.
 	 * 
 	 * @param sBlock
 	 * @return
 	 */
 	public static boolean isDispenser(SpoutBlock sBlock) {
 		if (sBlock != null)
 			if (sBlock.getType().equals(Material.DISPENSER))
 				return true;
 		return false;
 	}
 
 	// *******************************************************
 	//
 	// DOORS
 	//
 	// *******************************************************
 
 	/**
 	 * Check if sBlock is a LEVER.
 	 * 
 	 * @param sBlock
 	 * @return
 	 */
 	public static boolean isLever(SpoutBlock sBlock) {
 		if (sBlock != null)
 			if (sBlock.getType().equals(Material.LEVER))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Set the LEVER on - on sBlock.
 	 * 
 	 * @param sBlock
 	 * @return
 	 */
 	public static boolean isLeverOn(SpoutBlock sBlock) {
 		Lever lever = (Lever) sBlock.getState().getData();
 		return lever.isPowered();
 	}
 
 	public static void leverOn(SpoutPlayer sPlayer, SpoutBlock sBlock, int cost) {
 		boolean doTheWork = false;
 		BITDigiLock digilock = BITDigiLock.loadDigiLock(sBlock);
 		if (digilock != null) {
 			SpoutBlock nextBlock = digilock.getNextLockedBlock(sPlayer, sBlock);
 			if (nextBlock != null) {
 				BITDigiLock nextDigilock = BITDigiLock.loadDigiLock(nextBlock);
 
 				if (digilock.getOwner().equalsIgnoreCase(
 						nextDigilock.getOwner())) {
 					doTheWork = true;
 				} else {
 					sPlayer.sendMessage("You are not the owner of the "
 							+ nextBlock.getType());
 				}
 				boolean setleveron = true;
 				if (BIT.useEconomy
 						&& cost > 0
 						&& doTheWork
 						&& digilock.isUser(sPlayer)
 						&& !(digilock.isOwner(sPlayer) || digilock
 								.isCoowner(sPlayer))) {
 					if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 						if (BIT.plugin.Method.getAccount(sPlayer.getName())
 								.hasEnough(cost)) {
 							BIT.plugin.Method.getAccount(sPlayer.getName())
 									.subtract(cost);
 							if (BIT.plugin.Method.hasAccount(nextDigilock
 									.getOwner())) {
 								BIT.plugin.Method.getAccount(
 										nextDigilock.getOwner()).add(cost);
 							}
 							sPlayer.sendMessage("Your account ("
 									+ BIT.plugin.Method.getAccount(
 											sPlayer.getName()).balance()
 									+ ") has been deducted "
 									+ BIT.plugin.Method.format(cost) + ".");
 						} else {
 							sPlayer.sendMessage("You dont have enough money ("
 									+ BIT.plugin.Method.getAccount(
 											sPlayer.getName()).balance()
 									+ "). Cost is:"
 									+ BIT.plugin.Method.format(cost));
 							setleveron = false;
 						}
 					}
 				}
 				if (setleveron && doTheWork) {
 
 					Lever lever = (Lever) sBlock.getState().getData();
 					lever.setPowered(true);
 					// x | 8 ^ 8 = 0
 					// sBlock.setData((byte) ((lever.getData() | 8) ^ 8));
 
 					if (BITDigiLock.isDoubleDoor(nextBlock)) {
 						BITDigiLock.openDoubleDoor(sPlayer, nextBlock, 0);
 					} else if (BITDigiLock.isDoor(nextBlock)) {
 						BITDigiLock.openDoor(sPlayer, nextBlock, 0);
 					} else if (BITDigiLock.isTrapdoor(nextBlock)) {
 						BITDigiLock.openTrapdoor(sPlayer, nextBlock, 0);
 					} else if (nextBlock.getType().equals(Material.DISPENSER)) {
 						Dispenser dispenser = (Dispenser) nextBlock.getState();
 						dispenser.dispense();
 					}
 					if (digilock.getClosetimer() > 0) {
 						scheduleLeverOff(sPlayer, sBlock,
 								digilock.getClosetimer());
 					}
 				}
 			}
 		}
 	}
 
 	public static void leverOff(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		if (BITDigiLock.isLeverOn(sBlock)) {
 			BITDigiLock digilock = BITDigiLock.loadDigiLock(sBlock);
 			if (digilock != null) {
 				SpoutBlock nextBlock = digilock.getNextLockedBlock(sPlayer,
 						sBlock);
 				if (nextBlock != null) {
 					BITDigiLock nextDigilock = BITDigiLock
 							.loadDigiLock(nextBlock);
 					if (digilock.getOwner().equalsIgnoreCase(
 							nextDigilock.getOwner())) {
 						Lever lever = (Lever) sBlock.getState().getData();
 						lever.setPowered(false);
 						// sBlock.setData((byte) (lever.getData() |8));
 						if (BITDigiLock.isDoubleDoor(nextBlock)) {
 							BITDigiLock.closeDoubleDoor(sPlayer, nextBlock, 0);
 						} else if (BITDigiLock.isDoor(nextBlock)) {
 							BITDigiLock.closeDoor(sPlayer, nextBlock, 0);
 						} else if (BITDigiLock.isTrapdoor(nextBlock)) {
 							BITDigiLock.closeTrapdoor(sPlayer, nextBlock);
 						}
 					} else {
 						sPlayer.sendMessage("You are not the owner of the "
 								+ nextBlock.getType());
 					}
 				}
 			}
 		}
 	}
 
 	//
 	public static int scheduleLeverOff(final SpoutPlayer sPlayer,
 			final SpoutBlock sBlock, final int closetimer) {
 		int fs = closetimer * 20;
 		// 20 ticks / second
 		int taskID = BIT.plugin.getServer().getScheduler()
 				.scheduleSyncDelayedTask(BIT.plugin, new Runnable() {
 					public void run() {
 						SpoutBlock sb = sBlock;
 						SpoutPlayer sp = sPlayer;
 						if (BITConfig.DEBUG_DOOR)
 							sp.sendMessage("Turning lever off in " + closetimer
 									+ " seconds");
 						if (BITDigiLock.isLeverOn(sBlock))
 							BITDigiLock.leverOff(sp, sb);
 					}
 				}, fs);
 		return taskID;
 	}
 
 	// *******************************************************
 	//
 	// DOORS
 	//
 	// *******************************************************
 
 	public static boolean isDoor(Block block) {
 		if (block != null)
 			if (block.getType().equals(Material.WOOD_DOOR))
 				return true;
 			else if (block.getType().equals(Material.WOODEN_DOOR))
 				return true;
 			else if (block.getType().equals(Material.IRON_DOOR))
 				return true;
 			else if (block.getType().equals(Material.IRON_DOOR_BLOCK))
 				return true;
 		return false;
 	}
 
 	public static boolean isDoorOpen(SpoutBlock sBlock) {
 		if ((sBlock.getState().getData().getData() & 4) == 4) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static void openDoor(SpoutPlayer sPlayer, SpoutBlock sBlock, int cost) {
 		boolean opendoor = true;
 		BITDigiLock digilock = loadDigiLock(sBlock);
 		if (BIT.useEconomy && cost > 0 && digilock.isUser(sPlayer)
 				&& !(digilock.isOwner(sPlayer) || digilock.isCoowner(sPlayer))) {
 			if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 				if (BIT.plugin.Method.getAccount(sPlayer.getName()).hasEnough(
 						cost)) {
 					BIT.plugin.Method.getAccount(sPlayer.getName()).subtract(
 							cost);
 					if (BIT.plugin.Method.hasAccount(digilock.getOwner())) {
 						BIT.plugin.Method.getAccount(digilock.getOwner()).add(
 								cost);
 					}
 					sPlayer.sendMessage("Your account ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + ") has been deducted "
 							+ BIT.plugin.Method.format(cost) + ".");
 				} else {
 					sPlayer.sendMessage("You dont have enough money ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + "). Cost is:"
 							+ BIT.plugin.Method.format(cost));
 					opendoor = false;
 				}
 
 			}
 		}
 		Door door = (Door) sBlock.getState().getData();
 		SpoutBlock nextBlock;
 		if (opendoor) {
 			if (!isDoorOpen(sBlock)) {
 				playDigiLockSound(sBlock);
 				sBlock.setData((byte) (sBlock.getState().getData().getData() | 4));
 				if (door.isTopHalf()) {
 					nextBlock = sBlock.getRelative(BlockFace.DOWN);
 					nextBlock.setData((byte) (nextBlock.getState().getData()
 							.getData() | 4));
 				} else {
 					nextBlock = sBlock.getRelative(BlockFace.UP);
 					nextBlock.setData((byte) (nextBlock.getState().getData()
 							.getData() | 4));
 				}
 				if (digilock != null) {
 					if (digilock.getClosetimer() > 0 && !isDoubleDoor(sBlock)) {
 						scheduleCloseDoor(sPlayer, sBlock,
 								digilock.getClosetimer(), 0);
 					}
 				}
 			}
 		}
 	}
 
 	public static void closeDoor(SpoutPlayer sPlayer, SpoutBlock sBlock,
 			int cost) {
 		boolean closedoor = true;
 		BITDigiLock digilock = loadDigiLock(sBlock);
 		if (BIT.useEconomy && cost > 0 && digilock.isUser(sPlayer)
 				&& !(digilock.isOwner(sPlayer) || digilock.isCoowner(sPlayer))) {
 			if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 				if (BIT.plugin.Method.getAccount(sPlayer.getName()).hasEnough(
 						cost)) {
 					BIT.plugin.Method.getAccount(sPlayer.getName()).subtract(
 							cost);
 					if (BIT.plugin.Method.hasAccount(digilock.getOwner())) {
 						BIT.plugin.Method.getAccount(digilock.getOwner()).add(
 								cost);
 					}
 
 					sPlayer.sendMessage("Your account ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + ") has been deducted "
 							+ BIT.plugin.Method.format(cost) + ".");
 				} else {
 					sPlayer.sendMessage("You dont have enough money ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + "). Cost is:"
 							+ BIT.plugin.Method.format(cost));
 					closedoor = false;
 				}
 			}
 		}
 		if (closedoor) {
 			if (isDoorOpen(sBlock)) {
 				playDigiLockSound(sBlock);
 				Door door = (Door) sBlock.getState().getData();
 				SpoutBlock nextBlock;
 				sBlock.setData((byte) ((sBlock.getState().getData().getData() | 4) ^ 4));
 				if (door.isTopHalf()) {
 					nextBlock = sBlock.getRelative(BlockFace.DOWN);
 					nextBlock.setData((byte) ((nextBlock.getState().getData()
 							.getData() | 4) ^ 4));
 				} else {
 					nextBlock = sBlock.getRelative(BlockFace.UP);
 					nextBlock.setData((byte) ((nextBlock.getState().getData()
 							.getData() | 4) ^ 4));
 				}
 			}
 		}
 	}
 
 	public static void closeDoor(SpoutBlock sBlock) {
 		if (isDoorOpen(sBlock)) {
 			playDigiLockSound(sBlock);
 			Door door = (Door) sBlock.getState().getData();
 			SpoutBlock nextBlock;
 			sBlock.setData((byte) ((sBlock.getState().getData().getData() | 4) ^ 4));
 			if (door.isTopHalf()) {
 				nextBlock = sBlock.getRelative(BlockFace.DOWN);
 				nextBlock.setData((byte) ((nextBlock.getState().getData()
 						.getData() | 4) ^ 4));
 			} else {
 				nextBlock = sBlock.getRelative(BlockFace.UP);
 				nextBlock.setData((byte) ((nextBlock.getState().getData()
 						.getData() | 4) ^ 4));
 			}
 		}
 	}
 
 	public static void toggleDoor(SpoutBlock sBlock) {
 		playDigiLockSound(sBlock);
 		Door door = (Door) sBlock.getState().getData();
 		SpoutBlock nextBlock;
 		sBlock.setData((byte) (sBlock.getState().getData().getData() ^ 4));
 		if (door.isTopHalf()) {
 			nextBlock = sBlock.getRelative(BlockFace.DOWN);
 			nextBlock
 					.setData((byte) (nextBlock.getState().getData().getData() ^ 4));
 		} else {
 			nextBlock = sBlock.getRelative(BlockFace.UP);
 			nextBlock
 					.setData((byte) (nextBlock.getState().getData().getData() ^ 4));
 		}
 	}
 
 	public static int scheduleCloseDoor(final SpoutPlayer sPlayer,
 			final SpoutBlock sBlock, final int closetimer, final int cost) {
 		int fs = closetimer * 20;
 		// 20 ticks / second
 		int taskID = BIT.plugin.getServer().getScheduler()
 				.scheduleSyncDelayedTask(BIT.plugin, new Runnable() {
 					public void run() {
 						SpoutBlock sb = sBlock;
 						SpoutPlayer sp = sPlayer;
 						int c = cost;
 						if (BITConfig.DEBUG_DOOR)
 							sp.sendMessage("Autoclosing the door in "
 									+ closetimer + " seconds");
 						if (isDoor(sb) && !isDoubleDoor(sb)) {
 							if (isDoorOpen(sb)) {
 								closeDoor(sp, sb, c);
 								playDigiLockSound(sBlock);
 							}
 						}
 					}
 				}, fs);
 		return taskID;
 	}
 
 	// *******************************************************
 	//
 	// TRAPDOORS
 	//
 	// *******************************************************
 
 	public static boolean isTrapdoor(Block block) {
 		if (block != null)
 			if (block.getType().equals(Material.TRAP_DOOR))
 				return true;
 		return false;
 	}
 
 	public static boolean isTrapdoorOpen(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		if ((sBlock.getState().getData().getData() & 4) == 4) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static void openTrapdoor(SpoutPlayer sPlayer, SpoutBlock sBlock,
 			int cost) {
 		boolean opentrapdoor = true;
 		BITDigiLock digilock = loadDigiLock(sBlock);
 		if (BIT.useEconomy && cost > 0 && digilock.isUser(sPlayer)
 				&& !(digilock.isOwner(sPlayer) || digilock.isCoowner(sPlayer))) {
 			if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 				if (BIT.plugin.Method.getAccount(sPlayer.getName()).hasEnough(
 						cost)) {
 					BIT.plugin.Method.getAccount(sPlayer.getName()).subtract(
 							cost);
 					if (BIT.plugin.Method.hasAccount(digilock.getOwner())) {
 						BIT.plugin.Method.getAccount(digilock.getOwner()).add(
 								cost);
 					}
 					sPlayer.sendMessage("Your account ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + ") has been deducted "
 							+ BIT.plugin.Method.format(cost) + ".");
 				} else {
 					sPlayer.sendMessage("You dont have enough money ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + "). Cost is:"
 							+ BIT.plugin.Method.format(cost));
 					opentrapdoor = false;
 				}
 			}
 		}
 		if (opentrapdoor) {
 			if (!isTrapdoorOpen(sPlayer, sBlock)) {
 				sBlock.setData((byte) (sBlock.getState().getData().getData() | 4));
 				if (digilock != null) {
 					playDigiLockSound(sBlock);
 					if (digilock.getClosetimer() > 0) {
 						scheduleCloseTrapdoor(sPlayer, sBlock,
 								digilock.getClosetimer());
 					}
 				}
 			}
 		}
 	}
 
 	public static void closeTrapdoor(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		sBlock.setData((byte) ((sBlock.getState().getData().getData() | 4) ^ 4));
 	}
 
 	public static void toggleTrapdoor(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		sBlock.setData((byte) (sBlock.getState().getData().getData() ^ 4));
 	}
 
 	public static int scheduleCloseTrapdoor(final SpoutPlayer sPlayer,
 			final SpoutBlock sBlock, final int closetimer) {
 		int fs = closetimer * 20;
 		// 20 ticks / second
 		int taskID = BIT.plugin.getServer().getScheduler()
 				.scheduleSyncDelayedTask(BIT.plugin, new Runnable() {
 					public void run() {
 						SpoutBlock sb = sBlock;
 						SpoutPlayer sp = sPlayer;
 						if (BITConfig.DEBUG_DOOR)
 							sp.sendMessage("Autoclosing the trapdoor in "
 									+ closetimer + " seconds");
 						if (sBlock.getType() == Material.TRAP_DOOR) {
 							if (isTrapdoorOpen(sp, sb)) {
 								closeTrapdoor(sp, sb);
 								playDigiLockSound(sBlock);
 							}
 						}
 					}
 				}, fs);
 		return taskID;
 	}
 
 	// *******************************************************
 	//
 	// FENCE_GATE
 	//
 	// *******************************************************
 
 	public static boolean isFenceGate(Block block) {
 		if (block != null)
 			if (block.getType().equals(Material.FENCE_GATE))
 				return true;
 		return false;
 	}
 
 	public static boolean isFenceGateOpen(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		if ((sBlock.getState().getData().getData() & 4) == 4) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static void openFenceGate(SpoutPlayer sPlayer, SpoutBlock sBlock,
 			int cost) {
 		boolean openFenceGate = true;
 		BITDigiLock digilock = loadDigiLock(sBlock);
 		if (BIT.useEconomy && cost > 0 && digilock.isUser(sPlayer)
 				&& !(digilock.isOwner(sPlayer) || digilock.isCoowner(sPlayer))) {
 			if (BIT.plugin.Method.hasAccount(sPlayer.getName())) {
 				if (BIT.plugin.Method.getAccount(sPlayer.getName()).hasEnough(
 						cost)) {
 					BIT.plugin.Method.getAccount(sPlayer.getName()).subtract(
 							cost);
 					if (BIT.plugin.Method.hasAccount(digilock.getOwner())) {
 						BIT.plugin.Method.getAccount(digilock.getOwner()).add(
 								cost);
 					}
 					sPlayer.sendMessage("Your account ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + ") has been deducted "
 							+ BIT.plugin.Method.format(cost) + ".");
 				} else {
 					sPlayer.sendMessage("You dont have enough money ("
 							+ BIT.plugin.Method.getAccount(sPlayer.getName())
 									.balance() + "). Cost is:"
 							+ BIT.plugin.Method.format(cost));
 					openFenceGate = false;
 				}
 			}
 		}
 		if (openFenceGate) {
 			if (!isFenceGateOpen(sPlayer, sBlock)) {
 				sBlock.setData((byte) (sBlock.getState().getData().getData() | 4));
 				if (digilock != null) {
 					playDigiLockSound(sBlock);
 					if (digilock.getClosetimer() > 0) {
 						scheduleCloseFenceGate(sPlayer, sBlock,
 								digilock.getClosetimer());
 					}
 				}
 			}
 		}
 	}
 
 	public static void closeFenceGate(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		sBlock.setData((byte) ((sBlock.getState().getData().getData() | 4) ^ 4));
 	}
 
 	public static void toggleFenceGate(SpoutPlayer sPlayer, SpoutBlock sBlock) {
 		sBlock.setData((byte) (sBlock.getState().getData().getData() ^ 4));
 	}
 
 	public static int scheduleCloseFenceGate(final SpoutPlayer sPlayer,
 			final SpoutBlock sBlock, final int closetimer) {
 		int fs = closetimer * 20;
 		// 20 ticks / second
 		int taskID = BIT.plugin.getServer().getScheduler()
 				.scheduleSyncDelayedTask(BIT.plugin, new Runnable() {
 					public void run() {
 						SpoutBlock sb = sBlock;
 						SpoutPlayer sp = sPlayer;
 						if (BITConfig.DEBUG_DOOR)
 							sp.sendMessage("Autoclosing the fencegate in "
 									+ closetimer + " seconds");
 						if (sBlock.getType() == Material.FENCE_GATE) {
 							if (isFenceGateOpen(sp, sb)) {
 								closeFenceGate(sp, sb);
 								playDigiLockSound(sBlock);
 							}
 						}
 					}
 				}, fs);
 		return taskID;
 	}
 
 	// *******************************************************
 	//
 	// DOUBLEDOORS
 	//
 	// *******************************************************
 	/**
 	 * Checks if the block is a part of a double door
 	 * 
 	 * @param sBlock
 	 * @return
 	 */
 	public static boolean isDoubleDoor(SpoutBlock sBlock) {
 		// left door:NORTH,NORTH_EAST Right door:WEST,NORTH_WEST
 		// left door:WEST,NORTH_WEST Right door:SOUTH,SOUTH_WEST
 		// left door:SOUTH,SOUTH_WEST Right door:EAST,SOUTH_EAST
 		// left door:EAST,SOUTH_EAST Right door:NORTH,NORTH_EAST
 		if (sBlock != null)
 			if (isDoor(sBlock)) {
 				if (isDoor(sBlock.getRelative(BlockFace.EAST))
 						|| isDoor(sBlock.getRelative(BlockFace.NORTH))
 						|| isDoor(sBlock.getRelative(BlockFace.SOUTH))
 						|| isDoor(sBlock.getRelative(BlockFace.WEST))) {
 					Door door = (Door) sBlock.getState().getData();
 					if (door.getFacing() == BlockFace.EAST
 							&& door.getHingeCorner() == BlockFace.SOUTH_EAST) {
 						if (isDoor(sBlock.getRelative(BlockFace.NORTH))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.NORTH).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.NORTH_EAST) {
 								return true;
 							}
 						} else if (isDoor(sBlock.getRelative(BlockFace.WEST))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.WEST).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.SOUTH_WEST) {
 								return true;
 							}
 						} else
 							BITMessages.showInfo("Doubledoor EAST5 false");
 					} else if (door.getFacing() == BlockFace.NORTH
 							&& door.getHingeCorner() == BlockFace.NORTH_EAST) {
 						if (isDoor(sBlock.getRelative(BlockFace.WEST))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.WEST).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.NORTH_WEST) {
 								return true;
 							}
 						} else if (isDoor(sBlock.getRelative(BlockFace.SOUTH))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.SOUTH).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.SOUTH_EAST) {
 								return true;
 							}
 						}
 					} else if (door.getFacing() == BlockFace.SOUTH
 							&& door.getHingeCorner() == BlockFace.SOUTH_WEST) {
 						if (isDoor(sBlock.getRelative(BlockFace.EAST))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.EAST).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.SOUTH_EAST) {
 								return true;
 							}
 						} else if (isDoor(sBlock.getRelative(BlockFace.NORTH))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.NORTH).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.NORTH_WEST) {
 								return true;
 							}
 						}
 					} else if (door.getFacing() == BlockFace.WEST
 							&& door.getHingeCorner() == BlockFace.NORTH_WEST) {
 						if (isDoor(sBlock.getRelative(BlockFace.SOUTH))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.SOUTH).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.SOUTH_WEST) {
 								return true;
 							}
 						} else if (isDoor(sBlock.getRelative(BlockFace.EAST))) {
 							Door door2 = (Door) sBlock
 									.getRelative(BlockFace.EAST).getState()
 									.getData();
 							if (door2.getHingeCorner() == BlockFace.NORTH_EAST) {
 								return true;
 							}
 						}
 					}
 				}
 			}
 		return false;
 	}
 
 	/**
 	 * checks if the double door is open
 	 * 
 	 * @param sBlock
 	 * @return true if the double door is open, false if not
 	 */
 	public static boolean isDoubleDoorOpen(SpoutBlock sBlock) {
 		return (isDoorOpen(getLeftDoubleDoor(sBlock)) || !isDoorOpen(getRightDoubleDoor(sBlock)));
 	}
 
 	public static void closeDoubleDoor(SpoutPlayer sPlayer, SpoutBlock sBlock,
 			int cost) {
 		if (isDoubleDoor(sBlock)) {
 			if (isLeftDoubleDoor(sBlock)) {
 				closeDoor(sPlayer, sBlock, 0);
 				openDoor(sPlayer, getRightDoubleDoor(sBlock), 0);
 			} else {
 				openDoor(sPlayer, sBlock, cost);
 				closeDoor(sPlayer, getLeftDoubleDoor(sBlock), cost);
 			}
 		}
 	}
 
 	public static void openDoubleDoor(SpoutPlayer sPlayer, SpoutBlock sBlock,
 			int cost) {
 		if (isDoubleDoor(sBlock)) {
 			if (isLeftDoubleDoor(sBlock)) {
 				openDoor(sPlayer, sBlock, cost);
 				closeDoor(sPlayer, getRightDoubleDoor(sBlock), 0);
 			} else {
 				closeDoor(sPlayer, sBlock, 0);
 				openDoor(sPlayer, getLeftDoubleDoor(sBlock), cost);
 			}
 		}
 		BITDigiLock digilock = loadDigiLock(sBlock);
 		if (digilock != null) {
 			if (digilock.getClosetimer() > 0) {
 				scheduleCloseDoubleDoor(sPlayer, sBlock,
 						digilock.getClosetimer(), 0);
 			}
 		}
 	}
 
 	public static boolean isLeftDoubleDoor(SpoutBlock sBlock) {
 		if (isDoubleDoor(sBlock)) {
 			Door door = (Door) sBlock.getState().getData();
 			// left door:NORTH,NORTH_EAST Right door:WEST,NORTH_WEST
 			// left door:WEST,NORTH_WEST Right door:SOUTH,SOUTH_WEST
 			// left door:SOUTH,SOUTH_WEST Right door:EAST,SOUTH_EAST
 			// left door:EAST,SOUTH_EAST Right door:NORTH,NORTH_EAST
 			if (door.getFacing() == BlockFace.NORTH
 					&& door.getHingeCorner() == BlockFace.NORTH_EAST) {
 				if (isDoor(sBlock.getRelative(BlockFace.WEST))) {
 					Door door2 = (Door) sBlock.getRelative(BlockFace.WEST)
 							.getState().getData();
 					if (door2.getHingeCorner() == BlockFace.NORTH_WEST)
 						return true;
 				} else {
 					return false;
 				}
 			} else if (door.getFacing() == BlockFace.WEST
 					&& door.getHingeCorner() == BlockFace.NORTH_WEST) {
 				if (isDoor(sBlock.getRelative(BlockFace.SOUTH))) {
 					Door door2 = (Door) sBlock.getRelative(BlockFace.SOUTH)
 							.getState().getData();
 					if (door2.getHingeCorner() == BlockFace.SOUTH_WEST)
 						return true;
 				} else {
 					return false;
 				}
 			} else if (door.getFacing() == BlockFace.EAST
 					&& door.getHingeCorner() == BlockFace.SOUTH_EAST) {
 				if (isDoor(sBlock.getRelative(BlockFace.NORTH))) {
 					Door door2 = (Door) sBlock.getRelative(BlockFace.NORTH)
 							.getState().getData();
 					if (door2.getHingeCorner() == BlockFace.NORTH_EAST)
 						return true;
 				} else {
 					return false;
 				}
 			} else if (door.getFacing() == BlockFace.SOUTH
 					&& door.getHingeCorner() == BlockFace.SOUTH_WEST) {
 				if (isDoor(sBlock.getRelative(BlockFace.EAST))) {
 					Door door2 = (Door) sBlock.getRelative(BlockFace.EAST)
 							.getState().getData();
 					if (door2.getHingeCorner() == BlockFace.SOUTH_EAST)
 						return true;
 				} else {
 					return false;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static SpoutBlock getLeftDoubleDoor(SpoutBlock sBlock) {
 		if (isLeftDoubleDoor(sBlock)) {
 			return sBlock;
 		} else {
 			Door door = (Door) sBlock.getState().getData();
 			// left door:NORTH,NORTH_EAST Right door:WEST,NORTH_WEST
 			// left door:WEST,NORTH_WEST Right door:SOUTH,SOUTH_WEST
 			// left door:EAST,SOUTH_EAST Right door:NORTH,NORTH_EAST
 			// left door:SOUTH,SOUTH_WEST Right door:EAST,SOUTH_EAST
 			if (door.getFacing() == BlockFace.NORTH
 					&& door.getHingeCorner() == BlockFace.NORTH_EAST) {
 				return sBlock.getRelative(BlockFace.SOUTH);
 			} else if (door.getFacing() == BlockFace.WEST
 					&& door.getHingeCorner() == BlockFace.NORTH_WEST) {
 				return sBlock.getRelative(BlockFace.EAST);
 			} else if (door.getFacing() == BlockFace.EAST
 					&& door.getHingeCorner() == BlockFace.SOUTH_EAST) {
 				return sBlock.getRelative(BlockFace.WEST);
 			} else {
 				// if (door.getFacing() == BlockFace.SOUTH
 				// && door.getHingeCorner() == BlockFace.SOUTH_WEST) {
 				return sBlock.getRelative(BlockFace.NORTH);
 			}
 		}
 	}
 
 	public static boolean isRightDoubleDoor(SpoutBlock sBlock) {
 		if (isDoubleDoor(sBlock)) {
 			if (isLeftDoubleDoor(sBlock)) {
 				return false;
 			} else {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static SpoutBlock getRightDoubleDoor(SpoutBlock sBlock) {
 		if (isRightDoubleDoor(sBlock)) {
 			return sBlock;
 		} else {
 			Door door = (Door) sBlock.getState().getData();
 			// left door:NORTH,NORTH_EAST Right door:WEST,NORTH_WEST
 			// left door:WEST,NORTH_WEST Right door:SOUTH,SOUTH_WEST
 			// left door:EAST,SOUTH_EAST Right door:NORTH,NORTH_EAST
 			// left door:SOUTH,SOUTH_WEST Right door:EAST,SOUTH_EAST
 			if (door.getFacing() == BlockFace.NORTH
 					&& door.getHingeCorner() == BlockFace.NORTH_EAST) {
 				return sBlock.getRelative(BlockFace.WEST);
 			} else if (door.getFacing() == BlockFace.WEST
 					&& door.getHingeCorner() == BlockFace.NORTH_WEST) {
 				return sBlock.getRelative(BlockFace.SOUTH);
 			} else if (door.getFacing() == BlockFace.EAST
 					&& door.getHingeCorner() == BlockFace.SOUTH_EAST) {
 				return sBlock.getRelative(BlockFace.NORTH);
 			} else {
 				// (door.getFacing() == BlockFace.SOUTH
 				// && door.getHingeCorner() == BlockFace.SOUTH_WEST) {
 				return sBlock.getRelative(BlockFace.EAST);
 			}
 		}
 	}
 
 	public static int scheduleCloseDoubleDoor(final SpoutPlayer sPlayer,
 			final SpoutBlock sBlock, final int closetimer, final int cost) {
 		int fs = closetimer * 20;
 		// 20 ticks / second
 		int taskID = BIT.plugin.getServer().getScheduler()
 				.scheduleSyncDelayedTask(BIT.plugin, new Runnable() {
 					public void run() {
 						SpoutBlock sb = sBlock;
 						SpoutPlayer sp = sPlayer;
 						int c = cost;
 						if (BITConfig.DEBUG_DOOR)
 							sp.sendMessage("Autoclosing the DoubleDoor in "
 									+ closetimer + " seconds");
 						if (isDoubleDoor(sBlock)) {
 							if (isDoubleDoorOpen(sb)) {
 								closeDoubleDoor(sp, sb, c);
 								playDigiLockSound(sBlock);
 							}
 						}
 					}
 				}, fs);
 		return taskID;
 	}
 
 	// *******************************************************
 	//
 	// JUKEBOX
 	//
 	// *******************************************************
 	/**
 	 * Check if sBlock is a JUKEBOX
 	 * 
 	 * @param sBlock
 	 * @return true or false
 	 */
 	public static boolean isJukebox(SpoutBlock sBlock) {
 		if (sBlock != null)
 			if (sBlock.getType().equals(Material.JUKEBOX))
 				return true;
 		return false;
 	}
 
 }
