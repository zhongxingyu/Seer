 package test.cli.cloudify.cloud.services.ec2;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.esc.jclouds.WindowsServerEC2ReviseParsedImage;
 import org.jclouds.aws.ec2.compute.strategy.AWSEC2ReviseParsedImage;
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.ComputeServiceContextFactory;
 import org.jclouds.compute.domain.ComputeMetadata;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.NodeState;
 import org.jclouds.logging.jdk.config.JDKLoggingModule;
 
 import test.cli.cloudify.cloud.services.AbstractCloudService;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Module;
 
 import framework.tools.SGTestHelper;
 import framework.utils.IOUtils;
 import framework.utils.LogUtils;
 
 public class Ec2CloudService extends AbstractCloudService {
 	
 	public Ec2CloudService() {
 		super("ec2");
 	}
 	
 	public Ec2CloudService(final String cloudName) {
 		super(cloudName);
 	}
 
 	public static final String DEFAULT_US_EAST_LINUX_AMI = "us-east-1/ami-76f0061f";
 	public static final String DEFAULT_US_EAST_UBUNTU_AMI = "us-east-1/ami-82fa58eb";
 	public static final String DEFAULT_EU_WEST_LINUX_AMI = "eu-west-1/ami-c37474b7";
 	public static final String DEFAULT_EU_WEST_UBUNTU_AMI = "eu-west-1/ami-c1aaabb5";
 	
 	public static final String DEFAULT_EC2_LINUX_AMI_USERNAME = "ec2-user";
 	public static final String DEFAULT_EC2_UBUNTU_AMI_USERNAME = "ubuntu";
 	
 	private String user = "AKIAI4OVPQZZQT53O6SQ";
 	private String apiKey = "xI/BDTPh0LE9PcC0aHhn5GEUh+/hjOiRcKwCNVP5";
 	private String keyPair = "ec2-sgtest";
 	private ComputeServiceContext context;
 	
 	public void setUser(final String user) {
 		this.user = user;
 	}
 
 	@Override
 	public String getUser() {
 		return user;
 	}
 
 	public void setApiKey(final String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	@Override
 	public String getApiKey() {
 		return apiKey;
 	}
 	
 	public String getRegion() {
 		return System.getProperty("ec2.region" , "eu");
 	}
 	
 	public void setRegion(final String region) {
 		System.setProperty("ec2.region", region);
 	}
 
 	public void setKeyPair(final String keyPair) {
 		this.keyPair = keyPair;
 	}
 
 	public String getKeyPair() {
 		return keyPair;
 	}
 
 	private ComputeServiceContext createContext() {
 		final Set<Module> wiring = new HashSet<Module>();
 		wiring.add(new AbstractModule() {
 
 			@Override
 			protected void configure() {
 				bind(
 						AWSEC2ReviseParsedImage.class).to(
 						WindowsServerEC2ReviseParsedImage.class);
 			}
 
 		});
 
 		// Enable logging using gs_logging
 		wiring.add(new JDKLoggingModule());
 
 		Properties overrides = new Properties();
 		overrides.put("jclouds.ec2.ami-query", "");
 		overrides.put("jclouds.ec2.cc-ami-query", "");
 
 		final String provider = getCloud().getProvider().getProvider();
 		final String user = getCloud().getUser().getUser();
 		final String key = getCloud().getUser().getApiKey();
 		LogUtils.log("Creating jclouds context, this may take a few seconds");
 		return new ComputeServiceContextFactory().createContext(
 				provider, user, key, wiring, overrides);
 	}
 
 	@Override
 	public boolean scanLeakedAgentAndManagementNodes() {
 
 		if (this.context == null) {
 			this.context = createContext();
 		}
 		
 		final String agentPrefix = getCloud().getProvider().getMachineNamePrefix();
 		final String managerPrefix = getCloud().getProvider().getManagementGroup();
 
 		final List<ComputeMetadata> leakedNodes = checkForLeakedNodesWithPrefix(agentPrefix, managerPrefix);
 
 		return leakedNodes.isEmpty();
 
 	}
 
 	@Override
 	public void teardownCloud() throws IOException, InterruptedException {
 		super.teardownCloud();
 		LogUtils.log("Closing ec2 ComputeServiceContext");
 		this.context.close();
 		this.context = null;
 	}
 
 	@Override
 	public void injectCloudAuthenticationDetails()
 			throws IOException {
 		
 		getProperties().put("user", this.user);
 		getProperties().put("apiKey", this.apiKey);
 
 		final Map<String, String> propsToReplace = new HashMap<String, String>();
 		
 		if (getRegion().contains("eu")) {
 			LogUtils.log("Working in eu region");
 			getProperties().put("locationId", "eu-west-1");
 			setKeyPair("sgtest-eu");
 			if (!getCloudName().contains("win")) {
 				getProperties().put("linuxImageId", DEFAULT_EU_WEST_LINUX_AMI);
 				getProperties().put("ubuntuImageId", DEFAULT_EU_WEST_UBUNTU_AMI);
 				getProperties().put("hardwareId", "m1.small");
 			}
		} else {
			getProperties().put("locationId", "us-east-1");
			if (!getCloudName().contains("win")) {
				getProperties().put("linuxImageId", DEFAULT_US_EAST_LINUX_AMI);
				getProperties().put("ubuntuImageId", DEFAULT_US_EAST_UBUNTU_AMI);
				getProperties().put("hardwareId", "m1.small");
			}

 		}
 		getProperties().put("keyPair", this.keyPair);
 		getProperties().put("keyFile", this.keyPair + ".pem");
 		
 		propsToReplace.put("cloudify_agent_", getMachinePrefix() + "cloudify-agent");
 		propsToReplace.put("cloudify_manager", getMachinePrefix() + "cloudify-manager");
 		propsToReplace.put("numberOfManagementMachines 1", "numberOfManagementMachines " + getNumberOfManagementMachines());
 
 		IOUtils.replaceTextInFile(getPathToCloudGroovy(), propsToReplace);
 
 		// add a pem file
 		final String sshKeyPemName = this.keyPair + ".pem";
 		final File fileToCopy =
 				new File(SGTestHelper.getSGTestRootDir() + "/apps/cloudify/cloud/" + getCloudName() + "/"
 						+ sshKeyPemName);
 		final File targetLocation = new File(getPathToCloudFolder() + "/upload/");
 		FileUtils.copyFileToDirectory(fileToCopy, targetLocation);		
 	}
 
 	@Override
 	public boolean scanLeakedAgentNodes() {
 		
 		if (this.context == null) {
 			this.context = createContext();
 		}
 		
 		final String agentPrefix = getCloud().getProvider().getMachineNamePrefix();
 
 		final List<ComputeMetadata> leakedNodes = checkForLeakedNodesWithPrefix(agentPrefix);
 
 		return leakedNodes.isEmpty();
 
 	}
 
 	private List<ComputeMetadata> checkForLeakedNodesWithPrefix(final String... prefixes) {
 		List<ComputeMetadata> leakedNodes = null;
 		int iteration = 0;
 		while (iteration < 3) {
 			leakedNodes = new LinkedList<ComputeMetadata>();
 			LogUtils.log("Checking for leaked nodes with prefix: " + Arrays.toString(prefixes));
 			final Set<? extends ComputeMetadata> allNodes = this.context.getComputeService().listNodes();
 			
 			boolean foundPendingNodes = scanNodes(leakedNodes, allNodes,
 					prefixes);
 
 			if (!foundPendingNodes) {
 				break; // no need to re-run the scan
 			}
 			try {
 				LogUtils.log("Sleeping for several seconds to give pending nodes time to reach their new state");
 				Thread.sleep(10000);
 			} catch (InterruptedException e) {
 				// ignore.
 			}
 			++iteration;
 		}
 
 		if (leakedNodes.size() > 0) {
 			LogUtils.log("Killing leaked EC2 nodes");
 			for (final ComputeMetadata leakedNode : leakedNodes) {
 
 				LogUtils.log("Killing node: " + leakedNode.getName() + ": " + leakedNode);
 				try {
 					this.context.getComputeService().destroyNode(leakedNode.getId());
 				} catch (final Exception e) {
 					LogUtils.log("Failed to kill a leaked node. This machine may be leaking!", e);
 				}
 			}
 
 		}
 		return leakedNodes;
 	}
 
 	private boolean scanNodes(final List<ComputeMetadata> leakedNodes,
 			final Set<? extends ComputeMetadata> allNodes, final String... prefixes) {
 		boolean foundPendingNodes = false;
 		for (final ComputeMetadata computeMetadata : allNodes) {
 			final String name = computeMetadata.getName();
 			final NodeState state = ((NodeMetadata) computeMetadata).getState();
 			switch (state) {
 			case TERMINATED:
 				// ignore - node is shut down
 				break;
 			case PENDING:
 			case ERROR:
 			case RUNNING:
 			case UNRECOGNIZED:
 			case SUSPENDED:
 			default:
 				if (name == null || name.length() == 0) {						
 					LogUtils.log("WARNING! Found a non-terminated node with an empty name. " 
 							+ "The test can't tell if this is a leaked node or not! Node details: "
 							+ computeMetadata);						
 				} else {
 					for (String prefix : prefixes) {
 						if (name.startsWith(prefix)) {
 							if (state.equals(NodeState.PENDING)) {
 								// node is transitioning from one state to another 
 								// 		- might be shutting down, might be starting up!
 								// Must try again later.
 								foundPendingNodes = true;
 								LogUtils.log("While scanning for leaked nodes, found a node in state PENDING. " 
 										+ "Leak scan will be retried. Node in state pending was: "
 										+ computeMetadata);
 							} else {
 								LogUtils.log("ERROR: Found a leaking node: " + computeMetadata);
 								leakedNodes.add(computeMetadata);
 							}
 						}
 					}
 
 				}
 
 				break;
 			}
 		}
 		return foundPendingNodes;
 	}
 
 }
