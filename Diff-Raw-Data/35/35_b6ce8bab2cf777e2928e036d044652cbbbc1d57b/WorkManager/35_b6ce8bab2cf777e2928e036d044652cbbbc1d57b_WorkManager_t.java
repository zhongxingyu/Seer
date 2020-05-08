 package org.remus.manage;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.thrift.TException;
 import org.remus.JSON;
 import org.remus.PeerInfo;
 import org.remus.RemusDatabaseException;
 import org.remus.RemusManager;
 import org.remus.RemusWorker;
 import org.remus.core.RemusApp;
 import org.remus.core.RemusApplet;
 import org.remus.core.RemusPipeline;
 import org.remus.core.WorkStatus;
 import org.remus.plugin.PluginInterface;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.BadPeerName;
 import org.remus.thrift.JobStatus;
 import org.remus.thrift.NotImplemented;
 import org.remus.thrift.PeerInfoThrift;
 import org.remus.thrift.PeerType;
 import org.remus.thrift.RemusNet;
 import org.remus.thrift.WorkDesc;
 import org.remus.thrift.WorkMode;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author kellrott
  *
  */
 public class WorkManager extends RemusManager {
 
 	Logger logger;
 	private HashMap<String, PeerInfoThrift> peerMap;
 	private HashMap<String, Long> lastPing;
 
 	@Override
 	public PeerInfo getPeerInfo() {
 		PeerInfo out = new PeerInfo();
 		out.name = "Work Manager";
 		out.peerType = PeerType.MANAGER;
 		return out;
 	}
 
 	@Override
 	public void init(Map params) throws Exception {
 		logger = LoggerFactory.getLogger(WorkManager.class);	
 		workerSets = new HashMap<String, Set<Long>>();
 		activeSet = new HashSet<Long>();
 		assignSet = new HashSet<Long>();
 		assignRate = 1;
 		
 		peerMap = new HashMap<String, PeerInfoThrift>();
 		lastPing = new HashMap<String, Long>();
 		
 		//lastAccess = new HashMap<String, Date>();
 		finishTimes = new HashMap<String,Date>();
 	}
 
 	PluginManager plugins;
 	@Override
 	public void start(PluginManager pluginManager) throws Exception {
 		plugins = pluginManager;		
 		for (PluginInterface pi : pluginManager.getPlugins()) {
 			PeerInfo info = pi.getPeerInfo();
			info.setPeerID(UUID.randomUUID().toString());
			info.setHost(Util.getDefaultAddress());
			info.setPort(pluginManager.addLocalPeer(info.peerID, (RemusNet.Iface) pi));
 			logger.info("Local Peer:" + info.name + " " + info.host + " " + info.port);
 			addPeer(info);
 		}		
 	}
 
 
 	
 	@Override
 	public void addPeer(PeerInfoThrift info) throws BadPeerName, TException, NotImplemented {
 		synchronized (peerMap) {
 			synchronized (lastPing) {
 				if (info.name == null) {
 					throw new BadPeerName();
 				}
 				logger.info("Adding peer: "
 						+ info.name + " (" + info.host + ":" + info.port + ")");
 				peerMap.put(info.name, info);
 				lastPing.put(info.name, (new Date()).getTime());
 			}			
 		}	
 	}
 
 
 	@Override
 	public void delPeer(String peerName) throws TException, NotImplemented {
 		synchronized (peerMap) {
 			peerMap.remove(peerName);
 		}	
 	}
 
 
 	@Override
 	public List<PeerInfoThrift> getPeers() throws TException, NotImplemented {
 		List<PeerInfoThrift> out;
 		synchronized (peerMap) {
 			out = new LinkedList<PeerInfoThrift>();
 			for (PeerInfoThrift pi : peerMap.values()){
 				if (pi != null) {
 					out.add(pi);
 				}
 			}
 		}
 		return out;
 	}
 	
 	/*
 	public void ping(List<PeerInfoThrift> workers) throws TException {
 		logger.info( local.name + " PINGED with " + workers.size() + " records" );
 		boolean added = false;
 		synchronized (peerMap) {
 			synchronized (lastPing) {
 				for (PeerInfoThrift worker : workers) {
 					if (!peerMap.containsKey(worker.name)) {
 						peerMap.put(worker.name, worker);
 						added = true;
 					}
 					if (peerMap.get(worker.name) != null) {
 						lastPing.put(worker.name, (new Date()).getTime());
 					}
 				}
 			}
 		}
 		if (added) {
 			logger.info( local.name + " learned about new peers" );
 			callback.reqPing();
 		}
 	}
 	*/
 	
 	public void flushOld(long oldest) {
 		long minPing = (new Date()).getTime() - oldest;
 		List<String> removeList = new LinkedList<String>();
 		synchronized (peerMap) {
 			synchronized (lastPing) {
 				for (String name : lastPing.keySet()) {
 					if (lastPing.get(name) < minPing) {
 						removeList.add(name);
 					}
 				}
 				for (String name : removeList) {
 					peerMap.put(name, null);
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private WorkStatus activeStack = null;
 	private Set<Long> activeSet;
 	private Set<Long> assignSet;
 
 	private Map<String,Set<Long>> workerSets;
 	//private Map<String,Date> lastAccess;
 	private int assignRate;
 	private HashMap<String, Date> finishTimes;
 
 	public static final int MAX_REFRESH_TIME = 30 * 1000;
 
 	@Override
 	public void scheduleRequest() throws TException, NotImplemented {
 
 		Set<WorkStatus> fullList = new HashSet();
 		try {
 			RemusApp app = new RemusApp(plugins.getDataServer(),plugins.getAttachStore());
 			for (String name : app.getPipelines()) {
 				RemusPipeline pipe = app.getPipeline(name);
 				Set<WorkStatus> curSet = pipe.getWorkQueue();
 				fullList.addAll(curSet);
 			}
 			logger.info("MANAGER found " + fullList.size() + " active stacks");
 		} catch (RemusDatabaseException e) {
 			throw new TException(e);
 		}
 
 		for (RemusWorker worker : plugins.getWorkers()) {
 			PeerInfo pi = worker.getPeerInfo();
 			List<String> langs = pi.workTypes;
 			logger.info("Worker " + pi.name + " offers " + langs);
 			for (WorkStatus stat : fullList){
 				if (langs.contains(stat.getApplet().getType())) {
 					logger.info("Assigning " + stat + " to " + pi.name);
 					WorkDesc wdesc = new WorkDesc();
 					wdesc.workStack = new AppletRef(
 							stat.getPipeline().getID(), 
 							stat.getInstance().toString(), 
 							stat.getApplet().getID());
 					wdesc.lang = stat.getApplet().getType();
 					wdesc.infoJSON = JSON.dumps(stat.getInstanceInfo());
 
 					switch (stat.getApplet().getMode()) {
 					case RemusApplet.MAPPER: {
 						wdesc.mode = WorkMode.MAP;
 						break;
 					}
 					case RemusApplet.REDUCER: {
 						wdesc.mode = WorkMode.REDUCE;
 						break;
 					}
 					default: {}
 					}
 
 					Collection<Long> jobs;
 					do {
 						jobs = stat.getReadyJobs(10);
 						logger.info("Found " + jobs.size() + " jobs");
 						if (jobs.size() > 0) {
 							wdesc.jobs = new ArrayList(jobs);
 							String jobID = worker.jobRequest("test", wdesc);
 
 							boolean done = false;
 							do {
 								JobStatus s = worker.jobStatus(jobID);
 								if (s == JobStatus.DONE) {
 									for (long job : jobs) {
 										stat.finishJob(job, pi.name);
 									}
 									done = true;
 								} else if (s == JobStatus.ERROR) {
 									for (long job : jobs) {
 										stat.errorJob(job, "javascript error");
 									}
 									done = true;
 								}
 							} while (!done);
 						}
 					} while (jobs.size() > 0);
 
 				}
 			}
 		}
 	}
 
 
 	void fillWorkerSet(String workerID) {
 		do {
 			if ( activeStack == null ) {
 				//activeStack = parent.requestWorkStack(this, codeTypes);
 				if ( activeStack == null )
 					return;
 			}
 			if (activeStack.isComplete()) {
 				//parent.completeWorkStack(activeStack);
 				activeStack = null;
 			}
 		} while ( activeStack == null );
 
 		if ( activeStack != null ) {
 			Set<Long> workerMap = workerSets.get( workerID );
 			if ( workerMap == null ) {
 				workerMap = new HashSet<Long>();
 				workerSets.put(workerID, workerMap);
 			}
 			if ( activeSet.size() + assignSet.size() < assignRate ) {
 				Collection<Long> workSet = activeStack.getReadyJobs( workerSets.size() * assignRate );
 				activeSet.addAll( workSet );
 				activeSet.removeAll( assignSet );
 			}
 			while ( workerMap.size() < assignRate && activeSet.size() > 0) {
 				long id = activeSet.iterator().next();
 				workerMap.add( id );
 				activeSet.remove( id );
 				assignSet.add( id );
 			}
 		}
 	}
 
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	/*
 	 while ((curline = br.readLine()) != null) {
 				Map m = (Map)JSON.loads( curline );
 				for ( Object instObj : m.keySet() ) {
 					for ( Object appletObj : ((Map)m.get(instObj)).keySet() ) {
 						List jobList = (List)((Map)m.get(instObj)).get(appletObj);
 						for ( Object key2 : jobList ) {
 							long jobID = Long.parseLong( key2.toString() );
 							//TODO:add emit id count check
 							activeStack.finishJob( jobID, workerID );
 							synchronized ( finishTimes ) {			
 								Date d = new Date();		
 								Date last = finishTimes.get( workerID );
 								if ( last != null ) {
 									if ( d.getTime() - last.getTime() > MAX_REFRESH_TIME ) {
 										assignRate /= 2;
 									}
 									assignRate++;
 								}
 								finishTimes.put(workerID, d);
 							}
 
 						}
 
 					}
 				}
 			}
 
 	 */
 
 
 
 
 }
