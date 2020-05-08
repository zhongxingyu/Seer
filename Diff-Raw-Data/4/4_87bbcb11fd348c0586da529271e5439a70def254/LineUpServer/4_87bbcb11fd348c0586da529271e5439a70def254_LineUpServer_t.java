 /**
  *
  * lineup - In-Memory high-throughput queue
  * Copyright (c) 2013, Sandeep Gupta
  * 
  * http://www.sangupta/projects/lineup
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * 		http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 
 package com.sangupta.lineup.server;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.sangupta.jerry.util.AssertUtils;
 import com.sun.grizzly.http.SelectorThread;
 import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
 
 /**
  * A Grizzly based webserver that can accept REST based incoming
  * connections for operations on message queues.
  * 
  * @author sangupta
  *
  */
 public class LineUpServer {
 
 	/**
 	 * Default webservices packages to load  
 	 */
 	private static final String DEFAULT_WEBSERVICES_PACKAGES = "com.sangupta.lineup com.sangupta.jerry.jersey";
 
 	/**
 	 * The server URL where we will hook up.
 	 */
 	private final String serverURL;
 	
 	/**
 	 * Initialization parameters for grizzly container
 	 */
 	private final Map<String, String> initParams;
 	
 	/**
 	 * Keeps track of whether the server is running or not.
 	 * 
 	 */
 	private volatile boolean started = false;
 	
 	/**
 	 * The thread selector obtained from Grizzly container
 	 */
     private SelectorThread threadSelector = null;
     
     /**
      * Create a new {@link LineUpServer} instance with default
      * webservices.
      * 
      * @param serverURL
      */
 	public LineUpServer(String serverURL) {
 		this(serverURL, null);
 	}
 	
 	/**
 	 * Create a new {@link LineUpServer} instance also loading
 	 * custom webservices from the package provided. The webservices
 	 * must be Jersey-enabled to work properly.
 	 * 
 	 * @param serverURL
 	 * @param customJerseyWebservices
 	 */
 	public LineUpServer(final String serverURL, final String[] customJerseyWebservices) {
 		if(AssertUtils.isEmpty(serverURL)) {
 			throw new IllegalArgumentException("Server URL must be provided, cannot be null/empty");
 		}
 		
 		this.serverURL = serverURL;
 		initParams = new HashMap<String, String>();
 		
 		if(AssertUtils.isEmpty(customJerseyWebservices)) {
 			initParams.put("com.sun.jersey.config.property.packages", DEFAULT_WEBSERVICES_PACKAGES);
 		} else {
 			final StringBuilder packages = new StringBuilder(DEFAULT_WEBSERVICES_PACKAGES);
 			for(String customPackage : customJerseyWebservices) {
 				packages.append(' ');
 				packages.append(customPackage);
 			}
 			
 			initParams.put("com.sun.jersey.config.property.packages", packages.toString());
 		}
 	}
 	
 	/**
 	 * Start the server.
 	 *  
 	 * @throws IOException
 	 *  
 	 * @throws IllegalArgumentException 
 	 * 
 	 * @throws IllegalStateException if the server is already running.
 	 * 
 	 */
 	public void startServer() throws IllegalArgumentException, IOException {
 		if(this.started) {
 			throw new IllegalStateException("Server is already running.");
 		}
 		
         
 		this.threadSelector = GrizzlyWebContainerFactory.create(serverURL, initParams);
 		this.threadSelector.setReuseAddress(false);
 		this.threadSelector.setSocketKeepAlive(false);
 		
 		this.started = true;
 	}
 	
 	/**
 	 * Stop the currently running server.
 	 * 
 	 * @throws IllegalStateException if the server is already stopped.
 	 * 
 	 */
 	public void stopServer() {
 		if(!this.started) {
 			throw new IllegalStateException("Server has not yet started.");
 		}
 		
 		if(this.threadSelector != null) {
 			this.threadSelector.stopEndpoint();
 		}
 		
 		this.started = false;
 		this.threadSelector = null;
 	}
 	
 	/**
 	 * Returns whether the server is running or not.
 	 * 
 	 * @return
 	 */
 	public boolean isRunning() {
 		return this.started;
 	}
 	
 	/**
 	 * Method that will register a shutdown hook so that the server
 	 * can be closed, when the application exits.
 	 * 
 	 */
 	public void registerShutdownHook() {
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			
 			/**
 			 * @see java.lang.Thread#run()
 			 */
 			@Override
 			public void run() {
 				super.run();
 				
				if(started) {
					stopServer();
				}
 			}
 			
 		});
 	}
 
 }
