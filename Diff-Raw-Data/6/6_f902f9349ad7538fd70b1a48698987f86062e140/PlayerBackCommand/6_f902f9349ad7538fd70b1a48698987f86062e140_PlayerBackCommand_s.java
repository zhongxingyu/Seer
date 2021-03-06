 package edgruberman.bukkit.playeractivity.interpreters.AwayBack;
 
 import org.bukkit.event.EventHandler;
 
 import edgruberman.bukkit.playeractivity.Interpreter;
 import edgruberman.bukkit.playeractivity.Main;
 
 public class PlayerBackCommand extends Interpreter {
 
     @EventHandler
     public void onEvent(final org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        if (!Main.awayBack.isEnabled() || !Main.idleNotify.awayBroadcastOverride || !Main.awayBack.isAway(event.getPlayer())) return;
 
         final String message = event.getMessage().toLowerCase();
         if (!message.equals("/back") && !message.startsWith("/back ")) return;
 
         this.player = event.getPlayer();
     }
 
 }
