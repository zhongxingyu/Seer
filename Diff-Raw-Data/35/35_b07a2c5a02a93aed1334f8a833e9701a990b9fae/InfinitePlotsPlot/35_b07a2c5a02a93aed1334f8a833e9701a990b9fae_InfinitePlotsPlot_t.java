 package alshain01.Flags.area;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import alshain01.Flags.Flags;
 import alshain01.Flags.Message;
 import uk.co.jacekk.bukkit.infiniteplots.InfinitePlots;
 import uk.co.jacekk.bukkit.infiniteplots.plot.Plot;
 import uk.co.jacekk.bukkit.infiniteplots.plot.PlotLocation;
 
 public class InfinitePlotsPlot extends Area implements Removable {
 	protected final static String dataHeader = "InfinitePlotsData";
 	protected Plot plot;
 	
 	// ******************************
 	// Constructors
 	// ******************************
 	public InfinitePlotsPlot(Location location) {
 		this.plot = InfinitePlots.getInstance().getPlotManager().getPlotAt(PlotLocation.fromWorldLocation(location));
 	}
 	
	public InfinitePlotsPlot(String plotLocation) {
		String[] plotLocData = plotLocation.split(":");
		this.plot = InfinitePlots.getInstance().getPlotManager()
				.getPlotAt(new PlotLocation(plotLocData[0], Integer.valueOf(plotLocData[1]), Integer.valueOf(plotLocData[2])));
 	}
 	
 	// ******************************
 	// Area Interface
 	// ******************************
 	@Override
 	protected String getDataPath() {
 		return dataHeader + getSystemID();
 	}
 
 	@Override
 	public String getSystemID() {
		PlotLocation loc = plot.getLocation();
		return plot.getAdmin() + "." + loc.getWorldName() + ":" + loc.getX() + ":" + loc.getZ();
 	}
 
 	@Override
 	public String getAreaType() {
 		return Message.InfinitePlots.get();
 	}
 
 	@Override
 	public Set<String> getOwners() {
 		return new HashSet<String>(Arrays.asList(plot.getAdmin()));
 	}
 
 	@Override
 	public World getWorld() {
 		return plot.getLocation().getWorld();
 	}
 
 	@Override
 	public boolean isArea() {
		if(plot != null && plot.getAdmin() != null) { return true; }
 		return false;
 	}
 	
 	// ******************************
 	// Comparable Interface
 	// ******************************
 	/**
 	 * 0 if the the plots are the same, 3 if they are not.
 	 * 
 	 * @return The value of the comparison.
 	 */	
 	@Override
 	public int compareTo(Area a) {
 		if(a instanceof InfinitePlotsPlot && a.getSystemID().equalsIgnoreCase(this.getSystemID())) {
 			return 0;
 		}
 
 		return 3;
 	}
 	
 	// ******************************
 	// Removable Interface
 	// ******************************
 	/**
 	 * Permanently removes the area from the data store
 	 * USE CAUTION!
 	 */
 	@Override
 	public void remove() {
  	   Flags.instance.dataStore.write(getDataPath(), (String)null);
 	}
 }
