 package de.unisiegen.informatik.bs.alvis.vm;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 /**
  * Class capsulating Algo with all Information needed for handling plus methods
  * to move the thread forward and backward
  * 
  * TODO DP Handling
  * 
  * @author Dominik Dingel
  * 
  */
 
 public class AlgoThread {
 	// BreakPointlisteners, to be identified in case of Breakpoint
 	private ArrayList<BPListener> bpListeners;
 
 	// a arraylist of pcobjects
 	// to be passed as start parameters, these can to enable shared access also
 	// be references to the PCObjects of an other Algo
 	private ArrayList<PCObject> parameters;
 
 	// Current Thread Object of the Algo
 	private Thread algoThread;
 
 	// Current AbstractAlgo Object
 	private AbstractAlgo algoInst;
 
 	// Current Class Object
 	private Class<AbstractAlgo> algoClass;
 
 	// Saving to a Line Number a counter how often we hit this
 	private HashMap<Integer, Integer> lineCounter;
 
 	// last LineCounter, comes handy when stepping backwards, sometimes
 	// containing computed values instead of realworld
 	private HashMap<Integer, Integer> lastCounter;
 
 	// helper to decide if we are currently on break, or moving backwards
 	private boolean onBreak;
 
 	/**
 	 * Creates new AlgoThread, will directly load the fileName, create the Class
 	 * Inst and the Thread Object
 	 * 
 	 * @param key
 	 * @param fileName
 	 */
 	public AlgoThread(String fileName) throws ClassNotFoundException {
 		bpListeners = new ArrayList<BPListener>();
 		lineCounter = new HashMap<Integer, Integer>();
 		lastCounter = new HashMap<Integer, Integer>();
 		parameters = null;

 		loadAlgo(fileName);
 		createThread();
 	}
 
 	/**
 	 * Set the start parameters should be called before the algo started
 	 * 
 	 * @param paras
 	 */
 	public void setParameters(ArrayList<PCObject> paras) {
		algoInst.setParameters(paras);
 		parameters = paras;
 	}
 
 	/**
 	 * deletes the parameters
 	 */
 	public void deleteParamters() {
 		parameters = null;
 	}
 
 	/**
 	 * add BP Listener, if the current one is already listed, the request will
 	 * be ignored
 	 * 
 	 * @param wantsToListen
 	 */
 	public void addBPListener(BPListener wantsToListen) {
 		// if bplistener already is in the to notify list, just ignore the wish
 		// "one cross each"
 		if (bpListeners.contains(wantsToListen)) {
 			return;
 		}
 		bpListeners.add(wantsToListen);
 	}
 
 	/**
 	 * bpListeners to remove
 	 * 
 	 * @param toRemove
 	 */
 	public void removeBPListener(BPListener toRemove) {
 		bpListeners.remove(toRemove);
 	}
 
 	/**
 	 * removes all BP Listeners
 	 */
 	public void removeBPListeners() {
 		bpListeners.clear();
 	}
 
 	/**
 	 * gives Current ThreadState
 	 * 
 	 * @return ThreadState Object
 	 */
 	public Thread.State getCurrentThreadState() {
 		return algoThread.getState();
 	}
 
 	/**
 	 * Passes over the needed Parameter Types for the specific Algo
 	 * 
 	 * @return ArrayList with PCObject ArrayList, should be never a nullpointer
 	 */
 	public ArrayList<PCObject> getParameterTypes() {
 		return algoInst.getParameterTypes();
 	}
 
 	/**
 	 * helper function to create algo Thread from class object
 	 */
 	private void createThread() {
 		try {
 			algoInst = (AbstractAlgo) algoClass.getConstructors()[0]
 					.newInstance();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		algoThread = new Thread(algoInst);
 	}
 
 	/**
 	 * loads the Algo key from the file named fileName
 	 * 
 	 * @param key
 	 * @param fileName
 	 */
 	private void loadAlgo(String fileName) throws ClassNotFoundException {
 		DynaCode dynacode = new DynaCode();
 		dynacode.addSourceDir(new File("src"));
 		// set aA to the loaded class
 		algoClass = dynacode.loadClass(fileName);
 	}
 
 	/**
 	 * Start the Thread, only if parameters is NOT NULL
 	 */
 	public void startAlgo() {
 		if (parameters == null)
 			return;
 		onBreak = false;
 		algoInst.addBPListener(new BPListener() {
 			/**
 			 * BPNr = Line number
 			 */
 			public void onBreakPoint(int BPNr) {
 				if (lineCounter.containsKey(new Integer(BPNr))) {
 					int tmp = lineCounter.get(new Integer(BPNr)).intValue();
 					tmp++;
 					lineCounter.put(new Integer(BPNr), tmp);
 				}
 				// first time we reach this breakpoint
 				else {
 					int tmp = 1;
 					lineCounter.put(new Integer(BPNr), tmp);
 				}
 				onBreak = true;
 				// inform all registerd breakpoint listeners
 				for (BPListener toInform : bpListeners) {
 					toInform.onBreakPoint(BPNr);
 				}
 			}
 		});
 		algoThread.start();
 	}
 
 	/**
 	 * will wait for Algo
 	 */
 	public void waitAlgoFinished() {
 		try {
 			algoThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * stops running thread with creating new one
 	 */
 	public void stopAlgo() {
 		createThread();
 	}
 
 	/**
 	 * let the algo get one step forward, can also be called if there is no need
 	 * to step forward
 	 */
 	public void stepForward() {
 		if (algoThread.getState().compareTo(Thread.State.TIMED_WAITING) == 0
 				|| algoThread.getState().compareTo(Thread.State.WAITING) == 0) {
 			synchronized (algoThread) {
 				algoInst.stopBreak();
 				algoThread.notify();
 			}
 		}
 	}
 
 	/**
 	 * blocking call to wait for breakpoint event
 	 */
 	public void waitForBreakpoint() {
 		while (onBreak == false && algoThread.isAlive()) {
 			synchronized (this) {
 				try {
 					this.wait(100);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Variables used from the algo
 	 * 
 	 * @return List of PCObjects of used Variables
 	 */
 	public ArrayList<PCObject> returnReferences() {
 		return algoInst.getVariableReferences();
 	}
 
 	/**
 	 * let the algo restart, and running till the previous Breakpoint
 	 */
 	@SuppressWarnings("unchecked")
 	public void stepBackward() {
 		HashMap<Integer, Integer> tmp = new HashMap<Integer, Integer>();
 		tmp.put(new Integer(0), new Integer(1));
 		// we are already on the first step, there is no way we can step more
 		// backwards
 		if (diff(lineCounter, tmp) < 1)
 			return;
 
 		this.createThread();
 		lastCounter = (HashMap<Integer, Integer>) lineCounter.clone();
 		reduce(lastCounter);
 		lineCounter.clear();
 		onBreak = false;
 		algoInst.addBPListener(new BPListener() {
 			public void onBreakPoint(int BPNr) {
 				// this is a already reached Breakpoint
 				if (lineCounter.containsKey(new Integer(BPNr))) {
 					int tmp = lineCounter.get(new Integer(BPNr)).intValue();
 					tmp++;
 					lineCounter.put(new Integer(BPNr), tmp);
 				}
 				// first time we reach this breakpoint
 				else {
 					int tmp = 1;
 					lineCounter.put(new Integer(BPNr), tmp);
 				}
 
 				// reached the previous state, great, so we are done
 				if (diff(lineCounter, lastCounter) == 0) {
 					reduce(lastCounter);
 					onBreak = true;
 					algoInst.addBPListener(new BPListener() {
 						public void onBreakPoint(int BPNr) {
 							if (lineCounter.containsKey(new Integer(BPNr))) {
 								int tmp = lineCounter.get(new Integer(BPNr))
 										.intValue();
 								tmp++;
 								lineCounter.put(new Integer(BPNr), tmp);
 							}
 							// first time we reach this breakpoint
 							else {
 								int tmp = 1;
 								lineCounter.put(new Integer(BPNr), tmp);
 							}
 							onBreak = true;
 							// inform all registerd breakpoint listeners
 							for (BPListener toInform : bpListeners) {
 								toInform.onBreakPoint(BPNr);
 							}
 						}
 					});
 				} else {
 					synchronized (algoThread) {
 						algoInst.stopBreak();
 						algoThread.notify();
 					}
 				}
 			}
 		});
 		algoThread.start();
 	}
 
 	/**
 	 * Helper function calculation from to HashMaps the difference, needed for
 	 * Breakpoint counting
 	 * 
 	 * @param first
 	 *            HashMap
 	 * @param second
 	 *            HashMap
 	 * @return Sum(first) - Sum(second)
 	 */
 	private int diff(HashMap<Integer, Integer> first,
 			HashMap<Integer, Integer> second) {
 		int sum_first = 0;
 		int sum_second = 0;
 		for (Integer it : first.values()) {
 			sum_first += it.intValue();
 		}
 		for (Integer it : second.values()) {
 			sum_second += it.intValue();
 		}
 		return sum_first - sum_second;
 	}
 
 	/**
 	 * Helper function to reduce from lastCounter one Breakpoint
 	 * 
 	 * @param arg
 	 */
 	private void reduce(HashMap<Integer, Integer> arg) {
 		// lastCounter is already empty, nothing to do here
 		if (arg.isEmpty())
 			return;
 
 		int tmp = 0;
 		tmp = arg.get(arg.keySet().toArray()[0]).intValue();
 		tmp -= 1;
 		arg.put((Integer) arg.keySet().toArray()[0], new Integer(tmp));
 
 	}
 
 	/**
 	 * If the Algo is alive
 	 * 
 	 * @return
 	 */
 	public boolean isAlive() {
 		return algoThread.isAlive();
 	}
 }
