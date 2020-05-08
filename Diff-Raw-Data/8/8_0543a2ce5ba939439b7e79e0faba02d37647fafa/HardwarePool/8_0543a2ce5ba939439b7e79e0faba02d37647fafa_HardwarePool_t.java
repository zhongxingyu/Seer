 package com.frc2013.rmr662.system;
 
 import com.frc2013.rmr662.wrappers.RMRDigitalInput;
 import com.frc2013.rmr662.wrappers.RMRSolenoid;
 
 import edu.wpi.first.wpilibj.Jaguar;
 
 /**
  * A pool of Jaguars, RMRSolenoids, RMRDigitalInputs, etc.
  */
 public class HardwarePool {
 	// Singleton stuff
 	private static HardwarePool instance;
 	
 	public synchronized static HardwarePool getInstance() {
 		if (instance == null) {
 			instance = new HardwarePool();
 		}
 		return instance;
 	}
 	
 	// Constants
 	private static final int INITIAL_POOL_SIZE = 12;
 	
 	// Fields
 	private Object[] hardwares;
 	
 	private HardwarePool() {
 		hardwares = new Object[INITIAL_POOL_SIZE];
 	}
 	
 	/**
 	 * Gets a Jaguar on the given channel
 	 * 
 	 * @param channel
 	 * @return
 	 */
 	public synchronized Jaguar getJaguar(int channel) {
 		ensureArrayFits(channel);
 		
 		final Object o = hardwares[channel];
 		if (o == null) { // Need to create a new Jaguar
 			
 			final Jaguar jaguar = new Jaguar(channel);
 			hardwares[channel] = jaguar;
 			return jaguar;
 			
 		} else if (o instanceof Jaguar) { // Jaguar already exists on that channel
 			
 			return (Jaguar) o;
 			
 		} else { // Other type of hardware already exists on that channel
 			
			throw new IllegalStateException("A " + o.getClass().getName()
 					+ " has already been initialized on channel " + channel);
 		}
 	}
 	
 	/**
 	 * Gets an RMRSolenoid on the given channel
 	 * 
 	 * @param channel
 	 * @param inverted
 	 * @return
 	 */
 	public synchronized RMRSolenoid getSolenoid(int channel, boolean inverted) {
 		ensureArrayFits(channel);
 		
 		final Object o = hardwares[channel];
 		if (o == null) { // Need to create a new RMRSolenoid
 			
 			final RMRSolenoid solenoid = new RMRSolenoid(channel, inverted);
 			hardwares[channel] = solenoid;
 			return solenoid;
 			
 		} else if (o instanceof RMRSolenoid) { // RMRSolenoid on that channel already exists
 			
 			final RMRSolenoid solenoid = (RMRSolenoid) o;
 			if (solenoid.inverted != inverted) {
 				System.err
 						.println("The RMRSolenoid that already exists on channel "
 								+ channel
 								+ " does not match the requested inversion.");
 			}
 			return solenoid;
 			
 		} else { // Other type of hardware already exists on that channel
 			
 			throw new IllegalArgumentException("A "
					+ o.getClass().getName()
 					+ " has already been initialized on channel " + channel);
 		}
 	}
 	
 	public synchronized RMRDigitalInput getDigitalInput(int channel,
 			boolean inverted) {
 		ensureArrayFits(channel);
 		
 		final Object o = hardwares[channel];
 		if (o == null) { // Need to create a new RMRDigitalInput
 			
 			final RMRDigitalInput digitalInput = new RMRDigitalInput(channel,
 					inverted);
 			hardwares[channel] = digitalInput;
 			return digitalInput;
 			
 		} else if (o instanceof RMRDigitalInput) { // RMRDigitalInput on that channel already exists
 			
 			final RMRDigitalInput digitalInput = (RMRDigitalInput) o;
 			if (digitalInput.inverted != inverted) {
 				System.err
 						.println("The RMRSolenoid that already exists on channel "
 								+ channel
 								+ " does not match the requested inversion.");
 			}
 			return digitalInput;
 			
 		} else { // Other type of hardware already exists on that channel
 			
			throw new IllegalStateException("A " + o.getClass().getName()
 					+ " has already been initialized on channel " + channel);
 		}
 	}
 	
 	private void ensureArrayFits(int newIndex) {
 		final int tempLength = hardwares.length;
 		if (tempLength <= newIndex) {
 			final Object[] temp = hardwares;
 			hardwares = new Object[newIndex + 1];
 			System.arraycopy(temp, 0, hardwares, 0, tempLength);
 		}
 	}
 }
