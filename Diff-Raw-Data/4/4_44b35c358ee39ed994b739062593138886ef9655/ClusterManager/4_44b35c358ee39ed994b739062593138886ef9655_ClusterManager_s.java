 
 package org.wso2.carbon.cep.wihidum.core.cluster;
 
 import com.hazelcast.config.Config;
 import com.hazelcast.core.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
 import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;
 import org.wso2.carbon.cep.wihidum.core.internal.WihidumCoreValueHolder;
 import org.wso2.carbon.context.CarbonContext;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 public class ClusterManager {
 
     private static ClusterManager clusterManager;
     private HazelcastInstance hazelcastInstance;
     private static final Log log = LogFactory.getLog(ClusterManager.class);
     private Member localMember;
     private ConcurrentSkipListSet<Member> memberList;
     private String localMemberAddress;
     private ArrayList<String> membersAddressList;
 
     private ClusterManager() {
         hazelcastInstance = Hazelcast.newHazelcastInstance(new Config().setInstanceName(UUID.randomUUID().toString()));
     }
 
 
     public static ClusterManager getInstant() {
         if (clusterManager == null) {
             clusterManager = new ClusterManager();
 
         }
         return clusterManager;
 
     }
 
     public void initiate() {
         Cluster cluster = hazelcastInstance.getCluster();
         localMember = cluster.getLocalMember();
        //memberList = (ConcurrentSkipListSet)cluster.getMembers();
         cluster.addMembershipListener(new MembershipListener() {
             public void memberAdded(MembershipEvent membersipEvent) {
                 configureBrokers(membersipEvent.getMember());
                 //memberList.add(membersipEvent.getMember());
             }
 
             public void memberRemoved(MembershipEvent membersipEvent) {
                 memberList.remove(membersipEvent.getMember());
                 //TODO
             }
         });
         for (Member member : cluster.getMembers()) {
             configureBrokers(member);
         }
     }
 
     private synchronized void configureBrokers(Member member) {
         memberList.add(member);
         String brokerName = member.getInetSocketAddress().getAddress().toString().substring(1);
         BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
         brokerConfiguration.setName(brokerName);
         brokerConfiguration.setType(Constants.AGENT_BROKER_TYPE);
         int tenantId = CarbonContext.getCurrentContext().getTenantId();
         Map<String,String> properties = new HashMap<String, String>();
         PropertyGenerator generator;
 
         generator = new PropertyGenerator(brokerName);
         properties.put("receiverURL",generator.getReceiverURL());
         properties.put("authenticatorURL",generator.getAuthenticatorURL());
         properties.put("username",generator.getUsername());
         properties.put("password",generator.getPassword());
 
         // add broker properties
         for (Map.Entry<String, String> entry : properties.entrySet()) {
             brokerConfiguration.addProperty(entry.getKey(), entry.getValue());
         }
         // add broker configuration
         try {
             WihidumCoreValueHolder.getInstance().getBrokerManagerService().addBrokerConfiguration(brokerConfiguration, -1234);
             //testBrokerConfiguration(brokerName);
         } catch (BMConfigurationException e) {
             log.error("Cannot add broker for CEP node on " + member.getInetSocketAddress());
         }
 
 
     }
     //Return string list of all members in the cluster except local member
     public ArrayList<String> getMemberList(){
        membersAddressList = new ArrayList<String>(memberList.size());
        for(Member member:memberList){
           if(!member.equals(localMember)){
               membersAddressList.add(member.getInetSocketAddress().getAddress().toString().substring(1));
           }
        }
         return membersAddressList;
     }
 
     //return string address of the local member
     public String getLocalMemberAddress(){
         localMemberAddress = localMember.getInetSocketAddress().getAddress().toString().substring(1);
         return localMemberAddress;
     }
     /*
     Return a Map consisting node ip and the bucket deployed at the node
      */
     public Map<String,Object> getBucketConfigurations(){
         Map<String,Object> bucketConfigurations = hazelcastInstance.getMap(Constants.CONFIG_MAP);
         return bucketConfigurations;
     }
 
     /*
     Set bucket configuration in Hazelcast
     @nodeAddress: ip address of the node
     @bucket: Bucket configuration of the node.
      */
     public void setBucketConfigurations(String nodeAddress,Object bucket){
        Map<String,Object> bucketConfigurations = hazelcastInstance.getMap(Constants.CONFIG_MAP);
        bucketConfigurations.put(nodeAddress,bucket);
     }
 
 }
