 package cmu.ds.mr.mapred;
 
 import java.io.Serializable;
 
 /**
  * Task status to keep track of task progress and state
  * 
  * */
 public class TaskStatus implements Serializable {
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
 
   public static enum TaskState {
     SUCCEEDED, WAITING, DEFINE, RUNNING, READY, FAILED, KILLED
   };
   
   public static enum TaskType {
     MAP("m"), REDUCE("r");
     private TaskType(final String text) {
       this.text = text;
     }
 
     private final String text;
 
     @Override
     public String toString() {
         return text;
     }
   };
   
   // this taskId is read-only. Set type, taskNum, tryNum in this class
   private TaskID taskId; 
   
   private TaskState state = TaskState.DEFINE;
   private TaskType type;
   private int taskNum;
   private int tryNum;
   
   //private float progress;
   
   public TaskStatus(TaskID taskId, TaskState state, TaskType type) {
     super();
     this.taskId = taskId;
     this.state = state;
     this.type = type;
   }
   
   
   public TaskID getTaskId() {
     return taskId;
   }
 
   public void setTaskId(TaskID taskId) {
     this.taskId = taskId;
   }
 
 
   public int getTaskNum() {
     return taskNum;
   }
 
 
   public void setTaskNum(int taskNum) {
     this.taskNum = taskNum;
   }
 
 
   public TaskState getState() {
     return state;
   }
   
   public void setState(TaskState state) {
     this.state = state;
   }
   public TaskType getType() {
     return type;
   }
   public void setType(TaskType type) {
     this.type = type;
   }
 
   public int getTryNum() {
     return tryNum;
   }
 
   public void setTryNum(int tryNum) {
     this.tryNum = tryNum;
   }
 
   
 }
