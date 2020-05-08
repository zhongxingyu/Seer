 package me.arno.blocklog.database;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.logs.LoggedBlock;
 import me.arno.blocklog.logs.LoggedInteraction;
 
 public class PushBlocks {
 	BlockLog plugin;
 	
 	public PushBlocks(BlockLog plugin) {
 		this.plugin = plugin;
 		startPush();
 	}
 	
 	public void startPush() {
 		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
 			public void run() {
 				try {
 					if(plugin.getInteractions().size() > 0) {
 			    		LoggedInteraction interaction = plugin.getInteractions().get(0);
 			    		interaction.save();
 				    	plugin.getInteractions().remove(0);
 			    	}
 					if(plugin.getBlocks().size() > 0) {
 						LoggedBlock block = plugin.getBlocks().get(0);
 				    	block.save();
 				    	plugin.getBlocks().remove(0);
 			    	}
 				} catch(Exception e) { e.printStackTrace(); }
 		    }
 		}, 100L, plugin.getConfig().getInt("database.delay") * 20L);
 	}
 }
