 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.wiredwidgets.cow.server.convert;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.jbpm.task.Content;
 import org.jbpm.task.TaskData;
 //import org.jbpm.task.service.local.LocalTaskService;
 import org.jbpm.task.utils.ContentMarshallerHelper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.wiredwidgets.cow.server.api.service.Task;
 import org.wiredwidgets.cow.server.api.service.Variable;
 import org.wiredwidgets.cow.server.api.service.Variables;
 import org.wiredwidgets.cow.server.manager.TaskServiceFactory;
import org.wiredwidgets.cow.server.transform.v2.bpmn20.Bpmn20UserTaskNodeBuilder;
 
 /**
  *
  * @author FITZPATRICK
  */
 @Component
 public class JbpmTaskToSc2Task extends AbstractConverter<org.jbpm.task.Task, Task> {
 
     // NOTE: Autowiring does not work here!
     @Autowired
     //org.jbpm.task.service.TaskClient taskClient;
     //LocalTaskService localService;
     TaskServiceFactory taskServiceFactory;
     
 //    @Autowired
 //    MinaHTWorkItemHandler minaWorkItemHandler;
     
     private static Logger log = Logger.getLogger(JbpmTaskSummaryToSc2Task.class);
     
     @Override
     public Task convert(org.jbpm.task.Task s) {
         Task target = new Task();
 
         if (s == null){
             return null;
         }
         TaskData source = s.getTaskData();
         
         //target.setDescription(source.getDescriptions().get(0).getText());
 
         if (source != null && source.getActualOwner() != null){
             target.setAssignee(source.getActualOwner().getId());
         }
 
         if (source != null && source.getCreatedOn() != null) {
             target.setCreateTime(convert(source.getCreatedOn()));
         }
 
         if (source != null && source.getExpirationTime() != null) {
             target.setDueDate(convert(source.getExpirationTime()));
         }
         
         target.setId(String.valueOf(s.getId()));
         target.setProcessInstanceId(String.valueOf(source.getProcessInstanceId()));
         
         target.setState(source.getStatus().name());
         
         if (s.getNames() != null && !s.getNames().isEmpty()){
         	// see Bpmn20UserTaskNodeBuilder
         	String[] parts = s.getNames().get(0).getText().split("/");
             target.setActivityName(parts[0]); // corresponds to "key" in workflow
             target.setName(parts[1]); // used for display to the user
         }
         
         target.setPriority(new Integer(s.getPriority()));
         
         // add task outcomes using the "Options" variable from the task
         //BlockingGetTaskResponseHandler getTaskResponseHandler = new BlockingGetTaskResponseHandler();
         //taskClient.getTask(s.getId(), getTaskResponseHandler);  
         //org.jbpm.task.Task task = getTaskResponseHandler.getTask();
         //org.jbpm.task.Task task = localService.getTask(s.getId());
         org.jbpm.task.Task task = taskServiceFactory.getTaskService().getTask(s.getId());
         
         //BlockingGetContentResponseHandler getContentResponseHandler = new BlockingGetContentResponseHandler();
         //taskClient.getContent(task.getTaskData().getDocumentContentId(), getContentResponseHandler);   
         //Content content = getContentResponseHandler.getContent();
         //Content content = localService.getContent(task.getTaskData().getDocumentContentId());
         Content content = taskServiceFactory.getTaskService().getContent(task.getTaskData().getDocumentContentId());
         
         Map<String, Object> map = (Map<String, Object>) ContentMarshallerHelper.unmarshall(
         		content.getContent(), null);  
         
         if (map.containsKey("Options")){
             String[] options = ( (String) map.get("Options") ).split(",");
             target.getOutcomes().addAll(Arrays.asList(options));
         }
         
         // get ad-hoc variables from the "Content" map
        if (map.containsKey(Bpmn20UserTaskNodeBuilder.TASK_INPUT_VARIABLES_NAME)){
            Map<String, Object> contentMap = (Map<String, Object>) map.get(Bpmn20UserTaskNodeBuilder.TASK_INPUT_VARIABLES_NAME);
             if (contentMap != null) {
                     for (Map.Entry<String, Object> entry : contentMap.entrySet()) {
                             addVariable(target, entry.getKey(), entry.getValue());
                     }
             }
         }
         
         return target;
     }
     
     private void addVariable(Task task, String key, Object value) {
         Variable var = new Variable();
         var.setName(key);
         // Support strings only.  Other types will cause ClassCastException
         try {
             var.setValue((String)value);
         } catch (ClassCastException e) {
             var.setValue("Variable type " + value.getClass().getName() + " is not supported");
         }    	
         addVariable(task, var);
     }
 
     private void addVariable(Task task, Variable var) {
         if (task.getVariables() == null) {
             task.setVariables(new Variables());
         }
         task.getVariables().getVariables().add(var);
     }
     
     /*
      * Fix for COW-132
      * Tasks in parallel structures may cause sub-executions  with IDs in the form key.number.number.number
      * In this case we only want to look at the top level process execution.  
      */
 //    private String getTopLevelExecutionId(String executionId) {
 //        String[] parts = executionId.split("\\.");
 //        return parts[0] + "." + parts[1];
 //    }
 
 }
