 /*
  * This file is part of LinkJVM.
  *
  * Java Framework for the KIPR Link
  * Copyright (C) 2014 Markus Klein<m@mklein.co.at>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package linkjvm.low.factory;
 
 public class JNIController implements Runnable{
 
 	static {
 		System.loadLibrary("linkjvmjni");
 	}
 	
 	private final static JNIController instance = new JNIController();
 	
 	public static JNIController getInstance(){
 		instance.startCleanup();
 		return instance;
 	}
 	
 	private final CreateFactory createFactory;
 	private final DepthFactory depthFactory;
 	private final UsbInputProviderFactory usbInputProviderFactory;
 	
 	private final AccelerationFactory accelerationFactory;
 	private final AnalogFactory analogFactory;
 	private final Analog8Factory analog8Factory;
 	private final ButtonFactory buttonFactory;
 	private final DigitalFactory digitalFactory;
 	private final MotorFactory motorFactory;
 	private final ServoFactory servoFactory;
 	private final CameraFactory cameraFactory;
 	private final ConfigFactory configFactory;
 	
 	private volatile boolean stopCleanup = false;
 	
 	private Thread cleanupThread;
 	
 	/**
 	 * 
 	 */
 	private JNIController(){
 		accelerationFactory = new AccelerationFactory();
 		analog8Factory = new Analog8Factory();
 		analogFactory = new AnalogFactory();
 		buttonFactory = new ButtonFactory();
 		digitalFactory = new DigitalFactory();
 		motorFactory = new MotorFactory();
 		servoFactory = new ServoFactory();
 		cameraFactory = new CameraFactory();
 		configFactory = new ConfigFactory();
 		
 		createFactory = new CreateFactory();
 		depthFactory = new DepthFactory();
 		usbInputProviderFactory = new UsbInputProviderFactory();
 	}
 
 	@Override
 	public void run() {
 		stopCleanup = false;
 		while(!stopCleanup){
 			accelerationFactory.cleanup();
 			analog8Factory.cleanup();
 			analogFactory.cleanup();
 			buttonFactory.cleanup();
 			digitalFactory.cleanup();
 			motorFactory.cleanup();
 			servoFactory.cleanup();
 		}		
 	}
 	
 	/**
 	 * 
 	 */
 	public void stopCleanup(){
 		stopCleanup = true;
 	}
 	
 	/**
 	 * 
 	 */
 	public void startCleanup(){
		if(cleanupThread == null){
 			cleanupThread = new Thread(this);
		}
		if(!cleanupThread.isAlive()){
 			cleanupThread.start();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public CreateFactory getCreateFactory (){
 		return createFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public AccelerationFactory getAccelerationFactory(){
 		return accelerationFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public AnalogFactory getAnalogFactory(){
 		return analogFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Analog8Factory getAnalog8Factory(){
 		return analog8Factory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ButtonFactory getButtonFactory(){
 		return buttonFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public DigitalFactory getDigitalFactory(){
 		return digitalFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public MotorFactory getMotorFactory(){
 		return motorFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ServoFactory getServoFactory(){
 		return servoFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public CameraFactory getCameraFactory(){
 		return cameraFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public DepthFactory getDepthFactory(){
 		return depthFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public UsbInputProviderFactory getUsbInputProviderFactory(){
 		return usbInputProviderFactory;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ConfigFactory getConfigFactory(){
 		return configFactory;
 	}
 }
