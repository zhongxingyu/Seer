 package net.minecore.mineplot;
 
 import net.minecore.mineplot.plot.Plot;
 import net.minecore.mineplot.world.PlotWorld;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 public class WorldListener implements Listener {
 	
 	private MinePlot mp;
 
 	public WorldListener(MinePlot mp){
 		this.mp = mp;
 	}
 	
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent e){
 		
 		PlotWorld pw = mp.getPWM().getPlotWorld(e.getBlock().getLocation().getWorld().getName());
 		if(pw == null)
 			return;
 		
 		//Check if block is too high to count
 		if(pw.getEffectiveDepth() < e.getBlock().getLocation().getBlockY())
 			return;
 		
 		Plot plot = pw.getContainingPlot(e.getBlock().getLocation());
 		
 		if(plot == null){
 			e.getPlayer().sendMessage(ChatColor.RED + "Sorry! You cannot mine here. If you would like to buy a plot use the command /plot");
 			e.setCancelled(true);
 			return;
 		}
 		
 		if(!plot.canUse(e.getPlayer())){
			e.getPlayer().sendMessage(ChatColor.RED + "Sorry! You cannot mine here, this plot is owned by someone else. If you would like to use it, they can give you permission.");
 			e.setCancelled(true);
 			return;
 		}
 		
 		e.setCancelled(false);
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent e){
 		
 		PlotWorld pw = mp.getPWM().getPlotWorld(e.getBlock().getLocation().getWorld().getName());
 		if(pw == null)
 			return;
 		
 		if(!pw.preventBuilding())
 			return;
 		
 		//Check if block is too high to count
 		if(pw.getEffectiveDepth() < e.getBlock().getLocation().getBlockY())
 			return;
 		
 		Plot plot = pw.getContainingPlot(e.getBlock().getLocation());
 		
 		if(plot == null){
 			e.getPlayer().sendMessage(ChatColor.RED + "Sorry! You cannot build here. If you would like to buy a plot use the command /plot");
 			e.setCancelled(true);
 			return;
 		}
 		
 		if(!plot.canUse(e.getPlayer())){
 			e.getPlayer().sendMessage(ChatColor.RED + "Sorry! You cannot build here, this plot is owned by someone else. If you would like to use it, they can give you permission.");
 			e.setCancelled(true);
 			return;
 		}
 		
 		e.setCancelled(false);
 	}
 }
