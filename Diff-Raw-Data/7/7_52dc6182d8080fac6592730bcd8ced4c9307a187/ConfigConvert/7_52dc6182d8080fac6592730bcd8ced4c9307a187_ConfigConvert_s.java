 package com.turt2live.antishare.config;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 
 /**
  * Converts a 5.3.0 configuration to a 5.4.0 configuration
  * 
  * @author turt2live
  */
 public class ConfigConvert {
 
 	/**
 	 * Does the actual conversion.<br>
 	 * <i>This does attempt to look for 5.4.0-BETA keys that may exist</i>
 	 */
 	public static void doConvert(){
 		AntiShare p = AntiShare.p;
 		// Note: AntiShare#onEnable() will automatically clean up the mess left here
 		EnhancedConfiguration c = p.getConfig();
 
 		if(c.getString("other.version_string") != null){
 			return; // Already converted!
 		}
 
 		// Load block lists
 		c.set("lists.break", convertList(c.getString("blocked-lists.block-break")));
 		c.set("lists.place", convertList(c.getString("blocked-lists.block-place")));
 		c.set("lists.death", convertList(c.getString("blocked-lists.dropped-items-on-death")));
 		c.set("lists.pickup", convertList(c.getString("blocked-lists.picked-up-items")));
 		c.set("lists.drop", convertList(c.getString("blocked-lists.dropped-items")));
 		c.set("lists.use", merge(c.getString("blocked-lists.right-click"), c.getString("blocked-lists.use-items")));
 		c.set("lists.commands", convertList(c.getString("blocked-lists.commands")));
 		c.set("lists.attack-mobs", convertList(c.getString("blocked-lists.mobs")));
 		c.set("lists.interact-mobs", convertList(c.getString("blocked-lists.right-click-mobs")));
 		c.set("lists.crafting", convertList(c.getString("blocked-lists.crafting-recipes")));
 
 		// Load tracked lists
 		c.set("tracking.creative", convertList(c.getString("block-tracking.tracked-creative-blocks")));
 		c.set("tracking.survival", convertList(c.getString("block-tracking.tracked-survival-blocks")));
 		c.set("tracking.adventure", convertList(c.getString("block-tracking.tracked-adventure-blocks")));
 
 		// Convert crafted mobs
 		List<String> mobs = new ArrayList<String>();
 		if(c.getBoolean("enabled-features.mob-creation.allow-snow-golems")){
 			mobs.add("snow golem");
 		}
 		if(c.getBoolean("enabled-features.mob-creation.allow-iron-golems")){
 			mobs.add("iron golem");
 		}
 		if(c.getBoolean("enabled-features.mob-creation.allow-wither")){
 			mobs.add("wither");
 		}
 		c.set("lists.craft-mob", mobs);
 
 		// Interaction settings
 		c.set("interaction.survival-breaking-creative.deny", c.get("settings.survival-breaking-creative-blocks.deny"));
 		c.set("interaction.creative-breaking-survival.deny", c.get("settings.creative-breaking-survival-blocks.deny"));
 		c.set("interaction.survival-breaking-adventure.deny", c.get("settings.survival-breaking-adventure-blocks.deny"));
 		c.set("interaction.creative-breaking-adventure.deny", c.get("settings.creative-breaking-adventure-blocks.deny"));
 		c.set("interaction.adventure-breaking-survival.deny", c.get("settings.adventure-breaking-survival-blocks.deny"));
 		c.set("interaction.adventure-breaking-creative.deny", c.get("settings.adventure-breaking-creative-blocks.deny"));
 		c.set("interaction.survival-breaking-creative.drop-items", c.get("settings.survival-breaking-creative-blocks.block-drops"));
 		c.set("interaction.creative-breaking-survival.drop-items", c.get("settings.creative-breaking-survival-blocks.block-drops"));
 		c.set("interaction.survival-breaking-adventure.drop-items", c.get("settings.survival-breaking-adventure-blocks.block-drops"));
 		c.set("interaction.creative-breaking-adventure.drop-items", c.get("settings.creative-breaking-adventure-blocks.block-drops"));
 		c.set("interaction.adventure-breaking-survival.drop-items", c.get("settings.adventure-breaking-survival-blocks.block-drops"));
 		c.set("interaction.adventure-breaking-creative.drop-items", c.get("settings.adventure-breaking-creative-blocks.block-drops"));
 
 		// Cleanup functions
 		c.set("settings.cleanup.inventories.enabled", c.get("settings.cleanup.use"));
 		c.set("settings.cleanup.inventories.method", c.get("settings.cleanup.method"));
 		c.set("settings.cleanup.inventories.after", c.get("settings.cleanup.after"));
 		c.set("settings.cleanup.inventories.remove-old-worlds", c.get("settings.remove-old-inventories"));
 
 		// Gamemode cooldown
 		c.set("settings.cooldown.enabled", c.get("gamemode-change-cooldown.use"));
 		c.set("settings.cooldown.wait-time-seconds", c.get("gamemode-change-cooldown.time-in-seconds"));
 
 		// Natural protection
 		c.set("settings.natural-protection.allow-mismatch-gamemode", c.get("settings.similar-gamemode-allow"));
 		c.set("settings.natural-protection.remove-attached-blocks", c.get("settings.enabled-features.no-drops-when-block-break.attached-blocks"));
 		c.set("settings.natural-protection.empty-inventories", c.get("settings.enabled-features.no-drops-when-block-break.inventories"));
 
 		// Gamemode change settings
 		c.set("settings.gamemode-change.change-level", c.get("enabled-features.change-level-on-gamemode-change"));
 		c.set("settings.gamemode-change.change-economy-balance", c.get("enabled-features.change-balance-on-gamemode-change"));
 		c.set("settings.gamemode-change.change-inventory", c.get("handled-actions.gamemode-inventories"));
 		c.set("settings.gamemode-change.change-ender-chest", c.get("handled-actions.gamemode-ender-chests"));
 
 		// Hook settings
 		c.set("hooks.magicspells.block-creative", c.get("magicspells.block-creative"));
 		c.set("hooks.logblock.stop-spam", c.get("other.stop-logblock-spam"));
 
 		// Other settings
 		c.set("other.ignore-updates", c.get("other.dont-look-for-updates"));
 		c.set("settings.adventure-is-creative", c.get("other.adventure-is-creative"));
 		c.set("settings.use-per-world-inventories", c.get("handled-actions.world-transfers"));
 
 		c.save();
 
 		p.getLogger().warning("=========================");
 		p.getLogger().warning(p.getMessages().getMessage("configuration-update"));
 		p.getLogger().warning("=========================");
 	}
 
 	private static List<String> merge(String s1, String s2){
 		List<String> l = new ArrayList<String>();
 		l.addAll(convertList(s1));
 		l.addAll(convertList(s2));
 		return l;
 	}
 
 	private static List<String> convertList(String raw){
 		List<String> l = new ArrayList<String>();
 		String[] parts = raw.split(",");
 		for(String s : parts){
 			s = s.trim();
 			l.add(s);
 		}
 		return l;
 	}
 
 }
