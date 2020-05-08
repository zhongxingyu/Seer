 package no.uio.master.autoscale;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import no.uio.master.autoscale.config.Config;
 import no.uio.master.autoscale.host.CassandraHost;
 import no.uio.master.autoscale.service.AutoscaleDaemon;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Initial startup for autoscale implementation.
  * @author toraba 
  */
 public class Autoscale {
 	private static Logger LOG = LoggerFactory.getLogger(Autoscale.class);
 	
 	private ScheduledExecutorService executor;
 	private static AutoscaleDaemon instance;
 	
 	/**
 	 * Default parameters
 	 */
	public Autoscale() {
 		LOG.debug("Initializing autoscaling with default properties...");
 		init();
 	}
 	
 	
 	public Autoscale(Integer intervallTimerAgent, Integer intervallTimerScaler, Integer thresholdBreachLimit, Integer minNumberOfNodes, Double minMemoryUsage, Double maxMemoryUsage, Long minDiskSpace, Long maxDiskSpace, String initHost, Integer initPort) {
 		LOG.debug("Initializing autoscaling...");
 		
 		Config.intervall_timer_agent = intervallTimerAgent;
 		Config.intervall_timer_scaler = intervallTimerScaler;
 		Config.threshold_breach_limit = thresholdBreachLimit;
 		Config.min_number_of_nodes = minNumberOfNodes;
 		Config.min_memory_usage = minMemoryUsage;
 		Config.max_memory_usage = maxMemoryUsage;
 		Config.min_disk_space_used = minDiskSpace;
 		Config.max_disk_space_used = maxDiskSpace;
 		Config.getActiveHosts().add(new CassandraHost(initHost, initPort));
 		init();
 	}
 	
 	private void init() {
 		instance = new AutoscaleDaemon();
 		executor = Executors.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(instance, 0, 1, TimeUnit.SECONDS);
 		
 		LOG.debug("Initializing autoscaling complete");
 	}
 	
 	public void shutdownAutoscaler() throws InterruptedException {
 		executor.shutdown();
 		Thread.sleep(1000);
 		executor = null;
 		instance = null;
 	}
 }
