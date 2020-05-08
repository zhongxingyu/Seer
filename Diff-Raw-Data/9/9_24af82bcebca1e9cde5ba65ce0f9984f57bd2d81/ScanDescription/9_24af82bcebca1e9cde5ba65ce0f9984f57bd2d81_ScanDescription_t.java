 package de.ptb.epics.eve.data.scandescription;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.log4j.Logger;
 
 import de.ptb.epics.eve.data.EventTypes;
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Option;
 import de.ptb.epics.eve.data.measuringstation.filter.ExcludeFilter;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider;
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
 
 	/** */
 	public static final String REPEAT_COUNT_PROP = "repeatCount";
 	
 	/** */
 	public static final String MONITOR_OPTION_PROP = "monitorOption";
 
 	/** */
 	public static final String MONITOR_OPTIONS_LIST_PROP = "monitors";
 	
 	private String fileName;
 	
 	// version of the scan description.
 	private int inputVersion;
 	
 	// the input revision.
 	private int inputRevision;
 	
 	// the input modification.
 	private int inputModification;
 	
 	// the number of times the scan is repeated.
 	private int repeatCount;
 	
 	private Event startEvent;
 	
 	// the chains of the scan description.
 	private List<Chain> chains;
 	
 	// monitor options type
 	private MonitorOption monitorOption;
 
 	// options that should be monitored
 	private List<Option> monitors;
 	
 	// the listeners that will be notified if something changed.
 	private List<IModelUpdateListener> modelUpdateListener;
 	
 	// the measuring station used by this scan description.
 	private final IMeasuringStation measuringStation;
 	
 	private boolean dirty;
 	
 	/** */
 	public static final String DIRTY_PROP = "dirty";
 	
 	private PropertyChangeSupport propertyChangeSupport;
 
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
 		this.modelUpdateListener = new ArrayList<IModelUpdateListener>();
 		// default start event
 		startEvent = new Event(EventTypes.SCHEDULE);
 		startEvent.setName("Start");
 		this.fileName = "";
 		this.measuringStation = measuringStation;
 		this.dirty = false;
 		this.monitorOption = MonitorOption.NONE;
 		this.monitors = new ArrayList<Option>();
 		this.propertyChangeSupport = new PropertyChangeSupport(this);
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
 	 * @return the fileName
 	 */
 	public String getFileName() {
 		return fileName;
 	}
 
 	/**
 	 * @param fileName the fileName to set
 	 */
 	public void setFileName(String fileName) {
 		this.fileName = fileName;
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
 		this.propertyChangeSupport.firePropertyChange(REPEAT_COUNT_PROP, 
 				this.repeatCount, this.repeatCount=repeatCount);
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
 	 * Returns a list (COPY !) holding all monitors.
 	 * 
 	 * @return a list holding all monitors.
 	 */
 	public List<Option> getMonitors() {
 		return new ArrayList<Option>(this.monitors);
 	}
 
 	/**
 	 * Returns the type of the monitor options.
 	 * @return 
 	 * 
 	 * @return a MonitorOption for the monitor option type.
 	 */
 	public MonitorOption getMonitorOption() {
 		return this.monitorOption;
 	}
 
 	/**
 	 * Sets the type of the monitor options.
 	 * @param monitorOption the selection of monitored options will be 
 	 * specific by monitorOption
 	 */
 	public void setMonitorOption(final MonitorOption monitorOption) {
 		this.propertyChangeSupport.firePropertyChange(MONITOR_OPTION_PROP, 
 				this.monitorOption, this.monitorOption=monitorOption);
 		updateListeners();
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
 			if (chain.getId() == chainId) {
 				retChain = chain;
 			}
 		}
 		return retChain;
 	}
 
 	/**
 	 * Returns a default start event for chains without start event tag
 	 * this is a hack to not break existing code
 	 * 
 	 * @return the default StartEvent
 	 */
 	public Event getDefaultStartEvent() {
 		return this.startEvent;
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
 	 * @return the dirty
 	 */
 	public boolean isDirty() {
 		return dirty;
 	}
 
 	/**
 	 * @param dirty the dirty to set
 	 */
 	public void setDirty(boolean dirty) {
 		this.dirty = dirty;
 	}
 	
 	/**
 	 * Adds an option to the list of monitors
 	 * @param option the option to add
 	 * @since 1.12
 	 */
 	public void addMonitor(Option option) {
 		this.monitors.add(option);
 		this.propertyChangeSupport.firePropertyChange(
 				ScanDescription.MONITOR_OPTIONS_LIST_PROP, null, monitors);
 	}
 	
 	/**
 	 * Removes an option to the list of monitors
 	 * @param option the option to remove
 	 * @since 1.12
 	 */
 	public void removeMonitor(Option option) {
 		this.monitors.remove(option);
 		this.propertyChangeSupport.firePropertyChange(
 				ScanDescription.MONITOR_OPTIONS_LIST_PROP, null, monitors);
 	}
 	
 	/**
 	 * Removes all option to the list of monitors
 	 * @since 1.14
 	 */
 	public void removeAllMonitor() {
 		this.monitors.clear();
 		this.propertyChangeSupport.firePropertyChange(
 				ScanDescription.MONITOR_OPTIONS_LIST_PROP, null, monitors);
 	}
 
 	/**
 	 * Adds all options to the list of monitors which are marked in the
 	 * messplatz.xml File with monitor="true"
 	 * @since 1.14
 	 */
 	public void addMpMonitor() {
 		// first, clear List
 		this.monitors.clear();
 		// add option to list
 
 		for (Detector d : measuringStation.getDetectors()) {
 			for (Option o : d.getOptions()) {
 				if(!o.isMonitor()) {
 					continue;
 				}
 				this.monitors.add(o);
 			}
 			for (DetectorChannel ch : d.getChannels()) {
 				for (Option o : ch.getOptions()) {
 					if(!o.isMonitor()) {
 						continue;
 					}
 					this.monitors.add(o);
 				}
 			}
 		}
 
 		for (Motor m : measuringStation.getMotors()) {
 			for (Option o : m.getOptions()) {
 				if(!o.isMonitor()) {
 					continue;
 				}
 				this.monitors.add(o);
 			}
 			for (MotorAxis ma : m.getAxes()) {
 				for (Option o : ma.getOptions()) {
 					if(!o.isMonitor()) {
 						continue;
 					}
 					this.monitors.add(o);
 				}
 			}
 		}
 
 		for (Device dev : measuringStation.getDevices()) {
 			for (Option o : dev.getOptions()) {
 				if(!o.isMonitor()) {
 					continue;
 				}
 				this.monitors.add(o);
 			}
 		}
 		
 		this.propertyChangeSupport.firePropertyChange(
 				ScanDescription.MONITOR_OPTIONS_LIST_PROP, null, monitors);
 	}
 
 	/**
 	 * Adds all options of the scan devices to the list of 
 	 * monitors which are marked in the
 	 * messplatz.xml File with monitor="true"
 	 * @since 1.15
 	 */
 	public void addInvolvedMonitor() {
 		// first, clear List
 		this.monitors.clear();
 		// add option to list
 		// do the filtering
 
 		if(logger.isDebugEnabled()) {
 			logger.debug("ScanDescription "
 					+ this.getMonitorOption().name() + " selected.");
 		}
 		ExcludeFilter measuringStation2 = new ExcludeFilter();
 		measuringStation2.setSource(this.getMeasuringStation());
 		measuringStation2.excludeUnusedDevices(this);
 		
 		for (Detector d : measuringStation2.getDetectors()) {
 			if(logger.isDebugEnabled()) {
 				logger.debug("Detector "
 						+ d.getName() + " is used in Scan");
 			}
 			for (Option o : d.getOptions()) {
 				if(!o.isMonitor()) {
 					continue;
 				}
 				this.monitors.add(o);
 				if(logger.isDebugEnabled()) {
 					logger.debug("Option of Detector "
 							+ d.getName() + ": " + o.toString());
 				}
 			}
 			for (DetectorChannel ch : d.getChannels()) {
 				for (Option o : ch.getOptions()) {
 					if(!o.isMonitor()) {
 						continue;
 					}
 					this.monitors.add(o);
 				}
 			}
 		}		
 
 		for (Motor m : measuringStation2.getMotors()) {
 			for (Option o : m.getOptions()) {
 				if(!o.isMonitor()) {
 					continue;
 				}
 				this.monitors.add(o);
 			}
 			for (MotorAxis ma : m.getAxes()) {
 				for (Option o : ma.getOptions()) {
 					if(!o.isMonitor()) {
 						continue;
 					}
 					this.monitors.add(o);
 				}
 			}
 		}
 
 		for (Device dev : measuringStation2.getDevices()) {
 			for (Option o : dev.getOptions()) {
 				if(!o.isMonitor()) {
 					continue;
 				}
 				this.monitors.add(o);
 			}
 		}
 		
 		this.propertyChangeSupport.firePropertyChange(
 				ScanDescription.MONITOR_OPTIONS_LIST_PROP, null, monitors);
 	}
 
 	/**
 	 * Adds all options of the messplatz.xml File to the list of monitors
 	 * @since 1.14
 	 */
 	public void addAllMonitor() {
 		// first, clear List
 		this.monitors.clear();
 		// add option to list
 
 		for (Detector d : measuringStation.getDetectors()) {
 			for (Option o : d.getOptions()) {
 				this.monitors.add(o);
 			}
 			for (DetectorChannel ch : d.getChannels()) {
 				for (Option o : ch.getOptions()) {
 					this.monitors.add(o);
 				}
 			}
 		}
 
 		for (Motor m : measuringStation.getMotors()) {
 			for (Option o : m.getOptions()) {
 				this.monitors.add(o);
 			}
 			for (MotorAxis ma : m.getAxes()) {
 				for (Option o : ma.getOptions()) {
 					this.monitors.add(o);
 				}
 			}
 		}
 
 		for (Device dev : measuringStation.getDevices()) {
 			for (Option o : dev.getOptions()) {
 				this.monitors.add(o);
 			}
 		}
 		
 		this.propertyChangeSupport.firePropertyChange(
 				ScanDescription.MONITOR_OPTIONS_LIST_PROP, null, monitors);
 	}
 
 	/**
 	 * Checks the list of monitors for invalid entries and removes them.
 	 * <p>
 	 * Necessary due to devices lost during SCML file loading.
 	 */
 	public void checkMonitors() {
 		for (Option o : this.getMonitors()) {
 			if (o.getParent() == null) {
 				this.monitors.remove(o);
 				if (logger.isDebugEnabled()) {
 					logger.debug("removed orphaned monitor " + o.getName());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Checks whether a detector ready event of a given channel is used
 	 * 
 	 * @param channel the channel of the detector ready event
 	 * @return <code>true</code> if a detector ready event of the given channel 
 	 * 		is used, <code>false</code> otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean isUsedAsEvent(Channel channel) {
 		for (Chain chain : this.chains) {
 			if (isEventOfList(channel, chain.getPauseEvents()) ||
 					isEventOfList(channel, chain.getRedoEvents()) ||
 					isEventOfList(channel, chain.getBreakEvents()) ||
 					isEventOfList(channel, chain.getStopEvents())) {
 				return true;
 			}
 			for (ScanModule sm : chain.getScanModules()) {
 				if (isEventOfList(channel, sm.getPauseEvents()) ||
 						isEventOfList(channel, sm.getRedoEvents()) ||
 						isEventOfList(channel, sm.getBreakEvents()) ||
 						isEventOfList(channel, sm.getTriggerEvents())) {
 					return true;
 				}
 				for (Channel smChannel : sm.getChannels()) {
 					if (isEventOfList(channel, smChannel.getRedoEvents())) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	/*
 	 * Checks whether the given channel is used as (detector) event in the 
 	 * given list.
 	 * (Helper for #isUsedAsEvent(Channel))
 	 */
 	private boolean isEventOfList(Channel channel, List<ControlEvent> events) {
 		for (ControlEvent e : events) {
 			if (e.getEventType() != EventTypes.DETECTOR) {
 				continue;
 			}
 			if (e.getEvent().getDetectorId()
					.equals(channel.getAbstractDevice().getID())
					&& e.getEvent().getChainId() == channel.getScanModule()
							.getChain().getId()
					&& e.getEvent().getScanModuleId() == channel
							.getScanModule().getId()) {
 				return true;
 			}
 		}
 		return false;
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
 	private void updateListeners() {
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(this.modelUpdateListener);
 		
 		Iterator<IModelUpdateListener> it = list.iterator();
 		
 		while(it.hasNext()) {
 			it.next().updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}
 	
 	/**
 	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
 	 */
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.addPropertyChangeListener(propertyName,
 				listener);
 	}
 
 	/**
 	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)
 	 */
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.removePropertyChangeListener(propertyName,
 				listener);
 	}
 }
