 package com.nexus.shutdown;
 
 import com.nexus.NexusServer;
 import com.nexus.event.events.server.ServerCloseEvent;
 import com.nexus.logging.NexusLog;
 
 public class ShutdownManager extends Thread{
 	
 	private static boolean ShutdownRequested = false;
 	private static boolean RebootRequested = false;
 	
 	private static Thread PreventShutdownThread;
 	
 	public static void RequestRestart(){
 		NexusLog.info("Rebooting server...");
 		NexusServer.getEventBus().post(new ServerCloseEvent.Restart());
 		RebootRequested = true;
		
		PreventShutdownThread.notify();
 	}
 	
 	public static void RequestShutdown(ShutdownReason reason){
 		NexusLog.info("Shutting down server (%s)", reason.name());
 		NexusServer.getEventBus().post(new ServerCloseEvent.Shutdown(reason));
 		ShutdownRequested = true;
 		System.exit(reason.getStatusCode());

		PreventShutdownThread.notify();
 	}
 	
 	public static void Start(){
 		ShutdownRequested = false;
 		RebootRequested = false;
 		PreventShutdownThread = new ShutdownManager();
 		PreventShutdownThread.setName("MainLoopLock");
 		PreventShutdownThread.setDaemon(true);
 		PreventShutdownThread.start();
 	}
 	
 	public static boolean ShouldReboot(){
 		return RebootRequested;
 	}
 	
 	public static void Join(){
 		try{
 			PreventShutdownThread.join();
		}catch(Exception e){
		}
		return;
 	}
 	
 	@Override
 	public void run(){
 		while (!ShutdownRequested && !RebootRequested){
 			try{
 				Thread.sleep(1000);
 			}catch(Exception e){
 			}
 		}
 	}
 	
 	public static boolean ShouldQuitMainLoop(){
 		return ShutdownRequested || RebootRequested;
 	}
 	
 	public static void OnUnexpectedServerShutdown(){
 		if(ShutdownRequested || RebootRequested) return;
 		NexusLog.severe("Somebody's closing me!");
 		RequestShutdown(ShutdownReason.CRASH);
 	}
 }
