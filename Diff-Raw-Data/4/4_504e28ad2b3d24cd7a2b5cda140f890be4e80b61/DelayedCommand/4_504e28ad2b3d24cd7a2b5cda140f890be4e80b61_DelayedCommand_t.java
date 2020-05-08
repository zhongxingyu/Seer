 package me.asofold.bukkit.delayedcommand;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class DelayedCommand extends JavaPlugin {
 	
 	final String msgLabel = "[DelayedCommand]";
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
		if (!sender.isOp() && !sender.hasPermission("delayedcommand.use")){
			sender.sendMessage("You do not have permission to use this!");
			return true;
		}
 		int len = args.length;
 		if (len == 0) return true; // allows for a no op command!
 		int startIndex = 0;
 		long delay = -1;
 		if (args[0].trim().toLowerCase().startsWith("-delay=")){
 			// parse delay:
 			startIndex = 1;
 			try{
 				delay = Long.parseLong(args[0].substring(7));
 			} catch(NumberFormatException nEx){
 				getServer().getLogger().severe(msgLabel+" Bad delay definition ("+args[0]+") on command: "+getCmdLine(args, 0));
 				return false;
 			}
 		}
 		if ( startIndex >= len) return true; // still a no-op
 		scheduleCmd(getCmdLine(args, startIndex), delay);
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param args
 	 * @param startIndex
 	 * @return
 	 */
 	private String getCmdLine(String[] args, int startIndex) {
 		StringBuilder b = new StringBuilder(200);
 		for (int i = startIndex; i<args.length; i++){
 			if (i>startIndex) b.append(' ');
 			b.append(args[i]);
 		}
 		return b.toString();
 	}
 	
 	private void scheduleCmd(final String cmdLine, long delay) {
 		int taskId;
 		Runnable task = new Runnable(){
 			@Override
 			public void run() {
 				executeCmd(cmdLine);
 			}
 		};
 		Server server = getServer();
 		BukkitScheduler sched = server.getScheduler();
 		if (delay <=0) taskId = sched.scheduleSyncDelayedTask(this, task);
 		else taskId = sched.scheduleSyncDelayedTask(this, task, delay);
 		if (taskId == -1) server.getLogger().severe(msgLabel+" Failed to schedule command: "+cmdLine);
 	}
 
 	
 	private void executeCmd(String cmd){
 		Server server = getServer();
 		Logger logger = server.getLogger();
 		try{
 			if (!server.dispatchCommand(server.getConsoleSender(), cmd)) logger.warning(msgLabel+" Command not understood: "+cmd);
 		} catch (Throwable t){
 			logger.severe(msgLabel+" Command failed: "+cmd);
 			StringWriter w = new StringWriter();
 			t.printStackTrace(new PrintWriter(w));
 			logger.severe(w.toString());
 		}
 	}
 
 }
