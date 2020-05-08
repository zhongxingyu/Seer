 package de.jaschastarke.bukkit.lib.events;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Hanging;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Player;
 import org.bukkit.event.hanging.HangingBreakByEntityEvent;
 import org.bukkit.event.hanging.HangingBreakEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class HangingBreakByPlayerBlockEvent extends HangingBreakByEntityEvent {
     private final RemoveCause cause;
     private List<ItemStack> drops;
     public HangingBreakByPlayerBlockEvent(final Hanging hanging, final Player remover, final RemoveCause cause) {
         super(hanging, remover);
         this.cause = cause;
         drops = new ArrayList<ItemStack>(2);
         if (hanging instanceof Painting) {
             drops.add(new ItemStack(Material.PAINTING));
         } else if (hanging instanceof ItemFrame) {
             drops.add(new ItemStack(Material.ITEM_FRAME));
            ItemStack containedItem = ((ItemFrame) hanging).getItem();
            if (containedItem != null && containedItem.getType() != Material.AIR)
                drops.add(containedItem);
         }
     }
     public Player getPlayer() {
         return (Player) getRemover();
     }
     public HangingBreakEvent.RemoveCause getCause() {
         return cause;
     }
     public List<ItemStack> getDrops() {
         return drops;
     }
 }
