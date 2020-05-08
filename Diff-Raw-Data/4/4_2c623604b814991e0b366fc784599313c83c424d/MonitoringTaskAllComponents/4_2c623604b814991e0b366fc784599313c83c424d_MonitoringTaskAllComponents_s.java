 package org.kevoree.monitoring.strategies;
 
 import org.kevoree.ComponentInstance;
 import org.kevoree.api.Bootstraper;
 import org.kevoree.api.service.core.handler.KevoreeModelHandlerService;
 import org.kevoree.microsandbox.api.communication.MonitoringReporterFactory;
 import org.kevoree.microsandbox.api.event.ContractViolationEvent;
 import org.kevoree.microsandbox.api.event.MonitoringNotification;
 import org.kevoree.microsandbox.api.sla.Metric;
 import org.kevoree.monitoring.comp.MyLowLevelResourceConsumptionRecorder;
 import org.kevoree.monitoring.comp.monitor.GCWatcher;
 import org.kevoree.monitoring.models.SimpleIdAssigner;
 import org.kevoree.monitoring.ranking.ComponentRankerFunctionFactory;
 import org.kevoree.monitoring.ranking.ComponentsInfoStorage;
 import org.kevoree.monitoring.ranking.ComponentsRanker;
 import org.kevoree.monitoring.sla.FaultyComponent;
 import org.kevoree.monitoring.sla.MeasurePoint;
 import org.kevoree.monitoring.strategies.adaptation.KillThemAll;
 import org.kevoree.monitoring.strategies.adaptation.SlowDownComponentInteraction;
 import org.kevoree.monitoring.strategies.monitoring.AllComponentsForEver;
 import org.kevoree.monitoring.strategies.monitoring.FineGrainedMonitoringStrategy;
 import org.kevoree.monitoring.strategies.monitoring.FineGrainedStrategyFactory;
 import org.kevoree.monitoring.strategies.monitoring.RankChecker;
 
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 7/9/13
  * Time: 5:03 PM
  *
  */
 public class MonitoringTaskAllComponents extends AbstractMonitoringTask implements RankChecker {
 
     public MonitoringTaskAllComponents(String nodeName,
                           String nameOfRankerFunction,
                           KevoreeModelHandlerService service,
                           Bootstraper bootstraper) {
         super(bootstraper,service,nameOfRankerFunction,nodeName);
     }
 
 
     @Override
     public void run() {
         System.out.printf("Initiating Monitoring task\n");
 
         ComponentsInfoStorage.object$.getInstance().setIdAssigner(new SimpleIdAssigner(service));
 
         gcWatcher = new GCWatcher();
         gcWatcher.addContractVerificationRequieredListener(this);
         gcWatcher.register();
 
         switchToSimpleLocal(EnumSet.allOf(Metric.class), true);
 
         stopped = false;
         while (!isStopped()) {
             waitMessage();
             if (isStopped()) continue;
             if (currentStrategy.isThereContractViolation()) {
                 currentStrategy.pause();
                 FineGrainedMonitoringStrategy s =(FineGrainedMonitoringStrategy)currentStrategy;
                 List<FaultyComponent> tmpList = s.getFaultyComponents();
                 for (FaultyComponent c : tmpList) {
                     ComponentsInfoStorage.object$.getInstance().getExecutionInfo(c.getComponentPath()).increaseFailures();
                     EnumMap<Metric, MeasurePoint> map = c.getMetrics();
                     for (Metric m : map.keySet())
                         MonitoringReporterFactory.reporter().trigger(
                                 new ContractViolationEvent(c.getComponentPath(),
                                         m, map.get(m).getObserved(), map.get(m).getMax()));
                 }
 
                 // FIXME in Monitoring component, reconfiguration must be avoid. Monitoring event must be sent to something else which is able to take decisions
 //                tmpList = new SlowDownComponentInteraction(service).adapt(nodeName, tmpList);
 //                tmpList = new KillThemAll(service).adapt(nodeName, tmpList);
 
 //                if (tmpList.isEmpty()) {
                     switchToSimpleLocal(EnumSet.allOf(Metric.class),false);
 //                }
 //                else {
 //                    // TODO: the system cannot perform an adaptation. Die
 //                    System.err.println("Why am I here?");
 //                    System.exit(3);
 //                }
             }
         }
 
         currentStrategy.stop();
         gcWatcher.unregister();
         gcWatcher = null;
     }
 
     private void switchToSimpleLocal(EnumSet<Metric> reason, boolean b) {
         MonitoringReporterFactory.reporter().trigger(new MonitoringNotification(false, reason))/*.monitoring(false)*/;
         if (b)
             MyLowLevelResourceConsumptionRecorder.getInstance().turnMonitoring(true,
                     !FineGrainedStrategyFactory.instance$.isSingleMonitoring());
 
         currentStrategy = new AllComponentsForEver( new ArrayList<ComponentInstance>(), msg, this);
         currentStrategy.init(0);
     }
 
 
     @Override
     public List<ComponentInstance> getRanking() {
         try {
 
             return ComponentsRanker.instance$.rank(nodeName, service, bootstraper,nameOfRankerFunction);
         }
         catch (Exception e) {
             return new ArrayList<ComponentInstance>();
         }
     }
 }
