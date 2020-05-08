 package org.finomnis.mcdaemon;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.finomnis.mcdaemon.automation.BackupTask;
 import org.finomnis.mcdaemon.automation.HealthCheckTask;
 import org.finomnis.mcdaemon.automation.TaskScheduler;
 import org.finomnis.mcdaemon.automation.UpdateTask;
 import org.finomnis.mcdaemon.downloaders.MCDownloader;
 import org.finomnis.mcdaemon.downloaders.bukkit.BukkitDownloader;
 import org.finomnis.mcdaemon.downloaders.ftb.FTBDownloader;
 import org.finomnis.mcdaemon.downloaders.vanilla.VanillaDownloader;
 import org.finomnis.mcdaemon.server.ServerMonitor;
 import org.finomnis.mcdaemon.server.wrapper.ServerWrapper;
 import org.finomnis.mcdaemon.tools.ConfigNotFoundException;
 import org.finomnis.mcdaemon.tools.Log;
 
 public class MCDaemon {
 
 	private static boolean running = false;
 	private static Lock lock = new ReentrantLock();
 	private static Condition runningChangedCondition = lock.newCondition();
 	private static MainConfigFile configFile = null;
 	private static MCDownloader mcDownloader = null;
 	private static ServerMonitor serverMonitor = null;
 	private static Thread serverMonitorThread = null;
 	private static ServerWrapper serverWrapper = null;
 	private static TaskScheduler taskScheduler = null;
 	private static Thread taskSchedulerThread = null;
 	private static Lock backupLock = new ReentrantLock();
 	
 	public static void start() {
 		Log.out("Starting Daemon ...");
 
 		lock.lock();
 		if (running == true) {
 			Log.err("Daemon already running!");
 			lock.unlock();
 			return;
 		}
 
 		running = true;
 		runningChangedCondition.signalAll();
 
 		initialize();
 
 		lock.unlock();
 
 		Log.debug("Daemon is now running.");
 
 	}
 
 	public static void stop() {
 		Log.out("Stopping Daemon ...");
 
 		lock.lock();
 		if (running == false) {
 			Log.err("Daemon already stopped!");
 			lock.unlock();
 			return;
 		}
 
 		terminate();
 
 		running = false;
 		runningChangedCondition.signalAll();
 
 		lock.unlock();
 
 		Log.debug("Daemon stopped.");
 
 	}
 
 	public static void waitForTermination() {
 		Log.out("Waiting for Daemon to shut down ...");
 
 		lock.lock();
 
 		while (running) {
 			try {
 				runningChangedCondition.await();
 			} catch (InterruptedException e) {
 				Log.warn(e);
 			}
 		}
 
 		lock.unlock();
 
 		Log.debug("Daemon shut down.");
 	}
 
 	private static void initialize() {
 		try {
 
 			// Load config file
 			configFile = new MainConfigFile();
 			configFile.init();
 			
 			taskScheduler = new TaskScheduler();
 
 			// Load Minecraft Downloader
 			switch (configFile.getConfig("mcEdition")) {
 			case "ftb":
 				mcDownloader = new FTBDownloader();
 				break;
 			case "vanilla":
 				mcDownloader = new VanillaDownloader();
 				break;
 			case "bukkit":
 				mcDownloader = new BukkitDownloader();
 				break;
 			default:
 				throw new RuntimeException("Unable to determine downloader!");
 			}
 
 			mcDownloader.initialize();
 
 			// ProcessBuilder pb = new
 			// ProcessBuilder(mcDownloader.getInvocationCommand());
 			// pb.directory(new File(mcDownloader.getFolderName()));
 			// Process proc = pb.start();
 
 			serverMonitor = new ServerMonitor(mcDownloader);
 			serverWrapper = serverMonitor.getWrapper();
 			serverMonitorThread = new Thread(serverMonitor);
 			serverMonitorThread.start();
 
 			taskScheduler.addTask(new HealthCheckTask());
 			
 			boolean autoPatcherEnabled = Boolean.parseBoolean(configFile.getConfig("autoPatcherEnabled"));
 			if(autoPatcherEnabled)
 				taskScheduler.addTask(new UpdateTask(mcDownloader));
 			
 			boolean autoBackupEnabled = Boolean.parseBoolean(configFile.getConfig("backupEnabled"));
 			if(autoBackupEnabled)
 				taskScheduler.addTask(new BackupTask(serverMonitor));
 			
 			taskSchedulerThread = new Thread(taskScheduler);
 			taskSchedulerThread.start();
 			/*
 			 * serverWrapper.startServer(); Thread.sleep(20000);
 			 * serverWrapper.stopServer(); Thread.sleep(3000);
 			 * serverWrapper.startServer();
 			 */
 		} catch (Exception e) {
 			Log.err(e);
 			throw new RuntimeException("Unable to initialize");
 		}
 
 	}
 
 	public static void kill() {
 		if (serverWrapper != null)
 			serverWrapper.killServer();
 	}
 
 	private static void terminate() {
 
 		Log.out("Shutting down taskScheduler...");
 		if (taskScheduler != null) {
 			taskScheduler.requestShutdown();
 			try {
 				if (taskSchedulerThread != null)
 					taskSchedulerThread.join();
 			} catch (InterruptedException e) {
 				Log.warn(e);
 			}
 		}
 
 		Log.out("Shutting down serverMonitor...");
 		if (serverMonitor != null) {
 			serverMonitor.initShutdown();
 			try {
 				if (serverMonitorThread != null)
 					serverMonitorThread.join();
 			} catch (InterruptedException e) {
 				Log.warn(e);
 			}
 		}
 
 	}
 
 	public static String getConfig(String config)
 			throws ConfigNotFoundException {
 
 		return configFile.getConfig(config);
 
 	}
 
 	public static void scheduleHealthCheck() {
 		serverMonitor.requestHealthCheck();
 	}
 
 	public static void enterMaintenanceMode(){
 		serverMonitor.enterMaintenanceMode();
 	}
 	
 	public static void exitMaintenanceMode(){
 		serverMonitor.exitMaintenanceMode();
 	}
 	
 	public static void runBackup(){
 		backupLock.lock();
 		try{
 			
 			boolean backupEnabled = Boolean.parseBoolean(configFile.getConfig("backupEnabled"));
 			if(backupEnabled)
 			{
 				Log.out("Running backup...");
				ProcessBuilder pb = new ProcessBuilder(configFile.getConfig("backupScript"));
 				Process backupProcess = pb.start();
 				backupProcess.waitFor();	
 				Log.debug("Successfully backed up.");
 			}
 			
 		} catch (Exception e){
 			Log.err(e);			
 		}finally{
 			backupLock.unlock();
 		}
 	}
 
 	public static void say(String msg) {
 		serverMonitor.say(msg);
 	}
 }
