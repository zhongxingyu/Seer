 package robot;
 
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import data.Host;
 import data.Position;
 import de.unihamburg.informatik.tams.project.communication.exploration.Exploration.RobotState;
 import de.unihamburg.informatik.tams.project.communication.network.CommunicationFactory;
 import device.Device;
 import device.DeviceNode;
 import device.Localize;
 import device.external.IDevice;
 import device.external.ILocalizeListener;
 
 
 public class Starter {
 
 	PatrolRobot robot = null;
 	String host = "localhost";
 	Integer port = 6665;
 	Integer robotIdx = 0;
   Boolean hasLaser = true;
   Boolean hasSimu = true;
   Integer devIdx = 0;
   Boolean hasGripper = true;
   long lastCalled = System.currentTimeMillis();
 	
 	public static void main(String[] args) {
 		Starter start = new Starter();
 		CommunicationFactory cf = new CommunicationFactory();
		cf.startMasterMap(null);
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		start.initialize();
 		start.run();
 	}
 
 
 	private void run() {
 		while(true) {
 			if(Math.abs(lastCalled - System.currentTimeMillis()) > 1000) {
 				robot.doStep();
 				lastCalled = System.currentTimeMillis();
 			}
 		}
 	}
 
 		
 	private void initialize() {
 		/** Device list */
 		CopyOnWriteArrayList<Device> devList = new CopyOnWriteArrayList<Device>();
 		devList.add( new Device(IDevice.DEVICE_POSITION2D_CODE,host,port,devIdx) );
 		if (hasSimu == true)
 			devList.add( new Device(IDevice.DEVICE_SIMULATION_CODE,null,-1,-1) );
         
 		devList.add( new Device(IDevice.DEVICE_PLANNER_CODE,host,port+1,devIdx) );
 		devList.add( new Device(IDevice.DEVICE_LOCALIZE_CODE,host,port+1,devIdx) );
 
 		if (hasLaser == true)
 			devList.add( new Device(IDevice.DEVICE_RANGER_CODE,host,port,-1));
 		
 		if (hasGripper == true)
 			devList.add( new Device(IDevice.DEVICE_GRIPPER_CODE,host,port,-1));
 
 		/** Host list */
 		CopyOnWriteArrayList<Host> hostList = new CopyOnWriteArrayList<Host>();
 		hostList.add(new Host(host,port));
 		hostList.add(new Host(host,port+1));
         
 		/** Get the device node */
 		DeviceNode devNode = new DeviceNode(hostList.toArray(new Host[hostList.size()]), devList.toArray(new Device[devList.size()]));
 		devNode.runThreaded();
 
 		robot = new AntRobot(devNode.getDeviceListArray());
 		System.out.println("Robot erstellt");
 		robot.setRobotId("r"+robotIdx);
 		robot.setState(RobotState.NEEDS_NEW_GOAL);
 	}
 }
