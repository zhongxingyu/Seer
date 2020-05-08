 package com.powertac.tourney.beans;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import javax.annotation.PreDestroy;
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.powertac.tourney.scheduling.MainScheduler;
 import com.powertac.tourney.services.Database;
 import com.powertac.tourney.services.RunBootstrap;
 import com.powertac.tourney.services.RunGame;
 
 @Service("scheduler")
 public class Scheduler {
 	
 	@Autowired
 	MainScheduler gamescheduler;
 	
 	public static final String key = "scheduler";
 	public static boolean running = false;
 	public static boolean multigame = false;
 	private Timer watchDogTimer = null;
 	SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
 	
 	private HashMap<Integer,Integer> AgentIdToBrokerId = new HashMap<Integer,Integer>();
 	private HashMap<Integer,Integer> ServerIdToMachineId = new HashMap<Integer,Integer>();
 	
 	Properties props = new Properties();
 	
 	private HashMap<Integer,Timer> bootToBeRun = new HashMap<Integer,Timer>();
 	private HashMap<Integer,Timer> simToBeRun = new HashMap<Integer,Timer>();
 	
 	public static String getKey(){
 		return key;
 	}
 	
 	@PreDestroy
 	public void cleanUp() throws Exception {
 		  System.out.println("[INFO] Spring Container is destroyed! Scheduler clean up");
 		  if(watchDogTimer!=null){
 			  watchDogTimer.cancel();
 		  }
 		  for(Timer t: bootToBeRun.values()){
 			  if(t!=null){
 				  t.cancel();
 			  }
 		  }
 		  for(Timer t: simToBeRun.values()){
 			  if(t!=null){
 				  t.cancel();
 			  }
 		  }
 		  
 		  
 	}
 	
 	public Scheduler(){
 		
 		
 		try {
 			props.load(Database.class.getClassLoader().getResourceAsStream(
 					"/tournament.properties"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
 		this.startWatchDog();
 	}
 	
 	public void startWatchDog(){
 		if(!running){
 			Timer t = new Timer();
 			TimerTask watchDog = new TimerTask(){
 	
 				@Override
 				public void run() {
 					System.out.println("[INFO] " + dateFormatUTC.format(new Date()) + " : WatchDogTimer Looking for Games To Start..");
 					Database db = new Database();
 					// Check Database for startable games
 					try {
 						List<Game> games = db.getStartableGames();
 						
 						String hostip = "http://";
 						
 						try {
 							InetAddress thisIp =InetAddress.getLocalHost();
 							hostip += thisIp.getHostAddress() + ":8080";
 						} catch (UnknownHostException e2) {
 							// TODO Auto-generated catch block
 							e2.printStackTrace();
 						}
 						
 						for(Game g : games){
 							Tournament t = db.getTournamentByGameId(g.getGameId());
 							System.out.println("[INFO] " + dateFormatUTC.format(new Date()) + " : Game: " + g.getGameId() + " will be started...");
 							Scheduler.this.runSimTimer(g.getGameId(),new RunGame(g.getGameId(), hostip+"/TournamentScheduler/", t.getPomUrl(), props.getProperty("destination")), new Date());
 						}
 						
 						db.closeConnection();
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						this.cancel();
 						e.printStackTrace();
 					}
 					
 					// Use scheduler to schedule boostraps on multi-game tournaments
 					/*
 					db = new Database();
 					
 					
 					
 					try {
 						if(multigame==false){
 							multigame=true;
 							Tournament t = db.getTournamentByType("MULTI_GAME");
 							int noofagents = t.getMaxBrokers();
 							int noofcopies = t.getMaxBrokerInstances(); 
 							int noofservers = db.getMachines().size();
 							int iteration = 1,num;
 							int[] gtypes = {t.getSize1(),t.getSize2(),t.getSize3()};
 							int[] mxs = {t.getNumberSize1(),t.getNumberSize2(),t.getNumberSize3()};
 							
 							gamescheduler.init(noofagents, noofcopies, noofservers, gtypes, mxs);
 							gamescheduler.initializeAgentsDB(noofagents,noofcopies);
 							gamescheduler.initGameCube(gtypes,mxs);
 							
 							List<Broker> brokers = db.getBrokersRegistered(t.getTournamentId());
 							List<Machine> machines = db.getMachines();
 							db.closeConnection();
 							for(int i=0; i<brokers.size() ; i++){
 								AgentIdToBrokerId.put(i, brokers.get(i).getBrokerId());
 							}
 							
 							for(int i=0; i<machines.size();i++){
 								ServerIdToMachineId.put(i, machines.get(i).getMachineId());
 							}
 						}	
 							
 						int gamesScheduled = gamescheduler.Schedule();
 						
 						if (gamesScheduled==0){
 							System.out.println("[INFO] WatchDog reports no games to schedule this tick");
 						}else{
 							System.out.println("[INFO] WatchDog found games to schedule");
 							
 						}
 						
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						System.out.println("[ERROR] Scheduling exception!");
 						e.printStackTrace();
 					}
 				*/	
 				}
 				
 			};
 						
 			
 			System.out.println("[INFO] " + dateFormatUTC.format(new Date()) + " : Starting WatchDog...");
 			running=true;
 			t.schedule(watchDog, new Date(), 120000);
 			
 			this.watchDogTimer = t;
 			
 		}else{
 			System.out.println("[WARN] Watchdog already running");
 		}
 	}
 	
 	public void restartWatchDog(){
 		this.stopWatchDog();
 		this.startWatchDog();
 		
 	}
 	
 	public void stopWatchDog(){
 		if(watchDogTimer!=null){
 			watchDogTimer.cancel();
 			running=false;
 			System.out.println("[INFO] " + dateFormatUTC.format(new Date()) + " : Stopping WatchDog...");
 		}else{
 			System.out.println("[WARN] " + dateFormatUTC.format(new Date()) + " : WatchDogTimer Already Stopped");
 		}
 	}
 	
 	
 	public void runBootTimer(int gameId, TimerTask t, Date time){
 		Timer timer = new Timer();
 		timer.schedule(t, time);
 		bootToBeRun.put(gameId, timer);
 	}
 	
 	public void runSimTimer(int gameId, TimerTask t, Date time){
 		Timer timer = new Timer();
 		timer.schedule(t, time);
 		simToBeRun.put(gameId, timer);
 	}
 	
 	public void deleteSimTimer(int gameId){
 		Timer t = simToBeRun.get(gameId);
 		if (t!=null){
 			t.cancel();
 			simToBeRun.remove(gameId);
 		}else{
 			System.out.println("Timer thread is null for game: " + gameId);
 		}
 	}
 	
 	public void deleteBootTimer(int gameId){
 		Timer t = bootToBeRun.get(gameId);
 		if (t!=null){
 			t.cancel();
 			bootToBeRun.remove(gameId);
 		}else{
 			System.out.println("Timer thread is null for game: " + gameId);
 		}
 	}
 	
 
 }
