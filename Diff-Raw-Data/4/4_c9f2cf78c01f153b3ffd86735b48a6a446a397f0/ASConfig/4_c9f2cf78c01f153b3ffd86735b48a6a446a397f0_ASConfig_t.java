 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.config;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.entity.EntityType;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.util.ASMaterialList;
 
 /**
  * AntiShare configuration
  * 
  * @author turt2live
  */
 public class ASConfig{
 
 	public static class InventoryCleanupSettings{
 		public final boolean archive, enabled, removeOldWorlds;
 		public final int after;
 
 		InventoryCleanupSettings(boolean enabled, boolean archive, int after, boolean removeOldWorlds){
 			this.archive = archive;
 			this.enabled = enabled;
 			this.after = after;
 			this.removeOldWorlds = removeOldWorlds;
 		}
 	}
 
 	public static class CooldownSettings{
 		public final boolean enabled;
 		public final int seconds;
 
 		CooldownSettings(boolean enabled, int seconds){
 			this.enabled = enabled;
 			this.seconds = seconds;
 		}
 	}
 
 	public static class NaturalSettings{
 		public final boolean allowMismatchedGM;
 		public final boolean breakAsPiston, breakAsAttached, breakAsWater, breakAsBomb;
 		public final boolean emptyInventories;
 		public final boolean removeAttached;
 		public final boolean breakSand;
 		public final boolean spreading;
 
 		NaturalSettings(boolean spreading, boolean mismatch, boolean piston, boolean attached, boolean water, boolean bombs,
 				boolean empty, boolean removeAttached, boolean breakSand){
 			this.allowMismatchedGM = mismatch;
 			this.breakAsPiston = piston;
 			this.breakAsAttached = attached;
 			this.breakAsWater = water;
 			this.emptyInventories = empty;
 			this.removeAttached = removeAttached;
 			this.breakSand = breakSand;
 			this.breakAsBomb = bombs;
 			this.spreading = spreading;
 		}
 	}
 
 	public static class GameModeChangeSettings{
 		public final boolean changeLevel, changeBalance, changeInventory, changeEnder, changePotionEffects;
 
 		GameModeChangeSettings(boolean level, boolean balance, boolean inventory, boolean ender, boolean effects){
 			this.changeBalance = balance;
 			this.changeEnder = ender;
 			this.changeInventory = inventory;
 			this.changeLevel = level;
 			this.changePotionEffects = effects;
 		}
 	}
 
 	public static class InteractionSettings{
 		public final boolean deny, drop;
 
 		InteractionSettings(boolean deny, boolean drop){
 			this.deny = deny;
 			this.drop = drop;
 		}
 	}
 
 	public static class NotifySettings{
 		public final boolean enabled, admins, console;
 
 		NotifySettings(boolean enabled, boolean admins, boolean console){
 			this.enabled = enabled;
 			this.admins = admins;
 			this.console = console;
 		}
 	}
 
 	public static class FeatureSettings{
 		public final boolean inventories, fines;
 
 		FeatureSettings(boolean inventories, boolean fines){
 			this.inventories = inventories;
 			this.fines = fines;
 		}
 	}
 
 	public final ASMaterialList blockBreak, blockPlace, death, pickup, drop, use, interact, craft, trackedCreative, trackedSurvival, trackedAdventure, eat;
 	public final List<String> commands;
 	public final List<EntityType> interactMobs, attackMobs, craftedMobs;
 	public final boolean adventureEqCreative, perWorldInventories, updateChecker, magicSpells, logBlockSpam, potions, thrownPotions, playerVaults, ignoreMagicValues, allowTestCommand;
 	public final InventoryCleanupSettings inventoryCleanupSettings;
 	public final CooldownSettings cooldownSettings;
 	public final NaturalSettings naturalSettings;
 	public final GameModeChangeSettings gamemodeChangeSettings;
 	public final InteractionSettings survivalBreakCreative, creativeBreakSurvival, survivalBreakAdventure, creativeBreakAdventure, adventureBreakSurvival, adventureBreakCreative;
 	public final NotifySettings notificationSettings;
 	public final EnhancedConfiguration rawConfiguration;
 	public final FeatureSettings features;
 
 	private AntiShare p = AntiShare.p;
 
 	public ASConfig(EnhancedConfiguration regionConfig, EnhancedConfiguration worldConfig){
 		LayeredConfig layer = new LayeredConfig(regionConfig, worldConfig, p.getConfig());
 		layer.loadAll();
 		rawConfiguration = regionConfig == null ? (worldConfig == null ? p.getConfig() : worldConfig) : regionConfig;
 		potions = (layer.configFor("lists.no-potions", false)).getBoolean("lists.no-potions");
 		thrownPotions = (layer.configFor("lists.no-thrown-potions", false)).getBoolean("lists.no-thrown-potions");
 		blockBreak = new ASMaterialList((layer.configFor("lists.break", true)).getList("lists.break"));
 		blockPlace = new ASMaterialList((layer.configFor("lists.place", true)).getList("lists.place"));
 		death = new ASMaterialList((layer.configFor("lists.death", true)).getList("lists.death"));
 		pickup = new ASMaterialList((layer.configFor("lists.pickup", true)).getList("lists.pickup"));
 		drop = new ASMaterialList((layer.configFor("lists.drop", true)).getList("lists.drop"));
 		use = new ASMaterialList((layer.configFor("lists.use", true)).getList("lists.use"));
 		interact = new ASMaterialList((layer.configFor("lists.interact", true)).getList("lists.interact"));
 		eat = new ASMaterialList((layer.configFor("lists.eat", true)).getList("lists.eat"));
 		craft = new ASMaterialList((layer.configFor("lists.crafting", true)).getList("lists.crafting"));
 		trackedCreative = new ASMaterialList((layer.configFor("tracking.creative", true)).getList("tracking.creative"));
 		trackedSurvival = new ASMaterialList((layer.configFor("tracking.survival", true)).getList("tracking.survival"));
 		trackedAdventure = new ASMaterialList((layer.configFor("tracking.adventure", true)).getList("tracking.adventure"));
 		commands = toStringList((layer.configFor("lists.commands", true)).getList("lists.commands"));
 		interactMobs = stringToEntityList((layer.configFor("lists.interact-mobs", true)).getList("lists.interact-mobs"));
 		attackMobs = stringToEntityList((layer.configFor("lists.attack-mobs", true)).getList("lists.attack-mobs"));
 		craftedMobs = stringToEntityList((layer.configFor("lists.craft-mob", true)).getList("lists.craft-mob"));
 		adventureEqCreative = (layer.configFor("settings.adventure-is-creative", false)).getBoolean("settings.adventure-is-creative");
 		perWorldInventories = (layer.configFor("settings.use-per-world-inventories", false)).getBoolean("settings.use-per-world-inventories");
 		magicSpells = (layer.configFor("hooks.magicspells.block-creative", false)).getBoolean("hooks.magicspells.block-creative");
 		playerVaults = (layer.configFor("hooks.playervaults.block-creative", false)).getBoolean("hooks.playervaults.block-creative");
 		logBlockSpam = (layer.configFor("hooks.logblock.stop-spam", false)).getBoolean("hooks.logblock.stop-spam");
 		updateChecker = !(layer.configFor("other.ignore-updates", false)).getBoolean("other.ignore-updates");
		ignoreMagicValues = (layer.configFor("other.ignore-magic-value", false)).getBoolean("other.ignore-magic-value");
		allowTestCommand = (layer.configFor("other.allow-test-command", false)).getBoolean("other.allow-test-command");
 		inventoryCleanupSettings = new InventoryCleanupSettings(
 				(layer.configFor("settings.cleanup.inventories.enabled", false)).getBoolean("settings.cleanup.inventories.enabled"),
 				!(layer.configFor("settings.cleanup.inventories.method", false)).getString("settings.cleanup.inventories.method").equalsIgnoreCase("delete"),
 				(layer.configFor("settings.cleanup.inventories.after", false)).getInt("settings.cleanup.inventories.after"),
 				(layer.configFor("settings.cleanup.inventories.remove-old-worlds", false)).getBoolean("settings.cleanup.inventories.remove-old-worlds"));
 		cooldownSettings = new CooldownSettings(
 				(layer.configFor("settings.cooldown.enabled", false)).getBoolean("settings.cooldown.enabled"),
 				(layer.configFor("settings.cooldown.wait-time-seconds", false)).getInt("settings.cooldown.wait-time-seconds"));
 		naturalSettings = new NaturalSettings(
 				(layer.configFor("settings.natural-protection.gamemode-spreading", false)).getBoolean("settings.natural-protection.gamemode-spreading"),
 				(layer.configFor("settings.natural-protection.allow-mismatch-gamemode", false)).getBoolean("settings.natural-protection.allow-mismatch-gamemode"),
 				(layer.configFor("settings.natural-protection.break-as-gamemode.pistons", false)).getBoolean("settings.natural-protection.break-as-gamemode.pistons"),
 				(layer.configFor("settings.natural-protection.break-as-gamemode.attached-blocks", false)).getBoolean("settings.natural-protection.break-as-gamemode.attached-blocks"),
 				(layer.configFor("settings.natural-protection.break-as-gamemode.water", false)).getBoolean("settings.natural-protection.break-as-gamemode.water"),
 				(layer.configFor("settings.natural-protection.break-as-gamemode.blown-up", false)).getBoolean("settings.natural-protection.break-as-gamemode.blown-up"),
 				(layer.configFor("settings.natural-protection.empty-inventories", false)).getBoolean("settings.natural-protection.empty-inventories"),
 				(layer.configFor("settings.natural-protection.remove-attached-blocks", false)).getBoolean("settings.natural-protection.remove-attached-blocks"),
 				(layer.configFor("settings.natural-protection.break-as-gamemode.falling-blocks", false)).getBoolean("settings.natural-protection.break-as-gamemode.falling-blocks"));
 		survivalBreakCreative = new InteractionSettings(
 				(layer.configFor("interaction.survival-breaking-creative.deny", false)).getBoolean("interaction.survival-breaking-creative.deny"),
 				(layer.configFor("interaction.survival-breaking-creative.drop-items", false)).getBoolean("interaction.survival-breaking-creative.drop-items"));
 		creativeBreakSurvival = new InteractionSettings(
 				(layer.configFor("interaction.creative-breaking-survival.deny", false)).getBoolean("interaction.creative-breaking-survival.deny"),
 				(layer.configFor("interaction.creative-breaking-survival.drop-items", false)).getBoolean("interaction.creative-breaking-survival.drop-items"));
 		survivalBreakAdventure = new InteractionSettings(
 				(layer.configFor("interaction.survival-breaking-adventure.deny", false)).getBoolean("interaction.survival-breaking-adventure.deny"),
 				(layer.configFor("interaction.survival-breaking-adventure.drop-items", false)).getBoolean("interaction.survival-breaking-adventure.drop-items"));
 		adventureBreakCreative = new InteractionSettings(
 				(layer.configFor("interaction.adventure-breaking-creative.deny", false)).getBoolean("interaction.adventure-breaking-creative.deny"),
 				(layer.configFor("interaction.adventure-breaking-creative.drop-items", false)).getBoolean("interaction.adventure-breaking-creative.drop-items"));
 		adventureBreakSurvival = new InteractionSettings(
 				(layer.configFor("interaction.adventure-breaking-survival.deny", false)).getBoolean("interaction.adventure-breaking-survival.deny"),
 				(layer.configFor("interaction.adventure-breaking-survival.drop-items", false)).getBoolean("interaction.adventure-breaking-survival.drop-items"));
 		creativeBreakAdventure = new InteractionSettings(
 				(layer.configFor("interaction.creative-breaking-adventure.deny", false)).getBoolean("interaction.creative-breaking-adventure.deny"),
 				(layer.configFor("interaction.creative-breaking-adventure.drop-items", false)).getBoolean("interaction.creative-breaking-adventure.drop-items"));
 		gamemodeChangeSettings = new GameModeChangeSettings(
 				(layer.configFor("settings.gamemode-change.change-level", false)).getBoolean("settings.gamemode-change.change-level"),
 				(layer.configFor("settings.gamemode-change.change-economy-balance", false)).getBoolean("settings.gamemode-change.change-economy-balance"),
 				(layer.configFor("settings.gamemode-change.change-inventory", false)).getBoolean("settings.gamemode-change.change-inventory"),
 				(layer.configFor("settings.gamemode-change.change-ender-chest", false)).getBoolean("settings.gamemode-change.change-ender-chest"),
 				(layer.configFor("settings.gamemode-change.change-potion-effects", false)).getBoolean("settings.gamemode-change.change-potion-effects"));
 		notificationSettings = new NotifySettings(
 				(layer.configFor("settings.notify.use", false)).getBoolean("settings.notify.use"),
 				(layer.configFor("settings.notify.with-permission", false)).getBoolean("settings.notify.with-permission"),
 				(layer.configFor("settings.notify.console", false)).getBoolean("settings.notify.console"));
 		features = new FeatureSettings(
 				(layer.configFor("settings.features.use-inventories", false)).getBoolean("settings.features.use-inventories"),
 				(layer.configFor("settings.features.use-fines-rewards", false)).getBoolean("settings.features.use-fines-rewards"));
 	}
 
 	private List<String> toStringList(List<?> list){
 		List<String> strings = new ArrayList<String>();
 		for(Object o : list){
 			if(!(o instanceof String)){
 				continue;
 			}
 			String s = ((String) o).toLowerCase();
 			if(s.startsWith("/")){
 				s = s.substring(1);
 			}
 			strings.add(s);
 		}
 		return Collections.unmodifiableList(strings);
 	}
 
 	private List<EntityType> stringToEntityList(List<?> list){
 		List<EntityType> entities = new ArrayList<EntityType>();
 		for(Object o : list){
 			if(!(o instanceof String)){
 				continue;
 			}
 			String string = (String) o;
 			string = string.trim();
 			if(string.equalsIgnoreCase("all")){
 				entities.clear();
 				for(EntityType e : EntityType.values()){
 					entities.add(e);
 				}
 				break;
 			}else if(string.equalsIgnoreCase("none")){
 				entities.clear();
 				break;
 			}
 			String modified = string.toLowerCase().replace(" ", "");
 			if(modified.equalsIgnoreCase("irongolem")){
 				modified = "villagergolem";
 			}else if(modified.equalsIgnoreCase("snowgolem")){
 				modified = "snowman";
 			}else if(modified.equalsIgnoreCase("wither")){
 				modified = "witherboss";
 			}else if(modified.equalsIgnoreCase("players") || modified.equalsIgnoreCase("player")){
 				entities.add(EntityType.PLAYER);
 				continue;
 			}
 			@SuppressWarnings ("deprecation")
 			// TODO: Magic value
 			EntityType entity = EntityType.fromName(modified);
 			if(entity == null){
 				p.getLogger().warning(p.getMessages().getMessage("unknown-entity", string));
 				continue;
 			}
 			entities.add(entity);
 		}
 		return Collections.unmodifiableList(entities);
 	}
 
 }
