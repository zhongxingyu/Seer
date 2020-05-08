 package ussr.physics.jme.robots;
 
 import java.awt.Color;
 
 import ussr.comm.TransmissionType;
 import ussr.model.Module;
 import ussr.model.Sensor;
 import ussr.physics.ModuleFactory;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.jme.JMEGeometryHelper;
 import ussr.physics.jme.JMEModuleComponent;
 import ussr.physics.jme.JMESimulation;
 import ussr.physics.jme.connectors.JMEConnectorAligner;
 import ussr.physics.jme.sensors.JMETiltSensor;
 import ussr.robotbuildingblocks.BoxShape;
 import ussr.robotbuildingblocks.ReceivingDevice;
 import ussr.robotbuildingblocks.Robot;
 import ussr.robotbuildingblocks.RotationDescription;
 import ussr.robotbuildingblocks.TransmissionDevice;
 
 import com.jme.math.Quaternion;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jmex.physics.DynamicPhysicsNode;
 import com.jmex.physics.contact.MutableContactInfo;
 import com.jmex.physics.material.Material;
 
 public class JMEWhiteFactory implements ModuleFactory {
     private JMESimulation simulation;
     float pi = (float)Math.PI;
     
   //create ATRON
     public void createModule(int module_id, Module module, Robot robot, String module_name) {
         if(!robot.getDescription().getType().startsWith("White")) throw new Error("Illegal module type: "+robot.getDescription().getType());
         
         if(robot.getDescription().getModuleGeometry().size()!=1) throw new RuntimeException("Not an White");
         
         createModuleComponents(module, robot, module_id, module_name);
         
         JMEModuleComponent moduleComponent = (JMEModuleComponent)module.getComponent(0);
 
         setMass(0.2f,moduleComponent.getModuleNode()); //200 grams?
         
         setMaterials(module, robot,module_name);
         addConnectors(module);
         addCommunication(module);
         addTiltSensors(module,moduleComponent.getModuleNode());
      }
     
     private void createModuleComponents(Module module, Robot robot, int module_id, String module_name) {
         BoxShape moduleShape = (BoxShape) robot.getDescription().getModuleGeometry().get(0);
         DynamicPhysicsNode moduleNode = simulation.getPhysicsSpace().createDynamicNode();
         JMEModuleComponent moduleComponent = new JMEModuleComponent(simulation,robot,moduleShape,module_name+"_module#"+Integer.toString(module_id)+".north",module,moduleNode);
         moduleNode.setName("WhiteModule");
         module.addComponent(moduleComponent);
         simulation.getModuleComponents().add(moduleComponent);
 	}
     
 	private void setMaterials(Module module, Robot robot, String module_name) {
     	DynamicPhysicsNode moduleNode = ((JMEModuleComponent) module.getComponent(0)).getModuleNode();
     	moduleNode.setMaterial(Material.ICE);
 		
     	//moduleNode.getMaterial().putContactHandlingDetails(simulation.getStaticPlane().getMaterial(), getDefaultATRONContactDetails());
 		
 	}
 	private MutableContactInfo getDefaultATRONContactDetails() {
 		MutableContactInfo contactDetails = new MutableContactInfo();
 		contactDetails.setBounce( 0.01f );
 		contactDetails.setMu(1f );
 		contactDetails.setMuOrthogonal(1f);
 		contactDetails.setMinimumBounceVelocity(100);
 		contactDetails.setSlip(new Vector2f(0.01f,0.01f));
 		//contactDetails.setSlip(new Vector2f(0.0f,0.0f));
 		//contactDetails.setSlip(new Vector2f( 0.001f, 0.001f));
 		//contactDetails.setSlip(new Vector2f(100f,100f));
 		return contactDetails; 
 	}
 	private void addTiltSensors(Module module, DynamicPhysicsNode node) {
     	// Tilt sensors - NOTE: rotation is probably not correct, but is meant to be inverse of connector rotation
     	 JMETiltSensor xsensor = new JMETiltSensor(simulation, "TiltSensor:x", 'x', node, new RotationDescription(0,-pi,-pi/4));
          JMETiltSensor ysensor = new JMETiltSensor(simulation, "TiltSensor:y", 'y', node, new RotationDescription(0,-pi,-pi/4));
          JMETiltSensor zsensor = new JMETiltSensor(simulation, "TiltSensor:z", 'z', node, new RotationDescription(0,-pi,-pi/4));
          module.addSensor(new Sensor(xsensor));
          module.addSensor(new Sensor(ysensor));
          module.addSensor(new Sensor(zsensor));		
 	}
 
 	private void setMass(float mass, DynamicPhysicsNode node) {
		float density = mass*1f/node.getMass();
     	node.getMaterial().setDensity(density);
    	node.computeMass();
     	if(Math.abs((node.getMass()-mass))>mass/100) setMass(mass, node); //why oh why is this nesseary???
 	}
 
 	private void addCommunication(Module module) {
     	TransmissionDevice atronTrans = new TransmissionDevice(TransmissionType.IR,0.05f);
         ReceivingDevice atronRec = new ReceivingDevice(TransmissionType.IR,10);
         for(int channel=0;channel<4;channel++) {
             module.addTransmissionDevice(JMEGeometryHelper.createTransmitter(module, module.getConnectors().get(channel),atronTrans));
             module.addReceivingDevice(JMEGeometryHelper.createReceiver(module, module.getConnectors().get(channel),atronRec));
         }		
 	}
 
 
 	private void addConnectors(Module module) {
         float unit = (float) 0.1f;
         float maxAlginmentForce = 0.1f;
         float maxAlignmentDistance = 0.1f;
         float epsilonAlignmentDistance = 0.01f;
         Color[] colors = new Color[]{Color.black,Color.white,Color.black,Color.white};
         Vector3f[] cPos = new Vector3f[]{new Vector3f(unit,0,unit),new Vector3f(unit,0,-unit),new Vector3f(-unit,0,-unit),new Vector3f(-unit,0,unit)};
         JMEModuleComponent moduleComponent =  (JMEModuleComponent) module.getComponent(0);
         for(int i=0;i<4;i++) {
         	moduleComponent.addConnector("Connector "+i+" on Module "+module.getID(),cPos[i],colors[i],new Quaternion(new float[]{pi/2,0,0}));
         	moduleComponent.getConnector(i).getConnectorAligner().addAlignmentPoint(cPos[i].add(new Vector3f(0,unit,0)), 1, 0, maxAlginmentForce, maxAlignmentDistance,epsilonAlignmentDistance);
         	moduleComponent.getConnector(i).getConnectorAligner().addAlignmentPoint(cPos[i].add(new Vector3f(0,-unit,0)), 2, 0, maxAlginmentForce,maxAlignmentDistance,epsilonAlignmentDistance);
         	moduleComponent.getConnector(i).setTimeToConnect(3.0f); //FIXME not a good model instead use a criteria (connect when distance < epsilon)
         	moduleComponent.getConnector(i).setTimeToDisconnect(0.0f);
         }
          
         /*moduleComponent.addConnector("Connector 1 on Module "+module.getID(), ,Color.white,new Quaternion(new float[]{pi/2,0,0})); 
         moduleComponent.addConnector("Connector 2 on Module "+module.getID(), ,Color.black, new Quaternion(new float[]{pi/2,0,0})); 
         moduleComponent.addConnector("Connector 3 on Module "+module.getID(), ,Color.white, new Quaternion(new float[]{pi/2,0,0}));*/
         
         /*((JMEHingeMechanicalConnector)moduleComponent.getConnector(0)).setLimits(-3.14f,0f);
         ((JMEHingeMechanicalConnector)moduleComponent.getConnector(1)).setLimits(-3.14f,0f);
         ((JMEHingeMechanicalConnector)moduleComponent.getConnector(2)).setLimits(-3.14f,0f);
         ((JMEHingeMechanicalConnector)moduleComponent.getConnector(3)).setLimits(-3.14f,0f);*/
 	}
 
 	public String getModulePrefix() {
         return "White";
     }
 
     public void setSimulation(PhysicsSimulation simulation) {
     	this.simulation = (JMESimulation)simulation;
     }
 }
