 package jadex.agent;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import robot.ExploreRobot;
 import data.BlobfinderBlob;
 import data.Board;
 import data.BoardObject;
 import data.Host;
 import data.Position;
 import device.Device;
 import device.DeviceNode;
 import device.IBlobfinderListener;
 import device.IDevice;
 import jadex.bridge.*;
 import jadex.micro.MicroAgentMetaInfo;
 import jadex.service.HelloService;
 import jadex.service.ReceiveNewGoalService;
 import jadex.service.SendPositionService;
 
 public class ExploreAgent extends WallfollowAgent
 {	
     /** Services */
     ReceiveNewGoalService gs;
 
     /** Data */
     Board bb;
 
     @Override public void agentCreated()
 	{
 	    hs = new HelloService(getExternalAccess());
         ps = new SendPositionService(getExternalAccess());
         gs = new ReceiveNewGoalService(getExternalAccess());
 
         addDirectService(hs);
         addDirectService(ps);
         addDirectService(gs);
 
         String host = (String)getArgument("host");
         Integer port = (Integer)getArgument("port");
         Integer robotIdx = (Integer)getArgument("index");
         Integer devIdx = (Integer)getArgument("devIndex");
         Boolean hasLaser = (Boolean)getArgument("laser");
         
         /** Device list */
         CopyOnWriteArrayList<Device> devList = new CopyOnWriteArrayList<Device>();
         devList.add( new Device(IDevice.DEVICE_POSITION2D_CODE,host,port,devIdx) );
         devList.add( new Device(IDevice.DEVICE_RANGER_CODE,host,port,devIdx) );
         devList.add( new Device(IDevice.DEVICE_SONAR_CODE,host,port,devIdx) );
         devList.add( new Device(IDevice.DEVICE_BLOBFINDER_CODE,host,port,devIdx) );
         devList.add( new Device(IDevice.DEVICE_SIMULATION_CODE,host,port,-1) );
         devList.add( new Device(IDevice.DEVICE_PLANNER_CODE,host,port+1,devIdx) );
         devList.add( new Device(IDevice.DEVICE_LOCALIZE_CODE,host,port+1,devIdx) );
         
         if (hasLaser == true)
             devList.add( new Device(IDevice.DEVICE_RANGER_CODE,host,port,devIdx+1));
        
         /** Host list */
         CopyOnWriteArrayList<Host> hostList = new CopyOnWriteArrayList<Host>();
         hostList.add(new Host(host,port));
         hostList.add(new Host(host,port+1));
 
         /** Get the device node */
         setDeviceNode( new DeviceNode(hostList.toArray(new Host[hostList.size()]), devList.toArray(new Device[devList.size()])));
         getDeviceNode().runThreaded();
 
         setRobot( new ExploreRobot(getDeviceNode().getDeviceListArray()) );
         getRobot().setRobotId("r"+robotIdx);
        
         /**
          *  Check if a particular position is set
          */
         Position setPose = new Position(
                 (Double)getArgument("X"),
                 (Double)getArgument("Y"),
                 (Double)getArgument("Angle"));
         
         if ( setPose.equals(new Position(0,0,0)) == false )
             getRobot().setPosition(setPose);
         
         bb = new Board();
         
         sendHello();
 	}
 	
 	/**
      * @see jadex.agent.WallfollowAgent#executeBody()
      */
     @Override public void executeBody()
     {    
         super.executeBody();
         
         /**
          * Register to Blobfinder device
          */
         if (getRobot().getBloFi() != null)
         {
             scheduleStep(new IComponentStep()
             {
                 public Object execute(IInternalAccess ia)
                 {
                     getRobot().getBloFi().addBlobListener(new IBlobfinderListener()
                     {
                         @Override public void newBlobFound(BlobfinderBlob newBlob)
                         {
                             /** Board object */
                             if (bb.getObject(newBlob.getColorString()) == null)
                             {
                                Position blobPose = new Position(newBlob.getRange(),0,newBlob.getAngle(Math.PI/2,80));
                                Position globPose = blobPose.getCartesianCoordinates().getGlobalCoordinates(getRobot().getPosition()); 
 
                                 BoardObject bo = new BoardObject();
                                 bo.setTopic(""+newBlob.getClass());
                                 bo.setPosition(globPose);
                                 bo.setTimeout(10000);
 
                                 bb.addObject(newBlob.getColorString(), bo);
                                 sendBlobInfo(bo);
                             }
                         }
                     });
                     return null;
                 }
             });
         }
     }
     /**
      * Send a new goal locating a Boardobject.
      * @param bo The board object to be found.
      */
     void sendBlobInfo(BoardObject bo)
     {
         getReceiveNewGoalService().send(""+getComponentIdentifier(), "collectGoal", bo.getPosition());
     }
 
     public ReceiveNewGoalService getReceiveNewGoalService() { return gs; }
 
     public static MicroAgentMetaInfo getMetaInfo()
     {
         IArgument[] args = {
                 new Argument("host", "Player", "String", "localhost"),
                 new Argument("port", "Player", "Integer", new Integer(6665)),
                 new Argument("index", "Robot index", "Integer", new Integer(0)),
                 new Argument("devIndex", "Device index", "Integer", new Integer(0)),
                 new Argument("X", "Meter", "Double", new Double(0.0)),
                 new Argument("Y", "Meter", "Double", new Double(0.0)),
                 new Argument("Angle", "Degree", "Double", new Double(0.0)),
                 new Argument("laser", "Laser ranger", "Boolean", new Boolean(true)),
                 new Argument("localize", "Localize device", "Boolean", new Boolean(true))
         };
         
         return new MicroAgentMetaInfo("This agent starts up an explore agent.", null, args, null);
     }
 }
