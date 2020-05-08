 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.hubiquitus.hubotsdk;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.camel.impl.DefaultCamelContext;
 import org.apache.camel.impl.JndiRegistry;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.client.HMessageDelegate;
 import org.hubiquitus.hapi.client.HStatusDelegate;
 import org.hubiquitus.hapi.exceptions.MissingAttrException;
 import org.hubiquitus.hapi.hStructures.ConnectionStatus;
 import org.hubiquitus.hapi.hStructures.HAckValue;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HMessageOptions;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.hStructures.ResultStatus;
 import org.hubiquitus.hubotsdk.adapters.HChannelAdapterInbox;
 import org.hubiquitus.hubotsdk.adapters.HubotAdapterInbox;
 import org.hubiquitus.hubotsdk.adapters.HubotAdapterOutbox;
 import org.hubiquitus.hubotsdk.topology.HAdapterConf;
 import org.hubiquitus.hubotsdk.topology.HTopology;
 import org.hubiquitus.util.HubotStatus;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class Hubot {
 
 	final Logger logger = LoggerFactory.getLogger(Hubot.class);
 	
 	private static final String HUBOT_ADAPTER_OUTBOX = "hubotAdapterOutbox";
 	private static final String HUBOT_ADAPTER_INBOX = "hubotAdapterInbox";
 	
 	
 	private Hubot outerclass = this;
 	private HubotStatus status;
 	
 	private HClient hClient = new HClient();
 	private DefaultCamelContext camelContext = null;
 	private ArrayList<String> adapterOutboxActors = new ArrayList<String>();
 	private Map<String, Adapter> adapterInstances = new HashMap<String, Adapter>();
 	private HubotDispatcher hubotDispatcher;
 
 	
 	private HTopology topology;
 	
 	private MessagesDelegate messageDelegate = new MessagesDelegate();
 	private StatusDelegate statusDelegate = new StatusDelegate();
 	
 	
 	 public static String fileToString(String file) {
 	        String result = null;
 	        DataInputStream in = null;
 
 	        try {
 	            File f = new File(file);
 	            byte[] buffer = new byte[(int) f.length()];
 	            in = new DataInputStream(new FileInputStream(f));
 	            in.readFully(buffer);
 	            result = new String(buffer);
 	        } catch (IOException e) {
 	            throw new RuntimeException("IO problem in fileToString", e);
 	        } finally {
 	            try {
 	                in.close();
 	            } catch (IOException e) { /* ignore it */
 	            }
 	        }
 	        return result;
 	    }
 	
 	/**
 	 * Connect the Hubot to the hAPI with params set in the file "config.txt"
 	 */
 	protected final void start() {
 		try {
 			// Create a default context for Camel
 			camelContext = new DefaultCamelContext();
 			ProducerTemplateSingleton.setContext(camelContext);
 			URL configFilePath = ClassLoader.getSystemResource("config.txt");
 			String jsonString = fileToString(configFilePath.getFile());
 			topology = new HTopology(jsonString);
 			setStatus(HubotStatus.CREATED);
 			//Connecting to HNode
 			HOptions options = new HOptions();
 			options.setTransport("socketio");
 			JSONArray endpoints = new JSONArray();
 			endpoints.put(topology.getHserver());
 			options.setEndpoints(endpoints);
 			hClient.onStatus(statusDelegate);
 			hClient.connect(topology.getActor(), topology.getPwd() , options);
 			// see onStatus to know how this actor go the started status
 		} catch (Exception e) {
 			logger.error("Oooops a big error on starting this bot : ", e);
 		}
 	}
 	
 	/**
 	 * An Hubot may override this method in order to perform its own initializations 
 	 * At the end, the hubot must call the <i>initialized()</i> method to create and start the adapters
 	 * and routes.
 	 * @param hClient
 	 */
 	protected void init(HClient hClient) {
 		initialized();
 	}
 	
 	/**
 	 * Create the hubotAdapter and all adapters declared in the file. Call the <i>startAdapters</i> for 
 	 * start all adapters
 	 * Set the status status READY when its over
 	 */
 	protected final void initialized() {
 		setStatus(HubotStatus.INITIALIZED);
 
 		//Create HubotAdapter (Mandatory)
 		createHubotAdapter();
 
 		//Create other adapters
 		createAdapters();
 		createRoutes();
 		startAdapters();
 		createDispatcher();
 		setStatus(HubotStatus.READY);
 	}
 	
 	private void createRoutes() {
 		
 		RouteGenerator routes = new RouteGenerator(adapterOutboxActors);
 		try {
 			camelContext.setRegistry(createRegistry());
 			camelContext.addRoutes(routes);
 		} catch (Exception e) {
 			logger.error(e.toString());
 		}
 		
 	}
 
 	private void createHubotAdapter() {
 		HubotAdapterInbox hubotAdapterInbox = new HubotAdapterInbox();
 		HubotAdapterOutbox hubotAdapterOutbox = new HubotAdapterOutbox();
 		hubotAdapterInbox.setHClient(hClient);
 		hubotAdapterInbox.setCamelContext(camelContext);
 		hubotAdapterOutbox.setHClient(hClient);
 		hubotAdapterOutbox.setCamelContext(camelContext);
 		
 		
 		hubotAdapterInbox.setActor(HUBOT_ADAPTER_INBOX);
 		hubotAdapterOutbox.setActor(HUBOT_ADAPTER_OUTBOX);
 		hubotAdapterInbox.setProperties(topology);
 		hubotAdapterOutbox.setProperties(topology);
 		
 		//Launch the HubotAdapter and put him in adapterInstances 
 		hubotAdapterInbox.start();
 		hubotAdapterOutbox.start();
 		
 		adapterInstances.put(HUBOT_ADAPTER_OUTBOX,hubotAdapterOutbox); 
 		adapterInstances.put(HUBOT_ADAPTER_INBOX,hubotAdapterInbox); 
 		
 	}
 
 	//Created other Adapters
 	@SuppressWarnings("unchecked")
 	private void createAdapters() {
 		JSONArray adapters = topology.getAdapters();
 		// Create instance of all Adapter
 		if(adapters != null) {
 			try {
 				for(int i=0; i< adapters.length(); i++) {
 					HAdapterConf adapterConf = new HAdapterConf(adapters.getJSONObject(i));
 					if(adapterConf != null){
 						if (adapterConf.getType() == null) { //ChannelAdapterInbox
 							HChannelAdapterInbox channelAdapterInbox = new HChannelAdapterInbox();
 							channelAdapterInbox.setActor(adapterConf.getActor());
 							channelAdapterInbox.setProperties(adapterConf.getProperties());
 							channelAdapterInbox.setHClient(hClient);
 							channelAdapterInbox.setCamelContext(camelContext);
 							adapterInstances.put(channelAdapterInbox.getActor(), channelAdapterInbox);
 						} else {
 							String nameAdapter = adapterConf.getType();
 							Class<Object> fc;
 							fc = (Class<Object>) Class.forName(nameAdapter);
 							Adapter newAdapter = (Adapter) fc.newInstance();
 							newAdapter.setActor(adapterConf.getActor());
 							newAdapter.setProperties(adapterConf.getProperties());
 							newAdapter.setHClient(hClient);
 							newAdapter.setCamelContext(camelContext);
 							adapterInstances.put(newAdapter.getActor(), newAdapter);
 							if(newAdapter instanceof AdapterOutbox){
 								adapterOutboxActors.add(newAdapter.getActor());
 							}
 						}
 					}
 				} 
 			}catch (Exception e) {
 				logger.error(e.toString());
 			}
 		}
 	}
 	
 	private void startAdapters() {
 		if(adapterInstances != null) {
 			for(String key : adapterInstances.keySet()) {
 				adapterInstances.get(key).start();
 			}   
 		}
 		try {
 			camelContext.start();
 		} catch (Exception e) {
 			logger.error(e.toString());
 		}
 	}
 	
 	private void createDispatcher(){
 		hubotDispatcher = new HubotDispatcher(adapterOutboxActors);
 	}
 	
 	/**
 	 * Method used by camel. See Camel documentation for more information
 	 * @return
 	 * @throws Exception
 	 */
 	private JndiRegistry createRegistry() throws Exception {
 		JndiRegistry jndi = new JndiRegistry();
 
 		jndi.bind("actor", messageDelegate);
 		jndi.bind(HUBOT_ADAPTER_OUTBOX, adapterInstances.get(HUBOT_ADAPTER_OUTBOX));
 
 		
 		if(adapterOutboxActors != null) {
 			for(String key : adapterOutboxActors) {
 				jndi.bind(key, adapterInstances.get(key));
 			}   
 		}
 		return jndi;
 	}
 	
 	protected final void stop() {
 		for(String key : adapterInstances.keySet()) {
 		  adapterInstances.get(key).stop();
 	  	}
 	  	try {
 		  camelContext.stop();
 	  	} catch (Exception e) {
 		  	logger.error(e.toString());
 		}
 	}
 		
 	protected class MessagesDelegate {
 		/* Method use for incoming message/command */
 		public final void inProcess(Object obj) {
 			if (obj != null) {
 				if (obj instanceof HubotStructure) {
 				 	HubotStructure hubotStruct = (HubotStructure)obj; 
 				 	HMessage message = hubotStruct.getMessage();
 				 	HMessageDelegate callback = hubotStruct.getCallback();
 				 	if(callback != null){
 				 		callback.onMessage(message);
 				 	} else
 					    inProcessMessage(message);
 				}
 			}
 		}
 	}
 	
 	protected abstract void inProcessMessage(HMessage incomingMessage);
 
 
     /**
      * Send an HMessage to a specified adapter outbox.
      * @param hmessage
      */
     protected final void send(HMessage hmessage){
         hubotDispatcher.dispatcher(hmessage, null);
     }
 
 	/**
 	 * Send an HMessage to another actor.
 	 * @param hmessage a hmessage to send to a dedicated actor
      * @param callback the callback to call if an answer is sent by the actor called. Note, you must set a timer value
      * on the hMessage. if not the callback will be ingored
 	 */
 	protected final void send(HMessage hmessage, HMessageDelegate callback){
 		hubotDispatcher.dispatcher(hmessage, callback);
 	}
 
     /**
      * Send an HMessage to another actor.
      * @param hmessage a hmessage to send to a dedicated actor
      * @param callback the callback to call if an answer is sent by the actor called. Note, you must set a timer value
      * on the hMessage. if not the callback will be ingored
      * @param timeout the timeout value to use. (ms)
      */
     protected final void send(HMessage hmessage, HMessageDelegate callback, long timeout){
         hmessage.setTimeout(timeout);
         hubotDispatcher.dispatcher(hmessage, callback);
     }
	
 
 	/**
 	 * Check if the hClient is connect and if connected, set the status to STARTED 
 	 * and launch the init method
 	 */
 	private class StatusDelegate implements HStatusDelegate {
 		/* Method use for incoming message/command */
 		public final void onStatus(HStatus status) {
 			if(status.getStatus() == ConnectionStatus.CONNECTED && outerclass.status == HubotStatus.CREATED) {
 				setStatus(HubotStatus.STARTED);
 				init(hClient);			
 			}
             logger.info("Hubiquitus connection : "+status);
 		}		
 	}
 
 	private void setStatus(HubotStatus status) {
 		this.status = status;
 		logger.info((topology.getType()+"("+topology.getActor()+") : "+status));
 	}
 	
 	/**
 	 * Update a specified inboxAdapter's properties
	 * @param adapterName : adapterName
 	 * @param properties : params - params for update properties
 	 */
	protected void updateInboxAdapterProperties(String adapterName, JSONObject properties) {
		Adapter updatedAdapter = adapterInstances.get(adapterName + "Inbox");
 		updatedAdapter.updateProperties(properties);
 	}
 	
 	/**
 	 * Update a specified outboxAdapter's properties
	 * @param adapterName : adapterName
 	 * @param properties : params - params for update properties
 	 */
	protected void updateOutboxAdapterProperties(String adapterName, JSONObject properties) {
		Adapter updatedAdapter = adapterInstances.get(adapterName + "Outbox");
 		updatedAdapter.updateProperties(properties);
 	}
 	
 	
 	/**
 	 * Retrived the instance of a specified AdapterInbox
 	 * You can use this method to modified some properties of this adapter
 	 * @param actor
 	 * @return the instance of this adapter
 	 */
 	protected final AdapterInbox getAdapterInbox(String actor) {
 		return (AdapterInbox) adapterInstances.get(actor);
 	}
 
 	/**
 	 * Retrived the instance of a specified AdapterOutbox
 	 * You can use this method to modified some properties of this adapter
 	 * @param actor
 	 * @return the instance of this adapter
 	 */
 	protected final AdapterOutbox getAdapterOutbox(String actor) {
 		return (AdapterOutbox) adapterInstances.get(actor);
 	}
 	
 	
 	/**
 	 * Add a adapterInbox
 	 * Can be used before starting the bot
 	 * You can use this method to modified some properties of this adapter
 	 * @param adapterInbox
 	 * @return
 	 */
 	private final void addAdapterInbox(AdapterInbox adapterInbox) {
 		adapterInstances.put(adapterInbox.getActor(), adapterInbox);
 	}
 	
 	/**
 	 * Retrived the properties of this Bot
 	 * @return a JSONObject for the properties set in the topology of the hubot (could be null)
 	 */
 	protected final JSONObject getProperties() {
 		return topology.getProperties();
 	}
 	
 	/**
 	 * Helper to create a hMessage. Payload type could be instance of JSONObject(HAlert, HAck, HCommand ...), JSONObject, JSONArray, String, Boolean, Number
 	 * @param actor : The Hubot for the hMessage. Mandatory.
 	 * @param type : The type of the hMessage. Not mandatory.
 	 * @param payload : The payload for the hMessage. Not mandatory.
 	 * @param options : The options if any to use for the creation of the hMessage. Not mandatory.
      * @return a hMessage which can be used with the send method
 	 */
 	public HMessage buildMessage(String actor, String type, Object payload, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildMessage(actor, type, payload, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
 	
 	/**
 	 * Helper to create a hMessage with a hConvState payload.
 	 * @param actor : The channel id for the hMessage. Mandatory
 	 * @param convid : The convid where the status have to be updated. Mandatory
 	 * @param status : Status of the conversation. Mandatory.
 	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
 	 * @return A hMessage with a hConvState payload.
 	 */
 	public HMessage buildConvState(String actor, String convid, String status, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildConvState(actor, convid, status, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
 	
 	/**
 	 * Helper to create a hMessage wiht a hAck payload.
 	 * @param actor : The actor for the hMessage.  Mandatory.
 	 * @param ref : The msgid to acknowledged. Mandatory.
 	 * @param ack : The following values are authorized : 
 	 * (1). “recv” : means that the message has been received by the participant (on at least one of its devices). 
 	 * (2). “read” : means that the message has been read by the participant.
 	 * Mandatory.
 	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
 	 * @return A hMessage with a hAck payload.
 	 */
 	public HMessage buildAck(String actor, String ref, HAckValue ack, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildAck(actor, ref, ack, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
 	
 	/**
 	 * Helper to create a hMessage with a hAlert payload.
 	 * @param actor : The channel id for the hMessage. Mandatory.
 	 * @param alert : The alert message. Mandatory.
 	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
 	 * @return A hMessage with a hAlert payload.
 	 */
 	public HMessage buildAlert(String actor, String alert, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildAlert(actor, alert, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
 
 	/**
 	 * Helper to create a hMessage with a hMeasure payload.
 	 * @param actor : The actor for the hMessage. Mandatory
 	 * @param value : The value of the measure. Mandatory
 	 * @param unit : The unit of the measure. Mandatory
 	 * @param options : The options to use if any for the creation of the hMessage. Not Mandatory.
 	 * @return A hMessage with a hMeasure payload. 
 	 */
 	public HMessage buildMeasure(String actor, String value, String unit, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildMeasure(actor, value, unit, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
 	
 	/**
 	 * Helper to create a hMessage with a hCommand payload.
 	 * @param actor : The actor for the hMessage. Mandatory.
 	 * @param cmd : The name of the command. Mandatory.
 	 * @param params : Parameters of the command. Not mandatory.
 	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
 	 * @return A hMessage with a hCommand payload.
 	 */
 	public HMessage buildCommand(String actor, String cmd, JSONObject params, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildCommand(actor, cmd, params, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
 	
 	/**
 	 * Helper to create a hMessage with a hResult payload.
 	 * @param actor : The actor for the hMessage. Mandatory.
 	 * @param ref : The id of the message received, for correlation purpose. Mandatory.
 	 * @param status : Result status code. Mandatory.
 	 * @param result : The result String of a command.
 	 * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
 	 * @return A hMessage with a hResult payload.
 	 */
 	public HMessage buildResult(String actor, String ref, ResultStatus status, String result, HMessageOptions options){
 		HMessage message = null;
 		try {
 			message = hClient.buildResult(actor, ref, status, result, options);
 		} catch (MissingAttrException e) {
 			logger.debug(e.toString());
 		}
 		return message;
 	}
     /**
      * Helper to create a hMessage with a hResult payload.
      * @param actor : The actor for the hMessage. Mandatory.
      * @param ref : The id of the message received, for correlation purpose. Mandatory.
      * @param status : Result status code. Mandatory.
      * @param result : The result boolean of a command.
      * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
      * @return A hMessage with a hResult payload.
      */
     public HMessage buildResult(String actor, String ref, ResultStatus status, boolean result, HMessageOptions options){
         HMessage message = null;
         try {
             message = hClient.buildResult(actor, ref, status, result, options);
         } catch (MissingAttrException e) {
             logger.debug(e.toString());
         }
         return message;
     }
     /**
      * Helper to create a hMessage with a hResult payload.
      * @param actor : The actor for the hMessage. Mandatory.
      * @param ref : The id of the message received, for correlation purpose. Mandatory.
      * @param status : Result status code. Mandatory.
      * @param result : The result double of a command.
      * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
      * @return A hMessage with a hResult payload.
      */
     public HMessage buildResult(String actor, String ref, ResultStatus status, double result, HMessageOptions options){
         HMessage message = null;
         try {
             message = hClient.buildResult(actor, ref, status, result, options);
         } catch (MissingAttrException e) {
             logger.debug(e.toString());
         }
         return message;
     }
     /**
      * Helper to create a hMessage with a hResult payload.
      * @param actor : The actor for the hMessage. Mandatory.
      * @param ref : The id of the message received, for correlation purpose. Mandatory.
      * @param status : Result status code. Mandatory.
      * @param result : The result JSONArray of a command.
      * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
      * @return A hMessage with a hResult payload.
      */
     public HMessage buildResult(String actor, String ref, ResultStatus status, JSONArray result, HMessageOptions options){
         HMessage message = null;
         try {
             message = hClient.buildResult(actor, ref, status, result, options);
         } catch (MissingAttrException e) {
             logger.debug(e.toString());
         }
         return message;
     }
     /**
      * Helper to create a hMessage with a hResult payload.
      * @param actor : The actor for the hMessage. Mandatory.
      * @param ref : The id of the message received, for correlation purpose. Mandatory.
      * @param status : Result status code. Mandatory.
      * @param result : The result JSONObject of a command.
      * @param options : The options to use if any for the creation of the hMessage. Not mandatory.
      * @return A hMessage with a hResult payload.
      */
     public HMessage buildResult(String actor, String ref, ResultStatus status, JSONObject result, HMessageOptions options){
         HMessage message = null;
         try {
             message = hClient.buildResult(actor, ref, status, result, options);
         } catch (MissingAttrException e) {
             logger.debug(e.toString());
         }
         return message;
     }
 }
