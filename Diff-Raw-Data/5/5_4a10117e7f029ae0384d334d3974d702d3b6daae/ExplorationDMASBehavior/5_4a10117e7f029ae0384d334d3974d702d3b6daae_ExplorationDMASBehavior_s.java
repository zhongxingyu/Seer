 package be.kuleuven.cs.gridlock.dmas.coordination.vehicle.behaviors;
 
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.model.IDMASModelAPI;
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.model.exploration.NoRoutePossibleException;
 import be.kuleuven.cs.gridlock.dmas.coordination.delegate.util.Itinerary;
 import be.kuleuven.cs.gridlock.dmas.coordination.vehicle.elec.behavioral.IVehicleContext;
 import be.kuleuven.cs.gridlock.simulation.api.NodeReference;
 import be.kuleuven.cs.gridlock.simulation.api.VirtualTime;
 
 /**
  * Reference behavior to compare DMAS solution with.
  * Behavior chooses the least occupied station within range.
  * @author Kristof Coninx <kristof.coninx at student.kuleuven.be>
  */
 public class ExplorationDMASBehavior implements IElecVehicleBehavior {
 
     private long timeoutcounter;
     private int period;
     private IDMASModelAPI api;
 
     public ExplorationDMASBehavior(int period, IDMASModelAPI api) {
         this.period = period;
         this.timeoutcounter = 0;
         this.api = api;
     }
 
     @Override
     public void executeBehavior(VirtualTime currentTime, double timeFrameDuration, IVehicleContext context) {
         //if (timeoutcounter++ % period == 0) {
         if (context.isRouteStale()) {
             if (context.getCurrentPosition() != null) //TODO Sanitize test.
             {
                 doExplorationStep(context, currentTime);
             }
         }
     }
 
     private void doExplorationStep(IVehicleContext context, VirtualTime currentTime) {
         //find optimal sequence of stations.
         //Collection<NodeReference> stations = context.getStationManager().findAllStationsWithinRange(context.getCurrentPosition(), context.getEV().getActionRadius(), 
         //                                                              context.getPassedNodes().toArray(new NodeReference[context.getPassedNodes().size()]));
         //float ar = context.getEV().getActionRadius() / 1000; //convert m to km.
         //float distance = (float) context.calculateRouteReference(context.getCurrentPosition()).resolveDistance(context.getGraphReference());
        if (true /*|| distance > ar*/) {//TODO check influence.
             Itinerary<NodeReference, VirtualTime> sequence;
             try {
                 sequence = api.getSequenceOfStations(context, currentTime);
                 context.setItinerary(sequence);
             } catch (NoRoutePossibleException ex) {
                 //Logger.getLogger(ExplorationDMASBehavior.class.getName()).log(Level.INFO, "No route possible for vehicle{0}", context.getEV().getVehicleEntity().getVehicleReference());
             }
        }
     }
 }
