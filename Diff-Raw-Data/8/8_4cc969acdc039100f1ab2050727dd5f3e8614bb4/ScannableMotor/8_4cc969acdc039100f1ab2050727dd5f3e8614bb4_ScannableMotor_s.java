 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.scannable;
 
 import gda.configuration.properties.LocalProperties;
 import gda.device.BlockingMotor;
 import gda.device.DeviceException;
 import gda.device.IScannableMotor;
 import gda.device.Motor;
 import gda.device.MotorException;
 import gda.device.MotorProperties.MotorEvent;
 import gda.device.MotorProperties.MotorProperty;
 import gda.device.MotorStatus;
 import gda.device.motor.DummyMotor;
 import gda.device.motor.TotalDummyMotor;
 import gda.device.scannable.component.MotorLimitsComponent;
 import gda.factory.FactoryException;
 import gda.factory.Finder;
 import gda.jython.InterfaceProvider;
 import gda.observable.IObserver;
 
 import java.text.MessageFormat;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Adapter class for motor to work as scannables. This class uses units and has an offset/scaling factor for the motor
  * position.
  * <p>
  * The returned position (user position) is calculated by:
  * <p>
  * userPosition = (motorPosition*scalingFactor) + offset
  * <p>
  * GDALimits are based on the userPosition, but must be in the same units as the motor.
  */
 public class ScannableMotor extends ScannableMotionUnitsBase implements IObserver, IScannableMotor {
 
 	/**
 	 * String to use in getAttribute to get the motor name
 	 */
 	public static final String MOTOR_NAME = "motorName";
 
 	/**
 	 * Name of java property which when set true causes the upper & lower gda limits to be set from the motor limits if
 	 * not already set. Due to the possibility the motor high limit can be associated with a gda low limit we cannot
 	 * separate out the setting of the upper gda limit without also setting the lower gda limit and vice versa. If the
 	 * motor lower and upper limits are not set they are taken to be -Double.MAX_VALUE and Double.MAX_VALUE
 	 * respectively.
 	 */
 	public static final String COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS = "gda.device.scannable.ScannableMotor.copyMotorLimitsIntoScannableLimits";
 
 	private static final Logger logger = LoggerFactory.getLogger(ScannableMotor.class);
 
 	@Deprecated
 	// introduced for 8.18, remove for 8.20 or 8.22 if there have been no troubles
 	private static final String REVERT_TO_POLLING_MOTOR_STATUS = "gda.device.scannable.ScannableMotor.revertToPollingMotorStatus";
 
 	private String motorName;
 
 	private Motor motor;
 
 	private MotorLimitsComponent motorLimitsComponent;
 
 	private static final String IS_BUSY_THROWS_EXCEPTION_WHEN_MOTOR_GOES_INTO_FAULT = "gda.device.scannable.ScannableMotor.isBusyThrowsExceptionWhenMotorGoesIntoFault";
 	
 	private boolean isBusyThrowsExceptionWhenMotorGoesIntoFault = LocalProperties.check(IS_BUSY_THROWS_EXCEPTION_WHEN_MOTOR_GOES_INTO_FAULT, true);
 
 	private boolean returnDemandPosition = false;
 
 	private double demandPositionTolerance;
 
 	private Double lastDemandedInternalPosition = null;
 
 	/**
 	 * Field used to identify whether the demand position tolerance is set.
 	 */
 	private boolean isDemandPositionToleranceSet = false;
 
 	private boolean pollBlockingMotorsStatus = false;
 	
 	private boolean logMoveRequestsWithInfo = false;
 
 	/**
 	 * Constructor
 	 */
 	public ScannableMotor() {
 	}
 
 	/**
 	 * Sets the motor used by this scannable motor.
 	 * 
 	 * @param motor
 	 *            the motor
 	 */
 	public void setMotor(Motor motor) {
 		this.motor = motor;
 		if (motorLimitsComponent == null)
 			setMotorLimitsComponent(new MotorLimitsComponent(motor));
 	}
 
 	/**
 	 * Method required by scripts which need to access the real motor at times. Before the script could get the motor
 	 * name but now that the motor may be set by spring, scripts cannot get the underlying motor.
 	 * 
 	 * @return Motor
 	 */
 	public Motor getMotor() {
 		return motor;
 	}
 
 	@Override
 	public void configure() throws FactoryException {
 		if (motor == null) {
 			setMotor((Motor) Finder.getInstance().find(motorName));
 		}
 
 		motor.addIObserver(this);
 		this.inputNames = new String[] { getName() };
 
 		try {
 
 			// get the hardware units for the underlying motor
 			// perhaps hardware units should be in the motor interface?
 			if (motor instanceof MotorUnitStringSupplier) {
				// try to work out the units the motor works in
				unitsComponent.setHardwareUnitString(((MotorUnitStringSupplier) motor).getUnitString());
 			} else if ((motor instanceof DummyMotor || motor instanceof TotalDummyMotor) // TODO: Get rid of this
 																							// malarchy
 					&& getHardwareUnitString() == null) {
 				// default for simulations
 				unitsComponent.setHardwareUnitString("mm");
 			} else if (getHardwareUnitString() == null) {
 				// else use the value from this.motorUnitString, but if that has not been set then log an error
 				logger.warn("No hardware units set for " + getName()
 						+ ". This will probably cause exceptions and should be resolved.");
 			}
 
 			if (LocalProperties.check(COPY_MOTOR_LIMITS_INTO_SCANNABLE_LIMITS, false)) {
 				copyMotorLimitsIntoScannableLimits();
 			}
 
 			if (LocalProperties.check(REVERT_TO_POLLING_MOTOR_STATUS, false)) {
 				pollBlockingMotorsStatus = true;
 			}
 
 		} catch (Exception e) {
 			throw new FactoryException("Exception during configure of " + getName(), e);
 		}
 		configured = true;
 	}
 
 	private void copyMotorLimitsIntoScannableLimits() throws Exception {
 		if (getUpperGdaLimits() == null || getLowerGdaLimits() == null) {
 			Double upperMotorLimit = getUpperMotorLimit();
 			Double lowerMotorLimit = getLowerMotorLimit();
 			setLowerGdaLimits((lowerMotorLimit == null) ? -Double.MAX_VALUE : lowerMotorLimit);
 			setUpperGdaLimits((upperMotorLimit == null) ? Double.MAX_VALUE : upperMotorLimit);
 		}
 	}
 
 	@Override
 	public Object getAttribute(String attributeName) throws DeviceException {
 		if (attributeName.equals(MOTOR_NAME)) {
 			return this.motorName;
 		} else if (attributeName.equals("lowerMotorLimit")) {
 			return this.getLowerMotorLimit();
 		} else if (attributeName.equals("upperMotorLimit")) {
 			return this.getUpperMotorLimit();
 		}
 		return super.getAttribute(attributeName);
 	}
 
 	@Override
 	public Double[] getFirstInputLimits() throws DeviceException {
 		Double lowerInnerLimit = getLowerInnerLimit();
 		Double upperInnerLimit = getUpperInnerLimit();
 		return lowerInnerLimit == null && upperInnerLimit == null ? null : new Double[] { lowerInnerLimit,
 				upperInnerLimit };
 	}
 
 	/**
 	 * @return Returns the motorName.
 	 */
 	public String getMotorName() {
 		return motorName;
 	}
 
 	/**
 	 * @param motorName
 	 *            The motorName to set.
 	 */
 	public void setMotorName(String motorName) {
 		this.motorName = motorName;
 	}
 
 	Object isBusyAndMovetoLock = new Object();
 
 	private boolean demand_msg_shown = false;
 
 	/**
 	 * {@inheritDoc}. Triggers a motor to move. Throws a DeviceException if the motor is already busy. This method's
 	 * guts are synchronised so that another thread can't enter it before the first thread has completed the motor's
 	 * (asynchronous) moveTo call. For the DeviceException to be thrown properly when a move request is made of a moving
 	 * motor, the motor's getStatus() method must *immediately* report BUSY after it's moveTo call has completed.
 	 */
 	@Override
 	public void rawAsynchronousMoveTo(Object internalPosition) throws DeviceException {
 
 		synchronized (isBusyAndMovetoLock) {
 
 			Double internalDoublePosition;
 
 			// check motor status. Throw an error if it is not idle. TODO: There is a race condition here
 			if (this.isBusy()) {
 				throw new DeviceException("The motor " + getName() + " was already busy so could not be moved");
 			}
 
 			try {
 				internalDoublePosition = PositionConvertorFunctions.toDouble(internalPosition);
 				if (isLogMoveRequestsWithInfo()) {
 					logger.info("{}: move to {} ", getName(), internalPosition);
 				} else {
 					logger.debug("{}: move to {} ", getName(), internalPosition);
 				}
 				notifyIObservers(this, new ScannableStatus(getName(), ScannableStatus.BUSY));
 				this.motor.moveTo(internalDoublePosition);
 				lastDemandedInternalPosition = internalDoublePosition;
 			} catch (IllegalArgumentException e) {
 				throw new DeviceException(getName() + ".rawAsynchronousMoveTo() could not convert "
 						+ internalPosition.toString() + " to a double.");
 			} catch (MotorException e) {
 				throw new DeviceException(e.getMessage(), e);
 			}
 			demand_msg_shown = false;
 		}
 
 	}
 
 	/**
 	 * Read the position in its internal (user) representation. If configured to return demand positions with
 	 * {@link #setReturnDemandPosition(boolean)} and the last demanded position is within
 	 * {@link #getDemandPositionTolerance()} of the physical internal motor position, then return this demand position
 	 * instead.
 	 */
 	@Override
 	public Object rawGetPosition() throws DeviceException {
 		if (this.motor == null) {
 			return null; // TODO: Should throw an exception.
 		}
 		try {
 			return this.motor.getPosition();
 		} catch (MotorException e) {
 			throw new DeviceException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Return the last demanded motor/internal position if one has been set, otherwise return the current position. If
 	 * the motor is stopped, and the actual position is not close to the the demand position, then the current position
 	 * is returned instead.
 	 * 
 	 * @throws DeviceException
 	 */
 	public Object rawGetDemandPosition() throws DeviceException {
 
 		double currentInternalPosition = (Double) rawGetPosition();
 
 		if (lastDemandedInternalPosition == null) {
 			return currentInternalPosition;
 		}
 
 		if (isBusy()) {
 			return lastDemandedInternalPosition;
 		}
 
 		// motor is not moving
 		if (Math.abs(currentInternalPosition - lastDemandedInternalPosition) <= demandPositionTolerance) {
 			return lastDemandedInternalPosition;
 		} // else
 
 		// motor is not moving but is not at the last demand position.
 		if (!demand_msg_shown) {
 			String msg = MessageFormat.format(
 					"{0} is returning a position based on its real motor position ({1}) rather than its last demanded position({2}),\n"
 							+ "as these differ by more than the configured demand position tolerance ({3}).",
 					getName(), currentInternalPosition, lastDemandedInternalPosition, demandPositionTolerance);
 			demand_msg_shown = true; // reset by rawAsynchMoveto
 			logger.info(msg);
 			InterfaceProvider.getTerminalPrinter().print("WARNING: " + msg);
 		}
 		return currentInternalPosition;
 
 	}
 
 	/**
 	 * {@inheritDoc} Return the demand position from {@link #getDemandPosition()} if configured with
 	 * {@link #setReturnDemandPosition(boolean)} to do so and the motor is not moving, otherwise returns the actual
 	 * position. i.e. if the motor is moving the actual position is always returned.
 	 */
 	@Override
 	public Object getPosition() throws DeviceException {
 		if (isReturningDemandPosition() && !isBusy()) {
 			return getDemandPosition();
 		}
 		return getActualPosition();
 	}
 
 	/**
 	 * Return the last demanded user/external position if one has been set. If the motor is stopped, and the actual
 	 * position is not close to the the demand position, then the current position is returned instead.
 	 * 
 	 * @throws DeviceException
 	 */
 	public Object getDemandPosition() throws DeviceException {
 		return internalToExternal(rawGetDemandPosition());
 	}
 
 	public Object getActualPosition() throws DeviceException {
 		return internalToExternal(rawGetPosition());
 	}
 
 	/**
 	 * Return true if motor is busy. Throw an exception if the motor is in a FAULT state and
 	 * {@link #isIsBusyThrowingExceptionWhenMotorGoesIntoFault()} is true.
 	 */
 	@Override
 	public boolean isBusy() throws DeviceException {
 		try {
 			if (isIsBusyThrowingExceptionWhenMotorGoesIntoFault()) {
 				raiseExceptionIfInFault();
 			}
 			int currentStatus = this.motor.getStatus().value();
 			return currentStatus == MotorStatus._BUSY;
 		} catch (MotorException e) {
 			throw new DeviceException(e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public void waitWhileBusy() throws DeviceException, InterruptedException {
 		logger.debug("{}: start waiting <<", getName());
 
 		if ((getMotor() instanceof BlockingMotor) && (!pollBlockingMotorsStatus)) {
 			((BlockingMotor) motor).waitWhileStatusBusy();
 			if (isIsBusyThrowingExceptionWhenMotorGoesIntoFault()) {
 				raiseExceptionIfInFault();
 			}
 		} else {
 			super.waitWhileBusy();
 		}
 
 		logger.debug("{}: waiting complete >>", getName());
 	}
 
 	private void raiseExceptionIfInFault() throws MotorException {
 		if ((motor.getStatus() == MotorStatus.FAULT) || (motor.getStatus() == MotorStatus.UPPERLIMIT)
 				|| (motor.getStatus() == MotorStatus.LOWERLIMIT)
 				|| (motor.getStatus() == MotorStatus.SOFTLIMITVIOLATION))
 			throw new MotorException(motor.getStatus(), "\nDuring " + getName() + ".isBusy() EPICS Motor was found in "
 					+ motor.getStatus() + " status. Please check EPICS Screen.");
 	}
 
 	@Override
 	public void stop() throws DeviceException {
 		try {
 			this.motor.stop();
 		} catch (MotorException e) {
 			throw new DeviceException(e.getMessage(), e);
 		}
 	}
 
 	private boolean isScalingFactorNegative() {
 		return (getScalingFactor() == null) ? false : getScalingFactor()[0] < 0.;
 	}
 
 	/**
 	 * Returns the lower motor limit in its external representation. Null if there is no lower Motor limit.
 	 * 
 	 * @return limit in external representation
 	 * @throws DeviceException
 	 */
 	public Double getLowerMotorLimit() throws DeviceException {
 		if (motorLimitsComponent == null) {
 			throw new DeviceException(
 					"Could not check "
 							+ getName()
 							+ "'s lower motor limit as there is no limits component (probably because the no motor has been set).");
 		}
 		Double[] internalArray = (isScalingFactorNegative()) ? motorLimitsComponent.getInternalUpper()
 				: motorLimitsComponent.getInternalLower();
 		Double internal = (internalArray == null) ? null : internalArray[0];
 		return PositionConvertorFunctions.toDouble(internalToExternal(internal));
 	}
 
 	/**
 	 * Returns the upper motor limit in its external representation. Null if there is no upper Motor limit.
 	 * 
 	 * @return limit in external representation
 	 * @throws DeviceException
 	 */
 	public Double getUpperMotorLimit() throws DeviceException {
 		if (motorLimitsComponent == null) {
 			throw new DeviceException(
 					"Could not check "
 							+ getName()
 							+ "'s upper motor limit as there is no limits component (probably because the no motor has been set).");
 		}
 		Double[] internalArray = (isScalingFactorNegative()) ? motorLimitsComponent.getInternalLower()
 				: motorLimitsComponent.getInternalUpper();
 		Double internal = (internalArray == null) ? null : internalArray[0];
 		return PositionConvertorFunctions.toDouble(internalToExternal(internal));
 	}
 
 	/**
 	 * Returns the innermost (i.e. the most limiting) of the lower Scannable and Motor limits.
 	 * 
 	 * @return the highest minimum limit, or null if neither are set.
 	 * @throws DeviceException
 	 */
 	public Double getLowerInnerLimit() throws DeviceException {
 
 		Double lowerGdaLimit = (getLowerGdaLimits() == null) ? null : getLowerGdaLimits()[0];
 		Double lowerMotorLimit = getLowerMotorLimit();
 		if (lowerGdaLimit == null & lowerMotorLimit == null)
 			return null;
 		if (lowerGdaLimit == null)
 			return lowerMotorLimit;
 		if (lowerMotorLimit == null)
 			return lowerGdaLimit;
 		return (lowerGdaLimit > lowerMotorLimit) ? lowerGdaLimit : lowerMotorLimit;
 	}
 
 	/**
 	 * Returns the innermost (i.e. the most limiting) of the upper Scannable and Motor limits.
 	 * 
 	 * @return the lowest maximum limit, or null if neither are set.
 	 * @throws DeviceException
 	 */
 	public Double getUpperInnerLimit() throws DeviceException {
 
 		Double upperGdaLimit = (getUpperGdaLimits() == null) ? null : getUpperGdaLimits()[0];
 		Double upperMotorLimit = getUpperMotorLimit();
 		if (upperGdaLimit == null & upperMotorLimit == null)
 			return null;
 		if (upperGdaLimit == null)
 			return upperMotorLimit;
 		if (upperMotorLimit == null)
 			return upperGdaLimit;
 		return (upperGdaLimit < upperMotorLimit) ? upperGdaLimit : upperMotorLimit;
 	}
 
 	@Override
 	public String toFormattedString() {
 		String report = super.toFormattedString();
 		Double lowerMotorLimit;
 		Double upperMotorLimit;
 		if (motorLimitsComponent == null) {
 			return report;
 		}
 		try {
 			lowerMotorLimit = getLowerMotorLimit();
 			upperMotorLimit = getUpperMotorLimit();
 		} catch (DeviceException e) {
 			throw new RuntimeException(getName() + ": exception while getting Motor limits. " + e.getMessage() + "; "
 					+ e.getCause(), e);
 		}
 
 		if (lowerMotorLimit != null || upperMotorLimit != null) {
 			report += " mot(";
 			if (lowerMotorLimit != null)
 				report += String.format(getOutputFormat()[0], lowerMotorLimit);
 			report += ":";
 			if (upperMotorLimit != null)
 				report += String.format(getOutputFormat()[0], upperMotorLimit);
 			report += ")";
 		}
 
 		try {
 			if (isReturningDemandPosition() && (lastDemandedInternalPosition != null) && (!isBusy())) {
 				if (Math.abs(((Double) rawGetPosition()) - lastDemandedInternalPosition) <= demandPositionTolerance) {
 					report += " demand";
 				} else {
 					// stopped, but not at target so show demand as well
 					Double[] offsetArray = new Double[getInputNames().length + getExtraNames().length];
 					if (getOffset() != null) {
 						// Complication - the offset array may not have offsets for the extra fields.
 						System.arraycopy(getOffset(), 0, offsetArray, 0, getOffset().length);
 					}
 					String[] unitStringArray = new String[getInputNames().length + getExtraNames().length];
 					for (int i = 0; i < unitStringArray.length; i++) {
 						unitStringArray[i] = getUserUnits();
 					}
 
 					report += " *demand="
 							+ ScannableUtils.getFormattedCurrentPositionArray(
 									internalToExternal(lastDemandedInternalPosition), 1, getOutputFormat())[0] + "*";
 				}
 			}
 		} catch (DeviceException e) {
 			throw new RuntimeException(getName() + ".toFormattedString() exception:", e);
 		}
 		return report;
 	}
 
 	/**
 	 * Get the speed of the underlying motor
 	 * 
 	 * @return speed in the motor's units
 	 * @throws DeviceException
 	 */
 	@Override
 	public double getSpeed() throws DeviceException {
 		try {
 			if (this.motor != null) {
 				return motor.getSpeed();
 			}
 		} catch (MotorException e) {
 			logger.error(getName() + ": exception while getting speed from motor. ", e);
 			throw new DeviceException(e.getMessage(), e);
 		}
 		return -1;
 	}
 
 	/**
 	 * Set the speed of the underlying motor
 	 * 
 	 * @param theSpeed
 	 *            in the motor's units
 	 * @throws DeviceException
 	 */
 	public void setSpeed(double theSpeed) throws DeviceException {
 		try {
 			if (this.motor != null) {
 				motor.setSpeed(theSpeed);
 			}
 		} catch (MotorException e) {
 			logger.error(getName() + ": exception while setting speed of motor. ", e);
 			throw new DeviceException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * Returns this motor's time to velocity.
 	 */
 	public double getTimeToVelocity() throws DeviceException {
 		try {
 			return motor.getTimeToVelocity();
 		} catch (MotorException me) {
 			logger.error(String.format("%s: unable to get time to velocity", getName()), me);
 			throw new DeviceException(me.getMessage(), me);
 		}
 	}
 
 	/**
 	 * Sets this motor's time to velocity.
 	 */
 	public void setTimeToVelocity(double timeToVelocity) throws DeviceException {
 		try {
 			motor.setTimeToVelocity(timeToVelocity);
 		} catch (MotorException me) {
 			logger.error(String.format("%s: unable to set time to velocity", getName()), me);
 			throw new DeviceException(me.getMessage(), me);
 		}
 	}
 
 	@Override
 	public void update(Object theObserved, Object changeCode) {
 
 		if (theObserved == this.motor && changeCode instanceof MotorStatus) {
 
 			int motorStatus = ((MotorStatus) changeCode).value();
 
 			if (motorStatus == MotorStatus._READY) {
 				notifyIObservers(this, new ScannableStatus(this.getName(), ScannableStatus.IDLE));
 			} else if (motorStatus == MotorStatus._BUSY) {
 				// do nothing as should have already informed IObservers during asynchronousMoveTo
 			} else {
 				notifyIObservers(this, new ScannableStatus(this.getName(), ScannableStatus.FAULT));
 			}
 		} else if (theObserved instanceof MotorProperty && changeCode instanceof MotorStatus) {
 
 			int motorStatus = ((MotorStatus) changeCode).value();
 
 			if (motorStatus == MotorStatus._READY) {
 				notifyIObservers(this, new ScannableStatus(this.getName(), ScannableStatus.IDLE));
 			} else if (motorStatus == MotorStatus._BUSY) {
 				// do nothing as should have already informed IObservers during asynchronousMoveTo
 			} else {
 				notifyIObservers(this, new ScannableStatus(this.getName(), ScannableStatus.FAULT));
 			}
 		}
 
 		// to account for inconsistent message types from EpicsMotor
 		else if (changeCode instanceof MotorEvent) {
 			if (((MotorEvent) changeCode) == MotorEvent.MOVE_COMPLETE) {
 				notifyIObservers(this, new ScannableStatus(this.getName(), ScannableStatus.IDLE));
 			}
 		}
 		// According to Paul G this will cause too many events to be fired.
 		// if (theObserved == MotorProperty.POSITION) {
 		// notifyIObservers(this, new ScannablePosition(this.getName(), (Double)changeCode));
 		// }
 
 	}
 
 	public void setMotorLimitsComponent(MotorLimitsComponent motorLimitsComponent) {
 		this.motorLimitsComponent = motorLimitsComponent;
 		addPositionValidator(getMotorLimitsComponent());
 	}
 
 	public MotorLimitsComponent getMotorLimitsComponent() {
 		return motorLimitsComponent;
 	}
 
 	// Castor fails with super's vararg method
 	public void setScalingFactor(Double scale) {
 		super.setScalingFactor(new Double[] { scale });
 	}
 
 	// Castor fails with super's vararg method
 	public void setOffset(Double offset) {
 		super.setOffset(new Double[] { offset });
 	}
 
 	public void setIsBusyThrowsExceptionWhenMotorGoesIntoFault(boolean isBusyThrowsExceptionWhenMotorGoesIntoFault) {
 		this.isBusyThrowsExceptionWhenMotorGoesIntoFault = isBusyThrowsExceptionWhenMotorGoesIntoFault;
 	}
 
 	public boolean isIsBusyThrowingExceptionWhenMotorGoesIntoFault() {
 		return isBusyThrowsExceptionWhenMotorGoesIntoFault;
 	}
 
 	public boolean isLogMoveRequestsWithInfo() {
 		return logMoveRequestsWithInfo;
 	}
 
 	public void setLogMoveRequestsWithInfo(boolean logMoveRequestsWithInfo) {
 		this.logMoveRequestsWithInfo = logMoveRequestsWithInfo;
 	}
 
 	/**
 	 * If true, calls to {@link #rawGetPosition()} will return the last demanded position. If the current position is
 	 * greater than demandPositionTolerance from the last demanded position the actual current position is returned and
 	 * a warning logged and displayed on the console.
 	 * 
 	 * @param returnDemandPosition
 	 */
 	public void setReturnDemandPosition(boolean returnDemandPosition) {
 		this.returnDemandPosition = returnDemandPosition;
 	}
 
 	/**
 	 * See {@link #setReturnDemandPosition(boolean)}
 	 */
 	public boolean isReturningDemandPosition() {
 		return returnDemandPosition;
 	}
 
 	/**
 	 * See {@link #setReturnDemandPosition(boolean)} Value is in internal/motor units and scale
 	 */
 	public void setDemandPositionTolerance(double demandPositionTolerance) {
 		this.demandPositionTolerance = demandPositionTolerance;
 		this.isDemandPositionToleranceSet = true;
 	}
 
 	/**
 	 * See {@link #setReturnDemandPosition(boolean)} Value is in internal/motor units and scale
 	 */
 	@Override
 	public double getDemandPositionTolerance() {
 		if (!isDemandPositionToleranceSet) {
 			try {
 				demandPositionTolerance = motor.getRetryDeadband();
 			} catch (MotorException e) {
 				logger.error("{} failed to get the tolerance from motor: {}", getName(), e);
 			}
 		}
 		return demandPositionTolerance;
 	}
 
 	@Override
 	public double getMotorResolution() throws DeviceException {
 		return motor.getMotorResolution();
 	}
 
 	@Override
 	public double getUserOffset() throws DeviceException {
 		return motor.getUserOffset();
 	}
 
 }
