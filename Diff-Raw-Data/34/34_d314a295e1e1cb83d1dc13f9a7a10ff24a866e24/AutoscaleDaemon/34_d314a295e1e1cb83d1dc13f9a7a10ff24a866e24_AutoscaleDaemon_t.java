 package no.uio.master.autoscale.service;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.HashSet;
 
 import me.prettyprint.cassandra.connection.HConnectionManager;
 import me.prettyprint.cassandra.service.CassandraClientMonitor;
 import me.prettyprint.cassandra.service.CassandraHost;
 import me.prettyprint.cassandra.service.JmxMonitor;
 import me.prettyprint.hector.api.Cluster;
 import no.uio.master.autoscale.cassandra.CassandraHostManager;
 import no.uio.master.autoscale.config.Config;
import no.uio.master.autoscale.model.SlaveMessage;
import no.uio.master.autoscale.model.enumerator.SlaveMessageType;
 import no.uio.master.autoscale.node.NodeMonitor;
 import no.uio.master.autoscale.slave.SlaveCommunicator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The runnable instance of Autoscale, for external usage.
  * @author toraba
  * 
  */
 public class AutoscaleDaemon implements Runnable {
 	private static Logger LOG = LoggerFactory.getLogger(AutoscaleDaemon.class);
 	private static HConnectionManager connectionManager;
 	private static CassandraClientMonitor monitor;
 	private static Config config = new Config();
 	private static CassandraHostManager nodeManager;
 	private static NodeMonitor nodeMonitor;
 	
 	private static Socket socket;
 	/**
 	 * Initialize autoscaler
 	 * @param clusterName
 	 * @param host
 	 */
 	public AutoscaleDaemon(Cluster c) {
 		init(c);
 		LOG.debug("Daemon started");
 	}
 	
 	private void init(Cluster c) {
 		connectionManager = c.getConnectionManager();
 		nodeManager = new CassandraHostManager(connectionManager);
 		nodeManager.setInactiveNodes(new HashSet<CassandraHost>(0));
 		nodeManager.setActiveNodes(connectionManager.getHosts());
 		monitor = JmxMonitor.getInstance().getCassandraMonitor(connectionManager);
 		nodeMonitor = new NodeMonitor(nodeManager.getActiveNodes());
 		initializeSlaves();
 	}
 	
 	public Config getConfig() {
 		return config;
 	}
 	
 	@Override
 	public void run() {
		//String msg = "InactiveNodes: " + nodeManager.getNumberOfInactiveHosts() + ", ActiveNodes: " + nodeManager.getNumberOfActiveHosts();
		//	msg += "\n Heap usage: " + nodeMonitor.getHeapMemoryUsage("127.0.0.1");
 		//LOG.debug(msg);
 			LOG.debug("Daemon running...");
 	}
 	
 	/**
 	 * Send initialization-message to all slaves.
 	 */
 	public void initializeSlaves() {
 		
 		// Construct message
 		SlaveMessage slaveMsg = new SlaveMessage(SlaveMessageType.INITIALIZATION);
 		slaveMsg.put("intervall_timer", getConfig().intervall_timer);
 		slaveMsg.put("threshold_breach_limit", getConfig().threshold_breach_limit);
 		slaveMsg.put("min_memory_usage", getConfig().min_memory_usage);
 		slaveMsg.put("max_memory_usage", getConfig().max_memory_usage);
 		slaveMsg.put("min_free_disk_space",getConfig().min_free_disk_space);
 		slaveMsg.put("max_free_disk_space",getConfig().max_free_disk_space);
		slaveMsg.put("storage_location", getConfig().storage_location);
 		
 		// Send message to all slaves
 		for(CassandraHost host : nodeManager.getActiveNodes()) {
 			try {
 				socket = new Socket(host.getHost(),7799);
 			} catch (IOException e) {
 				LOG.error("Failed to init connection with ");
 			}
 			SlaveCommunicator.sendMessage(slaveMsg, socket);
 		}
 	}
 
 	
 }
