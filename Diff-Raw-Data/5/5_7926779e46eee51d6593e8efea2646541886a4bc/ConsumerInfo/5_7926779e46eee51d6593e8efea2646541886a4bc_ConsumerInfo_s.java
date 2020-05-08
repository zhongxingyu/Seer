 package co.gridport.server.kafka;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.I0Itec.zkclient.IZkChildListener;
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonPropertyOrder;
 
 @JsonPropertyOrder({"groupId","status","partitions","processes"})
 public class ConsumerInfo {
 
     private ClusterInfo cluster;
     private String groupid;
     private Map<String, ConsumerTopicInfo> topics;
     private List<String> processes;
     private Map<String, Map<String, Object>> status;
 
     public ConsumerInfo(ClusterInfo cluster, String groupid) {
         this.cluster = cluster;
         this.groupid = groupid;
     }
 
     public String getGroupId() {
         return groupid;
     }
 
     public synchronized List<String> getProcesses() {
         if (processes == null) {
             processes = new ArrayList<String>();
             if (cluster.zk.exists("/consumers/"+groupid+"/ids")) {
                 setProcesses(cluster.zk.getChildren("/consumers/"+groupid+"/ids"));
                 cluster.zk.subscribeChildChanges("/consumers/"+groupid+"/ids", new IZkChildListener() {
                     @Override
                     public void handleChildChange(String parentPath, List<String> processList) throws Exception {
                         setProcesses(processList);
                     }
                 });
             } else {
                 cluster.zk.subscribeChildChanges("/consumers/"+groupid, new IZkChildListener() {
                     @Override
                     public void handleChildChange(String parentPath, List<String> changes) throws Exception {
                         if (changes.contains("ids")) {
                             processes = null;
                         }  
                     }
                 });
             }
         }
         return processes;
     }
 
     private void setProcesses(List<String> processList) {
         synchronized(processes) {
             processes.clear();
             processes.addAll(processList);
             Collections.sort(processes);
         }
     }
 
     public Map<String,Map<String,Object>> getStatus() {
         getTopics();
         for(String topicName: status.keySet()) {
             topics.get(topicName).updateStatus();
         }
         return status;
     }
 
     @JsonIgnore
     public ConsumerTopicInfo getConsumerTopic(String topicName) {
         getTopics();
         return topics.get(topicName);
     }
 
     private Map<String,ConsumerTopicInfo> getTopics() {
         if (topics == null) {
             try {
                 if (cluster.zk.exists("/consumers/"+groupid+"/offsets")) {
                    topics = new HashMap<String,ConsumerTopicInfo>();
                    status = new HashMap<String,Map<String, Object>>();
                     setTopics(cluster.zk.getChildren("/consumers/"+groupid+"/offsets"));
                     cluster.zk.subscribeChildChanges("/consumers/"+groupid+"/offsets", new IZkChildListener() {
                         @Override
                         public void handleChildChange(String parentPath, List<String> topicList) throws Exception {
                             setTopics(topicList);
                         }
                     });
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return topics;
     }
 
 
     private void setTopics(List<String> topicList) {
         synchronized(topics) {
             for(String topicName: topicList) {
                 if (!topics.containsKey(topicName)) {
                     ConsumerTopicInfo topicInfo = new ConsumerTopicInfo(
                         cluster, 
                         cluster.getTopics().get(topicName),
                         this
                     );
                     topics.put(topicName, topicInfo);
                     status.put(topicName, topicInfo.getStatus());
                 }
             }
             for(String topicName: topics.keySet()) {
                 if (!topicList.contains(topicName)) {
                     topics.remove(topicName);
                     status.remove(topicName);
                 }
             }
         }
     }
 }
