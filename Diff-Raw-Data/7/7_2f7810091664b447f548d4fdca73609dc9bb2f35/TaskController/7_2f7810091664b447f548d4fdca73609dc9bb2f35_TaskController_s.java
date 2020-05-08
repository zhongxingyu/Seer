 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.rest.process.task;
 
 import com.abada.bpm.console.server.integration.AbadaTaskManagement;
 import com.abada.extjs.ExtjsStore;
 import java.net.URL;
 
 import java.util.List;
 import javax.annotation.security.RolesAllowed;
 import javax.servlet.http.HttpServletRequest;
 import org.jboss.bpm.console.client.model.TaskRef;
 import org.jboss.bpm.console.server.integration.TaskManagement;
 import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
 import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.AbstractAuthenticationToken;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author katsu
  */
 @Controller
 @RequestMapping("/rs/tasks")
 public class TaskController {
 
     @Autowired
     private AbadaTaskManagement taskManagementAbada;
     @Autowired
     private TaskManagement taskManagement;
     @Autowired
     private FormDispatcherPlugin formDispatcherPlugin;
 
     /**
      * Return the tasks for a given patient that have to be completed by user.
      *
      * @param userId User id.
      * @param patientId Patient id.
      * @return Return the tasks for a given patient that have to be completed by
      * user.
      */
     @RequestMapping(value = "/patient/{patientId}/loggeduser", method = RequestMethod.GET)
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     public ExtjsStore<TaskRef> getTaskForPatient(@PathVariable Long patientId, HttpServletRequest request) {
         AbstractAuthenticationToken principal=(AbstractAuthenticationToken)request.getUserPrincipal();
 //        User user=(User) userDao.loadUserByUsername(principal.getName());
         List<TaskRef> aux=processTaskListResponse(taskManagementAbada.getTaskByUser(patientId, principal.getName()));
         ExtjsStore<TaskRef> result=new ExtjsStore<TaskRef>();
         result.setData(aux);
         return result;
     }
 
     /**
      * Return the tasks for a given processInstance that have to completed by
      * user.
      *
      * @param userId User id.
      * @param processInstanceId ProcessInstance id.
      * @return Return the tasks for a given processInstance that have to
      * completed by user.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     @RequestMapping(value = "/process/{processInstanceId}/loggeduser", method = RequestMethod.GET)
     public ExtjsStore<TaskRef> getTaskForProcessInstanceAndSubProcess(@PathVariable Long processInstanceId, HttpServletRequest request) {
         AbstractAuthenticationToken principal=(AbstractAuthenticationToken)request.getUserPrincipal();
 //        User user=(User) userDao.loadUserByUsername(principal.getName());
         List<TaskRef> aux=processTaskListResponse(taskManagementAbada.getTaskForProcessInstanceAndSubProcess(processInstanceId, principal.getName()));
         ExtjsStore<TaskRef> result=new ExtjsStore<TaskRef>();
         result.setData(aux);
         return result;
     }
 
     /**
      * Return the tasks that have to be completed by user.
      *
      * @param userId User id.
      * @return Return the tasks that have to be completed by user.
      */
     @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     public ExtjsStore<TaskRef> getAssignedTasks(@PathVariable String userId) {
         List<TaskRef> aux=processTaskListResponse(taskManagement.getAssignedTasks(userId));
         ExtjsStore<TaskRef> result=new ExtjsStore<TaskRef>();
         result.setData(aux);
         return result;
     }
 
     /**
      * Return the tasks that have to be completed by user and the groups you
      * belong to.
      *
      * @param userId User id.
      * @return Return the tasks that have to be completed by user and the groups
      * you belong to.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
    @RequestMapping(value = "/{userId}/participation", method = RequestMethod.GET)
    public ExtjsStore<TaskRef> getTasksForIdRefPaticipation(@PathVariable String userId) {
        List<TaskRef> aux=processTaskListResponse(taskManagement.getUnassignedTasks(userId, null));
         ExtjsStore<TaskRef> result=new ExtjsStore<TaskRef>();
         result.setData(aux);
         return result;
     }
 
     /**
      * Assign user to task
      *
      * @param taskId Task id.
      * @param idRef User id.
      * @param request Do nothing.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     @RequestMapping(value = "/{taskId}/assign/{idRef}", method = RequestMethod.POST)
     public void assignTask(@PathVariable Long taskId, @PathVariable String idRef, HttpServletRequest request) {
         taskManagement.assignTask(taskId, idRef, request.getUserPrincipal().getName());
     }
 
     /**
      * Release Task
      *
      * @param taskId Task id.
      * @param request Do nothing.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     @RequestMapping(value = "/{taskId}/release", method = RequestMethod.POST)
     public void releaseTask(@PathVariable Long taskId, HttpServletRequest request) {
         taskManagement.assignTask(taskId, null, request.getUserPrincipal().getName());
     }
 
     /**
      * Close a task with the passed params in the http request.
      *
      * @param taskid Task id.
      * @param request
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     @RequestMapping(value = "/{taskid}/close", method = RequestMethod.POST)
     public void close(@PathVariable Long taskid, HttpServletRequest request) {
         taskManagement.completeTask(taskid, com.abada.web.util.URL.parseRequest(request), request.getUserPrincipal().getName());
     }
 
     /**
      * Completing a task by passing data.
      *
      * @param taskid Task id.
      * @param outcome Reason comment.
      * @param request Do nothing.
      */
     @RolesAllowed(value = {"ROLE_ADMIN", "ROLE_USER"})
     @RequestMapping(value = "/{taskid}/close/{outcome}", method = RequestMethod.POST)
     public void closeTaskWithSignal(@PathVariable Long taskid, @PathVariable String outcome, HttpServletRequest request) {
         taskManagement.completeTask(taskid, outcome, null, request.getUserPrincipal().getName());
     }
 
     private List<TaskRef> processTaskListResponse(List<TaskRef> taskList) {
         // decorate task form URL if plugin available
         if (formDispatcherPlugin != null) {
             for (TaskRef task : taskList) {
                 URL taskFormURL = formDispatcherPlugin.getDispatchUrl(
                         new FormAuthorityRef(String.valueOf(task.getId())));
                 if (taskFormURL != null) {
                     task.setUrl(taskFormURL.toExternalForm());
                 }
             }
         }
         return taskList;
     }
 }
