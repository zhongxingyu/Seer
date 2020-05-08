 package bencvt.minecraft.server.extrafluffy;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Material;
import org.bukkit.entity.MagmaCube;
 import org.bukkit.entity.Sheep;
 import org.bukkit.entity.Slime;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.SlimeSplitEvent;
 import org.bukkit.event.player.PlayerShearEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 class ExtraFluffyListener implements Listener {
     private final ExtraFluffyPlugin plugin;
     private static final Random rand = new Random();
 
     ExtraFluffyListener(ExtraFluffyPlugin instance) {
         plugin = instance;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerShearEntity(PlayerShearEntityEvent event) {
         if (!(event.getEntity() instanceof Sheep)) {
             // Mooshrooms aren't fluffy, go away
             return;
         }
         Sheep sheep = (Sheep) event.getEntity();
         int numItems = getRandomDropAmount(plugin.configHelper.EXTRA_SHEAR_WOOL);
         if (plugin.configHelper.EXTRA_SHEAR_WOOL.stack && numItems > 0) {
             sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(Material.WOOL, numItems, (short) 0, sheep.getColor().getData()));
         } else {
             for (int i = 0; i < numItems; i++) {
                 sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(Material.WOOL, 1, (short) 0, sheep.getColor().getData()));
             }
         }
         // The normal shear will still occur, yielding 1-3 more blocks on top of what we just did.
         //
         // We could cancel the event and take over the shearing entirely, but then we'd end up with
         // a fair amount of duplicated code to manage the durability on the shears. There is no API
         // for this, and the situation is further complicated by the fact that the client
         // independently tracks durability as well.
         //
         // Until such an API exists, it's saner to just let the shear event happen.
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onSlimeSplit(SlimeSplitEvent event) {
         Slime slime = event.getEntity();
        if (slime instanceof MagmaCube) {
            // Only the green kind, please
            return;
        }
         int numItems = getRandomDropAmount(plugin.configHelper.EXTRA_SPLIT_SLIME_BALLS);
         if (plugin.configHelper.EXTRA_SPLIT_SLIME_BALLS.stack && numItems > 0) {
             slime.getWorld().dropItemNaturally(slime.getLocation(), new ItemStack(Material.SLIME_BALL, numItems));
         } else {
             for (int i = 0; i < numItems; i++) {
                 slime.getWorld().dropItemNaturally(slime.getLocation(), new ItemStack(Material.SLIME_BALL, 1));
             }
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onEntityDeath(EntityDeathEvent event) {
         List<ItemStack> drops = event.getDrops();
         if (plugin.configHelper.CLEAR_DEATH_LOOT.contains(event.getEntityType())) {
             drops.clear();
         }
         for (ConfigHelper.ExtraDeathLoot lootInfo : plugin.configHelper.EXTRA_DEATH_LOOT) {
             if (event.getEntityType() == lootInfo.entityType) {
                 int numItems = getRandomDropAmount(lootInfo.dropAmount);
                 if (lootInfo.dropAmount.stack && numItems > 1) {
                     ItemStack stacked = new ItemStack(lootInfo.item);
                     stacked.setAmount(numItems * Math.max(1, stacked.getAmount()));
                     drops.add(stacked);
                 } else {
                     for (int i = 0; i < numItems; i++) {
                         drops.add(new ItemStack(lootInfo.item));
                     }
                 }
             }
         }
     }
 
     private static int getRandomDropAmount(ConfigHelper.ExtraDropAmount extraDropAmount) {
         if (rand.nextDouble() >= extraDropAmount.chance) {
             return 0;
         }
         int lo = extraDropAmount.min;
         int hi = extraDropAmount.max;
         if (lo > hi) {
             hi = lo;
         }
         return Math.max(0, lo + rand.nextInt(hi - lo + 1));
     }
 }
