 package framework.utils;
 
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.esm.ElasticServiceManagers;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsc.GridServiceContainers;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.gsm.GridServiceManagers;
 import org.openspaces.admin.lus.LookupService;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 
 import static framework.utils.LogUtils.log;
 import static framework.utils.ToStringUtils.gscToString;
 import static framework.utils.ToStringUtils.puInstanceToString;
 
 /**
  * Utility methods for test teardown.
  * 
  * @author Moran Avigdor
  */
 public class TeardownUtils {
 
     public static void teardownAll(Admin admin) {
        snapshot(admin);
 
         // kill all ESMs first
         for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
             ElasticServiceManagers elasticServiceManagers = gsa.getMachine()
                     .getElasticServiceManagers();
             for (ElasticServiceManager esm : elasticServiceManagers) {
                 try {
                     log("killing ESM [ID:" + esm.getAgentId() +"] [PID: "+esm.getVirtualMachine().getDetails().getPid() +" ]");
                     esm.kill();
                 } catch (Exception e) {
                     log("ESM kill failed - " + e, e);
                 }
             }
         }
         
         // kill all GSMs 
         for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
             GridServiceManagers gridServiceManagers = gsa.getMachine()
                     .getGridServiceManagers();
             for (GridServiceManager gsm : gridServiceManagers) {
                 try {
                     log("killing GSM [ID:" + gsm.getAgentId() +"] [PID: "+gsm.getVirtualMachine().getDetails().getPid() +" ]");
                     gsm.kill();
                 } catch (Exception e) {
                     log("GSM kill failed - " + e, e);
                 }
             }
         }
 
         // kill all GSCs
         for (GridServiceAgent gsa : admin.getGridServiceAgents()) {
             GridServiceContainers gridServiceContainers = gsa.getMachine()
                     .getGridServiceContainers();
             for (GridServiceContainer gsc : gridServiceContainers) {
                 try {
                     log("killing GSC [ID:" + gsc.getAgentId() +"] [PID: "+gsc.getVirtualMachine().getDetails().getPid() +" ]");
                     gsc.kill();
                 } catch (Exception e) {
                     log("GSC kill failed - " + e, e);
                 }
             }
         }
     }
 
     public static void dumpLogs(Admin... admins) {
         for (Admin a : admins) {
             if (a != null) {
                 try {
                     DumpUtils.dumpLogs(a);
                 } catch (Throwable t) {
                     log("failed to dump logs", t);
                 }
             }
         }
     }
 
     public static void teardownAll(Admin ... admins) {
         dumpLogs(admins);
         for(Admin admin : admins){
             teardownAll(admin);
         }
     }
 
     public static void snapshot(Admin admin) {
         if (admin == null) {
         	log("> no snapshot. admin is null");
         	return;
         }
         
     	log("> snapshot " + admin.getGroups()[0] +": ");
         for (Machine machine : admin.getMachines()) {
             log("Machine: " + machine.getHostName() + "/"
                     + machine.getHostAddress());
             for (LookupService lus : machine.getLookupServices()) {
                 log("\t LUS [ID: " + lus.getAgentId() + "] [PID: "+lus.getVirtualMachine().getDetails().getPid() +"]");
             }
             for (GridServiceAgent gsa : machine.getGridServiceAgents()) {
                 log("\t GSA [" + gsa.getProcessesDetails() + "] [PID: "+gsa.getVirtualMachine().getDetails().getPid() +"]");
             }
             for (GridServiceManager gsm : machine.getGridServiceManagers()) {
                 log("\t GSM [ID: " + gsm.getAgentId() + "] [PID: "+gsm.getVirtualMachine().getDetails().getPid() +"]");
             }
             for (ElasticServiceManager esm : machine.getElasticServiceManagers()) {
                 log("\t ESM [ID: " + esm.getAgentId() + "] [PID: "+esm.getVirtualMachine().getDetails().getPid() +" ]");
             }
             for (GridServiceContainer gsc : machine.getGridServiceContainers()) {
                 log("\t " + gscToString(gsc));
                 for (ProcessingUnitInstance puInstance : gsc
                         .getProcessingUnitInstances()) {
                     log("\t - " + puInstanceToString(puInstance));
                 }
             }
             log("total PU instances: "
                     + machine.getProcessingUnitInstances().length);
             log("---");
         }
         log("\n");
         log("Processing Units:");
         for (ProcessingUnit pu : admin.getProcessingUnits()) {
         	log("\t PU " + pu.getName() + " status " + pu.getStatus().toString() + " total instances: " + pu.getInstances().length);
         }
         log("---\n");
     }
 
     public static void snapshot(Admin ... admins) {
         for(Admin admin : admins){
             snapshot(admin);
         }
     }
 }
