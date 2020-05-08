 package no.uio.master.autoscale;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import me.prettyprint.hector.api.Cluster;
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
 	public Autoscale(Cluster c) {
 		LOG.info("Initializing autoscaling with default properties...");
 		instance = new AutoscaleDaemon(c);
 		instance.getConfig().cluster = c;
 		init();
 	}
 	
 	
	public Autoscale(Cluster c, Integer intervallTimer, Integer thresholdBreachLimit, Integer minNumberOfNodes, Double minMemoryUsage, Double maxMemoryUsage, Long minDiskSpace, Long maxDiskSpace) {
 		LOG.info("Initializing autoscaling...");
 		instance = new AutoscaleDaemon(c);
 		
 		instance.getConfig().intervall_timer = intervallTimer;
 		instance.getConfig().threshold_breach_limit = thresholdBreachLimit;
 		instance.getConfig().min_number_of_nodes = minNumberOfNodes;
 		instance.getConfig().min_memory_usage = minMemoryUsage;
 		instance.getConfig().max_memory_usage = maxMemoryUsage;
 		instance.getConfig().min_free_disk_space = minDiskSpace;
 		instance.getConfig().max_free_disk_space = maxDiskSpace;
 		instance.getConfig().cluster = c;
 		
 		init();
 	}
 	
 	private void init() {
 		
 		executor = Executors.newSingleThreadScheduledExecutor();
 		executor.scheduleAtFixedRate(instance, 0, instance.getConfig().intervall_timer, TimeUnit.SECONDS);
 		
 		LOG.info("Initializing autoscaling complete");
 	}
 }
