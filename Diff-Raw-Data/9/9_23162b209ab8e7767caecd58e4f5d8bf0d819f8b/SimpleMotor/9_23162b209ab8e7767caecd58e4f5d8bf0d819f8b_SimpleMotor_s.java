 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
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
 
 package gda.device.motor.simplemotor;
 
 import gda.device.DeviceException;
 import gda.device.Motor;
 import gda.device.MotorException;
 import gda.device.MotorStatus;
 import gda.factory.FactoryException;
 import gda.jython.accesscontrol.MethodAccessProtected;
 import gda.observable.IObserver;
 import gda.observable.ObservableComponent;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.StringUtils;
 
 /**
  * part of 
  */
 public class SimpleMotor implements Motor, InitializingBean{
 
 	SimpleMotorController smc;
 	
 	@Override
 	@MethodAccessProtected(isProtected = true)
 	public void setAttribute(String attributeName, Object value) throws DeviceException {
 		//do not support attributes
 	}
 
 	@Override
 	public Object getAttribute(String attributeName) throws DeviceException {
 		//do not support attributes
 		return null;
 	}
 
 	@Override
 	public void close() throws DeviceException {
 	}
 
 	@Override
 	@MethodAccessProtected(isProtected = true)
 	public void setProtectionLevel(int newLevel) throws DeviceException {
 		//do not support protection
 	}
 
 	@Override
 	public int getProtectionLevel() throws DeviceException {
 		//do not support protection
 		return 0;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	String name="";
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	private ObservableComponent observableComponent = new ObservableComponent();
 	@Override
 	public void addIObserver(IObserver observer) {
 		observableComponent.addIObserver(observer);
 	}
 
 	@Override
 	public void deleteIObserver(IObserver observer) {
 		observableComponent.deleteIObserver(observer);
 	}
 
 	@Override
 	public void deleteIObservers() {
 		observableComponent.deleteIObservers();
 	}
 
 	@Override
 	public void reconfigure() throws FactoryException {
 		//do not support reconfigure
 	}
 
 	@Override
 	@MethodAccessProtected(isProtected = true)
 	public void moveBy(double increment) throws MotorException {
 		try {
 			double targetPosition = getPosition() + increment;
 			targetRangeCheck(targetPosition);
 			moveTo(targetPosition);
 		} catch (Throwable ex) {
 			throw new MotorException(getStatus(), "Motor " + name + "failed to moveBy " + increment, ex);
 		}
 	}
 
 	private void targetRangeCheck(double requestedPosition) throws MotorException {
 
 		final double lowerLimit = getMinPosition();
 		final double upperLimit = getMaxPosition();
 
 		if (requestedPosition < lowerLimit) {
 			throw (new MotorException(MotorStatus.LOWERLIMIT, "Motor " + name + " " + requestedPosition + " outside lower hardware limit of "
 					+ lowerLimit));
 		}
 
 		else if (requestedPosition > upperLimit) {
 			throw (new MotorException(MotorStatus.UPPERLIMIT, "Motor " + name + " " + requestedPosition + " outside upper hardware limit of "
 					+ upperLimit));
 		}
 	}
 
 	@Override
 	@MethodAccessProtected(isProtected = true)
 	public void moveTo(double position) throws MotorException {
 		targetRangeCheck(position);
 		if (getStatus() == MotorStatus.BUSY)
 			throw new MotorException(getStatus(), "Motor " + name + " moveTo aborted because previous move not yet completed");
 		try {
 			smc.moveTo(position);
 		} catch (DeviceException e) {
 			throw new MotorException(MotorStatus.FAULT,"Motor " + name + " error moving motor to position " + position,e);
 		}
 	}
 
 	@Override
 	@MethodAccessProtected(isProtected = true)
 	public void moveContinuously(int direction) throws MotorException {
 		throw new MotorException(MotorStatus.UNKNOWN, "moveContinuous is not supported");
 	}
 
 	@Override
 	@MethodAccessProtected(isProtected = true)
 	public void setPosition(double steps) throws MotorException {
 		throw new MotorException(MotorStatus.UNKNOWN, "setPosition is not supported");
 	}
 
 	@Override
 	public double getPosition() throws MotorException {
 		try {
 			return smc.getMotorPosition();
 		} catch (DeviceException e) {
 			throw new MotorException(MotorStatus.FAULT,"Motor " + name + " error reading motor position",e);
 		}
 	}
 
 	@Override
 	public void setSpeed(double speed) throws MotorException {
 		try {
 			smc.setSpeed(speed);
		} catch (DeviceException e) {
 			throw new MotorException(MotorStatus.FAULT,"Motor " + name + " error setting motor speed",e);
 		}
 	}
 
 	@Override
 	public void setSpeedLevel(int level) throws MotorException {
 		//not supported
 	}
 
 	@Override
 	public double getSpeed() throws MotorException {
 		try {
 			return smc.getSpeed();
 		} catch (DeviceException e) {
 			throw new MotorException(MotorStatus.FAULT,"Motor " + name + " error reading motor speed",e);
 		}
 	}
 
 	@Override
 	public double getTimeToVelocity() throws MotorException {
 		//not supported
 		return 0;
 	}
 
 	@Override
 	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
 		//not supported
 	}
 
 	@Override
 	public void stop() throws MotorException {
 		try {
 			smc.stop();
 		} catch (DeviceException e) {
 			throw new MotorException(MotorStatus.FAULT,"Motor " + name + " error stopping motor",e);
 		}
 	}
 
 	@Override
 	public void panicStop() throws MotorException {
 		stop();
 	}
 
 	@Override
 	public MotorStatus getStatus() throws MotorException {
 		try {
 			return smc.isBusy() ? MotorStatus.BUSY : MotorStatus.READY;
 		} catch (DeviceException e) {
 			throw new MotorException(MotorStatus.FAULT,"Error getting status from motor " + name,e);
 		}
 	}
 
 	@Override
 	public void correctBacklash() throws MotorException {
 		//not supported
 	}
 
 	@Override
 	public boolean isMoving() throws MotorException {
 		return getStatus() == MotorStatus.BUSY;
 	}
 
 	@Override
 	public boolean isHomeable() throws MotorException {
 		return false;
 	}
 
 	@Override
 	public boolean isHomed() throws MotorException {
 		return true;
 	}
 
 	@Override
 	public void home() throws MotorException {
 		//not supported
 	}
 
 	@Override
 	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException {
 		this.minPosition=minPosition;
 		this.maxPosition=maxPosition;
 	}
 
 	double minPosition=-Double.MAX_VALUE;
 	@Override
 	public double getMinPosition() throws MotorException {
 		return minPosition;
 	}
 
 	double maxPosition=Double.MAX_VALUE;
 	@Override
 	public double getMaxPosition() throws MotorException {
 		return maxPosition;
 	}
 
 	
 	
 
 	public SimpleMotorController getSmc() {
 		return smc;
 	}
 
 	public void setSmc(SimpleMotorController smc) {
 		this.smc = smc;
 	}
 
 	public void setMinPosition(double minPosition) {
 		this.minPosition = minPosition;
 	}
 
 	public void setMaxPosition(double maxPosition) {
 		this.maxPosition = maxPosition;
 	}
 
 	@Override
 	public boolean isLimitsSettable() throws MotorException {
 		return false;
 	}
 
 	@Override
 	public boolean isInitialised() throws MotorException {
 		return false;
 	}
 
 	@Override
 	public double getRetryDeadband() throws MotorException {
 		return 0;
 	}
 
 	double resolution=.001; //1 micron
 	
 	@Override
 	public double getMotorResolution() throws MotorException {
 		return resolution;
 	}
 
 	@Override
 	public double getUserOffset() throws MotorException {
 		return 0;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if( smc == null)
 			throw new Exception("smc is null");
 		if( !StringUtils.hasText(name))
 			throw new Exception("name is not set");
 		
 	}
 
 }
