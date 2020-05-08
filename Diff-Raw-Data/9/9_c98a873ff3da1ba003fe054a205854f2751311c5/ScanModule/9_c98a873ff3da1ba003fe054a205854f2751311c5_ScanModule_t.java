 package de.ptb.epics.eve.data.scandescription;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javafx.collections.FXCollections;
 import javafx.collections.ListChangeListener;
 import javafx.collections.ObservableList;
 
 import org.apache.log4j.Logger;
 
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.measuringstation.event.DetectorEvent;
 import de.ptb.epics.eve.data.measuringstation.event.ScanEvent;
 import de.ptb.epics.eve.data.measuringstation.event.ScheduleEvent;
 import de.ptb.epics.eve.data.scandescription.axismode.AddMultiplyMode;
import de.ptb.epics.eve.data.scandescription.axismode.AxisMode;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelErrorProvider;
 import de.ptb.epics.eve.data.scandescription.errors.ScanModuleError;
 import de.ptb.epics.eve.data.scandescription.errors.ScanModuleErrorTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventManager;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateProvider;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.util.math.statistics.DescriptiveStats;
 
 /**
  * This class represents a scan module.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class ScanModule implements IModelUpdateListener, IModelUpdateProvider, 
 								IModelErrorProvider, PropertyChangeListener {
 	
 	private static Logger logger = Logger.getLogger(ScanModule.class.getName());
 	
 	// delegated observable
 	private PropertyChangeSupport propertyChangeSupport;
 
 	/** */
 	public static final String PARENT_CONNECTION_PROP = 
 			"ScanModule.PARENT_CONNECTION_PROP";
 	/** */
 	public static final String APPENDED_CONNECTION_PROP = 
 			"ScanModule.APPENDED_CONNECTION_PROP";
 	/** */
 	public static final String NESTED_CONNECTION_PROP = 
 			"ScanModule.NESTED_CONNECTION_PROP";
 	/** */
 	public static final String MAIN_AXIS_PROP = "mainAxis";
 	
 	/** */
 	public static final String CHANNELS_PROP = "channels";
 	
 	/** */
 	public static final String VALUE_COUNT_PROP = "valueCount";
 	
 	/** */
 	public static final String TRIGGER_DELAY_PROP = "triggerDelay";
 	
 	/** */
 	public static final String SETTLE_TIME_PROP = "settleTime";
 	
 	/** */
 	public static final String TRIGGER_CONFIRM_AXIS_PROP = "triggerConfirmAxis";
 
 	/** */
 	public static final String TRIGGER_CONFIRM_CHANNEL_PROP = "triggerConfirmChannel";
 	
 	/** */
 	public static final String TYPE_PROP = "type";
 	
 	/**
 	 * @since 1.19
 	 */
 	public static final String TRIGGER_EVENT_PROP = "triggerEvent";
 	public static final String BREAK_EVENT_PROP = "breakEvent";
 	public static final String REDO_EVENT_PROP = "redoEvent";
 	public static final String PAUSE_EVENT_PROP = "pauseEvent";
 	
 	/**
 	 * @since 1.18
 	 */
 	public static final String STORAGE_PROP = "storage";
 	
 	/** */
 	public static final int DEFAULT_WIDTH = 70;
 	/** */
 	public static final int DEFAULT_HEIGHT = 30;
 	
 	// the id of the scan module
 	private int id;
 	
 	// the type of the scan module
 	private ScanModuleTypes type;
 	
 	private Storage storage;
 	
 	// the name of the scan module
 	private String name;
 	
 	// the number of measurements per position
 	private int valueCount;
 	
 	// the settle time
 	private double settleTime;
 	
 	// the trigger delay
 	private double triggerDelay;
 	
 	// indicates whether a trigger should be confirmed by hand
 	private boolean triggerConfirmAxis;
 	private boolean triggerConfirmChannel;
 	
 	// a list containing all prescans
 	private List<Prescan> prescans;
 	
 	// a list containing all postscans
 	private List<Postscan> postscans;
 	
 	// a list containing all channels
 	private ObservableList<Channel> channels;
 	
 	// a list containing all axes
 	private List<Axis> axes;
 	
 	// indicates whether an axis in the scan module is set as main axis
 	private Axis mainAxis;
 	
 	// a list containing all plot windows
 	private List<PlotWindow> plotWindows;
 	
 	// the connector to the appended scan module
 	private Connector appended;
 	
 	// the connector to the nested scan module
 	private Connector nested;
 	
 	// the connector to the parent element
 	private Connector parent;
 	
 	// The chain of the scan module
 	private Chain chain;
 	
 	// the x position of the scan module in the graphical editor
 	private int x;
 	
 	// the y position of the scan module in the graphical editor
 	private int y;
 	
 	private int width;
 	private int height;
 	
 	// the control event manager that controls the break events
 	private ControlEventManager breakControlEventManager;
 	
 	// the control event manager that controls the redo events
 	private ControlEventManager redoControlEventManager;
 	
 	// the control event manager that controls the trigger events
 	private ControlEventManager triggerControlEventManager;
 	
 	// the control event manager that controls the pause events
 	private ControlEventManager pauseControlEventManager;
 	
 	// List that is holding all objects that need to get an update message 
 	// if this object was updated.
 	private List<IModelUpdateListener> updateListener;
 		
 	// list that holds all Positionings
 	private List<Positioning> positionings;
 
 	// sm_loading is true, if the scan module is loading 
 	public boolean sm_loading; // TODO information hiding ?
 	
 	/**
 	 * Constructs a <code>ScanModule</code> with the given id.
 	 * 
 	 * @param id the id that should be set
 	 * @throws IllegalArgumentException if <code>id</code> is less than 1
 	 */
 	public ScanModule(final int id) {
 		if(id < 0) {
 			throw new IllegalArgumentException(
 					"The parameter 'id' must be larger than 0!");
 		}
 		this.id = id;
 		this.prescans = new ArrayList<Prescan>();
 		this.postscans = new ArrayList<Postscan>();
 		this.channels = FXCollections.observableList(new ArrayList<Channel>());
 		this.axes = new ArrayList<Axis>();
 		this.mainAxis = null;
 		this.plotWindows = new ArrayList<PlotWindow>();
 		this.valueCount = 1;
 		this.settleTime = 0.0;
 		this.triggerDelay = 0.0;
 		this.type = ScanModuleTypes.CLASSIC;
 		this.name = "";
 
 		this.storage = Storage.DEFAULT;
 		
 		this.triggerConfirmAxis = false;
 		this.triggerConfirmChannel = false;
 		
 		this.breakControlEventManager = new ControlEventManager(
 				ControlEventTypes.CONTROL_EVENT);
 		this.redoControlEventManager = new ControlEventManager(
 				ControlEventTypes.CONTROL_EVENT);
 		this.pauseControlEventManager = new ControlEventManager(
 				ControlEventTypes.PAUSE_EVENT);
 		this.triggerControlEventManager = new ControlEventManager(
 				ControlEventTypes.CONTROL_EVENT);
 		
 		this.breakControlEventManager.addModelUpdateListener(this);
 		this.redoControlEventManager.addModelUpdateListener(this);
 		this.pauseControlEventManager.addModelUpdateListener(this);
 		this.triggerControlEventManager.addModelUpdateListener(this);
 		
 		this.updateListener = new ArrayList<IModelUpdateListener>();
 		
 		this.positionings = new ArrayList<Positioning>();
 
 		this.width = ScanModule.DEFAULT_WIDTH;
 		this.height = ScanModule.DEFAULT_HEIGHT;
 		
 		this.propertyChangeSupport = new PropertyChangeSupport(this);
 		
 		if (logger.isDebugEnabled()) {
 			this.addChannelChangeListener(new ListChangeListener<Channel>() {
 				@Override
 				public void onChanged(
 						javafx.collections.ListChangeListener.Change<? extends Channel> c) {
 					while (c.next()) {
 						if (c.wasPermutated()) {
 							logger.debug("channels permutated");
 						} else if (c.wasUpdated()) {
 							logger.debug("channel updated");
 						} else {
 							for (Channel ch : c.getRemoved()) {
 								logger.debug("channel '" 
 										+ ch.getDetectorChannel().getName() 
 										+ "' was removed from scan module '" 
 										+ ch.getScanModule().getName() + "'");
 							}
 							for (Channel ch : c.getAddedSubList()) {
 								logger.debug("channel '" 
 										+ ch.getDetectorChannel().getName() 
 										+ "' was added to scan module '" 
 										+ ch.getScanModule().getName() + "'");
 							}
 						}
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Gives back an Array that contains all Prescans.
 	 * 
 	 * @return An Array, that contains all Prescans.
 	 */
 	public Prescan[] getPrescans() {
 		return this.prescans.toArray(new Prescan[0]);
 	}
 	
 	/**
 	 * Gives back an Array that contains all Postscans.
 	 * 
 	 * @return An Array, that contains all Postscans.
 	 */
 	public Postscan[] getPostscans() {
 		return this.postscans.toArray(new Postscan[0]);
 	}
 	
 	/**
 	 * Gives back an Array that contains all channel behaviors.
 	 * 
 	 * @return An Array, that contains all channel behaviors.
 	 */
 	public Channel[] getChannels() {
 		return this.channels.toArray(new Channel[0]);
 	}
 	
 	/**
 	 * Gives back an Array that contains all axis behaviors.
 	 * 
 	 * @return An Array, that contains all axes behaviors.
 	 */
 	public Axis[] getAxes() {
 		return this.axes.toArray(new Axis[0]);
 	}
 	
 	/**
 	 * @return the mainAxis
 	 */
 	public Axis getMainAxis() {
 		return mainAxis;
 	}
 
 	/**
 	 * Gives back an Array that contains all plot windows.
 	 * 
 	 * @return An Array, that contains all plot windows.
 	 */
 	public PlotWindow[] getPlotWindows() {
 		return this.plotWindows.toArray(new PlotWindow[0]);
 	}
 	
 	/**
 	 * This method returns an array of all positionings.
 	 * 
 	 * @return An array of all positionings.
 	 */
 	public Positioning[] getPositionings() {
 		return this.positionings.toArray(new Positioning[0]);
 	}
 	
 	/**
 	 * Adds a prescan to the Scan Modul.
 	 * 
 	 * @param prescan The prescan that should be added to the Scan Modul.
 	 */
 	public void add(final Prescan prescan) {
 		prescan.readDiscreteValues();
 		this.prescans.add(prescan);
 		prescan.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a postscan to the Scan Modul.
 	 * 
 	 * @param postscan The postscan that should be added to the Scan Modul.
 	 */
 	public void add(final Postscan postscan) {
 		postscan.readDiscreteValues();
 		this.postscans.add(postscan);
 		postscan.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a channel behavior to the Scan Modul.
 	 * 
 	 * @param channel The channel behavior that should be added to the Scan 
 	 * Modul.
 	 */
 	public void add(final Channel channel) {
 		channel.addModelUpdateListener(this);
 		this.channels.add(channel);
 		this.propertyChangeSupport.firePropertyChange(ScanModule.CHANNELS_PROP,
 				null, this.channels);
 		// channel has to notice if its normalize channel is removed
 		this.propertyChangeSupport.addPropertyChangeListener(
 				ScanModule.CHANNELS_PROP, channel);
 		propertyChangeSupport.firePropertyChange("addChannel", channel, null);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a axis behavior to the Scan Modul.
 	 * 
 	 * @param axis The axis behavior that should be added to the Scan Modul.
 	 */
 	public void add(final Axis axis) {
 		axis.addModelUpdateListener(this);
 		// scan module has to be notified if an axis is set as main axis:
 		axis.addPropertyChangeListener(AddMultiplyMode.MAIN_AXIS_PROP, this);
 		// each axis has to be notified that some axis is set as main axis 
 		// (or reset)
 		this.propertyChangeSupport.addPropertyChangeListener(
 				ScanModule.MAIN_AXIS_PROP, axis);
 		this.axes.add(axis);
 		if (axis.isMainAxis()) {
			// should only be executed during scml load
 			this.mainAxis = axis;
			axis.addPropertyChangeListener(
					AddMultiplyMode.STEPCOUNT_PROP, this);
 		}
 		if (axis.getMode() instanceof AddMultiplyMode<?>) {
 			((AddMultiplyMode<?>)axis.getMode()).matchMainAxis(this.mainAxis);
 		}
 		propertyChangeSupport.firePropertyChange("addAxis", axis, null);
 		updateListeners();
 	}
 	
 	/**
 	 * Adds a plot window to the Scan Modul.
 	 * 
 	 * @param plotWindow The plot window that should be added to the Scan Modul.
 	 */
 	public void add(final PlotWindow plotWindow) {
 		this.plotWindows.add(plotWindow);
 		plotWindow.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * This methods adds a positioning to a scan module.
 	 * 
 	 * @param positioning The positioning to add.
 	 */
 	public void add(final Positioning positioning) {
 		this.positionings.add(positioning);
 		positioning.addModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a prescan from the Scan Modul.
 	 * 
 	 * @param prescan The prescan that should be removed.
 	 */
 	public void remove(final Prescan prescan) {
 		this.prescans.remove(prescan);
 		prescan.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a postscan from the Scan Modul.
 	 * 
 	 * @param postscan The postscan that should be removed.
 	 */
 	public void remove(final Postscan postscan) {
 		this.postscans.remove(postscan);
 		postscan.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a channel behavior from the Scan Modul.
 	 * 
 	 * @param channel The channel behavior that should be removed.
 	 */
 	public void remove(final Channel channel) {
 		// 1. log off listener
 		channel.removeModelUpdateListener(this);
 		// channel no longer needs to listen to changes
 		this.propertyChangeSupport.removePropertyChangeListener(
 				ScanModule.CHANNELS_PROP, channel);
 		// 2. remove channel
 		this.channels.remove(channel);
 		// 3. tell that channel was removed
 		this.propertyChangeSupport.firePropertyChange(ScanModule.CHANNELS_PROP,
 				null, this.channels);
 		propertyChangeSupport.firePropertyChange("removeChannel", channel, null);
 		propertyChangeSupport.firePropertyChange("removePosChannel", channel, null);
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a axis behavior from the Scan Modul.
 	 * 
 	 * @param axis The axis behavior that should be removed.
 	 */
 	public void remove(final Axis axis) {
 		axis.removeModelUpdateListener(this);
 		axis.removePropertyChangeListener(AddMultiplyMode.MAIN_AXIS_PROP, this);
 		this.propertyChangeSupport.removePropertyChangeListener(
 				ScanModule.MAIN_AXIS_PROP, axis);
 
 		this.axes.remove(axis);
 		
 		propertyChangeSupport.firePropertyChange("removeAxis", axis, null);
 		propertyChangeSupport.firePropertyChange("removePosAxis", axis, null);
 		
 		if (this.mainAxis != null && this.mainAxis.equals(axis)) {
			axis.removePropertyChangeListener(AddMultiplyMode.STEPCOUNT_PROP,
					this);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.MAIN_AXIS_PROP, this.mainAxis,
 					this.mainAxis = null);
 		}
 		updateListeners();
 	}
 	
 	/**
 	 * Removes a plot window behavior from the Scan Modul.
 	 * 
 	 * @param plotWindow The plot window that should be removed.
 	 */
 	public void remove(final PlotWindow plotWindow) {
 		// remove property Listener of plotWindow
 		this.removePropertyChangeListener("removeAxis", plotWindow);
 		this.removePropertyChangeListener("removeChannel", plotWindow);
 		this.removePropertyChangeListener("addAxis", plotWindow);
 		
 		propertyChangeSupport.firePropertyChange("removePlot", plotWindow, null);
 
 		this.plotWindows.remove(plotWindow);
 		plotWindow.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * This method removes a positioning from the scan module.
 	 * 
 	 * @param positioning The positioning to remove.
 	 */
 	public void remove(final Positioning positioning) {
 		this.positionings.remove(positioning);
 		positioning.removeModelUpdateListener(this);
 		updateListeners();
 	}
 	
 	/**
 	 * Gives back the connector, that brings you to the appended scan modul.
 	 * 
 	 * @return The connector to the appended scan modul or null if it's not 
 	 * setted.
 	 */
 	public Connector getAppended() {
 		return appended;
 	}
 
 
 	/**
 	 * Sets the connector, that brings you to the appended scan modul.
 	 * 
 	 * @param appended The connector that brings you to the appended scan modul.
 	 */
 	public void setAppended(final Connector appended) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.APPENDED_CONNECTION_PROP, this.appended,
 				this.appended = appended);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the id of the scan modul.
 	 * 
 	 * @return The id of the scan modul.
 	 */
 	public int getId() {
 		return id;
 	}
 
 	/**
 	 * Sets the id of the scan modul.
 	 * 
 	 * @param id The new id of the scan modul.
 	 */
 	public void setId(final int id) {
 		if(id < 1) {
 			throw new IllegalArgumentException(
 					"The parameter 'id' must be larger than 0!");
 		}
 		this.id = id;
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the name of the scan modul.
 	 * 
 	 * @return The name of the scan modul.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name of the scan module.
 	 * 
 	 * @param name The name of the scan modul. Must not be null!
 	 */
 	public void setName(final String name) {
 		this.propertyChangeSupport.firePropertyChange("name", this.name,
 				this.name = name);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the Connector that brings you to the nested scan module.
 	 * 
 	 * @return The connector to the nested scan module.
 	 */
 	public Connector getNested() {
 		return nested;
 	}
 
 	/**
 	 * Sets the Connector to the nested scan module.
 	 * 
 	 * @param nested The Connector to the nested scan module.
 	 */
 	public void setNested(final Connector nested) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.NESTED_CONNECTION_PROP, this.nested,
 				this.nested = nested);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the Connector that brings you to the parent element.
 	 * 
 	 * @return The Connector to the parent Element.
 	 */
 	public Connector getParent() {
 		return parent;
 	}
 
 	/**
 	 * Sets the Connector to the parent element.
 	 * 
 	 * @param parent The Connector to the parent element.
 	 */
 	public void setParent(final Connector parent) {
 		this.propertyChangeSupport.firePropertyChange(PARENT_CONNECTION_PROP,
 				this.parent, this.parent = parent);
 		updateListeners();
 	}
 
 
 
 	/**
 	 * Gives back the settle time of the scan module.
 	 * 
 	 * @return The settle time.
 	 */
 	public double getSettleTime() {
 		return settleTime;
 	}
 
 	/**
 	 * Sets the settle time.
 	 * 
 	 * @param settletime The settle time.
 	 */
 	public void setSettleTime(final double settletime) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.SETTLE_TIME_PROP, this.settleTime,
 				this.settleTime = settletime);
 		updateListeners();
 	}
 
 	/**
 	 * Returns whether trigger configrmation is enabled.
 	 * 
 	 * @return whether trigger confirmation is enabled
 	 */
 	public boolean isTriggerConfirmAxis() {
 		return triggerConfirmAxis;
 	}
 
 	/**
 	 * Sets if trigger have to be confirmed by hand.
 	 * 
 	 * @param triggerconfirmaxis
 	 *            <code>true</code> to enable manual trigger, <code>false</code>
 	 *            otherwise
 	 */
 	public void setTriggerConfirmAxis(final boolean triggerconfirmaxis) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_CONFIRM_AXIS_PROP, this.triggerConfirmAxis,
 				this.triggerConfirmAxis = triggerconfirmaxis);
 		updateListeners();
 	}
 
 	/**
 	 * @return the triggerconfirmchannel
 	 */
 	public boolean isTriggerConfirmChannel() {
 		return triggerConfirmChannel;
 	}
 
 	/**
 	 * @param triggerconfirmchannel the triggerconfirmchannel to set
 	 */
 	public void setTriggerConfirmChannel(boolean triggerconfirmchannel) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_CONFIRM_CHANNEL_PROP,
 				this.triggerConfirmChannel,
 				this.triggerConfirmChannel = triggerconfirmchannel);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the trigger delay.
 	 * 
 	 * @return The trigger delay
 	 */
 	public double getTriggerDelay() {
 		return triggerDelay;
 	}
 
 	/**
 	 * Sets the trigger delay
 	 * 
 	 * @param triggerdelay The trigger delay
 	 */
 	public void setTriggerDelay(final double triggerdelay) {
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_DELAY_PROP, this.triggerDelay,
 				this.triggerDelay = triggerdelay);
 		updateListeners();
 	}
 
 
 	/**
 	 * @return the valuecount
 	 */
 	public int getValueCount() {
 		return valueCount;
 	}
 
 	/**
 	 * @param valuecount the valuecount to set
 	 * @throws IllegalArgumentException if <code>valuecount</code> < 1
 	 */
 	public void setValueCount(int valuecount) {
 		if (valuecount < 1) {
 			throw new IllegalArgumentException("valuecount must be > 0!");
 		}
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.VALUE_COUNT_PROP, this.valueCount,
 				this.valueCount = valuecount);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the type of the scan modul.
 	 * 
 	 * @return The type of the scan modul.
 	 */
 	public ScanModuleTypes getType() {
 		return type;
 	}
 
 	/**
 	 * Sets the type of the scan modul.
 	 * 
 	 * @param type The type of the scan modul.
 	 */
 	public void setType(final ScanModuleTypes type) {
 		if (type.equals(ScanModuleTypes.SAVE_AXIS_POSITIONS) ||
 				type.equals(ScanModuleTypes.SAVE_CHANNEL_VALUES)) {
 			this.setStorage(Storage.ALTERNATE);
 		} else if (type.equals(ScanModuleTypes.CLASSIC)) {
 			this.setStorage(Storage.DEFAULT);
 		}
 		this.propertyChangeSupport.firePropertyChange(ScanModule.TYPE_PROP,
 				this.type, this.type = type);
 		updateListeners();
 	}
 	
 	/**
 	 * @return the storage
 	 */
 	public Storage getStorage() {
 		return storage;
 	}
 
 	/**
 	 * @param storage the storage to set
 	 */
 	public void setStorage(Storage storage) {
 		this.propertyChangeSupport.firePropertyChange(ScanModule.STORAGE_PROP,
 				this.storage, this.storage = storage);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the Chain, where this scan modul is in.
 	 * 
 	 * @return The Chain where the scan modul is in or null if it is in no 
 	 * chain.
 	 */
 	public Chain getChain() {
 		return chain;
 	}
 
 	/**
 	 * Sets the chain, where this scan modul is in. This method gets called by 
 	 * the add and remove method of Chain.
 	 * 
 	 * @param chain The Chain where the scan modul is in.
 	 */
 	protected void setChain(final Chain chain) {
 		this.chain = chain;
 	}
 
 	/**
 	 * Returns all used plot window Ids.
 	 * 
 	 * @return all used plot wind Ids
 	 */
 	public List<Integer> getPlotIds() {
 		List<Integer> list = new ArrayList<Integer>();
 		for(PlotWindow pw : this.plotWindows) {
 			list.add(pw.getId());
 		}
 		return list;
 	}
 	
 	/**
 	 * Returns the first found path of an axis (other than given axis) with 
 	 * step function file or <code>null</code> if none.
 	 * 
 	 * @param givenAxis axis which is ignored
 	 * @return the first found path of an axis with step function file or 
 	 * <code>null</code> if none.
 	 * @since 1.20
 	 */
 	public String getAxisPath(Axis givenAxis) {
 		for (Axis axis : this.getAxes()) {
 			if (axis.equals(givenAxis)) {
 				continue;
 			}
 			if (axis.getStepfunction().equals(Stepfunctions.FILE)) {
 				int lastSeperatorIndex = axis.getFile().getAbsolutePath()
 						.lastIndexOf(File.separatorChar);
 				return axis.getFile().getAbsolutePath()
 						.substring(0, lastSeperatorIndex + 1);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gives back the x-position of the scan modul in the graphic diagramm.
 	 * 
 	 * @return The x-position of the scan modul in the graphic diagram.
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Sets the x-position in the graphical diagramm.
 	 * 
 	 * @param x The x-position in the graphical diagramm.
 	 */
 	public void setX(final int x) {
 		this.propertyChangeSupport.firePropertyChange("x", this.x, this.x = x);
 		updateListeners();
 	}
 
 	/**
 	 * Gives back the y-position in the graphical diagram.
 	 * 
 	 * @return The y-position in the graphical diagram.
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Sets the y-position in the graphical diagram
 	 * 
 	 * @param y The y-position in the graphical diagram.
 	 */
 	public void setY(final int y) {
 		this.propertyChangeSupport.firePropertyChange("y", this.y, this.y = y);
 		updateListeners();
 	}
 
 	/**
 	 * @return the width
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @return the height
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Returns a list of pause events (original list).
 	 * 
 	 * @return a list of pause events (original list)
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public List<ControlEvent> getPauseEvents() {
 		return this.pauseControlEventManager.getEvents();
 	}
 	
 	/**
 	 * Adds a pause event.
 	 * 
 	 * @param pauseEvent the pause event that should be added
 	 * @return <code>true</code> if the event was added, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean addPauseEvent(final PauseEvent pauseEvent) {
 		if (this.pauseControlEventManager.addControlEvent(pauseEvent)) {
 			this.registerEventValidProperty(pauseEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.PAUSE_EVENT_PROP, null, pauseEvent);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes a pause event.
 	 * 
 	 * @param pauseEvent the pause event that should be removed 
 	 * @return <code>true</code> if the event was removed, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean removePauseEvent(final PauseEvent pauseEvent) {
 		if (this.pauseControlEventManager.removeEvent(pauseEvent)) {
 			this.unregisterEventValidProperty(pauseEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.PAUSE_EVENT_PROP, pauseEvent, null);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes all pause events.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public void removePauseEvents() {
 		this.pauseControlEventManager.removeAllEvents();
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.PAUSE_EVENT_PROP,
 				this.pauseControlEventManager.getEvents(), null);
 	}
 	
 	/**
 	 * Returns a list of break events (original list).
 	 * 
 	 * @return a list of break events (original list)
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public List<ControlEvent> getBreakEvents() {
 		return this.breakControlEventManager.getEvents();
 	}
 	
 	/**
 	 * Adds a break event.
 	 * 
 	 * @param breakEvent the break event that should be added
 	 * @return <code>true</code> if the event was added, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean addBreakEvent(final ControlEvent breakEvent) {
 		if (this.breakControlEventManager.addControlEvent(breakEvent)) {
 			this.registerEventValidProperty(breakEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.BREAK_EVENT_PROP, null, breakEvent);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes a break event.
 	 * 
 	 * @param breakEvent the break event that should be removed 
 	 * @return <code>true</code> if the event was removed, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean removeBreakEvent(final ControlEvent breakEvent) {
 		if (this.breakControlEventManager.removeEvent(breakEvent)) {
 			this.unregisterEventValidProperty(breakEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.BREAK_EVENT_PROP, breakEvent, null);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes all break events.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public void removeBreakEvents() {
 		this.breakControlEventManager.removeAllEvents();
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.BREAK_EVENT_PROP,
 				this.breakControlEventManager.getEvents(), null);
 	}
 	
 	/**
 	 * Returns a list of redo events.
 	 * 
 	 * @return a list of redo events
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public List<ControlEvent> getRedoEvents() {
 		return this.redoControlEventManager.getEvents();
 	}
 	
 	/**
 	 * Adds a redo event.
 	 * 
 	 * @param redoEvent the redo event that should be added
 	 * @return <code>true</code> if the event was added, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean addRedoEvent(final ControlEvent redoEvent) {
 		if (this.redoControlEventManager.addControlEvent(redoEvent)) {
 			this.registerEventValidProperty(redoEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.REDO_EVENT_PROP, null, redoEvent);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes a redo event.
 	 * 
 	 * @param redoEvent the redo event that should be removed
 	 * @return <code>true</code> if the event was removed, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean removeRedoEvent(final ControlEvent redoEvent) {
 		if (this.redoControlEventManager.removeEvent(redoEvent)) {
 			this.unregisterEventValidProperty(redoEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.REDO_EVENT_PROP, redoEvent, null);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes all redo events.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public void removeRedoEvents() {
 		this.redoControlEventManager.removeAllEvents();
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.REDO_EVENT_PROP,
 				this.redoControlEventManager.getEvents(), null);
 	}
 	
 	/**
 	 * Returns a list of trigger events (original list).
 	 * 
 	 * @return a list of trigger events (original list)
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public List<ControlEvent> getTriggerEvents() {
 		return this.triggerControlEventManager.getEvents();
 	}
 	
 	/**
 	 * Adds a trigger event.
 	 * 
 	 * @param triggerEvent the trigger event that should be added
 	 * @return <code>true</code> if the event was added, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean addTriggerEvent(final ControlEvent triggerEvent) {
 		if (this.triggerControlEventManager.addControlEvent(triggerEvent)) {
 			this.registerEventValidProperty(triggerEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.TRIGGER_EVENT_PROP, null, triggerEvent);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes a trigger event.
 	 * 
 	 * @param triggerEvent the trigger event that should be removed
 	 * @return <code>true</code> if the event was removed, <code>false</code>
 	 * 		otherwise
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public boolean removeTriggerEvent(final ControlEvent triggerEvent) {
 		if (this.triggerControlEventManager.removeEvent(triggerEvent)) {
 			this.unregisterEventValidProperty(triggerEvent);
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.TRIGGER_EVENT_PROP, triggerEvent, null);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Removes all trigger events.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 */
 	public void removeTriggerEvents() {
 		this.triggerControlEventManager.removeAllEvents();
 		this.propertyChangeSupport.firePropertyChange(
 				ScanModule.TRIGGER_EVENT_PROP,
 				this.triggerControlEventManager.getEvents(), null);
 	}
 	
 	/**
 	 * Checks whether the given pause event is a pause event of the scan module.
 	 * 
 	 * @param controlEvent the pause event that should be checked
 	 * @return <code>true</code> if the given pause event is a pause event of 
 	 * 			the scan module, <code>false</code> otherwise
 	 */
 	public boolean isPauseEventOfScanModule(final PauseEvent controlEvent) {
 		return this.pauseControlEventManager.getEvents().contains(controlEvent);
 	}
 	
 	/**
 	 * Checks whether the given control event is a redo event of the scan 
 	 * module.
 	 * 
 	 * @param redoEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a redo event of 
 	 * 			the scan module, <code>false</code> otherwise
 	 */
 	public boolean isRedoEventOfScanModule(final ControlEvent redoEvent) {
 		return this.redoControlEventManager.getEvents().contains(redoEvent);
 	}
 	
 	/**
 	 * Checks whether the given control event is a break event of the scan 
 	 * module.
 	 * 
 	 * @param breakEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a break event of 
 	 * 			the scan module, <code>false</code> otherwise
 	 */
 	public boolean isBreakEventOfScanModule(final ControlEvent breakEvent) {
 		return this.breakControlEventManager.getEvents().contains(breakEvent);
 	}
 	
 	/**
 	 * Checks whether the given control event is a trigger event of the scan 
 	 * module.
 	 * 
 	 * @param triggerEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a trigger event 
 	 * 			of the scan module, <code>false</code> otherwise
 	 */
 	public boolean isTriggerEventOfScanModule(final ControlEvent triggerEvent) {
 		return this.triggerControlEventManager.getEvents().contains(
 				triggerEvent);
 	}
 
 	
 	
 	/**
 	 * Checks whether the given control event is a pause, redo or break event 
 	 * of the scan module.
 	 * 
 	 * @param controlEvent the control event that should be checked
 	 * @return <code>true</code> if the given control event is a pause, redo or 
 	 * 			break event of the scan module
 	 */
 	public boolean isAEventOfTheScanModul(final ControlEvent controlEvent) {
 		return (controlEvent instanceof PauseEvent && 
 				this.isPauseEventOfScanModule((PauseEvent)controlEvent)) || 
 				this.isBreakEventOfScanModule(controlEvent) || 
 				this.isRedoEventOfScanModule(controlEvent) || 
 				this.isTriggerEventOfScanModule(controlEvent);
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
 		return this.updateListener.add(modelUpdateListener);
 	}
 
 	/**
 	 * {@inheritDoc} 
 	 */
 	@Override
 	public boolean removeModelUpdateListener(
 			final IModelUpdateListener modelUpdateListener) {
 		return this.updateListener.remove(modelUpdateListener);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List<IModelError> errorList = new ArrayList<IModelError>();
 		
 		errorList.addAll(this.pauseControlEventManager.getModelErrors());
 		errorList.addAll(this.breakControlEventManager.getModelErrors());
 		errorList.addAll(this.redoControlEventManager.getModelErrors());
 		errorList.addAll(this.triggerControlEventManager.getModelErrors());
 		
 		for(Axis axis : this.axes) {
 			errorList.addAll(axis.getModelErrors());
 		}
 		for(Channel channel : this.channels) {
 			errorList.addAll(channel.getModelErrors());
 		}
 		for(Prescan prescan : this.prescans) {
 			errorList.addAll(prescan.getModelErrors());
 		}
 		for(Postscan postscan : this.postscans) {
 			errorList.addAll(postscan.getModelErrors());
 		}
 		for(Positioning positioning : this.positionings) {
 			errorList.addAll(positioning.getModelErrors());
 		}
 		for(PlotWindow plotwindow : this.plotWindows) {
 			errorList.addAll(plotwindow.getModelErrors());
 		}
 
 		if(Double.compare(this.triggerDelay, Double.NaN) == 0) {
 			errorList.add(new ScanModuleError(this, 
 					ScanModuleErrorTypes.TRIGGER_DELAY_NOT_POSSIBLE));
 		}
 		if(Double.compare(this.settleTime, Double.NaN) == 0) {
 			errorList.add(new ScanModuleError(this, 
 					ScanModuleErrorTypes.SETTLE_TIME_NOT_POSSIBLE));
 		}
 		return errorList;
 	}
 	
 	/*
 	 * 
 	 */
 	private void updateListeners() {
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(this.updateListener);
 		
 		for(IModelUpdateListener imul : list) {
 			imul.updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getSource() instanceof AddMultiplyMode<?> &&
 				e.getPropertyName().equals(AddMultiplyMode.MAIN_AXIS_PROP)) {
 			Axis newAxis = null;
 			if ((Boolean)e.getNewValue()) {
 				newAxis = ((AddMultiplyMode<?>) e.getSource()).getAxis();
 			}
 			if (this.mainAxis != null) {
 				// remove Listener of old main axis
 				this.mainAxis.removePropertyChangeListener(
 						AddMultiplyMode.STEPCOUNT_PROP, this);
 			}
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.MAIN_AXIS_PROP, this.mainAxis,
 					this.mainAxis = newAxis);
 			if (this.mainAxis != null) {
 				// register listener to new main axis
 				this.mainAxis.addPropertyChangeListener(
 						AddMultiplyMode.STEPCOUNT_PROP, this);
 			}
 			if (logger.isDebugEnabled()) {
 				logger.debug("Axis " + this.mainAxis.getMotorAxis().getName()
 						+ " has been set as main axis");
 			}
 		} else if (e.getPropertyName().equals(AddMultiplyMode.STEPCOUNT_PROP)) { 
 			for (Axis axis : this.axes) {
 				if (axis.getMode() instanceof AddMultiplyMode<?>) {
 					((AddMultiplyMode<?>) axis.getMode())
 							.matchMainAxis(this.mainAxis);
 				}
 			}
 		} else if (e.getPropertyName().equals(AddMultiplyMode.MAIN_AXIS_PROP)) {
 			// main axis property geändet obwohl kein AddMultiplyMode
 			
 			if (this.mainAxis != null) {
 				// remove Listener of old main axis
 				this.mainAxis.removePropertyChangeListener(
 						AddMultiplyMode.STEPCOUNT_PROP, this);
 			}
 			this.propertyChangeSupport.firePropertyChange(
 					ScanModule.MAIN_AXIS_PROP, this.mainAxis, this.mainAxis = null);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Main Axis has been reset");
 			}
 		}
 		if (e.getPropertyName().equals(ScanEvent.VALID_PROP) &&
 				e.getNewValue().equals(Boolean.FALSE)) {
 			logger.debug(((ScanEvent)e.getSource()).getName() + 
 					" (SM: " + this.getName() + ") " +
 					" got invalid -> start removal");
 			this.removeInvalidScanEvents();
 		}
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param propertyName the name of the property
 	 * @param listener the {@link java.beans.PropertyChangeListener}
 	 * @see {@link java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.addPropertyChangeListener(
 				propertyName, listener);
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param propertyName the name of the property
 	 * @param listener {@link java.beans.PropertyChangeListener}
 	 * @see {@link java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.removePropertyChangeListener(
 				propertyName, listener);
 	}
 	
 	/**
 	 * Adds all available motor axes of the given device definition to the 
 	 * scan module setting each to motion disabled.
 	 * 
 	 * @param measuringStation the device definition containing the axes
 	 * @since 1.9
 	 */
 	public void saveAllAxisPositions(IMeasuringStation measuringStation) {
 		// get available motor axes
 		List<MotorAxis> motorAxes = new ArrayList<MotorAxis>();
 		for(Motor m : measuringStation.getMotors()) {
 			for(MotorAxis ma : m.getAxes()) {
 				motorAxes.add(ma);
 			}
 		}
 		// create axes
 		for(MotorAxis ma : motorAxes) {
 			final Axis axis = new Axis(this);
 			axis.setMotorAxis(ma);
 			axis.setStepfunction(Stepfunctions.PLUGIN);
 			PlugIn motionDisabled = measuringStation
 					.getPluginByName("MotionDisabled");
 			axis.setPluginController(new PluginController(motionDisabled));
 			axis.getPluginController().setPlugin(motionDisabled);
 			this.add(axis);
 		}
 	}
 	
 	/**
 	 * Adds all available detector channels of the given device definition to 
 	 * the scan module setting each to average count 1.
 	 * 
 	 * @param measuringStation the device definition containing the channels
 	 * @since 1.9
 	 */
 	public void saveAllChannelValues(IMeasuringStation measuringStation) {
 		// get available channels
 		List<DetectorChannel> detectorChannels = new ArrayList<DetectorChannel>();
 		for (Detector det : measuringStation.getDetectors()) {
 			for (DetectorChannel ch : det.getChannels()) {
 				detectorChannels.add(ch);
 			}
 		}
 		// creating channels
 		for (DetectorChannel ch : detectorChannels) {
 			Channel channel = new Channel(this);
 			channel.setDetectorChannel(ch);
 			channel.setAverageCount(1);
 			this.add(channel);
 		}
 	}
 	
 	/**
 	 * Removes all axes, channels, prescans, postscans, positiongs and 
 	 * plot windows.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAll() {
 		this.removeAllAxes();
 		this.removeAllChannels();
 		this.removeAllPrescans();
 		this.removeAllPostscans();
 		this.removeAllPositionings();
 		this.removeAllPlotWindows();
 	}
 	
 	/**
 	 * Removes all axes.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllAxes() {
 		for(Axis a : this.getAxes()) {
 			this.remove(a);
 		}
 	}
 	
 	/**
 	 * Removes all channels.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllChannels() {
 		for(Channel ch : this.getChannels()) {
 			this.remove(ch);
 		}
 	}
 	
 	/**
 	 * Removes all prescans.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPrescans() {
 		for(Prescan pre : this.getPrescans()) {
 			this.remove(pre);
 		}
 	}
 	
 	/**
 	 * Removes all postscans.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPostscans() {
 		for(Postscan post : this.getPostscans()) {
 			this.remove(post);
 		}
 	}
 	
 	/**
 	 * Removes all positionings.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPositionings() {
 		for(Positioning pos : this.getPositionings()) {
 			this.remove(pos);
 		}
 	}
 	
 	/**
 	 * Removes all plot windows.
 	 * 
 	 * @since 1.1
 	 */
 	public void removeAllPlotWindows() {
 		for(PlotWindow plot : this.getPlotWindows()) {
 			this.remove(plot);
 		}
 	}
 	
 	/**
 	 * Returns the number of devices.
 	 * 
 	 * @return the number of devices
 	 * @since 1.1
 	 */
 	public int getDeviceCount() {
 		int count = 0;
 		
 		count += this.axes.size();
 		count += this.channels.size();
 		count += this.prescans.size();
 		count += this.postscans.size();
 		count += this.positionings.size();
 		count += this.plotWindows.size();
 		
 		return count;
 	}
 	
 	/**
 	 * Returns the number of motor positions.
 	 * 
 	 * @return the number of motor positions or <code>null</code> if 
 	 * 			calculation is not possible
 	 * @author Marcus Michalsky
 	 * @since 1.10
 	 */
 	public Integer getPositionCount() {
 		if (!this.getType().equals(ScanModuleTypes.CLASSIC)) {
 			return 0; // For now, save axis/channels modules have 0 positions
 		}
 		// a main axis defines the global position count (of a scan module)
 		if (this.getMainAxis() != null) {
 			return this.getMainAxis().getMode().getPositionCount();
 		}
 		List<Double> positionCounts = new ArrayList<Double>();
 		for (Axis axis : this.getAxes()) {
 			// if any axis position count is not available -> abort
 			if (axis.getMode().getPositionCount() == null) {
 				return null;
 			}
 			positionCounts.add(axis.getMode().getPositionCount().doubleValue());
 		}
 		DescriptiveStats stats = new DescriptiveStats(positionCounts);
 		stats.calculateStats();
 		// no main axis, no uncalculatable axes -> return max
 		return stats.getMaximum().intValue();
 	}
 	
 	/**
 	 * Add FXCollection listener to detect changes of channels.
 	 * <p>
 	 * A change can be one of the following:
 	 * <ul>
 	 * 	<li>a channel was updated</li>
 	 *  <li>one or more channels have been added</li>
 	 *  <li>one or more channels have been removed</li>
 	 * </ul>
 	 * 
 	 * @param listener the list change listener
 	 * @since 1.19
 	 * @author Marcus Michalsky
 	 * @see {@link javafx.collections.ObservableList}
 	 */
 	public void addChannelChangeListener(ListChangeListener<? super Channel> 
 			listener) {
 		this.channels.addListener(listener);
 	}
 	
 	/**
 	 * Remove FXCollection listener to no longer detect changes of channels
 	 * 
 	 * @param listener the list change listener
 	 * @since 1.19
 	 * @author Marcus Michalsky
 	 */
 	public void removeChannelChangeListener(ListChangeListener<? super Channel> 
 			listener) {
 		this.channels.removeListener(listener);
 	}
 	
 	private void registerEventValidProperty(ControlEvent controlEvent) {
 		if (controlEvent.getEvent() instanceof ScheduleEvent || 
 				controlEvent.getEvent() instanceof DetectorEvent) {
 			((ScanEvent) controlEvent.getEvent()).addPropertyChangeListener(
 					ScanEvent.VALID_PROP, this);
 		}
 	}
 	
 	private void unregisterEventValidProperty(ControlEvent controlEvent) {
 		if (controlEvent.getEvent() instanceof ScheduleEvent || 
 				controlEvent.getEvent() instanceof DetectorEvent) {
 			((ScanEvent) controlEvent.getEvent()).removePropertyChangeListener(
 					ScanEvent.VALID_PROP, this);
 		}
 	}
 	
 	/**
 	 * Due to the late registration of ScanEvents (due to mutability) during 
 	 * scan description loading the control events don't register themselves 
 	 * via registerEventValidProperty(ControlEvent) because their events aren't
 	 * set at that time. So it must be triggered manually afterwards.
 	 * Usage of this function is therefore only necessary during scan description 
 	 * loading.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.19
 	 * @see Redmine #1401 Comments #16,#17
 	 */
 	public void registerEventValidProperties() {
 		for (ControlEvent controlEvent : this.getPauseEvents()) {
 			this.registerEventValidProperty(controlEvent);
 		}
 		for (ControlEvent controlEvent : this.getRedoEvents()) {
 			this.registerEventValidProperty(controlEvent);
 		}
 		for (ControlEvent controlEvent : this.getBreakEvents()) {
 			this.registerEventValidProperty(controlEvent);
 		}
 		for (ControlEvent controlEvent : this.getTriggerEvents()) {
 			this.registerEventValidProperty(controlEvent);
 		}
 	}
 	
 	private void removeInvalidScanEvents() {
 		for (ControlEvent controlEvent : new CopyOnWriteArrayList<ControlEvent>(
 				this.getPauseEvents())) {
 			if (controlEvent.getEvent() instanceof ScanEvent &&
 					!((ScanEvent)controlEvent.getEvent()).isValid()) {
 				this.removePauseEvent((PauseEvent)controlEvent);
 				logger.debug("Pause Event " + 
 						controlEvent.getEvent().getName() + 
 						" removed from sm " + this.getName() + 
 						" (Chain " + this.getChain().getId() + ") ");
 			}
 		}
 		for (ControlEvent controlEvent : new CopyOnWriteArrayList<ControlEvent>(
 				this.getRedoEvents())) {
 			if (controlEvent.getEvent() instanceof ScanEvent &&
 					!((ScanEvent)controlEvent.getEvent()).isValid()) {
 				this.removeRedoEvent(controlEvent);
 				logger.debug("Redo Event " + 
 						controlEvent.getEvent().getName() + 
 						" removed from sm " + this.getName() +
 						" (Chain " + this.getChain().getId() + ") ");
 			}
 		}
 		for (ControlEvent controlEvent : new CopyOnWriteArrayList<ControlEvent>(
 				this.getBreakEvents())) {
 			if (controlEvent.getEvent() instanceof ScanEvent &&
 					!((ScanEvent)controlEvent.getEvent()).isValid()) {
 				this.removeBreakEvent(controlEvent);
 				logger.debug("Break Event " + 
 						controlEvent.getEvent().getName() + 
 						" removed from sm " + this.getName() +
 						" (Chain " + this.getChain().getId() + ") ");
 			}
 		}
 		for (ControlEvent controlEvent : new CopyOnWriteArrayList<ControlEvent>(
 				this.getTriggerEvents())) {
 			if (controlEvent.getEvent() instanceof ScanEvent &&
 					!((ScanEvent)controlEvent.getEvent()).isValid()) {
 				this.removeTriggerEvent(controlEvent);
 				logger.debug("Trigger Event " + 
 						controlEvent.getEvent().getName() + 
 						" removed from sm " + this.getName() +
 						" (Chain " + this.getChain().getId() + ") ");
 			}
 		}
 	}
 }
