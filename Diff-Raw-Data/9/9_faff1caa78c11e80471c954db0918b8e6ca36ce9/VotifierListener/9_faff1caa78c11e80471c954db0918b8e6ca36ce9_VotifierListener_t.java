 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.utils.IRCBot;
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VotifierEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 /**
  *
  * @author Bruce
  */
 public class VotifierListener implements Listener {
 	public ArcherGames plugin;
 	
 	public VotifierListener(ArcherGames plugin){
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL)
     public void onVotifierEvent(VotifierEvent event) {
 		Vote vote = event.getVote();
 		String message = vote.getUsername()+"§"+vote.getServiceName()+"§"+vote.getAddress()+"§"+vote.getTimeStamp();
 		plugin.IRCBot.bot.sendMessage(plugin.IRCBot.channel, message);
 		if(plugin.getServer().getPlayer(vote.getUsername())!=null){
			plugin.econ.givePlayer(plugin.getServer().getPlayer(vote.getUsername()).getName(), 3000);
 		}
 	}
 }
