 package net.robbytu.banjoserver.bungee.directsupport;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.event.ChatEvent;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.event.EventHandler;
 import net.robbytu.banjoserver.bungee.Main;
 
 public class TicketChatHandler implements Listener {
     @EventHandler
     public static void handleChat(ChatEvent event) {
        if(event.getMessage().substring(0, 1).equals("/")) return;
 
        ProxiedPlayer sender = null;
         for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
             if(player.getAddress().equals(event.getSender().getAddress())) {
                 sender = player;
             }
         }
 
         if(sender == null || !Tickets.inTicket(sender.getName())) return;
 
         Ticket ticket = Tickets.getCurrentTicketForUser(sender.getName());
         String origMessage = event.getMessage();
 
         if(ticket.admin.equalsIgnoreCase(sender.getName())) {
             event.setMessage(ChatColor.DARK_AQUA + "[Support: " + ticket.admin + " -> " + ticket.username + "] " + ChatColor.AQUA + origMessage);
             if(Main.instance.getProxy().getPlayer(ticket.username) != null) Main.instance.getProxy().getPlayer(ticket.username).sendMessage(event.getMessage());
         }
         else {
             event.setMessage(ChatColor.DARK_AQUA + "[Support: " + ticket.username + " -> " + ticket.admin + "] " + ChatColor.AQUA + origMessage);
             if(Main.instance.getProxy().getPlayer(ticket.admin) != null) Main.instance.getProxy().getPlayer(ticket.admin).sendMessage(event.getMessage());
         }
 
         sender.sendMessage(event.getMessage());
         Main.instance.getLogger().info("Ticket #" + ticket.id + ": " + event.getMessage());
 
         event.setCancelled(true);
     }
 }
