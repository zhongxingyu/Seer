 
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.status.VirtualMachineErrorCode;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.status.VirtualMachineStatus;
 import org.inria.myriads.snoozecommon.guard.Guard;
 import org.inria.myriads.snoozenode.groupmanager.managerpolicies.placement.PlacementPlan;
 import org.inria.myriads.snoozenode.groupmanager.managerpolicies.placement.PlacementPolicy;
 import org.inria.myriads.snoozenode.util.ManagementUtils;
import org.inria.myriads.snoozenoze.groupmanager.estimator.ResourceDemandEstimator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Implements the round-robin virtual machine placement policy.
  * 
  * @author Eugen Feller
  */
 public final class RandomScheduling implements PlacementPolicy
 {
     /** Define the logger. */
     private static final Logger log_ = LoggerFactory.getLogger(PlacementPolicy.class);
 
     /** Resource demand estimator. */
     private ResourceDemandEstimator estimator_;
 
     /**
      * Constructor.
      * 
      * @param estimator     The estimator
      */
     public RandomScheduling(ResourceDemandEstimator estimator)
     {
         Guard.check(estimator);
         log_.debug("Initializing random virtual machine placement policy");
         estimator_ = estimator;
     }
 
     /**
      * Places a single virtual machine.
      * 
      * @param virtualMachines   The virtual machines
      * @param localControllers  The local controller descriptions
      * @return                  Placement Plan
      */
     public PlacementPlan place(List<VirtualMachineMetaData> virtualMachines,
             List<LocalControllerDescription> localControllers)
     {
         Guard.check(virtualMachines, localControllers);
         log_.debug(String.format("Placing %d virtual machines", virtualMachines.size()));
 
         if (localControllers.size() == 0)
         {
             log_.debug("No local controllers to use for virtual machine placement");
             PlacementPlan placementPlan = new PlacementPlan(localControllers, virtualMachines);
             return placementPlan;
         }
         Map<String, LocalControllerDescription> targetLocalControllers = 
                 new HashMap<String, LocalControllerDescription>();
         List<VirtualMachineMetaData> unassignedVirtualMachines = new ArrayList<VirtualMachineMetaData>();      
 
         int numberOfLocalControllers = localControllers.size();
         
         for (VirtualMachineMetaData virtualMachine : virtualMachines)
         {
             log_.debug(
                     String.format("Placing virtual machine %s", virtualMachine.getVirtualMachineLocation().getVirtualMachineId()));
             String virtualMachineId = virtualMachine.getVirtualMachineLocation().getVirtualMachineId();
             
             Random randomGenerator = new Random();
             int nextLocalControllerIndex = randomGenerator.nextInt(localControllers.size());
             ArrayList<LocalControllerDescription> testedLocalControllers= new ArrayList<LocalControllerDescription>(localControllers.size());
             
             
             boolean isAssigned = false;
             LocalControllerDescription nextLocalController = null ;
 
             while (testedLocalControllers.size()!=numberOfLocalControllers) 
             {
                 while (testedLocalControllers.contains(localControllers.get(nextLocalControllerIndex)))
                 {
                     nextLocalControllerIndex = randomGenerator.nextInt(localControllers.size());    
                 }
                 nextLocalController = localControllers.get(nextLocalControllerIndex);
                 log_.debug(String.format("Random policy picked local controller %s", nextLocalController.getId()));
                 
                 if (estimator_.hasEnoughLocalControllerCapacity(virtualMachine, nextLocalController))
                 {
                     log_.debug(String.format("Local controller %s has enough capacity for virtual machine: %s!",
                             nextLocalController.getId(), 
                             virtualMachineId));
                     
                     nextLocalController.getVirtualMachineMetaData().put(virtualMachineId, virtualMachine);
                     nextLocalController.getAssignedVirtualMachines().add(virtualMachine);
                     String localControllerId = nextLocalController.getId();
                     if (!targetLocalControllers.containsKey(localControllerId))
                     {
                         log_.debug(String.format("Adding local controller %s to the used list", localControllerId));
                         targetLocalControllers.put(localControllerId, nextLocalController);
                     }
                     isAssigned = true;
                     break;
                 }
                 testedLocalControllers.add(nextLocalController);
                 
                 log_.debug(String.format("Local controller %s has not enough capacity for virtual machine: %s!",
                         nextLocalController.getId(), 
                         virtualMachineId));
                 
                 
             }
 
             if (!isAssigned)
             {
                 log_.debug(String.format("No suitable local controller to host the virtual machine: %s", 
                                          virtualMachineId));
                 ManagementUtils.updateVirtualMachineMetaData(virtualMachine, 
                                                              VirtualMachineStatus.ERROR, 
                                                              VirtualMachineErrorCode.NOT_ENOUGH_LOCAL_CONTROLLER_CAPACITY);
                 unassignedVirtualMachines.add(virtualMachine);
             }
             
         }
 
         List<LocalControllerDescription> usedLocalControllers = 
                 new ArrayList<LocalControllerDescription>(targetLocalControllers.values());
         PlacementPlan placementPlan = new PlacementPlan(usedLocalControllers, unassignedVirtualMachines);
         return placementPlan;
     }
 }
