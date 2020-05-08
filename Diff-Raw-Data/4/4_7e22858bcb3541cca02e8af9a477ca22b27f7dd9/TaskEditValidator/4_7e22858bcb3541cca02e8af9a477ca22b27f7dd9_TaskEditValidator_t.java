 package com.projectportal.validator;
 
 import com.projectportal.data.RequestData;
 import com.projectportal.entity.Project;
 import com.projectportal.entity.Task;
 import org.jboss.seam.faces.validation.InputElement;
 import org.jboss.seam.security.Identity;
 import org.jboss.solder.logging.Logger;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.FacesValidator;
 import javax.faces.validator.Validator;
 import javax.faces.validator.ValidatorException;
 import javax.inject.Inject;
 import javax.persistence.EntityManager;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lastcow
  * Date: 1/21/13
  * Time: 5:16 PM
  * To change this template use File | Settings | File Templates.
  */
 @FacesValidator("taskEditValidator")
 public class TaskEditValidator extends AbstractValidator implements Validator {
 
     @Inject EntityManager em;
     @Inject Identity identity;
     @Inject Logger logger;
 
     @Inject RequestData requestData;
     @Inject InputElement<String> txtEditTaskName;
     @Inject InputElement<Integer> txtEditTaskPercentage;
     @Inject InputElement<Date> txtEditTaskStartDate;
     @Inject InputElement<Date> txtEditTaskEndDate;
     @Inject InputElement<Date> txtEditTaskActualStartDate;
     @Inject InputElement<Date> txtEditTaskActualEndDate;
     @Inject InputElement<String> editTaskId;
 
 
     @Override
     public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
 
         logger.info("Start task edit form validation");
         Task task = em.find(Task.class, editTaskId.getValue());
         Project project = task.getProject();
         Task parentTask = task.getParentTask();
         Task preTask = task.getPreTask();
 
         // Start Date can't earyly than project start date.
         if(txtEditTaskStartDate.getValue() != null ){
 
             if(project != null){
                 if(txtEditTaskStartDate.getValue().compareTo(project.getStartDate()) <0 ){
                     // Error.
                     doError(txtEditTaskStartDate, "Invalid task start/end dates !", "Task start date must be after project start date");
                 }
             }
 
             if(parentTask != null){
                 if(txtEditTaskStartDate.getValue().compareTo(parentTask.getTaskEstimatedStartDate()) <0 ){
                     // Error.
                     doError(txtEditTaskStartDate, "Invalid task start/end dates !", "Task start date must be after parent task start date");
                 }
                if(txtEditTaskEndDate.getValue().compareTo(parentTask.getTaskEstimatedEndDate()) >0 ){
                    // Error.
                    doError(txtEditTaskEndDate, "Invalid task start/end dates !", "Task end date can't be after parent task end date");
                }
             }
 
             // Check for the pretask.
             if(preTask  != null ){
                 // Check for the date.
                 if (! (txtEditTaskStartDate.getValue().compareTo(preTask.getTaskEstimatedEndDate()) > 0)){
                     // Error.
                     doError(txtEditTaskStartDate, "Invalid task start/end dates !", "Task start date must be after pre-task end date");
                 }
             }
         }
 
         Date estimatedStartDate = txtEditTaskStartDate.getValue();
         Date estimatedEndDate = txtEditTaskEndDate.getValue();
         Date actualStartDate = txtEditTaskActualStartDate.getValue();
         Date actualEndDate = txtEditTaskActualEndDate.getValue();
 
         // Check for the start/end date.
         if(estimatedStartDate != null && estimatedEndDate != null){
             if(estimatedStartDate.compareTo(estimatedEndDate) > 0){
                 // Start date can't be after end date.
                 doError(txtEditTaskStartDate, "Invalid task start/end dates !", "Task estimated start date must be before estimated end date");
             }
         }
 
         logger.info("Validate actual start/end date: " + actualStartDate + "/" + actualEndDate);
 
 
         if(actualStartDate != null && actualEndDate != null){
             // Check for time period.
             if(actualStartDate.compareTo(actualEndDate) > 0){
                 logger.info("ask end date must be after task start date: " + (actualStartDate.compareTo(actualEndDate) > 0));
                 // Wrong
                 doError(txtEditTaskActualEndDate, "Invalid task start/end dates !", "Task end date must be after task start date ");
             }
         }
 
         // Check for pre tasks.
         if(requestData.getPreTaskList() != null && requestData.getPreTaskList().size() > 0){
             preTask = requestData.getPreTaskList().get(0);
             if(preTask != null){
                 Date preTaskEndDate = preTask.getTaskActualEndDate() == null ? preTask.getTaskEstimatedEndDate() : preTask.getTaskActualEndDate();
 
                 logger.info("Compare to pretask: " + preTask.toString());
                 if( actualStartDate != null){
                     if( ! (actualStartDate.compareTo(preTaskEndDate) > 0 )){
                         // Error
                         doError(txtEditTaskActualStartDate, "Invalid task start/end dates !", "Task actual start date must be after pre-task end date");
                     }
                 }
             }
         }
 
     }
 }
