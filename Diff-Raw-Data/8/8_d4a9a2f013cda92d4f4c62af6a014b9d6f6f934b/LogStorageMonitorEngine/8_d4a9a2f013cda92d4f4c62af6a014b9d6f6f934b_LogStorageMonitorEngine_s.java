 /*
  * Copyright 2011 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logstorage.engine;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.araqne.confdb.ConfigService;
 import org.araqne.cron.PeriodicJob;
 import org.araqne.logstorage.DiskLackAction;
 import org.araqne.logstorage.DiskLackCallback;
 import org.araqne.logstorage.DiskSpaceType;
 import org.araqne.logstorage.LogRetentionPolicy;
 import org.araqne.logstorage.LogStorage;
 import org.araqne.logstorage.LogStorageMonitor;
 import org.araqne.logstorage.LogStorageStatus;
 import org.araqne.logstorage.LogTableRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @PeriodicJob("* * * * *")
 @Component(name = "logstorage-monitor")
 @Provides
 public class LogStorageMonitorEngine implements LogStorageMonitor {
 	private static final String DEFAULT_MIN_FREE_SPACE_TYPE = DiskSpaceType.Percentage.toString();
 	private static final int DEFAULT_MIN_FREE_SPACE_VALUE = 10;
 	private static final String DEFAULT_DISK_LACK_ACTION = DiskLackAction.StopLogging.toString();
 
 	private final Logger logger = LoggerFactory.getLogger(LogStorageMonitorEngine.class.getName());
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private LogStorage storage;
 
 	@Requires
 	private ConfigService conf;
 
 	// last check time, check retention policy and purge files for every hour
 	private long lastPurgeCheck;
 
 	private DiskSpaceType minFreeSpaceType;
 	private int minFreeSpaceValue;
 	private DiskLackAction diskLackAction;
 	private Set<DiskLackCallback> diskLackCallbacks = new HashSet<DiskLackCallback>();
 
 	public LogStorageMonitorEngine() {
 		reload();
 	}
 
 	private void reload() {
 		minFreeSpaceType = DiskSpaceType.valueOf(getStringParameter(Constants.MinFreeDiskSpaceType, DEFAULT_MIN_FREE_SPACE_TYPE));
 		minFreeSpaceValue = getIntParameter(Constants.MinFreeDiskSpaceValue, DEFAULT_MIN_FREE_SPACE_VALUE);
 		diskLackAction = DiskLackAction.valueOf(getStringParameter(Constants.DiskLackAction, DEFAULT_DISK_LACK_ACTION));
 	}
 
 	@Override
 	public int getMinFreeSpaceValue() {
 		return minFreeSpaceValue;
 	}
 
 	@Override
 	public DiskSpaceType getMinFreeSpaceType() {
 		return minFreeSpaceType;
 	}
 
 	@Override
 	public void setMinFreeSpace(int value, DiskSpaceType type) {
 		if (type == DiskSpaceType.Percentage) {
 			if (value <= 0 || value >= 100)
 				throw new IllegalArgumentException("invalid value");
 		} else if (type == DiskSpaceType.Megabyte) {
 			if (value <= 0)
 				throw new IllegalArgumentException("invalid value");
 		} else if (type == null)
 			throw new IllegalArgumentException("type cannot be null");
 
 		this.minFreeSpaceType = type;
 		this.minFreeSpaceValue = value;
 
 		ConfigUtil.set(conf, Constants.MinFreeDiskSpaceType, type.toString());
 		ConfigUtil.set(conf, Constants.MinFreeDiskSpaceValue, Integer.toString(value));
 	}
 
 	@Override
 	public DiskLackAction getDiskLackAction() {
 		return diskLackAction;
 	}
 
 	@Override
 	public void setDiskLackAction(DiskLackAction action) {
 		if (action == null)
 			throw new IllegalArgumentException("action cannot be null");
 
 		this.diskLackAction = action;
 
 		ConfigUtil.set(conf, Constants.DiskLackAction, action.toString());
 	}
 
 	@Override
 	public void registerDiskLackCallback(DiskLackCallback callback) {
 		diskLackCallbacks.add(callback);
 	}
 
 	@Override
 	public void unregisterDiskLackCallback(DiskLackCallback callback) {
 		diskLackCallbacks.remove(callback);
 	}
 
 	@Override
 	public void forceRetentionCheck() {
 		checkRetentions(true);
 	}
 
 	private boolean isDiskLack(File dir) {
 		long usable = dir.getUsableSpace();
 		long total = dir.getTotalSpace();
 
 		logger.trace("araqne logstorage: check {} {} free space of partition [{}], current [{}/{}]", new Object[] { minFreeSpaceValue,
 				minFreeSpaceType.toString().toLowerCase(), dir.getAbsolutePath(), usable, total });
 		if (minFreeSpaceType == DiskSpaceType.Percentage) {
 			int percent = (int) (usable * 100 / total);
 			if (percent < minFreeSpaceValue) {
 				return true;
 			}
 		} else if (minFreeSpaceType == DiskSpaceType.Megabyte) {
 			int mega = (int) (usable / 1048576);
 			if (mega < minFreeSpaceValue) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public void run() {
 		try {
 			runOnce();
 		} catch (Exception e) {
 			logger.error("araqne logstorage: storage monitor error", e);
 		}
 	}
 
 	private void runOnce() {
 		reload();
 		checkRetentions(false);
 		checkDiskLack();
 	}
 
 	private void checkRetentions(boolean force) {
 		long now = System.currentTimeMillis();
 		if (!force && now - lastPurgeCheck < 3600 * 1000)
 			return;
 
 		for (String tableName : tableRegistry.getTableNames()) {
 			checkAndPurgeFiles(tableName);
 		}
 
 		lastPurgeCheck = now;
 	}
 
 	private void checkAndPurgeFiles(String tableName) {
 		LogRetentionPolicy p = storage.getRetentionPolicy(tableName);
 		if (p == null || p.getRetentionDays() == 0) {
 			logger.debug("araqne logstorage: no retention policy for table [{}]", tableName);
 			return;
 		}
 
 		// purge tables
 		Date logBaseline = storage.getPurgeBaseline(tableName);
 		if (logBaseline != null)
 			storage.purge(tableName, null, logBaseline);
 	}
 
 	private void checkDiskLack() {
 		// categorize by partition path
 		Map<File, List<String>> partitionTables = new HashMap<File, List<String>>();
 
 		for (String tableName : tableRegistry.getTableNames()) {
 			File dir = storage.getTableDirectory(tableName).getParentFile();
 
 			List<String> tables = partitionTables.get(dir);
 			if (tables == null) {
 				tables = new ArrayList<String>();
 				partitionTables.put(dir, tables);
 			}
 
 			tables.add(tableName);
 		}
 
 		for (File dir : partitionTables.keySet()) {
 			checkDiskPartitions(dir, partitionTables.get(dir));
 		}
 	}
 
 	private void checkDiskPartitions(File partitionPath, List<String> tableNames) {
 		if (isDiskLack(partitionPath)) {
 			logger.trace("araqne logstorage: not enough disk space, current minimum free space config [{}] {}", minFreeSpaceValue,
 					(minFreeSpaceType == DiskSpaceType.Percentage ? "%" : "MB"));
 			if (diskLackAction == DiskLackAction.StopLogging) {
 				if (storage.getStatus() == LogStorageStatus.Open) {
 					logger.error("araqne logstorage: [{}] not enough space, stop logging", partitionPath.getAbsolutePath());
 					storage.stop();
 				}
 			} else if (diskLackAction == DiskLackAction.RemoveOldLog) {
 				List<LogFile> files = new ArrayList<LogFile>();
 				for (String tableName : tableNames) {
 					for (Date date : storage.getLogDates(tableName))
 						files.add(new LogFile(tableName, date));
 				}
 				Collections.sort(files, new LogFileComparator());
 				int index = 0;
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 				do {
 					if (index >= files.size()) {
 						if (storage.getStatus() == LogStorageStatus.Open) {
 							logger.error("araqne logstorage: no more data files in [{}], stop logging", partitionPath.getAbsolutePath());
 							storage.stop();
 						}
 						break;
 					}
 
 					LogFile lf = files.get(index++);
 					logger.info("araqne logstorage: removing old log, table [{}], date [{}]", lf.tableName, sdf.format(lf.date));
 					storage.purge(lf.tableName, lf.date);
 				} while (isDiskLack(partitionPath));
 			}
 
 			for (DiskLackCallback callback : diskLackCallbacks) {
 				try {
 					callback.callback();
 				} catch (Throwable t) {
 					logger.warn("araqne logstorage: disk lack callback should not throw any exception", t);
 				}
 			}
 		}
 	}
 
 	private String getStringParameter(Constants key, String defaultValue) {
 		String value = ConfigUtil.get(conf, key);
 		if (value != null)
 			return value;
 		return defaultValue;
 	}
 
 	private int getIntParameter(Constants key, int defaultValue) {
 		String value = ConfigUtil.get(conf, key);
 		if (value != null)
 			return Integer.valueOf(value);
 		return defaultValue;
 	}
 
 	private class LogFile {
 		private String tableName;
 		private Date date;
 
 		private LogFile(String tableName, Date date) {
 			this.tableName = tableName;
 			this.date = date;
 		}
 	}
 
 	private class LogFileComparator implements Comparator<LogFile> {
 		@Override
 		public int compare(LogFile o1, LogFile o2) {
 			return o1.date.compareTo(o2.date);
 		}
 	}
 }
