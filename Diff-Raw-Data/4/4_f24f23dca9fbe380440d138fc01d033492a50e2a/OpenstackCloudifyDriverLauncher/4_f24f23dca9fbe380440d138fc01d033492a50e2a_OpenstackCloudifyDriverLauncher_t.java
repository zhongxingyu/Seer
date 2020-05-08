 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.openstack;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.domain.Service;
 import org.cloudifysource.domain.cloud.Cloud;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.esc.driver.provisioning.ComputeDriverConfiguration;
 import org.cloudifysource.esc.driver.provisioning.MachineDetails;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackCloudifyDriver;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackComputeClient;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackNetworkClient;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenStackResourcePrefixes;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackException;
 import org.cloudifysource.esc.driver.provisioning.openstack.OpenstackJsonSerializationException;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.FloatingIp;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Network;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.NovaServer;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Port;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.RouteFixedIp;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Router;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.SecurityGroup;
 import org.cloudifysource.esc.driver.provisioning.openstack.rest.Subnet;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.openstack.OpenstackService;
 
 /**
  * This class help to test the driver {@link OpenStackCloudifyDriver} without actually performing a cloudify bootstrap.<br />
  * It helps to test the network DSL configuration vs Openstack platform.
  * 
  * @author victor
  * 
  */
 public class OpenstackCloudifyDriverLauncher {
 
     static final String TEMPLATE_APPLICATION_NET = "APPLICATION_NET";
     static final String TEMPLATE_APPLICATION_NET2 = "APPLICATION_NET2";
     static final String COMPUTE_SOME_INTERNAL_NETWORK_1 = "SOME_INTERNAL_NETWORK_1";
     static final String COMPUTE_SOME_INTERNAL_NETWORK_2 = "SOME_INTERNAL_NETWORK_2";
 
     static final String DEFAULT_TEMPLATE = "LINUX";
     static final String DEFAULT_APPLICATION_NAME = "default";
 
     private static final String SERVICES_RELATIVE_PATH = "src/main/resources/apps/USM/usm/networks/openstack/";
 
     private static final String TEST_PREFIX = "openstack-test-itest-os-";
 
     private static long END_TIME = System.currentTimeMillis() + 1000L * 60l * 15l; // 15min
 
     private OpenStackComputeClient computeApi;
     private OpenStackNetworkClient networkApi;
 
     private Boolean skipExternalNetworking;
 
     /**
      * Create supposing existing networks.
      */
     public void createExpectedExistingNetworks() throws OpenstackException {
         createExistingNetwork(COMPUTE_SOME_INTERNAL_NETWORK_1, 1, 1); // 151.0.0.0/24
         createExistingNetwork(COMPUTE_SOME_INTERNAL_NETWORK_2, 2, 1); // 152.0.0.0/24 and 152.1.0.0/24
     }
 
     private void createExistingNetwork(String networkName, int networkIndex, int nbSubnet) throws OpenstackException {
         Network network = new Network();
         network.setName(networkName);
         String networkId = networkApi.createNetworkIfNotExists(network).getId();
 
         for (int i = 0; i < nbSubnet; i++) {
             Subnet requestSubnet = new Subnet();
             requestSubnet.setName(networkName + "_subnet_" + i);
             requestSubnet.setCidr("15" + networkIndex + "." + i + ".0.0/24");
             requestSubnet.setIpVersion("4");
             requestSubnet.setNetworkId(networkId);
             networkApi.createSubnet(requestSubnet);
         }
 
     }
 
     /**
      * Clean supposing existing networks
      */
     public void cleanExpectedExistingNetworks() throws OpenstackException {
         String[] existingNetworkNames = { COMPUTE_SOME_INTERNAL_NETWORK_1, COMPUTE_SOME_INTERNAL_NETWORK_2 };
         for (String name : existingNetworkNames) {
             networkApi.deleteNetworkByName(name);
         }
     }
 
     /**
      * Configure the cloud DSL.<br />
      * <ul>
      * <li>It create a copy of $CLOUDIFY_HOME/clouds/openstack.</li>
      * <li>It will copy the file src/main/resources/custom-cloud-configs/openstack/#overrideCloud#/openstack-cloud.groovy into
      * $CLOUDIFY_HOME/clouds/copy_of_openstack.</li>
      * <li>Override openstack-cloud.properties.</li>
      * <li>Read the cloud file from $CLOUDIFY_HOME/clouds/copy_of_openstack/openstack-cloud.properties.</li>
      * <li>Clean existing tests resources in Openstack.</li>
      * <li>Return the cloud object.</li>
      *
      * @param overrideCloud
      *            The relative path to the cloud groovy file.
      * @param additionalProps
      *            Additional properties to replace in the cloud driver dsl.
      * @return The cloud object read from $CLOUDIFY_HOME/clouds/copy_of_openstack/openstack-cloud.properties
      * @exception Exception
      *                When error occurs.
      */
     public Cloud createCloud(String overrideCloud, Map<String,String> additionalProps) throws Exception {
         OpenstackService service = this.createOpenstackService(overrideCloud, additionalProps);
         Cloud cloud = ServiceReader.readCloudFromDirectory(service.getPathToCloudFolder());
         this.computeApi = this.createComputeClient(service);
         this.networkApi = this.createNetworkClient(service);
         this.cleanExpectedExistingNetworks();
         this.cleanOpenstackResources(cloud);
         return cloud;
     }
 
     public Cloud createCloud(String overrideCloud) throws Exception {
         return createCloud(overrideCloud, null);
     }
 
     private OpenstackService createOpenstackService(String overrideCloud) throws Exception, IOException {
         return createOpenstackService(overrideCloud, null);
     }
 
     private OpenstackService createOpenstackService(String overrideCloud, Map<String,String> additionalProps) throws Exception, IOException {
         final OpenstackService service = new OpenstackService();
         service.setMachinePrefix(TEST_PREFIX);
         service.init(this.getClass().getSimpleName());
         if (overrideCloud != null) {
             File customCloudFile = new File(SGTestHelper.getCustomCloudConfigDir(service.getCloudName()) + overrideCloud);
             File originalCloudGroovy = new File(service.getPathToCloudGroovy());
             LogUtils.log("replacing file: " + originalCloudGroovy + " with " + customCloudFile);
             IOUtils.replaceFile(originalCloudGroovy, customCloudFile);
             new File(service.getPathToCloudFolder(), customCloudFile.getName())
                     .renameTo(new File(service.getPathToCloudFolder(), originalCloudGroovy.getName()));
         }
 
         if(additionalProps != null){
             service.getAdditionalPropsToReplace().putAll(additionalProps);
         }
 
         service.injectCloudAuthenticationDetails();
         this.addOverrideProps(service);
         IOUtils.replaceTextInFile(service.getPathToCloudGroovy(), service.getAdditionalPropsToReplace());
         IOUtils.writePropertiesToFile(service.getProperties(), new File(service.getPathToCloudFolder() + "/" + service.getCloudName() + "-cloud.properties"));
         return service;
     }
 
     private void addOverrideProps(final OpenstackService service) {
         if (this.skipExternalNetworking != null) {
             String oldValue = "_SKIP_EXTERNAL_NETWORKING_,";
             String newValue = "\"skipExternalNetworking\" : \"" + this.skipExternalNetworking + "\",";
             service.getAdditionalPropsToReplace().put(oldValue, newValue);
         } else {
             String oldValue = "_SKIP_EXTERNAL_NETWORKING_,";
             service.getAdditionalPropsToReplace().put(oldValue, "");
         }
     }
 
     private OpenStackNetworkClient createNetworkClient(final OpenstackService service) throws OpenstackJsonSerializationException {
         final String imageId = service.getCloudProperty(OpenstackService.IMAGE_PROP);
         final String region = imageId.split("/")[0];
         OpenStackNetworkClient networkApi = new OpenStackNetworkClient(
                 service.getCloudProperty(OpenstackService.ENDPOINT_PROP),
                 service.getCloudProperty(OpenstackService.USER_PROP),
                 service.getCloudProperty(OpenstackService.API_KEY_PROP),
                 service.getCloudProperty(OpenstackService.TENANT_PROP),
                 region);
         return networkApi;
     }
 
     private OpenStackComputeClient createComputeClient(final OpenstackService service) throws OpenstackJsonSerializationException {
         final String imageId = service.getCloudProperty(OpenstackService.IMAGE_PROP);
         final String region = imageId.split("/")[0];
         OpenStackComputeClient computeApi = new OpenStackComputeClient(
                 service.getCloudProperty(OpenstackService.ENDPOINT_PROP),
                 service.getCloudProperty(OpenstackService.USER_PROP),
                 service.getCloudProperty(OpenstackService.API_KEY_PROP),
                 service.getCloudProperty(OpenstackService.TENANT_PROP),
                 region);
         return computeApi;
     }
 
     /**
      * Remove all tests resources from Openstack environment.<br />
      * It uses prefix name to delete the resources.
      */
     public void cleanOpenstackResources(Cloud cloud) throws Exception {
         List<NovaServer> servers = computeApi.getServers();
         Set<String> remainingServerIds = new HashSet<String>();
 
         if (servers != null) {
             // Clean remaining servers
             for (NovaServer novaServer : servers) {
                 String svName = novaServer.getName();
                 if (svName != null && svName.startsWith(TEST_PREFIX)) {
 
                     // Release existing floatingips
                     List<Port> ports = networkApi.getPortsByDeviceId(novaServer.getId());
                     for (Port port : ports) {
                         FloatingIp floatingip = networkApi.getFloatingIpByPortId(port.getId());
                         if (floatingip != null) {
                             networkApi.releaseFloatingIp(floatingip.getId());
                         }
                     }
 
                     computeApi.deleteServer(novaServer.getId());
                     remainingServerIds.add(novaServer.getId());
                 }
             }
         }
 
         // Wait until all servers has been shutdown
         boolean wait = true;
         while (wait) {
             wait = false;
             for (String id : remainingServerIds) {
                 NovaServer sv = computeApi.getServerDetails(id);
                 if (sv != null) {
                     Thread.sleep(5000L);
                     wait = true;
                 }
             }
         }
 
         // Clean remaining secgroups
         List<SecurityGroup> secgroups = networkApi.getSecurityGroupsByPrefix(TEST_PREFIX);
         for (SecurityGroup secgroup : secgroups) {
             networkApi.deleteSecurityGroup(secgroup.getId());
         }
 
         // Clean remaining routers
         List<Router> routers = networkApi.getRouters();
 
         String mngRouterName = "";
         if(cloud != null){
             mngRouterName = this.getManagementRouterNameFromDsl(cloud);
         }
 
         for (Router router : routers) {
             String rName = router.getName();
             if (rName != null && (rName.startsWith(TEST_PREFIX) || rName.equals(mngRouterName))) {
                 List<Port> ports = networkApi.getPortsByDeviceId(router.getId());
                 List<Network> relevantNetworks = networkApi.getNetworkByPrefix(TEST_PREFIX);
                 for (Port port : ports) {
                     for (RouteFixedIp route : port.getFixedIps()) {
                         for(Network network : relevantNetworks){
 
                             // disconnect from the main router only interfaces that connect to relevant testing networks.
                             for(String subnetId : network.getSubnets()){
                                 if(subnetId.equals(route.getSubnetId())){
                                     networkApi.deleteRouterInterface(router.getId(), route.getSubnetId());
                                 }
                             }
                         }
                     }
                 }
                 if(!rName.equals(mngRouterName)){
                     networkApi.deleteRouter(router.getId());
                 }
             }
         }
 
         // Clean remaining networks
         List<Network> networks = networkApi.getNetworkByPrefix(TEST_PREFIX);
         for (Network network : networks) {
             networkApi.deleteNetwork(network.getId());
         }
     }
 
     public Service getService(String relativeServicePath) throws PackagingException {
         File serviceFile = new File(SERVICES_RELATIVE_PATH, relativeServicePath);
         final Service service = ServiceReader.getServiceFromFile(serviceFile);
         return service;
     }
 
     /**
      * Start a management VM using
      * {@link OpenStackCloudifyDriver#startManagementMachines(org.cloudifysource.esc.driver.provisioning.ManagementProvisioningContext, long, TimeUnit)} using
      * the given cloud object.<br />
      * It will also make assertions to verify that management security groups are successfully created.
      *
      * @param cloud
      *            The cloud object.
      * @return Management machine details.
      * @throws Exception
      */
     public MachineDetails[] startManagementMachines(Cloud cloud) throws Exception {
         final ComputeDriverConfiguration configuration = new ComputeDriverConfiguration();
         configuration.setCloud(cloud);
         configuration.setManagement(true);
 
         OpenStackCloudifyDriver driver = new OpenStackCloudifyDriver();
         driver.setConfig(configuration);
         MachineDetails[] mds = driver.startManagementMachines(null, END_TIME, TimeUnit.MINUTES);
         this.assertManagementSecurityGroups(cloud);
         this.assertManagementAttachedSecurityGroups(mds, cloud.getProvider().getManagementGroup());
         return mds;
     }
 
     /**
      * Assert that cluster, agent and management exist.
      *
      * @param cloud
      * @throws OpenstackException
      */
     private void assertManagementSecurityGroups(Cloud cloud) throws OpenstackException {
         List<SecurityGroup> secgroups = networkApi.getSecurityGroupsByPrefix(TEST_PREFIX);
         AssertUtils.assertNotNull("Management security groups are missing", secgroups);
         AssertUtils.assertEquals("When bootstrapping the driver must create 3 secgroups. Actual: " + secgroups, 3, secgroups.size());
 
         Set<String> names = new HashSet<String>(3);
         for (SecurityGroup secgroup : secgroups) {
             names.add(secgroup.getName());
         }
 
         OpenStackResourcePrefixes secgroupnames = new OpenStackResourcePrefixes(cloud.getProvider().getManagementGroup(), null, null);
         AssertUtils.assertTrue("Management security group not found. Got: " + names, names.contains(secgroupnames.getManagementName()));
         AssertUtils.assertTrue("Agent security group not found. Got: " + names, names.contains(secgroupnames.getAgentName()));
         AssertUtils.assertTrue("Cluster security group not found. Got: " + names, names.contains(secgroupnames.getClusterName()));
 
         SecurityGroup mngSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getManagementName());
         AssertUtils.assertNotNull("Missing management security rules", mngSecgroup.getSecurityGroupRules());
         AssertUtils.assertTrue("Missing management security rules", mngSecgroup.getSecurityGroupRules().length > 2);
 
         SecurityGroup agentSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getAgentName());
         AssertUtils.assertNotNull("Missing agent security rules", agentSecgroup.getSecurityGroupRules());
         AssertUtils.assertTrue("Missing agent security rules", agentSecgroup.getSecurityGroupRules().length > 2);
     }
 
     private void assertManagementAttachedSecurityGroups(MachineDetails[] mds, String managementGroup) throws OpenstackException {
 
         OpenStackResourcePrefixes secgroupnames = new OpenStackResourcePrefixes(managementGroup, null, null);
         SecurityGroup mngSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getManagementName());
         SecurityGroup clusterSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getClusterName());
 
         for (MachineDetails md : mds) {
             List<Port> ports = networkApi.getPortsByDeviceId(md.getMachineId());
             AssertUtils.assertNotNull("Cannot find a ports on management VM '" + md.getMachineId() + "'", ports);
             for (Port port : ports) {
                 Network network = networkApi.getNetwork(port.getNetworkId());
                 Set<String> securityGroups = port.getSecurityGroups();
                 AssertUtils.assertTrue("Management security group not found on port attached to network '" + network.getName() + "'",
                         securityGroups.contains(mngSecgroup.getId()));
                 AssertUtils.assertTrue("Cluster security group not found on port attached to network '" + network.getName() + "'",
                         securityGroups.contains(clusterSecgroup.getId()));
             }
         }
     }
 
     /**
      * Stop a management VM using {@link OpenStackCloudifyDriver#stopMachine(String, long, TimeUnit)} using the given cloud object.<br />
      * It will also make assertions to verify that all Openstack resources has been removed.
      *
      * @param cloud
      *            The cloud object.
      * @throws Exception
      */
     public void stopManagementMachines(Cloud cloud) throws Exception {
         // Retrieve floatingips for assertion.
         Set<String> floatingipids = new HashSet<String>();
         List<NovaServer> servers = computeApi.getServers();
         for (NovaServer novaServer : servers) {
             String svName = novaServer.getName();
             if (svName != null && svName.startsWith(TEST_PREFIX)) {
                 List<Port> ports = networkApi.getPortsByDeviceId(novaServer.getId());
                 for (Port port : ports) {
                     FloatingIp floatingip = networkApi.getFloatingIpByPortId(port.getId());
                     if (floatingip != null) {
                         floatingipids.add(floatingip.getFloatingIpAddress());
                     }
                 }
             }
         }
 
         final ComputeDriverConfiguration configuration = new ComputeDriverConfiguration();
         configuration.setCloud(cloud);
         configuration.setManagement(true);
 
         OpenStackCloudifyDriver driver = new OpenStackCloudifyDriver();
         driver.setConfig(configuration);
         driver.stopManagementMachines();
 
         assertNoResourcesLeft(floatingipids);
     }
 
     /**
      * Start an agent VM using {@link OpenStackCloudifyDriver#startMachine(org.cloudifysource.esc.driver.provisioning.ProvisioningContext, long, TimeUnit)}
      * using parameters configuration.<br />
      * Be aware that an agent VMs require that the management security groups and management networks are already created in Openstack, you may want to use
      * {@link OpenstackCloudifyDriverLauncher#startMachineWithManagement()}.
      *
      * @param service
      *            The service recipe to use.
      * @param cloud
      *            The cloud object.
      * @return
      *         The machine details object.
      * @throws Exception
      */
     public MachineDetails startMachine(Service service, Cloud cloud) throws Exception {
         final ComputeDriverConfiguration configuration = new ComputeDriverConfiguration();
         configuration.setCloud(cloud);
         configuration.setManagement(false);
         configuration.setNetwork(service.getNetwork());
         String managementMachineTemplate = cloud.getConfiguration().getManagementMachineTemplate();
 
         String serviceCloudTemplate = service.getCompute() == null || service.getCompute().getTemplate().isEmpty() ? managementMachineTemplate : service.getCompute()
                 .getTemplate();
         configuration.setCloudTemplate(serviceCloudTemplate);
         configuration.setServiceName(DEFAULT_APPLICATION_NAME + "." + service.getName());
 
         OpenStackCloudifyDriver driver = new OpenStackCloudifyDriver();
         driver.setConfig(configuration);
         MachineDetails md = driver.startMachine(null, END_TIME, TimeUnit.MINUTES);
         this.assertAgentSecurityGroups(cloud, service.getName());
         this.assertAgentAttachedSecurityGroups(md, cloud.getProvider().getManagementGroup(), service.getName());
         return md;
     }
 
     /**
      * Assert that cluster, agent and management exist.
      *
      * @param cloud
      * @throws OpenstackException
      */
     private void assertAgentSecurityGroups(Cloud cloud, String serviceName) throws OpenstackException {
         List<SecurityGroup> secgroups = networkApi.getSecurityGroupsByPrefix(TEST_PREFIX);
         AssertUtils.assertNotNull("Agent security groups not found", secgroups);
 
         Set<String> names = new HashSet<String>();
         for (SecurityGroup secgroup : secgroups) {
             names.add(secgroup.getName());
         }
 
         OpenStackResourcePrefixes secgroupnames = new OpenStackResourcePrefixes(cloud.getProvider().getManagementGroup(), DEFAULT_APPLICATION_NAME, serviceName);
         AssertUtils.assertTrue("Agent security group not found. Got: " + names, names.contains(secgroupnames.getAgentName()));
         AssertUtils.assertTrue("Cluster security group not found. Got: " + names, names.contains(secgroupnames.getClusterName()));
         AssertUtils.assertTrue("Application security group not found. Got: " + names, names.contains(secgroupnames.getApplicationName()));
         AssertUtils.assertTrue("Service security group not found. Got: " + names, names.contains(secgroupnames.getServiceName()));
 
         SecurityGroup mngSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getApplicationName());
         AssertUtils.assertNotNull("Missing application security rules", mngSecgroup.getSecurityGroupRules());
         // Application security groups should not have rules unless the default rules from openstack (2 egress rules for ipv4 and ipv6).
         AssertUtils.assertTrue("Application security groups should not have rules", mngSecgroup.getSecurityGroupRules().length == 2);
 
         SecurityGroup agentSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getServiceName());
         AssertUtils.assertNotNull("Missing service security rules", agentSecgroup.getSecurityGroupRules());
         AssertUtils.assertTrue("Missing service security rules", agentSecgroup.getSecurityGroupRules().length >= 2);
     }
 
     private void assertAgentAttachedSecurityGroups(MachineDetails md, String managementGroup, String serviceName) throws OpenstackException {
 
         OpenStackResourcePrefixes secgroupnames = new OpenStackResourcePrefixes(managementGroup, DEFAULT_APPLICATION_NAME, serviceName);
         SecurityGroup agentSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getAgentName());
         SecurityGroup clusterSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getClusterName());
         SecurityGroup appliSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getApplicationName());
         SecurityGroup serviceSecgroup = networkApi.getSecurityGroupsByName(secgroupnames.getServiceName());
 
         List<Port> ports = networkApi.getPortsByDeviceId(md.getMachineId());
         AssertUtils.assertNotNull("Cannot find a ports on management VM '" + md.getMachineId() + "'", ports);
         for (Port port : ports) {
             Network network = networkApi.getNetwork(port.getNetworkId());
             Set<String> securityGroups = port.getSecurityGroups();
             AssertUtils.assertTrue("Agent security group not found on port attached to network '" + network.getName() + "'",
                     securityGroups.contains(agentSecgroup.getId()));
             AssertUtils.assertTrue("Cluster security group not found on port attached to network '" + network.getName() + "'",
                     securityGroups.contains(clusterSecgroup.getId()));
             AssertUtils.assertTrue("Application security group not found on port attached to network '" + network.getName() + "'",
                     securityGroups.contains(appliSecgroup.getId()));
             AssertUtils.assertTrue("Service security group not found on port attached to network '" + network.getName() + "'",
                     securityGroups.contains(serviceSecgroup.getId()));
         }
     }
 
     /**
      * Stop an agent VM using {@link OpenStackCloudifyDriver#stopMachine(String, long, TimeUnit)} using parameters configuration.<br />
      *
      * @param service
      *            The service recipe to use.
      * @param cloud
      *            The cloud object.
      * @param serverIp
      *            The ip of the server to stop.
      * @param template
      *            The compute template to use.
      * @return
      *         The machine details object.
      * @throws Exception
      */
     public void stopMachine(Service service, Cloud cloud, String serverIp, String template) throws Exception {
         final ComputeDriverConfiguration configuration = new ComputeDriverConfiguration();
         configuration.setCloud(cloud);
         configuration.setManagement(false);
         configuration.setNetwork(service.getNetwork());
        String managementMachineTemplate = cloud.getConfiguration().getManagementMachineTemplate();

        String serviceCloudTemplate = service.getCompute() == null || service.getCompute().getTemplate().isEmpty() ? managementMachineTemplate : service.getCompute()
                 .getTemplate();
         configuration.setCloudTemplate(serviceCloudTemplate);
         configuration.setServiceName(DEFAULT_APPLICATION_NAME + "." + service.getName());
 
         OpenStackCloudifyDriver driver = new OpenStackCloudifyDriver();
         driver.setConfig(configuration);
         driver.stopMachine(serverIp, 60, TimeUnit.MINUTES);
     }
 
     public MachineDetails startMachineWithManagement(Service service, Cloud cloud) throws Exception {
         this.startManagementMachines(cloud);
         return this.startMachine(service, cloud);
     }
 
     public void stopMachineWithManagement(Service service, Cloud cloud, String serverIp) throws Exception {
         this.stopMachine(service, cloud, serverIp, DEFAULT_TEMPLATE);
         this.stopManagementMachines(cloud);
     }
 
     public void stopMachineWithManagement(Service service, Cloud cloud, String serverIp, String template) throws Exception {
         this.stopMachine(service, cloud, serverIp, template);
         this.stopManagementMachines(cloud);
     }
 
     private void assertNoResourcesLeft(Set<String> floatingips) throws OpenstackException {
         for (String ip : floatingips) {
             FloatingIp floatingIpByIp = networkApi.getFloatingIpByIp(ip);
             if (floatingIpByIp != null) {
                 AssertUtils.assertFail("Remaining floating ip in Openstack (" + floatingIpByIp + ")");
             }
         }
 
         // Clean remaining servers
         List<NovaServer> servers = computeApi.getServers();
         for (NovaServer novaServer : servers) {
             String svName = novaServer.getName();
             if (svName != null && svName.startsWith(TEST_PREFIX)) {
                 AssertUtils.assertFail("Remaining servers in Openstack (" + novaServer + ")");
             }
         }
 
         // Clean remaining secgroups
         List<SecurityGroup> secgroups = networkApi.getSecurityGroupsByPrefix(TEST_PREFIX);
         for (SecurityGroup secgroup : secgroups) {
             AssertUtils.assertFail("Remaining security groups in Openstack (" + secgroup + ")");
         }
 
         // Clean remaining routers
         List<Router> routers = networkApi.getRouters();
         for (Router router : routers) {
             String rName = router.getName();
             if (rName != null && rName.startsWith(TEST_PREFIX)) {
                 AssertUtils.assertFail("Remaining router in Openstack (" + router + ")");
             }
         }
 
         // Clean remaining networks
         List<Network> networks = networkApi.getNetworkByPrefix(TEST_PREFIX);
         for (Network network : networks) {
             AssertUtils.assertFail("Remaining network in Openstack (" + network + ")");
         }
 
     }
 
     /**
      * Verify that a router with the given prefix exists in Openstack environment.
      */
     public void assertRouterExists(String prefix) throws OpenstackException {
         String name = prefix + "management-public-router";
         Router router = networkApi.getRouterByName(name);
         AssertUtils.assertNotNull("Router '" + name + "' not found", router);
     }
 
     public void assertRouterExistsByFullName(String name) throws OpenstackException {
         Router router = networkApi.getRouterByName(name);
         AssertUtils.assertNotNull("Router '" + name + "' not found", router);
     }
 
     public String getManagementRouterNameFromDsl(Cloud cloud){
         String managementMachineTemplate = cloud.getConfiguration().getManagementMachineTemplate();
         return (String)cloud.getCloudCompute().getTemplates().get(managementMachineTemplate).getOptions().get("externalRouterName");
     }
 
     public void assertNoRouter(String prefix) throws OpenstackException {
         String name = prefix + "management-public-router";
         Router router = networkApi.getRouterByName(name);
         AssertUtils.assertNull("Router'" + name + "' not expected", router);
     }
 
     /**
      * Assert that a network with the given name exists in Openstack environment.
      */
     public void assertNetworkExists(String name) throws OpenstackException {
         Network network = networkApi.getNetworkByName(name);
         AssertUtils.assertNotNull("Network '" + name + "' not found", network);
     }
 
     /**
      * Assert that a network contains exactly the given number of subnets.
      */
     public void assertSubnetSize(String networkName, int size) throws OpenstackException {
         List<Subnet> subnets = networkApi.getSubnetsByNetworkName(networkName);
         AssertUtils.assertNotNull("No subnets found in '" + networkName + "' network ", subnets);
         AssertUtils.assertEquals("Expected to found '" + size + "' subnets in '" + networkName + "' network", size, subnets.size());
     }
 
     /**
      * Assert that a subnet exists.
      */
     public void assertSubnetExists(String networkName, String subnetName) throws OpenstackException {
         List<Subnet> subnets = networkApi.getSubnetsByNetworkName(networkName);
         AssertUtils.assertNotNull("No subnets found in '" + networkName + "' network ", subnets);
         for (Subnet subnet : subnets) {
             if (subnetName.equals(subnet.getName())) {
                 return;
             }
         }
         AssertUtils.assertFail("Subnet '" + subnetName + "' not found");
     }
 
     public void assertVMBoundToNetwork(String serverId, String networkName) throws OpenstackException {
 
         Network network = networkApi.getNetworkByName(networkName);
         Port port = networkApi.getPort(serverId, network.getId());
 
         boolean isTrue = network.getSubnets().length == port.getFixedIps().size();
         AssertUtils.assertTrue("The server '" + serverId + "' is not bound to all subnets of network '" + networkName + "'", isTrue);
         // List<RouteFixedIp> fixedIps = port.getFixedIps();
         //
         // Set<String> vmSubnetIds = new HashSet<String>();
         // for (RouteFixedIp routeFixedIp : fixedIps) {
         // vmSubnetIds.add(routeFixedIp.getSubnetId());
         // }
         //
         // String[] networkSubnetIds = network.getSubnets();
         // for (String subnetId : networkSubnetIds) {
         // AssertUtils.assertTrue("The server '" + serverId + "' is not bound to subnet '" + subnetId + "'", vmSubnetIds.contains(subnetId));
         // }
 
     }
 
     /**
      * Assert that a floating IP is correctly bind to a server.
      */
     public void assertFloatingIpBindToServer(MachineDetails md) throws OpenstackException {
         String serverId = md.getMachineId();
         List<Port> ports = networkApi.getPortsByDeviceId(serverId);
         for (Port port : ports) {
             FloatingIp floatingip = networkApi.getFloatingIpByPortId(port.getId());
             if (floatingip != null) {
                 AssertUtils.assertEquals("Wrong floating ip address bind to server '" + serverId + "'", floatingip.getFloatingIpAddress(),
                         md.getPublicAddress());
                 return;
             }
         }
         AssertUtils.assertFail("No floating ip bind to server '" + serverId + "'");
     }
 
     /**
      * Assert that a server has no floating IP.
      */
     public void assertNoFloatingIp(String serverId) throws OpenstackException {
         List<Port> ports = networkApi.getPortsByDeviceId(serverId);
         for (Port port : ports) {
             FloatingIp floatingip = networkApi.getFloatingIpByPortId(port.getId());
             AssertUtils.assertNull("Floating ip for server '" + serverId + "' not expected", floatingip);
         }
     }
 
     /**
      * Assert that a server has no floating IP. Checks only for management machine.
      */
     public void assertAssignedFloatingIp(String serverId) throws OpenstackException {
         List<Port> ports = networkApi.getPortsByDeviceId(serverId);
         List<Network> networks = networkApi.getNetworkByPrefix(TEST_PREFIX);
         List<String> networkIds = new LinkedList<String>();
 
         for(Network network : networks){
             networkIds.add(network.getId());
         }
         for (Port port : ports) {
             if(networkIds.contains(port.getNetworkId())){
                 FloatingIp floatingip = networkApi.getFloatingIpByPortId(port.getId());
                 AssertUtils.assertNotNull("Floating ip for server '" + serverId + "' expected", floatingip);
             }
         }
     }
 
     /**
      * Assert that a security group exists.
      */
     public void assertSecurityGroupExists(String name) throws OpenstackException {
         SecurityGroup secgroup = networkApi.getSecurityGroupsByName(name);
         AssertUtils.assertNotNull("Security group '" + name + "' not found", secgroup);
     }
 
     /**
      * Assert that security group has the expected number of rules.
      */
     public void assertSecurityGroupIncomingRulesNumber(String secgroupName, int numberOfDeclaredRules) throws OpenstackException {
         SecurityGroup secgroup = networkApi.getSecurityGroupsByName(secgroupName);
         AssertUtils.assertNotNull("Security group '" + secgroupName + "' not found", secgroup);
         AssertUtils.assertNotNull("No rules found in security group '" + secgroupName + "'", secgroup.getSecurityGroupRules());
         // expectedIncomingRules.size() + 3 because of egress ipv4 and ipv6 default rules AND ingress port 22 for management.
         AssertUtils.assertEquals("Wrong number of security rules in '" + secgroupName + "'", numberOfDeclaredRules + 3,
                 secgroup.getSecurityGroupRules().length);
     }
 
     public OpenStackComputeClient getComputeApi() {
         return computeApi;
     }
 
     public OpenStackNetworkClient getNetworkApi() {
         return networkApi;
     }
 
     public void setSkipExternalNetworking(Boolean skipExternalNetworking) {
         this.skipExternalNetworking = skipExternalNetworking;
     }
 
     /**
      * Assert that networks with the given names doesn't exist in Openstack environment.
      *
      * @throws OpenstackException
      */
     public void assertNetworksNotExists(List<String> networkNames) throws OpenstackException {
 
         for (String networkName : networkNames) {
             Network network = networkApi.getNetworkByName(networkName);
             AssertUtils.assertNull("Network '" + networkName + "' must not exist.", network);
 
         }
     }
 
 }
