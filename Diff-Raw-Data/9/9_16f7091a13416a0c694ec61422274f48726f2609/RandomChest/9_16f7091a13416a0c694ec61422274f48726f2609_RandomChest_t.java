 package com.github.owatakun.mahime;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class RandomChest {
 
 	private boolean rcEditMode;
 	private int rcEditBlock;
 	private Plugin plugin;
 	private YamlConfiguration rcConf;
 	private LinkedHashMap<String, ArrayList<Point>> rcPointLists;
 	private World enableWorld;
 	private File rcFile;
 	private String nowEditing;
 	protected static RandomChest instance;
 
 	public RandomChest(Plugin plugin, FileConfiguration config, YamlConfiguration rcConf) {
 		instance = this;
 		rcEditMode = false;
 		this.plugin = plugin;
 		this.rcFile = new File(plugin.getDataFolder(), "rc.yml");
 		this.rcConf = rcConf;
 		this.rcPointLists = new LinkedHashMap<String, ArrayList<Point>>();
 		loadRcPointLists();
 		this.rcEditBlock = rcConf.getInt("rcEditBlock", 120);
 		this.nowEditing = null;
 	}
 	/**
 	 * rcEditModeを有効にして初期処理を行う
 	 */
 	protected void enableEditMode(World world, CommandSender sender, String listName) {
 		rcEditMode = true;
 		enableWorld = world;
 		ArrayList<Point> rcPointList = new ArrayList<Point>();
 		if (rcPointLists.containsKey(listName)) {
 			rcPointList = rcPointLists.get(listName);
 		} else {
 			sender.sendMessage("リスト名\"" + listName + "\"を作成します");
 			rcPointLists.put(listName, rcPointList);
 		}
 		// リストのデータに従って指示ブロックを設置する
 		ArrayList<Point> errList = new ArrayList<Point>();
 		for (int i = 0; i < rcPointList.size();) {
 			Point pt = rcPointList.get(i);
 			Block block = world.getBlockAt(pt.getX(), pt.getY(), pt.getZ());
 			if (block.getTypeId() == 0) {
 				block.setTypeId(getRcEditBlock());
 				i++;
 			} else {
 				errList.add(pt);
 				rcPointList.remove(i);
 			}
 		}
 		((Player) sender).getInventory().addItem(new ItemStack(getRcEditBlock(), 1));
 		nowEditing = listName;
		plugin.getServer().broadcast(listName + " のEditModeを開始しました", "mahime.admin");
 		if (errList.size() != 0) {
 			sender.sendMessage("Error: 以下の箇所に他のブロックが存在したため、これらの指定を削除しました");
 			for (Point errPt: errList) {
 				sender.sendMessage(errPt.serialize());
 			}
 		}
 	}
 
 	/**
 	 * EditModeを終了して保存処理を行う
 	 */
 	protected void disableEditMode(CommandSender sender) {
 		if (rcEditMode) {
 			ArrayList<Point> rcPointList = rcPointLists.get(nowEditing);
 			ArrayList<String> saveList = new ArrayList<String>();
 			for (Point pt: rcPointList) {
 				saveList.add(pt.serialize());
 				Block block = enableWorld.getBlockAt(pt.getX(), pt.getY(), pt.getZ());
 				block.setTypeId(0);
 			}
 			rcConf.set(nowEditing, saveList);
 			try {
 				rcConf.save(rcFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			rcEditMode = false;
			plugin.getServer().broadcast(nowEditing + " のEditModeを終了しました", "mahime.admin");
 			nowEditing = null;
 			enableWorld = null;
 		}
 	}
 
 	/**
 	 * rcPointListに追加
 	 * @return 追加結果(現在は全てtrue)
 	 */
 	protected boolean addRCPoint(Player player, Location loc) {
 		ArrayList<Point> rcPointList = rcPointLists.get(nowEditing);
 		int x, y, z;
 		x = loc.getBlockX();
 		y = loc.getBlockY();
 		z = loc.getBlockZ();
 		Point pt = new Point(x, y, z);
 		rcPointList.add(pt);
 		plugin.getServer().broadcast(Util.repSec("&7") + "Add: " + x + "," + y + "," + z , "mahime.admin");
 		return true;
 	}
 	/**
 	 * rcPointListから消去
 	 * @return 消去結果
 	 */
 	protected boolean removeRCPoint(Player player, Location loc) {
 		ArrayList<Point> rcPointList = rcPointLists.get(nowEditing);
 		boolean deleted = false;
 		int x, y, z;
 		x = loc.getBlockX();
 		y = loc.getBlockY();
 		z = loc.getBlockZ();
 		for (int i = 0; i < rcPointList.size();) {
 			Point pt = rcPointList.get(i);
 			if (pt.getX() == x && pt.getY() == y && pt.getZ() == z) {
 				rcPointList.remove(i);
 				deleted = true;
 				break;
 			}
 			i++;
 		}
 		if (deleted) {
 			plugin.getServer().broadcast(Util.repSec("&7") + "Remove: " + x + "," + y + "," + z , "mahime.admin");
 			return true;
 		} else {
 			player.sendMessage("Error: リストに存在しません");
 			return false;
 		}
 	}
 
 	/**
 	 * rcPointListのコピー取得
 	 * @param listName リスト名
 	 * @return rcPointListのコピー 未存在時はnull
 	 */
 	protected ArrayList<Point> getrcPointListCopy(String listName) {
 		if (rcPointLists.containsKey(listName)) {
 			ArrayList<Point> rcPointList = rcPointLists.get(listName);
 			return new ArrayList<Point>(rcPointList);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * rcEditModeのgetter
 	 * @return rcEditMode
 	 */
 	protected boolean isRcEditMode() {
 		return rcEditMode;
 	}
 
 	/**
 	 * rcEditBlockのgetter
 	 * @return rcEditBlock;
 	 */
 	protected int getRcEditBlock() {
 		return rcEditBlock;
 	}
 
 	/**
 	 * rcPointListsのkeySetのgetter
 	 * @return rcPointLists.keySet()
 	 */
 	protected Set<String> getListsKeys() {
 		return rcPointLists.keySet();
 	}
 
 	/**
 	 * rcPointListsの初期化
 	 */
 	private void loadRcPointLists() {
 		Set<String> keys = rcConf.getKeys(true);
 		for (String key: keys) {
 			List<String> tempList = rcConf.getStringList(key);
 			ArrayList<Point> rcPointList = new ArrayList<Point>();
 			for (String temp: tempList) {
 				Point pt = Point.deserialize(temp);
 				if (pt != null) {
 					rcPointList.add(pt);
 				}
 			}
 			rcPointLists.put(key, rcPointList);
 		}
 	}
 }
