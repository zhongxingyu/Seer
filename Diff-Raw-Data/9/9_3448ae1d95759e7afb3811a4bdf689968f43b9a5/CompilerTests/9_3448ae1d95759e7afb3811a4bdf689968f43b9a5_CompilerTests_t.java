 package hu.modembed.test;
 
 import hu.modembed.MODembedCore;
 import hu.modembed.model.modembed.core.object.AssemblerObject;
 import hu.modembed.simulator.SimulatorCore;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class CompilerTests {
 	
 	@Before
 	public void setUp() throws Exception {
 		ModembedTests.testSetUp();
 	}
 
 	@Test
 	public void test_assembler_pic16e() throws InvocationTargetException, InterruptedException, CoreException {
 		IProject project = ModembedTests.loadProject("test.assembler");
 		ModembedTests.runAntScript(project, "test.asm.pic16e.ant.xml");
 		ModembedTests.assertModelsAreEquivalent(project, "test.asm.pic16e.hex", ".test.asm.pic16e.out.hex");
 	}
 
 	@Test
 	public void test_assembler_pic18() throws InvocationTargetException, InterruptedException, CoreException {
 		IProject project = ModembedTests.loadProject("test.assembler");
 		ModembedTests.runAntScript(project, "test.asm.pic18.ant.xml");
 		ModembedTests.assertModelsAreEquivalent(project, "test.asm.pic18.hex", ".test.asm.pic18.out.hex");
 	}
 	
 	@Test
 	public void test_assembler_msp430() throws InvocationTargetException, InterruptedException, CoreException, IOException {
 		IProject project = ModembedTests.loadProject("test.assembler");
 		ModembedTests.runAntScript(project, "msp430/test.asm.msp430.xml");
 		
 		ResourceSet rs = MODembedCore.createResourceSet();
 		EObject[] elements = new EObject[2];
 		elements[0] = ModembedTests.load(project.getFile("msp430/.test.asm1.msp430.model"), rs);
 		elements[1] = ModembedTests.load(project.getFile("msp430/.test.asm2.msp430.model"), rs);
 		
 		for(int i=0;i<elements.length;i++){
 			Assert.assertTrue("Element "+i+" is not an AssemblerObject", elements[i] instanceof AssemblerObject);
 			AssemblerObject aso = (AssemblerObject)elements[i];
 			Assert.assertTrue("Element "+i+" is invalid", aso.getInstructions().size() > 2);
 		}
 		
 	}
 	
 	@Test
 	public void test_compiler_pic16e() throws Exception{
 		IProject project = ModembedTests.loadProject("test.compileToDevice");
 		ModembedTests.runAntScript(project, "test.compile.pic16e.ant.xml");
 		
 		IFile asmfile = project.getFile(".test.main.asm.xmi");
 		Assert.assertTrue("AssemblerObject model does not exist!", asmfile.exists());
 		ResourceSet rs = MODembedCore.createResourceSet();
 		EObject asm = ModembedTests.load(asmfile, rs);
 		Assert.assertTrue("Invalid AssemblerObject", asm instanceof AssemblerObject);
 		
 		SimulatorCore simulator = new SimulatorCore();
 		TestPic16Device device = new TestPic16Device();
 		simulator.setCore(device);
 		simulator.setCode((AssemblerObject)asm);
 		simulator.setProgramCounter(device.PC());
 		
 		simulator.start();
 		long counter = 0;
 		while(simulator.isRunning()){
 			if (counter >= 100){
 				Assert.fail("Simulation did not end!");
 			}
 			simulator.step();
 			counter++;
 		}
 		
		Assert.assertTrue(device.memory().getValue(32) == 5);
 	}
 	
 	@Test
 	public void test_compile_structuralModule() throws Exception{
 		IProject project = ModembedTests.loadProject("test.module.compile");
 		ModembedTests.runAntScript(project, "build.xml");
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
 	
 }
