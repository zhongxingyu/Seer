 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.easymock.EasyMock;
 import org.inria.myriads.snoozecommon.communication.localcontroller.LocalControllerDescription;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.VirtualMachineMetaData;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.status.VirtualMachineErrorCode;
 import org.inria.myriads.snoozecommon.communication.virtualcluster.status.VirtualMachineStatus;
 import org.inria.myriads.snoozenode.groupmanager.managerpolicies.placement.PlacementPlan;
 import org.inria.myriads.snoozenode.groupmanager.managerpolicies.placement.PlacementPolicy;
import org.inria.myriads.snoozenoze.groupmanager.estimator.ResourceDemandEstimator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import junit.framework.TestCase;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 
 /**
  * @author msimonin
  *
  */
 public class TestRandomScheduling  extends TestCase 
 {
     /** Logger. */
     private static final Logger log_ = LoggerFactory.getLogger(TestRandomScheduling.class);
     
     @Override
     protected void setUp() throws Exception 
     {
         System.getProperties().put("org.restlet.engine.loggerFacadeClass", 
                 "org.restlet.ext.slf4j.Slf4jLoggerFacade");
         String logFile = "./configs/log4j.xml";
         File file = new File(logFile);
         if (file.exists() && file.canRead()) 
         {   
             DOMConfigurator.configure(logFile);
             System.out.println("Log configuration loaded");
         } 
         else
         {
             System.out.println("Log file " + logFile + " does not exist or is not readable! Falling back to default!");
             BasicConfigurator.configure();
         }
     }
         
     
     /**
      * No local controller.
      *  
      * All virtual machines must be unassigned
      */
     public void testNoLocalController()
     {
         int numberOfVirtualMachines = 10;
         ArrayList<VirtualMachineMetaData> virtualMachines = new ArrayList<VirtualMachineMetaData>();
         for (int i = 0; i<numberOfVirtualMachines; i++ )
         {
             VirtualMachineMetaData virtualMachine = new VirtualMachineMetaData();
             virtualMachines.add(virtualMachine);
         }
         ResourceDemandEstimator estimator = EasyMock.createMock(ResourceDemandEstimator.class);
         
         PlacementPolicy randomScheduling = new RandomScheduling(estimator);
         PlacementPlan placementPlan = randomScheduling.place(virtualMachines, new ArrayList<LocalControllerDescription>());
         
         assertEquals(numberOfVirtualMachines, placementPlan.gettUnassignedVirtualMachines().size());
         assertEquals(0, placementPlan.getLocalControllers().size());
         assertEquals(virtualMachines, placementPlan.gettUnassignedVirtualMachines());
     }
     
     /**
      * 
      * Local controllers don't have enough capacity.
      * 
      * All virtual machines must be unassigned. 
      * Error code must set to NOT_ENOUGH_LOCAL_CONTROLLER_CAPACITY. 
      * 
      */
     public void testNotEnoughCapacity()
     {
         ResourceDemandEstimator estimator = EasyMock.createMock(ResourceDemandEstimator.class);
         int numberOfLocalControllers = 2;
         ArrayList<LocalControllerDescription> localControllers = new ArrayList<LocalControllerDescription>();
         for (int i = 0; i<numberOfLocalControllers; i++ )
         {
             LocalControllerDescription localController = new LocalControllerDescription();
             localController.setId("lc"+String.valueOf(i));
             localControllers.add(localController);
         }
         
         
         int numberOfVirtualMachines = 10;
         ArrayList<VirtualMachineMetaData> virtualMachines = new ArrayList<VirtualMachineMetaData>();
         for (int i = 0; i<numberOfVirtualMachines; i++ )
         {
             VirtualMachineMetaData virtualMachine = new VirtualMachineMetaData();
             virtualMachine.getVirtualMachineLocation().setVirtualMachineId("vm"+String.valueOf(i));
             virtualMachines.add(virtualMachine);
             //mock 
             for (int j = 0; j<numberOfLocalControllers; j++ )
             {
                 expect(estimator
                         .hasEnoughLocalControllerCapacity(virtualMachine, localControllers.get(j)))
                         .andReturn(false)
                         .anyTimes();
             }
         }
         EasyMock.replay(estimator);
         PlacementPolicy randomScheduling = new RandomScheduling(estimator);
         PlacementPlan placementPlan = randomScheduling.place(virtualMachines, localControllers);
         
         assertEquals(numberOfVirtualMachines, placementPlan.gettUnassignedVirtualMachines().size());
         assertEquals(0, placementPlan.getLocalControllers().size());
         assertEquals(virtualMachines, placementPlan.gettUnassignedVirtualMachines());
         for (int i=0; i < placementPlan.gettUnassignedVirtualMachines().size(); i++)
         {
             VirtualMachineMetaData virtualMachine = placementPlan.gettUnassignedVirtualMachines().get(i);
             assertEquals(VirtualMachineStatus.ERROR, virtualMachine.getStatus());
             assertEquals(VirtualMachineErrorCode.NOT_ENOUGH_LOCAL_CONTROLLER_CAPACITY, virtualMachine.getErrorCode());
         }
     }
     
     /**
      * 
      * Local controllers have enough capacity.
      * 
      * All virtual machines must be assigned. 
      * 
      */
     public void testEnoughCapacity()
     {
         ResourceDemandEstimator estimator = EasyMock.createMock(ResourceDemandEstimator.class);
         int numberOfLocalControllers = 2;
         ArrayList<LocalControllerDescription> localControllers = new ArrayList<LocalControllerDescription>();
         for (int i = 0; i<numberOfLocalControllers; i++ )
         {
             LocalControllerDescription localController = new LocalControllerDescription();
             localController.setId("lc"+String.valueOf(i));
             localControllers.add(localController);
         }
         
         int numberOfVirtualMachines = 10;
         ArrayList<VirtualMachineMetaData> virtualMachines = new ArrayList<VirtualMachineMetaData>();
         for (int i = 0; i<numberOfVirtualMachines; i++ )
         {
             VirtualMachineMetaData virtualMachine = new VirtualMachineMetaData();
             virtualMachine.getVirtualMachineLocation().setVirtualMachineId("vm"+String.valueOf(i));
             virtualMachines.add(virtualMachine);
             //mock 
             for (int j = 0; j<numberOfLocalControllers; j++ )
             {
                 expect(estimator
                         .hasEnoughLocalControllerCapacity(virtualMachine, localControllers.get(j)))
                         .andReturn(true)
                         .anyTimes();
             }
         }
         EasyMock.replay(estimator);
         
         PlacementPolicy randomScheduling = new RandomScheduling(estimator);
         PlacementPlan placementPlan = randomScheduling.place(virtualMachines, localControllers);
         assertEquals(0, placementPlan.gettUnassignedVirtualMachines().size());
     }
     
     /**
      * 
      * Local controllers have enough capacity.
      * 
      * 
      */
     public void testMixedCapacity()
     {
         ResourceDemandEstimator estimator = EasyMock.createMock(ResourceDemandEstimator.class);
         int numberOfLocalControllers = 2;
         ArrayList<LocalControllerDescription> localControllers = new ArrayList<LocalControllerDescription>();
         for (int i = 0; i<numberOfLocalControllers; i++ )
         {
             LocalControllerDescription localController = new LocalControllerDescription();
             localController.setId("lc"+String.valueOf(i));
             localControllers.add(localController);
         }
         
         int numberOfVirtualMachines = 10;
         ArrayList<VirtualMachineMetaData> virtualMachines = new ArrayList<VirtualMachineMetaData>();
         for (int i = 0; i<numberOfVirtualMachines; i++ )
         {
             VirtualMachineMetaData virtualMachine = new VirtualMachineMetaData();
             virtualMachine.getVirtualMachineLocation().setVirtualMachineId("vm"+String.valueOf(i));
             virtualMachines.add(virtualMachine);
             //mock 
             for (int j = 0; j<numberOfLocalControllers; j++ )
             {
                 if (i % 2 == 0)
                 {
                     expect(estimator
                             .hasEnoughLocalControllerCapacity(virtualMachine, localControllers.get(j)))
                             .andReturn(true)
                             .anyTimes();
                 }
                 else
                 {
                     expect(estimator
                             .hasEnoughLocalControllerCapacity(virtualMachine, localControllers.get(j)))
                             .andReturn(false)
                             .anyTimes();
                 }
             }
         }
         EasyMock.replay(estimator);
         
         PlacementPolicy randomScheduling = new RandomScheduling(estimator);
         PlacementPlan placementPlan = randomScheduling.place(virtualMachines, localControllers);
         assertEquals(numberOfVirtualMachines / 2, placementPlan.gettUnassignedVirtualMachines().size());
     }
 }
