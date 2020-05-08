 package com.herocraftonline.dev.heroes.effects;
 
 import net.minecraft.server.Packet20NamedEntitySpawn;
 import net.minecraft.server.Packet29DestroyEntity;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class InvisibleEffect extends ExpirableEffect {
 
     private final String applyText;
     private final String expireText;
     
     public InvisibleEffect(Skill skill, long duration, String applyText, String expireText) {
         super(skill, "Invisible", duration);
         this.types.add(EffectType.BENEFICIAL);
         this.applyText = applyText;
         this.expireText = expireText;
     }
 
     @Override
     public void apply(Hero hero) {
         super.apply(hero);
         Player player = hero.getPlayer();
         // Tell all the logged in Clients to Destroy the Entity - Appears Invisible.
         for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (player.equals(onlinePlayer) || player.getLocation().distanceSquared(onlinePlayer.getLocation()) > 16000 )
                 continue;
             
             ((CraftPlayer) onlinePlayer).getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(((CraftPlayer) player).getEntityId()));
         }
 
         broadcast(player.getLocation(), applyText, player.getDisplayName());
     }
 
     @Override
     public void remove(Hero hero) {
         super.remove(hero);
         Player player = hero.getPlayer();
         for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
             if (onlinePlayer.equals(player) || player.getLocation().distanceSquared(onlinePlayer.getLocation()) > 16000) 
                 continue;
             
             ((CraftPlayer) onlinePlayer).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(((CraftPlayer) player).getHandle()));
         }
 
         broadcast(player.getLocation(), expireText, player.getDisplayName());
     }
 }
