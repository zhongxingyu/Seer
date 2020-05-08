 package at.ac.tuwien.lsdc.mape;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.UUID;
 
 import at.ac.tuwien.lsdc.actions.Action;
 import at.ac.tuwien.lsdc.actions.CreateAppInsertIntoVm;
 import at.ac.tuwien.lsdc.actions.CreateVmInsertApp;
 import at.ac.tuwien.lsdc.actions.MoveApp;
 import at.ac.tuwien.lsdc.actions.MoveVm;
 import at.ac.tuwien.lsdc.resources.App;
 import at.ac.tuwien.lsdc.resources.PhysicalMachine;
 import at.ac.tuwien.lsdc.resources.Resource;
 import at.ac.tuwien.lsdc.resources.VirtualMachine;
 
 public class Monitor {
 	private int globalTicks =0;
 	private PrintWriter pmLog;
 	private PrintWriter vmLog;
 	private PrintWriter appLog;
 	private PrintWriter analysisLog;
 	private PrintWriter possibilitiesLog;
 	private PrintWriter executionsLog;
 	private UUID executionUuid;
 	
 	public int getGlobalTicks() {
 		return globalTicks;
 	}
 
 	public void setGlobalTicks(int globalTicks) {
 		this.globalTicks = globalTicks;
 	}
 
 	private List<PhysicalMachine> pms = new LinkedList<PhysicalMachine>();
 	
 	static volatile Monitor instance;
 	
 	private Monitor() {
 		try {
 			executionUuid = UUID.randomUUID();
 			pmLog = new PrintWriter("log/pmlog.txt");
 			vmLog = new PrintWriter("log/vmlog.txt");
 			appLog = new PrintWriter("log/applog.txt");
 			this.analysisLog = new PrintWriter("log/analysis.txt");
 			this.possibilitiesLog = new PrintWriter("log/possibilities.txt");
 			this.executionsLog = new PrintWriter("log/executions.txt");
 			
 			this.analysisLog.println ("GlobalTick;ProblemType;ResourceType;PmId;VmId;AppId;CPUAll;CPUUse;MEMAll;MEMUse;STORAll;STORUse");
 			this.possibilitiesLog.println ("GlobalTick;PmId;VmId;AppId;Action;Preconditions;Estimation;Prediction;DestinationPmId;DestinationVmId");
 			this.executionsLog.println ("GlobalTick;PmId;VmId;AppId;Action;Preconditions;Estimation;Prediction;DestinationPmId;DestinationVmId;Evaluation");
 			
 			pmLog.println(getLogHeader());
 			vmLog.println(getLogHeader());
 			appLog.println(getLogHeader());
 			
 		} catch (IOException e) {
 		
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public static Monitor getInstance() {
 		if (Monitor.instance == null) {
 			synchronized (Monitor.class) {
 				if (Monitor.instance == null) {
 					Monitor.instance = new Monitor();
 				}
 			}
 		}
 		
 		return instance;
 	}
 	
 	public void nextTick() {
 		this.globalTicks++;
 		for (PhysicalMachine pm : pms){
 			pm.nextTick();
 		}
 	}
 	
 	public void getNewStati() {
 		// updates PMs, which update VMs, which update Apps
 		for (PhysicalMachine pm : this.pms) {
 			pm.nextTick();
 		}
 	}
 
 	public List<PhysicalMachine> getPms() {
 		return pms;
 	}
 
 	public void setPms(List<PhysicalMachine> pms) {
 		this.pms = pms;
 	}
 	
 	public void addPm(PhysicalMachine pm) {
 		this.pms.add(pm);
 	}
 
 	public void logSystemStatus() throws IOException {
 		for (PhysicalMachine pm : pms) {
 			StringBuffer sb = new StringBuffer();
 			sb.append(executionUuid); 
 			sb.append(";");
 			sb.append(pm.getResourceId()); 
 			sb.append(";");
 			sb.append(";");
 			sb.append(globalTicks); 
 			sb.append(";");
 			sb.append(pm.getRunningTicks()); 
 			sb.append(";");
 			sb.append(pm.getSuspendedTicks()); 
 			sb.append(";");
 			sb.append(pm.getCurrentCpuAllocation()); 
 			sb.append(";");
 			sb.append(pm.getCurrentMemoryAllocation()); 
 			sb.append(";");
 			sb.append(pm.getCurrentStorageAllocation()); 
 			sb.append(";");
 			sb.append(pm.getCurrentCpuUsage()); 
 			sb.append(";");
 			sb.append(pm.getCurrentMemoryUsage()); 
 			sb.append(";");
 			sb.append(pm.getCurrentStorageUsage()); 
 			sb.append(";");
 			sb.append(";");
 			sb.append(";");
 			sb.append(";");
 			sb.append(";");
 			sb.append(pm.isRunning());
 
 			pmLog.println(sb.toString());
 			
 			for (VirtualMachine vm : pm.getVms()) {
 				sb = new StringBuffer();
 				sb.append(executionUuid); 
 				sb.append(";");
 				sb.append(vm.getResourceId()); 
 				sb.append(";");
 				sb.append(vm.getPm().getResourceId()); 
 				sb.append(";");
 				sb.append(globalTicks); 
 				sb.append(";");
 				sb.append(vm.getRunningTicks()); 
 				sb.append(";");
 				sb.append(vm.getSuspendedTicks()); 
 				sb.append(";");
 				sb.append(vm.getCurrentCpuAllocation()); 
 				sb.append(";");
 				sb.append(vm.getCurrentMemoryAllocation()); 
 				sb.append(";");
 				sb.append(vm.getCurrentStorageAllocation()); 
 				sb.append(";");
 				sb.append(vm.getCurrentCpuUsage()); 
 				sb.append(";");
 				sb.append(vm.getCurrentMemoryUsage()); 
 				sb.append(";");
 				sb.append(vm.getCurrentStorageUsage()); 
 				
 				vmLog.println(sb.toString());
 				
 				for(App a : vm.getApps()){
 					sb = new StringBuffer();
 					sb.append(executionUuid); 
 					sb.append(";");
 					sb.append(a.getResourceId()); 
 					sb.append(";");
 					sb.append(a.getVm().getResourceId()); 
 					sb.append(";");
 					sb.append(globalTicks); 
 					sb.append(";");
 					sb.append(a.getRunningTicks()); 
 					sb.append(";");
 					sb.append(a.getSuspendedTicks()); 
 					sb.append(";");
 					sb.append(a.getCurrentCpuAllocation()); 
 					sb.append(";");
 					sb.append(a.getCurrentMemoryAllocation()); 
 					sb.append(";");
 					sb.append(a.getCurrentStorageAllocation()); 
 					sb.append(";");
 					sb.append(a.getCurrentCpuUsage()); 
 					sb.append(";");
 					sb.append(a.getCurrentMemoryUsage()); 
 					sb.append(";");
 					sb.append(a.getCurrentStorageUsage());
 					sb.append(";");
 					sb.append(a.getCpu());
 					sb.append(";");
 					sb.append(a.getMemory());
 					sb.append(";");
 					sb.append(a.getStorage());
 					sb.append(";");
 					sb.append(a.getCpuUsage().size());
 					
 					
 					appLog.println(sb.toString());
 				}
 			}
 		}
 		
 	}
 	
 	public void logAnalysis(Resource problem, String problemtype) {
 		if(problem!=null){
 			StringBuffer sb = new StringBuffer();
 			sb.append(this.globalTicks);
 			sb.append(";");
 			sb.append(problemtype);
 			sb.append(";");
 			
 			if (problem instanceof PhysicalMachine) {
 				PhysicalMachine pm = (PhysicalMachine) problem;
 				sb.append("PM");
 				sb.append(";");
 				sb.append(problem.getResourceId());
 				sb.append(";;;");
 				sb.append(pm.getCurrentCpuAllocation());
 				sb.append(";");
 				sb.append(pm.getCurrentMemoryAllocation());
 				sb.append(";");
 				sb.append(pm.getCurrentStorageAllocation());
 				sb.append(";");
 				sb.append(pm.getCurrentCpuUsage());
 				sb.append(";");
 				sb.append(pm.getCurrentMemoryUsage());
 				sb.append(";");
 				sb.append(pm.getCurrentStorageUsage());
 				sb.append(";");
 			}
 			else if (problem instanceof VirtualMachine) {
 				VirtualMachine vm = (VirtualMachine) problem;
 				if (vm.getPm()!=null){
 					sb.append("VM");
 					sb.append(";");
 					sb.append(vm.getPm().getResourceId());
 				}
 				else {
 					sb.append(";");
 				}
 					sb.append(";");
 					sb.append(vm.getResourceId());
 					sb.append(";;");
 					sb.append(vm.getCurrentCpuAllocation());
 					sb.append(";");
 					sb.append(vm.getCurrentMemoryAllocation());
 					sb.append(";");
 					sb.append(vm.getCurrentStorageAllocation());
 					sb.append(";");
 					sb.append(vm.getCurrentCpuUsage());
 					sb.append(";");
 					sb.append(vm.getCurrentMemoryUsage());
 					sb.append(";");
 					sb.append(vm.getCurrentStorageUsage());
 					sb.append(";");
 				
 			}
 			else {
 				sb.append("APP");
 				sb.append(";");
 				App a = (App) problem;
 				if(a.getVm()!=null) {
 					if(a.getVm().getPm()!=null){
 						sb.append(a.getVm().getPm().getResourceId());
 					}
 					
 					sb.append(";");
 					sb.append(a.getVm().getResourceId());
 					sb.append(";");
 				}
 				else {
 					sb.append(";;");
 				}
 					
 				sb.append(a.getResourceId());
 				sb.append(";");
 				
 				if(a.getVm()!=null) {
 					sb.append(a.getVm().getCurrentCpuAllocation());
 					sb.append(";");
 					sb.append(a.getVm().getCurrentMemoryAllocation());
 					sb.append(";");
 					sb.append(a.getVm().getCurrentStorageAllocation());
 					sb.append(";");
 				}
 				else {
 					sb.append(";;;");
 				}
 					
 				
 				
 				sb.append(a.getCurrentCpuUsage());
 				sb.append(";");
 				sb.append(a.getCurrentMemoryUsage());
 				sb.append(";");
 				sb.append(a.getCurrentStorageUsage());
 				
 				
 			}
 			
 			this.analysisLog.println(sb.toString());
 			
 			
 			
 			
 			
 		}
 		
 	//	this.possibilitiesLog.println ("GlobalTick;PmId;VmId;AppId;Action;Preconditions;Estimation;Prediction;DestinationPmId;DestinationVmId");
 	//	this.executionsLog.println ("GlobalTick;PmId;VmId;AppId;Action;Preconditions;Estimation;Prediction;DestinationPmId;DestinationVmId;Evaluation");
 		
 	}
 
 	public void logPossibilities(Resource problem, Action action) {
 		// GlobalTick;PmId;VmId;AppId;Action;Preconditions;Estimation;Prediction;DestinationPmId;DestinationVmId
 		if (problem != null) {
 			StringBuffer sb = new StringBuffer();
 			// global tick
 			sb.append(this.globalTicks);
 			sb.append(";");
 			// PM, VM, App
 			if (problem instanceof PhysicalMachine) {
 				sb.append(problem.getResourceId());
 				sb.append(";;;");
 			} else if (problem instanceof VirtualMachine) {
 				sb.append(((VirtualMachine) problem).getPm().getResourceId());
 				sb.append(";");
 				// VM
 				sb.append(problem.getResourceId());
 				sb.append(";;");				
 			} else if (problem instanceof App) {
 				VirtualMachine vm = ((App) problem).getVm();
 				
 				if (vm != null) {
 					// PM
 					if (vm.getPm() != null) {
 						sb.append(vm.getPm().getResourceId());
 					}
 					sb.append(";");
 					// VM
 					sb.append(vm.getResourceId());
 					sb.append(";");
 				} else {
 					sb.append(";;");
 				}
 				// App
 				sb.append(problem.getResourceId());
 				sb.append(";");
 			}
 			// Action
 			sb.append(action.getClass().getSimpleName());
 			sb.append(";");
 			// Precondition
 			sb.append(action.preconditions());
 			sb.append(";");
 			// Estimation
 			sb.append(action.estimate());
 			sb.append(";");
 			// Prediction
 			sb.append(action.predict());
 			sb.append(";");
 			// DestinationPmId
 			if (action instanceof MoveVm) {
 				MoveVm moveVmAction = (MoveVm) action;
				if (moveVmAction.getSelectedPm() != null) {
					sb.append(moveVmAction.getSelectedPm().getResourceId());
				}
 			} else if (action instanceof CreateVmInsertApp) {
 				CreateVmInsertApp a = (CreateVmInsertApp) action;
				if (a.getSelectedPm() != null) {
					sb.append(a.getSelectedPm().getResourceId());
				}
 			}
 			sb.append(";");
 			// DestinationVmId
 			if (action instanceof CreateAppInsertIntoVm) {
 				CreateAppInsertIntoVm a = (CreateAppInsertIntoVm) action;
 				if (a.getSelectedVm() != null) {
 					sb.append(a.getSelectedVm().getResourceId());
 				}
 			} else if (action instanceof MoveApp) {
 				MoveApp a = (MoveApp) action;
 				if (a.getSelectedVm() != null) {
 					sb.append(a.getSelectedVm().getResourceId());
 				}
 			}
 			sb.append(";");
 			
 			possibilitiesLog.println(sb.toString());
 			possibilitiesLog.flush();
 		}
 	}
 	
 	public void log (PrintWriter logfile, String text){
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append(this.globalTicks);
 		sb.append(";");
 		
 		
 	}
 	
 	private String getLogHeader() {
 		return "ExecutionID;ID;RootID;GlobalTick;RunningTicks;SuspendedTicks;AllocatedCpu;AllocatedMemory;AllocatedStorage;UsedCpu;UsedMemory;UsedStorage;SLACpu;SLAMemory;SLAStorage;AppRuntime;PMIsRunning";
 	}
 	
 }
