 package be.kuleuven.cs.gridlock.dmas.coordination.delegate;
 
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.model.IExplorationContext;
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.model.exploration.ExplorationAnt;
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.pheromones.IPheromone;
 import be.kuleuven.cs.gridlock.simulation.api.InfrastructureReference;
 import be.kuleuven.cs.gridlock.simulation.model.charging.station.IChargingStationReference;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Interface representing Dmas components, ie. charging stations or other
  * infrastructure elements.
  *
  * @author Kristof Coninx <kristof.coninx at student.kuleuven.be>
  */
 public interface IDMASModelComponent {
 
     /**
     * Default getter for infrastructure reference
      *
     * @return
      */
     InfrastructureReference getInfrastructureReference();
 
     /**
     * Default getter for delegated station instance
      *
     * @return
      */
     IChargingStationReference getStationDelegate();
 
     /**
     * Drop a registration pheromone
      *
     * @param phero
      */
     void dropRegistration(IPheromone phero);
 
     /**
     * Get all registrations
      *
     * @return
      */
     Set<IPheromone> getRegistrations();
 
     /**
     * Evaporate all information
      *
     * @param timespan
      */
     void evaporateDelegate(double timespan);
 
     /**
      * Explore this infrastructure node.
      *
     * @param ants
     * @param context
      */
     void exploreNode(List<ExplorationAnt> ants, IExplorationContext context);
 }
