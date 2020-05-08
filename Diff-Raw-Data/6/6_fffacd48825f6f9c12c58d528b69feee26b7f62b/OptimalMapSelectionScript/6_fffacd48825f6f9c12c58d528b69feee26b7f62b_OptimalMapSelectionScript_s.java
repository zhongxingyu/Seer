 package chalmers.dax021308.ecosystem.controller.scripting;
 
 import java.beans.PropertyChangeEvent;
 import java.util.Map;
 
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 import chalmers.dax021308.ecosystem.model.environment.SimulationSettings;
 import chalmers.dax021308.ecosystem.model.environment.mapeditor.SimulationMap;
 
 /**
  * Script to run for selecting the optimal or inverse-optimal map for the given population.
  * 
  * @author Erik Ramqvist
  *
  */
 public class OptimalMapSelectionScript implements IScript {
 	
 	private Map<Long, SimulationMap> bestMaps;
 	private Map<Long, SimulationMap> worstMaps;
 	private EcoWorld e;
 	
 
 	@Override
 	public void init(EcoWorld e) {
 		this.e = e;
 		e.addObserver(this);
 		SimulationSettings s = SimulationSettings.DEFAULT;
 		e.loadSimulationSettings(s);
 		e.start();
 	}
 
 	@Override
 	public void onFinishOneRun() {
 		//Handle data
 		SimulationSettings s = SimulationSettings.DEFAULT;
 		e.loadSimulationSettings(s);
 		e.start();
 	}
 
 	@Override
 	public void postRun() {
 		
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		
 	}
 	
 	@Override
 	public String getName() {
 		return "Optimal Map Selection Script";
 	} 
 	
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 }
