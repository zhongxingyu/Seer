 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
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
 
 package gda.device.motor;
 
 import gda.device.Motor;
 import gda.device.MotorException;
 import gda.device.MotorStatus;
 import gda.device.MotorProperties.MotorEvent;
 import gda.observable.IObservable;
 
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A Dummy motor class
  */
 public class DummyMotor extends MotorBase implements Runnable, IObservable, Motor {
 	
 	private static final Logger logger = LoggerFactory.getLogger(DummyMotor.class);
 	
 	// Moves are simulated in a separate Thread (see run()) which
 	// add positionIncrement to currentPosition numberOfIncrements times
 	// at intervals of incrementalSleepTime.
 	private int numberOfIncrements;
 
 	private int incrementalSleepTime;
 
 	private double positionIncrement;
 
 	// These are the two default values for numberOfIncrements
 	private static final int continuousIncrements = 1000000;
 
 	private static final int nonContinuousIncrements = 10;
 
 	// This flag is used to indicate to the run() thread
 	// whether or not it should be trying to simulate a move.
 	private volatile boolean simulatedMoveRequired = false;
 
 	// This flag has the same purpose as similar flags in real motors -
 	// to indicate to callers of isMoving() whether or not the motor is
 	// actually moving.
 	private volatile boolean motorMoving = false;
 
 	// This flag is used by the run() thread to indicate that it has
 	// reached the wait() state - i.e. that it is ready to work.
 	private volatile boolean waiting = false;
 
 	private volatile double currentPosition;
 
 	private volatile int status;
 
 	private double speed = 0;
 	
 	private double timeToVelocity = 0.1;
 
 	private double targetPosition;
 
 	private Thread runner;
 
 	private Random random = new Random();
 
 	// If randomlyProduceExceptions is true then a limit will be generated
 	// during a move if random.nextGaussian() produces a value greater
 	// than randomLimitTriggerLevel. When a limit is set limitCount is set
 	// to 4.This means that if the next move is away from the limit the
 	// first
 	// 4 incremental moves will still show the limit and then it will clear.
 	private boolean randomlyProduceLimits = false;
 	
 	private boolean randomPositionVariation=false;
 	private double randomPositionVariationAmount = 0.;
 
 	private double randomLimitTriggerLevel = 10.0;
 
 	private int limitCount = 0;
 
 	// If randomlyProduceExceptions is true then moveTo will generate
 	// an exception if random.nextGaussian() produces a value greater
 	// than randomeExceptionTriggerLevel.
 	private boolean randomlyProduceExceptions = false;
 
 	private double randomExceptionTriggerLevel = 10.0;
 
 	// These flags are used to simulate a homing move.
 	private boolean isHomeable = false;
 
 	private boolean homed = false;
 
 	private boolean homing = false;
 
 	/**
 	 * Constructor.
 	 */
 	public DummyMotor() {
 	}
 
 	@Override
 	public void configure() {
 		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
 		runner.start();
 
 		// We have to be sure that the monitoring thread is ready for
 		// work. So this thread yields until the runner thread has set
 		// the waiting flag to true. It might be better to actually
 		// wait() but since this only happens at creation it is unlikely
 		// to cause a problem.
 		while (!waiting) {
 			Thread.yield();
 		}
 		Thread.yield();
 
 		status = MotorStatus._READY;
 		isInitialised = true;
 
 		runner.setName(getClass().getName() + " " + getName());
 		loadPosition(getName(), currentPosition);
 		logger.debug("Loaded motor position " + getPosition());
 		if (speed == 0.0) {
 			speed = getSlowSpeed();
 			if (speed == 0.0) {
 				speed = 1.0;
 			}
 		}
 
 		// if limits not set, then set them to something useable during testing
		if (this.minPosition == Double.NaN) {
 			this.minPosition = -Double.MAX_VALUE;
 		}
 		
		if (this.maxPosition == Double.NaN) {
 			this.maxPosition = Double.MAX_VALUE;
 		}
 
 		this.isInitialised = true;
 	}
 
 	public void setMinPosition(double minPosition) {
 		this.minPosition = minPosition;
 	}
 	
 	public void setMaxPosition(double maxPosition) {
 		this.maxPosition = maxPosition;
 	}
 	
 	/**
 	 * Returns a string to represent the motor.
 	 * 
 	 * @return a string to represent the motor
 	 */
 	@Override
 	public String toString() {
 		return ("DummyMotor " + getName() + " currently at position " + currentPosition + " with status " + status);
 	}
 
 	/**
 	 * Starts a move to requestedPosition
 	 * 
 	 * @param requestedPosition
 	 * @throws MotorException
 	 */
 	@Override
 	public void moveTo(double requestedPosition) throws MotorException {
 
 		if (randomlyProduceExceptions && Math.abs(random.nextGaussian()) > randomExceptionTriggerLevel) {
 			logger.debug("DummyMotor " + getName() + " randomly throwing exception");
 			throw new MotorException(MotorStatus.FAULT, "Random dummy motor fault");
 		}
 
 		double positionChange = requestedPosition - currentPosition;
 		if (positionChange != 0.0) {
 			// The status must be set first otherwise checking status
 			// immediately
 			// after moveTo may return the wrong answer
 			status = MotorStatus._BUSY;
 
 			// The targetPosition is the currentPosition plus the
 			// positionChange
 			// adjusted for any specified backlash. (The backlash will be
 			// corrected
 			// later by the Positioner.)
 			targetPosition = addInBacklash(positionChange) + currentPosition;
 
 			// The numberOfIncrements used is based on the expected time the
 			// move
 			// will take. It should be large enough so that the position
 			// changes
 			// at least every half a second.
 			double totalExpectedMoveTime = Math.abs((targetPosition - currentPosition) / speed);
 			numberOfIncrements = Math.max(nonContinuousIncrements, (int) (totalExpectedMoveTime * 2.0));
 
 			// This is the number of steps the motor will appear to move
 			// each time.
 			positionIncrement = (targetPosition - currentPosition) / numberOfIncrements;
 
 			// The currentSpeed is in steps per second, incrementalSleepTime
 			// should
 			// be in ms hence the 1000.0 in the calculation.
 			incrementalSleepTime = (int) Math.abs(positionIncrement * 1000.0 / speed);
 
 			// An incrementalSleepTime of 0 will cause disaster,
 			if (incrementalSleepTime == 0)
 				incrementalSleepTime = 1;
 
 			logger.debug("DummyMotor speed is: " + speed + " steps per second");
 			logger.debug("DummyMotor total move is: " + (targetPosition - currentPosition) + " steps");
 			logger.debug("DummyMotor move ought to take: " + totalExpectedMoveTime + " seconds");
 			logger.debug("DummyMotor number of numberOfIncrements: " + numberOfIncrements);
 			logger.debug("DummyMotor incrementalSleepTime is: " + incrementalSleepTime + "milliseconds");
 			logger.debug("DummyMotor expected total time for this move: "
 					+ Math.abs(incrementalSleepTime * numberOfIncrements / 1000.0) + "s");
 
 			motorMoving = true;
 			synchronized (this) {
 				simulatedMoveRequired = true;
 				notifyAll();
 			}
 		} else {
 			notifyIObservers(this, MotorEvent.MOVE_COMPLETE);			
 		}
 	}
 
 	/**
 	 * Starts the motor moving continuously
 	 * 
 	 * @param direction
 	 *            the direction to move in
 	 */
 	@Override
 	public void moveContinuously(int direction) {
 		// Continuous movement is simulated by doing a very large
 		// numberOfIncrements in the relevant direction with a very small
 		// incrementalSleepTime.
 		status = MotorStatus._BUSY;
 		numberOfIncrements = continuousIncrements;
 		positionIncrement = 1 * direction;
 		incrementalSleepTime = 1;
 		motorMoving = true;
 		synchronized (this) {
 			simulatedMoveRequired = true;
 			notifyAll();
 		}
 	}
 
 	/**
 	 * Moves the motor by the specified amount.
 	 * 
 	 * @param amount
 	 *            the specified amount.
 	 * @throws MotorException
 	 */
 	@Override
 	public void moveBy(double amount) throws MotorException {
 		moveTo(currentPosition + amount);
 	}
 
 	/**
 	 * Sets the current position (i.e. changes the value without actually moving).
 	 * 
 	 * @param newPosition
 	 *            the current position
 	 */
 	@Override
 	public void setPosition(double newPosition) {
 		currentPosition = newPosition;
 	}
 
 	/**
 	 * Returns the current position
 	 * 
 	 * @return the current position
 	 */
 	@Override
 	public double getPosition() {
 		return currentPosition + ( randomPositionVariation ? (random.nextGaussian()-0.5)*randomPositionVariationAmount : 0.);
 	}
 
 	/**
 	 * Set the speed
 	 * 
 	 * @param stepsPerSecond
 	 *            the new speed
 	 * @throws MotorException
 	 */
 	@Override
 	public void setSpeed(double stepsPerSecond) throws MotorException {
 		speed = stepsPerSecond;
 	}
 
 	/**
 	 * Return the current speed.
 	 * 
 	 * @return the current speed
 	 * @throws MotorException
 	 */
 	@Override
 	public double getSpeed() throws MotorException {
 		return speed;
 	}
 	
 	@Override
 	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
 		this.timeToVelocity = timeToVelocity;
 	}
 	
 	@Override
 	public double getTimeToVelocity() throws MotorException {
 		return timeToVelocity;
 	}
 	
 	/**
 	 * Stop the motor
 	 * 
 	 * @throws MotorException
 	 */
 	@Override
 	public void stop() throws MotorException {
 		simulatedMoveRequired = false;
 		status = MotorStatus._READY;
 		motorMoving = false;
 	}
 
 	/**
 	 * Should do an immediate stop but actually does the same as stop() *
 	 * 
 	 * @throws MotorException
 	 */
 	@Override
 	public void panicStop() throws MotorException {
 		simulatedMoveRequired = false;
 		status = MotorStatus._READY;
 	}
 
 	/**
 	 * Returns the current status.
 	 * 
 	 * @return the current status
 	 */
 	@Override
 	public MotorStatus getStatus() {
 		return MotorStatus.from_int(status);
 	}
 
 	/**
 	 * Returns whether or not the motor is moving.
 	 * 
 	 * @return true if the motor is moving
 	 */
 	@Override
 	public boolean isMoving() {
 		return motorMoving;
 	}
 
 	/**
 	 * Does the position updating which simulates a move.
 	 */
 
 	@Override
 	public synchronized void run() {
 		int i = 0;
 
 		while (true) {
 			// Wait until the simulatedMoveRequired flag is set to true
 			try {
 				waiting = true;
 				do {
 					wait();
 				} while (!simulatedMoveRequired);
 			} catch (Exception ex) {
 				logger.debug(ex.getMessage());
 			}
 
 			// If limitCount is 0 set the status to BUSY a limit may still
 			// be set from last move.
 			if (limitCount == 0)
 				status = MotorStatus._BUSY;
 
 			for (i = 0; i < numberOfIncrements; i++) {
 				if (simulatedMoveRequired) {
 					// When a limit is randomly set, limitCount is set to 4.
 					// It is decremented here and when it reaches 0 the
 					// limit
 					// is cleared. This means that at the beginning of a
 					// move
 					// away from a limit the limit will still show. After 4
 					// increments it will clear.
 					if (status == MotorStatus._UPPERLIMIT) {
 						if (positionIncrement < 0)
 							limitCount--;
 						else
 							break;
 					} else if (status == MotorStatus._LOWERLIMIT) {
 						if (positionIncrement > 0)
 							limitCount--;
 						else
 							break;
 					} else {
 						// If not at a limit see whether a random limit should
 						// be set,
 						// upper or lower is set according to the direction of
 						// movement.
 						if (randomlyProduceLimits && Math.abs(random.nextGaussian()) > randomLimitTriggerLevel) {
 							limitCount = 4;
 							if (positionIncrement > 0)
 								status = MotorStatus._UPPERLIMIT;
 							else if (positionIncrement < 0)
 								status = MotorStatus._LOWERLIMIT;
 							break;
 						}
 					}
 
 					// Clear limit
 					if (limitCount == 0)
 						status = MotorStatus._BUSY;
 
 					// Wait for the incrementalSleepTime
 					try {
 						logger.debug("DummyMotor " + getName() + " incremental wait starting");
 						wait(incrementalSleepTime);
 						logger.debug("DummyMotor " + getName() + " incremental wait over");
 					} catch (InterruptedException ex) {
 						logger.error("DummyMotor " + getName() + " InterruptedException in incremental wait");
 					}
 
 					// Increment the position
 					currentPosition += positionIncrement;
 					logger.debug("DummyMotor " + getName() + " position is now " + currentPosition);
 				}
 
 				// This is the else for the if (simulatedMoveRequired). This
 				// flag
 				// can be set to false by the stop() method which should cause
 				// the
 				// move to be abandoned hence the break.
 				else{
 					status = MotorStatus._READY;
 					break;
 				}
 			}
 
 			// If the move has been completed need to adjust the final
 			// position
 			// to what it should be (may be slightly wrong due to rounding).
 			if (i == numberOfIncrements)
 				currentPosition = targetPosition;
 
 			// If the status is still BUSY the move was completed without a
 			// limit
 			// flag being set and so status should be set to READY.
 			if (status == MotorStatus._BUSY)
 				status = MotorStatus._READY;
 
 			logger.debug("dummy motor" + this.getName() + " status now " + status);
 
 			// Switch off the moving flags
 			simulatedMoveRequired = false;
 			motorMoving = false;
 
 			// If this was a homing move, and it succeeded, then mark the
 			// motor as
 			// homed. NB status = READY is not enough to say that the move
 			// succeeded
 			// as this will happen when a move is stopped.
 			if (currentPosition == targetPosition && homing) {
 				homed = true;
 				homing = false;
 			}
 			notifyIObservers(this, MotorEvent.MOVE_COMPLETE);
 
 		} // End of while(true) loop
 	}
 
 	/**
 	 * Returns whether the motor will randomly produce exceptions. Stupid name of method forced by the conventions.
 	 * 
 	 * @return whether the motor will randomly produce exceptions
 	 */
 	public boolean isRandomlyProduceExceptions() {
 		return randomlyProduceExceptions;
 	}
 
 	/**
 	 * Sets the randomlyProduceExceptions flag.
 	 * 
 	 * @param randomlyProduceExceptions
 	 */
 	public void setRandomlyProduceExceptions(boolean randomlyProduceExceptions) {
 		this.randomlyProduceExceptions = randomlyProduceExceptions;
 	}
 
 	/**
 	 * Returns whether or not the motor is homeable. For real motors this is a fixed property however DummyMotor allows
 	 * it to be set in XML.
 	 * 
 	 * @return true if motor is homeable
 	 */
 	@Override
 	public boolean isHomeable() {
 		return isHomeable;
 	}
 
 	/**
 	 * Tells the motor to home itself.
 	 * 
 	 * @throws MotorException
 	 */
 	@Override
 	public void home() throws MotorException {
 		// homing = true;
 		// moveTo(0.0);
 		homed = true;
 	}
 
 	/**
 	 * Returns whether or not the motor has been homed.
 	 * 
 	 * @return true if motor is already homed
 	 */
 	@Override
 	public boolean isHomed() {
 		return homed;
 	}
 
 	/**
 	 * Gets the value of the trigger level for randomly produced expections.
 	 * 
 	 * @return the trigger level
 	 */
 	public double getRandomExceptionTriggerLevel() {
 		return randomExceptionTriggerLevel;
 	}
 
 	/**
 	 * Sets the trigger level for randomly produced exceptions. The value is used in the same way as the
 	 * randomLimitTriggerLevel. See setRandomLimitTriggerLevel() for an explanation.
 	 * 
 	 * @param randomExceptionTriggerLevel
 	 *            the new value
 	 */
 	public void setRandomExceptionTriggerLevel(double randomExceptionTriggerLevel) {
 		this.randomExceptionTriggerLevel = randomExceptionTriggerLevel;
 	}
 
 	/**
 	 * Gets the trigger level for randomly produced limits.
 	 * 
 	 * @return the trigger level for randomly produced limits
 	 */
 	public double getRandomLimitTriggerLevel() {
 		return randomLimitTriggerLevel;
 	}
 
 	/**
 	 * Sets the trigger level for randomly produced limits. The random number generator is used to generate a Gaussian
 	 * distribution with centre 0.0 and sigma 1.0. A random limit is triggered if abs(random number) is greater than
 	 * trigger level. This means a value of 1.0 will trigger roughly 40% of the time, 2.0 roughly 5% and 3.0 roughly
 	 * 0.3%. The default value of 10.0 has a negligable chance of triggering an event.
 	 * 
 	 * @param randomLimitTriggerLevel
 	 */
 	public void setRandomLimitTriggerLevel(double randomLimitTriggerLevel) {
 		this.randomLimitTriggerLevel = randomLimitTriggerLevel;
 	}
 
 	/**
 	 * Gets the value of the randomlyProduceLimits flag. Stupid name because of conventions.
 	 * 
 	 * @return true if motor produces ramndom limits
 	 */
 	public boolean isRandomlyProduceLimits() {
 		return randomlyProduceLimits;
 	}
 
 	/**
 	 * Sets randomlyProduceLimits flag.
 	 * 
 	 * @param randomlyProduceLimits
 	 */
 	public void setRandomlyProduceLimits(boolean randomlyProduceLimits) {
 		this.randomlyProduceLimits = randomlyProduceLimits;
 	}
 
 	/**
 	 * Sets the isHomeable flag - real motors would not allow this of course.
 	 * 
 	 * @param isHomeable
 	 */
 	public void setHomeable(boolean isHomeable) {
 		this.isHomeable = isHomeable;
 	}
 
 	public boolean isRandomPositionVariation() {
 		return randomPositionVariation;
 	}
 
 	public void setRandomPositionVariation(boolean randomPositionVariation) {
 		this.randomPositionVariation = randomPositionVariation;
 	}
 
 	public double getRandomPositionVariationAmount() {
 		return randomPositionVariationAmount;
 	}
 
 	public void setRandomPositionVariationAmount(double randomPositionVariationAmount) {
 		this.randomPositionVariationAmount = randomPositionVariationAmount;
 	}
 
 	@Override
 	public double getUserOffset() throws MotorException {
 		return 0.;
 	}
 
 }
