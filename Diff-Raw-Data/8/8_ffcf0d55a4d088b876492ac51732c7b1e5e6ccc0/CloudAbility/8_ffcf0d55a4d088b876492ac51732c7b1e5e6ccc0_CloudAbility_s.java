 /**
  * Copyright (C) 2012  Lipu Fei
  */
 package org.cloudability;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.apache.log4j.BasicConfigurator;
 
 import org.cloudability.resource.ResourceManager;
 import org.cloudability.scheduling.Job;
 import org.cloudability.scheduling.Scheduler;
 import org.cloudability.server.job.ClientJobListener;
 import org.cloudability.util.CloudConfigException;
 
 /**
  * The main class of CloudAbility
  * @author Lipu Fei
  * @version 0.1
  *
  */
 public class CloudAbility {
 
 	static {
 		/* add shutdown hook */
 		Thread shutdownThread = new Thread(new ShutdownHook());
 		shutdownThread.setPriority(Thread.MAX_PRIORITY);
 		Runtime.getRuntime().addShutdownHook(shutdownThread);
 	}
 
 	public static ClientJobListener listenerThread;
 	public static Thread schedulerThread;
 
 	/**
 	 * @param args
 	 * @throws InterruptedException
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws InterruptedException, IOException {
 		/* common initialization */
 		try {
 			CentralManager.initialize("config/cloud.config");
 			ResourceManager.initialize();
 
 		} catch (CloudConfigException e) {
 			e.printStackTrace();
 		}
 
		//testSemiAuto();
 		testFullAuto();
 	}
 
 	/**
 	 * This test tests full automation.
 	 * To exit the system, just press ENTER once, and it will start the
 	 * finalization. After the system has been finalized, a statistics file
 	 * will be created named "statistics.txt".
 	 */
 	public static void testFullAuto() {
 		/* start scheduler */
 		schedulerThread = new Thread(new Scheduler());
 		schedulerThread.start();
 
 		/* start request listener */
 		int port = Integer.parseInt(
 			CentralManager.instance().getConfigMap().get("CONFIG.LISTEN_PORT")
 			);
 		listenerThread = new ClientJobListener(port);
 		listenerThread.start();
 
 		try {
 			while (true) {
 				System.in.read();
 				System.exit(0);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static void testSemiAuto() {
 		/* start scheduler */
 		Scheduler scheduler = new Scheduler();
 		Thread schedulerThread = new Thread(scheduler);
 		schedulerThread.start();
 
 		/* start request listener */
 		int port = Integer.parseInt(
 			CentralManager.instance().getConfigMap().get("CONFIG.LISTEN_PORT")
 			);
 		Thread listenerThread = new Thread(new ClientJobListener(port));
 		listenerThread.start();
 
 		try {
 			int i = 0;
 			while (true) {
 				System.in.read();
 
 				/* at most 5 jobs */
 				if (i++ < 5) {
 					HashMap<String, String> p = new HashMap<String, String>();
 					p.put("REMOTE_DIR", "/opt");
 					p.put("APP.LOCAL", "/home/lfei/in4392/largelab/ffmpeg-centos.tar.gz");
 					p.put("APP.REMOTE", "ffmpeg/bin/ffmpeg");
 					p.put("APP.PARAMS", String.format("-i /opt/cloudatlas-trailer1b_h1080p.mov -target ntsc-dvd -y /opt/output%d.mov", i));
 					p.put("INPUT.LOCAL", "/home/lfei/in4392/largelab/cloudatlas-trailer1b_h1080p.mov");
 					p.put("INPUT.REMOTE", "cloudatlas-trailer1b_h1080p.mov");
 					p.put("OUTPUT.LOCAL", "/home/lfei/in4392/largelab");
 					p.put("OUTPUT.REMOTE", String.format("output%d.mov", i));
 					Job job = new Job(i, p);
 					CentralManager.instance().getPendingJobQueue().addJob(job);
 					continue;
 				}
 				System.exit(0);
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
