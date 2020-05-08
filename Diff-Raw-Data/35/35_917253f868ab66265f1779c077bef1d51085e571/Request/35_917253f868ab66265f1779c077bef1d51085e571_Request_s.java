 package at.ac.tuwien.lsdc.generator;
 
 import java.util.List;
 
 import at.ac.tuwien.lsdc.resources.App;
 
 public class Request {
 	private int ticks;
 	
 	// SLAs
 	private int cpu;
 	private int memory;
 	private int storage;
 	
 	// usage per tick
	private List<Integer> cpuUsage;
	private List<Integer> memoryUsage;
	private List<Integer> storageUsage;
 	
 	public App createApp() {
		App app = new App();
		
		// populate app with date from this request
		app.setCpu(this.cpu);
		app.setMemory(this.memory);
		app.setStorage(this.storage);
 		
 		app.setCpuUsage(this.cpuUsage);
 		app.setMemoryUsage(this.memoryUsage);
 		app.setStorageUsage(this.storageUsage);
 		
 		return app;
 	}
 
 	public int getTicks() {
 		return ticks;
 	}
 
 	public void setTicks(int ticks) {
 		this.ticks = ticks;
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
 
 	public List<Integer> getCpuUsage() {
 		return cpuUsage;
 	}
 
	public void setCpuUsage(List<Integer> cpuUsage) {
 		this.cpuUsage = cpuUsage;
 	}
 
 	public List<Integer> getMemoryUsage() {
 		return memoryUsage;
 	}
 
	public void setMemoryUsage(List<Integer> memoryUsage) {
 		this.memoryUsage = memoryUsage;
 	}
 
 	public List<Integer> getStorageUsage() {
 		return storageUsage;
 	}
 
	public void setStorageUsage(List<Integer> storageUsage) {
 		this.storageUsage = storageUsage;
 	}
 }
