 // SimulationManager.java
 package org.eclipse.stem.jobs.simulation;
 
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
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.stem.core.Utility;
 import org.eclipse.stem.core.common.DublinCore;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.scenario.Scenario;
 import org.eclipse.stem.core.scenario.ScenarioPackage;
 import org.eclipse.stem.core.sequencer.Sequencer;
 import org.eclipse.stem.jobs.Activator;
 import org.eclipse.stem.jobs.execution.ExecutableManager;
 import org.eclipse.stem.jobs.preferences.PreferenceConstants;
 import org.eclipse.swt.widgets.Display;
 
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
  * The {@link SimulationManager} maintains a sequence number and assigns
  * successive values of that number to the {@link Simulation} instances it
  * creates.
  */
 public class SimulationManager extends ExecutableManager implements
 		ISimulationListener {
 
 	/**
 	 * Singleton instance of the manager
 	 */
 	private static SimulationManager INSTANCE = null;
 
 	/**
 	 * This is the sequence number assigned to each successive
 	 * {@link Simulation} instance the manager creates.
 	 */
 	private static int sequenceNumber = 0;
 
 	/**
 	 * The collection of {@link ISimulationManagerListener}'s waiting to be told
 	 * about {@link SimulationManagerEvent}s
 	 */
 	private final List<ISimulationManagerListener> listeners = new CopyOnWriteArrayList<ISimulationManagerListener>();
 
 	/**
 	 * The collection of {@link ISimulationManagerListenerSync}'s waiting to be
 	 * told about {@link SimulationManagerEvent}s
 	 */
 	private final List<ISimulationManagerListenerSync> listenersSync = new CopyOnWriteArrayList<ISimulationManagerListenerSync>();
 
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
 	private SimulationManager() {
 		activeSimulations = new ArrayList<ISimulation>();
 
 		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
 				ScenarioPackage.eNAME, new XMIResourceFactoryImpl());
 	} // SimulationManager
 
 	/**
 	 * @return the singleton instance of the model
 	 */
 	public static final SimulationManager getManager() {
 		if (INSTANCE == null) {
 			INSTANCE = new SimulationManager();
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
 		simulation.addSimulationListener(this);
 		fireSimulationManagerChanged(new ISimulation[] { simulation }, NONE);
 	} // addActiveSimulation
 
 	/**
 	 * Remove an {@link ISimulation} from the collection of active
 	 * {@link ISimulation}s.
 	 * 
 	 * @param simulation
 	 *            the {@link ISimulation} to remove
 	 */
 	private final void removeActiveSimulation(final ISimulation simulation) {
 		activeSimulations.remove(simulation);
 		// We're no longer a listener
 		simulation.removeSimulationListener(this);
 		fireSimulationManagerChanged(NONE, new ISimulation[] { simulation });
 	} // removeActiveSimulation
 
 	/**
 	 * Create a {@link ISimulation} that's ready to run.
 	 * 
 	 * @param configurationElement
 	 *            a {@link IConfigurationElement} that specifies the details of
 	 *            a serialized {@link Scenario} that is in a plug-in
 	 * @param monitor
 	 *            a progress monitor
 	 * @return an {@link ISimulation} constructed from the
 	 *         {@link IConfigurationElement}, or null if there was a problem.
 	 * 
 	 * @Deprecated
 	 */
 	public ISimulation createSimulation(
 			final IConfigurationElement configurationElement,
 			final IProgressMonitor monitor) {
 		ISimulation retValue = null;
 
 		final String scenarioURIString = configurationElement
 				.getAttribute(DublinCore.IDENTIFIER);
 
 		try {
 			retValue = createSimulation(URI.createURI(scenarioURIString),
 					monitor);
 		} catch (final Exception e) {
 			Activator
 					.logError(
 							MessageFormat
 									.format(
 											Messages
 													.getString("SimMgr.Deserialization_Error"), scenarioURIString), e); //$NON-NLS-1$
 			retValue = null;
 		}
 		return retValue;
 	} // createSimulation
 
 	/**
 	 * Create an {@link ISimulation} that's ready to run.
 	 * 
 	 * @param scenarioURI
 	 *            the URI of a serialized {@link Scenario}
 	 * @param monitor
 	 *            a progress monitor
 	 * @return an {@link ISimulation} constructed from the URI, or
 	 *         <code>null</code> if there was a problem.
 	 *         
 	 * @Deprecated
 	 */
 	public ISimulation createSimulation(final URI scenarioURI,
 			final IProgressMonitor monitor) {
 		ISimulation retValue = null;
 
 			try {
 				
 					// Read scenario
 				monitor.subTask("Reading Scenario from file");
 				Scenario simulationScenario = (Scenario)Utility.getIdentifiable(scenarioURI);
 				
 				monitor.subTask("Creating Simulation from Scenario");
 				retValue = createSimulation(simulationScenario, monitor);
 			} catch (final Exception e) {
 				Activator
 						.logError(
 								MessageFormat
 										.format(
 												Messages
 														.getString("SimMgr.Deserialization_Error"), scenarioURI.toString()), e); //$NON-NLS-1$
 			retValue = null;
 		}
 		return retValue;
 	} // createSimulation
 
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
 			final Preferences preferences = 
 				org.eclipse.stem.jobs.Activator.getDefault()
 					.getPluginPreferences();
 			boolean useCache = preferences
 					.getBoolean(PreferenceConstants.USE_SCENARIOS_CACHING_BOOLEAN);
 
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
 			
 			final Simulation simulation = new Simulation(scenario,
 					getAndIncrementSimulationSequenceNumber());
 			
 			if(scenario.getCanonicalGraph() == null) 
 				scenario.initialize(); // needed for preferences ...
 			else scenario.reset();
 	
 			simulation.setPriority(Job.LONG);
 			retValue = new SimulationAdapter(simulation);
 
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
 	 * Create a {@link ISimulation} from a {@link IConfigurationElement} and
 	 * then start it running.
 	 * 
 	 * @param configurationElement
 	 *            a {@link IConfigurationElement} that specifies the details of
 	 *            a serialized {@link Scenario} that is in a plug-in
 	 * 
 	 *@Deprecated
 	 */
 	public void createAndRunSimulation(
 			final IConfigurationElement configurationElement) {
 		new Job(Messages.getString("SimMgr.Start_Sim")) { //$NON-NLS-1$
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				monitor.beginTask(Messages.getString("SimMgr.CrtSim"),
 						IProgressMonitor.UNKNOWN);
 				try {
 					final ISimulation simulation = createSimulation(
 							configurationElement, monitor);
 					monitor.subTask(Messages.getString("SimMgr.Run"));
 					simulation.run();
 				} // try
 				catch (final NullPointerException e) {
 					// The error was logged in createSimulation
 					monitor.done();
 				}
 				monitor.done();
 				return Status.OK_STATUS;
 			} // run
 		}.schedule();
 	} // createAndRunSimulation
 
 	/**
 	 * Create a {@link ISimulation} from a {@link Scenario} instance and then
 	 * start it running.
 	 * 
 	 * @param scenario
 	 *            the {@link Scenario} to be simulated
 	 */
 	public void createAndRunSimulation(final Scenario scenario) {
 		new Job(Messages.getString("SimMgr.Start_Sim")) {
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				try {
 					final ISimulation simulation = createSimulation(scenario, monitor);
 					monitor.subTask(Messages.getString("SimMgr.Run"));
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
 	 * Create an {@link ISimulation} from a {@link IConfigurationElement} and
 	 * then start it running.
 	 * 
 	 * @param configurationElement
 	 *            an {@link IConfigurationElement} that specifies the details of
 	 *            a serialized {@link Scenario} that is in a plug-in
 	 * 
 	 * @Deprecated
 	 */
 	public void createAndStepSimulation(
 			final IConfigurationElement configurationElement) {
 		new Job(Messages.getString("SimMgr.Start_Sim")) { //$NON-NLS-1$
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				try {
 					final ISimulation simulation = createSimulation(
 							configurationElement, monitor);
 					monitor.subTask(Messages.getString("SimMgr.Run"));
 					simulation.step();
 				} // try
 				catch (final NullPointerException e) {
 					// The error was logged in createSimulation
 					monitor.done();
 				}
 				return Status.OK_STATUS;
 			} // run
 		}.schedule();
 	} // createAndStepSimulation
 	
 	/**
 	 * Create an {@link ISimulation} from a {@link Scenario} instance and then
 	 * start it running.
 	 * 
 	 * @param scenario
 	 *            the {@link Scenario} to be simulated
 	 */
 	public void createAndStepSimulation(final Scenario scenario) {
 		new Job(Messages.getString("SimMgr.Start_Sim")) { //$NON-NLS-1$
 			@Override
 			protected IStatus run(final IProgressMonitor monitor) {
 				try {
 					final ISimulation simulation = createSimulation(scenario, monitor);
 					monitor.subTask(Messages.getString("SimMgr.Run"));
 					simulation.step();
 				} // try
 				catch (final NullPointerException e) {
 					// The error was logged in createSimulation
 					monitor.done();
 				}
 				return Status.OK_STATUS;
 			} // run
 		}.schedule();
 	} // createAndStepSimulation
 
 	
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
 
 	/**
 	 * @param listener
 	 *            a listener wanting to be told about changes to the manager.
 	 */
 	public void addSimulationManagerListener(
 			final ISimulationManagerListener listener) {
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	} // addSimulationManagerListener
 
 	/**
 	 * @param listener
 	 *            a listener wanting to be told about changes to the manager.
 	 */
 	public void addSimulationManagerListenerSync(
 			final ISimulationManagerListenerSync listener) {
 		if (!listenersSync.contains(listener)) {
 			listenersSync.add(listener);
 		}
 	} // addSimulationManagerListenerSync
 
 	/**
 	 * @param listener
 	 *            a listener wanting NOT to be told about changes to the
 	 *            manager.
 	 */
 	public void removeListener(final ISimulationManagerListener listener) {
 		listeners.remove(listener);
 	} // removeListener
 
 	/**
 	 * @param listener
 	 *            a listener NOT wanting to be told about changes to the
 	 *            manager.
 	 */
 	public void removeListenerSync(final ISimulationManagerListenerSync listener) {
 		listenersSync.remove(listener);
 	} // removeListener
 
 	/**
 	 * Tell the listeners about the change.
 	 * 
 	 * @param simulationsAdded
 	 *            the {@link ISimulation}s added
 	 * @param simulationsRemoved
 	 *            the {@link ISimulation}s removed
 	 */
 	private void fireSimulationManagerChanged(
 			final ISimulation[] simulationsAdded,
 			final ISimulation[] simulationsRemoved) {
 		final SimulationManagerEvent event = new SimulationManagerEvent(this,
 				simulationsAdded, simulationsRemoved);
 
 		for (final ISimulationManagerListener listener : listeners) {
 			try {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						listener.simulationsChanged(event);
 					}
 				});
 			} catch (final Error ncdfe) {
 				// If we get this exception, it is because we're not running in
 				// eclipse. Just fire the event.
 				listener.simulationsChanged(event);
 			} // catch
 		} // for
 
 		for (final ISimulationManagerListenerSync listener : listenersSync) {
 			try {
 				Display.getDefault().syncExec(new Runnable() {
 					public void run() {
 						listener.simulationsChangedSync(event);
 					}
 				});
 			} catch (final Error ncdfe) {
 				// If we get this exception, it is because we're not running in
 				// eclipse. Just fire the event.
 				listener.simulationsChangedSync(event);
 			} // catch
 		} // for
 	} // fireSimulationManagerChanged
 
 	/**
 	 * This is where the manager hears about changes in the {@link ISimulation}s
 	 * it is managing. It is really only interested in those that "stop" so it
 	 * can remove them from its collection of active {@link ISimulation}s.
 	 * 
 	 * @see org.eclipse.stem.jobs.simulation.ISimulationListener#simulationChanged(org.eclipse.stem.jobs.simulation.SimulationEvent)
 	 */
 	public void simulationChanged(final SimulationEvent event) {
 		// Has a simulation stopped?
 		if (event.getSimulationState().equals(SimulationState.STOPPED)) {
 			// Yes
 			if (event.getSimulation() instanceof SimulationAdapter) {
 				SimulationAdapter stoppedSimulationAdapter = (SimulationAdapter)event.getSimulation();
 				Simulation stoppedSimulation = (Simulation)stoppedSimulationAdapter.getSimulation();
 				Activator.getDefault().getPluginPreferences().removePropertyChangeListener(stoppedSimulation);
 			}
 			removeActiveSimulation(event.getSimulation());
 		}
 	} // simulationChanged
 
 	/**
 	 * This class adapts a {@link Simulation} instance that runs as an
 	 * asynchronous Eclipse {@link Job} to be an {@link ISimulation} instance.
 	 * The adapter manages thread safety issues so that the UI thread can access
 	 * the {@link ISimulation} instance directly.
 	 */
 	public static class SimulationAdapter implements ISimulation,
 			ISimulationListener, ISimulationListenerSync {
 
 		/**
 		 * The collection of {@link ISimulationListener}'s waiting to be told
 		 * about {@link SimulationEvent}s.
 		 */
 		private final List<ISimulationListener> listeners = new CopyOnWriteArrayList<ISimulationListener>();
 
 		/**
 		 * The collection of {@link ISimulationListenerSync}'s waiting to be
 		 * told about {@link SimulationEvent}s.
 		 */
 		private final List<ISimulationListenerSync> listenersSync = new CopyOnWriteArrayList<ISimulationListenerSync>();
 
 		/**
 		 * The {@link Simulation} instance to adapt. This will be an instance of
 		 * {@link Simulation} which is also an eclipse {@link Job} and runs
 		 * asynchronously from the UI thread of eclipse. This adapter registers
 		 * as a listener of adapted {@link Simulation} and safely passes
 		 * {@link SimulationEvent}'s on to listeners in the UI or other threads.
 		 */
 		private final ISimulation simulation;
 
 		/**
 		 * Constructor
 		 * 
 		 * @param simulation
 		 *            the {@link Simulation} to adapt
 		 */
 		protected SimulationAdapter(final ISimulation simulation) {
 			this.simulation = simulation;
 			simulation.addSimulationListener(this);
 			simulation.addSimulationListenerSync(this);
 		} // SimulationAdapter
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#getName()
 		 */
 		public String getName() {
 			return simulation.getName();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#getNameWithSequenceNumber()
 		 */
 		public String getNameWithSequenceNumber() {
 			return simulation.getNameWithSequenceNumber();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.execution.IExecutable#getCreationTime()
 		 */
 		public Date getCreationTime() {
 			return simulation.getCreationTime();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.execution.IExecutable#getUniqueIDString()
 		 */
 		public String getUniqueIDString() {
 			return simulation.getUniqueIDString();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#getScenario()
 		 */
 		public Scenario getScenario() {
 			return simulation.getScenario();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#getSimulationState()
 		 */
 		public SimulationState getSimulationState() {
 			return simulation.getSimulationState();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#getSequenceNumber()
 		 */
 		public int getSequenceNumber() {
 			return simulation.getSequenceNumber();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#setSequenceNumber(int)
 		 */
 		public void setSequenceNumber(final int sequenceNumber) {
 			simulation.setSequenceNumber(sequenceNumber);
 		} // setSequenceNumber
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#pause()
 		 */
 		public void pause() {
 			simulation.pause();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#reset()
 		 */
 		public void reset() {
 			simulation.reset();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#run()
 		 */
 		public void run() {
 			simulation.run();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#step()
 		 */
 		public void step() {
 			simulation.step();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#stop()
 		 */
 		public void stop() {
 			simulation.stop();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.execution.IExecutable#isRunning()
 		 */
 		public boolean isRunning() {
 			return simulation.isRunning();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.execution.IExecutable#isStoppable()
 		 */
 		public boolean isStoppable() {
 			return simulation.isStoppable();
 		}
 
 		/**
 		 * @throws InterruptedException
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#join()
 		 */
 		public void join() throws InterruptedException {
 			simulation.join();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#cancel()
 		 */
 		public boolean cancel() {
 			return simulation.cancel();
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#addSimulationListener(org.eclipse.stem.jobs.simulation.ISimulationListener)
 		 */
 		public void addSimulationListener(final ISimulationListener listener) {
 			if (!listeners.contains(listener)) {
 				listeners.add(listener);
 			}
 		} // addSimulationListener
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#removeSimulationListener(org.eclipse.stem.jobs.simulation.ISimulationListener)
 		 */
 		public void removeSimulationListener(final ISimulationListener listener) {
 			listeners.remove(listener);
 		} // removeSimulationListener
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#addSimulationListenerSync(org.eclipse.stem.jobs.simulation.ISimulationListenerSync)
 		 */
 		public void addSimulationListenerSync(
 				final ISimulationListenerSync listener) {
 			if (!listenersSync.contains(listener)) {
 				listenersSync.add(listener);
 			}
 		}
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulation#removeSimulationListenerSync(org.eclipse.stem.jobs.simulation.ISimulationListenerSync)
 		 */
 		public void removeSimulationListenerSync(
 				final ISimulationListenerSync listener) {
 			listenersSync.remove(listener);
 		}
 
 		/**
 		 * Tell the listeners about the change in the {@link Simulation}s state
 		 * 
 		 * @param simulationState
 		 *            the new state of the {@link Simulation}
 		 */
 		protected void fireSimulationChanged(
 				final SimulationEvent event) {
 			final SimulationEvent ev = new SimulationEvent(this,
 					event.getSimulationState(), event.getIterationProgress());
 			for (final ISimulationListener listener : listeners) {
 				listener.simulationChanged(ev);
 			} // for
 		} // fireSimulationManagerChanged
 
 		/**
 		 * Tell the listeners about the change in the {@link Simulation}s state
 		 * 
 		 * @param simulationState
 		 *            the new state of the {@link Simulation}
 		 */
 		protected void fireSimulationChangedSync(
 				final SimulationEvent event) {
 			final SimulationEvent ev = new SimulationEvent(this,
 					event.getSimulationState(), event.getIterationProgress());
 			for (final ISimulationListenerSync listener : listenersSync) {
 				listener.simulationChangedSync(ev);
 			} // for
 		} // fireSimulationChangedSync
 
 		/**
 		 * @see java.lang.Object#toString()
 		 */
 		@Override
 		public String toString() {
 			return simulation.toString();
 		} // toString
 
 		/**
 		 * This is where the adapted {@link Simulation} tells us of its state
 		 * changes and we need to adapt them to the UI thread.
 		 * 
 		 * @see org.eclipse.stem.jobs.simulation.ISimulationListener#simulationChanged(org.eclipse.stem.jobs.simulation.SimulationEvent)
 		 */
 		public void simulationChanged(final SimulationEvent event) {
 			try {
 				if (!Display.getDefault().isDisposed()) {
 					// Yes
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							fireSimulationChanged(event);
 						}
 					});
 				} // if
 			} // try
 			catch (final NullPointerException e) {
 				// Nothing to do, shutting down...
 			} // catch NullPointerException
 			catch (final Error ncdfe) {
 				// If we get this exception, it is because we're not running in
 				// eclipse. Just fire the event.
 				fireSimulationChanged(event);
 			} // catch
 
 		} // simulationChanged
 
 		/**
 		 * @see org.eclipse.stem.jobs.simulation.ISimulationListenerSync#simulationChangedSync(org.eclipse.stem.jobs.simulation.SimulationEvent)
 		 */
 		public void simulationChangedSync(final SimulationEvent event) {
 			try {
 				if (!Display.getDefault().isDisposed()) {
 					// Yes
 					Display.getDefault().syncExec(new Runnable() {
 						public void run() {
 							fireSimulationChangedSync(event);
 						}
 					});
 				} // if
 			} // try
 			catch (final NullPointerException e) {
 				// Nothing to do, shutting down...
 			} // catch NullPointerException
 			catch (final Error ncdfe) {
 				// If we get this exception, it is because we're not running in
 				// eclipse. Just fire the event.
 				fireSimulationChangedSync(event);
 			} // catch
 
 		} // simulationChangedSync
 
 		/**
 		 * @return the simulation
 		 */
 		public ISimulation getSimulation() {
 			return simulation;
 		}
 
 	} // SimulationAdapter
 } // SimulationManager
