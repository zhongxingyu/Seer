 package test.cli.cloudify.cloud;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.jclouds.compute.ComputeServiceContext;
 import org.jclouds.compute.ComputeServiceContextFactory;
 import org.jclouds.compute.RunNodesException;
 import org.jclouds.compute.domain.ComputeMetadata;
 import org.jclouds.compute.domain.Hardware;
 import org.jclouds.compute.domain.NodeMetadata;
 import org.jclouds.compute.domain.NodeState;
 import org.jclouds.compute.domain.Template;
 import org.jclouds.compute.domain.TemplateBuilder;
 import org.jclouds.domain.Location;
 
 import test.cli.cloudify.cloud.services.CloudService;
 
 import com.google.common.base.Predicate;
 
 import framework.utils.LogUtils;
 
 /**
  * The class provides JClouds functionality. 
  * 
  * @author nirb
  *
  */
 public class JcloudsUtils {
 
 	private static ComputeServiceContext context = null;
 	private static String cloudName = null;
 	
 	/**
 	 * Creates context that offers the portable ComputeService api.
 	 * must be called first.
 	 * 
 	 * @param service - the cloud service
 	 */
 	public static void createContext(CloudService service){
 		
 		if(context != null)
 			context.close();
 		
 		cloudName = service.getCloudName();
 		
 		if(cloudName.equalsIgnoreCase("ec2"))
 			context = new ComputeServiceContextFactory().createContext("aws-ec2", service.getUser(), service.getApiKey());
 		else if(cloudName.equalsIgnoreCase("rsopenstack"))
 			context = new ComputeServiceContextFactory().createContext("cloudservers-us", service.getUser(), service.getApiKey());
 		else
 			LogUtils.log("Failed to create context: invalid cloud name.");
 	}
 	
 	/**
 	 * Runs a new node with the passed name.
 	 * @param serverName
 	 * @return
 	 * @throws RunNodesException
 	 */
 	public static NodeMetadata createServer(String serverName) throws RunNodesException{
 		Set<? extends NodeMetadata> nodes = createServers(serverName, 1);
 		if(nodes != null && !nodes.isEmpty())
 			return nodes.iterator().next();
 		return null;
 	}
 	/**
 	 * Runs a new node with the passed name and according to thr template.
 	 * @param serverName
 	 * @param template
 	 * @return
 	 * @throws RunNodesException
 	 */
 	public static NodeMetadata createServer(String serverName, Template template) throws RunNodesException{
 		Set<? extends NodeMetadata> nodes = createServers(serverName, 1, template);
 		if(nodes != null && !nodes.isEmpty())
 			return nodes.iterator().next();
 		return null;
 	}
 	
 	/**
 	 * Runs {@code count} new nodes with the passed name.
 	 * @param serverName
 	 * @param count
 	 * @return
 	 * @throws RunNodesException
 	 */
 	public static Set<? extends NodeMetadata> createServers(String serverName, int count) throws RunNodesException{
 		
 		Template template = null;
 		
 		if(cloudName.equalsIgnoreCase("ec2"))
 			template = getTemplateBuilder().imageId("us-east-1/ami-76f0061f").minRam(1600).hardwareId("m1.small").locationId("us-east-1").build();
 		else if(cloudName.equalsIgnoreCase("rsopenstack"))
 			template = getTemplateBuilder().imageId("118").minRam(1600).hardwareId("4").build();
 
 		return createServers(serverName, count, template);
 	}
 	
 	/**
 	 * Runs {@code count} nodes with the passed name and according to thr template.
 	 * @param serverName
 	 * @param count
 	 * @param template
 	 * @throws RunNodesException
 	 */
 	public static Set<? extends NodeMetadata> createServers(String serverName, int count, Template template) throws RunNodesException{
 		
 		Set<? extends NodeMetadata> nodes;
 		
 		if(template == null)
 			nodes = context.getComputeService().createNodesInGroup(serverName, count);
 		else
 			nodes = context.getComputeService().createNodesInGroup(serverName, count, template);
 		
 		if(nodes != null && !nodes.isEmpty()){
 			LogUtils.log("---Credential of each created node (in ec2: use it for .pem file):---");
 
 			for(NodeMetadata n : nodes){
 				LogUtils.log("Credential of node with id " + n.getId() + " and user " + n.getCredentials().getUser() + ":\n" + 
 						n.getCredentials().credential + "\n");
 			}
 			return nodes;
 		}
 		return null;
 	}
 	
 	public static void shutdownServer(String serverId){
 		context.getComputeService().destroyNode(serverId);
 	}
 	
 	/**
 	 * returns TemplateBuilder - can be used to define templates.
 	 * @return TemplateBuilder
 	 */
 	public static TemplateBuilder getTemplateBuilder(){
 		return context.getComputeService().templateBuilder();
 	}
 	
 	public static Set<? extends ComputeMetadata> getAllNodes(){
 		return context.getComputeService().listNodes();
 	}
 	
 	public static Set<? extends ComputeMetadata> getAllImages(){
 		return context.getComputeService().listImages();
 	}
 	/**
 	 * returns the hardwares of all running nodes.
 	 */
 	public static Set<Hardware> getHardwares(){
 		Set<? extends NodeMetadata> nodes = getAllRunningNodes();
 		Set<Hardware> hardwares = new HashSet<Hardware>();
 		for(NodeMetadata node : nodes){
 			hardwares.add(node.getHardware());
 		}
 		
 		return hardwares;
 	}
 	
 	public static ComputeServiceContext getContext() {
 		return context;
 	}
 
 	/**
 	 * returns the locations of all running nodes.
 	 */
 	public static Set<Location> getLocations(){
 		Set<? extends NodeMetadata> nodes = getAllRunningNodes();
 		Set<Location> locations = new HashSet<Location>();
 		for(NodeMetadata node : nodes){
 			locations.add(node.getLocation());
 		}
 		
 		return locations;		
 	}
 	
 	/**
 	 * returns all nodes which matches {@code filter}.
 	 * @param filter
 	 * @return
 	 */
 	public static Set<? extends NodeMetadata> getServers(final Predicate<ComputeMetadata> filter) {
 		return context.getComputeService().listNodesDetailsMatching(filter);
 	}
 	
 	public static Set<? extends NodeMetadata> getAllRunningNodes(){
 		return getServers(new Predicate<ComputeMetadata>() {
 
 			public boolean apply(final ComputeMetadata input) {
 				final NodeMetadata node = (NodeMetadata) input;
 				return (node.getState() == NodeState.RUNNING);
 			}
 
 		});
 	}
 	
 	/**
 	 * returns all nodes which CONTAINS the sub-string {@code serverName}.
 	 * @param serverName
 	 */
 	public static Set<? extends NodeMetadata> getServersByName(final String serverName){
 		Set<? extends NodeMetadata> matchingNodes = getServers(new Predicate<ComputeMetadata>() {
 			
 			public boolean apply(final ComputeMetadata input) {
 				final NodeMetadata node = (NodeMetadata) input;
 				return (node.getName().indexOf(serverName) >= 0);
 			}
 			
 		});
 		
 		if (matchingNodes == null || matchingNodes.isEmpty())
 			return null;
 		return matchingNodes;
 	}
 	
 	/**
 	 * closes the context. Should be called last to free resources.
 	 */
 	public static void closeContext(){
		context.close();
 	}
 	
 }
