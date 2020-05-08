 package robot;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import data.Host;
 import de.unihamburg.informatik.tams.project.communication.exploration.Grid;
 import de.unihamburg.informatik.tams.project.communication.exploration.GridPosition;
 import de.unihamburg.informatik.tams.project.communication.network.CommunicationFactory;
 import device.Device;
 import device.DeviceNode;
 import device.external.IDevice;
 
 public class AntRobotTest {
 
 	static AntRobot robot;
 	static String host = "localhost";
 	static Integer port = 6665;
 	static Integer robotIdx = 0;
   static Boolean hasLaser = true;
   static Boolean hasSimu = true;
   static Integer devIdx = 0;
   static Boolean hasGripper = true;
   static GridPosition north;
   static GridPosition south;
   static GridPosition west;
   static GridPosition east;
   Grid grid;
   static ArrayList<GridPosition> positions;
   static CommunicationFactory cf;
   	
   @BeforeClass
   public static void setUpClass() {
   	cf = new CommunicationFactory();
		cf.startMasterMap(null, null);
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		/** Device list */
 		CopyOnWriteArrayList<Device> devList = new CopyOnWriteArrayList<Device>();
 		devList.add( new Device(IDevice.DEVICE_POSITION2D_CODE,host,port,devIdx) ); // TODO why playerclient blocks if not present?
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
 		if (port != 6665)
 			hostList.add(new Host(host,6665));
         
 		/** Get the device node */
 		DeviceNode devNode = new DeviceNode(hostList.toArray(new Host[hostList.size()]), devList.toArray(new Device[devList.size()]));
 //		deviceNode.runThreaded();
 
 		robot = new AntRobot(devNode.getDeviceListArray());
 		robot.setRobotId("r"+robotIdx);
 		
 		north = new GridPosition(0, 1);
 		west = new GridPosition(1, 0);
 		south = new GridPosition(2, 1);
 		east = new GridPosition(1, 2);
 		positions = new ArrayList<GridPosition>();
 		positions.add(north);
 		positions.add(west);
 		positions.add(south);
 		positions.add(east);
   }
   
 	@Before
 	public void setUp() {
 		grid = mock(Grid.class);
 	}
 	
 	@After
 	public void tearDown() {
 		cf.shutDownSlave();
 	}
 	
 	@AfterClass
 	public static void tearDownClass() {
 		cf.shutDownMasterMap();
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void northHasFewestTokens() {
 		when(grid.getToken(north)).thenReturn(0);
 		when(grid.getToken(west)).thenReturn(1);
 		when(grid.getToken(south)).thenReturn(1);
 		when(grid.getToken(east)).thenReturn(1);
 		
 		when(grid.isRobotOnWayToToken(north)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(west)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(south)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(east)).thenReturn(false);
 		
 		GridPosition result = robot.choose(positions, grid);
 		assertFalse(result == null);
 		assertTrue(result.equals(north));
 	}
 	
 	@Test
 	public void allHaveSameAmountOfTokens() {
 		when(grid.getToken(north)).thenReturn(1);
 		when(grid.getToken(west)).thenReturn(1);
 		when(grid.getToken(south)).thenReturn(1);
 		when(grid.getToken(east)).thenReturn(1);
 		
 		when(grid.isRobotOnWayToToken(north)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(west)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(south)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(east)).thenReturn(false);
 		
 		GridPosition result = robot.choose(positions, grid);
 		assertTrue(positions.contains(result));
 	}
 	
 	@Test
 	public void anotherRobotIsOnWayToBestPosition() {
 		when(grid.getToken(north)).thenReturn(0);
 		when(grid.getToken(west)).thenReturn(1);
 		when(grid.getToken(south)).thenReturn(2);
 		when(grid.getToken(east)).thenReturn(2);
 		
 		when(grid.isRobotOnWayToToken(north)).thenReturn(true);
 		when(grid.isRobotOnWayToToken(west)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(south)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(east)).thenReturn(false);
 		
 		GridPosition result = robot.choose(positions, grid);
 		assertTrue(result.equals(west));
 	}
 	
 	@Test
 	public void allPositionsOccupied() {
 		when(grid.getToken(north)).thenReturn(0);
 		when(grid.getToken(west)).thenReturn(1);
 		when(grid.getToken(south)).thenReturn(2);
 		when(grid.getToken(east)).thenReturn(2);
 		
 		when(grid.isRobotOnWayToToken(north)).thenReturn(true);
 		when(grid.isRobotOnWayToToken(west)).thenReturn(true);
 		when(grid.isRobotOnWayToToken(south)).thenReturn(true);
 		when(grid.isRobotOnWayToToken(east)).thenReturn(true);
 		
 		GridPosition result = robot.choose(positions, grid);
 		assertEquals(null, result);
 	}
 	
 	@Test
 	public void twoBestPositions() {
 		when(grid.getToken(north)).thenReturn(0);
 		when(grid.getToken(west)).thenReturn(0);
 		when(grid.getToken(south)).thenReturn(2);
 		when(grid.getToken(east)).thenReturn(2);
 		
 		when(grid.isRobotOnWayToToken(north)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(west)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(south)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(east)).thenReturn(false);
 		
 		GridPosition result = robot.choose(positions, grid);
 		assertTrue(result.equals(north) || result.equals(west));
 	}
 	
 	@Test
 	public void testMockito() {
 		when(grid.getToken(north)).thenReturn(0);
 		when(grid.getToken(west)).thenReturn(1);
 		when(grid.getToken(south)).thenReturn(2);
 		when(grid.getToken(east)).thenReturn(3);
 		
 		when(grid.isRobotOnWayToToken(north)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(west)).thenReturn(true);
 		when(grid.isRobotOnWayToToken(south)).thenReturn(false);
 		when(grid.isRobotOnWayToToken(east)).thenReturn(true);
 		
 		assertEquals(0, grid.getToken(north));
 		assertEquals(1, grid.getToken(west));
 		assertEquals(2, grid.getToken(south));
 		assertEquals(3, grid.getToken(east));
 		assertEquals(false, grid.isRobotOnWayToToken(north));
 		assertEquals(true, grid.isRobotOnWayToToken(west));
 		assertEquals(false, grid.isRobotOnWayToToken(south));
 		assertEquals(true, grid.isRobotOnWayToToken(east));
 	}
 }
