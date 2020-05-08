 package edgruberman.bukkit.doorman;
 
 import java.io.File;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.Permissible;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.doorman.messaging.Individual;
 import edgruberman.bukkit.doorman.messaging.Message;
 
 /** manages player join messages */
 public final class Doorman implements Listener, Runnable {
 
     private static final long TICKS_PER_SECOND = 20;
 
     private final RecordKeeper records;
     private final long grace;
 
     /** permission dependent messages (keyed by permission name) */
     private final Map<String, Object> switches = new HashMap<String, Object>();
 
     private final List<String> headers  = new ArrayList<String>();
     private final List<String> arguments  = new ArrayList<String>();
     private final Map<String, Long> lastDeclaration = new HashMap<String, Long>();
     private final Plugin plugin;
 
     Doorman(final Plugin plugin, final RecordKeeper records, final long grace, final Map<String, Object> switches, final List<String> headers, final List<String> arguments) {
         this.plugin = plugin;
         this.records = records;
         this.grace = grace;
         this.switches.putAll(switches);
         this.headers.addAll(headers);
         this.arguments.addAll(arguments);
 
         Bukkit.getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerJoin(final PlayerJoinEvent join) {
         // headers - always displayed, if player has permission, each on separate line
         for (final String header : this.headers) {
             final Object value = this.switchFor(join.getPlayer(), header);
             if (value.toString().length() > 0) Main.courier.sendMessage(join.getPlayer(), "{1}", value);
         }
 
         // greeting - always displayed, switches appended as arguments, empty strings if player does not have permission
         final String serverAge = Bukkit.getWorlds().get(0).getFullTime() / 20 / 86400 + " days";
         long serverSize = 0; for (final World world : Bukkit.getWorlds()) serverSize += Doorman.directorySize(world.getWorldFolder());
         final List<Object> args = new ArrayList<Object>();
         args.add(serverAge);
         args.add(Doorman.readableFileSize(serverSize));
         for (final String argument : this.arguments) args.add(this.switchFor(join.getPlayer(), argument));
         final Message message = Main.courier.compose("greeting", args.toArray());
 
         // declaration - do not show if player has already received this message in the last grace period
         if (this.records.getHistory().size() > 0) {
             final Long last = this.lastDeclaration.get(join.getPlayer().getName());
            if ((last != null) && ((System.currentTimeMillis() - last) <= this.grace)) return;
            message.append(this.records.declare(join.getPlayer()));
            this.updateLast(join.getPlayer().getName());
         }
 
         // missed - excluding the declaration just sent, check if at least the previous one was missed
         if (this.records.getHistory().size() > 1 && join.getPlayer().getLastPlayed() < this.records.getHistory().get(1).set)
             message.append(Main.courier.compose("missed"));
 
         Main.courier.submit(new Individual(join.getPlayer()), message);
     }
 
     @EventHandler
     public void onPlayerQuit(final PlayerQuitEvent event) {
         // Run a clean-up task when the grace period has expired (In case someone joins and immediately crashes)
         // TODO figure a better way to do this to avoid large numbers of tasks building up
         Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, this, Doorman.convertMsToTicks(this.grace));
     }
 
     @Override
     public void run() {
         final long now = System.currentTimeMillis();
         final Iterator<Map.Entry<String, Long>> it = this.lastDeclaration.entrySet().iterator();
         while (it.hasNext()) {
             final Map.Entry<String, Long> entry = it.next();
             if ((now - entry.getValue()) >= this.grace) it.remove();
         }
     }
 
     public void clearLast() {
         this.lastDeclaration.clear();
     }
 
     public void updateLast(final String name) {
         this.lastDeclaration.put(name, System.currentTimeMillis());
     }
 
     private Object switchFor(final Permissible target, final String name) {
         final Object value = this.switches.get(name);
         return (value != null && target.hasPermission(name) ? value : "");
     }
 
     private static long directorySize(final File directory) {
         long length = 0;
         for (final File file : directory.listFiles()) {
             if (file.isFile())
                 length += file.length();
             else
                 length += Doorman.directorySize(file);
         }
         return length;
     }
 
     private static String readableFileSize(final long size) {
         if(size <= 0) return "0";
         final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
         final int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
         return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
     }
 
     private static long convertMsToTicks(final long milliseconds) {
         return TimeUnit.MILLISECONDS.toSeconds(Math.max(0, milliseconds)) * Doorman.TICKS_PER_SECOND;
     }
 
 }
