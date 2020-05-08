 // SimulationManager.java
 package org.eclipse.stem.analysis.automaticexperiment;
 
 /*******************************************************************************
  * Copyright (c) 2006, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.scenario.ScenarioPackage;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.jobs.execution.ExecutableManager;
 import org.eclipse.stem.jobs.simulation.ISimulation;
 import org.eclipse.stem.jobs.simulation.Messages;
 import org.eclipse.stem.jobs.simulation.Simulation;
 import org.eclipse.stem.jobs.simulation.SimulationCaching;
 import org.eclipse.stem.jobs.simulation.SimulationEvent;
 
 /**
  * This class manages the life-cycle of active {@link Simulation}s. There is a
  * singleton instance of the manager that is referenced by other parts of the
  * system to manage {@link Simulation}. It creates {@link Simulation} instances
  * from {@link Scenario} instances and can obtain {@link Scenario} instances
  * from file URI's or from {@link IConfigurationElement}'s. It maintains a
  * collection of all of the active {@link Simulation}s in the system and
  * generates {@link SimulationEvent}'s whenever a {@link Simulation} is added to
  * that collection or removed.
  * <p>
  * The {@link CustomSimulationManager} maintains a sequence number and assigns
  * successive values of that number to the {@link Simulation} instances it
  * creates.
  */
 public class CustomSimulationManager extends ExecutableManager {
 
 	/**
 	 * Singleton instance of the manager
 	 */
 	private static CustomSimulationManager INSTANCE = null;
 
 	/**
 	 * This is the sequence number assigned to each successive
 	 * {@link Simulation} instance the manager creates.
 	 */
 	private static int sequenceNumber = 0;
 
 	/**
 	 * Constant empty array.
 	 */
 	public static final ISimulation[] NONE = new Simulation[] {};
 
 	/**
 	 * This is the collection of active {@link Simulation} jobs
 	 */
 	private List<ISimulation> activeSimulations = null;
 
 	/**
 	 * Constructor
 	 */
 	private CustomSimulationManager() {
 		activeSimulations = new ArrayList<ISimulation>();
 
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				ScenarioPackage.eNAME, new XMIResourceFactoryImpl());
 	} // SimulationManager
 
 	/**
 	 * @return the singleton instance of the model
 	 */
 	public static final CustomSimulationManager getManager() {
 		if (INSTANCE == null) {
 			INSTANCE = new CustomSimulationManager();
 		}
 		return INSTANCE;
 	} // getModel
 
 	/**
 	 * Return the next simulation sequence number and increment the value.
 	 * 
 	 * @return the next simulation sequence number
 	 */
 	synchronized private static final int getAndIncrementSimulationSequenceNumber() {
 		return sequenceNumber++;
 	} // getAndIncrementSimulationSequenceNumber
 
 	/**
 	 * This is used for testing purposes to reset the state of the model.
 	 */
 	public static final void resetSimulationManager() {
 		INSTANCE = null;
 		sequenceNumber = 0;
 	} // resetSimulationManager
 
 	/**
 	 * @return the active {@link Simulation}s
 	 */
 	public final List<ISimulation> getActiveSimulations() {
 		return activeSimulations;
 	} // getActiveSimulations
 
 	/**
 	 * Add a {@link Simulation} to the collection of active {@link Simulation}s.
 	 * 
 	 * @param simulation
 	 *            the {@link ISimulation} to add
 	 */
 	private final void addActiveSimulation(final ISimulation simulation) {
 		activeSimulations.add(simulation);
 	} // addActiveSimulation
 
 	/**
 	 * Remove an {@link ISimulation} from the collection of active
 	 * {@link ISimulation}s.
 	 * 
 	 * @param simulation
 	 *            the {@link ISimulation} to remove
 	 */
 	public final void removeActiveSimulation(final ISimulation simulation) {
 		activeSimulations.remove(simulation);
 	} // removeActiveSimulation
 
 	/**
 	 * Create a {@link ISimulation} from a {@link Scenario} instance.
 	 * 
 	 * @param scenario
 	 *            the {@link Scenario} to simulate
 	 * @param monitor 
 	 * 			  Progress monitor
 	 * @return a {@link ISimulation} that's ready to run.
 	 */
 	public ISimulation createSimulation(final Scenario scenario, final IProgressMonitor monitor) {
 		ISimulation retValue = null;
 		try {
 			boolean useCache = false;
 
 			Scenario simulationScenario = null;
 			
 			if (useCache) {
 				if (SimulationCaching.INSTANCE.isScenarioInCache(scenario.getURI())) {
 					simulationScenario = SimulationCaching.INSTANCE.getCachedScenario(scenario.getURI());
 					boolean running = false;
 					for(ISimulation sim : getActiveSimulations()) {
 						if(sim.getScenario().equals(simulationScenario)) {
 							// The scenario is already running. Copy the whole scenario before resetting
 							Scenario newSimulation = null;
							newSimulation = (Scenario)EcoreUtil.copy(simulationScenario);
							if(simulationScenario.getModel() != null) newSimulation.setModel((Model)EcoreUtil.copy(simulationScenario.getModel()));
							if(simulationScenario.getSequencer() != null) newSimulation.setSequencer((Sequencer)EcoreUtil.copy(simulationScenario.getSequencer()));
 							running = true;
 							break;
 						}
 					}
 					if(!running)simulationScenario.reset(); // safe since canonical graph is set 
 				}
 				else {
 					// Read scenario
 					// Add to cache
 					SimulationCaching.INSTANCE.addScenarioToCache(scenario);
 				}
 				
 			}
 			
 			if(scenario.getCanonicalGraph() == null) 
 				scenario.initialize(); // needed for preferences ...
 			else scenario.reset();
 			final Simulation simulation = new Simulation(scenario,
 					getAndIncrementSimulationSequenceNumber());
 			simulation.setPriority(Job.LONG);
 			retValue = simulation;
 
 			addActiveSimulation(retValue);
 		} catch (final RuntimeException e) {
 			// We could get an exception here if the Scenario doesn't have
 			// a Sequencer or Model which would cause problems when trying to
 			// initialize
 			Simulation.handleException(scenario, scenario.getDublinCore()
 					.getTitle(), true, e);
 			retValue = null;
 		}
 		return retValue;
 	} // createSimulation
 
 	/**
 	 * Create a {@link ISimulation} from a {@link Scenario} instance and then
 	 * start it running.
 	 * 
 	 * @param scenario
 	 *            the {@link Scenario} to be simulated
 	 */
 	public void createAndRunSimulation(final Scenario scenario) {
 		new Job(Messages.getString("SimMgr.Start_Sim")) { //$NON-NLS-1$ 
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				try {
 					final ISimulation simulation = createSimulation(scenario, monitor);
 					monitor.subTask(Messages.getString("SimMgr.Run")); //$NON-NLS-1$ 
 					simulation.run();
 				} catch (final Exception e) {
 					// The error was logged in createSimulation
 					monitor.done();
 				} // catch Exception
 				return Status.OK_STATUS;
 			} // run
 		}.schedule();
 	} // createAndRunSimulation
 	
 	/**
 	 * Given a {@link Graph} find the {@link ISimulation} that created it.
 	 * 
 	 * @param graph
 	 *            an instance of a {@link Graph}
 	 * @return the {@link ISimulation} instance that created the {@link Graph},
 	 *         <code>null</code>, if no match could be found.
 	 */
 	public ISimulation mapGraphToSimulation(final Graph graph) {
 		ISimulation retValue = null;
 		for (final ISimulation simulation : activeSimulations) {
 			final Graph simulationGraph = simulation.getScenario()
 					.getCanonicalGraph();
 			// Is this the one we're looking for?
 			if (graph == simulationGraph) {
 				// Yes
 				retValue = simulation;
 				break;
 			} // if
 		} // for each ISimulation
 		return retValue;
 	} // mapGraphToSimulation
 } // SimulationManager
