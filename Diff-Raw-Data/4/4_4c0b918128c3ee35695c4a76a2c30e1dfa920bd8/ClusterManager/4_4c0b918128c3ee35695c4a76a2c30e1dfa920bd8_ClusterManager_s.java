 package org.wso2.carbon.cep.wihidum.core;
 
 import com.hazelcast.config.Config;
 import com.hazelcast.core.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wso2.carbon.brokermanager.admin.internal.BrokerManagerAdminService;
 import org.wso2.carbon.brokermanager.admin.internal.BrokerProperty;
 import org.wso2.carbon.brokermanager.admin.internal.exception.BrokerManagerAdminServiceException;
 
 import java.util.Set;
 import java.util.UUID;
 
 public class ClusterManager {
 
     private static ClusterManager clusterManager;
     private HazelcastInstance hazelcastInstance;
     private BrokerManagerAdminService brokerService;
     private static final Log log = LogFactory.getLog(ClusterManager.class);
 
     private ClusterManager() {

     }
 
 
     public static ClusterManager getInstant() {
         if (clusterManager == null) {
             clusterManager = new ClusterManager();
 
         }
         return clusterManager;
 
     }
 
     public void initiate() {
        hazelcastInstance = Hazelcast.newHazelcastInstance(new Config().setInstanceName(UUID.randomUUID().toString()));
         Cluster cluster = hazelcastInstance.getCluster();
         Member localMember = cluster.getLocalMember();
         Set<Member> memberList = cluster.getMembers();
         cluster.addMembershipListener(new MembershipListener() {
             public void memberAdded(MembershipEvent membersipEvent) {
                 configureBrokers(membersipEvent.getMember());
             }
 
             public void memberRemoved(MembershipEvent membersipEvent) {
                 //TODO
             }
         });
         for (Member member : memberList) {
             configureBrokers(member);
         }
     }
 
     private synchronized void configureBrokers(Member member) {
         brokerService = new BrokerManagerAdminService();
         BrokerProperty[] properties = new BrokerProperty[Constants.BASIC_PROPERTIES];
         PropertyGenerator generator;
 
         generator = new PropertyGenerator(member.getInetSocketAddress());
         properties[0] = new BrokerProperty("receiverURL", generator.getReceiverURL());
         properties[1] = new BrokerProperty("authenticatorURL", generator.getAuthenticatorURL());
         properties[2] = new BrokerProperty("username", generator.getUsername());
         properties[3] = new BrokerProperty("password", generator.getPassword());
         try {
             brokerService.addBrokerConfiguration(member.getInetSocketAddress().toString(), "agent", properties);
         } catch (BrokerManagerAdminServiceException e) {
             log.error("Cannot add broker for CEP node on " + member.getInetSocketAddress());
         }
 
 
     }
 
 
 }
