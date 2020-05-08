 package n3phele.service.actions;
 /**
 * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
 * except in compliance with the License. 
 * 
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 *  specific language governing permissions and limitations under the License.
 */
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import n3phele.service.core.Resource;
 import n3phele.service.core.UnprocessableEntityException;
 import n3phele.service.lifecycle.ProcessLifecycle;
 import n3phele.service.model.Action;
 import n3phele.service.model.Command;
 import n3phele.service.model.FileTracker;
 import n3phele.service.model.ParameterType;
 import n3phele.service.model.SignalKind;
 import n3phele.service.model.TypedParameter;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.VirtualServer;
 import n3phele.service.rest.impl.ActionResource;
 
 import com.googlecode.objectify.annotation.Cache;
 import com.googlecode.objectify.annotation.EntitySubclass;
 import com.googlecode.objectify.annotation.Ignore;
 import com.googlecode.objectify.annotation.OnLoad;
 import com.googlecode.objectify.annotation.OnSave;
 import com.googlecode.objectify.annotation.Unindex;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.ClientFilter;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 @EntitySubclass
 @XmlRootElement(name = "VMAction")
 @XmlType(name = "VMAction", propOrder = { "epoch", "fileTable" })
 @Unindex
 @Cache
 public class VMAction extends Action {
     private ActionLogger logger;
 	final protected static java.util.logging.Logger log = java.util.logging.Logger.getLogger(VMAction.class.getName());
 	@XmlTransient
 	private ArrayList<FileTracker> fileTableList = new ArrayList<FileTracker>();
 	@Ignore Map<String,FileTracker> fileTable = new HashMap<String, FileTracker>();
 	@OnLoad void makeFileMap() {  
 		for(FileTracker v : this.fileTableList) { 
 			if(v != null)
 				this.fileTable.put(v.getName(), v); 
 		} 
 	}
 	@OnSave void saveFileMap() { 
 		this.fileTableList.clear(); if(this.fileTable.size() != 0) this.fileTableList.addAll(fileTable.values()); 
 	}
 	
 	/* (non-Javadoc)
 	 * @see n3phele.service.model.Action#getDescription()
 	 */
 	@Override
 	public String getDescription() {
 		return "Virtual Machine "+this.getName();
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see n3phele.service.model.Action#getPrototype()
 	 */
 	@Override
 	public Command getPrototype() {
 		Command command = new Command();
 		command.setUri(UriBuilder.fromUri(ActionResource.dao.path).path("history").path(this.getClass().getSimpleName()).build());
 		command.setName("VM");
 		command.setOwner(this.getOwner());
 		command.setOwnerName(this.getOwner().toString());
 		command.setPublic(false);
 		command.setDescription("Virtual Machine");
 		command.setPreferred(true);
 		command.setVersion("1");
 		command.setIcon(URI.create("https://www.n3phele.com/icons/vm"));
 		List<TypedParameter> myParameters = new ArrayList<TypedParameter>();
 		command.setExecutionParameters(myParameters);
 		
 		myParameters.add(new TypedParameter("vmFactory", "factory service URI of VM", ParameterType.String, "", this.context.getValue("vmFactory")));
 		myParameters.add(new TypedParameter("agentURI", "VM agent URI", ParameterType.String, "", ""));
 		for(TypedParameter param : command.getExecutionParameters()) {
 			param.setDefaultValue(this.context.getValue(param.getName()));
 		}
 		return command;
 	}
 	
 	
 	
 	
 	
 	private long epoch;
 	@Override
 	public void init() throws Exception {
 		logger = new ActionLogger(this);
 		this.epoch = new Date().getTime();
 	}
 	
 	
 	@Override
 	public boolean call() throws n3phele.service.lifecycle.ProcessLifecycle.WaitForSignalRequest, Exception {
 		if(epoch != 0) {
 			/*
 			 * Waiting for the VM to initally start up
 			 */
 			long timeout = Long.valueOf(Resource.get("vmProvisioningTimeoutInSeconds", "900"))*1000;
 			if(epoch+timeout < Calendar.getInstance().getTimeInMillis()) {
 				logger.error("vm creation FAILED due to timeout");
 				log.log(Level.SEVERE, "vm creation FAILED due to timeout");
 				killVM();
 				throw new UnprocessableEntityException("vm creation FAILED due to timeout");
 			}
 				
 			Client client = ClientFactory.create();
 
 			String clientURI = this.context.getValue("vmFactory");
 			try {
 				if(!this.context.containsKey("agentURI")) {	
 					
 					VirtualServer vs = getVirtualServer(client, this.context.getValue("vmFactory"));
					log.info("VMAction: Server status is "+vs.getStatus());
 					URI notification = UriBuilder.fromUri(this.getProcess()).scheme("http").path("event").build();
 					if(!notification.equals(vs.getNotification())) {
 						// FIXME: Update factory to accept notification put .. resource.put(notification);
 					}
 					if(vs.getStatus().equalsIgnoreCase("Running") && vs.getOutputParameters() != null) {
 						for(NameValue p : vs.getOutputParameters()) {
 							if(p.getKey().equals("publicIpAddress")) {
 								this.context.putValue("publicIpAddress", p.getValue());
								log.info("VMAction: publicURI is "+p.getValue());
 								URI agentURI = URI.create(String.format(Resource.get("agentPattern", "http://%s:8887/task"),p.getValue()));
 								this.context.putValue("agentURI", agentURI);
 								log.info("agentURI is "+agentURI);
 							} else {
 								this.context.putValue(p.getKey(), p.getValue());
 							}
 						}
 					} else if(vs.getStatus().equalsIgnoreCase("Terminated")) {
 						logger.error("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 					} 
 					if(!this.context.containsKey("agentURI"))
 							return false; // keep waiting
 				}
 				
 				/*
 				 * The VM is starting to run. Try to contact the agent, if a restart is requested....
 				 */
 				
 	
 				String agentURI = this.context.getValue("agentURI");
 				clientURI = agentURI;
 				if(this.context.getBooleanValue("forceAgentRestart")) {
 					try {
 						int status = forceAgentRestart(client, agentURI);
 						if(status == 200) {
 							this.context.putValue("forceAgentRestart", false);
 							Thread.sleep(3000);
 						} else {
 							log.info("Agent restart "+status);
 						}
 					} catch (Exception e) {
 						log.log(Level.SEVERE, "Agent restart exception", e);
 					}
 				}
 				aliveTest(client, agentURI);
 				long duration = (Calendar.getInstance().getTimeInMillis() - epoch)/1000;
 				if(duration == 0)
 					duration = 1;
 				epoch = 0;
 				log.info(this.name+" vm created successfully. Elapsed time "+duration+" seconds.");
 				logger.info("vm created completed successfully. Elapsed time "+duration+" seconds.");
 				ProcessLifecycle.mgr().signalParent(this.getProcess(), SignalKind.Event, this.getProcess().toString());
 				throw new ProcessLifecycle.WaitForSignalRequest();
 					
 			} catch (UniformInterfaceException e) {
 				ClientResponse response = e.getResponse();
 				if(response != null) {
 					if(response.getStatus() == 404) {
 						logger.error(this.context.getValue("vmFactory")+" unexpected death.");
 						log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 					}
 				}
 				logger.info("Awaiting host "+URI.create(clientURI).getHost()+" initialization");
 				log.info("Waiting for agent "+URI.create(clientURI).getHost()+" "+e.getMessage());
 			} catch (ClientHandlerException e) {
 				logger.info("Awaiting host "+URI.create(clientURI).getHost()+" initialization");
 				log.info("Waiting for agent "+URI.create(clientURI).getHost()+" "+e.getMessage());
 			} catch (ProcessLifecycle.WaitForSignalRequest wait) {
 				throw wait;
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Unexpected exception", e);
 			} finally {
 				ClientFactory.give(client);
 			}
 			return false; // keep waiting
 		} else {
 			/*
 			 * On going monitoring of the VM
 			 */
 			Client client = ClientFactory.create();
 			VirtualServer vs = null;
 			try {	
 				vs = getVirtualServer(client, this.context.getValue("vmFactory"));
 			} catch (UniformInterfaceException e) {
 				ClientResponse response = e.getResponse();
 				if(response != null) {
 					if(response.getStatus() == 404) {
 						logger.error(this.context.getValue("vmFactory")+" unexpected death.");
 						log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 						throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 					}
 				}
 
 				logger.info("Access error to "+this.context.getValue("vmFactory"));
 				log.log(Level.INFO, "Access error to "+this.context.getValue("vmFactory"), e);
 				throw new ProcessLifecycle.WaitForSignalRequest();
 			} catch (ClientHandlerException e) {
 				logger.info("Access error to "+this.context.getValue("vmFactory"));
 				log.log(Level.INFO, "Access error to "+this.context.getValue("vmFactory"), e);
 			} catch (Exception e) {
 				log.log(Level.SEVERE, "Unexpected exception", e);
 			} finally {
 				ClientFactory.give(client);
 			}
 			
 			log.info("VM "+this.context.getValue("vmFactory")+" "+(vs==null?"disappeared":vs.getName()+":"+vs.getStatus()));
 			if(vs != null && vs.getStatus().equalsIgnoreCase("Running") && vs.getOutputParameters() != null) {
 				/*
 				 * All is well
 				 */
 			} else if(vs == null || vs.getStatus().equalsIgnoreCase("Terminated")) {
 				/*
 				 * The VM has died
 				 */
 				logger.error(this.context.getValue("vmFactory")+" unexpected death.");
 				log.severe("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 				throw new UnprocessableEntityException("Client "+this.context.getValue("vmFactory")+" unexpected death.");
 			} 
 					
 			throw new ProcessLifecycle.WaitForSignalRequest();
 		}
 	}
 
 	@Override
 	public void cancel() {
 		killVM();
 
 	}
 
 	@Override
 	public void dump() {
 		killVM();
 
 	}
 
 	@Override
 	public void signal(SignalKind kind, String assertion) {
 		log.info("Signal "+kind+":"+assertion);
 	}
 
 
 	public void killVM() {
 		Client client = ClientFactory.create();
 		ClientFilter factoryAuth = new HTTPBasicAuthFilter(this.context.getValue("factoryUser"), 
 				this.context.getValue("factorySecret"));
 		client.setReadTimeout(90000);
 		client.setConnectTimeout(5000);
 		client.addFilter(factoryAuth);
 		try {
 			boolean debug = Resource.get("suppressDeleteVM", false);
 			int status = terminate(client, this.context.getValue("vmFactory"), true, debug);
 			log.info("Delete status is "+status);
 			if(debug) {
 				logger.warning("Attempting to create a debug VM to allow error inspection. Debug instance will be removed aproximately 55 minutes after inital creation.");
 			}
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "Exception terminating VM", e);
 			logger.error("Exception terminating VM "+e.getMessage());
 		} finally {
 			ClientFactory.give(client);
 		}
 	}
 
 
 	/**
 	 * @return the fileTable
 	 */
 	public Map<String, FileTracker> getFileTable() {
 		return fileTable;
 	}
 
 
 	/**
 	 * @param fileTable the fileTable to set
 	 */
 	public void setFileTable(Map<String, FileTracker> fileTable) {
 		this.fileTable.clear();
 		this.fileTable.putAll(fileTable);
 	}
 	
 	/*
 	 * Unit Testing
 	 * ============
 	 */
 	
 	protected int terminate(Client client, String factory, boolean error, boolean debug) {
 		WebResource resource = client.resource(factory);
 		ClientResponse response = resource.queryParam("error", Boolean.toString(error)).queryParam("debug", Boolean.toString(debug)).delete(ClientResponse.class);
 		return response.getStatus();
 	}
 	
 	protected int forceAgentRestart(Client client, String agentURI) {
 		client.removeAllFilters();
 		ClientFilter agentAuth = new HTTPBasicAuthFilter(this.context.getValue("agentUser"), 
 				this.context.getValue("agentSecret"));
 		client.setReadTimeout(25000);
 		client.setConnectTimeout(5000);
 		client.addFilter(agentAuth);
 		WebResource resource = client.resource(agentURI);
 		ClientResponse response = resource.path("terminate").get(ClientResponse.class);
 		return response.getStatus();
 	}
 	
 	protected void aliveTest(Client client, String agentURI) {
 		client.removeAllFilters();
 		ClientFilter agentAuth = new HTTPBasicAuthFilter(this.context.getValue("agentUser"), 
 				this.context.getValue("agentSecret"));
 		client.setReadTimeout(25000);
 		client.setConnectTimeout(5000);
 		client.addFilter(agentAuth);
 		WebResource resource = client.resource(agentURI);
 		resource.path("date").get(String.class);
 	}
 	
 	protected VirtualServer getVirtualServer(Client client, String uri) {
 		client.removeAllFilters();
 		ClientFilter factoryAuth = new HTTPBasicAuthFilter(this.context.getValue("factoryUser"), 
 				this.context.getValue("factorySecret"));
 		client.setReadTimeout(25000);
 		client.setConnectTimeout(5000);
 		client.addFilter(factoryAuth);
 		WebResource resource = client.resource(uri);
 		VirtualServer vs = resource.get(VirtualServer.class);
 		return vs;
 	}
 
 	/**
 	 * @return the epoch
 	 */
 	public long getEpoch() {
 		return epoch;
 	}
 
 
 	/**
 	 * @param epoch the epoch to set
 	 */
 	public void setEpoch(long epoch) {
 		this.epoch = epoch;
 	}
 
 
 	/**
 	 * @param fileTable the fileTable to set
 	 */
 	public void setFileTable(HashMap<String, FileTracker> fileTable) {
 		this.fileTable = fileTable;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return String
 				.format("VMAction [fileTable=%s, epoch=%s, %s]",
 						fileTable, epoch, super.toString());
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + (int) (epoch ^ (epoch >>> 32));
 		result = prime * result
 				+ ((fileTable == null) ? 0 : fileTable.hashCode());
 		result = prime * result + ((logger == null) ? 0 : logger.hashCode());
 		return result;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (!super.equals(obj))
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		VMAction other = (VMAction) obj;
 		if (epoch != other.epoch)
 			return false;
 		if (fileTable == null) {
 			if (other.fileTable != null)
 				return false;
 		} else if (!fileTable.equals(other.fileTable))
 			return false;
 		if (logger == null) {
 			if (other.logger != null)
 				return false;
 		} else if (!logger.equals(other.logger))
 			return false;
 		return true;
 	}
 	
 }
