 package jp.ac.titech.cs.de.ykstorage.service;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
import java.util.Map;
 import java.util.SortedMap;
 import java.util.logging.Logger;
 
 import jp.ac.titech.cs.de.ykstorage.util.DiskState;
 import jp.ac.titech.cs.de.ykstorage.util.StorageLogger;
 
 
 public class MAIDCacheDiskManager {
 	private Logger logger = StorageLogger.getLogger();
 	private MAIDCacheDiskStateManager sm;
 	
 	/**
 	 * e.g. /ecoim/ykstorage/data/disk1/
 	 */
 	private String[] diskpaths;
 	
 	/**
 	 * Capacity of a cache disk. It's unit is byte.
 	 */
 	private long maxCapacity;
 	
 	/**
 	 * key: device file path.  e.g. /dev/sdb
 	 * value: cache disk usage
 	 */
 	private HashMap<String, Long> capacity = new HashMap<String, Long>();
 	
 	/**
 	 * proposal1 is true when manager changes the number of cache disks.
 	 */
 	private boolean proposal1;
 	
 	/**
 	 * key: disk path on file system
 	 * value: device file path.  e.g. /dev/sdb
 	 */
 	private SortedMap<String, String> mountPointPaths;
 	
 	/**
 	 * key: data key
 	 * value: actual file path corresponding to the key
 	 * Using LinkedHashMap for LRU algorithm
 	 */
 //	private HashMap<Integer, String> keyFileMap = new HashMap<Integer, String>();
 //	private LinkedHashMap<Integer, String> keyFileMap = new LinkedHashMap<Integer, String>();
	private Map<Integer, String> keyFileMap = 
			Collections.synchronizedMap(new LinkedHashMap<Integer, String>());
 	
 	/**
 	 * ラウンドロビンでディスクの選択時に使用
 	 */
 	private int diskIndex = 0;
 	
 	public MAIDCacheDiskManager(
 			String[] diskpaths,
 			String savePath,
 			SortedMap<String, String> mountPointPaths,
 			double spinDownThreshold,
 			long maxCapacity,
 			MAIDCacheDiskStateManager sm) {
 		this.diskpaths = diskpaths;
 //		this.savePath = savePath;
 		this.mountPointPaths = mountPointPaths;
 		this.maxCapacity = maxCapacity;
 
 		init();
 		
 		this.proposal1 = Parameter.PROPOSAL1;	// TODO Parameter
 		if(proposal1) {
 			this.sm = sm;
 			this.sm.start();
 		}
 		logger.fine("MAIDCacheDiskManager: Capacity: " + maxCapacity + "[B]");
 	}
 
 	public Value get(int key) {
 		return read(key);
 	}
 
 	public boolean put(int key, Value value) {
 		return write(key, value);
 	}
 
 	public boolean delete(int key) {
 		return remove(key);
 	}
 
 //	public void end() {
 //		saveHashMap();
 //	}
 
 	private void init() {
 		for (String path : diskpaths) {
 			File f = new File(path);
 			if(!f.exists() || !f.isDirectory()) {
 				if(!f.mkdirs()) {
 					throw new SecurityException("cannot create dir: " + path);
 				}
 				logger.fine("CacheDisk [MKDIR]: " + path);
 			}
 			
 			String devicePath = mountPointPaths.get(path);
 			capacity.put(devicePath, 0L);
 		}
 
 //		loadHashMap();
 	}
 	
 	private boolean isStandby(int key) {
 		if(getDiskState(key).equals(DiskState.STANDBY)) {
 			return true;
 		} else if(getDiskReset(key)) {
 			String devicePath = getDevicePath(key);
 			sm.setDiskReset(devicePath, false);
 			Iterator<Integer> itr = keyFileMap.keySet().iterator();
 			while(itr.hasNext()) {
 				int tmpKey = itr.next();
 				if(devicePath.equals(getDevicePath(tmpKey))) {
 					remove(tmpKey);
 				}
 			}
 			
 //			sm.setDiskReset(devicePath, false);
 //			return true;
 			return false;
 		}
 		return false;
 	}
 	
 	private boolean getDiskReset(int key) {
 		String devicePath = getDevicePath(key);
 		return sm.getDiskReset(devicePath);
 	}
 	
 	private DiskState getDiskState(int key) {
 		String devicePath = getDevicePath(key);
 		return sm.getDiskState(devicePath);
 	}
 	
 	private String getDevicePath(int key) {
 		String filePath = keyFileMap.get(key);
 		String diskPath = getDiskPath(filePath);
 		String devicePath = mountPointPaths.get(diskPath);
 		return devicePath;
 	}
 	
 	private String getDiskPath(String filePath) {
 		String diskPath = "";
 		if(filePath != null) {
 			diskPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
 		}
 		return diskPath;
 	}
 
 	// キーに基づいて格納先のディスクを選択
 	private String selectDisk(int key) {
 		String filepath = keyFileMap.get(key);
 		if(filepath != null) {
 			return filepath;
 		}
 
 		filepath = roundRobin(key);
 		keyFileMap.put(key, filepath);
 		return filepath;
 	}
 
 	private String roundRobin(int key) {
 		String filepath = diskpaths[diskIndex] + key;
 		diskIndex++;
 		if(diskIndex > diskpaths.length - 1) {
 			diskIndex = 0;
 		}
 		return filepath;
 	}
 
 	private Value read(int key) {
 		Value result = Value.NULL;
 		
 		String filepath = keyFileMap.get(key);
 		if(filepath == null) {
 			return result;
 		}
 		
 		if(proposal1) {
 			if(isStandby(key)) {
 				return result;	// データの削除は書き込むときに行う
 			} else {
 				sm.incAccessCount(getDevicePath(key));
 			}
 		}
 		
 		String diskPath = getDiskPath(filepath);
 		String devicePath = mountPointPaths.get(diskPath);
 		try {
 //			sm.setDiskState(devicePath, DiskState.ACTIVE);	// TODO setした方がいい??
 			File f = new File(filepath);
 			FileInputStream fis = new FileInputStream(f);
 			BufferedInputStream bis = new BufferedInputStream(fis);
 
 			byte[] value = new byte[(int) f.length()];
 			bis.read(value);
 
 			bis.close();
 			result = new Value(value);
 			
 			keyFileMap.put(key, keyFileMap.remove(key));
 			logger.fine("CacheDisk [GET]: " + key + ", " + filepath + ", " + devicePath);
 		}catch(Exception e) {
 			e.printStackTrace();
 			logger.warning("failed CacheDisk [GET]: " + key + ", " + filepath + ", " + devicePath);
 		}
 		return result;
 	}
 
 	private boolean write(int key, Value value) {
 		boolean result = false;
 		long valueSize = value.getValue().length;
 		
 		String prevFilePath = keyFileMap.get(key);
 		long prevValueSize = 0L;
 		if(prevFilePath != null) {	// when update
 			File prevf = new File(prevFilePath);
 			prevValueSize = prevf.length();
 		}
 		
 		String filepath = selectDisk(key);
 		
 		if(valueSize > maxCapacity) {
 			keyFileMap.remove(key);
 			return false;
 		}
 		
 		if(proposal1) {
 			for(int i = 0; i < diskpaths.length; i++) {	// TODO diskpaths.length
 				if(isStandby(key)) {
 					keyFileMap.remove(key);	// delete
 					filepath = selectDisk(key);	// replace
 					sm.incAccessCount(getDevicePath(key));
 				} else {
 					sm.incAccessCount(getDevicePath(key));
 					break;
 				}
 				if((i == diskpaths.length - 1) && isStandby(key)) {	// TODO ???
 					keyFileMap.remove(key);
 					return false;
 				}
 			}
 		}
 		
 		String diskPath = getDiskPath(filepath);
 		String devicePath = mountPointPaths.get(diskPath);
 		
 		while(capacity.get(devicePath) + valueSize > maxCapacity) {
 			if(!lru(devicePath)) {
 				keyFileMap.remove(key);
 				return false;
 			}
 		}
 		
 		try {	
 //			sm.setDiskState(devicePath, DiskState.ACTIVE);	// TODO setした方がいい??
 			File f = new File(filepath);
 			if(Parameter.DEBUG) {
 				f.deleteOnExit();
 			}
 			FileOutputStream fos = new FileOutputStream(f);
 			BufferedOutputStream bos = new BufferedOutputStream(fos);
 
 			bos.write(value.getValue());
 			bos.flush();
 
 			bos.close();
 			
 			capacity.put(devicePath, capacity.get(devicePath) - prevValueSize + valueSize);
 			
 			result = true;
 			
 			keyFileMap.put(key, keyFileMap.remove(key));
 			logger.fine("CacheDisk [PUT]: " + key + ", " + filepath + ", " + devicePath + ", size: " + valueSize + "[B], usage: " + capacity.get(devicePath) + "[B], max: " + maxCapacity);
 		}catch(Exception e) {
 			keyFileMap.remove(key);
 			e.printStackTrace();
 			logger.warning("failed CacheDisk [PUT]: " + key + ", " + filepath + ", " + devicePath);
 		}
 		return result;
 	}
 
 	private boolean remove(int key) {
 		boolean result = false;
 		
 		String filepath = keyFileMap.get(key);
 		if(filepath == null) {
 			return result;
 		}
 		
 		if(proposal1) {
 			if(isStandby(key)) {
 				return true;	// TODO return true???
 			} else {
 				sm.incAccessCount(getDevicePath(key));
 			}
 		}
 		
 		keyFileMap.remove(key);
 		
 		String diskPath = getDiskPath(filepath);
 		String devicePath = mountPointPaths.get(diskPath);
 		try {
 //			sm.setDiskState(devicePath, DiskState.ACTIVE);	// TODO setした方がいい??
 			File f = new File(filepath);
 			long tmp = f.length();
 			result = f.delete();
 			
 			capacity.put(devicePath, capacity.get(devicePath) - tmp);
 			logger.fine("CacheDisk [DELETE]: " + key + ", " + filepath + ", " + devicePath + ", size: " + tmp + "[B], usage: " + capacity.get(devicePath) + "[B], max: " + maxCapacity);
 		}catch(SecurityException e) {
 			keyFileMap.put(key, filepath);
 			e.printStackTrace();
 			logger.warning("failed CacheDIsk [DELETE]: " + key + ", " + filepath + ", " + devicePath);
 		}
 		return result;
 	}
 	
 	private boolean lru(String devicePath) {
 		// アクセスが古い順にremoveする
 		Iterator<Integer> itr = keyFileMap.keySet().iterator();
 		while(itr.hasNext()) {
 			int key = itr.next();
 			// key が devicePath かどうかの確認
 			String filePath = keyFileMap.get(key);
 			String diskPath = getDiskPath(filePath);
 			if(devicePath.equals(mountPointPaths.get(diskPath))) {
 				logger.fine("CacheDisk [LRU]: " + keyFileMap.get(key) + ", usage: " + capacity.get(devicePath) + "[B]" + ", max: " + maxCapacity + "[B]");
 				return remove(key);
 			}			
 		}
 		logger.warning("failed CacheDisk [LRU]: usage: " + capacity.get(devicePath) + "[B]" + ", max: " + maxCapacity + "[B]");
 		return false;
 	}
 }
