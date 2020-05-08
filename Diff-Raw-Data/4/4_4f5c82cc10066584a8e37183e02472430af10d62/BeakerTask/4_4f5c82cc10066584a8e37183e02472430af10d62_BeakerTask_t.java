 package org.fedorahosted.beaker4j.remote_model;
 
 import java.util.Map;
 
 import org.apache.xmlrpc.XmlRpcException;
 import org.fedorahosted.beaker4j.client.BeakerClient;
 import org.fedorahosted.beaker4j.xmlrpc.client.XmlRpcApi;
 
 public class BeakerTask extends RemoteBeakerObject {
     
     private final String taskId;
     
     
     public BeakerTask(String taskId) {
         this.taskId = taskId;
     }
 
     public BeakerTask(String taskId, BeakerClient beakerClient) {
         super(beakerClient);
         this.taskId = taskId;
     }
     
     public TaskInfo getInfo() throws XmlRpcException {
         System.out.println("taksId je " + taskId);
         @SuppressWarnings("unchecked")
         Map<String,?>taskInfo = (Map<String,?>)callOnBeaker(XmlRpcApi.TASKACTIONS_TASK_INFO, new Object[] {taskId});
         return new TaskInfo(taskInfo);
     }
     
     public int getRemainingTime() throws XmlRpcException {
         Integer id = getTaskIdAsInt();
         return new Watchdog(beakerClient).getRemaingTime(id.intValue());
     }
     
     public int extend(int extendTime) throws XmlRpcException {
         Integer id = getTaskIdAsInt();
         System.out.println("Extendsing time for " + taskId);
         //TODO need a check if task is running
         return (Integer)callOnBeaker(XmlRpcApi.RECIPES_TASKS_EXTEND, new Object[] {id, extendTime});
     }
     
     private Integer getTaskIdAsInt() {
         Integer id;
         if(taskId.startsWith("T:")) {
             id = new Integer(taskId.substring(2, taskId.length()));
         } else {
             id = new Integer(taskId);
         }
         return id;
     }
     
     public static class TaskInfo {
 
         private static final String ID_FIELD = "id";
         private static final String METHOD_FIELD = "method";
         private static final String FAILED_FIELD = "is_failed";
         private static final String FINISHED_FIELD = "is_finished";
         private static final String LABEL_FIELD = "state_label";
         private static final String STATE_FIELD = "state";
         private static final String RESULT_FIELD = "result";
         private static final String WORKER_FIELD = "worker";
         
         private final String id;
         private String method;
         private boolean isFailed;
         private boolean isFinished;
         private String stateLabel;
         private TaskStatus state;
         private TaskResult result;
         private Worker worker;
         
         
         public TaskInfo(String id) {
             this.id = id;
         }
         
         public TaskInfo(Map<String,?> taskInfo) {
             if(!taskInfo.containsKey("id"))
                 throw new IllegalArgumentException("Provided task info map doesn't contain id");
             this.id = (String)taskInfo.get(ID_FIELD);
             this.method = (String)taskInfo.get(METHOD_FIELD);
             this.isFailed = (Boolean)taskInfo.get(FAILED_FIELD);
             this.isFinished = (Boolean)taskInfo.get(FINISHED_FIELD);
             this.stateLabel = (String)taskInfo.get(LABEL_FIELD);
            this.state = TaskStatus.valueOf(((String)taskInfo.get(STATE_FIELD)).toUpperCase());
            this.result = TaskResult.valueOf(((String)taskInfo.get(RESULT_FIELD)).toUpperCase());
             this.worker = new Worker((Map<String,String>)taskInfo.get(WORKER_FIELD));
         }
 
         
         public String toString() {
             StringBuffer sb = new StringBuffer("Beaker task");
             sb.append(ID_FIELD).append(":\t").append(id).append("\n");
             sb.append(METHOD_FIELD).append(":\t").append(method).append("\n");
             sb.append(FAILED_FIELD).append(":\t").append(isFailed).append("\n");
             sb.append(FINISHED_FIELD).append(":\t").append(isFinished).append("\n");
             sb.append(LABEL_FIELD).append(":\t").append(stateLabel).append("\n");
             sb.append(STATE_FIELD).append(":\t").append(state).append("\n");
             sb.append(RESULT_FIELD).append(":\t").append(result).append("\n");
             sb.append(WORKER_FIELD).append(":\t").append(worker.toString()).append("\n");
             return sb.toString();
         }
 
         public String getMethod() {
             return method;
         }
 
 
         public void setMethod(String method) {
             this.method = method;
         }
 
 
         public boolean isFailed() {
             return isFailed;
         }
 
 
         public void setFailed(boolean isFailed) {
             this.isFailed = isFailed;
         }
 
 
         public boolean isFinished() {
             return isFinished;
         }
 
 
         public void setFinished(boolean isFinished) {
             this.isFinished = isFinished;
         }
 
 
         public String getStateLabel() {
             return stateLabel;
         }
 
 
         public void setStateLabel(String stateLabel) {
             this.stateLabel = stateLabel;
         }
 
 
         public TaskStatus getState() {
             return state;
         }
 
 
         public void setState(TaskStatus state) {
             this.state = state;
         }
         
         public TaskResult getResult() {
             return result;
         }
 
         public void setResult(TaskResult result) {
             this.result = result;
         }
 
         public Worker getWorker() {
             return worker;
         }
 
 
         public void setWorker(Worker worker) {
             this.worker = worker;
         }
 
 
         public String getId() {
             return id;
         }
         
     }
     
     public static class Worker {
 
         private static final String NAME_FIELD = "name";
         
         private final String name;
         
         public Worker(String name) {
             this.name = name;
         }
 
         public Worker(Map<String,String> worker) {
             this.name = worker.get(NAME_FIELD);
         }
         
         public String toString() {
             return "Beaker worker: \n " + NAME_FIELD + ":\t" + name;
         }
         
         public String getName() {
             return name;
         }
         
     }
 
     
 }
