 package com.herocraftonline.dev.heroes.command.commands;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.BasicCommand;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class PartyInviteCommand extends BasicCommand {
 
     private static final int MAX_PARTY_SIZE = 10;
     private final Heroes plugin;
 
     public PartyInviteCommand(Heroes plugin) {
         super("Party Invite");
         this.plugin = plugin;
         setDescription("Invites a player to your party");
         setUsage("/party invite");
         setArgumentRange(1, 1);
         setIdentifiers(new String[] { "party invite" });
     }
 
     @Override
     public boolean execute(CommandSender sender, String identifier, String[] args) {
         if (!(sender instanceof Player)) return false;
 
         Player player = (Player) sender;
         Player target = plugin.getServer().getPlayer(args[0]);
         Hero hero = plugin.getHeroManager().getHero(player);
 
         if (target == null) {
             Messaging.send(player, "Player not found.");
             return false;
         }

         if (hero.getParty() == null) {
             HeroParty newParty = new HeroParty(hero);
             plugin.getPartyManager().addParty(newParty);
             hero.setParty(newParty);
             Messaging.send(player, "Your party has been created");
         }
 
        HeroParty party = hero.getParty();

         if (!party.getLeader().equals(hero)) {
             Messaging.send(player, "You are not leader of this party.");
             return false;
         }
 
         if (target.equals(player)) {
             Messaging.send(player, "You cannot invite yourself.");
             return false;
         }
 
         int memberCount = party.getMembers().size();
 
         if (memberCount >= MAX_PARTY_SIZE) {
             Messaging.send(player, "Your party is full.");
             return false;
         }
 
         if (memberCount + party.getInviteCount() >= MAX_PARTY_SIZE) {
             party.removeOldestInvite();
         }
 
         party.addInvite(target.getName());
         Messaging.send(target, "$1 has invited you to their party", player.getName());
         Messaging.send(target, "Type /party accept $1 to join", player.getName());
         Messaging.send(player, "$1 has been invited to your party", target.getName());
 
         return true;
     }
 
 }
