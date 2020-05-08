 /**
  * @author Nigel Cook
  * @author Alexandre Leites
  * @author Cristina Scheibler
  *
  * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
  
  package n3phele.factory.rest.impl;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.UUID;
 
 import javax.annotation.security.RolesAllowed;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import n3phele.factory.hpcloud.HPCloudCreateServerRequest;
 import n3phele.factory.hpcloud.HPCloudCreateServerResponse;
 import n3phele.factory.hpcloud.HPCloudCredentials;
 import n3phele.factory.hpcloud.HPCloudManager;
 import n3phele.factory.model.ServiceModelDao;
 import n3phele.service.core.NotFoundException;
 import n3phele.service.core.Resource;
 import n3phele.service.model.core.AbstractManager;
 import n3phele.service.model.core.BaseEntity;
 import n3phele.service.model.core.Collection;
 import n3phele.service.model.core.ExecutionFactoryCreateRequest;
 import n3phele.service.model.core.GenericModelDao;
 import n3phele.service.model.core.NameValue;
 import n3phele.service.model.core.ParameterType;
 import n3phele.service.model.core.TypedParameter;
 import n3phele.service.model.core.VirtualServer;
 
 import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
 import org.jclouds.openstack.nova.v2_0.domain.RebootType;
 import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
 import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.apphosting.api.DeadlineExceededException;
 import com.googlecode.objectify.Key;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 
 
 /** EC2 Virtual Server Resource manages the lifecycle of virtual machines on Amazon EC2 (or compatible) clouds.
  * @author Nigel Cook
  * @author Alexandre Leites
  * @author Cristina Scheibler
  */
 @Path("/")
 public class VirtualServerResource {
 	private Client						client	= null;
 	private final VirtualServerManager	manager;
 	
 	private final static String FACTORY_NAME	= "nova-factory";
 	final Logger logger = LoggerFactory.getLogger(VirtualServerResource.class);
 
 	public VirtualServerResource()
 	{
 		manager = new VirtualServerManager();
 	}
 
 	@Context
 	UriInfo uriInfo;
 	
 	@GET
 	@RolesAllowed("authenticated")
 	@Path("dump")
 	public String dump(@QueryParam("id") String id, @QueryParam("key") String key, @DefaultValue("https://ec2.amazonaws.com") @QueryParam("location") String location, @QueryParam("locationId") String locationId)
 	{
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(id, key));
 		int count = hpcManager.getKeyPairsCount(locationId);
 
 		if (count > 1)
 			return String.valueOf(count) + " key pairs";
 		else
 			return null;
 	}
 
 	
 	@POST
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/accountTest")
 	public String accountTest(@DefaultValue("false") @FormParam("fix") Boolean fix, @FormParam("id") String id, @FormParam("secret") String secret, @FormParam("key") String key, @FormParam("location") URI location, @FormParam("locationId") String locationId, @FormParam("email") String email, @FormParam("firstName") String firstName, @FormParam("lastName") String lastName, @FormParam("securityGroup") String securityGroup)
 	{
 		logger.info("accountTest with fix " + fix);
 		if (fix && (email == null || email.trim().length() == 0) || (firstName == null || firstName.trim().length() == 0) || (lastName == null || lastName.trim().length() == 0))
 			throw new IllegalArgumentException("email details must be supplied with option to fix");
 		
 		boolean resultKey = checkKey(key, id, secret, location, locationId);
 		if (!resultKey && fix)
 			resultKey = createKey(key, id, secret, location, email, firstName, lastName, locationId);
 		
 		boolean result = checkSecurityGroup(securityGroup, id, secret, location, locationId);
 		if (!result && fix)
 			result = makeSecurityGroup(securityGroup, id, secret, location, email, firstName, lastName, locationId);
 
 		String reply = "";
 		if (!resultKey)
 			reply = "KeyPair " + key + " does not exist"
 					+ (fix ? " and could not be created.\n" : "\n");
 		if (!result)
 			reply = "Security group " + securityGroup + " does not exist"
 					+ (fix ? " and could not be created.\n" : "\n");
 		return reply;
 	}
 
 	
 
 	/** Collection of virtual servers managed by the n3phele.resource
 	 * @param summary True to return only a collection summary, else return the collection children
 	 * @return the collection
 	 * @see Collection
 	 * @See BaseEntity
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer")
 	public Collection<BaseEntity> list(@DefaultValue("false") @QueryParam("summary") Boolean summary)
 	{
 		logger.info("get entered with summary " + summary);
 
 		Collection<BaseEntity> result = getCollection().collection(summary);
 		return result;
 	}
 	
 	/** List of input parameters supported by the factory for VM creation
 	 * 
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/inputParameters")
 	public TypedParameter[] inputParameterList()
 	{
 		return inputParameters;
 	}
 	
 	/** List of output parameters supported by the factory for VM creation
 	 * 
 	 */
 	@GET
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer/outputParameters")
 	public TypedParameter[] outputParameterList()
 	{
 		return outputParameters;
 	}
 	
 
 	/** create one or more new virtual servers. When multiple virtual servers are created, the siblings field of the virtualServer
 	 * object contains the URIs of all created virtual servers including that virtual server itself. 
 	 * @param r vm request information
 	 * @return URI of the first created virtual server. 
 	 * @throws Exception
 	 * @see ExecutionFactoryCreateRequest
 	 * @see VirtualServer
 	 * 
 	 */
 	@POST
 	@Produces("application/json")
 	@RolesAllowed("authenticated")
 	@Path("virtualServer")
 	public Response create(ExecutionFactoryCreateRequest r) throws Exception
 	{
 		logger.info("Create VM called");
 
 		int nodeCount = 1;
 		logger.info("Creating hp cloud request");
 		HPCloudCreateServerRequest hpcRequest = new HPCloudCreateServerRequest();
 		logger.info("Creating hp cloud manager with access key: "+r.accessKey+" and encryptedSecret: "+r.encryptedSecret);
 		HPCloudManager hpcManager = new HPCloudManager(new HPCloudCredentials(r.accessKey, r.encryptedSecret));
 		
 		for (NameValue p : r.parameters)
 		{
 			if (p.getKey().equalsIgnoreCase("nodeCount"))
 			{
 				String value = p.getValue();
 				try
 				{
 					nodeCount = Integer.valueOf(value);
 					if (nodeCount <= 0)
 						nodeCount = 1;
 				} catch (Exception e){}
 			}
 
 			if (p.getKey().equalsIgnoreCase("imageId"))
 			{
 				hpcRequest.imageId = p.getValue();
 			}
 
 			if (p.getKey().equalsIgnoreCase("instanceType"))
 			{
 				hpcRequest.hardwareId = p.getValue();
 			}
 
 			if (p.getKey().equalsIgnoreCase("securityGroup"))
 			{
 				hpcRequest.securityGroup = p.getValue();
 			}
 
 			if (p.getKey().equalsIgnoreCase("userData"))
 			{
				hpcRequest.userData = p.getValue();
 			}
 
 			if (p.getKey().equalsIgnoreCase("locationId"))
 			{
 				hpcRequest.locationId = p.getValue();
 			}
 			
 			if (p.getKey().equalsIgnoreCase("keyName"))
 			{
 				hpcRequest.keyName = p.getValue();
 			}
 		}
 
 		hpcRequest.nodeCount = nodeCount;
 		hpcRequest.serverName = r.name;
 
 		List<ServerCreated> resultList = hpcManager.createServerRequest(hpcRequest);
 		List<URI> uriList = new ArrayList<URI>(resultList.size());
 		ArrayList<String> siblings = new ArrayList<String>(resultList.size());
 		ArrayList<VirtualServer> vsList = new ArrayList<VirtualServer>(resultList.size());
 		
 		Date epoch = new Date();
 		for (ServerCreated srv : resultList)
 		{
 			VirtualServer item = new VirtualServer(srv.getName(), r.description, r.location, r.parameters, r.notification, r.accessKey, r.encryptedSecret, r.owner, r.idempotencyKey);
 			item.setCreated(epoch);
 			item.setInstanceId(srv.getId());
 			add(item);
 			vsList.add(item);			
 		}
 		
 		for(VirtualServer s : vsList)
 		{
 			uriList.add(s.getUri());
 			siblings.add(s.getUri().toString());
 		}
 		
 		for(VirtualServer s : vsList)
 		{
 			s.setSiblings(siblings);
 			update(s);
 		}
 		return Response.created(uriList.get(0)).entity(new HPCloudCreateServerResponse(uriList)).build();
 	}
 
 	/** Get details of a specific virtual server. This operation does a deep get, getting information from the cloud before
 	 * issuing a reply.
 	 * @param id the virtual server Id.
 	 * @return virtualServer object
 	 * @throws NotFoundException
 	 */
 	@GET
 	@Produces({ "application/json",
 			"application/vnd.com.n3phele.VirtualServer+json" })
 	@Path("virtualServer/{id}")
 	@RolesAllowed("authenticated")
 	public VirtualServer get(@PathParam("id") Long id) throws NotFoundException
 	{
 		VirtualServer item = deepGet(id);
 
 		return item;
 	}
 
 	/** Kill the nominated virtual server
 	 * @param id the id of the virtual server to termination
 	 * @throws NotFoundException
 	 */
 	@DELETE
 	@Path("virtualServer/{id}")
 	@RolesAllowed("authenticated")
 	public void kill(@PathParam("id") Long id, @DefaultValue("false") @QueryParam("debug") boolean debug, @DefaultValue("false") @QueryParam("error") boolean error) throws NotFoundException
 	{
 		VirtualServer virtualServer = null;
 		try
 		{
 			virtualServer = deepGet(id);
 			if (error && !debug)
 			{
 				terminate(virtualServer);
 			} else
 				softKill(virtualServer, error);
 		} catch (Exception e)
 		{
 			try
 			{
 				virtualServer = get(id);
 				terminate(virtualServer);
 			} catch (Exception ee)
 			{
 				if (virtualServer != null)
 					delete(virtualServer);
 			}
 		}
 	}
 	
 	
 	@GET
 	@Produces("text/plain")
 	@Path("total")
 	public String total()
 	{
 		String result;
 		Collection<VirtualServer> servers = getCollection();
 		result = Long.toString(servers.getTotal()) + "\n";
 		result += Calendar.getInstance().getTime().toString() + "\n";
 		return result;
 	}
 	
 	/** terminate a nominated virtual server
 	 * @param virtualServer virtual server to be terminated
 	 * @param error true that the server needs to be terminated, false it is a candidate for reuse
 	 */
 	protected void terminate(VirtualServer virtualServer)
 	{
 		;
 		try
 		{
 			deleteInstance(virtualServer, UUID.randomUUID(), 0);
 		} catch (Exception e)
 		{
 			manager.delete(virtualServer);
 		}
 	}
 	
 	/** Kill a nominated virtual server, preserving as a zombie if it is suitable
 	 * @param virtualServer virtual server to be terminated
 	 * @param stop true only stop the server, false terminate the server
 	 */
 	protected void softKill(VirtualServer virtualServer, boolean error)
 	{
 		;
 		try
 		{
 			if (!isZombieCandidate(virtualServer))
 				deleteInstance(virtualServer, UUID.randomUUID(), 0);
 			else
 			{
 				if (error)
 					makeDebug(virtualServer, UUID.randomUUID(), 0);
 				else
 					makeZombie(virtualServer, UUID.randomUUID(), 0);
 			}
 		} catch (Exception e)
 		{
 			manager.delete(virtualServer);
 		}
 	}
 	
 	private void makeZombie(VirtualServer item, UUID reference, int sequence) throws Exception
 	{
 		String instanceId = item.getInstanceId();
 		try
 		{
 			HPCloudCredentials credentials = new HPCloudCredentials(item.getAccessKey(), item.getEncryptedKey());
 			HPCloudManager hpcManager = new HPCloudManager(credentials);
 			item.setInstanceId(null);
 			item.setZombie(true);
 			updateStatus(item, "Terminated", reference, sequence);
 			update(item);
 
 			/**
 			 * Now we're using metadata to set instance behavior (zombie or debug)
 			 */
 			String locationId = getLocationId(item);
 			Map<String, String> tags = new HashMap<String, String>();
 			tags.put("n3phele-name", item.getName());
 			tags.put("n3phele-behavior", "zombie");
 			tags.put("n3phele-factory", Resource.get("factoryName", FACTORY_NAME));
 			tags.put("n3phele-uri", "");
 
 			hpcManager.putServerTags(item.getInstanceId(), locationId, tags);
 			
 			hpcManager.rebootNode(locationId, item.getInstanceId(), RebootType.SOFT);
 
 			/**
 			 * Create a new zombie virtualServer object, and then set item
 			 * instance Id to null. Update item. Update status.
 			 */
 			VirtualServer clone = new VirtualServer("zombie", item.getDescription(), item.getLocation(), item.getParameters(), null, item.getAccessKey(), item.getEncryptedKey(), item.getOwner(), item.getIdempotencyKey());
 			clone.setCreated(item.getCreated());
 			clone.setInstanceId(instanceId);
 
 			/**
 			 * The add operation does two writes in order to fix the reference
 			 * URI.
 			 * This creates a race condition for a fetch based on name of zombie
 			 * Similarly, refresh amd update could cause a race condition.
 			 * Updates semantics
 			 * need to be strengthened to fail if the object is not in the
 			 * store, and the check and write wrapped in
 			 * a transaction.
 			 */
 			add(clone);
 		} catch (Exception e)
 		{
 			logger.error("makeZombie delete of instanceId " + instanceId, e);
 			deleteInstance(item, UUID.randomUUID(), 0);
 			throw e;
 		}
 
 	}
 	
 	private void makeDebug(VirtualServer item, UUID reference, int sequence) throws Exception
 	{
 		String instanceId = item.getInstanceId();
 		try
 		{
 			HPCloudCredentials credentials = new HPCloudCredentials(item.getAccessKey(), item.getEncryptedKey());
 			HPCloudManager hpcManager = new HPCloudManager(credentials);
 			item.setInstanceId(null);
 			item.setZombie(true);
 			updateStatus(item, "Terminated", reference, sequence);
 			update(item);
 
 			/**
 			 * Now we're using metadata to set instance behavior (zombie or debug)
 			 */
 			String locationId = getLocationId(item);
 			Map<String, String> tags = new HashMap<String, String>();
 			tags.put("n3phele-name", item.getName());
 			tags.put("n3phele-behavior", "debug");
 			tags.put("n3phele-factory", Resource.get("factoryName", FACTORY_NAME));
 			tags.put("n3phele-uri", "");
 
 			hpcManager.putServerTags(item.getInstanceId(), locationId, tags);
 
 			/**
 			 * Create a new zombie virtualServer object, and then set item
 			 * instance Id to null. Update item. Update status.
 			 */
 			VirtualServer clone = new VirtualServer("debug", item.getDescription(), item.getLocation(), item.getParameters(), null, item.getAccessKey(), item.getEncryptedKey(), item.getOwner(), item.getIdempotencyKey());
 			clone.setCreated(item.getCreated());
 			clone.setInstanceId(instanceId);
 
 			add(clone);
 		} catch (Exception e)
 		{
 			logger.error("makeDebug delete of instanceId "	+ instanceId, e);
 			item.setInstanceId(instanceId);
 			deleteInstance(item, UUID.randomUUID(), 0);
 			throw e;
 		}
 	}
 	
 	private boolean isZombieCandidate(VirtualServer virtualServer)
 	{
 		boolean result = virtualServer != null
 				&& virtualServer.getInstanceId() != null
 				&& virtualServer.getInstanceId().length() > 0;
 
 		if (result)
 		{
 			if (virtualServer.getSiblings() != null	&& virtualServer.getSiblings().size() > 1)
 			{
 				logger.info("Server has " + virtualServer.getSiblings().size()	+ " siblings");
 				result = false;
 			}
 			else
 			{
 				if (virtualServer.getSpotId() != null && virtualServer.getSpotId().length() != 0)
 				{
 					logger.info("Server is spot instance");
 					result = false;
 				}
 				else if (!virtualServer.getStatus().equalsIgnoreCase("running")) 
 				{
 					logger.info("Server is " + virtualServer.getStatus());
 					result = false;
 				}
 			}
 		} else
 		{
 			logger.info("Null server or instanceId");
 		}
 		return result;
 	}
 
 	/** Refresh the model. The refresh process walks the model and updates the state of virtual servers to reflect their
 	 * status in EC2. Change of state notifications will be issued as appropriate to the notification URL nominated in the create request.
 	 * @return summary of the virtualServer collection. The collection total size field, if negitive, denotes a partial refresh operation.
 	 */
 	@GET
 	@Path("admin/refresh")
 	@Produces("application/json")
 	@RolesAllowed("admin")
 	public Collection<BaseEntity> refresh()
 	{
 		long start = Calendar.getInstance().getTimeInMillis();
 
 		Collection<BaseEntity> result = refreshCollection();
 
 		logger.info(String.format("-----refresh-- %d ms processing %d items", (Calendar.getInstance().getTimeInMillis() - start), result.getTotal()));
 		return result;
 	}
 
 	protected VirtualServer deepGet(Long id) throws NotFoundException
 	{
 		VirtualServer s = load(id);
 		updateVirtualServer(s, UUID.randomUUID(), 0);
 		return s;
 	}
 	
 	/** Updates virtual server object and data store state
 	 * @param item object to update
 	 * @param reference reference UUID used for notifications
 	 * @param sequence notification sequence number
 	 */
 
 	protected void updateVirtualServer(VirtualServer item, UUID reference, int sequence) throws IllegalArgumentException
 	{
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(item.getAccessKey(), item.getEncryptedKey()));
 		String instanceId = item.getInstanceId();
 		boolean madeIntoZombie = item.isZombie();
 
 		if (madeIntoZombie)
 		{
 			//TODO: Should we consider to use the Enums instead of hard coded string?
 			if (updateStatus(item, "terminated", reference, sequence))
 				update(item);
 			
 			if (item.getStatus().equals("terminated"))
 			{
 				logger.warn("Instance " + item.getName() + " terminated .. purging");
 				delete(item);
 				return;
 			}
 		} else if (instanceId != null && instanceId.length() > 0)
 		{
 			String locationId = getLocationId(item);
 
 			Server s = hpcManager.getServerById(locationId, item.getInstanceId());
 			if (s != null)
 			{
 				//Status currentStatus = s.getStatus();
 				String currentStatus = "";
 				if(s.getStatus().toString().compareTo("ACTIVE")==0) currentStatus = "running";
 				else if(s.getStatus().toString().compareTo("BUILD")==0 || s.getStatus().toString().compareTo("REBUILD")==0 || s.getStatus().toString().compareTo("REBOOT")==0 || s.getStatus().toString().compareTo("HARD_REBOOT")==0){
 					currentStatus = "initializing";
 				}else{
 					currentStatus = "terminated";
 				}
 				/**
 				 * If the statuses are different, and the current cloud status
 				 * is ACTIVE (Running), we should update.
 				 */
 				if (!item.getStatus().equalsIgnoreCase(currentStatus) && currentStatus.compareTo("running") == 0)  
 				{
 					Map<String, String> tags = new HashMap<String, String>();
 					tags.put("n3phele-name", item.getName());
 					tags.put("n3phele-factory", Resource.get("factoryName", FACTORY_NAME));
 					tags.put("n3phele-uri", item.getUri().toString());
 					s.getExtendedAttributes();
 					item.setOutputParameters(HPCloudExtractor.extract(s));					
 					hpcManager.putServerTags(item.getInstanceId(), locationId, tags);
 				}
 
 				if (updateStatus(item, currentStatus, reference, sequence))
 					update(item);
 
 				//TODO: Should we consider to use the Enums instead of hard coded string?
 				if (item.getStatus().equals("terminated"))
 				{
 					logger.warn("Instance " + item.getInstanceId() + " terminated .. purging");
 					delete(item);
 					return;
 				}
 			} else
 			{
 				logger.warn("Instance " + item.getInstanceId() + " not found, assumed terminated .. purging");
 				delete(item);
 				return;
 			}
 		}
 	}
 	
 	private String getLocationId(VirtualServer item)
 	{
 		ArrayList<NameValue> listParameters = item.getParameters();
 		String locationId = null;
 
 		for (NameValue p : listParameters)
 		{
 			if (p.getKey().equalsIgnoreCase("locationId"))
 			{
 				locationId = p.getValue();
 				break;
 			}
 		}
 		
 		return locationId;
 	}
 
 
 	private void refreshVirtualServer(VirtualServer item)
 	{
 		if (item == null)
 			return;
 
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(item.getAccessKey(), item.getEncryptedKey()));
 
 		String locationId = getLocationId(item);
 
 		Server s = hpcManager.getServerById(locationId, item.getInstanceId());
 		if (s != null)
 		{
 			Status currentStatus = s.getStatus();
 			item.setStatus(currentStatus.toString());
 		} else
 		{
 			logger.warn("Instance " + item.getInstanceId() + " not found, assumed terminated ..");
 			item.setStatus("Terminated");
 		}
 	}
 	
 	/** Check if a zombie has expired and clean up if it has
 	 * @param s virtual server
 	 * @return TRUE if zombie
 	 */
 	private boolean checkForZombieExpiry(VirtualServer s)
 	{
 		boolean debugInstance = false;
 		boolean zombieInstance = false;
 		ArrayList<NameValue> listParameters = s.getParameters();
 		
 		for(NameValue p : listParameters)
 		{
 			if( p.getKey().equalsIgnoreCase("n3phele-behavior") )
 			{
 				if( p.getValue().equalsIgnoreCase("debug") )
 					debugInstance = true;
 				else if( p.getValue().equalsIgnoreCase("zombie") )
 					zombieInstance = true;
 				
 				break;
 			}
 		}
 		
 		if(zombieInstance || debugInstance )
 		{
 			HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(s.getAccessKey(), s.getEncryptedKey()));
 			
 			if( s.getStatus().equalsIgnoreCase("terminated") )
 			{
 				logger.info("Found dead "+s.getName()+" with id "+s.getInstanceId()+" created "+s.getCreated());
 				manager.delete(s);
 				return true;
 			}
 			
 			long created = s.getCreated().getTime();
 			long now = new Date().getTime();
 			long age = ((now - created)% (60*60*1000))/60000;
 			
 			if(age > 55 || !s.getStatus().equalsIgnoreCase("running") || debugInstance || zombieInstance )
 			{
 				logger.info("Killing "+s.getName()+" with id "+s.getInstanceId()+" created "+s.getCreated());
 				s.setName(debugInstance? "debug" : "zombie");
 				update(s);
 				
 				String locationId = getLocationId(s);
 				hpcManager.terminateNode(locationId, s.getInstanceId());
 			}
 		}
 		return false;
 	}
 
 	private Collection<BaseEntity> refreshCollection()
 	{
 		Collection<VirtualServer> servers = getCollection();
 		Collection<BaseEntity> result = servers.collection(true);
 		UUID reference = UUID.randomUUID();
 		int sequence = 0;
 		try {
 			for (VirtualServer s : servers.getElements()) {
 	
 					try {
 						if(s.getUri() != null && !checkForZombieExpiry(s))
 							updateVirtualServer(s, reference, sequence++);
 					} 
 					//TODO implement this using jcloud
 					/*catch (AmazonClientException ignore) {
 						log.log(Level.WARNING, s.getUri()+" refresh failed. ignoring..",ignore);
 					}*/
 					catch (Exception e) {
 						logger.warn(s.getUri()+" refresh failed. Killing..",e);
 						try {
 							terminate(s);
 						} catch (Exception another) {
 							
 						} finally {
 							delete(s);
 						}
 					}
 			}
 		} catch (DeadlineExceededException deadline) {
 			return result;
 		}
 		return result;
 	}
 
 	private void deleteInstance(VirtualServer item, UUID reference, int sequence) throws Exception
 	{
 		String instanceId = item.getInstanceId();
 		try
 		{
 			//TODO: Should we consider to use the Enums instead of hard coded string?
 			if (!item.getStatus().equalsIgnoreCase("Terminated") && instanceId != null && instanceId.length() > 0)
 			{
 
 				HPCloudCredentials credentials = new HPCloudCredentials(item.getAccessKey(), item.getEncryptedKey());
 				HPCloudManager hpManager = new HPCloudManager(credentials);
 
 				String locationId = getLocationId(item);
 
 				if (locationId == null)
 				{
 					logger.error("locationId is null, cannot delete instance "	+ item.getInstanceId(), new IllegalArgumentException("locationId: null"));
 					throw new IllegalArgumentException("locationId: null");
 				}
 
 				boolean result = hpManager.terminateNode(locationId, item.getInstanceId());
 
 				if (result)
 				{
 					logger.warn("Instance " + item.getInstanceId() + "deleted");
 					if (updateStatus(item, "Terminated", reference, sequence))
 						update(item);
 					
 				} else
 				{
 					logger.warn("Instance " + item.getInstanceId() + "could not be deleted");
 				}
 			}
 			else
 			{
 				if (updateStatus(item, "Terminated", reference, sequence))
 					update(item);
 			}
 
 		} catch (Exception e)
 		{
 			logger.error("Cleanup delete of instanceId " + instanceId, e);
 			throw e;
 		}
 
 	}
 	
 	private boolean createWithZombie(VirtualServer item)
 	{
 		List<VirtualServer> zombies = getZombie();
 		if (zombies != null)
 		{
 			logger.info("Got " + zombies.size() + " Zombies ");
 			zombieCheck: for (VirtualServer s : zombies)
 			{
 				boolean locationMatch = s.getLocation().equals(item.getLocation());
 				boolean accessMatch = s.getAccessKey().equals(item.getAccessKey());
 				boolean secretMatch = s.getEncryptedKey().equals(item.getEncryptedKey());
 				logger.info(" Zombie " + s.getInstanceId() + " location "+ locationMatch + " access " + accessMatch + " secret "+ secretMatch);
 				
 				if (locationMatch && accessMatch && secretMatch)
 				{
 					Map<String, String> map = s.getParametersMap();
 					for (NameValue x : item.getParameters())
 					{
 						if (!SafeEquals(x.getValue(), map.get(x.getKey())))
 						{
 							logger.info("Mismatch on " + x.getKey() + " need "+ x.getValue() + " zombie "+ map.get(x.getKey()));
 							continue zombieCheck;
 						}
 					}
 					
 					// zombie matches
 					GenericModelDao<VirtualServer> itemDaoTxn = null;
 					boolean claimed = false;
 					try
 					{
 						itemDaoTxn = manager.itemDaoFactory(true);
 						VirtualServer zombie = itemDaoTxn.get(s.getId());
 						zombie.setIdempotencyKey(new Date().toString());
 						itemDaoTxn.put(zombie);
 						itemDaoTxn.delete(zombie);
 						itemDaoTxn.ofy().getTxn().commit();
 						claimed = true;
 					} catch (Exception e)
 					{
 						logger.warn( "Zombie processing contention", e);
 					} finally
 					{
 						if (itemDaoTxn.ofy().getTxn().isActive())
 							itemDaoTxn.ofy().getTxn().rollback();
 					}
 					if (claimed)
 					{
 						List<VirtualServer> leftOverZombies = getZombie();
 						if (leftOverZombies != null)
 							logger.info("Got " + leftOverZombies.size() + " zombies remaining");
 						else
 							logger.info("Got 0 Zombies remaining");
 						
 						logger.info("Claimed " + s.getInstanceId());
 						refreshVirtualServer(s);
 						
 						if( !s.getStatus().equalsIgnoreCase("running") )
 						{
 							terminate(s);
 							continue;
 						}
 						item.setInstanceId(s.getInstanceId());
 						item.setCreated(s.getCreated());
 						updateVirtualServer(item, UUID.randomUUID(), 0);
 						
 						if( item.getStatus().equalsIgnoreCase("running") )
 							return true;
 						else
 							continue; // There's no difference calling continue here, i think.
 						
 					} else
 					{
 						logger.warn("Zombie contention on " + s.getInstanceId());
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean SafeEquals(String a, String b)
 	{
 		if ((a == null || a.length() == 0) && (b == null || b.length() == 0))
 			return true;
 		if (a == null || b == null)
 			return false;
 		return (a.equals(b));
 	}
 
 	protected boolean updateStatus(VirtualServer s, String newStatus, UUID reference, int sequence)
 	{
 		String oldStatus = s.getStatus();
 		newStatus = newStatus.toLowerCase();
 		if (oldStatus.equals(newStatus))
 			return false;
 		s.setStatus(newStatus);
 		try
 		{
 			sendNotification(s, oldStatus.toLowerCase(), newStatus, reference.toString(), sequence);
 		} catch (Exception e)
 		{
 			logger.info("SendNotification exception to <" + s.getNotification() + "> from " + s.getUri() + " old: " + oldStatus + " new: " + s.getStatus(), e);
 			if (oldStatus.equals(newStatus.toUpperCase()))
 			{
 				logger.warn("Cancelling SendNotification to <" + s.getNotification() + "> from " + s.getUri() + " old: " + oldStatus + " new: " + s.getStatus());
 			}
 			else
 			{
 				s.setStatus(newStatus.toUpperCase());
 			}
 		}
 		return true;
 	}
 
 	private void sendNotification(VirtualServer s, String oldStatus, String newStatus, String reference, int sequence) throws Exception
 	{
 		URI notification = s.getNotification();
 		logger.info("SendNotification to <" + notification + "> from " + s.getUri() + " old: " + oldStatus + " new: " + s.getStatus());
 
 		if (notification == null)
 			return;
 
 		if (client == null)
 		{
 			client = Client.create();
 		}
 		WebResource resource = client.resource(s.getNotification());
 
 		ClientResponse response = resource.queryParam("source", s.getUri().toString()).queryParam("oldStatus", oldStatus).queryParam("newStatus", newStatus).queryParam("reference", reference).queryParam("sequence", Integer.toString(sequence)).type(MediaType.TEXT_PLAIN).get(ClientResponse.class);
 		logger.info("Notificaion status " + response.getStatus());
 		if (response.getStatus() == 410)
 		{
 			logger.info("VM GONE .. killing " + s.getUri() + " silencing reporting to " + s.getNotification());
 			s.setNotification(null);
 			deleteInstance(s, UUID.randomUUID(), 0);
 		}
 	}
 
 	protected boolean checkKey(String key, String id, String secret, URI location, String locationId)
 	{
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(id, secret));
 
 		return hpcManager.checkKeyPair(key, locationId);
 	}
 
 	protected boolean createKey(String key, String id, String secret, URI location, String email, String firstName, String lastName, String locationId)
 	{
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(id, secret));
 		KeyPair newKey = hpcManager.createKeyPair(key, locationId);
 
 		if (newKey != null)
 		{
 			logger.warn("Got " + newKey.toString());
 			sendNotificationEmail(newKey, email, firstName, lastName, location);
 			return true;
 		}
 
 		logger.error("Key pair couldn't be created");
 		return false;
 	}
 
 	public void sendNotificationEmail(KeyPair keyPair, String to, String firstName, String lastName, URI location)
 	{
 		try
 		{
 			StringBuilder subject = new StringBuilder();
 			StringBuilder body = new StringBuilder();
 			subject.append("Auto-generated keyPair \"");
 			subject.append(keyPair.getName());
 			subject.append("\"");
 			body.append(firstName);
 			body.append(",\n\nA keypair named \"");
 			body.append(keyPair.getName());
 			body.append("\" has been generated for you. \n\n");
 			body.append("Please keep this information secure as it allows access to the virtual machines");
 			body.append(" run on your behalf by n3phele on the cloud at ");
 			body.append(location.toString());
 			body.append(". To access the machines using ssh copy all of the lines");
 			body.append(" including -----BEGIN RSA PRIVATE KEY----- and -----END RSA PRIVATE KEY-----");
 			body.append(" into a file named ");
 			body.append(keyPair.getName());
 			body.append(".pem\n\n");
 			// TODO: check if private key is the same as key material
 			body.append(keyPair.getPrivateKey());
 			body.append("\n\nn3phele\n--\nhttps://n3phele.appspot.com\n\n");
 
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress("n3phele@gmail.com", "n3phele"));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, firstName
 					+ " " + lastName));
 			msg.setSubject(subject.toString());
 			msg.setText(body.toString());
 			Transport.send(msg);
 
 		} catch (AddressException e)
 		{
 			logger.error("Email to " + to, e);
 		} catch (MessagingException e)
 		{
 			logger.error("Email to " + to, e);
 		} catch (UnsupportedEncodingException e)
 		{
 			logger.error( "Email to " + to, e);
 		} catch (Exception e)
 		{
 			logger.error( "Email to " + to, e);
 		}
 	}
 
 	protected boolean checkSecurityGroup(String groupName, String id, String secret, URI location, String locationId)
 	{
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(id, secret));
 
 		return hpcManager.checkSecurityGroup(groupName, locationId);
 	}
 	
 	public void sendSecurityGroupNotificationEmail(String securityGroup, String to, String firstName, String lastName, URI location)
 	{
 		try
 		{
 			StringBuilder subject = new StringBuilder();
 			StringBuilder body = new StringBuilder();
 			subject.append("Auto-generated security group: \"");
 			subject.append(securityGroup);
 			subject.append("\"");
 			body.append(firstName);
 			body.append(",\n\nA security group named \"");
 			body.append(securityGroup);
 			body.append("\" has been generated for you. \n\n");
 			body.append("This is used as the default firewall for machines");
 			body.append(" run on your behalf on ");
 			body.append(location.toString());
 			body.append(".\n\nn3phele\n--\nhttps://n3phele.appspot.com\n\n");
 
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 
 			Message msg = new MimeMessage(session);
 			msg.setFrom(new InternetAddress("n3phele@gmail.com", "n3phele"));
 			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, firstName
 					+ " " + lastName));
 			msg.setSubject(subject.toString());
 			msg.setText(body.toString());
 			Transport.send(msg);
 
 		} catch (AddressException e)
 		{
 			logger.error( "Email to " + to, e);
 		} catch (MessagingException e)
 		{
 			logger.error( "Email to " + to, e);
 		} catch (UnsupportedEncodingException e)
 		{
 			logger.error("Email to " + to, e);
 		} catch (Exception e)
 		{
 			logger.error("Email to " + to, e);
 		}
 
 	}
 	
 	protected boolean makeSecurityGroup(String groupName, String id, String secret, URI location, String to, String firstName, String lastName, String locationId)
 	{
 		HPCloudManager hpcManager = new HPCloudManager(getHPCredentials(id, secret));
 
 		SecurityGroup sg = hpcManager.createSecurityGroup(groupName, locationId);
 		sendSecurityGroupNotificationEmail(sg.getName(), to, firstName, lastName, location);
 
 		return true;
 	}
 	
 	protected HPCloudCredentials getHPCredentials(String identity, String secretKey)
 	{
 		try
 		{
 			HPCloudCredentials credentials = new HPCloudCredentials(identity, secretKey);
 			return credentials;
 		}
 		catch (Exception e)
 		{
 			throw new WebApplicationException();
 		}
 	}
 
 	private static class VirtualServerManager extends AbstractManager<VirtualServer> {
 
 		@Override
 		protected URI myPath()
 		{
 			return UriBuilder.fromUri(Resource.get("baseURI", "http://localhost:8889/resources")).path("virtualServer").build();
 		}
 
 		@Override
 		protected GenericModelDao<VirtualServer> itemDaoFactory(boolean transactional)
 		{
 			return new ServiceModelDao<VirtualServer>(VirtualServer.class, transactional);
 		}
 	}
 
 	/**
 	 * Located a item from the persistent store based on the item id.
 	 * @param id
 	 * @return the item
 	 * @throws NotFoundException is the object does not exist
 	 */
 	public VirtualServer load(Long id) throws NotFoundException { return manager.get(id); }
 	/**
 	 * Locate a item from the persistent store based on the item name.
 	 * @param name
 	 * @return the item
 	 * @throws NotFoundException is the object does not exist
 	 */
 	public VirtualServer load(String name) throws NotFoundException { return manager.get(name); }
 	/**
 	 * Locate a item from the persistent store based on the item URI.
 	 * @param uri
 	 * @return the item
 	 * @throws NotFoundException is the object does not exist
 	 */
 	public VirtualServer load(URI uri) throws NotFoundException { return manager.get(uri); }
 	/** Add a new item to the persistent data store. The item will be updated with a unique key, as well
 	 * the item URI will be updated to include that defined unique team.
 	 * @param virtualServer to be added
 	 * @throws IllegalArgumentException for a null argument
 	 */
 	public void add(VirtualServer virtualServer) throws IllegalArgumentException { manager.add(virtualServer); }
 	/** Update a particular object in the persistent data store
 	 * @param virtualServer the virtualServer to update
 	 * @throws NotFoundException is the object does not exist 
 	 */
 	public void update(VirtualServer virtualServer) throws NotFoundException { manager.update(virtualServer); }
 	/**
 	 * Delete item from the persistent store
 	 * @param virtualServer to be deleted
 	 */
 	public void delete(VirtualServer virtualServer) { manager.delete(virtualServer); }
 	
 	/**
 	 * Collection of resources of a particular class in the persistent store. The will be extended
 	 * in the future to return the collection of resources accessible to a particular user.
 	 * @return the collection
 	 */
 	public Collection<VirtualServer> getCollection() {return manager.getCollection();}
 	
 	public List<Key<VirtualServer>> getCollectionKeys() {
 
 		List<Key<VirtualServer>> result = null;
 		try { 
 			result = manager.itemDao().ofy().query(manager.itemDao().clazz).listKeys();
 		} catch (NotFoundException e) {
 		}
 		return result;
 
 	}
 	
 	public List<VirtualServer> getByIdempotencyKey(String key) { return manager.itemDao().ofy().query(VirtualServer.class).filter("idempotencyKey", key).list(); }
 	
 	public List<VirtualServer> getZombie() { return manager.itemDao().ofy().query(VirtualServer.class).filter("name", "zombie").list(); }
 
 	public final static TypedParameter inputParameters[] =  {
 		new TypedParameter("instanceType", "Specifies the virtual machine size. Valid Values: 100 (standard.xsmall), 101 (standard.small), 102 (standard.medium), 103 (standard.large), 104 (standard.xlarge), 105 (standard.2xlarge)", ParameterType.String,"", "100"),
 		new TypedParameter("imageId", "Unique ID of a machine image, returned by a call to RegisterImage", ParameterType.String, "", "75845"),
 		new TypedParameter("keyName", "Name of the SSH key  to be used for communication with the VM", ParameterType.String, "", "hpdefault"),
 		new TypedParameter("nodeCount", "Number of instances to launch.", ParameterType.Long, "", "1"),
 		new TypedParameter("locationId", "Unique ID of hpcloud zone. Valid Values: az-1.region-a.geo-1 | az-2.region-a.geo-1 | az-3.region-a.geo-1", ParameterType.String, null, "az-1.region-a.geo-1"),
 		new TypedParameter("securityGroup", "Name of the security group which controls the open TCP/IP ports for the VM.", ParameterType.String, "", "default"),
 		new TypedParameter("userData", "Base64-encoded MIME user data made available to the instance(s). May be used to pass startup commands.", ParameterType.String, "value", "#!/bin/bash\necho n3phele agent injection... \nset -x\n wget -q -O - https://n3phele-agent.s3.amazonaws.com/n3ph-install-tgz-basic | su - -c '/bin/bash -s ec2-user ~/agent ~/sandbox' ec2-user\n")
 	};
 	
 	public final static TypedParameter outputParameters[] =  {		
 		new TypedParameter("AccessIPv4", "IPv4 public server address", ParameterType.String, "", ""),
 		new TypedParameter("AccessIPv6", "IPv6 public server address", ParameterType.String, "", ""),
 		new TypedParameter("privateIpAddress", "Specifies the private IP address that is assigned to the instance.", ParameterType.String, "", ""),
 		new TypedParameter("publicIpAddress", "Specifies the public IP address of the instance.", ParameterType.String, "", ""),
 		new TypedParameter("Class", "Class of the server object", ParameterType.String, "", ""),
 		new TypedParameter("ConfigDrive", "Drive configuration of the server", ParameterType.String, "", ""),
 		new TypedParameter("Created", "Date when the server was created", ParameterType.String, "", ""),
 		new TypedParameter("DiskConfig", "Disk config attribute from the Disk Config Extension (alias OS-DCF)", ParameterType.String, "", ""),
 		new TypedParameter("ExtendedAttributes", "Extended server attributes fields (alias OS-EXT-SRV-ATTR)", ParameterType.String, "", ""),
 		new TypedParameter("Flavor", "Standard Instance type of the server", ParameterType.String, "", ""),
 		new TypedParameter("HostId", "Host identifier, or null if in Server.Status.BUILD", ParameterType.String, "", ""),
 		new TypedParameter("Id", "Id of the server", ParameterType.String, "", ""),
 		new TypedParameter("Image", "Image of the server", ParameterType.String, "", ""),
 		new TypedParameter("KeyName", "KeyName if extension is present and there is a value for this server", ParameterType.String, "", ""),
 		new TypedParameter("Links", "The links of the id address allocated to the new server", ParameterType.List, "", ""),
 		new TypedParameter("Name", "Name of the server", ParameterType.String, "", ""),
 		new TypedParameter("TenantId", "Group id of the server", ParameterType.String, "", ""),
 		new TypedParameter("Updated", "When the server was last updated", ParameterType.String, "", ""),
 		new TypedParameter("UserId", "User id of the server", ParameterType.String, "", ""),
 		new TypedParameter("UuId", "Unique server id", ParameterType.String, "", ""),
 		new TypedParameter("InstanceName", "Server instance name", ParameterType.String, "", ""),
 		new TypedParameter("HostName", "Server host name", ParameterType.String, "", ""),
 		new TypedParameter("HypervisorHostName", "Server hypervisor host name", ParameterType.String, "", "")
 	};
 }
