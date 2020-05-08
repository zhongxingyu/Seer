 package de.hpi_web.cloudSim.profiling.datacenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cloudbus.cloudsim.Cloudlet;
 import org.cloudbus.cloudsim.DatacenterBroker;
 import org.cloudbus.cloudsim.Log;
 import org.cloudbus.cloudsim.UtilizationModel;
 import org.cloudbus.cloudsim.Vm;
 import org.cloudbus.cloudsim.core.CloudSim;
 import org.cloudbus.cloudsim.core.CloudSimTags;
 import org.cloudbus.cloudsim.core.SimEvent;
 
 import de.hpi_web.cloudSim.profiling.utilization.UtilManager;
 import de.hpi_web.cloudSim.profiling.utilization.UtilizationModelFixed;
 
 public class ProfilingBroker extends DatacenterBroker{
 	
 	private List<Cloudlet> cloudlets;
 
 	public ProfilingBroker(String name) throws Exception {
 		super(name);
 		cloudlets = new ArrayList<Cloudlet>();
 		// TODO Auto-generated constructor stub
 	}
 	
 	public void processOtherEvent(SimEvent ev) {
 		switch (ev.getTag()) {
 		// Resource characteristics request
 			case UtilManager.CLOUDLET_UPDATE:
 				processCloudletUpdate(ev);
 				break;
 			case UtilManager.UTIL_SIM_FINISHED:
 				processUtilFinished(ev);
 				break;
 				
 		}
 	}
 	
 	private void processUtilFinished(SimEvent ev) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Finishing ");
 		for (int datacenter : getDatacenterIdsList()) {
 			sendNow(datacenter, UtilManager.UTIL_SIM_FINISHED, null);
 		}
 		
 	}
 
 	private void processCloudletUpdate(SimEvent ev) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Updating Cloudlets for next round ");
 		double cpuUtil = Double.parseDouble(ev.getData().toString());
 		if(cloudletsSubmitted == 0) {
 			
 			List<Vm> vms = getVmsCreatedList();
 			for (Vm vm : vms) {
 			    Cloudlet cloudlet = createCloudlet(vm, cpuUtil);
 				cloudlet.setVmId(vm.getId());
 				sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
 				//cloudlets.add(cloudlet);
 				getCloudletSubmittedList().add(cloudlet);
 				cloudletsSubmitted++;
 				// remove submitted cloudlets from waiting list
 				for (Cloudlet submittedCloudlet : getCloudletSubmittedList()) {
 					getCloudletList().remove(submittedCloudlet);
 				}
 			}
 			
 		} else {
			for (Cloudlet cloudlet : cloudlets) {
				cloudlet.setUtilizationModelCpu(new UtilizationModelFixed(cpuUtil/(double)cloudletsSubmitted)); //TODO value...
 			}
 		}
 		
 		for(Cloudlet cloudlet : cloudletSubmittedList) {
 			Log.printLine(CloudSim.clock() + ": " + getName() + ": cloudlet at CPU util  "+ cloudlet.getUtilizationOfCpu(CloudSim.clock()));
 		}
 		
 
 	  sendNow(ev.getSource(), UtilManager.ROUND_COMPLETED, null);
 	}
 
 	private Cloudlet createCloudlet(Vm vm, double cpuUtil) {
 		Log.printLine(CloudSim.clock() + ": " + getName() + ": Creating Cloudlet ");
 	  // Cloudlet properties
 		
 	  int id = 0;
 	  int pesNumber = 1;
 	  long length = 100000000; //TODO calc it
 	  double utilizationPerVm = (cpuUtil/(double)getVmsCreatedList().size());	// util = 1 means 100% utilization
 
 	  long fileSize = 300;
 	  long outputSize = 300;
 	  UtilizationModel utilizationModel = new UtilizationModelFixed(utilizationPerVm);
 	  Cloudlet cloudlet = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
 	  cloudlet.setUserId(getId());
 	  return cloudlet;
 	}
 
 }
