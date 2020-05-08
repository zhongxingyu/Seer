 package com.craftfire.taboo.layer.bukkit.actions;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.craftfire.taboo.Action;
 import com.craftfire.taboo.Taboo;
 import com.craftfire.taboo.TabooPlayer;
 import com.craftfire.taboo.layer.bukkit.TabooBukkitPlayer;
 
 import com.craftfire.commons.yaml.YamlException;
 import com.craftfire.commons.yaml.YamlNode;
 
 public class Lightning extends Action {
     private boolean effect = false;
     private String worldName = null;
    private String playerName = null;
     private double x, y, z;
 
     public Lightning(YamlNode args) {
         super(args);
         try {
             if (getArgs().hasChild("effect")) {
                 this.effect = getArgs().getChild("effect").getBool();
             }
             if (getArgs().hasChild("target")) {
                 YamlNode node = getArgs().getChild("target");
                 if (node.isScalar()) {
                    this.playerName = node.getString();
                     return;
                 }
                 if (node.isMap()) {
                     throw new IllegalArgumentException("Argument 'target' must not be a map.");
                 }
                 if (node.getChildrenCount() < 3) {
                     throw new IllegalArgumentException("Argument 'target' is a list, so it must have at least 3 elements (x, y, z).");
                 }
                 this.x = node.getChildrenList().get(0).getDouble();
                 this.y = node.getChildrenList().get(1).getDouble();
                 this.z = node.getChildrenList().get(2).getDouble();
                 if (node.getChildrenCount() >= 4) {
                     this.worldName = node.getChildrenList().get(3).getString();
                 }
             }
         } catch (YamlException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void execute(TabooPlayer target, Taboo taboo, String message) {
         Location loc;
         try {
             if (!getArgs().hasChild("target") || getArgs().getChild("target").isNull()) {
                 loc = ((TabooBukkitPlayer) target).getPlayer().getLocation();
             } else {
                 Player player = ((TabooBukkitPlayer) target).getPlayer();
                if (this.playerName != null) {
                    loc = player.getServer().getPlayer(this.playerName).getLocation();
                } else {
                    World world = null;
                    if (this.worldName != null) {
                        world = player.getServer().getWorld(this.worldName);
                    }
                    if (world == null) {
                        world = player.getWorld();
                    }
                    loc = new Location(world, this.x, this.y, this.z);
                 }
             }
         } catch (YamlException e) {
             throw new RuntimeException(e);
         }
         if (this.effect) {
             loc.getWorld().strikeLightningEffect(loc);
         } else {
             loc.getWorld().strikeLightning(loc);
         }
     }
 
 }
