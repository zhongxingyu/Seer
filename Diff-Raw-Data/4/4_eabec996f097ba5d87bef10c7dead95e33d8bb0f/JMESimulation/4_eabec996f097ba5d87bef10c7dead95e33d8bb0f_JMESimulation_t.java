 /**
  * Unified Simulator for Self-Reconfigurable Robots (USSR)
  * (C) University of Southern Denmark 2008
  * This software is distributed under the BSD open-source license.
  * For licensing see the file LICENCE.txt included in the root of the USSR distribution.
  */
 package ussr.physics.jme;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CyclicBarrier;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.lwjgl.opengl.Display;
 
 import ussr.description.Robot;
 import ussr.description.geometry.RotationDescription;
 import ussr.description.geometry.VectorDescription;
 import ussr.description.setup.BoxDescription;
 import ussr.description.setup.ModuleConnection;
 import ussr.description.setup.ModulePosition;
 import ussr.description.setup.WorldDescription;
 import ussr.model.ActBasedController;
 import ussr.model.Connector;
 import ussr.model.Module;
 import ussr.physics.ModuleFactory;
 import ussr.physics.PhysicsFactory;
 import ussr.physics.PhysicsLogger;
 import ussr.physics.PhysicsObserver;
 import ussr.physics.PhysicsParameters;
 import ussr.physics.PhysicsSimulation;
 import ussr.physics.PhysicsSimulationHelper;
 import ussr.physics.TimedPhysicsObserver;
 import ussr.physics.jme.connectors.JMEBasicConnector;
 import ussr.physics.jme.connectors.JMEConnector;
 import ussr.physics.jme.pickers.PhysicsPicker;
 import ussr.physics.jme.pickers.Picker;
 import ussr.util.TopologyWriter;
 import ussr.util.WindowSaver;
 import ussr.visualization.DataDumper;
 import ussr.visualization.VisualizationParameters;
 
 import com.jme.app.AbstractGame;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.input.action.InputAction;
 import com.jme.input.action.InputActionEvent;
 import com.jme.math.Quaternion;
 import com.jme.math.Vector3f;
 import com.jme.scene.TriMesh;
 import com.jme.scene.shape.AxisRods;
 import com.jme.system.DisplaySystem;
 import com.jme.system.GameSettings;
 import com.jme.system.PropertiesGameSettings;
 import com.jmex.physics.DynamicPhysicsNode;
 import com.jmex.physics.Joint;
 import com.jmex.physics.PhysicsNode;
 import com.jmex.physics.PhysicsSpace;
 import com.jmex.physics.contact.ContactCallback;
 import com.jmex.physics.contact.PendingContact;
 import com.jmex.physics.impl.ode.OdePhysicsSpace;
 import com.jmex.physics.impl.ode.geometry.OdeMesh;
 
 /**
  * The physical simulation: initialization and main loop, references to all simulated entities. 
  * 
  * @author Modular Robots @ MMMI
  */
 public class JMESimulation extends JMEBasicGraphicalSimulation implements PhysicsSimulation {
 
     public Map<String, JMEConnector> connectorRegistry = new HashMap<String, JMEConnector>();
     public Set<Joint> dynamicJoints = new HashSet<Joint>();
     Hashtable<String,Robot> robots = new Hashtable<String,Robot>();
     public WorldDescription worldDescription;
     private List<JMEModuleComponent> moduleComponents = new ArrayList<JMEModuleComponent>();
     private List<Module> modules = new ArrayList<Module>();
     private Map<TriMesh,String> geometryMap = new HashMap<TriMesh,String>();
     private ArrayList<Thread> moduleControlThreads = new ArrayList<Thread>();
     
     protected long physicsSteps = 0;
     protected float physicsSimulationStepSize; // Set from ussr.physics.SimulationParameters = 0.005f; // 0.001f  // 0.0005f; //0.001f; // 
     protected float gravity; // Set from ussr.physics.SimulationParameters
     static class Lock extends Object {}
     static public Lock physicsLock = new Lock(); //should be used every time physics space is changed 
    
     protected List<PhysicsNode> obstacleBoxes;
     private JMEGeometryHelper helper = new JMEGeometryHelper(this);
     private JMEFactoryHelper factory;    
     private long mainLoopCounter=0;
     private List<PhysicsObserver> physicsObservers = new CopyOnWriteArrayList<PhysicsObserver>();
     private List<ActBasedController> actControllers = Collections.synchronizedList(new ArrayList<ActBasedController>());
     private PhysicsFactory.Options options;
     private Picker picker;
     
     public CyclicBarrier controlSyncBarrier;
               
     public JMESimulation(ModuleFactory[] factories, PhysicsFactory.Options options) {
         super(options);
         this.options = options; 
         PhysicsParameters parameters = PhysicsParameters.get();
         this.gravity = parameters.getGravity();
        // this.physicsSimulationStepSize = parameters.getPhysicsSimulationStepSize();
         factory = new JMEFactoryHelper(this,factories);
     }
     
     protected void simpleInitGame() {
     	// Create and init data dumping object
     	if(VisualizationParameters.get().getUseDataDumper() == true) {
     		DataDumper dumper = new DataDumper();
     		subscribePhysicsTimestep(dumper);
     		dumper.init(worldDescription.getNumberOfModules());
     	}
     	 // Create underlying plane or terrain
         if(worldDescription.theWorldIsFlat())
           setStaticPlane(helper.createPlane(worldDescription.getPlaneSize(),worldDescription.getPlaneTexture()));
         else
     	  setStaticPlane(helper.createTerrain(worldDescription.getPlaneSize(), worldDescription.getPlaneTexture()));
     	
         if(!options.getHeadless()) createSky(worldDescription);
         setGravity(gravity);
 
         setPhysicsErrorParameters(PhysicsParameters.get().getConstraintForceMix(), PhysicsParameters.get().getErrorReductionParameter()); 
         this.grapFrames = worldDescription.getIsFrameGrabbingActive();
 		
         // Create obstacle boxes
         obstacleBoxes = new ArrayList<PhysicsNode>();
         float obstacleMass = worldDescription.hasHeavyObstacles() ? 100 : 0;
         for(int i=0; i<worldDescription.getObstacles().size();i++)
             obstacleBoxes.add(helper.createBox(0.05f,0.05f,0.05f,obstacleMass,false)); //compute mass from size
         for(int i=0; i<worldDescription.getBigObstacles().size();i++) {
             VectorDescription size = worldDescription.getBigObstacles().get(i).getSize();
             BoxDescription.Heaviness heaviness = worldDescription.getBigObstacles().get(i).getHeaviness();
             boolean isStatic = worldDescription.getBigObstacles().get(i).getIsStatic();
             float mass = worldDescription.getBigObstacles().get(i).getMass();
             if(heaviness==BoxDescription.Heaviness.VERY)
                 obstacleBoxes.add(helper.createBox(size.getX(),size.getY(),size.getZ(),800f,isStatic));
             else if(heaviness==BoxDescription.Heaviness.KINDOF)
                 obstacleBoxes.add(helper.createBox(size.getX(),size.getY(),size.getZ(),mass*10,isStatic));
             else obstacleBoxes.add(helper.createBox(size.getX(),size.getY(),size.getZ(),mass,isStatic));
         }
         
         // Create modules
         for(int i=0; i<worldDescription.getNumberOfModules(); i++) {
             ModulePosition position = worldDescription.getModulePositions().get(i);
             createModule(position,false);
         }
 
         if(actControllers.size()>0) {
             System.out.println("Creating act-based control for: "+actControllers.size());
             Thread actThread = new Thread() {
                 public void run() {
                     for(ActBasedController controller: actControllers)
                         controller.initializationActStep();
                     while(true)
                         for(Iterator<ActBasedController> iterator = actControllers.iterator(); iterator.hasNext(); ) {
                             boolean reschedule = iterator.next().singleActStep();
                             if(!reschedule) iterator.remove();
                         }
                 }
             };
             moduleControlThreads.add(actThread);
             //actThread.start();
         }
         
         //showPhysics = true;
 
         // Reset action and keybinding
         final InputAction resetAction = new InputAction() {
             public void performAction( InputActionEvent evt ) {
                 for(Joint j: dynamicJoints) j.detach();
                 dynamicJoints = new HashSet<Joint>();
                 if(worldDescription.getModulePositions().size()>=0)
                     placeModules();
                 else
                     throw new Error("Module placement broken for random placement");
                 
                 List<VectorDescription> combinedPosList = new LinkedList<VectorDescription>();
                 List<RotationDescription> combinedRotList = new LinkedList<RotationDescription>();
                 
                 for(VectorDescription elm: worldDescription.getObstacles()) {
                 	combinedPosList.add(elm);
                 	combinedRotList.add(new RotationDescription(new Quaternion()));                	
                 }
                 for(BoxDescription elm: worldDescription.getBigObstacles()) {
                 	combinedPosList.add(elm.getPosition());
                 	combinedRotList.add(elm.getRotation());
                 }
                 Iterator<VectorDescription> positions = combinedPosList.iterator();
                 Iterator<RotationDescription> rotations = combinedRotList.iterator();
                 for(PhysicsNode box: obstacleBoxes) {
                 	VectorDescription p = positions.next();
                 	RotationDescription r = rotations.next();
                 	box.getLocalTranslation().set( p.getX(), p.getY(), p.getZ() );
                 	//box.getLocalRotation().set( 0, 0, 0, 1 );
                 	box.getLocalRotation().set(r.getRotation());
                 	if(box instanceof DynamicPhysicsNode) ((DynamicPhysicsNode)box).clearDynamics();
                 }
             }
         };
         if(!options.getHeadless())
             input.addAction( resetAction, InputHandler.DEVICE_KEYBOARD, KeyInput.KEY_R, InputHandler.AXIS_NONE, false );
         resetAction.performAction( null );
 
         // Add any external input handlers
         doAddInputHandlers();
         
         if(!options.getHeadless()) {
             if(picker==null)
                 setPicker(new PhysicsPicker());
             else {
                 Picker current = this.picker;
                 this.picker = null;
                 setPicker(current);
             }
             MouseInput.get().setCursorVisible( true );
         }
         addContactCallback();
     }
 
     public synchronized Module createModule(ModulePosition position, boolean assign) throws Error {
         String robotType;
         robotType = position.getType();
         System.out.println("RobotType:"+robotType.toString());
         Robot robot = robots.get(robotType);
         if(robot==null) throw new Error("No definition for robot "+robotType);
         String module_name = position.getName();
         final Module module = new Module(this);
         module.setProperty("ussr.module.name", module_name);
         factory.createModule(module, robot, module_name);
         module.setController(robot.createController());
         modules.add(module);
         
         if(module.getController() instanceof ActBasedController)
             synchronized(actControllers) {
                 actControllers.add((ActBasedController)module.getController());
                 actControllers.notifyAll();
             }
         else {
             Thread moduleControlThread = new Thread() {
                 public void run() {
                     module.waitForReady();
                     module.getController().activate();
                     if(!isStopped()) {
                         PhysicsLogger.log("Warning: unexpected controller exit");
                     }
                 }
             };
             //moduleThread.setPriority(Thread.NORM_PRIORITY-1);
 
             moduleControlThreads.add(moduleControlThread);
             //moduleControlThread.start();
         }
         
         if(assign) {
             module.assignToModulePosition(position);
             module.setColor(Color.GREEN);
         }
         return module;
     }
     
     private void addContactCallback() {
 		//collision callback that removes? the internalt ODE error 
          //caused by trimeshes colliding with some singularity 
          getPhysicsSpace().getContactCallbacks().add(new ContactCallback(){  
         	 public boolean adjustContact( PendingContact contact ) {
         		 if (contact.getGeometry1() instanceof OdeMesh && contact.getGeometry2() instanceof OdeMesh) {
         			 if(contact.getPenetrationDepth()>0.001) {
         				 //System.out.println("Serious ATRON collision...ignored "+contact.getPenetrationDepth());
         				 contact.setIgnored(true);
         				 return true;
         			 }
         		 }
         		 return false;
         	 };
          });
 	}
 
     public synchronized void setPicker(Picker picker) {
         if(this.picker!=null) this.picker.delete();
         this.picker = picker;
         if(input!=null) picker.attach(this, input, rootNode, getPhysicsSpace());
     }
     
     private void setPhysicsErrorParameters(float cfm, float erp) {
     	if(getPhysicsSpace() instanceof OdePhysicsSpace)  {
 	    	((OdePhysicsSpace)getPhysicsSpace()).getODEJavaWorld().setConstraintForceMix(cfm); //default = 10E-5f, typical = [10E-9, 1]
 			((OdePhysicsSpace)getPhysicsSpace()).getODEJavaWorld().setErrorReductionParameter(erp); //default = 0.2, typical = [0.1,0.8]
     	}
 	}
     long stop, start = System.currentTimeMillis();;
     public final void start() {
     	Logger.getLogger(PhysicsSpace.LOGGER_NAME).setLevel(Level.OFF); //FIXME unable to turn off logger (JME_2.0 still uses LoggingSystem which can not be accessed)
     	//One way to turn off logging is to modify the logging.properties file in the java installation directory 
     	Logger.getLogger("jme").setLevel(Level.OFF);
     	//Logger.getLogger(AbstractGame.class.getName()).setLevel(Level.OFF);
     	try {
             getAttributes();
             //a 
             if (!finished) {
                 if(!options.getHeadless()) {
                     System.out.println("Available Display Modes: ");
                     try {
                         org.lwjgl.opengl.DisplayMode[] modes = Display.getAvailableDisplayModes();
                         for(int i=0;i<modes.length;i++) System.out.println(" Mode "+i+" = "+modes[i]);
                     } catch(UnsatisfiedLinkError err) {
                         throw new Error("Unable to initialize LWJGL, cannot load native library; path = "+System.getProperty("java.library.path"));
                     }
                 }
                 
                 initSystem();
                 assertDisplayCreated();
         		initGame();
                 readWorldParameters();
 
               
                 controlSyncBarrier = new CyclicBarrier(moduleControlThreads.size() + 1/*the sim itself*/, new Runnable() {
                     public void run() { physicsCallBack(); }
                 });                
                 
                 // main loop
                 long realTimeReference = System.currentTimeMillis();
                                
                 //start here the control threads, otherwise they might run for long
                 //before the actual simulation is started by unpausing it
                 for(Thread t: moduleControlThreads)
                 	t.start();
                 
                 while (!finished && !getDisplay().isClosing() && physicsSteps < PhysicsParameters.get().getMaxPhysicsIterations()) {
                 	boolean physicsStep = false;
                     if ( !pause ||singleStep ) {
                     	if(PhysicsParameters.get().syncWithControllers() == false)
                     		physicsCallBack();
                         synchronized(this) {
                             physicsStep(); // 1 call to = 32ms (one example setup)
                             physicsStep = true;
                         }
                         //waitForPhysicsStep(true);
                          
                         //full sync
                         if(PhysicsParameters.get().syncWithControllers()) {
                         	controlSyncBarrier.await();
                         	//physicsCallBack() is performed by the barrier before releasing the threads
                         }
                         //this instead assumes that the module controllers finish faster than the sim step
                         else {
                         	unlockModules();
                         }
                         
                     }
                     
                
             	   
                     if(!options.getHeadless()) KeyInput.get().update();
             	   //if(mainLoopCounter%5==0 ||singleStep) { // 1 call to = 16ms (same example setup)
                     float fps = 25;
                     float loopsPerSecond = 1.0f/getPhysicsSimulationStepSize();
                     int loopsPerUpdate = (int)(loopsPerSecond/fps);
                     if(mainLoopCounter%loopsPerUpdate==0 ||singleStep) { // 1 call to = 16ms (same example setup)
                     	if(!options.getHeadless()) MouseInput.get().update(); //InputSystem.update();	            		   
                 		update(-1.0f);
                 		render(-1.0f);
                 		if(grapFrames) {
 	                    	grapFrame();
 	                    }
                 		getDisplay().getRenderer().displayBackBuffer();// swap buffers
                     }
                     mainLoopCounter++;
 					Thread.yield();
 					if(singleStep&&physicsStep) singleStep = false;
 			
 					if(realtime && !pause) {
 						long realTime = (System.currentTimeMillis()-realTimeReference);
 						long simTime = (long)(1000*getTime());
 						long diffTime = simTime-realTime;
 						if(diffTime>0) {
 							if(diffTime>100) {
 								//System.out.println(" ... Way Ahead "+(simTime-realTime));
 								//System.out.println(" ... Catching down... ");
 								realTimeReference = System.currentTimeMillis()-simTime+100;
 								diffTime = 100;
 							}
 							Thread.sleep(diffTime);
 						}
 						else {
 							if(diffTime<-100) {
 								//System.out.println(" ... Way behind "+(simTime-realTime));
 								//System.out.println(" ... Catching up... ");
 								realTimeReference = System.currentTimeMillis()-simTime-100;
 							}
 						}	
 					}
 					if(pause) Thread.sleep(10);
                 }
             }
         } catch (Throwable t) {
             t.printStackTrace();
         }
         stop();
         cleanup();
      
        
         if (getDisplay() != null)
             getDisplay().reset();
         
         if(options.getSaveWindowSettingOnExit()) {
         	try {
         		Display.setLocation(0,0);
         		
 				WindowSaver.saveSettings();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
         }
         display.close();
         waitForPhysicsStep(true);
        	quit();
     }
     
     public void stop() {
     	finished = true;
     }
     
 	public boolean isStopped() {
 		return finished;
 	}
 	
     private void readWorldParameters() {
         if(worldDescription.getCameraPosition()==WorldDescription.CameraPosition.FAROUT)
             cam.setLocation(cam.getLocation().add(0, 0, 50f));
         else if(worldDescription.getCameraPosition()==WorldDescription.CameraPosition.MIDDLE)
             cam.setLocation(cam.getLocation().add(0,5f,15f));
         else if(!(worldDescription.getCameraPosition()==WorldDescription.CameraPosition.DEFAULT))
             throw new Error("Unknown camera position");
     }
 
     private final void physicsStep() {
     	synchronized(physicsLock) {
     		getPhysicsSpace().update(PhysicsParameters.get().getPhysicsSimulationStepSize());
 	    	physicsSteps++;
 	    	addWorldEffects(); //e.g. damping
     	}
     }
 
     /*
      * To add 'hacked' world effects like damping which can make the simulation a lot more realistic but 
      * which fundamentally is not modeled correctly 
      */
     private void addWorldEffects() {
     	float linVelDamp = PhysicsParameters.get().getWorldDampingLinearVelocity();
     	float angVelDamp = PhysicsParameters.get().getWorldDampingAngularVelocity();
     	if(linVelDamp!=0.0f||angVelDamp!=0.0f) {
     		for(JMEModuleComponent components: getModuleComponents()) {
     			if(angVelDamp!=0.0f) components.getModuleNode().setAngularVelocity(components.getModuleNode().getAngularVelocity(null).multLocal(angVelDamp));
     			if(linVelDamp!=0.0f) components.getModuleNode().setLinearVelocity(components.getModuleNode().getLinearVelocity(null).multLocal(linVelDamp));
             }
     	}
 	}
 
 	public void setRobot(Robot bot) {
     	robots.put("default",bot);
     }
     public void setRobot(Robot bot, String type) {
 		robots.put(type, bot);
 	}
     public void setWorld(WorldDescription world) {
         this.worldDescription = world;        
     }
     /**
      * 
      */
     public void placeModules() {
         Iterator<ModulePosition> positions = this.worldDescription.getModulePositions().iterator();
         Map<String,Module> registry = new HashMap<String,Module>();
         for(Module module: modules) {
             ModulePosition p = positions.next();
             module.assignToModulePosition(p);
             registry.put(p.getName(), module);
         }
         // The following only works for mechanical connectors
         // HARDCODED: assumes one physics per connector
         List<ModuleConnection> connections = this.worldDescription.getConnections();
         TopologyWriter writer = options.getTopologyWriter();
         for(ModuleConnection connection: connections) {
             Module m1 = registry.get(connection.getModule1());
             Module m2 = registry.get(connection.getModule2());
             if(m1==null) throw new Error("Undefined module for connection "+connection.getModule1());
             if(m2==null) throw new Error("Undefined module for connection "+connection.getModule2());
             if(m1.getID()==m2.getID()) {
                 throw new RuntimeException("Module("+m1.getID()+") can not connect to itself("+m2.getID()+")");
             }
             int c1i = connection.getConnector1();
             int c2i = connection.getConnector2();
             if(c1i==-1||c2i==-1) {
                 c1i = helper.findBestConnection(m1,m2);
                 c2i = helper.findBestConnection(m2,m1);
             }
             if(c1i!=-1||c2i!=-1) {
                 Connector c1 = m1.getConnectors().get(c1i);
                 Connector c2 = m2.getConnectors().get(c2i);
                 PhysicsLogger.displayInfo("Connecting "+m1.getProperty("name")+"<"+c1i+":"+c1.getProperty("name")+"> to "+m2.getProperty("name")+"<"+c2i+":"+c2.getProperty("name")+">");
                 writer.addConnection(m1, m2);
                 if((c1.getPhysics().get(0) instanceof JMEBasicConnector)&&(c2.getPhysics().get(0) instanceof JMEBasicConnector)) {
                     JMEBasicConnector jc1 = (JMEBasicConnector)c1.getPhysics().get(0);
                     JMEBasicConnector jc2 = (JMEBasicConnector)c2.getPhysics().get(0);
                     if(jc1.canConnectTo(jc2))
                     	jc1.connectTo(jc2);
                     else if(jc2.canConnectTo(jc1)) {
                     	jc2.connectTo(jc1);
                     }
                     else {
                     	System.err.println("Unable to connect connector "+c1i+" on module "+m1.getID()+" to connector "+c2i+" on module "+m2.getID());
                     }
                 }
                 else {
                     PhysicsLogger.log("Warning: connector initialization ignored");
                 }
             }
         }
         writer.finish();
         for(Module module: modules)
             module.setReady(true);
     }   
 
     public synchronized void associateGeometry(String name, TriMesh shape) {
         geometryMap.put(shape,name);
     }
 
     public String getGeometryName(TriMesh mesh) {
         return geometryMap.get(mesh);
     }
 
     public List<Module> getModules() {
         return modules;
     }
     
     public void setGravity(float g) {
         gravity = g;
         getPhysicsSpace().setDirectionalGravity(new Vector3f(0,gravity,0));
     }
     HashSet<Module> waitingModules = new HashSet<Module>();
     HashSet<Module> startedModules = new HashSet<Module>();
     private PriorityQueue<TimedPhysicsObserver> oneShotObserverQueue = new PriorityQueue<TimedPhysicsObserver>();
     
     public synchronized void waitForPhysicsStep(Module module) {
     	waitingModules.add(module);
     	waitForPhysicsStep(false);
     	waitingModules.remove(module);
     	startedModules.add(module);
     }
     public void unlockModules() {   	
        	int waitingModuleCount =  waitingModules.size();
        	waitForPhysicsStep(true);
     	while(startedModules.size()<waitingModuleCount&&!isStopped()) {
     		//System.out.println(startedModules.size()+" vs "+waitingModuleCount);
     		Thread.yield();
     	}
     	startedModules.clear();
     	while(waitingModules.size()!=waitingModuleCount&&!isStopped()) {
     		Thread.yield();
     	}
     }
     public synchronized void waitForPhysicsStep(boolean notify) {
         if(notify) {
         	notifyAll();
         }
         else { //modules wait here
             try {
                 wait();
                 if(finished) {
                  //   System.out.println("I should stop now "+this);
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void subscribePhysicsTimestep(PhysicsObserver observer) {
         synchronized(physicsObservers) {
             if(observer==null) throw new Error("Null observer added");
             if(physicsObservers.contains(observer)) throw new Error("Duplicate");// System.err.println("Warning - same observer added twize");;
             physicsObservers.add(observer);
         }
     }
     
     public void unsubscribePhysicsTimestep(PhysicsObserver observer) {
         synchronized(physicsObservers) {
             physicsObservers.remove(observer);
         }
     }
 
     private void physicsCallBack() {
         // physicsObservers is guaranteed to copy on write, so we can safely iterate
         final List<PhysicsObserver> observers = physicsObservers;
         // Now iterate through the list
         for(PhysicsObserver observer: observers)
             observer.physicsTimeStepHook(this);
         // Any timed events?
         if(this.oneShotObserverQueue.size()>0) {
             synchronized(this.oneShotObserverQueue) {
                 float currentTime = this.getTime();
                 while(this.oneShotObserverQueue.peek().getTime()<currentTime)
                     this.oneShotObserverQueue.remove().physicsTimeStepHook(this);
             }
         }
     }
 
     public DisplaySystem getDisplay() {
         return display;
     }
 
     public void setModuleComponents(List<JMEModuleComponent> moduleComponents) {
         this.moduleComponents = moduleComponents;
     }
 
     public List<JMEModuleComponent> getModuleComponents() {
         return moduleComponents;
     }
     public List<PhysicsNode> getObstacles() { return obstacleBoxes; }
 
     public PhysicsSimulationHelper getHelper() {
         return helper;
     }
 
     public long getPhysicsSteps() { return physicsSteps; }
 
     public float getPhysicsSimulationStepSize() { return PhysicsParameters.get().getPhysicsSimulationStepSize(); }
 
 	public WorldDescription getWorldDescription() {
 		return worldDescription;
 	}
 
 	public void setModules(List<Module> modules) {
 		this.modules = modules;
 	}
 
     public List<VectorDescription> getObstaclePositions() {
         ArrayList<VectorDescription> positions = new ArrayList<VectorDescription>();
         for(PhysicsNode node: this.obstacleBoxes) {
             Vector3f pos = node.getLocalTranslation();
             positions.add(new VectorDescription(pos.x,pos.y,pos.z));
         }
         return positions;
     }
 
 	protected GameSettings getNewSettings() {
 		PropertiesGameSettings setting = new PropertiesGameSettings("properties.cfg");
 		setting.load();
 		setting.setFrequency(50);
 		return setting;
 	}
 
     public void waitForPhysicsTimestep(TimedPhysicsObserver observer) {
         synchronized(oneShotObserverQueue) {
             oneShotObserverQueue.add(observer);
         }
     }
    
    public  void moveDisplay(int displayX, int displayY){
    	display.moveWindowTo(displayX, displayY);
    }
 
 }
 
