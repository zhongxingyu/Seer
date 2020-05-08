 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.bukkit.persistent;
 
 import at.co.hohl.mcutils.chat.Chat;
 import at.co.hohl.myresidence.*;
 import at.co.hohl.myresidence.exceptions.MyResidenceException;
 import at.co.hohl.myresidence.storage.persistent.*;
 import com.avaje.ebean.EbeanServer;
 import com.sk89q.util.StringUtil;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * MyResidence Nation implementation for Bukkit.
  *
  * @author Michael Hohl
  */
 public class PersistNation implements Nation {
   // Plugin which holds this nation.
   protected final MyResidence plugin;
 
   // ChunkManager used by this nation.
   private ChunkManager chunkManager;
 
   // PermissionsResolver of this nation.
   private PermissionsResolver permissionsResolver;
 
   /**
    * Creates a new Nation for the passed plugin.
    *
    * @param plugin the plugin to create the nation.
    */
   public PersistNation(MyResidence plugin) {
     this.plugin = plugin;
   }
 
   /**
    * Sends the passed player information about the passed object.
    *
    * @param object object to retrieve information. Could be a Residence or a Town.
    */
   public void sendInformation(Player player, Object object) throws MyResidenceException {
     if (object instanceof Residence) {
       Residence residence = (Residence) object;
       ResidenceManager manager = getResidenceManager(residence);
 
       player.sendMessage(ChatColor.LIGHT_PURPLE + "= = = ABOUT RESIDENCE = = =");
 
       // Send name
       player.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + residence.getName());
 
       // Retrieve and send owner...
       String owner = "NOBODY";
       if (residence.getOwnerId() != -1) {
         owner = getInhabitant(residence.getOwnerId()).toString();
       }
       player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + owner);
 
       // Retrieve and send town...
       String town = "ANY (wildness)";
       if (residence.getTownId() != -1) {
         Town townData = getTown(residence.getTownId());
         town = townData.getName() +
                 " (Major: " + StringUtil.joinString(getTownManager(townData).getMajors(), ", ", 0) + ")";
       }
       player.sendMessage(ChatColor.GRAY + "Town: " + ChatColor.WHITE + town);
 
       // Retrieve and send area...
       Selection area = manager.getArea();
       Chat.sendMessage(player, "&7Size: &f{0}x{1}x{2}", area.getLength(), area.getWidth(), area.getHeight());
 
       // Retrieve flags
       List<ResidenceFlag.Type> flags = manager.getFlags();
       if (flags.size() > 0) {
         player.sendMessage(
                 ChatColor.GRAY + "Flags: " + ChatColor.WHITE + StringUtil.joinString(flags, ", ", 0));
       }
 
       // Retrieve members
       List<Inhabitant> members = manager.getMembers();
       if (members.size() > 0) {
         player.sendMessage(
                 ChatColor.GRAY + "Members: " + ChatColor.WHITE + StringUtil.joinString(members, ", ", 0));
       }
 
       // Retrieve likes
       List<Inhabitant> likes = manager.getLikes();
       if (likes.size() > 0) {
         player.sendMessage(
                 ChatColor.GRAY + "Likes Received: " + ChatColor.WHITE + StringUtil.joinString(likes, ", ", 0));
       }
 
       // Retrieve and send money values.
       player.sendMessage(ChatColor.GRAY + "Value: " + ChatColor.WHITE + plugin.format(residence.getValue()));
       if (residence.isForSale()) {
         player.sendMessage(ChatColor.YELLOW + "RESIDENCE FOR SALE!");
         player.sendMessage(ChatColor.YELLOW + "Price: " + plugin.format(residence.getPrice()));
       }
     } else if (object instanceof Town) {
       Town town = (Town) object;
       TownManager manager = getTownManager(town);
 
       player.sendMessage(ChatColor.LIGHT_PURPLE + "= = = ABOUT TOWN = = =");
 
       // Send name
       player.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + town.getName());
 
       // Retrieve and send major
       player.sendMessage(ChatColor.GRAY + "Major: " +
               ChatColor.WHITE + StringUtil.joinString(manager.getMajors(), " ,", 0));
 
       // Retrieve residences
       List<Residence> residences = manager.getResidences();
       player.sendMessage(ChatColor.GRAY + "Residences: " + ChatColor.WHITE + residences.size());
 
       // Retrieve value
       double value = 0;
       for (Residence residence : residences) {
         value += residence.getValue();
       }
       player.sendMessage(ChatColor.GRAY + "Value: " + ChatColor.WHITE + plugin.format(value));
 
       // Retrieve and send money values.
       player.sendMessage(ChatColor.GRAY + "Money: " + ChatColor.WHITE + plugin.format(town.getMoney()));
 
       // Retrieve flags
       List<TownFlag.Type> flags = manager.getFlags();
       if (flags.size() > 0) {
         player.sendMessage(
                 ChatColor.GRAY + "Flags: " + ChatColor.WHITE + StringUtil.joinString(flags, ", ", 0));
       }
 
       // Retrieve members
       List<String> rules = getRuleManager(town).getRules();
       if (rules.size() > 0) {
         player.sendMessage(ChatColor.GRAY + "Rules:");
         for (String line : rules) {
           player.sendMessage(" " + line);
         }
       }
     } else {
       throw new MyResidenceException("Can't retrieve information about that object!");
     }
   }
 
   public void searchInvalidResidences(final InvalidResidenceListener invalidResidenceListener) {
     final List<List<ResidenceSign>> residenceSignPackages = new LinkedList<List<ResidenceSign>>();
     final List<Residence> invalidResidences = new LinkedList<Residence>();
 
     List<ResidenceSign> residenceSigns = getDatabase().find(ResidenceSign.class).findList();
     while (!residenceSigns.isEmpty()) {
       List<ResidenceSign> residenceSignPackage = residenceSigns.subList(0, Math.min(residenceSigns.size(), 5));
       residenceSignPackages.add(residenceSignPackage);
       residenceSigns.removeAll(residenceSignPackage);
     }
 
     plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
       public void run() {
         List<ResidenceSign> residenceSignsToCheck = residenceSignPackages.get(0);
         residenceSignPackages.remove(residenceSignsToCheck);
 
         // Check residences and save invalid ones
         for (ResidenceSign residenceSign : residenceSignsToCheck) {
           Block residenceSignBlock = plugin.getServer().getWorld(residenceSign.getWorld())
                   .getBlockAt(residenceSign.getX(), residenceSign.getY(), residenceSign.getZ());
          if (!(residenceSignBlock.getType().equals(Material.SIGN_POST) ||
                  residenceSignBlock.getType().equals(Material.WALL_SIGN))) {
             invalidResidences.add(getResidence(residenceSign.getResidenceId()));
           }
         }
 
         // Inform users or search other residences if there are others
         if (!residenceSignPackages.isEmpty()) {
           plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this);
         } else {
           invalidResidenceListener.invalidResidencesFound(invalidResidences);
         }
       }
     });
   }
 
   /**
    * Returns the residence at the passed location
    *
    * @param location the location to look for.
    * @return the founded residence or null.
    */
   public Residence getResidence(Location location) {
     ResidenceArea residenceArea = getDatabase().find(ResidenceArea.class).where()
             .ieq("world", location.getWorld().getName())
             .le("lowX", location.getBlockX())
             .le("lowY", location.getBlockY())
             .le("lowZ", location.getBlockZ())
             .ge("highX", location.getBlockX())
             .ge("highY", location.getBlockY())
             .ge("highZ", location.getBlockZ())
             .findUnique();
 
     if (residenceArea != null) {
       return getResidence(residenceArea.getResidenceId());
     } else {
       return null;
     }
   }
 
   /**
    * Returns the residence with the passed id.
    *
    * @param id the id of the residence to look for.
    * @return the founded residence or null.
    */
   public Residence getResidence(int id) {
     return getDatabase().find(Residence.class).where().idEq(id).findUnique();
   }
 
   /**
    * Returns the residence with the passed name.
    *
    * @param name the name to look for.
    * @return the founded residence or null.
    */
   public Residence getResidence(String name) {
     return getDatabase().find(Residence.class).where().ieq("name", name).findUnique();
   }
 
   /**
    * Returns the residence by the passed sign.
    *
    * @param sign the sign to look for.
    * @return the founded residence or null.
    */
   public Residence getResidence(Sign sign) {
     Location blockLocation = sign.getBlock().getLocation();
 
     Map<String, Object> locationArgs = new HashMap<String, Object>();
     locationArgs.put("x", blockLocation.getBlockX());
     locationArgs.put("y", blockLocation.getBlockY());
     locationArgs.put("z", blockLocation.getBlockZ());
     locationArgs.put("world", blockLocation.getWorld().getName());
 
     ResidenceSign residenceSign = getDatabase().find(ResidenceSign.class).where().allEq(locationArgs).findUnique();
 
     if (residenceSign != null) {
       return getDatabase().find(Residence.class).where().idEq(residenceSign.getResidenceId()).findUnique();
     } else {
       return null;
     }
   }
 
   /**
    * Find a residence by name owned by the passed residence.
    *
    * @param inhabitant the inhabitant which owns the residence to look for.
    * @param search     a part of the name to search.
    * @return the residence found.
    */
   public List<Residence> findResidences(Inhabitant inhabitant, String search) {
     return getDatabase().find(Residence.class).where()
             .eq("ownerId", inhabitant.getId())
             .like("name", "%" + search + "%")
             .findList();
   }
 
   /**
    * Find a residence by name.
    *
    * @param search a part of the name to search.
    * @return the residence found.
    */
   public List<Residence> findResidences(String search) {
     return getDatabase().find(Residence.class)
             .where().like("name", "%" + search + "%")
             .findList();
   }
 
   /**
    * Finds all residences owned by the passed inhabitant.
    *
    * @param inhabitant the inhabitant to look for.
    * @return list of the found residences.
    */
   public List<Residence> findResidences(Inhabitant inhabitant) {
     return getDatabase().find(Residence.class).where()
             .eq("ownerId", inhabitant.getId())
             .findList();
   }
 
   /**
    * Returns all residences, which are at the passed location or inside the overflow.
    *
    * @param location the location to retreive.
    * @param overlay  integer which defines what's the maximum overlay.
    * @return list of found residences.
    */
   public List<Residence> findResidencesNearTo(Location location, int overlay) {
     List<ResidenceArea> residenceAreas = getDatabase().find(ResidenceArea.class).where()
             .ieq("world", location.getWorld().getName())
             .le("lowX", location.getBlockX() + overlay)
             .le("lowY", location.getBlockY())
             .le("lowZ", location.getBlockZ() + overlay)
             .ge("highX", location.getBlockX() - overlay)
             .ge("highY", location.getBlockY())
             .ge("highZ", location.getBlockZ() - overlay)
             .findList();
 
     List<Residence> residences = new LinkedList<Residence>();
     for (ResidenceArea residenceArea : residenceAreas) {
       Residence residence = getResidence(residenceArea.getResidenceId());
       if (residence != null) {
         residences.add(residence);
       } else {
         plugin.warning("ResidenceArea without a Residence found! Remove ResidenceArea %d...", residenceArea.getId());
         getDatabase().delete(residenceArea);
       }
     }
 
     return residences;
   }
 
   /**
    * Returns the town with the passed id.
    *
    * @param id the id of the town to look for.
    * @return the founded town or null.
    */
   public Town getTown(int id) {
     return getDatabase().find(Town.class).where().idEq(id).findUnique();
   }
 
   /**
    * Returns the town with the passed name.
    *
    * @param name the name to look for.
    * @return the founded town or null.
    */
   public Town getTown(String name) {
     return getDatabase().find(Town.class).where().ieq("name", name).findUnique();
   }
 
   /**
    * Returns the town at the passed location.
    *
    * @param location the location to look for.
    * @return the founded town or null.
    */
   public Town getTown(Location location) {
     Map<String, Object> chunkArgs = new HashMap<String, Object>();
     chunkArgs.put("x", location.getBlock().getChunk().getX());
     chunkArgs.put("z", location.getBlock().getChunk().getZ());
     chunkArgs.put("world", location.getWorld().getName());
 
     TownChunk currentChunk = getDatabase().find(TownChunk.class).where().allEq(chunkArgs).findUnique();
 
     if (currentChunk != null) {
       return getDatabase().find(Town.class).where().idEq(currentChunk.getTownId()).findUnique();
     } else {
       return null;
     }
   }
 
   /**
    * Finds a town for the passed name.
    *
    * @param name the name of the town to find. Could alos be only a part of the name.
    * @return founded towns.
    */
   public List<Town> findTown(String name) {
     return getDatabase().find(Town.class).where().like("name", name + "%").findList();
   }
 
   /**
    * Removes a town.
    *
    * @param town town to remove.
    */
   public void remove(Town town) throws MyResidenceException {
     if (getDatabase().find(Residence.class).where().eq("townId", town.getId()).findRowCount() > 0) {
       throw new MyResidenceException("You can not remove cities with inhabitants and residences!");
     }
 
     final List<TownChunk> townChunks = getDatabase().find(TownChunk.class)
             .where()
             .eq("townId", town.getId())
             .findList();
 
     final List<Major> townMajors = getDatabase().find(Major.class)
             .where()
             .eq("townId", town.getId())
             .findList();
 
     final List<TownFlag> townFlags = getDatabase().find(TownFlag.class)
             .where()
             .eq("townId", town.getId())
             .findList();
 
     final List<TownRule> townRules = getDatabase().find(TownRule.class)
             .where()
             .eq("townId", town.getId())
             .findList();
 
     getDatabase().delete(townChunks);
     getDatabase().delete(townMajors);
     getDatabase().delete(townFlags);
     getDatabase().delete(townRules);
   }
 
   /**
    * Removes a residence.
    *
    * @param residence residence to remove.
    */
   public void remove(Residence residence) {
     if (residence == null) {
       throw new NullPointerException("null is not a residence!");
     }
 
     final ResidenceArea residenceArea = getDatabase().find(ResidenceArea.class)
             .where()
             .eq("residenceId", residence.getId())
             .findUnique();
 
     final ResidenceSign residenceSign = getDatabase().find(ResidenceSign.class)
             .where()
             .eq("residenceId", residence.getId())
             .findUnique();
 
     final List<HomePoint> residenceHomes = getDatabase().find(HomePoint.class)
             .where()
             .eq("residenceId", residence.getId())
             .findList();
 
     final List<ResidenceFlag> residenceFlags = getDatabase().find(ResidenceFlag.class)
             .where()
             .eq("residenceId", residence.getId())
             .findList();
 
     final List<ResidenceMember> residenceMembers = getDatabase().find(ResidenceMember.class)
             .where()
             .eq("residenceId", residence.getId())
             .findList();
 
     final List<Like> residenceLikes = getDatabase().find(Like.class)
             .where()
             .eq("residenceId", residence.getId())
             .findList();
 
     getDatabase().delete(residence);
     if (residenceArea != null) {
       getDatabase().delete(residenceArea);
     } else {
       plugin.warning("Deleted residence which does not have an area!");
     }
     getDatabase().delete(residenceSign);
     getDatabase().delete(residenceHomes);
     getDatabase().delete(residenceMembers);
     getDatabase().delete(residenceFlags);
     getDatabase().delete(residenceLikes);
   }
 
   /**
    * Returns the player data for the passed name.
    *
    * @param name the name to look for.
    * @return the founded player or null.
    */
   public Inhabitant getInhabitant(String name) {
     Inhabitant player = getDatabase().find(Inhabitant.class).where().ilike("name", "%" + name + "%").findUnique();
 
     if (player == null) {
       player = new Inhabitant();
       player.setName(name);
       getDatabase().save(player);
 
       plugin.info("Created database entry for player %s.", name);
     }
 
     return player;
   }
 
   /**
    * Returns the player with the passed id.
    *
    * @param id the id of the player to look for.
    * @return the founded player or null.
    */
   public Inhabitant getInhabitant(int id) {
     return getDatabase().find(Inhabitant.class).where().idEq(id).findUnique();
   }
 
   /**
    * Returns a manager for the rules of the town.
    *
    * @param town the town to manage.
    * @return the rule manager for the town.
    */
   public RuleManager getRuleManager(Town town) {
     return new PersistRuleManager(this, town);
   }
 
   /**
    * Returns a manager for the residence.
    *
    * @param residence the residence.
    * @return the manager for the residence.
    */
   public ResidenceManager getResidenceManager(Residence residence) {
     return new PersistResidenceManager(plugin, this, residence);
   }
 
   /**
    * Returns a manager for the town.
    *
    * @param town the manager for the town.
    * @return the town.
    */
   public TownManager getTownManager(Town town) {
     return new PersistTownManager(this, town);
   }
 
   /**
    * Returns a manager for the flags of the residence.
    *
    * @param residence the residence to manage.
    * @return the manager for the flags.
    */
   public FlagManager<ResidenceFlag.Type> getFlagManager(Residence residence) {
     return new PersistResidenceFlagManager(this, residence);
   }
 
   /**
    * Returns a manager for the flags of the town.
    *
    * @param town the town to manage.
    * @return the manager for the flags.
    */
   public FlagManager<TownFlag.Type> getFlagManager(Town town) {
     return new PersistTownFlagManager(this, town);
   }
 
   /**
    * @return manager for the chunks.
    */
   public ChunkManager getChunkManager() {
     if (chunkManager == null) {
       chunkManager = new PersistChunkManager(this);
     }
 
     return chunkManager;
   }
 
   /**
    * @return the PermissionsResolver used by this nation.
    */
   public PermissionsResolver getPermissionsResolver() {
     if (permissionsResolver == null) {
       permissionsResolver = new PersistPermissionsResolver(plugin, this);
     }
 
     return permissionsResolver;
   }
 
   /**
    * Saves any changes to towns or residences.
    *
    * @param object the object of the town or residence to save.
    */
   public void save(Object object) {
     plugin.getDatabase().save(object);
   }
 
   /**
    * @return the database which holds all information about towns and residences.
    */
   public EbeanServer getDatabase() {
     return plugin.getDatabase();
   }
 }
