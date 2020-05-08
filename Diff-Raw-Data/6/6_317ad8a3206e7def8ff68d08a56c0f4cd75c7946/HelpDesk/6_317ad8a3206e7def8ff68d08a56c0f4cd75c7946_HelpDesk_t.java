 package com.connor.helpdesk;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 public class HelpDesk extends JavaPlugin {
 
     public Logger log = Logger.getLogger("Minecraft");
     private ArrayList<HelpTicket> tickets;
     
     private HelpCommandExecutor commandExecutor;
     
     public void onEnable() {
         tickets = new ArrayList<HelpTicket>();
         commandExecutor = new HelpCommandExecutor(this);
 
         getCommand("helpdesk").setExecutor(commandExecutor);
         getServer().getPluginManager().registerEvents(new HelpDeskGodListener(this), this);
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 int modTickets = 0;
                 int adminTickets = 0;
                 int opTickets = 0;
                 HashMap<String, Integer> playerAssignedCounts = new HashMap<String, Integer>();
                 for (HelpTicket ticket : tickets) {
                     if (!ticket.isAssigned() && !ticket.isCompleted()) {
                         if (ticket.getLevel() == HelpLevel.MOD) {
                             modTickets++;
                             adminTickets++;
                             opTickets++;
                         } else if (ticket.getLevel() == HelpLevel.ADMIN) {
                             adminTickets++;
                             opTickets++;
                         } else if (ticket.getLevel() == HelpLevel.OP) {
                             opTickets++;
                         }
                     } else if (ticket.isAssigned() && !ticket.isCompleted()) {
                         int current = 0;
                         if (playerAssignedCounts.get(ticket.getAssignedUser()) != null)
                             current = playerAssignedCounts.get(ticket.getAssignedUser());
                         playerAssignedCounts.put(ticket.getAssignedUser(), ++current);
                     }
                 }
                 
                 if (modTickets > 0) {
                     if (modTickets == 1) {
                         sendMessageToStaffLevel(HelpLevel.MOD, ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "There is " + ChatColor.DARK_GREEN + "1 ticket open");
                     } else {
                         sendMessageToStaffLevel(HelpLevel.MOD, ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "There are " + ChatColor.DARK_GREEN + modTickets + " tickets open");
                     }
                 }
                 if (adminTickets > 0) {
                     if (modTickets == 1) {
                         sendMessageToStaffLevel(HelpLevel.ADMIN, ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "There is " + ChatColor.DARK_GREEN + "1 ticket open");
                     } else {
                         sendMessageToStaffLevel(HelpLevel.ADMIN, ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "There are " + ChatColor.DARK_GREEN + adminTickets + " tickets open");
                     }
                 }
                 if (opTickets > 0) {
                     if (opTickets == 1) {
                         sendMessageToStaffLevel(HelpLevel.OP, ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "There is " + ChatColor.DARK_GREEN + "1 ticket open");
                     } else {
                         sendMessageToStaffLevel(HelpLevel.OP, ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "There are " + ChatColor.DARK_GREEN + opTickets + " tickets open");
                     }
                 }
                 
                 for (String playerName : playerAssignedCounts.keySet()) {
                     Player p = getServer().getPlayerExact(playerName);
                     if (p == null) continue;
                     int amount = playerAssignedCounts.get(playerName);
 
                     if (amount == 1) {
                         p.sendMessage(ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "You are " + ChatColor.DARK_GREEN + "assigned to 1 ticket");
                     } else {
                         p.sendMessage(ChatColor.GOLD + "[HELPDESK] " + ChatColor.GRAY + "You are " + ChatColor.DARK_GREEN + "assigned to " + amount + " tickets");
                     }
                 }
             }
         }, 600, 600);
 
         log.info("HelpDesk enabled");
     }
 
     public void onDisable() {
         getServer().broadcastMessage(ChatColor.GOLD + "[HELPDESK] " + ChatColor.RED + "Tickets cleared");
         log.info("HelpDesk disabled");
     }
 
     public void addTicket(HelpTicket ticket) {
         synchronized (this) {
             if (!tickets.contains(ticket)) tickets.add(ticket);
         }
     }
     
     public HelpTicket getTicketWithID(int ID) {
         synchronized(this) {
             for (HelpTicket ticket : tickets) {
                 if (ticket.getID() == ID) {
                     return ticket;
                 }
             }
             return null;
         }
     }
     
     public HelpTicket getTicketWithID(String ID) {
         try {
             return getTicketWithID(Integer.parseInt(ID));
         } catch (NumberFormatException e) {
             return null;
         }
     }
     
     public boolean removeTicket(HelpTicket ticket) {
         synchronized (this) {
             return tickets.remove(ticket);
         }
     }
     
     public ArrayList<HelpTicket> sortTickets() {
         synchronized (this) {
             Collections.sort(tickets, new Comparator<HelpTicket>() {
                 public int compare(HelpTicket o1, HelpTicket o2) {
                     if (o1.isUrgent() && !o2.isUrgent()) {
                         return -1; //first is urgent, should go before second    
                     } else if (o2.isUrgent() && !o1.isUrgent()) {
                         return 1; //second is urgent, should go before first
                     } else {
                         if (o1.getLevel().toInt() > o2.getLevel().toInt()) {
                             return -1; //first has higher level, should go before second
                         } else if (o2.getLevel().toInt() > o1.getLevel().toInt()) {
                             return 1; //second has higher level, should go before first
                         } else {
                             if (o1.getFileTime() < o2.getFileTime()) {
                                 return -1; //first was filed before second
                             } else if (o2.getFileTime() < o1.getFileTime()) {
                                 return 1; //second was filed before first
                             } else {
                                 return 0; //probably will never happen. they were both filed at the same time.
                             }
                         }
                     }
                 }
             });
             return tickets;
         }
     }
 
     public void sendMessageToStaffLevel(HelpLevel level, String message) {
         for (Player p : getServer().getOnlinePlayers()) {
             if (level == HelpLevel.MOD) {
                 if (HelpLevel.getPlayerHelpLevel(p) == HelpLevel.MOD) {
                     p.sendMessage(message);
                 }
             } else if (level == HelpLevel.ADMIN) {
                 if (HelpLevel.getPlayerHelpLevel(p) == HelpLevel.ADMIN) {
                     p.sendMessage(message);
                 }
             } else if (level == HelpLevel.OP) {
                 if (HelpLevel.getPlayerHelpLevel(p) == HelpLevel.OP) {
                     p.sendMessage(message);
                 }
             }
         }
     }
 
     public void notifyAllHelpdeskStaff(String message) {
         for (Player p : getServer().getOnlinePlayers()) {
             if (isHelpdeskStaff(p)) {
                 p.sendMessage(message);
             }
         }
     } 
     
     public boolean isHelpdeskStaff(Player player) {
         return HelpLevel.getPlayerHelpLevelInt(player) > HelpLevel.NONE.toInt();
     }
     
     public boolean doesHaveTicketAssigned(Player player) {
         for (HelpTicket ticket : tickets) {
            if (ticket.getAssignedUser() != null) {
                if (ticket.getAssignedUser().equals(player.getName()))
                    return true;
            }
         }
         return false;
     }
 }
