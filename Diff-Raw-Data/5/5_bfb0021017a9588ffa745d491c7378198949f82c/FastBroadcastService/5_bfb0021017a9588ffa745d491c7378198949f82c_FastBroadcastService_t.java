 package it.unipd.testbase.protocol;
 
 import it.unipd.testbase.AppController;
 import it.unipd.testbase.eventdispatcher.EventDispatcher;
 import it.unipd.testbase.eventdispatcher.event.IEvent;
 import it.unipd.testbase.eventdispatcher.event.location.LocationChangedEvent;
 import it.unipd.testbase.eventdispatcher.event.location.UpdateLocationEvent;
 import it.unipd.testbase.eventdispatcher.event.message.UpdateStatusEvent;
 import it.unipd.testbase.eventdispatcher.event.protocol.NewMessageArrivedEvent;
 import it.unipd.testbase.eventdispatcher.event.protocol.SendAlertMessageEvent;
 import it.unipd.testbase.eventdispatcher.event.protocol.SendBroadcastMessageEvent;
 import it.unipd.testbase.eventdispatcher.event.protocol.ShowSimulationResultsEvent;
 import it.unipd.testbase.eventdispatcher.event.protocol.SimulationStartEvent;
 import it.unipd.testbase.eventdispatcher.event.protocol.StopSimulationEvent;
 import it.unipd.testbase.helper.DebugLogger;
 import it.unipd.testbase.helper.LogPrinter;
 import it.unipd.testbase.wificonnection.message.IMessage;
 import it.unipd.testbase.wificonnection.message.MessageBuilder;
 import it.unipd.testbase.wificonnection.transmissionmanager.sender.IPaketSender;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import android.location.Location;
 
 /**
  * An implementation of fast broadcast estimation phase.
  * 
  * @author Moreno Ambrosin
  *
  */
 public class FastBroadcastService implements IFastBroadcastComponent{
 	
 	protected static final String TAG = "it.unipd.testbase";
 	
 	
 	public static final int DEFAULT_RANGE = 300;
 	
 	/**
 	 * Alert message for debugging purposes
 	 */
 	public static final int ALERT_MESSAGE_TYPE = 2;
 	
 	/**
 	 * Identifies Hello messages, used to perform range estimation
 	 */
 	public static final int HELLO_MESSAGE_TYPE = 3;
 	
 	private DebugLogger logger = new DebugLogger(FastBroadcastService.class);
 	
 	private static FastBroadcastService instance = null;
 	
 	
 	
 	public static FastBroadcastService getInstance() {
 		if(instance == null)
 			instance = new FastBroadcastService();
 		return instance;
 	}
 	
 	/**
 	 * Current-turn Maximum Front Range
 	 */
 	private double cmfr = DEFAULT_RANGE;
 	/**
 	 * Current-turn Maximum Back Range
 	 */
 	private double cmbr = DEFAULT_RANGE;
 	/**
 	 * Last-turn Maximum Front Range
 	 */
 	private double lmfr = DEFAULT_RANGE;
 	/**
 	 * Last-turn Maximum Back Range
 	 */
 	private double lmbr = DEFAULT_RANGE;
 	
 	/**
 	 * Error used to determine if two devices are moving on the same direction
 	 * 
 	 */
 	private final float ERROR = 1f;
 	
 	/**
 	 * Tells whether another hello message arrived
 	 */
 	private Boolean helloMessageArrived = false;
 	
 	/**
 	 * Message forwarder thread used to send Alert messages back
 	 * 
 	 */
 	private MessageForwarder messageForwarder = new MessageForwarder();
 	
 	/**
 	 * Filters
 	 */
 	private List<DistanceFilter> filters = new ArrayList<DistanceFilter>();
 	
 	/**
 	 * Static field which indicates the simulated actual range of each device
 	 * This value is used to filter messages received from a "distance" > ACTUAL_MAX_RANGE
 	 */
 	private static final int ACTUAL_MAX_RANGE = 1000;
 	
 	/**
 	 * Slot size in milliseconds
 	 */
 	private static final int SLOT_SIZE = 250;
 	
 	/**
 	 * Turn duration in milliseconds
 	 */
 	public static final int TURN_DURATION = 500;
 	
 	/**
 	 * Contention window bounds
 	 */
 	private static int CwMax = 10;
 	private static int CwMin = 5;
 	
 	private Location currentLocation;
 	
 	private HelloMessageSender helloMessageSender;
 	
 	private List<Integer> __timeWaited	= new ArrayList<Integer>();
 	private List<Integer> __cwndSize	= new ArrayList<Integer>();
 	
 	/****************************************************** DECLARATIONS ***************************************************/
 	
 	/**
 	 * Interface used to define messages' filters 
 	 * 
 	 * @author Moreno Ambrosin
 	 *
 	 */
 	public interface DistanceFilter {
 		/**
 		 * Returns message if is not filtered
 		 * 
 		 * @param message
 		 * @return
 		 */
 		boolean filter(IMessage message);
 	}
 	
 	/**
 	 * Task scheduled at a fixed TURN_DURATION time, which sends out an hello message 
 	 * to perform Range estimation
 	 * 
 	 * @author Moreno Ambrosin
 	 */
 	private class HelloMessageSender extends Thread {
 		
 		private Random randomGenerator = new Random();
 		private boolean keepRunning = true;
 		private DebugLogger logger = new DebugLogger(HelloMessageSender.class);
 		
 		@Override
 		public void run(){
 			while(keepRunning){
 				try {
 					Thread.sleep(TURN_DURATION);
 				} catch (InterruptedException e) {
 					logger.e(e);
 				}
 				int randomTime = 0;
 				// Store previous current range into last current range
 				lmbr = cmbr;
 				lmfr = cmfr;
 				
 				synchronized (this) {
 					helloMessageArrived = false;
 					randomTime = randomGenerator.nextInt(TURN_DURATION);
 					try{
 						logger.d("Going to sleep for "+randomTime+" ms");
 						this.wait(randomTime);
 					}catch(InterruptedException ex){
 						logger.e(ex);
 						stopExecuting();
 					}
 				}
 				// After waiting a random time check whether another hello message arrived, and if not, sends an hello message
 				if(!helloMessageArrived){
 					this.sendHelloMessage();
 					logger.d("Sent Hello message to all after " +(TURN_DURATION+randomTime)+"ms");
 				}else{
 					logger.d("Hello Message was already sent!!");
 				}
 			}
 			logger.d("Tearing down Hello Message Sender");
 		}
 		
 		/**
 		 * Stops Hello Message Sender Execution
 		 * 
 		 */
 		public synchronized void stopExecuting(){
 			keepRunning = false;
 			// In case thread was waiting
 			this.notify();
 			logger.d("Hello Message Handler asked to Stop");
 		}
 		
 		/**
 		 * Sends hello message in broadcast
 		 * 
 		 */
 		private void sendHelloMessage(){
 			IMessage helloMessage = MessageBuilder.getInstance().getMessage(
 					HELLO_MESSAGE_TYPE,
 					IPaketSender.BROADCAST_ADDRESS,
 					AppController.MAC_ADDRESS);
 			
 			helloMessage.addContent(IMessage.SENDER_LATITUDE_KEY,currentLocation.getLatitude()+"");
 			helloMessage.addContent(IMessage.SENDER_LONGITUDE_KEY,currentLocation.getLongitude()+"");
 			// Add sender range estimation
 			helloMessage.addContent(IMessage.SENDER_RANGE_KEY,Math.max(lmfr, cmfr)+"");
 			// Add the bearing
 			helloMessage.addContent(IMessage.SENDER_DIRECTION_KEY,currentLocation.getBearing()+"");
 			
 			helloMessage.prepare();
 			
 			// Send the hello message broadcast
 			EventDispatcher.getInstance().triggerEvent(new SendBroadcastMessageEvent(helloMessage));
 		}
 	}
 	
 	/**
 	 * Thread used to eventually forward an ALERT message. This thread waits on his queue for an incoming Alert
 	 * Message, and when arrives it decides weather to forward or not.
 	 * 
 	 * @author Moreno Ambrosin
 	 *
 	 */
 	private class MessageForwarder extends Thread {
 		
 		/*********************************** DECLARATIONS *************************************/
 		
 		private ArrayBlockingQueue<IMessage> messageQueue = new ArrayBlockingQueue<IMessage>(10);
 		private Random randomGenerator = new Random();
 		private Object synchPoint = new Object();
 		private boolean keepRunning = true;
 		private DebugLogger logger = new DebugLogger(MessageForwarder.class);
 		
 		/************************************* METHODS ****************************************/
 		
 		/**
 		 * Method used to send a message to the message forwarder
 		 * 
 		 * @param message
 		 * @throws InterruptedException
 		 */
 		public void put(IMessage message) throws InterruptedException{
 			LogPrinter.getInstance().writeTimedLine("ALERT PUT IN QUEUE. (SIZE "+(messageQueue.size()+1)+")");
 			messageQueue.put(message);
 			// As soon as a new message arrived, notify the forwarder, to let him preceding without waiting 
 			// for the entire random amount 
 			synchronized (synchPoint) {
 				synchPoint.notify();
 			}
 		}
 		
 		/**
 		 * Stops Message Forwarder Execution
 		 */
 		public void stopExecuting(){
 			keepRunning = false;
 			logger.d("KeepRunning = "+keepRunning);
 		}
 		
 		@Override
 		public void run() {
 			while(keepRunning){
 				IMessage message = null;
 				try {
 					message = messageQueue.take();
 					Map<String,String> content = message.getContent();
 					int hopCount = Integer.valueOf(content.get(IMessage.MESSAGE_HOP_KEY));
 					LogPrinter.getInstance().writeTimedLine("ALET TAKEN FROM QUEUE (SIZE = "+messageQueue.size()+")");
 					LogPrinter.getInstance().writeTimedLine("" +
 							"CURRENT POSITION = ("+currentLocation.getLatitude()+","+currentLocation.getLongitude()+") " +
 									"#HOPS = "+hopCount);
 				} catch (InterruptedException e) {
 					logger.e(e);
 					return;
 				}
 				
 				// If I'm here, an Alert message Arrived
 				logger.d("ALERT MESSAGE ARRIVED!!");
 				// now I am sure message != null
 				Map<String,String> content = message.getContent();
 				double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
 				double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
 				double maxRange 	= Double.valueOf(content.get(IMessage.SENDER_RANGE_KEY));
 				float direction 	= Float.valueOf(content.get(IMessage.SENDER_DIRECTION_KEY));
 				int hopCount 		= Integer.valueOf(content.get(IMessage.MESSAGE_HOP_KEY));
 				hopCount ++;
 				float distance = getDistance(latitude, longitude);
 				
 				EventDispatcher.getInstance().triggerEvent(new UpdateStatusEvent("Alert Message arrived from "+message.getSenderID()));
 				
 				logger.d("Distance = "+distance);
 				logger.d("MaxRange = "+maxRange);
 				
 				int contentionWindow = CwMin;
 				if(maxRange - distance > 0){
 					// If the difference is negative, the device will use minimum contention window.
 					contentionWindow = (int)Math.floor((((maxRange-distance)/maxRange) * (CwMax-CwMin))+CwMin); 
 				}
 
 				logger.d("Contention Window = "+contentionWindow);
 				
 				// wait for a random time... 
 				int timeToWait = 0;
 				synchronized (synchPoint) {
 					try {
 						int rnd = randomGenerator.nextInt(contentionWindow-1)+1;
 						timeToWait = rnd * SLOT_SIZE;
 						LogPrinter.getInstance().writeTimedLine("CONTENTION WINDOW = "+contentionWindow+" WAITING "+rnd+" SLOTS");
 						logger.d("BroadcastPhase: sleeping for "+rnd+" slots");
 						// Waiting until:
 						//    1) A message arrives, so stop waiting and change position
 						//    2) Time expired, and so forward the message
 						synchPoint.wait(timeToWait);
 					} catch (InterruptedException e) {
 						logger.e(e);
 						return;
 					}
 				}
 				logger.d("BroadcastPhase: waking up");
 				if(messageQueue.size() == 0){
 					// No message arrived while I was asleep
 					
 					__cwndSize.add(contentionWindow);
 					__timeWaited.add(timeToWait);
 					
 					sendAlert(hopCount);
 					EventDispatcher.getInstance().triggerEvent(new UpdateStatusEvent("Alert Message FORWARDED"));
 				}else{
 					EventDispatcher.getInstance().triggerEvent(new UpdateStatusEvent("Alert Message NOT FORWARDED"));
 					LogPrinter.getInstance().writeTimedLine("DOUBLE ALERT, DISCARDED. " +
 							"QUEUE SIZE = "+messageQueue.size());
 					logger.d("Double Alert, message discarded");
 					// At least another message arrived
 					if(receivedFromBack(direction, latitude, longitude)){
 						// Someone else forwarded it already
 						LogPrinter.getInstance().writeTimedLine("MESSAGE RECEIVED FROM BACK!!");
 						EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
 						
 						// Re-send the message to me to simulate message just arrived
 //						IMessage m;
 //						try {
 //							m = messageQueue.take();
 //							EventDispatcher.getInstance().triggerEvent(
 //									new NewMessageArrivedEvent(m,m.getSenderID()));
 //						} catch (InterruptedException e) {
 //							logger.e(e);
 //						}
 					}
 					LogPrinter.getInstance().writeTimedLine("MESSAGE NOT FORWARDED!!");
 				}
 				LogPrinter.getInstance().writeLine("\n");
 			}
 			logger.d("Tearing down Message Forwarder");
 		}
 	}
 	
 	/************************************************** METHODS ***********************************************/
 	
 	/**
 	 * Method used to set helloMessageArrived variable.
 	 * 
 	 * @param arrived
 	 */
 	protected synchronized void setHelloMessageArrived(boolean arrived){
 		this.helloMessageArrived = arrived;
 	}
 	
 	/**
 	 * Performs updates necessary when a message arrives, on a separate Thread
 	 * 
 	 * @param message
 	 */
 	private void hanldeHelloMessage(final IMessage message){
 		// In case an hello message arrives before current location is setted first
 		// just ignore it.
 		if(currentLocation == null){
 			logger.d("Hello message arrived before location setting, ignoring it");
 			return;
 		}
 		
 		new Thread(){
 			public void run() {
 				Map<String,String> content = message.getContent();
 				double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
 				double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
 				double max_range  	= Double.parseDouble(content.get(IMessage.SENDER_RANGE_KEY));
 				// Retrieve the sender bearing
 				float direction 	= Float.valueOf(content.get(IMessage.SENDER_DIRECTION_KEY));
 				float distance 		= getDistance(latitude, longitude);
 				
 				// If I'm in the same direction, check whether I'm in front of him or not
 				if(areEquals(currentLocation.getBearing(),direction,ERROR)){
 					if(receivedFromBack(direction,latitude,longitude)){
 						// Received from back
 						cmbr = Math.max(cmbr, Math.max(distance, max_range));
 						logger.d("Distance = "+distance+"   cmbr = "+cmbr);
 						logger.d("Sono davanti, aggiorno cmbr a = "+cmbr);
 					}else{
 						// Received from front
 						cmfr = Math.max(cmfr, Math.max(distance,max_range));
 						logger.d("Sono dietro, aggiorno cmfr a = "+cmfr);
 					}
 				}else{
 					// Different directions, ignore the message
 					logger.d("Messaggio proveniente da direzione diversa, lo ignoro");
 				}
 			}
 		}.start();
 	}
 	
 	/**
 	 * Tells if the direction are the same, within a certain error
 	 * 
 	 * @param myBearing
 	 * @param direction
 	 * @return
 	 */
 	private boolean areEquals(float myBearing,float direction,float error){
 		return Math.abs(myBearing-direction) < error;
 	}
 	
 	/**
 	 * Tells if a received message comes from back or not 
 	 * 
 	 * @param direction
 	 * @param senderLatitude
 	 * @param senderLongitude
 	 * 
 	 * @return true if the sender is back, false otherwise
 	 */
 	private boolean receivedFromBack(float direction,double senderLatitude,	double senderLongitude){
 		if(direction <= 180){
 			if(direction == 90){
 				if(senderLongitude < currentLocation.getLongitude()){
 					return true;
 				}
 			}else if(direction < 90){
 				// 1° quadrante
 				if(senderLatitude < currentLocation.getLatitude()){
 					// Sono davanti
 					return true;
 				}
 			}else{
 				// 4° quadrante
				if(senderLatitude > currentLocation.getLatitude()){
 					// Sono davanti
 					return true;
 				}
 			}
 		} else {
 			if(direction == 270){
 				if(senderLongitude > currentLocation.getLongitude()){
 					return true;
 				}
 			}else if(direction > 270){
 				// 2° quadrante
				if(senderLatitude < currentLocation.getLatitude()){
 					// Sono davanti
 					return true;
 				}
 			}else{
 				// 3° quadrante
 				if(senderLatitude > currentLocation.getLatitude()){
 					// Sono davanti
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 
 	/**
 	 * Stops the simulation. If quit == true, it quits the simulation.
 	 * 
 	 * @param quit
 	 */
 	private void stopSimulation(boolean quit){
 		// Stop hello message sender and message forwarder
 		if(quit && helloMessageSender != null){
 			logger.d("STOPPING HELLO MESSAGE SENDER");
 			helloMessageSender.stopExecuting();
 		}
 		if(quit && messageForwarder != null){
 			logger.d("STOPPING MESSAGE FORWARDER");
 			messageForwarder.stopExecuting();
 		}
 		
 	}
 	
 	/**
 	 * Default constructor. It adds a filter for the distance and registers this instance as
 	 * a component to Event Bus
 	 * 
 	 */
 	public FastBroadcastService() {
 		logger.d("Estimator Service is Up");
 
 		filters.add(new DistanceFilter(){
 			@Override
 			public boolean filter(IMessage message) {
 				Map<String,String> content = message.getContent();
 				double latitude 	= Double.parseDouble(content.get(IMessage.SENDER_LATITUDE_KEY));
 				double longitude 	= Double.parseDouble(content.get(IMessage.SENDER_LONGITUDE_KEY));
 				float distance = getDistance(latitude, longitude);
 				// If distance is > actual maximum range, message shouldn't be heard...
 				
 				if(message.getType() == ALERT_MESSAGE_TYPE){
 					LogPrinter.getInstance().writeTimedLine("ACTUAL RANGE = "+ACTUAL_MAX_RANGE);
 					LogPrinter.getInstance().writeTimedLine("ESTIMATED RANGE = "+distance);
 				}
 				
 				if(distance > ACTUAL_MAX_RANGE) return false;
 				return true;
 			}
 		});
 		register();
 	}
 	
 	/**
 	 * Handles an incoming Alert Message in a separate Thread
 	 * 
 	 * @param message
 	 */
 	private void handleAlertMessage(final IMessage message){
 		// Using a new thread to prevent main thread interruption
 		new Thread(){
 			@Override
 			public void run() {
 				try {
 					messageForwarder.put(message);
 				} catch (InterruptedException e) {
 					logger.e(e);
 				}
 			}
 		}.start();
 	}
 	
 	
 	/**
 	 * Returns estimated transmission range
 	 * 
 	 * @return
 	 */
 	private double getEstimatedTrasmissionRange() {
 		return Math.max(cmbr,lmbr);
 	}
 	
 	/**
 	 * Returns the distance between current position and given latitude/logitude
 	 * 
 	 * @param latitude
 	 * @param longitude
 	 * @return
 	 */
 	private float getDistance(double latitude, double longitude){
 		float[] results = new float[3];
 		Location.distanceBetween(
 				currentLocation.getLatitude(),
 				currentLocation.getLongitude(),
 				latitude, 
 				longitude,
 				results);
 		return results[0];
 	}
 	
 	/**
 	 * Sends an alert message in broadcast, and register that on LogPrinter
 	 * 
 	 * @param hops: current hops number
 	 */
 	private void sendAlert(int hops) {
 		
 		IMessage message = MessageBuilder.getInstance().getMessage(
 				ALERT_MESSAGE_TYPE, 
 				IPaketSender.BROADCAST_ADDRESS,
 				AppController.MAC_ADDRESS
 		);
 		message.addContent(IMessage.SENDER_LATITUDE_KEY, ""+currentLocation.getLatitude());
 		message.addContent(IMessage.SENDER_LONGITUDE_KEY, ""+currentLocation.getLongitude());
 		message.addContent(IMessage.SENDER_RANGE_KEY, ""+getEstimatedTrasmissionRange());
 		message.addContent(IMessage.SENDER_DIRECTION_KEY, ""+currentLocation.getBearing());
 		message.addContent(IMessage.MESSAGE_HOP_KEY, ""+hops);
 		message.prepare();
 		
 		EventDispatcher.getInstance().triggerEvent(new SendBroadcastMessageEvent(message));
 		
 		if(hops == 0){
 			LogPrinter.getInstance().writeTimedLine("ALERT message SENT, #HOPS = "+hops);
 		}else{
 			LogPrinter.getInstance().writeTimedLine("ALERT message FROWARDED. #HOPS = "+hops);
 		}
 		
 		EventDispatcher.getInstance().triggerEvent(new UpdateLocationEvent());
 	}
 
 	private boolean started = false;
 	
 	@Override
 	public void handle(IEvent event) {
 		if(event.getClass().equals(SimulationStartEvent.class)){
 			if(!started){
 				logger.d("Inizializzo HelloMessageSender");
 			
 				if(helloMessageSender == null ) {
 					helloMessageSender = new HelloMessageSender();
 				}
 				helloMessageSender.start();
 				if(messageForwarder == null) {
 					messageForwarder = new MessageForwarder();
 				}
 				messageForwarder.start();
 				started = true;
 			}
 			return;
 		}
 		if(event.getClass().equals(NewMessageArrivedEvent.class)){
 			NewMessageArrivedEvent ev = (NewMessageArrivedEvent) event;
 			
 			// Filtering the message by distance
 			boolean arrived = true;
 			for(DistanceFilter filter : filters){
 				arrived = arrived & (filter.filter(ev.message));
 			}
 			
 			if(!arrived){
 				if(ev.message.getType() == ALERT_MESSAGE_TYPE){
 					LogPrinter.getInstance().writeTimedLine(
 						"ALERT DISCARDED: TOO FAR AWAY " +
 						"(#HOPS = "+ev.message.getContent().get(IMessage.MESSAGE_HOP_KEY)+")");
 				}
 				logger.d("MESSAGE SHOULDN'T HAVE BEEN ARRIVED");
 				return;
 			}
 			// If we reach this code, the message shoul have been arrived!
 			if(ev.message.getType() == ALERT_MESSAGE_TYPE){
 				LogPrinter.getInstance().writeTimedLine(
 						"ALERT RECEIVED FROM "+ev.senderID+". " +
 						"#HOPS = "+ev.message.getContent().get(IMessage.MESSAGE_HOP_KEY));
 				this.handleAlertMessage(ev.message);
 			}else if(ev.message.getType() == HELLO_MESSAGE_TYPE){
 				this.setHelloMessageArrived(true);
 				this.hanldeHelloMessage(ev.message);
 			}
 			return;
 		}
 		if(event.getClass().equals(LocationChangedEvent.class)){
 			LocationChangedEvent ev = (LocationChangedEvent) event;
 			this.currentLocation = ev.location;
 			return;
 		}
 		if(event.getClass().equals(StopSimulationEvent.class)){
 			StopSimulationEvent ev = (StopSimulationEvent) event;
 			
 			stopSimulation(ev.quitSimulation);
 			
 			LogPrinter.getInstance().writeLine("\n");
 			
 			int avgTime = 0;
 			for(Integer t : __timeWaited){
 				LogPrinter.getInstance().writeLine("time = "+t);
 				avgTime = avgTime + t;
 			}
 			if(__timeWaited.size() > 0){
 				avgTime = avgTime / __timeWaited.size();
 			}
 			
 			int cwnd = 0;
 			for(Integer s : __cwndSize){
 				LogPrinter.getInstance().writeLine("Size = "+s);
 				cwnd = cwnd + s;
 			}
 			if(__cwndSize.size() > 0){
 				cwnd 	= cwnd / __cwndSize.size();
 			}
 			
 			LogPrinter.getInstance().writeTimedLine("AVG TIME\t\t= "+avgTime+"ms");
 			LogPrinter.getInstance().writeTimedLine("AVG CWND SIZE\t= "+cwnd);
 			
 			if(ev.showResults){
 				EventDispatcher.getInstance().triggerEvent(new ShowSimulationResultsEvent());
 				__timeWaited.clear();
 				__cwndSize.clear();
 			}
 			// Reset default range
 			lmbr = cmbr = cmfr = lmfr = DEFAULT_RANGE;
 			return;
 		}
 		if(event.getClass().equals(SendAlertMessageEvent.class)){
 			SendAlertMessageEvent ev = (SendAlertMessageEvent) event;
 			sendAlert(ev.hops);
 			return;
 		}
 	}
 
 	@Override
 	public void register() {
 		List<Class<? extends IEvent>> events = new ArrayList<Class<? extends IEvent>>();
 		events.add(SimulationStartEvent.class);
 		events.add(LocationChangedEvent.class);
 		events.add(NewMessageArrivedEvent.class);
 		events.add(StopSimulationEvent.class);
 		events.add(SendAlertMessageEvent.class);
 		EventDispatcher.getInstance().registerComponent(this, events);
 	}
 }
