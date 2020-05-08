 package net.worldoftomorrow.MultiPack;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.server.v1_4_R1.MinecraftServer;
 
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class Util {
 	private final MultiPack plugin;
 
 	protected Util(MultiPack plugin) {
 		this.plugin = plugin;
 	}
 
 	/**
 	 * Set the players texture pack to the given texture pack
 	 * 
 	 * @param Player
 	 * @param TexturePack
 	 */
 	protected void setTexturePack(Player p, TexturePack pack) {
 		if (plugin.playerCurrent.get(p.getName()) == pack) return;
 		p.setTexturePack(pack.getUrl());
 		plugin.playerCurrent.put(p.getName(), pack);
 	}
 
 	/**
 	 * Set the players texture pack to the default one.
 	 * 
 	 * @param Player
 	 */
 	protected void setDefaultPack(Player p, boolean force) {
 		TexturePack current = plugin.playerCurrent.get(p.getName());
 		String defurl = MinecraftServer.getServer().getTexturePack();
 		if (current == null && !force) return;
 
 		if (plugin.defaultPacks.containsKey(p.getWorld().getName())) {
 			p.setTexturePack(plugin.defaultPacks.get(p.getWorld().getName()));
 		} else if (defurl.equals("") || defurl == null) {
 			p.setTexturePack("https://dl.dropbox.com/u/52707344/default.zip");
 		} else {
 			p.setTexturePack(defurl);
 		}
 		plugin.playerCurrent.put(p.getName(), null);
 	}
 
 	/**
 	 * Get the texture pack for the highest priority region the player is in.
 	 * 
 	 * @param RegionManager
 	 * @param Player
 	 * @return Highest Priority Pack
 	 */
 
 	protected TexturePack getHighestPriorityPack(RegionManager rm, Player p) {
 		TexturePack tp = null;
 		if (!plugin.texturePacks.containsKey(p.getWorld().getName())) return null;
 		ArrayList<TexturePack> worldPacks = plugin.texturePacks.get(p.getWorld().getName());
 		if (worldPacks == null || worldPacks.isEmpty()) {
 			return null;
 		}
 
 		ApplicableRegionSet set = rm.getApplicableRegions(p.getLocation());
		List<TexturePack> regionPacks = this.getApplicablePacks(set, worldPacks);
 
 		for (ProtectedRegion region : set) {
 			for (TexturePack pack : regionPacks) {
 				ProtectedRegion packRegion = rm.getRegion(pack.getRegion());
 				if (packRegion == null) continue;
 				if (tp == null) tp = pack;
 				if (region.getPriority() < packRegion.getPriority()) tp = pack;
 			}
 		}
 		return tp;
 	}
 
	private List<TexturePack> getApplicablePacks(ApplicableRegionSet set, ArrayList<TexturePack> worldPacks) {
 		List<TexturePack> packs = new ArrayList<TexturePack>();
 		for (ProtectedRegion region : set) {
 			for (TexturePack pack : worldPacks) {
 				if (region.getId().equals(pack.getRegion())) {
 					packs.add(pack);
 				}
 			}
 		}
 		return packs;
 	}
 }
