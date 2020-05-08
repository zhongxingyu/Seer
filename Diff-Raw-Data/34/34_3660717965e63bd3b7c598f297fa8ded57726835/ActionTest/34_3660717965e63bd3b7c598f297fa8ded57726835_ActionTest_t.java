 package at.ac.tuwien.lsdc.generator.test;
 
 import static org.junit.Assert.fail;
 
 import java.util.LinkedList;
 
 import org.junit.Test;
 
 import at.ac.tuwien.lsdc.actions.Action;
 import at.ac.tuwien.lsdc.actions.ChangeVmConfiguration;
 import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;
 import at.ac.tuwien.lsdc.generator.Request;
 import at.ac.tuwien.lsdc.resources.App;
 import at.ac.tuwien.lsdc.resources.VirtualMachine;
 
 public class ActionTest { 
 
 	@Test
 	public void testArffReader() {
 		try {
 			String filepath = "knowledge/cpu.arff";
 			Action.loadKnowledge(filepath);
 			System.out.println("loading complete!");
 			Action.saveKnowledge(filepath, CreateVmInsertApp.getKnowledgeBase());
 			System.out.println("saving complete!");
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testChangeVmConfiguration() {
 		// setup App
 		LinkedList<Integer> cpuUsage = new LinkedList<Integer>();
 		cpuUsage.add(5);
 		cpuUsage.add(10);
 		cpuUsage.add(15);
 		cpuUsage.add(20);
 		cpuUsage.add(25);
 		cpuUsage.add(30);
 		cpuUsage.add(10);
 		cpuUsage.add(5);
 		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
		cpuUsage.add(0);
 		
 		LinkedList<Integer> memoryUsage = cpuUsage;
 		LinkedList<Integer> storageUsage = cpuUsage;
 		
 		App app = new App(10, 10, 10, cpuUsage, memoryUsage, storageUsage, null);
 		
 		// setup VM
 		VirtualMachine vm = new VirtualMachine(0, 0, 0, 0);
 		vm.createApp(app);
 
 		// setup action
 		ChangeVmConfiguration action = new ChangeVmConfiguration();
 		action.init(vm);
 		
 		for (int i = 0; i < cpuUsage.size(); i++) {
 			System.out.println("Tick "+ i +":");
 			System.out.println("VM CPU allocation: "+ vm.getCurrentCpuAllocation());
 			System.out.println("VM CPU usage: "+ vm.getCurrentCpuUsage());
 			
 			System.out.println("Action prediction: "+ action.predict());
 			action.execute();
 
 			vm.nextTick();
 		}
 	}
 
 }
