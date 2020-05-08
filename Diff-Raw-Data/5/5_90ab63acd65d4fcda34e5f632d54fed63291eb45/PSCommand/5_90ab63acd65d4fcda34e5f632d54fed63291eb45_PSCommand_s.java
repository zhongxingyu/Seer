 package net.eonz.bukkit.psduo;
 
 /*
  * This code is Copyright (C) 2011 Chris Bode, Some Rights Reserved.
  *
  * Copyright (C) 1999-2002 Technical Pursuit Inc., All Rights Reserved. Patent 
  * Pending, Technical Pursuit Inc.
  *
  * Unless explicitly acquired and licensed from Licensor under the Technical 
  * Pursuit License ("TPL") Version 1.0 or greater, the contents of this file are 
  * subject to the Reciprocal Public License ("RPL") Version 1.1, or subsequent 
  * versions as allowed by the RPL, and You may not copy or use this file in 
  * either source code or executable form, except in compliance with the terms and 
  * conditions of the RPL.
  *
  * You may obtain a copy of both the TPL and the RPL (the "Licenses") from 
  * Technical Pursuit Inc. at http://www.technicalpursuit.com.
  *
  * All software distributed under the Licenses is provided strictly on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, AND TECHNICAL
  * PURSUIT INC. HEREBY DISCLAIMS ALL SUCH WARRANTIES, INCLUDING WITHOUT 
  * LIMITATION, ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE, QUIET ENJOYMENT, OR NON-INFRINGEMENT. See the Licenses for specific 
  * language governing rights and limitations under the Licenses. 
  */
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.eonz.bukkit.psduo.signs.PSSign;
 import net.eonz.bukkit.psduo.signs.SignType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class PSCommand implements CommandExecutor {
 
 	private final PailStone main;
 
 	public PSCommand(PailStone pailStone) {
 		this.main = pailStone;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 
 		if (args.length > 0) {
 			if (args[0].equalsIgnoreCase("msg") && isPlayer(sender)) {
 				setMessage(sender, command, label, args);
 				return true;
 			}
 
 			if (args[0].equalsIgnoreCase("listsigns")) {
 				listSigns(sender, command, label, args);
 				return true;
 			}
 
 			if (args[0].equalsIgnoreCase("loadsigns")) {
 				loadSigns(sender, command, label, args);
 				return true;
 			}
 
 			if (args[0].equalsIgnoreCase("area")) {
 				areaManager(sender, command, label, args);
 				return true;
 			}
 		}
 
 		PailStone.alert(sender, "Malformed Command");
 
 		return true;
 	}
 
 	private void areaManager(CommandSender sender, Command command, String label, String[] args) {
 		if (sender instanceof Player && !main.hasPermission(((Player) sender).getName(), "area", ((Player) sender).getWorld().getName())) {
 			PailStone.alert(sender, "You do not have the 'pailstone.area' permission.");
 			return;
 		}
 
 		if (args.length == 1) {
 			if (isPlayer(sender)) {
 				Player p = (Player) sender;
 				List<Area> areas = main.areas.getPlayerAreas(p.getName());
 
 				String list = "";
 
 				for (int i = 0; i < areas.size(); i++) {
 					if (i != 0) {
 						list += ", ";
 					}
 					list += areas.get(i).getName();
 				}
 
 				PailStone.alert(p, "Your Areas: " + list);
			} else {
 				return;
 			}
 		}
 
 		if (args.length >= 2) {
 			if (args[1].equalsIgnoreCase("define") && isPlayer(sender)) {
 				defineArea(sender, command, label, args);
 				return;
 			}
 
 			if (args[1].equalsIgnoreCase("restore")) {
 				restoreArea(sender, command, label, args);
 				return;
 			}
 		}
 
 		PailStone.alert(sender, "Malformed Command");
 	}
 
 	private void restoreArea(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length >= 3) {
 			Area a = main.areas.getArea(args[2]);
 			if (a != null) {
 				a.draw(main);
 				PailStone.alert(sender, "Area restored.");
 			}
 		} else {
 			PailStone.alert(sender, "Specify the name of the area to restore.");
 		}
 	}
 
 	private void defineArea(CommandSender sender, Command command, String label, String[] args) {
 		Player p = (Player) sender;
 
 		if (args.length < 3) {
 			PailStone.alert(sender, "You must specify a name for the proposed area.");
 		} else {
 			String areaName = args[2];
 
 			if (areaName.length() > 15) {
 				PailStone.alert(sender, "The area name you specified was " + areaName.length() + " long, but only 15 letters can fit on a sign. Pick another name for your area.");
 				return;
 			}
 
 			PSPlayer psp = this.main.players.safelyGet(p.getName(), main);
 			if (!(psp.l1 && (psp.loc2 != null))) {
 				PailStone.alert(sender, "You must first define two points with glowstone dust.");
 			} else if (!psp.validatePoints()) {
 				PailStone.alert(sender, "The two points you specified are invalid. Try again.");
 			} else if (CuboidUtil.areaBetween(psp.loc1, psp.loc2) > this.main.cfgMaxCuboid && !this.main.hasPermission(p.getName(), "ignoremaxsize", psp.loc1.getWorld().getName())) {
 				PailStone.alert(sender, "The toggle area you specified was " + CuboidUtil.areaBetween(psp.loc1, psp.loc2) + " blocks big. This is " + (CuboidUtil.areaBetween(psp.loc1, psp.loc2) - this.main.cfgMaxCuboid) + " larger than the limit of " + this.main.cfgMaxCuboid
 						+ ". Please designate a smaller area.");
 			} else {
 				this.main.areas.defineArea(areaName, p.getName(), psp.loc1, psp.loc2);
 				PailStone.alert(sender, "Created '" + areaName + "'.");
 			}
 		}
 	}
 
 	private void loadSigns(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			PailStone.alert(sender, "This command must be called by a player.");
 			return;
 		}
 
 		Player player = (Player) sender;
 
 		Chunk player_chunk = player.getWorld().getChunkAt(player.getLocation());
 
 		int radius = 32;
 
 		if (args.length >= 2) {
 			try {
 				radius = Integer.parseInt(args[1]);
 			} catch (Exception e) {
 				PailStone.alert(sender, "Could not read radius as a number.");
 				return;
 			}
 		}
 
 		int maxRadius = 16 * 5;
 
 		if (radius <= 0 || radius > maxRadius) {
 			PailStone.alert(sender, "Radius out of bounds. Acceptable values are 1 to " + maxRadius + ".");
 			return;
 		}
 
 		int pcx = player_chunk.getX();
 		int pcz = player_chunk.getZ();
 
 		ArrayList<Sign> signs = new ArrayList<Sign>();
 
 		ArrayList<Chunk> chunks = new ArrayList<Chunk>();
 
 		int chunk_radius = radius / 16 + 1;
 
 		for (int x = -chunk_radius; x <= +chunk_radius; x++) {
 			for (int z = -chunk_radius; z <= +chunk_radius; z++) {
 				chunks.add(player.getWorld().getChunkAt(pcx + x, pcz + z));
 			}
 		}
 
 		int existingSigns = 0;
 
 		java.util.Iterator<Chunk> chunkIt = chunks.iterator();
 		while (chunkIt.hasNext()) {
 			Chunk nextChunk = chunkIt.next();
 
 			if (!nextChunk.isLoaded()) {
 				nextChunk.load();
 			}
 
 			BlockState[] entities = nextChunk.getTileEntities();
 			for (BlockState entity : entities) {
 				if (entity instanceof Sign) {
 					int xdiff = entity.getX() - player.getLocation().getBlockX();
 					int zdiff = entity.getZ() - player.getLocation().getBlockZ();
 					if (Math.floor(Math.sqrt(xdiff * xdiff + zdiff * zdiff)) <= radius) {
 						if (this.main.sgc.getPSInstance((Sign) entity) == null) {
 							signs.add((Sign) entity);
 						} else {
 							existingSigns++;
 						}
 					}
 				}
 			}
 		}
 
 		PailStone.alert(sender, "Found " + (signs.size() + existingSigns) + " signs within " + radius + " blocks. " + signs.size() + " are unloaded. Attempting to load them now...");
 		Iterator<Sign> signIt = signs.iterator();
 		while (signIt.hasNext()) {
 			Sign next = signIt.next();
 			Direction d = PSSign.getDirection(next);
 			PSSign.signFactory(PailStone.formatLines(next.getLines()), player.getName(), "", next.getWorld().getName(), next.getBlock().getLocation(), d, true, true, null, this.main);
 		}
 	}
 
 	/**
 	 * Sets the player's message for announce, disp, etc.
 	 * 
 	 * @param sender
 	 * @param command
 	 * @param label
 	 * @param args
 	 */
 	private void setMessage(CommandSender sender, Command command, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			PailStone.alert(sender, "This command must be called by a player.");
 			return;
 		}
 
 		Player p = (Player) sender;
 
 		if (args.length > 1) {
 			String newMsg = combine(args, 1);
 			PailStone.alert(p, "Saved: \"" + newMsg + "\"");
 			PSPlayer psp = this.main.players.safelyGet(p.getName(), this.main);
 			psp.setMessage(newMsg);
 		} else {
 			PSPlayer psp = this.main.players.safelyGet(p.getName(), this.main);
 			if (psp.message != null) {
 				PailStone.alert(p, "Stored message: \"" + psp.message + "\"");
 			} else {
 				PailStone.alert(p, "You have not stored a message. " + org.bukkit.ChatColor.AQUA + "/ps msg <message>");
 			}
 		}
 	}
 
 	/**
 	 * Parse the listsigns command.
 	 * 
 	 * @param sender
 	 * @param command
 	 * @param label
 	 * @param args
 	 */
 	private void listSigns(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length >= 2) {
 			String p = args[1];
 			if (p != null) {
 				if (args.length >= 3) {
 					if (main.getServer().getWorld(args[2]) != null) {
 						listSigns(p, sender, args[2]);
 					} else {
 						PailStone.alert(sender, "There is no \"" + args[2] + "\".");
 					}
 				} else {
 					listSigns(p, sender, null);
 				}
 			}
 		} else if (sender instanceof Player) {
 			listSigns(((Player) sender).getName(), sender, null);
 		} else {
 			PailStone.alert(sender, "This command must be called by or on a player.");
 		}
 	}
 
 	/**
 	 * List what signs 'p' can use on 'world' to 'sender'.
 	 * 
 	 * @param p
 	 * @param sender
 	 * @param world
 	 */
 	private void listSigns(String p, CommandSender sender, String world) {
 		String message = p + " can use: " + ChatColor.WHITE;
 
 		String actualWorld = ((world == null) ? this.main.getServer().getWorlds().get(0).getName() : world);
 
 		for (SignType sign : SignType.values()) {
 			if (main.hasPermission(p, sign.name().toLowerCase(), actualWorld)) {
 				message += sign.name() + " ";
 			}
 		}
 		message += ChatColor.GOLD + "on world \"" + actualWorld + "\".";
 		PailStone.alert(sender, message);
 	}
 
 	/**
 	 * Returns true if the sender is a player, sends a message to the sender and
 	 * returns false if not.
 	 * 
 	 * @param sender
 	 * @return
 	 */
 	public boolean isPlayer(CommandSender sender) {
 		if (sender instanceof Player) {
 			return true;
 		} else {
 			sender.sendMessage("This command can only be used by a player.");
 			return false;
 		}
 	}
 
 	public static String combine(String[] args, int from) {
 		String out = "";
 		for (int i = from; i < args.length; i++) {
 			if (i != from) {
 				out += " ";
 			}
 			out += args[i];
 		}
 		return out;
 	}
 
 }
