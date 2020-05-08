 package de.ptb.epics.eve.data.scandescription;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.log4j.Logger;
 
 import de.ptb.epics.eve.data.EventTypes;
 import de.ptb.epics.eve.data.PluginTypes;
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * <code>ScanDescription</code> is the representation of a scan. It contains 
  * all components necessary to describe a scan (e.g. chains, scan modules).
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class ScanDescription implements IModelUpdateProvider, 
 									IModelUpdateListener, IModelErrorProvider {
 	
 	private static Logger logger = 
 		Logger.getLogger(ScanDescription.class.getName());
 	
 	/**
 	 * Schema version of the output.
 	 */
 	public static final String outputVersion = "2.0";
 	
 	// version of the scan description.
 	private int inputVersion;
 	
 	// the input revision.
 	private int inputRevision;
 	
 	// the input modification.
 	private int inputModification;
 	
 	// the number of times the scan is repeated.
 	private int repeatCount;
 	
 	// the chains of the scan description.
 	private List<Chain> chains;
 	
 	// the events of the scan description.
 	private Map<String, Event> eventsMap;
 	
 	// the listeners that will be notified if something changed.
 	private List<IModelUpdateListener> modelUpdateListener;
 	
 	// the measuring station used by this scan description.
 	private final IMeasuringStation measuringStation;
 	
 	/**
 	 * Constructs a <code>ScanDescription</code> and adds the S0 start event
 	 * to it's event list.
 	 *
 	 * @param measuringStation the measuring station the scan description is 
 	 * 		  based on
 	 */
 	public ScanDescription(final IMeasuringStation measuringStation) {
 		super();
 		this.chains = new ArrayList<Chain>();
 		//this.events = new ArrayList<Event>();
 		this.eventsMap = new HashMap<String, Event>();
 		this.modelUpdateListener = new ArrayList<IModelUpdateListener>();
 		// default start event
 		Event s0 = new Event(EventTypes.SCHEDULE);
 		s0.setName("Start");
 		this.add(s0);
 		this.measuringStation = measuringStation;
 	}
 
 	/**
 	 * Adds a chain to the scan description. 
 	 * 
 	 * @param chain the chain that should be added
 	 * @return <code>true</code> if the chain was added, 
 	 * 		   <code>false</code> otherwise
 	 */
 	public boolean add(final Chain chain) {
 		chain.setScanDescription(this);
 		boolean returnValue = chains.add(chain);
 		chain.addModelUpdateListener(this);
 		updateListeners();
 		return returnValue;
 	}
 
 	/**
 	 * Removes a chain from the scan description.
 	 * 
 	 * @param chain the chain that should be removed
 	 * @return <code>true</code> if the chain was removed, 
 	 * 		   <code>false</code> otherwise
 	 */
 	public boolean remove(final Chain chain) {
 		boolean returnValue = chains.remove(chain);
 		chain.removeModelUpdateListener(this);
 		updateListeners();
 		return returnValue;
 	}
 	
 	/**
 	 * Adds an event to the scan description. 
 	 * 
 	 * @param event the event that should be added
 	 * @return <code>true</code> if the event was added,
 	 * 		   <code>false</code> otherwise
 	 */
 	public boolean add(final Event event) {
 		this.eventsMap.put(event.getID(), event);
 		updateListeners();	
 		return true; // TODO always return true ?
 	}
 
 	/**
 	 * Removes an event from the scan description.
 	 * 
 	 * @param event the event that should be removed
 	 * @return <code>true</code> if the event has been removed,
 	 * 		   <code>false</code> otherwise
 	*/
 	public boolean remove(final Event event) {
 		boolean returnValue = this.eventsMap.containsValue(event); // TODO return Value ???
 		updateListeners();
 		this.eventsMap.remove(event.getID());
 		//TODO
 		// we loop through chains and collect all ControlEvents
 		// this should be done easier
 		List<ControlEvent> eventList = new ArrayList<ControlEvent>();
 		for (Chain loopChain : chains) {
 			removeControlEventIfNotInList(
 					loopChain.getBreakControlEventManager(), event);
 			removeControlEventIfNotInList(
 					loopChain.getStartControlEventManager(), event);
 			removeControlEventIfNotInList(
 					loopChain.getStopControlEventManager(), event);
 			removeControlEventIfNotInList(
 					loopChain.getRedoControlEventManager(), event);
 			removeControlEventIfNotInList(
 					loopChain.getPauseControlEventManager(), event);
 			for (ScanModule loopScanModule : loopChain.getScanModules()){
 				removeControlEventIfNotInList(
 						loopScanModule.getBreakControlEventManager(), event);
 				removeControlEventIfNotInList(
 						loopScanModule.getRedoControlEventManager(), event);
 				removeControlEventIfNotInList(
 						loopScanModule.getTriggerControlEventManager(), event);
 				removeControlEventIfNotInList(
 						loopScanModule.getPauseControlEventManager(), event);
 			}
 		}
 		// if a controlEvent uses the event, remove the ControlEvent
 		for (ControlEvent cevent : eventList) {
 			Event embeddedEvent = cevent.getEvent();
 			if (embeddedEvent != null){
 				if (embeddedEvent == event) 
 					cevent.updateEvent(new ModelUpdateEvent(this, null));
 			}
 		}
 		return returnValue;
 	}
 
 	/**
 	 * This method removes a control event if it is not longer in the list.
 	 * 
 	 * @param manager The control event manager.
 	 * @param event The event.
 	 */
 	private void removeControlEventIfNotInList(
 			final ControlEventManager manager, final Event event) {
 		final List<? extends ControlEvent> eventList = 
 				manager.getControlEventsList();
 		// if a controlEvent uses the event, remove the ControlEvent
 		for(ControlEvent cevent : eventList) {
 			final Event embeddedEvent = cevent.getEvent();
 			if(embeddedEvent != null){
 				if(embeddedEvent == event) 
 					manager.removeControlEvent(cevent);
 			}
 		}
 	}
 	/**
 	 * Returns the version of the scan description.
 	 * 
 	 * @return the version of the scan description.
 	 */
 	public String getVersion() {
 		return String.valueOf(inputVersion) + "." + 
 			   String.valueOf(inputRevision) + "." + 
 			   String.valueOf(inputModification);
 	}
 
 	/**
 	 * Sets the version of the scan description.
 	 * 
 	 * @param version The version of the scan description.
 	 */
 	public void setVersion(final String version) {
 		String[] versionArray = version.split("\\.");
 		if(versionArray.length == 3) {
 			inputVersion =  Integer.parseInt(versionArray[0]);
 			inputRevision =  Integer.parseInt(versionArray[1]);
 			inputModification =  Integer.parseInt(versionArray[2]);
 		}
 	}
 
 	/**
 	 * Gives back the repeat count of the scan description.
 	 * 
 	 * @return repeatCount The number of repeats of the scan description.
 	 */
 	public int getRepeatCount() {
 		return this.repeatCount;
 	}
 
 	/**
 	 * Sets the repeat count of the scan description.
 	 * 
 	 * @param repeatCount the scan will be repeated repeatCount times
 	 */
 	public void setRepeatCount(final int repeatCount) {
 		this.repeatCount = repeatCount;
 		updateListeners();
 	}
 	
 	/**
 	 * Returns a list holding all chains.
 	 * 
 	 * @return a list holding all chain.
 	 */
 	public List<Chain> getChains() {
 		return new ArrayList<Chain>(this.chains);
 	}
 
 	/**
 	 * Returns the chain corresponding to the given id.
 	 * 
 	 * @param chainId the id of the chain
 	 * @return the chain corresponding to the given id or 
 	 * 		   <code>null</code> if none
 	 */
 	public Chain getChain(int chainId) {
 		Chain retChain = null;
 		for (Chain chain : this.chains) {
 			if (chain.getId() == chainId) retChain = chain;
 		}
 		return retChain;
 	}
 
 	/**
 	 * Returns the event corresponding to the given id.
 	 * 
 	 * @param id id of an event
 	 * @return the event corresponding to the given id or
 	 * 		   <code>null</code> if none
 	 */
 	public Event getEventById(final String id) {
 		return this.eventsMap.get(id);
 	}
 	
 	/**
 	 * Convenience method 
 	 * 
 	 * @param id A id of a event.
 	 * @return true if successful
 	 */
 	public boolean removeEventById(final String id) {
 		return remove(getEventById(id));
 	}
 	/**
 	 * Returns a default start event for chains without start event tag
 	 * this is a hack to not break existing code
 	 * 
 	 * @return the default StartEvent
 	 */
 	public Event getDefaultStartEvent() { // TODO replace hack with real code ?
 		return this.getEventById("S-0-0-E");
 	}
 	/**
 	 * Returns a list holding all events.
 	 * 
 	 * @return a list holding all events
 	 */
 	public List<Event> getEvents() {
 		return new ArrayList<Event>(this.eventsMap.values());
 	}
 	
 	/**
 	 * Returns a valid id for a plot.
 	 * 
 	 * @return a valid id for a plot
 	 */
 	public int getAvailablePlotId() {
 		List<Integer> plotIds = new ArrayList<Integer>();
 		for(Chain ch : this.chains) {
 			for(ScanModule sm : ch.getScanModules()) {
 				for(PlotWindow pw : sm.getPlotWindows()) {
 					plotIds.add(pw.getId());
 				}
 			}
 		}
 		Collections.sort(plotIds);
 		int i=1;
 		while(plotIds.contains(i)) {
 			i++;
 		}
 		return i;
 	}
 	
 	/**
 	 * This method returns the used measuring station of this scan description.
 	 * 
 	 * @return The used measuring station. Never returns 'null'.
 	 */
 	public IMeasuringStation getMeasuringStation() {
 		return this.measuringStation;
 	}
 	
 	/**
 	 * {@inheritDoc} 
 	 */
 	@Override
 	public void updateEvent(final ModelUpdateEvent modelUpdateEvent) {
 		if(logger.isDebugEnabled()) {
 			if(modelUpdateEvent != null) {
 				logger.debug(modelUpdateEvent.getSender());
 			}
 			logger.debug("null");
 		}
 		updateListeners();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean addModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.modelUpdateListener.add(modelUpdateListener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean removeModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.modelUpdateListener.remove(modelUpdateListener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List<IModelError> errorList = new ArrayList<IModelError>();
 		final Iterator<Chain> it = this.chains.iterator();
 		while(it.hasNext()) {
 			errorList.addAll(it.next().getModelErrors());
 		}
 		return errorList;
 	}
 	
 	/*
 	 * 
 	 */
 	private void updateListeners()
 	{
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(this.modelUpdateListener);
 		
 		Iterator<IModelUpdateListener> it = list.iterator();
 		
 		while(it.hasNext()) {
 			it.next().updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}
 }
