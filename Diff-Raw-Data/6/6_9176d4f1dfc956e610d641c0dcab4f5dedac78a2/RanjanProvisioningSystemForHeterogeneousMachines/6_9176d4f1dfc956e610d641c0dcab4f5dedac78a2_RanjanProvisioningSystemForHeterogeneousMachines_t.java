 package provisioning;
 
 import java.util.Iterator;
 
 import commons.cloud.MachineType;
 import commons.cloud.Provider;
 import commons.cloud.Request;
 import commons.config.Configuration;
 import commons.sim.provisioningheuristics.RanjanStatistics;
 
 /**
  * This class represents the DPS business logic modified from original RANJAN. Here some statistics of current
  * available machines (i.e, utilisation) is used to verify if new machines need to be added to 
  * an application tier, or if some machines can be removed from any application tier. After the number of servers needed
  * is calculated, the DPS verifies if any powerful reserved machine is available to be added and, if not, accelerator nodes
  * are purchased from the cloud provider.
  * 
  * @author David candeia
  *
  */
 public class RanjanProvisioningSystemForHeterogeneousMachines extends DynamicProvisioningSystem {
 
 	private double TARGET_UTILIZATION = 0.66;
 	public static long UTILIZATION_EVALUATION_PERIOD_IN_MILLIS = 1000 * 60 * 5;//in millis
 	
 	protected MachineType[] acceleratorTypes = {MachineType.SMALL};
 
 	public RanjanProvisioningSystemForHeterogeneousMachines() {
 		super();
 	}
 	
 	@Override
 	public void evaluateUtilisation(long now, RanjanStatistics statistics, int tier) {
 		long numberOfServersToAdd = evaluateNumberOfServersForNextInterval(statistics);
 		
 		if(numberOfServersToAdd > 0){
 			evaluateMachinesToBeAdded(tier, numberOfServersToAdd);
 		}else if(numberOfServersToAdd < 0){
 			for (int i = 0; i < -numberOfServersToAdd; i++) {
 				configurable.removeServer(tier, false);
 			}
 		}
 	}
 
 	public long evaluateNumberOfServersForNextInterval(RanjanStatistics statistics) {
 		double averageUtilization = statistics.averageUtilisation;
 		double d;
 		if(statistics.numberOfRequestsCompletionsInLastInterval == 0){
 			d = averageUtilization;
 		}else{
 			d = averageUtilization / statistics.numberOfRequestsCompletionsInLastInterval;
 		}
 		
 		double u_lign = Math.max(statistics.numberOfRequestsArrivalInLastInterval, statistics.numberOfRequestsCompletionsInLastInterval) * d;
 		long newNumberOfServers = (int)Math.ceil( statistics.totalNumberOfServers * u_lign / TARGET_UTILIZATION );
 		
 		long numberOfServersToAdd = (newNumberOfServers - statistics.totalNumberOfServers);
 		if(numberOfServersToAdd != 0){
 			return numberOfServersToAdd;
 		}
 		if(statistics.numberOfRequestsArrivalInLastInterval > 0 && 
 				statistics.totalNumberOfServers == 0){
 			return 1l;
 		}
 		return numberOfServersToAdd;
 	}
 	
 	private void evaluateMachinesToBeAdded(int tier, long numberOfServersToAdd) {
 		int serversAdded = 0;
 		Configuration config = Configuration.getInstance();
 		
		MachineType[] machineTypes = MachineType.values();
		
		for(int i = machineTypes.length - 1; i >= 0; i--){//Retrieving any reserved machine left
			MachineType machineType = machineTypes[i];
			
 			Iterator<Provider> iterator = this.providers.values().iterator();
 			while(iterator.hasNext()){
 				Provider provider = iterator.next();
 				while(provider.canBuyMachine(true, machineType) && 
 						serversAdded + config.getRelativePower(machineType) <= numberOfServersToAdd){
 					configurable.addServer(tier, provider.buyMachine(true, machineType), true);
 					serversAdded += config.getRelativePower(machineType);
 				}
 				if(serversAdded == numberOfServersToAdd){
 					break;
 				}
 			}
 			if(serversAdded == numberOfServersToAdd){
 				break;
 			}
 		}
 		
 		//If servers are still needed ...
 		if(serversAdded < numberOfServersToAdd){
 			for(MachineType machineType : this.acceleratorTypes){
 				Iterator<Provider> iterator = this.providers.values().iterator();
 				while(iterator.hasNext()){
 					Provider provider = iterator.next();
 					while(provider.canBuyMachine(true, machineType) && 
 							serversAdded + config.getRelativePower(machineType) <= numberOfServersToAdd){
 						configurable.addServer(tier, provider.buyMachine(true, machineType), true);
 						serversAdded += config.getRelativePower(machineType);
 					}
 					if(serversAdded == numberOfServersToAdd){
 						break;
 					}
 				}
 				if(serversAdded == numberOfServersToAdd){
 					break;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void requestQueued(long timeMilliSeconds, Request request, int tier) {
 		reportLostRequest(request);
 	}
 }
