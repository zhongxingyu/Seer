 package org.levi.engine.impl.db;
 
 import org.levi.engine.Deployment;
 import org.levi.engine.EngineData;
 import org.levi.engine.db.DBManager;
 import org.levi.engine.identity.Group;
 import org.levi.engine.identity.User;
 import org.levi.engine.impl.bpmn.StartEvent;
 import org.levi.engine.impl.bpmn.UserTask;
 import org.levi.engine.persistence.hibernate.HibernateDao;
 import org.levi.engine.persistence.hibernate.process.hobj.DeploymentBean;
 import org.levi.engine.persistence.hibernate.process.hobj.EngineDataBean;
 import org.levi.engine.persistence.hibernate.process.hobj.ProcessInstanceBean;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.persistence.hibernate.process.ql.HqlManager;
 import org.levi.engine.persistence.hibernate.user.hobj.GroupBean;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 import org.levi.engine.runtime.ProcessInstance;
 import org.levi.engine.utils.Bean2Impl;
 import org.levi.engine.utils.Impl2Bean;
 import org.omg.spec.bpmn.x20100524.model.TPotentialOwner;
 import org.omg.spec.bpmn.x20100524.model.TUserTask;
 
 import java.util.*;
 
 public class DBManagerImpl implements DBManager {
 
     HibernateDao dao;
     HqlManager qlManager;
 
     public DBManagerImpl() {
         dao = new HibernateDao();
         qlManager = new HqlManager();
     }
 
     /**
      * This method saves a UserBean to the database; if the UserBean already exists, it updates the attributes
      *
      * @param user The user
      */
 
 
     public void saveUser(UserBean user) {
         dao.save(user);
     }
 
     public void saveUser(User user) {
         UserBean userBean = null;
         if (dao.getObject(UserBean.class, user.getUserId()) != null) {
             userBean = (UserBean) dao.getObject(UserBean.class, user.getUserId());
             dao.update(Impl2Bean.getUserBean(user, userBean, true));
         } else {
             userBean = new UserBean();
             dao.save(Impl2Bean.getUserBean(user, userBean, false));
         }
         if (user.getUserGroups() != null) {
             for (Group group : user.getUserGroups()) {
                 addUserToGroup(user.getUserId(), group.getGroupId());
             }
         }
     }
 
     public void saveGroup(GroupBean group) {
         dao.save(group);
     }
 
     public void saveGroup(Group group) {
         GroupBean groupBean = null;
         if (dao.getObject(GroupBean.class, group.getGroupId()) != null) { // group Exists
             groupBean = (GroupBean) dao.getObject(GroupBean.class, group.getGroupId());
             dao.update(Impl2Bean.getGroupBean(group, groupBean, true));
         } else {
             groupBean = new GroupBean();
             dao.save(Impl2Bean.getGroupBean(group, groupBean, false));
         }
     }
 
     public UserBean getUser(String userId) {
         return (UserBean) dao.getObject(UserBean.class, userId);
     }
 
     public GroupBean getGroup(String groupId) {
         return (GroupBean) dao.getObject(GroupBean.class, groupId);
     }
 
     public void addUserToGroup(String userId, String groupId) {
         UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
         GroupBean group = (GroupBean) dao.getObject(GroupBean.class, groupId);
 
         int id = -1;
         List<GroupBean> grps = user.getUserGroups();
         for (GroupBean grp : grps) {
             if (grp.getGroupId().equals(group.getGroupId())) {
                 id = grps.indexOf(grp);
                 break;
             }
 
         }
 
         if (id == -1) {
             user.getUserGroups().add(group);
         }
         dao.update(user);
         //dao.update(group);
     }
 
     public void deleteUser(String userId) {
         dao.remove(UserBean.class, userId);
     }
 
     public void deleteGroup(String groupId) {
         dao.remove(GroupBean.class, groupId);
     }
 
     public void removeUserFromGroup(String userId, String groupId) {
         dao.close();
         dao = new HibernateDao();
         GroupBean group = (GroupBean) dao.getObject(GroupBean.class, groupId);
         UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
 
         int id = -1;
 
         List<GroupBean> grps = user.getUserGroups();
         for (GroupBean grp : grps) {
             if (grp.getGroupId().equals(group.getGroupId())) {
                 id = grps.indexOf(grp);
                 break;
             }
         }
 
         if (id != -1) {
             user.getUserGroups().remove(id);
         }
         dao.update(user);
     }
 
     /**
      * Given the userId, return the list of groups the user has membership of
      *
      * @param userId
      * @return
      */
     public List<String> getGroupIds(String userId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void saveTask(TaskBean task) {
         dao.save(task);
     }
 
     public void deleteTask(String taskId) {
         dao.remove(TaskBean.class, taskId);
     }
 
     public void updateTask(TaskBean task) {
         dao.update(task);
     }
 
     public void saveProcess(DeploymentBean deployedProcess) {
         dao.save(deployedProcess);
     }
 
     public void deleteProcess(String processId) {
         dao.remove(DeploymentBean.class, processId);
     }
 
     public void updateProcess(ProcessInstanceBean process) {
         dao.update(process);
     }
 
     public void saveProcessInstance(ProcessInstanceBean process) {
         dao.save(process);
     }
 
     public void updateProcess(DeploymentBean process) {
         dao.update(process);
     }
 
     public void deleteProcessInstance(String processId) {
         dao.remove(ProcessInstanceBean.class, processId);
     }
 
     public void updateProcessInstance(ProcessInstanceBean process) {
         dao.update(process);
     }
 
     public List<TaskBean> getUserTaskList(String userId) {
         List<TaskBean> list = new ArrayList<TaskBean>();
         UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
         for (TaskBean task : user.getAssigned()) {
             if (task.isActive()) {
                 list.add(task);
             }
         }
         return list;
     }
 
     public List<ProcessInstanceBean> getRunningProcessesInstancesList() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<DeploymentBean> getDeployedProcessList() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public UserBean getAssigneeForTask(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(UserBean.class, taskId);
         return task.getAssignee();
     }
 
     public List<TaskBean> getActiveTasks() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getUnassignedTasks() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public List<TaskBean> getUnassignedTasks(String groupId) {
         return dao.getUnassignedTasks(groupId);
     }
 
     public List<TaskBean> getActiveTasks(String processId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public String getProcessInstanceId(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(UserBean.class, taskId);
         return task.getProcesseInstance().getProcessId();
     }
 
     public TaskBean getTaskBean(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         return task;
     }
 
     // Update the database to set assignee=username for the Task identified by taskId & processInstanceId
     public boolean claimUserTask(String taskId, String processInstanceId, String userId) {
         TaskBean task = dao.getTask(taskId, processInstanceId);
         if (task != null) {
             if (task.isAssigned()) {
                 return false;
             }
             task.setAssigned(true);
            dao.update(task);
             UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
             user.getAssigned().add(task);
             dao.update(user);
             return true;
         } else {
             // throw exception
             return false;
         }
     }
 
     public List<UserBean> getUserList() {
         //return dao.getUserObjects();
         return qlManager.getUserObjects();
     }
 
     public List<GroupBean> getGroupList() {
         //return dao.getGroupObjects();
         return qlManager.getGroupObjects();
     }
 
     public List<String> getGroupIdList() {
         return qlManager.getGroupIds();
     }
 
     public void assignTask(String taskId, String userId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         task.setActive(true);
         dao.update(task);
         UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
         user.getAssigned().add(task);
         dao.update(user);
     }
 
     public void unassignTask(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         if (task != null) {
             task.setActive(false);
             dao.update(task);
         }
     }
 
     /*
             This method can be use to remove the TASK from the task list of the USER
      */
     public void removeTask(String taskId, String userId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         UserBean user = (UserBean) dao.getObject(UserBean.class, userId);
         user.getAssigned().remove(task);
         dao.update(user);
     }
 
     public EngineData getEngineData() {
         EngineData engineData;
         try {
             EngineDataBean bean = getEngineDataBean();
             Bean2Impl b2i = new Bean2Impl();
             engineData = b2i.engineData(bean);
             //TODO need to clarified the exception
         } catch (Exception e) {
             engineData = new EngineData();
         }
 
         return engineData;
     }
 
     public EngineDataBean getEngineDataBean() {
         return (EngineDataBean) dao.getObject(EngineDataBean.class, "1");
     }
 
     public void persistDeployment(Deployment deployment) {
         //Converting to DeploymentBean
         DeploymentBean deploymentBean = new DeploymentBean();
         deploymentBean.setDefinitionsId(deployment.getDefinitionsId());
         deploymentBean.setExtractPath(deployment.getExtractPath());
         deploymentBean.setProcessDefinitionPath(deployment.getProcessDefinitionPath());
         deploymentBean.setDiagramPath(deployment.getDiagramPath());
         deploymentBean.setDeploymentTime(deployment.getDate());
 
         dao.save(deploymentBean);
         EngineDataBean engineDataBean = getEngineDataBean();
         if (engineDataBean != null) {
             engineDataBean.addDeployment(deploymentBean);
             dao.update(engineDataBean);
         } else {
             engineDataBean = new EngineDataBean();
             engineDataBean.setId("1");
             //engineDataBean.set_dateCreated(new Date());
             engineDataBean.addDeployment(deploymentBean);
             dao.save(engineDataBean);
         }
     }
 
     public void undeployProcess(String processId) {
         EngineDataBean bean = getEngineDataBean();
         bean.getDeployedProcesses().remove(processId);
         dao.save(bean);
         dao.remove(DeploymentBean.class, processId);
     }
 
     public void persistProcessInstance(ProcessInstance processInstance) {
         DeploymentBean deploymentBean = (DeploymentBean) dao.getObject(DeploymentBean.class, processInstance.getDefinitionsId());
         assert deploymentBean != null;
         ProcessInstanceBean processInstanceBean = new ProcessInstanceBean();
         processInstanceBean.setProcessId(processInstance.getProcessId());
         processInstanceBean.setDeployedProcess(deploymentBean);
         UserBean userBean = new UserBean();
         userBean.setUserId(processInstance.getStartUserId());
         UserBean user = (UserBean) dao.getObject(UserBean.class, processInstance.getStartUserId());
         if (user != null) {
             processInstanceBean.setStartUser(user);
         } else {
             processInstanceBean.setStartUser(userBean);
         }
 
         processInstanceBean.setStartTime(new Date());
         processInstanceBean.setVariables(processInstance.getVariables());
         processInstanceBean.setStartEventId(processInstance.getObjectModel().getStartEvent().getId());
         processInstanceBean.setRunning(true);
 
         dao.save(processInstanceBean);
         if (user != null) {
             user.addStartedProcessInstances(processInstanceBean);
             dao.update(user);
         } else {
             userBean.addStartedProcessInstances(processInstanceBean);
             dao.save(userBean);
         }
         EngineDataBean engineDataBean = (EngineDataBean) dao.getObject(EngineDataBean.class, "1");
         if (engineDataBean != null) {
             engineDataBean.addProcessInstance(processInstanceBean);
             dao.update(engineDataBean);
         } else {
             engineDataBean = new EngineDataBean();
             engineDataBean.setId("1");
             engineDataBean.addProcessInstance(processInstanceBean);
             dao.save(engineDataBean);
         }
     }
 
     public String getProcessDefinition(String processId) {
         ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processId);
         return processInstanceBean.getDeployedProcess().getDefinitionsId();
     }
 
     public List<String> getCompletedTasks(String processId) {
         dao.close();
         dao = new HibernateDao();
         ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processId);
         if (processInstanceBean.getCompletedTasks() != null)
             return (new ArrayList(processInstanceBean.getCompletedTasks().keySet()));
         else return new ArrayList<String>();
     }
 
     public List<String> getRunningTasks(String processId) {
         dao.close();
         dao = new HibernateDao();
         ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, processId);
         if (processInstanceBean.getRunningTasks() != null)
             return (new ArrayList(processInstanceBean.getRunningTasks().keySet()));
         else return new ArrayList<String>();
     }
 
     public void persistUserTask(UserTask userTask) {
         TaskBean userTaskBean = (TaskBean) dao.getObject(TaskBean.class, userTask.getId());
         if (userTaskBean == null) {   //TODO what is the purpose of such validation
             userTaskBean = new TaskBean();
             userTaskBean.setTaskId(userTask.getId());
             userTaskBean.setTaskId(userTask.getId());
             userTaskBean.setActive(true);
             ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, userTask.getProcessInstance().getProcessId());
             userTaskBean.setProcesseInstance(processInstanceBean);
             TUserTask task = userTask.getTTask();
             String potentialOwner = ((TPotentialOwner) (task.getResourceRoleArray()[0])).getResourceAssignmentExpression().getExpression().getDomNode().getChildNodes().item(0).getNodeValue();
             GroupBean potentialGroup = (GroupBean) dao.getObject(GroupBean.class, potentialOwner);
             if (potentialGroup != null) {
                 userTaskBean.setPotentialGroup(potentialGroup);
             }
             /*UserBean user = (UserBean) dao.getObject(UserBean.class, task.getAssignee());
             userTaskBean.setAssignee(user);*/
             userTaskBean.setFormName(task.getName());
             userTaskBean.setTaskName(task.getName());
             userTaskBean.setHasUserForm(userTask.hasInputForm());
             userTaskBean.setFromPath(task.getInputForm());
             dao.save(userTaskBean);
         }
     }
 
     public void persistStartEvent(StartEvent startEvent) {
         TaskBean starteventbean = new TaskBean();
         starteventbean.setActive(true);
         starteventbean.setTaskId(startEvent.getId());
         starteventbean.setTaskId(startEvent.getId());
         ProcessInstanceBean processInstanceBean = (ProcessInstanceBean) dao.getObject(ProcessInstanceBean.class, startEvent.getProcessInstance().getProcessId());
         starteventbean.setProcesseInstance(processInstanceBean);
         starteventbean.setAssignee(processInstanceBean.getStartUser());
         starteventbean.setFormName(startEvent.getTStartEvent().getInputForm());
         dao.save(starteventbean);
     }
 
     public void addRunningTask(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         if (task != null) {
             ProcessInstanceBean processInstanceBean = task.getProcesseInstance();
             processInstanceBean.addToRunningTask(task);
             //TODO this addToRunningTask(task) should implement here
             dao.update(processInstanceBean);
         }
     }
 
     public void removeRunningTask(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         if (task != null) {
             ProcessInstanceBean processInstanceBean = task.getProcesseInstance();
             processInstanceBean.removeFromRunningTask(task);
             //TODO this removeFromRunningTask(task) should implement here
             dao.update(processInstanceBean);
         }
     }
 
     public void addCompletedTask(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         if (task != null) {
             ProcessInstanceBean processInstanceBean = task.getProcesseInstance();
             processInstanceBean.addToCompletedTask(task);
             //TODO this addToCompletedTask(task) should implement here
             dao.update(processInstanceBean);
         }
     }
 
     public List<String> getDeploymentIds() {
         HibernateDao dao = new HibernateDao();
         if (dao.getObject(EngineDataBean.class, "1") == null) {
             return new ArrayList<String>();
         }
         EngineDataBean engineDataBean = (EngineDataBean) dao.getObject(EngineDataBean.class, "1");
         Map<String, DeploymentBean> deployedProcesses = engineDataBean.getDeployedProcesses();
         List<String> deploymentIds = new ArrayList<String>();
         for (String id : deployedProcesses.keySet()) {
             deploymentIds.add(((DeploymentBean) deployedProcesses.get(id)).getDefinitionsId());
         }
         return deploymentIds;
     }
 
     public String getPotentialGroup(String taskId) {
         TaskBean task = (TaskBean) dao.getObject(TaskBean.class, taskId);
         return task.getPotentialGroup().getGroupId();
     }
 
     public void closeSession() {
         dao.close();
     }
 }
