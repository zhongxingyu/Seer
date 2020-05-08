 package linewars.gameLogic;
 
 import java.util.List;
 
 import linewars.gamestate.MapConfiguration;
 import linewars.init.PlayerData;
 import linewars.network.MessageProvider;
 import linewars.network.messages.Message;
 
 /**
  * This class handles the top-level logic related to updating the game - the so-called 'game loop'.
  * Its most important duty is determining how rapidly the game updates.
  * 
  * @author Taylor Bergquist
  *
  */
 public class TimingManager implements Runnable{
 	public static final int TIME_PER_TICK_MILLIS = 50;
 	public static final double GAME_TIME_PER_TICK_S = TIME_PER_TICK_MILLIS / 1000.0;
 	
 	private static final long SLEEP_PRECISION_MS = 4;
 	private static final long BUSY_WAIT_PRECISION_MS = 0;
 	
 	private GameStateUpdater manager;
 	private MessageProvider network;
 
 	private int nextTickID;
 	
 	public TimingManager(MapConfiguration map, List<PlayerData> players){
//		manager = new LogicBlockingManager(map, players);
		manager = new GameStateSwappingManager(map, players);
 //		manager = new SingleGameStateManager(map, players);
 		nextTickID = 1;
 	}
 	
 	/**
 	 * Tells this TimingManager what will be providing it with messages
 	 * @param n
 	 */
 	public void setClientReference(MessageProvider n){
 		network = n;
 	}
 	
 	/**
 	 * Provides a reference to this TimingManager's GameStateProvider.
 	 * @return this TimingManager's GameStateProvider
 	 */
 	public GameStateProvider getGameStateManager(){
 		return (GameStateProvider) manager;
 	}
 
 	@Override
 	public void run() {
 		boolean someoneWon = false;
 		
 		while(!someoneWon){
 			long loopStartTime = System.currentTimeMillis();
 			
 			//get orders from network
 			Message[] messagesForTick = network.getMessagesForTick(nextTickID);
 			
 			//give orders to manager
 			if (manager.addOrdersForTick(nextTickID, messagesForTick))
 				someoneWon = true;
 			//update tick id
 			++nextTickID;
 			//compute time to sleep for
 			long timeToSleep = TIME_PER_TICK_MILLIS - (System.currentTimeMillis() - loopStartTime);
 			
 			//idle until it's time to update again
 			if(timeToSleep > 0)
 			{
 				try {
 					accurateIdle(timeToSleep * 1000000);
 					//Thread.sleep(timeToSleep);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Attempts to return in nanosToWait nanoseconds.  More accurate than Thread.sleep(), but has greater CPU usage when it is almost time to wake up.
 	 * @param nanosToWait
 	 * @throws InterruptedException
 	 */
 	private void accurateIdle(long nanosToWait) throws InterruptedException{
 		final long end = System.nanoTime() + nanosToWait;
 		while(nanosToWait > 0){
 			if(nanosToWait > SLEEP_PRECISION_MS * 1000000){
 				Thread.sleep(1);
 			}
 			else if(nanosToWait > BUSY_WAIT_PRECISION_MS * 1000000){
 				Thread.sleep(0);
 			}
 			nanosToWait = end - System.nanoTime();
 		}
 	}
 }
