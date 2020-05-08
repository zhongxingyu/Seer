 package eu.europeana.uim.gui.cp.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import eu.europeana.uim.api.ActiveExecution;
 import eu.europeana.uim.api.Orchestrator;
 import eu.europeana.uim.api.StorageEngine;
 import eu.europeana.uim.api.StorageEngineException;
 import eu.europeana.uim.gui.cp.client.services.ExecutionService;
 import eu.europeana.uim.gui.cp.shared.ExecutionDTO;
 import eu.europeana.uim.gui.cp.shared.ParameterDTO;
 import eu.europeana.uim.gui.cp.shared.ProgressDTO;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.UimDataSet;
 import eu.europeana.uim.workflow.Workflow;
 
 /**
  * Orchestration service implementation.
  * 
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since May 11, 2011
  */
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class ExecutionServiceImpl extends AbstractOSGIRemoteServiceServlet implements
         ExecutionService {
     private final static Logger log = Logger.getLogger(ExecutionServiceImpl.class.getName());
 
     /**
      * Creates a new instance of this class.
      */
     public ExecutionServiceImpl() {
         super();
     }
 
     private Map<Long, ExecutionDTO> wrappedExecutionDTOs = new HashMap<Long, ExecutionDTO>();
     private Map<String, String>     workflowNames        = new HashMap<String, String>();
 
     @Override
     public List<ExecutionDTO> getActiveExecutions() {
         List<ExecutionDTO> r = new ArrayList<ExecutionDTO>();
 
         java.util.Collection<ActiveExecution<Long>> activeExecutions = null;
         try {
             Orchestrator<Long> orchestrator = (Orchestrator<Long>)getEngine().getRegistry().getOrchestrator();
             activeExecutions = orchestrator.getActiveExecutions();
         } catch (Throwable t) {
             log.log(Level.SEVERE, "Could not query active execution!", t);
         }
 
         if (activeExecutions != null) {
             for (ActiveExecution<Long> execution : activeExecutions) {
                 if (execution != null && execution.getExecution().getId() != null) {
                     Execution<Long> executionBean = execution.getExecution();
                     try {
                         ExecutionDTO exec = getWrappedExecutionDTO(executionBean.getId(),
                                executionBean, execution);
                         r.add(exec);
                     } catch (Throwable t) {
                         log.log(Level.WARNING, "Error in copy data to DTO of execution!", t);
                         wrappedExecutionDTOs.remove(executionBean.getId());
                     }
                 } else {
                     log.log(Level.WARNING, "An active execution or its identifier is null!");
                 }
             }
         } else {
             log.log(Level.WARNING, "Active executions are null!");
         }
 
         return r;
     }
 
     @Override
     public List<ExecutionDTO> getPastExecutions() {
         return getPastExecutions(null);
     }
 
     @Override
     public List<ExecutionDTO> getPastExecutions(String[] workflows) {
         Set<String> filter = new HashSet<String>();
         if (workflows != null) {
             filter.addAll(Arrays.asList(workflows));
         }
 
         List<ExecutionDTO> r = new ArrayList<ExecutionDTO>();
 
         StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorageEngine();
         if (storage == null) {
             log.log(Level.SEVERE, "Storage connection is null!");
             return r;
         }
 
         List<Execution<Long>> executions = null;
         try {
             executions = storage.getAllExecutions();
         } catch (Throwable t) {
             log.log(Level.SEVERE, "Could not query past execution!", t);
         }
 
         if (executions != null) {
             for (Execution<Long> execution : executions) {
                 if (!execution.isActive()) {
                     try {
                         if (filter.isEmpty() || filter.contains(execution.getWorkflow())) {
                            ExecutionDTO exec = getWrappedExecutionDTO(execution.getId(), execution, null);
                             r.add(exec);
                         }
                     } catch (Throwable t) {
                         log.log(Level.WARNING, "Error in copy data to DTO of execution!", t);
                         wrappedExecutionDTOs.remove(execution.getId());
                     }
                 }
             }
 
             Collections.sort(r, new Comparator<ExecutionDTO>() {
                 @Override
                 public int compare(ExecutionDTO o1, ExecutionDTO o2) {
                     return o2.getStartTime().compareTo(o1.getStartTime());
                 }
 
             });
         } else {
             log.log(Level.WARNING, "Past executions are null!");
         }
 
         return r;
     }
 
     @Override
     public ExecutionDTO startCollection(String workflow, Long collection, String executionName,
             Set<ParameterDTO> parameters) {
         StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorageEngine();
         if (storage == null) {
             log.log(Level.SEVERE, "Storage connection is null!");
             return null;
         }
         Orchestrator<Long> orchestrator = (Orchestrator<Long>)getEngine().getRegistry().getOrchestrator();
 
         Collection<Long> c = null;
         try {
             c = storage.getCollection(collection);
         } catch (Throwable t) {
             log.log(Level.SEVERE, "Could not query collection!", t);
         }
         if (c == null) {
             log.log(Level.WARNING, "Collection are null!");
             return null;
         }
 
         eu.europeana.uim.workflow.Workflow w = getWorkflow(workflow);
         ExecutionDTO execution = new ExecutionDTO();
 
         GWTProgressMonitor monitor = new GWTProgressMonitor(execution);
 
         ActiveExecution<Long> ae;
         if (parameters != null) {
             Properties properties = prepareProperties(parameters);
             ae = orchestrator.executeWorkflow(w, c, properties);
         } else {
             ae = orchestrator.executeWorkflow(w, c);
         }
         ae.getMonitor().addListener(monitor);
         if (executionName != null) {
             ae.getExecution().setName(executionName);
         }
         populateWrappedExecutionDTO(execution, ae, w, c, executionName);
 
         return execution;
     }
 
     private Properties prepareProperties(Set<ParameterDTO> parameters) {
         Properties properties = new Properties();
         for (ParameterDTO parameter : parameters) {
             if (parameter.getValues() != null) {
                 if (parameter.getValues().length > 1) {
                     StringBuilder b = new StringBuilder();
                     for (String val : parameter.getValues()) {
                         b.append(val);
                         b.append(",");
                     }
                     b.deleteCharAt(b.length() - 1);
                     properties.put(parameter.getKey(), b.toString());
                 } else if (parameter.getValues().length == 1) {
                     properties.put(parameter.getKey(), parameter.getValues()[0]);
                 }
             }
         }
         return properties;
     }
 
     @Override
     public ExecutionDTO startProvider(String workflow, Long provider, String executionName,
             Set<ParameterDTO> parameters) {
 // try {
 // StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorageEngine();
 // Provider<Long> p = storage.getProvider(provider);
 // if (p == null) { throw new RuntimeException("Error: cannot find provider " + provider); }
 // eu.europeana.uim.workflow.Workflow w = getWorkflow(workflow);
         ExecutionDTO execution = new ExecutionDTO();
 // GWTProgressMonitor monitor = new GWTProgressMonitor(execution);
 
 // FIXME
 // ActiveExecution<Long> ae;
 // if (parameters != null) {
 // Properties properties = prepareProperties(parameters);
 // ae = (ActiveExecution<Long>)getEngine().getRegistry().getOrchestrator().executeWorkflow(
 // w, p, properties);
 // } else {
 // ae = (ActiveExecution<Long>)getEngine().getRegistry().getOrchestrator().executeWorkflow(
 // w, p);
 // }
 // ae.setName(executionName);
 //
 // ae.getMonitor().addListener(monitor);
 //
 // populateWrappedExecutionDTO(execution, ae, w, p, executionName);
         return execution;
 // } catch (StorageEngineException e) {
 // e.printStackTrace();
 // }
 // return null;
     }
 
     private void populateWrappedExecutionDTO(ExecutionDTO execution, ActiveExecution<Long> ae,
             eu.europeana.uim.workflow.Workflow w, UimDataSet<Long> dataset, String executionName) {
         execution.setId(ae.getExecution().getId());
         execution.setName(executionName);
 
         execution.setWorkflow(getWorkflowName(ae.getExecution().getWorkflow()));
 
         execution.setCompleted(ae.getCompletedSize());
         execution.setFailure(ae.getFailureSize());
         execution.setScheduled(ae.getScheduledSize());
         execution.setCanceled(ae.getExecution().isCanceled());
         execution.setStartTime(ae.getExecution().getStartTime());
         execution.setDataSet(dataset.toString());
         if (dataset instanceof Collection) {
             execution.setDataSet(((Collection)dataset).getName());
         } else if (dataset instanceof Provider) {
             execution.setDataSet(((Provider)dataset).getName());
         } else {
             execution.setDataSet(dataset.toString());
         }
 
         ProgressDTO progress = new ProgressDTO();
         progress.setWork(ae.getTotalSize());
         progress.setWorked(ae.getFailureSize() + ae.getCompletedSize());
         progress.setTask(ae.getMonitor().getTask());
         progress.setSubtask(ae.getMonitor().getSubtask());
 
         execution.setProgress(progress);
 
         wrappedExecutionDTOs.put(ae.getExecution().getId(), execution);
     }
 
     @Override
     public ExecutionDTO getExecution(Long id) {
         StorageEngine<Long> storage = (StorageEngine<Long>)getEngine().getRegistry().getStorageEngine();
         if (storage == null) {
             log.log(Level.SEVERE, "Storage connection is null!");
             return null;
         }
         Orchestrator<Long> orchestrator = (Orchestrator<Long>)getEngine().getRegistry().getOrchestrator();
 
         ExecutionDTO exec = null;
         ActiveExecution<Long> ae = orchestrator.getActiveExecution(id);
         if (ae != null) {
            exec = getWrappedExecutionDTO(ae.getExecution().getId(), ae.getExecution(), ae);
         } else {
             Execution<Long> execution;
             try {
                 execution = storage.getExecution(id);
                exec = getWrappedExecutionDTO(execution.getId(), execution, null);
             } catch (StorageEngineException e) {
                 e.printStackTrace();
             }
         }
         return exec;
     }
 
    private ExecutionDTO getWrappedExecutionDTO(Long execution, Execution<Long> e, ActiveExecution<Long> ae) {
         ExecutionDTO wrapped = wrappedExecutionDTOs.get(execution);
         if (wrapped == null) {
             wrapped = new ExecutionDTO();
             wrapped.setId(execution);
             wrapped.setStartTime(e.getStartTime());
             if (e.getDataSet() instanceof Collection) {
                 wrapped.setDataSet(((Collection)e.getDataSet()).getName());
             } else if (e.getDataSet() instanceof Provider) {
                 wrapped.setDataSet(((Provider)e.getDataSet()).getName());
             } else {
                 wrapped.setDataSet(e.getDataSet().toString());
             }
 
             wrapped.setName(e.getName());
 
             wrapped.setWorkflow(getWorkflowName(e.getWorkflow()));
 
             wrapped.setProgress(new ProgressDTO());
             wrappedExecutionDTOs.put(execution, wrapped);
         }
         // update what may have changed
         wrapped.setActive(e.isActive());
         if (e.getEndTime() != null) {
             wrapped.setEndTime(e.getEndTime());
         }
         wrapped.setCanceled(e.isCanceled());
        if (e.isActive() && ae != null) {
             wrapped.setScheduled(ae.getScheduledSize());
             wrapped.setCompleted(ae.getCompletedSize());
             wrapped.setFailure(ae.getFailureSize());
             wrapped.setPaused(ae.isPaused());
 
             ProgressDTO progress = wrapped.getProgress();
             progress.setWork(ae.getTotalSize());
             progress.setWorked(ae.getFailureSize() + ae.getCompletedSize());
             progress.setTask(ae.getMonitor().getTask());
             progress.setSubtask(ae.getMonitor().getSubtask());
             progress.setDone(!e.isActive());
         } else if (!e.isActive()) {
             wrapped.setScheduled(e.getProcessedCount());
             wrapped.setCompleted(e.getSuccessCount());
             wrapped.setFailure(e.getFailureCount());
         }
         return wrapped;
     }
 
     private eu.europeana.uim.workflow.Workflow getWorkflow(String identifier) {
         eu.europeana.uim.workflow.Workflow workflow = getEngine().getRegistry().getWorkflow(
                 identifier);
         if (workflow == null) {
             log.log(Level.WARNING, "There is not workflow '" + identifier + "'!");
         }
         return workflow;
     }
 
     @Override
     public Boolean pauseExecution(Long execution) {
         Orchestrator<Long> orchestrator = (Orchestrator<Long>)getEngine().getRegistry().getOrchestrator();
 
         ActiveExecution<Long> ae = orchestrator.getActiveExecution(execution);
         if (ae != null) {
             orchestrator.pause(ae);
             return ae.isPaused();
         } else {
             return false;
         }
     }
 
     @Override
     public Boolean resumeExecution(Long execution) {
         Orchestrator<Long> orchestrator = (Orchestrator<Long>)getEngine().getRegistry().getOrchestrator();
 
         ActiveExecution<Long> ae = orchestrator.getActiveExecution(execution);
         if (ae != null) {
             orchestrator.resume(ae);
             return !ae.isPaused();
         } else {
             return false;
         }
     }
 
     @Override
     public Boolean cancelExecution(Long execution) {
         Orchestrator<Long> orchestrator = (Orchestrator<Long>)getEngine().getRegistry().getOrchestrator();
 
         ActiveExecution<Long> ae = orchestrator.getActiveExecution(execution);
         if (ae != null) {
             orchestrator.cancel(ae);
             return ae.getExecution().isCanceled();
         } else {
             return false;
         }
     }
 
     private String getWorkflowName(String workflow) {
         synchronized (workflowNames) {
             if (workflowNames.isEmpty()) {
                 List<Workflow> workflows = getEngine().getRegistry().getWorkflows();
                 for (Workflow wf : workflows) {
                     workflowNames.put(wf.getIdentifier(), wf.getName());
                 }
             }
         }
 
         if (workflowNames.containsKey(workflow)) { return workflowNames.get(workflow); }
         return workflow;
     }
 }
