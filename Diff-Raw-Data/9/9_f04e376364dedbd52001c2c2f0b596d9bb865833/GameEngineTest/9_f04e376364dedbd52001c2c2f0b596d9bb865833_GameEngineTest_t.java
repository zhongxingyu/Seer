 package tests;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import anchovy.*;
 import anchovy.Components.*;
 import anchovy.Pair.Label;
 
 public class GameEngineTest {
 	GameEngine gameEngine = null;
 	Valve valve1 = null;
 	ArrayList<InfoPacket> v1Info = null;
 	
 	/**
 	 * Makes a basic power plant with one component (Valve 1) that is not connected to anything and has had not info assigned to it.
 	 * 
 	 * @throws Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		gameEngine = new GameEngine();
 		valve1 = new Valve("Valve 1");
 		v1Info = new ArrayList<InfoPacket>();
 		gameEngine.addComponent(valve1);
 	}
 
 	/**
 	 * Test whether adding a component to the list of components in the power plant is sucsessful.
 	 */
 	@Test
 	public void testAddComponent() {
 		
 		ArrayList<InfoPacket> info = gameEngine.getAllComponentInfo();
 		v1Info.add(valve1.getInfo());
 		assertTrue(v1Info.equals(info));
 		
 	}
 	
 	/**
 	 * Tests whether connecting two components is successful.
 	 */
 	@Test
 	public void testConnectComponents(){
 		gameEngine.connectComponentTo(valve1, valve1, true);
 		
 		InfoPacket info = null;
 		Iterator<InfoPacket> it = gameEngine.getAllComponentInfo().iterator();
 		while(it.hasNext()){
 			info = it.next();
 			Iterator<Pair<?>> pIt = info.namedValues.iterator();
 			while(pIt.hasNext()){
				Pair<?> testedpair = pIt.next();
				if(testedpair.getLabel() == Label.oPto){
					assertTrue(testedpair.second() == "Valve 1");
				}else if(testedpair.getLabel() == Label.rcIF){
					assertTrue(testedpair.second() == "Valve 1");
 				}
 				
 			}
 			
 			
 		}
 		
 	}
 	/**
 	 * Tests whether the game engine is able to find the correct component and assign info to it. 
 	 */
 	@Test
 	public void testAssignInfoToComponent(){
 		InfoPacket info = gameEngine.getAllComponentInfo().get(0);
 	
 		
 		info.namedValues.add(new Pair<Boolean>(Label.psit, true));
 		info.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));
 
 
 		info.namedValues.add(new Pair<Double>(Label.Amnt, 40.0));
 		info.namedValues.add(new Pair<Double>(Label.Vlme, 0.0));
 		
 		try {
 			gameEngine.assignInfoToComponent(info);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Component v1 = gameEngine.getPowerPlantComponent("Valve 1");
 		info = new InfoPacket();
 		info.namedValues.add(new Pair<String>(Label.cNme, "Valve 1"));
 		info.namedValues.add(new Pair<Boolean>(Label.psit, true));
 		info.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));;
 		info.namedValues.add(new Pair<Double>(Label.falT, v1.getFailureTime()));
 		info.namedValues.add(new Pair<Double>(Label.Amnt, 40.0));
 		info.namedValues.add(new Pair<Double>(Label.Vlme, 0.0));
 		
 		InfoPacket v1Info = v1.getInfo();
 		assertTrue(v1Info.equals(info));
 		
 	}
 	/**
 	 * Tests whether the game engine correctly sets up a power plant given a list of infoPackets
 	 */
 	@Test
 	public void testSetupPowerPlantConfigureation(){
 		ArrayList<InfoPacket> infoList = new ArrayList<InfoPacket>();
 		
 		InfoPacket info1 = new InfoPacket();
 		info1.namedValues.add(new Pair<String>(Label.cNme, "Valve 1"));
 		info1.namedValues.add(new Pair<Boolean>(Label.psit, true));
 		info1.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));
 		info1.namedValues.add(new Pair<String>(Label.rcIF, "Valve 2"));
 		info1.namedValues.add(new Pair<String>(Label.oPto, "Valve 2"));
 		info1.namedValues.add(new Pair<Double>(Label.Amnt, 40.0));
 		info1.namedValues.add(new Pair<Double>(Label.Vlme, 0.0));
 		infoList.add(info1);
 		
 		InfoPacket info2 = new InfoPacket();
 		info2.namedValues.add(new Pair<String>(Label.cNme, "Valve 2"));
 		info2.namedValues.add(new Pair<Boolean>(Label.psit, true));
 		info2.namedValues.add(new Pair<Double>(Label.OPFL, 12.34));
 		info2.namedValues.add(new Pair<String>(Label.oPto, "Valve 1"));
 		info2.namedValues.add(new Pair<String>(Label.rcIF, "Valve 1"));
 		info2.namedValues.add(new Pair<Double>(Label.Amnt, 40.0));
 		info2.namedValues.add(new Pair<Double>(Label.Vlme, 0.0));
 		
 		infoList.add(info2);
 		
 		gameEngine.clearPowerPlant();
 		assertTrue(gameEngine.getAllComponentInfo().isEmpty());
 		
 		gameEngine.setupPowerPlantConfiguration(infoList);
 		info1.namedValues.add(new Pair<Double>(Label.falT, gameEngine.getPowerPlantComponent("Valve 1").getFailureTime()));
 		info2.namedValues.add(new Pair<Double>(Label.falT, gameEngine.getPowerPlantComponent("Valve 2").getFailureTime()));
 		
 		infoList = new ArrayList<InfoPacket>();
 		infoList.add(info1);
 		infoList.add(info2);
 		
 		ArrayList<InfoPacket> allCompInfo = gameEngine.getAllComponentInfo();
 		assertTrue(allCompInfo.equals(infoList));
 	}
 	
 	/**
 	 * Tests whether components are repaired correctly.
 	 */
 	@Test
 	public void testRepair(){
 		Component v1 = gameEngine.getPowerPlantComponent("Valve 1");
 		v1.setFailed(true);
 		assertTrue(v1.isFailed());
 		
 		gameEngine.repair(v1);
 		assertTrue(!v1.isFailed());
 	}
 	
 }
