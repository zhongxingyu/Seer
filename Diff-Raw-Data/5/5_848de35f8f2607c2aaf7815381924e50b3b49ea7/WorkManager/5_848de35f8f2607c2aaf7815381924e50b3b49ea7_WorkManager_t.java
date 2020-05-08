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
 
 import org.remus.RemusApp;
 import org.remus.RemusInstance;
 import org.remus.work.AppletInstance;
 import org.remus.work.RemusApplet;
 import org.remus.work.SimpleAppletInstance;
 import org.remus.work.WorkKey;
 
 public class WorkManager {
 	public static final int QUEUE_MAX = 10000;
 	Map<AppletInstance,Set<WorkKey>>  workQueue;
 	Map<String,Map<AppletInstance,Set<WorkKey>>> workerSets;
 	Map<String,Date> lastAccess;
 
 	public static final int MAX_REFRESH_TIME = 30 * 1000;
 	Map<RemusApplet,Integer> assignRate;
 	Map<RemusApplet,Date> finishTimes;
 
 	RemusApp app;
 	public WorkManager(RemusApp app) {
 		this.app = app;		
 		workQueue = new HashMap<AppletInstance,Set<WorkKey>>();
 		workerSets = new HashMap<String, Map<AppletInstance,Set<WorkKey>>>();
 		lastAccess = new HashMap<String, Date>();
 		finishTimes = new HashMap<RemusApplet,Date>();
 		assignRate = new HashMap<RemusApplet,Integer>();
 	}
 
 
 	public static final long WORKER_TIMEOUT = 5 * 60 * 1000;
 
 	public Map<AppletInstance,Set<WorkKey>> getWorkList( String workerID, int maxCount ) {
 		Date curDate = new Date();
 		synchronized ( lastAccess ) {
 			lastAccess.put(workerID, curDate );
 			synchronized (workerSets) {			
 				//if worker id isn't setup, do so now
 				if ( !workerSets.containsKey( workerID ) ) {
 					workerSets.put(workerID, new HashMap<AppletInstance,Set<WorkKey>>());
 				}
 				//release work from an worker that hasn't checked in within the time limit
 				for ( String worker : lastAccess.keySet() ) {
 					Date last = lastAccess.get(worker);
 					if ( curDate.getTime() - last.getTime() > WORKER_TIMEOUT && workerSets.containsKey(worker)) {
 						workerSets.remove(worker);
 					}
 				}
 			}
 		}		
 		//scan applets for new work that hasn't been assigned to workers
 		if ( workQueue.size() == 0 ) {
 			Map<AppletInstance, Set<WorkKey>> newwork = app.getWorkQueue(QUEUE_MAX);
 			for (AppletInstance ai : newwork.keySet() ) {
 				assert ai != null;
 				for ( WorkKey wk : newwork.get(ai) ) {
 					boolean found = false;
 					for ( Map<AppletInstance, Set<WorkKey> > worker : workerSets.values() ) {
 						if ( worker.containsKey( ai ) && worker.get(ai).contains(wk) ) {
 							found = true;
 						}
 					}
 					if ( !found ) {
 						synchronized (workQueue) {		
 							if ( !workQueue.containsKey(ai) ) {
 								workQueue.put(ai, new HashSet<WorkKey>() );
 							}
 							workQueue.get(ai).add(wk);
 						}
 					}
 				}
 			}
 		}
 		//add jobs to worker's queue 
 		Map<AppletInstance,Set<WorkKey>> wMap = workerSets.get(workerID);
 		synchronized ( workQueue ) {
 			Map<AppletInstance,Integer> workCount = new HashMap<AppletInstance,Integer>();
 			for ( AppletInstance ai : wMap.keySet() ) {
 				workCount.put(ai, wMap.get(ai).size() );
 			}
 			for ( AppletInstance ai : workQueue.keySet() ) {
 				Set<WorkKey> wqSet = workQueue.get(ai);
 				HashSet<WorkKey> addSet = new HashSet<WorkKey>();
 				int maxAssign = 1;
 				if ( !assignRate.containsKey(ai) ) {
 					assignRate.put(ai.applet, 1);
 				} else {
 					maxAssign = assignRate.get(ai);
 				}
 				int wc = 0;
 				if ( workCount.containsKey(ai) )
 					wc = workCount.get(ai);
 				for ( WorkKey wk : wqSet ) {
 					if ( wc < maxAssign ) {
 						addSet.add(wk);
 						wc++;
 					}
 				}
 				workCount.put(ai,wc);
 				wqSet.removeAll(addSet);
 				if ( !wMap.containsKey(ai) )
 					wMap.put(ai, new HashSet<WorkKey>() );
 				wMap.get(ai).addAll(addSet);
 			}
 		}
 		emptyQueues();
 		return workerSets.get(workerID);
 	}
 
 	private void emptyQueues() {
 		Set<AppletInstance> rmSet = new HashSet<AppletInstance>();
 		for ( AppletInstance ai : workQueue.keySet() ) {	
 			Set<WorkKey> wqSet = workQueue.get(ai);
 			if ( wqSet.size() == 0)
 				rmSet.add(ai);
 		}
 		for (AppletInstance ai : rmSet){
 			workQueue.remove(ai);			
 		}	
 	}
 
 	public void errorWork( String workerID, RemusApplet applet, RemusInstance inst, int jobID, String error )	 {
 		synchronized ( lastAccess ) {
 			lastAccess.put(workerID, new Date() );
 		}
 		WorkKey ref = new WorkKey(inst, jobID);
 
 		synchronized (workerSets) {
			AppletInstance ai = new SimpleAppletInstance(applet,inst);
			workerSets.get(workerID).get(ai).remove(ref);
 		}
 		applet.errorWork(inst, jobID, workerID, error);		
 	}
 
 
 
 	public void finishWork( String workerID, RemusApplet applet, RemusInstance inst, int jobID, long emitCount  ) {
 		Date d = new Date();		
 		synchronized (lastAccess) {
 			lastAccess.put(workerID, d );			
 		}
 		AppletInstance ai = new SimpleAppletInstance(applet,inst);
 		synchronized (finishTimes) {
 			Date last = finishTimes.get(ai);
 			synchronized ( assignRate ) {			
 				if ( last != null ) {
 					if ( d.getTime() - last.getTime() < MAX_REFRESH_TIME ) {
 						assignRate.put(ai.applet, assignRate.get(ai) + 1);
 					} else {
 						assignRate.put(ai.applet, Math.max(1, assignRate.get(ai) / 2) );
 					}
 				}
 			}
 			finishTimes.put(ai.applet,d);
 		}
 		WorkKey ref = new WorkKey(inst, jobID);
 		synchronized (workerSets) {
 			workerSets.get(workerID).get(ai).remove(ref);
 			if ( workerSets.get(workerID).get(ai).size() == 0 )
 				workerSets.get(workerID).remove(ai);			
 		}
 		applet.finishWork(inst, jobID, workerID, emitCount);
 	}
 
 
 	public Object getWorkMap(String workerID, int count) {
 		Map<AppletInstance,Set<WorkKey>> workList = getWorkList( workerID, count );						
 		int i = 0;
 		Map<String,Map<String,List>> out = new HashMap();
 		for ( AppletInstance ai : workList.keySet() ) {
 			assert ai != null;
 			assert ai.applet != null;
 			String appletStr = ai.applet.getPath();
 			String instStr = ai.inst.toString();
 			if ( i < count ) {
 				Set<WorkKey> addSet = new HashSet<WorkKey>();
 				for ( WorkKey wk : workList.get(ai) ) {
 					if ( i < count ) {
 						addSet.add(wk);
 						i++;
 					}
 				}
 				Map instMap = new HashMap();
 				instMap.put(instStr, ai.formatWork(addSet) );
 				if ( ! out.containsKey(instStr) ) {
 					out.put( instStr, new HashMap() );
 				}
 				if ( ! out.get( instStr ).containsKey( appletStr ) ) {
 					out.get( instStr ).put( appletStr, new ArrayList());
 				}
 				out.get( instStr ).get( appletStr ).add( ai.formatWork(addSet) );
 			}
 		}
 		return out;
 	}
 
 
 	public Collection<String> getWorkers() {
 		return workerSets.keySet();
 	}
 
 	public int getWorkerActiveCount(String workerID) {
 		int count = 0;
 		for ( Set<WorkKey> set : workerSets.get(workerID).values() ) {
 			count += set.size();
 		}
 		return count;
 	}
 
 	public void touchWorkerStatus(String workerID) {
 		synchronized ( lastAccess ) {
 			lastAccess.put(workerID, new Date() );
 		}		
 	}
 
 	public Date getLastAccess(String workerID) {
 		return lastAccess.get(workerID);
 	}
 
 	public int getWorkBufferSize() {
 		int count = 0;
 		synchronized (workQueue) {			
 			for ( AppletInstance ai : workQueue.keySet() ) {
 				count += workQueue.get(ai).size();	
 			}
 		}
 		return count;
 	}
 
 
 }
