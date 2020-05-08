 /**
  * Uniform Simulator for Self-reconfigurable (modular) Robots
  * 
  * (C) 2006 University of Southern Denmark
  */
 package ussr.samples;
 
 import java.util.logging.Level;
 import ussr.model.Controller;
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsLogger;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.jme.JMESimulation;
 import ussr.robotbuildingblocks.GeometryDescription;
 import ussr.robotbuildingblocks.Robot;
 import ussr.robotbuildingblocks.RotationDescription;
 import ussr.robotbuildingblocks.SphereShape;
 import ussr.robotbuildingblocks.VectorDescription;
 import ussr.robotbuildingblocks.WorldDescription;
 
 import com.jme.math.Vector3f;
 import com.jme.util.LoggingSystem;
 
 /**
  * A simple simulation with the sticky bot, using the key "Z" to globally toggle stickiness
  * of the connectors.
  * 
  * @author ups
  *
  */
 public class ATRONSimulation1 extends GenericSimulation {
     
     public static void main( String[] args ) {
        setConnectorsAreActive(true);
         WorldDescription world = new WorldDescription();
         world.setPlaneSize(42);
         float quart = (float)(0.5*Math.PI);
         float eigth = (float)(0.25*Math.PI);
         float unit = 1.87f;
         RotationDescription rotation_NS = new RotationDescription(0,0,0);
         RotationDescription rotation_UD = new RotationDescription(quart,eigth,0);
         RotationDescription rotation_EW = new RotationDescription(new VectorDescription(eigth,0,0),new VectorDescription(0,quart,0));
         world.setModulePositions(new WorldDescription.ModulePosition[] {
                 new WorldDescription.ModulePosition("leftleg",new VectorDescription(0,0,0), rotation_EW),
                 new WorldDescription.ModulePosition("middle",new VectorDescription(unit,unit,0), rotation_UD),
                 new WorldDescription.ModulePosition("rightleg",new VectorDescription(2*unit,0,0), rotation_EW)
         });
         world.setModuleConnections(new WorldDescription.Connection[] {
                 new WorldDescription.Connection("leftleg",0,"middle",6),
                 new WorldDescription.Connection("rightleg",4,"middle",2)
         });
         new ATRONSimulation1().runSimulation(world,true);
     }
 
     @Override
     protected Robot getRobot() {
         return new ATRON() {
             public Controller createController() {
                 return new ATRONSampleController1();
             }
             
         };
     }
 
  
 
 }
