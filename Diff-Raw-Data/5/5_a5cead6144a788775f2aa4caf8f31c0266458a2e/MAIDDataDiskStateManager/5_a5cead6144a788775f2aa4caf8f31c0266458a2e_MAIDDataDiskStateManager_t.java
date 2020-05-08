 package jp.ac.titech.cs.de.ykstorage.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.logging.Logger;
 
 import org.streamspinner.connection.CQException;
 import org.streamspinner.connection.CQRowSet;
 import org.streamspinner.connection.CQRowSetEvent;
 import org.streamspinner.connection.CQRowSetListener;
 import org.streamspinner.connection.DefaultCQRowSet;
 
 import jp.ac.titech.cs.de.ykstorage.util.DiskState;
 import jp.ac.titech.cs.de.ykstorage.util.StorageLogger;
 
 
 public class MAIDDataDiskStateManager {
 	private static String[] UNIT_NAMES;
 	static {
 		int maxUnits = 6;
 		int maxDisksPerUnit = 7;
 		UNIT_NAMES = new String[maxUnits * maxDisksPerUnit];
 		String prefix = "Unit1.Power%d";
 		for(int i = 0; i < UNIT_NAMES.length; i++) {
 			UNIT_NAMES[i] = String.format(prefix, i + 1);
 		}
 	}
 	
 	/**
 	 * key: device file path
 	 * value: disk state
 	 */
 	private Map<String, DiskState> diskStates;
 
 	/**
 	 * key: device file path
 	 * value: start time of idle state TODO millisecond??
 	 */
 	private Map<String, Long> idleIntimes;
 	
 	/**
 	 * key: device file path
 	 * value: start time of standby state TODO millisecond??
 	 */
 	private Map<String, Long> standbyIntimes;
 	
 	/**
 	 * key: device file path
 	 * value: power of idle
 	 */
 	private Map<String, Double> wIdle;
 	
 	/**
 	 * key: device file path
 	 * value: power of standby
 	 */
 	private Map<String, Double> wStandby;
 	
 	/**
 	 * key: device file path
 	 * value: Joule of spinup
 	 */
 	private Map<String, Double> jSpinup;
 	
 	/**
 	 * key: device file path
 	 * value: Joule of spindown
 	 */
 	private Map<String, Double> jSpindown;
 
 //	private double spindownThreshold;
 	/**
 	 * In this class, given spin down threshold time is converted
 	 * from second(in double type) to millisecond(in long type) value
 	 */
 	private long spindownThreshold;
 	
 	/**
 	 * key: device file path
 	 * value: idle time threshold (spin down threshold) (millisecond?)
 	 */
 	private Map<String, Long> tIdle;
 	
 	/**
 	 * key: device file path
 	 * value: standby time TODO millisecond??
 	 */
 	private Map<String, Long> tStandby;
 	
 	/**
 	 * key: device file path
 	 * value: isSpinup
 	 */
 	private Map<String, Boolean> isSpinup;
 	
 	/**
 	 * key: device file path
 	 * value: isSpindown
 	 */
 	private Map<String, Boolean> isSpindown;
 	
 	private double initWidle = 6.8;//0.0;
 	private double initWstandby = 0.9;//0.0;
 	private double initJspinup = 450.0;//0.0;
 	private double initJspindown = 35.0;//0.0;
 	private long initTstandby = 0L;
 	
 	private double minWspinup = 11.2;
 	private double maxWidle = 7.6;
 	private double minWidle = 4.5;
 	private double maxWstandby = 1.0;
 	
 	private boolean minWup = false;
 	
 	private int spindownCount = 0;
 	private int spinupCount = 0;
 	
 	// add millisecond for proposal2
 	private long alpha = 10000;	// TODO Parameter. break even time
 	
 	/**
 	 * the interval of checking disk state for proposal1
 	 */
 	private long interval;
 	
 	private String rmiUrl;
 	private boolean[] isCacheDisk;
 	private int numOfCacheDisks;
 	private int numOfDataDisks;
 	private double acc;
 	
 	private ArrayList<String> devicePaths;
 	
 	private String avgCommand;
 	private String inteCommand;
 	
 	private final Logger logger = StorageLogger.getLogger();
 
 	private StateCheckThread sct;
 	private StateCheckThread2 sct2;
 	private GetDataThread gdt;
 
 
 	public MAIDDataDiskStateManager(SortedMap<String, String> mountPointPaths, String[] dataDiskPaths,
 			double spinDownThreshold, long interval, String rmiUrl, boolean[] isCacheDisk,
 			int numOfCacheDisks, int numOfDataDisks, double acc) {
 		
 		System.setProperty("java.security.policy","file:./security/StreamSpinner.policy");	// XXX
 		System.setProperty("sun.rmi.dgc.ackTimeout", "3000");
 		
 		this.devicePaths = new ArrayList<String>();
 		for(String diskpath : dataDiskPaths) {
 			devicePaths.add(mountPointPaths.get(diskpath));
 		}
 		
 		this.diskStates = initDiskStates(devicePaths);
 		this.idleIntimes = initIdleInTimes(devicePaths);
 		this.standbyIntimes = initIdleInTimes(devicePaths);
 		this.spindownThreshold = (long)(spinDownThreshold * 1000);
 		this.interval = interval;
 		this.rmiUrl = rmiUrl;
 		this.isCacheDisk = isCacheDisk;
 		this.numOfCacheDisks = numOfCacheDisks;
 		this.numOfDataDisks = numOfDataDisks;
 		this.acc = acc;
 		
 		this.wIdle = initWJ(devicePaths, initWidle);
 		this.wStandby = initWJ(devicePaths, initWstandby);
 		this.jSpinup = initWJ(devicePaths, initJspinup);
 		this.jSpindown = initWJ(devicePaths, initJspindown);
 		this.tIdle = initT(devicePaths, spindownThreshold);
 		this.tStandby = initT(devicePaths, initTstandby);
 		this.isSpinup = initIs(devicePaths, false);
 		this.isSpindown = initIs(devicePaths, false);
 		
 		makeSQLCommand();
 		
 		this.sct = new StateCheckThread();
 		this.sct2 = new StateCheckThread2();
 		this.gdt = new GetDataThread();
 	}
 
 	private Map<String, DiskState> initDiskStates(Collection<String> devicePaths) {
 		Map<String, DiskState> result = new HashMap<String, DiskState>();
 		for (String device : devicePaths) {
 			result.put(device, DiskState.IDLE);
 		}
 		return result;
 	}
 
 	private Map<String, Long> initIdleInTimes(Collection<String> devicePaths) {
 		Map<String, Long> result = new HashMap<String, Long>();
 		long thisTime = System.currentTimeMillis();
 		for (String device : devicePaths) {
 			result.put(device, thisTime);
 		}
 		return result;
 	}
 	
 	private Map<String, Double> initWJ(Collection<String> devicePaths, double data) {
 		Map<String, Double> result = new HashMap<String, Double>();
 		for (String device : devicePaths) {
 			result.put(device, data);
 		}
 		return result;
 	}
 	
 	private Map<String, Long> initT(Collection<String> devicePaths, long time) {
 		Map<String, Long> result = new HashMap<String, Long>();
 		for (String device : devicePaths) {
 			result.put(device, time);
 		}
 		return result;
 	}
 	
 	private Map<String, Boolean> initIs(Collection<String> devicePaths, boolean bool) {
 		Map<String, Boolean> result = new HashMap<String, Boolean>();
 		for (String device : devicePaths) {
 			result.put(device, bool);
 		}
 		return result;
 	}
 
 	private boolean devicePathCheck(String devicePath) {
 		boolean result = true;
 		if(devicePath == null || devicePath == "") {
 			result = false;
 		}
 		return result;
 	}
 	
 	private void makeSQLCommand() {
 		// e.g.) "MASTER Unit1 SELECT avg(Unit1.Power3),avg(Unit1.Power4) FROM Unit1[1000]",
 		//       "MASTER Unit1 SELECT inte(Unit1.Power1),inte(Unit1.Power2) FROM Unit1[1000]"
 		
 		avgCommand = "MASTER Unit1 SELECT ";
 		inteCommand = "MASTER Unit1 SELECT ";
 		
 		int numOfDisks = numOfCacheDisks + numOfDataDisks;
 		for(int i = 0; i < numOfDisks; i++) {
 			if(!isCacheDisk[i]) {
 				inteCommand = inteCommand.concat("inte(" + UNIT_NAMES[i] + "),");
 				avgCommand = avgCommand.concat("avg(" + UNIT_NAMES[i] + "),");
 			}
 		}
 		
 		avgCommand = avgCommand.substring(0, avgCommand.length() - 1) + " FROM Unit1[1000]";
 		inteCommand = inteCommand.substring(0, inteCommand.length() - 1) + " FROM Unit1[1000]";
 		
 		logger.fine("MAID DataDisk State [DataDisk AVG SQL Command]: " + avgCommand);
 		logger.fine("MAID DataDisk State [DataDisk INTE SQL Command]: " + inteCommand);
 	}
 
 	public void start() {
 		sct.start();
 	}
 	
 	public void start2() {
 		sct2.start();
 		gdt.start();
 	}
 
 	public boolean spinup(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;
 		setDiskState(devicePath, DiskState.IDLE);
 
 		String[] cmdarray = {"ls", devicePath};
 		int returnCode = execCommand(cmdarray);
 		if(returnCode == 0) {
 			spinupCount++;
 			logger.fine("[SPINUP]: " + devicePath + "count: " + spinupCount);
 			long currentTime = System.currentTimeMillis();
 			avgTstandby(devicePath, currentTime - getStandbyIntime(devicePath));
 			setIsSpinup(devicePath, true);
 			initJspinup(devicePath);
 			return true;
 		}
 		setDiskState(devicePath, DiskState.STANDBY);
 		return false;
 	}
 
 	public boolean spindown(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;
 		setDiskState(devicePath, DiskState.STANDBY);
 		
 		String[] sync = {"sync"};
 		int syncRet = execCommand(sync);
 		if(syncRet != 0) {
 			setDiskState(devicePath, DiskState.IDLE);
 			return false;
 		}
 		
 		String[] hdparm = {"hdparm", "-y", devicePath};
 		int hdparmRet = execCommand(hdparm);
 		if(hdparmRet == 0) {
 			spindownCount++;
 			logger.fine("[SPINDOWN]: " + devicePath + "count: " + spindownCount);
 			long currentTime = System.currentTimeMillis();
 			setStandbyIntime(devicePath, currentTime);
 //			setTidle(devicePath, currentTime - getIdleIntime(devicePath));	// XXX setはいらない??? setIdleInTimeは欲しい???
 			setIsSpindown(devicePath, true);
 			initJspindown(devicePath);
 			return true;
 		}
 		setDiskState(devicePath, DiskState.IDLE);
 		return false;
 	}
 
 	private int execCommand(String[] cmd) {
 		int returnCode = 1;
 		try {
 			Runtime r = Runtime.getRuntime();
 			Process p = r.exec(cmd);
 			returnCode = p.waitFor();
 			if(returnCode != 0) {
 				logger.info(cmd[0] + " return code: " + returnCode);
 			}
 		} catch (IOException e) {
 //			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return returnCode;
 	}
 
 	public boolean setDiskState(String devicePath, DiskState state) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath))
 			result = false;
 		this.diskStates.put(devicePath, state);
 		return result;
 	}
 
 	public DiskState getDiskState(String devicePath) {
 		if(!devicePathCheck(devicePath)) return DiskState.NA;
 
 		DiskState state = diskStates.get(devicePath);
 		if (state == null) {
 			state = DiskState.NA;
 		}
 		return state;
 	}
 
 	public boolean setIdleIntime(String devicePath, long time) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		idleIntimes.put(devicePath, time);
 //		logger.fine("setIdleIntime: " + devicePath + ", " + time);
 		return result;
 	}
 
 //	public double getIdleIntime(String devicePath) {
 	public long getIdleIntime(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1L;
 		return idleIntimes.get(devicePath);
 	}
 	
 	private synchronized boolean setStandbyIntime(String devicePath, long time) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		standbyIntimes.put(devicePath, time);
 //		logger.fine("setStandbyIntime: " + devicePath + ", " + time);
 		return result;
 	}
 
 //	private synchronized double getStandbyIntime(String devicePath) {
 	private synchronized long getStandbyIntime(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1L;
 		return standbyIntimes.get(devicePath);
 	}
 	
 	private synchronized boolean setWidle(String devicePath, double data) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		wIdle.put(devicePath, data);
 //		logger.fine("setWidle: " + devicePath + ", " + data);
 		return result;
 	}
 	
 	private synchronized double getWidle(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1.0;
 		return wIdle.get(devicePath);
 	}
 	
 	private synchronized boolean setWstandby(String devicePath, double data) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		wStandby.put(devicePath, data);
 //		logger.fine("setWstandby: " + devicePath + ", " + data);
 		return result;
 	}
 	
 	private synchronized double getWstandby(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1.0;
 		return wStandby.get(devicePath);
 	}
 	
 	private synchronized boolean addJspinup(String devicePath, double data) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		jSpinup.put(devicePath, jSpinup.get(devicePath) + data);
 //		logger.fine("addJspinup: " + devicePath + ", " + data);
 		return result;
 	}
 	
 	private synchronized double getJspinup(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1.0;
 		return jSpinup.get(devicePath);
 	}
 	
 	private synchronized void initJspinup(String devicePath) {
 //		Iterator<String> itr = jSpinup.keySet().iterator();
 //		while(itr.hasNext()) {
 //			String key = itr.next();
 //			jSpinup.put(key, 0.0);
 //		}
 		jSpinup.put(devicePath, 0.0);
 	}
 	
 	private synchronized boolean addJspindown(String devicePath, double data) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		jSpindown.put(devicePath, jSpindown.get(devicePath) + data);
 //		logger.fine("addJspindown: " + devicePath + ", " + data);
 		return result;
 	}
 	
 	private synchronized double getJspindown(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1.0;
 		return jSpindown.get(devicePath);
 	}
 	
 	private synchronized void initJspindown(String devicePath) {
 //		Iterator<String> itr = jSpindown.keySet().iterator();
 //		while(itr.hasNext()) {
 //			String key = itr.next();
 //			jSpindown.put(key, 0.0);
 //		}
 		jSpindown.put(devicePath, 0.0);
 	}
 	
 	private synchronized boolean setTidle(String devicePath, long time) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		tIdle.put(devicePath, time);
 //		logger.fine("setTidle: " + devicePath + ", " + time);
 		return result;
 	}
 	
 	private synchronized long getTidle(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1L;
 		return tIdle.get(devicePath);
 	}
 	
 	private synchronized boolean addTidle(String devicePath, long time) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		
 		long tmp = tIdle.get(devicePath);
 		if(tmp + time < 0) {
 			tIdle.put(devicePath, 0L);
 			logger.fine("addTidle: " + devicePath + ", " + 0);
 		} else {
 			tIdle.put(devicePath, tmp + time);
 			logger.fine("addTidle: " + devicePath + ", " + tmp  + " + " + time);
 		}
 		
 		return result;
 	}
 	
 //	private synchronized boolean setTstandby(String devicePath, long data) {
 //		boolean result = true;
 //		if(!devicePathCheck(devicePath)) {
 //			result = false;
 //		}
 //		tStandby.put(devicePath, data);
 //		return result;
 //	}
 	
 	private synchronized long getTstandby(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1L;
 		return tStandby.get(devicePath);
 	}
 	
 	private synchronized boolean avgTstandby(String devicePath, long time) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		
 		long tmp = getTstandby(devicePath);
 		if(tmp == 0L) {
 			tStandby.put(devicePath, time + tmp);
 //			logger.fine("avgTstandby: " + devicePath + ", " + time + tmp);
 		} else {
 			tStandby.put(devicePath, (time + tmp) / 2);
 //			logger.fine("avgTstandby: " + devicePath + ", " + ((time + tmp) / 2));
 		}
 		return result;
 	}
 	
 	private synchronized boolean setIsSpinup(String devicePath, boolean data) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		isSpinup.put(devicePath, data);
 //		logger.fine("setIsSpinup: " + devicePath + ", " + data);
 		return result;
 	}
 	
 	private synchronized boolean getIsSpinup(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;	// TODO false?
 		return isSpinup.get(devicePath);
 	}
 	
 	private synchronized boolean setIsSpindown(String devicePath, boolean data) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		isSpindown.put(devicePath, data);
 //		logger.fine("setIsSpindown: " + devicePath + ", " + data);
 		return result;
 	}
 	
 	private synchronized boolean getIsSpindown(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;	// TODO false?
 		return isSpindown.get(devicePath);
 	}
 	
 //	private void breakEvenTime(String devicePath, long tStandby) {
 //		double ws = getWstandby(devicePath);
 //		double wi = getWidle(devicePath);
 //		double ts = (double)tStandby / 1000.0;
 //		double jup = getJspinup(devicePath);
 //		double jdown = getJspindown(devicePath);
 //		long ti = 0L;
 //		
 //		ti = (long) ((((ws - wi) * ts + jup + jdown) / wi) * 1000);
 //		
 //		setTidle(devicePath, ti);
 //	}
 	
 	class StateCheckThread extends Thread {
 		public void run() {
 			while(true) {
 				long now = System.currentTimeMillis();	// TODO long double
 
 				for (String devicePath : diskStates.keySet()) {
 					if (DiskState.IDLE.equals(getDiskState(devicePath)) &&
 						(now - getIdleIntime(devicePath)) > spindownThreshold) {
 						// TODO アイドル時間をログに出力
 						spindown(devicePath);
 					}
 				}
 
 				try {
 					sleep(interval);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	class StateCheckThread2 extends Thread {
 		public void run() {
 			while(true) {
 				long now = System.currentTimeMillis();
 
 				for (String devicePath : diskStates.keySet()) {
 					
 					// IDLE時間閾値の変更 TODO 初期値を設定する
 					double wi = getWidle(devicePath);
 					double ws = getWstandby(devicePath);
 //					long ts = getTstandby(devicePath);
 					double ts = (double) getTstandby(devicePath) / 1000.0;
 					long ti = getTidle(devicePath);
 					double ju = getJspinup(devicePath);
 					double jd = getJspindown(devicePath);
 					double al = (double) alpha / 1000.0;
 //					if(getWidle(devicePath) * getTidle(devicePath) * getWstandby(devicePath) * getTstandby(devicePath)
 //							* getJspinup(devicePath) * getJspindown(devicePath) != 0.0) {
 					if(wi * ws * ts * ju * jd != 0.0) {
 						
 //						breakEvenTime(devicePath, getTstandby(devicePath));
 						
 //						if(getWidle(devicePath) * getTstandby(devicePath) > getWstandby(devicePath) * getTstandby(devicePath)
 //								+ getJspinup(devicePath) + getJspindown(devicePath)) {
 //							//addTidle(devicePath, -1000);	// TODO -1000
 //							setTidle(devicePath, getTidle(devicePath)/alpha);
 //							logger.fine("sub [PROPOSAL2]: Tidle: " + getTidle(devicePath) + "[ms], Tstandby: " + getTstandby(devicePath) + "[ms]");
 //						} else {
 //							//addTidle(devicePath, 1000);	// TODO +1000
 //							setTidle(devicePath, getTidle(devicePath)*alpha);
 //							logger.fine("add [PROPOSAL2]: Tidle: " + getTidle(devicePath) + "[ms], Tstandby: " + getTstandby(devicePath) + "[ms]");
 //						}
 						
 						logger.fine("[PROPOSAL2]: wIdle: " + wi + ", tIdle: " + ti + ", wStandby: " + ws + ", tStandby: " + ts + ", jSpinup: " + ju + ", jSpindown: " + jd);
 						if(wi * (ts + al) > ws * (ts + al) + ju + jd) {
 							addTidle(devicePath, -alpha);
 							logger.fine("sub [PROPOSAL2]: new Tidle: " + getTidle(devicePath) + "[ms], Tstandby: " + getTstandby(devicePath) + "[ms]");
 						}
 						if(wi * (ts - al) < ws * (ts - al) + ju + jd) {
 							addTidle(devicePath, alpha);
 							logger.fine("add [PROPOSAL2]: new Tidle: " + getTidle(devicePath) + "[ms], Tstandby: " + getTstandby(devicePath) + "[ms]");
 						}
 						
 					} 
 //					else {
 //						logger.fine("not [PROPOSAL2]: ts: " + ts + ", ti: " + ti + ", ju: " + ju + ", jd: " + jd);
 //					}
 					
 					// IDLE時間閾値を超えたディスクをspindownさせる
 					if (DiskState.IDLE.equals(getDiskState(devicePath)) &&
 						(now - getIdleIntime(devicePath)) > getTidle(devicePath)) {
 						// TODO アイドル時間をログに出力
 						spindown(devicePath);
 					}
 				}
 				
 				try {
 					sleep(interval);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	class GetDataThread extends Thread {
 		public void run() {
 			logger.fine("DataDisk GetDataThread [START]");
 			try {
 				CQRowSet rs = new DefaultCQRowSet();
 				rs.setUrl(rmiUrl);   // StreamSpinnerの稼働するマシン名を指定
 				rs.setCommand(avgCommand);   // 問合せのセット
 				CQRowSetListener ls = new MyListener();
 				rs.addCQRowSetListener(ls);   // リスナの登録
 				rs.start();   // 問合せ処理の開始
 			} catch(CQException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 			
 			try {
 				CQRowSet rs2 = new DefaultCQRowSet();
 				rs2.setUrl(rmiUrl);   // StreamSpinnerの稼働するマシン名を指定
 				rs2.setCommand(inteCommand);   // 問合せのセット
 				CQRowSetListener ls2 = new MyListener2();
 				rs2.addCQRowSetListener(ls2);   // リスナの登録
 				rs2.start();   // 問合せ処理の開始
 			} catch(CQException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		class MyListener implements CQRowSetListener {
 			public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
 				CQRowSet rs = (CQRowSet)(e.getSource());
 	    		try {
 	    			while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
 	    				int i = 0;
 	    				double wtmp = 0.0;
 	    				// TODO spinup/down中はセットしない
 	    				for (String devicePath : diskStates.keySet()) {
 	    					wtmp = rs.getDouble(i + 1);
 	    					if(DiskState.IDLE.equals(getDiskState(devicePath)) && (minWidle < wtmp) && (wtmp < maxWidle)) {
 	    						setWidle(devicePath, wtmp);
 	    					}else if(DiskState.STANDBY.equals(getDiskState(devicePath)) && (wtmp < maxWstandby)) {
 	    						setWstandby(devicePath, wtmp);
 	    					}
 	    					i++;
 	    				}
 	    			}
 	    		} catch (CQException e1) {
 	    			e1.printStackTrace();
 	    			System.exit(1);
 				}
 			}
 		}
 		
 		class MyListener2 implements CQRowSetListener {
 			public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
 				CQRowSet rs = (CQRowSet)(e.getSource());
 	    		try {
 	    			double wcurrent = 0.0;
 	    			while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
 	    				int i = 0;
 	    				
//	    				wcurrent = rs.getDouble(i + 1);
 //	    				if(!minWup) {
 //	    					minWup = (wcurrent > minWspinup)? true: false;
 //	    				}
 	    				
 	    				for (String devicePath : diskStates.keySet()) {
 	    					if(!minWup) {
		    					minWup = (rs.getDouble(i + 1) > minWspinup)? true: false;
 		    				}
 	    					if(getIsSpinup(devicePath)) {
 	    						addJspinup(devicePath, rs.getDouble(i + 1));
 	    					}
 	    					if(getIsSpindown(devicePath)) {
 	    						addJspindown(devicePath, rs.getDouble(i + 1));
 	    					}
 	    					if(((minWup) && (wcurrent < getWidle(devicePath) + acc + 0.4)) || (getJspinup(devicePath) > 450.0)) {
 	    						setIsSpinup(devicePath, false);
 	    						minWup = false;
 	    					}
 	    					if((wcurrent < getWstandby(devicePath) + acc) || (getJspindown(devicePath) > 35.0)) {
 	    						setIsSpindown(devicePath, false);
 	    					}
 	    					i++;
 	    				}
 	    			}
 //	    			System.out.println("wcurrent: " + wcurrent);
 	    		} catch (CQException e1) {
 	    			e1.printStackTrace();
 	    			System.exit(1);
 				}
 			}
 		}
 	}
 
 }
