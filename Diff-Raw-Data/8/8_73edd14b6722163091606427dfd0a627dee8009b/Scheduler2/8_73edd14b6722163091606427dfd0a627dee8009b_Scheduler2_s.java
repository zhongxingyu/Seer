 package at.ac.tuwien.ldsc.group1.application;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import at.ac.tuwien.ldsc.group1.domain.CloudOverallInfo;
 import at.ac.tuwien.ldsc.group1.domain.CloudStateInfo;
 import at.ac.tuwien.ldsc.group1.domain.Event;
 import at.ac.tuwien.ldsc.group1.domain.EventType;
 import at.ac.tuwien.ldsc.group1.domain.components.Application;
 import at.ac.tuwien.ldsc.group1.domain.components.Component;
 import at.ac.tuwien.ldsc.group1.domain.components.Machine;
 import at.ac.tuwien.ldsc.group1.domain.components.PhysicalMachine;
 import at.ac.tuwien.ldsc.group1.domain.components.PhysicalMachineImpl;
 import at.ac.tuwien.ldsc.group1.domain.components.VirtualMachine;
 import at.ac.tuwien.ldsc.group1.domain.components.VirtualMachineImpl;
 import at.ac.tuwien.ldsc.group1.domain.exceptions.ResourceUnavailableException;
 import at.ac.tuwien.ldsc.group1.domain.exceptions.SchedulingNotPossibleException;
 
 import com.google.common.collect.TreeMultiset;
 
 public class Scheduler2 implements Scheduler {
 
 	private final double MAGICPROPORTION = 4400/1900;
     private int maxPMs;
     private int currentPms = 0;
     private long internalTime = 0L;
     long lastInternalTime = -1L;
     double lastTotalConsumption = 0;
 
     //Use maps to map VM --> PM and App --> VM
     private Map<VirtualMachine, PhysicalMachine> pmAllocations;
     List<Application> runningApps = new ArrayList<>();
     private Map<Application, VirtualMachine> appAllocations = new Hashtable<>();
     private Queue<Application> queuedApplications = new LinkedList<>();
     
     
     private boolean eventHandled = false;
 
     @Autowired
     @Qualifier("scenarioWriter2")
     CsvWriter scenarioWriter;
 
     private final CloudOverallInfo overallInfo = new CloudOverallInfo();
     private TreeMultiset<Event> events;
     
     public Scheduler2(int maxPMs) {
         this.maxPMs = maxPMs;
        
     }
 
     @Override
     public void schedule(Event event) {
         Application application = event.getApplication();
         if (event.getEventType() == EventType.START) {
             try {
                 //Handle logging
                 if(event.getEventTime() != internalTime && eventHandled)
                     this.writeLog();
                 //Add Application
                 eventHandled = false;
                 this.addApplication(application);
                 application.start();
                 updateEventTime(event);
                 eventHandled = true;
                 //Add stop event
                 events.add(new Event(event.getEventTime() + application.getDuration(), EventType.STOP, application));
                 
                 
                //MIGRATION
                 //TODO
                 doMigration();
             } catch (ResourceUnavailableException e) {
                 e.printErrorMsg();
             } catch (SchedulingNotPossibleException e) {
                 System.out.println("[" + internalTime + "/" + event.getEventTime() + "] Application delayed...");
                 queuedApplications.add(application);
             }
         } else {
             if(event.getEventTime() != internalTime && eventHandled)
                 this.writeLog();
             this.removeApplication(application);
             application.stop();
             updateEventTime(event);
             eventHandled = true;
             Application nextApplication = queuedApplications.poll();
             if (nextApplication != null) {
                 long startTime = internalTime;
                 events.add(new Event(startTime, EventType.START, nextApplication));
             }
           //TODO
             doMigration();
         }
     }
 
   
 
 	private void updateEventTime(Event event) {
         lastInternalTime = internalTime;
         internalTime = event.getEventTime();
     }
 
     @Override
     public void handleEvents(TreeMultiset<Event> events) {
         if(maxPMs <= 0)
             throw new RuntimeException("The cloud does not contain any physical machines");
         this.events = events;
         while (events.size() > 0) {
             Iterator<Event> iterator = events.iterator();
             Event event = iterator.next();
             iterator.remove();
             schedule(event);
         }
         System.out.println("Number of queued applications:" + queuedApplications.size());
         /* TODO: check if queue still contains some applications and schedule them
                   It might be possible that the queue still contains some applications which have not been
                   executed yet. Simply calling another loop at this point, could possibly introduce an endless loop.
                   We need to also consider the case that there are applications which are too large to run on any
                   physical machine (even if its empty).*/
     }
 
     @Override
     public PhysicalMachine addApplication(Application application) throws ResourceUnavailableException, SchedulingNotPossibleException {
         //1. Find a physical machine which can host this application
         int neededRam = application.getRam();
         int neededHddSize = application.getHddSize();
         int neededCpuInMHz = application.getCpuInMhz();
         PhysicalMachine pm = selectOptimalPM(neededRam, neededHddSize, neededCpuInMHz);
                
         //2 get VM and resize it, every PM will have max 1 VM
         VirtualMachine vm = (VirtualMachine) pm.getComponents().get(0);
 
         
         
         
         //Try to allocate resources and start the VM
         try {
             vm.addComponent(application); //resources are allocated inside this method
             
         } catch (ResourceUnavailableException e) {
             e.printResourceAllocationErrorLog(pm, vm, neededCpuInMHz, neededHddSize, neededRam);
         }
 
         //if everything worked, we add the (app, vm) tuple to the map of applications
         appAllocations.put(application, vm);
         runningApps.add(application);
         return pm;
     }
 
     @Override
     public void removeApplication(Application application) {
         //1. find the virtual machine on which this application runs
         //   and remove it.
         VirtualMachine currentVm = appAllocations.remove(application);
         runningApps.remove(application);
         if (currentVm != null) {
             currentVm.removeComponent(application);     // free resources inside this method
             //2. Kill VM if not needed anymore (we just removed the last app from it)
             if (currentVm.getComponents() == null || currentVm.getComponents().isEmpty()) {
                 currentVm.stop(); //this also removes this VM from its parent
                 // if there are no applications running on this VM then it implies that appAllocations does not
                 // contain the currentVM
                 assert (!appAllocations.containsValue(currentVm));
 
                 //3. Kill PM if not needed anymore (we just removed the last VM from it)
                 PhysicalMachine currentPm = pmAllocations.remove(currentVm);
                 if (currentPm != null && (currentPm.getComponents() == null || currentPm.getComponents().isEmpty())) {
                     currentPm.stop();
                     currentPms--;
                     pmAllocations.remove(currentPm);
                    
                 }
             }
         } else {
             System.out.println("How come app is running on no virtual machine?");
             throw new RuntimeException("Unexpected scheduler state");
         }
     }
 
     private PhysicalMachine selectOptimalPM(Integer neededRam, Integer neededHddSize, Integer neededCpuInMHz) throws SchedulingNotPossibleException {
     	
         if (this.pmAllocations == null) {
             this.pmAllocations = new Hashtable<>();
             PhysicalMachine pm = createNewPM();
             pm.start();
             VirtualMachine vm = null; 
             try {
 				vm = new VirtualMachineImpl(pm);
 			} catch (ResourceUnavailableException e) {
 				e.printStackTrace();
 				System.err.println("Something went terrible wrong: not enogh space on a new PM to start VM on it.");
 			}
             pmAllocations.put(vm, pm);
             vm.start();
             overallInfo.setTotalVMs(overallInfo.getTotalVMs() + 1);
             overallInfo.setTotalPMs(overallInfo.getTotalPMs() + 1);
             return pm;
         } else {
       
         	if(neededRam/neededCpuInMHz > MAGICPROPORTION){
         		
         		PhysicalMachine pm = selectPMwithMoreRAMProportion(neededRam, neededHddSize, neededCpuInMHz);
         		if(pm != null) return pm;
         		
         	}else{
         		
         		PhysicalMachine pm = selectPMwithMoreCPUProportion(neededRam, neededHddSize, neededCpuInMHz);
         		if(pm != null) return pm;
         		
         	}
         	
         	PhysicalMachine firstPossiblePM = selectFirstPossiblePM(neededRam, neededHddSize, neededCpuInMHz);
         	if(firstPossiblePM != null) return firstPossiblePM;
         	
             //list iterated and no pm could give back -> start new pm
             PhysicalMachine pm = createNewPM();
             pm.start();
             VirtualMachine vm = null; 
             try {
 				vm = new VirtualMachineImpl(pm);
 			} catch (ResourceUnavailableException e) {
 				e.printStackTrace();
 				System.err.println("Something went terrible wrong: not enogh space on a new PM to start VM on it.");
 			}
             pmAllocations.put(vm, pm);
             vm.start();
             overallInfo.setTotalVMs(overallInfo.getTotalVMs() + 1);
             overallInfo.setTotalPMs(overallInfo.getTotalPMs() + 1);
             return pm;
         }
     }
     
     private PhysicalMachine selectPMwithMoreCPUProportion(Integer neededRam, Integer neededHddSize, Integer neededCpuInMHz) {
     	for (PhysicalMachine pm : this.pmAllocations.values()) {
     		if (pm.getCpuAvailable() >= neededCpuInMHz &&
     				pm.getRamAvailable() >= neededRam &&
     				pm.getHddAvailable() >= neededHddSize &&
     				pm.getRamAvailable()/pm.getCpuAvailable() > MAGICPROPORTION) {
     			return pm;
     		}
     	}
     	return null;
     }
 
 	private PhysicalMachine selectPMwithMoreRAMProportion(Integer neededRam, Integer neededHddSize, Integer neededCpuInMHz) {
 		for (PhysicalMachine pm : this.pmAllocations.values()) {
     		if (pm.getCpuAvailable() >= neededCpuInMHz &&
     				pm.getRamAvailable() >= neededRam &&
     				pm.getHddAvailable() >= neededHddSize &&
     				pm.getRamAvailable()/pm.getCpuAvailable() < MAGICPROPORTION) {
     			return pm;
     		}
     	}
     	return null;
 	}
 
 	//iterate over PMList give back first possible
     private PhysicalMachine selectFirstPossiblePM(int neededRam,int neededHddSize,int neededCpuInMHz){
     	 for (PhysicalMachine pm : this.pmAllocations.values()) {
              if (pm.getCpuAvailable() >= neededCpuInMHz &&
                      pm.getRamAvailable() >= neededRam &&
                      pm.getHddAvailable() >= neededHddSize) {
                  return pm;
              }
          }
     	 return null;
     }
 
 
     //Scheduler2: create new PM inclusive VM
     private PhysicalMachine createNewPM() throws SchedulingNotPossibleException {
         if (this.currentPms < maxPMs) {
             this.currentPms++;
             PhysicalMachine pm = new PhysicalMachineImpl();
            
             return pm;
         } else {
             throw new SchedulingNotPossibleException();
         }
     }
 
     private void writeLog() {
         int timestamp;
         int totalRAM = 0;
         int totalCPU = 0;
         int totalSize = 0;
         int runningVMs = 0;
         double totalPowerConsumption = 0;
         int inSourced = 0;        //TODO
         int outSourced = 0;        //TODO
 
         timestamp = (int) internalTime;
         //Note that the pmAllocations map can contain each PM several times, thus we need to create a set from it first
         Set<PhysicalMachine> pms = new HashSet<>(pmAllocations.values());
         for (Machine pm : pms) {
             totalRAM += pm.getRam();
             totalCPU += pm.getCpuInMhz();
             totalSize += pm.getHddSize();
             runningVMs += pm.getComponents().size();
             //this consumption is the overall powerConsumption of the cloud in the moment
             totalPowerConsumption += pm.getPowerConsumption();
         }
 
         CloudStateInfo info = new CloudStateInfo(timestamp, totalRAM, totalCPU, totalSize, currentPms, runningVMs, totalPowerConsumption, inSourced, outSourced);
         this.updatePowerConsumption(lastTotalConsumption);
         lastTotalConsumption = totalPowerConsumption;
         this.scenarioWriter.writeLine(info);
     }
 
 
     private void updatePowerConsumption(double lastTotalConsumption) {
         //total consumption after the previous event * time interval between last and new event in seconds
         this.overallInfo.setTotalPowerConsumption(lastTotalConsumption * (lastInternalTime / 1000));
     }
 
     @Override
     public void finalize() {
         this.scenarioWriter.close();
     }
 
     @Override
     public CloudOverallInfo getOverAllInfo() {
         overallInfo.setScheduler(this.getClass().getName());
         overallInfo.setTotalDuration(internalTime);
         return this.overallInfo;
     }
 
     @Override
     public void setMaxNumberOfPhysicalMachines(int nr) {
         this.maxPMs = nr;
     }
     
     private void doMigration() {
     	
     	//Is there a distribution so that we can shut down a PM?
     	if(!isMigrationReasonable()) return;
     	
     	//Sorting collection: in order of expensiveness (More expensive Apps -> Less Expensive Apps)
     	sortRunningApps();
     	
     	clearAssociations();
     	
     	//all we need to now is the currentPms and the runningApps
     	binPacking();
   		
   	}
 
 
 	private void sortRunningApps() {
     	//Sorting collection: in order of expensiveness (More expensive Apps -> Less Expensive Apps)
     	Collections.sort(runningApps, new Comparator<Application>() {
     	    public int compare(Application s1, Application s2) {
     	    	if((s1.getCpuInMhz()*MAGICPROPORTION) + s1.getRam() > (s2.getCpuInMhz()*MAGICPROPORTION) + s2.getRam()){
     	    		return -1;
     	    	}
     	    	else if((s1.getCpuInMhz()*MAGICPROPORTION) + s1.getRam() < (s2.getCpuInMhz()*MAGICPROPORTION) + s2.getRam()){
     	    		return 1;
     	    	}else{
     	    		return 0;
     	    	}
     	    }
     	});
     	
     	/** DEBUG OUTPUT
     	 * 
     	System.out.println("------------------------------");
     	for(Application a : runningApps){
     		System.out.println(a.toString());
     	}
     	System.out.println("------------------------------");
     	 */
 		
 	}
 
 	private boolean isMigrationReasonable() {
 
 		int totalFreeRAM = 0;
 		int totalFreeCPU = 0;
 		int totalFreeSize = 0;
 
 		Set<PhysicalMachine> pms = new HashSet<>(pmAllocations.values());
 		int numPMs = pms.size();
 		
 		for (PhysicalMachine pm : pms) {
 			totalFreeRAM += pm.getRamAvailable();
 			totalFreeCPU += pm.getCpuAvailable();
 			totalFreeSize += pm.getHddAvailable();
 			
 		}
 		
 		//TODO get this PhyisicalMachine Max-attributes somewhere elsewhere from
 		if(((totalFreeRAM / numPMs) > 4700) && ((totalFreeCPU / numPMs) > 2400) && ((totalFreeSize / numPMs) > 50000)	){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	private void clearAssociations() {
 		
 		//TODO make snapshot about the allocation state -> clone everything in different Hashtables so that in case of error we can recover the original state
 		
 		for (VirtualMachine vm : this.appAllocations.values()) {
 			for(Component app : vm.getComponents()){
 				vm.removeComponent(app);
 			}
 		}
 		
 		for (PhysicalMachine pm : this.pmAllocations.values()) {
 			for(Component vm : pm.getComponents()){
 				pm.removeComponent(vm);
 			}
 		}
 		
 		pmAllocations = new Hashtable<>();
 		appAllocations = new Hashtable<>();
 	}
 	
 	/**
 	 * 	implement a bin packing heuristic here:
 	 * 
 	 * 	we create N-1 PMs
 	 * 	start filling them up with the applications
 	 * 	we iterate over the PMs and put into them one Application at one time while the list of applications are sorted by "Weight" (how much resource they consume)
 	 * 	this way in each PM first come the "Bigger" peaces and later the smaller ones
 	 * 	and we hope that this way a better distribution can be made, if no then the rest of remaining apps will be stored on the N-th PM
 	 * 
 	 * 	
 	 * 
 	 */
 	private void binPacking() {
 		
 		//1.)create currentPms-1 PMs, add VMs to every of them
 		for(int i = 0;i< currentPms-1;i++){
 			PhysicalMachine pm = new PhysicalMachineImpl();
 			VirtualMachine vm = null; 
 			try {
 				vm = new VirtualMachineImpl(pm);
 			} catch (ResourceUnavailableException e) {
 				System.err.println("wtf?");
 				e.printStackTrace();
 			}
 			pmAllocations.put(vm, pm);
 		}
 		
 		//2.)try to fill them up if we succeed we do spare 1 Pm else start one more and deploy Apps that wouldnt fit elsewhere
 		List<Application> appsRemained = new ArrayList<>();
 		//TODO can somebody tell me whether there are gaps in the indexing of this list?
 		List<PhysicalMachine> PMliste = (List<PhysicalMachine>) pmAllocations.values();
 		int i = 0;
 		for(Application a : runningApps){
 			VirtualMachine vm = (VirtualMachine) PMliste.get(i % (currentPms-1)).getComponents().get(0);
 			try {
 				vm.addComponent(a);
 				appAllocations.put(a, vm);
 			} catch (ResourceUnavailableException e) {
 				e.printStackTrace();
 				appsRemained.add(a);
 			}
 			i++;
 		}
 		
 		//3.) if there remained unassigned apps -> try to assign them anywhere in the n-1 container
 		List<Application> appsStillRemained = appsRemained;
 		if(!appsRemained.isEmpty()){
 			for(Application a : appsRemained){
 				for(PhysicalMachine pm : PMliste){
 					VirtualMachine vm = (VirtualMachine) pm.getComponents().get(0);
 					try {
 						vm.addComponent(a);
 						//TODO does this work or do i have to clone the appsRemained liste?
 						appsStillRemained.remove(a);
 						appAllocations.put(a, vm);
 					} catch (ResourceUnavailableException e) {
 						continue;
 					}
 				}
 			}
 		}
 		
 		//4.) if appsStillremained not empty ->  we need to start one more PM (number of PMs will be as much as before)
 		if(!appsStillRemained.isEmpty()){
 			
 			PhysicalMachine pm = new PhysicalMachineImpl();
 			VirtualMachine vm = null;
 			try {
 				vm = new VirtualMachineImpl(pm);
 			} catch (ResourceUnavailableException e) {
 				e.printStackTrace();
 			}
 			pmAllocations.put(vm, pm);
 			
 			for(Application a : appsStillRemained){
 				try {
 					vm.addComponent(a);
 					appAllocations.put(a, vm);
 				} catch (ResourceUnavailableException e) {
 					System.err.println("This exception is bad! it means that the distribution before was more efficient than the one what this Migration-bin packing does");
 					System.err.println("We should save the original allocations and if we ever see this message, the original state must be recovered and migration should stop here");
 					//TODO restore original state here
 				}
 			}
 			
 		}else{
 			System.out.println("#######################################!!!OMG OMG !! MIGRATION ALGORITHM WAS SUCCESSFUL !!!!!!##############################################");
 		}
 		
 	}
 
 	
 	
 }
