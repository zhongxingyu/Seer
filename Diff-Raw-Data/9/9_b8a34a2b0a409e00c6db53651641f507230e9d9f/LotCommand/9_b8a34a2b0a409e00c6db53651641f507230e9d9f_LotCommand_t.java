 package info.bytecraft.commands;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.math.Point;
 import info.bytecraft.api.math.Rectangle;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IContext;
 import info.bytecraft.database.IZoneDAO;
 import info.bytecraft.zones.Lot;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.zones.Zone.Permission;
 
 import java.util.List;
 
 import static org.bukkit.ChatColor.*;
 
 import org.bukkit.block.Block;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 
 public class LotCommand extends AbstractCommand
 {
     
     public LotCommand(Bytecraft plugin)
     {
         super(plugin, "lot");
     }
 
     @Override
     public boolean handlePlayer(BytecraftPlayer player, String[] args)
     {
         if (args.length == 0) {
             player.sendMessage(ChatColor.RED +
                     "Incorrect usage! Try:");
             player.sendMessage(ChatColor.AQUA +
                     "/lot create <lot name> <player>");
             player.sendMessage(ChatColor.AQUA +
                     "/lot addowner <lot name> <player>");
             player.sendMessage(ChatColor.AQUA +
                     "/lot delowner <lot name> <owner>");
             player.sendMessage(ChatColor.AQUA +
                     "/lot delete <lot name>");
             return true;
         }
 
         if ("create".equals(args[0])) {
             createLot(player, args);
             return true;
         }
         else if ("addowner".equals(args[0])) {
             setLotOwner(player, args);
             return true;
         }
         else if ("delowner".equals(args[0])) {
             setLotOwner(player, args);
             return true;
         }
         else if ("delete".equals(args[0])) {
             deleteLot(player, args);
             return true;
         }
         return false;
     }
 
     public void createLot(BytecraftPlayer player, String[] args)
     {
         if (args.length < 3) {
             player.sendMessage("syntax: /lot create [name] [owner]");
             return;
         }
 
         Block tb1 = player.getLotBlock1();
 
         Zone tzone = plugin.getZoneAt(tb1.getWorld(), new Point(tb1.getX(), tb1.getZ()));
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
             String name = args[1] + "." + tzone.getName();
 
             if (dao.lotExists(tzone.getName(), name)) {
                 player.sendMessage(RED + "A lot named " + name
                        + " already exists.");
                 return;
             }
 
             String playerName = args[2];
 
             BytecraftPlayer victim = plugin.getPlayerOffline(playerName);
             if (victim == null) {
                 player.sendMessage(RED + "Player " + playerName
                         + " was not found.");
                 return;
             }
 
             Block b1 = player.getLotBlock1();
             Block b2 = player.getLotBlock2();
             if (b1 == null || b2 == null) {
                 player.sendMessage("Please select two corners");
                 return;
             }
 
             Zone zone = plugin.getZoneAt(b1.getWorld(), new Point(b1.getX(), b1.getZ()));
             
             Permission perm = zone.getUser(player);
             if (perm != Permission.OWNER && !player.getRank().canEditZones()) {
                 player.sendMessage(RED
                         + "You are not allowed to create lots in zone "
                         + zone.getName() + " (" + perm + ").");
                 return;
             }
 
             Zone checkZone = plugin.getZoneAt(b2.getWorld(), new Point(b2.getX(), b2.getZ()));
 
             // identity check. both lookups should return exactly the same
             // object
             if (zone.getId() != checkZone.getId()) {
                 return;
             }
 
             Rectangle rect =
                     new Rectangle(b1.getX(), b1.getZ(), b2.getX(), b2.getZ());
 
             Lot lot = new Lot();
             lot.setZoneName(zone.getName());
             lot.setRect(rect);
             lot.setName(args[1] + "." + zone.getName());
             lot.addOwner(victim);
 
             for(Lot other: zone.getLots().values()){
                 if(other.intersects(lot)){
                     player.sendMessage(ChatColor.RED + "Lot intersects with lot: " + other.getName());
                     return;
                 }
             }
             
             zone.addLot(lot);
             dao.addLot(lot);
             dao.addLotUser(lot.getId(), victim.getName());
 
             player.sendMessage(GREEN + "[" + zone.getName() + "] Lot "
                     + args[1] + "." + zone.getName() + " created for player "
                     + playerName + ".");
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void setLotOwner(BytecraftPlayer player, String[] args)
     {
         if (args.length < 3) {
             player.sendMessage("syntax: /lot addowner [name] [player]");
             return;
         }
         
         Location loc = player.getLocation();
         
         String name = args[1];
         Zone zone = plugin.getZoneAt(loc.getWorld(), new Point(loc.getBlockX(), loc.getBlockZ()));
         Lot lot = zone.getLot(name);
         if (lot == null) {
             player.sendMessage(RED + "No lot named " + name + " found.");
             return;
         }
 
         Permission perm = zone.getUser(player);
         if (perm == Zone.Permission.OWNER) {
             // Zone owners can do this in communist zones
         }
         else if (lot.isOwner(player)) {
             // Lot owners can always do it
         }
         else if (player.getRank().canEditZones()) {
             // Admins etc.
         }
         else {
             player.sendMessage(RED
                     + "You are not an owner of lot " + lot.getName() + ".");
             return;
         }
 
         // try partial matching
         List<BytecraftPlayer> candidates = plugin.matchPlayer(args[2]);
         BytecraftPlayer candidate = null;
         if (candidates.size() != 1) {
             // try exact matching
             candidate = plugin.getPlayerOffline(args[2]);
             if (candidate == null) {
                 // give up
                 player.sendMessage(RED + "Player " + args[2]
                         + " was not found.");
                 return;
             }
         } else {
             candidate = candidates.get(0);
         }
 
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
 
             if ("addowner".equals(args[0])) {
 
                 if (lot.isOwner(candidate)) {
                     player.sendMessage(RED + candidate.getDisplayName() + RED +
                             " is already an owner of lot " + name + ".");
                     return;
                 }
                 else {
                     lot.addOwner(candidate);
                     dao.addLotUser(lot.getId(), candidate.getName());
                     player.sendMessage(GREEN + candidate.getDisplayName() + GREEN +
                             " has been added as owner of " + lot.getName() + ".");
                 }
             }
             else if ("delowner".equals(args[0])) {
                 if (!lot.isOwner(candidate)) {
                     player.sendMessage(RED + candidate.getDisplayName() + RED +
                             " is not an owner of lot " + name + ".");
                     return;
                 }
                 else {
                     lot.deleteOwner(candidate);
                     dao.deleteLotUser(lot.getId(), candidate.getName());
 
                     player.sendMessage(GREEN + candidate.getDisplayName() + GREEN +
                             " is no longer an owner of " + lot.getName() + ".");
                 }
             }
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void deleteLot(BytecraftPlayer player, String[] args)
     {
         if (args.length < 2) {
             player.sendMessage("syntax: /lot delete [name]");
             return;
         }
         Location loc = player.getLocation();
         Zone zone = plugin.getZoneAt(loc.getWorld(), new Point(loc.getBlockX(), loc.getBlockZ()));
        if(zone == null){
            player.sendMessage(ChatColor.RED + "You are not currently in a zone!");
            return;
        }
        String name = zone.getName() + "." + args[1];
 
         Lot lot = zone.getLot(name);
         if (lot == null) {
             player.sendMessage(RED + "No lot named " + name + " found.");
             return;
         }
 
         Zone.Permission perm = zone.getUser(player);
         if (perm == Permission.OWNER) {
             // Zone owners can do this in communist zones
         }
         else if (lot.isOwner(player)) {
             // Lot owners can always do it
         }
         else if (player.getRank().canEditZones()) {
             // Admins etc.
         }
         else {
             player.sendMessage(RED
                     + "You are not an owner of lot " + lot.getName() + ".");
             return;
         }
 
         try (IContext ctx = plugin.createContext()) {
             IZoneDAO dao = ctx.getZoneDAO();
             zone.deleteLot(lot);
             dao.deleteLot(lot.getId());
             dao.deleteLotUsers(lot.getId());
             player.sendMessage(GREEN + lot.getName() + " has been deleted.");
         } catch (DAOException e) {
             throw new RuntimeException(e);
         }
     }
 }
