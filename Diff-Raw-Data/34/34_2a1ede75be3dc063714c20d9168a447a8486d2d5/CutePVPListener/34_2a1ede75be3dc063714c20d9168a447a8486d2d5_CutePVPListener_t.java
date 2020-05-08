 package com.c45y.CutePVP;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.c45y.CutePVP.buff.TeamBuff;
 
 // ----------------------------------------------------------------------------
 /**
  * Event listener implementation.
  * 
  * The synchronous PlayerChatEvent is deprecated but it's just so much easier to
  * handle that way.
  */
 @SuppressWarnings("deprecation")
 public class CutePVPListener implements Listener {
 	// ------------------------------------------------------------------------
 	/**
 	 * Constructor.
 	 * 
 	 * @param plugin the plugin.
 	 */
 	public CutePVPListener(CutePVP plugin) {
 		_plugin = plugin;
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Cancel attempts to take off the woolen helmet.
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onInventoryClick(InventoryClickEvent event) {
 		Player player = (Player) event.getWhoClicked();
 		if (!_plugin.getTeamManager().isExempted(player) && event.getSlot() == 39) {
 			event.setCancelled(true);
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Respawn players in their team spawn.
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
 		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(event.getPlayer());
 		if (teamPlayer != null) {
 			_plugin.getLogger().info(event.getPlayer().getName() + " respawned on " + teamPlayer.getTeam().getName() + ".");
 			teamPlayer.setHelmet();
 			event.setRespawnLocation(teamPlayer.getTeam().getSpawn());
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * On join, allocate players to a team if not exempted.
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		TeamManager tm = _plugin.getTeamManager();
 		Player player = event.getPlayer();
 		if (player.hasPermission(Permissions.MOD)) {
 			tm.onStaffJoin(player);
 			_plugin.getLogger().info(player.getName() + " has staff permissions.");
 		}
 		if (tm.isExempted(player)) {
 			_plugin.getLogger().info(player.getName() + " is exempted from team assignment.");
 			return;
 		}
 
 		TeamPlayer teamPlayer = tm.getTeamPlayer(player);
 		if (teamPlayer == null) {
 			// We don't care whether they have played before or not. If not
 			// exempted, allocate to a team now.
 			tm.onFirstJoin(player);
 			teamPlayer = tm.getTeamPlayer(player);
 		} else {
 			Team team = teamPlayer.getTeam();
 			player.sendMessage("You're on " + team.encodeTeamColor(team.getName()) + ".");
 			_plugin.getLogger().info(player.getName() + " rejoined " + team.getName() + ".");
 		}
 
 		// The old OfflinePlayer instance can't be used to reference the player.
 		// This new one can, so store that.
 		teamPlayer.setOfflinePlayer(event.getPlayer());
 
 		player.setDisplayName(teamPlayer.getTeam().encodeTeamColor(player.getName()));
 		teamPlayer.setHelmet();
 		event.setJoinMessage(player.getDisplayName() + " joined the game.");
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * When leaving, show the (colorful) display name in the leave message.
 	 * 
 	 * Drop the flag if the player is carrying it.
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player player = event.getPlayer();
 		event.setQuitMessage(player.getDisplayName() + " left the game.");
 
 		TeamManager tm = _plugin.getTeamManager();
 		TeamPlayer teamPlayer = tm.getTeamPlayer(player);
 		if (teamPlayer != null && teamPlayer.isCarryingFlag()) {
 			teamPlayer.getCarriedFlag().drop();
 		}
 		tm.onPlayerQuit(player);
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * When the player steps on a block with special properties, apply the
 	 * corresponding buff.
 	 */
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
 		if (teamPlayer != null) {
 			_plugin.updateTeamPlayerEffects(teamPlayer);
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Bar held and projectile weapon PvP damage in player's own team's base.
 	 * 
 	 * Also block damage from own team members, and between participants and
 	 * non-participants (both cases).
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
 		if (!(event.getEntity() instanceof Player)) {
 			return;
 		}
 
 		if (event.getDamager() instanceof Player) {
 			TeamPlayer attacker = _plugin.getTeamManager().getTeamPlayer((Player) event.getDamager());
 			TeamPlayer victim = _plugin.getTeamManager().getTeamPlayer((Player) event.getEntity());
 			handlePvPDamage(event, attacker, victim);
 
 		} else if (event.getDamager() instanceof Projectile) {
 			Projectile projectile = (Projectile) event.getDamager();
 			if (projectile.getShooter() instanceof Player) {
 				TeamPlayer attacker = _plugin.getTeamManager().getTeamPlayer((Player) projectile.getShooter());
 				TeamPlayer victim = _plugin.getTeamManager().getTeamPlayer((Player) event.getEntity());
 				handlePvPDamage(event, attacker, victim);
 			}
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Handle EntityDamageByEntityEvent by checking for PvP and disallowing PvP
 	 * damage when:
 	 * 
 	 * <ul>
 	 * <li>Between participants and non-participants, or</li>
 	 * <li>Between participants on the same team, or</li>
 	 * <li>When the victim is in his team's base.</li>
 	 * </ul>
 	 */
 	protected void handlePvPDamage(EntityDamageByEntityEvent event, TeamPlayer attacker, TeamPlayer victim) {
 		if (attacker == null || victim == null) {
 			// One or other player is not a participant. No damage dealt.
 			event.setCancelled(true);
 		} else if (attacker.getTeam() == victim.getTeam()) {
 			// Both players on the same team. No damage dealt.
 			event.setCancelled(true);
 		} else if (victim.getTeam().inTeamBase(victim.getPlayer().getLocation())) {
 			attacker.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot attack within another team's base.");
 			event.setCancelled(true);
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Handle flag steals, captures (scoring) and returns, which all require
 	 * that a player right clicks on a flag.
 	 * 
 	 * <ul>
 	 * <li>To steal a flag, a player must right click on the opposing team's
 	 * flag (whether dropped or at home).</li>
 	 * <li>To capture a flag, a player must be carrying an opposing team's flag
 	 * when he clicks on his own flag at home.</li>
 	 * <li>To return a flag, a player must click on his own flag when it is
 	 * dropped (not at home).</li>
 	 * </ul>
 	 * 
 	 * @param event the event.
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		Player player = event.getPlayer();
 		TeamManager tm = _plugin.getTeamManager();
 		TeamPlayer teamPlayer = tm.getTeamPlayer(player);
 		if (teamPlayer == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
 			return;
 		}
 
 		// Is the clicked block a team buff?
 		Block clickedBlock = event.getClickedBlock();
 		TeamBuff teamBuff = _plugin.getBuffManager().getTeamBuffFromBlock(clickedBlock);
 		if (teamBuff != null) {
 			teamBuff.claimBy(teamPlayer);
 			return;
 		}
 
 		// Is it a team block and therefore possibly a flag?
 		Team clickedBlockTeam = tm.getTeamFromBlock(clickedBlock);
 		if (clickedBlockTeam != null) {
 			// Block is of the same material as a team block. Is it a flag?
 			Flag flag = clickedBlockTeam.getFlagFromBlock(clickedBlock);
 			if (flag != null) {
 				// Player's own team flag?
 				if (clickedBlockTeam == teamPlayer.getTeam()) {
 					// Returning a dropped flag?
 					if (flag.isDropped()) {
 						flag.doReturn();
 						teamPlayer.getScore().returns.increment();
 						teamPlayer.getTeam().getScore().returns.increment();
 						Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR + " returned " +
 											teamPlayer.getTeam().getName() + "'s flag.");
 					} else if (flag.isHome() && teamPlayer.isCarryingFlag()) {
 						// Capturing an opposition team's flag.
 						teamPlayer.getScore().captures.increment();
 						teamPlayer.getTeam().getScore().captures.increment();
 
 						Flag carriedFlag = teamPlayer.getCarriedFlag();
 						carriedFlag.doReturn();
 						Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR + " captured " +
 											carriedFlag.getTeam().getName() + "'s " + carriedFlag.getName() + " flag.");
 					}
 				} else {
 					// An opposition team flag.
 					if (teamPlayer.isCarryingFlag()) {
 						player.sendMessage(ChatColor.DARK_RED + "You can only carry one flag at a time.");
 					} else {
 						flag.stealBy(teamPlayer);
 						teamPlayer.getScore().steals.increment();
 						teamPlayer.getTeam().getScore().steals.increment();
 						Messages.broadcast(player.getDisplayName() + Messages.BROADCAST_COLOR +
 											" has stolen " + clickedBlockTeam.getName() + "'s flag.");
 					}
 				}
 			}
 		}
 	} // onPlayerInteract
 
 	// ------------------------------------------------------------------------
 	/**
 	 * When a player dies:
 	 * 
 	 * <ul>
 	 * <li>Drop the flag if he's carrying it.</li>
 	 * <li>Increment his and his team's kill scores if they are not on the same
 	 * team. Players can't hurt their team mates, so the check is redundant, but
 	 * cheap and future-proof.</li>
 	 * </ul>
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
 		if (teamPlayer != null) {
 			if (teamPlayer.isCarryingFlag()) {
 				teamPlayer.getCarriedFlag().drop();
 			}
 
 			Player killer = player.getKiller();
 			TeamPlayer teamKiller = _plugin.getTeamManager().getTeamPlayer(killer);
 			if (teamKiller != null && teamPlayer.getTeam() != teamKiller.getTeam()) {
 				teamKiller.getTeam().getScore().kills.increment();
 				teamKiller.getScore().kills.increment();
 			}
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Handle chat by cancelling the normal event and directly messaging players
 	 * with team colors inserted.
 	 */
 	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerChat(PlayerChatEvent event) {
 		event.setCancelled(true);
 		Player player = event.getPlayer();
 		_plugin.getLogger().info(player.getName() + ": " + ChatColor.stripColor(event.getMessage()));
 
 		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
 		String message = "<" + player.getDisplayName() + "> " + ChatColor.stripColor(event.getMessage());
 		if (teamPlayer != null) {
 			// A match participant.
 			Team team = teamPlayer.getTeam();
 			team.message(message);
 		}
 
 		// Copy the message to staff (all non-participants).
 		for (Player staff : _plugin.getTeamManager().getOnlineStaff()) {
 			staff.sendMessage(message);
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Allow block placement per
 	 * {@link CutePVPListener#allowBlockEdit(Player, Location)}.
 	 */
 	@EventHandler(ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (!allowBlockEdit(event.getPlayer(), event.getBlock().getLocation())) {
 			event.setCancelled(true);
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Allow block breaks per
 	 * {@link CutePVPListener#allowBlockEdit(Player, Location)}.
 	 */
 	@EventHandler(ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (!allowBlockEdit(event.getPlayer(), event.getBlock().getLocation())) {
 			event.setCancelled(true);
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Return true if attempted edits should be allowed.
 	 * 
 	 * <ul>
 	 * <li>Staff can edit blocks anywhere.</li>
 	 * <li>Players in their own base regions can edit, except for the flag
 	 * blocks.</li>
 	 * <li>Players in the base regions of other teams cannot edit any blocks.</li>
 	 * </ul>
 	 * 
 	 * @param player the player doing the edit.
 	 * @param location the location of the edited block.
 	 * @return true if attempted edits should be allowed.
 	 */
 	protected boolean allowBlockEdit(Player player, Location location) {
 		TeamPlayer teamPlayer = _plugin.getTeamManager().getTeamPlayer(player);
 		if (teamPlayer == null) {
 			// Staff member.
 			return true;
 		}
 
 		if (teamPlayer.getTeam().inTeamBase(location)) {
 			return !teamPlayer.getTeam().isFlagHomeLocation(location);
		} else if (_plugin.getTeamManager().inEnemyTeamBase(player, location)) {
 			player.sendMessage(ChatColor.DARK_RED + "You cannot build in an enemy base");
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	// ------------------------------------------------------------------------
 	/**
 	 * Owning plugin.
 	 */
 	private final CutePVP _plugin;
 } // CutePVPListener
