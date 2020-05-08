 package com.dsh105.sparktrail.command;
 
 import com.dsh105.dshutils.pagination.Paginator;
 import com.dsh105.dshutils.util.EnumUtil;
 import com.dsh105.dshutils.util.StringUtil;
 import com.dsh105.sparktrail.SparkTrailPlugin;
 import com.dsh105.sparktrail.chat.BlockData;
 import com.dsh105.sparktrail.data.DataFactory;
 import com.dsh105.sparktrail.data.EffectCreator;
 import com.dsh105.sparktrail.data.EffectManager;
 import com.dsh105.sparktrail.listeners.InteractDetails;
 import com.dsh105.sparktrail.listeners.InteractListener;
 import com.dsh105.sparktrail.menu.ParticleMenu;
 import com.dsh105.sparktrail.trail.*;
 import com.dsh105.sparktrail.trail.type.Critical;
 import com.dsh105.sparktrail.trail.type.Potion;
 import com.dsh105.sparktrail.trail.type.Smoke;
 import com.dsh105.sparktrail.trail.type.Swirl;
 import com.dsh105.sparktrail.util.Lang;
 import com.dsh105.sparktrail.util.Permission;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.FireworkEffect;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class TrailCommand implements CommandExecutor {
 
     public String label;
     ChatColor c1 = SparkTrailPlugin.getInstance().primaryColour;
     ChatColor c2 = SparkTrailPlugin.getInstance().secondaryColour;
     private Paginator help;
 
     public TrailCommand(String name) {
         this.label = name;
         this.help = this.generateHelp();
     }
 
     private Paginator generateHelp() {
         ArrayList<String> list = new ArrayList<String>();
         for (HelpEntry he : HelpEntry.values()) {
             list.add(he.getLine());
         }
         return new Paginator(list, 5);
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
 
         if (args.length == 0) {
             if (Permission.TRAIL.hasPerm(sender, true, false)) {
                 Player p = (Player) sender;
                 ParticleMenu pm = new ParticleMenu(p, p.getName());
                 if (pm.fail) {
                     Lang.sendTo(sender, Lang.MENU_ERROR.toString());
                     return true;
                 }
                 pm.open(true);
                 return true;
             } else return true;
         } else if (args.length == 1 && args[0].equalsIgnoreCase("location")) {
             if (Permission.LOC_TRAIL.hasPerm(sender, true, false)) {
                 Player p = (Player) sender;
                 InteractListener.INTERACTION.put(p.getName(), new InteractDetails(InteractDetails.InteractType.BLOCK, InteractDetails.ModifyType.ADD));
                 Lang.sendTo(sender, Lang.INTERACT_BLOCK.toString());
                 return true;
             } else return true;
         } else if (args.length == 1 && args[0].equalsIgnoreCase("mob")) {
             if (Permission.MOB_TRAIL.hasPerm(sender, true, false)) {
                 Player p = (Player) sender;
                 InteractListener.INTERACTION.put(p.getName(), new InteractDetails(InteractDetails.InteractType.MOB, InteractDetails.ModifyType.ADD));
                 Lang.sendTo(sender, Lang.INTERACT_MOB.toString());
                 return true;
             } else return true;
        } else if (args.length == 1 || (args.length >= 2 && (args[0].equalsIgnoreCase("blockbreak") || args[0].equalsIgnoreCase("firework")))) {
             if (args[0].equalsIgnoreCase("help")) {
                 if (Permission.TRAIL.hasPerm(sender, true, true)) {
                     sender.sendMessage(c2 + "------------ SparkTrail Help 1/" + help.getIndex() + " ------------");
                     sender.sendMessage(c2 + "Parameters: <> = Required      [] = Optional");
                     for (String s : help.getPage(1)) {
                         sender.sendMessage(s);
                     }
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("random")) {
                 if (!(sender instanceof Player)) {
                     Lang.sendTo(sender, Lang.IN_GAME_ONLY.toString());
                     return true;
                 }
                 Player p = (Player) sender;
 
                 EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
                 if (eh == null) {
                     eh = EffectCreator.createPlayerHolder(p.getName());
                 }
 
                 ArrayList<ParticleType> list = new ArrayList<ParticleType>();
                 for (ParticleType pt : ParticleType.values()) {
                     if (!pt.requiresDataMenu()) {
                         if (Permission.hasEffectPerm(p, false, pt, EffectHolder.EffectType.PLAYER) && !eh.hasEffect(pt)) {
                             list.add(pt);
                         }
                     }
                 }
 
                 if (list.isEmpty()) {
                     Lang.sendTo(p, Lang.RANDOM_SELECT_FAILED.toString());
                     return true;
                 }
                 ParticleType pt = list.get(new Random().nextInt(list.size()));
                 if (Permission.hasEffectPerm(p, true, pt, EffectHolder.EffectType.PLAYER)) {
                     if (eh.addEffect(pt, true)) {
                         Lang.sendTo(p, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                     }
                     return true;
                 } else {
                     EffectManager.getInstance().clear(eh);
                     return true;
                 }
             } else if (args[0].equalsIgnoreCase("demo")) {
                 if (Permission.DEMO.hasPerm(sender, true, false)) {
                     Player p = (Player) sender;
                     if (ParticleDemo.ACTIVE.containsKey(p.getName())) {
                         ParticleDemo.ACTIVE.get(p.getName()).cancel();
                         Lang.sendTo(sender, Lang.DEMO_STOP.toString());
                         return true;
                     }
                     Lang.sendTo(sender, Lang.DEMO_BEGIN.toString());
                     new ParticleDemo((Player) sender);
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("info")) {
                 if (Permission.INFO.hasPerm(sender, true, false)) {
                     EffectHolder eh = EffectManager.getInstance().getEffect(((Player) sender).getName());
                     if (eh == null || eh.getEffects().isEmpty()) {
                         Lang.sendTo(sender, Lang.NO_ACTIVE_EFFECTS.toString());
                         return true;
                     }
                     sender.sendMessage(c2 + "------------ Trail Effects ------------");
                     for (Effect e : eh.getEffects()) {
                         if (e.getParticleType().requiresDataMenu()) {
                             sender.sendMessage(c1 + StringUtil.capitalise(e.getParticleType().toString()) + ": " + c2 + e.getParticleData());
                         } else {
                             sender.sendMessage(c1 + StringUtil.capitalise(e.getParticleType().toString()));
                         }
                     }
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("start")) {
                 if (Permission.START.hasPerm(sender, true, false)) {
                     EffectHolder current = EffectManager.getInstance().getEffect(sender.getName());
                     EffectHolder eh = EffectManager.getInstance().createFromFile(sender.getName());
                     if (eh == null || eh.getEffects().isEmpty()) {
                         Lang.sendTo(sender, Lang.NO_EFFECTS_TO_LOAD.toString());
                         return true;
                     }
                     if (current != null) {
                         EffectManager.getInstance().clearFromMemory(current);
                     }
                     Lang.sendTo(sender, Lang.EFFECTS_LOADED.toString());
                     return true;
                 }
             } else if (args[0].equalsIgnoreCase("stop")) {
                 if (Permission.STOP.hasPerm(sender, true, false)) {
                     EffectHolder eh = EffectManager.getInstance().getEffect(sender.getName());
                     if (eh == null) {
                         Lang.sendTo(sender, Lang.NO_ACTIVE_EFFECTS.toString());
                         return true;
                     }
                     EffectManager.getInstance().remove(eh);
                     Lang.sendTo(sender, Lang.EFFECTS_STOPPED.toString());
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("clear")) {
                 if (Permission.CLEAR.hasPerm(sender, true, false)) {
                     EffectHolder eh = EffectManager.getInstance().getEffect(sender.getName());
                     if (eh == null) {
                         Lang.sendTo(sender, Lang.NO_ACTIVE_EFFECTS.toString());
                         return true;
                     }
                     EffectManager.getInstance().clear(eh);
                     Lang.sendTo(sender, Lang.EFFECTS_CLEARED.toString());
                     return true;
                 } else return true;
             } else {
                 if (EnumUtil.isEnumType(ParticleType.class, args[0].toUpperCase())) {
                     ParticleType pt = ParticleType.valueOf(args[0].toUpperCase());
                     if (!(sender instanceof Player)) {
                         Lang.sendTo(sender, Lang.IN_GAME_ONLY.toString());
                         return true;
                     }
                     Player p = (Player) sender;
                     if (pt.requiresDataMenu()) {
                         if (pt == ParticleType.BLOCKBREAK || pt == ParticleType.ITEMSPRAY) {
                             if (Permission.hasEffectPerm(p, true, pt, EffectHolder.EffectType.PLAYER)) {
                                 if (args.length == 1) {
                                     Lang.sendTo(p, Lang.INVALID_EFFECT_ARGS.toString().replace("%effect%", pt == ParticleType.BLOCKBREAK ? "Block Break" : "ItemSpray").replace("%extra_info%", "Structure: &e<IdValue> <BlockMeta>"));
                                     return true;
                                 }
 
                                 BlockData bd = DataFactory.findBlockData(StringUtil.combineSplit(1, args, " "));
                                 ParticleDetails pd = new ParticleDetails(pt);
                                 pd.blockId = bd.id;
                                 pd.blockMeta = bd.data;
                                 EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
                                 if (eh == null) {
                                     eh = EffectCreator.createPlayerHolder(p.getName());
                                 }
                                 if (eh.hasEffect(pt)) {
                                     eh.removeEffect(pt);
                                     Lang.sendTo(p, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
                                 } else {
                                     if (eh.addEffect(pt, true)) {
                                         Lang.sendTo(p, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                                     }
                                 }
                             }
                             return true;
                         } else if (pt == ParticleType.FIREWORK) {
                             if (Permission.hasEffectPerm(p, true, pt, EffectHolder.EffectType.PLAYER)) {
                                 if (args.length == 1) {
                                     Lang.sendTo(p, Lang.INVALID_EFFECT_ARGS.toString().replace("%effect%", "Firework").replace("%extra_info%", "Separate each parameter with a space."));
                                     return true;
                                 }
 
                                 FireworkEffect fe = DataFactory.generateFireworkEffectFrom(StringUtil.combineSplit(1, args, " "));
                                 ParticleDetails pd = new ParticleDetails(pt);
                                 pd.fireworkEffect = fe;
                                 EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
                                 if (eh == null) {
                                     eh = EffectCreator.createPlayerHolder(p.getName());
                                 }
                                 if (eh.hasEffect(pt)) {
                                     eh.removeEffect(pt);
                                     Lang.sendTo(p, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
                                 } else {
                                     if (eh.addEffect(pt, true)) {
                                         Lang.sendTo(p, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                                     }
                                 }
                             }
                             return true;
                         } else if (pt == ParticleType.CRITICAL) {
                             Lang.sendTo(sender, Lang.CRITICAL_HELP.toString());
                             return true;
                         } else if (pt == ParticleType.POTION) {
                             Lang.sendTo(sender, Lang.POTION_HELP.toString());
                             return true;
                         } else if (pt == ParticleType.SMOKE) {
                             Lang.sendTo(sender, Lang.SMOKE_HELP.toString());
                             return true;
                         } else if (pt == ParticleType.SWIRL) {
                             Lang.sendTo(sender, Lang.SWIRL_HELP.toString());
                             return true;
                         }
                     } else if (Permission.hasEffectPerm(p, true, pt, EffectHolder.EffectType.PLAYER)) {
                         EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
                         if (eh == null) {
                             eh = EffectCreator.createPlayerHolder(p.getName());
                         }
                         if (eh.hasEffect(pt)) {
                             eh.removeEffect(pt);
                             Lang.sendTo(p, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
                         } else {
                             if (eh.addEffect(pt, true)) {
                                 Lang.sendTo(p, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                             }
                         }
                         return true;
                     } else return true;
                 }
             }
         } else if (args.length == 2) {
             if (args[0].equals("help")) {
                 if (Permission.TRAIL.hasPerm(sender, true, true)) {
                     if (StringUtil.isInt(args[1])) {
                         String[] str = help.getPage(Integer.parseInt(args[1]));
                         if (str == null) {
                             Lang.sendTo(sender, Lang.HELP_INDEX_TOO_BIG.toString().replace("%index%", args[1]));
                             return true;
                         }
                         sender.sendMessage(c2 + "------------ SparkTrail Help " + args[1] + "/" + help.getIndex() + " ------------");
                         sender.sendMessage(c2 + "Parameters: <> = Required      [] = Optional");
                         for (String s : str) {
                             sender.sendMessage(s);
                         }
                     }
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("timeout")) {
                 if (Permission.TIMEOUT.hasPerm(sender, true, false)) {
                     if (!StringUtil.isInt(args[1])) {
                         Lang.sendTo(sender, Lang.INT_ONLY_WITH_ARGS.toString().replace("%string%", args[1]).replace("%argNum%", "1"));
                         return true;
                     }
                     Player p = (Player) sender;
                     EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
                     if (eh == null || eh.getEffects().isEmpty()) {
                         Lang.sendTo(sender, Lang.NO_ACTIVE_EFFECTS.toString());
                         return true;
                     }
                     eh.setTimeout(Integer.parseInt(args[1]));
                     Lang.sendTo(sender, Lang.TIMEOUT_SET.toString().replace("%timeout%", args[1]));
                 }
             } else if (args[0].equalsIgnoreCase("sound")) {
                 if (Permission.SOUND.hasPerm(sender, true, false)) {
                     EffectHolder eh = EffectManager.getInstance().getEffect(sender.getName());
                     if (eh == null || eh.getEffects().isEmpty()) {
                         eh = EffectCreator.createPlayerHolder(sender.getName());
                     }
                     if (!EnumUtil.isEnumType(org.bukkit.Sound.class, args[1].toUpperCase())) {
                         Lang.sendTo(sender, Lang.NO_SOUND_IN_STRING.toString().replace("%string%", args[1]));
                         return true;
                     }
 
                     org.bukkit.Sound enumSound = org.bukkit.Sound.valueOf(args[1].toUpperCase());
                     ParticleDetails pd = new ParticleDetails(ParticleType.SOUND);
                     pd.sound = enumSound;
                     if (eh.addEffect(pd, true)) {
                         Lang.sendTo(sender, Lang.EFFECT_ADDED.toString().replace("%effect%", "Sound"));
                     }
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("player")) {
                 if (args[1].equalsIgnoreCase("list")) {
                     if (Permission.PLAYER_LIST.hasPerm(sender, true, true)) {
                         ArrayList<EffectHolder> list = new ArrayList<EffectHolder>();
                         for (EffectHolder eh : EffectManager.getInstance().getEffectHolders()) {
                             if (eh.getEffectType().equals(EffectHolder.EffectType.PLAYER)) {
                                 list.add(eh);
                             }
                         }
                         if (list.isEmpty()) {
                             Lang.sendTo(sender, Lang.PLAYER_LIST_NO_ACTIVE_EFFECTS.toString());
                             return true;
                         }
                         sender.sendMessage(SparkTrailPlugin.getInstance().secondaryColour + "------------ " + SparkTrailPlugin.getInstance().primaryColour + "Player" + " Trail Effects ------------");
                         for (EffectHolder eh : list) {
                             sender.sendMessage(SparkTrailPlugin.getInstance().primaryColour + eh.getDetails().playerName);
                             sender.sendMessage(SparkTrailPlugin.getInstance().primaryColour + " ---> " + SparkTrailPlugin.getInstance().secondaryColour + DataFactory.serialiseEffects(eh.getEffects(), true, true, false));
                         }
                         return true;
                     } else return true;
                 }
                 if (Permission.TRAIL.hasPerm(sender, true, false)) {
                     Player target = Bukkit.getPlayer(args[1]);
                     if (target == null) {
                         Lang.sendTo(sender, Lang.NULL_PLAYER.toString().replace("%player%", args[1]));
                         return true;
                     }
                     ParticleMenu pm = new ParticleMenu(((Player) sender), target.getName());
                     if (pm.fail) {
                         Lang.sendTo(sender, Lang.MENU_ERROR.toString());
                         return true;
                     }
                     pm.open(false);
                     Lang.sendTo(sender, Lang.ADMIN_OPEN_MENU.toString().replace("%player%", target.getName()));
                     return true;
                 } else return true;
             } else if (args[0].equalsIgnoreCase("location")) {
                 if (args[1].equalsIgnoreCase("list")) {
                     if (Permission.LOC_LIST.hasPerm(sender, true, true)) {
                         ArrayList<EffectHolder> list = new ArrayList<EffectHolder>();
                         for (EffectHolder eh : EffectManager.getInstance().getEffectHolders()) {
                             if (eh.getEffectType().equals(EffectHolder.EffectType.LOCATION)) {
                                 list.add(eh);
                             }
                         }
                         if (list.isEmpty()) {
                             Lang.sendTo(sender, Lang.LOC_LIST_NO_ACTIVE_EFFECTS.toString());
                             return true;
                         }
                         sender.sendMessage(SparkTrailPlugin.getInstance().secondaryColour + "------------ " + SparkTrailPlugin.getInstance().primaryColour + "Location" + " Trail Effects ------------");
                         for (EffectHolder eh : list) {
                             sender.sendMessage(SparkTrailPlugin.getInstance().primaryColour + DataFactory.serialiseLocation(eh.getLocation()));
                             sender.sendMessage(SparkTrailPlugin.getInstance().primaryColour + " ---> " + SparkTrailPlugin.getInstance().secondaryColour + DataFactory.serialiseEffects(eh.getEffects(), true, true, false));
                         }
                         return true;
                     } else return true;
                 } else if (args[1].equalsIgnoreCase("stop")) {
                     if (Permission.LOC_STOP.hasPerm(sender, true, false)) {
                         InteractListener.INTERACTION.put(sender.getName(), new InteractDetails(InteractDetails.InteractType.BLOCK, InteractDetails.ModifyType.STOP));
                         Lang.sendTo(sender, Lang.INTERACT_BLOCK.toString());
                         return true;
                     } else return true;
                 } else if (args[1].equalsIgnoreCase("start")) {
                     if (Permission.LOC_START.hasPerm(sender, true, false)) {
                         InteractListener.INTERACTION.put(sender.getName(), new InteractDetails(InteractDetails.InteractType.BLOCK, InteractDetails.ModifyType.START));
                         Lang.sendTo(sender, Lang.INTERACT_BLOCK.toString());
                         return true;
                     } else return true;
                 } else if (args[1].equalsIgnoreCase("clear")) {
                     if (Permission.LOC_CLEAR.hasPerm(sender, true, false)) {
                         InteractListener.INTERACTION.put(sender.getName(), new InteractDetails(InteractDetails.InteractType.BLOCK, InteractDetails.ModifyType.CLEAR));
                         Lang.sendTo(sender, Lang.INTERACT_BLOCK.toString());
                         return true;
                     } else return true;
                 }
             } else if (args[0].equalsIgnoreCase("mob")) {
                 if (args[1].equalsIgnoreCase("list")) {
                     if (Permission.MOB_LIST.hasPerm(sender, true, true)) {
                         ArrayList<EffectHolder> list = new ArrayList<EffectHolder>();
                         for (EffectHolder eh : EffectManager.getInstance().getEffectHolders()) {
                             if (eh.getEffectType().equals(EffectHolder.EffectType.MOB)) {
                                 list.add(eh);
                             }
                         }
                         if (list.isEmpty()) {
                             Lang.sendTo(sender, Lang.MOB_LIST_NO_ACTIVE_EFFECTS.toString());
                             return true;
                         }
                         sender.sendMessage(SparkTrailPlugin.getInstance().secondaryColour + "------------ " + SparkTrailPlugin.getInstance().primaryColour + "Mob" + " Trail Effects ------------");
                         for (EffectHolder eh : list) {
                             Entity e = DataFactory.getMob(eh.getDetails().mobUuid);
                             if (e != null) {
                                 sender.sendMessage(SparkTrailPlugin.getInstance().primaryColour + StringUtil.capitalise(e.getType().toString()));
                                 sender.sendMessage(SparkTrailPlugin.getInstance().primaryColour + " ---> " + SparkTrailPlugin.getInstance().secondaryColour + DataFactory.serialiseEffects(eh.getEffects(), true, true, false));
                             }
                         }
                         return true;
                     } else return true;
                 } else if (args[1].equalsIgnoreCase("stop")) {
                     if (Permission.MOB_STOP.hasPerm(sender, true, false)) {
                         InteractListener.INTERACTION.put(sender.getName(), new InteractDetails(InteractDetails.InteractType.MOB, InteractDetails.ModifyType.STOP));
                         Lang.sendTo(sender, Lang.INTERACT_MOB.toString());
                         return true;
                     } else return true;
                 } else if (args[1].equalsIgnoreCase("start")) {
                     if (Permission.MOB_START.hasPerm(sender, true, false)) {
                         InteractListener.INTERACTION.put(sender.getName(), new InteractDetails(InteractDetails.InteractType.MOB, InteractDetails.ModifyType.START));
                         Lang.sendTo(sender, Lang.INTERACT_MOB.toString());
                         return true;
                     } else return true;
                 } else if (args[1].equalsIgnoreCase("clear")) {
                     if (Permission.MOB_CLEAR.hasPerm(sender, true, false)) {
                         InteractListener.INTERACTION.put(sender.getName(), new InteractDetails(InteractDetails.InteractType.MOB, InteractDetails.ModifyType.CLEAR));
                         Lang.sendTo(sender, Lang.INTERACT_MOB.toString());
                         return true;
                     } else return true;
                 }
             } else {
                 if (EnumUtil.isEnumType(ParticleType.class, args[0].toUpperCase())) {
                     if (!(sender instanceof Player)) {
                         Lang.sendTo(sender, Lang.IN_GAME_ONLY.toString());
                         return true;
                     }
                     ParticleType pt = ParticleType.valueOf(args[0].toUpperCase());
                     Player p = (Player) sender;
                     ParticleDetails pd = null;
                     if (pt == ParticleType.CRITICAL) {
                         if (EnumUtil.isEnumType(Critical.CriticalType.class, args[1].toUpperCase())) {
                             Critical.CriticalType type = Critical.CriticalType.valueOf(args[1].toUpperCase());
                             if (Permission.hasEffectPerm(p, true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.PLAYER)) {
                                 pd = new ParticleDetails(pt);
                                 pd.criticalType = type;
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.CRITICAL_HELP.toString());
                             return true;
                         }
                     } else if (pt == ParticleType.POTION) {
                         if (EnumUtil.isEnumType(Potion.PotionType.class, args[1].toUpperCase())) {
                             Potion.PotionType type = Potion.PotionType.valueOf(args[1].toUpperCase());
                             if (Permission.hasEffectPerm(p, true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.PLAYER)) {
                                 pd = new ParticleDetails(pt);
                                 pd.potionType = type;
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.POTION_HELP.toString());
                             return true;
                         }
                     } else if (pt == ParticleType.SMOKE) {
                         if (EnumUtil.isEnumType(Smoke.SmokeType.class, args[1].toUpperCase())) {
                             Smoke.SmokeType type = Smoke.SmokeType.valueOf(args[1].toUpperCase());
                             if (Permission.hasEffectPerm(p, true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.PLAYER)) {
                                 pd = new ParticleDetails(pt);
                                 pd.smokeType = type;
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.SMOKE_HELP.toString());
                             return true;
                         }
                     } else if (pt == ParticleType.SWIRL) {
                         if (EnumUtil.isEnumType(Swirl.SwirlType.class, args[1].toUpperCase())) {
                             Swirl.SwirlType type = Swirl.SwirlType.valueOf(args[1].toUpperCase());
                             if (Permission.hasEffectPerm(p, true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.PLAYER)) {
                                 pd = new ParticleDetails(pt);
                                 pd.swirlType = type;
                                 pd.setPlayer(p.getName(), p.getUniqueId());
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.SWIRL_HELP.toString());
                             return true;
                         }
                     }
                     if (pd != null) {
                         EffectHolder eh = EffectManager.getInstance().getEffect(p.getName());
                         if (eh == null) {
                             eh = EffectCreator.createPlayerHolder(p.getName());
                         }
                         if (eh.hasEffect(pd)) {
                             eh.removeEffect(pd);
                             Lang.sendTo(p, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
                         } else {
                             if (eh.addEffect(pd, true)) {
                                 Lang.sendTo(p, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                             }
                         }
                         return true;
                     }
                 }
             }
         } else if (args.length == 3) {
             if (args[0].equalsIgnoreCase("player")) {
                 if (args[2].equalsIgnoreCase("info")) {
                     if (Permission.PLAYER_INFO.hasPerm(sender, true, true)) {
                         Player target = Bukkit.getPlayer(args[1]);
                         if (target == null) {
                             Lang.sendTo(sender, Lang.NULL_PLAYER.toString().replace("%player%", args[1]));
                             return true;
                         }
                         EffectHolder eh = EffectManager.getInstance().getEffect(target.getName());
                         if (eh == null || eh.getEffects().isEmpty()) {
                             Lang.sendTo(sender, Lang.PLAYER_NO_ACTIVE_EFFECTS.toString().replace("%player%", args[1]));
                             return true;
                         }
                         sender.sendMessage(c2 + "------------ Trail Effects ------------");
                         for (Effect e : eh.getEffects()) {
                             if (e.getParticleType().requiresDataMenu()) {
                                 sender.sendMessage(c1 + StringUtil.capitalise(e.getParticleType().toString()) + ": " + c2 + e.getParticleData());
                             } else {
                                 sender.sendMessage(c1 + StringUtil.capitalise(e.getParticleType().toString()));
                             }
                         }
                         return true;
                     } else return true;
                 } else if (args[2].equalsIgnoreCase("start")) {
                     if (Permission.PLAYER_START.hasPerm(sender, true, true)) {
                         Player target = Bukkit.getPlayer(args[1]);
                         if (target == null) {
                             Lang.sendTo(sender, Lang.NULL_PLAYER.toString().replace("%player%", args[1]));
                             return true;
                         }
                         EffectHolder eh = EffectManager.getInstance().createFromFile(target.getName());
                         if (eh == null || eh.getEffects().isEmpty()) {
                             Lang.sendTo(sender, Lang.PLAYER_NO_EFFECTS_TO_LOAD.toString().replace("%player%", target.getName()));
                             EffectManager.getInstance().clear(eh);
                             return true;
                         }
                         Lang.sendTo(sender, Lang.PLAYER_EFFECTS_LOADED.toString().replace("%player%", target.getName()));
                         return true;
                     } else return true;
                 } else if (args[2].equalsIgnoreCase("stop")) {
                     if (Permission.PLAYER_START.hasPerm(sender, true, true)) {
                         Player target = Bukkit.getPlayer(args[1]);
                         if (target == null) {
                             Lang.sendTo(sender, Lang.NULL_PLAYER.toString().replace("%player%", args[1]));
                             return true;
                         }
                         EffectHolder eh = EffectManager.getInstance().getEffect(target.getName());
                         if (eh == null) {
                             Lang.sendTo(sender, Lang.PLAYER_NO_ACTIVE_EFFECTS.toString().replace("%player%", args[1]));
                             return true;
                         }
                         EffectManager.getInstance().remove(eh);
                         Lang.sendTo(sender, Lang.PLAYER_EFFECTS_STOPPED.toString().replace("%player%", target.getName()));
                         return true;
                     } else return true;
                 } else if (args[2].equalsIgnoreCase("clear")) {
                     if (Permission.PLAYER_START.hasPerm(sender, true, true)) {
                         Player target = Bukkit.getPlayer(args[1]);
                         if (target == null) {
                             Lang.sendTo(sender, Lang.NULL_PLAYER.toString().replace("%player%", args[1]));
                             return true;
                         }
                         EffectHolder eh = EffectManager.getInstance().getEffect(target.getName());
                         if (eh == null) {
                             Lang.sendTo(sender, Lang.PLAYER_NO_ACTIVE_EFFECTS.toString().replace("%player%", args[1]));
                             return true;
                         }
                         EffectManager.getInstance().clear(eh);
                         Lang.sendTo(sender, Lang.PLAYER_EFFECTS_CLEARED.toString().replace("%player%", target.getName()));
                         return true;
                     } else return true;
                 }
             } else if (args[0].equalsIgnoreCase("location")) {
                 if (args[1].equalsIgnoreCase("stop")) {
                     if (args[2].equalsIgnoreCase("all")) {
                         if (Permission.LOC_STOP_ALL.hasPerm(sender, true, true)) {
                             for (EffectHolder eh : EffectManager.getInstance().getEffectHolders()) {
                                 if (eh.getEffectType().equals(EffectHolder.EffectType.LOCATION)) {
                                     EffectManager.getInstance().remove(eh);
                                 }
                             }
                             Lang.sendTo(sender, Lang.LOC_STOP_ALL.toString());
                         } else return true;
                     }
                 }
             }
         } else if (args.length == 4 && args[0].equalsIgnoreCase("location")) {
             if (Permission.LOC_STOP.hasPerm(sender, true, false)) {
                 Location l = DataFactory.getLocation(sender, args, 1);
                 if (l == null) {
                     return true;
                 }
 
                 ParticleMenu pm = new ParticleMenu((Player) sender, l);
                 pm.open(true);
                 Lang.sendTo(sender, Lang.OPEN_MENU.toString());
                 return true;
             } else return true;
         } else if (args.length == 5 && args[0].equals("location") && args[1].equalsIgnoreCase("stop")) {
             if (Permission.LOC_STOP.hasPerm(sender, true, true)) {
                 Location l = DataFactory.getLocation(sender, args, 2);
                 if (l == null) {
                     return true;
                 }
 
                 EffectHolder eh = EffectManager.getInstance().getEffect(l);
                 if (eh == null) {
                     Lang.sendTo(sender, Lang.LOC_NO_ACTIVE_EFFECTS.toString());
                     return true;
                 }
 
                 EffectManager.getInstance().remove(eh);
                 Lang.sendTo(sender, Lang.LOC_EFFECTS_STOPPED.toString());
                 return true;
             } else return true;
         } else if (args.length == 5 && args[0].equals("location") && args[1].equalsIgnoreCase("start")) {
             if (Permission.LOC_START.hasPerm(sender, true, true)) {
                 Location l = DataFactory.getLocation(sender, args, 2);
                 if (l == null) {
                     return true;
                 }
                 EffectHolder eh = EffectManager.getInstance().createFromFile(l);
                 if (eh == null || eh.getEffects().isEmpty()) {
                     Lang.sendTo(sender, Lang.LOC_NO_EFFECTS_TO_LOAD.toString());
                     EffectManager.getInstance().clear(eh);
                     return true;
                 }
                 Lang.sendTo(sender, Lang.LOC_EFFECTS_LOADED.toString());
                 return true;
             } else return true;
         } else if (args.length == 5 && args[0].equals("location") && args[1].equalsIgnoreCase("clear")) {
             if (Permission.LOC_CLEAR.hasPerm(sender, true, true)) {
                 Location l = DataFactory.getLocation(sender, args, 2);
                 if (l == null) {
                     return true;
                 }
                 EffectHolder eh = EffectManager.getInstance().getEffect(l);
                 EffectManager.getInstance().clear(eh);
                 Lang.sendTo(sender, Lang.LOC_EFFECTS_CLEARED.toString());
                 return true;
             } else return true;
        } else if (args.length == 5 || (args.length >= 6 && (args[4].equalsIgnoreCase("blockbreak") || args[4].equalsIgnoreCase("firework")))) {
             if (args[0].equalsIgnoreCase("location")) {
                 Location l = DataFactory.getLocation(sender, args, 1);
                 if (l == null) {
                     return true;
                 }
 
                 if (EnumUtil.isEnumType(ParticleType.class, args[4].toUpperCase())) {
                     ParticleType pt = ParticleType.valueOf(args[4].toUpperCase());
                     if (pt == ParticleType.BLOCKBREAK || pt == ParticleType.ITEMSPRAY) {
                         if (!(sender instanceof Player) || Permission.hasEffectPerm(((Player) sender), true, pt, EffectHolder.EffectType.PLAYER)) {
                             if (args.length == 5) {
                                 Lang.sendTo(sender, Lang.INVALID_EFFECT_ARGS.toString().replace("%effect%", pt == ParticleType.BLOCKBREAK ? "Block Break" : "ItemSpray").replace("%extra_info%", "Separate each parameter with a space."));
                                 return true;
                             }
 
                             BlockData bd = DataFactory.findBlockData(StringUtil.combineSplit(5, args, " "));
                             ParticleDetails pd = new ParticleDetails(pt);
                             pd.blockId = bd.id;
                             pd.blockMeta = bd.data;
                             add(l, pt, sender, pd);
                         }
                         return true;
                     } else if (pt == ParticleType.FIREWORK) {
                         if (!(sender instanceof Player) || Permission.hasEffectPerm(((Player) sender), true, pt, EffectHolder.EffectType.PLAYER)) {
                             if (args.length == 5) {
                                 Lang.sendTo(sender, Lang.INVALID_EFFECT_ARGS.toString().replace("%effect%", "Firework").replace("%extra_info%", "Separate each parameter with a space."));
                                 return true;
                             }
                             FireworkEffect fe = DataFactory.generateFireworkEffectFrom(StringUtil.combineSplit(5, args, " "));
                             ParticleDetails pd = new ParticleDetails(pt);
                             pd.fireworkEffect = fe;
                             add(l, pt, sender, pd);
                         }
                         return true;
                     } else if (pt == ParticleType.CRITICAL) {
                         Lang.sendTo(sender, Lang.CRITICAL_HELP.toString());
                         return true;
                     } else if (pt == ParticleType.POTION) {
                         Lang.sendTo(sender, Lang.POTION_HELP.toString());
                         return true;
                     } else if (pt == ParticleType.SMOKE) {
                         Lang.sendTo(sender, Lang.SMOKE_HELP.toString());
                         return true;
                     } else if (pt == ParticleType.SWIRL) {
                         Lang.sendTo(sender, Lang.SWIRL_NOT_ALLOWED.toString());
                         return true;
                     } else if (!(sender instanceof Player) || Permission.hasEffectPerm(((Player) sender), true, pt, EffectHolder.EffectType.PLAYER)) {
                         EffectHolder eh = EffectManager.getInstance().getEffect(l);
                         if (eh == null) {
                             eh = EffectCreator.createLocHolder(l);
                         }
                         if (eh.hasEffect(pt)) {
                             eh.removeEffect(pt);
                             Lang.sendTo(sender, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
                         } else {
                             if (eh.addEffect(pt, true)) {
                                 Lang.sendTo(sender, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                             }
                         }
                         return true;
                     } else return true;
                 }
             }
         } else if (args.length == 6) {
             if (args[0].equalsIgnoreCase("location")) {
                 Location l = DataFactory.getLocation(sender, args, 1);
                 if (l == null) {
                     return true;
                 }
 
                 ParticleDetails pd = null;
 
                 if (EnumUtil.isEnumType(ParticleType.class, args[4].toUpperCase())) {
                     ParticleType pt = ParticleType.valueOf(args[4].toUpperCase());
                     if (pt == ParticleType.CRITICAL) {
                         if (EnumUtil.isEnumType(Critical.CriticalType.class, args[5].toUpperCase())) {
                             Critical.CriticalType type = Critical.CriticalType.valueOf(args[5].toUpperCase());
                             if (!(sender instanceof Player) || Permission.hasEffectPerm(((Player) sender), true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.LOCATION)) {
                                 pd = new ParticleDetails(pt);
                                 pd.criticalType = type;
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.CRITICAL_HELP.toString());
                             return true;
                         }
                     } else if (pt == ParticleType.POTION) {
                         if (EnumUtil.isEnumType(Potion.PotionType.class, args[5].toUpperCase())) {
                             Potion.PotionType type = Potion.PotionType.valueOf(args[5].toUpperCase());
                             if (!(sender instanceof Player) || Permission.hasEffectPerm(((Player) sender), true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.LOCATION)) {
                                 pd = new ParticleDetails(pt);
                                 pd.potionType = type;
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.POTION_HELP.toString());
                             return true;
                         }
                     } else if (pt == ParticleType.SMOKE) {
                         if (EnumUtil.isEnumType(Smoke.SmokeType.class, args[5].toUpperCase())) {
                             Smoke.SmokeType type = Smoke.SmokeType.valueOf(args[5].toUpperCase());
                             if (!(sender instanceof Player) || Permission.hasEffectPerm(((Player) sender), true, pt, type.toString().toLowerCase(), EffectHolder.EffectType.LOCATION)) {
                                 pd = new ParticleDetails(pt);
                                 pd.smokeType = type;
                             } else return true;
                         } else {
                             Lang.sendTo(sender, Lang.SMOKE_HELP.toString());
                             return true;
                         }
                     } else if (pt == ParticleType.SWIRL) {
                         Lang.sendTo(sender, Lang.SWIRL_NOT_ALLOWED.toString());
                         return true;
                     }
                     if (pd != null) {
                         EffectHolder eh = EffectManager.getInstance().getEffect(l);
                         if (eh == null) {
                             eh = EffectCreator.createLocHolder(l);
                         }
                         if (eh.hasEffect(pd)) {
                             eh.removeEffect(pd);
                             Lang.sendTo(sender, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
                         } else {
                             if (eh.addEffect(pd, true)) {
                                 Lang.sendTo(sender, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
                             }
                         }
                         return true;
                     }
                 }
             }
         }
 
         Lang.sendTo(sender, Lang.COMMAND_ERROR.toString()
                 .replace("%cmd%", "/" + cmd.getLabel() + " " + (args.length == 0 ? "" : StringUtil.combineSplit(0, args, " "))));
         return true;
     }
 
     public void add(Location l, ParticleType pt, CommandSender sender, ParticleDetails pd) {
         EffectHolder eh = EffectManager.getInstance().getEffect(l);
         if (eh == null) {
             eh = EffectCreator.createLocHolder(l);
         }
         if (eh.hasEffect(pd)) {
             eh.removeEffect(pd);
             Lang.sendTo(sender, Lang.EFFECT_REMOVED.toString().replace("%effect%", pt.getName()));
         } else {
             if (eh.addEffect(pd, true)) {
                 Lang.sendTo(sender, Lang.EFFECT_ADDED.toString().replace("%effect%", pt.getName()));
             }
         }
     }
 }
