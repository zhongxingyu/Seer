 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashMap;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.managers.ClassManager;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.PlayerClass;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.tosort.ParticleEffect;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 
 public class SignListener extends APIRelatedListener {
 
     private static final String LINE = "----------";
 
     public SignListener(BattleNightAPI api) {
         super(api);
     }
 
     private static void reset(Player player) {
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
         for (PotionEffect effect : player.getActivePotionEffects()) {
             player.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             Block block = event.getClickedBlock();
             Player player = event.getPlayer();
 
             if (block.getState() instanceof Sign) {
                 Sign sign = (Sign) block.getState();
                 String title = sign.getLine(1);
 
                 HashMap<String, PlayerClass> classes = new HashMap<String, PlayerClass>();
                 ClassManager manager = BattleNight.instance.getAPI().getClassManager();
                 for (PlayerClass clazz : manager.getClasses()) {
                     classes.put(clazz.getName(), clazz);
                 }
 
                // For old signs
                if (classes.containsKey(sign.getLine(0))) {
                    title = sign.getLine(0);
                    sign.setLine(0, "");
                    sign.setLine(1, title);
                    sign.setLine(2, LINE);
                    sign.setLine(3, "");
                    sign.update();
                }

                 if (getAPI().getLobby().getPlayers().contains(player.getName())) {
                     if (classes.containsKey(title)) {
                         PlayerClass playerClass = classes.get(title);
 
                         if (player.hasPermission(playerClass.getPermission())) {
                             if (Metadata.getPlayerClass(player) != playerClass) {
                                 ParticleEffect.classSelect(player, ConfigManager.get(Config.MAIN).getString("Particles.ClassSelection", "smoke"));
                             }
 
                             reset(player);
                             getAPI().setPlayerClass(player, classes.get(title));
                         } else {
                             getAPI().getMessenger().tell(player, Message.NO_PERMISSION_CLASS);
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onSignChange(SignChangeEvent e) {
         Player player = e.getPlayer();
         String title = e.getLine(0);
 
         HashMap<String, PlayerClass> classes = new HashMap<String, PlayerClass>();
         ClassManager manager = BattleNight.instance.getAPI().getClassManager();
         for (PlayerClass clazz : manager.getClasses()) {
             classes.put(clazz.getName(), clazz);
         }
 
        if (classes != null) {
            if (classes.containsKey(title)) {
                if (!e.getLine(1).isEmpty() || !e.getLine(2).isEmpty() || !e.getLine(3).isEmpty()) {
                    getAPI().getMessenger().tell(player, Message.UNSUCCESSFUL_SIGN, title);
                    return;
                }

                e.setLine(0, "");
                e.setLine(1, title);
                e.setLine(2, LINE);
                e.setLine(3, "");
                getAPI().getMessenger().tell(player, Message.SUCCESSFUL_SIGN, title);
             }
         }
     }
 }
