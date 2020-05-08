 package ch.uzh.ddis.katts.monitoring;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher;
 import org.apache.zookeeper.ZooDefs.Ids;
 import org.apache.zookeeper.ZooKeeper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.uzh.ddis.katts.bolts.source.FileTripleReader;
 import ch.uzh.ddis.katts.utils.Cluster;
 
 public class TerminationMonitor implements Watcher {
 
 	private static TerminationMonitor instance;
 
 	private ZooKeeper zooKeeper;
 
 	private Map stormConf;
 	
 	private boolean isTerminated = false;
 
 	public static final String CONF_TERMINATION_CHECK_INTERVAL = "katts.terminationCheckInterval";
 	public static final String KATTS_TERMINATION_ZK_PATH = "/katts_terminated";
 	public static final String KATTS_TUPLES_OUTPUTTED_ZK_PATH = "/katts_number_of_tuples_outputed";
 
 	private List<TerminationWatcher> watchers = new ArrayList<TerminationWatcher>();
 	
 	private Logger logger = LoggerFactory.getLogger(TerminationMonitor.class);
 
 	private TerminationMonitor(Map stormConf) {
 
 		this.stormConf = stormConf;
 		
 		this.connect();
 
 	}
 
 	private synchronized void connect() {
 		try {
 			zooKeeper = Cluster.createZooKeeper(stormConf);
 		} catch (IOException e1) {
 			throw new RuntimeException("Could not create ZooKeeper instance.", e1);
 		}
 		try {
 			zooKeeper.exists(KATTS_TERMINATION_ZK_PATH, this);
 		} catch (KeeperException e) {
 			throw new RuntimeException("Could not add watcher on the termination znode on ZooKeeper.", e);
 		} catch (InterruptedException e) {
 			throw new RuntimeException("Could not add watcher on the termination znode on ZooKeeper.", e);
 		}
 	}
 
 	public static TerminationMonitor getInstance(Map stormConf) {
 		if (instance == null) {
 			instance = new TerminationMonitor(stormConf);
 		}
 
 		return instance;
 	}
 
 	public synchronized void addTerminationWatcher(TerminationWatcher watcher) {
 		watchers.add(watcher);
 
 	}
 
 	public synchronized void terminate(Date terminatedOn) {
 		// If this method is invoked multiple times, we need to make sure, that the barrier is only written the first time.
 		if (!isTerminated) {
 			logger.info("End of file reached.");
 			
 			ZooKeeper zooKeeper;
 			try {
 				zooKeeper = Cluster.createZooKeeper(stormConf);
 			} catch (IOException e1) {
 				throw new RuntimeException("Could not create ZooKeeper instance.", e1);
 			}
 
 			try {
 				zooKeeper.create(KATTS_TERMINATION_ZK_PATH, Long.toString(terminatedOn.getTime()).getBytes(),
 						Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (KeeperException e) {
				if (e.code().equals(KeeperException.Code.NODEEXISTS)) {
					throw new RuntimeException("Can't create the termination barrier in ZooKeeper, because there was already one.", e);
				} else {
					throw new RuntimeException("Can't create the termination ZooKeeper entry.", e);
				}
 			} catch (Exception e) {
 				throw new RuntimeException("The termination barrier could not be written.", e);
 			}
 		}
 		isTerminated = true;
 	
 	}
 
 	@Override
 	public synchronized void process(WatchedEvent event) {
 
 		if (event.getType() == Event.EventType.None) {
 			switch (event.getState()) {
 
 			case Expired:
 				// We need to reconnect
 				this.connect();
 				break;
 			}
 		} else {
 
 			for (TerminationWatcher watcher : this.watchers) {
 				watcher.terminated();
 			}
 		}
 	}
 }
