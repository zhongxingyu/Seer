 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import net.wgr.xenmaster.controller.BadAPICallException;
 import net.wgr.xenmaster.monitoring.LogEntry;
 import net.wgr.xenmaster.monitoring.LogKeeper;
 import org.apache.commons.collections.CollectionUtils;
 
 /**
  * 
  * @created Oct 2, 2011
  * @author double-u
  */
 public class VM extends NamedEntity {
 
     @ConstructorArgument
     protected int userVersion = 1;
     @ConstructorArgument
     protected ShutdownAction actionsAfterReboot;
     @ConstructorArgument
     protected ShutdownAction actionsAfterShutdown;
     @ConstructorArgument
     protected CrashedAction actionsAfterCrash;
     @ConstructorArgument
     protected int startupVCPUs, maxVCPUs;
     @ConstructorArgument
     protected long minimumStaticMemory, minimumDynamicMemory;
     @ConstructorArgument
     protected long maximumStaticMemory;
     @ConstructorArgument
     protected long maximumDynamicMemory;
     protected int domainId;
     @ConstructorArgument
     protected boolean template;
     protected boolean controlDomain;
     protected String poolName;
     protected boolean autoPowerOn;
     @ConstructorArgument
     protected String pvArgs, pvRamdisk, pvBootloader, pvKernel, pvBootloaderArgs;
     protected PowerState powerState;
     @ConstructorArgument
     protected String hvmBootPolicy;
     @Fill
     @ConstructorArgument
     protected Map<String, String> hvmBootParams;
     @ConstructorArgument
     @Fill
     protected Map<String, String> platform;
     @ConstructorArgument
     protected String pciBus;
     protected String metrics, guestMetrics;
     protected String host;
     @ConstructorArgument
     protected String hostAffinity;
     @Fill
     protected Object[] vbds, vifs, consoles;
     @Fill
     @ConstructorArgument
     protected Map<String, String> vcpuParams;
     @Fill
     @ConstructorArgument
     protected Map<String, String> otherConfig;
     @ConstructorArgument
     protected String recommendations;
     protected static final int MEGABYTE = 1024 * 1024;
 
     public VM() {
         this.actionsAfterReboot = ShutdownAction.RESTART;
         this.actionsAfterShutdown = ShutdownAction.DESTROY;
         this.actionsAfterCrash = CrashedAction.DESTROY;
 
         this.platform = new HashMap<>();
     }
 
     public VM(String ref, boolean autoFill) {
         super(ref, autoFill);
     }
 
     public VM(String ref) {
         super(ref);
     }
 
     public String create(int maxVCPUs) throws BadAPICallException {
         this.maxVCPUs = maxVCPUs;
 
         if (startupVCPUs == 0) {
             startupVCPUs = maxVCPUs;
         }
 
         if (startupVCPUs < 1 || maxVCPUs < 1 || startupVCPUs > maxVCPUs) {
             throw new IllegalArgumentException("VM CPU count is zero or startup VCPU count is larger than max VCPU count");
         }
 
         Map<String, Object> ctorArgs = collectConstructorArgs();
         // Not putting legacy args in the model, we don't do legacy
         ctorArgs.put("PV_legacy_args", "");
 
         this.reference = (String) dispatch("create", ctorArgs);
         return this.reference;
     }
 
     public void destroy() throws BadAPICallException {
         dispatch("destroy");
     }
 
     public void start(boolean startPaused, boolean force) throws BadAPICallException {
         start(startPaused, force, null);
     }
 
     public void start(boolean startPaused, boolean force, Host host) throws BadAPICallException {
         try {
             easeStart();
             if (host != null) {
                 dispatch("start", host.getReference(), startPaused, force);
             } else {
                 dispatch("start", startPaused, force);
             }
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might be already running");
                     break;
                 case "VM_HVM_REQUIRED":
                     ex.setErrorDescription("Your CPU(s) does not support VT-x or AMD-v, which this VM requires");
                     break;
                 case "NO_HOST_AVAILABLE":
                     ex.setErrorDescription("There are no hosts available for this machine to run on");
                     break;
             }
 
             throw ex;
         }
     }
 
     /** 
      * Check if this VM will be able to start up properly
      * @return 
      */
     public void easeStart() throws BadAPICallException {
         for (VBD vbd : this.getVBDs()) {
             if (vbd.getType() != VBD.Type.DISK) continue;
             if (vbd.getVDI() == null) {
                 LogKeeper.log(new LogEntry(vbd.getReference(), getClass(), "VM_START_COULD_NOT_PLUG_VBD", LogEntry.Level.WARNING));
                 continue;
             }
             for (PBD pbd : vbd.getVDI().getSR().getPBDs()) {
                 if (!pbd.isPlugged()) {
                     LogKeeper.log(new LogEntry(pbd.getReference(), getClass(), "PLUGGED_IN_PBD", LogEntry.Level.INFORMATION));
                     pbd.plug();
                 }
             }
         }
     }
 
     public void pause() throws BadAPICallException {
         try {
             dispatch("pause");
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might be already paused");
             }
 
             throw ex;
         }
     }
 
     public void resume() throws BadAPICallException {
         try {
             dispatch("unpause");
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might be already running");
             }
 
             throw ex;
         }
     }
 
     /**
      * Stop the VM
      * @param polite it's up to you to keep your manners
      */
     public void stop(boolean polite) throws BadAPICallException {
         try {
             if (polite) {
                 dispatch("clean_shutdown");
             } else {
                 dispatch("hard_shutdown");
             }
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     /**
      * Reboot the VM
      * @param polite it's up to you to keep your manners
      */
     public void reboot(boolean polite) throws BadAPICallException {
         try {
             if (polite) {
                 dispatch("clean_reboot");
             } else {
                 dispatch("hard_reboot");
             }
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     public void suspend() throws BadAPICallException {
         try {
             dispatch("suspend");
         } catch (BadAPICallException ex) {
 
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     public void wake(boolean startPaused, boolean force) throws BadAPICallException {
         try {
             dispatch("resume", startPaused, force);
         } catch (BadAPICallException ex) {
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be suspended");
             }
 
             throw ex;
         }
     }
 
     public void migrateInsidePool(Host host, Map<String, String> options) throws BadAPICallException {
         if (options == null) {
             options = new HashMap<>();
         }
         try {
             dispatch("pool_migrate", host, options);
         } catch (BadAPICallException ex) {
             switch (ex.getMessage()) {
                 case "BAD_POWER_STATE":
                     ex.setErrorDescription("The VM has a bad power state. It might not be running");
             }
 
             throw ex;
         }
     }
 
     public int computeMemoryOverhead() throws BadAPICallException {
         return (int) dispatch("compute_memory_overhead");
     }
 
     public void sendSysRq(String sysrq) throws BadAPICallException {
         dispatch("send_sysrq", sysrq);
     }
 
     public void sendTrigger(String trigger) throws BadAPICallException {
         dispatch("send_trigger", trigger);
     }
 
     // todo check what this does and decide on good name
 //    public int computeMaximumAvailableMemory() throws BadAPICallException {
 //        
 //    }
     public List<Object> getDataSources() throws BadAPICallException {
         Object[] result = (Object[]) dispatch("get_data_sources");
         ArrayList<Object> arrr = new ArrayList<>();
         CollectionUtils.addAll(arrr, result);
         return arrr;
     }
 
     public int[] getFreeVBDIndexes() throws BadAPICallException {
         Object[] result = (Object[]) dispatch("get_allowed_VBD_devices");
         int[] indexes = new int[result.length];
         for (int i = 0; i < result.length; i++) {
             indexes[i] = Integer.parseInt(result[i].toString());
         }
         return indexes;
     }
 
     public int getNextAvailableVBDIndex() throws BadAPICallException {
         return getFreeVBDIndexes()[0];
     }
 
     public int[] getFreeVIFIndexes() throws BadAPICallException {
         Object[] result = (Object[]) dispatch("get_allowed_VIF_devices");
         int[] indexes = new int[result.length];
         for (int i = 0; i < result.length; i++) {
             indexes[i] = Integer.parseInt(result[i].toString());
         }
         return indexes;
     }
 
     public int getNextAvailableVIFIndex() throws BadAPICallException {
         return getFreeVIFIndexes()[0];
     }
 
     public VMMetrics getMetrics() {
         this.metrics = value(this.metrics, "get_vm_metrics");
         return new VMMetrics(this.metrics);
     }
 
     public GuestMetrics getGuestMetrics() {
         this.guestMetrics = value(this.guestMetrics, "get_guest_metrics");
         return new GuestMetrics(this.guestMetrics);
     }
 
     public static List<VM> getAll() throws BadAPICallException {
         List<VM> allVMs = getAllEntities(VM.class);
         for (ListIterator<VM> it = allVMs.listIterator(); it.hasNext();) {
             if (it.next().isTemplate()) it.remove();
         }
         return allVMs;
     }
 
     public static List<VM> getTemplates() throws BadAPICallException {
         List<VM> allVMs = getAllEntities(VM.class);
         for (ListIterator<VM> it = allVMs.listIterator(); it.hasNext();) {
             if (!it.next().isTemplate()) it.remove();
         }
         return allVMs;
     }
 
     public List<VBD> getVBDs() {
         return getEntities(VBD.class, "get_VBDs");
     }
 
     public List<VIF> getVIFs() {
         return getEntities(VIF.class, "get_VIFs");
     }
 
     public List<Console> getConsoles() {
         return getEntities(Console.class, "get_consoles");
     }
 
     public Map<String, String> getVCPUParams() {
         if (vcpuParams == null) {
             vcpuParams = new HashMap<>();
         }
         return vcpuParams;
     }
 
     public void setMemoryLimits(double maxStaticMemMb, double minStaticMemMb, double maxDynMemMb, double minDynMemMb) throws BadAPICallException {
         dispatch("set_memory_limits", minStaticMemMb * MEGABYTE, maxStaticMemMb * MEGABYTE, minDynMemMb * MEGABYTE, maxDynMemMb * MEGABYTE);
     }
 
     public void setActionsAfterCrash(CrashedAction actionsAfterCrash) throws BadAPICallException {
         this.actionsAfterCrash = setter(actionsAfterCrash, "set_actions_after_crash");
     }
 
     public void setActionsAfterReboot(ShutdownAction actionsAfterReboot) throws BadAPICallException {
         this.actionsAfterReboot = setter(actionsAfterReboot, "set_actions_after_reboot");
     }
 
     public void setActionsAfterShutdown(ShutdownAction actionsAfterShutdown) throws BadAPICallException {
         this.actionsAfterShutdown = setter(actionsAfterShutdown, "set_actions_after_shutdown");
     }
 
     public String getHVMBootPolicy() {
         return hvmBootPolicy;
     }
 
     public void setDefaultHVMBootPolicy() throws BadAPICallException {
         setHVMBootPolicy("BIOS order");
     }
 
     public void setHVMBootPolicy(String policy) throws BadAPICallException {
         hvmBootPolicy = setter(policy, "set_HVM_boot_policy");
     }
 
     public Map<String, String> getHVMBootParams() {
         return hvmBootParams;
     }
 
    public void setHVMBootParams(Map<String, String> params) throws BadAPICallException {
        hvmBootParams = setter(params, "set_HVM_boot_params");
     }
 
     public Platform getPlatform() {
         return new Platform(platform);
     }
 
     public void setPlatform(Platform platform) throws BadAPICallException {
         this.platform = setter(platform.getMap(), "set_platform");
     }
 
     public String getPVargs() {
         return pvArgs;
     }
 
     public String getPVBootloader() {
         return pvBootloader;
     }
 
     public void setPVBootloader(String bootloader) throws BadAPICallException {
         pvBootloader = setter(bootloader, "set_PV_bootloader");
     }
 
     public String getPVBootloaderArgs() {
         return pvBootloader;
     }
 
     public void setPVBootloaderArgs(String bootloaderargs) throws BadAPICallException {
         pvBootloader = setter(bootloaderargs, "set_PV_bootloader_args");
     }
 
     public String getPVKernel() {
         return pvKernel;
     }
 
     public void setPVKernel(String kernel) throws BadAPICallException {
         pvKernel = setter(kernel, "set_PV_kernel");
     }
 
     public String getPVRamdisk() {
         return pvRamdisk;
     }
 
     public void setPVRamdisk(String ramdisk) throws BadAPICallException {
         pvRamdisk = setter(ramdisk, "set_PV_ramdisk");
     }
 
     public CrashedAction getActionsAfterCrash() {
         return actionsAfterCrash;
     }
 
     public ShutdownAction getActionsAfterReboot() {
         return actionsAfterReboot;
     }
 
     public ShutdownAction getActionsAfterShutdown() {
         return actionsAfterShutdown;
     }
 
     public boolean isAutoPowerOn() {
         return autoPowerOn;
     }
 
     public int getDomainId() {
         return domainId;
     }
 
     public boolean isControlDomain() {
         return controlDomain;
     }
 
     public boolean isTemplate() {
         return template;
     }
 
     public int getMaximumVCPUs() {
         return maxVCPUs;
     }
 
     public int getStartupVCPUs() {
         return value(startupVCPUs, "getVCPUs_at_startup");
     }
 
     public void setStartupVCPUs(int startupVCPUs) throws BadAPICallException {
         this.startupVCPUs = setter(startupVCPUs, "set_VCPUs_at_startup");
     }
 
     public long getMaximumDynamicMemory() {
         return maximumDynamicMemory;
     }
 
     public void setMaximumDynamicMemory(double mdmMb) throws BadAPICallException {
         this.maximumDynamicMemory = setter((long) mdmMb * MEGABYTE, "set_memory_dynamic_max");
     }
 
     public long getMaximumStaticMemory() {
         return value(maximumStaticMemory, "get_memory_static_max");
     }
 
     public void setMaximumStaticMemory(double msmMb) throws BadAPICallException {
         this.maximumStaticMemory = setter((long) msmMb * MEGABYTE, "set_memory_static_max");
     }
 
     public long getMinimumStaticMemory() {
         return minimumStaticMemory;
     }
 
     public String getPoolName() {
         poolName = value(poolName, "get_pool_name");
         return poolName;
     }
 
     public PowerState getPowerState() {
         powerState = value(powerState, "get_power_state");
         return powerState;
     }
 
     public int getVCPUs() {
         return startupVCPUs;
     }
 
     public void setVCPUs(int count, boolean live) throws BadAPICallException {
         if (getPowerState() == PowerState.RUNNING && live) {
             dispatch("set_VCPUs_number_live", count);
             startupVCPUs = count;
         } else {
             startupVCPUs = count;
         }
     }
 
     public int getUserVersion() {
         return userVersion;
     }
 
     public Host getHost() {
         return new Host(value(host, "get_resident_on"));
     }
 
     public Host getAffinityHost() {
         return new Host(hostAffinity);
     }
 
     @Override
     protected Map<String, String> interpretation() {
         HashMap<String, String> map = (HashMap<String, String>) super.interpretation();
         map.put("startupVCPUcount", "VCPUs_at_startup");
         map.put("minimumStaticMemory", "memory_static_min");
         map.put("maximumStaticMemory", "memory_static_max");
         map.put("maximumDynamicMemory", "memory_dynamic_max");
         map.put("minimumDynamicMemory", "memory_dynamic_min");
         map.put("template", "is_a_template");
         map.put("controlDomain", "is_control_domain");
         map.put("domainId", "domid");
         map.put("pvArgs", "PV_args");
         map.put("pvRamdisk", "PV_ramdisk");
         map.put("pvKernel", "PV_kernel");
         map.put("pvBootloader", "PV_bootloader");
         map.put("pvBootloaderArgs", "PV_bootloader_args");
         map.put("hvmBootPolicy", "HVM_boot_policy");
         map.put("hvmBootParams", "HVM_boot_params");
         map.put("startupVCPUs", "VCPUs_at_startup");
         map.put("maxVCPUs", "VCPUs_max");
         map.put("host", "resident_on");
         map.put("hostAffinity", "affinity");
         map.put("vcpuParams", "VCPUs_params");
         map.put("pciBus", "PCI_bus");
 
         return map;
     }
 
     public enum ShutdownAction {
 
         /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * destroy the VM state
          */
         DESTROY,
         /**
          * restart the VM
          */
         RESTART
     };
 
     public enum CrashedAction {
 
         /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * destroy the VM state
          */
         DESTROY,
         /**
          * record a coredump and then destroy the VM state
          */
         COREDUMP_AND_DESTROY,
         /**
          * restart the VM
          */
         RESTART,
         /**
          * record a coredump and then restart the VM
          */
         COREDUMP_AND_RESTART,
         /**
          * leave the crashed VM paused
          */
         PRESERVE,
         /**
          * rename the crashed VM and start a new copy
          */
         RENAME_RESTART
     };
 
     public enum PowerState {
 
         /**
          * The value does not belong to this enumeration
          */
         UNRECOGNIZED,
         /**
          * VM is offline and not using any resources
          */
         HALTED,
         /**
          * All resources have been allocated but the VM itself is paused and its vCPUs are not running
          */
         PAUSED,
         /**
          * Running
          */
         RUNNING,
         /**
          * VM state has been saved to disk and it is no longer running. Note that disks remain in-use while the VM is suspended.
          */
         SUSPENDED
     };
 }
