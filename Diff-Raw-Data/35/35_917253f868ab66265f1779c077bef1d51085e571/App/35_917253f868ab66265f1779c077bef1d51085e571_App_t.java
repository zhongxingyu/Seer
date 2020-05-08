 package at.ac.tuwien.lsdc.resources;
 
 import java.util.LinkedList;
 
 
 import at.ac.tuwien.lsdc.mape.Problem;
 
 public class App extends Resource implements Problem {
 	// how many ticks the App will run
 	private int ticks;
 	
 	// extra ticks that the App cannot run.
 	// If this is > 0 the App is suspendend and the extraTicks should be decremented.
 	private int extraTicks = 0;
 	
 	// SLAs
 	private int cpu;
 	private int memory;
 	private int storage;
 
 	// used resources per tick
 	private LinkedList<Integer> cpuUsage;
 	private LinkedList<Integer> memoryUsage;
 	private LinkedList<Integer> storageUsage;
 	
 	// link to the hosting vm
 	private VirtualMachine vm;
 	
 	
	public App(int cpu, int memory, int storage) {
 		this.cpu= cpu;
 		this.memory = memory;
 		this.storage = storage;
 	}
 	
 	public int getTicks() {
 		return ticks;
 	}
 
 	public void setTicks(int ticks) {
 		this.ticks = ticks;
 	}
 
 
 	public int getExtraTicks() {
 		return extraTicks;
 	}
 
 	public void setExtraTicks(int extraTicks) {
 		this.extraTicks = extraTicks;
 	}
 
 	public int getCpu() {
 		return cpu;
 	}
 
 	public void setCpu(int cpu) {
 		this.cpu = cpu;
 	}
 
 	public int getMemory() {
 		return memory;
 	}
 
 	public void setMemory(int memory) {
 		this.memory = memory;
 	}
 
 	public int getStorage() {
 		return storage;
 	}
 
 	public void setStorage(int storage) {
 		this.storage = storage;
 	}
 
 	public Integer getCurrentCpuUsage() {
 		return this.getCpuUsage().get(this.runningTicks);
 	}
 
 	public Integer getCurrentMemoryUsage() {
 		return this.getMemoryUsage().get(this.runningTicks);
 	}
 
 	public Integer getCurrentStorageUsage() {
 		return this.getStorageUsage().get(this.runningTicks);
 	}
 
 	public LinkedList<Integer> getCpuUsage() {
 		return cpuUsage;
 	}
 
 	public void setCpuUsage(LinkedList<Integer> cpuUsage) {
 		this.cpuUsage = cpuUsage;
 	}
 
 	public LinkedList<Integer> getMemoryUsage() {
 		return memoryUsage;
 	}
 
 	public void setMemoryUsage(LinkedList<Integer> memoryUsage) {
 		this.memoryUsage = memoryUsage;
 	}
 
 	public LinkedList<Integer> getStorageUsage() {
 		return storageUsage;
 	}
 
 	public void setStorageUsage(LinkedList<Integer> storageUsage) {
 		this.storageUsage = storageUsage;
 	}
 	
 	public LinkedList<Integer> getCpuUsageHistory(int maxNumberOfEntries) {
 		return getLastEntriesUtil(cpuUsage, maxNumberOfEntries);
 	}
 	
 	public LinkedList<Integer> getMemoryUsageHistory(int maxNumberOfEntries) {
 		return getLastEntriesUtil(memoryUsage, maxNumberOfEntries);
 	}
 	
 	public LinkedList<Integer> getStorageUsageHistory(int maxNumberOfEntries) {
 		return getLastEntriesUtil(storageUsage, maxNumberOfEntries);
 	}
 
 	@Override
 	//at app level equivalent to usage 
 	public Integer getCurrentCpuAllocation() {
 		return this.getCurrentCpuUsage();
 	}
 
 	@Override
 	//at app level equivalent to usage
 	public Integer getCurrentMemoryAllocation() {
 		// TODO Auto-generated method stub
 		return this.getCurrentMemoryUsage();
 	}
 
 	@Override
 	//at app level equivalent to usage
 	public Integer getCurrentStorageAllocation() {
 		// TODO Auto-generated method stub
 		return this.getCurrentStorageUsage();
 	}
 
 	@Override
 	public void nextTick() {
 		if (suspendedTicks>0) {
 			suspendedTicks--;
 		}
 		else {
 			runningTicks++;
 			
 			//terminate?
 			if(runningTicks>=ticks){
 				vm.getApps().remove(this);
 			}
 		}
 	}
 
 	@Override
 	//at app level equivalent to usage
 	public LinkedList<Integer> getStorageAllocationHistory(int maxEntries) {
 		return this.getStorageUsageHistory(maxEntries);
 	}
 
 	@Override	
 	//at app level equivalent to usage	
 	public LinkedList<Integer> getCpuAllocationHistory(int maxEntries) {
 		return this.getCpuUsageHistory(maxEntries);
 	}
 
 	@Override
 	//at app level equivalent to usage	
 	public LinkedList<Integer> getMemoryAllocationHistory(int maxEntries) {
 		return this.getMemoryUsageHistory(maxEntries);
 	}
 
 }
