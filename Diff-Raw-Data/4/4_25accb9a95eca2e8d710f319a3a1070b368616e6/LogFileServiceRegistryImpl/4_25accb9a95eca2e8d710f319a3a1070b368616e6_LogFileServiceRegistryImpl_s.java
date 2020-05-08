 package org.araqne.logstorage.engine;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.logstorage.LogFileService;
 import org.araqne.logstorage.LogFileServiceEventListener;
 import org.araqne.logstorage.LogFileServiceRegistry;
 import org.araqne.logstorage.file.LogFileReader;
 import org.araqne.logstorage.file.LogFileServiceV1;
 import org.araqne.logstorage.file.LogFileServiceV2;
 import org.araqne.logstorage.file.LogFileWriter;
 import org.araqne.logstorage.repair.IntegrityChecker;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logstorage-log-file-service-registry")
 @Provides
 public class LogFileServiceRegistryImpl implements LogFileServiceRegistry {
 	private final static Logger logger = LoggerFactory.getLogger(LogFileServiceRegistryImpl.class);
 
 	/**
 	 * force dependency
 	 */
 	@Requires
 	private IntegrityChecker consistencyChecker;
 
 	private BundleContext bc;
 	private ConcurrentHashMap<String, WaitEvent> availableEngines = new ConcurrentHashMap<String, WaitEvent>();
 
 	private ConcurrentMap<String, LogFileService> services = new ConcurrentHashMap<String, LogFileService>();
 
 	private Set<LogFileServiceEventListener> listeners = new CopyOnWriteArraySet<LogFileServiceEventListener>();
 
 	private LogFileServiceV1 v1 = new LogFileServiceV1();
 	private LogFileServiceV2 v2 = new LogFileServiceV2();
 
 	public LogFileServiceRegistryImpl(BundleContext bc) {
 		this.bc = bc;
 	}
 
 	@Validate
 	public void start() throws IOException {
 		loadEngineList();
 		register(v1);
 		register(v2);
 	}
 
 	@Invalidate
 	public void stop() {
		register(v2);
		register(v1);
 	}
 
 	private void loadEngineList() throws FileNotFoundException, UnsupportedEncodingException, IOException {
 		File f = getEngineListFile();
 		if (!f.exists()) {
 			availableEngines.put("v1", new WaitEvent("v1"));
 			availableEngines.put("v2", new WaitEvent("v2"));
 			return;
 		}
 
 		// load file engine list
 		FileInputStream is = null;
 		BufferedReader br = null;
 		try {
 			is = new FileInputStream(f);
 			br = new BufferedReader(new InputStreamReader(is, "utf-8"));
 
 			while (true) {
 				String line = br.readLine();
 				if (line == null)
 					break;
 
 				String type = line.trim();
 				availableEngines.putIfAbsent(type, new WaitEvent(type));
 			}
 		} finally {
 			ensureClose(br);
 			ensureClose(is);
 		}
 	}
 
 	private void ensureClose(Closeable c) {
 		if (c != null) {
 			try {
 				c.close();
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	private File getEngineListFile() {
 		return bc.getDataFile("engine.lst");
 	}
 
 	@Override
 	public LogFileWriter newWriter(String type, Map<String, Object> options) {
 		WaitEvent ev = availableEngines.get(type);
 		if (ev == null)
 			throw new UnsupportedOperationException("not supported engine: " + type);
 
 		ev.await();
 
 		LogFileService logFileService = services.get(type);
 		return logFileService.newWriter(options);
 	}
 
 	@Override
 	public LogFileReader newReader(String tableName, String type, Map<String, Object> options) {
 		WaitEvent ev = availableEngines.get(type);
 		if (ev == null)
 			throw new UnsupportedOperationException("not supported engine: " + type);
 
 		ev.await();
 
 		LogFileService logFileService = services.get(type);
 		return logFileService.newReader(tableName, options);
 	}
 
 	@Override
 	public void register(LogFileService service) {
 		String type = service.getType();
 		logger.info("araqne logstorage: loaded file engine [{}]", type);
 		services.put(type, service);
 
 		WaitEvent ev = new WaitEvent(type, true);
 		WaitEvent old = availableEngines.putIfAbsent(type, ev);
 		if (old != null) {
 			old.setReady();
 		} else {
 			rewriteEngineListFile();
 		}
 	}
 
 	private void rewriteEngineListFile() {
 		// update engine list file
 		FileOutputStream fos = null;
 		BufferedWriter bw = null;
 
 		try {
 			File f = getEngineListFile();
 			f.delete();
 
 			fos = new FileOutputStream(f);
 			bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
 
 			for (String name : availableEngines.keySet())
 				bw.write(name + "\n");
 
 		} catch (IOException e) {
 			logger.error("araqne logstorage: cannot update engine list", e);
 		} finally {
 			if (bw != null) {
 				try {
 					bw.close();
 				} catch (IOException e) {
 				}
 			}
 
 			if (fos != null) {
 				try {
 					fos.close();
 				} catch (IOException e) {
 				}
 			}
 		}
 	}
 
 	@Override
 	public void unregister(LogFileService service) {
 		String type = service.getType();
 
 		WaitEvent ev = availableEngines.get(type);
 		if (ev == null)
 			throw new UnsupportedOperationException("not supported engine: " + type);
 
 		for (LogFileServiceEventListener l : listeners) {
 			try {
 				l.onUnloadingFileService(type);
 			} catch (Throwable t) {
 				logger.warn("exception caught", t);
 			}
 		}
 
 		ev.ready = false;
 		services.remove(type);
 
 		logger.info("araqne logstorage: unloaded file engine [{}]", type);
 	}
 
 	@Override
 	public String[] getServiceTypes() {
 		return services.keySet().toArray(new String[0]);
 	}
 
 	@Override
 	public LogFileService getLogFileService(String type) {
 		WaitEvent ev = availableEngines.get(type);
 		if (ev == null)
 			throw new UnsupportedOperationException("not supported engine: " + type);
 
 		ev.await();
 
 		return services.get(type);
 	}
 
 	@Override
 	public String[] getInstalledTypes() {
 		Set<String> types = new HashSet<String>(availableEngines.keySet());
 		types.add("v1");
 		types.add("v2");
 		return types.toArray(new String[0]);
 	}
 
 	@Override
 	public void uninstall(String type) {
 		WaitEvent ev = availableEngines.remove(type);
 		if (ev == null)
 			throw new IllegalStateException("not installed engine: " + type);
 
 		rewriteEngineListFile();
 	}
 
 	private static class WaitEvent {
 		private String type;
 		private Lock lock = new ReentrantLock();
 		private Condition cond = lock.newCondition();
 		private volatile boolean ready;
 
 		public WaitEvent(String type) {
 			this(type, false);
 		}
 
 		public WaitEvent(String type, boolean ready) {
 			this.type = type;
 			this.ready = ready;
 		}
 
 		public void await() {
 			if (ready)
 				return;
 
 			lock.lock();
 			try {
 				while (!ready) {
 					try {
 						logger.debug("araqne logstorage: waiting file engine [{}]", type);
 						cond.await();
 					} catch (InterruptedException e) {
 					}
 				}
 			} finally {
 				lock.unlock();
 			}
 
 			logger.debug("araqne logstorage: file engine [{}] ready!", type);
 		}
 
 		public void setReady() {
 			ready = true;
 			lock.lock();
 			try {
 				cond.signalAll();
 			} finally {
 				lock.unlock();
 			}
 		}
 	}
 
 	@Override
 	public void addListener(LogFileServiceEventListener listener) {
 		listeners.add(listener);
 	}
 
 	@Override
 	public void removeListener(LogFileServiceEventListener listener) {
 		listeners.remove(listener);
 	}
 }
