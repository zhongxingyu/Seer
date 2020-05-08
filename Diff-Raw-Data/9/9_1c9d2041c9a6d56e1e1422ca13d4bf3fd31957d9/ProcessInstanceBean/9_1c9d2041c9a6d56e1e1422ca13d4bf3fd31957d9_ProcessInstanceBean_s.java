 package org.levi.engine.persistence.hibernate.process.hobj;
 
 
 import org.levi.engine.persistence.hibernate.HObject;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import javax.persistence.*;
 import java.util.*;
 
 /* This class represents a Process instance  */
 
 @Entity
 @Table(name = "process_instance")
 @SecondaryTable(name = "process_started_user")
 public class ProcessInstanceBean extends HObject {
     private UserBean startUser;
     private String processId;
     //private String processDefId;
     private DeploymentBean deployedProcess;
     private Map<String, Object> variables;
     //private List<String> runningTaskIds;
     private Map<String, TaskBean> runningTasks;
     //private List<String> completedTaskIds;
     private Map<String, TaskBean> completedTasks;
     private Boolean isRunning;
     private Date startTime;
     private String startEventId;
     private String endEventId;
     private List<TaskBean> tasks;
 
 
     @Id
     public String getProcessId() {
         return processId;
     }
 
     public void setProcessId(String processId) {
         this.processId = processId;
     }
 
 
     @OneToOne(targetEntity = DeploymentBean.class)
     public DeploymentBean getDeployedProcess() {
         return deployedProcess;
     }
 
     public void setDeployedProcess(DeploymentBean deployedProcess) {
         this.deployedProcess = deployedProcess;
     }
 
     /*
     public String getProcessDefId() {
         return processDefId;
     }
 
     public void setProcessDefId(String processDefId) {
         this.processDefId = processDefId;
     }
     */
 
     @Transient //TODO Object (use a wrapper class)
     public Map<String, Object> getVariables() {
         return variables;
     }
 
     public void setVariables(Map<String, Object> variables) {
         this.variables = variables;
     }
 
     /*
     public List<String> getRunningTaskIds() {
         return runningTaskIds;
     }
 
     public void setRunningTaskIds(ArrayList<String> runningTaskIds) {
         this.runningTaskIds = runningTaskIds;
     }
     */
 
    @OneToMany(targetEntity = TaskBean.class,fetch = FetchType.EAGER)
    @MapKey(name = "id")
     @JoinTable(name = "running_tasks", joinColumns = {@JoinColumn(name = "processId")})
     public Map<String, TaskBean> getRunningTasks() {
         return runningTasks;
     }
 
     public void setRunningTasks(Map<String, TaskBean> runningTasks) {
         this.runningTasks = runningTasks;
     }
 
     /*
     @Transient
     public List<String> getCompletedTaskIds() {
         return completedTaskIds;
     }
 
     public void setCompletedTaskIds(ArrayList<String> completedTaskIds) {
         this.completedTaskIds = completedTaskIds;
     }
     */
 
    @OneToMany(targetEntity = TaskBean.class,fetch = FetchType.EAGER)
    @MapKey(name = "id")
     @JoinTable(name = "completed_tasks", joinColumns = {@JoinColumn(name = "processId")})
     public Map<String, TaskBean> getCompletedTasks() {
         return completedTasks;
     }
 
     public void setCompletedTasks(Map<String, TaskBean> completedTasks) {
         this.completedTasks = completedTasks;
     }
 
     public Boolean getRunning() {
         return isRunning;
     }
 
     public void setRunning(Boolean running) {
         isRunning = running;
     }
 
     @ManyToOne(cascade = CascadeType.PERSIST)
     @JoinColumn(name = "start_user", table = "process_started_user")
     public UserBean getStartUser() {
         return startUser;
     }
 
     public void setStartUser(UserBean startUser) {
         this.startUser = startUser;
     }
 
     public Date getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Date startTime) {
         this.startTime = startTime;
     }
 
     public String getStartEventId() {
         return startEventId;
     }
 
     public void setStartEventId(String startEventId) {
         this.startEventId = startEventId;
     }
 
     public String getEndEventId() {
         return endEventId;
     }
 
     public void setEndEventId(String endEventId) {
         this.endEventId = endEventId;
     }
 
     @OneToMany(targetEntity = TaskBean.class, mappedBy = "processeInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
     public List<TaskBean> getTasks() {
         return tasks;
     }
 
     public void setTasks(List<TaskBean> tasks) {
         this.tasks = tasks;
     }
 
     public void addToCompletedTask(TaskBean taskBean) {
         if (completedTasks == null) {
             completedTasks = new HashMap<String, TaskBean>();
         }
         if (taskBean != null)
             completedTasks.put(taskBean.getTaskId(), taskBean);
     }
 
     public void addToRunningTask(TaskBean taskBean) {
         if (runningTasks == null) {
             runningTasks = new HashMap<String, TaskBean>();
         }
         if (taskBean != null)
             runningTasks.put(taskBean.getTaskId(), taskBean);
     }
 
     public void removeFromRunningTask(TaskBean taskBean) {
         if (runningTasks == null) {
             return;
         }
         if (runningTasks.containsKey(taskBean.getTaskId())) {
             runningTasks.remove(taskBean.getTaskId());
         }
     }
 }
