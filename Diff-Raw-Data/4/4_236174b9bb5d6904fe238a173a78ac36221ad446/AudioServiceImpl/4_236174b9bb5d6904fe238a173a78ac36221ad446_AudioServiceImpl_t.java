 package ch.cern.atlas.apvs.server;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 
 import org.asteriskjava.live.AsteriskServer;
 import org.asteriskjava.live.DefaultAsteriskServer;
 import org.asteriskjava.manager.AuthenticationFailedException;
 import org.asteriskjava.manager.ManagerConnection;
 import org.asteriskjava.manager.ManagerConnectionFactory;
 import org.asteriskjava.manager.ManagerEventListener; 
 import org.asteriskjava.manager.TimeoutException;
 import org.asteriskjava.manager.action.HangupAction;
 import org.asteriskjava.manager.event.ManagerEvent;
 
 import ch.cern.atlas.apvs.client.AudioException;
 import ch.cern.atlas.apvs.client.event.AudioSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.PtuSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.event.ServerSettingsChangedEvent;
 import ch.cern.atlas.apvs.client.service.AudioService;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.client.settings.ServerSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 
 
 
 public class AudioServiceImpl extends ResponsePollService implements AudioService, ManagerEventListener {
 
 	private ManagerConnection managerConnection;
 	private AsteriskServer asteriskServer;
 	private AudioSettings voipAccounts;
 	
 	private ExecutorService executorService;
 	private Future<?> connectFuture;
 	
 	// Account Details
 	private static final String ASTERISK_URL = "pcatlaswpss02.cern.ch";
 	private static final String AMI_ACCOUNT = "manager";
 	private static final String PASSWORD = "password";
 	
 	
 	private static final String CONTEXT = "internal";
 	private static final int PRIORITY = 1;
 	private static final int TIMEOUT = 20000;
 	
 	private RemoteEventBus eventBus;
 	
 	public AudioServiceImpl(){
 		if(eventBus != null)
 			return;
 		System.out.println("Creating AudioService...");
 		eventBus = APVSServerFactory.getInstance().getEventBus();
 		executorService = Executors.newSingleThreadExecutor();
 	}
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException{
 		super.init(config);
 		
 		//if(audioHandler != null)
 			//return;
 		
 		System.out.println("Starting Audio Service...");
 		
 		//audioHandler = new AudioHandler(eventBus);
 	
 		//Local List of the current Users
 		voipAccounts = new AudioSettings();
 		
 		//Asterisk Connection Manager 
 		ManagerConnectionFactory factory = new ManagerConnectionFactory(ASTERISK_URL, AMI_ACCOUNT, PASSWORD);
 		this.managerConnection = factory.createManagerConnection();
 		
 		// Eases the communication with asterisk server
 		asteriskServer = new DefaultAsteriskServer(managerConnection);
 	
 		
 		// Event handler
 		managerConnection.addEventListener(this);
 		
 		connectFuture = executorService.submit(new Runnable() {
 			
 			@Override
 			public void run() {
 				System.err.println("Login in to Asterisk Server on " + ASTERISK_URL.toLowerCase() + " ...");
 				try {
 					login();
 				} catch (AudioException e) {
 					e.printStackTrace();
 				}
 				
 			}
 		});
 		
 	}
 	
 //*********************************************	
 	// Constructor
 	
 	public void login() throws AudioException{
 		try{
 			managerConnection.login();
 		}catch (IllegalStateException e){
 			throw new AudioException(e.getMessage());
 		}catch (IOException e){
 			throw new AudioException(e.getMessage());
 		}catch (AuthenticationFailedException e) {
 			throw new AudioException("Failed login to Asterisk Manager: " + e.getMessage());
 		}catch (TimeoutException e) {
 			throw new AudioException("Login to Asterisk Timeout: " + e.getMessage());
 		}
 	}
 
 //*********************************************	
 	// RPC Methods	
 	
 	@Override
 	public void call(String callerOriginater, String callerDestination) {
 		asteriskServer.originateToExtension(callerOriginater, CONTEXT, callerDestination, PRIORITY, TIMEOUT);
 	}
 
 	@Override
 	public void hangup(String channel) throws AudioException {
 		HangupAction hangupCall = new HangupAction(channel);
 		try{
 			managerConnection.sendAction(hangupCall);
 		}catch (IllegalArgumentException e){
 			throw new AudioException(e.getMessage());
 		}catch (IllegalStateException e){
 			throw new AudioException(e.getMessage());
 		}catch (IOException e){
 			throw new AudioException(e.getMessage());
 		}catch (TimeoutException e){
 			throw new AudioException("Timeout: " + e.getMessage());
 		}
 	}
 	
 	
 //*********************************************	
 	// Event Handler
 	
 	@Override
 	public void onManagerEvent(ManagerEvent event) {
 		String[] eventContent = event.toString().split("\\[");
 		System.err.println("Event " + eventContent[0] );
 				
 		// NewChannelEvent    	
 		if(eventContent[0].contains("NewChannelEvent")){
 	    	newChannelEvent(eventContent[1]);
 		}
 
 	    // BridgeEvent
 		if(eventContent[0].contains("BridgeEvent"))
 	    	;//bridgeEvent(eventContent[1]);
 	    	
 		// PeerStatusEvent
 		if(eventContent[0].contains("PeerStatusEvent"))
 			peerStatusEvent(eventContent[1]);
 		
 		// HangupEvent
 		if(eventContent[0].contains("HangupEvent"))
 			;//hangupEvent(eventContent[1]);
 		
 		((RemoteEventBus)eventBus).fireEvent(new AudioSettingsChangedEvent(voipAccounts));
 	}
 	
 	public String contentValue(String content){
 		return content.substring(content.indexOf("'",0)+1,content.indexOf("'",content.indexOf("'",0)+1));
 	}
 	
 	
 //*********************************************	
 	// Event Methods
 	
 	//New Channel
 	public void newChannelEvent(String channel){
 		//System.err.println(channel);
 		String[] list = channel.replace(',','\n').split("\\n");
 		for (int i=0 ; i<list.length; i++){
 			//System.err.println("ENTROU");
 			if(list[i].contains("channel=")){
 				channel=contentValue(list[i]);
 				String[] aux = channel.split("-");
 				//TODO
 				/*
 				 * 
 				 * Update channel to voipAccount list
 				 * 
 				 */
 				//usersList.get(getIndexOfUsername(aux[0])).setActiveCallChannel(channel);
 				//System.out.println(usersList.get(getIndexOfUsername(aux[0])).getActiveCallChannel());
 				break;
 			}			
 		}								
 	}
 
 					
 	// Users Register and Unregister
 	public void peerStatusEvent(String evntContent) {
 				
 		String[] list = evntContent.replace(',','\n').split("\\n");
 		boolean canRead= false;
 		VoipAccount user = new VoipAccount();
 		
 		for(int i=0 ; i<list.length; i++){
 			if(list[i].contains("peer=")){
 				String[] number=contentValue(list[i]).split("/");
 				user.setNumber(number[1]);
 				canRead = true;
 			}else{ 
 				if(canRead==true){
 					if(list[i].contains("channeltype"))
 						user.setType(contentValue(list[i]));
 					
 					if(list[i].contains("peerstatus")){
 						if(contentValue(list[i]).equals("Registered")){			
 							user.setStatus("Online");
 							break;
 						}
 						if(contentValue(list[i]).equals("Unregistered")){
 							user.setStatus("Offline");
 							break;
 						}else{
 							user.setStatus("Unknown");
 							break;
 						}
 					}
 				}
 			}
 							
 		}
 	}
 	
 	
 }
