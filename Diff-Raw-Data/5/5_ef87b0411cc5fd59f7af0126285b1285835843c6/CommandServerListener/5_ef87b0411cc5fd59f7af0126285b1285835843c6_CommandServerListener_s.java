 package littlegruz.autoruncommands.listeners;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 
 import littlegruz.autoruncommands.CommandMain;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.scheduler.BukkitTask;
 
 public class CommandServerListener implements Listener {
    private CommandMain plugin;
    
    public CommandServerListener(CommandMain instance){
       plugin = instance;
    }
    
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event){
       // Run the startup tasks one second after everything has loaded
       plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 
          public void run(){
             String cmd;
             StringTokenizer st = new StringTokenizer(plugin.getStartupCommands(), ":");
             
             if(!plugin.isStartupDone()){
                while(st.countTokens() > 0){
                   cmd = st.nextToken();
                   if(plugin.getCommandMap().get(cmd) != null)
                      plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(cmd));
                   else
                      plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(cmd + "[op]"));
                }
                plugin.setStartupDone(true);
             }
          }
       }, 20L);
       
       /* Start the repeating tasks one and a half seconds after everything has
        * loaded.
        * I heard you like scheduled tasks, so I put a scheduled task in your
        * scheduled task */
       plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
          
          public void run(){
             int interval;
             BukkitTask repeatTask;
             HashMap<String, Integer> remainderMap = new HashMap<String, Integer>();
             Iterator<Map.Entry<String, Integer>> it = plugin.getRepeatMap().entrySet().iterator();
             
             // Attach the remainder times onto the tasks that need to repeat
             try{
                BufferedReader br = new BufferedReader(new FileReader(plugin.getRemainderFile()));
                StringTokenizer st;
                String input;
                
                while((input = br.readLine()) != null){
                   if(input.compareToIgnoreCase("<Command> <Remainder>") == 0){
                      continue;
                   }
                   st = new StringTokenizer(input, " ");
                   remainderMap.put(st.nextToken(), Integer.parseInt(st.nextToken()));
                }
                
                br.close();
                
             }catch(FileNotFoundException e){
                plugin.getServer().getLogger().info("No original repeating task remaining file, a new one will be created on shutdown/restart.");
             }catch(Exception e){
                plugin.getServer().getLogger().info("Incorrectly formatted repeating task remaining file");
             }
             
             // The running of the tasks
             while(it.hasNext()){
                Entry<String, Integer> mp = it.next();
                final String command = mp.getKey();
                long time = new Date().getTime();
                time /= 1000;
                
                interval = mp.getValue();
                
                if((plugin.getCommandMap().get(command) != null
                      || plugin.getCommandMap().get(command + "[op]") != null)
                      && plugin.getRunningRepeatMap().get(command) == null){
             	   repeatTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){
 
                      public void run() {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(command));
                      }
                   }, remainderMap.get(command) * 20, interval * 20);
                   
                   // This sets the "starting" time to be what it would be if the server was running
                   time = time - (interval - remainderMap.get(command));
                   plugin.getRunningRepeatMap().put(command, Integer.toString(repeatTask.getTaskId()) + "|" + Long.toString(time));
                }
             }
          }
       }, 40L);
    }
 }
