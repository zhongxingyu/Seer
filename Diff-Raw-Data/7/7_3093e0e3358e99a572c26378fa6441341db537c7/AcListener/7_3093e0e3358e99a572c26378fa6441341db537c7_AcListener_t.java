 package com.github.mckillroy.accontrol;
 
 import java.text.MessageFormat;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.massivecraft.factions.Board;
 import com.massivecraft.factions.FLocation;
 import com.massivecraft.factions.FPlayer;
 import com.massivecraft.factions.FPlayers;
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.struct.Relation;
 
 import com.minesworn.autocraft.Autocraft;
 
 /**
  * @author McYorlik
  * 
  */
 public final class AcListener implements Listener {
 	/**
 	 * Constructor
 	 */
 	AcListener() {
 		super();
 	}
 
 	/**
 	 * @param MyPlayer
 	 *            Playa who's ridin' da ship
 	 * @return Maximum allowed dimension fer da ship
 	 */
 	public static int getMaxShipsize(final Player MyPlayer) {
 		// return MaxShipsize if player is a pilot, else returns false (?)
 		return Autocraft.shipmanager.ships.get(MyPlayer.getName()).properties.MAX_SHIP_DIMENSIONS;
 	}
 
 	/**
 	 * @param MyPlayer
 	 *            Da captain of da ship
 	 * @return Speed of da ship
 	 */
 	public static int getMoveSpeed(final Player MyPlayer) {
 		// return MaxShipsize if player is a pilot, else returns false (?)
 		return Autocraft.shipmanager.ships.get(MyPlayer.getName()).properties.MOVE_SPEED;
 	}
 
 	// Factions related functions
 
 	/**
 	 * @param MyPlayer
 	 *            Da Playa
 	 * @return Da faction of da Playa
 	 */
 	public static Faction getFaction(final Player MyPlayer) {
 		return FPlayers.i.get(MyPlayer).getFaction();
 	}
 
 	/**
 	 * @param MyPlayer
 	 *            Da Playa
 	 * @param chunks
 	 *            Chunks round da playa to check fer forign factions - 1 makes a
 	 *            3x3 chunk area check
 	 * @return "Yeah" if foreigners found, "Nope" if none
 	 */
 	public static boolean hasForeignChunks(final Player MyPlayer,
 			final int chunks) {
 		// check chunks in a radius around the Player for foreign factions and
 		// returns true if foreign factions are found
 		// chunks = chunks around the player to check
 		final Location PLocation = MyPlayer.getLocation();
 		final Location TLocation = PLocation.clone();
 		final FLocation MyFLocation = new FLocation(TLocation);
 
 		final double x = MyPlayer.getLocation().getX();
 		final double z = MyPlayer.getLocation().getZ();
 		// //get faction from Fplayer whos is our player
 		final Faction PFaction = getFaction(MyPlayer);
 		Faction TFaction = Board.getFactionAt(MyFLocation);
 		// //////////////////////////////////////////////////////////
 		// //// Chunklevel test loop
 		// //////////////////////////////////////////////////////////
 		for (int iX = -chunks; iX <= chunks; iX++) {
 			// // modify testlocation
 			TLocation.setX(x + 16 * iX); // // // multiplier 16 is for
 			// interpreting the number as
 			// chunks, not blocks
 			for (int iZ = -chunks; iZ <= chunks; iZ++) {
 				// // modify testlocation
 				TLocation.setZ(z + 16 * iZ); // // multiplier 16 is for
 				// interpreting the number as
 				// chunks, not blocks
 
 				// // debug
 				// //msg(MyPlayer,"iX: %s iZ: %s"%(iX,iZ))
 
 				// //get faction at Test Location
 
 				MyFLocation.setX(FLocation.blockToChunk(TLocation.getBlockX()));
 				MyFLocation.setZ(FLocation.blockToChunk(TLocation.getBlockZ()));
 				TFaction = Board.getFactionAt(MyFLocation);
 
 				if (P.config.getConfig().getBoolean("debug")) {
 					MyPlayer.sendMessage(String.format(
 							"Chunklevel Check: x%s z%s Tags: %s %s",
 							MyFLocation.getX(), MyFLocation.getZ(), TFaction
 									.getTag(), Board.getFactionAt(MyFLocation)
 									.getTag()));
 				}
 
 				if (isAllowedFaction(TFaction, PFaction)) {
 					// // The tested location has one of the allowed factions.
 					// We can move on.
 					// pass
 					// return false later;
 				} else {
 					// // The tested location has none of the allowed factions
 
 					return true; //
 
 					// //debug
 					// //msg(MyPlayer,"Chunklevel: px %5.1f py %5.1f pz  %5.1f tx %5.1f ty %5.1f tz %5.1f"%(PLocation.getX(),PLocation.getY(),PLocation.getZ(),TLocation.getX(),TLocation.getY(),TLocation.getZ())
 					// )
 					// //msg(MyPlayer,"Chunklevel: Dist: %5.1f Allowed: %5.1f"%(dist,MaxShipsize+safetyzone))
 					// //msg(MyPlayer,"Chunklevel: Forbidden faction tag: %s"%TFaction.getTag())
 				}
 			}
 		}
 		// End of Loop - no foreign land found
 		return false;
 	}
 
 	/**
 	 * @param MyPlayer
 	 *            Da Playa
 	 * @param blocks
 	 *            Blox round playa to check fer
 	 * @return Tag of the found foreign faction or "" if none is found
 	 */
 	public static String factionBlockCheck(Player MyPlayer, int blocks) {
 
 		// ////////////////////////////////////////////////////////////////////////////////////
 		// //// Blocklevel test loop
 		// Diving into blockwise checking for foreign factions
 		// around the player (in each direction on x-z plane)
 		// /////////////////////////////////////////////////////////////////////////////////////
 		Faction tFaction = new Faction();
 		Location pLocation = MyPlayer.getLocation();
 		Location tLocation = pLocation.clone();
 		double x = pLocation.getX();
 		double z = pLocation.getZ();
 		Faction pFaction = getFaction(MyPlayer);
 		FLocation tFLocation = new FLocation(tLocation);
 
 		for (double bx = x - blocks; bx <= x + blocks; bx++) {
 			// // bx has the x coordinate of the testarea
 			// // modify testlocation
 			tLocation.setX(bx);
 			for (double bz = z - blocks; bz <= z + blocks; bz++) { // //
 				// bz has the z coordinate of the testarea
 				// // modify testlocation
 				tLocation.setZ(bz);
 
 				// //now check the factions at the blocklevel:
 				// //get faction at Test Location
 				tFLocation.setX(FLocation.blockToChunk(tLocation.getBlockX()));
 				tFLocation.setZ(FLocation.blockToChunk(tLocation.getBlockZ()));
 				tFaction = Board.getFactionAt(tFLocation);
 
 				if (isAllowedFaction(tFaction, pFaction)) {
 					// // The tested location has one of the
 					// allowed factions. We can move on.
 					if (P.config.getConfig().getBoolean("debug")) {
 						MyPlayer.sendMessage(String.format(
 								"Blocklevel allowed faction tag: %s",
 								tFaction.getTag()));
 					}
 				}
 				// pass
 				else {
 					// The tested location has none of the
 					// allowed factions
 					// We must return the found faction now
 
 					if (P.config.getConfig().getBoolean("debug")) {
 						MyPlayer.sendMessage(String.format(
 								"Blocklevel forbidden faction found: %s",
 								tFaction.getTag()));
 					}
 					// Check distance of player to the tested
 					// location
 					// dist = PLocation.distance(TLocation); // //distance
 
 					// debug
 					// //msg(MyPlayer,"Blocklevel: px %5.1f py %5.1f pz  %5.1f tx %5.1f ty %5.1f tz %5.1f"%(PLocation.getX(),PLocation.getY(),PLocation.getZ(),TLocation.getX(),TLocation.getY(),TLocation.getZ())
 					// )
 					// //msg(MyPlayer,"Blocklevel:Dist: %5.1f Allowed: %5.1f"%(dist,MaxShipsize+safetyzone))
 					// if (dist <= MaxShipsize + safetyzone) {
 					// Returning the tag of the found faction
 					return tFaction.getTag();
 					// }
 				}
 			}
 		}
 		// No forbidden faction found: returning empty string
 		return "";
 	}
 
 	/**
 	 * @param faction
 	 *            Faction to test
 	 * @param playerFaction
 	 *            Second faction to test
 	 * @return true if allowed, false if disallowed
 	 */
 	public static boolean isAllowedFaction(final Faction faction,
 			final Faction playerFaction) {
 		// P.broadcast(P.CHAT_PREFIX+"Debug: "+String.valueOf(P.config.getConfig().getBoolean(
 		// "debug")));
 		// P.broadcast(P.CHAT_PREFIX+"AllowedFactionIDs: "+P.config.getConfig()
 		// .getStringList("factions.AllowedFactionIds").toString());
 		// P.broadcast(P.CHAT_PREFIX+"PlayerFactionOK? "+String.valueOf(P.config.getConfig().getBoolean(
 		// "factions.PlayerFactionOK")));
 		// P.broadcast(P.CHAT_PREFIX+"AlliedFactionOK? "+String.valueOf(P.config.getConfig().getBoolean(
 		// "factions.AlliedFactionOK")));
 		// if (debug) {
 		// P.broadcast(String.format(
 		// "isAllowedfaction? F= %s; Playerfaction= %s",
 		// faction.getTag(), playerFaction.getTag()));
 		// }
 		// Check 0: Flying everywhere??
 		if (P.config.getConfig().getBoolean("factions.AllAllowed")) {
 			return true;
 		}
 		// Check 1: Is tested faction in the allowed factions list?
 		if (P.config.getConfig().getStringList("factions.AllowedFactionIds")
 				.contains(faction.getId())) {
 			return true;
 		}
 		// Check 2: Is the faction the players faction and is this allowed?
 		else if ((P.config.getConfig().getBoolean("factions.PlayerFactionOK") && (faction
 				.getId().equals(playerFaction.getId())))) {
 			return true;
 		}
 		// Check 3: Is the faction allied to the players faction and is this
 		// allowed?
 		else if ((P.config.getConfig().getBoolean("factions.AlliedFactionOK") && (playerFaction
 				.getRelationTo(faction) == Relation.ALLY))) {
 			return true;
 		}
 		// No allowed faction found
 		// @ToDo: Check for allies
 		return false;
 	}
 
 	// /// Event Listeners
 	@EventHandler(priority = EventPriority.LOWEST)
 	public static void onPlayerInteract(PlayerInteractEvent event) {
 
 		// // Get the player
 		Player MyPlayer = event.getPlayer();
 
 		// //////////////////////////////////////////////////////////
 		// Check if Player is a pilot - if not just leave
 		// //////////////////////////////////////////////////////////
 		if (!(P.isPilot(MyPlayer))) {
 			// //player is no pilot
 			return;
 		}
 		// //////////////////////////////////////////////////////////////////
 		// // Player is a Pilot, so lets get stuff we need
 		// // Getting some data:
 		// //////////////////////////////////////////////////////////////////
 
 		// // There is the property MAX_SHIP_DIMENSIONS - we need to make
 		// sure, that the pilot is at least this amount of blocks plus
 		// a safety distance away from foreign land
 		// Therefore we need to get the ship the player is piloting and
 		// check its MAX_SHIP_DIMENSIONS property.
 		int MaxShipsize = getMaxShipsize(MyPlayer);
 		// We also need the move speed of the ship
 		int MoveSpeed = getMoveSpeed(MyPlayer);
 
 		if (P.config.getConfig().getBoolean("debug")) {
 			MyPlayer.sendMessage(String.format(
 					"onInteract: MAX_SHIP_DIMENSIONS: %s; MOVE_SPEED: %s",
 					MaxShipsize, MoveSpeed));
 			MyPlayer.sendMessage(String.format(
 					"onInteract: Global safetyzone is %s Blocks", P.config
 							.getConfig().getInt("safetyzone")));
 		}
 
 		// Amount of chunks around the player to test for foreign land. 1
 		// means a 3x3 chunks area around the player is checked.
 		// This must be at least of the size MAX_SHIP_DIMENSION plus the
 		// safetydistance plus the move speed to stay away from foreign
 		// territory
 		int chunks = ((MaxShipsize + P.config.getConfig().getInt("safetyzone") + MoveSpeed) / 16) + 1;
 
 		if (P.config.getConfig().getBoolean("debug")) {
 			MyPlayer.sendMessage(String.format(
 					"SetData: chunks is %s chunks arund the Pilot", chunks));
 		}
 
 		if (hasForeignChunks(MyPlayer, chunks)) {
 			// do blocklevel check for ForeignFactions
 			int blocks = MaxShipsize
 					+ P.config.getConfig().getInt("safetyzone") + MoveSpeed;
 			String FFac = factionBlockCheck(MyPlayer, blocks);
 			if (!(FFac.equals(""))) {
 				Autocraft.shipmanager.ships.remove(MyPlayer.getName()); // unpiloting
 				// event.setCancelled(true); Cancelling the event did not
 				// prevent ship movements. Only unpiloting the player
 				// effectively stopped it
 
 				MyPlayer.sendMessage(MessageFormat
 						.format("\u00a7cToo close to forbidden zone: \u00a7b{0}\u00a7c. \u00a7eAircontrol does not grant flight permission here! You have been unpiloted.",
 								FFac));
 
 			}
 		}
 
 		// // End of Player Interaction Eventhandler
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public static void onPlayerCommandPreprocess(
 			PlayerCommandPreprocessEvent event) {
 		if (P.config.getConfig().getBoolean("debug")) {
 			P.broadcast("Entering CmdPre");
 		}
 		// //////////////////////////////
 		// // Plugin startup logic: ////
 		// //////////////////////////////
 
 		// get the command the player wants to issue
 		String Cmd = event.getMessage();
 		Cmd = Cmd.toLowerCase();
 		// Split up the command in its components
 		String Words[] = event.getMessage().split(" ");
 
 		// do we have an ac command (=autocraft)? if not return.
 		if (!(Words[0].equals("/ac"))) {
 			if (P.config.getConfig().getBoolean("debug")) {
 				P.broadcast("Issued non-ac command: ".concat(Words[0]));
 			}
 			return;
 		}
 
 		// ////////////////////////////////////////////////////////////////
 		// //////////////////// Getting some data: ////////////////////////
 		// ////////////////////////////////////////////////////////////////
 
 		// Get the player
 		Player MyPlayer = event.getPlayer();
 
 		if (P.config.getConfig().getBoolean("debug")) {
 			MyPlayer.sendMessage(String.format(
 					"DbgCmd: Hello %s! You issued the command: %s",
 					MyPlayer.getName(), Cmd));
 		}
 		// Get player faction
 		// get faction player from player:
 		FPlayer FPlayer = FPlayers.i.get(MyPlayer);
 
 		// get faction from Fplayer whos is our player
 		Faction PFaction = FPlayer.getFaction();
 
 		if (P.config.getConfig().getBoolean("debug")) {
 			MyPlayer.sendMessage(String.format("DbgCmd: Your faction is %s",
 					PFaction.getTag()));
 		}
 
 		// //////////////////////
 		// // Command Parser ////
 		// //////////////////////
 
 		switch (P.aclvl2Cmds.valueOf(Words[1])) {
 
 		case pilot:
 			// Handling /ac pilot command here
 			// If the player attempts to pilot a ship what shall happen?
			if (P.config.getConfig().getBoolean("debug")) {
				P.msg(MyPlayer, "Loc : " + MyPlayer.getLocation().toString());
				P.msg(MyPlayer, "Last: " + FPlayer.getLastStoodAt().toString());
			}
 			if (isAllowedFaction(Board.getFactionAt(FPlayer.getLastStoodAt()),
 					PFaction)) {
 
 				if (P.config.getConfig().getBoolean("debug")) {
 					P.broadcast("pilot check: Standing on land of allowed faction: "
 							.concat(Board
 									.getFactionAt(FPlayer.getLastStoodAt())
 									.getTag()));
 					P.broadcast("Leaving case pilot, nocancel. event:"
 							.concat(event.toString()));
 				}
 				return;
 			} else {
 
 				if (P.config.getConfig().getBoolean("debug")) {
 					P.broadcast("pilot check: Standing on land of forbidden faction: "
 							.concat(Board
 									.getFactionAt(FPlayer.getLastStoodAt())
 									.getTag()));
 				}
 
 				// Okay - he is in forbidden territory
 				// Now screw him up that crappy pilot !
 				Autocraft.shipmanager.ships.remove(MyPlayer.getName()); // unpiloting
 				event.setCancelled(true);
 				// player
 				MyPlayer.sendMessage(String
 						.format("\u00a7eAircontrol does not allow piloting here! (%s's land)",
 								Board.getFactionAt(FPlayer.getLastStoodAt())
 										.getTag()));
 				if (P.config.getConfig().getBoolean("debug")) {
 					P.broadcast("Leaving case pilot, cancelled");
 				}
 				return;
 			}
 		case turn:
 			// ////////////////////////////////////////////////////////
 			// Core logic - player is verified as being a pilot
 			// ////////////////////////////////////////////////////////
 			if (!(Autocraft.shipmanager.ships.containsKey(MyPlayer.getName()))) {
 				// player is no pilot - we can ignore and return
 				return;
 			}
 			// //////////////////////////////////////////////////////////////////
 			// ////////////////////// Defining some data:
 			// //////////////////////////////////////////////////////////////////
 
 			// There is the property MAX_SHIP_DIMENSIONS - i need to make sure,
 			// that the pilot is at least this amount of blocks plus a safety
 			// distance away from foreign land before turning the ship
 			// Therefore we need to get the ship the player is piloting and
 			// check its MAX_SHIP_DIMENSIONS property.
 			int MaxShipsize = Autocraft.shipmanager.ships.get(MyPlayer
 					.getName()).properties.MAX_SHIP_DIMENSIONS;
 			int MoveSpeed = getMoveSpeed(MyPlayer);
 
 			// if a player attempts to turn his ship he must be in a safe
 			// distance from foreign territory:
 			// which is safezone+MaxShipsize
 			// debug
 			if (P.config.getConfig().getBoolean("debug")) {
 				MyPlayer.sendMessage(String.format("Autocraft command: %s",
 						Words[1]));
 				MyPlayer.sendMessage(String.format(
 						"PilotData: MAX_SHIP_DIMENSIONS: %s, MoveSpeed: %s",
 						MaxShipsize, MoveSpeed));
 				MyPlayer.sendMessage(String.format(
 						"ConfigData: safetyzone is %s Blocks", P.config
 								.getConfig().getInt("safetyzone")));
 			}
 
 			// Amount of blocks around the player to test for foreign land.
 			// 1 means a 3x3 chunks area around the player is checked.
 			// This must be at least of the size MAX_SHIP_DIMENSION plus the
 			// safetydistance plus the shipspeed to stay away from foreign
 			// territory
 			int chunks = (MaxShipsize + P.config.getConfig().getInt(
 					"safetyzone")) / 16 + 1;
 			if (P.config.getConfig().getBoolean("debug")) {
 				MyPlayer.sendMessage(String.format(
 						"SetData: turn - chunks is %s chunks arund the Pilot",
 						chunks));
 
 			}
 
 			// ////////////////////////////////////////////////////////
 			// // Chunklevel test loop
 			// ////////////////////////////////////////////////////////
 			// If no foreign factions are found in the surrounding chunks
 			// within the test didstance we can return - else we make a
 			// blocklevel check
 			if (!(hasForeignChunks(MyPlayer, chunks))) {
 				return;
 			}
 			// The tested chunks have none of the allowed factions
 			// We must to further tests and maybe screw him up since a foreign
 			// faction was found in the currently checked location
 			// Check distance of player to the tested location
 			// ///////////////////////////////////////////////////////////////////////////////
 			// // Blocklevel test loop - reached when a foreign faction is found
 			// at chunklevel
 			// ///////////////////////////////////////////////////////////////////////////////
 			// Now we need to dive into blockwise checking for foreign factions
 			// test area is a region of saftetyzone+MaxShipSize around the
 			// player (in each direction on x-z plane)
 			// for just turning the ship we don't need to check the speed
 			int blocks = P.config.getConfig().getInt("safetyzone")
 					+ MaxShipsize;
 			String FactionBlockCheckResult = factionBlockCheck(MyPlayer, blocks);
 			if (FactionBlockCheckResult.equals("")) {
 				return;
 			}
 			// The tested location has none of the allowed factions
 			// We must calculate distance now
 			if (P.config.getConfig().getBoolean("debug")) {
 				MyPlayer.sendMessage(String.format(
 						"Blocklevel forbidden faction tag: %s",
 						FactionBlockCheckResult));
 			}
 			// Okay - he is too near !
 			// Autocraft.shipmanager.ships.remove(MyPlayer.getName()); //
 			// unpiloting
 			event.setCancelled(true);// cancelling command
 			// player
 			MyPlayer.sendMessage(String
 					.format("\u00a7cToo close to forbidden zone: \u00a7b%s\u00a7c. \u00a7eAircontrol does not allow turning here!",
 							FactionBlockCheckResult));
 			// end of case "turn"
 
 		default:
 			break;
 
 		// end of switch
 		}
 
 		// End of Player Command Eventhandler
 		// return;
 	}
 
 	// End of Listener Class
 }
