 /**
  * 
  */
 package hu.modembed.test;
 
 import hu.modembed.MODembedCore;
 import hu.modembed.model.modembed.abstraction.behavior.SymbolMap;
 import hu.modembed.model.modembed.core.object.AssemblerObject;
 import hu.modembed.simulator.DeviceSimulator;
 import hu.modembed.simulator.IDevice;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * @author balazs.grill
  *
  */
 @RunWith(value = Parameterized.class)
 public class SimulatorTests {
 	
 	private static final int OPT_BANKSEL = 1;
 	
 	private static final IDeviceFactory pic16Device = new IDeviceFactory() {
 		
 		@Override
 		public IDevice createDevice() {
 			return new TestPic16Device();
 		}
 	};
 	
 	private static final IDeviceFactory pic18Device = new IDeviceFactory() {
 		
 		@Override
 		public IDevice createDevice() {
 			return new TestPic18Device();
 		}
 	};
 	
 	@Parameters
 	public static Collection<Object[]> data() {
 		
 		Object[][] data = new Object[][] { 
 				{"pic16e", pic16Device, 0 },
 				{"pic16e", pic16Device, OPT_BANKSEL },
 				{"pic18", pic18Device, 0 },
 				{"pic18", pic18Device, OPT_BANKSEL },
 				};
 		return Arrays.asList(data);
 	}
 	
 	private final String device;
 	private final IDeviceFactory deviceFactory;
 	private final int optimizations;
 	
 	public SimulatorTests(String device, IDeviceFactory deviceFactory, int optimizations) {
 		this.device = device;
 		this.deviceFactory = deviceFactory;
 		this.optimizations = optimizations;
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		ModembedTests.testSetUp();
 	}
 	
 	private DeviceSimulator test_operation(String test) throws Exception{
 		IProject project = ModembedTests.loadProject("test.operations");
 		Map<String, String> properties = new HashMap<String, String>();
 		properties.put("device", device);
 		if ((optimizations & OPT_BANKSEL) != 0){
 			properties.put("opt.pic.banksel", "true");
 		}
 		ModembedTests.runAntScript(project, "build.xml", test, properties);
 		ResourceSet resourceSet = MODembedCore.createResourceSet();
 		
 		AssemblerObject asm = ModembedTests.load(project.getFile(".test.asm.xmi"), resourceSet, AssemblerObject.class);
 		SymbolMap map = ModembedTests.load(project.getFile(".test.map.xmi"), resourceSet, SymbolMap.class);
 		Assert.assertNotNull(deviceFactory);
 		IDevice device = deviceFactory.createDevice();
 		Assert.assertNotNull(device);
 		DeviceSimulator simulator = new DeviceSimulator(device, asm, map);
 		
 		int rcode = simulator.execute(10000);
 		Assert.assertEquals("Simulation ran too long!", 0, rcode);
 		return simulator;
 	}
 	
 	@Test
 	public void test1() throws Exception{
 		DeviceSimulator simulator = test_operation("test1");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void test2() throws Exception{
 		DeviceSimulator simulator = test_operation("test2");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void test3() throws Exception{
 		DeviceSimulator simulator = test_operation("test3");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void lower_test1() throws Exception{
 		DeviceSimulator simulator = test_operation("lower.test1");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void lower_test2() throws Exception{
 		DeviceSimulator simulator = test_operation("lower.test2");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void lower_test3() throws Exception{
 		DeviceSimulator simulator = test_operation("lower.test3");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void lower_test1c() throws Exception{
 		DeviceSimulator simulator = test_operation("lower.test1c");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void lower_test2c() throws Exception{
 		DeviceSimulator simulator = test_operation("lower.test2c");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void lower_test3c() throws Exception{
 		DeviceSimulator simulator = test_operation("lower.test3c");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void test4() throws Exception{
 		DeviceSimulator simulator = test_operation("test4");
 		
 		long v = simulator.getSymbolValue("v");
 		Assert.assertEquals(254, v);
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void test5() throws Exception{
 		DeviceSimulator simulator = test_operation("test5");
 		
 		long v = simulator.getSymbolValue("v");
 		Assert.assertEquals(247, v);
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void test6() throws Exception{
 		DeviceSimulator simulator = test_operation("test6");
 		
 		long v = simulator.getSymbolValue("v");
 		Assert.assertEquals(1, v);
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void test7() throws Exception{
 		DeviceSimulator simulator = test_operation("test7");
 		
 		long v = simulator.getSymbolValue("v");
 		Assert.assertEquals(8, v);
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 
 /*
  * UINT16 tests	
  */
 	
 	@Test
 	public void test2_1() throws Exception{
 		DeviceSimulator simulator = test_operation("test2_1");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void test2_2() throws Exception{
 		DeviceSimulator simulator = test_operation("test2_2");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 	
 	@Test
 	public void test2_3() throws Exception{
 		DeviceSimulator simulator = test_operation("test2_3");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}
 
 	@Test
 	public void test2_4() throws Exception{
 		DeviceSimulator simulator = test_operation("test2_4");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void test2_5() throws Exception{
 		DeviceSimulator simulator = test_operation("test2_5");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(1, r);
 	}
 	
 	@Test
 	public void test2_6() throws Exception{
 		DeviceSimulator simulator = test_operation("test2_6");
 		
 		long r = simulator.getSymbolValue("r");
 		Assert.assertEquals(0, r);
 	}	
 	
 	@Test
 	public void cycle_test1() throws Exception{
 		DeviceSimulator simulator = test_operation("cycle.test1");
 		
 		long r = simulator.getSymbolValue("i");
 		Assert.assertEquals(16, r);
 	}
 	
 	@Test
 	public void arrays_test1() throws Exception{
 		DeviceSimulator simulator = test_operation("arrays.test1");
 		
 		long r = simulator.getSymbolAddress("buffer");
 		for(int i=0;i<16;i++){
 			long v = simulator.getValueByAddress(r+i);
 			Assert.assertEquals((long)i, v);
 		}
 	}
 	
 	@Test
 	public void arrays_test2() throws Exception{
 		DeviceSimulator simulator = test_operation("arrays.test2");
 		
 		long r = simulator.getSymbolValue("sum");
		Assert.assertEquals(120, r);
 	}
 	
 	@Test
 	public void add_test1() throws Exception{
 		DeviceSimulator simulator = test_operation("add.test1");
 		
 		long r = simulator.getSymbolValue("v1");
 		Assert.assertEquals(5, r);
 	}
 	
 	@Test
 	public void add_test2() throws Exception{
 		DeviceSimulator simulator = test_operation("add.test2");
 		
 		long r = simulator.getSymbolValue("v1");
 		Assert.assertEquals(10, r);
 	}
 	
 	@Test
 	public void add_test1c() throws Exception{
 		DeviceSimulator simulator = test_operation("add.test1c");
 		
 		long r = simulator.getSymbolValue("v1");
 		Assert.assertEquals(5, r);
 	}
 	
 	@Test
 	public void add_test2c() throws Exception{
 		DeviceSimulator simulator = test_operation("add.test2c");
 		
 		long r = simulator.getSymbolValue("v1");
 		Assert.assertEquals(10, r);
 	}
 }
