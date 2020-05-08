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
 import java.io.File; 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 
 import org.apache.camel.impl.DefaultCamelContext;
 import org.apache.camel.impl.JndiRegistry;
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.hStructures.HCommand;
 import org.hubiquitus.hapi.hStructures.HJsonObj;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hubotsdk.adapters.HubotAdapterInbox;
 import org.hubiquitus.hubotsdk.adapters.HubotAdapterOutbox;
 import org.hubiquitus.util.ConfigActor;
 import org.hubiquitus.util.ConfigActor.AdapterConfig;
 
 import com.fasterxml.jackson.databind.ObjectMapper;            
 
 public abstract class Actor {
 
 	protected HClient hClient = new HClient();
 	protected DefaultCamelContext camelContext = null;
 	protected Map<String, Class<Object>> adapterOutClasses = new HashMap<String, Class<Object>>();
 	protected Map<String, Adapter> adapterInstances = new HashMap<String, Adapter>();
 	protected ConfigActor configActor;
 	
 	protected String name; 
 	protected String jid;
 	protected String pwd;
 	protected String endpoint;
 
 	protected void start() {
 		try {
 			// Create a default context for Camel
 			camelContext = new DefaultCamelContext();
 			ProducerTemplateSingleton.setContext(camelContext);
 	
 			// Parsing configuration file with Jackson 
 			ObjectMapper mapper = new ObjectMapper();
			configActor = mapper.readValue(new File("./src/main/resources/config.txt"), ConfigActor.class);
 			
 			this.name = configActor.getName();
 			this.jid = configActor.getJid();
 			this.pwd = configActor.getPwdhash();
 			this.endpoint = configActor.getEndpoint();
 	
 			//Connecting to HNode
 			HOptions options = new HOptions();
 			options.setTransport("socketio");
 			ArrayList<String> endpoints = new ArrayList<String>();
 			endpoints.add(endpoint);
 			options.setEndpoints(endpoints);
 			hClient.connect(jid, pwd , options);
 			
 			
 			//Launch the init()
 			init();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void init() {
 		initialized();
 	}
 	
 	protected void initialized() {
 		//Create HubotAdapter (Mandatory)
 		createHubotAdapter();
 		
 		//Create other adapters
 		adapters();
 		ready();
 	}
 	protected void adapters() {		
 		//Create Adapters
 		createAdapters();
 		
 		//Create routes for Camel
 		RouteGenerator routes = new RouteGenerator(adapterOutClasses);
 		try {
 			camelContext.setRegistry(createRegistry());
 			camelContext.addRoutes(routes);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}	
 	}
 
 	protected void ready() {
 		if(adapterInstances != null) {
 			for(String key : adapterInstances.keySet()) {
 				adapterInstances.get(key).start();
 			}   
 		}
 		try {
 			camelContext.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void createHubotAdapter() {
 		HubotAdapterInbox hubotAdapterInbox = new HubotAdapterInbox("hubotAdapterInbox");
 		HubotAdapterOutbox hubotAdapterOutbox = new HubotAdapterOutbox("hubotAdapterOutbox");
 		hubotAdapterInbox.setHClient(hClient);
 		hubotAdapterInbox.setCamelContext(camelContext);
 		hubotAdapterOutbox.setHClient(hClient);
 		hubotAdapterOutbox.setCamelContext(camelContext);
 	
 		
 		Map<String,String> propertiesMap = new HashMap<String,String>();
 		propertiesMap.put("jid", configActor.getJid());
 		propertiesMap.put("pwdhash", configActor.getPwdhash());
 		propertiesMap.put("endpoint", configActor.getEndpoint());
 		
 		hubotAdapterInbox.setName("hubotAdapterInbox");
 		hubotAdapterOutbox.setName("hubotAdapterOutbox");
 		hubotAdapterInbox.setProperties(propertiesMap);
 		hubotAdapterOutbox.setProperties(propertiesMap);
 		
 		//Launch the HubotAdapter and put him in adapterInstances 
 		hubotAdapterInbox.start();
 		hubotAdapterOutbox.start();
 		
 		adapterInstances.put("hubotAdapterOutbox",hubotAdapterOutbox); 
 		adapterInstances.put("hubotAdapterInbox",hubotAdapterInbox); 
 		
 	}
 
 	//Created other Adapters
 	@SuppressWarnings("unchecked")
 	protected void createAdapters() {
 		ArrayList<AdapterConfig> adapters = configActor.getAdapters();
 		ArrayList<String> outAdaptersName = configActor.getOutboxes();
 		ArrayList<String> inAdaptersName = configActor.getOutboxes();
 
 		// Create instance of all Adapter
 		if(adapters != null) {
 			try {
 				for(int i=0; i< adapters.size(); i++) {
 					if(inAdaptersName != null && inAdaptersName.contains(adapters.get(i).getName())) {
 						Class<Object> fc;
 						fc = (Class<Object>) Class.forName("org.hubiquitus.hubotsdk.adapters." + adapters.get(i).getType() + "Inbox");
 						Adapter newAdapterInbox = (AdapterInbox) fc.newInstance();
 						newAdapterInbox.setName(adapters.get(i).getName() + "Inbox");
 						newAdapterInbox.setProperties(adapters.get(i).getProperties());
 						newAdapterInbox.setHClient(hClient);
 						newAdapterInbox.setCamelContext(camelContext);
 						adapterInstances.put(adapters.get(i).getName() + "Inbox", newAdapterInbox);
 					}
 					if(outAdaptersName != null && outAdaptersName.contains(adapters.get(i).getName())) {
 						Class<Object> fc;
 						fc = (Class<Object>) Class.forName("org.hubiquitus.hubotsdk.adapters." + adapters.get(i).getType() + "Outbox");
 						Adapter newAdapterOutbox = (AdapterOutbox) fc.newInstance();
 						newAdapterOutbox.setName(adapters.get(i).getName() + "Outbox");
 						newAdapterOutbox.setProperties(adapters.get(i).getProperties());
 						newAdapterOutbox.setHClient(hClient);
 						newAdapterOutbox.setCamelContext(camelContext);
 						adapterInstances.put(adapters.get(i).getName() + "Outbox", newAdapterOutbox);
 						adapterOutClasses.put(adapters.get(i).getName() + "Outbox", fc);			
 					}
 				} 
 			}catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private JndiRegistry createRegistry() throws Exception {
 		JndiRegistry jndi = new JndiRegistry();
 
 		jndi.bind("actor", this);
 		jndi.bind("hubotAdapterOutbox", adapterInstances.get("hubotAdapterOutbox"));
 
 		
 		if(adapterOutClasses != null) {
 			for(String key : adapterOutClasses.keySet()) {
 				jndi.bind(key, adapterInstances.get(key));
 			}   
 		}
 		return jndi;
 	}
 
 //	protected final void start() {
 //		try {
 //			initialize();
 //			context.start();
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //	}
 
 	protected final void stop() {
 		for(String key : adapterInstances.keySet()) {
 			adapterInstances.get(key).stop();
 		}  
 		try {
 			camelContext.stop();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* Method use for incoming message/command */
 	protected final void inProcess(Object obj) {
 		if (obj != null) {
 			if (obj instanceof HMessage) {
 				inProcessMessage((HMessage)obj);
 			}
 			if (obj instanceof HCommand) {
 				inProcessCommand((HCommand)obj);
 			}
 		}		
 	}
 
 	protected abstract void inProcessMessage(HMessage messageIncoming);
 	protected abstract void inProcessCommand(HCommand commandIncoming);
 
 	/* Send message to a specified adapter */
 	protected final void put(String adapterName, HJsonObj hjson) {
 		if(hjson.getHType().equalsIgnoreCase("hcommand")) {
 			put(adapterName, new HCommand(hjson.toJSON()));
 		} else if (hjson.getHType().equalsIgnoreCase("hmessage")){
 			put(adapterName, new HMessage(hjson.toJSON()));
 		} else {
 			// TODO faire un gros message NOT SUPPORTED
 		}
 	}
 
 	protected final void put(String adapterName, HMessage msg) {
 		String route = "seda:" + adapterName + "Outbox";
 		ProducerTemplateSingleton.getProducerTemplate().sendBody(route,msg);		
 	}
 
 	protected final void put(String adapterName, HCommand cmd) {
 		String route = "seda:" + adapterName + "Outbox";
 		ProducerTemplateSingleton.getProducerTemplate().sendBody(route,cmd);		
 	}
 
 }
