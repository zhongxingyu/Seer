 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import java.util.List;
 
import net.minecraft.server.EntityHuman;

 import org.bukkit.Material;
 import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.nijiko.coelho.iConomy.util.Messaging;
 
 public class SkillTelekinesis extends ActiveSkill {
 
     public SkillTelekinesis(Heroes plugin) {
         super(plugin, "Telekinesis");
         setDescription("Activate levers, buttons and other interactable objects from afar!");
         setUsage("/skill telekinesis");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill telekinesis"});
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         HashSet<Byte> transparent = new HashSet<Byte>();
         transparent.add((byte) Material.AIR.getId());
         transparent.add((byte) Material.WATER.getId());
         List<Block> lineOfSight = player.getLineOfSight(transparent, 15);
         Block block = lineOfSight.get(lineOfSight.size() - 1);
        if (block.getType() == Material.LEVER || block.getType() == Material.STONE_BUTTON) {
            //Can't adjust levers/Buttons through CB 
            net.minecraft.server.Block.byId[block.getTypeId()].interact(((CraftWorld)block.getWorld()).getHandle(), block.getX(), block.getY(), block.getZ(), (EntityHuman) player);
            //In Case Bukkit ever fixes blockState changes on levers:
            //Lever lever = (Lever) block.getState().getData();
            //lever.setPowered(!lever.isPowered());
            //block.getState().update();
             broadcastExecuteText(hero);
            return true;
        } 
        Messaging.send(player, "You must target a lever or button");
        return false;
     }
 
 }
