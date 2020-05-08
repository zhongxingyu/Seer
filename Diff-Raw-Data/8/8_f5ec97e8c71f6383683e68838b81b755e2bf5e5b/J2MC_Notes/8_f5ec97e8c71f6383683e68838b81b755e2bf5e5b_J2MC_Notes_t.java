 package to.joe.j2mc.notes;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.notes.command.NoteCommand;
 import to.joe.j2mc.notes.util.Note;
 
 public class J2MC_Notes extends JavaPlugin implements Listener {
 
     public Manager manager;
 
     @Override
     public void onEnable() {
         this.manager = new Manager(this);
         
         this.getCommand("note").setExecutor(new NoteCommand(this));
         this.getCommand("anote").setExecutor(new NoteCommand(this));
        this.getServer().getPluginManager().registerEvents(this, this);
         
         this.getLogger().info("Notes module enabled");
     }
 
     @Override
     public void onDisable() {
 
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         ArrayList<Note> notes = manager.grabNotes(event.getPlayer().getName());
         if(notes.size() > 0){
             event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "You have notes!");
             for (final Note note : notes) {
                 if (note != null) {
                     event.getPlayer().sendMessage(note.toString());
                 }
             }
         }
     }
 
 }
