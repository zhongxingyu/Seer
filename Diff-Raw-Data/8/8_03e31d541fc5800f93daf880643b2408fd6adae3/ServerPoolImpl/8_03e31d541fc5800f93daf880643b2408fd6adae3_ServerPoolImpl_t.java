 /*******************************************************************************
  * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *******************************************************************************/
 package beans;
 
 import java.util.List;
 
 import beans.config.Conf;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 
 import models.ServerNode;
 import models.WidgetInstance;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import server.*;
 
 import javax.inject.Inject;
 
 
 /**
  * This class manages a server pool of available/busy bootstrapped machines(servers).
  * A server pool initialize with configured minimum/maximum number of servers. 
  * The get() method returns a bootstrap server instance.
  * On init() a server-pool deletes an expires or orphans servers. 
  * It also provides ability to startup with a cold init which deletes a running server. See {@link server.Config} class.
  * 
  * @author Igor Goldenberg
  * @see ServerBootstrapperImpl
  */
 public class ServerPoolImpl implements ServerPool
 {
 
     private static Logger logger = LoggerFactory.getLogger( ServerPoolImpl.class );
     @Inject
     private ServerBootstrapper serverBootstrapper;
 
     @Inject
     private ExpiredServersCollector expiredServerCollector;
 
     @Inject
     private Conf conf;
 
 
 	private void init()
 	{
 		logger.info( String.format( "Started to initialize ServerPool, cold-init=%s", conf.server.pool.coldInit ) );
 		
 		// get all available running servers
 		List<Server> serverList = serverBootstrapper.getServerList();
 		for( java.util.Iterator<Server> iter = serverList.iterator(); iter.hasNext(); )
 		{
 			Server srv = iter.next();
 			
 			// ignore our server
 			if ( srv.getId().equals( conf.widget.serverId ) )
 			{
 				iter.remove();
 				continue;
 			}
 			
 			ServerNode server = ServerNode.getServerNode(srv.getId());
 			
 			// if null this server wasn't found in our DB or server expired - we terminate it
 			if ( server == null || server.isExpired() || conf.server.pool.coldInit )
 			{
 				logger.info( String.format( "ServerId: %s expired or not found in server-pool, address: %s", srv.getId(), srv.getAddresses() ) );
				serverBootstrapper.destroyServer(srv.getId());
 				iter.remove();
 			}
 			else
 			{
 				if ( server.isBusy() )
 				{
 				   logger.info( String.format( "Found a busy server, leave it: %s", srv ) );
 				   iter.remove();
 				      
 				   if ( server.isTimeLimited() )
 				     expiredServerCollector.scheduleToDestroy(server);
 				}
 				else
 				   logger.info( String.format( "Found a free bootstrapped server, add to a server pool: %s", srv ) );
 			}
 		}// for
 
 		
 		/* check whether in server-pool left some orphans servers,
 		 * it may happen if server-pool still keeps some server that already terminated 
 		 */
 		logger.info("Check whether in server-pool left some orphans servers...");
 		for( ServerNode node : ServerNode.all() )
 		{
 			boolean isFound = false;
 			for( Server activeServer : serverList  )
 			{
 				if ( activeServer.getId().equals(node.getId()) )
 				{
 					isFound = true;
 					break;
 				}
 			}
 			
 			if ( !isFound )
 			{
 				logger.info("Delete orphans server from a server pool " + node);
 				node.delete();
 			}
 		}
 		
 		// create new servers if need
 		if ( serverList.size() < conf.server.pool.minNode )
 		{
 			int serversToInit = conf.server.pool.minNode - serverList.size();
 			
 			logger.info( "ServerPool starting to initialize " + serversToInit + " servers..." );
 			
 			List<ServerNode> servers = serverBootstrapper.createServers(serversToInit);
 
 			// keep in DB as free servers
 			for( ServerNode srv :  servers )
 				srv.save();
 		}
 	}
 	
 	/** @return a ServerNode from the pool, otherwise <code>null</code> if no free server available */
 	synchronized public ServerNode get()
 	{
 		ServerNode freeServer = ServerNode.getFreeServer();
 		if ( freeServer != null)
 		{
 			freeServer.setBusy(true);
 			
 			// schedule to destroy after time expiration 
 			// TODO when unlimited server will support uncomment this line if ( freeServer.isTimeLimited() )
 			ApplicationContext.get().getExpiredServersCollector().scheduleToDestroy(freeServer);
 		}
 
 		addNewServerToPool();
 
 		return freeServer;
 	}
 	
 	public void destroy(String serverId)
 	{
 		// when we move to Quarz just unregister from Cron
 		if ( ServerNode.getServerNode( serverId ) != null )
 			addNewServerToPool();
 
 		WidgetInstance.deleteByInstanceId(serverId);
 		ServerNode.deleteServer( serverId );
        serverBootstrapper.destroyServer( serverId );
 	}
 	
 	void addNewServerToPool()
 	{
 		if ( ServerNode.count() >= conf.server.pool.maxNodes )
 		{
 			logger.info("Server-pool has reached maximum capacity: " + conf.server.pool.maxNodes);
 			return;
 		}
 
 		new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
					List<ServerNode> servers = serverBootstrapper.createServers(1);
 					
 					for( ServerNode srv :  servers )
 						srv.save();
 				} catch (Exception e)
 				{
 					logger.error("ServerPool failed to create a new server node", e);
 				}
 			}
 		}).start();
 	}
 
     public void setServerBootstrapper(server.ServerBootstrapper serverBootstrapper) {
         this.serverBootstrapper = serverBootstrapper;
     }
 
     public void setExpiredServerCollector(ExpiredServersCollector expiredServerCollector) {
         this.expiredServerCollector = expiredServerCollector;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 }
