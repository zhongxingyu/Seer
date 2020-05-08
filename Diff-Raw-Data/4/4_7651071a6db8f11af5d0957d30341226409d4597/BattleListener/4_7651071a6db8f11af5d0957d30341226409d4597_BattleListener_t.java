 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleEndEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleParticipateEvent;
 import com.censoredsoftware.Demigods.Engine.Event.Battle.BattleStartEvent;
 import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedBattle;
 import com.google.common.base.Joiner;
 
 public class BattleListener implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBattleStart(BattleStartEvent event)
 	{
 		PlayerCharacter attacker = event.getAttacking();
 		PlayerCharacter defending = event.getDefending();
 		String attackerAlliance = attacker.getAlliance();
 		String defendingAlliance = defending.getAlliance();
 
		// Demigods.message.broadcast(ChatColor.RED + "BETA: " + ChatColor.YELLOW + "A battle has begun between the " + ChatColor.GREEN + attackerAlliance + "s" + ChatColor.YELLOW + " and the " + ChatColor.GREEN + defendingAlliance + "s" + ChatColor.YELLOW + ".");
		// Demigods.message.broadcast(ChatColor.RED + "BETA: " + ChatColor.GREEN + attacker.getName() + ChatColor.YELLOW + " took the first hit against " + ChatColor.GREEN + defending.getName() + ChatColor.YELLOW + ".");
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBattleParticipate(BattleParticipateEvent event)
 	{
 		// TODO
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBattleEnd(BattleEndEvent event)
 	{
 		long battleID = event.getID();
 		TrackedBattle battle = TrackedBattle.getBattle(battleID);
 		Demigods.message.broadcast(ChatColor.RED + "BETA: " + ChatColor.YELLOW + "The battle started by " + ChatColor.GREEN + battle.getWhoStarted().getName() + ChatColor.YELLOW + " has ended.");
 		Set<PlayerCharacter> chars = battle.getInvolvedCharacters();
 		Set<String> charNames = new HashSet<String>();
 		for(PlayerCharacter character : chars)
 			charNames.add(character.getName());
 		Demigods.message.broadcast(ChatColor.RED + "BETA: " + ChatColor.YELLOW + "The battle involved: " + ChatColor.AQUA + Joiner.on(", ").join(charNames) + ChatColor.YELLOW + ".");
 	}
 
 	// TODO Fix this.
 	// @EventHandler(priority = EventPriority.MONITOR)
 	// public void onBattleCombine(BattleCombineEvent event)
 	// {
 	// TrackedBattle first = event.getFirst();
 	// Set<PlayerCharacter> chars = combined.getInvolvedCharacters();
 	// Set<String> charNames = new HashSet<String>();
 	// for(PlayerCharacter character : chars)
 	// charNames.add(character.getName());
 	// Demigods.message.broadcast(ChatColor.RED + "BETA: " + ChatColor.YELLOW + "The battle started by " + ChatColor.GREEN + first.getWhoStarted().getName() + ChatColor.YELLOW + " has merged with another battle!");
 	// Demigods.message.broadcast(ChatColor.RED + "BETA: " + ChatColor.YELLOW + "The battle now involves the following: " + ChatColor.AQUA + Joiner.on(", ").join(charNames) + ChatColor.YELLOW + ".");
 	// }
 }
