 package net.minecore.mineplot.player;
 
 import java.util.ArrayList;
 
 import net.minecore.Miner;
 import net.minecore.mineplot.MinePlot;
 import net.minecore.mineplot.plot.InvalidPlotException;
 import net.minecore.mineplot.plot.Plot;
 import net.minecore.mineplot.world.PlotWorld;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 
 public class PlotPlayer{
 	
 	private ArrayList<Plot> plots;
 	private Miner m;
 	private ConfigurationSection plotConf;
 	private MinePlot mp;
 	
 	public PlotPlayer(Miner m, MinePlot mp){
 		
 		plots = new ArrayList<Plot>();
 		this.m = m;
 		this.mp = mp;
 		
 		if(m != null){
 			plotConf = m.getConfigurationSection("plots");
 			loadPlots();
 		}
 		
 	}
 
 	public ArrayList<Plot> getPlots() {
 		return plots;
 	}
 	
 	public boolean removePlot(Plot p){
 		return plots.remove(p);
 	}
 	
 	public boolean addPlot(Plot p){
 		if(p.getOwner() != null && !p.getOwner().equals(m.getPlayerName()))
 			return false;
 		
 		plots.add(p);
 		p.setOwner(m.getPlayerName());
 		return true;
 	}
 
 	public Plot getPlot(String name) {
 		for(Plot p : plots)
 			if(p.getName().equals(name))
 				return p;
 		return null;
 	}
 
 	public String getName() {
 		return m.getPlayerName();
 	}
 	
 	public Miner getMiner(){
 		return m;
 	}
 	
 	public boolean savePlots(){
 		if(m == null)
 			return false;
 		
		for(String s : plotConf.getKeys(false))
			plotConf.set(s, null);
		
 		for(Plot p : plots){
 			plotConf.set(p.getName(), null);
 			ConfigurationSection cs = plotConf.createSection(p.getName());
 			cs.set("world", p.getLocation1().getWorld().getName());
 			cs.set("x1", p.getLocation1().getBlockX());
 			cs.set("z1", p.getLocation1().getBlockZ());
 			cs.set("x2", p.getLocation2().getBlockX());
 			cs.set("z2", p.getLocation2().getBlockZ());
 			cs.set("allowed_players", p.getAllowedPlayers());
 		}
 		
 		return true;
 	}
 
 	public boolean loadPlots() {
 		if(m == null)
 			return false;
 		
 		mp.log.info("Loading data for player " + m.getPlayerName());
 		
 		for(String s : plotConf.getKeys(false)){
 			if(plotConf.isConfigurationSection(s))
 				try {
 					loadPlotFromConf(s, plotConf.getConfigurationSection(s));
 				} catch(InvalidConfigurationException e){
 					mp.log.warning("Couldn't load plot " + s + ", error with configuration: " + e.getMessage());
 				} catch (InvalidPlotException e) {
 					mp.log.warning("Couldn't load plot " + s + ", bad plot:" + e.getMessage());
 				}
 		}
 		
 		return true;	
 		
 	}
 	
 	private void loadPlotFromConf(String name, ConfigurationSection cs) throws InvalidConfigurationException, InvalidPlotException{
 		
 		String world = cs.getString("world");
 		int x1 = cs.getInt("x1");
 		int z1 = cs.getInt("z1");
 		int x2 = cs.getInt("x2");
 		int z2 = cs.getInt("z2");
 		
 		if(world == null)
 			throw new InvalidConfigurationException("No world set!");
 		
 		PlotWorld pw = mp.getPWM().getPlotWorld(world);
 		
 		if(pw == null)
 			throw new InvalidConfigurationException("World invalid or not initialized!");
 		
 		Plot p = pw.getNewPlot(x1, z1, x2, z2);
 		
 		if(!pw.registerPlot(p))
 			throw new InvalidConfigurationException("Plot invalid! Error during registration.");
 		
 		p.setName(name);
 		p.setOwner(m.getPlayerName());
 		
 		addPlot(p);
 		
 		for(String s : cs.getStringList("allowed_players"))
 			p.addPlayer(s);
 		
 	}
 }
