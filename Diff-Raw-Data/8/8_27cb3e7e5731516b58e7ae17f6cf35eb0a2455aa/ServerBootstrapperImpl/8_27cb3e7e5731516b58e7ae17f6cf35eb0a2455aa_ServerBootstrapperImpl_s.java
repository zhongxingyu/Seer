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
 
 import beans.api.ExecutorFactory;
 import beans.config.Conf;
 import com.google.common.net.HostAndPort;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import models.ServerNode;
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecuteResultHandler;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.JsonNode;
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.ExecResponse;
 import org.jclouds.domain.LoginCredentials;
 import org.jclouds.logging.config.NullLoggingModule;
 import org.jclouds.openstack.nova.v2_0.NovaApi;
 import org.jclouds.openstack.nova.v2_0.NovaAsyncApi;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.Server.Status;
 import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
 import org.jclouds.openstack.nova.v2_0.features.ServerApi;
 import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
 import org.jclouds.rest.RestContext;
 import org.jclouds.ssh.SshClient;
 import org.jclouds.sshj.config.SshjSshClientModule;
 import org.jclouds.util.Strings2;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import play.api.libs.ws.Response;
 import play.api.libs.ws.WS;
 import play.i18n.Messages;
 import play.libs.Json;
 import server.ApplicationContext;
 import server.DeployManager;
 import server.ProcExecutor;
 import server.ServerBootstrapper;
 import server.exceptions.ServerException;
 import utils.CloudifyUtils;
 import utils.Utils;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 
 
 
 /**
  * This class manages a compute cloud provider by JCloud openstack nova infrastructure.
  * It provides ability to create/delete specific server with desired flavor configuration.
  * On each new server runs a bootstrap script that prepare machine for a server-pool,
  * it includes a setup of firewall, JDK, cloudify installation and etc...
  * The bootstrap script can be found under ssh/bootstrap_machine.sh
  *
  * @author Igor Goldenberg
  */
 public class ServerBootstrapperImpl implements ServerBootstrapper
 {
 
     private static Logger logger = LoggerFactory.getLogger( ServerBootstrapperImpl.class );
 	private ComputeService _compute;
 	private RestContext<NovaApi, NovaAsyncApi> _nova;
 
     private int retries = 2;
     private int bootstrapRetries = 2;
 
     @Inject
     private Conf conf;
 
     @Inject
     private ExecutorFactory executorFactory;
 
     @Inject
     private DeployManager deployManager;
 
     private Exception lastKnownException = null;
 
     public List<ServerNode> createServers( int numOfServers )
 	{
 		List<ServerNode> servers = new ArrayList<ServerNode>();
 		logger.info("creating {} new instances", numOfServers );
 		for( int i=0; i< numOfServers; i++ )
 		{
             ServerNode server = createServer();
             servers.add( server );
         }
 		return servers;
 	}
 
     public ServerNode createServer()
     {
 
         ServerNode serverNode = null;
         int i =0;
         for ( ; i < retries && serverNode == null ; i++){
             logger.info( "creating new server node, try #[{}]",i );
             serverNode = tryCreateServer();
         }
 
         if ( serverNode == null ){
             throw new RuntimeException( "unable to create new nodes!! printed last known exception here", lastKnownException );
         }
         return serverNode;
 
     }
 
     public ServerNode tryCreateServer(){
         ServerNode srvNode = null;
         try {
             srvNode = createServerNode();
             return srvNode;
         } catch ( Exception e ) {
             lastKnownException = e;
             // failed to boostrap machine, nothing to do - let destroy :(
             if ( srvNode != null ) {
                 destroyServer( srvNode.getNodeId() );
             }
             logger.error( "Failed to bootstrap machine. [{}]", srvNode, e );
         }
         return null;
     }
 
     // guy - todo - need to ping machine first. If machine is down, we cannot validate - throw exception.
     // guy - todo - if machine is up we do the same thing we do today.
     @Override
     public BootstrapValidationResult validateBootstrap( ServerNode serverNode )
     {
         BootstrapValidationResult result = new BootstrapValidationResult();
         try{
         result.machineReachable = isMachineReachable( serverNode );
         }catch(Exception e){
             result.machineReachableException = e;
         }
 
         if ( result.machineReachable == Boolean.TRUE ){
             result.managementAvailable = isManagementAvailable( serverNode );
         }
         return result;
     }
 
     private boolean isMachineReachable( ServerNode serverNode ) throws Exception
     {

         String publicIP = serverNode.getPublicIP();
         InetAddress byName = InetAddress.getByName( publicIP );
        return byName.isReachable( 5000 );
     }
 
 
     private boolean isManagementAvailable( ServerNode serverNode )
       {
           logger.info( "testing if management is available at : {}", serverNode );
 
           try {
               Response response = WS.url( "http://" + serverNode.getPublicIP() + ":8100/service/testrest" ).get().value().get();
               String body = response.body();
               logger.info( "got testrest result with body [{}] ", body );
               if ( org.apache.commons.lang3.StringUtils.isEmpty( body ) ) {
                   logger.info( "body is empty" );
                   return false;
               } else {
                   logger.info( "body is not empty, testing if status successful" );
                   JsonNode parse = Json.parse( body );
                   if ( parse.has( "status" ) ){
                       JsonNode nodeValue = parse.get("status");
                       String textValue = nodeValue == null ? "null" : nodeValue.getTextValue();
                       logger.info( "response has 'status' key which is equal to [{}]", textValue );
                       return "success".equals( textValue );
                   }
                   logger.info( "parsed json to [{}]", parse );
               }
 
           } catch ( Exception e ) {  // guy - don't ask me how but compiler does not pick the real exception up.. maybe because it is scala.
               logger.error( "unable to check if serverNode [{}] is up", serverNode, e );
           }
           return false;
       }
 
 
 	public void destroyServer( String serverId )
 	{
        logger.info("destroying server {}", serverId );
 	   deleteServer(serverId);
        ServerNode.deleteServer(serverId);
 	}
 
     public void init(){
         String cloudProvider = conf.server.bootstrap.cloudProvider;
         String username = conf.server.bootstrap.username;
         String apiKey = conf.server.bootstrap.apiKey;
         logger.info( "initializing bootstrapper with [cloudProvider, username, apiKey]=[{},{},{}]", new Object[]{cloudProvider, username, apiKey} );
         ContextBuilder contextBuilder = ContextBuilder.newBuilder( cloudProvider );
         contextBuilder.credentials( username, apiKey );
         ComputeServiceContext context = contextBuilder.buildView( ComputeServiceContext.class );
         _compute = context.getComputeService();
         _nova = context.unwrap();
     }
 
 
     public ServerApi getApi(){
         return _nova.getApi().getServerApiForZone(conf.server.bootstrap.zoneName);
     }
 
 
 	private ServerNode createServerNode() throws RunNodesException, TimeoutException
 	{
 		logger.info( "Starting to create new Server [imageId={}, flavorId={}]", conf.server.bootstrap.imageId, conf.server.bootstrap.flavorId );
 
 		ServerApi serverApi = getApi();
 		CreateServerOptions serverOpts = new CreateServerOptions();
 
         Map<String,String> metadata = new HashMap<String, String>();
 
         List<String> tags = new LinkedList<String>();
         String hostname = null;
         try {
             java.net.InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException e) {
             logger.debug("unable to get hostname",e);
         }
 
         if ( hostname != null ){
             tags.add( hostname );
         }
 
         if ( !StringUtils.isEmpty(conf.server.bootstrap.tags) ){
             tags.add( conf.server.bootstrap.tags );
         }
         metadata.put("tags", StringUtils.join(tags, ","));
         serverOpts.metadata(metadata);
 		serverOpts.keyPairName( conf.server.bootstrap.keyPair );
 		serverOpts.securityGroupNames(conf.server.bootstrap.securityGroup);
 
 		ServerCreated serverCreated = serverApi.create( conf.server.bootstrap.serverNamePrefix + System.currentTimeMillis(), conf.server.bootstrap.imageId , conf.server.bootstrap.flavorId, serverOpts);
 		blockUntilServerInState(serverCreated.getId(), Server.Status.ACTIVE, 1000, 5, serverApi);
 		Server server = serverApi.get(serverCreated.getId());
 
 		ServerNode serverNode = new ServerNode( server );
 
 		logger.info("Server created, wait 10 seconds before starting to bootstrap machine: {}" ,  serverNode.getPublicIP() );
 		Utils.threadSleep(10000); // need for a network interfaces initialization
 
 
         boolean bootstrapSuccess = false;
         Exception lastBootstrapException = null;
         for ( int i = 0; i < bootstrapRetries && !bootstrapSuccess ; i ++ ){
 		    // bootstrap machine: firewall, jvm, start cloudify
             logger.info( "bootstrapping machine try #[{}]", i );
 		    try{
                 bootstrapMachine( serverNode );
                 BootstrapValidationResult bootstrapValidationResult = validateBootstrap( serverNode );
                 if ( bootstrapValidationResult.getResult() ) {
                     bootstrapSuccess = true;
                 }else{
                     logger.info( "machine [{}] did not bootstrap successfully [{}] retrying", serverNode, bootstrapValidationResult );
                     logger.info( "rebuilding machine" );
                     try{
                         serverApi.rebuild( serverNode.getNodeId() );
                     }catch(RuntimeException e){
                         logger.error( "error while rebuilding machine [{}]", serverNode ,e );
                     }
                 }
             }catch(RuntimeException e){
                  lastBootstrapException = e;
             }
         }
 
         if ( !bootstrapSuccess ){
             logger.error( "unable to bootstrap machine", lastBootstrapException );
         }
 
 		logger.info("Server created.{} " , server.getAddresses() );
 
 		return serverNode;
 	}
 
 
 	@Override
 	public ServerNode bootstrapCloud( ServerNode serverNode )  {
 		File cloudFolder = null;
 		ComputeServiceContext jCloudsContext = null;
 		try{
             String username = serverNode.getUserName();
             String apiKey = serverNode.getApiKey();
             logger.info("Creating cloud folder with specific user credentials. User: " + username + ", api key: " + apiKey);
             jCloudsContext = CloudifyUtils.createJcloudsContext(username, apiKey);
             cloudFolder = CloudifyUtils.createCloudFolder(username, apiKey, jCloudsContext);
 
 			logger.info("Creating security group for user.");
 			CloudifyUtils.createCloudifySecurityGroup( jCloudsContext );
 
 			//Command line for bootstrapping remote cloud.
 			CommandLine cmdLine = new CommandLine(conf.server.cloudBootstrap.remoteBootstrap.getAbsoluteFile());
 			cmdLine.addArgument(cloudFolder.getName());
 
 			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
 			ProcExecutor bootstrapExecutor = executorFactory.getBootstrapExecutor( serverNode );
 
 			logger.info("Executing command line: " + cmdLine);
 			bootstrapExecutor.execute(cmdLine, ApplicationContext.get().conf().server.environment.getEnvironment() , resultHandler);
             logger.info("waiting for output");
 			resultHandler.waitFor();
             logger.info("finished waiting , exit value is [{}]", resultHandler.getExitValue() );
 
 
 			String output = Utils.getOrDefault(Utils.getCachedOutput(serverNode), "");
 			if (resultHandler.getException() != null) {
                 logger.info("we have exceptions, checking for known issues");
 				if (output.contains("found existing management machines")) {
                     logger.info("found 'found existing management machines' - issuing cloudify already exists message");
 					throw new ServerException( Messages.get("cloudify.already.exists") );
 				}
 				logger.info("Command execution ended with errors: {}", output);
 				throw new RuntimeException("Failed to bootstrap cloudify machine: "
 						+ output, resultHandler.getException());
 			}
 
             logger.info("finished handling errors, extracting IP");
 			String publicIp = Utils.extractIpFromBootstrapOutput(output);
 			if (StringUtils.isEmpty(publicIp)) {
 				logger.warn("No public ip address found in bootstrap output. " + output);
 				throw new RuntimeException( "Bootstrap failed. No IP address found in bootstrap output."
 						+ output, resultHandler.getException() );
 			}
             logger.info("ip is [{}], saving to serverNode", publicIp);
 
 			String privateKey = CloudifyUtils.getCloudPrivateKey(cloudFolder);
 			if (StringUtils.isEmpty(privateKey)) {
 				throw new RuntimeException( "Bootstrap failed. No pem file found in cloud directory." );
 			}
             logger.info("found PEM string");
 			logger.info("Bootstrap cloud command ended successfully");
 
             logger.info("updating server node with new info");
             serverNode.setPublicIP(publicIp);
 			serverNode.setPrivateKey(privateKey);
 			serverNode.setRemote(true);
             serverNode.save();
             logger.info("server node updated and saved");
 			return serverNode;
 		} catch(Exception e) {
 			throw new RuntimeException("Unable to bootstrap cloud", e);
 		} finally {
 			if (cloudFolder != null) {
 				FileUtils.deleteQuietly(cloudFolder);
 			}
 			if (jCloudsContext != null) {
 				jCloudsContext.close();
 			}
 			serverNode.setStopped(true);
 			
 		}
 	}
 
 	private void deleteServer( String serverId )
 	{
 		ServerApi serverApi = _nova.getApi().getServerApiForZone( conf.server.bootstrap.zoneName );
 		serverApi.delete(serverId);
 		logger.info("Server id: {} was deleted.", serverId);
 	}
 
 
 	/**
 	 * Will block until the server is in the correct state.
 	 *
 	 * @param serverId The id of the server to block on
 	 * @param status The status the server needs to reach before the method stops blocking
 	 * @param timeoutSeconds The maximum amount of time to block before throwing a TimeoutException
 	 * @param delaySeconds The amount of time between server status checks
 	 * @param serverApi The ServerApi used to do the checking
 	 *
 	 * @throws TimeoutException If the server does not reach the status by timeoutSeconds
 	 */
 	private void blockUntilServerInState(String serverId, Status status,
 			int timeoutSeconds, int delaySeconds, ServerApi serverApi)
 			throws TimeoutException
 	{
 		int totalSeconds = 0;
 
 		while (totalSeconds < timeoutSeconds)
 		{
 			logger.info("Waiting for a server activation... Left timeout: {} sec", timeoutSeconds - totalSeconds);
 
 			Server server = serverApi.get(serverId);
 
 			if (server.getStatus().equals(status))
 				return;
 
 			Utils.threadSleep(delaySeconds * 1000);
 
 			totalSeconds += delaySeconds;
 		}
 
 		String message = String.format("Timed out at %d seconds waiting for server %s to reach status %s.",
 						timeoutSeconds, serverId, status);
 
 		throw new TimeoutException(message);
 	}
 
 	private void bootstrapMachine( ServerNode server )
 	{
 		try
 		{
 			logger.info("Starting bootstrapping for server:{} " , server.getPublicIP() );
 
 			String script = FileUtils.readFileToString( conf.server.bootstrap.script );
 			ExecResponse response = runScriptOnNode( conf, server.getPublicIP(), script );
 
             logger.info("script finished");
 			logger.info("Bootstrap for server: {} finished successfully successfully. " +
                     "ExitStatus: {} \nOutput:  {}", new Object[]{server.getPublicIP(),
                     response.getExitStatus(),
                     response.getOutput()} );
 		}catch(Exception ex)
 		{
             try{
                 destroyServer( server.getNodeId() );
             }catch(Exception e){
                 logger.info("destroying server after failed bootstrap threw exception",e);
             }
 			throw new ServerException("Failed to bootstrap cloudify machine: " + server.toDebugString(), ex);
 		}
 	}
 
 	static public ExecResponse runScriptOnNode( Conf conf, String serverIP, String script)
 			throws NumberFormatException, IOException
 	{
 		logger.debug("Run ssh on server: {} script: {}" , serverIP, script );
         Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
 		SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
 		SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIP, conf.server.bootstrap.ssh.port ),
 				LoginCredentials.builder().user( conf.server.bootstrap.ssh.user )
 						.privateKey(Strings2.toStringAndClose(new FileInputStream( conf.server.bootstrap.ssh.privateKey ))).build());
         ExecResponse execResponse = null;
 		try
 		{
 			sshConnection.connect();
             logger.info("ssh connected, executing");
 			execResponse = sshConnection.exec(script);
             logger.info("finished execution");
 		 }finally
 		 {
 			if (sshConnection != null)
 			   sshConnection.disconnect();
 		 }
 
 		return execResponse;
 	}
 
 	/**
 	 * Always close your service when you're done with it.
 	 */
 	public void close()
 	{
          if (_compute != null)
 		{
 			_compute.getContext().close();
 		}
 	}
 
     public void setDeployManager(DeployManager deployManager) {
         this.deployManager = deployManager;
     }
 
     public void setConf( Conf conf )
     {
         this.conf = conf;
     }
 
     public void setRetries( int retries )
     {
         this.retries = retries;
     }
 
     public void setBootstrapRetries( int bootstrapRetries )
     {
         this.bootstrapRetries = bootstrapRetries;
     }
 }
