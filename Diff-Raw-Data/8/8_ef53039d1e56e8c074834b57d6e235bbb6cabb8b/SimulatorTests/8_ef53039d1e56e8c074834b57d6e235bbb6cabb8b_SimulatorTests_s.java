 /**
  * 
  */
 package hu.modembed.test;
 
 import hu.modembed.MODembedCore;
 import hu.modembed.model.modembed.abstraction.behavior.SymbolMap;
 import hu.modembed.model.modembed.core.object.AssemblerObject;
 import hu.modembed.simulator.DeviceSimulator;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author balazs.grill
  *
  */
 public class SimulatorTests {
 
 	@Before
 	public void setUp() throws Exception {
 		ModembedTests.testSetUp();
 	}
 	
 	@Test
 	public void pic16_dio() throws Exception{
 		IProject project = ModembedTests.loadProject("test.dio");
 		ModembedTests.runAntScript(project, "build.xml", "pic16f1824");
 	}
 	
 	@Test
 	public void pic18_dio() throws Exception{
 		IProject project = ModembedTests.loadProject("test.dio");
 		ModembedTests.runAntScript(project, "build.xml", "pic18f14k50");
 	}
 	
 	private DeviceSimulator test_operation(String test) throws Exception{
 		IProject project = ModembedTests.loadProject("test.operations");
 		ModembedTests.runAntScript(project, "build.xml", test);
 		ResourceSet resourceSet = MODembedCore.createResourceSet();
 		
 		AssemblerObject asm = ModembedTests.load(project.getFile(".test.asm.xmi"), resourceSet, AssemblerObject.class);
 		SymbolMap map = ModembedTests.load(project.getFile(".test.map.xmi"), resourceSet, SymbolMap.class);
 		DeviceSimulator simulator = new DeviceSimulator(new TestPic16Core(), asm, map);
 		
 		simulator.execute(1000);
 		return simulator;
 	}
 	
 	@Test
 	public void pic16e_uint8_greater_test1() throws Exception{
 		DeviceSimulator simulator = test_operation("test1");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void pic16e_uint8_greater_test2() throws Exception{
 		DeviceSimulator simulator = test_operation("test2");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 }
