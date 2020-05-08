 package org.cloudifysource.quality.iTests.framework.utils.compute;
 
 import com.google.common.base.Predicate;
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.esc.driver.provisioning.MachineDetails;
 import org.cloudifysource.esc.jclouds.JCloudsDeployer;
 import org.codehaus.plexus.util.StringUtils;
 import org.jclouds.compute.domain.ComputeMetadata;
 import org.jclouds.compute.domain.NodeMetadata;
 
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: elip
  * Date: 4/11/13
  * Time: 9:05 AM
  * To change this template use File | Settings | File Templates.
  */
 public class JcloudsComputeApiHelper implements ComputeApiHelper {
 
     private JCloudsDeployer deployer;
     private String region;
 
     public JcloudsComputeApiHelper(final Cloud cloud, final String region) {
         try {
             this.deployer = new JCloudsDeployer(cloud.getProvider().getProvider(), cloud.getUser().getUser(),
                     cloud.getUser().getApiKey(), new Properties());
             this.region = region;
         } catch (final Exception e) {
             throw new RuntimeException("Failed to initialize compute helper : " + e.getMessage(), e);
         }
     }
 
 
     @Override
     public Set<MachineDetails> getServersByPrefix(final String machineNamePrefix) {
 
         Predicate<ComputeMetadata> predicate = new Predicate<ComputeMetadata>() {
 
             public boolean apply(final ComputeMetadata input) {
                 boolean nameFound = false;
                 final NodeMetadata node = (NodeMetadata) input;
                 if (StringUtils.isNotBlank(node.getName())) {
                     nameFound = node.getName().contains(machineNamePrefix) && (node.getStatus() == NodeMetadata.Status.RUNNING);
                 }
                 return nameFound;
             }
 
         };
 
         Set<? extends NodeMetadata> servers = deployer.getServers(predicate);
         Set<MachineDetails> machineDetailsSet = new HashSet<MachineDetails>();
         for (NodeMetadata node: servers) {
             MachineDetails machineDetails = new MachineDetails();
             machineDetails.setPublicAddress(node.getPublicAddresses().iterator().next());
             machineDetailsSet.add(machineDetails);
         }
         return machineDetailsSet;
     }
 
     @Override
     public MachineDetails getServerByAttachmentId(String attachmentId) {
         NodeMetadata serverByID = deployer.getServerByID(region + "/" + attachmentId);
         MachineDetails machineDetails = new MachineDetails();
         machineDetails.setPublicAddress(serverByID.getPrivateAddresses().iterator().next());
         machineDetails.setPrivateAddress(serverByID.getPrivateAddresses().iterator().next());
         return machineDetails;
     }
 
     @Override
    public void shutdownServer(String serverId) {
        deployer.shutdownMachine(serverId);
     }
 }
