 package org.remus.manage;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.thrift.TException;
 import org.remus.JSON;
 import org.remus.PeerInfo;
 import org.remus.RemusAttach;
 import org.remus.RemusDB;
 import org.remus.RemusDatabaseException;
 import org.remus.RemusManager;
 import org.remus.core.AppletInstance;
 import org.remus.core.AppletInstanceStack;
 import org.remus.core.BaseStackNode;
 import org.remus.core.PipelineSubmission;
 import org.remus.core.RemusApp;
 import org.remus.core.RemusApplet;
 import org.remus.core.RemusInstance;
 import org.remus.core.RemusMiniDB;
 import org.remus.core.RemusPipeline;
 import org.remus.plugin.PeerManager;
 import org.remus.plugin.PluginManager;
 import org.remus.thrift.AppletRef;
 import org.remus.thrift.Constants;
 import org.remus.thrift.JobState;
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
 
 	/**
 	 * A map of active applet instances to the peerIDs
 	 * of the workers doing jobs for this applet.
 	 */
 	private Map<AppletInstance, Set<RemoteJob>> activeStacks;
 	private Map<String, Set<RemoteJob>> peerStacks;
 	private Map<AppletInstance, Integer> assignRate;
 	private Logger logger;
 	private RemusMiniDB miniDB;
 
 	private RemusNet.Iface db, attach;
 
 	PeerManager peerManager;
 
 	public static int INACTIVE_SLEEP_TIME = 30000;
 
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
 		activeStacks = new HashMap<AppletInstance, Set<RemoteJob>>();
 		peerStacks = new HashMap<String, Set<RemoteJob>>();
 		assignRate = new HashMap<AppletInstance, Integer>();
 	}
 
 	@Override
 	public void start(PluginManager pluginManager) throws Exception {
 		peerManager = pluginManager.getPeerManager();
 		/**
 		 * The miniDB is a shim placed infront of the database, that will allow you
 		 * to add additional, dynamic, applets to the database. For the work manager
 		 * it is used to create the '/@agent' applets, which view all the applet instance
 		 * records as a single stack
 		 */
 
 		db = peerManager.getPeer(peerManager.getDataServer());
 		attach = peerManager.getPeer(peerManager.getAttachStore());			
 
 		miniDB = new RemusMiniDB(peerManager.getPeer(peerManager.getDataServer()));
 		setupAIStack();
 		miniDB.addBaseStack("@activity", new BaseStackNode() {			
 			@Override
 			public List<String> keySlice(String keyStart, int count) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 			
 			@Override
 			public List<String> getValueJSON(String key) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 			
 			@Override
 			public void delete(String key) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public boolean containsKey(String key) {
 				// TODO Auto-generated method stub
 				return false;
 			}
 			
 			@Override
 			public void add(String key, String data) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		sThread = new ScheduleThread();
 		sThread.start();
 	}
 
 
 	private void setupAIStack() throws RemusDatabaseException, TException {		
 		RemusApp app = new RemusApp(db, attach);		
 		miniDB.reset();
 		for (String pipeline : app.getPipelines()) {		
 			AppletInstanceStack aiStack = new AppletInstanceStack(db, attach, pipeline) {
 				@Override
 				public void add(String key, String data) {}				
 			};
 			miniDB.addBaseStack("/@agent?" + pipeline, aiStack);
 		}
 	}
 
 	private class RemoteJob {
 		private String peerID;
 		private String jobID;
 		private long workStart;
 		private long workEnd;
 		private long startTime;
 		RemoteJob(String peerID, String jobID, long workStart, long workEnd) {
 			this.peerID = peerID;
 			this.jobID = jobID;
 			this.workStart = workStart;
 			this.workEnd = workEnd;
 			this.startTime = (new Date()).getTime();
 		}
 
 		@Override
 		public int hashCode() {
 			return peerID.hashCode() + jobID.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof RemoteJob) {
 				return equals((RemoteJob) obj);
 			}
 			return super.equals(obj);
 		}
 
 		public boolean equals(RemoteJob job) {
 			if (job.peerID.equals(peerID) && job.jobID.equals(jobID)) {
 				return true;
 			}
 			return false;
 		}
 
 	}
 
 	private void syncAppletList(Set<AppletInstance> aiSet) {
 		Set<AppletInstance> removeSet = new HashSet<AppletInstance>();		
 		synchronized (activeStacks) {		
 			for (AppletInstance ai : aiSet) {
 				if (!activeStacks.containsKey(ai)) {
 					activeStacks.put(ai, new HashSet<RemoteJob>());
 					assignRate.put(ai, 1);
 				}
 			}
 			for (AppletInstance ai : activeStacks.keySet()) {
 				if (!aiSet.contains(ai)) {
 					removeSet.add(ai);
 				}
 			}
 		}
 
 		for (AppletInstance ai : removeSet) {
 			for (RemoteJob rj : activeStacks.get(ai)) {
 				removeRemoteJob(ai, rj, false);
 			}
 			synchronized (activeStacks) {
 				activeStacks.remove(ai);				
 			}
 			synchronized (workIDCache) {
 				workIDCache.remove(ai);				
 			}
 		}
 
 	}
 
 	private void addRemoteJob(AppletInstance ai, RemoteJob rj) {
 		activeStacks.get(ai).add(rj);
 		if (!peerStacks.containsKey(rj.peerID)) {
 			peerStacks.put(rj.peerID, new HashSet<RemoteJob>());
 		}
 		peerStacks.get(rj.peerID).add(rj);
 	}
 
 	private void removeRemoteJob(AppletInstance ai, RemoteJob rj, boolean adjustAssignRate) {
 		if (adjustAssignRate) {
 			int newAssignRate = assignRate.get(ai);
 			long runTime = (new Date()).getTime() - rj.startTime;
 			if (runTime > MAX_REFRESH_TIME) {
 				newAssignRate /= 2;
 			}
 			newAssignRate++;
 			logger.info("ASSIGN RATE: " + ai + " " + newAssignRate);
 			assignRate.put(ai, newAssignRate);
 		}
 		try {
 			RemusNet.Iface worker = peerManager.getPeer(rj.peerID);					
 			worker.jobCancel(rj.jobID);	
 			peerManager.returnPeer(worker);
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		} catch (TException e) {
 			e.printStackTrace();
 		}
 		peerStacks.get(rj.peerID).remove(rj);
 		activeStacks.get(ai).remove(rj);
 	}
 
 
 
 	public static final int MAX_REFRESH_TIME = 30 * 1000;
 
 	private ScheduleThread sThread;
 
 	private class ScheduleThread extends Thread {
 
 		public ScheduleThread() {
 			super("Manager Schedule Thread");
 		}
 
 		boolean quit = false;
 		Integer waitLock = new Integer(0);
 		private int sleepTime = 300;
 
 		@Override
 		public void run() {
 			while (!quit) {
 				Boolean workChange = false;
 				try {
 					//First, scan all of the stacks to find active worksets
 					scanJobs();
 					//scan the workers, start assigning untouched work to workers that aren't over limit
 					if (workSchedule()) {
 						workChange = true;
 					}
 					//collect and clean finished jobs
 					try {
 						if (cleanJobs()) {
 							workChange = true;
 						}
 					} catch (TException e) {
 						e.printStackTrace();
 					} catch (NotImplemented e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} catch (Exception e) {
 					logger.error(e.getMessage());
 				}
 				try {
 					if (!workChange) {
 						if (sleepTime < INACTIVE_SLEEP_TIME) {
 							sleepTime += 1000;
 						}
 					} else {
 						sleepTime = 100;
 					}
 					logger.debug("Manager SleepTime=" + sleepTime);
 					synchronized (waitLock) {
 						waitLock.wait(sleepTime);
 					}					
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 		public void touch() {
 			synchronized (waitLock) {
 				waitLock.notifyAll();	
 			}
 		}
 
 		public void quit() {
 			quit = true;
 		}
 
 	}
 
 	private void scanJobs() {
 		try {
 			RemusApp app = new RemusApp(RemusDB.wrap(db), RemusAttach.wrap(attach));
 			int activeCount = 0;
 			Set<AppletInstance> fullSet = new HashSet<AppletInstance>();
 			for (String name : app.getPipelines()) {
 				List<RemusInstance> instList = new LinkedList<RemusInstance>();
 				RemusPipeline pipe = app.getPipeline(name);
 				for (String subKey : pipe.getSubmits()) {
 					PipelineSubmission subData = pipe.getSubmitData(subKey);
 					if (checkSubmission(pipe, subKey, subData)) {
 						pipe.setSubmit(subKey,subData);
 						pipe.setInstanceSubkey(subData.getInstance(), subKey);
 					}
 					instList.add(subData.getInstance());
 				}
 				for (RemusInstance inst : instList) {
 					Set<AppletInstance> curSet = pipe.getActiveApplets(inst);
 					activeCount += curSet.size();
 					fullSet.addAll(curSet);
 				}
 				//check for work that needs to be instanced
 				for ( String appletName : pipe.getMembers() ) {
 					RemusApplet applet = pipe.getApplet(appletName);
 					if (applet.getMode() != RemusApplet.STORE) {
 						for (RemusInstance inst : instList) {
 							if (!pipe.hasAppletInstance(inst, appletName)) {
 								boolean inputFound = false;
 								for (String input : applet.getSources()) {
 									if (pipe.hasAppletInstance(inst, input)) {
 										inputFound = true;
 									}
 									if (inputFound) {
 										AppletInstance src = pipe.getAppletInstance(inst, applet.getSource());
 										if (src != null) {
 											PipelineSubmission info = src.getInstanceInfo();
 											applet.createInstance(info, inst);
 										} else {
 											//TODO: init new work!!!
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			syncAppletList(fullSet);
 			if (activeCount > 0) {
 				logger.info("MANAGER found " + activeCount + " active stacks");
 			}
 		} catch (RemusDatabaseException e) {
 			e.printStackTrace();
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean checkSubmission(RemusPipeline pipe, String subKey, PipelineSubmission subData) 
 	throws RemusDatabaseException, TException, NotImplemented {
 		Boolean changed = false;
 		if (!subData.hasSubmitKey() || subData.getSubmitKey().compareTo(subKey) != 0 ) {
 			subData.setSubmitKey(subKey);
 			changed = true;
 		}
 		if (!subData.hasInstance()) {
 			subData.setInstance(new RemusInstance());
 			changed = true;
 		} else {
 			//If the instance field doesn't match the key stored in the '@instance' stack
 			String instKey = pipe.getSubKey(subData.getInstance());
 			if (instKey != null && subKey.compareTo(instKey) != 0) {
 				subData.setInstance(new RemusInstance());
 				changed = true;				
 			}
 		}
 
 		for (String applet : subData.getInitApplets()) {
 			if (!pipe.hasAppletInstance(subData.getInstance(), applet)) {
 				RemusApplet ap = pipe.getApplet(applet);
				ap.createInstance(subData, subData.getInstance());
 			}
 		}					
 		return changed;
 	}
 
 	private boolean cleanJobs() throws TException, NotImplemented {
 		boolean found = false;
 		synchronized (activeStacks) {
 			int activeCount = 0;
 			for (AppletInstance ai : activeStacks.keySet()) {			
 				Map<RemoteJob, Boolean> removeSet = new HashMap<RemoteJob, Boolean>();
 				for (RemoteJob rj : activeStacks.get(ai)) {
 					logger.debug("Checking " + rj.peerID + " for " + rj.jobID + " " + ai);
 					try {
 						RemusNet.Iface worker = peerManager.getPeer(rj.peerID);
 						if (worker != null) {
 							JobStatus s = worker.jobStatus(rj.jobID);
 							if (s.status == JobState.DONE) {
 								found = true;
 								for (long i = rj.workStart; i < rj.workEnd; i++) {
 									ai.finishWork(i, rj.peerID, s.emitCount);
 								}
 								removeSet.put(rj, true);
 								logger.info("Worker Finished: " + rj.jobID);
 							} else if (s.status == JobState.ERROR) {
 								found = true;
 								for (long i = rj.workStart; i < rj.workEnd; i++) {
 									ai.errorWork(i, s.errorMsg);
 								}
 								removeSet.put(rj, false);
 								logger.warn("JOB ERROR: " + ai + " job:" + rj.jobID + " " + s.errorMsg);
 							} else if (s.status == JobState.UNKNOWN) {
 								logger.warn("Worker lost job: " + rj.jobID);
 							} else {
 								activeCount += 1;
 							}
 							peerManager.returnPeer(worker);
 						}
 					} catch (TException e) {
 						logger.warn("Worker Connection Failed: " + rj.peerID);
 						removeSet.put(rj, false);
 						peerManager.peerFailure(rj.peerID);
 					}
 				}
 				for (RemoteJob rj : removeSet.keySet()) {
 					removeRemoteJob(ai, rj, removeSet.get(rj));
 				}
 			}
 			if (activeCount > 0) {
 				logger.info("Active RemoteJobs: " + activeCount);			
 			}
 		}
 		return found;
 	}
 
 
 	private Map<AppletInstance,long []> workIDCache = new HashMap<AppletInstance, long[]>();
 
 	private long [] getAppletWorkCache(AppletInstance ai) throws NotImplemented, TException {
 		Set<String> peers = peerManager.getWorkers();
 		long [] workIDs = null;
 		synchronized (workIDCache) {			
 			if (workIDCache.containsKey(ai) && workIDCache.get(ai) != null) {
 				workIDs = workIDCache.get(ai);
 				Arrays.sort(workIDs);
 			} else {
 				workIDs = ai.getReadyJobs(assignRate.get(ai) * peers.size());				
 				workIDCache.put(ai, workIDs);
 			}
 		}
 		for (String peerID : peers) {
 			if (!peerStacks.containsKey(peerID)) {
 				peerStacks.put(peerID, new HashSet<RemoteJob>());
 			}
 			for (RemoteJob rj : peerStacks.get(peerID)) {
 				if (activeStacks.get(ai).contains(rj)) {
 					for (int i = 0; i < workIDs.length; i++) {
 						if (workIDs[i] >= rj.workStart && workIDs[i] < rj.workEnd) {
 							workIDs[i] = -1;
 						}
 					}
 				}
 			}
 		}
 
 		boolean valid = false;
 		for (int i = 0; i < workIDs.length; i++) {
 			if (workIDs[i] != -1) {
 				valid = true;
 			}
 		}
 		if (valid) {
 			Arrays.sort(workIDs);
 			return workIDs;
 		} 
 		workIDCache.put(ai, null);
 		return null;
 	}
 
 	private boolean workSchedule() {
 		boolean workAdded = false;
 
 		try {
 			Set<String> peers = peerManager.getWorkers();
 			for (String peerID : peers) {
 				if (!peerStacks.containsKey(peerID)) {
 					peerStacks.put(peerID, new HashSet<RemoteJob>());
 				}
 				if (peerStacks.get(peerID).size() == 0) {
 					//find an applet stack with a matching worktype					
 					PeerInfoThrift pinfo = peerManager.getPeerInfo(peerID);
 					AppletInstance ai = null;
 					long [] workIDs = null;
 					if (pinfo != null && pinfo.workTypes != null) {
 						synchronized (activeStacks) {						
 							for (AppletInstance acur : activeStacks.keySet()) {	
 								if (ai == null && acur.getApplet() != null) {
 									if (pinfo.workTypes.contains(acur.getApplet().getType())) {
 										workIDs = getAppletWorkCache(acur);
 										if (workIDs != null) {
 											ai = acur;
 										}
 									}
 								}
 							}
 						}
 					}
 					if (ai != null) {
 						if (workIDs != null) {
 							int curPos = 0;
 							while (curPos < workIDs.length) {
 								if (workIDs[curPos] == -1) {
 									curPos++;
 								} else {
 									int last = curPos;
 									do {
 										curPos++;
 									} while (curPos < workIDs.length && workIDs[curPos] - workIDs[last] == curPos - last);						
 									workAssign(ai, peerID, workIDs[last], workIDs[curPos - 1] + 1);
 									for (int i = last; i < curPos; i++) {
 										workIDs[i] = -1;
 									}
 									workAdded = true;
 								}
 							}
 						}
 					}
 				}
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return workAdded;
 	}
 
 	private void workAssign(AppletInstance ai, String peerID, long workStart, long workEnd) {
 		try {
 			PipelineSubmission instanceInfo = ai.getInstanceInfo();
 			if (instanceInfo == null) {
 				return;
 			}
 			WorkDesc wdesc = new WorkDesc();
 			wdesc.setWorkStack(new AppletRef());		
 			wdesc.workStack.setPipeline(ai.getPipeline().getID());
 			wdesc.workStack.setInstance(ai.getInstance().toString());
 			wdesc.workStack.setApplet(ai.getApplet().getID());
 			wdesc.setWorkStart(workStart);
 			wdesc.setWorkEnd(workEnd);
 			wdesc.setLang(ai.getApplet().getType());
 			logger.info("Assigning " + ai + ":" + workStart + "-" + workEnd + " to " + peerID + " " + wdesc.workStack);
 			wdesc.setInfoJSON(JSON.dumps(instanceInfo));
 
 			RemusNet.Iface worker = peerManager.getPeer(peerID);
 			if (worker == null) {
 				return;
 			}
 			int mode = ai.getApplet().getMode();
 			if (mode == WorkMode.AGENT.getValue()) {
 				logger.info("Agent Operation");
 				wdesc.setMode(WorkMode.MAP);				
 				setupAIStack();
 				String jobID = worker.jobRequest(peerManager.getPeerID(this), peerManager.getAttachStore(), wdesc);
 				synchronized (activeStacks) {
 					addRemoteJob(ai, new RemoteJob(peerID, jobID, workStart, workEnd));
 				}				
 			} else {
 				wdesc.setMode(WorkMode.findByValue(mode));
 				String jobID = worker.jobRequest(peerManager.getDataServer(), peerManager.getAttachStore(), wdesc);
 				synchronized (activeStacks) {
 					addRemoteJob(ai, new RemoteJob(peerID, jobID, workStart, workEnd));
 				}
 			}			
 			peerManager.returnPeer(worker);
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (NotImplemented e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (RemusDatabaseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void scheduleRequest() throws TException, NotImplemented {
 		sThread.touch();
 	}
 
 
 	@Override
 	public void stop() {
 		sThread.quit();
 	}
 
 	@Override
 	public String scheduleInfoJSON() throws NotImplemented, TException {
 		Map out = new HashMap();
 		synchronized (activeStacks) {
 			int outCount = activeStacks.size();
 			out.put("activeCount", Integer.toString(outCount));			
 			Map aiMap = new HashMap();
 			for (AppletInstance ai : activeStacks.keySet()) {
 				List o = new LinkedList();
 				for (RemoteJob rj : activeStacks.get(ai)) {
 					o.add(rj.peerID);
 				}
 				aiMap.put(ai.toString(), o);
 			}
 			out.put("active", aiMap);
 		}		
 		return JSON.dumps(out);
 	}
 
 	@Override
 	public List<String> keySlice(AppletRef stack, String keyStart, int count)
 	throws NotImplemented, TException {
 		logger.debug("Manage DB keySlice: " + stack + " " + keyStart + " " + count);
 		return miniDB.keySlice(stack, keyStart, count);
 	}
 
 	@Override
 	public boolean containsKey(AppletRef stack, String key)
 	throws NotImplemented, TException {
 		logger.debug("Manage DB containsKey: " + stack + " " + key);
 		return miniDB.containsKey(stack, key);	
 	}
 
 	@Override
 	public List<String> getValueJSON(AppletRef stack, String key)
 	throws NotImplemented, TException {
 		logger.debug("Manage DB getValueJSON: " + stack + " " + key);
 		return miniDB.getValueJSON(stack, key);
 	}
 
 	@Override
 	public void addDataJSON(AppletRef stack, long jobID, long emitID, String key,
 			String data) throws NotImplemented, TException {
 		logger.debug("Manage DB add: " + stack + " " + key);
 
 		if (stack.applet.compareTo(Constants.SUBMIT_APPLET) == 0) {
 			try {
 				PipelineSubmission subData = new PipelineSubmission(JSON.loads(data));
 				RemusApp app = new RemusApp(RemusDB.wrap(db), RemusAttach.wrap(attach));
 				RemusPipeline pipe = app.getPipeline(stack.pipeline);
 				if (pipe != null) {
 					if (checkSubmission(pipe, key, subData)) {
 						pipe.setSubmit(key, subData);
 						pipe.setInstanceSubkey(subData.getInstance(), key);
 					}	
 				} 
 			} catch (RemusDatabaseException e) {
 				throw new TException(e);
 			}
 		} else {			
 			miniDB.addDataJSON(stack, jobID, emitID, key, data);
 		}
 	}
 
 	@Override
 	public String status() throws TException {
 		return "OK";
 	}
 }
