 package br.edu.ufcg.lsd.oursim.events.broker;
 
 import java.util.Set;
 
 import br.edu.ufcg.lsd.oursim.OurSim;
 import br.edu.ufcg.lsd.oursim.entities.grid.Broker;
 import br.edu.ufcg.lsd.oursim.entities.job.ExecutionState;
 import br.edu.ufcg.lsd.oursim.entities.job.Job;
 import br.edu.ufcg.lsd.oursim.entities.job.Replica;
 import br.edu.ufcg.lsd.oursim.entities.job.Task;
 import br.edu.ufcg.lsd.oursim.entities.request.BrokerRequest;
 import br.edu.ufcg.lsd.oursim.events.AbstractEvent;
 import br.edu.ufcg.lsd.oursim.events.Event;
 import br.edu.ufcg.lsd.oursim.events.peer.PauseRequestEvent;
 import br.edu.ufcg.lsd.oursim.events.worker.StartWorkEvent;
 
 public class ScheduleEvent extends AbstractEvent {
 
 	private String brokerId;
 
 	public ScheduleEvent(Long time, String brokerId) {
 		super(time, Event.HIGHER_PRIORITY, null);
 		this.brokerId = brokerId;
 	}
 
 	@Override
 	public void process(OurSim ourSim) {
 		Broker broker = ourSim.getGrid().getObject(brokerId);
 		for (Job job : broker.getJobs()) {
 			schedule(broker, job, ourSim);
 			execute(job, ourSim);
 			clean(broker, job, ourSim);
 		}
 	}
 
 	private void clean(Broker broker, Job job, OurSim ourSim) {
		if (ExecutionState.FAILED.equals(job.getState()) || 
				ExecutionState.FINISHED.equals(job.getState())) {
			job.setRequest(null);
			return;
		}
		
		
 		if (SchedulerHelper.isJobSatisfied(job, ourSim)) {
 			BrokerRequest request = job.getRequest();
 			if (!request.isPaused()) {
 				request.setPaused(true);
 				ourSim.addNetworkEvent(new PauseRequestEvent(
 						getTime(), request.getSpec(), broker.getPeerId()));
 			}
 			
 			for (String worker : job.getAvailableWorkers()) {
 				SchedulerHelper.disposeWorker(job, broker, 
 						worker, ourSim, getTime());
 			}
 		}
 	}
 
 	private void schedule(Broker broker, Job job, OurSim ourSim) {
 		boolean scheduling = true;
 
 		while (scheduling) {
 			scheduling = false;
 
 			for (Task task : job.getTasks()) {
 				if (SchedulerHelper.canSchedule(task, ourSim)) {
 					String chosenWorkerId = null; 
 
 					Set<String> availableWorkers = job.getAvailableWorkers();
 
 					if (!availableWorkers.isEmpty()) {
 						chosenWorkerId = availableWorkers.iterator().next();
 						if (allocate(broker, task, chosenWorkerId, ourSim)) {
 							scheduling = true;
 						}
 					}
 				}
 			}
 		}
 		
 		broker.setScheduled(false);
 	}
 	
 	private boolean allocate(Broker broker, Task task, String chosenWorkerId, OurSim ourSim) {
 		if (!(ExecutionState.RUNNING.equals(task.getJob().getState()) || 
 				ExecutionState.UNSTARTED.equals(task.getJob().getState()))) {
 			return false;
 		}
 		
 		if (!(ExecutionState.RUNNING.equals(task.getState()) || 
 				ExecutionState.UNSTARTED.equals(task.getState()))) {
 			return false;
 		}
 		
 		if (!SchedulerHelper.canSchedule(task, ourSim)) {
 			return false;
 		}
 		
 		task.getJob().workerIsInUse(chosenWorkerId);
 		
 		Replica replica = new Replica();
 		replica.setWorker(chosenWorkerId);
 		replica.setTask(task);
 		replica.setCreationTime(getTime());
 		
 		task.addReplica(replica);
 		replica.setId(replica.getTask().getReplicas().size());
 		
 		task.setState(ExecutionState.RUNNING);
 		task.getJob().setState(ExecutionState.RUNNING);
 		
 		return true;
 	}
 
 	private void execute(Job job, OurSim ourSim) {
 		for (Task task : job.getTasks()) {
 			for (Replica replica : task.getReplicas()) {
 				if (isReadyToRun(replica)) {
 					replica.setState(ExecutionState.RUNNING);
 					ourSim.addNetworkEvent(new StartWorkEvent(
 							getTime(), replica));
 				}
 			}
 		}
 	}
 
 	private boolean isReadyToRun(Replica replica) {
 		return ExecutionState.UNSTARTED.equals(replica.getState())
 				&& replica.getWorker() != null;
 	}
 
 }
