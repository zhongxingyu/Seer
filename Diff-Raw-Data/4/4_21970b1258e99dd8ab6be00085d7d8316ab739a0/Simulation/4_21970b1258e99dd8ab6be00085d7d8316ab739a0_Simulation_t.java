 package org.abm.averageskill.simulation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.abm.averageskill.event.AllWorkCompletedEvent;
 import org.abm.averageskill.event.SimulationTerminatedEvent;
 import org.abm.averageskill.event.TimeoutEvent;
 
 // What can I ignore right now / or what's the weakest assumption I can make
 // right now.
 // Better yet when I start making an assumption ask myself can I make a
 // weaker assumption!
 
 //Is this a simulation
 public class Simulation implements TimeoutListener {
 	private boolean stopped = false;
 	private final List<SimulationTerminationListener> terminationListeners = new ArrayList<SimulationTerminationListener>();
 
 	public Simulation(SimulationTerminationListener terminationListener) {
 		this.terminationListeners.add(terminationListener);
 	}
 
 	public Simulation() {
 	}
 
 	private void stopTheSimulationAtUnlessItAlreadyStopped(int stoppingTime) {
 		if (!stopped) {
 			for (SimulationTerminationListener terminationListener : terminationListeners) {
 				terminationListener.onTermination(SimulationTerminatedEvent.at(stoppingTime));
 			}
 			stopped = true;
 		}
 	}
 
 	public void onWorkCompleted(AllWorkCompletedEvent event) {
 		stopTheSimulationAtUnlessItAlreadyStopped(event.getTicks());
 	}
 
 	@Override
 	public void onTimeout(TimeoutEvent event) {
 		stopTheSimulationAtUnlessItAlreadyStopped(event.getTicks());
 	}
 
 	public void addTerminationListener(SimulationTerminationListener terminationListener) {
 		terminationListeners.add(terminationListener);
 	}
 
 	public Results run(Config config) {
 		if (config.getWorkers() == 2 && config.getWorkOrders() == 2) {
 			boolean[] workerA = new boolean[] { false, false };
 			boolean[] workerB = new boolean[] { false, false };
 
 			float time = 0f;
 			int numItemsInWorkerAInbox = config.getWorkOrders();
 			List<Object> itemsComplete = new ArrayList<Object>();
 			List<Object> workerBInbox = new ArrayList<Object>();
 			time = 0f;
 			numItemsInWorkerAInbox--;
 			workerA[0] = true;
 			workerA[1] = false;
 
 			time = complete(config, workerB, time);
 			workerA[0] = false;
 			workerA[1] = true;
 
 			time += config.getTransitionTime();
 			workerBInbox.add(new Object());
 			numItemsInWorkerAInbox--;
 			workerA[0] = true;
 			workerA[1] = false;
 
 			workerBCompletesItem(workerB, itemsComplete);
 			if (!workerBInbox.isEmpty() && workerB[0] == false) {
 				workerBInbox.remove(0);
 				workerB[0] = true;
 				workerB[1] = false;
 			}
 
 			time = complete(config, workerB, time);
 			workerA[0] = false;
 			workerA[1] = true;
 
 			time += config.getTransitionTime();
 			workerBInbox.add(new Object());
 			workerA[0] = false;
 			workerA[1] = false;
 			workerBCompletesItem(workerB, itemsComplete);
 			if (!workerBInbox.isEmpty() && workerB[0] == false) {
 				workerBInbox.remove(0);
 				workerB[0] = true;
 				workerB[1] = false;
 			}
 
 			time = complete(config, workerB, time);
 			workerA[0] = false;
 			workerA[1] = false;
 
 			time += config.getTransitionTime();
 			workerA[0] = false;
 			workerA[1] = false;
 			workerBCompletesItem(workerB, itemsComplete);
 			if (!workerBInbox.isEmpty() && workerB[0] == false) {
 				workerBInbox.remove(0);
 				workerB[0] = true;
 				workerB[1] = false;
 			}
 			boolean workerAStillWorkingOnStuff = workerA[0] || workerA[1];
 			boolean workerBStillWorkingOnStuff = workerB[0] || workerB[1];
 			if (workerAStillWorkingOnStuff || workerBStillWorkingOnStuff) {
 				throw new RuntimeException("everyone whould be done");
 			}
 			return new Results(itemsComplete.size(), time);
 		}
 		float time = completeAWorkOrder(config) * config.getWorkers() * config.getWorkOrders();
 		return new Results(config.getWorkOrders(), time);
 	}
 
 	public float complete(Config config, boolean[] workerB, float time) {
 		time += config.getCompletionTime();
 		if (workerB[0] == true) {
 			workerB[0] = false;
 			workerB[1] = true;
 		}
 		return time;
 	}
 
 	public void workerBCompletesItem(boolean[] workerB, List<Object> itemsComplete) {
 		if (workerB[1] == true) {
			workerB[1] = false;
 			itemsComplete.add(new Object());
 		}
 	}
 
 	private Results runSimulationForNoOverlappingWork(Config config) {
 		float time = 0;
 		for (int i = 0; i < config.getWorkOrders() - 1; i++) {
 			time += howLongTheBottleneckIsBusy(config);
 		}
 		time += completeAWorkOrder(config);
 		return new Results(config.getWorkOrders(), time);
 	}
 
 	private int howLongTheBottleneckIsBusy(Config config) {
 		// So far, every worker is equally the bottleneck
 		return howLongAIsBusy(config);
 	}
 
 	private int howLongAIsBusy(Config config) {
 		return config.getCompletionTime() + config.getTransitionTime();
 	}
 
 	private int completeAWorkOrder(Config config) {
 		return config.getWorkers() * (config.getCompletionTime() + config.getTransitionTime());
 	}
 }
