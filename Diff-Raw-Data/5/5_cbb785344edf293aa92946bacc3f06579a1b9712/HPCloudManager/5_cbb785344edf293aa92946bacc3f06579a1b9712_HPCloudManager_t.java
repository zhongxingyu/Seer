 /**
  * @author Alexandre Leites
  */
 package n3phele.factory.hpcloud;
 
 import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.jclouds.ContextBuilder;
 import org.jclouds.compute.ComputeService;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.domain.ComputeMetadata;
 import org.jclouds.compute.domain.Hardware;
 import org.jclouds.compute.domain.Image;
 import org.jclouds.domain.Location;
 import org.jclouds.gae.config.AsyncGoogleAppEngineConfigurationModule;
 import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
 import org.jclouds.openstack.nova.v2_0.NovaApi;
 import org.jclouds.openstack.nova.v2_0.domain.Ingress;
 import org.jclouds.openstack.nova.v2_0.domain.IpProtocol;
 import org.jclouds.openstack.nova.v2_0.domain.KeyPair;
 import org.jclouds.openstack.nova.v2_0.domain.RebootType;
 import org.jclouds.openstack.nova.v2_0.domain.SecurityGroup;
 import org.jclouds.openstack.nova.v2_0.domain.Server;
 import org.jclouds.openstack.nova.v2_0.domain.ServerCreated;
 import org.jclouds.openstack.nova.v2_0.extensions.KeyPairApi;
 import org.jclouds.openstack.nova.v2_0.extensions.SecurityGroupApi;
 import org.jclouds.openstack.nova.v2_0.features.ServerApi;
 import org.jclouds.openstack.nova.v2_0.options.CreateServerOptions;
 import org.jclouds.rest.RestContext;
 
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.ImmutableSet;
 import com.google.inject.Module;
 
 /**
  * @author Alexandre Leites
  * 
  */
 public class HPCloudManager {
 	private ComputeService	mCompute;
 	private RestContext		mNova;
 	private NovaApi			mNovaApi;
 
 	public static String	JCLOUD_PROVIDER	= "hpcloud-compute";
 
 	/**
 	 * @param creds
 	 *            HP Cloud credentials
 	 */
 	public HPCloudManager(HPCloudCredentials creds)
 	{
 		initComputeService(creds.getIdentity(), creds.getSecretKey());
 	}
 
 	/**
 	 * @param identity
 	 *            A join of TenantName and AccessKey with a ":" between them.
 	 * @param secretKey
 	 *            A key associated with AccessKey provided in identity
 	 *            parameter.
 	 */
 	private void initComputeService(String identity, String secretKey)
 	{
 		Properties properties = new Properties();
 		long scriptTimeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
 		properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");
 		properties.setProperty("jclouds.modules","org.jclouds.gae.config.AsyncGoogleAppEngineConfigurationModule");
 
 		Iterable<Module> modules = ImmutableSet.<Module> of(new SLF4JLoggingModule(), new AsyncGoogleAppEngineConfigurationModule());
 
 		ContextBuilder builder = ContextBuilder.newBuilder(JCLOUD_PROVIDER).credentials(identity, secretKey).modules(modules).overrides(properties);
 
 		ComputeServiceContext context = builder.buildView(ComputeServiceContext.class);
 
 		mCompute = context.getComputeService();
 		mNova = context.unwrap();
 		mNovaApi = (NovaApi) mNova.getApi();
 	}
 
 	/**
 	 * @return list of available images into HP Cloud provider. This includes
 	 *         user custom images too.
 	 */
 	public Set<? extends Image> listImages()
 	{
 		return mCompute.listImages();
 	}
 
 	/**
 	 * @return list of available hardware profiles (flavors) into HP Cloud
 	 *         provider.
 	 */
 	public Set<? extends Hardware> listHardwareProfiles()
 	{
 		return mCompute.listHardwareProfiles();
 	}
 
 	/**
 	 * @return list of available locations into HP Cloud provider.
 	 */
 	public Set<? extends Location> listLocations()
 	{
 		return mCompute.listAssignableLocations();
 	}
 
 	/**
 	 * @return list of user nodes (servers).
 	 */
 	public Set<? extends ComputeMetadata> listNodes()
 	{
 		return mCompute.listNodes();
 	}
 
 	/**
 	 * @param nodeId
 	 *            our node identification.
 	 */
 	public void suspendNode(String nodeId)
 	{
 		mCompute.suspendNode(nodeId);
 	}
 
 	/**
 	 * @param nodeId
 	 *            our node identification.
 	 */
 	public void resumeNode(String nodeId)
 	{
 		mCompute.resumeNode(nodeId);
 	}
 	
 	/**
 	 * @param zone
 	 * @param nodeId our node identification.
 	 * @param rebootType 
 	 */
 	public void rebootNode(String zone, String nodeId, RebootType rebootType)
 	{
 		/**
 		 * Get server async api
 		 */
 		ServerApi serverApi = mNovaApi.getServerApiForZone(zone);
 		
 		serverApi.reboot(nodeId, rebootType);
 	}
 
 	/**
 	 * @param zone 
 	 * @param nodeId our node identification.
 	 */
	public boolean terminateNode(String zone, String nodeId)
 	{
 		/**
 		 * Get server async api
 		 */
 		ServerApi serverApi = mNovaApi.getServerApiForZone(zone);
 		
		return serverApi.delete(nodeId);
 	}
 	
 	/**
 	 * 
 	 * @param zone Compute zone
 	 * @param Id Server Id
 	 * @return Server object
 	 */
 	public Server getServerById(String zone, String Id)
 	{
 		/**
 		 * Get server async api
 		 */
 		ServerApi serverApi = mNovaApi.getServerApiForZone(zone);
 		
 		return serverApi.get(Id);
 	}
 
 	/**
 	 * @param r
 	 *            Represents our creation request
 	 * @return a list of created nodes.
 	 */
 	public List<ServerCreated> createServerRequest(HPCloudCreateServerRequest r)
 	{
 		/**
 		 * Get server async api
 		 */
 		ServerApi serverApi = mNovaApi.getServerApiForZone(r.locationId);
 		
 		/**
 		 * Create our security group with following ports opened: TCP: 22, 8887
 		 * UDP: None ICMP: Yes
 		 */
 		SecurityGroup secGroup = createSecurityGroup(r.securityGroup, r.locationId);
 		
 		/**
 		 * Create our keypair. Return existent keypair if already exists.
 		 */
 		KeyPair keyPair = createKeyPair(r.keyPair, r.locationId);
 		
 		/**
 		 * Build our server creation options.
 		 */
 		CreateServerOptions options = new CreateServerOptions();
 		options.securityGroupNames(secGroup.getName());
 		options.keyPairName(keyPair.getName());
 		
 		/**
 		 * Custom commands
 		 */
 		if( r.userData.length() > 0 )
 			options.userData(r.userData.getBytes());
 		
 		/**
 		 * Send our requests to HPCloud
 		 */
 		ArrayList<ServerCreated> serversList = new ArrayList<ServerCreated>();
 		
 		for(int i=0; i < r.nodeCount; i++)
 		{
 			String name = r.serverName;
 			//String name = "n3phele-" + r.serverName;
 			if( r.nodeCount > 1 )
 				name = name.concat("-" + String.valueOf(i));
 			
 			ServerCreated server = serverApi.create(name, r.imageId, r.hardwareId, options);
 			serversList.add(server);
 			
 			//HPCloudServer hpsrv = new HPCloudServer(server.getId(), server.getName());
 			//serversList.add(hpsrv);
 		}
 
 		return (serversList.size() > 0) ? serversList : null;
 	}
 
 	public SecurityGroup createSecurityGroup(String name, String zone)
 	{
 		SecurityGroup sg = null;
 		SecurityGroupApi sgApi = mNovaApi.getSecurityGroupExtensionForZone(zone).get();
 		String groupName = "n3phele-" + name;
 
 		try
 		{
 			sg = sgApi.createWithDescription(groupName, "Created by n3phele.");
 
 			/**
 			 * External rules
 			 */
 			sgApi.createRuleAllowingCidrBlock(sg.getId(), Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(22).toPort(22).build(), "0.0.0.0/0");
 			sgApi.createRuleAllowingCidrBlock(sg.getId(), Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(8887).toPort(8887).build(), "0.0.0.0/0");
 			sgApi.createRuleAllowingCidrBlock(sg.getId(), Ingress.builder().ipProtocol(IpProtocol.ICMP).fromPort(-1).toPort(-1).build(), "0.0.0.0/0");
 
 			/**
 			 * Internal rules. Allowing nodes access each other. TODO: It
 			 * doesn't allow string, just integer, needs to verify how to do
 			 */
 			/*
 			 * sgApi.createRuleAllowingSecurityGroupId(sg.getId(), Ingress.builder().ipProtocol(IpProtocol.TCP).fromPort(1).toPort(65535).build(), groupName);
 			 * sgApi.createRuleAllowingSecurityGroupId(sg.getId(), Ingress.builder().ipProtocol(IpProtocol.UDP).fromPort(1).toPort(65535).build(), groupName);
 			 * sgApi.createRuleAllowingSecurityGroupId(sg.getId(), Ingress.builder().ipProtocol(IpProtocol.ICMP).fromPort(-1).toPort(-1).build(), groupName);
 			 */
 		} catch (Exception e)
 		{
 			// TODO: What us are expected to do here?
 			FluentIterable<? extends SecurityGroup> groupList = sgApi.list();
 			for (SecurityGroup sg2 : groupList)
 			{
 				if (sg2.getName().equals(groupName))
 					return sg2;
 			}
 		}
 
 		return sg;
 	}
 
 	/**
 	 * Create a KeyPair with desired name
 	 * 
 	 * @param name KeyPair name
 	 * @param zone KeyPair zone
 	 * @return KeyPair
 	 */
 	public KeyPair createKeyPair(String name, String zone)
 	{
 		KeyPairApi kpApi = mNovaApi.getKeyPairExtensionForZone(zone).get();
 		String kpName = "n3phele-" + name;
 		KeyPair kp = null;
 
 		try
 		{
 			kp = kpApi.create(kpName);
 		} catch (Exception e)
 		{
 			FluentIterable<? extends KeyPair> kpList = kpApi.list();
 			for (KeyPair kp2 : kpList)
 			{
 				if(kp2.getName().equals(kpName))
 					return kp2;
 			}
 		}
 
 		return kp;
 	}
 }
