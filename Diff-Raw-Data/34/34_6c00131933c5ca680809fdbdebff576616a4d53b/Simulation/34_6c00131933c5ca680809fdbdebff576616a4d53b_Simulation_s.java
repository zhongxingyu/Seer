 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hpi_web.cloudSim.monitor;
 
 import de.hpi_web.cloudSim.model.ResourceModelCollection;
 import de.hpi_web.cloudSim.profiling.CloudProfiler;
 import de.hpi_web.cloudSim.profiling.builders.DatacenterBuilder;
 import de.hpi_web.cloudSim.profiling.builders.VmBuilder;
 import de.hpi_web.cloudSim.profiling.observer.Observer;
 import de.hpi_web.cloudSim.profiling.utilization.UtilizationThreshold;
 import java.io.File;
 import java.util.Map;
 
 import org.cloudbus.cloudsim.core.CloudSim;
 
 /**
  *
  * @author christoph
  */
 public class Simulation extends Thread {
     private Boolean stopped = false;
     private Observer observer;
     private File running, training;
     private double delay = SimulationDefaults.STEP_DELAY;
     private int vmsAtStart;
     private UtilizationThreshold cpuThreshold;
     private UtilizationThreshold memThreshold;
     private UtilizationThreshold bwThreshold;
     private UtilizationThreshold hdThreshold;
 
     private DatacenterBuilder dcBuilder;
     private VmBuilder vmBuilder;
     private Map<String, ResourceModelCollection> models;
     private double costPerMin;
     private double minPerTurn;
 
     
     Simulation(Observer observer, File training, File running) {
         super();
         this.observer = observer;
         this.running = running;
         this.training = training;
         this.vmsAtStart = 1;
         initializeThresholds();
     }
 
     @Override
     public void run() {
        // TODO: how to safely stop and resume the thread?
        CloudProfiler.start(
                 observer, 
                 delay, 
                 training.getAbsolutePath(), 
                 running.getAbsolutePath(), 
                 cpuThreshold, 
                 memThreshold,
                 bwThreshold,
                 hdThreshold,
                 dcBuilder,
                 vmBuilder,
                 models,
                 vmsAtStart,
                 minPerTurn,
                 costPerMin);
     }
     
     public void stopped(Boolean stopped) {
         this.stopped = stopped;
         CloudSim.terminateSimulation();
         
     }
     
     public void setModels(Map<String, ResourceModelCollection> models) {
         this.models = models;
     }
     
     public Map<String, ResourceModelCollection> getModels() {
         return models;
     }
     
     public double getDelay() {
         return delay;
     }
 
     public void setDelay(double delay) {
         this.delay = delay;
     }
 
     public double getVmsAtStart() {
         return vmsAtStart;
     }
 
     public void setVmsAtStart(int vmsAtStart) {
         this.vmsAtStart = vmsAtStart;
     }
     
     public File getRunning() {
         return running;
     }
 
     public void setRunning(File running) {
         this.running = running;
     }
 
     public File getTraining() {
         return training;
     }
 
     public void setTraining(File training) {
         this.training = training;
     }
 
     public UtilizationThreshold getBwThreshold() {
         return bwThreshold;
     }
 
     public void setBwThreshold(UtilizationThreshold bwThreshold) {
         this.bwThreshold = bwThreshold;
     }
 
     public UtilizationThreshold getCpuThreshold() {
         return cpuThreshold;
     }
 
     public void setCpuThreshold(UtilizationThreshold cpuThreshold) {
         this.cpuThreshold = cpuThreshold;
     }
 
     public UtilizationThreshold getMemThreshold() {
         return memThreshold;
     }
 
     public void setMemThreshold(UtilizationThreshold memThreshold) {
         this.memThreshold = memThreshold;
     }
 
     
     public DatacenterBuilder getDcBuilder() {
         return dcBuilder;
     }
 
     public void setDcBuilder(DatacenterBuilder dcBuilder) {
         this.dcBuilder = dcBuilder;
     }
 
     public UtilizationThreshold getHdThreshold() {
         return hdThreshold;
     }
 
     public void setHdThreshold(UtilizationThreshold hdThreshold) {
         this.hdThreshold = hdThreshold;
     }
 
 
     public VmBuilder getVmBuilder() {
         return vmBuilder;
     }
 
     public void setVmBuilder(VmBuilder vmBuilder) {
         this.vmBuilder = vmBuilder;
     }
     private void initializeThresholds() {
         cpuThreshold = new UtilizationThreshold(SimulationDefaults.MAX_THRESHOLD_CPU, SimulationDefaults.MIN_THRESHOLD_CPU);
         memThreshold = new UtilizationThreshold(SimulationDefaults.MAX_THRESHOLD_MEM, SimulationDefaults.MIN_THRESHOLD_MEM);
         bwThreshold = new UtilizationThreshold(SimulationDefaults.MAX_THRESHOLD_BW, SimulationDefaults.MIN_THRESHOLD_BW);
         hdThreshold = new UtilizationThreshold(SimulationDefaults.MAX_THRESHOLD_HDD, SimulationDefaults.MIN_THRESHOLD_HDD);
     }
 
     void setCostPerMin(double costPerMin) {
         this.costPerMin = costPerMin;
     }
 
     void setMinPerTurn(double minPerTurn) {
         this.minPerTurn = minPerTurn;
     }
 }
