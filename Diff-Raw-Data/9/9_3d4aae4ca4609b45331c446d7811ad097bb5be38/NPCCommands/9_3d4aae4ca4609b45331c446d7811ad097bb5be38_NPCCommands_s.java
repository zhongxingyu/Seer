 package net.citizensnpcs.command.command;
 
 import net.citizensnpcs.Citizens;
 import net.citizensnpcs.Settings.Setting;
 import net.citizensnpcs.Template;
 import net.citizensnpcs.api.npc.NPC;
 import net.citizensnpcs.api.npc.trait.Character;
 import net.citizensnpcs.api.npc.trait.DefaultInstanceFactory;
 import net.citizensnpcs.api.npc.trait.trait.MobType;
 import net.citizensnpcs.api.npc.trait.trait.Owner;
 import net.citizensnpcs.api.npc.trait.trait.Spawned;
 import net.citizensnpcs.command.CommandContext;
 import net.citizensnpcs.command.annotation.Command;
 import net.citizensnpcs.command.annotation.Requirements;
 import net.citizensnpcs.npc.CitizensNPCManager;
 import net.citizensnpcs.trait.LookClose;
 import net.citizensnpcs.util.Messaging;
 import net.citizensnpcs.util.StringHelper;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 
 @Requirements(selected = true, ownership = true)
 public class NPCCommands {
     private final Citizens plugin;
     private final CitizensNPCManager npcManager;
     private final DefaultInstanceFactory<Character> characterManager;
 
     public NPCCommands(Citizens plugin) {
         this.plugin = plugin;
         npcManager = plugin.getNPCManager();
         characterManager = plugin.getCharacterManager();
     }
 
     @Command(
              aliases = { "npc" },
              usage = "create [name] --type (type) --char (character) --temp (template)",
              desc = "Create a new NPC",
              modifiers = { "create" },
              min = 2,
              max = 5,
              permission = "npc.create")
     @Requirements
     public void createNPC(CommandContext args, Player player, NPC npc) {
         String name = args.getString(1);
         if (name.length() > 16) {
             Messaging.sendError(player, "NPC names cannot be longer than 16 characters. The name has been shortened.");
             name = name.substring(0, 15);
         }
         CreatureType type = CreatureType.MONSTER; // Default NPC type
         if (args.hasValueFlag("type"))
             try {
                 type = CreatureType.valueOf(args.getFlag("type").toUpperCase().replace('-', '_'));
             } catch (IllegalArgumentException ex) {
                 Messaging.sendError(player, "'" + args.getFlag("type")
                         + "' is not a valid mob type. Using default NPC.");
             }
         NPC create = npcManager.createNPC(type, name);
         String successMsg = ChatColor.GREEN + "You created " + StringHelper.wrap(create.getName());
         boolean success = true;
         if (args.hasValueFlag("char")) {
             if (characterManager.getInstance(args.getFlag("char"), create) == null) {
                 Messaging.sendError(player,
                         "The character '" + args.getFlag("char") + "' does not exist. " + create.getName()
                                 + " was created at your location without a character.");
                 success = false;
             } else {
                 create.setCharacter(characterManager.getInstance(args.getFlag("char"), create));
                 successMsg += " with the character " + StringHelper.wrap(args.getFlag("char"));
             }
         }
         if (args.hasValueFlag("temp")) {
             String template = args.getFlag("temp");
             if (!plugin.getTemplates().getKey("templates").keyExists(template)) {
                 Messaging.sendError(player, "The template '" + template
                         + "' does not exist. Did you type the name incorrectly?");
                 return;
             }
             new Template(plugin.getTemplates().getKey("templates." + template)).apply(plugin.getStorage().getKey(
                     "npc." + npc.getId()));
         }
         successMsg += " at your location.";
 
         // Set the owner
         create.addTrait(new Owner(player.getName()));
 
         // Set the mob type
         create.addTrait(new MobType(type == CreatureType.MONSTER ? "DEFAULT" : type.toString()));
 
         create.spawn(player.getLocation());
         npcManager.selectNPC(player, create);
         if (success)
             Messaging.send(player, successMsg);
     }
 
     @Command(
              aliases = { "npc" },
              usage = "despawn",
              desc = "Despawn an NPC",
              modifiers = { "despawn" },
              min = 1,
              max = 1,
              permission = "npc.despawn")
     public void despawnNPC(CommandContext args, Player player, NPC npc) {
         npc.getTrait(Spawned.class).setSpawned(false);
         npc.despawn();
         Messaging.send(player, ChatColor.GREEN + "You despawned " + StringHelper.wrap(npc.getName()) + ".");
     }
 
     @Command(
              aliases = { "npc" },
              usage = "rename [name]",
              desc = "Rename an NPC",
              modifiers = { "rename" },
              min = 2,
              max = 2,
              permission = "npc.rename")
     public void renameNPC(CommandContext args, Player player, NPC npc) {
         String oldName = npc.getName();
         npc.setName(args.getString(1));
        // Must reselect NPC after it is despawned
        npcManager.selectNPC(player, npc);
         Messaging.send(
                 player,
                 ChatColor.GREEN + "You renamed " + StringHelper.wrap(oldName) + " to "
                         + StringHelper.wrap(args.getString(1)) + ".");
     }
 
     @Command(
              aliases = { "npc" },
              usage = "select [id]",
              desc = "Selects an NPC with the given ID",
              modifiers = { "select" },
              min = 2,
              max = 2,
              permission = "npc.select")
     @Requirements(ownership = true)
     public void selectNPC(CommandContext args, Player player, NPC npc) {
         NPC toSelect = npcManager.getNPC(args.getInteger(1));
         if (toSelect == null || !toSelect.getTrait(Spawned.class).shouldSpawn()) {
             Messaging.sendError(player, "No NPC with the ID '" + args.getInteger(1) + "' is spawned.");
             return;
         }
         if (npc != null && toSelect.getId() == npc.getId()) {
             Messaging.sendError(player, "You already have that NPC selected.");
             return;
         }
         npcManager.selectNPC(player, toSelect);
         Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), toSelect);
     }
 
     @Command(
              aliases = { "npc" },
              usage = "spawn [id]",
              desc = "Spawn an existing NPC",
              modifiers = { "spawn" },
              min = 2,
              max = 2,
              permission = "npc.spawn")
     @Requirements
     public void spawnNPC(CommandContext args, Player player, NPC npc) {
         NPC respawn = npcManager.getNPC(args.getInteger(1));
         if (respawn == null) {
             Messaging.sendError(player, "No NPC with the ID '" + args.getInteger(1) + "' exists.");
             return;
         }
 
         if (!respawn.getTrait(Owner.class).getOwner().equals(player.getName())) {
             Messaging.sendError(player, "You must be the owner of this NPC to execute that command.");
             return;
         }
 
         if (respawn.spawn(player.getLocation())) {
             npcManager.selectNPC(player, respawn);
             Messaging.send(player, ChatColor.GREEN + "You respawned " + StringHelper.wrap(respawn.getName())
                     + " at your location.");
         } else {
             Messaging.sendError(player, respawn.getName() + " is already spawned at another location."
                     + " Use '/npc tphere' to teleport the NPC to your location.");
         }
     }
 
     @Command(
              aliases = { "npc" },
              usage = "tphere",
              desc = "Teleport an NPC to your location",
              modifiers = { "tphere" },
              min = 1,
              max = 1,
              permission = "npc.tphere")
     public void teleportNPCToPlayer(CommandContext args, Player player, NPC npc) {
         npc.getBukkitEntity().teleport(player, TeleportCause.COMMAND);
         Messaging.send(player, StringHelper.wrap(npc.getName()) + " was teleported to your location.");
     }
 
     @Command(
              aliases = { "npc" },
              usage = "tp",
              desc = "Teleport to an NPC",
              modifiers = { "tp", "teleport" },
              min = 1,
              max = 1,
              permission = "npc.tp")
     public void teleportToNPC(CommandContext args, Player player, NPC npc) {
         player.teleport(npc.getBukkitEntity(), TeleportCause.COMMAND);
         Messaging.send(player, ChatColor.GREEN + "You teleported to " + StringHelper.wrap(npc.getName()) + ".");
     }
 
     @Command(
             aliases = { "npc" },
             usage = "lookclose",
             desc = "Toggle an NPC's look-close state",
             modifiers = { "lookclose", "look", "rotate" },
             min = 1,
             max = 1,
             permission = "npc.look-close")
     public void toggleNPCLookClose(CommandContext args, Player player, NPC npc) {
         LookClose trait = npc.getTrait(LookClose.class);
         trait.toggle();
         String msg = StringHelper.wrap(npc.getName()) + " will "
                 + (trait.shouldLookClose() ? "now rotate" : "no longer rotate");
         Messaging.send(player, msg += " when a player is nearby.");
     }
 }
