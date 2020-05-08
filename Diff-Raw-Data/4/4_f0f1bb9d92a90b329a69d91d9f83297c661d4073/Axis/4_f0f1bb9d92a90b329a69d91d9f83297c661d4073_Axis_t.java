 package de.ptb.epics.eve.data.scandescription;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.xml.datatype.Duration;
 
 import org.apache.log4j.Logger;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 import de.ptb.epics.eve.data.scandescription.axismode.AddMultiplyMode;
 import de.ptb.epics.eve.data.scandescription.axismode.AxisMode;
 import de.ptb.epics.eve.data.scandescription.axismode.FileMode;
 import de.ptb.epics.eve.data.scandescription.axismode.PluginMode;
 import de.ptb.epics.eve.data.scandescription.axismode.PositionlistMode;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
  * This class describes the behavior of an axis during the main phase of a scan
  * module.
  * 
  * @author Stephan Rehfeld <stephan.rehfeld( -at -) ptb.de>
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class Axis extends AbstractMainPhaseBehavior implements
 		PropertyChangeListener {
 	
 	// logging
 	private static Logger logger = Logger.getLogger(Axis.class.getName());
 
 	// delegated observable
 	private PropertyChangeSupport propertyChangeSupport;
 	
 	private Stepfunctions stepfunction;
 	private AxisMode mode;
 	private PositionMode positionMode;
 
 	/**
 	 * Constructs an <code>Axis</code>.
 	 * 
 	 * @param scanModule the scan module the axis corresponds to
 	 * @throws IllegalArgumentException if the argument is <code>null</code>
 	 */
 	public Axis(final ScanModule scanModule) {
 		if (scanModule == null) {
 			throw new IllegalArgumentException(
 					"The parameter 'scanModule' must not be null!");
 		}
 		this.scanModule = scanModule;
 		this.positionMode = PositionMode.ABSOLUTE;
 		this.propertyChangeSupport = new PropertyChangeSupport(this);
 	}
 
 	/**
 	 * Better Constructor.
 	 * 
 	 * @param scanModule the scan module the axis corresponds to
 	 * @param axis the device for this behavior
 	 * @since 1.2
 	 * @throws IllegalArgumentException if <code>scanModule</code> is 
 	 * 			<code>null</code>
 	 */
 	public Axis(final ScanModule scanModule, MotorAxis axis) {
 		this(scanModule);
 		this.setMotorAxis(axis);
 		if (axis.getGoto().isDiscrete()) {
 			this.setStepfunction(Stepfunctions.POSITIONLIST);
 			StringBuffer sb = new StringBuffer();
 			for (String s : axis.getGoto().getDiscreteValues()) {
 				sb.append(s + ",");
 			}
 			this.setPositionlist(sb.substring(0, sb.length() - 1));
 			
 			axis.connect();
 			axis.addPropertyChangeListener("discreteValues", this);
 		} else {
 			this.setStepfunction(Stepfunctions.ADD);
 		}
 	}
 
 	/**
 	 * @return the stepfunction
 	 */
 	public Stepfunctions getStepfunction() {
 		return stepfunction;
 	}
 
 	/**
 	 * @param stepfunction the step function to set
 	 */
 	public void setStepfunction(Stepfunctions stepfunction) {
 		if (this.mode != null) {
 			this.mode.removePropertyChangeListener(this);
 		}
 		this.stepfunction = stepfunction;
 		// listener to forward mode changes to the axis (dirty state)
 		this.mode = AxisMode.newMode(stepfunction, this);
 		this.mode.addPropertyChangeListener(this);
 		updateListeners();
 	}
 
 	/**
 	 * Returns the motor axis.
 	 * 
 	 * @return the motor axis
 	 */
 	public MotorAxis getMotorAxis() {
 		return (MotorAxis) this.abstractDevice;
 	}
 
 	/**
 	 * Sets the motor axis.
 	 * 
 	 * @param motorAxis the motor axis that should be set
 	 */
 	public void setMotorAxis(final MotorAxis motorAxis) {
 		this.abstractDevice = motorAxis;
 		if (motorAxis.getGoto().isDiscrete()) {
 			this.setStepfunction(Stepfunctions.POSITIONLIST);
 		} else {
 			this.setStepfunction(Stepfunctions.ADD);
 		}
 		updateListeners();
 	}
 
 	/**
 	 * Returns the type of the motor axis (goto).
 	 * 
 	 * @return the type of the motor axis
 	 */
 	public DataTypes getType() {
 		return this.getMotorAxis().getType();
 	}
 
 	/**
 	 * Returns the position mode.
 	 * 
 	 * @return the position mode
 	 */
 	public PositionMode getPositionMode() {
 		return this.positionMode;
 	}
 
 	/**
 	 * Sets the position mode.
 	 * 
 	 * @param positionMode the position mode that should be set
 	 */
 	public void setPositionMode(final PositionMode positionMode) {
 		PositionMode oldValue = this.positionMode;
 		this.positionMode = positionMode;
		if (this.getType().equals(DataTypes.DATETIME)) {
			this.setStepfunction(this.getStepfunction());
		}
 		this.propertyChangeSupport.firePropertyChange("positionMode",
 				oldValue, this.positionMode);
 		updateListeners();
 	}
 	
 	/**
 	 * @return the mode
 	 */
 	public AxisMode getMode() {
 		return mode;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public File getFile() {
 		if (this.getStepfunction().equals(Stepfunctions.FILE)) {
 			return ((FileMode)this.mode).getFile();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param file
 	 */
 	public void setFile(File file) {
 		if (!this.getStepfunction().equals(Stepfunctions.FILE)) {
 			this.setStepfunction(Stepfunctions.FILE);
 		}
 		((FileMode)this.mode).setFile(file);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getPositionlist() {
 		if (this.getStepfunction().equals(Stepfunctions.POSITIONLIST)) {
 			return ((PositionlistMode)this.mode).getPositionList();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param list
 	 */
 	public void setPositionlist(String list) {
 		if (!this.getStepfunction().equals(Stepfunctions.POSITIONLIST)) {
 			this.setStepfunction(Stepfunctions.POSITIONLIST);
 		}
 		((PositionlistMode)this.mode).setPositionList(list);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public PluginController getPluginController() {
 		if (this.getStepfunction().equals(Stepfunctions.PLUGIN)) {
 			return ((PluginMode)this.mode).getPluginController();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param plugin
 	 */
 	public void setPluginController(PluginController plugin) {
 		if (!this.getStepfunction().equals(Stepfunctions.PLUGIN)) {
 			this.setStepfunction(Stepfunctions.PLUGIN);
 		}
 		((PluginMode)this.mode).setPluginController(plugin);
 	}
 	
 	
 	// TODO AddMultiply getter setter
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Object getStart() {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			return ((AddMultiplyMode<?>)this.mode).getStart();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param start
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStart(int start) {
 		if (!this.getType().equals(DataTypes.INT)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Integer>)this.mode).setStart(start);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param start
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStart(double start) {
 		if (!this.getType().equals(DataTypes.DOUBLE)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Double>)this.mode).setStart(start);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param start
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStart(Date start) {
 		if (!this.getType().equals(DataTypes.DATETIME)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Date>)this.mode).setStart(start);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param start
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStart(Duration start) {
 		if (!this.getType().equals(DataTypes.DATETIME)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Duration>)this.mode).setStart(start);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Object getStop() {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			return ((AddMultiplyMode<?>)this.mode).getStop();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param stop
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStop(int stop) {
 		if (!this.getType().equals(DataTypes.INT)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Integer>)this.mode).setStop(stop);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param stop
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStop(double stop) {
 		if (!this.getType().equals(DataTypes.DOUBLE)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Double>)this.mode).setStop(stop);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param stop
 	 */
 	@SuppressWarnings("javadoc")
 	public void setStop(Date stop) {
 		if (!this.getType().equals(DataTypes.DATETIME)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Date>)this.mode).setStop(stop);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param stop
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStop(Duration stop) {
 		if (!this.getType().equals(DataTypes.DATETIME)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Duration>)this.mode).setStop(stop);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Object getStepwidth() {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			return ((AddMultiplyMode<?>)this.mode).getStepwidth();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param stepwidth
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStepwidth(int stepwidth) {
 		if (!this.getType().equals(DataTypes.INT)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Integer>)this.mode).setStepwidth(stepwidth);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param stepwidth
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStepwidth(double stepwidth) {
 		if (!this.getType().equals(DataTypes.DOUBLE)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Double>)this.mode).setStepwidth(stepwidth);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param stepwidth
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStepwidth(Date stepwidth) {
 		if (!this.getType().equals(DataTypes.DATETIME)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Date>)this.mode).setStepwidth(stepwidth);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param stepwidth
 	 */
 	@SuppressWarnings("unchecked")
 	public void setStepwidth(Duration stepwidth) {
 		if (!this.getType().equals(DataTypes.DATETIME)) {
 			return;
 		}
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<Duration>)this.mode).setStepwidth(stepwidth);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public double getStepcount() {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			return ((AddMultiplyMode<?>)this.mode).getStepcount();
 		}
 		return Double.NaN;
 	}
 	
 	/**
 	 * 
 	 * @param stepcount
 	 */
 	public void setStepcount(double stepcount) {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<?>)this.mode).setStepcount(stepcount);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isMainAxis() {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			return ((AddMultiplyMode<?>)this.mode).isMainAxis();
 		}
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param mainAxis
 	 */
 	public void setMainAxis(boolean mainAxis) {
 		if (this.getStepfunction().equals(Stepfunctions.ADD) ||
 				this.getStepfunction().equals(Stepfunctions.MULTIPLY)) {
 			((AddMultiplyMode<?>)this.mode).setMainAxis(mainAxis);
 			
 		}
 	}
 	
 	/**
 	 * Return a well-formatted string with a valid value for the datatype. If
 	 * value can not be converted, return a default value
 	 * 
 	 * @param value The value that will be formatted.
 	 * @return a well-formatted string with a valid value
 	 */
 	public String formatValueDefault(final String value) {
 		return this.getMotorAxis().formatValueDefault(value);
 	}
 
 	/**
 	 * Return a well-formatted string with a valid value for the datatype. If
 	 * value can not be converted, return null
 	 * 
 	 * @param value
 	 *            The value that will be formatted.
 	 * @return a well-formatted string or null
 	 */
 	public String formatValue(final String value) {
 		return this.getMotorAxis().formatValue(value);
 	}
 
 	/**
 	 * Return a well-formatted string with a valid value for the datatype. If
 	 * value can not be converted, return a default value
 	 * 
 	 * @return a well-formatted string with a valid default value
 	 */
 	public String getDefaultValue() {
 		return this.getMotorAxis().getDefaultValue();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public List<IModelError> getModelErrors() {
 		final List<IModelError> errorList = new ArrayList<IModelError>();
 		if (this.mode != null) {
 			errorList.addAll(this.mode.getModelErrors());
 		}
 		return errorList;
 	}
 
 	/*
 	 * 
 	 */
 	private void updateListeners() {
 		final CopyOnWriteArrayList<IModelUpdateListener> list = 
 			new CopyOnWriteArrayList<IModelUpdateListener>(modelUpdateListener);
 		for (IModelUpdateListener imul : list) {
 			imul.updateEvent(new ModelUpdateEvent(this, null));
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals("discreteValues")) {
 			String values = "";
 			if (this.getMotorAxis().getGoto().getType().equals(DataTypes.STRING)) {
 				for (String s : (List<String>) e.getNewValue()) {
 					values += s + ",";
 				}
 			} else if (this.getMotorAxis().getGoto().getType()
 					.equals(DataTypes.INT)) {
 				for (int i = 1; i <= ((List<String>) e.getNewValue()).size(); i++) {
 					values += i + ",";
 				}
 			}
 			if (!values.isEmpty()) {
 				String result = values.substring(0, values.length() - 1);
 				if (!this.getPositionlist().equals(result)) {
 					this.setPositionlist(values.substring(0, values.length() -1));
 				}
 				if (logger.isDebugEnabled()) {
 					logger.debug("got enum values: "
 							+ values.substring(0, values.length() - 1));
 					logger.debug(this.getMotorAxis().getChannelAccess().getDiscretePositions().toString());
 				}
 			}
 			this.getMotorAxis().removePropertyChangeListener("discreteValues",
 					this);
 			this.getMotorAxis().disconnect();
 		}
 		if (e.getSource() instanceof AddMultiplyMode<?>) {
 			this.propertyChangeSupport.firePropertyChange(e);
 		}
 		if (e.getSource() instanceof ScanModule && 
 				e.getPropertyName().equals(ScanModule.MAIN_AXIS_PROP)) {
 			if (this.getMode() != null
 					&& this.getMode() instanceof AddMultiplyMode<?>) {
 				((AddMultiplyMode<?>) this.getMode())
 						.matchMainAxis(((ScanModule) e.getSource())
 								.getMainAxis());
 			}
 		}
 		if (e.getSource() instanceof AxisMode) {
 			updateListeners();
 		}
 	}
 
 	/**
 	 * @param propertyName the name of the property to listen to
 	 * @param listener the listener
 	 * @see {@link java.beans.PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.addPropertyChangeListener(propertyName,
 				listener);
 	}
 
 	/**
 	 * @param propertyName the name of the property to stop listen to
 	 * @param listener the listener
 	 * @see {@link java.beans.PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)}
 	 */
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		this.propertyChangeSupport.removePropertyChangeListener(propertyName,
 				listener);
 	}
 }
